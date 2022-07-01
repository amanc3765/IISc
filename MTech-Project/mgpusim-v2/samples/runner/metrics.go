package runner

import (
	"fmt"
	"strings"
)

type metric struct {
	where string
	what  string
	value float64
}

type collector struct {
	metrics []metric
}

func (c *collector) Collect(where, what string, value float64) {
	c.metrics = append(c.metrics, metric{
		where: where,
		what:  what,
		value: value,
	})
}

// CODE ---------------------------------------------------------------------------------------------

func (c *collector) Dump(name string) {
	// f, err := os.Create(name + ".csv")
	// if err != nil {
	// 	panic(err)
	// }
	// defer f.Close()

	// fmt.Fprintf(f, ", where, what, value\n")
	// for i, m := range c.metrics {
	// fmt.Fprintf(f, "%d, %s, %s, %.12f\n", i, m.where, m.what, m.value)
	// }

	for _, m := range c.metrics {
		processMetric(m)
	}

	fmt.Printf("[LOG] STAT L1VTLB_Miss_rate : %.2f\n", getMissRate(gpuStats.l1vTLB))
	fmt.Printf("[LOG] STAT L2TLB_Miss_rate : %.2f\n", getMissRate(gpuStats.l2TLB))

	fmt.Printf("[LOG] STAT L1VCache_Read_Miss_rate : %.2f\n", getMissRate(gpuStats.l1VCacheRead))
	fmt.Printf("[LOG] STAT L2Cache_Read_Miss_rate : %.2f\n", getMissRate(gpuStats.l2CacheRead))

	fmt.Printf("[LOG] STAT L1VCache_Write_Miss_rate : %.2f\n", getMissRate(gpuStats.l1VCacheWrite))
	fmt.Printf("[LOG] STAT L2Cache_Write_Miss_rate : %.2f\n", getMissRate(gpuStats.l2CacheWrite))

	fmt.Printf("[LOG] STAT L1VTLB_MPKI : %.2f\n", getMPKI(gpuStats.l1vTLB, gpuStats.totalInst))
	fmt.Printf("[LOG] STAT L2TLB_MPKI : %.2f\n", getMPKI(gpuStats.l2TLB, gpuStats.totalInst))

	fmt.Printf("[LOG] STAT Instruction : %.2f\n", float64(gpuStats.totalInst))
}

func printMetric(m metric) {
	fmt.Printf("%s, %s, %.12f\n", m.where, m.what, m.value)
}

type CacheStats struct {
	hit     uint64
	miss    uint64
	mshrHit uint64
}

type GPUStats struct {
	totalInst     uint64
	l1vTLB        CacheStats
	l2TLB         CacheStats
	l1VCacheRead  CacheStats
	l1VCacheWrite CacheStats
	l2CacheRead   CacheStats
	l2CacheWrite  CacheStats
}

var gpuStats GPUStats

func processMetric(m metric) {

	if strings.Contains(m.what, "inst_count") {
		// printMetric(m)
		gpuStats.totalInst += uint64(m.value)
	} else if strings.Contains(m.where, "L1VTLB") {
		processCacheMetric(m, &gpuStats.l1vTLB)
	} else if strings.Contains(m.where, "L2TLB") {
		processCacheMetric(m, &gpuStats.l2TLB)
	} else if strings.Contains(m.where, "L1VCache") {
		if strings.Contains(m.what, "read") {
			processCacheMetric(m, &gpuStats.l1VCacheRead)
		} else if strings.Contains(m.what, "write") {
			processCacheMetric(m, &gpuStats.l1VCacheWrite)
		}
	} else if strings.Contains(m.where, "L2_") {

		if strings.Contains(m.what, "read") {
			processCacheMetric(m, &gpuStats.l2CacheRead)
		} else if strings.Contains(m.what, "write") {
			processCacheMetric(m, &gpuStats.l2CacheWrite)
		}
	}
}

func processCacheMetric(m metric, cacheStats *CacheStats) {
	// printMetric(m)
	if strings.Contains(m.what, "mshr-hit") {
		(*cacheStats).mshrHit += uint64(m.value)
	} else if strings.Contains(m.what, "miss") {
		(*cacheStats).miss += uint64(m.value)
	} else {
		(*cacheStats).hit += uint64(m.value)
	}
}

func getMissRate(cacheStats CacheStats) float64 {
	totalRequests := float64(cacheStats.hit + cacheStats.miss + cacheStats.mshrHit)
	totalMiss := float64(cacheStats.miss + cacheStats.mshrHit)
	return (totalMiss * 100) / totalRequests
}

func getMPKI(cacheStats CacheStats, totalInst uint64) float64 {
	totalMiss := float64(cacheStats.miss + cacheStats.mshrHit)
	return ((totalMiss * 1000) / float64(totalInst))
}
