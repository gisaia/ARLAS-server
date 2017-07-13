# ARLAS-server

ARLAS-server provides a highly simplified **REST API** for exploring data collections available in **ElasticSearch**. 
**Enhanced capabilities** are provided for collections exposing a **geometry**, a **centroid** and a **timestamp**. A **Collection API** is also provided for managing collections.

The exploration API is described [here](doc/api/API-definition.md) while the  Collection API is described [here](doc/api/API-Collection-definition.md).

## Prerequisites :

ARLAS-server is a Dropwizard project. You need JDK 8 and Maven to be installed.

## Installing :

In order to download the project dependencies and build it :

```sh
mvn clean package
```

Depending on your system, you might need to install some dependencies : 

```sh
sudo apt-get install libc6-dev
sudo apt-get install libxml2-utils
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

#### whith running elasticsearch and arlas-server

```sh
export ARLAS_HOST="localhost"; export ARLAS_PORT="9999"; export ARLAS_PREFIX="/arlas/";
export ARLAS_ELASTIC_HOST="localhost"; export ARLAS_ELASTIC_PORT=9300;
mvn clean install -DskipTests=false
```

### Zipkin monitoring (optional)
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

## Generate API Documentation

Install swagger-codegen:
```sh
brew install swagger-codegen
```

Generate the documentation:
```sh
curl -XGET http://localhost:9999/arlas/swagger.json -o doc/api/swagger/swagger.json
curl -XGET http://localhost:9999/arlas/swagger.yaml -o doc/api/swagger/swagger.yaml
markdown-pdf doc/api/API-definition.md -o doc/api/API-definition.pdf -r landscape -z doc/api/markdown2pdf.css
markdown-pdf doc/api/API-Collection-definition.md -o doc/api/API-Collection-definition.pdf -r landscape -z doc/api/markdown2pdf.css
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l html2 -o doc/api/progapi/html/
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l typescript-angular2 -o doc/api/progapi/typescript-angular2
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l typescript-node -o doc/api/progapi/typescript-node
swagger-codegen generate  -i doc/api/swagger/swagger.json  -l typescript-fetch -o doc/api/progapi/typescript-fetch
mvn clean swagger2markup:convertSwagger2markup
```

## Contributing :

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning :

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags](https://gitlab.com/GISAIA.ARLAS/ARLAS-server/tags) on this repository.

## Authors :

- Gisaïa - *Initial work* - [Gisaïa](http://gisaia.fr/)

See also the list of [contributors](https://gitlab.com/GISAIA.ARLAS/ARLAS-server/graphs/develop) who participated in this project.

## License :

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments :
// TODO