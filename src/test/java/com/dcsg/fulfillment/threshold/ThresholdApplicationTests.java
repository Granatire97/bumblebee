package com.dcsg.fulfillment.threshold;

import org.junit.Test; 
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "scheduling.enabled=false")
public class ThresholdApplicationTests {

	@Test
	public void contextLoads() {
	}

}
