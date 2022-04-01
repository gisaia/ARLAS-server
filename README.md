# ARLAS-server

[![Build Status](https://api.travis-ci.org/gisaia/ARLAS-server.svg?branch=develop)](https://travis-ci.org/gisaia/ARLAS-server)

ARLAS-server provides a highly simplified **REST API** for exploring data collections available in **ElasticSearch**.
**Enhanced capabilities** are provided for collections exposing a **geometry**, a **centroid** and a **timestamp**. A **Collection API** is also provided for managing collections.

## Documentation

* [Overview](http://docs.arlas.io/arlas-tech/current/arlas-api/)
* [Configuration](http://docs.arlas.io/arlas-tech/current/arlas-server-configuration/)
* [API Reference](http://docs.arlas.io/arlas-tech/current/reference/)

## Prerequisites :

### Building

ARLAS-server is a Dropwizard project. You need JDK 8 and Maven 3 to be installed.

### Running

You need a Java Runtime (JRE) 17 and an ElasticSearch server running. ARLAS is compliant with the following versions:

| ElasticSearch Version |
|  ---  |
| 7.0.1 |
| 7.1.0 |
| 7.2.1 |
| 7.3.2 |
| 7.4.2 |
| 7.5.2 |
| 7.6.2 |
| 7.7.1 |
| 7.8.1 |
| 7.9.2 |
| 7.12.1 |
| 7.14.2 |

## Build

### JAR
In order to download the project dependencies and build it :

```sh
mvn clean package
```
### Docker

```sh
docker build --tag=gisaia/arlas-server:latest --tag=arlas-server:latest .
```

## Run

### JAR

To run the project :

```sh
java -jar target/arlas-server-x.x.jar server conf/configuration.yaml
```

Then, go to `http://localhost:9999/arlas/swagger` for exploring and testing the API.

### Docker

```sh
docker run -d -p 9999:9999  -e ARLAS_ELASTIC_NODES=my-host:9200 -e ARLAS_ELASTIC_CLUSTER=elasticsearch  gisaia/arlas-server:latest
```

## Running the tests
### Integration tests
#### with docker containers

```sh
./scripts/tests-integration.sh
```

Make sure to have docker installed and running on your system and you might need to install some dependencies :

```sh
sudo apt-get install xmlstarlet
```

Have a look to the official [elasticsearch image documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html) if you are in trouble with elasticsearch container.

#### with running elasticsearch and ARLAS-server

```sh
export ARLAS_HOST="localhost"; export ARLAS_PORT="9999"; export ARLAS_PREFIX="/arlas/";
export ARLAS_ELASTIC_NODES="localhost:9200";
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

We use our own versioning schema which looks like ```21.0.0``` where :

- `21` : represents ARLAS-server API major version
- `0` : represents ARLAS-server API minor version
- `0` : represents ARLAS-server API patch version

For the available versions, see the [releases](https://github.com/gisaia/ARLAS-server/releases) on this repository.

## Authors :

- Gisaïa - *Initial work* - [Gisaïa](http://gisaia.fr/)

See also the list of [contributors](https://gitlab.com/GISAIA.ARLAS/ARLAS-server/graphs/develop) who participated in this project.

## License :

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Acknowledgments :
This project has been initiated and is maintained by Gisaïa

### The Team
- Barbet Matthieu
- Bodiguel Alain
- Bousquet Sébastien
- Dezou Laurent
- Falquier Sébastien
- Gaudan Sylvain
- Hamou Mohamed
- Thiébaud Laurent
