# About

ARLAS-server provides a highly simplified **REST API** for exploring data collections available in **ElasticSearch**. 
**Enhanced capabilities** are provided for collections exposing a **geometry**, a **centroid** and a **timestamp**. A **Collection API** is also provided for managing collections.

The exploration API is described [here](doc/api/API-definition.md) while the  Collection API is described [here](doc/api/API-Collection-definition.md).

# Build

```sh
mvn clean package
```

Depending on your system, you might need to install some dependencies : 

```sh
sudo apt-get install libc6-dev
sudo apt-get install libxml2-utils
```


# Run
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


# Integration Tests

## with docker containers

```sh
./tests-integration/tests-integration.sh
```

Make sure to have docker installed and running on your system and you might need to install some dependencies : 

```sh
sudo apt-get install xmlstarlet
```

Have a look to the official [elasticsearch image documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html) if you are in trouble with elasticsearch container.

## whith running elasticsearch and arlas-server

```sh
export ARLAS_HOST="localhost"; export ARLAS_PORT="9999"; export ARLAS_PREFIX="/arlas/";
export ARLAS_ELASTIC_HOST="localhost"; export ARLAS_ELASTIC_PORT=9300;
mvn clean install -DskipTests=false
```


# Optional

## Zipkin monitoring
In order to monitor the REST service performances in ZIPKIN:
- Enable zipkin in configuration.yaml
- Then:

```sh
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
java -jar zipkin.jar &
```


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
mvn clean swagger2markup:convertSwagger2markup
```
