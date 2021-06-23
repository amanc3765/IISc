package cab_hailing.ride_service.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "Rides")
public class Ride {
	
	//----------------------------------------------
	@Id
	@GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "RideSeqGen"
    )
    @SequenceGenerator(name = "RideSeqGen",
                initialValue = 100001, allocationSize = 1
    )
	@Column(name = "ride_id")
	private Long rideID;	
	
	@Column(name = "cust_id")
	private Long custID;
	
	@Column(name = "src_pos")
	private Long srcPos;
	
	@Column(name = "dest_pos")
	private Long destPos;
		

	@Column(name = "ride_state",length=1)
	private String rideState;
	
		
	//-----------------------------------------------------
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="cab_id", referencedColumnName="cab_id")
    private CabStatus cabStatus;
		
	
	//----------------------------------------------
	public Ride(Long rideID, Long custID, Long srcPos, Long destPos, String rideState,
			CabStatus cabStatus) {
		super();
		this.rideID = rideID;
		this.custID = custID;
		this.cabStatus = cabStatus;
		this.srcPos = srcPos;
		this.destPos = destPos;
		this.rideState = rideState;
		this.cabStatus = cabStatus;
	}
	
	public Ride() {
		super();
	}

	//----------------------------------------------
	public Long getSrcPos() {
		return srcPos;
	}

	public void setSrcPos(Long srcPos) {
		this.srcPos = srcPos;
	}

	public Long getDestPos() {
		return destPos;
	}

	public void setDestPos(Long destPos) {
		this.destPos = destPos;
	}
	
	public Long getRideID() {
		return rideID;
	}

	public void setRideID(Long rideID) {
		this.rideID = rideID;
	}

	public Long getCustID() {
		return custID;
	}

	public void setCustID(Long custID) {
		this.custID = custID;
	}


	public String getRideState() {
		return rideState;
	}

	public void setRideState(String rideState) {
		this.rideState = rideState;
	}

	public CabStatus getCabStatus() {
		return cabStatus;
	}

	public void setCabStatus(CabStatus cabStatus) {
		this.cabStatus = cabStatus;
	}
	 
	
}
