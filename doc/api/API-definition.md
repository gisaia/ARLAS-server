# ARLAS

The ARLAS API makes the ARLAS catalog available for exploration and browsing. The catalog contains collections of geo-referenced products. Every product has a geometry, a centroid, a timestamp and a set of fields specific to the collection.

# URL Schema

The table below lists the URL endpoints and their optional "parts". A part is composed of optional parameters. The parameters are seperated with the character `&`.

| PATH Template | Description |
| ---- | -------- |
| /arlas/**_describe**                                                                              |  List  the collections configured in ARLAS  |
| /arlas/`{collection}`/**_describe**?[`form`]                                                      |  Describe the structure and the content of the given collection  |
| /arlas/`{collections}`/**_count**?[`filter`][`form`]                                              |  Count the number of elements found in the collection(s), given the filters  |
| /arlas/`{collections}`/**_search**?[`filter`][`form`][`format`][`projection`][`size`][`sort`]     |  Count the number of elements found in the collection(s), given the parameters  |
| /arlas/`{collections}`/**_aggregate**?[`aggregation`][`filter`][`form`][`format`][`size`][`sort`] |  Count the number of elements found in the collection(s), given the parameters  |
| /arlas/`{collections}`/**_suggest**?[`filter`][`form`][`size`][`suggest`]                         |  Suggest the the n (n=`size`) most relevant terms given the filters  |

When multiple collections are permited ({collections}), the comma is used for seperating the collection names.

# URL Parts

## Part: `aggregation`

The [`aggregation`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **agg**           | `None`    | `datehistogram,geohash,histogram` | Type of aggregation           | false |
| **agg_field**     | `None`    | `{field}`                         | Aggregates on the `{field}`.  | true  |
| **agg_interval**  | `None`    | interval                          | Size of the intervals.        | true  |


| Aggregation type | Interval | Description |
| --- | --- | ------ | --- |
| ***datehistogram*** | `{size}(year,quarter,month,week,day,hour,minute,second)` |  Size of a time interval with the given unit (no space between number and unit)  |
| ***geohash***  |  |   |
| ***numeric*** | `{size}` |   |


> Example: `...`


---
## Part: `filter`

The [`filter`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **f** | None | `{fieldName}{operator}{value}` | A triplet for filtering the result. Multiple filter can be provided. The order does not matter. A triplet is composed of a field name, a comparison operator and a value. The **AND** operator is applied between filters having different fieldNames. The **OR** operator is applied on filters having the same fieldName. If the fieldName starts with **-** then a **must not** filter is used | true |
| **q** | None | text | A full text search | false |
| **before** | None | timestamp | Any element having its point in time reference before the given timestamp | false |
| **after**  | None | timestamp | Any element having its point in time reference after the given timestamp | false |
| **pwithin** | None | geometry  | Any element having its centroid contained within the given geometry | false |
| **gwithin** | None | geometry  | Any element having its geometry contained within the given geometry | false |
| **gintersect** | None | geometry  | Any element having its geometry intersecting the given geometry | false |



| Operator | Description | Value type |
| --- | ------ | --- |
| **:** | `{fieldName}` equals `{value}` | numeric or strings |
| **:>=** | `{fieldName}` is greater than or equal to `{value}` | numeric |
| **:>** | `{fieldName}` is greater than `{value}` | numeric |
| **:<=** | `{fieldName}` is less than or equal to `{value}` | numeric |
| **:<** | `{fieldName}` is less than `{value}` | numeric |

> Example: `...`

---
## Part: `form`

The [`form`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **pretty** | `false` | `true,false` | Pretty print | false |
| **human** | `false` | `true,false` | Human readable print | false |

> Example: `pretty=true`

---
## Part: `format`

The [`format`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **format** | `false` | `json`,`geojson` | JSON or GeoJSON format | false |

> Example: `...`

---
## Part: `suggest`

The [`suggest`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
|  | `false` | `true,false` |  | false |

> Example: `...`

---
## Part: `size`

The [`size`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **size** | 10 | >0 | The maximum number of entries or sub-entries to be returned. | true |

> Example: `...`

---
## Part: `sort`

The [`sort`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **sort** | None | `{fieldName}`:(`ASC`,`DESC`) | Sort the result on a given field, ascending or descending. The parameter can be provided several times. The order matters. For aggregation, provide the `agg` keyword as the `{fieldName}`. | true |

> Example: `...`
