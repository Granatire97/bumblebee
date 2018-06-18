package com.dcsg.fulfillment.threshold;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse; 
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {
    
	private @Autowired ThresholdRepository repo;
	private @Autowired ThresholdConfiguration config;	
	
	public String makeHTMLTable(List<List<String>> driftAnalysisData) throws IOException {
		String driftSendToXMatters;
		
		StringBuilder driftAnalysisTable = new StringBuilder();
		driftAnalysisTable.append(
		           "<table>" +
		           "<tr>" +
		           "<th>Drift Type</th>" +
		           "<th>Partner</th>" +
		           "<th>Facility Count</th>" +
		           "<th>View Name</th>" +
		           "<th>Row Count</th>" +
		           "</tr>");

		for (int x = 0; x < driftAnalysisData.size(); x++){
			driftAnalysisTable.append("<tr>");
			for  (int  y = 0; y < driftAnalysisData.get(0).size(); y++) {
				driftAnalysisTable.append("<td>")
				.append(driftAnalysisData.get(x).get(y))
				.append("</td>");
			}
				driftAnalysisTable.append("</tr>");
		}
		
		driftAnalysisTable.append("</table>");
		driftSendToXMatters = driftAnalysisTable.toString();
		return driftSendToXMatters;
	}
	
    public double getAllocationFailures() throws IOException {
		return repo.getAllocationFailures();
	}
    
    public double getPickDeclineFailures() throws IOException {
		return repo.getPickDeclineFailures();

	}
    
    public List<List<String>> getDriftAnalysis() throws IOException {
		return repo.getDriftAnalysis();
	}
    
    public double getCreationFailure() throws IOException {
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
	
	private String makeDriftAnalysisString(String metricName, List<List<String>> dataFromQuery) throws IOException {
		String driftAnalysisAsTable = makeHTMLTable(dataFromQuery);
		String data = "{" +
				  "\"properties\": {" +
				    "\"" + metricName + "\": \"" + driftAnalysisAsTable + "\"" +
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
	
private int sendDriftAnalysisToXMatters(String metricName, List<List<String>> driftData, String url_name) throws IOException {
		
		String payload = makeDriftAnalysisString(metricName, driftData);
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
	private void monitorAllocationFailures() throws IOException {
		double allocationFailures = getAllocationFailures();
		boolean thresholdSurpassed = surpassesAllocationThreshold(allocationFailures, config.getAllocationThreshold());
		if (thresholdSurpassed)
			sendToXMatters(config.getAllocationName(), allocationFailures, config.getXMattersAllocationURL());
	}
	
	@Scheduled(fixedRate = 3600000)
	private void monitorPickDeclineFailures() throws IOException {
		double pickDeclineFailures = getPickDeclineFailures();
		boolean thresholdSurpassed = surpassesPickDeclineThreshold(pickDeclineFailures, config.getPickDeclineThreshold());
		if(thresholdSurpassed) 
			sendToXMatters(config.getPickDeclineName(), pickDeclineFailures, config.getXMattersPickDeclineURL());
	}
	
	@Scheduled(fixedRate = 3600000) // cron = "0 0 8/3 ? * * "
	private void monitorDriftAnalysis() throws IOException {
		List<List<String>> driftAnalysis = getDriftAnalysis();
		sendDriftAnalysisToXMatters(config.getDriftAnalysisName(), driftAnalysis, config.getXMattersDriftAnalysisURL());
	}
	
	@Scheduled(fixedRate = 3600000)
	private void monitorCreationFailures() throws IOException {
		double creationFailures = getCreationFailure();
		boolean thresholdSurpassed = supassesCreationFailureThreshold(creationFailures, config.getCreationFailureThreshold());
		if(thresholdSurpassed) 
			sendToXMatters(config.getCreationFailureName(), creationFailures, config.getXMattersCreationFailureURL());
		
		config.printAllocationURL();
	}
	
			
}
