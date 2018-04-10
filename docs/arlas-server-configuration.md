# Configuring ARLAS Server running environment

## ARLAS configuration file

ARLAS server is configured with a yaml configuration file.

External module configurations are available online:

| Module | Link |
| --- | --- |
| Swagger | https://github.com/federecio/dropwizard-swagger |
| Dropwizard | http://www.dropwizard.io/1.0.4/docs/manual/configuration.html |
| Zipkin | https://github.com/smoketurner/dropwizard-zipkin |

## Configure ARLAS Server as a docker container

#### With environment variables

ARLAS can run as a docker container. A rich set of properties of the configuration file can be overriden by passing environment variables to the container:

```shell
docker run -ti -d \
   --name arlas-server \
   -e "ARLAS_ELASTIC_CLUSTER=my-own-cluster" \
   gisaia/arlas-server:latest
```

All supported environment variables are listed below.

### With file/URL based configuration

Instead of overriding some properties of the configuration file, it is possible to start the ARLAS Server container with a given configuration file.

#### File

The ARLAS Server container can start with a mounted configuration file thanks to docker volume mapping. For instance, if the current directory of the host contains a `configuration.yaml` file, the container can be started as follow:

```shell
docker run -ti -d \
   --name arlas-server \
   -v `pwd`/configuration.yaml:/opt/app/configuration.yaml \
   gisaia/arlas-server:latest
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
   gisaia/arlas-server:latest
```

## ARLAS configuration properties

### Enabled Services

| Environment variable | ARLAS Server configuration variable | Default | Description |
| --- | --- | --- | --- |
| ARLAS_SERVICE_COLLECTIONS_ENABLE    | arlas-service-collections-enabled    | true     | Whether the collection service is enabled or not |
| ARLAS_SERVICE_EXPLORE_ENABLE    | arlas-service-explore-enabled    | true     | Whether the explore service is enabled or not |
| ARLAS_SERVICE_WFS_ENABLE    | arlas-service-wfs-enabled    | false     | Whether the WFS service is enabled or not |
| ARLAS_SERVICE_OPENSEARCH_ENABLE    | arlas-service-opensearch-enabled    | true     | Whether the opensearch service is enabled or not |
| ARLAS_SERVICE_CSW_ENABLE    | arlas-service-csw-enabled    | true     | Whether the CSW service is enabled or not |
| ARLAS_SERVICE_TAG_ENABLE    | arlas-service-tag-enabled    | false     | Whether the tag service is enabled or not |


### Datasource

| Environment variable | ARLAS Server configuration variable | Default | Description |
| --- | --- | --- | --- |
| ARLAS_ELASTIC_HOST    | elastic-host    | localhost     | hostname or ip address of the elasticsearch node that is used for storing ARLAS configuration |
| ARLAS_ELASTIC_PORT    | elastic-port    | 9300          | port of the elasticsearch node that is used for storing ARLAS configuration  |
| ARLAS_ELASTIC_CLUSTER | elastic-cluster | elasticsearch | clustername of the elasticsearch cluster that is used for storing ARLAS configuration |
| ARLAS_ELASTIC_INDEX   | arlas-index     | .arlas        | name of the index that is used for storing ARLAS configuration |

### Collection Cache & Disovery

| Environment variable | ARLAS Server configuration variable | Default | Description |
| --- | --- | --- | --- |
| ARLAS_CACHE_SIZE                       | arlas-cache-size                  | 1000 | Size of the cache used for managing the collections  |
| ARLAS_CACHE_TIMEOUT                    | arlas-cache-timeout               | 60 | Number of seconds for the cache used for managing the collections |
| ARLAS_CORS_ENABLED                     | arlas-cors-enabled                | false | whether the Cross-Origin Resource Sharing (CORS) mechanism is enabled or not |
| ARLAS_COLLECTION_AUTODISCOVER_SCHEDULE | collection-auto-discover.schedule | 0 |  Number of seconds between two auto discovery tasks |
| N/A                                    | collection-auto-discover.preferred-id-field-name | id,identifier | Name of the id field for auto discovery |
| N/A                                    | collection-auto-discover.preferred-timestamp-field-name | params.startdate |  Name of the timestamp field for auto discovery |
| N/A                                    | collection-auto-discover.preferred-centroid-field-name | geo_params.centroid |  Name of the centroid field for auto discovery |
| N/A                                    | collection-auto-discover.preferred-geometry-field-name | geo,geo_params.geometry |  Name of the geometry field for auto discovery |


