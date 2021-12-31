CC = export MESA_GL_VERSION_OVERRIDE=3.3 && clear && g++
CFLAGS = -O3 -Wall -w

SRCS = src/*.cpp src/open_gl/*.cpp src/utils/*.cpp
INCLUDE = -I include -I include/utils -I include/open_gl
LIBS = -L. -lGL -lGLU -lglfw3 -lglut -lGLEW -lX11 -lpthread -ldl -lXxf86vm -lXrandr -lXi -lXinerama -lXcursor -lc -lm
TARGET = Problem2b

$(TARGET): clean
	$(CC) $(CFLAGS) \
	$(SRCS) -o $(TARGET) \
	$(INCLUDE) \
	$(LIBS) 

clean:
	rm -rf *.o $(TARGET)