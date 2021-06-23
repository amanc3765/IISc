package cab_hailing.ride_service.db_init;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cab_hailing.ride_service.repository.RideRepository;
import cab_hailing.ride_service.values.CabMajorStates;
import cab_hailing.ride_service.values.CabMinorStates;
import cab_hailing.ride_service.repository.CabStatusRepository;

@Component
public class DBInitializer {
	
	
	@PersistenceContext
    private EntityManager entityManager;	

	@Autowired
	DBInitFileReader dbInitFileReader;
	
	@Autowired
	RideRepository rideRepository;
	
	@Autowired
	CabStatusRepository cabStatusRepository;	
	
	//-----------------------------------------------------------
	// To be used for first run DB initialization as well as resetting microservice
	@Transactional
	public void initAllTables() {
		System.out.println("LOG : Trying to load all DB tables from files in folder : " + dbInitFileReader.fileDirectory);
		try {
			dbInitFileReader.readInitFile();
			initCabStatusTable();
			System.out.println("LOG : All table initialization complete");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// To be used privately for resetting the micro-service
	@Transactional
	private void clearAllTables() {
		// Respect the foreign key constraints and order while truncating
		rideRepository.deleteAll();
		cabStatusRepository.deleteAll();		
	}
		
	// To be invoked by reset end-point to clear and reload the state of tables
	@Transactional
	public void resetAndLoadAllTables(){
		clearAllTables();
		initAllTables();
	}	
	
		
	@Transactional
	private void initCabStatusTable() throws IOException {
		for (Long cabID : dbInitFileReader.cabIDList) {
			entityManager.createNativeQuery(
					"INSERT INTO CAB_STATUS(CAB_ID,MAJOR_STATE ,MINOR_STATE, CURR_POS) SELECT ?,?,?,? "
					+ "FROM DUAL WHERE NOT EXISTS (SELECT * FROM CAB_STATUS WHERE CAB_ID=?)")
					.setParameter(1, cabID)
					.setParameter(2, CabMajorStates.SIGNED_OUT)
					.setParameter(3, CabMinorStates.NONE)
					.setParameter(4, Long.valueOf(-1))
					.setParameter(5, cabID)
					.executeUpdate();
		}
	}

}
