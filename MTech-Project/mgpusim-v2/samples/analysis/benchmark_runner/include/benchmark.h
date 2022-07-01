#ifndef BENCHMARK_H
#define BENCHMARK_H

#include "main.h"
#include "job.h"

enum benchmark
{
    MATRIX_TRANSPOSE,
    ATAX,
    BICG,
    SPMV,
    MATRIX_MULTIPLICATION,
    RELU,
    FFT,
    GEMM,
    ERROR
};

void getBenchmarkID(int argc, char **argv);
void getBenchmarkParams(int argc, char **argv);
char **getArguments(Job *job);
void copyIntoArgs(char **argsTemp);
void printArguments(char *name, char **args);
string getFileName(Job *job);

#endif