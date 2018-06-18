package com.dcsg.fulfillment.threshold;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.dcsg.fulfillment.threshold.ThresholdService;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "scheduling.enabled=false")
public class ThresholdServiceTests {
	
	private @Autowired ThresholdService thresholdService;
	private @Autowired ThresholdConfiguration thresholdConfig;
	
	
	@Test
	public void testAllocationFailuresBelowThreshold() {
		assertFalse(thresholdService.surpassesMetricThreshold(thresholdConfig.getAllocationThreshold() - 0.1, 
				thresholdConfig.getAllocationThreshold()));
	}
	
	@Test
	public void testAboveThreshold() {
		assertTrue(thresholdService.surpassesMetricThreshold(thresholdConfig.getAllocationThreshold() + 0.1, 
				thresholdConfig.getAllocationThreshold()));
	}
	
	@Test
	public void testAtThreshold() {
		assertTrue(thresholdService.surpassesMetricThreshold(thresholdConfig.getPickDeclineThreshold(), 
				thresholdConfig.getPickDeclineThreshold()));
	}
	
	@Test
	public void testMakeDataStringAllocationFailures() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<ThresholdService> service = ThresholdService.class;
		Method makeDataString = service.getDeclaredMethod("makeDataString", String.class, Object.class);
		makeDataString.setAccessible(true);
		String dataString = (String) makeDataString.invoke(new ThresholdService(),"Allocation Failure Percentage", 0.5);
		assertEquals("{\"properties\": {\"Allocation Failure Percentage\": \"0.5\"}}", dataString);
	}
	
	@Test
	public void testMakeDataStringEmptyString() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<ThresholdService> service = ThresholdService.class;
		Method makeDataString = service.getDeclaredMethod("makeDataString", String.class, Object.class);
		makeDataString.setAccessible(true);
		String dataString = (String) makeDataString.invoke(new ThresholdService(),"", 0.5);
		assertEquals("{\"properties\": {\"\": \"0.5\"}}", dataString);
	}
	
	@Test
	public void testmakeHTMLTable() {
		String expectedHtmlTable = "" +
		           "<table>" +
		           "<tr>" +
		           "<th>Drift Type</th>" +
		           "<th>Partner</th>" +
		           "<th>Facility Count</th>" +
		           "<th>View Name</th>" +
		           "<th>Row Count</th>" +
		           "</tr>" +
		           "<tr>" +
		           "<td>Peyton Manning</td>" +
		           "<td>Colts</td>" +
		           "<td>18</td>" +
		           "<td>6'5\"</td>" + 
		           "<td>University of Tennessee</td>" +
		           "<td>42</td>" + 
		           "</tr>" +
		           "</table>";
		
		List<List<String>> players = new ArrayList<List<String>>();
		List<String> peyton = new ArrayList<String>();
		peyton.add("Peyton Manning");
		peyton.add("Colts");
		peyton.add("18");
		peyton.add("6'5\"");
		peyton.add("University of Tennessee");
		peyton.add("42");
		players.add(peyton);
		String actualHtmlTable = thresholdService.makeHTMLTable(players);
		
		assertEquals(expectedHtmlTable, actualHtmlTable);
	}
	
	@Test
	public void testmakeHTMLTableEmpty() {
		String expectedHtmlTable = "" +
		           "<table>" +
		           "<tr>" +
		           "<th>Drift Type</th>" +
		           "<th>Partner</th>" +
		           "<th>Facility Count</th>" +
		           "<th>View Name</th>" +
		           "<th>Row Count</th>" +
		           "</tr>" +
		           "</table>";
		
		List<List<String>> players = new ArrayList<List<String>>();
		String actualHtmlTable = thresholdService.makeHTMLTable(players);
		
		assertEquals(expectedHtmlTable, actualHtmlTable);
	}

	
}
