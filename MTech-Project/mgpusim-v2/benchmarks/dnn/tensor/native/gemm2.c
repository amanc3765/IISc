#include <CL/opencl.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

uint64_t getTimeInNSecs() {
  struct timespec time;
  clock_gettime(CLOCK_MONOTONIC, &time);
  uint64_t timeInSec = time.tv_sec * 1e9 + time.tv_nsec;
  return timeInSec;
}

float f32abs(float a) {
  if (a < 0) {
    return -a;
  }

  return a;
}

void dump_gpu_matrix(cl_command_queue queue, cl_mem matrix, int rows,
                     int cols) {
  float *cpu_matrix;
  cpu_matrix = (float *)malloc(rows * cols * sizeof(float));

  clEnqueueReadBuffer(queue, matrix, CL_TRUE, 0, rows * cols * sizeof(float),
                      cpu_matrix, 0, NULL, NULL);
  clFinish(queue);

  for (int i = 0; i < rows; i++) {
    for (int j = 0; j < cols; j++) {
      printf("%f ", cpu_matrix[i * cols + j]);
    }
    printf("\n");
  }

  free(cpu_matrix);
}

char *read_file(const char *filename) {
  char *buffer = 0;
  long length;
  FILE *f = fopen(filename, "rb");

  if (f) {
    fseek(f, 0, SEEK_END);
    length = ftell(f);
    fseek(f, 0, SEEK_SET);
    buffer = (char *)(malloc(length));
    if (buffer) {
      fread(buffer, 1, length, f);
    }
    fclose(f);
  }

  return buffer;
}