### Server

| Environment variable | ARLAS Server configuration variable | Default |
| --- | --- | --- |
| ARLAS_ACCESS_LOG_FILE | server.requestLog.appenders.currentLogFilename | arlas-access.log |
| ACCESS_LOG_FILE_ARCHIVE | server.requestLog.appenders.archivedLogFilenamePattern | arlas-access-%d.log.gz |
| ARLAS_PREFIX | server.applicationContextPath | /arlas/ |
| ARLAS_ADMIN_PATH | server.adminContextPath | /admin |
| ARLAS_PORT | server.connector.port | 9999 |
| ARLAS_MAX_THREADS | server.maxThreads | 1024 |
| ARLAS_MIN_THREADS | server.minThreads | 8 |
| ARLAS_MAX_QUEUED_REQUESTS | server.maxQueuedRequests | 1024 |

### OGC

| Environment variable | ARLAS OGC Server configuration variable | Default |
| --- | --- | --- |
| ARLAS_OGC_SERVER_URI | arlas-ogc.serverUri | http://localhost:9999/arlas/ |
| ARLAS_OGC_SERVICE_PROVIDER_NAME | arlas-ogc.serviceProviderName | ARLAS |
| ARLAS_OGC_SERVICE_PROVIDER_SITE | arlas-ogc.serviceProviderSite | www.gisaia.com |
| ARLAS_OGC_SERVICE_PROVIDER_ROLE | arlas-ogc.serviceProviderRole | Developer |
| ARLAS_OGC_SERVICE_CONTACT_NAME | arlas-ogc.serviceContactIndividualName | John Doe |
| ARLAS_OGC_SERVICE_CONTACT_CITY | arlas-ogc.serviceContactAdressCity | Toulouse |
| ARLAS_OGC_SERVICE_CONTACT_CODE | arlas-ogc.serviceContactAdressPostalCode | 31000 |
| ARLAS_OGC_SERVICE_CONTACT_COUNTRY | arlas-ogc.serviceContactAdressCountry | France |
| ARLAS_OGC_QUERYMAXFEATURE | arlas-ogc.featureNamespace | 1000 |


### WFS

| Environment variable | ARLAS WFS Server configuration variable | Default |
| --- | --- | --- |
| ARLAS_WFS_FEATURE_NAMESPACE | arlas-wfs.queryMaxFeature | arlas |

### Logging

| Environment variable | ARLAS Server configuration variable | Default |
| --- | --- | --- |
| ARLAS_LOGGING_LEVEL | logging.level | INFO |
| ARLAS_LOGGING_CONSOLE_LEVEL | logging.appenders[type: console].threshold | INFO |
| ARLAS_LOGGING_FILE | logging.appenders[type: file].currentLogFilename | arlas.log |
| ARLAS_LOGGING_FILE_LEVEL | logging.appenders[type: file].threshold | INFO |
| ARLAS_LOGGING_FILE_ARCHIVE | logging.appenders[type: file].archive | true |
| ARLAS_LOGGING_FILE_ARCHIVE_FILE_PATTERN | logging.appenders[type: file].archivedLogFilenamePattern | arlas-%d.log |
| ARLAS_LOGGING_FILE_ARCHIVE_FILE_COUNT |logging.appenders[type: file].archivedFileCount  | 5 |

### Zipkin

| Environment variable | ARLAS Server configuration variable | Default |
| --- | --- | --- |
| ARLAS_ZIPKIN_ENABLED | zipkin.enabled | false |
| ARLAS_ZIPKIN_SERVICE_HOST | zipkin.serviceHost | 127.0.0.1 |
| ARLAS_ZIPKIN_COLLECTOR | zipkin.servicePort | 9999 |
| ARLAS_ZIPKIN_BASEURL | zipkin.collector | http |
| ARLAS_ZIPKIN_BASEURL | zipkin.baseUrl | http://localhost:9411 |

### JAVA

| Environment variable | Description |
| --- | --- |
| ARLAS_XMX | Java Maximum Heap Size |
