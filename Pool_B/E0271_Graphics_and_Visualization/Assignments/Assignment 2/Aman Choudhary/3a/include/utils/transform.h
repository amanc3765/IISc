#ifndef TRANSFORM_H
#define TRANSFORM_H

#include "math.h"

class Transform
{
public:
    float angle;
    Vector3f scale, translate, axis;
    Matrix4f transformMat, scaleMat, translateMat, rotateMat;

    Transform();
    ~Transform();

    void setIdentity();
    void setScale();
    void setTranslate();
    void setRotate();
};

void normalizeBuffer(void *data, unsigned int numVertices, unsigned int dim, float scaleLow, float scaleHigh);

#endif