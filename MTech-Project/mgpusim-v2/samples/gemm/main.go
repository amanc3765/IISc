package main

import (
	"flag"

	"gitlab.com/akita/mgpusim/v2/benchmarks/dnn/gemm"
	"gitlab.com/akita/mgpusim/v2/samples/runner"
)

var m = flag.Int("m", 1, "The height of the first matrix")
var n = flag.Int("n", 1, "The width of the second matrix")
var k = flag.Int("k", 1, "The width of the first matrix and the height of the second matrix")

func main() {
	flag.Parse()

	runner := new(runner.Runner).ParseFlag().Init()

	benchmark := gemm.NewBenchmark(runner.Driver())
	benchmark.M = *m
	benchmark.N = *n
	benchmark.K = *k

	runner.AddBenchmark(benchmark)

	runner.Run()
}
