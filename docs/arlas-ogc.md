# ARLAS-OGC-API

ARLAS offers an OGC API that makes the ARLAS catalog interoperable with GIS applications. The catalog is explorable through two standard endpoints:

- A WFS endpoint for each collection reference that provides access to the geographic features of the collection.
- A CSW endpoint to discover the available collections. 

A third endpoint is available to get an Opensearch description of CSW service.

## URL Schema

The table below lists the URL endpoints and their ogc "parameters". The parameters are separated with the character `&`.

| PATH Template                            | Description                              |
| ---------------------------------------- | ---------------------------------------- |
| /arlas/ogc/wfs/`{collection}`?`wfsParameters`      | Download the features of the given `{collection}` |
| /arlas/ogc/csw/?`cswParameters`      | Discover the available collections in ARLAS |
| /arlas/ogc/csw/opensearch      | OpenSearch CSW Description Document |


## URL Parameters

### WFS Parameters

The following parameters are mandatory and must be specified for all WFS requests

| Parameter      | Description           | Possible values |
| -------------- | --------------------- | --------------- |
| service        | name of the service   | `WFS`           |
| version        | Supported WFS standard version | `2.0.0`|
| request        | The WFS operation to perform | Six possible values. Check the table below |


The table below sums up the WFS operations supported by ARLAS-server

| Operation             | Description           |
| ------------------    | --------------------- |
| `GetCapabilities`     | Generates a metadata document describing the WFS service provided by ARLAS-server as well as valid WFS operations and parameters |
| `DescribeFeatureType` | Returns a description of feature types supported by WFS service   |
| `GetFeature`          | Returns a selection of features from the given `{collection}` including geometry and attribute values |
| `GetPropertyValue`    | Fetches the value of a given feature property of the `{collection}`     |
| `ListStoredQueries`   | Returns the list of the stored queries of WFS server |
| `DescribeStoredQueries` | Returns a metadata document describing the stored queries of WFS server  |


According to the chosen operation, you can specify a set of parameters

#### request=GetCapabilities

You can specify a language parameter :

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| language       | Language of the metadata   |One of the official languages of the European Community expressed in conformity with ISO 639-2 | Optional |

This parameter conforms to INSPIRE recommendations.

- If the requested language is contained in the list of supported languages, then natural language metadata elements are returned in the requested language.  
- If the requested language is not supported by the WFS, then this parameter is ignored. 

#### request=DescribeFeatureType

This operation gives information about the feature type before requesting the actual data.

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| typenames      | Name of the feature type to describe   | Must contain the name of the `{collection}` | Optional |


#### request=GetFeature

This operation returns a selection of features from the given `{collection}`.

You can specify pagination parameters : 

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| count      | Maximum number of returned features   | Integer | Optional |
| startindex | Index to start the search from   | Integer | Optional |

You can use stored queries : 

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| storedquery_id | Id of a storied query in WFS server.  | `urn:ogc:def:query:OGC-WFS::GetFeatureById` | Optional |
| id             | Id of a feature to query.             | text | Mandatory if `storedquery_id` is specified |

