#ifndef MARCHING_TETRA_H
#define MARCHING_TETRA_H

#include "file.h"
#include "math.h"

Vector3f linearInterpolate(ScalarFieldPoint pointA, ScalarFieldPoint pointB,
                           unsigned int currScalarValue);

void trianglesfromTetrahedrons(int pointA, int pointB, int pointC, int pointD);

void processCube(unsigned int cubeOrigin);

#endif