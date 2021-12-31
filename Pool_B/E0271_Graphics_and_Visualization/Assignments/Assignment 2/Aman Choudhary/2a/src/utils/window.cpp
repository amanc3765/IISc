#include "window.h"
#include "transform.h"
#include "vertex_buffer.h"
#include "file.h"

const char *windowTitle = "Sample";
bool isFullScreen = false, isAnimating = true;
unsigned int windowWidth = 800, windowHeight = 700;
unsigned int windowPositionX = 0, windowPositionY = 0;
extern float rotateAngle, boxSize, planeZ;
extern int currSlice;

/* 
 Callback Functions: 
 These functions are registered with the glut window and called when certain events occur.
 */
void initGlutCallbacks()
{
	/* tell glut how to display model */
	glutDisplayFunc(onDisplay);

	/* tell glutwhat to do when it would otherwise be idle */
	glutIdleFunc(onIdle);

	/* tell glut how to respond to changes in window size */
	glutReshapeFunc(onReshape);

	/* tell glut how to handle changes in window visibility */
	glutVisibilityFunc(onVisible);

	/* tell glut how to handle key presses */
	glutKeyboardFunc(onAlphaNumericKeyPress);
	glutSpecialFunc(onSpecialKeyPress);

	/* tell glut how to handle the mouse */
	glutMotionFunc(onMouseMotion);
	glutMouseFunc(onMouseButtonPress);
}

/* post: compute frames per second and display in window's title bar */
void computeFPS()
{
	static char *title = NULL;
	static int frameCount = 0;
	static int lastFrameTime = 0;
	int currentFrameTime;

	if (!title)
		title = (char *)malloc((strlen(windowTitle) + 20) * sizeof(char));

	frameCount++;
	currentFrameTime = glutGet((GLenum)(GLUT_ELAPSED_TIME));

	if (currentFrameTime - lastFrameTime > ONE_SECOND)
	{
		sprintf(title, "%s [ FPS: %4.2f ]",
				windowTitle,
				(frameCount * 1000.0) / (currentFrameTime - lastFrameTime));
		glutSetWindowTitle(title);
		lastFrameTime = currentFrameTime;
		frameCount = 0;
	}
}

/* pre:  glut window is not doing anything else
   post: scene is updated and re-rendered if necessary */
void onIdle()
{
	static int oldTime = 0;
	if (isAnimating)
	{
		int currentTime = glutGet((GLenum)(GLUT_ELAPSED_TIME));
		/* Ensures fairly constant framerate */
		if (currentTime - oldTime > ANIMATION_DELAY)
		{
			// do animation....

			oldTime = currentTime;
			/* compute the frame rate */
			computeFPS();
			/* notify window it has to be repainted */
			glutPostRedisplay();
		}
	}
}

/* pre:  glut window has been resized
 */
void onReshape(int width, int height)
{
	glViewport(0, 0, width, height);

	if (!isFullScreen)
	{
		windowWidth = width;
		windowHeight = height;
	}
	// update scene based on new aspect ratio....
}

/* pre:  glut window has just been iconified or restored 
   post: if window is visible, animate model, otherwise don't bother */
void onVisible(int state)
{
	if (state == GLUT_VISIBLE)
	{
		/* tell glut to show model again */
		glutIdleFunc(onIdle);
	}
	else
	{
		glutIdleFunc(NULL);
	}
}

/* pre:  key has been pressed
   post: scene is updated and re-rendered */
void onAlphaNumericKeyPress(unsigned char key, int x, int y)
{
	switch (key)
	{

	case '1':
		rotateAngle += 0.1f;
		break;

	case '2':
		rotateAngle -= 0.1f;
		break;

	case 'f':
		currSlice += 5;
		if (currSlice > 255)
			currSlice = 255;

		planeZ = currSlice / 255.0 - boxSize;
		break;

	case 'b':
		currSlice -= 5;
		if (currSlice < 0)
			currSlice = 0;

		planeZ = currSlice / 255.0 - boxSize;
		break;

	case 'r':
		rotateAngle = 0.00f;
		currSlice = 175;
		planeZ = currSlice / 255.0 - boxSize;
		break;

	/* quit! */
	case 'Q':
	case 'q':
	case 27:
		exit(0);
	}

	/* notify window that it has to be re-rendered */
	glutPostRedisplay();
}

/* pre:  arrow or function key has been pressed
   post: scene is updated and re-rendered */
void onSpecialKeyPress(int key, int x, int y)
{
	/* please do not change function of these keys */
	switch (key)
	{
		/* toggle full screen mode */
	case GLUT_KEY_F1:
		isFullScreen = !isFullScreen;
		if (isFullScreen)
		{
			windowPositionX = glutGet((GLenum)(GLUT_WINDOW_X));
			windowPositionY = glutGet((GLenum)(GLUT_WINDOW_Y));
			glutFullScreen();
		}
		else
		{
			glutReshapeWindow(windowWidth, windowHeight);
			glutPositionWindow(windowPositionX, windowPositionY);
		}
		break;
	}

	/* notify window that it has to be re-rendered */
	glutPostRedisplay();
}

/* pre:  mouse is dragged (i.e., moved while button is pressed) within glut window
   post: scene is updated and re-rendered  */
void onMouseMotion(int x, int y)
{
	/* notify window that it has to be re-rendered */
	glutPostRedisplay();
}

/* pre:  mouse button has been pressed while within glut window
   post: scene is updated and re-rendered */
void onMouseButtonPress(int button, int state, int x, int y)
{
	if (button == GLUT_LEFT_BUTTON && state == GLUT_DOWN)
	{
		// Left button pressed
	}
	if (button == GLUT_LEFT_BUTTON && state == GLUT_UP)
	{
		// Left button un pressed
	}

	/* notify window that it has to be re-rendered */
	glutPostRedisplay();
}
