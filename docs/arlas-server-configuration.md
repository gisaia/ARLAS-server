# Configuring ARLAS Server running environment

## ARLAS configuration file

ARLAS server is configured with a yaml configuration file.

External module configurations are available online:

| Module     | Link                                                          |
|------------|---------------------------------------------------------------|
| Swagger    | https://github.com/federecio/dropwizard-swagger               |
| Dropwizard | http://www.dropwizard.io/1.0.4/docs/manual/configuration.html |

## Configure ARLAS Server as a docker container

#### With environment variables

ARLAS can run as a docker container. A rich set of properties of the configuration file can be overriden by passing environment variables to the container:

```shell
docker run -ti -d \
   --name arlas-server \
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

| Environment variable    | Description                                                                           |
|-------------------------|---------------------------------------------------------------------------------------|
| ARLAS_CONFIGURATION_URL | URL of the ARLAS configuration file to be downloaded by the container before starting |

For instance, if the current directory of the host contains a `configuration.yaml` file, the container can be started as follow:

```shell
docker run -ti -d \
   --name arlas-server \
   -e ARLAS_CONFIGURATION_URL="http://somemachine/conf.yaml" \
   gisaia/arlas-server:latest
```

## ARLAS configuration properties

### Authentication
Refer to [ARLAS IAM configuration](arlas-iam.md) for a comprehensive configuration guide of authentication.

| Environment variable              | ARLAS Server configuration variable | Default                               | Description                                                                          |
|-----------------------------------|-------------------------------------|---------------------------------------|--------------------------------------------------------------------------------------|
| ARLAS_AUTH_POLICY_CLASS           | arlas_auth_policy_class             | io.arlas.filter.impl.NoPolicyEnforcer | Specify a PolicyEnforcer class to load in order to activate Authentication if needed |

### CORS, HEADERS for API response

| Environment variable           | ARLAS Server configuration variable | Default                                                                                                                 | Description                                                      |
|--------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| ARLAS_CORS_ENABLED             | arlas_cors.enabled                  | false                                                                                                                   | Whether to configure cors or not                                 |
| ARLAS_CORS_ALLOWED_ORIGINS     | arlas_cors.allowed_origins          | "*"                                                                                                                     | Comma-separated list of allowed origins                          |
| ARLAS_CORS_ALLOWED_HEADERS     | arlas_cors.allowed_headers          | "arlas-user,arlas-groups,arlas-organization,X-Requested-With,Content-Type,Accept,Origin,Authorization,X-Forwarded-User" | Comma-separated list of allowed headers                          |
| ARLAS_CORS_ALLOWED_METHODS     | arlas_cors.allowed_methods          | "OPTIONS,GET,PUT,POST,DELETE,HEAD"                                                                                      | Comma-separated list of allowed methods                          |
| ARLAS_CORS_ALLOWED_CREDENTIALS | arlas_cors.allowed_credentials      | true                                                                                                                    | Whether to allow credentials or not                              |
| ARLAS_CORS_EXPOSED_HEADERS     | arlas_cors.exposed_headers          | "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location"                                     | Comma-separated list of exposed headers, readable on client side |


### Enabled Services

| Environment variable              | ARLAS Server configuration variable | Default | Description                                       |
|-----------------------------------|-------------------------------------|---------|---------------------------------------------------|
| ARLAS_SERVICE_COLLECTIONS_ENABLE  | arlas-service-collections-enabled   | true    | Whether the collection service is enabled or not  |
| ARLAS_SERVICE_EXPLORE_ENABLE      | arlas-service-explore-enabled       | true    | Whether the explore service is enabled or not     |
| ARLAS_SERVICE_WFS_ENABLE          | arlas-service-wfs-enabled           | false   | Whether the WFS service is enabled or not         |
| ARLAS_SERVICE_OPENSEARCH_ENABLE   | arlas-service-opensearch-enabled    | true    | Whether the opensearch service is enabled or not  |
| ARLAS_SERVICE_CSW_ENABLE          | arlas-service-csw-enabled           | true    | Whether the CSW service is enabled or not         |
| ARLAS_SERVICE_RASTER_TILES_ENABLE | arlas-service-raster-tiles-enabled  | false   | Whether the RASTER tile service is enabled or not |


### Datasource

| Environment variable      | ARLAS Server configuration variable | Default        | Description                                                              |
|---------------------------|-------------------------------------|----------------|--------------------------------------------------------------------------|
| ARLAS_ELASTIC_NODES       | elastic.elastic-nodes               | localhost:9200 | coma separated list of elasticsearch nodes as host:port values           |
| ARLAS_ELASTIC_SNIFFING    | elastic.elastic-sniffing            | false          | allow elasticsearch to dynamically add new hosts and remove old ones (*) |
| ARLAS_ELASTIC_INDEX       | arlas-index                         | .arlas         | name of the index that is used for storing ARLAS configuration           |
| ARLAS_ELASTIC_ENABLE_SSL  | elastic.elastic-enable-ssl          | false          | use SSL to connect to elasticsearch                                      |
| ARLAS_ELASTIC_CREDENTIALS | elastic.elastic-credentials         | user:password  | credentials to connect to elasticsearch                                  |
| ARLAS_ELASTIC_SKIP_MASTER | elastic.elastic-skip-master         | true           | Skip dedicated master in Rest client                                     |

!!! note 
    (*) Note that the IP addresses the sniffer connects to are the ones declared as the publish address in those node’s Elasticsearch config.

!!! info "Important" 
    `elastic-host` and `elastic-port` configuration variables do not longer exist in 10.6.0. You can use `elastic-nodes` instead.
    
### Collection Cache & Disovery

| Environment variable                   | ARLAS Server configuration variable                     | Default                                               | Description                                                                  |
|----------------------------------------|---------------------------------------------------------|-------------------------------------------------------|------------------------------------------------------------------------------|
| ARLAS_CACHE_FACTORY_CLASS              | Cache manager class                                     | io.arlas.server.core.impl.cache.HazelcastCacheFactory | Class to be used for the cache manager                                       |
| ARLAS_CACHE_SIZE                       | arlas-cache-size                                        | 1000                                                  | Size of the cache used for managing the collections                          |
| ARLAS_CACHE_TIMEOUT                    | arlas-cache-timeout                                     | 60                                                    | Number of seconds for the cache used for managing the collections            |
| ARLAS_REST_CACHE_TIMEOUT               | arlas-rest-cache-timeout                                | 0                                                     | Number of seconds for the cache used for managing the REST responses         |
| ARLAS_COLLECTION_AUTODISCOVER_SCHEDULE | collection-auto-discover.schedule                       | 0                                                     | Number of seconds between two auto discovery tasks                           |
| N/A                                    | collection-auto-discover.preferred-id-field-name        | id,identifier                                         | Name of the id field for auto discovery                                      |
| N/A                                    | collection-auto-discover.preferred-timestamp-field-name | params.startdate                                      | Name of the timestamp field for auto discovery                               |
| N/A                                    | collection-auto-discover.preferred-centroid-field-name  | geo_params.centroid                                   | Name of the centroid field for auto discovery                                |
| N/A                                    | collection-auto-discover.preferred-geometry-field-name  | geo,geo_params.geometry                               | Name of the geometry field for auto discovery                                |


### Server

| Environment variable      | ARLAS Server configuration variable                    | Default                | Description                                                                         |
|---------------------------|--------------------------------------------------------|------------------------|-------------------------------------------------------------------------------------|
| ARLAS_ACCESS_LOG_FILE     | server.requestLog.appenders.currentLogFilename         | arlas-access.log       |                                                                                     |
| ACCESS_LOG_FILE_ARCHIVE   | server.requestLog.appenders.archivedLogFilenamePattern | arlas-access-%d.log.gz |                                                                                     |
| ARLAS_APP_PATH            | server.applicationContextPath                          | /                      | Base URL path                                                                       |
| ARLAS_PREFIX              | server.rootPath                                        | /arlas                 | Base sub-path for **general API**, gets appended to `server.applicationContextPath` |
| ARLAS_ADMIN_PATH          | server.adminContextPath                                | /admin                 | Base sub-path for **admin API**, gets appended to `server.applicationContextPath`   |
| ARLAS_PORT                | server.connector.port                                  | 9999                   |                                                                                     |
| ARLAS_MAX_THREADS         | server.maxThreads                                      | 1024                   |                                                                                     |
| ARLAS_MIN_THREADS         | server.minThreads                                      | 8                      |                                                                                     |
| ARLAS_MAX_QUEUED_REQUESTS | server.maxQueuedRequests                               | 1024                   |                                                                                     |

### URL Masking

| Environment variable | ARLAS Server configuration variable | Default | Description                                                        |
|----------------------|-------------------------------------|---------|--------------------------------------------------------------------|
| ARLAS_BASE_URI       | arlas-base-uri                      | `None`  | Base URI to ARLAS Server. If not set, the real base URI is exposed |

### OGC

| Environment variable              | ARLAS OGC Server configuration variable  | Default             | Description                                                                                                                    |
|-----------------------------------|------------------------------------------|---------------------|--------------------------------------------------------------------------------------------------------------------------------|
| ARLAS_OGC_SERVICE_PROVIDER_NAME   | arlas-ogc.serviceProviderName            | OrganisationName    | Name of the organization responsible for the establishment, management, maintenance and distribution of the WFS & CSW services |
| ARLAS_OGC_SERVICE_PROVIDER_SITE   | arlas-ogc.serviceProviderSite            | OrganisationWebSite | A link to the site of the WFS & CSW service provider                                                                           |
| ARLAS_OGC_SERVICE_PROVIDER_ROLE   | arlas-ogc.serviceProviderRole            | pointOfContact      | Function performed by the party responsible for the WFS & CSW services                                                         |
| ARLAS_OGC_SERVICE_CONTACT_NAME    | arlas-ogc.serviceContactIndividualName   | John Doe            | The primary contact person for the WFS & CSW services provider                                                                 |
| ARLAS_OGC_SERVICE_CONTACT_MAIL    | arlas-ogc.serviceContactMail             | j.doe@mail.com      | Email of the person/organization responsible for WFS & CSW services                                                            |
| ARLAS_OGC_SERVICE_CONTACT_CITY    | arlas-ogc.serviceContactAdressCity       | Toulouse            | City of the organization responsible for WFS & CSW services                                                                    |
| ARLAS_OGC_SERVICE_CONTACT_CODE    | arlas-ogc.serviceContactAdressPostalCode | 31000               | Postal code of the organization responsible for WFS & CSW services                                                             |
| ARLAS_OGC_SERVICE_CONTACT_COUNTRY | arlas-ogc.serviceContactAdressCountry    | France              | Country of the organization responsible for WFS & CSW services                                                                 |
| ARLAS_OGC_QUERYMAXFEATURE         | arlas-ogc.queryMaxFeature                | 1000                | Maximum number of features returned by OGC queries                                                                             |

### WFS

| Environment variable        | ARLAS WFS Server configuration variable | Default | Description               |
|-----------------------------|-----------------------------------------|---------|---------------------------|
| ARLAS_WFS_FEATURE_NAMESPACE | arlas-wfs.featureNamespace              | arlas   | Namespace of WFS features |

### CSW

| Environment variable             | ARLAS CSW Server configuration variable | Default                        | Description                                                                   |
|----------------------------------|-----------------------------------------|--------------------------------|-------------------------------------------------------------------------------|
| ARLAS_CSW_TITLE                  | arlas-csw.serviceIdentificationTitle    | Discovery Service - CSW        | Title of the CSW service. It's returned in the GetCapabilities Response       |
| ARLAS_CSW_ABSTRACT               | arlas-csw.serviceIdentificationAbstract | Discovery Service - CSW        | Description of the CSW service. It's returned in the GetCapabilities Response |
| ARLAS_CSW_LANGUAGE               | arlas-csw.serviceIdentificationLanguage | eng                            | Language of the CSW title and description                                     |
| ARLAS_CSW_OPENSEARCH_DESCRIPTION | arlas-csw.openSearchDescription         | Geo-BigData Collection Catalog | Opensearch description of CSW                                                 |
| ARLAS_CSW_OPENSEARCH_SHORTNAME   | arlas-csw.openSearchShortName           | Geo-BigData Collection Catalog | Opensearch short name for CSW                                                 |

### STAC

| Environment variable      | ARLAS INSPIRE Server configuration variable | Default                             | Description                                          |
|---------------------------|---------------------------------------------|-------------------------------------|------------------------------------------------------|
| ARLAS_SERVICE_STAC_ENABLE | arlas_service_stac_enabled                  | true                                | Whether to activate STAC compliant response elements |
| ARLAS_STAC_VERSION        | arlas_stac.stac_version                     | 1.0.0                               | STAC version compliancy                              |
| ARLAS_STAC_TYPE           | arlas_stac.type                             | Catalog                             | STAC type                                            |
| ARLAS_STAC_ID             | arlas_stac.id                               | arlas                               | STAC id                                              |
| ARLAS_STAC_TITLE          | arlas_stac.title                            | Arlas Exploration                   | STAC Title                                           |
| ARLAS_STAC_DESCRIPTION    | arlas_stac.description                      | Catalog for Arlas Exploration items | STAC description                                     |

### INSPIRE

| Environment variable                    | ARLAS INSPIRE Server configuration variable | Default              | Description                                                                                                                                                                                                                                                                                                                                                                                                                        |
|-----------------------------------------|---------------------------------------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ARLAS_INSPIRE_ENABLED                   | arlas-inspire.enabled                       | false                | Whether to activate INSPIRE compliant response elements                                                                                                                                                                                                                                                                                                                                                                            |
| ARLAS_INSPIRE_SERVICES_DATE_OF_CREATION | arlas-inspire.services_date_of_creation     | 2018-11-16           | Date of creation of WFS & CSW services. It must be in YYYY-MM-DD format                                                                                                                                                                                                                                                                                                                                                            |
| ARLAS_INSPIRE_ACCESS_AND_USE_CONDITIONS | arlas-inspire.access_and_use_conditions     | no conditons apply   | Conditions applied to access and use CSW and WFS services. If no conditions apply to the access and use of the resource, `no conditions apply` shall be used. If conditions are unknown, `conditions unknown` shall be used. This element shall also provide information on any fees necessary to access and use the resource, if applicable, or refer to a uniform resource locator (URL) where information on fees is available. |
| ARLAS_INSPIRE_PUBLIC_ACCESS_LIMITATIONS | arlas-inspire.public_access_limitations     | no limitations apply | Limitations applied to access CSW. When Member States limit public access to CSW under Article 13 of Directive 2007/2/EC, this metadata element shall provide information on the limitations and the reasons for them.                                                                                                                                                                                                             |

### Logging

| Environment variable                    | ARLAS Server configuration variable                      | Default      |
|-----------------------------------------|----------------------------------------------------------|--------------|
| ARLAS_LOGGING_LEVEL                     | logging.level                                            | INFO         |
| ARLAS_LOGGING_CONSOLE_LEVEL             | logging.appenders[type: console].threshold               | INFO         |
| ARLAS_LOGGING_FILE                      | logging.appenders[type: file].currentLogFilename         | arlas.log    |
| ARLAS_LOGGING_FILE_LEVEL                | logging.appenders[type: file].threshold                  | INFO         |
| ARLAS_LOGGING_FILE_ARCHIVE              | logging.appenders[type: file].archive                    | true         |
| ARLAS_LOGGING_FILE_ARCHIVE_FILE_PATTERN | logging.appenders[type: file].archivedLogFilenamePattern | arlas-%d.log |
| ARLAS_LOGGING_FILE_ARCHIVE_FILE_COUNT   | logging.appenders[type: file].archivedFileCount          | 5            |

### JAVA

| Environment variable | Description            |
|----------------------|------------------------|
| ARLAS_XMX            | Java Maximum Heap Size |

## Helm configuration

[Helm](helm.sh) is a package management system for kubernetes applications. Its packaging format is called *charts*. A Helm chart is implemented for arlas-server.

A Helm chart comes with a set of default values. In our case, they are found in file [packaging/helm/arlas-server/values.yaml](https://github.com/gisaia/ARLAS-server/tree/develop/packaging/helm/arlas-server/values.yaml).

When installing the chart, a user can pass its own configuration value (see [this section of the official documentation](https://docs.helm.sh/chart_template_guide/#values-files)):

```
helm install -f my_values.yaml packaging/helm/arlas-server
```

Helm will perform a merge between the two configuration sets, with priority to the user values in case of conflict.

All supported configuration values can be found in [packaging/helm/arlas-server/values.yaml](https://github.com/gisaia/ARLAS-server/tree/develop/packaging/helm/arlas-server/values.yaml):

| Key                    | Default value         | Description                                                                                                                                                                                                                 |
|------------------------|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `adminPath`            | `/admin`              | Base path of the administration API.                                                                                                                                                                                        |
| `affinity`             | `{}`                  | Allows to declare preferences for the type of node on which arlas-server is to run (more info [here](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#affinity-and-anti-affinity)).                       |
| `containerPort`        | `9999`                | Port on which the arlas-server application will listen inside its container.                                                                                                                                                |
| `deployment.name`      | `arlas-server`        |                                                                                                                                                                                                                             |
| `deployment.labels`    |                       | Additional labels for the deployment.                                                                                                                                                                                       |
| `environmentVariables` |                       | YAML map of environment variables to be passed to the container.                                                                                                                                                            |
| `image.repository`     | `gisaia/arlas-server` |                                                                                                                                                                                                                             |
| `image.pullPolicy`     | `Always`              | See field `imagePullPolicy` in [kubernetes container's resource definition](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.10/#container-v1-core)                                                         |
| `imagePullSecrets`     |                       | Array of kubernetes secrets to use for authentication to private docker registries (more info in the [official documentation page](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/)). |
| `namespace`            | `default`             | Kubernetes namespace where to install the Chart's components.                                                                                                                                                               |
| `nodeSelector`         | `{}`                  | See [here](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector) for official kubernetes documentation about `nodeSelector`.                                                                     |
| `replicaCount`         | `1`                   |                                                                                                                                                                                                                             |
| `revisionHistoryLimit` |                       | See [here](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#revision-history-limit).                                                                                                                   |
| `resources`            | `{}`                  | Resource requests & limits for the pod, see [here](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/).                                                                                  |
| `service.type`         | `ClusterIP`           | See [here](https://kubernetes.io/docs/concepts/services-networking/service/#publishing-services---service-types).                                                                                                           |
| `service.port`         | `9999`                | Port on which arlas-server will be reachable through the service.                                                                                                                                                           |
| `tolerations`          | `{}`                  | Allow to have kubernetes worker nodes repelling arlas-server ("opposite" of affinity, see [here](https://kubernetes.io/docs/concepts/configuration/taint-and-toleration/) for more info).                                   |
