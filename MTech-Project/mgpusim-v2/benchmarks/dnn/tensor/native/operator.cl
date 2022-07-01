int numElement(__global int* size, int dim) {
  int s = 1;

  for (int i = 0; i < dim; i++) {
    s *= size[i];
  }

  return s;
}

void unflatIndex(__global int* nd_index, int flat_index, __global int* size,
                 int dim) {
  int i;
  int total_size = numElement(size, dim);

  for (i = 0; i < dim; i++) {
    total_size /= size[i];
    int pos = flat_index / total_size;
    flat_index -= pos * total_size;

    nd_index[i] = pos;
  }
}

int flatIndex(__global int* nd_index, __global int* size, int dim) {
  int out = 0;
  int total_size = 1;

  for (int i = 0; i < dim; i++) {
    out += nd_index[dim - i - 1] * total_size;
    total_size *= size[dim - i - 1];
  }

  return out;
}

__kernel void transpose_tensor(__global float* in, __global float* out,
                               __global int* in_size, __global int* out_size,
                               __global int* order, __global int* in_index_buf,
                               __global int* out_index_buf, const int dim) {
  int tid = get_global_id(0);

  __global int* nd_in_index = in_index_buf + tid * dim;
  __global int* nd_out_index = out_index_buf + tid * dim;

  unflatIndex(nd_out_index, tid, out_size, dim);

  for (int i = 0; i < dim; i++) {
    nd_in_index[order[i]] = nd_out_index[i];
  }

  int input_index_flat = flatIndex(nd_in_index, in_size, dim);

  out[tid] = in[input_index_flat];
}

__kernel void convert_nchw_to_cnhw_large(__global float* in, __global float* out,
                                   int size_x, int size_y, int size_z, int size_w,
                                   int blockSize) {                             
  // Viewing x-y matrix as a single element of w-z matrix
  // simplifies w-z-y-x tensor to w-z matrix. 
  // As such, we just have to make z-w matrix
  int in_wz = get_group_id(0);     

  int in_z = in_wz % size_z;      // z is columns of w-z matrix 
  int in_w = in_wz / size_z;     // w is rows of w-z matrix

  int index = get_local_id(0);

  int size_xy = size_x * size_y;  //size of x-y matrix

  for (int i=index; i<size_xy; i+=blockSize) {
    if (i < size_xy) {
      int x = i % size_x;
      int y = i / size_x;
      float input = in[in_w*size_z*size_xy+in_z*size_xy+y*size_x+x];
      out[in_z*size_w*size_xy+in_w*size_xy+y*size_x+x] = input;
    }
  }
}

__kernel void convert_nchw_to_cnhw_small(__global float* in, __global float* out,
                                   int size_x, int size_y, int size_z, int size_w,
                                   int numTilesZ, int numTilesW) {  
  __local float tile[272]; // [16][17] matrix

  // Viewing x-y matrix as a single element of w-z matrix
  // simplifies w-z-y-x tensor to w-z matrix. 
  // As such, we just have to make z-w matrix
  int group_id = get_group_id(0);
  int index = get_local_id(0);
  int size_xy = size_x * size_y;  //size of x-y matrix
  int offset_wz = index / size_xy;
  int localZ = offset_wz % numTilesZ;
  int localW = offset_wz / numTilesZ;
  
  int numZ = (size_z - 1) / numTilesZ + 1;
  // z is columns of w-z matrix 
  int globalZ = (group_id % numZ) * numTilesZ + localZ;
  // w is rows of w-z matrix 
  int globalW = (group_id / numZ) * numTilesW + localW;

  int xy = index % size_xy;
  int x = xy % size_x;
  int y = xy / size_x;
  // int offset = index / 16;
  bool inBoundaries = globalW < size_w && globalZ < size_z && y < size_y && x < size_x;
  if (index < numTilesZ * numTilesW * size_xy && inBoundaries) {
    int inLocal = localW*numTilesZ*size_xy+localZ*size_xy+y*size_x+x;
    int inGlobal = globalW*size_z*size_xy+globalZ*size_xy+y*size_x+x;
    tile[inLocal] = in[inGlobal];
  }
  barrier(CLK_LOCAL_MEM_FENCE);
  int outLocalZ = offset_wz % numTilesW;
  int outLocalW = offset_wz / numTilesW;
  int outGlobalW = globalZ - localZ + outLocalW;
  int outGlobalZ = globalW - localW + outLocalZ;
  bool outBoundaries = outGlobalW < size_z && outGlobalZ < size_w && y < size_y && x < size_x;
  if (index < numTilesZ * numTilesW * size_xy && outBoundaries) {
    int out_id = outGlobalW*size_w*size_xy+outGlobalZ*size_xy+y*size_x+x;
    int out_tile_id = outLocalZ*numTilesZ*size_xy+outLocalW*size_xy+y*size_x+x;
    out[out_id] = tile[out_tile_id];
  }
}

