#ifndef WINDOW_H
#define WINDOW_H

#include "header.h"

#define ANIMATION_DELAY 20
#define ONE_SECOND 1000

extern const char *windowTitle;
extern float rotation;
extern bool isAnimating, isFullScreen;
extern unsigned int windowWidth, windowHeight, windowPositionX, windowPositionY;

void initGlutCallbacks();
void computeFPS();
void onIdle();
void onReshape(int width, int height);
void onVisible(int state);
void onAlphaNumericKeyPress(unsigned char key, int x, int y);
void onSpecialKeyPress(int key, int x, int y);
void onMouseMotion(int x, int y);
void onMouseButtonPress(int button, int state, int x, int y);

#endif