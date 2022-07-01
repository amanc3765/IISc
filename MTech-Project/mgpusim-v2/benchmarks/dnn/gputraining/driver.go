package gputraining

import "gitlab.com/akita/mgpusim/v2/driver"

// A Driver is a driver for a gpu simulation.
type Driver interface {
	AllocateMemory(ctx *driver.Context, size uint64) driver.Ptr
	FreeMemory(ctx *driver.Context, ptr driver.Ptr)
}
