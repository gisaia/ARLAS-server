# ARLAS API Tutorial

## About

This tutorial shows several examples of how to use the ARLAS API requests on a set of data. It will guide you through the
various steps needed to :

1. create an elasticsearch index and dump custom data in it;
2. reference the created index in ARLAS catalog to make it available for exploration and browsing, by creating an ARLAS
collection reference;
3. explore the data using the ARLAS API.

## Prerequisites

- Elasticsearch running
- Arlas Server started

!!! note
    In this tutorial Elasticsearch binds to localhost:9200. Please consider adapting the hostname and the port to your configuration.

    Also, to execute the commands below correctly, please place your terminal in **ARLAS-server/examples** folder.


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

 Those airports are fictive and created randomly in each country.

#### Indexing documents

To index this documents in **airport_index** :

```shell
curl -H "Content-Type: application/json" \
  -XPOST 'localhost:9200/airport_index/airport/_bulk?pretty&refresh' \
  --data-binary "@resources/data.txt"
```

## Referencing the index in ARLAS

In order to make your data available for exploration and browsing, you need to reference it in the ARLAS catalog.
To do so, create a collection reference, which is an arbitrary name chosen by the user, using the ARLAS API request :

> **PUT** `/arlas/collections/{collection}`

```shell
curl -X PUT \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/collections/airport_collection' \
  --data @resources/collectionParams.json
```

Where :

- airport_collection is the collection reference name
- collectionParams contains parameters that describe **airport_index**

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

## Request examples

Here are some request examples using Arlas API. Please refer to the documentation.

### List

To list and describe all the collections configured in ARLAS, use this service :

> **GET** `/explore/_list`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/_list'
```

The resulting list contains one item which is **airport_collection**.

### Describe

If you want to describe a collection reference specifically, use :

> **GET** `/explore/{collection}/_describe`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_describe?pretty=false'
```

### Count

Assuming you want to know the number of airports located in France and whose area is above 10km². You can use the ***count*** service:

> **GET** `/explore/{collection}/_count`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_count?f=country%3Aeq%3AFrance&f=area%3Agte%3A10&pretty=false'
```

> **POST** `/explore/{collection}/_count`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_count' \
  --data @requests/countParameters.json
```

where countParameters is :

```JSON
            {
              "filter": {
                "f": [
                  "country:eq:France",
                  "area:gte:10"
                ]
              },
              "form": {
                "pretty": true
              }
            }
```

The result should be 16 airports.

### Search - GeoSearch

The **search** and **geosearch** services return the elements found in the collection, given the filters. Both services take the same
parameters. Only, they return different formats : **search** service returns elements as JSON and **geosearch** service as GeoJSON.

##### Example 1

Assuming you look for airports located in the US, whose number of arrival passengers per year is less than 120000 passengers and
that you're only interested in the 3 smallest ones.

> **POST** `/arlas/explore/airport_collection/_search`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_search' \
  --data @requests/searchParameters.json
```

where searchParameters is :

```JSON
            {
              "filter": {
                "f": [
                  "country:eq:US",
                  "arrival_passengers:lte:120000"
                ]
              },
              "form": {
                "pretty": true
              },
              "size": {
                "size": 3,
                "from": 0
              },
              "sort": {
                "sort": "area"
              }
            }
```

> **GET** `/arlas/explore/{collection}/_search`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_search?f=country%3Aeq%3AUS&f=arrival_passengers%3Alte%3A120000&pretty=false&include=*&size=3&from=0&sort=area'
```

##### Example 2

Assuming you look for airports located in a specific region in the south of France, whose area is greater than 10km² and
that you want the result to be sorted decreasingly on the number of departure passengers .

> **POST** `/arlas/explore/{collection}/_geosearch`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
 'http://localhost:9999/arlas/explore/airport_collection/_geosearch' \
  --data @requests/geoSearchParameters.json
```

where geoSearchParameters is

```JSON
{
  "filter": {
    "f": [
      "country:eq:France",
      "area:gte:10"
      ],
    "pwithin": "45.3,-1,42.6,2.7"
  },
  "form": {
  "pretty": true
  },
  "sort": {
    "sort": "-departure_passengers"
  }
}
```

> **GET** `/arlas/explore/{collection}/_geosearch`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_geosearch?f=country%3Aeq%3AFrance&f=area%3Agte%3A5&pwithin=45.3%2C-1%2C42.6%2C2.7&pretty=false&exclude=city%2Cstate&size=10&from=0&sort=-departure_passengers'`
```

### Aggregate - GeoAggregate

The **aggregate** and **geoaggregate** services aggregate the elements in the collection, given the filters and the aggregation parameters. Both services take the same
parameters. Only, they return different formats : **aggregate** service returns elements as JSON and **geoaggregate** service as GeoJSON.

##### Example 1

Assuming you want to know the number of airports and the area of the largest one in each country of Europe.

> **POST** `/arlas/explore/{collection}/_aggregate`

```shell
curl -X POST --header 'Content-Type: application/json;charset=utf-8' --header 'Accept: application/json' --data @requests/aggregateParameters.json 'http://localhost:9999/arlas/explore/airport_collection/_aggregate'
```

Where aggregateParameters is :

```JSON
            {
              "filter": {
                "f": [
                  "continent:eq:Europe"
                ]
              },
              "form": {
                "pretty": true
              },
              "aggregations": {
                "aggregations": [
                  {
                    "type": "term",
                    "field": "country",
                    "collectField": "area",
                    "collectFct": "max"
                  }
                ]
              }
            }
```

> **GET** `/arlas/explore/{collection}/_aggregate`

```shell
curl -X GET \
  --header 'Accept: application/json' \
   'http://localhost:9999/arlas/explore/airport_collection/_aggregate?agg=term%3Acountry%3Acollect_field-area%3Acollect_fct-max&f=continent%3Aeq%3AEurope&pretty=false'
```


##### Example 2

Assuming you want to aggregate the airports on geohashes which length is 1 . Then in each geohash you want to know the number of arrival passengers for each airport type.

> **POST** `/arlas/explore/{collection}/_geoaggregate`

```shell
curl -X POST \
  --header 'Content-Type: application/json;charset=utf-8' \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_geoaggregate' \
  --data @requests/geoAggregateParameters.json
```

Where geoAggregateParameters is :

```JSON
    {
      "form": {
        "pretty": true
      },
      "aggregations": {
        "aggregations": [
          {
            "type": "geohash",
            "field": "centroid",
            "interval": "1"
          },
          {
            "type": "term",
            "field": "airport_type",
            "collectField": "arrival_passengers",
            "collectFct": "sum"
          }
        ]
      }
    }
```

 > **GET** `/arlas/explore/{collection}/_geoaggregate`

```shell
curl -X GET \
  --header 'Accept: application/json' \
  'http://localhost:9999/arlas/explore/airport_collection/_geoaggregate?agg=geohash%3Acentroid%3Ainterval-1&agg=term%3Aairport_type%3Acollect_field-arrival_passengers%3Acollect_fct-sum&pretty=false'
```
