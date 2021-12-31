#version 330

in vec3 vertex_position;
in vec3 vertex_normal;
uniform vec3 uf_lightPos;

out vec4 frag_color;

vec3 normalize(vec3 v){
    float length = sqrt(v.x * v.x + v.y * v.y + v.z * v.z);

    if (length != 0)
    {
        v.x /= length;
        v.y /= length;
        v.z /= length;
    }

    return v;
}

float dotProduct(vec3 a, vec3 b)
{
    return (a.x * b.x + a.y * b.y + a.z * b.z);
}

void main()
{

    // Ambient Light_______________________________________________________

    float ambientStrength = 0.1;
    vec3 ambientColor = ambientStrength * vec3(0.0, 0.0, 0.0); //Black

    // Diffuse Light_______________________________________________________

    vec3 lightRay = uf_lightPos - vertex_position;	
    vec3 lightRayNormal = normalize(lightRay);

    vec3 fragPosNormal = normalize(vertex_normal);

    float diffuseStrength = 0.5;
    float diffuseFactor = max(0.0, dotProduct(lightRayNormal, fragPosNormal));
    vec3 diffuseColor = diffuseStrength * diffuseFactor * vec3(1.0, 0.0, 0.0); //Red

    // Specular Light_______________________________________________________

    vec3 viewPos = vec3(5.0, 5.0, -5.0);    
    vec3 viewDirection = viewPos - vertex_position;
    vec3 viewDirectionNormal = normalize(viewDirection);

    vec3 lightRayReverseNormal = normalize(-1 * lightRayNormal);
    vec3 reflectedRay = lightRayReverseNormal - ((2 * dotProduct(lightRayReverseNormal, fragPosNormal)) * fragPosNormal);   
    vec3 reflectedRayNormal = normalize(reflectedRay);
    
    float specularStrength = 1.0;
    float specularFactor = pow(max(0.0, dotProduct(viewDirectionNormal, reflectedRayNormal)), 32);
    vec3 specularColor = specularStrength * specularFactor * vec3(1.0, 1.0, 1.0); //White

    // Phong Shading (Ambient + Diffuse + Specular) _______________________
    
    vec3 objectColor = vec3(1.0, 1.0, 1.0);
    vec3 final_color = (ambientColor + diffuseColor + specularColor) * objectColor;  
    frag_color = vec4(final_color, 1.0);
}


