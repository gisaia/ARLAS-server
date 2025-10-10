<!-- Generator: Widdershins v4.0.1 -->

<h1 id="arlas-exploration-api">ARLAS Exploration API v23.0.8-security.2</h1>

> Scroll down for example requests and responses.

Explore the content of ARLAS collections

Base URLs:

* <a href="/arlas">/arlas</a>

Email: <a href="mailto:contact@gisaia.com">Gisaia</a> Web: <a href="http://www.gisaia.com/">Gisaia</a> 
License: <a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache 2.0</a>

<h1 id="arlas-exploration-api-collections">collections</h1>

## Get a collection reference

<a id="opIdget"></a>

`GET /collections/{collection}`

Get a collection reference in ARLAS

<h3 id="get-a-collection-reference-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
{
  "collection_name": "string",
  "params": {
    "index_name": "string",
    "id_path": "string",
    "geometry_path": "string",
    "centroid_path": "string",
    "h3_path": "string",
    "timestamp_path": "string",
    "exclude_fields": "string",
    "update_max_hits": 0,
    "taggable_fields": "string",
    "exclude_wfs_fields": "string",
    "custom_params": {
      "property1": "string",
      "property2": "string"
    },
    "display_names": {
      "collection": "string",
      "fields": {
        "property1": "string",
        "property2": "string"
      },
      "shape_columns": {
        "property1": "string",
        "property2": "string"
      }
    },
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
    },
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
      "output_encoding": "string"
    },
    "inspire": {
      "keywords": [
        {
          "value": "string",
          "vocabulary": "string",
          "date_of_publication": "string"
        }
      ],
      "topic_categories": [
        "string"
      ],
      "lineage": "string",
      "languages": [
        "string"
      ],
      "spatial_resolution": {
        "value": {},
        "unit_of_measure": "string"
      },
      "inspire_uri": {
        "code": "string",
        "namespace": "string"
      },
      "inspire_limitation_access": {
        "access_constraints": "string",
        "other_constraints": "string",
        "classification": "string"
      },
      "inspire_use_conditions": "string"
    },
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
        "north": 0.1,
        "south": 0.1,
        "east": 0.1,
        "west": 0.1
      },
      "date": "string",
      "coverage": {
        "property1": {},
        "property2": {}
      },
      "coverage_centroid": "string"
    },
    "raster_tile_url": {
      "url": "string",
      "id_path": "string",
      "min_z": 0,
      "max_z": 0,
      "check_geometry": true
    },
    "raster_tile_width": 0,
    "raster_tile_height": 0,
    "filter": {
      "f": [
        [
          {
            "field": "string",
            "op": "eq",
            "value": "string"
          }
        ]
      ],
      "q": [
        [
          "string"
        ]
      ],
      "dateformat": "string",
      "righthand": true
    },
    "license_name": "string",
    "license_urls": [
      "string"
    ]
  }
}
```

<h3 id="get-a-collection-reference-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[CollectionReference](#schemacollectionreference)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Collection not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Add a collection reference

<a id="opIdput"></a>

`PUT /collections/{collection}`

Add a collection reference in ARLAS

> Body parameter

```json
{
  "index_name": "string",
  "id_path": "string",
  "geometry_path": "string",
  "centroid_path": "string",
  "h3_path": "string",
  "timestamp_path": "string",
  "exclude_fields": "string",
  "update_max_hits": 0,
  "taggable_fields": "string",
  "exclude_wfs_fields": "string",
  "custom_params": {
    "property1": "string",
    "property2": "string"
  },
  "display_names": {
    "collection": "string",
    "fields": {
      "property1": "string",
      "property2": "string"
    },
    "shape_columns": {
      "property1": "string",
      "property2": "string"
    }
  },
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
  },
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
    "output_encoding": "string"
  },
  "inspire": {
    "keywords": [
      {
        "value": "string",
        "vocabulary": "string",
        "date_of_publication": "string"
      }
    ],
    "topic_categories": [
      "string"
    ],
    "lineage": "string",
    "languages": [
      "string"
    ],
    "spatial_resolution": {
      "value": {},
      "unit_of_measure": "string"
    },
    "inspire_uri": {
      "code": "string",
      "namespace": "string"
    },
    "inspire_limitation_access": {
      "access_constraints": "string",
      "other_constraints": "string",
      "classification": "string"
    },
    "inspire_use_conditions": "string"
  },
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
      "north": 0.1,
      "south": 0.1,
      "east": 0.1,
      "west": 0.1
    },
    "date": "string",
    "coverage": {
      "property1": {},
      "property2": {}
    },
    "coverage_centroid": "string"
  },
  "raster_tile_url": {
    "url": "string",
    "id_path": "string",
    "min_z": 0,
    "max_z": 0,
    "check_geometry": true
  },
  "raster_tile_width": 0,
  "raster_tile_height": 0,
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "license_name": "string",
  "license_urls": [
    "string"
  ]
}
```

<h3 id="add-a-collection-reference-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|checkfields|query|boolean|false|none|
|body|body|[CollectionReferenceParameters](#schemacollectionreferenceparameters)|true|collectionParams|

> Example responses

> 200 Response

```json
{
  "collection_name": "string",
  "params": {
    "index_name": "string",
    "id_path": "string",
    "geometry_path": "string",
    "centroid_path": "string",
    "h3_path": "string",
    "timestamp_path": "string",
    "exclude_fields": "string",
    "update_max_hits": 0,
    "taggable_fields": "string",
    "exclude_wfs_fields": "string",
    "custom_params": {
      "property1": "string",
      "property2": "string"
    },
    "display_names": {
      "collection": "string",
      "fields": {
        "property1": "string",
        "property2": "string"
      },
      "shape_columns": {
        "property1": "string",
        "property2": "string"
      }
    },
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
    },
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
      "output_encoding": "string"
    },
    "inspire": {
      "keywords": [
        {
          "value": "string",
          "vocabulary": "string",
          "date_of_publication": "string"
        }
      ],
      "topic_categories": [
        "string"
      ],
      "lineage": "string",
      "languages": [
        "string"
      ],
      "spatial_resolution": {
        "value": {},
        "unit_of_measure": "string"
      },
      "inspire_uri": {
        "code": "string",
        "namespace": "string"
      },
      "inspire_limitation_access": {
        "access_constraints": "string",
        "other_constraints": "string",
        "classification": "string"
      },
      "inspire_use_conditions": "string"
    },
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
        "north": 0.1,
        "south": 0.1,
        "east": 0.1,
        "west": 0.1
      },
      "date": "string",
      "coverage": {
        "property1": {},
        "property2": {}
      },
      "coverage_centroid": "string"
    },
    "raster_tile_url": {
      "url": "string",
      "id_path": "string",
      "min_z": 0,
      "max_z": 0,
      "check_geometry": true
    },
    "raster_tile_width": 0,
    "raster_tile_height": 0,
    "filter": {
      "f": [
        [
          {
            "field": "string",
            "op": "eq",
            "value": "string"
          }
        ]
      ],
      "q": [
        [
          "string"
        ]
      ],
      "dateformat": "string",
      "righthand": true
    },
    "license_name": "string",
    "license_urls": [
      "string"
    ]
  }
}
```

<h3 id="add-a-collection-reference-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[CollectionReference](#schemacollectionreference)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|JSON parameter malformed.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found Error.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Delete a collection reference

<a id="opIddelete"></a>

`DELETE /collections/{collection}`

Delete a collection reference in ARLAS

<h3 id="delete-a-collection-reference-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
{
  "status": 0,
  "message": "string"
}
```

<h3 id="delete-a-collection-reference-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[Success](#schemasuccess)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Collection not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Get all collection references

<a id="opIdgetAll"></a>

`GET /collections`

Get all collection references in ARLAS

