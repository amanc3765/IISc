#version 330 core
layout (points) in;
layout (triangle_strip, max_vertices = 24) out;

uniform float step;
uniform float uf_iso_value;
uniform mat4 uf_transform_camera;
uniform mat4 uf_transform_scale;
uniform sampler3D ourTexture;

vec4 linearInterpolate(vec3 pointA, vec3 pointB)
{
    float scalarValA = texture(ourTexture, pointA).r;
    float scalarValB = texture(ourTexture, pointB).r;

    if (scalarValA > scalarValB)
    {
        vec3 temp;
        temp = pointA;
        pointA = pointB;
        pointB = temp;
    }

    vec3 result;
    result = pointB - pointA;
    result /= (scalarValB - scalarValA);
    result *= (uf_iso_value - scalarValA);
    result = pointA + result;

    return vec4(result, 1.0);
}

void processTetrahedrons(vec3 pointA, vec3 pointB, vec3 pointC, vec3 pointD)
{
    vec4 adjustment = vec4(-0.5, -0.5, -0.5, 1.0);
    float scalarValA = texture(ourTexture, pointA).r;
    float scalarValB = texture(ourTexture, pointB).r;
    float scalarValC = texture(ourTexture, pointC).r;
    float scalarValD = texture(ourTexture, pointD).r;

    int signA = (scalarValA < uf_iso_value) ? -1 : +1;
    int signB = (scalarValB < uf_iso_value) ? -1 : +1;
    int signC = (scalarValC < uf_iso_value) ? -1 : +1;
    int signD = (scalarValD < uf_iso_value) ? -1 : +1;

    if (signA != signB){
        gl_Position = uf_transform_camera * uf_transform_scale * (linearInterpolate(pointA, pointB) + adjustment);
        EmitVertex();
    }

    if (signA != signC){
        gl_Position = uf_transform_camera * uf_transform_scale * (linearInterpolate(pointA, pointC) + adjustment);
        EmitVertex();
    }
    
    if (signA != signD){
        gl_Position = uf_transform_camera * uf_transform_scale * (linearInterpolate(pointA, pointD) + adjustment);
        EmitVertex();
    }
    
    if (signB != signC){
        gl_Position = uf_transform_camera * uf_transform_scale * (linearInterpolate(pointB, pointC) + adjustment);
        EmitVertex();
    }
    
    if (signB != signD){
        gl_Position = uf_transform_camera * uf_transform_scale * (linearInterpolate(pointB, pointD) + adjustment);
        EmitVertex(); 
    }
    
    if (signC != signD){
        gl_Position = uf_transform_camera * uf_transform_scale * (linearInterpolate(pointC, pointD) + adjustment);
        EmitVertex(); 
    }    

    EndPrimitive();	        
}

void processCube(vec4 cubeOrigin)
{    
    vec3 vertexA = vec3(cubeOrigin.x        , cubeOrigin.y        , cubeOrigin.z       );
    vec3 vertexB = vec3(cubeOrigin.x + step , cubeOrigin.y        , cubeOrigin.z       );
    vec3 vertexC = vec3(cubeOrigin.x        , cubeOrigin.y + step , cubeOrigin.z       );
    vec3 vertexD = vec3(cubeOrigin.x + step , cubeOrigin.y + step , cubeOrigin.z       );
    vec3 vertexE = vec3(cubeOrigin.x        , cubeOrigin.y        , cubeOrigin.z + step);
    vec3 vertexF = vec3(cubeOrigin.x + step , cubeOrigin.y        , cubeOrigin.z + step);
    vec3 vertexG = vec3(cubeOrigin.x        , cubeOrigin.y + step , cubeOrigin.z + step);
    vec3 vertexH = vec3(cubeOrigin.x + step , cubeOrigin.y + step , cubeOrigin.z + step);

    processTetrahedrons(vertexA, vertexD, vertexF, vertexB); 
    processTetrahedrons(vertexA, vertexH, vertexD, vertexC); 
    processTetrahedrons(vertexA, vertexH, vertexD, vertexF); 
    processTetrahedrons(vertexA, vertexG, vertexF, vertexE); 
    processTetrahedrons(vertexA, vertexH, vertexG, vertexC); 
    processTetrahedrons(vertexA, vertexH, vertexG, vertexF); 	
}

void main() {    
    processCube(gl_in[0].gl_Position);
}  
