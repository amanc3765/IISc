package pods.cabs.test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import pods.cabs.Cab;
import pods.cabs.Cab.NumRidesReponse;
import pods.cabs.RideService;
import pods.cabs.utils.Logger;
import pods.cabs.values.CabStates;

import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

//#definition
public class AkkaCabHailingTestPersistence {
	@ClassRule
	public static final TestKitJunitResource testKit;
	
	static { 
		ActorSystem<Void> actorSystem = ActorSystem.create(Behaviors.empty(), "ClusterSystem", TestInterface.config);
		testKit = new TestKitJunitResource(actorSystem);
	}
		
	public TestInterface testInterface;

	@Test
	public void testMainPersistence() {
		this.testInterface = new TestInterface(testKit);
		Logger.log("Main Started\n");
		
		//Load Globals data
		testInterface.loadGlobalsData();
		
		testPersistence1();
		
	}
	
		
	public void testPersistence1() {
		System.out.println(Logger.ANSI_PURPLE + "\n\n----------Starting New Test Case - Persistence" + " ----------------\n" + Logger.ANSI_RESET); 
		testInterface.sleep();
		
		EntityRef<Cab.Command> cab101 = testInterface.getCabEntityRef("101");
		TestProbe<Cab.DebugCabStateResponse> debugProbe = testKit.createTestProbe();
		cab101.tell(new Cab.DebugCabState(debugProbe.getRef()));
		Cab.DebugCabStateResponse debugResponse = debugProbe.receiveMessage();
		
		
		// Sign in the cab if it is not signed in and set it to available
		if(debugResponse.majorState.equals(CabStates.MajorStates.SIGNED_OUT)) {
			Logger.logTestSuccess("Signing in the cab since it is signed out");
			cab101.tell(new Cab.SignIn(10));
		}
		else if (debugResponse.majorState.equals(CabStates.MajorStates.SIGNED_IN) && debugResponse.minorState.equals(CabStates.MinorStates.GIVING_RIDE)) {
			Logger.logTestSuccess("Resetting the cab as cab not in available state");
			TestProbe<Cab.NumRidesReponse> resetProbe = testKit.createTestProbe();
			cab101.tell(new Cab.Reset(resetProbe.getRef()));
			NumRidesReponse response = resetProbe.receiveMessage();	
			
			cab101.tell(new Cab.SignIn(10));
		}
		else {
			Logger.logTestSuccess("Cab is already in available state");
		}
		
		cab101.tell(new Cab.DebugCabState(debugProbe.getRef()));
		debugResponse = debugProbe.receiveMessage();
		
		Logger.logTestSuccess("Current State of the cab : " + debugResponse.toString());
		
		// Now we are sure that cab is signed in and ready for ride
	
		EntityRef<RideService.Command> rideService = testInterface.getRideServiceEntityRef("rideService1");
		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		TestProbe<RideService.RideResponse> probe = testKit.createTestProbe();
		rideService.tell(new RideService.RequestRide("201", 10, 100, probe.ref()));
		RideService.RideResponse resp = probe.receiveMessage();
		// Blocks and waits for a response message.
		// There is also an option to block for a bounded period of time
		// and give up after timeout.

		if(resp.rideId == -1) {
			Logger.logTestFail("Couldn't get a ride!");
		}
		else {
			cab101.tell(new Cab.RideEnded(resp.rideId));
			Logger.logTestSuccess("Got a ride and ended it!");
		}
		
		cab101.tell(new Cab.DebugCabState(debugProbe.getRef()));
		debugResponse = debugProbe.receiveMessage();
		Logger.logTestSuccess("Modified State of the cab : " + debugResponse.toString());
		Logger.logTestSuccess("Crash both the nodes and rerun them and retest to check if state of the cab persists");
	}
	
}
