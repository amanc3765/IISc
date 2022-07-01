package resource

import (
	"gitlab.com/akita/akita/v2/sim"
	"gitlab.com/akita/mgpusim/v2/kernels"
)

// CUResource handle CU resources
type CUResource interface {
	ReserveResourceForWG(wg *kernels.WorkGroup) (
		locations []WfLocation,
		ok bool,
	)
	FreeResourcesForWG(wg *kernels.WorkGroup)
	DispatchingPort() sim.Port
}
