# ARLAS API Tutorial

## About

This tutorial shows several examples of how to use the ARLAS API requests on a set of data. It will guide you through the
various steps needed to :

1. create an Elasticsearch index and dump custom data in it;
2. reference the created index in ARLAS catalog to make it available for exploration and browsing, by creating an ARLAS
collection reference;
3. explore the data using the ARLAS API.

## Prerequisites

- Elasticsearch running
- Arlas Server started

!!! note
    In this tutorial Elasticsearch binds to localhost:9200. Please consider adapting the hostname and the port to your configuration.

    Also, to execute the commands below correctly, please place your terminal in **ARLAS-server/docs** folder.


## Create an index

First create an index named '**airport_index**'.

```shell
curl -XPUT \
  -H 'Content-Type: application/json' \
  'localhost:9200/airport_index?pretty' \
  --data @resources/settings.json
```

Then, create the following '**airport**' mapping type, described in the table below :

```shell
curl -XPUT \
  -H 'Content-Type: application/json' \
  'localhost:9200/airport_index/_mapping/airport?pretty' \
  --data @resources/airport.mapping.json
```

| Field                 | Description                                       | Type      |
| --------------------- | ------------------------------------------------- | --------- |
| id                    | Airport's id                                      | keyword   |
| name                  | Airport name                                      | text      |
| airport_type          | Airport type                                      | keyword   |
| country               | Airport's country                                 | keyword   |
| continent             | Airport's continent                               | keyword   |
| area                  | Airport's surface                                 | double    |
| arrival_passengers    | number of arrival passengers in 2016              | long      |
| departure_passengers  | number of departure passengers in 2016            | long      |
| startdate             | Airport's opening date                            | date      |
| geometry              | Airport's shape                                   | geo_point |
| centroid              | Airport's centroid                                | geo_point |


## Indexing documents

Now, you are going to dump a set of data in the **airport_index**

#### Data description

In this example, you 'll index 130 documents formatted according the **airport** mapping type. Each document represents an airport :

 - that is located in France, Germany, US or Canada,
 - that is created between 1970 and 2017
 - whose area is between 0,1 and 30 km²
 - whose number of passengers for departures and arrivals per year is between 5000 and 1500000 passengers

!!! info
    These airports are fictional and are created randomly in each country.

#### Indexing documents

To index these documents in **airport_index** :

```shell
curl -H "Content-Type: application/json" \
  -XPOST 'localhost:9200/airport_index/airport/_bulk?pretty&refresh' \
  --data-binary "@resources/data.txt"
```

## Referencing the index in ARLAS

Now, in order to make your data available for exploration and browsing, you need to reference it in the ARLAS catalog.
To do so, create a collection reference with ARLAS collection API, using the following request :

> **PUT** `/arlas/collections/{collection}`

```shell
curl -X PUT \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/collections/airport_collection' \
  --data @resources/collectionParams.json
```

!!! note "Note 1"
    `airport_collection` is the collection reference name.

!!! note "Note 2"
    `collectionParams.json` sums up the parameters that describe `airport_index` index
    ```JSON
            {
              "index_name": "airport_index",
              "type_name": "airport",
              "id_path": "id",
              "geometry_path": "geometry",
              "centroid_path": "centroid",
              "timestamp_path": "startdate",
              "include_fields": "*"
            }
    ```

## Examples using exploration API of ARLAS

ARLAS exploration API allows to search and analyse spatial-temporal big data.

Here are some request examples using exploration API to discover `airport_collection` that we have just created. *Please refer to the [documentation](arlas-api-exploration.md)*.

### List

To list and describe all the collections configured in ARLAS, you can use `_list` service :

> **GET** `/explore/_list`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/_list?pretty=true'
```
!!! success "Response"
    The resulting list contains one item which is `airport_collection`.

### Describe

If you want to describe a collection reference specifically, you may use `_describe` service this way :

> **GET** `/explore/{collection}/_describe`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_describe?pretty=true'
```

### Count

`_count` service counts the number of elements found in a collection, given the filters.

##### Example

Assuming you want to know the number of airports located in France and whose area is above 10km². You can use `_count` service as follows:

> **GET** `/explore/{collection}/_count`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_count?f=country%3Aeq%3AFrance&f=area%3Agte%3A10&pretty=true'
```

> **POST** `/explore/{collection}/_count`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_count?pretty=true' \
  --data @requests/countParameters.json
```

!!! note "Note"
    In `countParameters.json` is defined the filter :
    ```JSON
            {
              "filter": {
                "f": [
                  [
                    {
                      "field": "country",
                      "op": "eq",
                      "value": "France"
                    }
                  ],
                  [
                    {
                      "field": "area",
                      "op": "gte",
                      "value": "10"
                    }
                  ]
                ]
              }
            }
    ```

!!! success "Response"
    ```JSON
            {
              "collection" : "airport_collection",
              "nbhits" : 10,
              "totalnb" : 16
            }
    ```
    There is 16 airports in our data set.
    