You can query data using the following parameters

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| resourceid     | Id of a feature to query.  | text | Optional |
| bbox           | A geographic extend used to query any feature having its centroid contained within it. | `west, south, east, north` | Optional |
| filter         | A filter used to select features by evaluating an expression | **Filter Encoding** language 2.0.2 [Reference](http://docs.opengeospatial.org/is/09-026r2/09-026r2.html) | Optional |
| srsname        | crs of the features | `http://www.opengis.net/def/crs/epsg/0/4326`, `urn:ogc:def:crs:EPSG::4326` | Optional |
| typenames      | Name of the feature type to describe   | Must contain the name of the `{collection}` | Optional |

#### request=GetPropertyValue

This operation returns the value of a feature property.

You must specify the feature property :

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| valueReference | Feature type property | text | Mandatory |

You can specify pagination parameters : 

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| count      | Maximum number of returned features   | Integer | Optional |
| startindex | Index to start the search from   | Integer | Optional |

You can use stored queries : 

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| storedquery_id | Id of a storied query in WFS server.  | `urn:ogc:def:query:OGC-WFS::GetFeatureById` | Optional |
| id             | Id of a feature to query.             | text | Mandatory if `storedquery_id` is specified |

You can query data using the following parameters

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| resourceid     | Id of a feature to query.  | text | Optional |
| bbox           | A geographic extend used to query any feature having its centroid contained within it. | `west, south, east, north` | Optional |
| filter         | A filter used to select features by evaluating an expression | **Filter Encoding** language 2.0.2 [Reference](http://docs.opengeospatial.org/is/09-026r2/09-026r2.html) | Optional |
| typenames      | Name of the feature type to describe   | Must contain the name of the `{collection}` | Optional |

!!! note
   - `bbox` and `resourceid` can't be used together
   - `bbox` and `filter` can't be used together
   - `resourceid` and `filter` can't be used together
   
### CSW Parameters

The following parameters are mandatory and must be specified for all CSW requests

| Parameter      | Description           | Possible values |
| -------------- | --------------------- | --------------- |
| service        | name of the service   | `CSW`           |
| version        | Supported CSW standard version | `3.0.0`|
| request        | The CSW operation to perform | Three possible values. Check the table below |

The table below sums up the CSW operations supported by ARLAS-server

| Operation             | Description           |
| ------------------    | --------------------- |
| `GetCapabilities`     | Generates a metadata document describing the CSW service provided by ARLAS-server as well as valid CSW operations and parameters |
| `GetRecords` | Returns collection references metadata and identifier   |
| `GetRecordById`          | Returns a specific collection reference metadata and identifier |

According to the chosen operation, you can specify a set of parameters

#### request=GetCapabilities

You can specify a language parameter :

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| language (1)   | Language of the metadata   |One of the official languages of the European Community expressed in conformity with ISO 639-2 | Optional |
| sections       | Specifies which parts of the GetCapabilities response to include    |`ServiceIdentification, ServiceProvider, OperationsMetadata, Filter_Capabilities, All` | Optional |

(1) This parameter conforms to INSPIRE recommendations.

- If the requested language is contained in the list of supported languages, then natural language metadata elements are returned in the requested language.  
- If the requested language is not supported by the WFS, then this parameter is ignored. 


#### request=GetRecords & request=GetRecordById

The following parameters are common to `GetRecords` and `GetRecordById`

You can specify pagination parameters : 

| Parameter      | Description           | Possible values | Mandatory/Optional |
| -------------- | --------------------- | --------------- | ------------------ |
| maxRecords      | Maximum number of returned collections   | Integer | Optional |
| startposition | Index to start the search from   | Integer | Optional |

You can choose which metadata to return : 

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| elementName    | Metadata elements to return (1).  | Comma separated metadata elements. For instance : `title,subject`. | Optional |
| elementSetName | Group of metada elements to return.             | `brief`, `summary` (default) or `full` | Optional |
| typenames      | Defines the level of details present in the result set. | `Record`, `csw:Record` or `gmd:MD_Metadata` | Optional |

(1) Possible values of `elementName` are : `title, abstract, identifier, subject, creator, publisher, contributor, type, format, source, language, modified, boundingbox`.

!!! note
   - `elementName` and `elementSetName` can't be used together

You can choose the format :

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| outputFormat   | Whether to return response in `application/xml` or `application/atom+xml` format | `application/xml` (default), `application/atom%2Bxml` | Optional |
| outputSchema   | The schema of output of GetRecords/GetRecordById response | `http://www.opengis.net/cat/csw/3.0.0` (default) or `http://www.isotc211.org/2005/gmd` | Optional |

You can query collection references metadata using the following parameters

| Parameter      | Description                           | Possible values | Mandatory/Optional |
| -------------- | ------------------------------------- | --------------- | ------------------ |
| recordIds      | Comma separated ids of collections  | text | Optional |
| id             | id of a collection  | text | Mandatory for `GetRecordById` |
| q              | Full text search on collection references  | text | Optional |
| bbox           | A geographic extend used to query any collection that intersects it. | `west, south, east, north` | Optional |
| constraint     | A filter used to select collections references by evaluating an expression | **Filter Encoding** language 2.0.2 [Reference](http://docs.opengeospatial.org/is/09-026r2/09-026r2.html) | Optional |
| constraintLanguage | Language of the constraint  | `Filter` | Optional |

!!! note
   - `bbox` and `recordids` can't be used together
   - `recordids` and `q` can't be used together
