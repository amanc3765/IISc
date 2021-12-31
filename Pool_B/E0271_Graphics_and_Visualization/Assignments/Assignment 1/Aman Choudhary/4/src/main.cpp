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
Plane *plane;

static void createBuffers()
{
	OffModel *model = readOffFile("../data/1duk.off");

	// Read vetices _____________________________________________________

	numVertices = model->numberOfVertices;
	unsigned int sizeVertices = numVertices * sizeof(Vector4f);
	unsigned char *vertices = new unsigned char[sizeVertices];

	float *currVertex = (float *)vertices;
	float temp = 0.0f;
	for (int i = 0; i < numVertices; ++i)
	{
		memcpy(currVertex, (const void *)&(model->vertices[i]), sizeof(Vertex));
		currVertex += 3;

		memcpy(currVertex, (const void *)&temp, sizeof(float));
		++currVertex;
	}

	normalizeBuffer(vertices, numVertices, 3, -0.60f, +0.60f);

	plane = new Plane({0.0f, 1.0f, 0.0f}, {0.0f, 0.0f, 0.0f});

	currVertex = (float *)vertices;
	for (int i = 0; i < numVertices; ++i)
	{
		*(currVertex + 3) = (plane->coeffs.x * (*(currVertex + 0)) +
							 plane->coeffs.y * (*(currVertex + 1)) +
							 plane->coeffs.z * (*(currVertex + 2)) +
							 plane->coeffs.w) >= 0;
		currVertex += 4;
	}

	// Read indices _____________________________________________________

	numIndices = model->numberOfPolygons * 3;
	unsigned int sizeIndices = numIndices * sizeof(unsigned int);
	unsigned char *indices = new unsigned char[sizeIndices];

	unsigned char *currPolygon = indices;
	unsigned int x, y, z, actualNumPolygons;

	currVertex = (float *)vertices;
	actualNumPolygons = 0;
	for (int i = 0; i < model->numberOfPolygons; ++i)
	{
		x = (model->polygons[i]).v[0] * 4 + 3;
		y = (model->polygons[i]).v[1] * 4 + 3;
		z = (model->polygons[i]).v[2] * 4 + 3;

		if (currVertex[x] == currVertex[y] && currVertex[y] == currVertex[z])
		{
			memcpy(currPolygon, (model->polygons[i]).v, 3 * sizeof(unsigned int));
			currPolygon += 3 * sizeof(unsigned int);
			++actualNumPolygons;
		}
	}
	numIndices = actualNumPolygons * 3;

	// Render __________________________________________________________

	vao = new VertexArray;

	vbo = new VertexBuffer(vertices, sizeVertices);

	VertexBufferLayout *layout = new VertexBufferLayout();
	layout->pushFloat(3);
	layout->pushFloat(1);

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

	trans = new Transform();

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	float shift = 0.15f;
	trans->translate = plane->normal * shift;
	trans->setTranslate();

	trans->angle = rotation;
	rotation += 0.01f;
	trans->setRotate();

	int uf_translate_location = glGetUniformLocation(shaderProgram, "uf_translate");
	glUniformMatrix4fv(uf_translate_location, 1, GL_TRUE, &((trans->translateMat).m[0][0]));

	int uf_rotate_location = glGetUniformLocation(shaderProgram, "uf_rotate");
	glUniformMatrix4fv(uf_rotate_location, 1, GL_TRUE, &((trans->rotateMat).m[0][0]));

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
