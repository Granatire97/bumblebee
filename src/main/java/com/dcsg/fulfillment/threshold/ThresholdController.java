package com.dcsg.fulfillment.threshold;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ThresholdController {

    private @Autowired ThresholdService thresholdService;
    private @Autowired ThresholdConfiguration thresholdConfig;
    
    @RequestMapping(path = "/AllocationFailures")
    @ResponseBody
    public HashMap<String,String> getAllocationFailures() throws IOException{
        
        Double allocationFailures = thresholdService.getAllocationFailures();
        Double allocationThreshold = thresholdConfig.getAllocationThreshold();
        String priority = thresholdService.surpassesMetricThreshold(allocationFailures, allocationThreshold) ? "BAD":"GOOD";
        HashMap<String, String> response = new HashMap<String, String>();
        response.put("priority", priority);
        response.put("Failure Percentage", allocationFailures.toString());
        return response;        
    }    
    
    @RequestMapping(path = "/PickDeclineFailures")
    @ResponseBody
    public HashMap<String,String> getPickDeclines() throws IOException{
        
        Double pickDeclineFailures = thresholdService.getPickDeclineFailures();
        Double pickDeclineThreshold = thresholdConfig.getPickDeclineThreshold();
        String priority = thresholdService.surpassesMetricThreshold(pickDeclineFailures, pickDeclineThreshold) ? "BAD":"GOOD";
        HashMap<String, String> response = new HashMap<String, String>();
        response.put("priority", priority);
        response.put("Failure Percentage", pickDeclineFailures.toString());
        return response;        
    }    
    
    @RequestMapping(path = "/DOCreationFailures")
    @ResponseBody
    public HashMap<String,String> getDOCreationFailures() throws IOException{
        
        Double doCreationFailures = thresholdService.getCreationFailure();
        Double doCreationThreshold = thresholdConfig.getCreationFailureThreshold();
        String priority = thresholdService.surpassesMetricThreshold(doCreationFailures, doCreationThreshold) ? "BAD":"GOOD";
        HashMap<String, String> response = new HashMap<String, String>();
        response.put("priority", priority);
        response.put("Failure Percentage", doCreationFailures.toString());
        return response;        
    }    
    
    @RequestMapping(path = "/DriftAnalysis")
    @ResponseBody
    public List<List<String>> getDriftAnalysis() throws IOException{
        
        List<List<String>> driftAnalysis = thresholdService.getDriftAnalysis();
        return driftAnalysis;        
    }  
    
    
}
