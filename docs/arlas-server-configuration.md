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

ARLAS can run as a docker container. A rich set of properties of the configuration file can be overriden by passing environment variables to the container:

```shell
docker run -ti -d \
   --name arlas-server \
   -e "ARLAS_ELASTIC_CLUSTER=my-own-cluster" \
   arlas-server:latest
```

The tables bellow list those properties.


### JAVA

| Environment variable | Description |
| --- | --- |
| ARLAS_XMX | Java Maximum Heap Size |

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

### File/URL based configuration

Instead of overriding some properties of the configuration file, it is possible to start the ARLAS Server container with a given configuration file.

#### File

The ARLAS Server container can start with a mounted configuration file thanks to docker volume mapping. For instance, if the current directory of the host contains a `configuration.yaml` file, the container can be started as follow:

```shell
docker run -ti -d \
   --name arlas-server \
   -v `pwd`/configuration.yaml:/opt/app/configuration.yaml \
   arlas-server:latest
  ```

#### URL

The ARLAS Server container can start with a configuration file that is downloaded before starting up. The configuration file must be available through an URL accessible from within the container. The URL is specified with an environment variable:

| Environment variable | Description |
| --- | --- |
| ARLAS_CONFIGURATION_URL | URL of the ARLAS configuration file to be downloaded by the container before starting |

For instance, if the current directory of the host contains a `configuration.yaml` file, the container can be started as follow:

```shell
docker run -ti -d \
   --name arlas-server \
   -e ARLAS_CONFIGURATION_URL="http://somemachine/conf.yaml" \
   arlas-server:latest
  ```
