/*

Submitted by: AMAN CHOUDHARY [17920] (amanc@iisc.ac.in)

*/

#include <pthread.h>

#define NUM_THREADS 4
#define BATCH 32

int * a, * b, ** o, n;

//Task performed done by each thread
void * work(void * threadid) {

    long tid = (long) threadid;
    int rowsPerThread = n / NUM_THREADS;
    int start = tid * rowsPerThread;
    int end = start + rowsPerThread;
    int i, j, x, y, xlim, ylim, xIndex, yIndex, temp;

    for (i = start; i < end;) {
        for (j = 0; j < n;) {

            xlim = i + BATCH;
            ylim = j + BATCH;
            xIndex = i * n;

            for (x = i; x < xlim; ++x) {

                temp = n - 1 - x;
                yIndex = j * n;

                for (y = j; y < ylim; ++y) {
                    o[tid][x + y] += a[xIndex + y] * b[yIndex + temp];
                    yIndex += n;
                }

                xIndex += n;
            }

            j = ylim;
        }

        i = xlim;
    }

    pthread_exit(NULL);
}

void multiThread(int N, int * matA, int * matB, int * output) {

    int rc, i;
    pthread_t threads[NUM_THREADS];
    pthread_attr_t attr;
    void * status;

    // Initialize and set thread joinable
    pthread_attr_init( & attr);
    pthread_attr_setdetachstate( & attr, PTHREAD_CREATE_JOINABLE);

    //Set up parameters for threads in global pointers
    a = matA;
    b = matB;
    n = N;
    int size = 2 * n - 1;
    o = new int * [NUM_THREADS];
    for (i = 0; i < NUM_THREADS; i++) {
        o[i] = new int[size];
    }

    //Create multiple threads
    for (i = 0; i < NUM_THREADS; i++) {
        rc = pthread_create( & threads[i], & attr, work, (void * ) i);
        if (rc) {
            cout << "Error:unable to create thread," << rc << endl;
            exit(-1);
        }
    }

    pthread_attr_destroy( & attr);

    //Wait for threads to join
    for (i = 0; i < NUM_THREADS; i++) {
        rc = pthread_join(threads[i], & status);
        if (rc) {
            cout << "Error:unable to join," << rc << endl;
            exit(-1);
        }
    }

    //Combine work done by each thread
    for (i = 0; i < NUM_THREADS; ++i) {
        for (int j = 0; j < size; ++j) {
            output[j] += o[i][j];
        }
    }

}