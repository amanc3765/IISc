#include "header.h"

void GLclearErrors()
{
    while (glGetError() != GL_NO_ERROR)
        ;
}

bool GLcheckErrors(const char *function, const char *file, int line)
{
    GLenum errorCode = glGetError();

    if (errorCode != GL_NO_ERROR)
    {
        cout << "[OPENGL Error] " << file << " : " << function << " : Line " << line << " : [0x" << hex << errorCode << "]\n";
        return false;
    }

    return true;
}

/*
GL_INVALID_ENUM, 0x0500
Given when an enumeration parameter is not a legal enumeration for that function. This is given only for local problems; if the spec allows the enumeration in certain circumstances, where other parameters or state dictate those circumstances, then GL_INVALID_OPERATION is the result instead.

GL_INVALID_VALUE, 0x0501
Given when a value parameter is not a legal value for that function. This is only given for local problems; if the spec allows the value in certain circumstances, where other parameters or state dictate those circumstances, then GL_INVALID_OPERATION is the result instead.

GL_INVALID_OPERATION, 0x0502
Given when the set of state for a command is not legal for the parameters given to that command. It is also given for commands where combinations of parameters define what the legal parameters are.

GL_STACK_OVERFLOW, 0x0503
Given when a stack pushing operation cannot be done because it would overflow the limit of that stack's size.

GL_STACK_UNDERFLOW, 0x0504
Given when a stack popping operation cannot be done because the stack is already at its lowest point.

GL_OUT_OF_MEMORY, 0x0505
Given when performing an operation that can allocate memory, and the memory cannot be allocated. The results of OpenGL functions that return this error are undefined; it is allowable for partial execution of an operation to happen in this circumstance.

GL_INVALID_FRAMEBUFFER_OPERATION, 0x0506
Given when doing anything that would attempt to read from or write/render to a framebuffer that is not complete.

GL_CONTEXT_LOST, 0x0507
Given if the OpenGL context has been lost, due to a graphics card reset.
*/
