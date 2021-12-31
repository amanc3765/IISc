#version 330

layout (location = 0) in vec3 attr_position;
layout (location = 1) in float attr_scalar; 
uniform mat4 uf_transform_scalar;

out float vertex_scalar; 

void main()
{
	gl_Position = uf_transform_scalar * vec4(attr_position, 1.0);
	vertex_scalar = attr_scalar;
}

