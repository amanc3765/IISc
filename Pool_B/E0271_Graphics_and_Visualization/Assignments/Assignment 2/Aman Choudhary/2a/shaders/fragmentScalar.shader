#version 330

in float vertex_scalar;
out vec4 frag_color;

vec4 colormap(float x) {
	float d = clamp(x, 0.0, 1.0);
	return vec4(d, d, d, 1.0);
}

void main()
{
    frag_color = colormap(vertex_scalar);
}


