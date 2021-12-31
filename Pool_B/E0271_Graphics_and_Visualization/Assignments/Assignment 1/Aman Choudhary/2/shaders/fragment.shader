#version 330

in vec4 vertex_position;
out vec4 frag_color;

// 0.51

void main()
{
    frag_color = vec4(0.0, 0.51, 0.0, 1.0);
    // if(vertex_position.y >= 0.3f)
    //     frag_color = vec4(0.8, 0.4, 0.0, 1.0);
    // else if(vertex_position.y >= -0.3f)
    //     frag_color = vec4(1.0, 1.0, 1.0, 1.0);
    // else
    //     frag_color = vec4(0.0, 0.50, 0.0, 1.0);
}

