CC = clear && g++
CFLAGS = -std=c++0x -w

SRCS = src/*.cpp 
INCLUDE = -I include 
TARGET = benchmark_runner

$(TARGET): clean
	$(CC) $(CFLAGS) \
	$(SRCS) -o $(TARGET) \
	$(INCLUDE) 

clean:
	rm -rf *.o $(TARGET)