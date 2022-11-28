
<a name="definitions"></a>
## Definitions

<a name="aggregation"></a>
### Aggregation

|Name|Schema|
|---|---|
|**aggregated_geometries**  <br>*optional*|< enum (BBOX, CENTROID, CELL, CELLCENTER, GEOHASH, GEOHASH_CENTER) > array|
|**fetch_hits**  <br>*optional*|[HitsFetcher](#hitsfetcher)|
|**field**  <br>*optional*|string|
|**format**  <br>*optional*|string|
|**include**  <br>*optional*|string|
|**interval**  <br>*optional*|[Interval](#interval)|
|**metrics**  <br>*optional*|< [Metric](#metric) > array|
|**on**  <br>*optional*|enum (field, count, result)|
|**order**  <br>*optional*|enum (asc, desc)|
|**raw_geometries**  <br>*optional*|< [RawGeometry](#rawgeometry) > array|
|**size**  <br>*optional*|string|
|**type**  <br>*optional*|enum (datehistogram, geohash, geotile, histogram, term, h3)|


<a name="aggregationmetric"></a>
### AggregationMetric

|Name|Schema|
|---|---|
|**field**  <br>*optional*|string|
|**type**  <br>*optional*|string|
|**value**  <br>*optional*|object|


<a name="aggregationresponse"></a>
### AggregationResponse

|Name|Schema|
|---|---|
|**count**  <br>*optional*|integer (int64)|
|**elements**  <br>*optional*|< [AggregationResponse](#aggregationresponse) > array|
|**flattened_elements**  <br>*optional*|< string, object > map|
|**geometries**  <br>*optional*|< [ReturnedGeometry](#returnedgeometry) > array|
|**hits**  <br>*optional*|< object > array|
|**key**  <br>*optional*|object|
|**key_as_string**  <br>*optional*|object|
|**metrics**  <br>*optional*|< [AggregationMetric](#aggregationmetric) > array|
|**name**  <br>*optional*|string|
|**query_time**  <br>*optional*|integer (int64)|
|**sumotherdoccounts**  <br>*optional*|integer (int64)|
|**total_time**  <br>*optional*|integer (int64)|
|**totalnb**  <br>*optional*|integer (int64)|


<a name="aggregationsrequest"></a>
### AggregationsRequest

|Name|Schema|
|---|---|
|**aggregations**  <br>*optional*|< [Aggregation](#aggregation) > array|
|**filter**  <br>*optional*|[Filter](#filter)|
|**form**  <br>*optional*|[Form](#form)|


<a name="bbox"></a>
### Bbox

|Name|Schema|
|---|---|
|**east**  <br>*required*|number (double)|
|**north**  <br>*required*|number (double)|
|**south**  <br>*required*|number (double)|
|**west**  <br>*required*|number (double)|


<a name="collectiondisplaynames"></a>
### CollectionDisplayNames

|Name|Schema|
|---|---|
|**collection**  <br>*optional*|string|
|**fields**  <br>*optional*|< string, string > map|
|**shape_columns**  <br>*optional*|< string, string > map|


<a name="collectionreference"></a>
### CollectionReference

|Name|Schema|
|---|---|
|**collection_name**  <br>*required*|string|
|**params**  <br>*required*|[CollectionReferenceParameters](#collectionreferenceparameters)|


<a name="collectionreferencedescription"></a>
### CollectionReferenceDescription

|Name|Schema|
|---|---|
|**collection_name**  <br>*required*|string|
|**params**  <br>*required*|[CollectionReferenceParameters](#collectionreferenceparameters)|
|**properties**  <br>*optional*|< string, [CollectionReferenceDescriptionProperty](#collectionreferencedescriptionproperty) > map|


<a name="collectionreferencedescriptionproperty"></a>
### CollectionReferenceDescriptionProperty

|Name|Schema|
|---|---|
|**format**  <br>*optional*|string|
|**indexed**  <br>*optional*|boolean|
|**properties**  <br>*optional*|< string, [CollectionReferenceDescriptionProperty](#collectionreferencedescriptionproperty) > map|
|**taggable**  <br>*optional*|boolean|
|**type**  <br>*optional*|enum (TEXT, KEYWORD, LONG, INTEGER, SHORT, BYTE, DOUBLE, FLOAT, DATE, BOOLEAN, BINARY, INT_RANGE, FLOAT_RANGE, LONG_RANGE, DOUBLE_RANGE, DATE_RANGE, OBJECT, NESTED, GEO_POINT, GEO_SHAPE, IP, COMPLETION, TOKEN_COUNT, MAPPER_MURMUR3, UNKNOWN, VARCHAR, CHAR, CHARACTER, BIT, TINYINT, SMALLINT, INT, BIGINT, DECIMAL, NUMERIC, REAL, DOUBLEPRECISION, TIMESTAMP, TIME, INTERVAL, GEOMETRY, GEOGRAPHY, POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, GEOMETRYCOLLECTION)|


<a name="collectionreferenceparameters"></a>
### CollectionReferenceParameters

|Name|Schema|
|---|---|
|**atom_feed**  <br>*optional*|[Feed](#feed)|
|**centroid_path**  <br>*required*|string|
|**custom_params**  <br>*optional*|< string, string > map|
|**display_names**  <br>*optional*|[CollectionDisplayNames](#collectiondisplaynames)|
|**dublin_core_element_name**  <br>*optional*|[DublinCoreElementName](#dublincoreelementname)|
|**exclude_fields**  <br>*optional*|string|
|**exclude_wfs_fields**  <br>*optional*|string|
|**filter**  <br>*optional*|[Filter](#filter)|
|**geometry_path**  <br>*required*|string|
|**h3_path**  <br>*optional*|string|
|**id_path**  <br>*required*|string|
|**index_name**  <br>*required*|string|
|**inspire**  <br>*optional*|[Inspire](#inspire)|
|**license_name**  <br>*optional*|string|
|**license_urls**  <br>*optional*|< string > array|
|**open_search**  <br>*optional*|[OpenSearch](#opensearch)|
|**raster_tile_height**  <br>*optional*|integer (int32)|
|**raster_tile_url**  <br>*optional*|[RasterTileURL](#rastertileurl)|
|**raster_tile_width**  <br>*optional*|integer (int32)|
|**taggable_fields**  <br>*optional*|string|
|**timestamp_path**  <br>*required*|string|
|**update_max_hits**  <br>*optional*|integer (int32)|


<a name="computationrequest"></a>
### ComputationRequest

|Name|Schema|
|---|---|
|**field**  <br>*optional*|string|
|**filter**  <br>*optional*|[Filter](#filter)|
|**form**  <br>*optional*|[Form](#form)|
|**metric**  <br>*optional*|enum (AVG, MAX, MIN, SUM, CARDINALITY, SPANNING, GEOBBOX, GEOCENTROID)|


<a name="computationresponse"></a>
### ComputationResponse

|Name|Schema|
|---|---|
|**field**  <br>*optional*|string|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**metric**  <br>*optional*|enum (AVG, MAX, MIN, SUM, CARDINALITY, SPANNING, GEOBBOX, GEOCENTROID)|
|**query_time**  <br>*optional*|integer (int64)|
|**total_time**  <br>*optional*|integer (int64)|
|**totalnb**  <br>*optional*|integer (int64)|
|**value**  <br>*optional*|number (double)|


<a name="count"></a>
### Count

|Name|Schema|
|---|---|
|**filter**  <br>*optional*|[Filter](#filter)|
|**form**  <br>*optional*|[Form](#form)|


<a name="crs"></a>
### Crs

|Name|Schema|
|---|---|
|**properties**  <br>*optional*|< string, object > map|
|**type**  <br>*optional*|enum (name, link)|


<a name="dublincoreelementname"></a>
### DublinCoreElementName

|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|[Bbox](#bbox)|
|**contributor**  <br>*optional*|string|
|**coverage**  <br>*optional*|< string, object > map|
|**coverage_centroid**  <br>*optional*|string|
|**creator**  <br>*optional*|string|
|**date**  <br>*optional*|string|
|**description**  <br>*optional*|string|
|**format**  <br>*optional*|string|
|**identifier**  <br>*optional*|string|
|**language**  <br>*optional*|string|
|**publisher**  <br>*optional*|string|
|**source**  <br>*optional*|string|
|**subject**  <br>*optional*|string|
|**title**  <br>*optional*|string|
|**type**  <br>*optional*|string|


<a name="error"></a>
### Error

|Name|Schema|
|---|---|
|**error**  <br>*optional*|string|
|**message**  <br>*optional*|string|
|**status**  <br>*optional*|integer (int32)|


<a name="expression"></a>
### Expression

|Name|Schema|
|---|---|
|**field**  <br>*optional*|string|
|**op**  <br>*optional*|enum (eq, gte, gt, lte, lt, like, ne, range, within, notwithin, intersects, notintersects)|
|**value**  <br>*optional*|string|


<a name="feature"></a>
### Feature

|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**crs**  <br>*optional*|[Crs](#crs)|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**id**  <br>*optional*|string|
|**properties**  <br>*optional*|< string, object > map|


<a name="featurecollection"></a>
### FeatureCollection

|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**crs**  <br>*optional*|[Crs](#crs)|
|**features**  <br>*optional*|< [Feature](#feature) > array|


<a name="feed"></a>
### Feed

|Name|Schema|
|---|---|
|**author**  <br>*optional*|[Person](#person)|
|**contributor**  <br>*optional*|[Person](#person)|
|**generator**  <br>*optional*|[Generator](#generator)|
|**icon**  <br>*optional*|string|
|**logo**  <br>*optional*|string|
|**rights**  <br>*optional*|string|
|**subtitle**  <br>*optional*|string|


<a name="filter"></a>
### Filter

|Name|Schema|
|---|---|
|**dateformat**  <br>*optional*|string|
|**f**  <br>*optional*|< < [Expression](#expression) > array > array|
|**q**  <br>*optional*|< < string > array > array|
|**righthand**  <br>*optional*|boolean|


<a name="form"></a>
### Form

|Name|Schema|
|---|---|
|**flat**  <br>*optional*|boolean|
|**pretty**  <br>*optional*|boolean|


<a name="generator"></a>
### Generator

|Name|Schema|
|---|---|
|**name**  <br>*optional*|string|
|**uri**  <br>*optional*|string|
|**version**  <br>*optional*|string|


<a name="geo"></a>
### Geo

|Name|Schema|
|---|---|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**path**  <br>*optional*|string|


<a name="geojsonobject"></a>
### GeoJsonObject

|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="geometrycollection"></a>
### GeometryCollection
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**crs**  <br>*optional*|[Crs](#crs)|
|**geometries**  <br>*optional*|< [GeoJsonObject](#geojsonobject) > array|


<a name="hit"></a>
### Hit

|Name|Schema|
|---|---|
|**data**  <br>*optional*|object|
|**md**  <br>*optional*|[MD](#md)|


<a name="hits"></a>
### Hits

|Name|Schema|
|---|---|
|**collection**  <br>*optional*|string|
|**hits**  <br>*optional*|< [Hit](#hit) > array|
|**links**  <br>*optional*|< string, [Link](#link) > map|
|**nbhits**  <br>*optional*|integer (int64)|
|**totalnb**  <br>*optional*|integer (int64)|


<a name="hitsfetcher"></a>
### HitsFetcher

|Name|Schema|
|---|---|
|**include**  <br>*optional*|< string > array|
|**size**  <br>*optional*|integer (int32)|


<a name="inspire"></a>
### Inspire

|Name|Schema|
|---|---|
|**inspire_limitation_access**  <br>*optional*|[InspireLimitationAccess](#inspirelimitationaccess)|
|**inspire_uri**  <br>*optional*|[InspireURI](#inspireuri)|
|**inspire_use_conditions**  <br>*optional*|string|
|**keywords**  <br>*optional*|< [Keyword](#keyword) > array|
|**languages**  <br>*optional*|< string > array|
|**lineage**  <br>*optional*|string|
|**spatial_resolution**  <br>*optional*|[InspireSpatialResolution](#inspirespatialresolution)|
|**topic_categories**  <br>*optional*|< string > array|


<a name="inspirelimitationaccess"></a>
### InspireLimitationAccess

|Name|Schema|
|---|---|
|**access_constraints**  <br>*optional*|string|
|**classification**  <br>*optional*|string|
|**other_constraints**  <br>*optional*|string|


<a name="inspirespatialresolution"></a>
### InspireSpatialResolution

|Name|Schema|
|---|---|
|**unit_of_measure**  <br>*optional*|string|
|**value**  <br>*optional*|[Number](#number)|


<a name="inspireuri"></a>
### InspireURI

|Name|Schema|
|---|---|
|**code**  <br>*optional*|string|
|**namespace**  <br>*optional*|string|


<a name="interval"></a>
### Interval

|Name|Schema|
|---|---|
|**unit**  <br>*optional*|enum (year, quarter, month, week, day, hour, minute, second)|
|**value**  <br>*optional*|[Number](#number)|


<a name="keyword"></a>
### Keyword

|Name|Schema|
|---|---|
|**date_of_publication**  <br>*optional*|string|
|**value**  <br>*optional*|string|
|**vocabulary**  <br>*optional*|string|


<a name="linestring"></a>
### LineString
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**coordinates**  <br>*optional*|< [LngLatAlt](#lnglatalt) > array|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="link"></a>
### Link

|Name|Schema|
|---|---|
|**body**  <br>*optional*|object|
|**href**  <br>*required*|string|
|**method**  <br>*required*|string|


<a name="lnglatalt"></a>
### LngLatAlt

|Name|Schema|
|---|---|
|**additionalElements**  <br>*optional*|< number (double) > array|
|**altitude**  <br>*optional*|number (double)|
|**latitude**  <br>*optional*|number (double)|
|**longitude**  <br>*optional*|number (double)|


<a name="md"></a>
### MD

|Name|Schema|
|---|---|
|**centroid**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**id**  <br>*optional*|string|
|**returned_geometries**  <br>*optional*|< [Geo](#geo) > array|
|**timestamp**  <br>*optional*|integer (int64)|


<a name="metric"></a>
### Metric

|Name|Schema|
|---|---|
|**collect_fct**  <br>*optional*|enum (AVG, CARDINALITY, MAX, MIN, SUM, GEOCENTROID, GEOBBOX)|
|**collect_field**  <br>*optional*|string|


<a name="multilinestring"></a>
### MultiLineString
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**coordinates**  <br>*optional*|< < [LngLatAlt](#lnglatalt) > array > array|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="multipoint"></a>
### MultiPoint
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**coordinates**  <br>*optional*|< [LngLatAlt](#lnglatalt) > array|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="multipolygon"></a>
### MultiPolygon
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**coordinates**  <br>*optional*|< < < [LngLatAlt](#lnglatalt) > array > array > array|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="number"></a>
### Number
*Type* : object


<a name="opensearch"></a>
### OpenSearch

|Name|Schema|
|---|---|
|**adult_content**  <br>*optional*|string|
|**attribution**  <br>*optional*|string|
|**contact**  <br>*optional*|string|
|**description**  <br>*optional*|string|
|**developer**  <br>*optional*|string|
|**image_height**  <br>*optional*|string|
|**image_type**  <br>*optional*|string|
|**image_url**  <br>*optional*|string|
|**image_width**  <br>*optional*|string|
|**input_encoding**  <br>*optional*|string|
|**language**  <br>*optional*|string|
|**long_name**  <br>*optional*|string|
|**output_encoding**  <br>*optional*|string|
|**short_name**  <br>*optional*|string|
|**syndication_right**  <br>*optional*|string|
|**tags**  <br>*optional*|string|


<a name="page"></a>
### Page

|Name|Schema|
|---|---|
|**after**  <br>*optional*|string|
|**before**  <br>*optional*|string|
|**from**  <br>*optional*|integer (int32)|
|**size**  <br>*optional*|integer (int32)|
|**sort**  <br>*optional*|string|


<a name="person"></a>
### Person

|Name|Schema|
|---|---|
|**email**  <br>*optional*|string|
|**name**  <br>*optional*|string|
|**uri**  <br>*optional*|string|


<a name="point"></a>
### Point
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**coordinates**  <br>*optional*|[LngLatAlt](#lnglatalt)|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="polygon"></a>
### Polygon
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**coordinates**  <br>*optional*|< < [LngLatAlt](#lnglatalt) > array > array|
|**crs**  <br>*optional*|[Crs](#crs)|


<a name="projection"></a>
### Projection

|Name|Schema|
|---|---|
|**excludes**  <br>*optional*|string|
|**includes**  <br>*optional*|string|


<a name="rastertileurl"></a>
### RasterTileURL

|Name|Schema|
|---|---|
|**check_geometry**  <br>*optional*|boolean|
|**id_path**  <br>*required*|string|
|**max_z**  <br>*optional*|integer (int32)|
|**min_z**  <br>*optional*|integer (int32)|
|**url**  <br>*required*|string|


<a name="rawgeometry"></a>
### RawGeometry

|Name|Schema|
|---|---|
|**geometry**  <br>*optional*|string|
|**sort**  <br>*optional*|string|


<a name="returnedgeometry"></a>
### ReturnedGeometry

|Name|Schema|
|---|---|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**is_raw**  <br>*optional*|boolean|
|**reference**  <br>*optional*|string|
|**sort**  <br>*optional*|string|


<a name="search"></a>
### Search

|Name|Schema|
|---|---|
|**filter**  <br>*optional*|[Filter](#filter)|
|**form**  <br>*optional*|[Form](#form)|
|**page**  <br>*optional*|[Page](#page)|
|**projection**  <br>*optional*|[Projection](#projection)|
|**returned_geometries**  <br>*optional*|string|


<a name="success"></a>
### Success

|Name|Schema|
|---|---|
|**message**  <br>*optional*|string|
|**status**  <br>*optional*|integer (int32)|



