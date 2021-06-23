package cab_hailing.wallet_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cab_hailing.wallet_service.db_init.DBInitializer;

@SpringBootApplication
@EnableTransactionManagement
public class WalletServiceApplication implements CommandLineRunner{
	
	@Autowired
	DBInitializer dbInitializer;
	
	public static void main(String[] args) {
		SpringApplication.run(WalletServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("LOG : Command Line Runner Invoked");
		dbInitializer.initAllTables();
	}
	
	
}
