#ifndef SHADER_H
#define SHADER_H

#include "header.h"

unsigned int createShader();
string parseShader(const string &filePath);
unsigned int compileShader(unsigned int shaderType, const string shaderSource);

#endif