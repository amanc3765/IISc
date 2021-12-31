// rawFileParser.cpp

/*NOTE: ensure that the filenames are in the following format
<name>_<XxYxZ>_<format>.raw
<name> shouldn't have '_','x','.' 
*/

#include <bits/stdc++.h>
using namespace std;

/*helper function to split input filename, source: stackoverflow*/
vector<string> split(const string &s, char delim)
{
    vector<string> tokenList;
    stringstream ss(s);
    string token;

    while (getline(ss, token, delim))
        tokenList.push_back(token);

    return tokenList;
}

int main(int argc, char *argv[])
{

    /*quit if the number of arguments is improper*/
    if (argc < 4)
    {
        std::cerr << "Usage: " << argv[0] << " <rawfile> <outputfile> <spacing>" << std::endl;
        return 1;
    }

    // Parse each argument separately
    string sourceFile(argv[1]);
    string destFile(argv[2]);
    string spacing(argv[3]);

    // Split source file using '_' as delimiter to get the x,y,z extants and type of data
    vector<string> params = split(sourceFile, '_');
    string xyzext = params[1];
    string formatspec = params[2];

    // Split the xyz extants using 'x' as delimiter and convert them to integers
    vector<string> xyz = split(xyzext, 'x');
    int x = stoi(xyz[0]);
    int y = stoi(xyz[1]);
    int z = stoi(xyz[2]);
    int size = x * y * z;

    // Remove the .raw extension
    vector<string> format = split(formatspec, '.');
    vector<string> space = split(spacing, 'x');

    // Read file
    ifstream infile;
    infile.open(sourceFile, ios::binary | ios::in);

    char *datac;
    int *datai;

    if (format[0] == "uint8")
    {
        datac = new char[size];
        infile.read(datac, size);
    }
    else if (format[0] == "uint16")
    {
        datai = new int[size];
        infile.read((char *)datai, size);
    }

    infile.close();

    /*write the contents of the buffer to the destination file along with other details
    x,y,z extants
    origin
    stepsize

    The output file has the data varies along these indices 
    [x y z]
    0 0 0
    1 0 0 
    2 0 0
    ...
    ...
    _ 1 0
    ...
    ...
    _ _ 1
    ...
    ...
    1 1 1
    */

    ofstream outfile;
    outfile.open(destFile, ios::out);
    outfile << x << " " << y << " " << z << endl;
    outfile << "origin 0 0 0\n";
    outfile << "stepsize " << space[0] << " " << space[1] << " " << space[2] << endl;

    int dataVal;
    for (int i = 0; i < size; i++)
    {
        if (format[0] == "uint8")
        {
            dataVal = datac[i];
            if (dataVal < 0)
                dataVal = dataVal + 256;
        }
        else if (format[0] == "uint16")
        {
            dataVal = datai[i];
            if (dataVal < 0)
                dataVal = dataVal + 65536;
        }

        outfile << dataVal << "\n";
    }
    outfile.close();

    //free the allocated buffer.
    if (format[0] == "uint8")
        delete[] datac;
    else if (format[0] == "uint16")
        delete[] datai;

    return 0;
}