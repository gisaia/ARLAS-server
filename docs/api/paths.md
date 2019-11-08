
<a name="paths"></a>
## Resources

<a name="collections_resource"></a>
### Collections

<a name="getall_1"></a>
#### Get all collection references
```
GET /collections
```


##### Description
Get all collection references in ARLAS


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|< [CollectionReference](#collectionreference) > array|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="exportcollections_1"></a>
#### Get all collection references as a json file
```
GET /collections/_export
```


##### Description
Get all collection references in ARLAS as json file


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|< [CollectionReference](#collectionreference) > array|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="importcollections_1"></a>
#### Add collection references from a json file
```
POST /collections/_import
```


##### Description
Add collection references in ARLAS from a json file


##### Parameters

|Type|Name|Schema|
|---|---|---|
|**FormData**|**file**  <br>*optional*|file|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|string|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `multipart/form-data`


##### Produces

* `application/json;charset=utf-8`


<a name="get_1"></a>
#### Get a collection reference
```
GET /collections/{collection}
```


##### Description
Get a collection reference in ARLAS


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[CollectionReference](#collectionreference)|
|**404**|Collection not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="put_1"></a>
#### Add a collection reference
```
PUT /collections/{collection}
```


##### Description
Add a collection reference in ARLAS


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**collectionParams**  <br>*required*|collectionParams|[CollectionReferenceParameters](#collectionreferenceparameters)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[CollectionReference](#collectionreference)|
|**400**|JSON parameter malformed.|[Error](#error)|
|**404**|Not Found Error.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="delete_1"></a>
#### Delete a collection reference
```
DELETE /collections/{collection}
```


##### Description
Delete a collection reference in ARLAS


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[Success](#success)|
|**404**|Collection not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="explore_resource"></a>
### Explore

<a name="list"></a>
#### List
```
GET /explore/_list
```


##### Description
List the collections configured in ARLAS.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[CollectionReferenceDescription](#collectionreferencedescription)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="opensearch"></a>
#### OpenSearch Description Document
```
GET /explore/ogc/opensearch/{collection}
```


##### Description
Access to the OpenSearch Description document for the given collection


##### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string|
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|No Content|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Produces

* `application/xml`


<a name="suggest"></a>
#### Suggest
```
GET /explore/{collections}/_suggest
```


##### Description
Suggest the the n (n=size) most relevant terms given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collections**  <br>*required*|collections, comma separated|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided. The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator   \|                   Description                      \| value type<br> <br>       :          \|  {fieldName} equals {value}                        \| numeric or strings <br> <br>       :gte:      \|  {fieldName} is greater than or equal to  {value}  \| numeric <br> <br>       :gt:       \|  {fieldName} is greater than {value}               \| numeric <br> <br>       :lte:      \|  {fieldName} is less than or equal to {value}      \| numeric <br> <br>       :lt:       \|  {fieldName}  is less than {value}                 \| numeric <br> <br><br> <br>- The AND operator is applied between filters having different fieldNames. <br> <br>- The OR operator is applied on filters having the same fieldName. <br> <br>- If the fieldName starts with - then a must not filter is used<br> <br>- If the fieldName starts with - then a must not filter is used<br> <br>For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md|< string > array(multi)||
|**Query**|**field**  <br>*optional*|Name of the field to be used for retrieving the most relevant terms|string|`"_all"`|
|**Query**|**from**  <br>*optional*|From index to start the search from. Defaults to 0.|integer (int32)|`0`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given geometry (WKT)|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given geometry (WKT)|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given geometry (WKT)|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry (WKT)|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given geometry (WKT)|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given geometry (WKT)|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search|string||
|**Query**|**size**  <br>*optional*|The maximum number of entries or sub-entries to be returned. The default value is 10|integer (int32)|`10`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|No Content|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="aggregatepost"></a>
#### Aggregate
```
POST /explore/{collection}/_aggregate
```


##### Description
Aggregate the elements in the collection(s), given the filters and the aggregation parameters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[AggregationsRequest](#aggregationsrequest)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[AggregationResponse](#aggregationresponse)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="aggregate"></a>
#### Aggregate
```
GET /explore/{collection}/_aggregate
```


##### Description
Aggregate the elements in the collection(s), given the filters and the aggregation parameters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**agg**  <br>*required*|- The agg parameter should be given in the following formats:  <br> <br>       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size} <br> <br>Where :<br> <br>   - **{type}:{field}** part is mandatory. <br> <br>   - **interval** must be specified only when aggregation type is datehistogram, histogram and geohash.<br> <br>   - **format** is optional for datehistogram, and must not be specified for the other types.<br> <br>   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types. <br> <br>   - It's possible to apply multiple metric aggregations by defining multiple (**collect_field**,**collect_fct**) couples.<br> <br>   - (**collect_field**,**collect_fct**) couples should be unique in that case.<br> <br>   - (**order**,**on**) couple is optional for all aggregation types.<br> <br>   - **size** is optional for term and geohash, and must not be specified for the other types.<br> <br>   - **include** is optional for term, and must not be specified for the other types.<br> <br>- {type} possible values are : <br> <br>       datehistogram, histogram, geohash and term. <br> <br>- {interval} possible values depends on {type}. <br> <br>       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. <br> <br>       If {type} = histogram, then {interval} = {size}. <br> <br>       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. <br> <br>       If {type} = term, then interval-{interval} is not needed. <br> <br>- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss.<br> <br>- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. <br> <br>  {collect_fct} possible values are : <br> <br>       avg,cardinality,max,min,sum<br> <br>- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. Its values are 'asc' or 'desc'. <br> <br>- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. <br> <br>- If {on} is equal to `result` and two or more (**collect_field**,**collect_fct**) couples are specified, then the order is applied on the first `collect_fct` that is different from geobbox and geocentroid<br> <br>- {size} Defines how many buckets should be returned. <br> <br>- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created.<br> <br>- **fetch_geometry**<br> <br>    > **What it does**: Specifies the strategy of fetching a geometry in each aggregation bucket.<br> <br>    > __**Syntax**__: `fetch_geometry` \|\| `fetch_geometry-{strategy}` \|\| `fetch_geometry-{field}-(first\|\|last)`.<br> <br>    > **fetch_geometry** or **fetch_geometry-byDefault**: the fetched geometry is the centroid of the geohash for `geohash` aggregation or a random geometry for the rest of aggregation types.<br> <br>    > **fetch_geometry-centroid**: the fetched geometry is the centroid of data inside each bucket.<br> <br>    > **fetch_geometry-bbox**: the fetched geometry is the data extend (bbox) in each bucket.<br> <br>    > **fetch_geometry-geohash**: the fetched geometry is the 'geohash' extend of each bucket. This strategy is supported for **geohash** aggregation type only.<br> <br>    > **fetch_geometry-first**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)<br> <br>    > **fetch_geometry-last**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)<br> <br>    > **fetch_geometry-{field}-first**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket.<br> <br>    > **fetch_geometry-{field}-last**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket.<br> <br>    > **Note 1**: if **fetch_geometry** is specified, the returned geometry is set int the 'geometry' attribute of the json response.<br> <br>    > **Note 2**: If **fetch_geometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned in 'geometry' attribute of the json response but not in the metrics. Same for **fetch_geometry-bbox** and **collect_fct**=`geobbox`<br> <br>- **fetchHits** <br> <br>    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits.<br> <br>    > __**Syntax**__: `fetchHits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`.<br> <br>    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default.<br> <br>    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters.<br> <br>    > __**Example**__: `fetchHits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket.<br> <br>**agg** parameter is multiple. Every agg parameter specified is a subaggregation of the previous one : order matters. <br> <br>For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md.|< string > array(multi)||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[AggregationResponse](#aggregationresponse)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="countpost"></a>
#### Count
```
POST /explore/{collection}/_count
```


##### Description
Count the number of elements found in the collection(s), given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collections|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[Count](#count)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[Hits](#hits)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="count"></a>
#### Count
```
GET /explore/{collection}/_count
```


##### Description
Count the number of elements found in the collection(s), given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collections|string||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[Hits](#hits)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="describe"></a>
#### Describe
```
GET /explore/{collection}/_describe
```


##### Description
Describe the structure and the content of the given collection.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[CollectionReferenceDescription](#collectionreferencedescription)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="geoaggregatepost"></a>
#### GeoAggregate
```
POST /explore/{collection}/_geoaggregate
```


##### Description
Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[AggregationsRequest](#aggregationsrequest)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[FeatureCollection](#featurecollection)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|
|**501**|Not implemented functionality.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="geoaggregate"></a>
#### GeoAggregate
```
GET /explore/{collection}/_geoaggregate
```


##### Description
Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**agg**  <br>*required*|- The agg parameter should be given in the following formats:  <br> <br>       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}:fetcbGeometry-{fetch_geometry values}<br> <br>Where :<br> <br>   - **{type}:{field}** part is mandatory. <br> <br>   - **interval** must be specified only when aggregation type is datehistogram, histogram and geohash.<br> <br>   - **format** is optional for datehistogram, and must not be specified for the other types.<br> <br>   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types.<br> <br>   - (**order**,**on**) couple is optional for all aggregation types.<br> <br>   - **size** is optional for term and geohash, and must not be specified for the other types.<br> <br>   - **include** is optional for term, and must not be specified for the other types.<br> <br>- {type} possible values are : <br> <br>       geohash, datehistogram, histogram and term. geohash must be the main aggregation.<br> <br>- {interval} possible values depends on {type}. <br> <br>       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. <br> <br>       If {type} = histogram, then {interval} = {size}. <br> <br>       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. <br> <br>       If {type} = term, then interval-{interval} is not needed. <br> <br>- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss.<br> <br>- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. <br> <br>  {collect_fct} possible values are : <br> <br>       avg,cardinality,max,min,sum,geobbox,geocentroid<br> <br>- (collect_field,collect_fct) should both be specified, except when collect_fct = `geobbox` or `geocentroid`, it could be specified alone. The metrics `geobbox` and `geocentroid` are returned as features collections.<br> <br>- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. Its values are 'asc' or 'desc'. <br> <br>- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. <br> <br>- When {on} = `result`, then (collect_field,collect_fct) should be specified. Except when {collect_fct} = `geobbox` or `geocentroid`, then {on}=`result` is prohibited<br> <br>- {size} Defines how many buckets should be returned. <br> <br>- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created.<br> <br>- **fetch_geometry**<br> <br>    > **What it does**: Specifies the strategy of fetching a geometry in each aggregation bucket.<br> <br>    > __**Syntax**__: `fetch_geometry` \|\| `fetch_geometry-{strategy}` \|\| `fetch_geometry-{field}-(first\|\|last)`.<br> <br>    > **fetch_geometry** or **fetch_geometry-byDefault**: the fetched geometry is the centroid of the geohash for `geohash` aggregation or a random geometry for the rest of aggregation types.<br> <br>    > **fetch_geometry-centroid**: the fetched geometry is the centroid of data inside each bucket.<br> <br>    > **fetch_geometry-bbox**: the fetched geometry is the data extend (bbox) in each bucket.<br> <br>    > **fetch_geometry-geohash**: the fetched geometry is the 'geohash' extend of each bucket. This strategy is supported for **geohash** aggregation type only.<br> <br>    > **fetch_geometry-first**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)<br> <br>    > **fetch_geometry-last**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)<br> <br>    > **fetch_geometry-{field}-first**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket.<br> <br>    > **fetch_geometry-{field}-last**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket.<br> <br>    > **Note 1**: if **fetch_geometry** is specified, the returned geometry is set int the 'geometry' attribute of the geojson.<br> <br>    > **Note 2**: If **fetch_geometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned in the geojson 'geometry' attribute but not in the metrics. Same for **fetch_geometry-bbox** and **collect_fct**=`geobbox`<br> <br>- **fetch_hits** <br> <br>    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits.<br> <br>    > __**Syntax**__: `fetch_hits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`.<br> <br>    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default.<br> <br>    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters.<br> <br>    > __**Example**__: `fetch_hits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket.<br> <br>**agg** parameter is multiple. The first (main) aggregation must be geohash. Every agg parameter specified is a subaggregation of the previous one : order matters. <br> <br>For more details, check https://github.com/gisaia/ARLAS-server/blob/master/docs/arlas-api-exploration.md|< string > array(multi)||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[FeatureCollection](#featurecollection)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|
|**501**|Not implemented functionality.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="geohashgeoaggregate"></a>
#### GeoAggregate on a geohash
```
GET /explore/{collection}/_geoaggregate/{geohash}
```


##### Description
Aggregate the elements in the collection(s) and localized in the given geohash as features, given the filters and the aggregation parameters.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Path**|**geohash**  <br>*required*|geohash|string||
|**Query**|**agg**  <br>*optional*|- The agg parameter should be given in the following formats:  <br> <br>       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}:fetcbGeometry-{fetch_geometry values}<br> <br>Where :<br> <br>   - **{type}:{field}** part is mandatory. <br> <br>   - **interval** must be specified only when aggregation type is datehistogram, histogram and geohash.<br> <br>   - **format** is optional for datehistogram, and must not be specified for the other types.<br> <br>   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types.<br> <br>   - (**order**,**on**) couple is optional for all aggregation types.<br> <br>   - **size** is optional for term and geohash, and must not be specified for the other types.<br> <br>   - **include** is optional for term, and must not be specified for the other types.<br> <br>- {type} possible values are : <br> <br>       geohash, datehistogram, histogram and term. geohash must be the main aggregation.<br> <br>- {interval} possible values depends on {type}. <br> <br>       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. <br> <br>       If {type} = histogram, then {interval} = {size}. <br> <br>       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. <br> <br>       If {type} = term, then interval-{interval} is not needed. <br> <br>- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss.<br> <br>- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. <br> <br>  {collect_fct} possible values are : <br> <br>       avg,cardinality,max,min,sum,geobbox,geocentroid<br> <br>- (collect_field,collect_fct) should both be specified, except when collect_fct = `geobbox` or `geocentroid`, it could be specified alone. The metrics `geobbox` and `geocentroid` are returned as features collections.<br> <br>- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. Its values are 'asc' or 'desc'. <br> <br>- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. <br> <br>- When {on} = `result`, then (collect_field,collect_fct) should be specified. Except when {collect_fct} = `geobbox` or `geocentroid`, then {on}=`result` is prohibited<br> <br>- {size} Defines how many buckets should be returned. <br> <br>- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created.<br> <br>- **fetch_geometry**<br> <br>    > **What it does**: Specifies the strategy of fetching a geometry in each aggregation bucket.<br> <br>    > __**Syntax**__: `fetch_geometry` \|\| `fetch_geometry-{strategy}` \|\| `fetch_geometry-{field}-(first\|\|last)`.<br> <br>    > **fetch_geometry** or **fetch_geometry-byDefault**: the fetched geometry is the centroid of the geohash for `geohash` aggregation or a random geometry for the rest of aggregation types.<br> <br>    > **fetch_geometry-centroid**: the fetched geometry is the centroid of data inside each bucket.<br> <br>    > **fetch_geometry-bbox**: the fetched geometry is the data extend (bbox) in each bucket.<br> <br>    > **fetch_geometry-geohash**: the fetched geometry is the 'geohash' extend of each bucket. This strategy is supported for **geohash** aggregation type only.<br> <br>    > **fetch_geometry-first**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)<br> <br>    > **fetch_geometry-last**: the fetched geometry is the first hit's geometry fetched in each bucket (chronologically)<br> <br>    > **fetch_geometry-{field}-first**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket.<br> <br>    > **fetch_geometry-{field}-last**: the fetched geometry is the geometry of the first hit - ordered by the {field} - fetched in each bucket.<br> <br>    > **Note 1**: if **fetch_geometry** is specified, the returned geometry is set int the 'geometry' attribute of the geojson.<br> <br>    > **Note 2**: If **fetch_geometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned in the geojson 'geometry' attribute but not in the metrics. Same for **fetch_geometry-bbox** and **collect_fct**=`geobbox`<br> <br>- **fetch_hits** <br> <br>    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits.<br> <br>    > __**Syntax**__: `fetch_hits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`.<br> <br>    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default.<br> <br>    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters.<br> <br>    > __**Example**__: `fetch_hits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket.<br> <br>**agg** parameter is multiple. The first (main) aggregation must be geohash. Every agg parameter specified is a subaggregation of the previous one : order matters. <br> <br>For more details, check https://github.com/gisaia/ARLAS-server/blob/master/docs/arlas-api-exploration.md|< string > array(multi)||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[FeatureCollection](#featurecollection)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|
|**501**|Not implemented functionality.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="geosearchpost"></a>
#### GeoSearch
```
POST /explore/{collection}/_geosearch
```


##### Description
Search and return the elements found in the collection(s) as features, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[Search](#search)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[FeatureCollection](#featurecollection)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="geosearch"></a>
#### GeoSearch
```
GET /explore/{collection}/_geosearch
```


##### Description
Search and return the elements found in the collection(s) as features, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**after**  <br>*optional*|List of values of fields present in sort param that are used to search after. <br> <br>> **What it does**: Retrieve the data placed after the pointed element, given the provided order (sort).<br> <br>> __**Restriction 1**__: **after** param works only combined with **sort** param.<br> <br>> __**Syntax**__: `after={value1},{value2},...,{valueN} & sort={field1},{field2},...,{fieldN}`.<br> <br>> **Note 1**: *{value1}` and `{value2}` are the values of `{field1}` and `{field2}` in the last hit returned in the previous search<br> <br>> **Note 2**: The order of fields and values matters. *{value1},{value2}* must be in the same order of *{field1},{field2}* in **sort** param<br> <br>> **Note 3**:  The last field `{fieldN}` must be the id field specified in the collection **collection.params.idPath** (returned as **md.id**) and `{valueN}` its corresponding value.<br> <br>> __**Example**__: *sort=`-date,id` & **after**=`01/02/2019,abcd1234`*. Gets the following hits of the previous search that stopped at date *01/02/2019* and id *abcd1234*.<br> <br>> __**Restriction 2**__: **from** param must be set to 0 or kept unset|string||
|**Query**|**before**  <br>*optional*|Same idea that after param, but to retrieve the data placed before the pointed element, given the provided order (sort).|string||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**exclude**  <br>*optional*|List the name patterns of the field to be excluded in the result. Separate patterns with a comma.|< string > array(multi)||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**from**  <br>*optional*|From index to start the search from. Defaults to 0.|integer|`0`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**include**  <br>*optional*|List the name patterns of the field to be included in the result. Separate patterns with a comma.|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||
|**Query**|**size**  <br>*optional*|The maximum number of entries or sub-entries to be returned. The default value is 10|integer|`10`|
|**Query**|**sort**  <br>*optional*|Sorts the resulted hits on the given fields and/or by distance to a given point:<br> <br>> __**Syntax**__: `{field1},{field2},-{field3},geodistance:{lat} {lon},{field4}  ...`.<br> <br>> **Note 1**: `{field}` can be preceded by **'-'**  for **descending** sort. By default, sort is ascending.<br> <br>> **Note 2**: The order of fields matters.<br> <br>> **Note 3** ***geodistance sort***: Sorts the hits centroids by distance to the given **{lat} {lon}** (ascending distance sort). It can be specified at most 1 time.<br> <br>> __**Example 1**__: sort=`age,-timestamp`. Resulted hits are sorted by age. For same age hits, they are decreasingly sorted in time.<br> <br>> __**Example 2**__: sort=`age,geodistance:89 179`. Resulted hits are sorted by age. For same age hits, they are sorted by closest distance to the point(89°,179°)|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[FeatureCollection](#featurecollection)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="tiledgeosearch"></a>
#### Tiled GeoSearch
```
GET /explore/{collection}/_geosearch/{z}/{x}/{y}
```


##### Description
Search and return the elements found in the collection(s) and localized in the given tile(x,y,z) as features, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Path**|**x**  <br>*required*|x|integer (int32)||
|**Path**|**y**  <br>*required*|y|integer (int32)||
|**Path**|**z**  <br>*required*|z|integer (int32)||
|**Query**|**after**  <br>*optional*|List of values of fields present in sort param that are used to search after. <br> <br>> **What it does**: Retrieve the data placed after the pointed element, given the provided order (sort).<br> <br>> __**Restriction 1**__: **after** param works only combined with **sort** param.<br> <br>> __**Syntax**__: `after={value1},{value2},...,{valueN} & sort={field1},{field2},...,{fieldN}`.<br> <br>> **Note 1**: *{value1}` and `{value2}` are the values of `{field1}` and `{field2}` in the last hit returned in the previous search<br> <br>> **Note 2**: The order of fields and values matters. *{value1},{value2}* must be in the same order of *{field1},{field2}* in **sort** param<br> <br>> **Note 3**:  The last field `{fieldN}` must be the id field specified in the collection **collection.params.idPath** (returned as **md.id**) and `{valueN}` its corresponding value.<br> <br>> __**Example**__: *sort=`-date,id` & **after**=`01/02/2019,abcd1234`*. Gets the following hits of the previous search that stopped at date *01/02/2019* and id *abcd1234*.<br> <br>> __**Restriction 2**__: **from** param must be set to 0 or kept unset|string||
|**Query**|**before**  <br>*optional*|Same idea that after param, but to retrieve the data placed before the pointed element, given the provided order (sort).|string||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**exclude**  <br>*optional*|List the name patterns of the field to be excluded in the result. Separate patterns with a comma.|< string > array(multi)||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**from**  <br>*optional*|From index to start the search from. Defaults to 0.|integer|`0`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**include**  <br>*optional*|List the name patterns of the field to be included in the result. Separate patterns with a comma.|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||
|**Query**|**size**  <br>*optional*|The maximum number of entries or sub-entries to be returned. The default value is 10|integer|`10`|
|**Query**|**sort**  <br>*optional*|Sorts the resulted hits on the given fields and/or by distance to a given point:<br> <br>> __**Syntax**__: `{field1},{field2},-{field3},geodistance:{lat} {lon},{field4}  ...`.<br> <br>> **Note 1**: `{field}` can be preceded by **'-'**  for **descending** sort. By default, sort is ascending.<br> <br>> **Note 2**: The order of fields matters.<br> <br>> **Note 3** ***geodistance sort***: Sorts the hits centroids by distance to the given **{lat} {lon}** (ascending distance sort). It can be specified at most 1 time.<br> <br>> __**Example 1**__: sort=`age,-timestamp`. Resulted hits are sorted by age. For same age hits, they are decreasingly sorted in time.<br> <br>> __**Example 2**__: sort=`age,geodistance:89 179`. Resulted hits are sorted by age. For same age hits, they are sorted by closest distance to the point(89°,179°)|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[FeatureCollection](#featurecollection)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="rangepost"></a>
#### Aggregate
```
POST /explore/{collection}/_range
```


##### Description
Calculates the min and max values of a field in the collection, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[RangeRequest](#rangerequest)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[RangeResponse](#rangeresponse)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="range"></a>
#### RangeRequest
```
GET /explore/{collection}/_range
```


##### Description
Calculates the min and max values of a field in the collection, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**field**  <br>*required*|The field whose range is calculated|string||
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[RangeResponse](#rangeresponse)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="searchpost"></a>
#### Search
```
POST /explore/{collection}/_search
```


##### Description
Search and return the elements found in the collection, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[Search](#search)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[Hits](#hits)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="search"></a>
#### Search
```
GET /explore/{collection}/_search
```


##### Description
Search and return the elements found in the collection, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**after**  <br>*optional*|List of values of fields present in sort param that are used to search after. <br> <br>> **What it does**: Retrieve the data placed after the pointed element, given the provided order (sort).<br> <br>> __**Restriction 1**__: **after** param works only combined with **sort** param.<br> <br>> __**Syntax**__: `after={value1},{value2},...,{valueN} & sort={field1},{field2},...,{fieldN}`.<br> <br>> **Note 1**: *{value1}` and `{value2}` are the values of `{field1}` and `{field2}` in the last hit returned in the previous search<br> <br>> **Note 2**: The order of fields and values matters. *{value1},{value2}* must be in the same order of *{field1},{field2}* in **sort** param<br> <br>> **Note 3**:  The last field `{fieldN}` must be the id field specified in the collection **collection.params.idPath** (returned as **md.id**) and `{valueN}` its corresponding value.<br> <br>> __**Example**__: *sort=`-date,id` & **after**=`01/02/2019,abcd1234`*. Gets the following hits of the previous search that stopped at date *01/02/2019* and id *abcd1234*.<br> <br>> __**Restriction 2**__: **from** param must be set to 0 or kept unset|string||
|**Query**|**before**  <br>*optional*|Same idea that after param, but to retrieve the data placed before the pointed element, given the provided order (sort).|string||
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**exclude**  <br>*optional*|List the name patterns of the field to be excluded in the result. Separate patterns with a comma.|< string > array(multi)||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**from**  <br>*optional*|From index to start the search from. Defaults to 0.|integer|`0`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**include**  <br>*optional*|List the name patterns of the field to be included in the result. Separate patterns with a comma.|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||
|**Query**|**size**  <br>*optional*|The maximum number of entries or sub-entries to be returned. The default value is 10|integer|`10`|
|**Query**|**sort**  <br>*optional*|Sorts the resulted hits on the given fields and/or by distance to a given point:<br> <br>> __**Syntax**__: `{field1},{field2},-{field3},geodistance:{lat} {lon},{field4}  ...`.<br> <br>> **Note 1**: `{field}` can be preceded by **'-'**  for **descending** sort. By default, sort is ascending.<br> <br>> **Note 2**: The order of fields matters.<br> <br>> **Note 3** ***geodistance sort***: Sorts the hits centroids by distance to the given **{lat} {lon}** (ascending distance sort). It can be specified at most 1 time.<br> <br>> __**Example 1**__: sort=`age,-timestamp`. Resulted hits are sorted by age. For same age hits, they are decreasingly sorted in time.<br> <br>> __**Example 2**__: sort=`age,geodistance:89 179`. Resulted hits are sorted by age. For same age hits, they are sorted by closest distance to the point(89°,179°)|string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[Hits](#hits)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`
* `application/atom+xml`


<a name="tiledgeosearch_1"></a>
#### Tiled GeoSearch
```
GET /explore/{collection}/_tile/{z}/{x}/{y}.png
```


##### Description
Search and return the elements found in the collection(s) and localized in the given tile(x,y,z) as features, given the filters


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Path**|**x**  <br>*required*|x|integer (int32)||
|**Path**|**y**  <br>*required*|y|integer (int32)||
|**Path**|**z**  <br>*required*|z|integer (int32)||
|**Query**|**after**  <br>*optional*|List of values of fields present in sort param that are used to search after. <br> <br>> **What it does**: Retrieve the data placed after the pointed element, given the provided order (sort).<br> <br>> __**Restriction 1**__: **after** param works only combined with **sort** param.<br> <br>> __**Syntax**__: `after={value1},{value2},...,{valueN} & sort={field1},{field2},...,{fieldN}`.<br> <br>> **Note 1**: *{value1}` and `{value2}` are the values of `{field1}` and `{field2}` in the last hit returned in the previous search<br> <br>> **Note 2**: The order of fields and values matters. *{value1},{value2}* must be in the same order of *{field1},{field2}* in **sort** param<br> <br>> **Note 3**:  The last field `{fieldN}` must be the id field specified in the collection **collection.params.idPath** (returned as **md.id**) and `{valueN}` its corresponding value.<br> <br>> __**Example**__: *sort=`-date,id` & **after**=`01/02/2019,abcd1234`*. Gets the following hits of the previous search that stopped at date *01/02/2019* and id *abcd1234*.<br> <br>> __**Restriction 2**__: **from** param must be set to 0 or kept unset|string||
|**Query**|**before**  <br>*optional*|Same idea that after param, but to retrieve the data placed before the pointed element, given the provided order (sort).|string||
|**Query**|**coverage**  <br>*optional*|Percentage (]0-100]) of acceptable transparent pixels. Higher the percentage, more tiles could be used for filling the tile|integer (int32)|`70`|
|**Query**|**dateformat**  <br>*optional*|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|string||
|**Query**|**f**  <br>*optional*|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. <br> <br>- A triplet is composed of a field name, a comparison operator and a value. <br> <br>  The possible values of the comparison operator are : <br> <br>       Operator \|                   Description                    \| value type<br> <br>       :eq:     \| {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values \| numeric or strings <br> <br>       :ne:     \| {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values \| numeric or strings <br> <br>       :like:   \| {fieldName}  is like {value}                     \| numeric or strings <br> <br>       :gte:    \| {fieldName} is greater than or equal to  {value} \| numeric <br> <br>       :gt:     \| {fieldName} is greater than {value}              \| numeric <br> <br>       :lte:    \| {fieldName} is less than or equal to {value}     \| numeric <br> <br>       :lt:     \| {fieldName}  is less than {value}                \| numeric <br> <br>       :range:  \| {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges \| numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression|< string > array(multi)||
|**Query**|**from**  <br>*optional*|From index to start the search from. Defaults to 0.|integer|`0`|
|**Query**|**gintersect**  <br>*optional*|Any element having its geometry intersecting the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**gwithin**  <br>*optional*|Any element having its geometry contained within the given WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**notgintersect**  <br>*optional*|Any element having its geometry not intersecting the given WKT string (clock-wise) nor the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notgwithin**  <br>*optional*|Any element having its geometry outside the given geometry WKT string (clock-wise) or the given BBOX : `west, south, east, north`|< string > array(multi)||
|**Query**|**notpwithin**  <br>*optional*|Any element having its centroid outside the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**pwithin**  <br>*optional*|Any element having its centroid contained within the given WKT Polygon or MultiPolygon (clock-wise) or the given BBOX : `west, south, east, north`.|< string > array(multi)||
|**Query**|**q**  <br>*optional*|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|< string > array(multi)||
|**Query**|**sampling**  <br>*optional*|Size of the sampling for testing transparency: 1: test every pixel, 10: test 1 pixel every 10 pixels, etc.|integer (int32)|`10`|
|**Query**|**size**  <br>*optional*|The maximum number of entries or sub-entries to be returned. The default value is 10|integer|`10`|
|**Query**|**sort**  <br>*optional*|Sorts the resulted hits on the given fields and/or by distance to a given point:<br> <br>> __**Syntax**__: `{field1},{field2},-{field3},geodistance:{lat} {lon},{field4}  ...`.<br> <br>> **Note 1**: `{field}` can be preceded by **'-'**  for **descending** sort. By default, sort is ascending.<br> <br>> **Note 2**: The order of fields matters.<br> <br>> **Note 3** ***geodistance sort***: Sorts the hits centroids by distance to the given **{lat} {lon}** (ascending distance sort). It can be specified at most 1 time.<br> <br>> __**Example 1**__: sort=`age,-timestamp`. Resulted hits are sorted by age. For same age hits, they are decreasingly sorted in time.<br> <br>> __**Example 2**__: sort=`age,geodistance:89 179`. Resulted hits are sorted by age. For same age hits, they are sorted by closest distance to the point(89°,179°)|< string > array(multi)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|No Content|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `image/png`


<a name="getarlashit"></a>
#### Get an Arlas document
```
GET /explore/{collection}/{identifier}
```


##### Description
Returns a raw indexed document.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Path**|**identifier**  <br>*required*|identifier|string||
|**Query**|**flat**  <br>*optional*|Flats the property map: only key/value on one level|boolean|`"false"`|
|**Query**|**max-age-cache**  <br>*optional*|max-age-cache|integer (int32)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[Hit](#hit)|
|**400**|Bad request.|[Error](#error)|
|**404**|Not Found Error.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="write_resource"></a>
### Write

<a name="tagpost"></a>
#### Tag
```
POST /write/{collection}/_tag
```

Caution : 
operation.deprecated


##### Description
Search and tag the elements found in the collection, given the filters (Deprecated)


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[TagRequest](#tagrequest)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[UpdateResponse](#updateresponse)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="untagpost"></a>
#### Untag
```
POST /write/{collection}/_untag
```

Caution : 
operation.deprecated


##### Description
Search and untag the elements found in the collection, given the filters (Deprecated)


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**collection**  <br>*required*|collection|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**body**  <br>*optional*||[TagRequest](#tagrequest)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[UpdateResponse](#updateresponse)|
|**400**|Bad request.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`



