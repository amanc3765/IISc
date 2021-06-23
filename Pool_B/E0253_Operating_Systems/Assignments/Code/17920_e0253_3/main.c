#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <time.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <pthread.h>
#include "testcases.h"

typedef unsigned long long u8;
typedef unsigned char byte;

#define SYS_CALL_BALLOON 548
#define SYS_CALL_FREEMEMORY 549
#define SIG_BALLOON 44
#define SIGNAL_TIMEOUT_LIMIT 60
#define ITER_BEFORE_IDLE_RESET 3
#define MEGA_BYTE 1024 * 1024UL
#define PAGE_SIZE 4096
#define HUGE_PAGE_SIZE 2 * MEGA_BYTE
#define THP_TO_NORMAL 512
#define ENTRY_SIZE 8
#define SWAPPED 62
#define PRESENT 63
#define THP 22
#define PFN_MASK 0x007FFFFFFFFFFFFF
#define THP_MASK 0x00000000001FFFFF
#define NUM_BINS 30
#define PAGE_INFO_SWAPPED -1
#define MIN_RECLAIM 32
#define MAX_RECLAIM 64
#define LOW_MEM_LIMIT 1024
#define MED_MEM_LIMIT LOW_MEM_LIMIT + MIN_RECLAIM
#define HIGH_MEM_LIMIT LOW_MEM_LIMIT + MAX_RECLAIM
#define GET_BIT(X, Y) (X & ((u8)1 << Y)) >> Y
#define RED printf("\x1b[31m");
#define GREEN printf("\x1b[32m");
#define BLUE printf("\x1b[34m");
#define YELLOW printf("\x1b[33m");
#define RESET printf("\x1b[0m");

void init();
void *worker_thread(void *args);
void reset_idle_bitmap();
void calc_threshold();
void error(char *message);

void *buff;
int pagemap, idle, kpageflags;
char *curr_page_addr;
clock_t last_signal_raised;
byte *page_info, *page_score, thread_running, thp_score_enable;
unsigned long nr_signals, num_pages, curr_page_num;
unsigned long free_memory, memory_to_reclaim, pages_to_reclaim, pages_reclaimed, threshold, score_bin[NUM_BINS];

/*
 *          placeholder-3
 * implement your page replacement policy here
 */
void replace_pages()
{
    u8 virt_addr, vpn, pagemap_word, pfn, kpageflags_word;
    int retry_mode = 0;

retry:

    for (int i = 0; i < num_pages; ++i)
    {
        if ((page_info[curr_page_num] != PAGE_INFO_SWAPPED) &&
            ((page_score[curr_page_num] <= threshold) || retry_mode))
        {
            virt_addr = (u8)curr_page_addr;
            if (madvise((void *)virt_addr, PAGE_SIZE, MADV_PAGEOUT) == -1)
                error("\n replace_pages(): madvise() failed \n");

            pages_reclaimed++;
            curr_page_num++;
            curr_page_addr += PAGE_SIZE;

            if (pages_reclaimed > pages_to_reclaim)
            {
                free_memory = syscall(SYS_CALL_FREEMEMORY);
                if (free_memory >= MED_MEM_LIMIT)
                {
                    return;
                }
                else
                {
                    memory_to_reclaim = (HIGH_MEM_LIMIT - free_memory);
                    pages_to_reclaim = (memory_to_reclaim * MEGA_BYTE) / PAGE_SIZE;
                    pages_reclaimed = 0;
                }
            }
        }
        else
        {
            curr_page_num++;
            curr_page_addr += PAGE_SIZE;
        }

        if (curr_page_num >= num_pages)
        {
            curr_page_num = 0;
            curr_page_addr = buff;
        }
    }

    free_memory = syscall(SYS_CALL_FREEMEMORY);
    if ((pages_reclaimed < pages_to_reclaim) || (free_memory <= MED_MEM_LIMIT))
    {
        memory_to_reclaim = (HIGH_MEM_LIMIT - free_memory);
        pages_to_reclaim = (memory_to_reclaim * MEGA_BYTE) / PAGE_SIZE;
        pages_reclaimed = 0;

        retry_mode = 1;
        goto retry;
    }
}

/*
 *          placeholder-2
 * implement your signal handler here
 */
void sig_balloon_handler()
{
    printf("\n sig_balloon_handler() : SIG_BALLOON received %lu \n", ++nr_signals);

    last_signal_raised = clock();
    if (nr_signals >= 2 && thread_running == 0)
    {
        pthread_t thread_id;
        if (pthread_create(&thread_id, NULL, worker_thread, NULL))
            error("\n init() : Unable to create worker_thread() thread. \n");
        thread_running = 1;
    }

    free_memory = syscall(SYS_CALL_FREEMEMORY);
    memory_to_reclaim = (HIGH_MEM_LIMIT - free_memory);
    pages_to_reclaim = (memory_to_reclaim * MEGA_BYTE) / PAGE_SIZE;
    pages_reclaimed = 0;
    replace_pages();
}

