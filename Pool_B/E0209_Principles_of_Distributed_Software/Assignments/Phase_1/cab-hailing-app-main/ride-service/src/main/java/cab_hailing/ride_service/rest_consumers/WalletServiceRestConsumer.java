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
public class WalletServiceRestConsumer {
	
	static RestTemplate restTemplate = new RestTemplate();

	@Value("${url.wallet_service_base_url}")
	private String walletServiceBaseURL;	
	
	public boolean consumeDeductAmount(long custID, long amount) {
		String endpointURL = walletServiceBaseURL+"deductAmount?custId={custId}&amount={amount}";
		
		Map<String, Long> requestParams = new HashMap<>();
		requestParams.put("custId", custID);
		requestParams.put("amount", amount);
		
		
		Boolean result = restTemplate.getForObject(endpointURL, Boolean.class, requestParams);
		
		Logger.log("Received result from wallet service for deduct amount: " + result);
		
		if(result!=null)
			return result.booleanValue();
		
		Logger.logErr("Couldn't get a valid response from Wallet Service for deductAmount");
		return false;
	}
}
