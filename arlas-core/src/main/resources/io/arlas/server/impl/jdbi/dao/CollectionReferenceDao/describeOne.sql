SELECT
    table_name,
    column_name,
    CASE
        WHEN (data_type = 'USER-DEFINED') THEN udt_name
	    WHEN (data_type LIKE 'timestamp %') THEN 'timestamp'
	    WHEN (data_type LIKE 'time %') THEN 'time'
	    WHEN (data_type LIKE 'varchar%') THEN 'varchar'
	    WHEN (data_type LIKE 'character var%') THEN 'varchar'
	    WHEN (data_type like 'double%') then 'double'
        ELSE data_type
    END AS data_type
FROM INFORMATION_SCHEMA.COLUMNS
WHERE table_name = ?;
