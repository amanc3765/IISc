package pods.cabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import akka.actor.typed.ActorRef;
import pods.cabs.utils.InitFileReader.InitReadWrapper;


public class Globals {
	
	public static final int N_RIDE_SERVICE_INSTANCES = 10;
	
	public static final HashMap<String,ActorRef<Cab.Command>> cabs;          //Cab Ids are keys
	public static final HashMap<String,ActorRef<Wallet.Command>> wallets; //Cust Ids are keys
	public static ActorRef<RideService.Command>[] rideService = null;
	
	public static InitReadWrapper initReadWrapperObj;
	
	public static final AtomicLong rideIdSequence;
	
	static {
		cabs = new HashMap<>();
		wallets = new HashMap<>();
		rideIdSequence = new AtomicLong(0);
	}
}
