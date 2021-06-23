package pods.cabs;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import pods.cabs.utils.CborSerializable;
import pods.cabs.utils.Logger;
import pods.cabs.values.CabStates;

public class Cab extends EventSourcedBehavior<Cab.Command, Cab.CabEvent, Cab.CabState> {

	String entityId;
	String entityName;

	public static final EntityTypeKey<Command> TypeKey = EntityTypeKey.create(Cab.Command.class, "CabEntity");

	public Cab(ActorContext<Command> context, PersistenceId persistenceId, String entityId) {
		super(persistenceId);
		this.entityId = entityId;
		this.entityName = "Cab-" + entityId;
	}

	public final class CabState implements CborSerializable {
		String majorState;
		String minorState;
		long rideID;
		long numRides;
		long numRequestsRecvd = 0;
		long curPos;
		boolean lastRideReqAccepted;
		long posAfterRideEnds;

		public CabState() {
			super();
			this.majorState = CabStates.MajorStates.SIGNED_OUT;
			this.minorState = CabStates.MinorStates.NONE;
			this.numRides = 0;
			this.curPos = -1;
		}
		
		public String toString() {
			return "majorState: "+majorState+", minorState: " + minorState + ", numRides: " +numRides
					+", numRequestsRecvd: "+numRequestsRecvd+", curPos: " + curPos + ", lastRideReqAccepted: "
					+ lastRideReqAccepted + ", posAfterRideEnds: "+posAfterRideEnds;
		}

	}

	public static class Command implements CborSerializable {
		int dummy;
	}

	public static class SignIn extends Command {
		public long initialPos;

		public SignIn(long initialPos) {
			super();
			this.initialPos = initialPos;
		}
		
		public SignIn() {}
	}

	public static class SignOut extends Command {
		public SignOut() {}
	}

	public static class RequestRide extends Command {
		public ActorRef<FulfillRide.Command> replyTo;
		public long srcLoc;
		public long destLoc;
		public long rideId;

		public RequestRide(ActorRef<FulfillRide.Command> replyTo, long srcLoc, long destLoc, long rideId) {
			super();
			this.replyTo = replyTo;
			this.srcLoc = srcLoc;
			this.destLoc = destLoc;
			this.rideId = rideId;
		}
		
		public RequestRide() {}
	}

	public static class RideEnded extends Command {
		long rideId;

		public RideEnded(long rideId) {
			super();
			this.rideId = rideId;
		}
		
		public RideEnded() {}
	}

	public static class NumRides extends Command {
		ActorRef<Cab.NumRidesReponse> replyTo;

		public NumRides(ActorRef<NumRidesReponse> replyTo) {
			super();
			this.replyTo = replyTo;
		}
		
		public NumRides() {}
	}

	public static class Reset extends Command {
		public ActorRef<Cab.NumRidesReponse> replyTo;

		public Reset(ActorRef<NumRidesReponse> replyTo) {
			super();
			this.replyTo = replyTo;
		}
		
		public Reset() {}
	}

	public static class NumRidesReponse extends Command {
		long numRides;

		public NumRidesReponse(long numRides) {
			super();
			this.numRides = numRides;
		}

		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			NumRidesReponse numRidesReponse = (NumRidesReponse) o;
			return numRidesReponse.numRides == this.numRides;
		}
		
