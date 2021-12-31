#ifndef VERTEX_ARRAY_H
#define VERTEX_ARRAY_H

#include "vertex_buffer.h"
#include "vertex_buffer_layout.h"

class VertexArray
{
    unsigned int m_RendererID;

public:
    VertexArray();
    ~VertexArray();

    void Bind();
    void unBind();
    void mapBufferToLayout(VertexBuffer *vbo, VertexBufferLayout *layout);
};

#endif