### Search - GeoSearch

The `_search` and `_geosearch` services return the elements found in the collection, given the filters. Both services take the same
parameters. Only, they return different formats : **_search** service returns elements as JSON and **_geosearch** service as GeoJSON.

##### Example 1

Assuming you look for airports located in the US, whose number of arrival passengers per year is less than 120000 passengers and
that you're only interested in the 2 smallest ones.

> **GET** `/arlas/explore/{collection}/_search`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_search?f=country%3Aeq%3AUS&f=arrival_passengers%3Alte%3A120000&size=2&from=0&sort=area&pretty=true'
```

> **POST** `/arlas/explore/airport_collection/_search`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_search?pretty=true' \
  --data @requests/searchParameters.json
```

!!! note "Note"
    `searchParameters.json` is the corresponding request's body
    ```JSON
            {
              "filter": {
                  "f": [
                    [
                      {
                        "field": "country",
                        "op": "eq",
                        "value": "US"
                      }
                    ],
                    [
                      {
                        "field": "arrival_passengers",
                        "op": "lte",
                        "value": "120000"
                      }
                    ]
                  ]
                },
                "page": {
                  "size": 2,
                  "from": 0,
                  "sort": "area"
                }
            }
    ```

!!! success "Response"
    ```JSON
            {
              "collection" : "airport_collection",
              "hits" : [ {
                "md" : {
                  "id" : "101",
                  "timestamp" : 872632800000,
                  "geometry" : {
                    "type" : "Point",
                    "coordinates" : [ -103.0069, 44.5 ]
                  },
                  "centroid" : {
                    "type" : "Point",
                    "coordinates" : [ -103.0069, 44.5 ]
                  }
                },
                "data" : {
                  "airport_type" : "heliport",
                  "continent" : "America",
                  "area" : 2,
                  "country" : "US",
                  "departure_passengers" : 581533,
                  "arrival_passengers" : 103471,
                  "centroid" : "44.5,-103.0069",
                  "name" : "airport 101",
                  "geometry" : "44.5,-103.0069",
                  "id" : 101,
                  "startdate" : "1997-08-27"
                }
              }, {
                "md" : {
                  "id" : "23",
                  "timestamp" : 1408312800000,
                  "geometry" : {
                    "type" : "Point",
                    "coordinates" : [ -90.1384, 45.4 ]
                  },
                  "centroid" : {
                    "type" : "Point",
                    "coordinates" : [ -90.1384, 45.4 ]
                  }
                },
                "data" : {
                  "airport_type" : "airport",
                  "continent" : "America",
                  "area" : 3.1,
                  "country" : "US",
                  "departure_passengers" : 821528,
                  "arrival_passengers" : 118860,
                  "centroid" : "45.4,-90.1384",
                  "name" : "airport 23",
                  "geometry" : "45.4,-90.1384",
                  "id" : 23,
                  "startdate" : "2014-08-18"
                }
              } ],
              "nbhits" : 2,
              "totalnb" : 8
    ```
     There are 8 airports matching the filter. Only the two smallest ones are returned.

##### Example 2

Assuming you look for airports located in a specific region in the south of France, whose area is greater than 10km² and
that you want the result to be sorted decreasingly on the number of departure passengers .

> **GET** `/arlas/explore/{collection}/_geosearch`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_geosearch?f=country%3Aeq%3AFrance&f=area%3Agte%3A5&pwithin=-1%2C42.6%2C2.7%2C45.3&sort=-departure_passengers&pretty=true'
```

> **POST** `/arlas/explore/{collection}/_geosearch`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
 'http://localhost:9999/arlas/explore/airport_collection/_geosearch?pretty=true' \
  --data @requests/geoSearchParameters.json
```

!!! note "Note"
    `geoSearchParameters.json` is the corresponding request's body 
    ```JSON
    {
      "filter": {
        "f": [
          [
            {
              "field": "country",
              "op": "eq",
              "value": "France"
            }
          ],
          [
            {
              "field": "area",
              "op": "gte",
              "value": 10
            }
          ]
        ],
        "pwithin": [["-1,42.6,2.7,45.3"]]
      },
      "page": {
        "sort": "-departure_passengers"
      }
    }
    ```
    
!!! success "Response"
    ```JSON
            {
              "type" : "FeatureCollection",
              "features" : [ {
                "type" : "Feature",
                "properties" : {
                  "airport_type" : "heliport",
                  "continent" : "Europe",
                  "area" : 19.3,
                  "feature_type" : "hit",
                  "country" : "France",
                  "departure_passengers" : 1102773,
                  "arrival_passengers" : 92077,
                  "centroid" : "45.0835,0.3",
                  "name" : "airport 8",
                  "id" : 8,
                  "startdate" : "1990-04-16"
                },
                "geometry" : {
                  "type" : "Point",
                  "coordinates" : [ 0.3, 45.0835 ]
                }
              }, {
                "type" : "Feature",
                "properties" : {
                  "airport_type" : "heliport",
                  "continent" : "Europe",
                  "area" : 13.9,
                  "feature_type" : "hit",
                  "country" : "France",
                  "departure_passengers" : 808842,
                  "arrival_passengers" : 1195080,
                  "centroid" : "44.6379,2",
                  "name" : "airport 10",
                  "id" : 10,
                  "startdate" : "1980-12-13"
                },
                "geometry" : {
                  "type" : "Point",
                  "coordinates" : [ 2.0, 44.6379 ]
                }
              } ]
            }
    ```
    There are 2 airports matching the filter


