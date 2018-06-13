package com.dcsg.fulfillment.threshold;

import java.io.IOException;

import org.apache.http.HttpResponse; 
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {
    
	private @Autowired ThresholdRepository repo;
	private @Autowired ThresholdConfiguration config;
	
    public double getAllocationFailures() {
		return repo.getAllocationFailures();
	}
    
    public double getPickDeclineFailures() {
		return repo.getPickDeclineFailures();
	} 
    
    public double getCreationFailure() {
    	return repo.getCreationFailures();
    }
	
	public boolean surpassesAllocationThreshold(double allocationFailures, double allocationThreshold){
		return allocationFailures >= allocationThreshold;
	} 
	
	public boolean surpassesPickDeclineThreshold(double pickDeclineFailures, double pickDeclineThreshold){
		return pickDeclineFailures >= pickDeclineThreshold;
	} 
	
	public boolean supassesCreationFailureThreshold(double creationFailures, double creationFailureThreshold) {
		return creationFailures >= creationFailureThreshold;
	}
	
	
	private String makeDataString(String metricName, double metric) {
		String data = "{" +
				  "\"properties\": {" +
				    "\"" + metricName + "\": \"" + metric + "\"" +
				  "}}";
		System.out.println(data);
		return data;
	}
	
	private int sendToXMatters(String metricName, double metric, String url_name) {
		
		String payload = makeDataString(metricName, metric);
        StringEntity entity = new StringEntity(payload,
                ContentType.APPLICATION_JSON);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url_name);
        request.setEntity(entity);     
        
        HttpResponse response;
		try {
			response = httpClient.execute(request);
			return response.getStatusLine().getStatusCode();
		} catch (IOException e) {}   
        return -1;
	}
	
	@Scheduled(fixedRate = 3600000)
	private void monitorAllocationFailures() {
		double allocationFailures = getAllocationFailures();
		boolean thresholdSurpassed = surpassesAllocationThreshold(allocationFailures, config.getAllocationThreshold());
		if (thresholdSurpassed)
			sendToXMatters(config.getAllocationName(), allocationFailures, config.getXMattersAllocationURL());
	}
	
	@Scheduled(fixedRate = 3600000)
	private void monitorPickDeclineFailures() {
		double pickDeclineFailures = getPickDeclineFailures();
		boolean thresholdSurpassed = surpassesPickDeclineThreshold(pickDeclineFailures, config.getPickDeclineThreshold());
		if(thresholdSurpassed) 
			sendToXMatters(config.getPickDeclineName(), pickDeclineFailures, config.getXMattersPickDeclineURL());
	}
	
	@Scheduled(fixedRate = 3600000)
	private void monitorCreationFailures() {
		double creationFailures = getCreationFailure();
		boolean thresholdSurpassed = supassesCreationFailureThreshold(creationFailures, config.getCreationFailureThreshold());
		if(thresholdSurpassed) 
			sendToXMatters(config.getCreationFailureName(), creationFailures, config.getXMattersCreationFailureURL());
	}
	
			
}