		public NumRidesReponse() {}
	}

	public static class DebugCabState extends Cab.Command {
		ActorRef<Cab.DebugCabStateResponse> replyTo;

		public DebugCabState(ActorRef<Cab.DebugCabStateResponse> replyTo) {
			super();
			this.replyTo = replyTo;
		}
		
		public DebugCabState() {}
	}

	public static class DebugCabStateResponse extends Cab.Command {
		public String majorState;
		public String minorState;
		public long curPos;
		public long numRides;
		public long numRequestsRecvd;

		public DebugCabStateResponse(String majorState, String minorState, long curPos, long numRides,
				long numRequestsRecvd) {
			super();
			this.majorState = majorState;
			this.minorState = minorState;
			this.curPos = curPos;
			this.numRides = numRides;
			this.numRequestsRecvd = numRequestsRecvd;
		}
		
		public DebugCabStateResponse() {}
		
		public String toString() {
			return "majorState: "+majorState+", minorState: " + minorState + ", numRides: " +numRides
						+", numRequestsRecvd: "+numRequestsRecvd+", curPos: " + curPos;			
		}
	}

	// ----------------- Define Events Here ------------------

	// Base Event
	public static class CabEvent implements CborSerializable {
		int dummy;
		
		public CabEvent() {
			dummy=1;
		}
	}

	public static class SignInEvent extends CabEvent {
		public long initPos;

		public SignInEvent(long initPos) {
			super();
			this.initPos = initPos;
		}
		public SignInEvent() {}
	}

	public static class SignOutEvent extends CabEvent {
		public SignOutEvent() {}
	}

	public static class RequestRideEvent extends CabEvent {
		public long srcPos;
		public long destPos;
		public long rideId;

		public RequestRideEvent(long srcPos, long destPos, long rideId) {
			super();
			this.srcPos = srcPos;
			this.destPos = destPos;
			this.rideId = rideId;
		}
		public RequestRideEvent() {}
	}

	public static class RideEndedEvent extends CabEvent {
		public long rideId;

		public RideEndedEvent(long rideId) {
			super();
			this.rideId = rideId;
		}
		
		public RideEndedEvent() {}
	}

	public static class ResetEvent extends CabEvent {
		public ResetEvent() {}
	}

	// ---------------- Define command handlers here ------------
	private Effect<CabEvent, CabState> onSignIn(SignIn command) {
		return Effect().persist(new SignInEvent(command.initialPos)).thenRun(newState -> {
		});
	}

	private Effect<CabEvent, CabState> onSignOut(SignOut command) {
		return Effect().persist(new SignOutEvent()).thenRun(newState -> {
		});
	}
	
	private Effect<CabEvent, CabState> onRequestRide(RequestRide command) {
		return Effect().persist(new RequestRideEvent(command.srcLoc, command.destLoc, command.rideId))
				.thenRun(newState -> {
					if (newState.lastRideReqAccepted) {
						// if event handler determined that ride request is accepted
						command.replyTo.tell(new FulfillRide.RideAcceptedInternal(true));
					} else {
						command.replyTo.tell(new FulfillRide.RideAcceptedInternal(false));
					}
				});
	}

	private Effect<CabEvent, CabState> onRideEnded(RideEnded command) {
		return Effect().persist(new RideEndedEvent(command.rideId))
				.thenRun(newState -> {
				});
	}

	private Effect<CabEvent, CabState> onNumRides(NumRides command) {
		// Nothing to persist after NumRides Command
		return Effect().none().thenRun(newState -> {
			Logger.log(entityName + ": Received NumRides Command");
			command.replyTo.tell(new NumRidesReponse(newState.numRides));
		});
	}

	private Effect<CabEvent, CabState> onReset(Reset resetCommand) {
		return Effect().persist(new ResetEvent()).thenRun(newState -> {
			resetCommand.replyTo.tell(new NumRidesReponse(newState.numRides));
		});
	}
	
	public Effect<CabEvent, CabState> onDebugCabState(DebugCabState command) {
		return Effect().none().thenRun(newState -> {
			DebugCabStateResponse response = new DebugCabStateResponse(newState.majorState, newState.minorState, 
					newState.curPos, newState.numRides, newState.numRequestsRecvd);
			command.replyTo.tell(response);
		});
	}

	// ---------------- Define event handlers here ------------
	public CabState onSignInEvent(CabState state, SignInEvent event) {
		Logger.log(entityName + ": Received SignInEvent");
		if (state.majorState == CabStates.MajorStates.SIGNED_OUT) {
			state.majorState = CabStates.MajorStates.SIGNED_IN;
			state.minorState = CabStates.MinorStates.AVAILABLE;
		} else {
			Logger.logErr(entityName + ": Couldn't sign in as already signed in");
		}
		return state;
	}

	public CabState onSignOutEvent(CabState state, SignOutEvent event) {
		Logger.log(entityName + ": Received SignOutEvent");
		if (state.majorState == CabStates.MajorStates.SIGNED_IN) {
			if (state.minorState == CabStates.MinorStates.AVAILABLE) {
				state.majorState = CabStates.MajorStates.SIGNED_OUT;
				state.minorState = CabStates.MinorStates.NONE;
			} else {
				Logger.logErr(entityName + ": Couldn't sign out as currently giving ride");
			}
		} else {
			Logger.logErr(entityName + ": Couldn't sign out as already signed out");
		}
		return state;
	}

	public CabState onRequestRideEvent(CabState state, RequestRideEvent event) {
		Logger.log(entityName + ": Received RequestRideEvent");
		
		state.lastRideReqAccepted = false;
		if (state.majorState == CabStates.MajorStates.SIGNED_IN
				&& state.minorState == CabStates.MinorStates.AVAILABLE) {
			if (state.numRequestsRecvd % 2 == 0) {
				state.minorState = CabStates.MinorStates.GIVING_RIDE;
				state.lastRideReqAccepted = true;
				state.numRides++;
				state.posAfterRideEnds = event.destPos;
				state.rideID = event.rideId;
			} else {
				Logger.logErr(entityName + ": RideRequest rejected due to alternate request rejection");
			}

			state.numRequestsRecvd++;
		} else {
			Logger.logErr(entityName + ": RideRequest rejected as either cab not signed in or already giving ride");
		}
		
//		Logger.logErr(entityName + ": RequestRideEvent : new numRequestsRecvd : " + state.numRequestsRecvd + ", new state : " + state.toString());
		return state;
	}

	public CabState onRideEndedEvent(CabState state, RideEndedEvent event) {
		Logger.log(entityName + ": Received RideEndedEvent");

		if (state.majorState == CabStates.MajorStates.SIGNED_IN
				&& state.minorState == CabStates.MinorStates.GIVING_RIDE) {
			if (state.rideID == event.rideId) {
				state.minorState = CabStates.MinorStates.AVAILABLE;
				state.rideID = -1;
				state.curPos = state.posAfterRideEnds;
			} else {
				Logger.logErr(entityName + ": Couldn't end ride as rideId is invalid");
			}
		} else {
			Logger.logErr(entityName + ": Couldn't end ride as cab not giving any ride or signed out");
		}

		return state;
	}

	public CabState onResetEvent(CabState state, ResetEvent event) {
		Logger.log(entityName + ": Received ResetEvent");

		state.majorState = CabStates.MajorStates.SIGNED_OUT;
		state.minorState = CabStates.MinorStates.NONE;
		state.numRides = 0;
		state.numRequestsRecvd = 0;
		state.curPos = -1;

		return state;
	}
	

	@Override
	public CommandHandler<Command, CabEvent, CabState> commandHandler() {
		return newCommandHandlerBuilder().forAnyState()
				.onCommand(SignIn.class, this::onSignIn)
				.onCommand(SignOut.class, this::onSignOut)
				.onCommand(RequestRide.class, this::onRequestRide)
				.onCommand(RideEnded.class, this::onRideEnded)
				.onCommand(NumRides.class, this::onNumRides)
				.onCommand(Reset.class, this::onReset)
				.onCommand(DebugCabState.class, this::onDebugCabState)
				.onCommand(Command.class, notUsed -> {
					Logger.logErr("Shouldn't have received this generic command for Cab Entity");
					return Effect().none();
				}).build();
	}

	@Override
	public CabState emptyState() {
		return new CabState();
	}

	@Override
	public EventHandler<CabState, CabEvent> eventHandler() {
		return newEventHandlerBuilder().forAnyState()
				.onEvent(SignInEvent.class, this::onSignInEvent)
				.onEvent(SignOutEvent.class, this::onSignOutEvent)
				.onEvent(RequestRideEvent.class, this::onRequestRideEvent)
				.onEvent(RideEndedEvent.class, this::onRideEndedEvent)
				.onEvent(ResetEvent.class, this::onResetEvent)
				.build();
	}

	public static Behavior<Command> create(String entityId, PersistenceId persistenceId) {
		Logger.logErr("In 'create' of a new cab entity with id: " + entityId);

		return Behaviors.setup(context -> new Cab(context, persistenceId, entityId));
	}
}