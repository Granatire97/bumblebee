package com.example.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class ThresholdService {
	
    private @Autowired JdbcTemplate jdbcTemplate;

	
	//TODO: implement timestamp range arguments
	public Double getAllocationFailures() {
		
		Double allocationFailures = 0.0;
		String sqlQuery = ""
				+ "select "
				+ "(a.ordercount/c.ordercount)*100 as allocation_failures "
				+ "from "
				+ "( "
				+ "select /*+ parallel(8) */ "
				+ "  to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24') as af_date, "
				+ "  count(distinct(poli.purchase_orders_line_item_id)) as ordercount "
				+ "from purchase_orders_event poe join purchase_orders_line_item poli on poli.purchase_orders_id = poe.purchase_orders_id "
				+ "where poe.purchase_orders_event_id in ( "
				+ "  select purchase_orders_event_id "
				+ "  from purchase_orders_event "
				+ "  where created_dttm  >= sysdate-2 "
				+ "  and field_name = 'LINE ITEM STATUS' "
				+ "  and new_value in ('Allocation Failed') and "
				+ "  old_value in ('Sourced') "
				+ "    and created_source in ('WCS') "
				+ "  ) "
				+ "and trunc(poli.created_dttm,'hh') = trunc(poe.created_dttm,'hh') "
				+ "and poli.parent_po_line_item_id is null "
				+ "and poli.dsg_ship_via <> 'BOPS' "
				+ "group by to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24') "
				+ "order by 1 desc, 2 "
				+ ")a "
				+ "join "
				+ "( "
				+ "select /*+ parallel(8) */ "
				+ "  to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24') as total_date, "
				+ "  count(distinct(poli.purchase_orders_line_item_id)) as ordercount "
				+ "from purchase_orders_event poe join purchase_orders_line_item poli on poli.purchase_orders_id = poe.purchase_orders_id "
				+ "where poe.purchase_orders_event_id in ( "
				+ "  select purchase_orders_event_id "
				+ "  from purchase_orders_event "
				+ "  where created_dttm >= sysdate-2 "
				+ "  and field_name = 'LINE ITEM STATUS' "
				+ "and "
				+ "  old_value in ('Created') "
				+ "and new_value in ('Sourced') "
				+ "  and created_source in ('WCS') "
				+ "  ) "
				+ "and trunc(poli.created_dttm,'hh') = trunc(poe.created_dttm,'hh') "
				+ "and poli.parent_po_line_item_id is null "
				+ "group by to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24'), old_value, new_value "
				+ "order by 1 desc, 2 "
				+ ")c on c.total_date = af_date "
				+ "order by 1 desc ";
		
		try {
			allocationFailures = jdbcTemplate.queryForObject(sqlQuery, Double.class);
		} catch (EmptyResultDataAccessException e) {}
		return allocationFailures;
	}
	
	public boolean surpassesThreshold(double allocationFailures){
		return allocationFailures >= 0.5;
	}
	
	@Scheduled(fixedRate = 3600000)
	private void monitorAllocationFailures() {
		double allocationFailures = getAllocationFailures();
		boolean issue = surpassesThreshold(allocationFailures);
		if(issue)
			sendToXMatters(allocationFailures, "api-endpoint");
		
	}
	private void sendToXMatters(double metric, String url_name) {
		
		String payload = "{" +
			  "\"properties\": {" +
			    "\"Allocation Failure Percentage\": \"" + metric + "\"" +
			  "}}";
        StringEntity entity = new StringEntity(payload,
                ContentType.APPLICATION_JSON);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url_name);
        request.setEntity(entity);
        
        try {
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine().getStatusCode());
        } catch (Exception e) {}
		
	}
			
}
