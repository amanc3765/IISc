#ifndef FILE_H
#define FILE_H

#include "header.h"
#include "math.h"

struct ScalarFieldPoint
{
    Vector3f position;
    unsigned char scalarVal;
};

struct ScalarField
{
    int side;
    ScalarFieldPoint *points;
};

ScalarField *readScalarFile(char *scalarFile);
void printScalarField(ScalarField *scalarField, int numPoints);

#endif