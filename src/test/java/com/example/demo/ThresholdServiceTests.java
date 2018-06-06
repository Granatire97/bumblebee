package com.example.demo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ThresholdServiceTests {
	
	private ThresholdService thresholdService;
	
	@Before
	public void setup() {
		thresholdService = new ThresholdService();		
	}
	
	@Test
	public void testBelowThreshold() {
		assertFalse(thresholdService.surpassesThreshold(0.1));
	}
	
	@Test
	public void testAboveThreshold() {
		assertTrue(thresholdService.surpassesThreshold(1.0));
	}
	
	@Test
	public void testAtThreshold() {
		assertTrue(thresholdService.surpassesThreshold(0.5));
	}

}
