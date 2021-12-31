#version 330

in vec4 attr_position;
out vec4 vertex_position;
uniform mat4 uf_transform;

void main()
{
    gl_Position = uf_transform * vec4(attr_position);
    vertex_position = gl_Position; 
}

