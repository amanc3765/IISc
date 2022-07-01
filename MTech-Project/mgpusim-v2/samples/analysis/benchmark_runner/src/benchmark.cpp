#include "main.h"
#include "benchmark.h"
#include "utility.h"

benchmark benchmarkID;
void *benchmarkParams;
extern char *arguments[];
char *argsBenchmark[20];
char *argsSimulator[] = {"-timing", "-parallel", "-report-all", NULL};
int argIndex;

map<benchmark, pair<string, string>> benchmarkMap = {{MATRIX_TRANSPOSE, {"matrixtranspose", ""}},
                                                     {ATAX, {"atax", ""}},
                                                     {BICG, {"bicg", ""}},
                                                     {SPMV, {"spmv", ""}},
                                                     {MATRIX_MULTIPLICATION, {"matrixmultiplication", ""}},
                                                     {RELU, {"relu", ""}},
                                                     {FFT, {"fft", ""}},
                                                     {GEMM, {"gemm", ""}}};

void getBenchmarkID(int argc, char **argv)
{
    char *benchmarkName;
    if (argc < 2)
        panic("\ngetBenchmarkID(): Benchmark name missing.");
    else
        benchmarkName = argv[1];

    benchmarkID = ERROR;
    if (strcmp(benchmarkName, "matrixtranspose") == 0)
        benchmarkID = MATRIX_TRANSPOSE;
    else if (strcmp(benchmarkName, "atax") == 0)
        benchmarkID = ATAX;
    else if (strcmp(benchmarkName, "bicg") == 0)
        benchmarkID = BICG;
    else if (strcmp(benchmarkName, "spmv") == 0)
        benchmarkID = SPMV;
    else if (strcmp(benchmarkName, "matrixmultiplication") == 0)
        benchmarkID = MATRIX_MULTIPLICATION;
    else if (strcmp(benchmarkName, "relu") == 0)
        benchmarkID = RELU;
    else if (strcmp(benchmarkName, "fft") == 0)
        benchmarkID = FFT;
    else if (strcmp(benchmarkName, "gemm") == 0)
        benchmarkID = GEMM;

    if (benchmarkID == ERROR)
        panic("\ngetBenchmarkID(): Benchmark not found.");
}

void getBenchmarkParams(int argc, char **argv)
{
    int currIndex = 0;

    string benchmarkName = benchmarkMap[benchmarkID].first;
    argsBenchmark[currIndex++] = stringToChar(benchmarkName);

    switch (benchmarkID)
    {
    case MATRIX_TRANSPOSE:
        if (argc < 3)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-width";
        argsBenchmark[currIndex++] = argv[2];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2])};
        break;

    case ATAX:
        if (argc < 4)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-x";
        argsBenchmark[currIndex++] = argv[2];
        argsBenchmark[currIndex++] = "-y";
        argsBenchmark[currIndex++] = argv[3];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2]) + "_" + charToString(argv[3])};
        break;

    case BICG:
        if (argc < 4)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-x";
        argsBenchmark[currIndex++] = argv[2];
        argsBenchmark[currIndex++] = "-y";
        argsBenchmark[currIndex++] = argv[3];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2]) + "_" + charToString(argv[3])};

        break;

    case SPMV:
        if (argc < 4)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-dim";
        argsBenchmark[currIndex++] = argv[2];
        argsBenchmark[currIndex++] = "-sparsity";
        argsBenchmark[currIndex++] = argv[3];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2]) + "_" + charToString(argv[3])};

        break;

    case MATRIX_MULTIPLICATION:
        if (argc < 5)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-x";
        argsBenchmark[currIndex++] = argv[2];
        argsBenchmark[currIndex++] = "-y";
        argsBenchmark[currIndex++] = argv[3];
        argsBenchmark[currIndex++] = "-z";
        argsBenchmark[currIndex++] = argv[4];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2]) + "_" + charToString(argv[3]) + "_" + charToString(argv[4])};

        break;

    case RELU:
        if (argc < 3)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-length";
        argsBenchmark[currIndex++] = argv[2];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2])};
        break;

    case FFT:
        if (argc < 4)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-MB";
        argsBenchmark[currIndex++] = argv[2];
        argsBenchmark[currIndex++] = "-passes";
        argsBenchmark[currIndex++] = argv[3];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2]) + "_" + charToString(argv[3])};
        break;

    case GEMM:
        if (argc < 5)
            panic("\ngetBenchmarkParams():  Insufficient parameters.");

        argsBenchmark[currIndex++] = "-k";
        argsBenchmark[currIndex++] = argv[2];
        argsBenchmark[currIndex++] = "-m";
        argsBenchmark[currIndex++] = argv[3];
        argsBenchmark[currIndex++] = "-n";
        argsBenchmark[currIndex++] = argv[4];

        benchmarkMap[benchmarkID] = {benchmarkName, charToString(argv[2]) + "_" + charToString(argv[3]) + "_" + charToString(argv[4])};

        break;
    }
    // cout << "benchmarkMap[benchmarkID].first: " << benchmarkMap[benchmarkID].first << endl;
    // cout << "benchmarkMap[benchmarkID].second: " << benchmarkMap[benchmarkID].second << endl;

    argsBenchmark[currIndex++] = NULL;
}

char **getArguments(Job *job)
{
    argIndex = 0;
    for (int i = 0; i < 100; ++i)
        arguments[i] = NULL;

    copyIntoArgs(argsBenchmark);

    copyIntoArgs(argsSimulator);

    char *argsGPU[] = {"-flag-num-agg-l1vtlbs", stringToChar(to_string(job->numAggregatedTLBs)),
                       "-flag-num-clusters", stringToChar(to_string(job->numClusters)),
                       "-flag-vivt",
                       NULL};

    if (!job->isVIVT)
        argsGPU[4] = NULL;

    copyIntoArgs(argsGPU);

    // printArguments("arguments", arguments);

    job->fileName = getFileName(job);

    return arguments;
}

void copyIntoArgs(char **argsTemp)
{
    for (int i = 0; argsTemp[i] != NULL; ++i)
        arguments[argIndex++] = argsTemp[i];
}

void printArguments(char *name, char **args)
{
    cout << name << " : ";
    for (int i = 0; args[i] != NULL; ++i)
        cout << args[i] << " ";
    cout << "\n\n";
}

string getFileName(Job *job)
{
    string fileName;

    fileName = benchmarkMap[benchmarkID].first + "/";
    fileName += benchmarkMap[benchmarkID].second + "/";
    fileName += to_string(job->id);
    fileName += "_{" + to_string(job->numAggregatedTLBs);
    fileName += "_" + to_string(job->numClusters) + "}";

    if (job->isVIVT)
        fileName += "_VIVT";
    else
        fileName += "_PIPT";

    return fileName;
}
