#ifndef DST_H
#define DST_H

#include "header.h"

class DSTreeNode {
public:
  int minScalarVal, maxScalarVal, cuboidOrigin, cubesX, cubesY, cubesZ;
  bool isUnitCube;
  DSTreeNode *left, *right;

  DSTreeNode(int _cuboidOrigin, int _cubesX, int _cubesY, int _cubesZ) {
    minScalarVal = 255;
    maxScalarVal = 0;
    cuboidOrigin = _cuboidOrigin;
    cubesX = _cubesX;
    cubesY = _cubesY;
    cubesZ = _cubesZ;
    left = NULL;
    right = NULL;
    isUnitCube = false;
  }

  void printNode() {
    cout << setw(5) << cuboidOrigin << " [" << setw(5) << cubesX << ","
         << setw(5) << cubesY << "," << setw(5) << cubesZ << "], [" << setw(5)
         << minScalarVal << " : " << setw(5) << maxScalarVal << "]" << endl;
  }
};

DSTreeNode *createTree(int cuboidOrigin, int cubesX, int cubesY, int cubesZ);
void domainSearch(DSTreeNode *rootNode);

#endif