package cab_hailing.wallet_service.db_init;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cab_hailing.wallet_service.repository.CustomerRepository;
import cab_hailing.wallet_service.repository.WalletRepository;


@Component
public class DBInitializer {

	@Autowired
	CustomerRepository custRepo;

	@Autowired
	WalletRepository walletRepo;
	
	@Autowired
	DBInitFileReader dbInitFileReader;
		
	@PersistenceContext
    private EntityManager entityManager;
	
	// To be used for first run DB initialization as well as resetting microservice
	@Transactional
	public void initAllTables() {
		System.out.println("LOG : Trying to load all DB tables from files in folder : " + dbInitFileReader.fileDirectory);
		try {
			dbInitFileReader.readInitFile();
			initCustomersTable();
			initWalletsTable();
			System.out.println("LOG : All table initialization complete");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// To be invoked by reset end-point to clear and reload the state of tables
	@Transactional
	public void resetAndLoadAllTables() {
		clearAllTables();
		initAllTables();
	}
	
	// To be used privately for resetting the micro-service
	private void clearAllTables() {
		// Respect the foreign key constraints and order while truncating
		walletRepo.deleteAll();
		custRepo.deleteAll();
	}
	
	
	@Transactional
	private void initCustomersTable() throws IOException {
		for (Long custID : dbInitFileReader.custIDList) {
			entityManager.createNativeQuery(
					"INSERT INTO CUSTOMERS (CUST_ID,PASSWORD) VALUES (?,?)")
					.setParameter(1, custID)
					.setParameter(2, "pass")
					.executeUpdate();
		}
	}
	
	@Transactional
	private void initWalletsTable() throws IOException {
		long wallet_id = 301;
		
		for (Long custID : dbInitFileReader.custIDList) {
			entityManager.createNativeQuery(
					"INSERT INTO WALLETS(wallet_id,balance_amt,cust_id) VALUES (?,?,?)")
					.setParameter(1, wallet_id++)
					.setParameter(2, dbInitFileReader.walletBalance)
					.setParameter(3, custID)
				    .executeUpdate();
		}
		
	}


}
