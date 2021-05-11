SELECT
    f_table_name AS table_name,
    f_geography_column AS column_name,
    type AS data_type
    FROM geography_columns
WHERE f_table_name IN (<tableNames>)
-- "f_table_catalog","f_table_schema","f_table_name","f_geography_column","coord_dimension","srid","type"
-- "arlas","public","myindex","mygeo",2,4326,"Point"