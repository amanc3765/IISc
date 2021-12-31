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

unsigned int numIndicesCube, numIndicesSphere, shaderProgram;
VertexArray *vaoCube, *vaoSphere;
VertexBuffer *vboCube, *vboSphere;
IndexBuffer *iboCube, *iboSphere;

Vector4f cubeColor, sphereColor, spherePosition, sphereVelocity;
Vector3f scaleSphere;
Transform *transCube, *transSphere, *transCamera;
Matrix4f *persProjection;
Plane *cubeFaces[6];

float sphereRadius, sphereSpeed, scaleSphereParam;

static void createBuffers()
{
	// Cube ____________________________________________

	unsigned int numVerticesCube = 8;
	Vector3f verticesCube[numVerticesCube];
	verticesCube[0] = {-0.5f, -0.5f, -0.5f};
	verticesCube[1] = {-0.5f, -0.5f, +0.5f};
	verticesCube[2] = {-0.5f, +0.5f, -0.5f};
	verticesCube[3] = {-0.5f, +0.5f, +0.5f};
	verticesCube[4] = {+0.5f, -0.5f, -0.5f};
	verticesCube[5] = {+0.5f, -0.5f, +0.5f};
	verticesCube[6] = {+0.5f, +0.5f, -0.5f};
	verticesCube[7] = {+0.5f, +0.5f, +0.5f};

	numIndicesCube = 24;
	unsigned int indicesCube[numIndicesCube] = {
		0, 2, 2, 3, 3, 1, 1, 0,
		7, 6, 6, 4, 4, 5, 5, 7,
		0, 4, 3, 7, 1, 5, 2, 6};

	// Sphere ___________________________________________

	OffModel *model = readOffFile("../data/sphere.off");

	unsigned int numVerticesSphere = model->numberOfVertices;
	unsigned int sizeVerticesSphere = numVerticesSphere * sizeof(Vertex);
	unsigned char *verticesSphere = new unsigned char[sizeVerticesSphere];

	memcpy(verticesSphere, model->vertices, sizeVerticesSphere);
	normalizeBuffer(verticesSphere, numVerticesSphere, 3, -1 * sphereRadius, sphereRadius);

	numIndicesSphere = model->numberOfPolygons * 3;
	unsigned int sizeIndicesSphere = numIndicesSphere * sizeof(unsigned int);
	unsigned char *indicesSphere = new unsigned char[sizeIndicesSphere];

	unsigned char *currPolygon = indicesSphere;
	for (int i = 0; i < model->numberOfPolygons; ++i)
	{
		memcpy(currPolygon, (model->polygons[i]).v, 3 * sizeof(unsigned int));
		currPolygon += 3 * sizeof(unsigned int);
	}

	// Allocate buffers for cube __________________________________

	vaoCube = new VertexArray;

	vboCube = new VertexBuffer(verticesCube, sizeof(verticesCube));

	VertexBufferLayout *layoutCube = new VertexBufferLayout();
	layoutCube->pushFloat(3);

	vaoCube->mapBufferToLayout(vboCube, layoutCube);

	iboCube = new IndexBuffer(indicesCube, numIndicesCube);

	// Allocate buffers for sphere __________________________________

	vaoSphere = new VertexArray;

	vboSphere = new VertexBuffer(verticesSphere, sizeVerticesSphere);

	VertexBufferLayout *layoutSphere = new VertexBufferLayout();
	layoutSphere->pushFloat(3);

	vaoSphere->mapBufferToLayout(vboSphere, layoutSphere);

	iboSphere = new IndexBuffer(indicesSphere, numIndicesSphere);
}

void initValues()
{
	// Cube _______________________________________________________________

	cubeFaces[0] = new Plane({+1.0f, 0.0f, 0.0f}, {-0.5f, -0.5f, -0.5f});
	cubeFaces[1] = new Plane({-1.0f, 0.0f, 0.0f}, {+0.5f, +0.5f, +0.5f});
	cubeFaces[2] = new Plane({0.0f, +1.0f, 0.0f}, {-0.5f, -0.5f, -0.5f});
	cubeFaces[3] = new Plane({0.0f, -1.0f, 0.0f}, {+0.5f, +0.5f, +0.5f});
	cubeFaces[4] = new Plane({0.0f, 0.0f, +1.0f}, {-0.5f, -0.5f, -0.5f});
	cubeFaces[5] = new Plane({0.0f, 0.0f, -1.0f}, {+0.5f, +0.5f, +0.5f});

	cubeColor = {1.0f, 1.0f, 1.0f, 1.0f};

	transCube = new Transform();
	transCube->axis = Vector3f(0.0f, 1.0f, 0.0f);

	// Sphere _____________________________________________________________

	sphereRadius = 0.08f;
	sphereSpeed = 0.1f;
	sphereColor = {1.0f, 0.0f, 0.0f, 1.0f};
	spherePosition = {0.0f, 0.0f, 0.0f, 1.0f};
	sphereVelocity = {getRandom(), getRandom(), getRandom(), 1.0f};
	scaleSphere = {1.0f, 1.0f, 1.0f};
	scaleSphereParam = 0.8f;

	transSphere = new Transform();

	// Camera _____________________________________________________________

	transCamera = new Transform();

	transCamera->translate = Vector3f(0.0f, 0.0f, +1.8f);
	transCamera->setTranslate();

	persProjection = new Matrix4f();
	PersProjInfo proj(90.0f, 1.0f, 1.0f, +1.0f, -1.0f);
	persProjection->InitPersProjTransform(proj);

	transCamera->setIdentity();
	transCamera->transformMat.Print();
	transCamera->transformMat = transCamera->translateMat * transCamera->transformMat;
	transCamera->transformMat = (*persProjection) * transCamera->transformMat;
	cout<<endl;
	transCamera->transformMat.Print();
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[])
{
	/* by default the back ground color is black */
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

	initValues();
	createBuffers();

	shaderProgram = createShader("shaders/vertex.shader", "shaders/fragment.shader");
	glUseProgram(shaderProgram);

	/* set to draw in window based on depth  */
	glEnable(GL_DEPTH_TEST);
}

