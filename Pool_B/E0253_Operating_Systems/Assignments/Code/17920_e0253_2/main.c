#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <time.h>
#include <fcntl.h>
#include <sys/mman.h>
#include "testcases.h"

typedef unsigned long long u8;
typedef unsigned char byte;

#define SIG_BALLOON 44
#define SYS_CALL_BALLOON 548
#define SYS_CALL_FREEMEMORY 549
#define PAGE_SIZE 4096
#define ENTRY_SIZE 8
#define PRESENT 63
#define PFN_MASK 0x007FFFFFFFFFFFFF
#define IDLE_RESET_TIME 180
#define NUM_BINS 10
#define MIN_RECLAIM 32
#define MAX_RECLAIM 64
#define LOW_MEM_LIMIT 1024
#define MED_MEM_LIMIT LOW_MEM_LIMIT + MIN_RECLAIM
#define HIGH_MEM_LIMIT LOW_MEM_LIMIT + MAX_RECLAIM
#define GET_BIT(X, Y)(X & ((u8) 1 << Y)) >> Y

void open_files();
void reset_idle_bitmap();
void read_idle_bitmap();
void calc_page_score();
void calc_threshold();
void error(char * message);

void * buff;
int pagemap, idle;
char * curr_page_addr;
clock_t last_reset_time, curr_time;
byte * idle_bit_map, * page_score;
unsigned long nr_signals, num_pages, curr_page_num;
unsigned long free_memory, memory_to_reclaim, pages_to_reclaim, pages_reclaimed, threshold, score_bin[NUM_BINS];

/*
 *          placeholder-3
 * implement your page replacement policy here
 */

void replace_pages() {

    u8 virt_addr;
    int retry_mode = 0;

    read_idle_bitmap();
    calc_page_score();
    calc_threshold();

    retry:

        for (int i = 0; i < num_pages; ++i) {

            if (idle_bit_map[curr_page_num] != -1) {
                if ((page_score[curr_page_num] <= threshold) || retry_mode) {
                    virt_addr = (u8) curr_page_addr;

                    if (madvise((void * ) virt_addr, PAGE_SIZE, MADV_PAGEOUT) == -1) {
                        error("\n replace_pages(): madvise() failed \n");
                    } else {
                        ++pages_reclaimed;
                        if (pages_reclaimed > pages_to_reclaim) {

                            free_memory = syscall(SYS_CALL_FREEMEMORY);
                            if (free_memory >= MED_MEM_LIMIT) {
                                return;
                            } else {
                                memory_to_reclaim = (HIGH_MEM_LIMIT - free_memory);
                                pages_to_reclaim = (memory_to_reclaim * 1024 * 1024) / PAGE_SIZE;
                                pages_reclaimed = 0;
                                calc_threshold();
                            }
                        }
                    }
                }
            }

            curr_page_num++;
            curr_page_addr += PAGE_SIZE;

            if (curr_page_num >= num_pages) {
                curr_page_num = 0;
                curr_page_addr = buff;
            }
        }

    free_memory = syscall(SYS_CALL_FREEMEMORY);
    if ((pages_reclaimed < pages_to_reclaim) || (free_memory <= MED_MEM_LIMIT)) {
        memory_to_reclaim = (HIGH_MEM_LIMIT - free_memory);
        pages_to_reclaim = (memory_to_reclaim * 1024 * 1024) / PAGE_SIZE;
        pages_reclaimed = 0;

        retry_mode = 1;
        goto retry;
    }
}


/*
 *          placeholder-2
 * implement your signal handler here
 */
void sig_balloon_handler() {
    printf("\n sig_balloon_handler() : SIG_BALLOON received %lu \n", ++nr_signals);

    free_memory = syscall(SYS_CALL_FREEMEMORY);
    memory_to_reclaim = (HIGH_MEM_LIMIT - free_memory);
    pages_to_reclaim = (memory_to_reclaim * 1024 * 1024) / PAGE_SIZE;
    pages_reclaimed = 0;

    replace_pages();

    curr_time = clock();
    double cpu_time_used = ((double)(curr_time - last_reset_time)) / CLOCKS_PER_SEC;
    if (cpu_time_used >= IDLE_RESET_TIME) {
        reset_idle_bitmap();
        last_reset_time = curr_time;
    }
}

