package pods.cabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.Wallet.ResponseBalance;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.Logger;
import pods.cabs.values.CabStates;

public class FulfillRide extends AbstractBehavior<FulfillRide.Command> {
	
	long rideId;
	String custId;
	long sourceLoc;
	long destinationLoc;
	HashMap<String,CabStatus> cabsMap;
	ActorRef<RideService.RideResponse> replyTo;
	
	int nTriesDone;
	int curCabIndex;
	int acceptedCabIndex;
	List<CabStatus> candidateCabs;
	
	long fare;
	
	String actorName;
	
	public FulfillRide(ActorContext<Command> context, long rideId, String custId, long sourceLoc, 
			long destinationLoc, HashMap<String,CabStatus> cabsMap, ActorRef<RideService.RideResponse> replyTo) {
		super(context);
		this.actorName = getContext().getSelf().path().name();
		this.nTriesDone = 0;
		this.rideId = rideId;
		this.custId = custId;
		this.sourceLoc = sourceLoc;
		this.destinationLoc = destinationLoc;
		this.cabsMap = cabsMap;
		this.replyTo = replyTo;
	}
	

	private static ArrayList<CabStatus> sortCabList(ArrayList<CabStatus> cablist,long srcPos){   
    
		class CabStatusComparator implements Comparator<CabStatus> {
		    @Override
		    public int compare(CabStatus cab1, CabStatus cab2) {
		    	long cab1RelativeDist = Math.abs(cab1.curPos-srcPos);
		    	long cab2RelativeDist = Math.abs(cab2.curPos-srcPos);
		        
		    	if(cab1RelativeDist < cab2RelativeDist) {
		    		return -1;
		    	}else if(cab1RelativeDist == cab2RelativeDist) {
		    		return Long.parseLong(cab1.cabId) < Long.parseLong(cab2.cabId) ? -1 : 1;
		    	}
		    	
		    	return 1;		    	
		    }
		}		
	    
		Collections.sort(cablist, new CabStatusComparator());
		
		return cablist;
	}  	

	public static class Command  {
	}

	public static class RideEnded extends Command {
	}
	
	public static class RideAcceptedInternal extends Command {
		boolean accepted;

		public RideAcceptedInternal(boolean accepted) {
			super();
			this.accepted = accepted;
		}
	}
	
	public static class WrappedResponseBalance extends Command {
		final Wallet.ResponseBalance responseBalanceCommand;

		public WrappedResponseBalance(ResponseBalance responseBalanceCommand) {
			super();
			this.responseBalanceCommand = responseBalanceCommand;
		}
	}
	
	// Define message handlers here
	private Behavior<Command> onRideAcceptedInternal(FulfillRide.RideAcceptedInternal rideAcceptedInternalCommand) {
		Logger.log(actorName + " : Received RideAcceptedInternal command");
		CabStatus cabStatus = candidateCabs.get(curCabIndex);
		if(rideAcceptedInternalCommand.accepted) {
			Logger.log(actorName + " : Cab Id " + cabStatus.cabId +" accepted the ride");
			acceptedCabIndex = curCabIndex;
			CabStatus acceptedCabStatus = cabStatus;
			
			// Initiate wallet operations
			this.fare = calcFare(sourceLoc, cabsMap.get(acceptedCabStatus.cabId).curPos, destinationLoc);
			ActorRef<Wallet.ResponseBalance> responseBalanceAdapter = getContext().messageAdapter(Wallet.ResponseBalance.class, WrappedResponseBalance::new);
			Globals.wallets.get(this.custId).tell(new Wallet.DeductBalance(this.fare, responseBalanceAdapter));
			
			//very important
			return this;
		}
		else {
			Logger.logErr(actorName + " : Cab Id " + cabStatus.cabId +" rejected the ride");
			if(curCabIndex < (candidateCabs.size()-1) && nTriesDone < 3) {
				curCabIndex++;
				Logger.log(actorName + " : Probing candidate cab no : "+ (curCabIndex+1));
				
				Globals.cabs.get(candidateCabs.get(curCabIndex).cabId).tell(new Cab.RequestRide(getContext().getSelf()));
				this.nTriesDone++;
				
				return this;
			}
			else {
				Logger.logErr(actorName + " : Couldn't find ride within 3 attempts");
				
				replyTo.tell(new RideService.RideResponse(-1, null, -1, getContext().getSelf()));
			}
		}
		
		return Behaviors.empty();
	}
	
