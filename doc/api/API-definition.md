# URL Schema


| PATH Template | Description |
| ---- | -------- |
| /arlas/**_describe**                                              |  List all the **REST API** versions supported by ARLAS. Use the keyword `current` to use the current one |
| /arlas/`{version}`/**_describe**                                  |  List all the collections configured in ARLAS  |
| /arlas/`{version}`/`{collection}`/**_describe**?[`search`][`form`]|  Describe the structure of the collection  |
| /arlas/`{version}`/`{collections}`/**_count**?[`search`][`form`]  |  Count the number of elements found in the collection(s)[^2], given the parameters  |
| /arlas/`{version}`/`{collections}`/**_search**?[`search`][`form`][`projection`][`sort`][`format`]      |  Count the number of elements found in the collection(s), given the parameters  |

[^2]: When multiple collections are allowed ({collections}), the comma is used for seperating the collection names.

## `sort`

The [`format`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| ---- | -------- |-------- |-------- |-------- |
| sort | false | json,geojson | JSON or GeoJSON format | true |

## `format`

The [`format`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| ---- | -------- |-------- |-------- | -------- |
| format | false | json,geojson | JSON or GeoJSON format | false |

## `form`

The [`form`] url part allows the following parameters to be specified:

| Parameter | Default value | Values | Description | Multiple |
| ---- | -------- |-------- |-------- | -------- |
| pretty | false | true,false | Pretty print | false |
| human | false | true,false | Human readable print | false |
