# scp -r ./gitlab.com/ aman@10.16.38.24:/stor/aman/go/pkg/mod
# cd /home/aman/Desktop/MTech-Project/mgpusim-v2/samples/analysis

PROJECT_DIR=~/Desktop/MTech-Project
SAMPLES_DIR=$PROJECT_DIR/mgpusim-v2/samples
ANALYSIS_DIR=$SAMPLES_DIR/analysis
BENCHMARK_RUNNER_DIR=$SAMPLES_DIR/analysis/benchmark_runner

print(){
	MESSAGE=$1

	echo
	echo ${1}
	echo
}
call_runner(){
	PROGRAM=$1
	PARAMS=$2
	ARGS=("$@") 
	NUM_ARGS=$#

	clear
	 
	for (( i=2;i<$NUM_ARGS;i++)); do 
	    PARAMS="${PARAMS}_${ARGS[${i}]}"
	done

	cd $SAMPLES_DIR/$PROGRAM
	PWD=$(pwd)	
	print "Go Build inside: ${PWD}"
	go build

	cd $BENCHMARK_RUNNER_DIR
	print "Running ${PROGRAM} with ${PARAMS}..." 
	make
	$BENCHMARK_RUNNER_DIR/benchmark_runner ${PROGRAM} $2 $3 $4

	cd $ANALYSIS_DIR
	print "Creating figure..."
	# python3 create_figure.py $PROGRAM/$PARAMS

	print "Done."
}

# call_runner matrixtranspose 20
# call_runner atax 1500 1500
# call_runner bicg 1500 1500
# call_runner spmv 12800 0.05
# call_runner matrixmultiplication 10 20 30
# call_runner relu 16777216
# call_runner fft 64 4
call_runner gemm 2500 2500 2500