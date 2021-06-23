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
public class RideActionsService {
	@Autowired
	CabRepository cabRepo;

	@Autowired
	CabStatusRepository cabStatusRepo;

	@Autowired
	RideServiceRestConsumer rideServiceRestConsumer;
	
	@PersistenceContext
	EntityManager em;

	@Transactional
	public boolean requestRide(long cabID, long rideID, long sourceLoc, long destinationLoc) {
		Logger.log("requestRide: Received request for cabID:" + cabID + ", rideID:" + rideID);

		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_WRITE); 
		if (cab == null) {
			Logger.logErr("requestRide: Cab id:" + cabID + " is invalid");
			return false;
		}

		CabStatus cabStatus = cab.getCabStatus();
		if (cabStatus != null) {
			String cabMajorState = cabStatus.getMajorState();
			String cabMinorState = cabStatus.getMinorState();
			Long nRequestsRecvd = cabStatus.getnRequestsRecvd();

			if (cabMajorState == null || !cabMajorState.equals(CabMajorStates.SIGNED_IN))
				return false;

			if (cabMinorState == null || !cabMinorState.equals(CabMinorStates.AVAILABLE))
				return false;

			long nReqVal = nRequestsRecvd == null ? 0 : nRequestsRecvd.longValue();

			cabStatus.setnRequestsRecvd(nReqVal + 1);

			if (nReqVal % 2 != 0) {
				// saving to update nRequests count
				cabStatusRepo.save(cabStatus);
				return false;
			}

			cabStatus.setCurrRideID(rideID);
			cabStatus.setMinorState(CabMinorStates.COMMITTED);

			cabStatusRepo.save(cabStatus);
			return true;
		}

		return false;
	}

	@Transactional
	public boolean rideCancelled(long cabID, long rideID) {
		Logger.log("rideCancelled: Received request for cabID:" + cabID + ", rideID:" + rideID);

		// Check if cab id is valid and in riding state with this rideID
		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_WRITE); 
		if (cab == null) {
			Logger.logErr("rideCancelled: Cab id:" + cabID + " is invalid");
			return false;
		}

		CabStatus cabStatus = cab.getCabStatus();
		if (cabStatus != null) {
			String cabMajorState = cabStatus.getMajorState();
			String cabMinorState = cabStatus.getMinorState();
			Long curRideID = cabStatus.getCurrRideID();

			// cab is signed in and available
			if (cabMajorState == null || !cabMajorState.equals(CabMajorStates.SIGNED_IN))
				return false;

			if (cabMinorState == null || !cabMinorState.equals(CabMinorStates.COMMITTED))
				return false;

			if (curRideID == null || curRideID.longValue() != rideID)
				return false;

			cabStatus.setMinorState(CabMinorStates.AVAILABLE);
			cabStatus.setCurrRideID(null);
			cabStatusRepo.save(cabStatus);
			
			Logger.log("rideCancelled: Success for Cab id:" + cabID + ", rideID:" + rideID);
			return true;
		}

		Logger.logErr("rideCancelled: Reached end of function for Cab id:" + cabID + ", rideID:" + rideID);
		return false;
	}

	@Transactional
	public boolean rideStarted(long cabID, long rideID) {
		Logger.log("rideStarted: Received request for cabID:" + cabID + ", rideID:" + rideID);
		
		// Check if cab id is valid and in riding state with this rideID
		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_WRITE); 
		if (cab == null) {
			Logger.logErr("rideStarted: Cab id:" + cabID + " is invalid");
			return false;
		}

		CabStatus cabStatus = cab.getCabStatus();
		if (cabStatus != null) {
			String cabMajorState = cabStatus.getMajorState();
			String cabMinorState = cabStatus.getMinorState();
			Long curRideID = cabStatus.getCurrRideID();

			// cab is signed in and available
			if (cabMajorState == null || !cabMajorState.equals(CabMajorStates.SIGNED_IN))
				return false;

			if (cabMinorState == null || !cabMinorState.equals(CabMinorStates.COMMITTED))
				return false;

			if (curRideID == null || curRideID.longValue() != rideID)
				return false;

			cabStatus.setMinorState(CabMinorStates.GIVING_RIDE);
			cabStatus.setnRidesGiven(cabStatus.getnRidesGiven() + 1);

			cabStatusRepo.save(cabStatus);
			
			Logger.log("rideStarted: Success for Cab id:" + cabID + ", rideID:" + rideID);
			return true;
		}

		return false;
	}

	@Transactional
	public boolean rideEnded(long cabID, long rideID) {
		Logger.log("Received rideEnded request for cab id : " + cabID + ", ride id : " + rideID);

		// Check if cab id is valid and in riding state with this rideID
		Cab cab = em.find(Cab.class, cabID, LockModeType.PESSIMISTIC_WRITE);
		if (cab == null) {
			Logger.logErr("Couldn't find entry for cab id : " + cabID);
			return false;
		}

		CabStatus cabStatus = cab.getCabStatus();
		if (cabStatus != null) {
			String cabMajorState = cabStatus.getMajorState();
			String cabMinorState = cabStatus.getMinorState();
			Long curRideID = cabStatus.getCurrRideID();

			// cab is signed in and available
			if (cabMajorState == null || !cabMajorState.equals(CabMajorStates.SIGNED_IN)) {
				Logger.logErr("Couldn't end ride - Cab id : " + cabID + " not in signed in state");
				return false;
			}

			if (cabMinorState == null || !cabMinorState.equals(CabMinorStates.GIVING_RIDE)) {
				Logger.logErr("Couldn't end ride - Cab id : " + cabID + " not in giving ride state");
				return false;
			}

			if (curRideID == null || curRideID.longValue() != rideID) {
				Logger.logErr(
						"Couldn't end ride - Cab id : " + cabID + " current ride id mismatch with ride id : " + rideID);
				return false;
			}

			// forward the request to RideService
			// if RideService responds with success, set status to available
			boolean ifRideEnded = rideServiceRestConsumer.consumeRideEnded(rideID);
			if (ifRideEnded) {
				Logger.log("Ride ended for ride id : " + rideID);
				cabStatus.setMinorState(CabMinorStates.AVAILABLE);
				cabStatus.setCurrRideID(null);

				cabStatusRepo.save(cabStatus);

				Logger.log("Successfully ended ride for cab id : " + cabID + ", ride id : " + rideID);
				return true;
			} else {
				Logger.logErr("Ride service responded with error for rideEnded request for cab id : " + cabID
						+ ", ride id : " + rideID);
			}

		}

		return false;
	}
}
