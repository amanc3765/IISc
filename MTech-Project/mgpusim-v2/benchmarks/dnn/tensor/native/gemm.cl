__kernel void gemm(int m, int n, int k, float alpha, float beta,
                   const __global float *a, const __global float *b,
                   const __global float *c, __global float *d) {
  const int TILE_SIZE = 16;
  __local float subTileM[TILE_SIZE][TILE_SIZE];
  __local float subTileN[TILE_SIZE][TILE_SIZE];

  int bx = get_global_id(0) / get_local_size(0);
  int by = get_global_id(1) / get_local_size(1);
  int tx = get_local_id(0);
  int ty = get_local_id(1);

  int Row = by * TILE_SIZE + ty;
  int Col = bx * TILE_SIZE + tx;

  float Pvalue = 0;
  for (int i = 0; i < ((k - 1) / TILE_SIZE + 1); i++) {
    // printf("Row %d, Col %d, Tile %d\n", Row, Col, i);
    int curL = Row * k + i * TILE_SIZE + tx;
    int curR = (i * TILE_SIZE + ty) * n + Col;

    if (i * TILE_SIZE + tx < k && Row < m) {
      subTileM[ty][tx] = a[curL];
    } else {
      subTileM[ty][tx] = 0.0;
    }

    if (i * TILE_SIZE + ty < k && Col < n) {
      subTileN[ty][tx] = b[curR];
    } else {
      subTileN[ty][tx] = 0.0;
    }

    barrier(CLK_LOCAL_MEM_FENCE);
    for (int j = 0; j < TILE_SIZE; j++) {
      if (j + TILE_SIZE * i < k) {
        Pvalue += subTileM[ty][j] * subTileN[j][tx];
      }
    }
    barrier(CLK_LOCAL_MEM_FENCE);
  }

  if (Row < m && Col < n) {
    d[Row * n + Col] = alpha * Pvalue + beta * c[Row * n + Col];
  }
}

__kernel void gemm_mul(int m, int n, int k, int num_supertile,
                       int super_tile_width, const __global float *a,
                       const __global float *b, __global float *out) {
  const int TILE_SIZE = 16;
  __local float As[TILE_SIZE][TILE_SIZE];
  __local float Bs[TILE_SIZE][TILE_SIZE];

  int row_width = ((n-1) / TILE_SIZE + 1) * TILE_SIZE * num_supertile;

  int out_col_id = get_global_id(0);
  int out_row_id = get_global_id(1);

  int local_x = get_local_id(0);
  int local_y = get_local_id(1);

  int in_col_id = out_col_id / TILE_SIZE / num_supertile;
  int depth_id  = out_col_id / TILE_SIZE % num_supertile;

  int iters = (k-1) / super_tile_width + 1;
  float sum = 0;
  for (int i = 0; i < iters; i ++) {
    int offset = (depth_id + i * num_supertile) * TILE_SIZE;
    if (out_row_id < m && offset + local_x < k)
      As[local_y][local_x] = a[out_row_id * k + offset + local_x];
    else
      As[local_y][local_x] = 0;
    if (offset + local_y < k && in_col_id * TILE_SIZE + local_x < n)
      Bs[local_y][local_x] = b[(offset + local_y) * n + in_col_id * TILE_SIZE + local_x];
    else
      Bs[local_y][local_x] = 0;
    barrier(CLK_LOCAL_MEM_FENCE);
    for (int j = 0; j < TILE_SIZE; j++) {
      sum += As[local_y][j] * Bs[j][local_x];
    }
    barrier(CLK_LOCAL_MEM_FENCE);
  }
  out[out_row_id * row_width + out_col_id] = sum;
}

__kernel void gemm_sum(int m, int n, int k, float alpha, float beta,
                       int num_supertile, const __global float *super_tiles,
                       __global float *c, __global float *d) {
  const int TILE_SIZE = 16;
  
  int col = get_global_id(0);
  int row = get_global_id(1);

  int local_x = get_local_id(0);
  
  int offset = ((n-1) / TILE_SIZE + 1) * TILE_SIZE * num_supertile * row;
  int block_offset = col / TILE_SIZE * num_supertile * TILE_SIZE;

  float sum = 0;
  for (int i = 0; i < num_supertile; i ++) {
    sum += super_tiles[offset + block_offset + i * TILE_SIZE + local_x];
  }
  if (col < n)
    d[row * n + col] = alpha * sum + beta * c[row * n + col];
}

__kernel void gemm_old(int m, int n, int k, float alpha, float beta,
                       const __global float *a, const __global float *b,
                       const __global float *c, __global float *d) {
  int x = get_global_id(0);
  int y = get_global_id(1);

  if (y >= m || x >= n) {
    return;
  }

  float acc = 0;
  for (int z = 0; z < k; z++) {
    acc += alpha * a[y * k + z] * b[z * n + x];
  }

  d[y * n + x] = acc + beta * c[y * n + x];
}
