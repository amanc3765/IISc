sim: data/input_128.in diag_mult_sim

server: data/input_128.in diag_mult_server

diag_mult_server: main.cu header/gpu_thread.h
	nvcc main.cu -o diag_mult_server -I ./header

diag_mult_sim: main.cu header/gpu_thread.h
	nvcc main.cu -o diag_mult -I ./header --cudart shared -gencode arch=compute_70,code=compute_70

data/generate: data/generate.cpp
	g++ ./data/generate.cpp -o ./data/generate

data/input_128.in: data/generate
	./data/generate 128

run_sim: data/input_128.in diag_mult_sim
	./diag_mult_sim data/input_128.in
	
run_server: data/input_128.in diag_mult_server
	./diag_mult_server data/input_128.in

clean:
	rm -f diag_mult_sim diag_mult_server
