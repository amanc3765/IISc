package cab_hailing.wallet_service.repository;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cab_hailing.wallet_service.model.Customer;
import cab_hailing.wallet_service.model.Wallet;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, Long> {
	
	public Wallet findWalletByCustomer(Customer customer);
	

}
