/*

Submitted by: AMAN CHOUDHARY [17920] (amanc@iisc.ac.in)

*/

__global__ void multiply(int N, int * matA, int * matB, int * output) {

    //Calculate thread ID
    int tid = blockIdx.x * blockDim.x + threadIdx.x;

    if (tid < 2 * N - 1) {
        int temp = 0;
        int a_i, a_j, b_i, b_j;

        //Initialize indices for matA and matB
        if (tid < N) {
            a_i = 0;
            a_j = tid;
            b_i = tid;
            b_j = N - 1;
        } else {
            a_i = tid - N + 1;
            a_j = N - 1;
            b_i = N - 1;
            b_j = 2 * N - 2 - tid;
        }

        //Perform diagonal multiplication
        while (a_i < N && a_j >= 0) {
            temp += matA[a_i * N + a_j] * matB[b_i * N + b_j];
            ++a_i, --a_j;
            --b_i, --b_j;
        }

        output[tid] = temp;
    }
}

void error_check(const char * s) {
    cudaError_t err = cudaGetLastError();
    if (err != cudaSuccess) {
        printf("Error: (%s) %s \n", s, cudaGetErrorString(err));
        exit(0);
    }
}

void gpuThread(int N, int * matA, int * matB, int * output) {
    int * devA, * devB, * devO;
    int sizeInput = sizeof(int) * (N * N);
    int sizeOutput = sizeof(int) * (2 * N - 1);

    cudaFree(0);

    //Allocate memory on GPU
    cudaMalloc((void ** ) & devA, sizeInput);
    error_check("cudaMallocA");
    cudaMalloc((void ** ) & devB, sizeInput);
    error_check("cudaMallocB");
    cudaMalloc((void ** ) & devO, sizeOutput);
    error_check("cudaMallocO");

    //Copy data CPU -> GPU 
    cudaMemcpy(devA, matA, sizeInput, cudaMemcpyHostToDevice);
    error_check("cudaMemcpyA");
    cudaMemcpy(devB, matB, sizeInput, cudaMemcpyHostToDevice);
    error_check("cudaMemcpyB");

    //Configure thread blocks
    int threads = min(N, 1024);
    int blocks = ceil((2 * N - 1.0) / threads);

    //Launch CUDA kernel
    multiply <<< blocks, threads >>> (N, devA, devB, devO);
    error_check("multiply");

    //Copy data GPU -> CPU
    cudaMemcpy(output, devO, sizeOutput, cudaMemcpyDeviceToHost);
    error_check("cudaMemcpyO");

    //Free memory on GPU
    cudaFree(devA);
    error_check("cudaFreeA");
    cudaFree(devB);
    error_check("cudaFreeB");
    cudaFree(devO);
    error_check("cudaFreeO");
}