<h3 id="get-all-collection-references-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
[
  {
    "collection_name": "string",
    "params": {
      "index_name": "string",
      "id_path": "string",
      "geometry_path": "string",
      "centroid_path": "string",
      "h3_path": "string",
      "timestamp_path": "string",
      "exclude_fields": "string",
      "update_max_hits": 0,
      "taggable_fields": "string",
      "exclude_wfs_fields": "string",
      "custom_params": {
        "property1": "string",
        "property2": "string"
      },
      "display_names": {
        "collection": "string",
        "fields": {
          "property1": "string",
          "property2": "string"
        },
        "shape_columns": {
          "property1": "string",
          "property2": "string"
        }
      },
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
      },
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
        "output_encoding": "string"
      },
      "inspire": {
        "keywords": [
          {
            "value": "string",
            "vocabulary": "string",
            "date_of_publication": "string"
          }
        ],
        "topic_categories": [
          "string"
        ],
        "lineage": "string",
        "languages": [
          "string"
        ],
        "spatial_resolution": {
          "value": {},
          "unit_of_measure": "string"
        },
        "inspire_uri": {
          "code": "string",
          "namespace": "string"
        },
        "inspire_limitation_access": {
          "access_constraints": "string",
          "other_constraints": "string",
          "classification": "string"
        },
        "inspire_use_conditions": "string"
      },
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
          "north": 0.1,
          "south": 0.1,
          "east": 0.1,
          "west": 0.1
        },
        "date": "string",
        "coverage": {
          "property1": {},
          "property2": {}
        },
        "coverage_centroid": "string"
      },
      "raster_tile_url": {
        "url": "string",
        "id_path": "string",
        "min_z": 0,
        "max_z": 0,
        "check_geometry": true
      },
      "raster_tile_width": 0,
      "raster_tile_height": 0,
      "filter": {
        "f": [
          [
            {
              "field": "string",
              "op": "eq",
              "value": "string"
            }
          ]
        ],
        "q": [
          [
            "string"
          ]
        ],
        "dateformat": "string",
        "righthand": true
      },
      "license_name": "string",
      "license_urls": [
        "string"
      ]
    }
  }
]
```

<h3 id="get-all-collection-references-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|Inline|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<h3 id="get-all-collection-references-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[[CollectionReference](#schemacollectionreference)]|false|none|none|
|» collection_name|string|true|none|none|
|» params|[CollectionReferenceParameters](#schemacollectionreferenceparameters)|true|none|none|
|»» index_name|string|true|none|none|
|»» id_path|string|true|none|none|
|»» geometry_path|string|true|none|none|
|»» centroid_path|string|true|none|none|
|»» h3_path|string|false|none|none|
|»» timestamp_path|string|true|none|none|
|»» exclude_fields|string|false|none|none|
|»» update_max_hits|integer(int32)|false|none|none|
|»» taggable_fields|string|false|none|none|
|»» exclude_wfs_fields|string|false|none|none|
|»» custom_params|object|false|none|none|
|»»» **additionalProperties**|string|false|none|none|
|»» display_names|[CollectionDisplayNames](#schemacollectiondisplaynames)|false|none|none|
|»»» collection|string|false|none|none|
|»»» fields|object|false|none|none|
|»»»» **additionalProperties**|string|false|none|none|
|»»» shape_columns|object|false|none|none|
|»»»» **additionalProperties**|string|false|none|none|
|»» atom_feed|[Feed](#schemafeed)|false|none|none|
|»»» author|[Person](#schemaperson)|false|none|none|
|»»»» name|string|false|none|none|
|»»»» email|string|false|none|none|
|»»»» uri|string|false|none|none|
|»»» contributor|[Person](#schemaperson)|false|none|none|
|»»» icon|string|false|none|none|
|»»» logo|string|false|none|none|
|»»» rights|string|false|none|none|
|»»» subtitle|string|false|none|none|
|»»» generator|[Generator](#schemagenerator)|false|none|none|
|»»»» name|string|false|none|none|
|»»»» version|string|false|none|none|
|»»»» uri|string|false|none|none|
|»» open_search|[OpenSearch](#schemaopensearch)|false|none|none|
|»»» short_name|string|false|none|none|
|»»» description|string|false|none|none|
|»»» contact|string|false|none|none|
|»»» tags|string|false|none|none|
|»»» long_name|string|false|none|none|
|»»» image_height|string|false|none|none|
|»»» image_width|string|false|none|none|
|»»» image_type|string|false|none|none|
|»»» image_url|string|false|none|none|
|»»» developer|string|false|none|none|
|»»» attribution|string|false|none|none|
|»»» syndication_right|string|false|none|none|
|»»» adult_content|string|false|none|none|
|»»» language|string|false|none|none|
|»»» input_encoding|string|false|none|none|
|»»» output_encoding|string|false|none|none|
|»» inspire|[Inspire](#schemainspire)|false|none|none|
|»»» keywords|[[Keyword](#schemakeyword)]|false|none|none|
|»»»» value|string|false|none|none|
|»»»» vocabulary|string|false|none|none|
|»»»» date_of_publication|string|false|none|none|
|»»» topic_categories|[string]|false|none|none|
|»»» lineage|string|false|none|none|
|»»» languages|[string]|false|none|none|
|»»» spatial_resolution|[InspireSpatialResolution](#schemainspirespatialresolution)|false|none|none|
|»»»» value|[Number](#schemanumber)|false|none|none|
|»»»» unit_of_measure|string|false|none|none|
|»»» inspire_uri|[InspireURI](#schemainspireuri)|false|none|none|
|»»»» code|string|false|none|none|
|»»»» namespace|string|false|none|none|
|»»» inspire_limitation_access|[InspireLimitationAccess](#schemainspirelimitationaccess)|false|none|none|
|»»»» access_constraints|string|false|none|none|
|»»»» other_constraints|string|false|none|none|
|»»»» classification|string|false|none|none|
|»»» inspire_use_conditions|string|false|none|none|
|»» dublin_core_element_name|[DublinCoreElementName](#schemadublincoreelementname)|false|none|none|
|»»» title|string|false|none|none|
|»»» creator|string|false|none|none|
|»»» subject|string|false|none|none|
|»»» description|string|false|none|none|
|»»» publisher|string|false|none|none|
|»»» contributor|string|false|none|none|
|»»» type|string|false|none|none|
|»»» format|string|false|none|none|
|»»» identifier|string|false|none|none|
|»»» source|string|false|none|none|
|»»» language|string|false|none|none|
|»»» bbox|[Bbox](#schemabbox)|false|none|none|
|»»»» north|number(double)|true|none|none|
|»»»» south|number(double)|true|none|none|
|»»»» east|number(double)|true|none|none|
|»»»» west|number(double)|true|none|none|
|»»» date|string|false|none|none|
|»»» coverage|object|false|none|none|
|»»»» **additionalProperties**|object|false|none|none|
|»»» coverage_centroid|string|false|none|none|
|»» raster_tile_url|[RasterTileURL](#schemarastertileurl)|false|none|none|
|»»» url|string|true|none|none|
|»»» id_path|string|true|none|none|
|»»» min_z|integer(int32)|false|none|none|
|»»» max_z|integer(int32)|false|none|none|
|»»» check_geometry|boolean|false|none|none|
|»» raster_tile_width|integer(int32)|false|none|none|
|»» raster_tile_height|integer(int32)|false|none|none|
|»» filter|[Filter](#schemafilter)|false|none|none|
|»»» f|[array]|false|none|none|
|»»»» field|string|false|none|none|
|»»»» op|string|false|none|none|
|»»»» value|string|false|none|none|
|» q|[array]|false|none|none|
|» dateformat|string|false|none|none|
|» righthand|boolean|false|none|none|
|license_name|string|false|none|none|
|license_urls|[string]|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|op|eq|
|op|gte|
|op|gt|
|op|lte|
|op|lt|
|op|like|
|op|ne|
|op|range|
|op|within|
|op|notwithin|
|op|intersects|
|op|notintersects|

<aside class="success">
This operation does not require authentication
</aside>

## Get all collection references as a json file

<a id="opIdexportCollections"></a>

`GET /collections/_export`

Get all collection references in ARLAS as json file

> Example responses

> 200 Response

```json
[
  {
    "collection_name": "string",
    "params": {
      "index_name": "string",
      "id_path": "string",
      "geometry_path": "string",
      "centroid_path": "string",
      "h3_path": "string",
      "timestamp_path": "string",
      "exclude_fields": "string",
      "update_max_hits": 0,
      "taggable_fields": "string",
      "exclude_wfs_fields": "string",
      "custom_params": {
        "property1": "string",
        "property2": "string"
      },
      "display_names": {
        "collection": "string",
        "fields": {
          "property1": "string",
          "property2": "string"
        },
        "shape_columns": {
          "property1": "string",
          "property2": "string"
        }
      },
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
      },
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
        "output_encoding": "string"
      },
      "inspire": {
        "keywords": [
          {
            "value": "string",
            "vocabulary": "string",
            "date_of_publication": "string"
          }
        ],
        "topic_categories": [
          "string"
        ],
        "lineage": "string",
        "languages": [
          "string"
        ],
        "spatial_resolution": {
          "value": {},
          "unit_of_measure": "string"
        },
        "inspire_uri": {
          "code": "string",
          "namespace": "string"
        },
        "inspire_limitation_access": {
          "access_constraints": "string",
          "other_constraints": "string",
          "classification": "string"
        },
        "inspire_use_conditions": "string"
      },
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
          "north": 0.1,
          "south": 0.1,
          "east": 0.1,
          "west": 0.1
        },
        "date": "string",
        "coverage": {
          "property1": {},
          "property2": {}
        },
        "coverage_centroid": "string"
      },
      "raster_tile_url": {
        "url": "string",
        "id_path": "string",
        "min_z": 0,
        "max_z": 0,
        "check_geometry": true
      },
      "raster_tile_width": 0,
      "raster_tile_height": 0,
      "filter": {
        "f": [
          [
            {
              "field": "string",
              "op": "eq",
              "value": "string"
            }
          ]
        ],
        "q": [
          [
            "string"
          ]
        ],
        "dateformat": "string",
        "righthand": true
      },
      "license_name": "string",
      "license_urls": [
        "string"
      ]
    }
  }
]
```

<h3 id="get-all-collection-references-as-a-json-file-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|Inline|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<h3 id="get-all-collection-references-as-a-json-file-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[[CollectionReference](#schemacollectionreference)]|false|none|none|
|» collection_name|string|true|none|none|
|» params|[CollectionReferenceParameters](#schemacollectionreferenceparameters)|true|none|none|
|»» index_name|string|true|none|none|
|»» id_path|string|true|none|none|
|»» geometry_path|string|true|none|none|
|»» centroid_path|string|true|none|none|
|»» h3_path|string|false|none|none|
|»» timestamp_path|string|true|none|none|
|»» exclude_fields|string|false|none|none|
|»» update_max_hits|integer(int32)|false|none|none|
|»» taggable_fields|string|false|none|none|
|»» exclude_wfs_fields|string|false|none|none|
|»» custom_params|object|false|none|none|
|»»» **additionalProperties**|string|false|none|none|
|»» display_names|[CollectionDisplayNames](#schemacollectiondisplaynames)|false|none|none|
|»»» collection|string|false|none|none|
|»»» fields|object|false|none|none|
|»»»» **additionalProperties**|string|false|none|none|
|»»» shape_columns|object|false|none|none|
|»»»» **additionalProperties**|string|false|none|none|
|»» atom_feed|[Feed](#schemafeed)|false|none|none|
|»»» author|[Person](#schemaperson)|false|none|none|
|»»»» name|string|false|none|none|
|»»»» email|string|false|none|none|
|»»»» uri|string|false|none|none|
|»»» contributor|[Person](#schemaperson)|false|none|none|
|»»» icon|string|false|none|none|
|»»» logo|string|false|none|none|
|»»» rights|string|false|none|none|
|»»» subtitle|string|false|none|none|
|»»» generator|[Generator](#schemagenerator)|false|none|none|
|»»»» name|string|false|none|none|
|»»»» version|string|false|none|none|
|»»»» uri|string|false|none|none|
|»» open_search|[OpenSearch](#schemaopensearch)|false|none|none|
|»»» short_name|string|false|none|none|
|»»» description|string|false|none|none|
|»»» contact|string|false|none|none|
|»»» tags|string|false|none|none|
|»»» long_name|string|false|none|none|
|»»» image_height|string|false|none|none|
|»»» image_width|string|false|none|none|
|»»» image_type|string|false|none|none|
|»»» image_url|string|false|none|none|
|»»» developer|string|false|none|none|
|»»» attribution|string|false|none|none|
|»»» syndication_right|string|false|none|none|
|»»» adult_content|string|false|none|none|
|»»» language|string|false|none|none|
|»»» input_encoding|string|false|none|none|
|»»» output_encoding|string|false|none|none|
|»» inspire|[Inspire](#schemainspire)|false|none|none|
|»»» keywords|[[Keyword](#schemakeyword)]|false|none|none|
|»»»» value|string|false|none|none|
|»»»» vocabulary|string|false|none|none|
|»»»» date_of_publication|string|false|none|none|
|»»» topic_categories|[string]|false|none|none|
|»»» lineage|string|false|none|none|
|»»» languages|[string]|false|none|none|
|»»» spatial_resolution|[InspireSpatialResolution](#schemainspirespatialresolution)|false|none|none|
|»»»» value|[Number](#schemanumber)|false|none|none|
|»»»» unit_of_measure|string|false|none|none|
|»»» inspire_uri|[InspireURI](#schemainspireuri)|false|none|none|
|»»»» code|string|false|none|none|
|»»»» namespace|string|false|none|none|
|»»» inspire_limitation_access|[InspireLimitationAccess](#schemainspirelimitationaccess)|false|none|none|
|»»»» access_constraints|string|false|none|none|
|»»»» other_constraints|string|false|none|none|
|»»»» classification|string|false|none|none|
|»»» inspire_use_conditions|string|false|none|none|
|»» dublin_core_element_name|[DublinCoreElementName](#schemadublincoreelementname)|false|none|none|
|»»» title|string|false|none|none|
|»»» creator|string|false|none|none|
|»»» subject|string|false|none|none|
|»»» description|string|false|none|none|
|»»» publisher|string|false|none|none|
|»»» contributor|string|false|none|none|
|»»» type|string|false|none|none|
|»»» format|string|false|none|none|
|»»» identifier|string|false|none|none|
|»»» source|string|false|none|none|
|»»» language|string|false|none|none|
|»»» bbox|[Bbox](#schemabbox)|false|none|none|
|»»»» north|number(double)|true|none|none|
|»»»» south|number(double)|true|none|none|
|»»»» east|number(double)|true|none|none|
|»»»» west|number(double)|true|none|none|
|»»» date|string|false|none|none|
|»»» coverage|object|false|none|none|
|»»»» **additionalProperties**|object|false|none|none|
|»»» coverage_centroid|string|false|none|none|
|»» raster_tile_url|[RasterTileURL](#schemarastertileurl)|false|none|none|
|»»» url|string|true|none|none|
|»»» id_path|string|true|none|none|
|»»» min_z|integer(int32)|false|none|none|
|»»» max_z|integer(int32)|false|none|none|
|»»» check_geometry|boolean|false|none|none|
|»» raster_tile_width|integer(int32)|false|none|none|
|»» raster_tile_height|integer(int32)|false|none|none|
|»» filter|[Filter](#schemafilter)|false|none|none|
|»»» f|[array]|false|none|none|
|»»»» field|string|false|none|none|
|»»»» op|string|false|none|none|
|»»»» value|string|false|none|none|
|» q|[array]|false|none|none|
|» dateformat|string|false|none|none|
|» righthand|boolean|false|none|none|
|license_name|string|false|none|none|
|license_urls|[string]|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|op|eq|
|op|gte|
|op|gt|
|op|lte|
|op|lt|
|op|like|
|op|ne|
|op|range|
|op|within|
|op|notwithin|
|op|intersects|
|op|notintersects|

<aside class="success">
This operation does not require authentication
</aside>

## Add collection references from a json file

<a id="opIdimportCollections"></a>

`POST /collections/_import`

Add collection references in ARLAS from a json file

> Body parameter

```yaml
file: string

```

<h3 id="add-collection-references-from-a-json-file-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|object|false|none|
|» file|body|string(binary)|false|none|

> Example responses

> 200 Response

```json
"string"
```

<h3 id="add-collection-references-from-a-json-file-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|string|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="arlas-exploration-api-explore">explore</h1>

## Aggregate

<a id="opIdaggregatePost"></a>

`POST /explore/{collection}/_aggregate`

Aggregate the elements in the collection(s), given the filters and the aggregation parameters

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "aggregations": [
    {
      "type": "datehistogram",
      "field": "string",
      "interval": {
        "value": {},
        "unit": "year"
      },
      "format": "string",
      "metrics": [
        {
          "collect_field": "string",
          "collect_fct": "AVG",
          "precision_threshold": 0
        }
      ],
      "order": "asc",
      "on": "field",
      "size": "string",
      "include": "string",
      "raw_geometries": [
        {
          "geometry": "string",
          "sort": "string"
        }
      ],
      "aggregated_geometries": [
        "BBOX"
      ],
      "fetch_hits": {
        "size": 0,
        "include": [
          "string"
        ]
      }
    }
  ]
}
```

<h3 id="aggregate-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[AggregationsRequest](#schemaaggregationsrequest)|false|none|

> Example responses

> 200 Response

```json
{
  "query_time": 0,
  "total_time": 0,
  "totalnb": 0,
  "name": "string",
  "count": 0,
  "sumotherdoccounts": 0,
  "key": {},
  "key_as_string": {},
  "elements": [
    {
      "query_time": 0,
      "total_time": 0,
      "totalnb": 0,
      "name": "string",
      "count": 0,
      "sumotherdoccounts": 0,
      "key": {},
      "key_as_string": {},
      "elements": [],
      "metrics": [
        {
          "type": "string",
          "field": "string",
          "value": {}
        }
      ],
      "hits": [
        {}
      ],
      "geometries": [
        {
          "reference": "string",
          "geometry": {
            "crs": {
              "type": "name",
              "properties": {
                "property1": {},
                "property2": {}
              }
            },
            "bbox": [
              0.1
            ]
          },
          "sort": "string",
          "is_raw": true
        }
      ],
      "flattened_elements": {
        "property1": {},
        "property2": {}
      }
    }
  ],
  "metrics": [
    {
      "type": "string",
      "field": "string",
      "value": {}
    }
  ],
  "hits": [
    {}
  ],
  "geometries": [
    {
      "reference": "string",
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "sort": "string",
      "is_raw": true
    }
  ],
  "flattened_elements": {
    "property1": {},
    "property2": {}
  }
}
```

<h3 id="aggregate-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[AggregationResponse](#schemaaggregationresponse)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## ShapeAggregate

<a id="opIdshapeaggregatePost"></a>

