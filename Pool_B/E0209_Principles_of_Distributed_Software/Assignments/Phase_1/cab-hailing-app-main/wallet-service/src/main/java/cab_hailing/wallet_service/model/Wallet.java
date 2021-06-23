package cab_hailing.wallet_service.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "Wallets")
public class Wallet {
	
	//-----------------------------------------------------
	@Id
	@GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "WalletSeqGen"
    )
    @SequenceGenerator(name = "WalletSeqGen",
                initialValue = 3001, allocationSize = 1
    )
	@Column(name = "wallet_id")
	long walletID;
	
	//-----------------------------------------------------
	@Column(name = "balance_amt")
	long balanceAmount;
	
	
	//-----------------------------------------------------
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="cust_id", referencedColumnName="cust_id")
	Customer customer;
	
	
	//-----------------------------------------------------
	public Wallet(long walletID, long balanceAmount, Customer customer ) {
		super();
		this.walletID = walletID;
		this.customer = customer;
		this.balanceAmount = balanceAmount;
	}

	public Wallet(Customer customer, long balanceAmount) {
		this.customer = customer;
		this.balanceAmount = balanceAmount;
	}
	
	public Wallet() {}
	
	
	//-----------------------------------------------------
	public long getWalletID() {
		return walletID;
	}

	public void setWalletID(long walletID) {
		this.walletID = walletID;
	}
	
	
	//-----------------------------------------------------
	public long getBalanceAmount() {
		return balanceAmount;
	}

	public void setBalanceAmount(long balanceAmount) {
		this.balanceAmount = balanceAmount;
	}

	
	//-----------------------------------------------------
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	
}
