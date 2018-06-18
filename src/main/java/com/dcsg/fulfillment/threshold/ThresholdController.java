package com.dcsg.fulfillment.threshold;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ThresholdController {

	private @Autowired ThresholdService thresholdService;
	private @Autowired ThresholdConfiguration thresholdConfig;
	
	@RequestMapping(path = "/getAllocationFailures")
	public HashMap<String,String> getAllocationFailures() throws IOException{
		
		Double allocationFailures = thresholdService.getAllocationFailures();
		Double allocationThreshold = thresholdConfig.getAllocationThreshold();
		String priority = thresholdService.surpassesMetricThreshold(allocationFailures, allocationThreshold) ? "BAD":"GOOD";
		HashMap<String, String> response = new HashMap<String, String>();
		response.put("priority", priority);
		response.put("Failure Percentage", allocationFailures.toString());
		return response;		
	}	
	
}
