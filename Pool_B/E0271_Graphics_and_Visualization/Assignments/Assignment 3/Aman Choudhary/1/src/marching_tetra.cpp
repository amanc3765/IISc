#include "marching_tetra.h"

extern ScalarField *scalarField;
extern Vector3f *verticesIsoSurface;
extern unsigned int numPointsSide, numPointsPlane, currIsoSurfaceVertex;
extern int currScalarValue;

Vector3f linearInterpolate(ScalarFieldPoint pointA, ScalarFieldPoint pointB,
                           unsigned int currScalarValue) {
  if (pointA.scalarVal > pointB.scalarVal) {
    ScalarFieldPoint temp;
    temp = pointA;
    pointA = pointB;
    pointB = temp;
  }

  Vector3f result;
  result = pointB.position - pointA.position;
  result /= (pointB.scalarVal - pointA.scalarVal);
  result *= (currScalarValue - pointA.scalarVal);
  result = pointA.position + result;

  return result;
}

void trianglesfromTetrahedrons(int pointA, int pointB, int pointC, int pointD) {
  ScalarFieldPoint vertexA = scalarField->points[pointA];
  ScalarFieldPoint vertexB = scalarField->points[pointB];
  ScalarFieldPoint vertexC = scalarField->points[pointC];
  ScalarFieldPoint vertexD = scalarField->points[pointD];

  int signA = (vertexA.scalarVal < currScalarValue) ? -1 : +1;
  int signB = (vertexB.scalarVal < currScalarValue) ? -1 : +1;
  int signC = (vertexC.scalarVal < currScalarValue) ? -1 : +1;
  int signD = (vertexD.scalarVal < currScalarValue) ? -1 : +1;

  int prevIsoSurfaceVertex = currIsoSurfaceVertex;

  if (signA != signB)
    verticesIsoSurface[currIsoSurfaceVertex++] =
        linearInterpolate(vertexA, vertexB, currScalarValue);
  if (signA != signC)
    verticesIsoSurface[currIsoSurfaceVertex++] =
        linearInterpolate(vertexA, vertexC, currScalarValue);
  if (signA != signD)
    verticesIsoSurface[currIsoSurfaceVertex++] =
        linearInterpolate(vertexA, vertexD, currScalarValue);
  if (signB != signC)
    verticesIsoSurface[currIsoSurfaceVertex++] =
        linearInterpolate(vertexB, vertexC, currScalarValue);
  if (signB != signD)
    verticesIsoSurface[currIsoSurfaceVertex++] =
        linearInterpolate(vertexB, vertexD, currScalarValue);
  if (signC != signD)
    verticesIsoSurface[currIsoSurfaceVertex++] =
        linearInterpolate(vertexC, vertexD, currScalarValue);

  if (currIsoSurfaceVertex - prevIsoSurfaceVertex == 4) {
    verticesIsoSurface[currIsoSurfaceVertex++] =
        verticesIsoSurface[currIsoSurfaceVertex - 1];
    verticesIsoSurface[currIsoSurfaceVertex++] =
        verticesIsoSurface[currIsoSurfaceVertex - 3];
  }
}

void processCube(unsigned int cubeOrigin) {
  int cubeIndices[8];

  cubeIndices[0] = cubeOrigin;
  cubeIndices[1] = cubeOrigin + 1;
  cubeIndices[2] = numPointsSide + cubeIndices[0];
  cubeIndices[3] = numPointsSide + cubeIndices[1];
  cubeIndices[4] = numPointsPlane + cubeIndices[0];
  cubeIndices[5] = numPointsPlane + cubeIndices[1];
  cubeIndices[6] = numPointsPlane + cubeIndices[2];
  cubeIndices[7] = numPointsPlane + cubeIndices[3];

  trianglesfromTetrahedrons(cubeIndices[0], cubeIndices[3], cubeIndices[5],
                            cubeIndices[1]);
  trianglesfromTetrahedrons(cubeIndices[0], cubeIndices[7], cubeIndices[3],
                            cubeIndices[2]);
  trianglesfromTetrahedrons(cubeIndices[0], cubeIndices[7], cubeIndices[3],
                            cubeIndices[5]);
  trianglesfromTetrahedrons(cubeIndices[0], cubeIndices[6], cubeIndices[5],
                            cubeIndices[4]);
  trianglesfromTetrahedrons(cubeIndices[0], cubeIndices[7], cubeIndices[6],
                            cubeIndices[2]);
  trianglesfromTetrahedrons(cubeIndices[0], cubeIndices[7], cubeIndices[6],
                            cubeIndices[5]);
}