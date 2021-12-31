#include "vertex_array.h"
#include "vertex_buffer_layout.h"
#include "error.h"

VertexArray::VertexArray()
{
    glGenVertexArrays(1, &m_RendererID);
    glBindVertexArray(m_RendererID);
}

VertexArray::~VertexArray()
{
    glDeleteVertexArrays(1, &m_RendererID);
}

void VertexArray::Bind()
{
    glBindVertexArray(m_RendererID);
}

void VertexArray::unBind()
{
    glBindVertexArray(0);
}

void VertexArray::mapBufferToLayout(VertexBuffer *vbo, VertexBufferLayout *layout)
{
    Bind();
    vbo->Bind();

    auto attributeList = layout->getAttributes();
    unsigned int offset = 0;

    for (unsigned int i = 0; i < attributeList.size(); ++i)
    {
        auto attribute = attributeList[i];
        GLcall(glEnableVertexAttribArray(i));
        // cout << i << " : " << attribute.count << " : " << layout->getStride() << endl;
        GLcall(glVertexAttribPointer(i, attribute.count, attribute.type, attribute.normalized, layout->getStride(), (const void *)offset));
        offset += attribute.count * Attributes::sizeOfType(attribute.type);
    }
}