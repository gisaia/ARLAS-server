# URL Schema


| PATH Template | Description |
| ---- | -------- |
| /arlas/**_describe**                                              |  List all the **REST API** versions supported by ARLAS. Use the keyword `current` to use the current one |
| /arlas/`{version}`/**_describe**                                  |  List all the collections configured in ARLAS  |
| /arlas/`{version}`/`{collection}`/**_describe**?[`search`][`form`]|  Describe the structure of the collection  |
| /arlas/`{version}`/`{collections}`/**_count**?[`search`][`form`]  |  Count the number of elements found in the collection(s)[^2], given the parameters  |
| /arlas/`{version}`/`{collections}`/**_search**?[`search`][`projection`][`sort`][`size`][`form`][`format`]      |  Count the number of elements found in the collection(s), given the parameters  |
| /arlas/`{version}`/`{collections}`/**_aggregate**?[`search`][`aggregation`][`sort`][`size`][`form`][`format`]      |  Count the number of elements found in the collection(s), given the parameters  |

[^2]: When multiple collections are allowed ({collections}), the comma is used for seperating the collection names.

## `aggregation`

The [`aggregation`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
|  | false | true,false |  | false |

## `form`

The [`form`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| pretty | false | true,false | Pretty print | false |
| human | false | true,false | Human readable print | false |

## `format`

The [`format`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| format | false | json,geojson | JSON or GeoJSON format | false |

## `search`

The [`search`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
|  | false | true,false |  | false |


## `size`

The [`size`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| size | 10 | >0 | The maximum number of entries to be returned.  | true |

## `sort`

The [`sort`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| --- | --- | --- | ------ | --- |
| sort | None | {fieldName}:(ASC,DESC) | Sort the result on a given field, ascending or descending. The parameter can be provided several times. The order matters.  | true |

