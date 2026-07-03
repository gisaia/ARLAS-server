# ARLAS — STAC API and OGC API Features

## Overview

ARLAS exposes [STAC](https://stacspec.org/en/about/stac-spec/) and [OGC API Features](https://ogcapi.ogc.org/features/) endpoints to search geospatial items and browse collections.
This documentation explains the available endpoints, how to filter results, and the main limitations to keep in mind before sending a request.

Filtering relies on [CQL2](https://docs.ogc.org/is/21-065r2/21-065r2.html) through the `filter` and `filter-lang` parameters , and ARLAS supports CQL2 Text and CQL2 JSON in both GET and POST requests.
The filterable properties are published through `/queryables` resources.

## Main Endpoints

### STAC

- `GET /stac`.
- `GET /stac/api`.
- `GET /stac/collections`.
- `GET /stac/collections/{collectionId}`.
- `GET /stac/collections/{collectionId}/items`.
- `GET /stac/collections/{collectionId}/items/{featureId}`.
- `GET /stac/collections/{collectionId}/queryables`.
- `GET /stac/conformance`.
- `GET /stac/queryables`.
- `GET /stac/search`.
- `POST /stac/search`.

### OGC API Features

- `GET /stac/collections/{collectionId}/items`.
- `GET /stac/collections/{collectionId}/items/{featureId}`.
- `GET /stac/collections/{collectionId}/queryables`.

The `queryables` resources let you discover which fields can be used in filters. They are published as JSON Schema documents.

## STAC Search

`/stac/search` lets you search STAC items using filtering criteria. 
In ARLAS, this route accepts **one and only one collection** in the `collections` parameter; if `collections` contains zero or more than one collection, the request is rejected.

Example:

```http
GET /stac/search?collections=my_collection&filter-lang=cql2-text&filter=eo:cloud_cover%20<%2010
```

## OGC Features Search

`/stac/collections/{collectionId}/items` lets you search the features of an OGC API Features collection. 
As with STAC, CQL2 filters can be combined with the other criteria supported by the server, namely `datetime` and `bbox`.

Example:

```http
GET /stac/collections/my_collection/items?filter-lang=cql2-text&filter=eo:cloud_cover%20<%2010&datetime>2018-02-12T23:20:50Z
```

ARLAS also supports the “Queryables as Query Parameters” capability for OGC API Features. 
This means that queryable properties can be exposed directly as query parameters on `GET /stac/collections/{collectionId}/items`, 
allowing clients to filter features without always building a CQL2 filter. 
This behavior applies only to queryables that are eligible to equal operator.

Example:

```http
GET /stac/collections/my_collection/items?eo:cloud_cover=20
```

## Queryables

The `/queryables` endpoints describe the filterable properties available for a collection or for the catalog. 
The returned document is a JSON Schema that specifies the allowed names and their type.

For STAC collections configured as STAC models (`is_stac_model=true`), queryable field names can be exposed in both forms: with the `properties.` prefix and without it. 
For example, a STAC item property named `eo:cloud_cover` may be available as both `properties.eo:cloud_cover` and `eo:cloud_cover` in queryables and filter expressions. 
This makes it easier to work with STAC item metadata while keeping compatibility with STAC-style property naming.

Simplified example:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://arlas.example.com/stac/collections/my_collection/queryables",
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "datetime": { "type": "string", "format": "date-time" },
    "eo:cloud_cover": { "type": "number" }
  },
  "additionalProperties": false
}
```

## Filtering Capabilities

ARLAS supports the following operators in CQL2 filters:

- `=`.
- `<>`.
- `>`.
- `>=`.
- `<`.
- `=<`.
- `between`.
- `like`.
- `in`.
- `st_within`.
- `st_intersect`.
- `bbox`.

Filters can be combined with `AND`. Combinations using `OR` or `NOT` are not supported.

Example:

```http
GET /stac/search?collections=my_collection&filter-lang=cql2-text&filter=eo:cloud_cover%20<%2010%20AND%20platform=%27SENTINEL-2%27
```

## Known Limitations

ARLAS implementation of the STAC API has a few limitations:

- `/stac/search` accepts **one and only one collection** in the `collections` parameter.
- `/stac/queryables` must also be used with a collection.
- filters only support `AND` combinations.
- `OR` is not supported.
- `NOT` is not supported.
- `IS NULL` is not supported.
- `like` does not support patterns containing `%`.
- `st_within` is not supported for `LineString`, `MultiLineString`, or `GeometryCollection` values containing them.

## Spatial Filtering

For spatial filters, `filter-crs` lets you specify the coordinate reference system used in the expression. 
In the current ARLAS implementation, only `http://www.opengis.net/def/crs/OGC/1.3/CRS84` is allowed; any other value must return an error.

Example:

```http
GET /stac/collections/my_collection/items?filter-lang=cql2-text&filter-crs=http://www.opengis.net/def/crs/OGC/1.3/CRS84&filter=S_INTERSECTS(geometry,POINT(103.8%203.2))
```

## Useful Examples

### Simple filter

```http
GET /stac/search?collections=my_collection&filter-lang=cql2-text&filter=platform=%27SENTINEL-2%27
```

### Combined filter

```http
GET /stac/search?collections=my_collection&filter-lang=cql2-text&filter=eo:cloud_cover%20<%2010%20AND%20platform=%27SENTINEL-2%27
```

### CQL2 JSON filter

```json
{
  "collections": ["my_collection"],
  "filter-lang": "cql2-json",
  "filter": {
    "op": "and",
    "args": [
      {
        "op": "<",
        "args": [
          { "property": "eo:cloud_cover" },
          10
        ]
      },
      {
        "op": "=",
        "args": [
          { "property": "platform" },
          "SENTINEL-2"
        ]
      }
    ]
  }
}
```
