#include "main.h"

string charToString(char *c)
{
    string s = "";
    for (int i = 0; c[i] != '\0'; i++)
        s += c[i];

    return s;
}

char *stringToChar(string s)
{
    char *c = new char[s.length() + 1];

    int i;
    for (i = 0; i < s.length(); ++i)
        c[i] = s[i];
    c[i] = '\0';

    return c;
}

void panic(string s)
{
    cout << s << endl;
    exit(0);
}