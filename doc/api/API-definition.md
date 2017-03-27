# URL Schema


| PATH Template | Description |
| ---- | -------- |
| /arlas/**_describe**                                              |  List all the **REST API** versions supported by ARLAS. Use the keyword `current` to use the current one |
| /arlas/`{version}`/**_describe**                                  |  List all the collections configured in ARLAS  |
| /arlas/`{version}`/`{collection}`/**_describe**?[`filter`][`form`]|  Describe the structure of the collection  |
| /arlas/`{version}`/`{collections}`/**_count**?[`filter`][`form`]  |  Count the number of elements found in the collection(s)[^2], given the parameters  |
| /arlas/`{version}`/`{collections}`/**_search**?[`filter`][`projection`][`sort`][`size`][`form`][`format`]      |  Count the number of elements found in the collection(s), given the parameters  |
| /arlas/`{version}`/`{collections}`/**_aggregate**?[`filter`][`aggregation`][`sort`][`size`][`form`][`format`]      |  Count the number of elements found in the collection(s), given the parameters  |

[^2]: When multiple collections are allowed ({collections}), the comma is used for seperating the collection names.


---
### `aggregation`

The [`aggregation`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
|  | false | true,false |  | false |

> Example: `...`


---
### `filter`

The [`filter`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| f | None | {fieldName}{operator}{value} | A triplet for filtering the result. Multiple filter can be provided. The order does not matter. A triplet is composed of a field name, a comparison operator and a value. The **AND** operator is applied between filters having different fieldNames. The **OR** operator is applied on filters having the same fieldName. | true |

> Example: `...`

---
### `form`

The [`form`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| pretty | false | true,false | Pretty print | false |
| human | false | true,false | Human readable print | false |

> Example: `pretty=true`

---
### `format`

The [`format`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| format | false | json,geojson | JSON or GeoJSON format | false |

> Example: `...`

---
### `size`

The [`size`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| size | 10 | >0 | The maximum number of entries to be returned.  | true |

> Example: `...`

---
### `sort`

The [`sort`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| sort | None | {fieldName}:(ASC,DESC) | Sort the result on a given field, ascending or descending. The parameter can be provided several times. The order matters.  | true |

> Example: `...`
