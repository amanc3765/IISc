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
Transform *transObj, *transLight;
Matrix4f rotate30;
Vector4f lightPos;
bool rotateLight, rotateObject;
float objectRotationAngle, lightRotationAngle;

static void addVector(Vector3f *a, Vector3f *b)
{
	a->x += b->x;
	a->y += b->y;
	a->z += b->z;
	a->Normalize();
}

static Vector3f computeNormal(Vertex a, Vertex b, Vertex c)
{
	Vector3f vec1, vec2, crossProduct;

	vec1.x = b.x - a.x;
	vec1.y = b.y - a.y;
	vec1.z = b.z - a.z;

	vec2.x = c.x - a.x;
	vec2.y = c.y - a.y;
	vec2.z = c.z - a.z;

	crossProduct.x = (vec1.y * vec2.z) - (vec1.z * vec2.y);
	crossProduct.y = (vec1.z * vec2.x) - (vec1.x * vec2.z);
	crossProduct.z = (vec1.x * vec2.y) - (vec1.y * vec2.x);

	return crossProduct.Normalize();
}

static void createBuffers()
{
	OffModel *model = readOffFile("../data/1duk.off");

	// Read data ________________________________________________________

	numVertices = model->numberOfVertices;
	numIndices = model->numberOfPolygons * 3;

	// Vertex Positions
	unsigned int sizePositions = numVertices * sizeof(Vertex);
	Vector3f positions[numVertices];
	memcpy(positions, model->vertices, sizePositions);
	normalizeBuffer(positions, numVertices, 3, -0.60f, +0.60f);

	// Vertex Normals
	unsigned int sizeNormals = numVertices * sizeof(Vector3f);
	Vector3f normals[numVertices], normalPolygon;

	for (int i = 0; i < numVertices; ++i)
		normals[i].x = normals[i].y = normals[i].z = 0;

	// Indices
	unsigned int sizeIndices = numIndices * sizeof(unsigned int);
	unsigned char indices[sizeIndices];

	unsigned char *currIndex = indices;
	for (int i = 0; i < model->numberOfPolygons; ++i)
	{
		memcpy(currIndex, (model->polygons[i]).v, 3 * sizeof(unsigned int));
		currIndex += 3 * sizeof(unsigned int);
	}

	// Compute Normals
	unsigned int v1, v2, v3;
	for (int i = 0; i < model->numberOfPolygons; ++i)
	{
		v1 = (model->polygons[i]).v[0];
		v2 = (model->polygons[i]).v[1];
		v3 = (model->polygons[i]).v[2];

		normalPolygon = computeNormal(model->vertices[v1], model->vertices[v2], model->vertices[v3]);

		addVector(&normals[v1], &normalPolygon);
		addVector(&normals[v2], &normalPolygon);
		addVector(&normals[v3], &normalPolygon);
	}

	// Create vertices data
	unsigned int sizeVertices = numVertices * 6 * sizeof(float);
	unsigned char vertices[sizeVertices];
	unsigned char *currVertex = vertices;
	for (int i = 0; i < numVertices; ++i)
	{
		memcpy(currVertex, positions[i], sizeof(Vector3f));
		currVertex += sizeof(Vector3f);

		memcpy(currVertex, normals[i], sizeof(Vector3f));
		currVertex += sizeof(Vector3f);
	}

	// Create buffers __________________________________________________________

	vao = new VertexArray;

	vbo = new VertexBuffer(vertices, sizeVertices);

	VertexBufferLayout *layout = new VertexBufferLayout();
	layout->pushFloat(3);
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

	shaderProgram = createShader("shaders/vertex.shader", "shaders/fragment.shader");
	glUseProgram(shaderProgram);

	transObj = new Transform();
	transObj->axis = Vector3f(1.0f, 0.0f, 0.0f);
	transObj->angle = ToRadian(+20);
	transObj->setRotate();
	rotate30 = transObj->rotateMat;
	transObj->axis = Vector3f(0.0f, 1.0f, 0.0f);
	rotateObject = false;

	transLight = new Transform();
	transLight->axis = Vector3f(0.0f, 1.0f, 0.0f);
	rotateLight = false;

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	// Tranformation for object ___________________________________________

	transObj->setIdentity();
	transObj->transformMat = rotate30 * transObj->transformMat;

	transObj->angle = objectRotationAngle;
	if (rotateObject)
		objectRotationAngle += ToRadian(-0.5);
	transObj->setRotate();
	transObj->transformMat = transObj->rotateMat * transObj->transformMat;

	int uf_transform_location = glGetUniformLocation(shaderProgram, "uf_transform");
	glUniformMatrix4fv(uf_transform_location, 1, GL_TRUE, &((transObj->transformMat).m[0][0]));

	// Tranformation for light source ____________________________________

	lightPos = Vector4f(3.0, 3.0, -3.0, 0.0);
	transLight->angle = lightRotationAngle;
	if (rotateLight)
		lightRotationAngle += ToRadian(+0.5);
	transLight->setRotate();
	lightPos = transLight->rotateMat * lightPos;

	int uf_lightPos_location = glGetUniformLocation(shaderProgram, "uf_lightPos");
	glUniform3f(uf_lightPos_location, lightPos.x, lightPos.y, lightPos.z);

	// Render object _____________________________________________________

	// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
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
