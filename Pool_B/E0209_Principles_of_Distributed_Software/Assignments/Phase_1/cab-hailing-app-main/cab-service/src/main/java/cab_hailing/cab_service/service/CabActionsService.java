package cab_hailing.cab_service.service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cab_hailing.cab_service.Logger;
import cab_hailing.cab_service.model.Cab;
import cab_hailing.cab_service.model.CabStatus;
import cab_hailing.cab_service.repository.CabRepository;
import cab_hailing.cab_service.repository.CabStatusRepository;
import cab_hailing.cab_service.rest_consumers.RideServiceRestConsumer;
import cab_hailing.cab_service.values.CabMajorStates;
import cab_hailing.cab_service.values.CabMinorStates;

@Component
public class CabActionsService {

	// ---------------------------------------------------------------------------------------------

	@Autowired
	CabRepository cabRepo;

	@Autowired
	CabStatusRepository cabStatusRepo;

	@Autowired
	RideServiceRestConsumer rideServiceRestConsumer;

	@PersistenceContext
	EntityManager em;

	// ---------------------------------------------------------------------------------------------

	/*
	 * Cab driver will send this request, to indicate his/her desire to sign-in with
	 * starting location initialPos. If cabId is a valid ID and the cab is currently
	 * in signed-out state, then send a request to RideService.cabSignsIn, forward
	 * the response from RideService.cabSignsIn back to the driver, and transition
	 * to signed-in state iff the response is true. Otherwise, else respond with -1
	 * and do not change state.
	 */
	@Transactional
	public boolean signIn(long cabID, long initialPos) {
		Logger.log("Received sign in request for cab id : " + cabID);
		// Check if cabID is valid
		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_WRITE); 
		
		if (cab == null) {
			Logger.logErr("Cab id : " + cabID + " is invalid so return false for signIn");
			return false;
		}
		
		if (initialPos < 0) {
			Logger.logErr("Initial Pos : " + initialPos + " is invalid so return false for signIn");
			return false;
		}

		/*
		 * Get corresponding record from cab_status. If cab status record not found,
		 * then insert the record houldn't happen ideally, but if so, it is a quick fix
		 * to insert the record
		 */
		CabStatus cabStatus = cab.getCabStatus();
		if (cabStatus == null) {
			cabStatus = new CabStatus(cabID);
			cabStatus = cabStatusRepo.save(cabStatus);
		}

		// Check if cab is in SIGNED_OUT state
		if (cabStatus.getMajorState() != null && cabStatus.getMajorState().equals(CabMajorStates.SIGNED_OUT)) {

			// If RideService responds with success
			boolean ifCabSignsInSuccess = rideServiceRestConsumer.consumeCabSignsIn(cabID, initialPos);
			if (ifCabSignsInSuccess) {
				cabStatus.setMajorState(CabMajorStates.SIGNED_IN);
				cabStatus.setMinorState(CabMinorStates.AVAILABLE);
				cabStatus.setCurrRideID(null);
				cabStatus.setnRequestsRecvd(Long.valueOf(0));
				cabStatus.setnRidesGiven(Long.valueOf(0));

				cabStatusRepo.save(cabStatus);

				Logger.log("Successfully signed in cab id : " + cabID);

				return true;
			}
		}

		Logger.logErr("Reached end of function and couldn't sign in cab id : " + cabID);
		return false;
	}

	// ---------------------------------------------------------------------------------------------

	/*
	 * Cab driver will send this request, to indicate his/her desire to sign-out. If
	 * cabId is a valid ID and the cab is currently in signed-in state, then send a
	 * request to RideService.cabSignsOut, forward the response from
	 * RequestRide.cabSignsOut back to the driver, and transition to signed-out
	 * state iff the response is true. Otherwise, else respond with -1 and do not
	 * change state.
	 */
	@Transactional
	public boolean signOut(long cabID) {
		Logger.log("Received sign out request for cab id : " + cabID);

		// Check if cabID is valid
		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_WRITE); 
		if (cab == null) {
			Logger.logErr("Cab id : " + cabID + " is invalid so return false for signOut");
			return false;
		}

		// Get corresponding record from cab_status.
		CabStatus cabStatus = cab.getCabStatus();

		if (cabStatus != null) {
			String cabMajorState = cabStatus.getMajorState();
			String cabMinorState = cabStatus.getMinorState();

			// Check if cab is SIGNED_IN
			if (cabMajorState == null || !cabMajorState.equals(CabMajorStates.SIGNED_IN)) {
				Logger.logErr("Cab not in signed in state so couldn't sign out cab id : " + cabID);
				return false;
			}

			// Check if cab is AVAILABLE
			if (cabMinorState == null || !cabMinorState.equals(CabMinorStates.AVAILABLE)) {
				Logger.logErr("Cab not in available state so couldn't sign out cab id : " + cabID);
				return false;
			}

			// If RideService responds with success
			boolean ifCabSignsOut = rideServiceRestConsumer.consumeCabSignsOut(cabID);
			if (ifCabSignsOut) {
				cabStatus.setMajorState(CabMajorStates.SIGNED_OUT);
				cabStatus.setMinorState(CabMinorStates.NONE);
				cabStatus.setnRidesGiven(Long.valueOf(0));
				cabStatus.setnRequestsRecvd(Long.valueOf(0));
				cabStatus.setCurrRideID(null);
				cabStatusRepo.save(cabStatus);

				Logger.log("Successfully signed out cab id : " + cabID);

				return true;
			}
		}

		Logger.logErr("Reached end of function and couldn't sign out cab id : " + cabID);
		return false;
	}

	// ---------------------------------------------------------------------------------------------

	/*
	 * To be used mainly for testing purposes. If cabId is invalid, return -1.
	 * Otherwise, if cabId is currently signed-in then return number of rides given
	 * so far after the last sign-in (including ongoing ride if currently in
	 * giving-ride state), else return 0.
	 */
	@Transactional
	public long numRides(long cabID) {
		Logger.log("Received request numRides for Cab id : " + cabID);
		
		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_READ); 
		if (cab == null) {
			Logger.logErr("Cab id : " + cabID + " is not valid so return -1");
			return -1;
		}

		CabStatus cabStatus = cab.getCabStatus();

		if (cabStatus != null) {
			Long nRidesGiven = cabStatus.getnRidesGiven();

			// cab is signed in and available
			if (nRidesGiven != null) {
				Logger.log("numRides request success for Cab id : " + cabID + ", returned : " + nRidesGiven.longValue());
				return nRidesGiven.longValue();
			}
		}
		
		Logger.logErr("Reached end of numRides function for Cab id : " + cabID + ", so return -1");
		return -1;
	}

	// ---------------------------------------------------------------------------------------------

}
