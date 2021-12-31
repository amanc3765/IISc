#ifndef ERROR_H
#define ERROR_H

#define ASSERT(x) \
    if (!(x))     \
        exit(0);
#define GLcall(x)    \
    GLclearErrors(); \
    x;               \
    ASSERT(GLcheckErrors(#x, __FILE__, __LINE__))

void GLclearErrors();
bool GLcheckErrors(const char *function, const char *file, int line);

#endif