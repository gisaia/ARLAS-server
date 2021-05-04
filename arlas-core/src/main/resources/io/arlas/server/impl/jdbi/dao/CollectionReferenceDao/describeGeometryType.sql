SELECT
    f_table_name AS table_name,
    f_geometry_column AS column_name,
--    type AS data_type
    CASE
        WHEN (type = 'POINT') THEN 'GEO_POINT'
        WHEN (type = 'MULTIPOLYGON') THEN 'GEO_SHAPE'
        ELSE type
    END AS data_type
FROM geometry_columns
WHERE f_table_name IN (<tableNames>)
-- "f_table_catalog","f_table_schema","f_table_name","f_geometry_column","coord_dimension","srid","type"
-- "arlas","public","myindex","mygeo",2,4326,"Point"