#include "shader.h"

string parseShader(const string &filePath)
{
    ifstream stream(filePath);
    if (!stream)
        cout << "\nInvalid file name. Failed to parse. \n";

    ostringstream ss;
    ss << stream.rdbuf();

    return ss.str();
}

unsigned int compileShader(unsigned int shaderType, const string shaderSource)
{
    unsigned int id = glCreateShader(shaderType);
    const char *src = &shaderSource[0];
    glShaderSource(id, 1, &src, NULL);
    glCompileShader(id);

    int result;
    glGetShaderiv(id, GL_COMPILE_STATUS, &result);
    if (result == GL_FALSE)
    {
        int length;
        glGetShaderiv(id, GL_INFO_LOG_LENGTH, &length);
        char *message = new char[length];

        glGetShaderInfoLog(id, length, &length, message);
        cout << "Failed to compile shader: " << message << endl;

        glDeleteShader(id);
        return 0;
    }

    return id;
}

unsigned int createShader()
{
    unsigned int program, vsObj, fsObj;

    program = glCreateProgram();

    const string vertexShaderSource = parseShader("shaders/vertex.shader");
    const string fragmentShaderSource = parseShader("shaders/fragment.shader");

    vsObj = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
    fsObj = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

    glAttachShader(program, vsObj);
    glAttachShader(program, fsObj);

    glLinkProgram(program);
    glValidateProgram(program);

    return program;
}
