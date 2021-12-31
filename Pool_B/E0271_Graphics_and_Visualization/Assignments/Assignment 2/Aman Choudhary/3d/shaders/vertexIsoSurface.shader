#version 330

layout (location = 0) in vec3 attr_position;

void main()
{
	gl_Position = vec4(attr_position, 1.0);
}
