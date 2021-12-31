#include "vertex_buffer_layout.h"

VertexBufferLayout::VertexBufferLayout()
{
    m_stride = 0;
}

VertexBufferLayout::~VertexBufferLayout()
{
}

void VertexBufferLayout::pushFloat(unsigned int count)
{
    m_attributeList.push_back({GL_FLOAT, count, GL_FALSE});
    m_stride += count * sizeof(GLfloat);
}

void VertexBufferLayout::pushUnsignedInt(unsigned int count)
{
    m_attributeList.push_back({GL_UNSIGNED_INT, count, GL_FALSE});
    m_stride += count * sizeof(GLuint);
}