int main(int argc, char *argv[])
{
    clock_t start = clock();

    int *ptr, nr_pages;

    ptr = mmap(NULL, TOTAL_MEMORY_SIZE, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);

    if (ptr == MAP_FAILED)
    {
        printf("mmap failed\n");
        exit(1);
    }

    buff = ptr;
    madvise(buff, TOTAL_MEMORY_SIZE, MADV_HUGEPAGE);
    memset(buff, 0, TOTAL_MEMORY_SIZE);

    init();

    /*
     *      placeholder-1
     * register me with the kernel ballooning subsystem
     */

    struct sigaction sig_balloon;
    sig_balloon.sa_sigaction = sig_balloon_handler;
    sig_balloon.sa_flags = SA_SIGINFO;
    sigaction(SIG_BALLOON, &sig_balloon, NULL);

    YELLOW printf("\n main(): Making System call \n");
    printf("\n main(): System call returned %ld \n", syscall(SYS_CALL_BALLOON));
    RESET

    int *dummy_alloc = mmap(NULL, PAGE_SIZE, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, 0, 0);
    if (dummy_alloc == MAP_FAILED)
        error("\n mmap failed \n");
    dummy_alloc[0] = 0;

    /* test-case */
    test_case_main(buff, TOTAL_MEMORY_SIZE);

    munmap(ptr, TOTAL_MEMORY_SIZE);
    printf("I received SIGBALLOON %lu times\n", nr_signals);

    clock_t end = clock();
    double cpu_time_used = ((double)(end - start)) / CLOCKS_PER_SEC;
    printf("\n main() : Execution Time %f. \n", cpu_time_used);
}

void init()
{
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

    sprintf(path, "/proc/kpageflags");
    kpageflags = open(path, O_RDONLY);
    if (!kpageflags)
        error("Failed to open /proc/kpageflags.");

    num_pages = TOTAL_MEMORY_SIZE / PAGE_SIZE;
    curr_page_num = 0;
    curr_page_addr = buff;
    threshold = (NUM_BINS - 1);

    page_score = (byte *)malloc(sizeof(byte) * num_pages);
    memset(page_score, NUM_BINS / 2, num_pages);

    page_info = (byte *)malloc(sizeof(byte) * num_pages);
    memset(page_info, 1, num_pages);
}

void reset_idle_bitmap()
{
    u8 offset = 0;
    long long word = -1;
    while (1)
    {
        if (pwrite(idle, &word, ENTRY_SIZE, offset) != ENTRY_SIZE)
        {
            return;
        }
        offset += ENTRY_SIZE;
    }
}

void calc_threshold()
{
    u8 virt_addr, vpn, pagemap_word, pfn, idle_word, kpageflags_word;
    unsigned long curr_page_num, page_count;
    char *curr_page_addr;
    byte is_idle;

    for (int i = 0; i < NUM_BINS; ++i)
        score_bin[i] = 0;

    curr_page_num = 0;
    curr_page_addr = buff;

    while (curr_page_num < num_pages)
    {
        virt_addr = (u8)curr_page_addr;
        vpn = (virt_addr / PAGE_SIZE);
        if (pread(pagemap, &pagemap_word, ENTRY_SIZE, vpn * ENTRY_SIZE) != ENTRY_SIZE)
            error("\n calc_page_score(): Failed to read from pagemap. \n");

        if (GET_BIT(pagemap_word, PRESENT))
        {
            pfn = pagemap_word & PFN_MASK;

            if (pread(idle, &idle_word, ENTRY_SIZE, (pfn / 64) * ENTRY_SIZE) != ENTRY_SIZE)
                error("\n calc_page_score(): Failed to read from idle bitmap \n");
            is_idle = GET_BIT(idle_word, pfn % 64);

            if ((virt_addr & THP_MASK) == 0)
            {
                if (pread(kpageflags, &kpageflags_word, ENTRY_SIZE, (pfn * ENTRY_SIZE)) != ENTRY_SIZE)
                    error("\n calc_page_score(): Failed to read from kpageflags. \n");

                if (GET_BIT(kpageflags_word, THP))
                {
                    for (int i = 0; i < THP_TO_NORMAL; ++i)
                    {
                        if (thp_score_enable && page_score[curr_page_num] < (NUM_BINS - 2))
                            ++page_score[curr_page_num];

                        if (is_idle)
                        {
                            if (page_score[curr_page_num] > 0)
                                --page_score[curr_page_num];
                        }
                        else
                        {
                            if (page_score[curr_page_num] < (NUM_BINS - 1))
                                ++page_score[curr_page_num];
                        }
                        ++score_bin[page_score[curr_page_num]];

                        page_info[curr_page_num] = is_idle;
                        curr_page_num++;
                    }

                    curr_page_addr += HUGE_PAGE_SIZE;
                    continue;
                }
            }

            if (is_idle)
            {
                if (page_score[curr_page_num] > 0)
                    --page_score[curr_page_num];
            }
            else
            {
                if (page_score[curr_page_num] < (NUM_BINS - 1))
                    ++page_score[curr_page_num];
            }
            ++score_bin[page_score[curr_page_num]];

            page_info[curr_page_num] = is_idle;
            curr_page_num++;
            curr_page_addr += PAGE_SIZE;
        }
        else
        {
            page_info[curr_page_num] = PAGE_INFO_SWAPPED;
            curr_page_num++;
            curr_page_addr += PAGE_SIZE;
        }
    }

    page_count = 0;
    threshold = (NUM_BINS - 1);

    for (int i = 0; i < NUM_BINS; ++i)
    {
        page_count += score_bin[i];
        if (page_count >= pages_to_reclaim)
        {
            threshold = i;
            return;
        }
    }
}

void *worker_thread(void *args)
{
    while (1)
    {
        clock_t curr_time = clock();
        double time_elapsed = ((double)(curr_time - last_signal_raised)) / CLOCKS_PER_SEC;
        if (time_elapsed > SIGNAL_TIMEOUT_LIMIT)
        {
            thread_running = 0;
            break;
        }

        reset_idle_bitmap();
        thp_score_enable = 1;

        for (int i = 0; i < ITER_BEFORE_IDLE_RESET; ++i)
        {
            calc_threshold();
            thp_score_enable = 0;
        }
    }
}

void error(char *message)
{
    perror(message);
    exit(0);
}