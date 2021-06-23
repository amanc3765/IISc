package pods.cabs;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import pods.cabs.utils.InitFileReader.InitReadWrapper;

public class Globals {
	public static InitReadWrapper initReadWrapperObj;

	public static final AtomicLong rideIdSequence;

	static {
		rideIdSequence = new AtomicLong(0);
	}
	
	public static String getRandRideService() {
		Random rand = new Random();
		int randRideServiceId = rand.nextInt(12)+1;
		
		return "rideService"+randRideServiceId;
	}
	
}
