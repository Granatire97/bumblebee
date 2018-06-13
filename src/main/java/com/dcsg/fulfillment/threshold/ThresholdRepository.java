package com.dcsg.fulfillment.threshold;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;

@Repository
public class ThresholdRepository {
	
	private @Autowired JdbcTemplate jdbcTemplate;
	Map<String,String> queries = new HashMap<String, String>();
	private String driftAnalysisData;
	
	private void loadQuery(String queryName) throws IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = classLoader.getResourceAsStream(queryName + ".sql");
		if(is == null) throw new IOException("Unable to find query: " + queryName);
		
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		while(true) {
			String line = r.readLine();
			if(line == null) break;
			sb.append(line).append("\n");
		}
		
		queries.put(queryName, sb.toString());
	}
	
	public String getDriftAnalysisData() {
		return driftAnalysisData;
	}
	
	
	
	public Double getAllocationFailures() throws IOException {
		
		Double allocationFailures = 0.0;
		
		loadQuery("AllocationFailureQuery");
		
		try {
			allocationFailures = jdbcTemplate.queryForObject(queries.get("AllocationFailureQuery"), Double.class);
		} catch (EmptyResultDataAccessException e) {}
		return allocationFailures;
	}
	
	public Double getPickDeclineFailures() throws IOException {

		Double pickDeclineFailures = 0.0;
	
		loadQuery("PickDeclineQuery");
		
		try {
			pickDeclineFailures = jdbcTemplate.queryForObject(queries.get("PickDeclineQuery"), Double.class);
		} catch (EmptyResultDataAccessException e) {}
		return pickDeclineFailures;
	}

	public List<String> getDriftAnalysis() throws IOException{
		
		List<String> data = null;
		loadQuery("DriftAnalysisQuery");
		
		try {

			data = jdbcTemplate.query(queries.get("DriftAnalysisQuery"), new RowMapper<String>(){
										public String mapRow(ResultSet rs, int rowNum)
																	throws SQLException {
											StringBuilder data = new StringBuilder();
											data.append(rs.getString(1));
											data.append(rs.getString(2));
											data.append(rs.getString(3));
											data.append(rs.getString(4));
											data.append(rs.getString(5));
											System.out.println(data);
											driftAnalysisData = data.toString();
											return driftAnalysisData;
										}
								
			});
		} catch (EmptyResultDataAccessException e) {}
		return data;
	}
	
	//System.out.print(data);

	

}
