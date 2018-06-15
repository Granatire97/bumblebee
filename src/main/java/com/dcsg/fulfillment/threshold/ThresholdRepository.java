package com.dcsg.fulfillment.threshold;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
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

	public List<List<String>> getDriftAnalysis() throws IOException{
		
		List<List<String>> data = null;
		loadQuery("DriftAnalysisQuery");
		
		try {

			data = jdbcTemplate.query(queries.get("DriftAnalysisQuery"), new RowMapper<List<String>>(){
										public List<String> mapRow(ResultSet rs, int rowNum)
																	throws SQLException {
											
											List<String> driftAnalysisData = new ArrayList<String>();	
											driftAnalysisData.add(rs.getString(1));
											driftAnalysisData.add(rs.getString(2));
											driftAnalysisData.add(rs.getString(3));
											driftAnalysisData.add(rs.getString(4));
											driftAnalysisData.add(rs.getString(5));
											return driftAnalysisData;
										
										}
								
			});
		} catch (EmptyResultDataAccessException e) {}

		return data;
	}
	
	public Double getCreationFailures() throws IOException {

		Double creationFailures = 0.0;
	
		loadQuery("DOCreationFailureQuery");
		
		try {
			creationFailures = jdbcTemplate.queryForObject(queries.get("DOCreationFailureQuery"), Double.class);
		} catch (EmptyResultDataAccessException e) {}
		return creationFailures;
	}

}
