package com.example.demo;

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
	
    @Value("${x-matters.allocation}")
    private String xMattersAllocationURL;
    @Value("${metrics.allocation.name}")
    private String allocationName;
    @Value("${metrics.allocation.threshold}")
    private double allocationThreshold;
	
    public double getAllocationFailures() {
		return repo.getAllocationFailures();
	}
    
	public boolean surpassesThreshold(double allocationFailures){
		return allocationFailures >= allocationThreshold;
	}
	
	private String makeDataString(String metricName, double metric) {
		String data = "{" +
				  "\"properties\": {" +
				    "\"" + metricName + "\": \"" + metric + "\"" +
				  "}}";
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
		boolean thresholdSurpassed = surpassesThreshold(allocationFailures);
		if(thresholdSurpassed) 
			sendToXMatters(allocationName, allocationFailures, xMattersAllocationURL);
	}
			
}
