package com.dcsg.fulfillment.threshold;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class ThresholdConfiguration {

	@Value("${x-matters.allocation}")
    private String xMattersAllocationURL;
    @Value("${metrics.allocation.name}")
    private String allocationName;
    @Value("${metrics.allocation.threshold}")
    private double allocationThreshold;
    
    @Value("${x-matters.pickDecline}")
    private String xMattersPickDeclineURL;
    @Value("${metrics.pickDecline.name}")
    private String pickDeclineName;
    @Value("${metrics.pickDecline.threshold}")
    private double pickDeclineThreshold;
    

    @Value("${x-matters.driftAnalysis}")
    private String xMattersDriftAnalysisURL;
    @Value("${metrics.driftAnalysis.name}")
    private String driftAnalysisName;
    

    @Value("${x-matters.creationFailure}")
    private String xMattersCreationFailureURL;
    @Value("${metrics.creationFailure.name}")
    private String creationFailureName;
    @Value("${metrics.creationFailure.threshold}")
    private double creationFailureThreshold;
    
    
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
	
    public void printAllocationURL() {
    	System.out.println("============================================");
		System.out.format("My secret variable is: %s\n", xMattersAllocationURL);
		System.out.format("My secret variable is: %s\n", xMattersPickDeclineURL);
		System.out.format("My secret variable is: %s\n", xMattersDriftAnalysisURL);
		System.out.format("My secret variable is: %s\n", xMattersCreationFailureURL);
		System.out.println("============================================");
    }
    
	
	
}
