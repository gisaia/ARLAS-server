select table_name,column_name,case
       when (data_type = 'USER-DEFINED') then udt_name
	   when (data_type like 'timestamp %') then 'timestamp'
	   when (data_type like 'time %') then 'time'
	   when (data_type like 'varchar%') then 'varchar'
	   when (data_type like 'character%') then 'character'
	   when (data_type like 'double%') then 'double'
       else data_type
    end as data_type
from INFORMATION_SCHEMA.COLUMNS where table_name in (select index_name from collection_reference where arlas_index = ?);