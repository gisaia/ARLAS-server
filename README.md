# ARLAS-server

ARLAS-server provides a highly simplified **REST API** for exploring data collections available in **ElasticSearch**.
**Enhanced capabilities** are provided for collections exposing a **geometry**, a **centroid** and a **timestamp**. A **Collection API** is also provided for managing collections.

The exploration API is described [here](doc/api/API-definition.md) while the  Collection API is described [here](doc/api/API-Collection-definition.md).

## Prerequisites :

### Building

ARLAS-server is a Dropwizard project. You need JDK 8 and Maven 3 to be installed.

### Running

You need a Java Runtime (JRE) 8 and an ElasticSearch server running. ARLAS is compliant with the following versions:

| ElasticSearch Version |
|  ---  |
| 5.0.2 |
| 5.1.2 |
| 5.2.2 |
| 5.3.3 |
| 5.4.3 |
| 5.5.1 |

## Installing :

In order to download the project dependencies and build it :

```sh
mvn clean package
```

To run the project :

```sh
java -jar target/arlas-server-x.x.jar server conf/configuration.yaml
```

Then, go to `http://localhost:9999/arlas/swagger` for exploring and testing the API.

To manage collections :

```sh
curl -X PUT --header 'Content-Type: application/json;charset=utf-8' --header 'Accept: application/json' -d '{ \
   "index_name": "myindex", \
   "type_name": "mytype", \
   "id_path": "mydoc.id", \
   "geometry_path": "mydoc.geometry", \
   "centroid_path": "mydoc.centroid", \
   "timestamp_path": "mydoc.timestamp" \
 }' 'http://localhost:9999/arlas/collections/mycollection'
curl -X GET --header 'Accept: application/json' 'http://localhost:9999/arlas/collections'
curl -X GET --header 'Accept: application/json' 'http://localhost:9999/arlas/collections/mycollection'
curl -X DELETE --header 'Accept: application/json' 'http://localhost:9999/arlas/collections/mycollection'
```

You can find more examples about how to use ARLAS-server in [examples/EXAMPLE.md](examples/EXAMPLE.md)

## Running the tests
### Integration tests
#### with docker containers

```sh
./tests-integration/tests-integration.sh
```

Make sure to have docker installed and running on your system and you might need to install some dependencies :

```sh
sudo apt-get install xmlstarlet
```

Have a look to the official [elasticsearch image documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html) if you are in trouble with elasticsearch container.

#### with running elasticsearch and ARLAS-server

```sh
export ARLAS_HOST="localhost"; export ARLAS_PORT="9999"; export ARLAS_PREFIX="/arlas/";
export ARLAS_ELASTIC_HOST="localhost"; export ARLAS_ELASTIC_PORT=9300;
mvn clean install -DskipTests=false
```

### [OPTIONAL] Zipkin monitoring
In order to monitor the REST service performances in ZIPKIN:
- Enable zipkin in configuration.yaml
- Then:

```sh
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
java -jar zipkin.jar &
```

## Built with :

- [Dropwizard](http://www.dropwizard.io) - The web framework used.
- [Maven](https://maven.apache.org/) - Dependency Management.
- [Elasticsearch](https://www.elastic.co/) -  A distributed, RESTful search and analytics engine

## Contributing :

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning :

We use our own versioning schema which looks like ```1.5.6``` where :

- `1` : represents ARLAS-server API version
- `5` : represents Elasticsearch compliance (see below for value correspondence)
- `6` : represents ARLAS-server incremental version

For Elasticsearch compliance, values currently supported are :

- `5` : indicates that this version is compliant with Elasticsearch from version 5.0 to 5.5

For the versions available, see the [tags](https://gitlab.com/GISAIA.ARLAS/ARLAS-server/tags) on this repository.

## Authors :

- Gisaïa - *Initial work* - [Gisaïa](http://gisaia.fr/)

See also the list of [contributors](https://gitlab.com/GISAIA.ARLAS/ARLAS-server/graphs/develop) who participated in this project.

## License :

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Acknowledgments :
This project has been initiated and is maintained by Gisaïa

### The Team
- Barbet Matthieu
- Bousquet Sébastien
- Dezou Laurent
- Falquier Sébastien
- Gaudan Sylvain
- Hamou Mohamed
