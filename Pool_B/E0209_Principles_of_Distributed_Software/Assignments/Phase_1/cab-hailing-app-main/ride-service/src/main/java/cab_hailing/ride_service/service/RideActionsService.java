package cab_hailing.ride_service.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import cab_hailing.ride_service.Logger;
import cab_hailing.ride_service.model.CabStatus;
import cab_hailing.ride_service.model.Ride;
import cab_hailing.ride_service.repository.CabStatusRepository;
import cab_hailing.ride_service.repository.RideRepository;
import cab_hailing.ride_service.rest_consumers.CabServiceRestConsumer;
import cab_hailing.ride_service.rest_consumers.WalletServiceRestConsumer;
import cab_hailing.ride_service.values.CabMajorStates;
import cab_hailing.ride_service.values.CabMinorStates;
import cab_hailing.ride_service.values.RideStates;
import org.springframework.data.domain.Sort;

@Component
public class RideActionsService {

	// ---------------------------------------------------------------------------------------------

	@Autowired
	RideRepository rideRepo;

	@Autowired
	CabStatusRepository cabStatusRepo;

	@Autowired
	CabServiceRestConsumer cabServiceRestConsumer;

	@Autowired
	WalletServiceRestConsumer walletServiceRestConsumer;
	
	@PersistenceContext
	EntityManager em;

	// ---------------------------------------------------------------------------------------------
	/*
	 * Cab uses this request, to signal that rideId has ended (at the chosen
	 * destination). Return true iff rideId corresponds to an ongoing ride.
	 */
	@Transactional
	public boolean rideEnded(long rideId) {
		Logger.log("Received rideEnded request for ride id : " + rideId);

		// Check if ride id is valid
		Ride ride = em.find(Ride.class, rideId, LockModeType.PESSIMISTIC_WRITE); 

		if (ride != null) {
			String rideStatus = ride.getRideState();

			if (rideStatus != null && rideStatus.equals(RideStates.ONGOING)) {
				ride.setRideState(RideStates.COMPLETED);

				CabStatus cabStatus = em.find(CabStatus.class, ride.getCabStatus().getCabID(), LockModeType.PESSIMISTIC_WRITE);
				cabStatus.setMinorState(CabMinorStates.AVAILABLE);
				cabStatus.setCurrPos(ride.getDestPos());

				rideRepo.saveAndFlush(ride);
				cabStatusRepo.saveAndFlush(cabStatus);

				Logger.log("Ride ended successfully for ride id : " + rideId);
				return true;
			} else {
				Logger.log("Ride id : " + rideId + " is not in ongoing state, so return false");
			}
		} else {
			Logger.log("Couldn't find ride id : " + rideId + ", so return false");
		}

		Logger.logErr("Reached end of function rideEnded so returning false for ride id : " + rideId);
		return false;
	}