__kernel void rotate_tensor(__global float* in, __global float* out,
                            int vol_h, int vol_w, int vol_other) { // vol_other - volume of other dims
  const int blockSize = 256;
  int threadIndex = get_global_id(0);
  int stride = get_num_groups(0) * blockSize;

  int vol_hw = vol_h * vol_w;

  int numBlocks = (vol_hw - 1) / blockSize + 1;
  int roundedTotalSize = vol_other * numBlocks * blockSize;
  int roundedMatrixSize = blockSize * numBlocks;
  for (int i = threadIndex; i < roundedTotalSize; i += stride) {
    int hwIndex = i % roundedMatrixSize;
    int groupIndex = i / roundedMatrixSize;
    int inIndex = groupIndex * vol_hw + hwIndex;
    if (hwIndex < vol_hw) {
      int outIndex = inIndex - hwIndex + vol_hw - 1 - hwIndex;
      out[outIndex] = in[inIndex];
    }
  }
}

__kernel void rotate_tensor_small(__global float* in, __global float* out,
                            int vol_h, int vol_w, int numMatrices) { // numMatrices instead of vol_other
  const int blockSize = 256;
  __local float tile[blockSize];

  int vol_hw = vol_h * vol_w;
  int tileIndex = get_group_id(0);
  int localIndex = get_local_id(0);

  int inIndex = tileIndex * numMatrices * vol_hw + localIndex;
  bool cond = localIndex < numMatrices * vol_hw;
  if (cond) {
    tile[localIndex] = in[inIndex];
  }
  barrier(CLK_LOCAL_MEM_FENCE);
  if (cond) {
    int hwIndex = localIndex % vol_hw;
    out[inIndex] = tile[localIndex - hwIndex + vol_hw - 1 - hwIndex];
  }
}

__kernel void dilate_tensor(__global float* in, __global float* out,   
                            int inVolH, int inVolW, int outVolH, int outVolW,
                            int dilateX, int dilateY) {

  int tid = get_global_id(0);
  int elemIndex = tid % (outVolH * outVolW);
  int hwMatrixIndex = tid / (outVolH * outVolW);
  int hIndex = elemIndex / outVolW;
  int wIndex = elemIndex % outVolW;
  float data = 0;
  if (hIndex % dilateY == 0 && wIndex % dilateX == 0) {
    data = in[hwMatrixIndex*(inVolH*inVolW) + 
              (hIndex/dilateY)*inVolW+wIndex/dilateX];
  }
  out[tid] = data;
}

__kernel void dilate_zero_tensor(__global float* in, __global float* out,       // assumed that out matrix is initially
                            int inVolH, int inVolW, int outVolH, int outVolW,   // a zero matrix
                            int dilateX, int dilateY) {
  int tid = get_global_id(0);
  float data = in[tid];

  int hwMatrixIndex = tid / (inVolH * inVolW);
  int elemIndex = tid % (inVolH * inVolW);
  int xIndex = elemIndex % inVolW;
  int yIndex = elemIndex / inVolW;
  out[hwMatrixIndex*(outVolH*outVolW)+yIndex*dilateY*outVolW+xIndex*dilateX] = data;
}

__kernel void softmax_exp(__global float* input, __global float* output,
                          int n) {
  uint tid = get_global_id(0);

  if (tid >= n) {
    return;
  }

  output[tid] = exp(input[tid]);
}

__kernel void softmax_div(__global float* exp_input, __global float* out,
                          __global float* denominator, int num_element,
                          int batch_size) {
  int tid = get_global_id(0);

  if (tid > num_element) {
    return;
  }

  int num_element_per_image = num_element / batch_size;
  int batch = tid / num_element_per_image;
  out[tid] = exp_input[tid] / denominator[batch];
}

void sum_out_index_to_in_index(__global int* nd_out_index,
                               __global int* nd_in_index, int index, int axis,
                               int in_dim) {
  int axis_index_added = false;
  for (int i = 0; i < in_dim; i++) {
    if (i == axis) {
      nd_in_index[i] = index;
      axis_index_added = true;
    } else if (!axis_index_added) {
      nd_in_index[i] = nd_out_index[i];
    } else {
      nd_in_index[i] = nd_out_index[i - 1];
    }
  }
}

