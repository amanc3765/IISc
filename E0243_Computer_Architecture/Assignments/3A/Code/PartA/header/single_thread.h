
/*

Submitted by: AMAN CHOUDHARY [17920] (amanc@iisc.ac.in)

*/

#define BATCH 32

void singleThread(int N, int * matA, int * matB, int * output) {
    
    int i, j, x, y, xlim, ylim, xIndex, yIndex, temp;

    for (i = 0; i < N;) {
        for (j = 0; j < N;) {

            xlim = i + BATCH;
            ylim = j + BATCH;
            xIndex = i * N;

            for (x = i; x < xlim; ++x) {

                temp = N - 1 - x;
                yIndex = j * N;

                for (y = j; y < ylim; ++y) {
                    output[x + y] += matA[xIndex + y] * matB[yIndex + temp];
                    yIndex += N;
                }

                xIndex += N;
            }

            j = ylim;
        }

        i = xlim;
    }

}