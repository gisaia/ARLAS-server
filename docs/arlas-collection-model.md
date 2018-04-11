# ARLAS Collection model

## About

A **Collection** is an Arlas object that references your indexed data and make it explorable by **ARLAS-server**.

As **ARLAS-server** is meant to deliver spatial-temporal data analysis, your indexed data must contain *identifier*, *time*, *centroid* and *geometry* fields.

And, by referencing your data in a **Collection**, you make your REST calls lighter as ARLAS-server already knows the fields to query.


## Model

A Collection has the following structure : 

```JSON
{
 "collection_name": "string",
 "params": {
   "index_name": "string",
   "type_name": "string",
   "id_path": "string",
   "geometry_path": "string",
   "centroid_path": "string",
   "timestamp_path": "string",
   "include_fields": "string",
   "exclude_fields": "string",
   "custom_params": {},
   "atom_feed": {
      ...
   },
   "open_search": {
      ...
   }
 }
   }
```

The `atom_feed` and `open_search` nodes are optionals. The most important fields are:

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
| index_name     | Name of the index in elasticsearch                | Mandatory |
| type_name      | Name of the mapping type used within the index    | Mandatory |
| id_path        | Path to the id field in the indexed documents     | Optional  |
| geometry_path  | Path to an [Elasticsearch geometric](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/geo-shape.html) field in the indexed documents| Optional  |
| centroid_path  | Path to an [Elasticsearch Geo-point field](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/geo-point.html)  in the indexed documents. | Optional  |
| timestamp_path | Path to a timestamp/date field in the indexed documents, that meets the [Elasticsearch date format](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/mapping-date-format.html), set when indexing data. | Optional  |
| include_fields | Comma separated fields names that will be included in ARLAS-server responses. By default, all the fields are included | Optional  |
| exclude_fields | Comma separated fields names that will be excluded from ARLAS-server responses. By default, none of the fields are excluded | Optional|


## ATOM

In case the ATOM output type on a collection is used in searches, the following properties can be set to customize the result:

```JSON
   "atom_feed": {

     "author": {
       "name": "string",
       "email": "string",
       "uri": "string"
     },
     "contributor": {
       "name": "string",
       "email": "string",
       "uri": "string"
     },
     "icon": "string",
     "logo": "string",
     "rights": "string",
     "subtitle": "string",
     "generator": {
       "name": "string",
       "version": "string",
       "uri": "string"
     }
   }
```

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
|  author.name |  Author name of the feed |  Optional |
|  author.email | Author email of the feed  | Optional  |
|  author.uri |  Author URI of the feed |  Optional |
|  contributor.name |  Name of the person or other entity who contributed to the feed |  Optional |
|  contributor.email |  Email of the person or other entity who contributed to the feed |  Optional |
|  contributor.uri | URI of the person or other entity who contributed to the feed  | Optional  |
|  icon |  IRI reference (RFC3987) that identifies an image that provides iconic visual identification for a feed |  Optional |
|  logo |  IRI reference (RFC3987) that identifies an image that provides visual identification for a feed |  Optional |
|  rights |  Text that conveys information about rights held in and over an entry or feed | Optional  |
|  subtitle |  Text that conveys a human-readable description or subtitle for a feed |  Optional |
|  generator.name |  Name of the agent used to generate a feed, for debugging and other purposes | Optional  |
|  generator.version |  Version of the agent used to generate a feed, for debugging and other purposes | Optional  |
|  generator.uri |  URI of the agent used to generate a feed, for debugging and other purposes | Optional  |

## OPENSEARCH

The OPENSEARCH Description document of the collection can be customized with the following properties:

```JSON
   "open_search": {
     "short_name": "string",
     "description": "string",
     "contact": "string",
     "tags": "string",
     "long_name": "string",
     "image_height": "string",
     "image_width": "string",
     "image_type": "string",
     "image_url": "string",
     "developer": "string",
     "attribution": "string",
     "syndication_right": "string",
     "adult_content": "string",
     "language": "string",
     "input_encoding": "string",
     "output_encoding": "string",
     "url_template_prefix": "string"
   }
```

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
|  short_name |  Contains a brief human-readable title that identifies this search engine. The value must contain 16 or fewer characters of plain text. The value must not contain HTML or other markup. |  Optional |
|  description |  Contains a human-readable text description of the search engine. The value must contain 1024 or fewer characters of plain text. The value must not contain HTML or other markup. |  Optional |
|  contact |  Contains an email address at which the maintainer of the description document can be reached. The value must conform to the requirements of Section 3.4.1 "Addr-spec specification" in RFC 2822.  |  Optional |
|  tags |  Contains a set of words that are used as keywords to identify and categorize this search content. Tags must be a single word and are delimited by the space character (' '). The value must contain 256 or fewer characters of plain text. The value must not contain HTML or other markup. |  Optional |
|  long_name |  Contains an extended human-readable title that identifies this search engine. The value must contain 48 or fewer characters of plain text. The value must not contain HTML or other markup. |  Optional |
|  image_url |  Contains a URL that identifies the location of an image that can be used in association with this search content. |  Optional |
|  image_height |  Contains the height, in pixels, of this image |  Optional |
|  image_width |  Contains the width, in pixels, of this image |  Optional |
|  image_type | Contains the the MIME type of this image |  Optional |
|  developer |  Contains the human-readable name or identifier of the creator or maintainer of the description document. The value must contain 64 or fewer characters of plain text. The value must not contain HTML or other markup.  |  Optional |
|  attribution |  Contains a list of all sources or entities that should be credited for the content contained in the search feed. The value must contain 256 or fewer characters of plain text. The value must not contain HTML or other markup.  |  Optional |
|  syndication_right |  Contains a value that indicates the degree to which the search results provided by this search engine can be queried, displayed, and redistributed. The value must be one of the following strings: "open" / "limited" / "private" / "closed" |  Optional |
|  adult_content |  Contains a boolean value that should be set to true if the search results may contain material intended only for adults: true / false |  Optional |
|  language |  Contains a string that indicates that the search engine supports search results in the specified language. The value must conform to the XML 1.0 Language Identification, as specified by RFC 5646. |  Optional |
|  input_encoding |  Contains a string that indicates that the search engine supports search requests encoded with the specified character encoding. The value must conform to the XML 1.0 Character Encodings, as specified by the IANA Character Set Assignments. |  Optional |
|  output_encoding |  Contains a string that indicates that the search engine supports search responses encoded with the specified character encoding. The value must conform to the XML 1.0 Character Encodings, as specified by the IANA Character Set Assignments. |  Optional |
|  url_template_prefix |  URL Template prefix for all the OPENSEARCH URLs |  Optional |