__kernel void sum_one_axis(__global float* in, __global float* out,
                           __global int* in_size, __global int* out_size,
                           int in_dim, int axis, __global int* in_index_buf,
                           __global int* out_index_buf) {
  int global_id = get_global_id(0);

  __global int* nd_in_index = in_index_buf + global_id * in_dim;
  __global int* nd_out_index = out_index_buf + global_id * (in_dim - 1);

  unflatIndex(nd_out_index, global_id, out_size, in_dim - 1);

  float sum = 0.0;
  for (int i = 0; i < in_size[axis]; i++) {
    sum_out_index_to_in_index(nd_out_index, nd_in_index, i, axis, in_dim);
    int in_flat_index = flatIndex(nd_in_index, in_size, in_dim);
    sum += in[in_flat_index];
  }

  out[global_id] = sum;
}

__kernel void sum_nhw_in_cnhw(__global float* in, __global float* out,
                           int dim1, int dim2, int dim3) {
  const int blockSize = 128;
  int globalC = get_group_id(0);
  int index = get_local_id(0);
  int totalSize = dim1 * dim2 * dim3;
  float data = 0;
  int offset = globalC * totalSize;
  for (int i=index; i<totalSize; i+=blockSize) {
    data += in[offset + i];
  }               
  __local float sum[blockSize];
  sum[index] = data;
  barrier(CLK_LOCAL_MEM_FENCE);
  if (index < 64) { sum[index] += sum[index+64]; } barrier(CLK_LOCAL_MEM_FENCE);
  if (index < 32) { sum[index] += sum[index+32]; } barrier(CLK_LOCAL_MEM_FENCE);
  if (index < 16) { sum[index] += sum[index+16]; } barrier(CLK_LOCAL_MEM_FENCE);
  if (index < 8) { sum[index] += sum[index+8]; } barrier(CLK_LOCAL_MEM_FENCE);
  if (index < 4) { sum[index] += sum[index+4]; } barrier(CLK_LOCAL_MEM_FENCE);
  if (index < 2) { sum[index] += sum[index+2]; } barrier(CLK_LOCAL_MEM_FENCE);
  if (index == 0)
    out[globalC] = sum[index] + sum[index+1];
}

__kernel void scaleAdd(__global float* out, __global float* in1,
                       __global float* in2, float alpha, float beta, int n) {
  int tid = get_global_id(0);
  if (tid > n) {
    return;
  }

  out[tid] = alpha * in1[tid] + beta * in2[tid];
}

__kernel void mul(__global float* out, __global float* in1, __global float* in2,
                  int n) {
  int tid = get_global_id(0);
  if (tid > n) {
    return;
  }

  out[tid] = in1[tid] * in2[tid];
}

__kernel void rmsProp(__global float* params, __global float* gradients,
                      __global float* sHistory, float smoothFactor,
                      float learningRate, int n) {
  int tid = get_global_id(0);
  if (tid > n) {
    return;
  }
  sHistory[tid] = smoothFactor * sHistory[tid] +
                  (1 - smoothFactor) * gradients[tid] * gradients[tid];

  float sqrt_shistory = sqrt(sHistory[tid]) + 1e-6;
  float direction = gradients[tid] / sqrt_shistory;
  params[tid] -= learningRate * direction;
}

__kernel void adam(__global float* params, __global float* gradients,
                   __global float* sHistory, __global float* vHistory,
                   float smoothFactor1, float smoothFactor2, float learningRate,
                   int n) {
  int tid = get_global_id(0);
  if (tid > n) {
    return;
  }

  float vHistoryPart1 = smoothFactor1 * vHistory[tid];
  float vHistoryPart2 = (1 - smoothFactor1) * gradients[tid];
  vHistory[tid] = vHistoryPart1 + vHistoryPart2;
  sHistory[tid] = smoothFactor2 * sHistory[tid] +
                  (1 - smoothFactor2) * gradients[tid] * gradients[tid];

  float squareRoot = (sqrt(sHistory[tid]) + 1e-8);
  float direction = vHistory[tid] / squareRoot;
  params[tid] -= learningRate * direction;
}

__kernel void reluForward(__global float* in, __global float* out, int count) {
  int index = get_global_id(0);

  if (index >= count) {
    return;
  }

  out[index] = in[index] > 0 ? in[index] : 0;
}

__kernel void reluBackward(__global float* in, __global float* backin,
                           __global float* out, int count) {
  int index = get_global_id(0);
  if (index >= count) {
    return;
  }

  out[index] = in[index] > 0 ? backin[index] : 0;
}