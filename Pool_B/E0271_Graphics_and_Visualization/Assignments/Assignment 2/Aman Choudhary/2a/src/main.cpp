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

VertexArray *vaoScalar, *vaoCube;
VertexBuffer *vboCube;
VertexBufferLayout *layoutScalar;
IndexBuffer *iboScalar, *iboCube;
ScalarField *scalarField;
Matrix4f *persProjection;
Transform *transPerspective, *transRotate, *transCamera;
unsigned int shaderProgramScalar, shaderProgramCube;
unsigned int numIndicesScalar, numIndicesCube;
float boxSize, planeZ, rotateAngle;
int currSlice;

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

	VertexBufferLayout *layoutCube = new VertexBufferLayout();
	layoutCube->pushFloat(3);

	vaoCube->mapBufferToLayout(vboCube, layoutCube);

	iboCube = new IndexBuffer(indicesCube, numIndicesCube);
}

static void createBuffersScalar()
{

	// Triangulate _____________________________________________

	unsigned int numPolygons = (scalarField->side - 1) * (scalarField->side - 1) * 2;
	numIndicesScalar = 3 * numPolygons;
	unsigned int *indicesScalar = new unsigned int[numIndicesScalar];

	int startPoint, endPoint, currIndex = 0;
	startPoint = 0;
	endPoint = startPoint + scalarField->side - 1;

	for (int i = startPoint; i < endPoint; ++i)
	{
		indicesScalar[currIndex++] = i;
		indicesScalar[currIndex++] = i + 1;
		indicesScalar[currIndex++] = i + 1 + scalarField->side;
	}

	for (int k = 0; k < scalarField->side - 2; ++k)
	{
		startPoint = endPoint + 1;
		endPoint = startPoint + scalarField->side - 1;
		for (int i = startPoint; i < endPoint; ++i)
		{
			indicesScalar[currIndex++] = i;
			indicesScalar[currIndex++] = i + 1;
			indicesScalar[currIndex++] = i + 1 + scalarField->side;

			indicesScalar[currIndex++] = i;
			indicesScalar[currIndex++] = i + 1;
			indicesScalar[currIndex++] = i - scalarField->side;
		}
	}

	startPoint = endPoint + 1;
	endPoint = startPoint + scalarField->side - 1;
	for (int i = startPoint; i < endPoint; ++i)
	{
		indicesScalar[currIndex++] = i;
		indicesScalar[currIndex++] = i + 1;
		indicesScalar[currIndex++] = i - scalarField->side;
	}

	// Create Buffers _____________________________________________

	vaoScalar = new VertexArray;
	layoutScalar = new VertexBufferLayout();
	layoutScalar->pushFloat(3);
	layoutScalar->pushFloat(1);
	iboScalar = new IndexBuffer(indicesScalar, numIndicesScalar);
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[])
{
	/* by default the back ground color is black */
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

	boxSize = 0.50f;
	currSlice = 175;
	planeZ = currSlice / 255.0 - boxSize;

	scalarField = readScalarFile("../data/skull_256x256x256_uint8_Scalar_Values.txt");
	// ScalarField *scalarField = readScalarFile("../data/test.txt");

	createBuffersScalar();
	createBuffersCube();

	shaderProgramScalar = createShader("shaders/vertexScalar.shader", "shaders/fragmentScalar.shader");
	shaderProgramCube = createShader("shaders/vertexCube.shader", "shaders/fragmentCube.shader");

	// Camera _____________________________________________________________

	transPerspective = new Transform();
	transRotate = new Transform();
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

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	transRotate->angle = rotateAngle;
	transRotate->setRotate();
	transCamera->transformMat = transPerspective->transformMat * transRotate->rotateMat;

	// Vertices of Scalar ________________________________
	float step, indexZ;
	int numPointsPlane, startPoint;

	step = (boxSize * 2) / (scalarField->side - 1);
	indexZ = ceil((planeZ + boxSize) / step);
	numPointsPlane = scalarField->side * scalarField->side;
	startPoint = indexZ * numPointsPlane;

	// cout << (scalarField->points[startPoint]).position.z << endl;

	unsigned char *verticesScalar = (unsigned char *)&(scalarField->points[startPoint]);
	unsigned int sizeVerticesScalar = numPointsPlane * sizeof(ScalarFieldPoint);

	glUseProgram(shaderProgramScalar);

	int ufTransformLocationScalar = glGetUniformLocation(shaderProgramScalar, "uf_transform_scalar");
	glUniformMatrix4fv(ufTransformLocationScalar, 1, GL_TRUE, &((transCamera->transformMat).m[0][0]));

	VertexBuffer *vboScalar = new VertexBuffer(verticesScalar, sizeVerticesScalar);
	vaoScalar->mapBufferToLayout(vboScalar, layoutScalar);

	vaoScalar->Bind();
	iboScalar->Bind();

	glDrawElements(GL_TRIANGLES, numIndicesScalar, GL_UNSIGNED_INT, NULL);

	// Render cube ________________________________________________

	glUseProgram(shaderProgramCube);

	int ufTransformLocationCube = glGetUniformLocation(shaderProgramCube, "uf_transform_cube");
	glUniformMatrix4fv(ufTransformLocationCube, 1, GL_TRUE, &((transCamera->transformMat).m[0][0]));

	vaoCube->Bind();
	iboCube->Bind();

	glLineWidth(2.0f);
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