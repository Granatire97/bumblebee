package com.dcsg.fulfillment.threshold;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class ThresholdConfiguration {
	@Value("${x-mattersCredentials.username}")
    private String xMattersUsername;
    @Value("${x-mattersCredentials.password}")
    private String xMattersPassword;
    
	@Value("${x-mattersUrl.allocation}")
    private String xMattersAllocationURL;
    @Value("${metrics.allocation.name}")
    private String allocationName;
    @Value("${metrics.allocation.threshold}")
    private double allocationThreshold;
    
    @Value("${x-mattersUrl.pickDecline}")
    private String xMattersPickDeclineURL;
    @Value("${metrics.pickDecline.name}")
    private String pickDeclineName;
    @Value("${metrics.pickDecline.threshold}")
    private double pickDeclineThreshold;
    

    @Value("${x-mattersUrl.driftAnalysis}")
    private String xMattersDriftAnalysisURL;
    @Value("${metrics.driftAnalysis.name}")
    private String driftAnalysisName;
    

    @Value("${x-mattersUrl.creationFailure}")
    private String xMattersCreationFailureURL;
    @Value("${metrics.creationFailure.name}")
    private String creationFailureName;
    @Value("${metrics.creationFailure.threshold}")
    private double creationFailureThreshold;
    
    public String getXMattersUsername() {
    	return xMattersUsername;
    }
    public String getXMattersPassword() {
    	return xMattersPassword;
    }
    public String getXMattersAllocationURL() {
		return xMattersAllocationURL;
	}
    
    public String getAllocationName() {
  		return allocationName;
  	}
    
    public double getAllocationThreshold() {
  		return allocationThreshold;
  	}
    
    public String getXMattersPickDeclineURL() {
		return xMattersPickDeclineURL;
	}
    
    public String getPickDeclineName() {
  		return pickDeclineName;
  	}
    
    public double getPickDeclineThreshold() {
  		return pickDeclineThreshold;
  	}
	
    public String getXMattersDriftAnalysisURL() {
		return xMattersDriftAnalysisURL;
	}
    
    public String getDriftAnalysisName() {
  		return driftAnalysisName;
  	}
    
    public String getXMattersCreationFailureURL() {
		return xMattersCreationFailureURL;
	}
    
    public String getCreationFailureName() {
  		return creationFailureName;
  	}
    
    public double getCreationFailureThreshold() {
  		return creationFailureThreshold;
  	}

}
