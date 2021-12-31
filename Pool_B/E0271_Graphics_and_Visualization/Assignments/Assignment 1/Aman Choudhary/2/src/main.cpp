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

unsigned int shaderProgram;
unsigned int numVertices, numIndices;
VertexArray *vao;
VertexBuffer *vbo;
IndexBuffer *ibo;
Transform *trans;
float translateX;
bool swapTransformOrder;

static void createBuffers()
{
	OffModel *model = readOffFile("../data/1duk.off");

	// Read vetices _____________________________________________________

	numVertices = model->numberOfVertices;
	unsigned int sizeVertices = numVertices * sizeof(Vertex);

	unsigned char *vertices = new unsigned char[sizeVertices];
	memcpy(vertices, model->vertices, sizeVertices);

	normalizeBuffer(vertices, numVertices, 3, -0.60f, +0.60f);

	// Read indices _____________________________________________________

	numIndices = model->numberOfPolygons * 3;
	unsigned int sizeIndices = numIndices * sizeof(unsigned int);

	unsigned char *indices = new unsigned char[sizeIndices];

	unsigned char *temp = indices;
	for (int i = 0; i < model->numberOfPolygons; ++i)
	{
		memcpy(temp, (model->polygons[i]).v, 3 * sizeof(unsigned int));
		temp += 3 * sizeof(unsigned int);
	}

	// Render __________________________________________________________

	vao = new VertexArray;

	vbo = new VertexBuffer(vertices, sizeVertices);

	VertexBufferLayout *layout = new VertexBufferLayout();
	layout->pushFloat(3);

	vao->mapBufferToLayout(vbo, layout);

	ibo = new IndexBuffer(indices, numIndices);
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[])
{
	/* by default the back ground color is black */
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

	createBuffers();

	shaderProgram = createShader();
	glUseProgram(shaderProgram);

	swapTransformOrder = false;
	trans = new Transform();
	trans->axis = Vector3f(0.0f, 0.0f, 1.0f);

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	trans->setIdentity();

	trans->translate.x = translateX;
	trans->setTranslate();

	trans->angle = rotation;
	trans->setRotate();

	if (swapTransformOrder)
	{
		trans->transformMat = trans->rotateMat * trans->transformMat;
		trans->transformMat = trans->translateMat * trans->transformMat;
	}
	else
	{
		trans->transformMat = trans->translateMat * trans->transformMat;
		trans->transformMat = trans->rotateMat * trans->transformMat;
	}

	int uf_transform_location = glGetUniformLocation(shaderProgram, "uf_transform");
	// ASSERT(uf_transform_location != -1);
	glUniformMatrix4fv(uf_transform_location, 1, GL_TRUE, &((trans->transformMat).m[0][0]));

	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, NULL);

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
