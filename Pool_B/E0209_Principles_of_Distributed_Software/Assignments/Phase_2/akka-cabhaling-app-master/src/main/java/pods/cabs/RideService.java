package pods.cabs;

import java.util.HashMap;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.models.CabStatus;
import pods.cabs.utils.Logger;
import pods.cabs.values.CabStates;

public class RideService extends AbstractBehavior<RideService.Command> {

	long rideServiceActorId;

	String actorName;

	public HashMap<String, CabStatus> cabsMap;

	public RideService(ActorContext<RideService.Command> context, long rideServiceActorId) {
		super(context);
		this.rideServiceActorId = rideServiceActorId;
		this.actorName = getContext().getSelf().path().name();

		cabsMap = new HashMap<>();

		// initialize all cabs in signed out state
		for (String cabId : Globals.initReadWrapperObj.cabIDList) {
			cabsMap.put(cabId, new CabStatus(cabId));
		}
	}

	public static class Command {
	}

	public static class CabSignsIn extends RideService.Command {
		String cabId;
		long initialPos;
		long timeCounter;

		public CabSignsIn(String cabId, long initialPos, long timeCounter) {
			super();
			this.cabId = cabId;
			this.initialPos = initialPos;
			this.timeCounter = timeCounter;
		}
	}

	public static class CabSignsInInternal extends RideService.Command {
		String cabId;
		long initialPos;
		long timeCounter;

		public CabSignsInInternal(String cabId, long initialPos, long timeCounter) {
			super();
			this.cabId = cabId;
			this.initialPos = initialPos;
			this.timeCounter = timeCounter;
		}
	}

	public static class CabSignsOut extends RideService.Command {
		String cabId;
		long timeCounter;
		public CabSignsOut(String cabId, long timeCounter) {
			super();
			this.cabId = cabId;
			this.timeCounter = timeCounter;
		}
	}

	public static class CabSignsOutInternal extends RideService.Command {
		String cabId;
		long timeCounter;
		public CabSignsOutInternal(String cabId, long timeCounter) {
			super();
			this.cabId = cabId;
			this.timeCounter = timeCounter;
		}
	}

	public static class RequestRide extends RideService.Command {
		String custId;
		long sourceLoc;
		long destinationLoc;
		ActorRef<RideService.RideResponse> replyTo;

		public RequestRide(String custId, long sourceLoc, long destinationLoc, ActorRef<RideResponse> replyTo) {
			super();
			this.custId = custId;
			this.sourceLoc = sourceLoc;
			this.destinationLoc = destinationLoc;
			this.replyTo = replyTo;
		}
	}

	public static class RideResponse extends RideService.Command {
		public long rideId;
		public String cabId;
		public long fare;
		public ActorRef<FulfillRide.Command> fRide;

		public RideResponse(long rideId, String cabId, long fare, ActorRef<FulfillRide.Command> fRide) {
			super();
			this.rideId = rideId;
			this.cabId = cabId;
			this.fare = fare;
			this.fRide = fRide;
		}
	}

	public static class RideResponseSuccessInternal extends RideService.Command {
		String cabId;

		public RideResponseSuccessInternal(String cabId) {
			super();
			this.cabId = cabId;
		}
	}

	public static class RideEnded extends RideService.Command {
		String cabId;
		long destinationPos;

		public RideEnded(String cabId, long destinationPos) {
			super();
			this.cabId = cabId;
			this.destinationPos = destinationPos;
		}
	}

	public static class RideEndedInternal extends RideService.Command {
		String cabId;
		long destinationPos;

		public RideEndedInternal(String cabId, long destinationPos) {
			super();
			this.cabId = cabId;
			this.destinationPos = destinationPos;
		}
	}
	
	public static class DebugCabState extends RideService.Command {
		String cabId;
		ActorRef<RideService.DebugCabStateResponse> replyTo;

		public DebugCabState(String cabId, ActorRef<RideService.DebugCabStateResponse> replyTo) {
			super();
			this.cabId = cabId;
			this.replyTo = replyTo;
		}
	}
	
	public static class DebugCabStateResponse extends RideService.Command {
		public String cabId;
		public String majorState;
		public String minorState;
		
		public DebugCabStateResponse(String cabId, String majorState, String minorState) {
			super();
			this.cabId = cabId;
			this.majorState = majorState;
			this.minorState = minorState;
		}
	}

	// Define message handlers here
	
	private Behavior<RideService.Command> onDebugCabState(RideService.DebugCabState debugCabStateCommand) {
		CabStatus cabStatus = this.cabsMap.get(debugCabStateCommand.cabId);
	
		debugCabStateCommand.replyTo.tell(new RideService.DebugCabStateResponse(cabStatus.cabId, cabStatus.majorState, cabStatus.minorState));
		
		return this;
	}

