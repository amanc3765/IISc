#version 330

layout (location = 0) in vec3 attr_position;
layout (location = 1) in vec3 attr_normal; 
uniform mat4 uf_transform;

out vec3 vertex_position;
out vec3 vertex_normal;

void main()
{
	gl_Position = uf_transform * vec4(attr_position, 1.0);	
	vec4 temp_vertex_normal = uf_transform * vec4(attr_normal, 1.0);

	vertex_position = vec3(gl_Position.x, gl_Position.y, gl_Position.z);
	vertex_normal = vec3(temp_vertex_normal.x, temp_vertex_normal.y, temp_vertex_normal.z);
}

