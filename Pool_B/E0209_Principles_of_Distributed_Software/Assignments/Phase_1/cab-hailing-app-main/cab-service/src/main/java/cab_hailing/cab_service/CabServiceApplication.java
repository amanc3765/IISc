package cab_hailing.cab_service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cab_hailing.cab_service.db_init.DBInitializer;

@SpringBootApplication
public class CabServiceApplication implements CommandLineRunner{
	
	@Autowired
	DBInitializer dbInitializer;
	
	public static void main(String[] args) {
		SpringApplication.run(CabServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("LOG : Command Line Runner Invoked");
		dbInitializer.initAllTables();
	}
	
	
}
