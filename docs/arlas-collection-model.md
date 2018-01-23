# ARLAS Collection model

## About

A **Collection** is an Arlas object that references your indexed data and make it explorable by **ARLAS-server**.

As **ARLAS-server** is meant to deliver spatial-temporal data analysis, your indexed data must contain *identifier*, *time*, *centroid* and *geometry* fields.

And, by referencing your data in a **Collection**, you make your REST calls lighter as ARLAS-server already knows the fields to query.


## Model

A Collection has the following structure : 

```JSON
{
  "index_name": "string",
  "type_name": "string",
  "id_path": "path.to.id.field",
  "geometry_path": "path.to.a.geometric.field",
  "centroid_path": "path.to.a.ponctual.geometric.field",
  "timestamp_path": "path.to.a.date.field",
  "include_fields": "fields,to,be,included,in,search,responses",
  "exclude_fields": "fields,to,be,excluded,from,search,responses",
  "custom_params": {}
}
```

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
| index_name     | Name of the index in elasticsearch                | Mandatory |
| type_name      | Name of the mapping type used within the index    | Mandatory |
| id_path        | Path to the id field in the indexed documents     | optional  |
| geometry_path  | Path to an [Elasticsearch geometric](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/geo-shape.html) field in the indexed documents| optional  |
| centroid_path  | Path to an [Elasticsearch Geo-point field](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/geo-point.html)  in the indexed documents. | optional  |
| timestamp_path | Path to a timestamp/date field in the indexed documents, that meets the [Elasticsearch date format](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/mapping-date-format.html), set when indexing data. | optional  |
| include_fields | Comma separated fields names that will be included in ARLAS-server responses. By default, all the fields are included | optional  |
| exclude_fields | Comma separated fields names that will be excluded from ARLAS-server responses. By default, none of the fields are excluded | optional|

