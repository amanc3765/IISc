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
    float step, mini, maxi;

    step = (boxSize * 2) / (scalarField->side - 1);
    mini = numeric_limits<float>::max();
    maxi = numeric_limits<float>::min();

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

                (scalarField->points[index]).position.x = currX * step - boxSize;
                (scalarField->points[index]).position.y = currY * step - boxSize;
                (scalarField->points[index]).position.z = (currZ * step - boxSize) * -1;
                (scalarField->points[index]).scalarVal = val;

                mini = min(mini, (float)val);
                maxi = max(maxi, (float)val);

                --currZ;
            }
            --currX;
        }
        ++currY;
    }

    for (int i = 0; i < numPoints; ++i)
    {
        float currVal = (scalarField->points[i]).scalarVal;
        (scalarField->points[i]).scalarVal = (currVal - mini) / (maxi - mini);
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
             << setw(10) << (scalarField->points[i]).scalarVal << endl;
    }
}