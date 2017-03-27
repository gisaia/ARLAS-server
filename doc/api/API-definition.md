# URL Schema

The table below lists the URL endpoints and their optional "parts". A part is composed of optional parameters. The parameters are seperated with the character `&`.

| PATH Template | Description |
| ---- | -------- |
| /arlas/**_describe**                                              |  List all the **REST API** versions supported by ARLAS. Use the keyword `current` to use the current one |
| /arlas/`{version}`/**_describe**                                  |  List all the collections configured in ARLAS  |
| /arlas/`{version}`/`{collection}`/**_describe**?[`filter`][`form`]|  Describe the structure of the collection  |
| /arlas/`{version}`/`{collections}`/**_count**?[`filter`][`form`]  |  Count the number of elements found in the collection(s)[^2], given the parameters  |
| /arlas/`{version}`/`{collections}`/**_search**?[`filter`][`form`][`format`][`projection`][`size`][`sort`]      |  Count the number of elements found in the collection(s), given the parameters  |
| /arlas/`{version}`/`{collections}`/**_aggregate**?[`aggregation`][`filter`][`form`][`format`][`size`][`sort`]      |  Count the number of elements found in the collection(s), given the parameters  |
| /arlas/`{version}`/`{collections}`/**_suggest**?[`filter`][`form`][`size`][`suggest`]      |  Suggest the the n (n=`size`) most relevant terms given the filters  |

[^2]: When multiple collections are allowed ({collections}), the comma is used for seperating the collection names.


---
### `aggregation`

The [`aggregation`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **histogram** | `None` | `{fieldName}` | Aggregates on the date buckets of `{fieldName}` | true |
| **interval**  | `None` | interval | Size of the intervals. | true |
| **** | None | `true`,`false` |  | false |
| **** | None | `true`,`false` |  | false |
| **** | None | `true`,`false` |  | false |


| Aggregation type | Interval | Description |
| --- | --- | ------ | --- |
| time | `{size}**(year,quarter,month,week,day,hour,minute,second)` |  Size of a time interval with the given unit (no space between number and unit)  |
| time | **** |   |
| numeric | **** |   |
| numeric | **** |   |


> Example: `...`


---
### `filter`

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
### `form`

The [`form`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **pretty** | `false` | `true,false` | Pretty print | false |
| **human** | `false` | `true,false` | Human readable print | false |

> Example: `pretty=true`

---
### `format`

The [`format`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **format** | `false` | `json`,`geojson` | JSON or GeoJSON format | false |

> Example: `...`

---
### `suggest`

The [`suggest`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
|  | `false` | `true,false` |  | false |

> Example: `...`

---
### `size`

The [`size`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **size** | 10 | >0 | The maximum number of entries or sub-entries to be returned. | true |

> Example: `...`

---
### `sort`

The [`sort`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| **sort** | None | `{fieldName}`:(`ASC`,`DESC`) | Sort the result on a given field, ascending or descending. The parameter can be provided several times. The order matters. For aggregation, provide the `agg` keyword as the `{fieldName}`. | true |

> Example: `...`
