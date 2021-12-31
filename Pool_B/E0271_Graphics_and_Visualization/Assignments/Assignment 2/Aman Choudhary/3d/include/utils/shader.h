#ifndef SHADER_H
#define SHADER_H

#include "header.h"

string parseShader(const string &filePath);
unsigned int createShader(const string &vertexShaderPath, const string &geometryShaderPath, const string &fragmentShaderPath);
unsigned int compileShader(unsigned int shaderType, const string shaderSource);

#endif