	private Behavior<Command> onWrappedResponseBalance(FulfillRide.WrappedResponseBalance wrappedResponseBalanceCommand) {
		Logger.log(actorName + " : Received WrappedResponseBalance command");
		
		CabStatus acceptedCabStatus = candidateCabs.get(acceptedCabIndex);
		
		//check if wallet deduction was successful
		if(wrappedResponseBalanceCommand.responseBalanceCommand.balance >= 0 ) {
			Logger.log(actorName + " : Balance Deducted, starting ride");
			
			//tell cab that it can go from committed to giving ride
			Globals.cabs.get(acceptedCabStatus.cabId).tell(new Cab.RideStarted(this.rideId, getContext().getSelf()));
			
			//tell ride service instance that ride was successful
			Random rand = new Random();
			int randRideServiceId = rand.nextInt(Globals.N_RIDE_SERVICE_INSTANCES);
			Globals.rideService[randRideServiceId].tell(new RideService.RideResponse(this.rideId, acceptedCabStatus.cabId, this.fare, getContext().getSelf()));
			
			//tell actor testkit
			replyTo.tell(new RideService.RideResponse(this.rideId, acceptedCabStatus.cabId, this.fare, getContext().getSelf()));
			
			return this;
		}
		else {
			Logger.log(actorName + " : Couldn't start ride due to insufficient balance");
			
			//tell cab that it can go from committed to available
			Globals.cabs.get(acceptedCabStatus.cabId).tell(new Cab.RideCanceled());
			
			//tell ride service instance that ride was unsuccessful
			Random rand = new Random();
			int randRideServiceId = rand.nextInt(Globals.N_RIDE_SERVICE_INSTANCES);
			Globals.rideService[randRideServiceId].tell(new RideService.RideResponse(-1, null, -1, null));		
			
			replyTo.tell(new RideService.RideResponse(-1, null, -1, null));
		}
		
		return Behaviors.empty();
	}
	
	
	private Behavior<Command> onCommand(FulfillRide.Command command) {
		Logger.log(actorName + " : Received FulfillRide.Command");
		
		// Test the candidate cabs 
		if( !probeCabsToRequestRide(sourceLoc, cabsMap) ) {
			// Since the first probe itself failed, don't do anything more
			replyTo.tell(new RideService.RideResponse(-1, null, -1, null));
			return Behaviors.empty();
		}
		
		return this;
	}
	
	private Behavior<Command> onRideEnded(FulfillRide.RideEnded rideEndedCommand) {
		Logger.log(actorName + " : Received FulfillRide.RideEnded");
		
		CabStatus acceptedCabStatus = candidateCabs.get(acceptedCabIndex);
		
		// Tell RideService that the ride has ended
		Random rand = new Random();
		int randRideServiceId = rand.nextInt(Globals.N_RIDE_SERVICE_INSTANCES);
		Globals.rideService[randRideServiceId].tell(new RideService.RideEnded(acceptedCabStatus.cabId, this.destinationLoc));
		
		// do harakiri
		return Behaviors.empty();
	}
	
	
	boolean probeCabsToRequestRide(long sourceLoc, HashMap<String,CabStatus> cabsMap) {
		
		// Copy all the available cabs into an array list
		ArrayList<CabStatus> cabList = new ArrayList<>();
		for(CabStatus cabStatus : cabsMap.values()) {
			if(cabStatus.majorState == CabStates.MajorStates.SIGNED_IN && cabStatus.minorState == CabStates.MinorStates.AVAILABLE) {
				cabList.add(cabStatus);
			}
		}
		cabList = sortCabList(cabList, sourceLoc);
		
		this.candidateCabs = cabList.subList(0, Math.min(cabList.size(), 3));
		
		if(this.candidateCabs.size() > 0) {
			// Probe the first cab
			curCabIndex=0;
			Logger.log(actorName + " : Probing candidate cab no : "+ (curCabIndex+1));
			Globals.cabs.get(candidateCabs.get(curCabIndex).cabId).tell(new Cab.RequestRide(getContext().getSelf()));
			this.nTriesDone++;
			return true;
		}
		
		// Tell that the first probing itself failed
		Logger.logErr(actorName + " : Couldn't probe even 1 cab for this request");
		return false;
	}
	
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(WrappedResponseBalance.class, this :: onWrappedResponseBalance)
				.onMessage(RideAcceptedInternal.class, this :: onRideAcceptedInternal)
				.onMessage(RideEnded.class, this :: onRideEnded)
				.onMessage(Command.class, this :: onCommand)
				.build();
	}

	public static Behavior<Command> create(long rideId, String custId, long sourceLoc, long destinationLoc, HashMap<String,CabStatus> cabsMap, ActorRef<RideService.RideResponse> replyTo) {
		Logger.log("Inside 'create' of a new FulfillRideGenericCommand actor");
		return Behaviors.setup(context -> {	
			return new FulfillRide(context, rideId, custId, sourceLoc, destinationLoc, cabsMap, replyTo);
		});
	}
	
	public static long calcFare(long sourcePos, long curPos, long destPos) {
		long fare = (Math.abs(sourcePos - curPos) + Math.abs(sourcePos - destPos)) * 10;

		return fare;
	}
}