	// ---------------------------------------------------------------------------------------------
	/*
	 * Customer uses this to request a ride from the service. The cab service should
	 * first generate a globally unique rideId corresponding to the received
	 * request. It should then try to find a cab (using Cab.requestRide) that is
	 * willing to accept this ride. It should request cabs that are currently in
	 * available state one by one in increasing order of current distance of the cab
	 * from sourceLoc. The first time a cab accepts the request, the service should
	 * calculate the fare (the formula for this is described later) and attempt to
	 * deduct the fare from custId’s wallet. If the deduction was a success, send
	 * request Cab.rideStarted to the accepting cabId and then respond to the
	 * customer with the generated rideId, else send request Cab.rideCanceled to the
	 * accepting cabId and then respond with -1 to the customer. If three cabs have
	 * been requested and all of them reject, then respond with -1 to the customer.
	 * If fewer than three cabs have been contacted and they all reject the requests
	 * and there are no more cabs available to request that are currently signed-in
	 * and not currently giving a ride, respond with -1 to the customer. The fare
	 * for a ride is equal to the distance from the accepting cab’s current location
	 * to sourceLoc plus the distance from sourceLoc to destinationLoc, times 10
	 * (i.e., Rs. 10 per unit distance).
	 * 
	 */
	@Transactional
	public String requestRide(long custID, long sourceLoc, long destinationLoc) {
		
		if (sourceLoc < 0) {
			Logger.logErr("sourceLoc : " + sourceLoc + " is invalid so return false for requestRide");
			return "-1";
		}
		
		if (destinationLoc < 0) {
			Logger.logErr("destinationLoc : " + destinationLoc + " is invalid so return false for requestRide");
			return "-1";
		}
		
		// Generate a globally unique rideId
		Ride ride = new Ride();
		ride.setSrcPos(sourceLoc);
		ride.setDestPos(destinationLoc);
		ride.setCustID(custID);
		ride = rideRepo.saveAndFlush(ride);

		long rideID = ride.getRideID();

		List<CabStatus> candidateCabs = new ArrayList<CabStatus>();
		candidateCabs.addAll(cabStatusRepo.findTop3ByCurrPosGreaterThanEqualAndMajorStateAndMinorStateOrderByCurrPosAsc(
				sourceLoc, CabMajorStates.SIGNED_IN, CabMinorStates.AVAILABLE));

		candidateCabs.addAll(cabStatusRepo.findTop3ByCurrPosLessThanAndMajorStateAndMinorStateOrderByCurrPosDesc(
				sourceLoc, CabMajorStates.SIGNED_IN, CabMinorStates.AVAILABLE));

		// Now we sort cab list by distance - nearest first
		candidateCabs.sort(new CabStatusComparatorByCurPos(sourceLoc));

		CabStatus selectedCab = null;
		int nTries = 0;

		for (CabStatus candidateCab : candidateCabs) {
			Logger.log("requestRide: Checking if cab : " + candidateCab + " can accept ride");

			boolean ifAcceptSuccess = cabServiceRestConsumer.consumeRequestRide(candidateCab.getCabID(), rideID,
					sourceLoc, destinationLoc);
			if (ifAcceptSuccess) {
				Logger.log("requestRide: Cab ID : " + candidateCab.getCabID() + " accepted the ride request");
				selectedCab = candidateCab;
				break;
			}
			nTries++;
			if (nTries >= 3) {
				Logger.logErr("requestRide: Couldn't find any ride after 3 attempts");
				return "-1";
			}
		}

		// if a cab accepted the ride
		if (selectedCab != null) {
			// Taking lock as we are modifying cabStatus
			selectedCab = em.find(CabStatus.class, selectedCab.getCabID(), LockModeType.PESSIMISTIC_WRITE); 
			
			long fare = calcFare(sourceLoc, selectedCab.getCurrPos(), destinationLoc);
			Logger.log("requestRide: Calculated fare for ride id : " + rideID + " is : " + fare);

			// attempt wallet deduction for the consumer
			boolean ifDeductionSuccess = walletServiceRestConsumer.consumeDeductAmount(custID, fare);

			if (ifDeductionSuccess) {
				Logger.log("requestRide: Amount deduction from wallet success for user : " + custID);
				// If the deduction was a success, send request Cab.rideStarted to the accepting
				// cabId
				boolean ifRideStartedSuccess = cabServiceRestConsumer.consumeRideStarted(selectedCab.getCabID(),
						rideID);
				if (ifRideStartedSuccess) {
					ride.setCabStatus(selectedCab);
					ride.setRideState(RideStates.ONGOING);

					ride = rideRepo.saveAndFlush(ride);

					CabStatus cabStatus = ride.getCabStatus();
					cabStatus.setMinorState(CabMinorStates.GIVING_RIDE);
					cabStatus.setCurrPos(sourceLoc);

					cabStatusRepo.saveAndFlush(cabStatus);

					Logger.log("requestRide: Ride started, CabID: " + selectedCab.getCabID() + ", RideID: " + rideID);
					
					//Return a tuple of [RideID] [CabID] [Fare]
					
					return rideID + " " + cabStatus.getCabID() + " " + fare;
				} else {
					Logger.log("requestRide: Ride id : " + rideID + " was rejected by Cab Service for Cab ID : "
							+ selectedCab.getCabID());
				}
			} else {
				// if deduction failed send ride cancelled to cab service
				Logger.log("requestRide: Amount deduction from wallet failed for user : " + custID);
				boolean ifRideCancelled = cabServiceRestConsumer.consumeRideCanceled(selectedCab.getCabID(), rideID);
				if (ifRideCancelled) {
					Logger.log("requestRide: Ride cancelled, CabID: " + selectedCab.getCabID() + ", RideID: " + rideID);
					rideRepo.delete(ride);
				}
			}

		}
		
		rideRepo.delete(ride);
		Logger.logErr("requestRide: Not enough rides available (less than 3 rides)");
		return "-1";

	}

	public static long calcFare(long sourcePos, long curPos, long destPos) {
		long fare = (Math.abs(sourcePos - curPos) + Math.abs(sourcePos - destPos)) * 10;

		return fare;
	}

}

// Comparator for comparing two CabStatus objects according to distance
class CabStatusComparatorByCurPos implements Comparator<CabStatus> {
	long srcLoc;

	public CabStatusComparatorByCurPos(long srcLoc) {
		this.srcLoc = srcLoc;
	}

	public int compare(CabStatus a, CabStatus b) {
		long relativeLocOfA = Math.abs(a.getCurrPos() - this.srcLoc);
		long relativeLocOfB = Math.abs(b.getCurrPos() - this.srcLoc);

		if (relativeLocOfA == relativeLocOfB)
			return Long.signum(a.getCabID() - b.getCabID());

		return Long.signum(relativeLocOfA - relativeLocOfB);
	}

}
