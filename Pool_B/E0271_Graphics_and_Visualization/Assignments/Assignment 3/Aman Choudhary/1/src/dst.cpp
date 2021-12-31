#include "dst.h"
#include "file.h"
#include "marching_tetra.h"

extern ScalarField *scalarField;
extern unsigned int numPointsSide, numPointsPlane;
extern int currScalarValue;

DSTreeNode *createTree(int cuboidOrigin, int cubesX, int cubesY, int cubesZ) {

  DSTreeNode *treeNode = new DSTreeNode(cuboidOrigin, cubesX, cubesY, cubesZ);

  if (cubesX > 1) {
    int cubesLeftX = (cubesX + 1) / 2;
    int cubesRightX = cubesX / 2;
    int shiftX = cubesLeftX;

    treeNode->left = createTree(cuboidOrigin, cubesLeftX, cubesY, cubesZ);
    treeNode->right =
        createTree(cuboidOrigin + shiftX, cubesRightX, cubesY, cubesZ);
  } else if (cubesY > 1) {
    int cubesLeftY = (cubesY + 1) / 2;
    int cubesRightY = cubesY / 2;
    int shiftY = cubesLeftY * numPointsSide;

    treeNode->left = createTree(cuboidOrigin, 1, cubesLeftY, cubesZ);
    treeNode->right = createTree(cuboidOrigin + shiftY, 1, cubesRightY, cubesZ);
  } else if (cubesZ > 1) {
    int cubesLeftZ = (cubesZ + 1) / 2;
    int cubesRightZ = cubesZ / 2;
    int shiftZ = cubesLeftZ * numPointsPlane;

    treeNode->left = createTree(cuboidOrigin, 1, 1, cubesLeftZ);
    treeNode->right = createTree(cuboidOrigin + shiftZ, 1, 1, cubesRightZ);
  } else {
    int cubeIndices[8];

    cubeIndices[0] = cuboidOrigin;
    cubeIndices[1] = cuboidOrigin + 1;
    cubeIndices[2] = numPointsSide + cubeIndices[0];
    cubeIndices[3] = numPointsSide + cubeIndices[1];
    cubeIndices[4] = numPointsPlane + cubeIndices[0];
    cubeIndices[5] = numPointsPlane + cubeIndices[1];
    cubeIndices[6] = numPointsPlane + cubeIndices[2];
    cubeIndices[7] = numPointsPlane + cubeIndices[3];

    for (int i = 0; i < 8; ++i) {
      treeNode->minScalarVal =
          min(treeNode->minScalarVal,
              (scalarField->points[cubeIndices[i]]).scalarVal);
      treeNode->maxScalarVal =
          max(treeNode->maxScalarVal,
              (scalarField->points[cubeIndices[i]]).scalarVal);
    }

    treeNode->isUnitCube = true;
    // treeNode->printNode();
    return treeNode;
  }

  treeNode->minScalarVal =
      min(treeNode->minScalarVal,
          min(treeNode->left->minScalarVal, treeNode->right->minScalarVal));
  treeNode->maxScalarVal =
      max(treeNode->maxScalarVal,
          max(treeNode->left->maxScalarVal, treeNode->right->maxScalarVal));

//   treeNode->printNode();
  return treeNode;
}

void domainSearch(DSTreeNode *rootNode) {
  if (rootNode and rootNode->minScalarVal <= currScalarValue and
      currScalarValue <= rootNode->maxScalarVal) {

    if (rootNode->isUnitCube) {
      processCube(rootNode->cuboidOrigin);
    } else {
      domainSearch(rootNode->left);
      domainSearch(rootNode->right);
    }
  }
}