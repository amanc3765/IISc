package pods.cabs;

import java.io.IOException;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.InitFileReader.InitReadWrapper;
import pods.cabs.utils.Logger;

public class Main extends AbstractBehavior<Main.Command> {
	
	public Main(ActorContext<Main.Command> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public static final class Command {}
	
	public static final class StartedCommand {
		boolean status;
		
		public StartedCommand(boolean status) {
			this.status = status;
		}		
		
		public boolean equals(Object o) {
	      if (this == o) return true;
	      if (o == null || getClass() != o.getClass()) return false;
	      
	      StartedCommand startedCommand = (StartedCommand) o;	      
	      return startedCommand.status == this.status;
	    }
	}
	
    public static Behavior<Main.Command> create(ActorRef<Main.StartedCommand> testProbe) {
    	Logger.log("Main actor being created");
    	
    	return Behaviors.setup(context -> {
    	
    	try {
    		InitReadWrapper wrapperObj = new InitReadWrapper(); 
    		
    		Globals.initReadWrapperObj = wrapperObj;
    		
			InitFileReader.readInitFile(wrapperObj);
			
			long initWalletBalance = wrapperObj.walletBalance;
			
			// Create multiple Cab Actors
			for (String cabID : wrapperObj.cabIDList) {
				Logger.log("Trying to spawn the actor cab-"+cabID);
				ActorRef<Cab.Command> cabActorRef = context.spawn(Cab.create(cabID), "cab-"+cabID);
				Globals.cabs.put(cabID, cabActorRef);
			}
			
			// Create multiple Wallet Actors
			for (String custID : wrapperObj.custIDList) {
				Logger.log("Trying to spawn the actor wallet-"+custID+" with wallet balance: "+ initWalletBalance);
				ActorRef<Wallet.Command> walletActorRef = context.spawn(Wallet.create(custID, initWalletBalance), "wallet-"+custID);
				Globals.wallets.put(custID, walletActorRef);
			}		
			
			// Create multiple RideService Actors
			Globals.rideService = new ActorRef[10];
			for (int i=0; i<Globals.N_RIDE_SERVICE_INSTANCES; i++) {
				Logger.log("Trying to spawn the Ride Service Actor instance " + i);
				ActorRef<RideService.Command> rideServiceActorRef = 
						context.spawn(RideService.create(i), "rideservice-"+i);
				Globals.rideService[i] = (rideServiceActorRef);
			}	
			
			testProbe.tell(new Main.StartedCommand(true));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return Behaviors.empty();
    	});
    }

	@Override
	public Receive<Main.Command> createReceive() {
		Logger.log("---------------Inside createReceive of Main--------------------");
		return newReceiveBuilder()
		        .onMessage(Main.Command.class, notUsed -> {
		        	Logger.logErr("Shouldn't have received this generic command for main actor");
		        	return this;
		        	})
		        .build();
	}
}
