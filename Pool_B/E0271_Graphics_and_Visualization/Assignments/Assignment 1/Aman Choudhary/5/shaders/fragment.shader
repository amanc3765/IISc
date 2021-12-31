#version 330

uniform vec4 uf_color;
out vec4 frag_color;

void main()
{
    frag_color = uf_color;
}


