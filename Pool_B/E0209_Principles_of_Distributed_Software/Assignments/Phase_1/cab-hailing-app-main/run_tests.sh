cd test_suites/self/
var1=""
echo "Please ensure services are running and press a key to start testing!"
read var1

t_count=1
for FILE in *; 
do
	echo ""
	echo "------------ Press any key to start Test Case $t_count : $FILE -------"
	read var1
	bash $FILE; 
	t_count=$((t_count+1))
done


cd ../concurrent
echo "__________________ Starting Concurrent Test Cases __________________"
./run_test_concurrent.sh