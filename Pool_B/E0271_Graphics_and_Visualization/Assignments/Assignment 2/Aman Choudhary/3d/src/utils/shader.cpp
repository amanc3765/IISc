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

unsigned int createShader(const string &vertexShaderPath, const string &geometryShaderPath, const string &fragmentShaderPath)
{
    unsigned int program, vsObj, fsObj;

    program = glCreateProgram();

    const string vertexShaderSource = parseShader(vertexShaderPath);
    const string fragmentShaderSource = parseShader(fragmentShaderPath);

    vsObj = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
    fsObj = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

    glAttachShader(program, vsObj);
    glAttachShader(program, fsObj);

    if (geometryShaderPath.length() > 0)
    {
        const string geometryShaderSource = parseShader(geometryShaderPath);
        unsigned int gsObj = compileShader(GL_GEOMETRY_SHADER, geometryShaderSource);
        glAttachShader(program, gsObj);
    }

    glLinkProgram(program);
    glValidateProgram(program);

    return program;
}
