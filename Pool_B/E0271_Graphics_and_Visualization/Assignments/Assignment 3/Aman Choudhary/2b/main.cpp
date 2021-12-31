#include <bits/stdc++.h>
using namespace std;

typedef long double ld;
ld dimX, dimY, placeHolder;

ld **createGrid(int dimX, int dimY) {
  ld **grid = new ld *[dimX];
  for (int i = 0; i < dimX; ++i) {
    grid[i] = new ld[dimY];
  }

  return grid;
}

void printGrid(ld **grid) {
  for (int i = 0; i < dimX; ++i) {
    cout << i << " : ";
    for (int j = 0; j < dimY; ++j) {
      cout << setw(15) << setprecision(12) << grid[i][j] << " ";
    }
    cout << "\n\n";
  }
}

bool isLocalMaxima(ld **grid, int i, int j) {
  if (i == 0 or i == dimX - 1 or j == 0 or j == dimY - 1) {
    return false;
  }

  for (int x = i - 1; x <= i + 1; ++x) {
    for (int y = j - 1; y <= j + 1; ++y) {
      if (grid[x][y] < 0.0f) {
        return false;
      }
    }
  }

  for (int x = i - 1; x <= i + 1; ++x) {
    for (int y = j - 1; y <= j + 1; ++y) {
      if (x == i and y == j) {
        continue;
      } else if (grid[i][j] < grid[x][y]) {
        return false;
      }
    }
  }

  return true;
}

void printNeighbors(ld **grid, int i, int j) {

  for (int x = i - 1; x <= i + 1; ++x) {
    for (int y = j - 1; y <= j + 1; ++y) {
      cout << setw(20) << "[" << x << "," << y << "] : " << grid[x][y] << " ";
    }
    cout << "\n\n";
  }

  cout << endl;
}

int main() {
  ifstream input;
  input.open("../data/ass3_ocean.vtk");

  ofstream output;
  output.open("../data/ass3_ocean_output.vtk");

  string line;
  for (int i = 0; i < 10; ++i) {
    getline(input, line);
    output << line << endl;
    ;
  }

  dimX = 253;
  dimY = 253;
  ld **inputGrid = createGrid(dimX, dimY);
  for (int i = 0; i < dimX; ++i) {
    for (int j = 0; j < dimY; ++j) {
      input >> inputGrid[i][j];
    }
  }

  output << setprecision(12);
  ld specialVal = 10.0;
  int k = 0;
  for (int i = 0; i < dimX; ++i) {
    for (int j = 0; j < dimY; ++j) {
      if (isLocalMaxima(inputGrid, i, j)) {
        cout << ++k << " -> [" << i << "," << j << "] : " << inputGrid[i][j]
             << endl;
        inputGrid[i][j] = specialVal;
      }
      output << inputGrid[i][j] << " ";
    }
  }

  while (getline(input, line)) {
    output << line << endl;
  }

  input.close();
  output.close();

  return 0;
}

// # vtk DataFile Version 4.2
// vtk output
// ASCII
// DATASET STRUCTURED_POINTS
// DIMENSIONS 253 253 1
// SPACING 0.0833333 0.0833333 1
// ORIGIN 75 5 0
// POINT_DATA 64009
// FIELD FieldData 1
// zos 1 64009 double
// METADATA
// INFORMATION 0