`POST /explore/{collection}/_shapeaggregate`

Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters, and returns a shapefile of it.

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "aggregations": [
    {
      "type": "datehistogram",
      "field": "string",
      "interval": {
        "value": {},
        "unit": "year"
      },
      "format": "string",
      "metrics": [
        {
          "collect_field": "string",
          "collect_fct": "AVG",
          "precision_threshold": 0
        }
      ],
      "order": "asc",
      "on": "field",
      "size": "string",
      "include": "string",
      "raw_geometries": [
        {
          "geometry": "string",
          "sort": "string"
        }
      ],
      "aggregated_geometries": [
        "BBOX"
      ],
      "fetch_hits": {
        "size": 0,
        "include": [
          "string"
        ]
      }
    }
  ]
}
```

<h3 id="shapeaggregate-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[AggregationsRequest](#schemaaggregationsrequest)|false|none|

> Example responses

> 400 Response

<h3 id="shapeaggregate-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|None|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|
|501|[Not Implemented](https://tools.ietf.org/html/rfc7231#section-6.6.2)|Not implemented functionality.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## GeoAggregate on a geohash

<a id="opIdgeohashgeoaggregate"></a>

`GET /explore/{collection}/_geoaggregate/{geohash}`

Aggregate the elements in the collection(s) and localized in the given geohash as features, given the filters and the aggregation parameters.

<h3 id="geoaggregate-on-a-geohash-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|geohash|path|string|true|geohash|
|agg|query|array[string]|false|- The agg parameter should be given in the following formats:  |
|f|query|array[string]|false|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. |
|q|query|array[string]|false|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|
|dateformat|query|string|false|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|
|righthand|query|boolean|false|If righthand = true, the passed WKT should be counter clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. |
|pretty|query|boolean|false|Pretty print|
|flat|query|boolean|false|Flats the property map: only key/value on one level|
|max-age-cache|query|integer(int32)|false|max-age-cache|

#### Detailed descriptions

**agg**: - The agg parameter should be given in the following formats:  
 
       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}:raw_geometries-{raw_geometries values}:aggregated_geometries-{aggregated_geometries values}:fetch_hits-{fetch_hits values}
 
Where :
 
   - **{type}:{field}** part is mandatory. 
 
   - **interval** must be specified only when aggregation type is datehistogram, histogram, geotile and geohash.
 
   - **format** is optional for datehistogram, and must not be specified for the other types.
 
   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types.
 
   - (**order**,**on**) couple is optional for all aggregation types.
 
   - **size** is optional for term and geohash/geotile, and must not be specified for the other types.
 
   - **include** is optional for term, and must not be specified for the other types.
 
- {type} possible values are : 
 
       geohash, geotile, datehistogram, histogram and term. geohash or geotile must be the main aggregation.
 
- {interval} possible values depends on {type}. 
 
       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. 
 
       If {type} = histogram, then {interval} = {size}. 
 
       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. 
 
       If {type} = geotile, then {interval} = {size}. It's an integer corresponding to zoom level of the aggregation, that should be larger than or equal to {z} in the path param, and no bigger than {z}+6 (max 29). 
 
       If {type} = term, then interval-{interval} is not needed. 
 
- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss.
 
- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. 
 
  {collect_fct} possible values are : 
 
       avg,cardinality,max,min,sum,geobbox,geocentroid
 
- (collect_field,collect_fct) should both be specified, except when collect_fct = `geobbox` or `geocentroid`, it could be specified alone. The metrics `geobbox` and `geocentroid` are returned as features collections.
 
- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. Its values are 'asc' or 'desc'. 
 
- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. 
 
- When {on} = `result`, then (collect_field,collect_fct) should be specified. Except when {collect_fct} = `geobbox` or `geocentroid`, then {on}=`result` is prohibited
 
- {size} Defines how many buckets should be returned. 
 
- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created.
 
- **aggregated_geometries**
 
    > **What it does**: Allows to specify a list of aggregated forms of geometries that represent the bucket.
 
    > __**Syntax**__: `aggregated_geometries-{COMMA_SEPARATED_AGGREGATED_GEOMETRIES}`.
 
    > __**Available aggregated geometries**__: `centroid, bbox, cell, cell_center`.
 
       - **centroid**: returns the centroid of data inside the bucket.
 
       - **bbox**: returns the data extent (bbox) in each bucket.
 
       - **cell**: returns the cell (zxy or geohash) extent of each bucket. This form is supported for **geohash** and **geotile** aggregation type only.
 
       - **cell_center**: returns the cell center of each bucket. This form is supported for **geohash** and **geotile** aggregation type only.
 
    > __**Response**__: Each bucket of the aggregation will be represented with as many features (in a feature collection) as there are specified aggregated geometries. The properties of each feature has :
 
       - **geometry_ref** attribute that informs which aggregated form is returned 
 
       - **geometry_type** attribute set to *aggregated*
 
    > __**Example**__: `aggregated_geometries-bbox,geohash`
 
- **raw_geometries**
 
    > **What it does**: Allows to specify a list of raw geometries provided by hits that represent the bucket and that are elected by a sort
 
    > __**Syntax**__: `raw_geometries-{GEOMETRY_FIELD}({COMMA_SEPERATED_SORT_FIELDS});{GEOMETRY_FIELD2}({COMMA_SEPERATED_SORT_FIELDS2})`.
 
    > __**Available raw geometries**__: any field of the collection whose type is **geo-point** or **geo-shape**.
 
       - sort fields are optional. If no sort is specified, an ascending sort on `collection.params.timestamp_path` is applied
 
       - a sort field can be preceded by '-' for descending sort. Otherwise the sort is ascending
 
    > __**Response**__: each bucket of the aggregation will be represented with as many features (in a feature collection) as there are specified raw geometries. The properties of each feature has :
 
       - **geometry_ref** attribute that informs which geometry path is returned 
 
       - **geometry_type** attribute set to *raw*
 
       - **geometry_sort** attribute that informs how the geometry path is fetched (with what sort)
 
    > __**Example**__: `raw_geometries-geo_field1,geo_field2  ||  raw_geometries-geo_field(-field1,field2)` || raw_geometries-geo_field1(field1);geo_field2(field2,field3)
 
- **fetch_hits** 
 
    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits.
 
    > __**Syntax**__: `fetch_hits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`.
 
    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default.
 
    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters.
 
    > __**Example**__: `fetch_hits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket.
 
**agg** parameter is multiple. The first (main) aggregation must be geohash or geotile. Every agg parameter specified is a subaggregation of the previous one : order matters. 
 
For more details, check https://github.com/gisaia/ARLAS-server/blob/master/docs/arlas-api-exploration.md 

**f**: - A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. 
 
