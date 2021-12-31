#ifndef INDEX_BUFFER_H
#define INDEX_BUFFER_H

class IndexBuffer
{
    unsigned int m_RendererID;
    unsigned int m_count;

public:
    IndexBuffer(const void *data, unsigned int count);
    ~IndexBuffer();

    void Bind();
    void unBind();

    inline unsigned int getCount()
    {
        return m_count;
    }
};

#endif