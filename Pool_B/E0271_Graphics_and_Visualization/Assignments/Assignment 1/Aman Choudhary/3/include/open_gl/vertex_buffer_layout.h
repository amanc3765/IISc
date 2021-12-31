#ifndef VERTEX_BUFFER_LAYOUT_H
#define VERTEX_BUFFER_LAYOUT_H

#include "header.h"

struct Attributes
{
    unsigned int type;
    unsigned int count;
    unsigned int normalized;

    static unsigned int sizeOfType(unsigned int type)
    {
        switch (type)
        {
        case GL_FLOAT:
            return 4;
            break;
        case GL_UNSIGNED_INT:
            return 4;
            break;
        }

        return 0;
    }
};

class VertexBufferLayout
{
    unsigned int m_stride;
    vector<Attributes> m_attributeList;

public:
    VertexBufferLayout();
    ~VertexBufferLayout();

    void pushFloat(unsigned int count);
    void pushUnsignedInt(unsigned int count);

    inline vector<Attributes> getAttributes() { return m_attributeList; }
    inline unsigned int getStride() { return m_stride; }
};

#endif
