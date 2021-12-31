#version 330

in vec4 attr_position;
in float shiftSlice;
out vec4 vertex_color;

uniform mat4 uf_translate;
uniform mat4 uf_rotate;

void main()
{
    if(shiftSlice == 1.0f){
        gl_Position = uf_translate * vec4(attr_position);
        vertex_color = vec4(1.0, 0.0, 0.0, 1.0);
    }else{
        gl_Position = vec4(attr_position);
        vertex_color = vec4(1.0, 1.0, 1.0, 1.0);
    }

    gl_Position = uf_rotate * gl_Position;
}

