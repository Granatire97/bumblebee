package com.dcsg.fulfillment.threshold;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ThresholdRepository {
	
	private @Autowired JdbcTemplate jdbcTemplate;
	
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
				+ "  where created_dttm  >= sysdate-1/24 "
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
				+ "  where created_dttm >= sysdate-1/24 "
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
	
	public Double getPickDeclineFailures() {

		Double pickDeclineFailures = 0.0;
		String sqlQuery = ""
						+ "Select /*+ parallel */ "
						+ "f.facility_Type_Bits "
						+ ",((sum(oli.orig_order_qty)-(sum(oli.order_qty)))/sum(oli.orig_order_qty)) *100 as decline_percent "
						+ "from "
						+ "  order_line_item oli "
						+ "  join orders o on o.order_id = oli.order_id "
						+ "  Join Purchase_Orders Po On Po.Purchase_Orders_Id = O.Purchase_Order_Id "
						+ "  join facility f on f.facility_id = o.o_facility_id --and f.facility_type_bits = 64 --only look for stores "
						+ "Where "
						+ "  Oli.Created_Dttm >= Trunc(Sysdate)-10 --only view today's order lines "
						+ "  And O.Dsg_Ship_Via <> 'BOPS' --remove bopis from this % "
						+ "  And O.Do_Status >= 190 --DOs are shipped or canceled "
						+ "  and exists --makes sure we are only tracking the parent line "
						+ "     (select null from purchase_orders_line_item poli "
						+ "        where oli.mo_line_item_id = poli.purchase_orders_line_item_id "
						+ "        and poli.parent_po_line_item_id is null "
						+ "      ) "
						+ "Group By "
						+ "f.facility_type_bits "
						+ "--order by "
						+ "--  trunc(po.purchase_orders_date_dttm,'DD') "
						+ ";";

		try {
			pickDeclineFailures = jdbcTemplate.queryForObject(sqlQuery, Double.class);
		} catch (EmptyResultDataAccessException e) {}
		return pickDeclineFailures;
	}








}
