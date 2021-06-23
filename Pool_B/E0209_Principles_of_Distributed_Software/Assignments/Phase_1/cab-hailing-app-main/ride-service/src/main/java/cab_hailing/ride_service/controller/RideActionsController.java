package cab_hailing.ride_service.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cab_hailing.ride_service.Logger;
import cab_hailing.ride_service.service.CabActionsService;
import cab_hailing.ride_service.service.RideActionsService;

@RestController
public class RideActionsController {
	@Value("${url.cab_service_base_url}")
	private String cabServiceBaseURL;	
	
	@Autowired
	CabActionsService cabActionsService;

	@Autowired
	RideActionsService rideActionsService;

	// Consumed by Cab Service
	@GetMapping("rideEnded")
	public boolean rideEnded(@RequestParam long rideId) {
		return rideActionsService.rideEnded(rideId);
	}
	
	// Consumed by Cab Service
	@GetMapping("cabSignsIn")
	public boolean cabSignsIn(@RequestParam long cabId, @RequestParam long initialPos) {
		return cabActionsService.cabSignsIn(cabId, initialPos);
	}

	// Consumed by Cab Service
	@GetMapping("cabSignsOut")
	public boolean cabSignsOut(@RequestParam long cabId) {
		return cabActionsService.cabSignsOut(cabId);
	}

	// Consumed by user 
	@GetMapping("requestRide")
	public String requestRide(@RequestParam long custId, @RequestParam long sourceLoc, @RequestParam long destinationLoc) {
		return rideActionsService.requestRide(custId, sourceLoc, destinationLoc);
	}

	// For testing
	@GetMapping("getCabStatus")
	public String getCabStatus(@RequestParam long cabId) {
		return cabActionsService.getCabStatus(cabId);
	}
	
	
	@GetMapping("reset")
	public void reset() {
		// hint cab service logger that reset has begun
		String cabServiceLogResetEndpoint = cabServiceBaseURL+"printLogReset";
		Logger.log("cabServiceLogResetEndpoint : " + cabServiceLogResetEndpoint);
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) new URL(cabServiceLogResetEndpoint).openConnection();
			httpConnection.setRequestMethod("GET");
			int response = httpConnection.getResponseCode();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cabActionsService.reset();		
	}

}