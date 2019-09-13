# ARLAS Collection model

## About

A **Collection** is an Arlas object that references your indexed data and make it explorable by **ARLAS-server**.

As **ARLAS-server** is meant to deliver spatial-temporal data analysis, your indexed data must contain *identifier*, *time*, *centroid* and *geometry* fields.

And, by referencing your data in a **Collection**, you make your REST calls lighter as ARLAS-server already knows the fields to query.


## Model

A Collection has the following structure : 

```json
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
    "dublin_core_element_name": {
      ...
    },
    "inspire": {
      ...
    },
    "open_search": {
      ...
    }
    "raster_tile_url": {
      ...
    },
    "raster_tile_width":"integer",
    "raster_tile_height":"integer"
  }
}
```

The `atom_feed`, `dublin_core_element_name`, `inspire`, `open_search` and `ratser_tiles` nodes are optionals.

The most important fields are:

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
| index_name     | Name of the index in elasticsearch                | Mandatory |
| type_name      | Name of the mapping type used within the index    | Optional (default to `_doc`) |
| id_path        | Path to the id field in the indexed documents     | Optional  |
| geometry_path  | Path to an [Elasticsearch geometric](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/geo-shape.html) field in the indexed documents| Optional  |
| centroid_path  | Path to an [Elasticsearch Geo-point field](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/geo-point.html)  in the indexed documents. | Optional  |
| timestamp_path | Path to a timestamp/date field in the indexed documents, that meets the [Elasticsearch date format](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/mapping-date-format.html), set when indexing data. | Optional  |
| include_fields | Comma separated fields names that will be included in ARLAS-server responses. By default, all the fields are included | Optional  |
| exclude_fields | Comma separated fields names that will be excluded from ARLAS-server responses. By default, none of the fields are excluded | Optional|
| raster_tile_width | In case the tile is too big, the crop width to apply. Set to -1 if not check must be applied |  Optional |  -1 |
| raster_tile_height | In case the tile is too big, the crop height to apply. Set to -1 if not check must be applied  |  Optional |  -1 |
| taggable_fields| Comma separated fields names/paths that are allowed to be updated by the [tag service](arlas-api-tagging). By default no field is taggable| Optional|
| update_max_hits | Maximum number of hits you can tag with one `tag request` | Optional|

!!! info "Important 1"
    Starting from 11.7.0 `type_name` is optional (defaults to `_doc`).
    
!!! info "Important 2"
    Taggable fields paths should not contain `tags`. It's a reserved word.
    