### Aggregate - GeoAggregate

The `_aggregate` and `_geoaggregate` services aggregate the elements in the collection, given the filters and the aggregation parameters. Both services take the same
parameters. Only, they return different formats : **_aggregate** service returns elements as JSON and **_geoaggregate** service as GeoJSON.

##### Example 1

Assuming you want to know how many airports are in each country of Europe and the area of the largest ones.

> **GET** `/arlas/explore/{collection}/_aggregate`

```shell
curl -X GET \
  --header 'Accept: application/json' \
   'http://localhost:9999/arlas/explore/airport_collection/_aggregate?f=continent%3Aeq%3AEurope&agg=term%3Acountry%3Acollect_field-area%3Acollect_fct-max&&pretty=true'
```

> **POST** `/arlas/explore/{collection}/_aggregate`

```shell
curl -X POST --header 'Content-Type: application/json;charset=utf-8' --header 'Accept: application/json' --data @requests/aggregateParameters.json 'http://localhost:9999/arlas/explore/airport_collection/_aggregate?pretty=true'
```

!!! note "Note"
    `aggregateParameters.json` is the corresponding request's body 
    ```JSON
            {
              "filter": {
                "f": [
                  [
                    {
                      "field": "continent",
                      "op": "eq",
                      "value": "Europe"
                    }
                  ]
                ]
              },
              "aggregations": [
                {
                  "type": "term",
                  "field": "country",
                  "metrics": [
                    {
                      "collectField": "area",
                      "collectFct": "max"
                    }
                  ]
                }
              ]
            }
    ```

!!! success "Response"
    ```JSON
            {
              "query_time" : 68,
              "total_time" : 78,
              "totalnb" : 30,
              "name" : "Term aggregation",
              "sumotherdoccounts" : 0,
              "elements" : [ {
                "count" : 20,
                "key" : "France",
                "key_as_string" : "France",
                "metrics" : [ {
                  "type" : "max",
                  "field" : "area",
                  "value" : 31.1
                } ]
              }, {
                "count" : 10,
                "key" : "Germany",
                "key_as_string" : "Germany",
                "metrics" : [ {
                  "type" : "max",
                  "field" : "area",
                  "value" : 32.3
                } ]
              } ]
            }
    ```
    There are 20 airports in France, the largest one is 31.1km².
    In Germany, there are 10 airports. The largest one is 32.3km².


##### Example 2

Assuming you want to aggregate the airports on geohashes which precision is 1 . Then in each geohash you want to know the total number of arrival passengers for each airport type.

> **GET** `/arlas/explore/{collection}/_geoaggregate`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_geoaggregate?agg=geohash%3Acentroid%3Ainterval-1&agg=term%3Aairport_type%3Acollect_field-arrival_passengers%3Acollect_fct-sum&pretty=true'
```

> **POST** `/arlas/explore/{collection}/_geoaggregate`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_geoaggregate?pretty=true' \
  --data @requests/geoAggregateParameters.json
```

!!! note "Note"
    `geoAggregateParameters.json` is the corresponding request's body
    ```JSON
        {
          "aggregations": [
            {
              "type": "geohash",
              "field": "centroid",
              "interval": {
                "value": 1
              }
            },
            {
              "type": "term",
              "field": "airport_type",
              "metrics": [
                {
                  "collectField": "arrival_passengers",
                  "collectFct": "sum"
                }
              ]
            }
          ]
        }
    ```

!!! success "Response"
    ```JSON
        {
          "type" : "FeatureCollection",
          "features" : [
            {
              "type" : "Feature",
              "properties" : {
                "feature_type" : "aggregation",
                "geohash" : "9",
                "elements" : [{
                  "name" : "Term aggregation",
                  "sumotherdoccounts" : 0,
                  "elements" : [ 
                    {
                      "count" : 48,
                      "key" : "heliport",
                      "key_as_string" : "heliport",
                      "metrics" : [{
                        "type" : "sum",
                        "field" : "arrival_passengers",
                        "value" : 4.0138731E7
                      }]
                    }, 
                    ...
                  ]
                }],
                "count" : 88,
                "metrics" : [ ]
              },
              "geometry" : {
                "type" : "Point",
                "coordinates" : [ -112.5, 22.5 ]
              }
            }, 
            ...
          ]
        }
    ```