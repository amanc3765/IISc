// Package customkernel defines a benchmark for the NC-CN transpose operation.
package customkernel

import (
	// "math/rand"
	"gitlab.com/akita/dnn/tensor"
	gpuTensor "gitlab.com/akita/mgpusim/v2/benchmarks/dnn/tensor"
	"gitlab.com/akita/mgpusim/v2/driver"
)

// A Benchmark is a benchmark for the im2col operation.
type Benchmark struct {
	driver           *driver.Driver
	context          *driver.Context
	gpus             []int
	useUnifiedMemory bool

	N, C, H, W int

	operator *gpuTensor.GPUOperator

	Input tensor.Tensor
}

// NewBenchmark creates a new NxC to CxN transpose benchmark. It requires the GPU driver as an
// argument.
func NewBenchmark(driver *driver.Driver) *Benchmark {
	b := &Benchmark{
		driver: driver,
	}

	b.context = b.driver.Init()
	b.operator = gpuTensor.NewGPUOperator(b.driver, b.context)
	b.operator.ReportTime()

	return b
}

// EnableVerification configures the benchmark to verify the result.
func (b *Benchmark) EnableVerification() {
	b.operator.EnableVerification()
}

// SelectGPU selects the GPU to run the benchmark on.
func (b *Benchmark) SelectGPU(gpus []int) {
	if len(gpus) > 1 {
		panic("NC-CN transpose benchmark can only run on a single GPU for now.")
	}

	b.gpus = gpus
}

// SetUnifiedMemory configures the benchmark to use unified memory.
func (b *Benchmark) SetUnifiedMemory() {
	b.useUnifiedMemory = true
}

// Run runs the benchmark.
func (b *Benchmark) Run() {
	b.driver.SelectGPU(b.context, b.gpus[0])
	b.initMem()
	b.exec()
}

func (b *Benchmark) initMem() {
	input := make([]float64, b.N*b.C*b.H*b.W)

	for i := 0; i < b.N*b.C*b.H*b.W; i++ {
		input[i] = float64(i)
	}

	b.Input = b.operator.CreateWithData(
		input, []int{b.N, b.C, b.H, b.W}, "NCHW")
}

func (b *Benchmark) exec() {
	// Enter your kernel
	b.operator.Transpose(b.Input, []int{1, 0, 2, 3})
	// b.operator.Sum(b.Input, []int{0, 2, 3})
}

// Verify does nothing for now.
func (b *Benchmark) Verify() {
}
