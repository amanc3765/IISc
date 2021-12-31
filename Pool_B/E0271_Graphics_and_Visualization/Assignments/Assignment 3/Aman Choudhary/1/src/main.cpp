#include "dst.h"
#include "error.h"
#include "file.h"
#include "header.h"
#include "index_buffer.h"
#include "marching_tetra.h"
#include "math.h"
#include "shader.h"
#include "transform.h"
#include "vertex_array.h"
#include "vertex_buffer.h"
#include "vertex_buffer_layout.h"
#include "window.h"

VertexArray *vaoIsoSurface, *vaoCube;
VertexBuffer *vboIsoSurface, *vboCube;
VertexBufferLayout *layoutIsoSurface, *layoutCube;
IndexBuffer *iboCube, *iboIsoSurface;
ScalarField *scalarField;
Matrix4f *persProjection;
Transform *transPerspective, *transRotate, *transCamera;
DSTreeNode *rootNode;
unsigned int shaderProgramIsoSurface, shaderProgramCube;
unsigned int numCubes, numTetrahedrons, numPointsSide, numPointsPlane;
unsigned int numVerticesIsoSurface, sizeVerticesIsoSurface,
    currIsoSurfaceVertex, numIndicesCube;
unsigned int *indicesCube;
Vector3f *verticesIsoSurface;
float boxSize, rotateAngle;
int currScalarValue;

static void createBuffersCube() {
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
      0, 2, 2, 3, 3, 1, 1, 0, 7, 6, 6, 4, 4, 5, 5, 7, 0, 4, 3, 7, 1, 5, 2, 6};

  // Create Buffers _____________________________________________

  vaoCube = new VertexArray;
  vboCube = new VertexBuffer(verticesCube, sizeof(verticesCube));
  layoutCube = new VertexBufferLayout();
  layoutCube->pushFloat(3);
  vaoCube->mapBufferToLayout(vboCube, layoutCube);
  iboCube = new IndexBuffer(indicesCube, numIndicesCube);
}

void createBuffersIsoSurface() {
  currIsoSurfaceVertex = 0;
  domainSearch(rootNode);
  sizeVerticesIsoSurface = currIsoSurfaceVertex * sizeof(Vector3f);

  // Create Buffers _____________________________________________

  vaoIsoSurface = new VertexArray;
  vboIsoSurface = new VertexBuffer(verticesIsoSurface, sizeVerticesIsoSurface);
  layoutIsoSurface = new VertexBufferLayout();
  layoutIsoSurface->pushFloat(3);
  vaoIsoSurface->mapBufferToLayout(vboIsoSurface, layoutIsoSurface);
}

/* pre:  glut window has been initialized
   post: model has been initialized */
void initModel(int argc, char *argv[]) {
  /* by default the back ground color is black */
  glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

  boxSize = 0.75f;
  currScalarValue = 25;
  rotateAngle = ToRadian(90);

  // scalarField = readScalarFile("../data/test.txt");
  // scalarField =
  //     readScalarFile("../data/skull_256x256x256_uint8_Scalar_Values.txt");
  // scalarField =
  //     readScalarFile("../data/teapot.txt");
  scalarField =
      readScalarFile("../data/lobster.txt");

  // scalarField =
  //     readScalarFile("../data/hydrogen_128x128x128_uint8_Scalar_values.txt");

  numPointsSide = scalarField->side;
  numPointsPlane = scalarField->side * scalarField->side;

  numCubes = (scalarField->side - 1) * (scalarField->side - 1) *
             (scalarField->side - 1);
  numTetrahedrons = numCubes * 6;

  numVerticesIsoSurface = numTetrahedrons * 6;
  sizeVerticesIsoSurface = numVerticesIsoSurface * sizeof(Vector3f);
  verticesIsoSurface = new Vector3f[numVerticesIsoSurface];

  rootNode = createTree(0, scalarField->side - 1, scalarField->side - 1,
                        scalarField->side - 1);
  createBuffersIsoSurface();
  createBuffersCube();

  shaderProgramIsoSurface = createShader("shaders/vertexIsoSurface.shader",
                                         "shaders/fragmentIsoSurface.shader");
  shaderProgramCube =
      createShader("shaders/vertexCube.shader", "shaders/fragmentCube.shader");

  // Transformations
  // _____________________________________________________________

  transPerspective = new Transform();
  transRotate = new Transform();
  transCamera = new Transform();

  transPerspective->translate = Vector3f(0.0f, 0.0f, +2.2f);
  transPerspective->setTranslate();

  persProjection = new Matrix4f();
  PersProjInfo proj(90.0f, 1.0f, 1.0f, +1.0, -1.0f);
  persProjection->InitPersProjTransform(proj);

  transPerspective->setIdentity();
  transPerspective->transformMat =
      transPerspective->translateMat * transPerspective->transformMat;
  transPerspective->transformMat =
      (*persProjection) * transPerspective->transformMat;

  transRotate = new Transform();
  transRotate->axis = Vector3f(0.0, 1.0, 0.0);

  /* set to draw in window based on depth  */
  glEnable(GL_DEPTH_TEST);
}

void onDisplay() {
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

  transRotate->angle = rotateAngle;
  transRotate->setRotate();
  transCamera->transformMat =
      transPerspective->transformMat * transRotate->rotateMat;

  // Render IsoSurface _________________________________________
  glUseProgram(shaderProgramIsoSurface);

  int ufLocationIsoSurface =
      glGetUniformLocation(shaderProgramIsoSurface, "uf_transform_isosurface");
  glUniformMatrix4fv(ufLocationIsoSurface, 1, GL_TRUE,
                     &((transCamera->transformMat).m[0][0]));

  vaoIsoSurface->Bind();

  glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
  glDrawArrays(GL_TRIANGLES, 0, numVerticesIsoSurface);

  // Render cube ________________________________________________
  glUseProgram(shaderProgramCube);

  int ufLocationCube =
      glGetUniformLocation(shaderProgramCube, "uf_transform_cube");
  glUniformMatrix4fv(ufLocationCube, 1, GL_TRUE,
                     &((transCamera->transformMat).m[0][0]));

  vaoCube->Bind();
  iboCube->Bind();

  glLineWidth(1.0f);
  glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
  glDrawElements(GL_LINES, numIndicesCube, GL_UNSIGNED_INT, NULL);

  glutSwapBuffers();
}

void initGlut(int argc, char **argv) {
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

void initGlew() {
  // Must be done after glut is initialized!
  GLenum res = glewInit();
  if (res != GLEW_OK) {
    fprintf(stderr, "Error: '%s'\n", glewGetErrorString(res));
    exit(0);
  }
}

int main(int argc, char **argv) {
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