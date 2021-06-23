package cab_hailing.cab_service.model;


import javax.persistence.Column;  
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "Cabs")
public class Cab {
	
	//Columns----------------------------------------------
	@Id
	@GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "CabSeqGen"
    )
    @SequenceGenerator(name = "CabSeqGen",
                initialValue = 1001, allocationSize = 1
    )
	@Column(name = "cab_id")
	private long cabID;	
	
	@Column(name = "password")
	private String password;
	
	
	//-----------------------------------------------------	
	@OneToOne(mappedBy="cab")
	private CabStatus cabStatus;

	
	//-----------------------------------------------------	
	public Cab() {
		super();
	}
	
	public Cab(long cabID, String cabPassword) {
		super();
		this.cabID = cabID;
		this.password = cabPassword;
	}
	
	
	//-----------------------------------------------------	
	public long getCabID() {
		return cabID;
	}

	public void setCabID(long cabID) {
		this.cabID = cabID;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String cabPassword) {
		this.password = cabPassword;
	}	
	
	public CabStatus getCabStatus() {
		return cabStatus;
	}

	public void setCabStatus(CabStatus cabStatus) {
		this.cabStatus = cabStatus;
	}
	
}
