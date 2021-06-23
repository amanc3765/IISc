package cab_hailing.wallet_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cab_hailing.wallet_service.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>{

}
