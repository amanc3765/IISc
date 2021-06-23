package pods.cabs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.persistence.typed.PersistenceId;
import pods.cabs.utils.InitFileReader;
import pods.cabs.utils.InitFileReader.InitReadWrapper;
import pods.cabs.utils.Logger;

public class App {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("ERROR : Insufficient Arguments\n");
		} else {
			int port = Integer.parseInt(args[0]);
			boolean firstTimeFlag = false;
			if (args.length > 1) {
				String firstTimeFlagStr = args[1];
				if (firstTimeFlagStr.equals("-firstTime")) {
					firstTimeFlag = true;
				}
			}

			InitReadWrapper wrapperObj = new InitReadWrapper();
			Globals.initReadWrapperObj = wrapperObj;
			try {
				InitFileReader.readInitFile(wrapperObj);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			
			startup(port, firstTimeFlag);
		}
	}

	private static void startup(int port, boolean firstTimeFlag) {

		Logger.logErr("inside startup");
		// Override the configuration of the port
		Map<String, Object> overrides = new HashMap<>();
		overrides.put("akka.remote.artery.canonical.port", port);

		// Specifying roles based on port number

		// First Cluster Node has to do journalling
		if (port == 25251) {
			overrides.put("akka.persistence.journal.plugin", "akka.persistence.journal.leveldb");
			overrides.put("akka.persistence.journal.proxy.start-target-journal", "on");

		} else {
			overrides.put("akka.persistence.journal.plugin", "akka.persistence.journal.proxy");

		}

		String nodeName;

		switch (port) {
		case 25251:
			nodeName = "A";
			break;
		case 25252:
			nodeName = "B";
			break;
		case 25253:
			nodeName = "C";
			break;
		case 25254:
			nodeName = "D";
			break;
		default:
			nodeName = "ERROR";
			Logger.log("Invalid port number provided");
			System.exit(1);
		}

		Config config = ConfigFactory.parseMap(overrides).withFallback(ConfigFactory.load());

		ActorSystem.create(rootBehavior(firstTimeFlag, nodeName), "ClusterSystem", config);

	}

	private static Behavior<Void> rootBehavior(boolean isFirstTime, String nodeName) {
		return Behaviors.setup(context -> {
			final ClusterSharding sharding = ClusterSharding.get(context.getSystem());
			Logger.logErr("inside root behavior");

			// Create sharding proxy for RideService entities
			sharding.init(
					Entity.of(RideService.TypeKey, entityContext -> RideService.create(entityContext.getEntityId())));

			// Create sharding proxy for Cab entities
			sharding.init(Entity.of(Cab.TypeKey, entityContext -> Cab.create(entityContext.getEntityId(),
					PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()))));

			int rideIdBase = 10;

			switch (nodeName) {
			case "A":
				rideIdBase = 0;
				break;
			case "B":
				rideIdBase = 1;
				break;
			case "C":
				rideIdBase = 2;
				break;
			case "D":
				rideIdBase = 3;
				break;
			}

			String rideEntityId;

			if (isFirstTime) {
				for (int i = 1; i <= 3; i++) {
					rideEntityId = "rideService" + (rideIdBase * 3 + i);
//		    	rideEntityId = "rideService"+(i);
					Logger.logErr("creating rideService entity: " + rideEntityId);
					EntityRef<RideService.Command> ref = sharding.entityRefFor(RideService.TypeKey, rideEntityId);
					ref.tell(new RideService.Command());
//		    	ref.tell(new RideService.Command());
				}
			}

			Logger.logErr("chkp1");

			EntityRef<Cab.Command> cabRef = sharding.entityRefFor(Cab.TypeKey, "101");
			cabRef.tell(new Cab.Command());
			return Behaviors.empty();
		});
	}
}
