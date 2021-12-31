#version 330

layout (location = 0) in vec3 attr_position;
uniform mat4 uf_transform_scalar;

out vec3 texture_coordinate;

void main()
{
	gl_Position = uf_transform_scalar * vec4(attr_position.x - 0.5, attr_position.y - 0.5, (attr_position.z - 0.5) * -1, 1.0);
	texture_coordinate = attr_position;
}

