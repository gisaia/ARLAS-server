# About

ARLAS-server provides a highly simplified **REST API** for exploring data collections available in **ElasticSearch**. 
**Enhanced capabilities** are provided for collections exposing a **geometry**, a **centroid** and a **timestamp**. A **Collection API** is also provided for managing collections.

The exploration API is described [here](doc/api/API-definition.md) while the  Collection API is described [here](doc/api/API-Collection-definition.md).

# Build

```sh
mvn clean install
```


# Run
```sh
java -jar target/arlas-server-x.x.jar server configuration.yaml
```

Then, go to `http://localhost:9999/arlas/swagger` for exploring and testing the API.

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
markdown-pdf doc/api/API-definition.md -o doc/api/API-definition.pdf -r landscape -z doc/api/markdown2pdf.css
markdown-pdf doc/api/API-Admin-definition.md -o doc/api/API-Admin-definition.pdf -r landscape -z doc/api/markdown2pdf.css
swagger-codegen generate  -i http://localhost:9999/arlas/swagger.json  -l html2 -o doc/api/html/
```
