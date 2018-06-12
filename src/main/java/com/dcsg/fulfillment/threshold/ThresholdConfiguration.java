package com.dcsg.fulfillment.threshold;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
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
	
	
}
