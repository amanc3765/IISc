#include "file.h"

OffModel *readOffFile(char *OffFile)
{
    FILE *input = fopen(OffFile, "r");

    char type[3];
    fscanf(input, "%s", type);
    /* First line should be OFF */
    if (strcmp(type, "OFF"))
    {
        printf("Not a OFF file");
        exit(1);
    }

    /* Read the no. of vertices, faces and edges */
    int nv, np, noEdges;
    fscanf(input, "%d", &nv);
    fscanf(input, "%d", &np);
    fscanf(input, "%d", &noEdges);

    /* initialize model */
    OffModel *model = (OffModel *)malloc(sizeof(OffModel));
    model->numberOfVertices = nv;
    model->numberOfPolygons = np;
    model->vertices = (Vertex *)malloc(nv * sizeof(Vertex));
    model->polygons = (Polygon *)malloc(np * sizeof(Polygon));

    /* Read the vertices' location*/
    float x, y, z;
    for (int i = 0; i < nv; i++)
    {
        fscanf(input, "%f %f %f", &x, &y, &z);
        (model->vertices[i]).x = x;
        (model->vertices[i]).y = y;
        (model->vertices[i]).z = z;
    }

    /* Read the Polygons */
    int n, v;
    for (int i = 0; i < np; i++)
    {
        /* No. of sides of the polygon (Eg. 3 => a triangle) */
        fscanf(input, "%d", &n);

        (model->polygons[i]).noSides = n;
        (model->polygons[i]).v = (int *)malloc(n * sizeof(int));
        /* read the vertices that make up the polygon */
        for (int j = 0; j < n; j++)
        {
            fscanf(input, "%d", &v);
            (model->polygons[i]).v[j] = v;
        }
    }

    fclose(input);

    return model;
}

void printOffFile(OffModel *model)
{
    printf("OFF\n");
    printf("%d %d 0 \n", model->numberOfVertices, model->numberOfPolygons);

    for (int i = 0; i < model->numberOfVertices; i++)
    {
        printf("%f %f %f \n", (model->vertices[i]).x, (model->vertices[i]).y, (model->vertices[i]).z);
    }

    for (int i = 0; i < model->numberOfPolygons; i++)
    {
        printf("%d ", (model->polygons[i]).noSides);
        for (int j = 0; j < (model->polygons[i]).noSides; j++)
        {
            printf("%d ", (model->polygons[i]).v[j]);
        }
        printf("\n");
    }
}

int FreeOffModel(OffModel *model)
{
    if (model == NULL)
        return 0;

    free(model->vertices);
    for (int i = 0; i < model->numberOfPolygons; ++i)
    {
        if ((model->polygons[i]).v)
            free((model->polygons[i]).v);
    }
    free(model->polygons);
    free(model);

    return 1;
}