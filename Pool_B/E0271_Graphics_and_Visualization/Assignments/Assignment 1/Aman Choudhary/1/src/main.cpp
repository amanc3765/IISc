#include "header.h"
#include "window.h"
#include "math.h"
#include "shader.h"
#include "error.h"
#include "vertex_array.h"
#include "vertex_buffer.h"
#include "index_buffer.h"
#include "vertex_buffer_layout.h"

unsigned int shaderProgram;
unsigned int numVertices, numIndices;
VertexArray *vao;

void generateFern(float vertices[], unsigned int numVertices)
{
	vertices[0] = 0.00f;
	vertices[1] = 0.00f;

	float p, prevX, prevY, currX, currY;

	for (unsigned int i = 1; i < numVertices; ++i)
	{
		p = getRandom();

		prevX = vertices[(i - 1) * 2];
		prevY = vertices[(i - 1) * 2 + 1];

		if (p <= 0.01f)
		{
			currX = 0.00f;
			currY = 0.16f * prevY;
		}
		else if (p <= 0.86f)
		{
			currX = 0.85f * prevX + 0.04f * prevY;
			currY = 0.04f * prevX + 0.85f * prevY + 1.60f;
		}
		else if (p <= 0.93f)
		{
			currX = 0.20f * prevX - 0.26f * prevY;
			currY = 0.23f * prevX + 0.22f * prevY + 1.60f;
		}
		else
		{
			currX = -0.15f * prevX + 0.28f * prevY;
			currY = +0.26f * prevX + 0.24f * prevY + 0.44f;
		}

		vertices[i * 2] = currX;
		vertices[i * 2 + 1] = currY;
	}
}

static void createBuffersSingleFern()
{
	numVertices = 350000;
	unsigned int vertexBufferLen = numVertices * 2;
	float vertices[vertexBufferLen];

	generateFern(vertices, numVertices);

	for (unsigned int i = 0; i < vertexBufferLen;)
	{
		vertices[i] /= 5.00f;
		++i;
		vertices[i] = (vertices[i] / 6.50f) - 0.90f;
		++i;
	}

	vao = new VertexArray;

	VertexBuffer *vbo = new VertexBuffer(vertices, sizeof(vertices));

	VertexBufferLayout *layout = new VertexBufferLayout();
	layout->pushFloat(2);

	vao->mapBufferToLayout(vbo, layout);
}

static void createBuffersDoubleFern()
{
	unsigned int numVerticesSingle = 300000;
	numVertices = numVerticesSingle * 2;
	unsigned int vertexBufferLen = numVerticesSingle * 2;
	float leftVertices[vertexBufferLen], rightVertices[vertexBufferLen];

	generateFern(leftVertices, numVerticesSingle);

	for (unsigned int i = 0; i < vertexBufferLen;)
	{

		leftVertices[i] = (leftVertices[i] / 6.00f) - 0.55f;
		rightVertices[i] = -leftVertices[i];
		++i;

		leftVertices[i] = (leftVertices[i] / 7.00f) - 0.80f;
		rightVertices[i] = leftVertices[i];
		++i;
	}

	unsigned char *data = new unsigned char[sizeof(leftVertices) + sizeof(rightVertices)];
	memcpy(data, leftVertices, sizeof(leftVertices));
	memcpy(data + sizeof(leftVertices), rightVertices, sizeof(rightVertices));

	vao = new VertexArray;

	VertexBuffer *vbo = new VertexBuffer(data, sizeof(leftVertices) + sizeof(rightVertices));

	VertexBufferLayout *layout = new VertexBufferLayout();
	layout->pushFloat(2);

	vao->mapBufferToLayout(vbo, layout);
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[])
{
	/* by default the back ground color is black */
	glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

	bool singleFern = true;
	if (argc > 1)
	{
		if (atoi(argv[1]) == 2)
			singleFern = false;
	}

	if (singleFern){
		cout<<"Problem 1 (a) \n";
		createBuffersSingleFern();
	}else{
		cout<<"Problem 1 (b) \n";
		createBuffersDoubleFern();
	}

	shaderProgram = createShader("shaders/vertex.shader", "shaders/fragment.shader");
	glUseProgram(shaderProgram);

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glDrawArrays(GL_POINTS, 0, numVertices);

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
