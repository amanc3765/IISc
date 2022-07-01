#include "main.h"
#include "job.h"

extern Job *jobList[TOTAL_NUM_JOBS];
string projectPath = "/home/aman/Desktop/MTech-Project/";
string statsPath = projectPath + "mgpusim-v2/samples/analysis/stats/";

unordered_set<int> jobsToRun({
    // 0, 1, 2, 3, 4, 
    // 5, 6, 7,
    // 8, 9, 10, 11, 12, 13, 14, 15,
    // 16, 17, 18, 19, 20,
    // 21, 22, 
    // 23, 24, 
    // 25, 26, 27, 28, 29, 30, 31,
    // 32, 33, 34, 35, 36, 37, 38, 39,
    // 40, 41, 42, 43, 44, 45, 46, 47,
    // 48, 49, 50, 51, 52, 53, 54, 55
    27
    // , 51, 53
});

void createJobs()
{
    for (int i = 0; i < MAX_NUM_TLBS; ++i)
        jobList[i] = NULL;

    int id = 0;
    for (int numAggregatedTLBs = 1; numAggregatedTLBs <= MAX_NUM_TLBS; numAggregatedTLBs *= 2)
    {
        for (int numClusters = 1; numClusters <= numAggregatedTLBs; numClusters *= 2)
        {
            if (jobsToRun.find(id) != jobsToRun.end())
                jobList[id] = new Job(id, numAggregatedTLBs, numClusters, false);
            ++id;
        }
    }

    for (int numAggregatedTLBs = 1; numAggregatedTLBs <= MAX_NUM_TLBS; numAggregatedTLBs *= 2)
    {
        for (int numClusters = 1; numClusters <= numAggregatedTLBs; numClusters *= 2)
        {
            if (jobsToRun.find(id) != jobsToRun.end())
                jobList[id] = new Job(id, numAggregatedTLBs, numClusters, true);
            ++id;
        }
    }

    // for (int i = 0; i < TOTAL_NUM_JOBS; ++i)
    // {
    // if (jobList[i])
    //     (jobList[i])->printJob();
    // }
}

int openOutputFile(string fileName)
{
    fileName = statsPath + fileName;
    // cout << fileName << endl;

    int fd = open(fileName.c_str(), O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR);
    if (fd < 0)
    {
        cout << "Could not open file: " << fileName << endl;
        exit(0);
    }

    return fd;
}

int spawn(char *process_name, char **arguments, int fd)
{
    pid_t child_id = fork();

    if (child_id != 0) // PARENT
        return child_id;
    else
    { // CHILD
        dup2(fd, STDOUT_FILENO);
        close(fd);
        execvp(process_name, arguments);
    }
}
