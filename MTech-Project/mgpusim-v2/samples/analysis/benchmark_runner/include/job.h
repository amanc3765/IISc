#ifndef JOB_H
#define JOB_H

#include "main.h"

class Job
{
public:
    int id, numAggregatedTLBs, numClusters;
    bool isVIVT;
    string fileName;

    Job() {}

    Job(int _id, int _numAggregatedTLBs, int _numClusters, int _isVIVT)
    {
        id = _id;
        numAggregatedTLBs = _numAggregatedTLBs;
        numClusters = _numClusters;
        isVIVT = _isVIVT;
    }

    void printJob()
    {
        cout << "ID: " << setw(4) << id
             << "  numAggregatedTLBs: " << setw(4) << numAggregatedTLBs
             << "  numClusters: " << setw(4) << numClusters
             << "  isVIVT: " << isVIVT
             << "  FileName: " << fileName << endl;
    }
};

void createJobs();
int openOutputFile(string fileName);
int spawn(char *process_name, char **arguments, int fd);

#endif