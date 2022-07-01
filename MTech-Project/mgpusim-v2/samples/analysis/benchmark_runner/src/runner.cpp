#include "main.h"
#include "benchmark.h"
#include "job.h"
#include "utility.h"

Job *jobList[TOTAL_NUM_JOBS];
char *arguments[100];

extern string projectPath;
extern map<benchmark, pair<string, string>> benchmarkMap;
extern benchmark benchmarkID;
int totalJobsToRun, jobsLeft, numConcurrentJobs;

void waitForChildren()
{
    int status;
    pid_t result = wait(&status);

    if (result != -1)
    {
        --numConcurrentJobs;
        --jobsLeft;

        int jobCompleted = totalJobsToRun - jobsLeft;
        cout << "\n(" << jobCompleted << "/" << totalJobsToRun << ") done.\n";
    }
}

int main(int argc, char **argv)
{
    getBenchmarkID(argc, argv);
    getBenchmarkParams(argc, argv);

    string samplesPath = projectPath + "mgpusim-v2/samples/";
    string benchmarkName = benchmarkMap[benchmarkID].first;
    string benchmarkConfig = benchmarkMap[benchmarkID].second;
    string processName = samplesPath + benchmarkName + "/" + benchmarkName;
    string benchmarkStatsDir = samplesPath + "analysis/stats/" + benchmarkName + "/" + benchmarkConfig;
    if (mkdir(benchmarkStatsDir.c_str(), 0777) == -1)
        cerr << "Error mkdir() :  " << strerror(errno) << endl;

    createJobs();

    queue<Job *> jobQueue;
    for (int i = 0; i < TOTAL_NUM_JOBS; ++i)
    {
        if (jobList[i])
        {
            jobQueue.push(jobList[i]);
            ++totalJobsToRun;
        }
    }

    numConcurrentJobs = 0;
    jobsLeft = totalJobsToRun;
    while (!jobQueue.empty())
    {
        if (numConcurrentJobs < MAX_NUM_CONCURRENT_JOBS)
        {
            Job *currJob = jobQueue.front();
            jobQueue.pop();

            getArguments(currJob);
            int fd = openOutputFile(currJob->fileName);

            int childPID = spawn(stringToChar(processName), arguments, fd);
            ++numConcurrentJobs;

            cout << "Running job: " << childPID << " =====> ";
            currJob->printJob();
        }
        else
        {
            waitForChildren();
        }
    }

    while (jobsLeft > 0)
        waitForChildren();

    return 0;
}
