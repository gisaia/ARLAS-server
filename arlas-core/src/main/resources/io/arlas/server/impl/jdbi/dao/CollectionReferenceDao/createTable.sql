CREATE TABLE IF NOT EXISTS collection_reference (
    date_inserted TIMESTAMP DEFAULT NOW(),
    arlas_index VARCHAR(255) NOT NULL,
    collection_name VARCHAR(255) NOT NULL,
    index_name VARCHAR(255) NOT NULL,
    description JSON NOT NULL
);
CREATE INDEX IF NOT EXISTS arlas_index ON collection_reference (arlas_index);
CREATE UNIQUE INDEX IF NOT EXISTS collection_name ON collection_reference (arlas_index, collection_name);

CREATE OR REPLACE FUNCTION lon2Xtile(lon DOUBLE PRECISION, zoom INTEGER)
  RETURNS INTEGER AS
$BODY$
    SELECT
   		CASE
            WHEN x < 0 THEN 0
            WHEN x >= (1 << zoom) THEN ((1 << zoom)-1)
            ELSE x
		  END from (select CASE
		                    WHEN lon = 180 THEN ((1 << zoom)-1)
		                    ELSE FLOOR( (lon + 180) / 360 * (1 << zoom) )::INTEGER
		                   END AS x) t;
$BODY$
  LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION lat2Ytile(lat double precision, zoom integer)
  RETURNS integer AS
$BODY$
    SELECT
   		CASE
            WHEN y < 0 THEN 0
            WHEN y >= (1 << zoom) THEN ((1 << zoom)-1)
            ELSE y
		  END from (select CASE
		                    WHEN lat = -90 THEN ((1 << zoom)-1)
		                    ELSE floor((1.0 - ln(tan(radians(lat)) + 1.0 / cos(radians(lat))) / pi()) / 2.0 * (1 << zoom) )::integer
		                   END AS y) t;
$BODY$
  LANGUAGE sql IMMUTABLE;