- A triplet is composed of a field name, a comparison operator and a value. 
 
  The possible values of the comparison operator are : 
 
       Operator        --                   Description                    -- value type
 
       :eq:            -- {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values -- numeric or strings 
 
       :ne:            -- {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values -- numeric or strings 
 
       :like:          -- {fieldName}  is like {value}                     -- numeric or strings 
 
       :gte:           -- {fieldName} is greater than or equal to  {value} -- numeric 
 
       :gt:            -- {fieldName} is greater than {value}              -- numeric 
 
       :lte:           -- {fieldName} is less than or equal to {value}     -- numeric 
 
       :lt:            -- {fieldName}  is less than {value}                -- numeric 
 
       :range:         -- {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges -- numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression
 
       :within:        -- {GeofieldName}` is within the `{given WKT string or the given BBOX }` -- a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :notwithin:     -- {GeofieldName} is not within the `{given WKT string or the given BBOX }` -- a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :intersects:    -- {GeofieldName} intersects the `{given WKT string or the given BBOX }` | a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :notintersects: -- {GeofieldName} does not intersect the `{given WKT string or the given }` -- a WKT string or the BBOX string : `"west, south, east, north"` 

**righthand**: If righthand = true, the passed WKT should be counter clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. 
 Inversely, If righthand = false, the passed WKT should be clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. 

> Example responses

> 200 Response

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "features": [
    {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ],
      "properties": {
        "property1": {},
        "property2": {}
      },
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "id": "string"
    }
  ]
}
```

<h3 id="geoaggregate-on-a-geohash-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[FeatureCollection](#schemafeaturecollection)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|
|501|[Not Implemented](https://tools.ietf.org/html/rfc7231#section-6.6.2)|Not implemented functionality.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## GeoAggregate on a geotile

<a id="opIdgeotilegeoaggregate"></a>

`GET /explore/{collection}/_geoaggregate/{z}/{x}/{y}`

Aggregate the elements in the collection(s) and localized in the given tile as features, given the filters and the aggregation parameters.

<h3 id="geoaggregate-on-a-geotile-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|z|path|integer(int32)|true|z|
|x|path|integer(int32)|true|x|
|y|path|integer(int32)|true|y|
|agg|query|array[string]|false|- The agg parameter should be given in the following formats:  |
|f|query|array[string]|false|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. |
|q|query|array[string]|false|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|
|dateformat|query|string|false|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|
|righthand|query|boolean|false|If righthand = true, the passed WKT should be counter clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. |
|pretty|query|boolean|false|Pretty print|
|flat|query|boolean|false|Flats the property map: only key/value on one level|
|max-age-cache|query|integer(int32)|false|max-age-cache|

#### Detailed descriptions

**agg**: - The agg parameter should be given in the following formats:  
 
       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on}:size-{size}:raw_geometries-{raw_geometries values}:aggregated_geometries-{aggregated_geometries values}:fetch_hits-{fetch_hits values}
 
Where :
 
   - **{type}:{field}** part is mandatory. 
 
   - **interval** must be specified only when aggregation type is datehistogram, histogram, geotile and geohash.
 
   - **format** is optional for datehistogram, and must not be specified for the other types.
 
   - (**collect_field**,**collect_fct**) couple is optional for all aggregation types.
 
   - (**order**,**on**) couple is optional for all aggregation types.
 
   - **size** is optional for term and geohash/geotile, and must not be specified for the other types.
 
   - **include** is optional for term, and must not be specified for the other types.
 
- {type} possible values are : 
 
       geohash, geotile, datehistogram, histogram and term. geohash or geotile must be the main aggregation.
 
- {interval} possible values depends on {type}. 
 
       If {type} = datehistogram, then {interval} = {size}(year,quarter,month,week,day,hour,minute,second). Size value must be equal to 1 for year,quarter,month and week unities. 
 
       If {type} = histogram, then {interval} = {size}. 
 
       If {type} = geohash, then {interval} = {size}. It's an integer between 1 and 12. Lower the length, greater is the surface of aggregation. 
 
       If {type} = geotile, then {interval} = {size}. It's an integer corresponding to zoom level of the aggregation, that should be larger than or equal to {z} in the path param, and no bigger than {z}+6 (max 29). 
 
       If {type} = term, then interval-{interval} is not needed. 
 
- format-{format} is the date format for key aggregation. The default value is yyyy-MM-dd-hh:mm:ss.
 
- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. 
 
  {collect_fct} possible values are : 
 
       avg,cardinality,max,min,sum,geobbox,geocentroid
 
- (collect_field,collect_fct) should both be specified, except when collect_fct = `geobbox` or `geocentroid`, it could be specified alone. The metrics `geobbox` and `geocentroid` are returned as features collections.
 
- {order} is set to sort the aggregation buckets on the field name, on the count of the buckets or on the the result of a metric sub-aggregation. Its values are 'asc' or 'desc'. 
 
- {on} is set to specify whether the {order} is on the field name, on the count of the aggregation or on the result of a metric sub-aggregation. Its values are 'field', 'count' or 'result'. 
 
- When {on} = `result`, then (collect_field,collect_fct) should be specified. Except when {collect_fct} = `geobbox` or `geocentroid`, then {on}=`result` is prohibited
 
- {size} Defines how many buckets should be returned. 
 
- {include} Specifies the values for which buckets will be created. This values are comma separated. If one value is specified then regular expressions can be used (only in this case) and buckets matching them will be created. If more than one value are specified then only buckets matching the exact values will be created.
 
- **aggregated_geometries**
 
    > **What it does**: Allows to specify a list of aggregated forms of geometries that represent the bucket.
 
    > __**Syntax**__: `aggregated_geometries-{COMMA_SEPARATED_AGGREGATED_GEOMETRIES}`.
 
    > __**Available aggregated geometries**__: `centroid, bbox, cell, cell_center`.
 
       - **centroid**: returns the centroid of data inside the bucket.
 
       - **bbox**: returns the data extent (bbox) in each bucket.
 
       - **cell**: returns the cell (zxy or geohash) extent of each bucket. This form is supported for **geohash** and **geotile** aggregation type only.
 
       - **cell_center**: returns the cell center of each bucket. This form is supported for **geohash** and **geotile** aggregation type only.
 
    > __**Response**__: Each bucket of the aggregation will be represented with as many features (in a feature collection) as there are specified aggregated geometries. The properties of each feature has :
 
       - **geometry_ref** attribute that informs which aggregated form is returned 
 
       - **geometry_type** attribute set to *aggregated*
 
    > __**Example**__: `aggregated_geometries-bbox,geohash`
 
- **raw_geometries**
 
    > **What it does**: Allows to specify a list of raw geometries provided by hits that represent the bucket and that are elected by a sort
 
    > __**Syntax**__: `raw_geometries-{GEOMETRY_FIELD}({COMMA_SEPERATED_SORT_FIELDS});{GEOMETRY_FIELD2}({COMMA_SEPERATED_SORT_FIELDS2})`.
 
    > __**Available raw geometries**__: any field of the collection whose type is **geo-point** or **geo-shape**.
 
       - sort fields are optional. If no sort is specified, an ascending sort on `collection.params.timestamp_path` is applied
 
       - a sort field can be preceded by '-' for descending sort. Otherwise the sort is ascending
 
    > __**Response**__: each bucket of the aggregation will be represented with as many features (in a feature collection) as there are specified raw geometries. The properties of each feature has :
 
       - **geometry_ref** attribute that informs which geometry path is returned 
 
       - **geometry_type** attribute set to *raw*
 
       - **geometry_sort** attribute that informs how the geometry path is fetched (with what sort)
 
    > __**Example**__: `raw_geometries-geo_field1,geo_field2  ||  raw_geometries-geo_field(-field1,field2)` || raw_geometries-geo_field1(field1);geo_field2(field2,field3)
 
- **fetch_hits** 
 
    > **What it does**: Specifies the number of hits to retrieve inside each aggregation bucket and which fields to include in the hits.
 
    > __**Syntax**__: `fetch_hits-{sizeOfHitsToFetch}(+{field1}, {field2}, -{field3}, ...)`.
 
    > **Note 1**: `{sizeOfHitsToFetch}` is optional, if not specified, 1 is considered as default.
 
    > **Note 2**: `{field}` can be preceded by **+** or **-** for **ascending** or **descending** sort of the hits. Order matters.
 
    > __**Example**__: `fetch_hits-3(-timestamp, geometry)`. Fetches the 3 last positions for each bucket.
 
**agg** parameter is multiple. The first (main) aggregation must be geohash or geotile. Every agg parameter specified is a subaggregation of the previous one : order matters. 
 
For more details, check https://github.com/gisaia/ARLAS-server/blob/master/docs/arlas-api-exploration.md 

**f**: - A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. 
 
- A triplet is composed of a field name, a comparison operator and a value. 
 
  The possible values of the comparison operator are : 
 
       Operator        --                   Description                    -- value type
 
       :eq:            -- {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values -- numeric or strings 
 
       :ne:            -- {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values -- numeric or strings 
 
       :like:          -- {fieldName}  is like {value}                     -- numeric or strings 
 
       :gte:           -- {fieldName} is greater than or equal to  {value} -- numeric 
 
       :gt:            -- {fieldName} is greater than {value}              -- numeric 
 
       :lte:           -- {fieldName} is less than or equal to {value}     -- numeric 
 
       :lt:            -- {fieldName}  is less than {value}                -- numeric 
 
       :range:         -- {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges -- numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression
 
       :within:        -- {GeofieldName}` is within the `{given WKT string or the given BBOX }` -- a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :notwithin:     -- {GeofieldName} is not within the `{given WKT string or the given BBOX }` -- a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :intersects:    -- {GeofieldName} intersects the `{given WKT string or the given BBOX }` | a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :notintersects: -- {GeofieldName} does not intersect the `{given WKT string or the given }` -- a WKT string or the BBOX string : `"west, south, east, north"` 

**righthand**: If righthand = true, the passed WKT should be counter clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. 
 Inversely, If righthand = false, the passed WKT should be clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. 

> Example responses

> 200 Response

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "features": [
    {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ],
      "properties": {
        "property1": {},
        "property2": {}
      },
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "id": "string"
    }
  ]
}
```

<h3 id="geoaggregate-on-a-geotile-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[FeatureCollection](#schemafeaturecollection)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|
|501|[Not Implemented](https://tools.ietf.org/html/rfc7231#section-6.6.2)|Not implemented functionality.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## GeoAggregate

<a id="opIdgeoaggregatePost"></a>

`POST /explore/{collection}/_geoaggregate`

Aggregate the elements in the collection(s) as features, given the filters and the aggregation parameters.

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "aggregations": [
    {
      "type": "datehistogram",
      "field": "string",
      "interval": {
        "value": {},
        "unit": "year"
      },
      "format": "string",
      "metrics": [
        {
          "collect_field": "string",
          "collect_fct": "AVG",
          "precision_threshold": 0
        }
      ],
      "order": "asc",
      "on": "field",
      "size": "string",
      "include": "string",
      "raw_geometries": [
        {
          "geometry": "string",
          "sort": "string"
        }
      ],
      "aggregated_geometries": [
        "BBOX"
      ],
      "fetch_hits": {
        "size": 0,
        "include": [
          "string"
        ]
      }
    }
  ]
}
```

<h3 id="geoaggregate-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[AggregationsRequest](#schemaaggregationsrequest)|false|none|

> Example responses

> 200 Response

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "features": [
    {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ],
      "properties": {
        "property1": {},
        "property2": {}
      },
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "id": "string"
    }
  ]
}
```

<h3 id="geoaggregate-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[FeatureCollection](#schemafeaturecollection)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|
|501|[Not Implemented](https://tools.ietf.org/html/rfc7231#section-6.6.2)|Not implemented functionality.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Compute

<a id="opIdcomputePost"></a>

`POST /explore/{collection}/_compute`

Computes the given metric on a field in the collection, given the filters

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "field": "string",
  "metric": "AVG",
  "precisionThreshold": 0
}
```

<h3 id="compute-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[ComputationRequest](#schemacomputationrequest)|false|none|

> Example responses

> 200 Response

```json
{
  "query_time": 0,
  "total_time": 0,
  "totalnb": 0,
  "field": "string",
  "metric": "AVG",
  "value": 0.1,
  "geometry": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  }
}
```

<h3 id="compute-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[ComputationResponse](#schemacomputationresponse)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Count

<a id="opIdcountPost"></a>

`POST /explore/{collection}/_count`

Count the number of elements found in the collection(s), given the filters

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  }
}
```

<h3 id="count-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collections|
|pretty|query|boolean|false|Pretty print|
|body|body|[Count](#schemacount)|false|none|

> Example responses

> 200 Response

```json
{
  "collection": "string",
  "hits": [
    {
      "md": {
        "id": "string",
        "timestamp": 0,
        "geometry": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        },
        "centroid": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        },
        "returned_geometries": [
          {
            "path": "string",
            "geometry": {
              "crs": {
                "type": "name",
                "properties": {
                  "property1": {},
                  "property2": {}
                }
              },
              "bbox": [
                0.1
              ]
            }
          }
        ]
      },
      "data": {}
    }
  ],
  "nbhits": 0,
  "totalnb": 0,
  "links": {
    "property1": {
      "href": "string",
      "method": "string",
      "body": {}
    },
    "property2": {
      "href": "string",
      "method": "string",
      "body": {}
    }
  }
}
```

<h3 id="count-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[Hits](#schemahits)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Describe

<a id="opIddescribe"></a>

`GET /explore/{collection}/_describe`

Describe the structure and the content of the given collection. 

<h3 id="describe-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|

> Example responses

> 200 Response

```json
{
  "properties": {
    "property1": {
      "type": "TEXT",
      "format": "string",
      "hash_field": "string",
      "properties": {
        "property1": {},
        "property2": {}
      },
      "taggable": true,
      "indexed": true
    },
    "property2": {
      "type": "TEXT",
      "format": "string",
      "hash_field": "string",
      "properties": {
        "property1": {},
        "property2": {}
      },
      "taggable": true,
      "indexed": true
    }
  },
  "collection_name": "string",
  "params": {
    "index_name": "string",
    "id_path": "string",
    "geometry_path": "string",
    "centroid_path": "string",
    "h3_path": "string",
    "timestamp_path": "string",
    "exclude_fields": "string",
    "update_max_hits": 0,
    "taggable_fields": "string",
    "exclude_wfs_fields": "string",
    "custom_params": {
      "property1": "string",
      "property2": "string"
    },
    "display_names": {
      "collection": "string",
      "fields": {
        "property1": "string",
        "property2": "string"
      },
      "shape_columns": {
        "property1": "string",
        "property2": "string"
      }
    },
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
    },
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
      "output_encoding": "string"
    },
    "inspire": {
      "keywords": [
        {
          "value": "string",
          "vocabulary": "string",
          "date_of_publication": "string"
        }
      ],
      "topic_categories": [
        "string"
      ],
      "lineage": "string",
      "languages": [
        "string"
      ],
      "spatial_resolution": {
        "value": {},
        "unit_of_measure": "string"
      },
      "inspire_uri": {
        "code": "string",
        "namespace": "string"
      },
      "inspire_limitation_access": {
        "access_constraints": "string",
        "other_constraints": "string",
        "classification": "string"
      },
      "inspire_use_conditions": "string"
    },
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
        "north": 0.1,
        "south": 0.1,
        "east": 0.1,
        "west": 0.1
      },
      "date": "string",
      "coverage": {
        "property1": {},
        "property2": {}
      },
      "coverage_centroid": "string"
    },
    "raster_tile_url": {
      "url": "string",
      "id_path": "string",
      "min_z": 0,
      "max_z": 0,
      "check_geometry": true
    },
    "raster_tile_width": 0,
    "raster_tile_height": 0,
    "filter": {
      "f": [
        [
          {
            "field": "string",
            "op": "eq",
            "value": "string"
          }
        ]
      ],
      "q": [
        [
          "string"
        ]
      ],
      "dateformat": "string",
      "righthand": true
    },
    "license_name": "string",
    "license_urls": [
      "string"
    ]
  }
}
```

<h3 id="describe-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[CollectionReferenceDescription](#schemacollectionreferencedescription)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## List

<a id="opIdlist"></a>

`GET /explore/_list`

List the collections configured in ARLAS. 

<h3 id="list-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|

> Example responses

> 200 Response

```json
[
  {
    "properties": {
      "property1": {
        "type": "TEXT",
        "format": "string",
        "hash_field": "string",
        "properties": {
          "property1": {},
          "property2": {}
        },
        "taggable": true,
        "indexed": true
      },
      "property2": {
        "type": "TEXT",
        "format": "string",
        "hash_field": "string",
        "properties": {
          "property1": {},
          "property2": {}
        },
        "taggable": true,
        "indexed": true
      }
    },
    "collection_name": "string",
    "params": {
      "index_name": "string",
      "id_path": "string",
      "geometry_path": "string",
      "centroid_path": "string",
      "h3_path": "string",
      "timestamp_path": "string",
      "exclude_fields": "string",
      "update_max_hits": 0,
      "taggable_fields": "string",
      "exclude_wfs_fields": "string",
      "custom_params": {
        "property1": "string",
        "property2": "string"
      },
      "display_names": {
        "collection": "string",
        "fields": {
          "property1": "string",
          "property2": "string"
        },
        "shape_columns": {
          "property1": "string",
          "property2": "string"
        }
      },
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
      },
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
        "output_encoding": "string"
      },
      "inspire": {
        "keywords": [
          {
            "value": "string",
            "vocabulary": "string",
            "date_of_publication": "string"
          }
        ],
        "topic_categories": [
          "string"
        ],
        "lineage": "string",
        "languages": [
          "string"
        ],
        "spatial_resolution": {
          "value": {},
          "unit_of_measure": "string"
        },
        "inspire_uri": {
          "code": "string",
          "namespace": "string"
        },
        "inspire_limitation_access": {
          "access_constraints": "string",
          "other_constraints": "string",
          "classification": "string"
        },
        "inspire_use_conditions": "string"
      },
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
          "north": 0.1,
          "south": 0.1,
          "east": 0.1,
          "west": 0.1
        },
        "date": "string",
        "coverage": {
          "property1": {},
          "property2": {}
        },
        "coverage_centroid": "string"
      },
      "raster_tile_url": {
        "url": "string",
        "id_path": "string",
        "min_z": 0,
        "max_z": 0,
        "check_geometry": true
      },
      "raster_tile_width": 0,
      "raster_tile_height": 0,
      "filter": {
        "f": [
          [
            {
              "field": "string",
              "op": "eq",
              "value": "string"
            }
          ]
        ],
        "q": [
          [
            "string"
          ]
        ],
        "dateformat": "string",
        "righthand": true
      },
      "license_name": "string",
      "license_urls": [
        "string"
      ]
    }
  }
]
```

<h3 id="list-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|Inline|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<h3 id="list-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[[CollectionReferenceDescription](#schemacollectionreferencedescription)]|false|none|none|
|» properties|object|false|none|none|
|»» **additionalProperties**|[CollectionReferenceDescriptionProperty](#schemacollectionreferencedescriptionproperty)|false|none|none|
|»»» type|string|false|none|none|
|»»» format|string|false|none|none|
|»»» hash_field|string|false|none|none|
|»»» properties|[CollectionReferenceDescriptionProperty](#schemacollectionreferencedescriptionproperty)|false|none|none|
|»»»» **additionalProperties**|[CollectionReferenceDescriptionProperty](#schemacollectionreferencedescriptionproperty)|false|none|none|
|»»» taggable|boolean|false|none|none|
|»»» indexed|boolean|false|none|none|
|» collection_name|string|true|none|none|
|» params|[CollectionReferenceParameters](#schemacollectionreferenceparameters)|true|none|none|
|»» index_name|string|true|none|none|
|»» id_path|string|true|none|none|
|»» geometry_path|string|true|none|none|
|»» centroid_path|string|true|none|none|
|»» h3_path|string|false|none|none|
|»» timestamp_path|string|true|none|none|
|»» exclude_fields|string|false|none|none|
|»» update_max_hits|integer(int32)|false|none|none|
|»» taggable_fields|string|false|none|none|
|»» exclude_wfs_fields|string|false|none|none|
|»» custom_params|object|false|none|none|
|»»» **additionalProperties**|string|false|none|none|
|»» display_names|[CollectionDisplayNames](#schemacollectiondisplaynames)|false|none|none|
|»»» collection|string|false|none|none|
|»»» fields|object|false|none|none|
|»»»» **additionalProperties**|string|false|none|none|
|»»» shape_columns|object|false|none|none|
|»»»» **additionalProperties**|string|false|none|none|
|»» atom_feed|[Feed](#schemafeed)|false|none|none|
|»»» author|[Person](#schemaperson)|false|none|none|
|»»»» name|string|false|none|none|
|»»»» email|string|false|none|none|
|»»»» uri|string|false|none|none|
|»»» contributor|[Person](#schemaperson)|false|none|none|
|»»» icon|string|false|none|none|
|»»» logo|string|false|none|none|
|»»» rights|string|false|none|none|
|»»» subtitle|string|false|none|none|
|»»» generator|[Generator](#schemagenerator)|false|none|none|
|»»»» name|string|false|none|none|
|»»»» version|string|false|none|none|
|»»»» uri|string|false|none|none|
|»» open_search|[OpenSearch](#schemaopensearch)|false|none|none|
|»»» short_name|string|false|none|none|
|»»» description|string|false|none|none|
|»»» contact|string|false|none|none|
|»»» tags|string|false|none|none|
|»»» long_name|string|false|none|none|
|»»» image_height|string|false|none|none|
|»»» image_width|string|false|none|none|
|»»» image_type|string|false|none|none|
|»»» image_url|string|false|none|none|
|»»» developer|string|false|none|none|
|»»» attribution|string|false|none|none|
|»»» syndication_right|string|false|none|none|
|»»» adult_content|string|false|none|none|
|»»» language|string|false|none|none|
|»»» input_encoding|string|false|none|none|
|»»» output_encoding|string|false|none|none|
|»» inspire|[Inspire](#schemainspire)|false|none|none|
|»»» keywords|[[Keyword](#schemakeyword)]|false|none|none|
|»»»» value|string|false|none|none|
|»»»» vocabulary|string|false|none|none|
|»»»» date_of_publication|string|false|none|none|
|»»» topic_categories|[string]|false|none|none|
|»»» lineage|string|false|none|none|
|»»» languages|[string]|false|none|none|
|»»» spatial_resolution|[InspireSpatialResolution](#schemainspirespatialresolution)|false|none|none|
|»»»» value|[Number](#schemanumber)|false|none|none|
|»»»» unit_of_measure|string|false|none|none|
|»»» inspire_uri|[InspireURI](#schemainspireuri)|false|none|none|
|»»»» code|string|false|none|none|
|»»»» namespace|string|false|none|none|
|»»» inspire_limitation_access|[InspireLimitationAccess](#schemainspirelimitationaccess)|false|none|none|
|»»»» access_constraints|string|false|none|none|
|»»»» other_constraints|string|false|none|none|
|»»»» classification|string|false|none|none|
|»»» inspire_use_conditions|string|false|none|none|
|»» dublin_core_element_name|[DublinCoreElementName](#schemadublincoreelementname)|false|none|none|
|»»» title|string|false|none|none|
|»»» creator|string|false|none|none|
|»»» subject|string|false|none|none|
|»»» description|string|false|none|none|
|»»» publisher|string|false|none|none|
|»»» contributor|string|false|none|none|
|»»» type|string|false|none|none|
|»»» format|string|false|none|none|
|»»» identifier|string|false|none|none|
|»»» source|string|false|none|none|
|»»» language|string|false|none|none|
|»»» bbox|[Bbox](#schemabbox)|false|none|none|
|»»»» north|number(double)|true|none|none|
|»»»» south|number(double)|true|none|none|
|»»»» east|number(double)|true|none|none|
|»»»» west|number(double)|true|none|none|
|»»» date|string|false|none|none|
|»»» coverage|object|false|none|none|
|»»»» **additionalProperties**|object|false|none|none|
|»»» coverage_centroid|string|false|none|none|
|»» raster_tile_url|[RasterTileURL](#schemarastertileurl)|false|none|none|
|»»» url|string|true|none|none|
|»»» id_path|string|true|none|none|
|»»» min_z|integer(int32)|false|none|none|
|»»» max_z|integer(int32)|false|none|none|
|»»» check_geometry|boolean|false|none|none|
|»» raster_tile_width|integer(int32)|false|none|none|
|»» raster_tile_height|integer(int32)|false|none|none|
|»» filter|[Filter](#schemafilter)|false|none|none|
|»»» f|[array]|false|none|none|
|»»»» field|string|false|none|none|
|»»»» op|string|false|none|none|
|»»»» value|string|false|none|none|
|» q|[array]|false|none|none|
|» dateformat|string|false|none|none|
|» righthand|boolean|false|none|none|
|license_name|string|false|none|none|
|license_urls|[string]|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|TEXT|
|type|KEYWORD|
|type|LONG|
|type|INTEGER|
|type|SHORT|
|type|BYTE|
|type|DOUBLE|
|type|FLOAT|
|type|DATE|
|type|BOOLEAN|
|type|BINARY|
|type|INT_RANGE|
|type|FLOAT_RANGE|
|type|LONG_RANGE|
|type|DOUBLE_RANGE|
|type|DATE_RANGE|
|type|OBJECT|
|type|NESTED|
|type|GEO_POINT|
|type|GEO_SHAPE|
|type|IP|
|type|COMPLETION|
|type|TOKEN_COUNT|
|type|MAPPER_MURMUR3|
|type|UNKNOWN|
|type|VARCHAR|
|type|CHAR|
|type|CHARACTER|
|type|BIT|
|type|TINYINT|
|type|SMALLINT|
|type|INT|
|type|BIGINT|
|type|DECIMAL|
|type|NUMERIC|
|type|REAL|
|type|DOUBLEPRECISION|
|type|TIMESTAMP|
|type|TIME|
|type|INTERVAL|
|type|GEOMETRY|
|type|GEOGRAPHY|
|type|POINT|
|type|LINESTRING|
|type|POLYGON|
|type|MULTIPOINT|
|type|MULTILINESTRING|
|type|MULTIPOLYGON|
|type|GEOMETRYCOLLECTION|
|type|MURMUR3|
|op|eq|
|op|gte|
|op|gt|
|op|lte|
|op|lt|
|op|like|
|op|ne|
|op|range|
|op|within|
|op|notwithin|
|op|intersects|
|op|notintersects|

<aside class="success">
This operation does not require authentication
</aside>

## Get an Arlas document

<a id="opIdgetArlasHit"></a>

`GET /explore/{collection}/{identifier}`

Returns a raw indexed document.

<h3 id="get-an-arlas-document-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|identifier|path|string|true|identifier|
|pretty|query|boolean|false|Pretty print|
|flat|query|boolean|false|Flats the property map: only key/value on one level|
|max-age-cache|query|integer(int32)|false|max-age-cache|

> Example responses

> 200 Response

```json
{
  "md": {
    "id": "string",
    "timestamp": 0,
    "geometry": {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ]
    },
    "centroid": {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ]
    },
    "returned_geometries": [
      {
        "path": "string",
        "geometry": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        }
      }
    ]
  },
  "data": {}
}
```

<h3 id="get-an-arlas-document-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[Hit](#schemahit)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found Error.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Tiled GeoSearch

<a id="opIdtiledgeosearch_1"></a>

`GET /explore/{collection}/_tile/{z}/{x}/{y}.png`

Search and return the elements found in the collection(s) and localized in the given tile(x,y,z) as features, given the filters

<h3 id="tiled-geosearch-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|x|path|integer(int32)|true|x|
|y|path|integer(int32)|true|y|
|z|path|integer(int32)|true|z|
|f|query|array[string]|false|- A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. |
|q|query|array[string]|false|A full text search. Optionally, it's possible to search on a field using this syntax: {fieldname}:{text}|
|dateformat|query|string|false|The format of dates. This parameter should be set only if a date field is queried in `f` param; when using `gt`, `lt`, `gte`, `lte` and `range` operations|
|righthand|query|boolean|false|If righthand = true, the passed WKT should be counter clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. |
|size|query|integer|false|The maximum number of entries or sub-entries to be returned. The default value is 10|
|from|query|integer|false|From index to start the search from. Defaults to 0.|
|sort|query|array[string]|false|Sorts the resulted hits on the given fields and/or by distance to a given point:|
|after|query|string|false|List of values of fields present in sort param that are used to search after. |
|before|query|string|false|Same idea that after param, but to retrieve the data placed before the pointed element, given the provided order (sort).|
|sampling|query|integer(int32)|false|Size of the sampling for testing transparency: 1: test every pixel, 10: test 1 pixel every 10 pixels, etc.|
|coverage|query|integer(int32)|false|Percentage (]0-100]) of acceptable transparent pixels. Higher the percentage, more tiles could be used for filling the tile|
|max-age-cache|query|integer(int32)|false|max-age-cache|

#### Detailed descriptions

**f**: - A triplet for filtering the result. Multiple filter can be provided in distinct parameters (AND operator is applied) or in the same parameter separated by semi-colons (OR operator is applied). The order does not matter. 
 
- A triplet is composed of a field name, a comparison operator and a value. 
 
  The possible values of the comparison operator are : 
 
       Operator        --                   Description                    -- value type
 
       :eq:            -- {fieldName} equals {comma separated values}. **OR** operation is applied for the specified values -- numeric or strings 
 
       :ne:            -- {fieldName} must not equal {comma separated values }. **AND** operation is applied for the specified values -- numeric or strings 
 
       :like:          -- {fieldName}  is like {value}                     -- numeric or strings 
 
       :gte:           -- {fieldName} is greater than or equal to  {value} -- numeric 
 
       :gt:            -- {fieldName} is greater than {value}              -- numeric 
 
       :lte:           -- {fieldName} is less than or equal to {value}     -- numeric 
 
       :lt:            -- {fieldName}  is less than {value}                -- numeric 
 
       :range:         -- {fieldName} is between `{comma separated [min<max] values}`. **OR** operation is applied for the specified ranges -- numeric or strings. If the field's type is date, then min & max should be timestamps in millisecond or a Date expression
 
       :within:        -- {GeofieldName}` is within the `{given WKT string or the given BBOX }` -- a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :notwithin:     -- {GeofieldName} is not within the `{given WKT string or the given BBOX }` -- a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :intersects:    -- {GeofieldName} intersects the `{given WKT string or the given BBOX }` | a WKT string or the BBOX string : `"west, south, east, north"` 
 
       :notintersects: -- {GeofieldName} does not intersect the `{given WKT string or the given }` -- a WKT string or the BBOX string : `"west, south, east, north"` 

**righthand**: If righthand = true, the passed WKT should be counter clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. 
 Inversely, If righthand = false, the passed WKT should be clock-wise; otherwise, ARLAS-server will attempt to parse it as the "Complementary" Polygon on the other facet of the planet. 

**sort**: Sorts the resulted hits on the given fields and/or by distance to a given point:
 
> __**Syntax**__: `{field1},{field2},-{field3},geodistance:{lat} {lon},{field4}  ...`.
 
> **Note 1**: `{field}` can be preceded by **'-'**  for **descending** sort. By default, sort is ascending.
 
> **Note 2**: The order of fields matters.
 
> **Note 3** ***geodistance sort***: Sorts the hits centroids by distance to the given **{lat} {lon}** (ascending distance sort). It can be specified at most 1 time.
 
> __**Example 1**__: sort=`age,-timestamp`. Resulted hits are sorted by age. For same age hits, they are decreasingly sorted in time.
 
> __**Example 2**__: sort=`age,geodistance:89 179`. Resulted hits are sorted by age. For same age hits, they are sorted by closest distance to the point(89°,179°)
 

**after**: List of values of fields present in sort param that are used to search after. 
 
> **What it does**: Retrieve the data placed after the pointed element, given the provided order (sort).
 
> __**Restriction 1**__: **after** param works only combined with **sort** param.
 
> __**Syntax**__: `after={value1},{value2},...,{valueN} & sort={field1},{field2},...,{fieldN}`.
 
> **Note 1**: *{value1}` and `{value2}` are the values of `{field1}` and `{field2}` in the last hit returned in the previous search
 
> **Note 2**: The order of fields and values matters. *{value1},{value2}* must be in the same order of *{field1},{field2}* in **sort** param
 
> **Note 3**:  The last field `{fieldN}` must be the id field specified in the collection **collection.params.idPath** (returned as **md.id**) and `{valueN}` its corresponding value.
 
> __**Example**__: *sort=`-date,id` & **after**=`01/02/2019,abcd1234`*. Gets the following hits of the previous search that stopped at date *01/02/2019* and id *abcd1234*.
 
> __**Restriction 2**__: **from** param must be set to 0 or kept unset
 

> Example responses

> 400 Response

<h3 id="tiled-geosearch-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|None|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## GeoSearch

<a id="opIdgeosearchPost"></a>

`POST /explore/{collection}/_geosearch`

Search and return the elements found in the collection(s) as features, given the filters

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "page": {
    "size": 0,
    "from": 0,
    "sort": "string",
    "after": "string",
    "before": "string"
  },
  "projection": {
    "includes": "string",
    "excludes": "string"
  },
  "returned_geometries": "string"
}
```

<h3 id="geosearch-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[Search](#schemasearch)|false|none|

> Example responses

> 200 Response

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "features": [
    {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ],
      "properties": {
        "property1": {},
        "property2": {}
      },
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "id": "string"
    }
  ]
}
```

<h3 id="geosearch-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[FeatureCollection](#schemafeaturecollection)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## ShapeSearch

<a id="opIdshapesearchPost"></a>

`POST /explore/{collection}/_shapesearch`

Search and return the elements found in the collection(s) as features, given the filters, exported as a Shapefile

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "page": {
    "size": 0,
    "from": 0,
    "sort": "string",
    "after": "string",
    "before": "string"
  },
  "projection": {
    "includes": "string",
    "excludes": "string"
  },
  "returned_geometries": "string"
}
```

<h3 id="shapesearch-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[Search](#schemasearch)|false|none|

> Example responses

> 400 Response

<h3 id="shapesearch-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|None|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Search

<a id="opIdsearchPost"></a>

`POST /explore/{collection}/_search`

Search and return the elements found in the collection, given the filters

> Body parameter

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "page": {
    "size": 0,
    "from": 0,
    "sort": "string",
    "after": "string",
    "before": "string"
  },
  "projection": {
    "includes": "string",
    "excludes": "string"
  },
  "returned_geometries": "string"
}
```

<h3 id="search-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collection|path|string|true|collection|
|pretty|query|boolean|false|Pretty print|
|max-age-cache|query|integer(int32)|false|max-age-cache|
|body|body|[Search](#schemasearch)|false|none|

> Example responses

> 200 Response

```json
{
  "collection": "string",
  "hits": [
    {
      "md": {
        "id": "string",
        "timestamp": 0,
        "geometry": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        },
        "centroid": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        },
        "returned_geometries": [
          {
            "path": "string",
            "geometry": {
              "crs": {
                "type": "name",
                "properties": {
                  "property1": {},
                  "property2": {}
                }
              },
              "bbox": [
                0.1
              ]
            }
          }
        ]
      },
      "data": {}
    }
  ],
  "nbhits": 0,
  "totalnb": 0,
  "links": {
    "property1": {
      "href": "string",
      "method": "string",
      "body": {}
    },
    "property2": {
      "href": "string",
      "method": "string",
      "body": {}
    }
  }
}
```

<h3 id="search-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[Hits](#schemahits)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad request.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Suggest

<a id="opIdsuggest"></a>

`GET /explore/{collections}/_suggest`

Suggest the the n (n=size) most relevant terms given the filters

<h3 id="suggest-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|collections|path|string|true|collections, comma separated|
|f|query|array[string]|false|- A triplet for filtering the result. Multiple filter can be provided. The order does not matter. |
|q|query|string|false|A full text search|
|pretty|query|boolean|false|Pretty print|
|size|query|integer(int32)|false|The maximum number of entries or sub-entries to be returned. The default value is 10|
|from|query|integer(int32)|false|From index to start the search from. Defaults to 0.|
|field|query|string|false|Name of the field to be used for retrieving the most relevant terms|
|max-age-cache|query|integer(int32)|false|max-age-cache|

#### Detailed descriptions

**f**: - A triplet for filtering the result. Multiple filter can be provided. The order does not matter. 
 
- A triplet is composed of a field name, a comparison operator and a value. 
 
  The possible values of the comparison operator are : 
 
       Operator   |                   Description                      | value type
 
       :          |  {fieldName} equals {value}                        | numeric or strings 
 
       :gte:      |  {fieldName} is greater than or equal to  {value}  | numeric 
 
       :gt:       |  {fieldName} is greater than {value}               | numeric 
 
       :lte:      |  {fieldName} is less than or equal to {value}      | numeric 
 
       :lt:       |  {fieldName}  is less than {value}                 | numeric 
 

 
- The AND operator is applied between filters having different fieldNames. 
 
- The OR operator is applied on filters having the same fieldName. 
 
- If the fieldName starts with - then a must not filter is used
 
- If the fieldName starts with - then a must not filter is used
 
For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md 

<h3 id="suggest-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|None|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_Bbox">Bbox</h2>
<!-- backwards compatibility -->
<a id="schemabbox"></a>
<a id="schema_Bbox"></a>
<a id="tocSbbox"></a>
<a id="tocsbbox"></a>

```json
{
  "north": 0.1,
  "south": 0.1,
  "east": 0.1,
  "west": 0.1
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|north|number(double)|true|none|none|
|south|number(double)|true|none|none|
|east|number(double)|true|none|none|
|west|number(double)|true|none|none|

<h2 id="tocS_CollectionDisplayNames">CollectionDisplayNames</h2>
<!-- backwards compatibility -->
<a id="schemacollectiondisplaynames"></a>
<a id="schema_CollectionDisplayNames"></a>
<a id="tocScollectiondisplaynames"></a>
<a id="tocscollectiondisplaynames"></a>

```json
{
  "collection": "string",
  "fields": {
    "property1": "string",
    "property2": "string"
  },
  "shape_columns": {
    "property1": "string",
    "property2": "string"
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|collection|string|false|none|none|
|fields|object|false|none|none|
|» **additionalProperties**|string|false|none|none|
|shape_columns|object|false|none|none|
|» **additionalProperties**|string|false|none|none|

<h2 id="tocS_CollectionReference">CollectionReference</h2>
<!-- backwards compatibility -->
<a id="schemacollectionreference"></a>
<a id="schema_CollectionReference"></a>
<a id="tocScollectionreference"></a>
<a id="tocscollectionreference"></a>

```json
{
  "collection_name": "string",
  "params": {
    "index_name": "string",
    "id_path": "string",
    "geometry_path": "string",
    "centroid_path": "string",
    "h3_path": "string",
    "timestamp_path": "string",
    "exclude_fields": "string",
    "update_max_hits": 0,
    "taggable_fields": "string",
    "exclude_wfs_fields": "string",
    "custom_params": {
      "property1": "string",
      "property2": "string"
    },
    "display_names": {
      "collection": "string",
      "fields": {
        "property1": "string",
        "property2": "string"
      },
      "shape_columns": {
        "property1": "string",
        "property2": "string"
      }
    },
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
    },
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
      "output_encoding": "string"
    },
    "inspire": {
      "keywords": [
        {
          "value": "string",
          "vocabulary": "string",
          "date_of_publication": "string"
        }
      ],
      "topic_categories": [
        "string"
      ],
      "lineage": "string",
      "languages": [
        "string"
      ],
      "spatial_resolution": {
        "value": {},
        "unit_of_measure": "string"
      },
      "inspire_uri": {
        "code": "string",
        "namespace": "string"
      },
      "inspire_limitation_access": {
        "access_constraints": "string",
        "other_constraints": "string",
        "classification": "string"
      },
      "inspire_use_conditions": "string"
    },
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
        "north": 0.1,
        "south": 0.1,
        "east": 0.1,
        "west": 0.1
      },
      "date": "string",
      "coverage": {
        "property1": {},
        "property2": {}
      },
      "coverage_centroid": "string"
    },
    "raster_tile_url": {
      "url": "string",
      "id_path": "string",
      "min_z": 0,
      "max_z": 0,
      "check_geometry": true
    },
    "raster_tile_width": 0,
    "raster_tile_height": 0,
    "filter": {
      "f": [
        [
          {
            "field": "string",
            "op": "eq",
            "value": "string"
          }
        ]
      ],
      "q": [
        [
          "string"
        ]
      ],
      "dateformat": "string",
      "righthand": true
    },
    "license_name": "string",
    "license_urls": [
      "string"
    ]
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|collection_name|string|true|none|none|
|params|[CollectionReferenceParameters](#schemacollectionreferenceparameters)|true|none|none|

<h2 id="tocS_CollectionReferenceParameters">CollectionReferenceParameters</h2>
<!-- backwards compatibility -->
<a id="schemacollectionreferenceparameters"></a>
<a id="schema_CollectionReferenceParameters"></a>
<a id="tocScollectionreferenceparameters"></a>
<a id="tocscollectionreferenceparameters"></a>

```json
{
  "index_name": "string",
  "id_path": "string",
  "geometry_path": "string",
  "centroid_path": "string",
  "h3_path": "string",
  "timestamp_path": "string",
  "exclude_fields": "string",
  "update_max_hits": 0,
  "taggable_fields": "string",
  "exclude_wfs_fields": "string",
  "custom_params": {
    "property1": "string",
    "property2": "string"
  },
  "display_names": {
    "collection": "string",
    "fields": {
      "property1": "string",
      "property2": "string"
    },
    "shape_columns": {
      "property1": "string",
      "property2": "string"
    }
  },
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
  },
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
    "output_encoding": "string"
  },
  "inspire": {
    "keywords": [
      {
        "value": "string",
        "vocabulary": "string",
        "date_of_publication": "string"
      }
    ],
    "topic_categories": [
      "string"
    ],
    "lineage": "string",
    "languages": [
      "string"
    ],
    "spatial_resolution": {
      "value": {},
      "unit_of_measure": "string"
    },
    "inspire_uri": {
      "code": "string",
      "namespace": "string"
    },
    "inspire_limitation_access": {
      "access_constraints": "string",
      "other_constraints": "string",
      "classification": "string"
    },
    "inspire_use_conditions": "string"
  },
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
      "north": 0.1,
      "south": 0.1,
      "east": 0.1,
      "west": 0.1
    },
    "date": "string",
    "coverage": {
      "property1": {},
      "property2": {}
    },
    "coverage_centroid": "string"
  },
  "raster_tile_url": {
    "url": "string",
    "id_path": "string",
    "min_z": 0,
    "max_z": 0,
    "check_geometry": true
  },
  "raster_tile_width": 0,
  "raster_tile_height": 0,
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "license_name": "string",
  "license_urls": [
    "string"
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|index_name|string|true|none|none|
|id_path|string|true|none|none|
|geometry_path|string|true|none|none|
|centroid_path|string|true|none|none|
|h3_path|string|false|none|none|
|timestamp_path|string|true|none|none|
|exclude_fields|string|false|none|none|
|update_max_hits|integer(int32)|false|none|none|
|taggable_fields|string|false|none|none|
|exclude_wfs_fields|string|false|none|none|
|custom_params|object|false|none|none|
|» **additionalProperties**|string|false|none|none|
|display_names|[CollectionDisplayNames](#schemacollectiondisplaynames)|false|none|none|
|atom_feed|[Feed](#schemafeed)|false|none|none|
|open_search|[OpenSearch](#schemaopensearch)|false|none|none|
|inspire|[Inspire](#schemainspire)|false|none|none|
|dublin_core_element_name|[DublinCoreElementName](#schemadublincoreelementname)|false|none|none|
|raster_tile_url|[RasterTileURL](#schemarastertileurl)|false|none|none|
|raster_tile_width|integer(int32)|false|none|none|
|raster_tile_height|integer(int32)|false|none|none|
|filter|[Filter](#schemafilter)|false|none|none|
|license_name|string|false|none|none|
|license_urls|[string]|false|none|none|

<h2 id="tocS_DublinCoreElementName">DublinCoreElementName</h2>
<!-- backwards compatibility -->
<a id="schemadublincoreelementname"></a>
<a id="schema_DublinCoreElementName"></a>
<a id="tocSdublincoreelementname"></a>
<a id="tocsdublincoreelementname"></a>

```json
{
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
    "north": 0.1,
    "south": 0.1,
    "east": 0.1,
    "west": 0.1
  },
  "date": "string",
  "coverage": {
    "property1": {},
    "property2": {}
  },
  "coverage_centroid": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|title|string|false|none|none|
|creator|string|false|none|none|
|subject|string|false|none|none|
|description|string|false|none|none|
|publisher|string|false|none|none|
|contributor|string|false|none|none|
|type|string|false|none|none|
|format|string|false|none|none|
|identifier|string|false|none|none|
|source|string|false|none|none|
|language|string|false|none|none|
|bbox|[Bbox](#schemabbox)|false|none|none|
|date|string|false|none|none|
|coverage|object|false|none|none|
|» **additionalProperties**|object|false|none|none|
|coverage_centroid|string|false|none|none|

<h2 id="tocS_Expression">Expression</h2>
<!-- backwards compatibility -->
<a id="schemaexpression"></a>
<a id="schema_Expression"></a>
<a id="tocSexpression"></a>
<a id="tocsexpression"></a>

```json
{
  "field": "string",
  "op": "eq",
  "value": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|field|string|false|none|none|
|op|string|false|none|none|
|value|string|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|op|eq|
|op|gte|
|op|gt|
|op|lte|
|op|lt|
|op|like|
|op|ne|
|op|range|
|op|within|
|op|notwithin|
|op|intersects|
|op|notintersects|

<h2 id="tocS_Feed">Feed</h2>
<!-- backwards compatibility -->
<a id="schemafeed"></a>
<a id="schema_Feed"></a>
<a id="tocSfeed"></a>
<a id="tocsfeed"></a>

```json
{
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

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|author|[Person](#schemaperson)|false|none|none|
|contributor|[Person](#schemaperson)|false|none|none|
|icon|string|false|none|none|
|logo|string|false|none|none|
|rights|string|false|none|none|
|subtitle|string|false|none|none|
|generator|[Generator](#schemagenerator)|false|none|none|

<h2 id="tocS_Filter">Filter</h2>
<!-- backwards compatibility -->
<a id="schemafilter"></a>
<a id="schema_Filter"></a>
<a id="tocSfilter"></a>
<a id="tocsfilter"></a>

```json
{
  "f": [
    [
      {
        "field": "string",
        "op": "eq",
        "value": "string"
      }
    ]
  ],
  "q": [
    [
      "string"
    ]
  ],
  "dateformat": "string",
  "righthand": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|f|[array]|false|none|none|
|q|[array]|false|none|none|
|dateformat|string|false|none|none|
|righthand|boolean|false|none|none|

<h2 id="tocS_Generator">Generator</h2>
<!-- backwards compatibility -->
<a id="schemagenerator"></a>
<a id="schema_Generator"></a>
<a id="tocSgenerator"></a>
<a id="tocsgenerator"></a>

```json
{
  "name": "string",
  "version": "string",
  "uri": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|name|string|false|none|none|
|version|string|false|none|none|
|uri|string|false|none|none|

<h2 id="tocS_Inspire">Inspire</h2>
<!-- backwards compatibility -->
<a id="schemainspire"></a>
<a id="schema_Inspire"></a>
<a id="tocSinspire"></a>
<a id="tocsinspire"></a>

```json
{
  "keywords": [
    {
      "value": "string",
      "vocabulary": "string",
      "date_of_publication": "string"
    }
  ],
  "topic_categories": [
    "string"
  ],
  "lineage": "string",
  "languages": [
    "string"
  ],
  "spatial_resolution": {
    "value": {},
    "unit_of_measure": "string"
  },
  "inspire_uri": {
    "code": "string",
    "namespace": "string"
  },
  "inspire_limitation_access": {
    "access_constraints": "string",
    "other_constraints": "string",
    "classification": "string"
  },
  "inspire_use_conditions": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|keywords|[[Keyword](#schemakeyword)]|false|none|none|
|topic_categories|[string]|false|none|none|
|lineage|string|false|none|none|
|languages|[string]|false|none|none|
|spatial_resolution|[InspireSpatialResolution](#schemainspirespatialresolution)|false|none|none|
|inspire_uri|[InspireURI](#schemainspireuri)|false|none|none|
|inspire_limitation_access|[InspireLimitationAccess](#schemainspirelimitationaccess)|false|none|none|
|inspire_use_conditions|string|false|none|none|

<h2 id="tocS_InspireLimitationAccess">InspireLimitationAccess</h2>
<!-- backwards compatibility -->
<a id="schemainspirelimitationaccess"></a>
<a id="schema_InspireLimitationAccess"></a>
<a id="tocSinspirelimitationaccess"></a>
<a id="tocsinspirelimitationaccess"></a>

```json
{
  "access_constraints": "string",
  "other_constraints": "string",
  "classification": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|access_constraints|string|false|none|none|
|other_constraints|string|false|none|none|
|classification|string|false|none|none|

<h2 id="tocS_InspireSpatialResolution">InspireSpatialResolution</h2>
<!-- backwards compatibility -->
<a id="schemainspirespatialresolution"></a>
<a id="schema_InspireSpatialResolution"></a>
<a id="tocSinspirespatialresolution"></a>
<a id="tocsinspirespatialresolution"></a>

```json
{
  "value": {},
  "unit_of_measure": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|value|[Number](#schemanumber)|false|none|none|
|unit_of_measure|string|false|none|none|

<h2 id="tocS_InspireURI">InspireURI</h2>
<!-- backwards compatibility -->
<a id="schemainspireuri"></a>
<a id="schema_InspireURI"></a>
<a id="tocSinspireuri"></a>
<a id="tocsinspireuri"></a>

```json
{
  "code": "string",
  "namespace": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|string|false|none|none|
|namespace|string|false|none|none|

<h2 id="tocS_Keyword">Keyword</h2>
<!-- backwards compatibility -->
<a id="schemakeyword"></a>
<a id="schema_Keyword"></a>
<a id="tocSkeyword"></a>
<a id="tocskeyword"></a>

```json
{
  "value": "string",
  "vocabulary": "string",
  "date_of_publication": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|value|string|false|none|none|
|vocabulary|string|false|none|none|
|date_of_publication|string|false|none|none|

<h2 id="tocS_Number">Number</h2>
<!-- backwards compatibility -->
<a id="schemanumber"></a>
<a id="schema_Number"></a>
<a id="tocSnumber"></a>
<a id="tocsnumber"></a>

```json
{}

```

### Properties

*None*

<h2 id="tocS_OpenSearch">OpenSearch</h2>
<!-- backwards compatibility -->
<a id="schemaopensearch"></a>
<a id="schema_OpenSearch"></a>
<a id="tocSopensearch"></a>
<a id="tocsopensearch"></a>

```json
{
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
  "output_encoding": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|short_name|string|false|none|none|
|description|string|false|none|none|
|contact|string|false|none|none|
|tags|string|false|none|none|
|long_name|string|false|none|none|
|image_height|string|false|none|none|
|image_width|string|false|none|none|
|image_type|string|false|none|none|
|image_url|string|false|none|none|
|developer|string|false|none|none|
|attribution|string|false|none|none|
|syndication_right|string|false|none|none|
|adult_content|string|false|none|none|
|language|string|false|none|none|
|input_encoding|string|false|none|none|
|output_encoding|string|false|none|none|

<h2 id="tocS_Person">Person</h2>
<!-- backwards compatibility -->
<a id="schemaperson"></a>
<a id="schema_Person"></a>
<a id="tocSperson"></a>
<a id="tocsperson"></a>

```json
{
  "name": "string",
  "email": "string",
  "uri": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|name|string|false|none|none|
|email|string|false|none|none|
|uri|string|false|none|none|

<h2 id="tocS_RasterTileURL">RasterTileURL</h2>
<!-- backwards compatibility -->
<a id="schemarastertileurl"></a>
<a id="schema_RasterTileURL"></a>
<a id="tocSrastertileurl"></a>
<a id="tocsrastertileurl"></a>

```json
{
  "url": "string",
  "id_path": "string",
  "min_z": 0,
  "max_z": 0,
  "check_geometry": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|url|string|true|none|none|
|id_path|string|true|none|none|
|min_z|integer(int32)|false|none|none|
|max_z|integer(int32)|false|none|none|
|check_geometry|boolean|false|none|none|

<h2 id="tocS_Error">Error</h2>
<!-- backwards compatibility -->
<a id="schemaerror"></a>
<a id="schema_Error"></a>
<a id="tocSerror"></a>
<a id="tocserror"></a>

```json
{
  "status": 0,
  "message": "string",
  "error": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|status|integer(int32)|false|none|none|
|message|string|false|none|none|
|error|string|false|none|none|

<h2 id="tocS_Success">Success</h2>
<!-- backwards compatibility -->
<a id="schemasuccess"></a>
<a id="schema_Success"></a>
<a id="tocSsuccess"></a>
<a id="tocssuccess"></a>

```json
{
  "status": 0,
  "message": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|status|integer(int32)|false|none|none|
|message|string|false|none|none|

<h2 id="tocS_AggregationMetric">AggregationMetric</h2>
<!-- backwards compatibility -->
<a id="schemaaggregationmetric"></a>
<a id="schema_AggregationMetric"></a>
<a id="tocSaggregationmetric"></a>
<a id="tocsaggregationmetric"></a>

```json
{
  "type": "string",
  "field": "string",
  "value": {}
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|false|none|none|
|field|string|false|none|none|
|value|object|false|none|none|

<h2 id="tocS_AggregationResponse">AggregationResponse</h2>
<!-- backwards compatibility -->
<a id="schemaaggregationresponse"></a>
<a id="schema_AggregationResponse"></a>
<a id="tocSaggregationresponse"></a>
<a id="tocsaggregationresponse"></a>

```json
{
  "query_time": 0,
  "total_time": 0,
  "totalnb": 0,
  "name": "string",
  "count": 0,
  "sumotherdoccounts": 0,
  "key": {},
  "key_as_string": {},
  "elements": [
    {
      "query_time": 0,
      "total_time": 0,
      "totalnb": 0,
      "name": "string",
      "count": 0,
      "sumotherdoccounts": 0,
      "key": {},
      "key_as_string": {},
      "elements": [],
      "metrics": [
        {
          "type": "string",
          "field": "string",
          "value": {}
        }
      ],
      "hits": [
        {}
      ],
      "geometries": [
        {
          "reference": "string",
          "geometry": {
            "crs": {
              "type": "name",
              "properties": {
                "property1": {},
                "property2": {}
              }
            },
            "bbox": [
              0.1
            ]
          },
          "sort": "string",
          "is_raw": true
        }
      ],
      "flattened_elements": {
        "property1": {},
        "property2": {}
      }
    }
  ],
  "metrics": [
    {
      "type": "string",
      "field": "string",
      "value": {}
    }
  ],
  "hits": [
    {}
  ],
  "geometries": [
    {
      "reference": "string",
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "sort": "string",
      "is_raw": true
    }
  ],
  "flattened_elements": {
    "property1": {},
    "property2": {}
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|query_time|integer(int64)|false|none|none|
|total_time|integer(int64)|false|none|none|
|totalnb|integer(int64)|false|none|none|
|name|string|false|none|none|
|count|integer(int64)|false|none|none|
|sumotherdoccounts|integer(int64)|false|none|none|
|key|object|false|none|none|
|key_as_string|object|false|none|none|
|elements|[[AggregationResponse](#schemaaggregationresponse)]|false|none|none|
|metrics|[[AggregationMetric](#schemaaggregationmetric)]|false|none|none|
|hits|[object]|false|none|none|
|geometries|[[ReturnedGeometry](#schemareturnedgeometry)]|false|none|none|
|flattened_elements|object|false|none|none|
|» **additionalProperties**|object|false|none|none|

<h2 id="tocS_Crs">Crs</h2>
<!-- backwards compatibility -->
<a id="schemacrs"></a>
<a id="schema_Crs"></a>
<a id="tocScrs"></a>
<a id="tocscrs"></a>

```json
{
  "type": "name",
  "properties": {
    "property1": {},
    "property2": {}
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|false|none|none|
|properties|object|false|none|none|
|» **additionalProperties**|object|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|name|
|type|link|

<h2 id="tocS_Feature">Feature</h2>
<!-- backwards compatibility -->
<a id="schemafeature"></a>
<a id="schema_Feature"></a>
<a id="tocSfeature"></a>
<a id="tocsfeature"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "properties": {
    "property1": {},
    "property2": {}
  },
  "geometry": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  },
  "id": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|crs|[Crs](#schemacrs)|false|none|none|
|bbox|[number]|false|none|none|
|properties|object|false|none|none|
|» **additionalProperties**|object|false|none|none|
|geometry|[GeoJsonObject](#schemageojsonobject)|false|none|none|
|id|string|false|none|none|

<h2 id="tocS_FeatureCollection">FeatureCollection</h2>
<!-- backwards compatibility -->
<a id="schemafeaturecollection"></a>
<a id="schema_FeatureCollection"></a>
<a id="tocSfeaturecollection"></a>
<a id="tocsfeaturecollection"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "features": [
    {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ],
      "properties": {
        "property1": {},
        "property2": {}
      },
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      },
      "id": "string"
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|crs|[Crs](#schemacrs)|false|none|none|
|bbox|[number]|false|none|none|
|features|[[Feature](#schemafeature)]|false|none|none|

<h2 id="tocS_GeoJsonObject">GeoJsonObject</h2>
<!-- backwards compatibility -->
<a id="schemageojsonobject"></a>
<a id="schema_GeoJsonObject"></a>
<a id="tocSgeojsonobject"></a>
<a id="tocsgeojsonobject"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|crs|[Crs](#schemacrs)|false|none|none|
|bbox|[number]|false|none|none|

<h2 id="tocS_GeometryCollection">GeometryCollection</h2>
<!-- backwards compatibility -->
<a id="schemageometrycollection"></a>
<a id="schema_GeometryCollection"></a>
<a id="tocSgeometrycollection"></a>
<a id="tocsgeometrycollection"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "geometries": [
    {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ]
    }
  ]
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» geometries|[[GeoJsonObject](#schemageojsonobject)]|false|none|none|

<h2 id="tocS_LineString">LineString</h2>
<!-- backwards compatibility -->
<a id="schemalinestring"></a>
<a id="schema_LineString"></a>
<a id="tocSlinestring"></a>
<a id="tocslinestring"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "coordinates": [
    {
      "longitude": 0.1,
      "latitude": 0.1,
      "altitude": 0.1,
      "additionalElements": [
        0.1
      ]
    }
  ]
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» coordinates|[[LngLatAlt](#schemalnglatalt)]|false|none|none|

<h2 id="tocS_LngLatAlt">LngLatAlt</h2>
<!-- backwards compatibility -->
<a id="schemalnglatalt"></a>
<a id="schema_LngLatAlt"></a>
<a id="tocSlnglatalt"></a>
<a id="tocslnglatalt"></a>

```json
{
  "longitude": 0.1,
  "latitude": 0.1,
  "altitude": 0.1,
  "additionalElements": [
    0.1
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|longitude|number(double)|false|none|none|
|latitude|number(double)|false|none|none|
|altitude|number(double)|false|none|none|
|additionalElements|[number]|false|none|none|

<h2 id="tocS_MultiLineString">MultiLineString</h2>
<!-- backwards compatibility -->
<a id="schemamultilinestring"></a>
<a id="schema_MultiLineString"></a>
<a id="tocSmultilinestring"></a>
<a id="tocsmultilinestring"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "coordinates": [
    [
      {
        "longitude": 0.1,
        "latitude": 0.1,
        "altitude": 0.1,
        "additionalElements": [
          0.1
        ]
      }
    ]
  ]
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» coordinates|[array]|false|none|none|

<h2 id="tocS_MultiPoint">MultiPoint</h2>
<!-- backwards compatibility -->
<a id="schemamultipoint"></a>
<a id="schema_MultiPoint"></a>
<a id="tocSmultipoint"></a>
<a id="tocsmultipoint"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "coordinates": [
    {
      "longitude": 0.1,
      "latitude": 0.1,
      "altitude": 0.1,
      "additionalElements": [
        0.1
      ]
    }
  ]
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» coordinates|[[LngLatAlt](#schemalnglatalt)]|false|none|none|

<h2 id="tocS_MultiPolygon">MultiPolygon</h2>
<!-- backwards compatibility -->
<a id="schemamultipolygon"></a>
<a id="schema_MultiPolygon"></a>
<a id="tocSmultipolygon"></a>
<a id="tocsmultipolygon"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "coordinates": [
    [
      [
        {
          "longitude": 0.1,
          "latitude": 0.1,
          "altitude": 0.1,
          "additionalElements": [
            0.1
          ]
        }
      ]
    ]
  ]
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» coordinates|[array]|false|none|none|

<h2 id="tocS_Point">Point</h2>
<!-- backwards compatibility -->
<a id="schemapoint"></a>
<a id="schema_Point"></a>
<a id="tocSpoint"></a>
<a id="tocspoint"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "coordinates": {
    "longitude": 0.1,
    "latitude": 0.1,
    "altitude": 0.1,
    "additionalElements": [
      0.1
    ]
  }
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» coordinates|[LngLatAlt](#schemalnglatalt)|false|none|none|

<h2 id="tocS_Polygon">Polygon</h2>
<!-- backwards compatibility -->
<a id="schemapolygon"></a>
<a id="schema_Polygon"></a>
<a id="tocSpolygon"></a>
<a id="tocspolygon"></a>

```json
{
  "crs": {
    "type": "name",
    "properties": {
      "property1": {},
      "property2": {}
    }
  },
  "bbox": [
    0.1
  ],
  "coordinates": [
    [
      {
        "longitude": 0.1,
        "latitude": 0.1,
        "altitude": 0.1,
        "additionalElements": [
          0.1
        ]
      }
    ]
  ]
}

```

### Properties

allOf - discriminator: GeoJsonObject.type

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[GeoJsonObject](#schemageojsonobject)|false|none|none|

and

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|object|false|none|none|
|» coordinates|[array]|false|none|none|

<h2 id="tocS_ReturnedGeometry">ReturnedGeometry</h2>
<!-- backwards compatibility -->
<a id="schemareturnedgeometry"></a>
<a id="schema_ReturnedGeometry"></a>
<a id="tocSreturnedgeometry"></a>
<a id="tocsreturnedgeometry"></a>

```json
{
  "reference": "string",
  "geometry": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  },
  "sort": "string",
  "is_raw": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|reference|string|false|none|none|
|geometry|[GeoJsonObject](#schemageojsonobject)|false|none|none|
|sort|string|false|none|none|
|is_raw|boolean|false|none|none|

<h2 id="tocS_Aggregation">Aggregation</h2>
<!-- backwards compatibility -->
<a id="schemaaggregation"></a>
<a id="schema_Aggregation"></a>
<a id="tocSaggregation"></a>
<a id="tocsaggregation"></a>

```json
{
  "type": "datehistogram",
  "field": "string",
  "interval": {
    "value": {},
    "unit": "year"
  },
  "format": "string",
  "metrics": [
    {
      "collect_field": "string",
      "collect_fct": "AVG",
      "precision_threshold": 0
    }
  ],
  "order": "asc",
  "on": "field",
  "size": "string",
  "include": "string",
  "raw_geometries": [
    {
      "geometry": "string",
      "sort": "string"
    }
  ],
  "aggregated_geometries": [
    "BBOX"
  ],
  "fetch_hits": {
    "size": 0,
    "include": [
      "string"
    ]
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|false|none|none|
|field|string|false|none|none|
|interval|[Interval](#schemainterval)|false|none|none|
|format|string|false|none|none|
|metrics|[[Metric](#schemametric)]|false|none|none|
|order|string|false|none|none|
|on|string|false|none|none|
|size|string|false|none|none|
|include|string|false|none|none|
|raw_geometries|[[RawGeometry](#schemarawgeometry)]|false|none|none|
|aggregated_geometries|[string]|false|none|none|
|fetch_hits|[HitsFetcher](#schemahitsfetcher)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|datehistogram|
|type|geohash|
|type|geotile|
|type|histogram|
|type|term|
|type|h3|
|order|asc|
|order|desc|
|on|field|
|on|count|
|on|result|

<h2 id="tocS_AggregationsRequest">AggregationsRequest</h2>
<!-- backwards compatibility -->
<a id="schemaaggregationsrequest"></a>
<a id="schema_AggregationsRequest"></a>
<a id="tocSaggregationsrequest"></a>
<a id="tocsaggregationsrequest"></a>

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "aggregations": [
    {
      "type": "datehistogram",
      "field": "string",
      "interval": {
        "value": {},
        "unit": "year"
      },
      "format": "string",
      "metrics": [
        {
          "collect_field": "string",
          "collect_fct": "AVG",
          "precision_threshold": 0
        }
      ],
      "order": "asc",
      "on": "field",
      "size": "string",
      "include": "string",
      "raw_geometries": [
        {
          "geometry": "string",
          "sort": "string"
        }
      ],
      "aggregated_geometries": [
        "BBOX"
      ],
      "fetch_hits": {
        "size": 0,
        "include": [
          "string"
        ]
      }
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|filter|[Filter](#schemafilter)|false|none|none|
|form|[Form](#schemaform)|false|none|none|
|aggregations|[[Aggregation](#schemaaggregation)]|false|none|none|

<h2 id="tocS_Form">Form</h2>
<!-- backwards compatibility -->
<a id="schemaform"></a>
<a id="schema_Form"></a>
<a id="tocSform"></a>
<a id="tocsform"></a>

```json
{
  "pretty": true,
  "flat": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|pretty|boolean|false|none|none|
|flat|boolean|false|none|none|

<h2 id="tocS_HitsFetcher">HitsFetcher</h2>
<!-- backwards compatibility -->
<a id="schemahitsfetcher"></a>
<a id="schema_HitsFetcher"></a>
<a id="tocShitsfetcher"></a>
<a id="tocshitsfetcher"></a>

```json
{
  "size": 0,
  "include": [
    "string"
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|size|integer(int32)|false|none|none|
|include|[string]|false|none|none|

<h2 id="tocS_Interval">Interval</h2>
<!-- backwards compatibility -->
<a id="schemainterval"></a>
<a id="schema_Interval"></a>
<a id="tocSinterval"></a>
<a id="tocsinterval"></a>

```json
{
  "value": {},
  "unit": "year"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|value|[Number](#schemanumber)|false|none|none|
|unit|string|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|unit|year|
|unit|quarter|
|unit|month|
|unit|week|
|unit|day|
|unit|hour|
|unit|minute|
|unit|second|

<h2 id="tocS_Metric">Metric</h2>
<!-- backwards compatibility -->
<a id="schemametric"></a>
<a id="schema_Metric"></a>
<a id="tocSmetric"></a>
<a id="tocsmetric"></a>

```json
{
  "collect_field": "string",
  "collect_fct": "AVG",
  "precision_threshold": 0
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|collect_field|string|false|none|none|
|collect_fct|string|false|none|none|
|precision_threshold|integer(int32)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|collect_fct|AVG|
|collect_fct|CARDINALITY|
|collect_fct|MAX|
|collect_fct|MIN|
|collect_fct|SUM|
|collect_fct|GEOCENTROID|
|collect_fct|GEOBBOX|

<h2 id="tocS_RawGeometry">RawGeometry</h2>
<!-- backwards compatibility -->
<a id="schemarawgeometry"></a>
<a id="schema_RawGeometry"></a>
<a id="tocSrawgeometry"></a>
<a id="tocsrawgeometry"></a>

```json
{
  "geometry": "string",
  "sort": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|geometry|string|false|none|none|
|sort|string|false|none|none|

<h2 id="tocS_ComputationResponse">ComputationResponse</h2>
<!-- backwards compatibility -->
<a id="schemacomputationresponse"></a>
<a id="schema_ComputationResponse"></a>
<a id="tocScomputationresponse"></a>
<a id="tocscomputationresponse"></a>

```json
{
  "query_time": 0,
  "total_time": 0,
  "totalnb": 0,
  "field": "string",
  "metric": "AVG",
  "value": 0.1,
  "geometry": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|query_time|integer(int64)|false|none|none|
|total_time|integer(int64)|false|none|none|
|totalnb|integer(int64)|false|none|none|
|field|string|false|none|none|
|metric|string|false|none|none|
|value|number(double)|false|none|none|
|geometry|[GeoJsonObject](#schemageojsonobject)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|metric|AVG|
|metric|MAX|
|metric|MIN|
|metric|SUM|
|metric|CARDINALITY|
|metric|SPANNING|
|metric|GEOBBOX|
|metric|GEOCENTROID|

<h2 id="tocS_ComputationRequest">ComputationRequest</h2>
<!-- backwards compatibility -->
<a id="schemacomputationrequest"></a>
<a id="schema_ComputationRequest"></a>
<a id="tocScomputationrequest"></a>
<a id="tocscomputationrequest"></a>

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "field": "string",
  "metric": "AVG",
  "precisionThreshold": 0
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|filter|[Filter](#schemafilter)|false|none|none|
|form|[Form](#schemaform)|false|none|none|
|field|string|false|none|none|
|metric|string|false|none|none|
|precisionThreshold|integer(int32)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|metric|AVG|
|metric|MAX|
|metric|MIN|
|metric|SUM|
|metric|CARDINALITY|
|metric|SPANNING|
|metric|GEOBBOX|
|metric|GEOCENTROID|

<h2 id="tocS_Geo">Geo</h2>
<!-- backwards compatibility -->
<a id="schemageo"></a>
<a id="schema_Geo"></a>
<a id="tocSgeo"></a>
<a id="tocsgeo"></a>

```json
{
  "path": "string",
  "geometry": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|path|string|false|none|none|
|geometry|[GeoJsonObject](#schemageojsonobject)|false|none|none|

<h2 id="tocS_Hit">Hit</h2>
<!-- backwards compatibility -->
<a id="schemahit"></a>
<a id="schema_Hit"></a>
<a id="tocShit"></a>
<a id="tocshit"></a>

```json
{
  "md": {
    "id": "string",
    "timestamp": 0,
    "geometry": {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ]
    },
    "centroid": {
      "crs": {
        "type": "name",
        "properties": {
          "property1": {},
          "property2": {}
        }
      },
      "bbox": [
        0.1
      ]
    },
    "returned_geometries": [
      {
        "path": "string",
        "geometry": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        }
      }
    ]
  },
  "data": {}
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|md|[MD](#schemamd)|false|none|none|
|data|object|false|none|none|

<h2 id="tocS_Hits">Hits</h2>
<!-- backwards compatibility -->
<a id="schemahits"></a>
<a id="schema_Hits"></a>
<a id="tocShits"></a>
<a id="tocshits"></a>

```json
{
  "collection": "string",
  "hits": [
    {
      "md": {
        "id": "string",
        "timestamp": 0,
        "geometry": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        },
        "centroid": {
          "crs": {
            "type": "name",
            "properties": {
              "property1": {},
              "property2": {}
            }
          },
          "bbox": [
            0.1
          ]
        },
        "returned_geometries": [
          {
            "path": "string",
            "geometry": {
              "crs": {
                "type": "name",
                "properties": {
                  "property1": {},
                  "property2": {}
                }
              },
              "bbox": [
                0.1
              ]
            }
          }
        ]
      },
      "data": {}
    }
  ],
  "nbhits": 0,
  "totalnb": 0,
  "links": {
    "property1": {
      "href": "string",
      "method": "string",
      "body": {}
    },
    "property2": {
      "href": "string",
      "method": "string",
      "body": {}
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|collection|string|false|none|none|
|hits|[[Hit](#schemahit)]|false|none|none|
|nbhits|integer(int64)|false|none|none|
|totalnb|integer(int64)|false|none|none|
|links|object|false|none|none|
|» **additionalProperties**|[Link](#schemalink)|false|none|none|

<h2 id="tocS_Link">Link</h2>
<!-- backwards compatibility -->
<a id="schemalink"></a>
<a id="schema_Link"></a>
<a id="tocSlink"></a>
<a id="tocslink"></a>

```json
{
  "href": "string",
  "method": "string",
  "body": {}
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|href|string|true|none|none|
|method|string|true|none|none|
|body|object|false|none|none|

<h2 id="tocS_MD">MD</h2>
<!-- backwards compatibility -->
<a id="schemamd"></a>
<a id="schema_MD"></a>
<a id="tocSmd"></a>
<a id="tocsmd"></a>

```json
{
  "id": "string",
  "timestamp": 0,
  "geometry": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  },
  "centroid": {
    "crs": {
      "type": "name",
      "properties": {
        "property1": {},
        "property2": {}
      }
    },
    "bbox": [
      0.1
    ]
  },
  "returned_geometries": [
    {
      "path": "string",
      "geometry": {
        "crs": {
          "type": "name",
          "properties": {
            "property1": {},
            "property2": {}
          }
        },
        "bbox": [
          0.1
        ]
      }
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string|false|none|none|
|timestamp|integer(int64)|false|none|none|
|geometry|[GeoJsonObject](#schemageojsonobject)|false|none|none|
|centroid|[GeoJsonObject](#schemageojsonobject)|false|none|none|
|returned_geometries|[[Geo](#schemageo)]|false|none|none|

<h2 id="tocS_Count">Count</h2>
<!-- backwards compatibility -->
<a id="schemacount"></a>
<a id="schema_Count"></a>
<a id="tocScount"></a>
<a id="tocscount"></a>

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|filter|[Filter](#schemafilter)|false|none|none|
|form|[Form](#schemaform)|false|none|none|

<h2 id="tocS_CollectionReferenceDescription">CollectionReferenceDescription</h2>
<!-- backwards compatibility -->
<a id="schemacollectionreferencedescription"></a>
<a id="schema_CollectionReferenceDescription"></a>
<a id="tocScollectionreferencedescription"></a>
<a id="tocscollectionreferencedescription"></a>

```json
{
  "properties": {
    "property1": {
      "type": "TEXT",
      "format": "string",
      "hash_field": "string",
      "properties": {
        "property1": {},
        "property2": {}
      },
      "taggable": true,
      "indexed": true
    },
    "property2": {
      "type": "TEXT",
      "format": "string",
      "hash_field": "string",
      "properties": {
        "property1": {},
        "property2": {}
      },
      "taggable": true,
      "indexed": true
    }
  },
  "collection_name": "string",
  "params": {
    "index_name": "string",
    "id_path": "string",
    "geometry_path": "string",
    "centroid_path": "string",
    "h3_path": "string",
    "timestamp_path": "string",
    "exclude_fields": "string",
    "update_max_hits": 0,
    "taggable_fields": "string",
    "exclude_wfs_fields": "string",
    "custom_params": {
      "property1": "string",
      "property2": "string"
    },
    "display_names": {
      "collection": "string",
      "fields": {
        "property1": "string",
        "property2": "string"
      },
      "shape_columns": {
        "property1": "string",
        "property2": "string"
      }
    },
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
    },
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
      "output_encoding": "string"
    },
    "inspire": {
      "keywords": [
        {
          "value": "string",
          "vocabulary": "string",
          "date_of_publication": "string"
        }
      ],
      "topic_categories": [
        "string"
      ],
      "lineage": "string",
      "languages": [
        "string"
      ],
      "spatial_resolution": {
        "value": {},
        "unit_of_measure": "string"
      },
      "inspire_uri": {
        "code": "string",
        "namespace": "string"
      },
      "inspire_limitation_access": {
        "access_constraints": "string",
        "other_constraints": "string",
        "classification": "string"
      },
      "inspire_use_conditions": "string"
    },
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
        "north": 0.1,
        "south": 0.1,
        "east": 0.1,
        "west": 0.1
      },
      "date": "string",
      "coverage": {
        "property1": {},
        "property2": {}
      },
      "coverage_centroid": "string"
    },
    "raster_tile_url": {
      "url": "string",
      "id_path": "string",
      "min_z": 0,
      "max_z": 0,
      "check_geometry": true
    },
    "raster_tile_width": 0,
    "raster_tile_height": 0,
    "filter": {
      "f": [
        [
          {
            "field": "string",
            "op": "eq",
            "value": "string"
          }
        ]
      ],
      "q": [
        [
          "string"
        ]
      ],
      "dateformat": "string",
      "righthand": true
    },
    "license_name": "string",
    "license_urls": [
      "string"
    ]
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|properties|object|false|none|none|
|» **additionalProperties**|[CollectionReferenceDescriptionProperty](#schemacollectionreferencedescriptionproperty)|false|none|none|
|collection_name|string|true|none|none|
|params|[CollectionReferenceParameters](#schemacollectionreferenceparameters)|true|none|none|

<h2 id="tocS_CollectionReferenceDescriptionProperty">CollectionReferenceDescriptionProperty</h2>
<!-- backwards compatibility -->
<a id="schemacollectionreferencedescriptionproperty"></a>
<a id="schema_CollectionReferenceDescriptionProperty"></a>
<a id="tocScollectionreferencedescriptionproperty"></a>
<a id="tocscollectionreferencedescriptionproperty"></a>

```json
{
  "type": "TEXT",
  "format": "string",
  "hash_field": "string",
  "properties": {
    "property1": {
      "type": "TEXT",
      "format": "string",
      "hash_field": "string",
      "properties": {},
      "taggable": true,
      "indexed": true
    },
    "property2": {
      "type": "TEXT",
      "format": "string",
      "hash_field": "string",
      "properties": {},
      "taggable": true,
      "indexed": true
    }
  },
  "taggable": true,
  "indexed": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|false|none|none|
|format|string|false|none|none|
|hash_field|string|false|none|none|
|properties|object|false|none|none|
|» **additionalProperties**|[CollectionReferenceDescriptionProperty](#schemacollectionreferencedescriptionproperty)|false|none|none|
|taggable|boolean|false|none|none|
|indexed|boolean|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|TEXT|
|type|KEYWORD|
|type|LONG|
|type|INTEGER|
|type|SHORT|
|type|BYTE|
|type|DOUBLE|
|type|FLOAT|
|type|DATE|
|type|BOOLEAN|
|type|BINARY|
|type|INT_RANGE|
|type|FLOAT_RANGE|
|type|LONG_RANGE|
|type|DOUBLE_RANGE|
|type|DATE_RANGE|
|type|OBJECT|
|type|NESTED|
|type|GEO_POINT|
|type|GEO_SHAPE|
|type|IP|
|type|COMPLETION|
|type|TOKEN_COUNT|
|type|MAPPER_MURMUR3|
|type|UNKNOWN|
|type|VARCHAR|
|type|CHAR|
|type|CHARACTER|
|type|BIT|
|type|TINYINT|
|type|SMALLINT|
|type|INT|
|type|BIGINT|
|type|DECIMAL|
|type|NUMERIC|
|type|REAL|
|type|DOUBLEPRECISION|
|type|TIMESTAMP|
|type|TIME|
|type|INTERVAL|
|type|GEOMETRY|
|type|GEOGRAPHY|
|type|POINT|
|type|LINESTRING|
|type|POLYGON|
|type|MULTIPOINT|
|type|MULTILINESTRING|
|type|MULTIPOLYGON|
|type|GEOMETRYCOLLECTION|
|type|MURMUR3|

<h2 id="tocS_Page">Page</h2>
<!-- backwards compatibility -->
<a id="schemapage"></a>
<a id="schema_Page"></a>
<a id="tocSpage"></a>
<a id="tocspage"></a>

```json
{
  "size": 0,
  "from": 0,
  "sort": "string",
  "after": "string",
  "before": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|size|integer(int32)|false|none|none|
|from|integer(int32)|false|none|none|
|sort|string|false|none|none|
|after|string|false|none|none|
|before|string|false|none|none|

<h2 id="tocS_Projection">Projection</h2>
<!-- backwards compatibility -->
<a id="schemaprojection"></a>
<a id="schema_Projection"></a>
<a id="tocSprojection"></a>
<a id="tocsprojection"></a>

```json
{
  "includes": "string",
  "excludes": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|includes|string|false|none|none|
|excludes|string|false|none|none|

<h2 id="tocS_Search">Search</h2>
<!-- backwards compatibility -->
<a id="schemasearch"></a>
<a id="schema_Search"></a>
<a id="tocSsearch"></a>
<a id="tocssearch"></a>

```json
{
  "filter": {
    "f": [
      [
        {
          "field": "string",
          "op": "eq",
          "value": "string"
        }
      ]
    ],
    "q": [
      [
        "string"
      ]
    ],
    "dateformat": "string",
    "righthand": true
  },
  "form": {
    "pretty": true,
    "flat": true
  },
  "page": {
    "size": 0,
    "from": 0,
    "sort": "string",
    "after": "string",
    "before": "string"
  },
  "projection": {
    "includes": "string",
    "excludes": "string"
  },
  "returned_geometries": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|filter|[Filter](#schemafilter)|false|none|none|
|form|[Form](#schemaform)|false|none|none|
|page|[Page](#schemapage)|false|none|none|
|projection|[Projection](#schemaprojection)|false|none|none|
|returned_geometries|string|false|none|none|

