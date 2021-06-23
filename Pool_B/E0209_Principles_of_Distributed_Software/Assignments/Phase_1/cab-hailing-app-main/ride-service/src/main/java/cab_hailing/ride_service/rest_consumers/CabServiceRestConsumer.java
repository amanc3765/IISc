package cab_hailing.ride_service.rest_consumers;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import cab_hailing.ride_service.Logger;

@Component
public class CabServiceRestConsumer {
	
	static RestTemplate restTemplate = new RestTemplate();

	@Value("${url.cab_service_base_url}")
	private String cabServiceBaseURL;	
	
	public boolean consumeRequestRide(long cabID, long rideID, long sourceLoc, long destinationLoc) {
		String endpointURL = cabServiceBaseURL+"requestRide?cabId={cabId}&rideId={rideId}"
				+ "&sourceLoc={sourceLoc}&destinationLoc={destinationLoc}";
		
		
		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);
		requestParams.put("rideId", rideID);
		requestParams.put("sourceLoc", sourceLoc);
		requestParams.put("destinationLoc", destinationLoc);
		
		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);
		
		Logger.log("Received result from Cab Service for requestRide : "+result);
		
		if(result!=null)
			return result.booleanValue();
		
		Logger.logErr("Couldn't get a valid response from Cab Service for requestRide");
		return false;
	}
	
	
	public boolean consumeRideStarted(long cabID, long rideID) {
		String endpointURL = cabServiceBaseURL+"rideStarted?cabId={cabId}&rideId={rideId}";		
		
		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);
		requestParams.put("rideId", rideID);
		
		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);
		
		Logger.log("Received result from Cab Service for rideStarted: "+result);
		
		if(result!=null)
			return result.booleanValue();
		
		Logger.logErr("Couldn't get a valid response from Cab Service for rideStarted");
		return false;
	}
	
	
	public boolean consumeRideCanceled(long cabID, long rideID) {
		String endpointURL = cabServiceBaseURL+"rideCanceled?cabId={cabId}&rideId={rideId}";		
		
		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);
		requestParams.put("rideId", rideID);
		
		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);
		
		Logger.log("Received result from Cab Service for rideCancelled : "+result);
		
		if(result!=null)
			return result.booleanValue();
		
		Logger.log("Couldn't get a valid response from Cab Service for rideCanceled");
		return false;
	}
	
	public boolean consumeSignOut(long cabID) {
		String endpointURL = cabServiceBaseURL+"signOut?cabId={cabId}";		
		
		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);
		
		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);
		
		Logger.log("Received result from Cab Service for signOut : "+result);
		
		if(result!=null)
			return result.booleanValue();
		
		Logger.logErr("Couldn't get a valid response from Cab Service for signOut");
		return false;
	}
	
	public boolean consumeRideEnded(long cabID, long rideID) {
		String endpointURL = cabServiceBaseURL+"rideEnded?cabId={cabId}&rideId={rideId}";		
		
		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);
		requestParams.put("rideId", rideID);
		
		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);
		
		Logger.log("Received result from Cab Service for rideEnded : "+result);
		
		if(result!=null)
			return result.booleanValue();
		
		Logger.logErr("Couldn't get a valid response from Cab Service for rideEnded");
		return false;
	}
}