int main(int argc, char *argv[])
{
    clock_t start = clock();

    int *ptr, nr_pages;

        ptr = mmap(NULL, TOTAL_MEMORY_SIZE, PROT_READ | PROT_WRITE,
            MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);

    if (ptr == MAP_FAILED) {
        printf("mmap failed\n");
            exit(1);
    }
    buff = ptr;
    memset(buff, 0, TOTAL_MEMORY_SIZE);

    /*
     *      placeholder-1
     * register me with the kernel ballooning subsystem
     */

    struct sigaction sig_balloon;
    sig_balloon.sa_sigaction = sig_balloon_handler;
    sig_balloon.sa_flags = SA_SIGINFO;
    sigaction(SIG_BALLOON, & sig_balloon, NULL);

    curr_page_num = 0;
    curr_page_addr = buff;
    num_pages = TOTAL_MEMORY_SIZE / PAGE_SIZE;
    idle_bit_map = (byte * ) malloc(sizeof(byte) * num_pages);
    page_score = (byte * ) malloc(sizeof(byte) * num_pages);
    memset(page_score, 5, num_pages);

    open_files();
    reset_idle_bitmap();
    last_reset_time = clock();

    printf("\n main(): Making System call \n");
    printf("\n main(): System call returned %ld \n", syscall(SYS_CALL_BALLOON));

    int * dummy_alloc = mmap(NULL, TOTAL_MEMORY_SIZE, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
    if (dummy_alloc == MAP_FAILED)
        error("\n mmap failed \n");
    dummy_alloc[0] = 0;

    /* test-case */
    test_case_main(buff, TOTAL_MEMORY_SIZE);

    munmap(ptr, TOTAL_MEMORY_SIZE);
    printf("I received SIGBALLOON %lu times\n", nr_signals);

    clock_t end = clock();
    double cpu_time_used = ((double)(end - start)) / CLOCKS_PER_SEC;
    printf("\n main() : Time Execution Time %f. \n", cpu_time_used);
}


void open_files() {

    int pid = getpid();
    char path[40] = {};

    sprintf(path, "/proc/%u/pagemap", pid);
    pagemap = open(path, O_RDONLY);
    if (!pagemap)
        error("Failed to open /proc/pagemap.");

    sprintf(path, "/sys/kernel/mm/page_idle/bitmap");
    idle = open(path, O_RDWR);
    if (!idle)
        error("Failed to open sys/kernel/mm/page_idle/bitmap.");
}

void reset_idle_bitmap() {
    u8 virt_addr, phy_addr, pfn, word;
    unsigned long curr_page_num;
    char * curr_page_addr;

    curr_page_num = 0;
    curr_page_addr = buff;

    while (curr_page_num < num_pages) {
        virt_addr = (u8) curr_page_addr;

        if (pread(pagemap, & phy_addr, ENTRY_SIZE, (virt_addr / PAGE_SIZE) * ENTRY_SIZE) != ENTRY_SIZE)
            error("\n reset_idle_bitmap(): Failed to read from pagemap. \n");

        if (GET_BIT(phy_addr, PRESENT)) {
            pfn = phy_addr & PFN_MASK;

            word = 1UL << (pfn % 64);
            if (pwrite(idle, & word, ENTRY_SIZE, (pfn / 64) * ENTRY_SIZE) != ENTRY_SIZE)
                error("\n reset_idle_bitmap(): Failed to write to idle bitmap. \n");
        }

        // page_score[curr_page_num] = 5;

        curr_page_num++;
        curr_page_addr += PAGE_SIZE;
    }
}

void read_idle_bitmap() {
    u8 virt_addr, phy_addr, pfn, word;
    unsigned long curr_page_num;
    char * curr_page_addr;

    curr_page_num = 0;
    curr_page_addr = buff;

    while (curr_page_num < num_pages) {
        virt_addr = (u8) curr_page_addr;

        if (pread(pagemap, & phy_addr, ENTRY_SIZE, (virt_addr / PAGE_SIZE) * ENTRY_SIZE) != ENTRY_SIZE)
            error("\n read_idle_bitmap(): Failed to read from pagemap. \n");

        if (GET_BIT(phy_addr, PRESENT)) {
            pfn = phy_addr & PFN_MASK;

            if (pread(idle, & word, ENTRY_SIZE, (pfn / 64) * ENTRY_SIZE) != ENTRY_SIZE)
                error("\n read_idle_bitmap(): Failed to read from idle bitmap \n");

            idle_bit_map[curr_page_num] = GET_BIT(word, pfn % 64);
        } else {
            idle_bit_map[curr_page_num] = -1;
        }

        curr_page_num++;
        curr_page_addr += PAGE_SIZE;
    }
}

void calc_page_score() {
    unsigned long curr_page_num = 0;

    for (int i = 0; i < NUM_BINS; ++i) {
        score_bin[i] = 0;
    }

    while (curr_page_num < num_pages) {
        if (idle_bit_map[curr_page_num] == 0) {
            if (page_score[curr_page_num] < (NUM_BINS-1))
                ++page_score[curr_page_num];
        } else if (idle_bit_map[curr_page_num] == 1) {
            if (page_score[curr_page_num] > 0)
                --page_score[curr_page_num];
        } 
        ++score_bin[page_score[curr_page_num]];

        curr_page_num++;
    }
}

void calc_threshold() {
    unsigned long page_count = 0;
    threshold = (NUM_BINS-1);

    for (int i = 0; i < NUM_BINS; ++i) {
        page_count += score_bin[i];
        if (page_count >= pages_to_reclaim) {
            threshold = i;
            return;
        }
    }
}

void error(char * message) {
    perror(message);
    exit(0);
}