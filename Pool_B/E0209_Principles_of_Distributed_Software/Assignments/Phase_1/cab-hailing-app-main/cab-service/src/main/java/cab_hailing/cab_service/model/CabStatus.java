package cab_hailing.cab_service.model;


import javax.persistence.CascadeType; 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import cab_hailing.cab_service.values.CabMajorStates;
import cab_hailing.cab_service.values.CabMinorStates;


@Entity
@Table(name = "Cab_Status")
public class CabStatus {
	
	//Columns----------------------------------------------
	@Id
	@Column(name = "cab_id")
	private Long cabID;	
	
	@Column(name = "major_state",length=1)
	private String majorState;
	
	@Column(name = "minor_state",length=1)
	private String minorState;
	
	@Column(name = "curr_ride_id")
	private Long currRideID;
	
	@Column(name = "n_requests_recvd")
	private Long nRequestsRecvd;
	
	@Column(name = "n_rides_given")
	private Long nRidesGiven;
	
	
	//-----------------------------------------------------
	@OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Cab cab;

	
	//-----------------------------------------------------
	public CabStatus() {
		super();
	}
	
	public CabStatus(Long cabID) {
		super();
		this.cabID = cabID;
		this.majorState = CabMajorStates.SIGNED_OUT;
		this.minorState = CabMinorStates.NONE;
		this.currRideID = null;
		this.nRequestsRecvd= Long.valueOf(0);
		this.nRidesGiven= Long.valueOf(0);
	}	

	public CabStatus(Long cabID, String majorState, String minorState, Long currRideID, 
			Long nRidesGiven, Long nRequestsRecvd, Cab cab) {
		super();
		this.cabID = cabID;
		this.majorState = majorState;
		this.minorState = minorState;
		this.currRideID = currRideID;
		this.nRidesGiven = nRidesGiven;
		this.nRequestsRecvd = nRequestsRecvd;
		this.cab = cab;
	}

	
	//-----------------------------------------------------	
	public Long getCabID() {
		return cabID;
	}

	public void setCabID(Long cabID) {
		this.cabID = cabID;
	}

	public String getMajorState() {
		return majorState;
	}

	public void setMajorState(String majorState) {
		this.majorState = majorState;
	}

	public String getMinorState() {
		return minorState;
	}

	public void setMinorState(String minorState) {
		this.minorState = minorState;
	}
	
	public Long getCurrRideID() {
		return currRideID;
	}

	public void setCurrRideID(Long currRideID) {
		this.currRideID = currRideID;
	}
	
	public Long getnRidesGiven() {
		return nRidesGiven;
	}

	public void setnRidesGiven(Long nRidesGiven) {
		this.nRidesGiven = nRidesGiven;
	}

	public Long getnRequestsRecvd() {
		return nRequestsRecvd;
	}

	public void setnRequestsRecvd(Long nRequestsRecvd) {
		this.nRequestsRecvd = nRequestsRecvd;
	}
	
	public Cab getCab() {
		return cab;
	}	
	
	public void setCab(Cab cab) {
		this.cab = cab;
	}

}
