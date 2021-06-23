package pods.cabs;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import pods.cabs.utils.Logger;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import pods.cabs.utils.CborSerializable;

public class RideService extends AbstractBehavior<RideService.Command> {
	String entityId;
	
	public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(RideService.Command.class,
			"RideServiceEntity");
	
	public RideService(ActorContext<RideService.Command> context, String rideServiceActorId) {
		super(context);
		this.entityId = rideServiceActorId;
	}
	
	public static class Command implements CborSerializable{
		int dummy=0;
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
		
		public RequestRide() {}
	}
	
	
	// ------------- Define Command Handlers Here ---------------------------
	
	private Behavior<RideService.Command> onRequestRide(RideService.RequestRide requestRideCommand) {
		Logger.log(entityId + ": Received RideService.RequestRide for (custId,srcLoc,destLoc) : ("
				+ requestRideCommand.custId + ", " + requestRideCommand.sourceLoc + ", "
				+ requestRideCommand.destinationLoc + ")");
		long rideId = Globals.rideIdSequence.incrementAndGet();

		// Perform sanity checks
		if (requestRideCommand.sourceLoc >= 0 && requestRideCommand.destinationLoc >= 0) {
			String fulfilRideActorName = "fRide-" + this.entityId + "-" + rideId;
			ActorRef<FulfillRide.Command> fRideActorRef = getContext().spawn(
					FulfillRide.create(rideId, requestRideCommand.custId, requestRideCommand.sourceLoc,
							requestRideCommand.destinationLoc, requestRideCommand.replyTo),
					fulfilRideActorName);
			Logger.log(entityId + " : Spawned a FulfillRide actor with id: " + fulfilRideActorName);
			
			fRideActorRef.tell(new FulfillRide.Command());
		} else {
			Logger.logErr(entityId + " : Invalid ride request parameters");
		}

		return this;
	}
	
	
	public static class RideResponse extends RideService.Command {
		public long rideId;
		public String cabId;
//		public ActorRef<FulfillRide.Command> fRide;

		public RideResponse(long rideId, String cabId) {
			super();
			this.rideId = rideId;
			this.cabId = cabId;
//			this.fRide = fRide;
		}
		
		public RideResponse() {}
	}
	
	public Receive<RideService.Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(RideService.RequestRide.class, this::onRequestRide)
				.onMessage(RideService.Command.class, notUsed -> {
					Logger.logErr("Shouldn't have received this generic command for rideservice");
					return this;
				}).build();
	}

	public static Behavior<RideService.Command> create(String rideServiceActorId) {
		Logger.logErr("In 'create' of a new RideService actor, id : " + rideServiceActorId);
		return Behaviors.setup(context -> {
			return new RideService(context, rideServiceActorId);
		});
	}
}
