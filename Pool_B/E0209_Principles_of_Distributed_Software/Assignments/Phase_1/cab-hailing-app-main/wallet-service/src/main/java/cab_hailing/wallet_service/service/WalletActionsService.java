package cab_hailing.wallet_service.service;

import javax.persistence.LockModeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cab_hailing.wallet_service.Logger;
import cab_hailing.wallet_service.model.Customer;
import cab_hailing.wallet_service.model.Wallet;
import cab_hailing.wallet_service.repository.CustomerRepository;
import cab_hailing.wallet_service.repository.WalletRepository;

/**
 * This service provides atomic transactions on users' wallet. The following
 * services are provided - getBalance - deductAmount - addAmount - reset
 * 
 * @author Shashank Singh (cse.shashanksingh@gmail.com, shashanksing@iisc.ac.in)
 */

@Service
public class WalletActionsService {

	@Autowired
	CustomerRepository custRep;

	@Autowired
	WalletRepository walletRep;

	@PersistenceContext
	EntityManager em;

	@Transactional
	public long getBalance(long custID) {

		Logger.log("getBalance : Request received for custID:" + custID);

		Customer customer = em.find(Customer.class, custID, LockModeType.PESSIMISTIC_READ);
//		Customer customer = custRep.findById(custID).orElse(new Customer());
		
		Wallet custWallet = customer.getWallet();

		if (custWallet != null) {
			Logger.log("getBalance : Request success for custID:" + custID + " with balance: "
					+ custWallet.getBalanceAmount());
			return custWallet.getBalanceAmount();
		} else
			return -1;
	}

	@Transactional
	public boolean deductAmount(long custID, long deductionAmount) {

		Logger.log("deductAmount : Request received for custID:" + custID + ", dedAmount:" + deductionAmount);

		if (deductionAmount < 0) {
			Logger.logErr("deductionAmount : " + deductionAmount + " is invalid so return false for deductAmount");
			return false;
		}

		Customer customer = em.find(Customer.class, custID, LockModeType.PESSIMISTIC_WRITE);
//		Customer customer = custRep.findById(custID).orElse(new Customer());
		
		Wallet custWallet = customer.getWallet();

		if (custWallet != null) {
			long availBalance = custWallet.getBalanceAmount();

			if (availBalance >= deductionAmount) {
				custWallet.setBalanceAmount(availBalance - deductionAmount);
				custWallet = walletRep.save(custWallet);
				Logger.log("deductAmount : Success for custID:" + custID + ", dedAmount:" + deductionAmount
						+ ", curBalance:" + custWallet.getBalanceAmount());

				return true;
			} else {
				Logger.logErr("Insufficient balance, couldn't deduct amount : " + deductionAmount + " for custID : "
						+ custID);
				return false;
			}

		} else
			return false;
	}

	@Transactional
	public boolean addAmount(long custID, long additionAmount) {

		Logger.log("addAmount : Request received for custID:" + custID + ", additionAmount:" + additionAmount);

		if (additionAmount < 0) {
			Logger.logErr("additionAmount : " + additionAmount + " is invalid so return false for addAmount");
			return false;
		}

		Customer customer = em.find(Customer.class, custID, LockModeType.PESSIMISTIC_WRITE);
//		Customer customer = custRep.findById(custID).orElse(new Customer());
		
		Wallet custWallet = customer.getWallet();

		if (custWallet != null) {
			long availBalance = custWallet.getBalanceAmount();

			custWallet.setBalanceAmount(availBalance + additionAmount);
			custWallet = walletRep.save(custWallet);

			Logger.log("addAmount : Success for custID:" + custID + ", additionAmount:" + additionAmount
					+ ", curBalance:" + custWallet.getBalanceAmount());
			return true;

		} else
			return false;
	}
}
