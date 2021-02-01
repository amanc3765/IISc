all: data/input_4096.in data/input_8192.in data/input_16384.in diag_mult

diag_mult: main.cpp header/single_thread.h header/multi_thread.h
	g++ main.cpp -o diag_mult -I ./header -lpthread

data/generate: data/generate.cpp
	g++ ./data/generate.cpp -o ./data/generate

data/input_4096.in: data/generate
	./data/generate 4096 

data/input_8192.in: data/generate
	./data/generate 8192 

data/input_16384.in: data/generate
	./data/generate 16384 

run: data/input_4096.in data/input_8192.in data/input_16384.in diag_mult
	./diag_mult data/input_4096.in
	./diag_mult data/input_8192.in
	./diag_mult data/input_16384.in

clean:
	rm diag_mult
