#ifndef FILE_H
#define FILE_H

#include "header.h"

typedef struct Vt
{
    float x, y, z;
} Vertex;

typedef struct Pgn
{
    int noSides;
    int *v;
} Polygon;

typedef struct offmodel
{
    Vertex *vertices;
    Polygon *polygons;
    int numberOfVertices;
    int numberOfPolygons;
} OffModel;

OffModel *readOffFile(char *OffFile);
void printOffFile(OffModel *model);
int FreeOffModel(OffModel *model);

#endif