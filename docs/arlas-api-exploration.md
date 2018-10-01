# ARLAS Exploration API

The ARLAS API makes the ARLAS catalog available for exploration and browsing. The catalog contains collections of geo-referenced elements. Every element has a geometry, a centroid, a timestamp and a set of fields specific to the collection.

## URL Schema

The table below lists the URL endpoints and their optional "parts". A part is composed of optional parameters. The parameters are seperated with the character `&`.

| PATH Template                            | Description                              |
| ---------------------------------------- | ---------------------------------------- |
| /arlas/explore/**_list**                 | List  the collections configured in ARLAS |
| /arlas/explore/`{collection}`/**_describe**?`form` | Describe the structure and the content of the given collection |
| /arlas/explore/`{collection}`/**_count**?`filter` & `form` | Count the number of elements found in the collection, given the filters |
| /arlas/explore/`{collection}`/**_range**?`field` & `filter` & `form` | Calculates the min and max values of a field in the collection, given the filters |
| /arlas/explore/`{collection}`/**_search**?`filter` & `form` & `projection` & `size` & `sort` | Search and return the elements found in the collection, given the filters |
| /arlas/explore/`{collection}`/**_geosearch**?`filter` & `form` & `projection` & `size` & `sort` | Search and return the elements found in the collection as features, given the filters |
| /arlas/explore/`{collection}`/**_geosearch**/`{z}`/`{x}`/`{y}`?`filter` & `form` & `projection` & `size` & `sort` | Search and return the elements found in the collection and localized in the given tile(x,y,z) as features, given the filters |
| /arlas/explore/`{collections}`/**_aggregate**?`aggregation` &`filter` & `form` | Aggregate the elements in the collection(s), given the filters and the aggregation parameters |
| /arlas/explore/`{collections}`/**_geoaggregate**?`aggregation` &`filter` & `form` & `size` & `sort` | Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters |
| /arlas/explore/`{collections}`/**_geoaggregate**/`{geohash}`?`aggregation` &`filter` & `form` & `size` & `sort` | Aggregate the elements in the collection(s) and localized in the given `{geohash}` as features, given the filters and the aggregation parameters |
| /arlas/explore/`{collections}`/**_suggest**?`filter` & `form` & `size` & `suggest` | Suggest the the n (n=`size`) most relevant terms given the filters |

When multiple collections are permitted ({collections}), the comma is used for separating the collection names.

| Examples                                 |
| ---------------------------------------- |
| https://api.gisaia.com/demo/arlas/explore/`_describe` |
| https://api.gisaia.com/demo/arlas/explore/`city,state,country`/`_describe` |
| https://api.gisaia.com/demo/arlas/explore/`city,state,country`/`_count`?`q=bord*`&`f=country:France`&`pretty=true` |
| https://api.gisaia.com/demo/arlas/explore/`election`/`_search`?`f=country:France`&`f=$timestamp:range:[0<1490613808000]`&`pretty=true`&`size=1000`&`include=id,name` |
| https://api.gisaia.com/demo/arlas/explore/`election`/`_aggregate`?`f=country:France`&`f=$timestamp:range:[0<1490613808000]`&`pretty=true`&`size=1000`&`include=id,name`&`agg=geohash`&`agg_interval=4` |

All URLs are accessible both with GET and POST requests. For POST requests, URL parts are passed as a JSON representation.

## URL Parts

### Part: `aggregation`

The [`aggregation`] url part allows the following parameters to be specified:

| Parameter | Default value | Description                              | Multiple                 |
| --------- | ------------- | ---------------------------------------- | ------------------------ |
| **agg**   | `None`        | Gathers a set of sub-parameters indicating the type of aggregation, the field used as the aggregation key and possibly the interval for numeric values | true for _aggregate only |

The agg parameter should be given in the following format :

- {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}

Where the `{type}:{field}` part is mandatory

The other parts must be specified or not depending on the aggregation type. All the cases are sum up in the following table.

| Parameter                 | Aggregation type          | Description                 |
| ---------                 | -------------                       | ---------------------------------------- |
| **interval**              | `datehistogram, histogram, geohash` | mandatory |
| **format**                | `datehistogram`                     | optional (default value : `yyyy-MM-dd-HH:mm:ss`) |
| (**collect_field**,**collect_fct**) | All types                 | optional and multiple |
| (**order**,**on**)        | `term, histogram, datehistogram`    | optional |
| **size**                  | `term, geohash`                     | optional |
| **include**               | `term`                              | optional |



> Example: `agg=datehistogram:date:interval-20day:format-dd.MM.yyyy`&`agg=term:sexe:collect_field-age:collect_fct-avg:order-asc:on-result:size-5`

The sub-parameters possible values are:

| Parameter         | Values                                          | Description                              |
| ----------------- | ----------------------------------------------  | ---------------------------------------- |
| **{type}**        | `datehistogram`, `histogram`, `geohash`, `term` | Type of aggregation |
| **{field}**       | {field}                                         | Aggregates on {field} |
| **interval**      | {interval}                                      | Size of the intervals.(1)                   |
| **format**        | [Date format](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-daterange-aggregation.html#date-format-pattern) for key aggregation | Date format for key aggregation.         |
| **collect_field** | `{collect_field}`                               | The field used to aggregate collections. |
| **collect_fct**   | `avg,cardinality,max,min,sum,geobbox,geocentroid (2)` | The aggregation function to apply to collections on the specified **collect_field**. |
| **order**         | `asc,desc`                                      | Sorts the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation, ascending or descending. |
| **on**            | `field,count,result` (3) (3')                       | {on} is set to specify whether the **order** is on the field name, on the count of the aggregation or the result of a metric subaggregation. |
| **size**          | {size}                                          | Defines how many buckets should be returned. |
| **include**       | Comma separated strings (4)                     | Specifies the values for which buckets will be created. |
| **fetchGeometry**   | `bbox`, `centroid`, `byDefault`, `first`, `last` or nothing specified      | Specifies which geometry to fetch for each bucket (5)(6)|

(1) Each aggregation type ({type}) has its own type of interval. The table below lists the semantic of the interval sub-parameter.

(2) (**collect_field**,**collect_fct**) should both be specified.
It's possible to apply multiple metric aggregations by defining multiple (**collect_field**,**collect_fct**) couples.
They should be unique in that case.
The metrics `geobbox` and `geocentroid` are returned as features collections.

(3) When **on** is `result`, then (**collect_field**,**collect_fct**) should be specified. Except when **collect_fct** = `geobbox` or `geocentroid`, then **on**=`result` is prohibited .

(3') If **on** is equal to `result` and two ore more (**collect_field**,**collect_fct**) couples are specified, then the order is applied on the first `collect_fct` different from `geobbox` and `geobbox`".

> Example: `agg=term:sexe:collect_field-location:collect_fct-geobbox:collect_field-age:collect_fct-avg:collect_field-height:collect_fct-max:order-asc:on-result`

The `order` is applied on the first collect_fct `avg` (that is different from `geobbox`).


(4) If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created.

(5) **fetchGeometry** is to be specified for `geohash` and `term` aggregations only. If **fetchGeometry** is specified, the returned geometry depends on the value it takes :

 - If `fetchGeometry-bbox`, then the returned geometry is the extend of data in each bucket
 - If `fetchGeometry-centroid`, then the returned geometry is the centroid of data in each bucket
 - If `fetchGeometry` or `fetchGeometry-byDefault` , then the returned geometry is the centroid of the geohash
 - If `fetchGeometry-first`, then the returned geometry is the geometry of the first document in each bucket (chronogically)
 - If `fetchGeometry-last`, then the returned geometry is the geometry of the last document in each bucket (chronogically)
 - If `fetchGeometry-{field}-first`, then the returned geometry is the geometry of the first document in each bucket (ordered on the {field})
 - If `fetchGeometry-{field}-last`, then the returned geometry is the geometry of the last document in each bucket (ordered on the {field})

(6) If **fetchGeometry-centroid** and **collect_fct**=`geocentroid` are both set, the centroid of each bucket is only returned as the geo-aggregation geometry and not in the metrics. Same for **fetchGeometry-bbox** and **collect_fct**=`geobbox`

| Service             | Aggregation type    | Interval                                 | Description                              |
| ------------------- | ------------------- | ---------------------------------------- | ---------------------------------------- |
| ***_aggregate***    | ***datehistogram*** | `{size}(year,quarter,month,week,day,hour,minute,second)` | Size of a time interval with the given unit (no space between number and unit). Size must be equal to 1 for year, quarter, month and week |
| ***_geoaggregate*** | ***geohash***       | `{length}`                               | The geohash length: lower the length, greater is the surface of aggregation. See table below. |
| ***_aggregate***    | ***histogram***     | `{size}`                                 | The interval size of the numeric aggregation |
| ***_aggregate***    | ***term***          | None                                     | None                                     |

The table below shows the metric dimensions for cells covered by various string lengths of geohash. Cell dimensions vary with latitude and so the table is for the worst-case scenario at the equator.

| GeoHash length | Area width x height   |
| -------------- | --------------------- |
| 1              | 5,009.4km x 4,992.6km |
| 2              | 1,252.3km x 624.1km   |
| 3              | 156.5km x 156km       |
| 4              | 39.1km x 19.5km       |
| 5              | 4.9km x 4.9km         |
| 6              | 1.2km x 609.4m        |
| 7              | 152.9m x 152.4m       |
| 8              | 38.2m x 19m           |
| 9              | 4.8m x 4.8m           |
| 10             | 1.2m x 59.5cm         |
| 11             | 14.9cm x 14.9cm       |
| 12             | 3.7cm x 1.9cm         |

**agg** parameter is multiple. Every agg parameter specified is a subaggregation of the previous one : the order matters.

For **_geoaggregate** service, the first (main) aggregation must be geohash.

---
### Part: `filter`

#### Available filter parameters

The `filter` url part allows the following parameters to be specified:

| Parameter         | Default value | Values                         | Description                              | Multiple |
| ----------------- | ------------- | ------------------------------ | ---------------------------------------- | -------- |
| **f**             | None          | `{fieldName}{operator}{value}` | A triplet for filtering the result. Multiple filter can be provided. The order does not matter. A triplet is composed of a field name, a comparison operator and a value. The **AND** operator is applied between filters. For the **`:eq:`** and **`:range:`** filters, values can be comma separated (field`:eq:`v1,v2) which stands for an **OR**. For the **`:ne:`**  filter, values can be comma separated (field`:ne:`v1,v2) which stands for an **AND** | true     |
| **q**             | None          | `{text}` or `{fieldname}:{text}` | A full text search. Optionally, it's possible to search the text on a specific field                       | false    |
| **pwithin**       | None          | geometry                       | Any element having its centroid contained within the given BBOX : `west, south, east, north`| false    |
| **gwithin**       | None          | geometry                       | Any element having its geometry contained within the given geometry (WKT) or the given BBOX : `west, south, east, north` | false    |
| **gintersect**    | None          | geometry                       | Any element having its geometry intersecting the given geometry (WKT) or the given BBOX : `west, south, east, north` | false    |
| **notpwithin**    | None          | geometry                       | Any element having its centroid outside the given BBOX : `west, south, east, north` | false    |
| **notgwithin**    | None          | geometry                       | Any element having its geometry not contained within the given geometry (WKT) or the given BBOX : `west, south, east, north` | false    |
| **notgintersect** | None          | geometry                       | Any element having its geometry not intersecting the given geometry (WKT) or the given BBOX : `west, south, east, north` | false    |

#### Filter parameters algebra

For each parameter, you can provide multiple filters. There are 2 different ways to do this :
- in distinct filter parameters = **AND** operator between filters
- in the same filter parameter as a semi-colon separated list = **OR** operator between filters

| Multiple type              | Example                                                                           | Operator | Result                              |
| -------------------------- | --------------------------------------------------------------------------------- | -------- | ----------------------------------- |
| Distinct filter parameters | `gwithin=POLYGON((0 0,1 0,1 1,0 1,0 0))&gwithin=POLYGON((0 0,1 0,1 -1,0 -1,0 0))` | **AND**  | elements within the 2 geometry      |
| Same filter parameter      | `gwithin=POLYGON((0 0,1 0,1 1,0 1,0 0));POLYGON((0 0,1 0,1 -1,0 -1,0 0))`         | **OR**   | elements within at least 1 geometry |

Of course, you can combine both way to handle complex multiple filters for each filter parameter type.

#### `f` parameter syntax

| Operator      | Description                                                                                                         | Value type         |
| ------------- | ------------------------------------------------------------------------------------------------------------------- | ------------------ |
| **`:eq:`**    | `{fieldName}` equals `{comma separated values}`. **OR** operation is applied for the specified values               | numeric or strings |
| **`:ne:`**    | `{fieldName}` must not equal `{comma separated values }`. **AND** operation is applied for the specified values     | numeric or strings |
| **`:like:`**  | `{fieldName}` is like `{value}`.                                                                                    | numeric or strings |
| **`:gte:`**   | `{fieldName}` is greater than or equal to `{value}`                                                                 | numeric            |
| **`:gt:`**    | `{fieldName}` is greater than `{value}`                                                                             | numeric            |
| **`:lte:`**   | `{fieldName}` is less than or equal to `{value}`                                                                    | numeric            |
| **`:lt:`**    | `{fieldName}` is less than `{value}`                                                                                | numeric            |
| **`:range:`** | `{fieldName}` is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges | numeric or strings. If the field's type is date, min & max should be timestamps in millisecond or [Date expression](https://www.elastic.co/guide/en/elasticsearch/reference/current/common-options.html#date-math) |

The `:range:` operator has a specific syntax to indicates if range bounds are taken into account or not.

| Range operator syntax             | Meaning                    |
| --------------------------------- | -------------------------- |
| `x:range:]min<max[`               | min<x<max                  |
| `x:range:[min<max]`               | min<=x<=max                |
| `x:range:]min<max]`               | min<x<=max                 |
| `x:range:[min<max[`               | min<=x<max                 |
| `x:range:]min1<max1[,]min2<max2[` | min1<x<max1 OR min2<x<max2 |

On top of that, `:range:` operator supports generic aliases to represent collection configured fields :
* `$timestamp` refers to collection's timestamp field.

> Example: `f=city:eq:Toulouse&f=city:eq:Bordeaux&f=$timestamp:range:[0<1490613808000]`

!!! note
    For `:range:`, `lt`, `lte`, `gt` and `gte` operation, if the field's type is date, then the values should be timestamps in millisecond or a [Date expression](https://www.elastic.co/guide/en/elasticsearch/reference/current/common-options.html#date-math).

!!! note
    Date expressions start with a date value. It can either be "now" or timestamp in millisecond followed by "||".
    The date value is followed by one or two opeations :
    - +1h : add one hour
    - -1M : substract one month
    - /d : round up or down to the nearest day
    The resulted date is rounded up for `lte` and `gt` and rounded down for `lt` and `gte`

 > The date expression: `timestamp:gte:now-1M/M` substracts 1 month from now then the resulted date is rounded down to the beginning of the month.
 > Assuming `now` is 2018-06-15. `timestamp:gte:now-1M/M` is equivalent to `timestamp:gte:2016-05-01`

#### Partition filtering

`filter` part can also be passed in request header `Partition-Filter` as a serialized json for partitioning concerns.

> Example: `curl --header "Partition-Filter: {f":[{"field":"city","op":"eq","value":"Bordeaux"}]}" https://api.gisaia.com/demo/arlas/explore/cities/_count`

---
### Part: `form`

The `form` url part allows the following parameters to be specified:

| Parameter  | Default value | Values       | Description          | Multiple |
| ---------- | ------------- | ------------ | -------------------- | -------- |
| **pretty** | `false`       | `true,false` | Pretty print         | false    |
| **flat**   | `false`       | `true,false` | Flat the data property map  | false    |

> Example: `pretty=true&flat=false`

---
### Part: `field`

The `field` url part is used by the Range service only:

| Parameter   | Default value | Values               | Description                              | Multiple |
| ----------- | ------------- | -------------------- | ---------------------------------------- | -------- |
| **field**   | None          | `{fieldNamePattern}` | The name pattern of the field on which the range service is applied | false     |

> Example: `field=timestamp`

---
### Part: `projection`

The `projection` url part allows the following parameters to be specified:

| Parameter   | Default value | Values               | Description                              | Multiple |
| ----------- | ------------- | -------------------- | ---------------------------------------- | -------- |
| **include** | `*`           | `{fieldNamePattern}` | List the name patterns of the field to be included in the result. Seperate patterns with a comma. | true     |
| **exclude** | `*`           | `{fieldNamePattern}` | List the name patterns of the field to be excluded in the result. Seperate patterns with a comma. | true     |

> Example: `include=*&exclude=city,state`

---
### Part: `suggest`

The `suggest` url part allows the following parameters to be specified:

| Parameter | Default value | Values        | Description                              | Multiple |
| --------- | ------------- | ------------- | ---------------------------------------- | -------- |
| field     | `_all`        | `{fieldName}` | Name of the field to be used for retrieving the most relevant terms | false    |

> Example: `field=recommended`

---
### Part: `size`

The `size` url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description                              | Multiple |
| --------- | ------------- | ------ | ---------------------------------------- | -------- |
| **size**  | 10            | >0     | The maximum number of entries or sub-entries to be returned. | false    |
| **from**  | 0             | >0     | From index to start the search from. Defaults to 0. | false    |

> Example: `size=1000`

---
### Part: `sort`

The `sort` url part allows the following parameters to be specified:

| Parameter | Default value | Values                         | Description                              | Multiple                                 |
| --------- | ------------- | ------------------------------ | ---------------------------------------- | ---------------------------------------- |
| **sort**  | None          | `((-?)({field}||geodistance:lat lon))(,(-?){field})*` | Sort the result on the given fields ascending or descending.
Fields can be provided several times by separating them with a comma. The order matters. For a descending sort, precede the field with '-'. The sort will be ascending otherwise. For aggregation, provide the `agg` keyword as the `{field}`. For a geodistance sort, specify the point, from which the distances are calculated, as follow : `geodistance:lat lon`  | false (separate fields with comma in the same parameter) |

> Example: `sort=-country,city,geodistance:-50 -110`

## OpenSearch

If enabled, ARLAS offers an Opensearch Description document (`/arlas/ogc/opensearch/{collection}`).
