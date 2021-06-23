package pods.cabs;

import java.util.ArrayList;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import pods.cabs.utils.CborSerializable;
import pods.cabs.utils.Logger;

public class FulfillRide extends AbstractBehavior<FulfillRide.Command> {

	long rideId;
	String custId;
	long sourceLoc;
	long destinationLoc;
	ActorRef<RideService.RideResponse> replyTo; // for testkit actor

	int nTriesDone;
	int curCabIndex;

	long fare;

	String actorName;

	public FulfillRide(ActorContext<Command> context, long rideId, String custId, long sourceLoc, long destinationLoc,
			ActorRef<RideService.RideResponse> replyTo) {
		super(context);
		this.actorName = getContext().getSelf().path().name();
		this.nTriesDone = 0;
		this.rideId = rideId;
		this.custId = custId;
		this.sourceLoc = sourceLoc;
		this.destinationLoc = destinationLoc;
		this.replyTo = replyTo;
	}

	public static class Command implements CborSerializable {
		public Command() {}
	}

	public static class RideAcceptedInternal extends Command {
		boolean accepted;

		public RideAcceptedInternal(boolean accepted) {
			super();
			this.accepted = accepted;
		}
		
		public RideAcceptedInternal() {}
		
	}

	// Define message handlers here
	private Behavior<Command> onRideAcceptedInternal(FulfillRide.RideAcceptedInternal rideAcceptedInternalCommand) {
		Logger.log(actorName + " : Received RideAcceptedInternal command");
		ArrayList<String> cabList = Globals.initReadWrapperObj.cabIDList;
		String respondingCabId = cabList.get(curCabIndex);

		if (rideAcceptedInternalCommand.accepted) {
			Logger.log(actorName + " : Cab Id " + respondingCabId + " accepted the ride");

			//tell actor testkit
			replyTo.tell(new RideService.RideResponse(this.rideId, respondingCabId));

		} else {
			Logger.logErr(actorName + " : Cab Id " + respondingCabId + " rejected the ride");
			if (curCabIndex < (cabList.size() - 1)) {
				curCabIndex++;
				String curCabId = cabList.get(curCabIndex);
				final ClusterSharding shardingProxy = ClusterSharding.get(getContext().getSystem());
				
				Logger.log(actorName + " : Probing candidate cab no : " + (curCabIndex + 1) + ", cabId: " + curCabId);

				shardingProxy.entityRefFor(Cab.TypeKey, curCabId).tell(new Cab.RequestRide(getContext().getSelf(), this.sourceLoc, this.destinationLoc, this.rideId));
				this.nTriesDone++;

				return this;
			} else {
				Logger.logErr(actorName + " : Couldn't find any ride after probing all cabs");

				replyTo.tell(new RideService.RideResponse(-1, null));
			}
		}

		return Behaviors.stopped();
	}

	private Behavior<Command> onCommand(FulfillRide.Command command) {
		Logger.log(actorName + " : Received FulfillRide.Command");

		// Test the candidate cabs
		if (!probeCabsToRequestRide()) {
			// Since the first probe itself failed, don't do anything more
			replyTo.tell(new RideService.RideResponse(-1, null));
			return Behaviors.stopped();
		}

		return this;
	}

	boolean probeCabsToRequestRide() {

		// Copy all the available cabs into an array list
		ArrayList<String> cabList = Globals.initReadWrapperObj.cabIDList;

		if (cabList.size() > 0) {
			// Probe the first cab
			curCabIndex = 0;
			String curCabId = cabList.get(curCabIndex);
			final ClusterSharding shardingProxy = ClusterSharding.get(getContext().getSystem());
			
			Logger.log(actorName + " : Probing candidate cab no : " + (curCabIndex + 1) + ", cabId: " + curCabId);
			shardingProxy.entityRefFor(Cab.TypeKey, curCabId).tell(new Cab.RequestRide(getContext().getSelf(), this.sourceLoc, this.destinationLoc, this.rideId));
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
				.onMessage(RideAcceptedInternal.class, this::onRideAcceptedInternal)
				.onMessage(Command.class, this::onCommand)
				.build();
	}

	public static Behavior<Command> create(long rideId, String custId, long sourceLoc, long destinationLoc,
			ActorRef<RideService.RideResponse> replyTo) {
		Logger.log("Inside 'create' of a new FulfillRide actor");
		return Behaviors.setup(context -> {
			return new FulfillRide(context, rideId, custId, sourceLoc, destinationLoc, replyTo);
		});
	}

	public static long calcFare(long sourcePos, long curPos, long destPos) {
		long fare = (Math.abs(sourcePos - curPos) + Math.abs(sourcePos - destPos)) * 10;

		return fare;
	}
}
