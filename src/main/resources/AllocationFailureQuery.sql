select
trunc ((a.ordercount/c.ordercount)*100, 2)
from
(
--Allocation failures, first pass
select /*+ parallel(8) */
  to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24') as af_date,
  count(distinct(poli.purchase_orders_line_item_id)) as ordercount
from purchase_orders_event poe join purchase_orders_line_item poli on poli.purchase_orders_id = poe.purchase_orders_id
where poe.purchase_orders_event_id in (
  select purchase_orders_event_id
  from purchase_orders_event
  where created_dttm  >= sysdate-1/24 --adjust as needed to incorporate more than the previous hour
  and field_name = 'LINE ITEM STATUS'
  and new_value in ('Allocation Failed') and
  old_value in ('Sourced')
    and created_source in ('WCS')
  )
and trunc(poli.created_dttm,'hh') = trunc(poe.created_dttm,'hh')
and poli.parent_po_line_item_id is null
and poli.dsg_ship_via <> 'BOPS'
group by to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24')
order by 1 desc, 2
)a
join
(
----total orders created
select /*+ parallel(8) */
  to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24') as total_date,
  count(distinct(poli.purchase_orders_line_item_id)) as ordercount
from purchase_orders_event poe join purchase_orders_line_item poli on poli.purchase_orders_id = poe.purchase_orders_id
where poe.purchase_orders_event_id in (
  select purchase_orders_event_id
  from purchase_orders_event
  where created_dttm >= sysdate-1/24 --adjust as needed to incorporate more than the previous hour
  and field_name = 'LINE ITEM STATUS'
and
  old_value in ('Created')
and new_value in ('Sourced')
  and created_source in ('WCS')
  )
--and trunc(poli.created_dttm,'dd') = trunc(poli.last_updated_dttm,'dd')
and trunc(poli.created_dttm,'hh') = trunc(poe.created_dttm,'hh')
and poli.parent_po_line_item_id is null
group by to_char(trunc(poli.created_dttm, 'HH24'),'YYYYMMDD-HH24'), old_value, new_value
order by 1 desc, 2
)c on c.total_date = af_date
order by 1 desc
