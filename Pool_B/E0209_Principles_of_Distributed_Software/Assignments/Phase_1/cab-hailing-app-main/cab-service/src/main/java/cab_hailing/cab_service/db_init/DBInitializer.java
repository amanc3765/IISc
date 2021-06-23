package cab_hailing.cab_service.db_init;

import java.io.File; 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cab_hailing.cab_service.repository.CabRepository;
import cab_hailing.cab_service.repository.CabStatusRepository;
import cab_hailing.cab_service.values.CabMajorStates;
import cab_hailing.cab_service.values.CabMinorStates;

@Component
public class DBInitializer {
	
	@Autowired
	DBInitFileReader dbInitFileReader;
	
	@Autowired
	CabRepository cabRepository;

	@Autowired
	CabStatusRepository cabStatusRepository;

	@PersistenceContext
	private EntityManager entityManager;

	// To be used for first run DB initialization as well as resetting microservice
	@Transactional
	public void initAllTables() {
		System.out.println("LOG : Trying to load all DB tables from files in folder : " + dbInitFileReader.fileDirectory);
		try {
			dbInitFileReader.readInitFile();
			initCabTable();
			initCabStatusTable();
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
		cabStatusRepository.deleteAll();
		cabRepository.deleteAll();
	}

	@Transactional
	private void initCabTable() throws IOException {

		for (Long cabID : dbInitFileReader.cabIDList) {
			entityManager.createNativeQuery("INSERT INTO CABS(cab_id, password) VALUES (?,?)")
						.setParameter(1, cabID)
					.setParameter(2, "pass").executeUpdate();
		}
	}

	@Transactional
	private void initCabStatusTable() throws IOException {
		for (Long cabID : dbInitFileReader.cabIDList) {
			entityManager.createNativeQuery(
					"INSERT INTO CAB_STATUS(CAB_ID,CURR_RIDE_ID,MAJOR_STATE,MINOR_STATE,N_REQUESTS_RECVD,N_RIDES_GIVEN) VALUES (?,?,?,?,?,?)")
					.setParameter(1, cabID)
					.setParameter(2, null)
					.setParameter(3, CabMajorStates.SIGNED_OUT)
					.setParameter(4, CabMinorStates.NONE)
					.setParameter(5, Long.valueOf(0))
					.setParameter(6, Long.valueOf(0)).executeUpdate();
		}
	}

}
