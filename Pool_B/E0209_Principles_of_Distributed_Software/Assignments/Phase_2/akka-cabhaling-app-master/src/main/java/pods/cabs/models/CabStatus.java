package pods.cabs.models;

import pods.cabs.values.CabStates;

public class CabStatus {
	public String cabId;
	public String majorState;
	public String minorState;
	public long curPos;
	public long timeCounter;
	
	public CabStatus(String cabId, String majorState, String minorState, long initialPos) {
		super();
		this.cabId = cabId;
		this.majorState = majorState;
		this.minorState = minorState;
		this.curPos = initialPos;
		this.timeCounter = 0;
	}
	
	public CabStatus(String cabId) {
		super();
		this.cabId = cabId;
		this.majorState = CabStates.MajorStates.SIGNED_OUT;
		this.minorState = CabStates.MinorStates.NONE;
		this.curPos = -1;
		this.timeCounter = 0;
	}	
	
}
