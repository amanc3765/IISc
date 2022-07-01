package crossbar

import (
	"gitlab.com/akita/akita/v2/sim"
	"gitlab.com/akita/mem/v2/vm"
	"gitlab.com/akita/mgpusim/v2/samples/runner/utility/logger"
)

type CrossbarDownTransaction struct {
	cyclesLeft int
	inPort     sim.Port
	outPort    sim.Port
	req        *vm.TranslationReq
}

type CrossbarUpTransaction struct {
	cyclesLeft int
	inPort     sim.Port
	outPort    sim.Port
	req        *vm.TranslationRsp
}

type Crossbar struct {
	*sim.TickingComponent

	name           string
	numInputPorts  int
	numOutputPorts int
	numSetsPerTLB  int
	log2PageSize   int
	reqIDMap       map[string]string

	latency         int
	upTransaction   *CrossbarUpTransaction
	downTransaction *CrossbarDownTransaction

	inputPortToService  int
	outputPortToService int
	InputPorts          []sim.Port
	OutputPorts         []sim.Port
	tlbTopPorts         []sim.Port
	atTranslationPorts  []sim.Port
}

var LogColorCrossbar = logger.Yellow

func (crossbar *Crossbar) Tick(now sim.VTimeInSec) bool {
	madeProgress := false

	if crossbar.downTransaction != nil {
		madeProgress = crossbar.processDownTransaction(now) || madeProgress
	} else {
		madeProgress = crossbar.parseInputPort(now) || madeProgress
	}

	if crossbar.upTransaction != nil {
		madeProgress = crossbar.processUpTransaction(now) || madeProgress
	} else {
		madeProgress = crossbar.parseOutputPort(now) || madeProgress
	}

	return madeProgress
}

func (crossbar *Crossbar) processDownTransaction(now sim.VTimeInSec) bool {
	downTransaction := crossbar.downTransaction

	if downTransaction.cyclesLeft > 0 {
		downTransaction.cyclesLeft--
	} else {
		req := downTransaction.req
		req.SendTime = now

		err := downTransaction.outPort.Send(req)
		if err == nil {
			downTransaction.inPort.Retrieve(now)
			crossbar.downTransaction = nil
		}
	}

	return true
}

func (crossbar *Crossbar) processUpTransaction(now sim.VTimeInSec) bool {
	upTransaction := crossbar.upTransaction

	if upTransaction.cyclesLeft > 0 {
		upTransaction.cyclesLeft--
	} else {
		req := upTransaction.req
		req.SendTime = now

		err := upTransaction.outPort.Send(req)
		if err == nil {
			upTransaction.inPort.Retrieve(now)
			crossbar.upTransaction = nil
		}
	}

	return true
}

func (crossbar *Crossbar) parseInputPort(now sim.VTimeInSec) bool {
	var currInputPortIndex int
	var currInputPort sim.Port
	var msg sim.Msg = nil

	for numProbes := 0; numProbes < crossbar.numInputPorts; numProbes++ {
		currInputPortIndex = crossbar.inputPortToService
		currInputPort = crossbar.InputPorts[currInputPortIndex]
		crossbar.inputPortToService = (currInputPortIndex + 1) % crossbar.numInputPorts

		msg = currInputPort.Peek()
		if msg != nil {
			break
		}
	}

	if msg == nil {
		return false
	}

	reqIn := msg.(*vm.TranslationReq)

	currOutputPortIndex := 0
	if crossbar.numOutputPorts > 1 {
		numSetsPerCluster := crossbar.numSetsPerTLB * crossbar.numOutputPorts
		pageSize := uint64(1 << crossbar.log2PageSize)
		VPageNum := int(reqIn.VAddr / pageSize)
		currOutputPortIndex = (VPageNum % numSetsPerCluster) / crossbar.numSetsPerTLB
	}
	currOutputPort := crossbar.OutputPorts[currOutputPortIndex]
	reqDestPort := crossbar.tlbTopPorts[currOutputPortIndex]

	reqOut := vm.TranslationReqBuilder{}.
		WithSendTime(now).
		WithSrc(currOutputPort).
		WithDst(reqDestPort).
		WithPID(reqIn.PID).
		WithVAddr(reqIn.VAddr).
		WithDeviceID(reqIn.DeviceID).
		WithATPortIndex(currInputPortIndex).
		Build()

	crossbar.reqIDMap[reqOut.ID] = reqIn.ID

	crossbar.downTransaction = &CrossbarDownTransaction{
		cyclesLeft: crossbar.latency,
		inPort:     currInputPort,
		outPort:    currOutputPort,
		req:        reqOut,
	}

	return true
}

func (crossbar *Crossbar) parseOutputPort(now sim.VTimeInSec) bool {
	var currOutputPortIndex int
	var currOutputPort sim.Port
	var msg sim.Msg = nil

	for numProbes := 0; numProbes < crossbar.numOutputPorts; numProbes++ {
		currOutputPortIndex = crossbar.outputPortToService
		currOutputPort = crossbar.OutputPorts[currOutputPortIndex]
		crossbar.outputPortToService = (currOutputPortIndex + 1) % crossbar.numOutputPorts

		msg = currOutputPort.Peek()
		if msg != nil {
			break
		}
	}

	if msg == nil {
		return false
	}

	reqIn := msg.(*vm.TranslationRsp)

	currInputPortIndex := reqIn.ATPortIndex
	currInputPort := crossbar.InputPorts[currInputPortIndex]
	reqDestPort := crossbar.atTranslationPorts[currInputPortIndex]

	respondTo, ok := crossbar.reqIDMap[reqIn.RespondTo]
	if !ok {
		panic("\nCould not find requestID")
	}

	reqOut := vm.TranslationRspBuilder{}.
		WithSendTime(now).
		WithSrc(currInputPort).
		WithDst(reqDestPort).
		WithRspTo(respondTo).
		WithPage(reqIn.Page).
		Build()

	crossbar.upTransaction = &CrossbarUpTransaction{
		cyclesLeft: crossbar.latency,
		inPort:     currOutputPort,
		outPort:    currInputPort,
		req:        reqOut,
	}

	return true
}
