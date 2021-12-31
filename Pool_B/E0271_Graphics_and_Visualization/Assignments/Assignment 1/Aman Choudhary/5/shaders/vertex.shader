#version 330

in vec4 attr_position;

uniform mat4 uf_transform;

void main()
{
    gl_Position = uf_transform * attr_position;
}

