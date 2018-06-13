select /*+ parallel */
  drift_type,
  partner,
  count(distinct facility_alias_id) as facility_count,
  view_name,
  count(*) as row_count
from (
  /* ALL_STH */
  select
    inv.facility_alias_id,
    inv.item_name,
    inv.partner,
    'ALL_STH' as view_name,
    case
      when (
        inv.last_updated_dttm > (sysdate - 15/60/24) -- 15 minutes
      ) then 'Not Analyzed'
      when (
        inv.abi_is_excluded = 1 and not (
          inv.is_outage = 'Y'
          or (
            inv.partner = 'OCE'
            and inv.commerce_attribute_1 = 'N'
          )
        )
      ) then 'Exclusion Mismatch'
      when (
        inv.abi_is_excluded = 0 and (
          inv.is_outage = 'Y'
          or (
            inv.partner = 'OCE'
            and inv.commerce_attribute_1 = 'N'
          )
        )
      ) then 'Inclusion Mismatch'
      when (
        inv.abi_is_excluded = 0
        and inv.partner in ('VDC', 'OCE')
        and inv.inv_protection_1 != coalesce(inv.abi_protection_quantity, 0)
      ) then 'Watermark Mismatch'
      when (
        inv.abi_is_excluded = 0
        and inv.quantity <> greatest(coalesce(inv.abi_quantity, 0), 0)
      ) then 'Quantity Mismatch'
      when (
        inv.abi_is_excluded = 0
        and inv.allocated_quantity <> coalesce(inv.abi_allocated_quantity, 0)
      ) then 'Allocated Mismatch'
      else 'No Drift'
    end as drift_type
  from (
    select /*+ no_unnest */
      ip.inventory_id,
      ip.item_id,
      i.item_name,
      ip.facility_id,
      fa.facility_alias_id,
      case
        when f.facility_type_bits = 1 then 'VDC'
        when f.facility_type_bits = 64 then 'OCE'
        when fa.facility_alias_id = '00843' then 'WM'
        when fa.facility_alias_id in ('00821', '00822') then 'RAD'
        else 'UNKNOWN'
      end as partner,
      greatest(
        coalesce(ip.last_updated_dttm,ip.created_dttm),
        coalesce(alloc.last_updated_dttm,alloc.created_dttm,sysdate-1),
        coalesce(sl.last_updated_dttm,sl.created_dttm,sysdate-1)
      ) as last_updated_dttm,
      coalesce(sl.commerce_attribute_1, 'N') as commerce_attribute_1,
      coalesce(sl.inv_protection_1, 0) as inv_protection_1,
      ( greatest(coalesce(ip.available_quantity, 0), 0) + greatest(coalesce(ip.available_soon_quantity, 0), 0) ) as quantity,
      coalesce(alloc.allocated_quantity, 0) as allocated_quantity,
      coalesce(outage1.is_outage,outage2.is_outage,outage3.is_outage,'N') as is_outage,
      abi.is_excluded as abi_is_excluded,
      greatest(coalesce(abi.quantity, 0), 0) as abi_quantity,
      coalesce(abi.allocated_quantity, 0) as abi_allocated_quantity,
      coalesce(abi.protection_quantity, 0) as abi_protection_quantity
    from i_avail_by_inv_6 abi
    join i_perpetual ip on ip.inventory_id = abi.perpetual_inv_id
    join item_cbo i on i.item_id = ip.item_id
    join facility f on f.facility_id = ip.facility_id
    join facility_alias fa on fa.facility_id = ip.facility_id
    left outer join sku_location sl on ip.item_id = sl.sku_id and ip.facility_id = sl.destination_facility_id
    left outer join i_allocation alloc on alloc.inventory_id = ip.inventory_id
    left outer join (
      -- Item_Facility Outages
      select /*+ no_unnest */ fa.facility_id, i.item_id, 'Y' as is_outage
      from (
        select /*+ no_merge */
          (',' || fdf.filter_value1 || ',') as facility_filter,
          (',' || fdi.filter_value1 || ',') as item_filter
        from i_ful_outage_rule fo
        join filter_detail fdf on fdf.filter_id = fo.facility_filter_id and fdf.filter_field like 'FACILITY.%' and fdf.filter_operator = '='
        join filter_detail fdi on fdi.filter_id = fo.item_filter_id and fdi.filter_field = 'ITEM_CBO.ITEM_ID' and fdi.filter_operator = '='
        where fo.status = 16899
      ) t1
      join item_cbo i on t1.item_filter like ('%,' || to_char(i.item_id) || ',%')
      join facility_alias fa on t1.facility_filter like ('%,' || to_char(fa.facility_id) || ',%')
    ) outage1 on outage1.facility_id = ip.facility_id and outage1.item_id = ip.item_id
    left outer join (
      -- Item Outages
      select /*+ no_unnest */ i.item_id, 'Y' as is_outage
      from (
        select /*+ no_merge */
          (',' || fdi.filter_value1 || ',') as item_filter
        from i_ful_outage_rule fo
        join filter_detail fdi on fdi.filter_id = fo.item_filter_id and fdi.filter_field = 'ITEM_CBO.ITEM_ID' and fdi.filter_operator = '='
        where fo.facility_filter_id is null
        and fo.status = 16899
      ) t1
      join item_cbo i on t1.item_filter like ('%,' || to_char(i.item_id) || ',%')
    ) outage2 on outage2.item_id = ip.item_id
    left outer join (
      -- Facility Outages
      select /*+ no_unnest */ fa.facility_id, 'Y' as is_outage
      from (
        select /*+ no_merge */
          (',' || fdf.filter_value1 || ',') as facility_filter
        from i_ful_outage_rule fo
        join filter_detail fdf on fdf.filter_id = fo.facility_filter_id and fdf.filter_field like 'FACILITY.%' and fdf.filter_operator = '='
        where fo.item_filter_id is null
        and fo.status = 16899
      ) t1
      join facility_alias fa on t1.facility_filter like ('%,' || to_char(fa.facility_id) || ',%')
    ) outage3 on outage3.facility_id = ip.facility_id
    where abi.is_deleted = 0 and ip.error_code is null and ip.is_deleted = 0 and ip.object_id is null
  ) inv
  union all
  /* ALL_STH - ABI to IA */
  select
    null as facility_alias_id,
    item_name,
    'N/A' as partner,
    'ALL_STH' as view_name,
    'ABI to IA' as drift_type
  from (
    select /*+ pq_distribute(ia hash hash) pq_distribute(i hash hash) */
      i.item_name,
      abi.avail_ref_id,
      coalesce(ia.atc_quantity, 0) as atc_quantity,
      sum(
        greatest(
          (
            abi.quantity
            - coalesce(abi.protection_quantity, 0)
            - coalesce(abi.allocated_quantity, 0)
          ),
          0
        )
      ) as abi_quantity
    from i_avail_by_inv_6 abi
    join i_perpetual ip on ip.inventory_id = abi.perpetual_inv_id
    join item_cbo i on i.item_id = ip.item_id
    left outer join i_availability_6 ia on ia.avail_ref_id = abi.avail_ref_id and ia.is_deleted = 0
    where abi.is_deleted = 0 and abi.is_excluded = 0
    group by i.item_name, abi.avail_ref_id, coalesce(ia.atc_quantity, 0)
  )
  where atc_quantity <> abi_quantity
  union all
  /* BOPIS */
  select
    inv.facility_alias_id,
    inv.item_name,
    inv.partner,
    'BOPIS' as view_name,
    case
      when (
        inv.last_updated_dttm > (sysdate - 15/60/24) -- 15 minutes
      ) then 'Not Analyzed'
      when (
        inv.abi_is_excluded = 1 and not inv.is_outage = 'Y'
      ) then 'Exclusion Mismatch'
      when (
        inv.abi_is_excluded = 0 and inv.is_outage = 'Y'
      ) then 'Inclusion Mismatch'
      when (
        inv.abi_is_excluded = 0
        and inv.inv_protection_2 != coalesce(inv.abi_protection_quantity, 0)
      ) then 'Watermark Mismatch'
      when (
        inv.abi_is_excluded = 0
        and inv.quantity <> greatest(coalesce(inv.abi_quantity, 0), 0)
      ) then 'Quantity Mismatch'
      when (
        inv.abi_is_excluded = 0
        and inv.allocated_quantity <> coalesce(inv.abi_allocated_quantity, 0)
      ) then 'Allocated Mismatch'
      else 'No Drift'
    end as drift_type
  from (
    select /*+ no_unnest */
      ip.inventory_id,
      ip.item_id,
      i.item_name,
      ip.facility_id,
      fa.facility_alias_id,
      case
        when f.facility_type_bits = 1 then 'VDC'
        when f.facility_type_bits = 64 then 'OCE'
        when fa.facility_alias_id = '00843' then 'WM'
        when fa.facility_alias_id in ('00821', '00822') then 'RAD'
        else 'UNKNOWN'
      end as partner,
      greatest(
        coalesce(ip.last_updated_dttm,ip.created_dttm),
        coalesce(alloc.last_updated_dttm,alloc.created_dttm,sysdate-1),
        coalesce(sl.last_updated_dttm,sl.created_dttm,sysdate-1)
      ) as last_updated_dttm,
      coalesce(sl.commerce_attribute_1, 'N') as commerce_attribute_1,
      coalesce(sl.inv_protection_2, 0) as inv_protection_2,
      ( greatest(coalesce(ip.available_quantity, 0), 0) + greatest(coalesce(ip.available_soon_quantity, 0), 0) ) as quantity,
      coalesce(alloc.allocated_quantity, 0) as allocated_quantity,
      coalesce(outage1.is_outage,outage2.is_outage,outage3.is_outage,'N') as is_outage,
      abi.is_excluded as abi_is_excluded,
      greatest(coalesce(abi.quantity, 0), 0) as abi_quantity,
      coalesce(abi.allocated_quantity, 0) as abi_allocated_quantity,
      coalesce(abi.protection_quantity, 0) as abi_protection_quantity
    from i_avail_by_inv_4 abi
    join i_perpetual ip on ip.inventory_id = abi.perpetual_inv_id
    join item_cbo i on i.item_id = ip.item_id
    join facility f on f.facility_id = ip.facility_id
    join facility_alias fa on fa.facility_id = ip.facility_id
    left outer join sku_location sl on ip.item_id = sl.sku_id and ip.facility_id = sl.destination_facility_id
    left outer join i_allocation alloc on alloc.inventory_id = ip.inventory_id
    left outer join (
      -- Item_Facility Outages
      select /*+ no_unnest */ fa.facility_id, i.item_id, 'Y' as is_outage
      from (
        select /*+ no_merge */
          (',' || fdf.filter_value1 || ',') as facility_filter,
          (',' || fdi.filter_value1 || ',') as item_filter
        from i_ful_outage_rule fo
        join filter_detail fdf on fdf.filter_id = fo.facility_filter_id and fdf.filter_field like 'FACILITY.%' and fdf.filter_operator = '='
        join filter_detail fdi on fdi.filter_id = fo.item_filter_id and fdi.filter_field = 'ITEM_CBO.ITEM_ID' and fdi.filter_operator = '='
        where fo.status = 16899
      ) t1
      join item_cbo i on t1.item_filter like ('%,' || to_char(i.item_id) || ',%')
      join facility_alias fa on t1.facility_filter like ('%,' || to_char(fa.facility_id) || ',%')
    ) outage1 on outage1.facility_id = ip.facility_id and outage1.item_id = ip.item_id
    left outer join (
      -- Item Outages
      select /*+ no_unnest */ i.item_id, 'Y' as is_outage
      from (
        select /*+ no_merge */
          (',' || fdi.filter_value1 || ',') as item_filter
        from i_ful_outage_rule fo
        join filter_detail fdi on fdi.filter_id = fo.item_filter_id and fdi.filter_field = 'ITEM_CBO.ITEM_ID' and fdi.filter_operator = '='
        where fo.facility_filter_id is null
        and fo.status = 16899
      ) t1
      join item_cbo i on t1.item_filter like ('%,' || to_char(i.item_id) || ',%')
    ) outage2 on outage2.item_id = ip.item_id
    left outer join (
      -- Facility Outages
      select /*+ no_unnest */ fa.facility_id, 'Y' as is_outage
      from (
        select /*+ no_merge */
          (',' || fdf.filter_value1 || ',') as facility_filter
        from i_ful_outage_rule fo
        join filter_detail fdf on fdf.filter_id = fo.facility_filter_id and fdf.filter_field like 'FACILITY.%' and fdf.filter_operator = '='
        where fo.item_filter_id is null
        and fo.status = 16899
      ) t1
      join facility_alias fa on t1.facility_filter like ('%,' || to_char(fa.facility_id) || ',%')
    ) outage3 on outage3.facility_id = ip.facility_id
    where abi.is_deleted = 0 and ip.error_code is null and ip.is_deleted = 0 and ip.object_id is null
  ) inv
  union all
  /* BOPIS - ABI to IA */
  select
    null as facility_alias_id,
    item_name,
    'N/A' as partner,
    'BOPIS' as view_name,
    'ABI to IA' as drift_type
  from (
    select /*+ pq_distribute(ia hash hash) pq_distribute(i hash hash) */
      i.item_name,
      abi.avail_ref_id,
      coalesce(ia.atc_quantity, 0) as atc_quantity,
      sum(
        greatest(
          (
            abi.quantity
            - coalesce(abi.protection_quantity, 0)
            - coalesce(abi.allocated_quantity, 0)
          ),
          0
        )
      ) as abi_quantity
    from i_avail_by_inv_4 abi
    join i_perpetual ip on ip.inventory_id = abi.perpetual_inv_id
    join item_cbo i on i.item_id = ip.item_id
    left outer join i_availability_4 ia on ia.avail_ref_id = abi.avail_ref_id and ia.is_deleted = 0
    where abi.is_deleted = 0 and abi.is_excluded = 0
    group by i.item_name, abi.avail_ref_id, coalesce(ia.atc_quantity, 0)
  )
  where atc_quantity <> abi_quantity
)
group by drift_type, partner, view_name
order by case drift_type when 'No Drift' then 'Z' when 'Not Analyzed' then 'Y' else drift_type end, partner, view_name desc
 