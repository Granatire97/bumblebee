package com.example.demo;

import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "scheduling.enabled=false")
public class ThresholdServiceTests {
	
	private @Autowired ThresholdService thresholdService;
	double allocationThreshold;
	HttpClient httpClient;
	
	@Before
	public void setup() {
		try {
			Field allocThreshold = ThresholdService.class.getDeclaredField("allocationThreshold");
			allocThreshold.setAccessible(true);
			allocationThreshold = (double) allocThreshold.get(thresholdService);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
	}
	
	@Test
	public void testBelowThreshold() {
		assertFalse(thresholdService.surpassesThreshold(allocationThreshold - 0.1));
	}
	
	@Test
	public void testAboveThreshold() {
		assertTrue(thresholdService.surpassesThreshold(allocationThreshold + 0.1));
	}
	
	@Test
	public void testAtThreshold() {
		assertTrue(thresholdService.surpassesThreshold(allocationThreshold));
	}
	
	@Test
	public void testMakeDataStringAllocationFailures() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<ThresholdService> service = ThresholdService.class;
		Method makeDataString = service.getDeclaredMethod("makeDataString", String.class, double.class);
		makeDataString.setAccessible(true);
		String dataString = (String) makeDataString.invoke(new ThresholdService(),"Allocation Failure Percentage", 0.5);
		assertEquals("{\"properties\": {\"Allocation Failure Percentage\": \"0.5\"}}", dataString);
	}
	
	@Test
	public void testMakeDataStringEmptyString() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<ThresholdService> service = ThresholdService.class;
		Method makeDataString = service.getDeclaredMethod("makeDataString", String.class, double.class);
		makeDataString.setAccessible(true);
		String dataString = (String) makeDataString.invoke(new ThresholdService(),"", 0.5);
		assertEquals("{\"properties\": {\"\": \"0.5\"}}", dataString);
	}
	
	
	
}
