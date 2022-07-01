// Package gemm defines a benchmark for the Gemm operation.
package gemm

import (
	gpuTensor "gitlab.com/akita/mgpusim/v2/benchmarks/dnn/tensor"
	"gitlab.com/akita/mgpusim/v2/driver"
)

// A Benchmark is a benchmark for the Convolutional Layer.
type Benchmark struct {
	driver           *driver.Driver
	context          *driver.Context
	gpus             []int
	useUnifiedMemory bool

	M, N, K int

	operator *gpuTensor.GPUOperator

	a, b, c, d  *gpuTensor.Tensor
	alpha, beta float64
}

// NewBenchmark creates a new Conv2D benchmark. It requires the GPU driver as an argument.
func NewBenchmark(driver *driver.Driver) *Benchmark {
	b := &Benchmark{
		driver: driver,
	}

	b.context = b.driver.Init()
	b.operator = gpuTensor.NewGPUOperator(b.driver, b.context)
	b.operator.ReportTime()

	b.alpha = 1
	b.beta = 1

	return b
}

// EnableVerification configures the benchmark to verify the result.
func (b *Benchmark) EnableVerification() {
	b.operator.EnableVerification()
}

// SelectGPU selects the GPU to run the benchmark on.
func (b *Benchmark) SelectGPU(gpus []int) {
	if len(gpus) > 1 {
		panic("Conv2D benchmark can only run on a single GPU for now.")
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
	hA := make([]float64, b.M*b.K)
	hB := make([]float64, b.K*b.N)
	hC := make([]float64, b.M*b.N)

	for i := 0; i < b.M; i++ {
		for j := 0; j < b.K; j++ {
			hA[i*b.K+j] = float64((i*b.K + j) % 10)
		}
	}

	for i := 0; i < b.K; i++ {
		for j := 0; j < b.N; j++ {
			hB[i*b.N+j] = float64((i*b.N + j) % 10)
		}
	}

	for i := 0; i < b.M; i++ {
		for j := 0; j < b.N; j++ {
			hC[i*b.N+j] = float64((i*b.N + j) % 10)
		}
	}

	b.a = b.operator.CreateWithData(hA, []int{b.M, b.K}, "").(*gpuTensor.Tensor)
	b.b = b.operator.CreateWithData(hB, []int{b.K, b.N}, "").(*gpuTensor.Tensor)
	b.c = b.operator.CreateWithData(hC, []int{b.M, b.N}, "").(*gpuTensor.Tensor)
}

func (b *Benchmark) exec() {
	// fmt.Println("A", b.operator.Dump(b.a))
	// fmt.Println("B", b.operator.Dump(b.b))
	// fmt.Println("C", b.operator.Dump(b.c))

	b.operator.Gemm(false, false, b.alpha, b.beta, b.a, b.b, b.c)

	// fmt.Println("D", b.operator.Dump(d))
}

// Verify does nothing for now.
func (b *Benchmark) Verify() {
}
