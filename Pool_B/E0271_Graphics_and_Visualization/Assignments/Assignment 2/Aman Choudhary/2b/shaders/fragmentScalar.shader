#version 330

in vec3 texture_coordinate;
uniform sampler3D ourTexture;

out vec4 frag_color;

void main()
{
	frag_color =  vec4(texture(ourTexture, texture_coordinate).r);
}


