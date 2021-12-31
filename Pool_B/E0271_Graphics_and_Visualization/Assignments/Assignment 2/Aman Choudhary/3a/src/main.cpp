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

VertexArray *vaoIsoContour, *vaoCube;
VertexBuffer *vboIsoContour, *vboCube;
VertexBufferLayout *layoutIsoContour, *layoutCube;
IndexBuffer *iboCube;
ScalarField *scalarField;
Matrix4f *persProjection;
Transform *transPerspective, *transRotate, *transCamera;
unsigned int shaderProgramIsoContour, shaderProgramCube;
unsigned int numSquares, numTriangles;
unsigned int numPointsPlane, scalarFieldOffset;
unsigned int numVerticesIsoContour, sizeVerticesIsoContour, currIsoContourVertex, numIndicesCube;
unsigned int *indicesCube;
Vector3f *verticesIsoContour;
float boxSize, rotateAngle;
int currSlice, currScalarValue;

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

Vector3f linearInterpolate(ScalarFieldPoint pointA, ScalarFieldPoint pointB, unsigned int currScalarValue)
{
	if (pointA.scalarVal > pointB.scalarVal)
	{
		ScalarFieldPoint temp;
		temp = pointA;
		pointA = pointB;
		pointB = temp;
	}

	Vector3f result;
	result = pointB.position - pointA.position;
	result /= (pointB.scalarVal - pointA.scalarVal);
	result *= (currScalarValue - pointA.scalarVal);
	result = pointA.position + result;

	return result;
}

void linesfromTriangles(int pointA, int pointB, int pointC)
{
	scalarFieldOffset = currSlice * numPointsPlane;
	ScalarFieldPoint vertexA = scalarField->points[scalarFieldOffset + pointA];
	ScalarFieldPoint vertexB = scalarField->points[scalarFieldOffset + pointB];
	ScalarFieldPoint vertexC = scalarField->points[scalarFieldOffset + pointC];

	int signA = (vertexA.scalarVal < currScalarValue) ? -1 : +1;
	int signB = (vertexB.scalarVal < currScalarValue) ? -1 : +1;
	int signC = (vertexC.scalarVal < currScalarValue) ? -1 : +1;

	if (signA != signB)
		verticesIsoContour[currIsoContourVertex++] = linearInterpolate(vertexA, vertexB, currScalarValue);
	if (signA != signC)
		verticesIsoContour[currIsoContourVertex++] = linearInterpolate(vertexA, vertexC, currScalarValue);
	if (signB != signC)
		verticesIsoContour[currIsoContourVertex++] = linearInterpolate(vertexB, vertexC, currScalarValue);
}

void createBuffersIsoContour()
{
	int squareOrigin, numPointsSide, squareIndices[4];
	numPointsSide = scalarField->side;
	currIsoContourVertex = 0;

	for (int j = 0; j < scalarField->side - 1; ++j)
	{
		for (int i = 0; i < scalarField->side - 1; ++i)
		{
			squareOrigin = j * scalarField->side + i;

			squareIndices[0] = squareOrigin;
			squareIndices[1] = squareOrigin + 1;
			squareIndices[2] = numPointsSide + squareIndices[0];
			squareIndices[3] = numPointsSide + squareIndices[1];

			linesfromTriangles(squareIndices[0], squareIndices[1], squareIndices[3]);
			linesfromTriangles(squareIndices[0], squareIndices[2], squareIndices[3]);
		}
	}

	sizeVerticesIsoContour = currIsoContourVertex * sizeof(Vector3f);

	// Create Buffers _____________________________________________

	vaoIsoContour = new VertexArray();
	vboIsoContour = new VertexBuffer(verticesIsoContour, sizeVerticesIsoContour);
	layoutIsoContour = new VertexBufferLayout();
	layoutIsoContour->pushFloat(3);
	vaoIsoContour->mapBufferToLayout(vboIsoContour, layoutIsoContour);
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[])
{
	/* by default the back ground color is black */
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

	boxSize = 0.75f;
	
	// scalarField = readScalarFile("../data/test.txt");
	scalarField = readScalarFile("../data/skull_256x256x256_uint8_Scalar_Values.txt");
	// scalarField = readScalarFile("../data/hydrogen_128x128x128_uint8_Scalar_values.txt");

	currSlice = 175;
	currScalarValue = 28;
	numPointsPlane = scalarField->side * scalarField->side;

	numSquares = (scalarField->side - 1) * (scalarField->side - 1);
	numTriangles = numSquares * 2;

	numVerticesIsoContour = numTriangles * 2;
	sizeVerticesIsoContour = numVerticesIsoContour * sizeof(Vector3f);
	verticesIsoContour = new Vector3f[numVerticesIsoContour];

	createBuffersIsoContour();
	createBuffersCube();

	shaderProgramIsoContour = createShader("shaders/vertexIsoContour.shader", "shaders/fragmentIsoContour.shader");
	shaderProgramCube = createShader("shaders/vertexCube.shader", "shaders/fragmentCube.shader");

	// Transformations _____________________________________________________________

	transPerspective = new Transform();
	transRotate = new Transform();
	transCamera = new Transform();

	transPerspective->translate = Vector3f(0.0f, 0.0f, +2.2f);
	transPerspective->setTranslate();

	persProjection = new Matrix4f();
	PersProjInfo proj(90.0f, 1.0f, 1.0f, +1.0, -1.0f);
	persProjection->InitPersProjTransform(proj);

	transPerspective->setIdentity();
	transPerspective->transformMat = transPerspective->translateMat * transPerspective->transformMat;
	transPerspective->transformMat = (*persProjection) * transPerspective->transformMat;

	transRotate = new Transform();
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

	// Render IsoContour _________________________________________
	glUseProgram(shaderProgramIsoContour);

	int ufLocationIsoContour = glGetUniformLocation(shaderProgramIsoContour, "uf_transform_isocontour");
	glUniformMatrix4fv(ufLocationIsoContour, 1, GL_TRUE, &((transCamera->transformMat).m[0][0]));

	vaoIsoContour->Bind();

	glLineWidth(1.48f);
	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	glDrawArrays(GL_LINES, 0, numVerticesIsoContour);

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