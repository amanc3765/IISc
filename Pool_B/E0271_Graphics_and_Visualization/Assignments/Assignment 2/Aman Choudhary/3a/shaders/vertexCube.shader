#version 330

layout (location = 0) in vec3 attr_position;
uniform mat4 uf_transform_cube;

void main()
{
	gl_Position = uf_transform_cube * vec4(attr_position, 1.0);
}

