#ifndef HEADER_H
#define HEADER_H

#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <string>
#include <cstring>
#include <cstdlib>
#include <cmath>
#include <vector>   
#include <GL/glew.h>
#include <GL/freeglut.h>

using namespace std;

void onDisplay();
void translate(void *data);
void initValues();
void updateCubeFaces();

#endif