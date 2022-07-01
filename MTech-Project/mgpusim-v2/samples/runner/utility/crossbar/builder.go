package crossbar

import (
	"fmt"
	"math"

	"gitlab.com/akita/akita/v2/sim"
	"gitlab.com/akita/mem/v2/vm/addresstranslator"
	"gitlab.com/akita/mem/v2/vm/tlb"

	"gitlab.com/akita/mgpusim/v2/samples/runner/utility/logger" //CODE
)

type Builder struct {
	engine         sim.Engine
	freq           sim.Freq
	numInputPorts  int
	numOutputPorts int
	numSetsPerTLB  int
}

func MakeBuilder() Builder {
	return Builder{
		freq: 1 * sim.GHz,
	}
}

// WithEngine sets the engine that the Crossbar uses
func (b Builder) WithEngine(engine sim.Engine) Builder {
	b.engine = engine
	return b
}

// WithFreq sets the freq that the Crossbar uses
func (b Builder) WithFreq(freq sim.Freq) Builder {
	b.freq = freq
	return b
}

// WithNumInputPorts sets the number of input ports for the crossbar
func (b Builder) WithNumInputPorts(numInputPorts int) Builder {
	b.numInputPorts = numInputPorts
	return b
}

// WithNumOutputPorts sets the number of output ports for the crossbar
func (b Builder) WithNumOutputPorts(numOutputPorts int) Builder {
	b.numOutputPorts = numOutputPorts
	return b
}

// WithNumSetsPerTLB sets the number of sets/TLB
func (b Builder) WithNumSetsPerTLB(numSetsPerTLB int) Builder {
	b.numSetsPerTLB = numSetsPerTLB
	return b
}

func (b Builder) Build(name string) *Crossbar {
	crossbar := &Crossbar{}
	crossbar.TickingComponent = sim.NewTickingComponent(name, b.engine, b.freq, crossbar)

	crossbar.name = name
	crossbar.numInputPorts = b.numInputPorts
	crossbar.numOutputPorts = b.numOutputPorts
	crossbar.numSetsPerTLB = b.numSetsPerTLB
	crossbar.reqIDMap = make(map[string]string)
	// crossbar.latency = int(math.Log2(float64(crossbar.numInputPorts)) + math.Log2(float64(crossbar.numOutputPorts)))
	crossbar.latency = int(math.Log2(math.Max(float64(crossbar.numInputPorts), float64(crossbar.numOutputPorts))))
	crossbar.inputPortToService = 0
	crossbar.outputPortToService = 0
	crossbar.log2PageSize = 12

	logger.PrintLog(true, fmt.Sprintf("\n[LOG] Building[%s] %d X %d NumSetsPerTLB[%d] Latency[%d]",
		crossbar.name, crossbar.numInputPorts, crossbar.numOutputPorts, crossbar.numSetsPerTLB, crossbar.latency), LogColorCrossbar)

	b.createPorts(crossbar)

	return crossbar
}

func (b Builder) createPorts(crossbar *Crossbar) *Crossbar {
	inputPortBufSize := 8
	outputPortBufSize := (inputPortBufSize * b.numInputPorts) / b.numOutputPorts

	for i := 0; i < b.numInputPorts; i++ {
		inputPortName := fmt.Sprintf("%s.InputPort_%02d", crossbar.name, i)
		inputPort := sim.NewLimitNumMsgPort(crossbar, inputPortBufSize, inputPortName)
		crossbar.AddPort(inputPortName, inputPort)
		crossbar.InputPorts = append(crossbar.InputPorts, inputPort)
	}

	for i := 0; i < b.numOutputPorts; i++ {
		outputPortName := fmt.Sprintf("%s.OutputPort_%02d", crossbar.name, i)
		outputPort := sim.NewLimitNumMsgPort(crossbar, outputPortBufSize, outputPortName)
		crossbar.AddPort(outputPortName, outputPort)
		crossbar.OutputPorts = append(crossbar.OutputPorts, outputPort)
	}

	return crossbar
}

func (crossbar *Crossbar) PopulateATPorts(crossbarIndex int, l1vAddrTrans []*addresstranslator.AddressTranslator) {
	atIndex := crossbarIndex * crossbar.numInputPorts
	for i := 0; i < crossbar.numInputPorts; i++ {
		at := l1vAddrTrans[atIndex]
		atTranslationPort := at.GetPortByName("Translation")
		crossbar.atTranslationPorts = append(crossbar.atTranslationPorts, atTranslationPort)
		atIndex++
	}
}

func (crossbar *Crossbar) PopulateTLBPorts(crossbarIndex int, l1vTLBs []*tlb.TLB) {
	tlbIndex := crossbarIndex * crossbar.numOutputPorts
	for i := 0; i < crossbar.numOutputPorts; i++ {
		tlb := l1vTLBs[tlbIndex]
		tlbTopPort := tlb.GetPortByName("Top")
		crossbar.tlbTopPorts = append(crossbar.tlbTopPorts, tlbTopPort)
		tlbIndex++
	}
}
