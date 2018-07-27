SELECT /*+ parallel */
--po.purchase_orders_date_dttm
--,SYSDATE
--,tc_purchase_orders_id
trunc (NVL(AVG(cast(sysdate AS DATE) - cast(po.purchase_orders_date_dttm AS DATE))*24*60,0), 2) AS calc
FROM
purchase_orders po 
WHERE
po.created_dttm >= trunc(sysdate) --Date filter
AND EXISTS ( 
                     SELECT NULL 
                     FROM purchase_orders_line_item poli 
                     WHERE poli.purchase_orders_id = po.purchase_orders_id
                     AND poli.dsg_ship_via = 'BOPS')    --Only looks for BOPIS order
AND not EXISTS (
                     SELECT NULL
                     FROM orders o 
                     WHERE o.purchase_order_id = po.purchase_orders_id
                     ) --doesn't exist in the ORDERS table, i.e. not yet DO created
AND po.IS_PURCHASE_ORDERS_CONFIRMED = 1 --is a confirmed order
AND po.purchase_orders_status < 492 --less than DO Create status
---need to add hold flag
 