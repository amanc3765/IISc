
var1=""
echo "Please ensure you are in ass1 directory! (Press any key to continue)"
read var1

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running brute-force algorithm on Test Case 1 (Press any key)-------"
read var1

cat test_cases/test1.txt
echo ""
echo "Colouring assignment: "
clisp bruteforce.lisp test_cases/test1.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running brute-force algorithm on Test Case 2 (Press any key)-------"
read var1

cat test_cases/test2.txt
echo ""
echo "Colouring assignment: "
clisp bruteforce.lisp test_cases/test2.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running brute-force algorithm on Test Case 3 (Press any key)-------"
read var1

cat test_cases/test3.txt
echo ""
echo "Colouring assignment: "
clisp bruteforce.lisp test_cases/test3.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running brute-force algorithm on Test Case 4 (Press any key)-------"
read var1

cat test_cases/test4.txt
echo ""
echo "Colouring assignment: "
clisp bruteforce.lisp test_cases/test4.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running greedy-backtracking algorithm on Test Case 1 (Press any key)-------"
read var1

cat test_cases/test1.txt
echo ""
echo "Colouring assignment: "
clisp greedy_backtrack.lisp test_cases/test1.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running greedy-backtracking algorithm on Test Case 2 (Press any key)-------"
read var1

cat test_cases/test2.txt
echo ""
echo "Colouring assignment: "
clisp greedy_backtrack.lisp test_cases/test2.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running greedy-backtracking algorithm on Test Case 3 (Press any key)-------"
read var1

cat test_cases/test3.txt
echo ""
echo "Colouring assignment: "
clisp greedy_backtrack.lisp test_cases/test3.txt 

# ---------------------------------------------------------------------------------------------------------------

echo ""
echo "------------ Running greedy-backtracking algorithm on Test Case 4 (Press any key)-------"
read var1

cat test_cases/test4.txt
echo ""
echo "Colouring assignment: "
clisp greedy_backtrack.lisp test_cases/test4.txt 

# ---------------------------------------------------------------------------------------------------------------
