#include "file.h"
#include "transform.h"
#include <limits>
extern float boxSize;

ScalarField *readScalarFile(char *scalarFile)
{
    FILE *input = fopen(scalarFile, "r");

    int x, y, z, numPoints;
    fscanf(input, "%d", &x);
    fscanf(input, "%d", &y);
    fscanf(input, "%d", &z);
    numPoints = x * y * z;

    ScalarField *scalarField = new ScalarField;
    scalarField->side = x;
    scalarField->points = new ScalarFieldPoint[numPoints];

    int currX, currY, currZ, val, index;
    float step = (boxSize * 2) / (scalarField->side - 1);

    currY = 0;
    for (int k = 0; k < scalarField->side; k++)
    {
        currX = x - 1;
        for (int j = 0; j < scalarField->side; j++)
        {
            currZ = z - 1;
            for (int i = 0; i < scalarField->side; i++)
            {
                fscanf(input, "%d", &val);

                index = (currZ * (scalarField->side * scalarField->side)) + (currY * scalarField->side) + currX;

                (scalarField->points[index]).position.x = currX * step;
                (scalarField->points[index]).position.y = currY * step;
                (scalarField->points[index]).position.z = currZ * step;
                (scalarField->points[index]).scalarVal = val;

                --currZ;
            }
            --currX;
        }
        ++currY;
    }

    // printScalarField(scalarField, numPoints);

    return scalarField;
}

void printScalarField(ScalarField *scalarField, int numPoints)
{
    cout << scalarField->side << endl;

    for (int i = 0; i < numPoints; ++i)
    {
        cout << setw(10) << (scalarField->points[i]).position.x << " "
             << setw(10) << (scalarField->points[i]).position.y << " "
             << setw(10) << (scalarField->points[i]).position.z << " "
             << setw(10) << (int)(scalarField->points[i]).scalarVal << endl;
    }
}