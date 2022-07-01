FLAGS = -L/opt/rocm/opencl/lib/ -lOpenCL -I/opt/rocm/opencl/include/

all: transpose rotate dilate im2col sum_one_axis gemm gemm2

transpose: transpose.c
	gcc -g $^ $(FLAGS) -o $@

rotate: rotate.c
	gcc  -g $^ $(FLAGS) -o $@

dilate: dilate.c
	gcc  -g $^ $(FLAGS) -o $@

im2col: im2col.c
	gcc -g $^ $(FLAGS) -o $@

sum_one_axis: sum_one_axis.c
	gcc -g $^ $(FLAGS) -o $@

gemm: gemm.c
	gcc -g $^ $(FLAGS) -o $@

gemm2: gemm2.c
	gcc -g $^ $(FLAGS) -o $@

