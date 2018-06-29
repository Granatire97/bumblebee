Select /*+ parallel */
((sum(oli.orig_order_qty)-(sum(oli.order_qty)))/sum(oli.orig_order_qty)) *100 as decline_percent
from
  order_line_item oli
  join orders o on o.order_id = oli.order_id
  Join Purchase_Orders Po On Po.Purchase_Orders_Id = O.Purchase_Order_Id
  join facility f on f.facility_id = o.o_facility_id and f.facility_type_bits = 64 --only look for stores
Where
  Oli.last_updated_dttm >= Trunc(Sysdate) --only view lines actioned today
  And O.Dsg_Ship_Via <> 'BOPS' --remove bopis from this %
  And O.Do_Status >= 190 --DOs are shipped or canceled
  and exists --makes sure we are only tracking the parent line
     (select null from purchase_orders_line_item poli
        where oli.mo_line_item_id = poli.purchase_orders_line_item_id
        and poli.parent_po_line_item_id is null
      )
Group By
f.facility_type_bits
--order by
--  trunc(po.purchase_orders_date_dttm,'DD')