	private Behavior<RideService.Command> onCabSignsIn(RideService.CabSignsIn cabSignsInCommand) {
		Logger.log(actorName + " : Received RideService.CabSignsIn for cabId : " + cabSignsInCommand.cabId
				+ " on RideService instance " + this.rideServiceActorId + " with timeCounter : "+cabSignsInCommand.timeCounter);

		// Broadcast the message to all including own
		for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
			Globals.rideService[i]
					.tell(new CabSignsInInternal(cabSignsInCommand.cabId, cabSignsInCommand.initialPos, cabSignsInCommand.timeCounter));
		}

		return this;
	}

	private Behavior<RideService.Command> onCabSignsInInternal(
			RideService.CabSignsInInternal cabSignsInInternalCommand) {
//		Logger.log(actorName + " : Received RideService.CabSignsInInternal for cabId : "
//				+ cabSignsInInternalCommand.cabId + " on RideService instance " + this.rideServiceActorId);

		CabStatus cabStatus = this.cabsMap.get(cabSignsInInternalCommand.cabId);
		
		if(cabStatus.timeCounter < cabSignsInInternalCommand.timeCounter) {
			cabStatus.majorState = CabStates.MajorStates.SIGNED_IN;
			cabStatus.minorState = CabStates.MinorStates.AVAILABLE;
			cabStatus.curPos = cabSignsInInternalCommand.initialPos;
			cabStatus.timeCounter = cabSignsInInternalCommand.timeCounter;
		}

//		if (cabStatus != null && cabStatus.majorState == CabStates.MajorStates.SIGNED_OUT) {
//			Logger.log("Successfully signed in cab id : " + cabSignsInInternalCommand.cabId
//					+ " on Ride Service instance " + this.rideServiceActorId);
//
//			cabStatus.majorState = CabStates.MajorStates.SIGNED_IN;
//			cabStatus.minorState = CabStates.MinorStates.AVAILABLE;
//			cabStatus.curPos = cabSignsInInternalCommand.initialPos;
//		} else {
//			Logger.logErr(actorName + " : Couldn't sign in cab id : " + cabSignsInInternalCommand.cabId
//					+ " on Ride Service instance " + this.rideServiceActorId + " ::: cab state : " + cabStatus.majorState + ", " + cabStatus.minorState);
//		}
		return this;
	}

	private Behavior<RideService.Command> onCabSignsOut(RideService.CabSignsOut cabSignsOutCommand) {
		Logger.log("Received RideService.CabSignsOut for cabId : " + cabSignsOutCommand.cabId
				+ " on RideService instance " + this.rideServiceActorId + " with timeCounter : "+cabSignsOutCommand.timeCounter);

		// Broadcast the message to all including own
		for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
			Globals.rideService[i].tell(new CabSignsOutInternal(cabSignsOutCommand.cabId, cabSignsOutCommand.timeCounter));
		}
		return this;
	}

	private Behavior<RideService.Command> onCabSignsOutInternal(
			RideService.CabSignsOutInternal cabSignsOutInternalCommand) {
//		Logger.log("Received RideService.CabSignsOutInternal for cabId : " + cabSignsOutInternalCommand.cabId
//				+ " on RideService instance " + this.rideServiceActorId);

		CabStatus cabStatus = this.cabsMap.get(cabSignsOutInternalCommand.cabId);
		
		if(cabStatus.timeCounter < cabSignsOutInternalCommand.timeCounter) {
			cabStatus.majorState = CabStates.MajorStates.SIGNED_OUT;
			cabStatus.minorState = CabStates.MinorStates.NONE;
			cabStatus.curPos = -1;
			cabStatus.timeCounter = cabSignsOutInternalCommand.timeCounter;
		}

//		if (cabStatus != null && cabStatus.majorState == CabStates.MajorStates.SIGNED_IN
//				&& cabStatus.minorState == CabStates.MinorStates.AVAILABLE) {
//			Logger.log("Successfully signed out cab id : " + cabSignsOutInternalCommand.cabId
//					+ " on Ride Service instance " + this.rideServiceActorId);
//
//			cabStatus.majorState = CabStates.MajorStates.SIGNED_OUT;
//			cabStatus.minorState = CabStates.MinorStates.NONE;
//			cabStatus.curPos = -1;
//		} else {
//			Logger.logErr("Couldn't sign out cab id : " + cabSignsOutInternalCommand.cabId
//					+ " on Ride Service instance " + this.rideServiceActorId + " ::: cab state : " + cabStatus.majorState + ", " + cabStatus.minorState);
//		}
		return this;
	}

	private Behavior<RideService.Command> onRequestRide(RideService.RequestRide requestRideCommand) {
		Logger.log(actorName + " : Received RideService.RequestRide for (custId,srcLoc,destLoc) : ("
				+ requestRideCommand.custId + ", " + requestRideCommand.sourceLoc + ", "
				+ requestRideCommand.destinationLoc + ")");
		long rideId = Globals.rideIdSequence.incrementAndGet();

		// Perform sanity checks
		if (requestRideCommand.sourceLoc >= 0 && requestRideCommand.destinationLoc >= 0
				&& Globals.wallets.containsKey(requestRideCommand.custId)) {
			ActorRef<FulfillRide.Command> fRideActorRef = getContext().spawn(
					FulfillRide.create(rideId, requestRideCommand.custId, requestRideCommand.sourceLoc,
							requestRideCommand.destinationLoc, this.cabsMap, requestRideCommand.replyTo),
					"fRide-" + this.rideServiceActorId + "-" + rideId);
			Logger.log(actorName + " : Spawning a FulfillRide actor");
			
			fRideActorRef.tell(new FulfillRide.Command());
		} else {
			Logger.logErr(actorName + " : Invalid ride request");
		}

		return this;
	}

	private Behavior<RideService.Command> onRideResponse(RideService.RideResponse rideResponseCommand) {
		Logger.log("Received RideService.RideResponse for (rideId,cabId,fare) : (" + rideResponseCommand.rideId + ", "
				+ rideResponseCommand.cabId + ", " + rideResponseCommand.fare);

		if (rideResponseCommand.rideId > 0) {
			Logger.log(actorName + " : FulfillRide successfully gave ride");
			// Broadcast the message to all including own
			for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
				Globals.rideService[i].tell(new RideResponseSuccessInternal(rideResponseCommand.cabId));
			}
		} else {
			Logger.logErr(actorName + " : FulfillRide couldn't give ride");
			// do nothing
		}

		return this;
	}

	private Behavior<RideService.Command> onRideResponseSuccessInternal(RideService.RideResponseSuccessInternal rideResponseSuccessInternalCommand) {
//		Logger.log(actorName + " : Received RideService.RideResponseSuccessInternal with cabId : "
//				+ rideResponseSuccessInternalCommand.cabId);

		// Understand that the cab has started giving ride so, converge your state
		// accordingly
		this.cabsMap.get(rideResponseSuccessInternalCommand.cabId).minorState = CabStates.MinorStates.GIVING_RIDE;

		return this;
	}

	private Behavior<RideService.Command> onRideEnded(RideService.RideEnded rideEndedCommand) {
		Logger.log(actorName + " : Received RideService.RideEnded with (cabId,destPos) : " + rideEndedCommand.cabId +", " + rideEndedCommand.destinationPos);

		// Broadcast the message to all including own
		for (int i = 0; i < Globals.N_RIDE_SERVICE_INSTANCES; i++) {
			Globals.rideService[i].tell(new RideEndedInternal(rideEndedCommand.cabId, rideEndedCommand.destinationPos));
		}

		return this;
	}

	private Behavior<RideService.Command> onRideEndedInternal(RideService.RideEndedInternal rideEndedInternalCommand) {
//		Logger.log(actorName + " : Received RideService.RideEndedInternal with cabId : " + rideEndedInternalCommand.cabId);
		
		this.cabsMap.get(rideEndedInternalCommand.cabId).minorState = CabStates.MinorStates.AVAILABLE;
		this.cabsMap.get(rideEndedInternalCommand.cabId).curPos = rideEndedInternalCommand.destinationPos;
		
		return this;
	}

	@Override
	public Receive<RideService.Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(CabSignsIn.class, this::onCabSignsIn)
				.onMessage(CabSignsInInternal.class, this::onCabSignsInInternal)
				.onMessage(CabSignsOut.class, this::onCabSignsOut)
				.onMessage(CabSignsOutInternal.class, this::onCabSignsOutInternal)
				.onMessage(RequestRide.class, this::onRequestRide)
				.onMessage(RideResponse.class, this::onRideResponse)
				.onMessage(RideResponseSuccessInternal.class, this::onRideResponseSuccessInternal)
				.onMessage(RideEnded.class, this::onRideEnded)
				.onMessage(RideEndedInternal.class, this::onRideEndedInternal)
				.onMessage(DebugCabState.class, this::onDebugCabState)
				.onMessage(RideService.Command.class, notUsed -> {
					Logger.logErr("Shouldn't have received this generic command for rideservice");
					return this;
				}).build();
	}

	public static Behavior<RideService.Command> create(long rideServiceActorId) {
//		Logger.log("In 'create' of a new RideService actor, id : " + rideServiceActorId);
		return Behaviors.setup(context -> {
			return new RideService(context, rideServiceActorId);
		});
	}
}
