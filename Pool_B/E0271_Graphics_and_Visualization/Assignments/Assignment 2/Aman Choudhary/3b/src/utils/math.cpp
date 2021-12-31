#include "math.h"
#include "header.h"

float getRandom()
{
    return ((float)random() / RAND_MAX);
}

float dotProduct(Vector4f &a, Vector4f &b)
{
    return (a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w);
}