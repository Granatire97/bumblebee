package com.dcsg.fulfillment.threshold;

import java.io.IOException;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {
    
	private @Autowired ThresholdRepository repo;
	private @Autowired ThresholdConfiguration config;	
	
	public String makeHTMLTable(List<List<String>> driftAnalysisData) {
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
 
	public boolean surpassesMetricThreshold(double metricFailures, double metricThreshold){
		return metricFailures >= metricThreshold;
	}
	
	
	private <T> String makeDataString(String metricName, T metric) {
		String data = "{" +
				  "\"properties\": {" +
				    "\"" + metricName + "\": \"" + metric + "\"" +
				  "}}";
		return data;
	}
	
	private String makeDriftAnalysisString(String metricName, List<List<String>> dataFromQuery) throws IOException {
		String driftAnalysisAsTable = makeHTMLTable(dataFromQuery);
		String data = makeDataString(metricName, driftAnalysisAsTable);
		return data;
	}
	
	private void sendToXMatters(String metricName, double metric, String url_name) throws ClientProtocolException, IOException {
		
		String payload = makeDataString(metricName, metric);
        StringEntity entity = new StringEntity(payload,
                ContentType.APPLICATION_JSON);
        
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
         = new UsernamePasswordCredentials(config.getXMattersUsername(), config.getXMattersPassword());
        provider.setCredentials(AuthScope.ANY, credentials);
          
        HttpClient client = HttpClientBuilder.create()
          .setDefaultCredentialsProvider(provider)
          .build();
        
        HttpPost request = new HttpPost(url_name);
        request.setEntity(entity);
        
        client.execute(request);
	}
	
	private void sendDriftAnalysisToXMatters(String metricName, List<List<String>> driftData, String url_name) throws IOException {
		
		String payload = makeDriftAnalysisString(metricName, driftData);
        StringEntity entity = new StringEntity(payload,
                ContentType.APPLICATION_JSON);

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
         = new UsernamePasswordCredentials(config.getXMattersUsername(), config.getXMattersPassword());
        provider.setCredentials(AuthScope.ANY, credentials);
          
        HttpClient client = HttpClientBuilder.create()
          .setDefaultCredentialsProvider(provider)
          .build();
        
        HttpPost request = new HttpPost(url_name);
        request.setEntity(entity);
        
        client.execute(request);
	}
	
	
	
	@Scheduled(cron = "0 0 8/1 ? * * ")
	private void monitorAllocationFailures() throws IOException {
		double allocationFailures = getAllocationFailures();
		boolean thresholdSurpassed = surpassesMetricThreshold(allocationFailures, config.getAllocationThreshold());
		if (thresholdSurpassed)
			sendToXMatters(config.getAllocationName(), allocationFailures, config.getXMattersAllocationURL());
	}
	
	@Scheduled(cron = "0 0 8/1 ? * * ")
	private void monitorPickDeclineFailures() throws IOException {
		double pickDeclineFailures = getPickDeclineFailures();
		boolean thresholdSurpassed = surpassesMetricThreshold(pickDeclineFailures, config.getPickDeclineThreshold());
		if(thresholdSurpassed) 
			sendToXMatters(config.getPickDeclineName(), pickDeclineFailures, config.getXMattersPickDeclineURL());
	}
	
	@Scheduled(cron = "0 0 8,17 ? * *")
	private void monitorDriftAnalysis() throws IOException {
		List<List<String>> driftAnalysis = getDriftAnalysis();
		sendDriftAnalysisToXMatters(config.getDriftAnalysisName(), driftAnalysis, config.getXMattersDriftAnalysisURL());
	}
	
	@Scheduled(cron = "0 0 8/1 ? * * ")
	private void monitorCreationFailures() throws IOException {
		double creationFailures = getCreationFailure();
		boolean thresholdSurpassed = surpassesMetricThreshold(creationFailures, config.getCreationFailureThreshold());
		if(thresholdSurpassed) 
			sendToXMatters(config.getCreationFailureName(), creationFailures, config.getXMattersCreationFailureURL());
	}
	
			
}
