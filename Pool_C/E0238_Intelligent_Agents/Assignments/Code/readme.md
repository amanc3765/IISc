IA Assignment
------------------------------------------------------------

Submitted by:

    Aman Choudhary
    MTech CSA 2020-22
    amanc@iisc.ac.in
    17920


Contents
------------------------------------------------------------

1) bruteforce.lisp : 

    A brute-force implementation to color maps. It enumerates all possible colorings
    and reports the one with minimum colors, given it is not more than the number of available colors. 

2) greedy_backtrack.lisp

    This is an alternative approach to map-coloring, since brute force is expected to work slowly.
    It first colors the map greedily. This may be a sub-optimal coloring. So, we try to reduce the
    number of colors used by running a backtracking algorithm on top to the result of greedy, to
    reduce the number of colors used. 

    The backtracking algorithm has been borrowed from the following paper:

    Paper: Improving the Performance of Graph Coloring Algorithms through Backtracking
    Link: https://link.springer.com/content/pdf/10.1007%2F978-3-540-69384-0_92.pdf


3) test_cases/

    This folder consists of 4 sample testcases. The format of tetscases is as given below. 
    Each testcase includes, a representation of the map as a list of lists followed by a list of colours available for use. 

    Format of test case:
    -----------------------------------------
    ((A B C F)
    (B A C D E F)
    (C A B F D)
    (D B C F E)
    (E B D F)
    (F B E D C A))

    (W G Y R)
    -----------------------------------------

    New tests can be included by just making a file in the test_cases/ directry using the above format.

4) run_script.sh

    A script to run all test cases, using both the algorithms. Do try it out! 


Running the program
------------------------------------------------------------

1) Make sure you are in the directory: ass1
2) Run the command:
    clisp <program_file> test_cases/<test_case_file>

For e.g., to run the brute-force method on test case 1,
    clisp bruteforce.lisp test_cases/test1.txt

Or, to run the greedy-backtracking method on test case 1,
    clisp greedy_backtrack.lisp test_cases/test1.txt 