package cab_hailing.cab_service.rest_consumers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import cab_hailing.cab_service.Logger;

@Component
public class RideServiceRestConsumer {

	static RestTemplate restTemplate = new RestTemplate();

	@Value("${url.ride_service_base_url}")
	private String rideServiceBaseURL;

	public boolean consumeRideEnded(long rideID) {
		String endpointURL = rideServiceBaseURL + "rideEnded?rideId={rideId}";

		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("rideId", rideID);

		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);

		Logger.log("Received result from Ride Service for ride id : " + rideID + " for rideEnded : " + result);

		if (result != null)
			return result.booleanValue();

		Logger.log("Couldn't get a valid response from Ride Service for rideEnded");
		return false;
	}

	public boolean consumeCabSignsOut(long cabID) {
		String endpointURL = rideServiceBaseURL + "cabSignsOut?cabId={cabId}";

		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);

		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);

		Logger.log("Received result from Ride Service for cabSignsOut : " + result + " for cab id : " + cabID);

		if (result != null)
			return result.booleanValue();

		Logger.log("Couldn't get a valid response from Ride Service for cabSignsOut for cab id : " + cabID);
		return false;
	}

	public boolean consumeCabSignsIn(long cabID, long initialPos) {
		String endpointURL = rideServiceBaseURL + "cabSignsIn?cabId={cabId}&initialPos={initialPos}";

		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("cabId", cabID);
		requestParams.put("initialPos", initialPos);

		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);

		Logger.log("Received result from Ride Service for cabSignsIn : " + result + " for cab id : "
				+ cabID + " and initil pos : " + initialPos);

		if (result != null)
			return result.booleanValue();

		Logger.log("Couldn't get a valid response from Ride Service for cabSignsIn for cab id : " + cabID);
		return false;
	}

}
