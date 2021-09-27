
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


<a name="collection"></a>
### Collection

|Name|Description|Schema|
|---|---|---|
|**assets**  <br>*optional*|This provides an optional mechanism to expose assets that don't make sense at the Item level.|< string, object > map|
|**crs**  <br>*optional*|List of crs describing the collection.|< string > array|
|**description**  <br>*required*|Detailed multi-line description to fully explain the catalog or collection. [CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.|string|
|**extent**  <br>*required*||[Extent](#extent)|
|**id**  <br>*required*|identifier of the collection used, for example, in URIs|string|
|**keywords**  <br>*optional*|List of keywords describing the collection.|< string > array|
|**license**  <br>*required*||string|
|**links**  <br>*required*||< [StacLink](#staclink) > array|
|**providers**  <br>*optional*||< [Provider](#provider) > array|
|**stac_extensions**  <br>*optional*||< string > array|
|**stac_version**  <br>*required*||string|
|**summaries**  <br>*optional*|Summaries are either a unique set of all available values *or* statistics. Statistics by default only specify the range (minimum and maximum values), but can optionally be accompanied by additional statistical values. The range can specify the potential range of values, but it is recommended to be as precise as possible. The set of values must contain at least one element and it is strongly recommended to list all values. It is recommended to list as many properties as reasonable so that consumers get a full overview of the Collection. Properties that are covered by the Collection specification (e.g. `providers` and `license`) may not be repeated in the summaries.|< string, object > map|
|**title**  <br>*optional*|human readable title of the collection|string|
|**type**  <br>*required*||string|


<a name="collectionlist"></a>
### CollectionList

|Name|Schema|
|---|---|
|**collections**  <br>*required*|< [Collection](#collection) > array|
|**links**  <br>*required*|< [StacLink](#staclink) > array|


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
|**dublin_core_element_name**  <br>*optional*|[DublinCoreElementName](#dublincoreelementname)|
|**exclude_fields**  <br>*optional*|string|
|**exclude_wfs_fields**  <br>*optional*|string|
|**filter**  <br>*optional*|[Filter](#filter)|
|**geometry_path**  <br>*required*|string|
|**h3_path**  <br>*optional*|string|
|**id_path**  <br>*required*|string|
|**index_name**  <br>*required*|string|
|**inspire**  <br>*optional*|[Inspire](#inspire)|
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


<a name="conformanceclasses"></a>
### ConformanceClasses

|Name|Description|Schema|
|---|---|---|
|**conformsTo**  <br>*required*|A list of all conformance classes implemented by the server. In addition to the STAC-specific conformance classes, all OGC-related conformance classes listed at `GET /conformances` must be listed here. This entry should mirror what `GET /conformances` returns, if implemented.|< string > array|


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


<a name="extent"></a>
### Extent

|Name|Schema|
|---|---|
|**spatial**  <br>*required*|[ExtentSpatial](#extentspatial)|
|**temporal**  <br>*required*|[ExtentTemporal](#extenttemporal)|


<a name="extentspatial"></a>
### ExtentSpatial

|Name|Description|Schema|
|---|---|---|
|**bbox**  <br>*required*|One or more bounding boxes that describe the spatial extent of the dataset.  The first bounding box describes the overall spatial extent of the data. All subsequent bounding boxes describe  more precise bounding boxes, e.g., to identify clusters of data. Clients only interested in the overall spatial extent will only need to access the first item in each array.|< < number (double) > array > array|
|**crs**  <br>*optional*|Coordinate reference system of the coordinates in the spatial extent (property `bbox`). The default reference system is WGS 84 longitude/latitude. In the Core this is the only supported coordinate reference system. Extensions may support additional coordinate reference systems and add additional enum values.|enum (HTTP_WWW_OPENGIS_NET_DEF_CRS_OGC_1_3_CRS84)|


<a name="extenttemporal"></a>
### ExtentTemporal

|Name|Description|Schema|
|---|---|---|
|**interval**  <br>*required*|One or more time intervals that describe the temporal extent of the dataset.  The first time interval describes the overall temporal extent of the data. All subsequent time intervals describe  more precise time intervals, e.g., to identify clusters of data. Clients only interested in the overall extent will only need to access the first item in each array.|< < string > array > array|
|**trs**  <br>*optional*|Coordinate reference system of the coordinates in the temporal extent (property `interval`). The default reference system is the Gregorian calendar. In the Core this is the only supported temporal reference system. Extensions may support additional temporal reference systems and add additional enum values.|enum (HTTP_WWW_OPENGIS_NET_DEF_UOM_ISO_8601_0_GREGORIAN)|


<a name="feature"></a>
### Feature
*Polymorphism* : Inheritance  
*Discriminator* : type


|Name|Schema|
|---|---|
|**bbox**  <br>*optional*|< number (double) > array|
|**crs**  <br>*optional*|[Crs](#crs)|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**id**  <br>*optional*|string|
|**properties**  <br>*optional*|< string, object > map|


<a name="featurecollection"></a>
### FeatureCollection
*Polymorphism* : Inheritance  
*Discriminator* : type


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


<a name="item"></a>
### Item

|Name|Schema|
|---|---|
|**assets**  <br>*required*|< string, object > map|
|**bbox**  <br>*optional*|< number (double) > array|
|**collection**  <br>*optional*|string|
|**crs**  <br>*optional*|[Crs](#crs)|
|**geometry**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**id**  <br>*optional*|string|
|**links**  <br>*required*|< [StacLink](#staclink) > array|
|**properties**  <br>*optional*|< string, object > map|
|**stac_extensions**  <br>*optional*|< string > array|
|**stac_version**  <br>*required*|string|
|**type**  <br>*required*|string|


<a name="keyword"></a>
### Keyword

|Name|Schema|
|---|---|
|**date_of_publication**  <br>*optional*|string|
|**value**  <br>*optional*|string|
|**vocabulary**  <br>*optional*|string|


<a name="landingpage"></a>
### LandingPage

|Name|Description|Schema|
|---|---|---|
|**conformsTo**  <br>*required*|A list of all conformance classes implemented by the server. In addition to the STAC-specific conformance classes, all OGC-related conformance classes listed at `GET /conformances` must be listed here. This entry should mirror what `GET /conformances` returns, if implemented.|< string > array|
|**description**  <br>*required*||string|
|**id**  <br>*required*||string|
|**links**  <br>*required*||< [StacLink](#staclink) > array|
|**stac_extensions**  <br>*optional*||< string > array|
|**stac_version**  <br>*required*||string|
|**title**  <br>*optional*||string|
|**type**  <br>*required*||string|


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
*Type* : object


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


<a name="provider"></a>
### Provider

|Name|Description|Schema|
|---|---|---|
|**description**  <br>*optional*|Multi-line description to add further provider information such as processing details for processors and producers, hosting details for hosts or basic contact information.  [CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.|string|
|**name**  <br>*required*|The name of the organization or the individual.|string|
|**roles**  <br>*optional*|Roles of the provider.  The provider's role(s) can be one or more of the following elements:  * licensor: The organization that is licensing the dataset under   the license specified in the collection's license field. * producer: The producer of the data is the provider that   initially captured and processed the source data, e.g. ESA for   Sentinel-2 data. * processor: A processor is any provider who processed data to a   derived product. * host: The host is the actual provider offering the data on their   storage. There should be no more than one host, specified as last   element of the list.|< enum (PRODUCER, LICENSOR, PROCESSOR, HOST) > array|
|**url**  <br>*optional*|Homepage on which the provider describes the dataset and publishes contact information.|string|


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


<a name="searchbody"></a>
### SearchBody

|Name|Schema|
|---|---|
|**after**  <br>*optional*|string|
|**bbox**  <br>*optional*|< number (double) > array|
|**before**  <br>*optional*|string|
|**collections**  <br>*optional*|< string > array|
|**datetime**  <br>*optional*|string|
|**from**  <br>*optional*|integer (int32)|
|**ids**  <br>*optional*|< string > array|
|**intersects**  <br>*optional*|[GeoJsonObject](#geojsonobject)|
|**limit**  <br>*optional*|integer (int32)|
|**sortby**  <br>*optional*|string|


<a name="stacfeaturecollection"></a>
### StacFeatureCollection

|Name|Description|Schema|
|---|---|---|
|**context**  <br>*optional*|Augments lists of resources with the number of returned and matches resource and the given limit for the request.|< string, object > map|
|**features**  <br>*required*||< [Item](#item) > array|
|**links**  <br>*optional*||< [StacLink](#staclink) > array|
|**numberMatched**  <br>*optional*||integer (int32)|
|**numberReturned**  <br>*optional*||integer (int32)|
|**stac_extensions**  <br>*optional*||< string > array|
|**stac_version**  <br>*required*||string|
|**timeStamp**  <br>*optional*||string|
|**type**  <br>*required*||string|


<a name="staclink"></a>
### StacLink

|Name|Schema|
|---|---|
|**body**  <br>*optional*|object|
|**headers**  <br>*optional*|< string, object > map|
|**href**  <br>*required*|string|
|**merge**  <br>*optional*|boolean|
|**method**  <br>*required*|string|
|**rel**  <br>*required*|string|
|**title**  <br>*optional*|string|
|**type**  <br>*optional*|string|


<a name="success"></a>
### Success

|Name|Schema|
|---|---|
|**message**  <br>*optional*|string|
|**status**  <br>*optional*|integer (int32)|