!!! info "Important 3"
    Taggable fields must initially be set to a value or to null at index time in ES.
    For instance, if you use [`Logstash`](https://www.elastic.co/products/logstash) to index in ES, you can add in this line in Logstash config file to set the field values to null
    
    ```
        ruby {
            code => "event.set('[labels][status]', nil);"
        }
    ```
    `labels.status` being the taggable field.

!!! info "Important 4"
    `geometry_path` field value must have the same format in all documents within the same collection: It's not supported to index documents where `geometry_path` value is WKT and others as GeoJson. Otherwise a ParseException will be thrown. It goes the same for `centroid_path`.

## ATOM

In case the ATOM output type on a collection is used in searches, the following properties can be set to customize the result:

```json
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

## DUBLIN CORE

The Dublin Core Description document of the collection can be customized with the following properties:

```json
   "dublin_core_element_name": {
       "title": "string",
       "creator": "string",
       "subject": "string",
       "description": "string",
       "publisher": "string",
       "contributor": "string",
       "type": "string",
       "format": "string",
       "identifier": "string",
       "source": "string",
       "language": "string",
       "bbox": {
         "north": 0,
         "south": 0,
         "east": 0,
         "west": 0
       },
       "date": "string",
       "coverage": {
         "additionalProp1": {},
         "additionalProp2": {},
         "additionalProp3": {}
       },
       "coverage_centroid": "string"
     }
```

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
|  title |  Title by which the resource is formally known |  Optional |
|  creator |  Entity responsible for making the content of the resource  | Optional  |
|  subject |  The topic of the content of the resource. |  Optional |
|  description|  An account of the content of the resource |  Optional |
|  publisher |  Entity responsible for making the resource available |  Optional |
|  contributor | Entity responsible for making contributions to the content of the resource  | Optional  |
|  type |  The nature or genre of the content of the resource. |  Optional |
|  format |  The physical or digital manifestation of the resource |  Optional |
|  identifier |  An unambiguous reference to the resource within a given context. | Generated by ARLAS-server  |
|  source |  A Reference to a resource from which the present resource is derived |  Optional |
|  language |  A language of the intellectual content of the resource (taken from the ISO 639 standard) | Optional  |
|  bbox |  Geographical extent of the resource content. Default to all globe | Optional  |
|  date |  A date associated with an event in the life cycle of the resource | Generated by ARLAS-server at collection creation  |
|  coverage |  Geographical extent of the resource content. | Calculated by ARLAS-server from `bbox` attribute  |
|  coverage_centroid |  Centroid of the geographical extent of the resource content. | Calculated by ARLAS-server from `bbox` attribute  |


## INSPIRE

In case the INSPIRE option is enabled in `configuration.yaml`, the following properties can be set to customize the result of WFS GetCapabilities and CSW GetCapabilities, GetRecords & GetRecordById:

```json
   "inspire": {
        "keywords": [
        {
          "value": "string",
          "vocabulary": "string",
          "date_of_publication": "string"
        }
        ],
        "languages": ["eng", ...],
        "topic_categories": "string",
        "lineage": "string",
        "spatial_resolution": {
          "value": "number",
          "unit_of_measure"
        }
        "inspire_uri": {
        "code": "string",
        "namespace": "string"
        },
        "inspire_use_conditions": "string",
        "inspire_limitation_access": {
        "access_constraints": "string",
        "otherConstraints": "string",
        "classification": "string"
        }
   }
```
The `inspire` node is mandatory only if INSPIRE option is enabled in `configuration.yaml`

| Attribute      | Description                                       | Mention   |
| ---------------| ------------------------------------------------- | --------- |
|  keywords.value |  Value of the keyword. If the keyword is originated from [Classification of Spatial data Services vocabulary](http://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceCategory), then the camel-case keyword should be set. For example : `thematicImageProcessingService` |  Mandatory |
|  keywords.vocabulary | Vocabulary from which the keyword value was taken. For example [GEMET Inspire-themes](https://www.eionet.europa.eu/gemet/en/inspire-themes/) or [Classification of Spatial data Services vocabulary](http://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceCategory)  | Mandatory for each keyword if the keyword value originates from a controlled vocabulary  |
|  keywords.date_of_publication |  Date of publication of the Vocabulary. Must be in `YYYY-MM-DD` format. |  Optional |
|  languages |  The language(s) used within the resource. The value domain of this metadata element is limited to the languages defined in ISO 639-2. |  Mandatory if the resource includes textual information. |
|  topic_categories |  List of topic categories. A topic category is a high-level classification scheme to assist in the grouping and topic-based search of available spatial data resources. Must be one of the values in [this list](http://inspire.ec.europa.eu/metadata-codelist/TopicCategory). The value should be in camel-case. For example, the topic category of [Climatology / Meteorology / Atmosphere](http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/climatologyMeteorologyAtmosphere), must be set as : `climatologyMeteorologyAtmosphere` |  Mandatory |
|  lineage | (Free text) This is a statement on process history and/or overall quality of the spatial data set. Where appropriate it may include a statement whether the data set has been validated or quality assured, whether it is the official version (if multiple versions exist) |  Mandatory |
|  spatial_resolution | Spatial resolution refers to the level of detail of the data set. It shall be expressed as a set of zero to many resolution distances (typically for gridded data and imagery-derived products) or equivalent scales (typically for maps or map-derived product |  Mandatory if an equivalent scale or a resolution distance can be specified |
|  spatial_resolution.value | An equivalent scale is expressed as an integer value expressing the scale denominator. A resolution distance should be expressed as a numerical value associated with a unit of length. |  Mandatory if an equivalent scale or a resolution distance can be specified |
|  spatial_resolution.unit_of_value | Unit of measure of the resolution distance. If it is not specified, that means spatial resolution is an equivalent scale |  Mandatory resolution distance can be specified |
|  inspire_uri.code |  A character string code uniquely identifying the collection reference (data set), assigned by the data owner. |  Mandatory. If not set, it takes the id value generated by ARLAS-server |
|  inspire_uri.namespace |  A character string namespace uniquely identifying the context of the identifier code (for example, the data owner). By default its value is `ARLAS.{COLLECTION-NAME}`|  Optional |
|  inspire_use_conditions | Provides information on any fees necessary to access and use the data set  | Optional; default :  `no conditions apply`|
|  inspire_limitation_access.access_constraints* | Possible values : `copyright`, `patent`, `patentPending`, `trademark`, `license`, `intellectualPropertyRights`, `restricted`, `otherRestrictions`.  | Mandatory. Default value is  `otherRestrictions`  |
|  inspire_limitation_access.otherConstraints* |  Free text or specify a URL to a link that describes eventual limitations. Default value : `no limitations apply` |  Optional |
|  inspire_limitation_access.classification* |  Name of the handling restrictions on the WFS. One of: `unclassified` (default value), `restricted`, `confidential`, `secret`, `topSecret` |  Optional |

*: `inspire_limitation_access` object describes access constraints applied to assure the protection of privacy or intellectual property, and any special restrictions or limitations on obtaining the Inspire compliant WFS.


## OPENSEARCH

The OPENSEARCH Description document of the collection can be customized with the following properties:

```json
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
|  url_template_prefix |  Is the url prefix that is rendered in the url template of Opensearch response. |  Optional |

## RASTER TILES

If the data in the collection are metadata of images and the images are available as 256x256 px tiles through a WMTS or X/Y/Z service (top left corner is 0/0), 
then ARLAS can, for a given tile, dynamically stack the tiles aligned with the requested one and for the images matching a given filter. 
In case the tile server does not provide the tiles always with the right size (too big), then you can set the `raster_tile_width` and `raster_tile_height` to the desired dimensions, such as 256 or 512. A crop operation is done to meet the right size when the tile is too big. The "too small" case is not handled.

```json
   "raster_tile_url": {
     "url": "string",
     "id_path": "string",
     "min_z": "integer",
     "max_z": "integer",
     "check_geometry": "boolean"
   }
```

| Attribute      | Description                                       | Mention   | Default value   |
| ---------------| ------------------------------------------------- | --------- | --------- |
|  url |  The URL pattern of the WMTS or X/Y/Z service. It should contain variable place holders for `{x}`, `{y}`, `{z}` and `{id}`  |  Mandatory |   |
|  id_path |  JSON path of the image id that will be injected in the URL of the tile service |  optional | id  |
|  min_z | Min zoom supported by the tile service  |  Optional |  0 |
|  max_z | Max zoom supported by the tile service  |  Optional |  18 |
|  check_geometry | Whether ARLAS should check that the matching images have their geometry intersecting the requested tile. Usefull if the search returns false positives on geometric queries | Optional  | false |