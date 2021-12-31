#include "header.h"
#include "window.h"
#include "math.h"
#include "transform.h"
#include "shader.h"
#include "error.h"
#include "file.h"
#include "vertex_array.h"
#include "vertex_buffer.h"
#include "index_buffer.h"
#include "vertex_buffer_layout.h"

VertexArray *vaoIsoSurface, *vaoCube;
VertexBuffer *vboIsoSurface, *vboCube;
VertexBufferLayout *layoutIsoSurface, *layoutCube;
IndexBuffer *iboCube;
ScalarField *scalarField;
Matrix4f *persProjection;
Transform *transPerspective, *transRotate, *transCamera, *transScale;
unsigned int shaderProgramIsoSurface, shaderProgramCube;
unsigned int numCubes, numTetrahedrons;
unsigned int numVerticesIsoSurface, sizeVerticesIsoSurface, currIsoSurfaceVertex, numIndicesCube;
unsigned int *indicesCube;
Vector3f *verticesIsoSurface;
int currScalarValue;
float boxSize, currIsoVal, rotateAngle, step;

static void createBuffersCube()
{
	// Vertices of Cube ___________________________________________

	unsigned int numVerticesCube = 8;
	Vector3f verticesCube[numVerticesCube];
	verticesCube[0] = {-boxSize, -boxSize, -boxSize};
	verticesCube[1] = {-boxSize, -boxSize, +boxSize};
	verticesCube[2] = {-boxSize, +boxSize, -boxSize};
	verticesCube[3] = {-boxSize, +boxSize, +boxSize};
	verticesCube[4] = {+boxSize, -boxSize, -boxSize};
	verticesCube[5] = {+boxSize, -boxSize, +boxSize};
	verticesCube[6] = {+boxSize, +boxSize, -boxSize};
	verticesCube[7] = {+boxSize, +boxSize, +boxSize};

	numIndicesCube = 24;
	unsigned int indicesCube[numIndicesCube] = {
		0, 2, 2, 3, 3, 1, 1, 0,
		7, 6, 6, 4, 4, 5, 5, 7,
		0, 4, 3, 7, 1, 5, 2, 6};

	// Create Buffers _____________________________________________

	vaoCube = new VertexArray;
	vboCube = new VertexBuffer(verticesCube, sizeof(verticesCube));
	layoutCube = new VertexBufferLayout();
	layoutCube->pushFloat(3);
	vaoCube->mapBufferToLayout(vboCube, layoutCube);
	iboCube = new IndexBuffer(indicesCube, numIndicesCube);
}

void createBuffersIsoSurface()
{
	int cubeOrigin, numPointsSide, numPointsPlane, cubeIndices[8];

	numPointsSide = scalarField->side;
	numPointsPlane = scalarField->side * scalarField->side;

	for (int k = 0; k < scalarField->side - 1; ++k)
	{
		for (int j = 0; j < scalarField->side - 1; ++j)
		{
			for (int i = 0; i < scalarField->side - 1; ++i)
			{
				verticesIsoSurface[currIsoSurfaceVertex++] = Vector3f(i * step, j * step, k * step);
			}
		}
	}

	// Create Buffers _____________________________________________

	vaoIsoSurface = new VertexArray;
	vboIsoSurface = new VertexBuffer(verticesIsoSurface, sizeVerticesIsoSurface);
	layoutIsoSurface = new VertexBufferLayout();
	layoutIsoSurface->pushFloat(3);
	vaoIsoSurface->mapBufferToLayout(vboIsoSurface, layoutIsoSurface);
}

