# ARLAS API Overview

The ARLAS Server offers 4 enpoints:

- a collection API for [managing collections](arlas-api-collection.md)
- an exploration API for [exploring](arlas-api-exploration.md), meaning searching and analyzing, spatial-temoral big data
- an API for monitoring the server health and performances
- an endpoints for testing the collection API  and the exploration API with swagger

!!! warning
    All endpoints are not necessarily enabled. See the [configuration](arlas-server-configuration.md) for more details.

## Monitoring

The monitoring API provides some information about the health and the performances of the ARLAS server that can be of interest:

| URL | Description |
| --- | --- |
| http://.../admin/metrics?pretty=true  |  Metrics about the performances of the ARLAS server. Metrics about the collection API  are prefixed with `io.arlas.server.rest.collections` and metrics about the explore API are prefixed with `io.arlas.server.rest.explore`|
| http://.../admin/ping | Returns pong  |
| http://.../admin/threads | List of running threads |
| http://.../admin/healthcheck?pretty=true  |  Whether the service is healthy or not |


## Swagger

| URL | Description |
| --- | --- |
| http://.../arlas/swagger  | The web application for testing the API  |
| http://.../arlas/swagger.yaml  | The swagger definition of the collections/exploration API with YAML format |
| http://.../arlas/swagger.yaml  | The swagger definition of the collections/exploration API with JSON format |
