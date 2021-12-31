#include "transform.h"

Transform::Transform()
{
    angle = 0.0f;
    scale = Vector3f(1.0f, 1.0f, 1.0f);
    translate = Vector3f(0.0f, 0.0f, 0.0f);
    axis = Vector3f(1.0f, 0.0f, 0.0f);

    setIdentity();
}

void Transform::setIdentity()
{
    transformMat.m[0][0] = 1.0f;
    transformMat.m[0][1] = 0.0f;
    transformMat.m[0][2] = 0.0f;
    transformMat.m[0][3] = 0.0f;
    transformMat.m[1][0] = 0.0f;
    transformMat.m[1][1] = 1.0f;
    transformMat.m[1][2] = 0.0f;
    transformMat.m[1][3] = 0.0f;
    transformMat.m[2][0] = 0.0f;
    transformMat.m[2][1] = 0.0f;
    transformMat.m[2][2] = 1.0f;
    transformMat.m[2][3] = 0.0f;
    transformMat.m[3][0] = 0.0f;
    transformMat.m[3][1] = 0.0f;
    transformMat.m[3][2] = 0.0f;
    transformMat.m[3][3] = 1.0f;
}

void Transform::setScale()
{
    scaleMat.m[0][0] = scale.x;
    scaleMat.m[0][1] = 0.0f;
    scaleMat.m[0][2] = 0.0f;
    scaleMat.m[0][3] = 0.0f;
    scaleMat.m[1][0] = 0.0f;
    scaleMat.m[1][1] = scale.y;
    scaleMat.m[1][2] = 0.0f;
    scaleMat.m[1][3] = 0.0f;
    scaleMat.m[2][0] = 0.0f;
    scaleMat.m[2][1] = 0.0f;
    scaleMat.m[2][2] = scale.z;
    scaleMat.m[2][3] = 0.0f;
    scaleMat.m[3][0] = 0.0f;
    scaleMat.m[3][1] = 0.0f;
    scaleMat.m[3][2] = 0.0f;
    scaleMat.m[3][3] = 1.0f;
}

void Transform::setTranslate()
{
    translateMat.m[0][0] = 1.0f;
    translateMat.m[0][1] = 0.0f;
    translateMat.m[0][2] = 0.0f;
    translateMat.m[0][3] = translate.x;
    translateMat.m[1][0] = 0.0f;
    translateMat.m[1][1] = 1.0f;
    translateMat.m[1][2] = 0.0f;
    translateMat.m[1][3] = translate.y;
    translateMat.m[2][0] = 0.0f;
    translateMat.m[2][1] = 0.0f;
    translateMat.m[2][2] = 1.0f;
    translateMat.m[2][3] = translate.z;
    translateMat.m[3][0] = 0.0f;
    translateMat.m[3][1] = 0.0f;
    translateMat.m[3][2] = 0.0f;
    translateMat.m[3][3] = 1.0f;
}

void Transform::setRotate()
{
    const float c = cosf(angle);
    const float s = sinf(angle);
    const float t = 1 - c;

    rotateMat.m[0][0] = t * axis.x * axis.x + c;
    rotateMat.m[0][1] = t * axis.x * axis.y - axis.z * s;
    rotateMat.m[0][2] = t * axis.x * axis.z + axis.y * s;
    rotateMat.m[0][3] = 0.0f;
    rotateMat.m[1][0] = t * axis.x * axis.y + axis.z * s;
    rotateMat.m[1][1] = t * axis.y * axis.y + c;
    rotateMat.m[1][2] = t * axis.y * axis.z - axis.x * s;
    rotateMat.m[1][3] = 0.0f;
    rotateMat.m[2][0] = t * axis.x * axis.z - axis.y * s;
    rotateMat.m[2][1] = t * axis.y * axis.z + axis.x * s;
    rotateMat.m[2][2] = t * axis.z * axis.z + c;
    rotateMat.m[2][3] = 0.0f;
    rotateMat.m[3][0] = 0.0f;
    rotateMat.m[3][1] = 0.0f;
    rotateMat.m[3][2] = 0.0f;
    rotateMat.m[3][3] = 1.0f;
}

void normalizeBuffer(void *data, unsigned int numVertices, unsigned int dim, float scaleLow, float scaleHigh)
{
    float mini[dim], maxi[dim], *curr;

    curr = (float *)data;
    for (int i = 0; i < numVertices; ++i)
    {
        for (int j = 0; j < dim; ++j)
        {
            mini[j] = min(mini[j], *curr);
            maxi[j] = max(maxi[j], *curr);
            ++curr;
        }
        ++curr;
    }

    curr = (float *)data;
    for (int i = 0; i < numVertices; ++i)
    {
        for (int j = 0; j < dim; ++j)
        {
            *curr = (((*curr - mini[j]) / (maxi[j] - mini[j])) * (scaleHigh - scaleLow)) + scaleLow;
            ++curr;
        }
        ++curr;
    }
}