void createTexture()
{
	unsigned int texture;
	glGenTextures(1, &texture);
	glBindTexture(GL_TEXTURE_3D, texture);

	// set the texture wrapping/filtering options (on the currently bound texture object)
	glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

	// load and generate the texture
	int numPoints = scalarField->side * scalarField->side * scalarField->side;
	unsigned char *data = new unsigned char[numPoints];
	for (int i = 0; i < numPoints; ++i)
		data[i] = (scalarField->points[i]).scalarVal;

	glTexImage3D(GL_TEXTURE_3D, 0, GL_RED, scalarField->side, scalarField->side, scalarField->side, 0, GL_RED, GL_UNSIGNED_BYTE, data);
	glGenerateMipmap(GL_TEXTURE_3D);
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[])
{
	/* by default the back ground color is black */
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

	boxSize = 0.5f;
	currScalarValue = 20;
	currIsoVal = currScalarValue / 255.0;

	// scalarField = readScalarFile("../data/test.txt");
	// scalarField = readScalarFile("../data/hydrogen_128x128x128_uint8_Scalar_values.txt");
	scalarField = readScalarFile("../data/heptane_302x302x302_uint8_Scalar_Values.txt");
	// scalarField = readScalarFile("../data/skull_256x256x256_uint8_Scalar_Values.txt");

	numCubes = (scalarField->side - 1) * (scalarField->side - 1) * (scalarField->side - 1);
	sizeVerticesIsoSurface = numCubes * sizeof(Vector3f);
	verticesIsoSurface = new Vector3f[numCubes];

	createTexture();
	createBuffersIsoSurface();
	createBuffersCube();

	shaderProgramIsoSurface = createShader("shaders/vertexIsoSurface.shader", "shaders/geometryIsoSurface.shader", "shaders/fragmentIsoSurface.shader");
	shaderProgramCube = createShader("shaders/vertexCube.shader", "", "shaders/fragmentCube.shader");

	// Transformations _____________________________________________________________

	transPerspective = new Transform();
	transRotate = new Transform();
	transScale = new Transform();
	transCamera = new Transform();

	persProjection = new Matrix4f();
	PersProjInfo proj(90.0f, 1.0f, 1.0f, +1.0, -1.0f);
	persProjection->InitPersProjTransform(proj);

	transPerspective->translate = Vector3f(0.0f, 0.0f, +1.8f);
	transPerspective->setTranslate();

	transPerspective->setIdentity();
	transPerspective->transformMat = transPerspective->translateMat * transPerspective->transformMat;
	transPerspective->transformMat = (*persProjection) * transPerspective->transformMat;

	transRotate->axis = Vector3f(0.0, 1.0, 0.0);

	transScale->scale = Vector3f(1.75, 1.75, 1.75);
	transScale->setScale();

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	transRotate->angle = rotateAngle;
	transRotate->setRotate();
	transCamera->transformMat = transPerspective->transformMat * transRotate->rotateMat;

	// Render IsoSurface _________________________________________
	glUseProgram(shaderProgramIsoSurface);

	int ufLocationCamera = glGetUniformLocation(shaderProgramIsoSurface, "uf_transform_camera");
	glUniformMatrix4fv(ufLocationCamera, 1, GL_TRUE, &((transCamera->transformMat).m[0][0]));

	int ufLocationScale = glGetUniformLocation(shaderProgramIsoSurface, "uf_transform_scale");
	glUniformMatrix4fv(ufLocationScale, 1, GL_TRUE, &((transScale->scaleMat).m[0][0]));

	glUniform1f(glGetUniformLocation(shaderProgramIsoSurface, "step"), step);

	glUniform1f(glGetUniformLocation(shaderProgramIsoSurface, "uf_iso_value"), currIsoVal);

	vaoIsoSurface->Bind();

	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	glDrawArrays(GL_POINTS, 0, numCubes);

	// Render cube ________________________________________________
	glUseProgram(shaderProgramCube);

	int ufLocationCube = glGetUniformLocation(shaderProgramCube, "uf_transform_cube");
	glUniformMatrix4fv(ufLocationCube, 1, GL_TRUE, &((transCamera->transformMat).m[0][0]));

	vaoCube->Bind();
	iboCube->Bind();

	glLineWidth(1.0f);
	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	glDrawElements(GL_LINES, numIndicesCube, GL_UNSIGNED_INT, NULL);

	glutSwapBuffers();
}

void initGlut(int argc, char **argv)
{
	/* initialize glut */
	glutInit(&argc, argv);

	/* request initial window size and position on the screen */
	glutInitWindowSize(windowWidth, windowHeight);
	glutInitWindowPosition(windowPositionX, windowPositionY);

	/* request full color with double buffering and depth-based rendering */
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_DEPTH | GLUT_RGBA);

	/* create window whose title is the name of the executable */
	glutCreateWindow(windowTitle);

	initGlutCallbacks();
}

void initGlew()
{
	// Must be done after glut is initialized!
	GLenum res = glewInit();
	if (res != GLEW_OK)
	{
		fprintf(stderr, "Error: '%s'\n", glewGetErrorString(res));
		exit(0);
	}
}

int main(int argc, char **argv)
{
	initGlut(argc, argv);
	initGlew();

	printf("GL version: %s\n", glGetString(GL_VERSION));

	/* initialize model */
	initModel(argc, argv);

	/* give control over to glut to handle rendering and interaction  */
	glutMainLoop();

	/* program should never get here */
	return 0;
}