#version 330

in vec3 texture_coordinate;
uniform sampler3D ourTexture;
uniform float uf_iso_val;

out vec4 frag_color;

void main()
{
	float texture_color = texture(ourTexture, texture_coordinate).r;
	float diff = abs(texture_color - uf_iso_val);
	
	frag_color = vec4(1.0, 1.0, 1.0, 1.0);
	if(diff > 0.025){
		frag_color = vec4(0.0, 0.0, 0.0, 1.0);
	}
}

