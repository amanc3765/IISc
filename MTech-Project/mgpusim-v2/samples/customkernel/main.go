package main

import (
	"flag"
	"gitlab.com/akita/mgpusim/v2/benchmarks/dnn/customkernel"
	"gitlab.com/akita/mgpusim/v2/samples/runner"
)

var n = flag.Int("N", 1, "batch size")
var c = flag.Int("C", 1, "input channels")
var h = flag.Int("H", 28, "input height")
var w = flag.Int("W", 28, "input width")

func main() {
	flag.Parse()

	runner := new(runner.Runner).ParseFlag().Init()

	benchmark := customkernel.NewBenchmark(runner.Driver())
	benchmark.N = *n
	benchmark.C = *c
	benchmark.H = *h
	benchmark.W = *w

	runner.AddBenchmark(benchmark)

	runner.Run()
}