int main(int argc, char *argv[]) {
  int m = 6;
  int n = 25;
  int k = 36864;
  printf("Sizes m=%d, n=%d, k=%d\n", m, n, k);

  float alpha = 0.4;
  float beta = 0.3;
  int num_supertile = 512;

  float *h_input_a;
  float *h_input_b;
  float *h_outer;
  float *h_input_c;
  float *cpu_out;
  float *gpu_out;

  h_input_a = malloc(m * k * sizeof(float));
  h_input_b = malloc(n * k * sizeof(float));
  h_input_c = malloc(m * n * sizeof(float));
  cpu_out = malloc(m * n * sizeof(float));
  gpu_out = malloc(m * n * sizeof(float));

  for (int i = 0; i < m * k; i++) {
    h_input_a[i] = (float)rand() / (float)RAND_MAX;
    // h_input_a[i] = i;
  }

  for (int i = 0; i < n * k; i++) {
    h_input_b[i] = (float)rand() / (float)RAND_MAX;
    // h_input_b[i] = i;
  }

  for (int i = 0; i < m * n; i++) {
    h_input_c[i] = (float)rand() / (float)RAND_MAX;
    // h_input_c[i] = i;
  }

  for (int x = 0; x < n; x++) {
    for (int y = 0; y < m; y++) {
      float sum = 0;
      for (int i = 0; i < k; i++) {
        sum += h_input_a[y * k + i] * h_input_b[i * n + x];
      }

      cpu_out[y * n + x] = beta * h_input_c[y * n + x] + alpha * sum;
    }
  }

  // Device input buffers
  cl_mem d_input_a;
  cl_mem d_input_b;
  cl_mem d_outer;
  cl_mem d_input_c;
  cl_mem d_output;

  cl_platform_id cpPlatform;         // OpenCL platform
  cl_device_id device_id;            // device ID
  cl_context context;                // context
  cl_command_queue queue;            // command queue
  cl_program program;                // program
  cl_kernel kernel_mul, kernel_sum;  // kernel

  int tile_size = 16;
  int tile_x_per_supertile = (n - 1) / tile_size + 1;
  int tile_y_per_supertile = (m - 1) / tile_size + 1;
  int super_tile_width = tile_x_per_supertile * tile_size;
  size_t globalSize[2] = {tile_x_per_supertile * num_supertile * tile_size,
                          tile_y_per_supertile * tile_size};
  size_t localSize[2] = {tile_size, tile_size};
  cl_int err;

  // Print global size and local size
  printf("Global size: %ld x %ld\n", (long)globalSize[0], (long)globalSize[1]);
  printf("Local size: %ld x %ld\n", (long)localSize[0], (long)localSize[1]);

  // Bind to platform
  err = clGetPlatformIDs(1, &cpPlatform, NULL);
  if (err != CL_SUCCESS) {
    printf("fail to get platform IDs");
    exit(1);
  }

  // Get ID for the device
  err = clGetDeviceIDs(cpPlatform, CL_DEVICE_TYPE_GPU, 1, &device_id, NULL);
  if (err != CL_SUCCESS) {
    printf("fail to get device IDs");
    exit(1);
  }

  // Create a context
  context = clCreateContext(0, 1, &device_id, NULL, NULL, &err);
  if (err != CL_SUCCESS) {
    printf("fail to get create context");
    exit(1);
  }

  // Create a command queue
  queue = clCreateCommandQueue(context, device_id, 0, &err);
  if (err != CL_SUCCESS) {
    printf("fail to get create queue");
    exit(1);
  }

  // Create the compute program from the source buffer
  char *kernelSource = read_file("gemm.cl");
  program = clCreateProgramWithSource(context, 1, (const char **)&kernelSource,
                                      NULL, &err);
  if (err != CL_SUCCESS) {
    printf("fail to create program with source");
    exit(1);
  }

  // Build the program executable
  err = clBuildProgram(program, 0, NULL, NULL, NULL, NULL);
  if (err != CL_SUCCESS) {
    printf("fail to build program");
    if (err == CL_BUILD_PROGRAM_FAILURE) {
      // Determine the size of the log
      size_t log_size;
      clGetProgramBuildInfo(program, device_id, CL_PROGRAM_BUILD_LOG, 0, NULL,
                            &log_size);

      // Allocate memory for the log
      char *log = (char *)malloc(log_size);

      // Get the log
      clGetProgramBuildInfo(program, device_id, CL_PROGRAM_BUILD_LOG, log_size,
                            log, NULL);

      // Print the log
      printf("%s\n", log);
    }
    exit(1);
  }

  kernel_mul = clCreateKernel(program, "gemm_mul", &err);
  if (err != CL_SUCCESS) {
    printf("fail to create kernel %d\n", err);
    exit(1);
  }

  kernel_sum = clCreateKernel(program, "gemm_sum", &err);
  if (err != CL_SUCCESS) {
    printf("fail to create kernel %d\n", err);
    exit(1);
  }

  // Create the input and output arrays in device memory for our calculation
  d_input_a = clCreateBuffer(context, CL_MEM_READ_ONLY, sizeof(float) * m * k,
                             NULL, NULL);
  d_input_b = clCreateBuffer(context, CL_MEM_READ_ONLY, sizeof(float) * n * k,
                             NULL, NULL);
  d_input_c = clCreateBuffer(context, CL_MEM_READ_ONLY, sizeof(float) * m * n,
                             NULL, NULL);
  size_t d_outer_size = sizeof(float) * m * (n * num_supertile);
  printf("d_outer_size %d x %d = %ld\n", n * num_supertile, m, d_outer_size);
  d_outer =
      clCreateBuffer(context, CL_MEM_READ_WRITE, d_outer_size, NULL, NULL);
  d_output = clCreateBuffer(context, CL_MEM_WRITE_ONLY, sizeof(float) * m * n,
                            NULL, NULL);

  // Write our data set into the input array in device memory
  err = clEnqueueWriteBuffer(queue, d_input_a, CL_TRUE, 0,
                             m * k * sizeof(float), h_input_a, 0, NULL, NULL);
  err = clEnqueueWriteBuffer(queue, d_input_b, CL_TRUE, 0,
                             n * k * sizeof(float), h_input_b, 0, NULL, NULL);
  err = clEnqueueWriteBuffer(queue, d_input_c, CL_TRUE, 0,
                             m * n * sizeof(float), h_input_c, 0, NULL, NULL);
  if (err != CL_SUCCESS) {
    printf("fail to enqueue write buffer %d", err);
    exit(1);
  }

  // Set the arguments to our compute kernel
  err = clSetKernelArg(kernel_mul, 0, sizeof(cl_int), &m);
  err |= clSetKernelArg(kernel_mul, 1, sizeof(cl_int), &n);
  err |= clSetKernelArg(kernel_mul, 2, sizeof(cl_int), &k);
  err |= clSetKernelArg(kernel_mul, 3, sizeof(cl_int), &num_supertile);
  err |= clSetKernelArg(kernel_mul, 4, sizeof(cl_int), &super_tile_width);
  err |= clSetKernelArg(kernel_mul, 5, sizeof(cl_mem), &d_input_a);
  err |= clSetKernelArg(kernel_mul, 6, sizeof(cl_mem), &d_input_b);
  err |= clSetKernelArg(kernel_mul, 7, sizeof(cl_mem), &d_outer);
  if (err != CL_SUCCESS) {
    printf("fail to set kernel arguments %d\n", err);
  }

  err = clSetKernelArg(kernel_sum, 0, sizeof(cl_int), &m);
  err |= clSetKernelArg(kernel_sum, 1, sizeof(cl_int), &n);
  err |= clSetKernelArg(kernel_sum, 2, sizeof(cl_int), &k);
  err |= clSetKernelArg(kernel_sum, 3, sizeof(float), &alpha);
  err |= clSetKernelArg(kernel_sum, 4, sizeof(float), &beta);
  err |= clSetKernelArg(kernel_sum, 5, sizeof(cl_int), &num_supertile);
  err |= clSetKernelArg(kernel_sum, 6, sizeof(cl_mem), &d_outer);
  err |= clSetKernelArg(kernel_sum, 7, sizeof(cl_mem), &d_input_c);
  err |= clSetKernelArg(kernel_sum, 8, sizeof(cl_mem), &d_output);
  if (err != CL_SUCCESS) {
    printf("fail to set kernel arguments %d\n", err);
  }

  // Execute the kernel over the entire range of the data set
  uint64_t start = getTimeInNSecs();
  err = clEnqueueNDRangeKernel(queue, kernel_mul, 2, NULL, globalSize,
                               localSize, 0, NULL, NULL);
  if (err != CL_SUCCESS) {
    printf("fail to enqueue ND Range Kernel\n");
    exit(1);
  }

  size_t sum_global_size[2] = {n, m};
  size_t sum_local_size[2] = {16, 16};
  err = clEnqueueNDRangeKernel(queue, kernel_sum, 2, NULL, sum_global_size,
                               sum_local_size, 0, NULL, NULL);
  if (err != CL_SUCCESS) {
    printf("fail to enqueue ND Range Kernel\n");
    exit(1);
  }

  // Wait for the command queue to get serviced before reading back results
  err = clFinish(queue);
  uint64_t end = getTimeInNSecs();
  if (err != CL_SUCCESS) {
    printf("fail to finish");
    exit(1);
  }

  printf("Time %ld\n", end - start);

  //   printf("Matrix A\n");
  //   dump_gpu_matrix(queue, d_input_a, m, k);

  //   printf("Matrix B\n");
  //   dump_gpu_matrix(queue, d_input_b, k, n);

  //   printf("Matrix C\n");
  //   dump_gpu_matrix(queue, d_input_c, m, n);

  //   printf("Outer Matrix\n");
  //   dump_gpu_matrix(queue, d_outer, m, n * num_supertile);

  //   printf("Output Matrix\n");
  //   dump_gpu_matrix(queue, d_output, m, n);

  //   Read the results from the device
  clEnqueueReadBuffer(queue, d_output, CL_TRUE, 0, m * n * sizeof(float),
                      gpu_out, 0, NULL, NULL);
  err = clFinish(queue);
  if (err != CL_SUCCESS) {
    printf("fail to read buffer");
    exit(1);
  }

  for (int y = 0; y < m; y++) {
    for (int x = 0; x < n; x++) {
      if (f32abs(cpu_out[y * n + x] - gpu_out[y * n + x]) >
          1e-5 * cpu_out[y * n + x]) {
        printf("Error at (%d, %d), expedted %f, but get %f\n", x, y,
               cpu_out[y * n + x], gpu_out[y * n + x]);
      }
    }
  }

  // release OpenCL resources
  clReleaseProgram(program);
  clReleaseKernel(kernel_mul);
  clReleaseKernel(kernel_sum);
  clReleaseCommandQueue(queue);
  clReleaseContext(context);

  return 0;
}
