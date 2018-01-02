# Configuring ARLAS Server running environment

## ARLAS configuration file

The configuration of ARLAS related functions are described below. The other  module configurations are available online:

| Module | Link |
| --- | --- |
| Swagger | https://github.com/federecio/dropwizard-swagger |
| Dropwizard | http://www.dropwizard.io/1.0.4/docs/manual/configuration.html |
| Zipkin | https://github.com/smoketurner/dropwizard-zipkin |

| Path | Default | Description |
| --- | --- | --- |
| elastic-host | / | hostname or ip address of the elasticsearch node that is used for storing ARLAS configuration |
| elastic-port | / | port of the elasticsearch node that is used for storing ARLAS configuration  |
| elastic-cluster | / | clustername of the elasticsearch cluster that is used for storing ARLAS configuration |
| arlas-index | / | name of the index that is used for storing ARLAS configuration |
| arlas-cache-size | 1000 | Size of the cache used for managing the collections  |
| arlas-cache-timeout | 60 | Number of seconds for the cache used for managing the collections |
| arlas-cors-enabled | false | whether the Cross-Origin Resource Sharing (CORS) mechanism is enabled or not |
| collection-auto-discover.preferred-id-field-name | / | Name of the id field for auto discovery |
| collection-auto-discover.preferred-timestamp-field-name | / |  Name of the timestamp field for auto discovery |
| collection-auto-discover.preferred-centroid-field-name | / |  Name of the centroid field for auto discovery |
| collection-auto-discover.preferred-geometry-field-name | / |  Name of the geometry field for auto discovery |
| collection-auto-discover.schedule | 0 |  Number of seconds between two auto discovery tasks |

## ARLAS Server as a docker container

### Server

| Environment variable | ARLAS Server configuration variable |
| --- | --- |
| ARLAS_ACCESS_LOG_FILE | server.requestLog.appenders.currentLogFilename |
| ACCESS_LOG_FILE_ARCHIVE | server.requestLog.appenders.archivedLogFilenamePattern |
| ARLAS_PREFIX | server.applicationContextPath |
| ARLAS_ADMIN_PATH | server.adminContextPath |
| ARLAS_PORT | server.connector.port |
| ARLAS_MAX_THREADS | server.maxThreads |
| ARLAS_MIN_THREADS | server.minThreads |
| ARLAS_MAX_QUEUED_REQUESTS | server.maxQueuedRequests |

### Logging

| Environment variable | ARLAS Server configuration variable |
| --- | --- |
| ARLAS_LOGGING_LEVEL | logging.level |
| ARLAS_LOGGING_CONSOLE_LEVEL | logging.appenders[type: console].threshold |
| ARLAS_LOGGING_FILE | logging.appenders[type: file].currentLogFilename |
| ARLAS_LOGGING_FILE_LEVEL | logging.appenders[type: file].threshold |
| ARLAS_LOGGING_FILE_ARCHIVE | logging.appenders[type: file].archive |
| ARLAS_LOGGING_FILE_ARCHIVE_FILE_PATTERN | logging.appenders[type: file].archivedLogFilenamePattern |
| ARLAS_LOGGING_FILE_ARCHIVE_FILE_COUNT |logging.appenders[type: file].archivedFileCount  |

### Datasource

| Environment variable | ARLAS Server configuration variable |
| --- | --- |
| ARLAS_ELASTIC_HOST | elastic-host |
| ARLAS_ELASTIC_PORT | elastic-port |
| ARLAS_ELASTIC_CLUSTER | elastic-cluster |
| ARLAS_ELASTIC_INDEX | arlas-index |

### Collection Cache & Disovery

| Environment variable | ARLAS Server configuration variable |
| --- | --- |
| ARLAS_CACHE_SIZE | arlas-cache-size |
| ARLAS_CACHE_TIMEOUT | arlas-cache-timeout |
| ARLAS_CORSE_ENABLED | arlas-cors-enabled |
| ARLAS_COLLECTION_AUTODISCOVER_SCHEDULE | collection-auto-discover.schedule |

### Zipkin

| Environment variable | ARLAS Server configuration variable |
| --- | --- |
| ARLAS_ZIPKIN_ENABLED | zipkin.enabled |
| ARLAS_ZIPKIN_SERVICE_HOST | zipkin.serviceHost |
| ARLAS_ZIPKIN_COLLECTOR | zipkin.servicePort |
| ARLAS_ZIPKIN_BASEURL | zipkin.collector |
| ARLAS_ZIPKIN_BASEURL | zipkin.baseUrl |