void updateWorld()
{
	// Translate sphere tentatively ________________________________________________________

	Vector4f tempSpherePosition;

	sphereVelocity.Normalize();
	transSphere->translate = Vector3f(sphereVelocity.x, sphereVelocity.y, sphereVelocity.z);
	transSphere->translate *= sphereSpeed;
	transSphere->setTranslate();

	tempSpherePosition.w = 1.0f;
	tempSpherePosition = transSphere->translateMat * spherePosition;

	// Calculate reflection normal ________________________________________________________

	Vector4f reflectionNormal(0.0f, 0.0f, 0.0f, 0.0f);

	transCube->angle -= 0.01f;
	transCube->setRotate();

	for (int i = 0; i < 6; ++i)
	{
		cubeFaces[i]->normal = (transCube->rotateMat * cubeFaces[i]->normal).Normalize();
		cubeFaces[i]->point = transCube->rotateMat * cubeFaces[i]->point;
		cubeFaces[i]->calculateCoeffs();

		if (cubeFaces[i]->distance(tempSpherePosition) < sphereRadius)
			reflectionNormal += cubeFaces[i]->normal;
	}

	reflectionNormal.Normalize();
	reflectionNormal *= (2.0f * dotProduct(sphereVelocity, reflectionNormal));
	sphereVelocity -= reflectionNormal;

	// Scale sphere ________________________________________________________________________

	if (scaleSphere.x < 1.0f)
		scaleSphere.x += 0.01f;
	else if (reflectionNormal.x != 0.0f)
		scaleSphere.x = scaleSphereParam;

	if (scaleSphere.y < 1.0f)
		scaleSphere.y += 0.01f;
	else if (reflectionNormal.y != 0.0f)
		scaleSphere.y = scaleSphereParam;

	if (scaleSphere.z < 1.0f)
		scaleSphere.z += 0.01f;
	else if (reflectionNormal.z != 0.0f)
		scaleSphere.z = scaleSphereParam;

	transSphere->scale = Vector3f(scaleSphere.x, scaleSphere.y, scaleSphere.z);
	transSphere->setScale();

	// Translate sphere ________________________________________________________________________

	sphereVelocity.Normalize();
	transSphere->translate = Vector3f(sphereVelocity.x, sphereVelocity.y, sphereVelocity.z);
	transSphere->translate *= sphereSpeed;
	transSphere->setTranslate();

	spherePosition.w = 1.0f;
	spherePosition = transSphere->translateMat * spherePosition;

	transSphere->translate = Vector3f(spherePosition.x, spherePosition.y, spherePosition.z);
	transSphere->setTranslate();
}

void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	updateWorld();

	// Render cube ________________________________________________________________________

	transCube->setIdentity();
	transCube->transformMat = transCube->rotateMat * transCube->transformMat;
	transCube->transformMat = transCamera->transformMat * transCube->transformMat;

	int uf_transform_location = glGetUniformLocation(shaderProgram, "uf_transform");
	glUniformMatrix4fv(uf_transform_location, 1, GL_TRUE, &((transCube->transformMat).m[0][0]));

	int uf_color_location = glGetUniformLocation(shaderProgram, "uf_color");
	glUniform4f(uf_color_location, cubeColor.x, cubeColor.y, cubeColor.z, cubeColor.w);

	vaoCube->Bind();
	iboCube->Bind();

	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	glDrawElements(GL_LINES, numIndicesCube, GL_UNSIGNED_INT, NULL);

	// Render sphere ________________________________________________________________________

	transSphere->setIdentity();
	transSphere->transformMat = transSphere->scaleMat * transSphere->transformMat;
	transSphere->transformMat = transSphere->translateMat * transSphere->transformMat;
	// transSphere->transformMat = transCube->rotateMat * transSphere->transformMat;
	transSphere->transformMat = transCamera->transformMat * transSphere->transformMat;

	uf_transform_location = glGetUniformLocation(shaderProgram, "uf_transform");
	glUniformMatrix4fv(uf_transform_location, 1, GL_TRUE, &((transSphere->transformMat).m[0][0]));

	uf_color_location = glGetUniformLocation(shaderProgram, "uf_color");
	glUniform4f(uf_color_location, sphereColor.x, sphereColor.y, sphereColor.z, sphereColor.w);

	vaoSphere->Bind();
	iboSphere->Bind();

	glLineWidth(2.0f);
	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	glDrawElements(GL_TRIANGLES, numIndicesSphere, GL_UNSIGNED_INT, NULL);

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
