# INSPIRE compliant Data sets and WFS & CSW services

As from v9.6.0 of ARLAS-server, Data sets and WFS & CSW are compliant with INSPIRE standard.

## Activation 

To activate INSPIRE compliant elements response, you need to enable the corresponding option in ARLAS-server configuration file.
`arlas-inspire.enabled: true`.

For more information about how to set ARLAS-server configuration options, please refer to [this link](arlas-server-configuration.md/#inspire).

## Required INSPIRE metadata elements for Spatial data sets (ARLAS collections references)

The metadata elements are to be set at ARLAS-server configuration stage and at collection reference creation.

The table below sums up these metadata elements and how to customize them through the [ARLAS-server configuration](arlas-server-configuration.md) and [ARLAS collection reference model](arlas-collection-model.md)


| Dataset INSPIRE Metadata elements|  Description | Customization |
| --- | ---| --- |
|Resource Title| Title of the data set| [`dublin_core_element_name.title` node in Collection Reference model](arlas-collection-model.md/#dublin-core) |
|Resource Abstract|Description of the data set| [`dublin_core_element_name.desciption` node in Collection Reference model](arlas-collection-model.md/#dublin-core)  |
|Resource Type| `dataset`| Nothing to customize |
|Resource Locator| Link to the GetFeature Request of WFS to download data set| Customize the server uri [`arlas-ogc.serverUri` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)  |
|Unique resource identifier|A value uniquely identifying the resource | [`inspire.inspire_uri` node in CollectionReference model](arlas-collection-model.md/#inspire) |
|Resource Language | The language(s) used within the resource. The value domain of this metadata element is limited to the languages defined in ISO 639-2 |[`inspire.languages` node in CollectionReference model](arlas-collection-model.md/#inspire)|
|Topic Category | The topic category is a high-level classification scheme to assist in the grouping and topic-based search of available spatial data resources. Must be one of the values in [this list](http://inspire.ec.europa.eu/metadata-codelist/TopicCategory)|[`inspire.topic_categories` node in CollectionReference model](arlas-collection-model.md/#inspire)|
|Keyword| Keywords characterising the data set.| [`inspire.keywords` node in CollectionReference model](arlas-collection-model.md/#inspire)  |
|Geographic Bounding Box| The extent of data set. | [`dublin_core_element_name.bbox` node in CollectionReference model](arlas-collection-model.md/#dublin-core). Long/lat must be in decimal degrees, with a precision of at least two decimals  |
|Temporal Reference| Corresponds to the creation date of the data set| Nothing to customize  |
|Lineage| This is a statement on process history and/or overall quality of the spatial data set. Where appropriate it may include a statement whether the data set has been validated or quality assured, whether it is the official version (if multiple versions exist) | [`inspire.lineage` node in CollectionReference model](arlas-collection-model.md/#inspire)  |
|Spatial Resolution| Spatial resolution refers to the level of detail of the data set. It shall be expressed as a set of zero to many resolution distances (typically for gridded data and imagery-derived products) or equivalent scales (typically for maps or map-derived product | [`inspire.spatial_resolution` node in CollectionReference model](arlas-collection-model.md/#inspire)  |
|Conformity| List of the implementing rules to which data set conforms. The date of publication and the degree conformity of these implementing rules are included.| Nothing to customize |
|Conditions for Access and Use| Provides information on any fees necessary to access and use the data set| [`inspire.inspire_use_conditions` node in CollectionReference model](arlas-collection-model.md/#inspire).  |
|Limitation on public access | Describes access constraints applied to assure the protection of privacy or intellectual property, and any special restrictions or limitations on obtaining the dataset.| [`inspire.inspire_limitation_access` node in CollectionReference model](arlas-collection-model.md/#inspire) |
|Responsible Organisation|Name and contact info of the organization responsible for providing and maintaining the dataset and its metadata| [`arlas-ogc` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)    |
|Metadata Point Of Contact|Name and email of the organization responsible for maintaining the dataset and its metadata| [`arlas-ogc.serviceProviderName` and `arlas-ogc.serviceContactMail` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)    |
|Metadata Date|The date which specifies when the metadata record was created or updated| Nothing to customize |
|Metadata Language|The language in which the metadata elements are expressed. The value domain of this metadata element is limited to the official languages of the European Community expressed in conformity with ISO 639-2. | [`dublin_core_element_name.language` node in CollectionReference model](arlas-collection-model.md/#dublin-core)|


## INSPIRE compliant WFS - Download Services

### WFS GetCapabilities Response

The GetCapabilities Response updates existing ISO 19142 XML elements of `<WFS_Capabilities>` and contains a new XML extension `<inspire_dls:ExtendedCapabilities>` that makes all INSPIRE metadata elements about WFS available.


The table below sums up these metadata elements and how to customize them through the [ARLAS-server configuration](arlas-server-configuration.md) and [Collection Reference model](arlas-collection-model.md)


| INSPIRE Metadata elements|  Description | Customization |
| --- | ---| --- |
|Resource Title| Title of the WFS service| Title of the data set it serves* in [`dublin_core_element_name.title` node in Collection Reference model](arlas-collection-model.md/#dublin-core) prefixed with `WFS Download service` |
|Resource Abstract|Description of the WFS service| Description of the data set it serves* [`dublin_core_element_name.desciption` node in Collection Reference model](arlas-collection-model.md/#dublin-core) prefixed with `WFS Download service`  |
|Resource Type| `service`| Nothing to customize |
|Resource Locator| Link to the GetCapabilities Request| Customize the server uri [`arlas-ogc.serverUri` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)  |
|Spatial Data Service Type| `download` | Nothing to customize |
|Keyword| `infoFeatureAccessService` | Nothing to customize  |
|Geographic Bounding Box| The extent of data queried by WFS. | [`dublin_core_element_name.bbox` node in CollectionReference model](arlas-collection-model.md/#dublin-core). Long/lat must be in decimal degrees, with a precision of at least two decimals  |
|Temporal Reference| Corresponds to the creation date of the WFS service| [`arlas-inspire.services_date_of_creation` in ARLAS-server configuration](arlas-server-configuration.md/#inspire).  |
|Conformity| List of the implementing rules to which WFS conforms. The date of publication and the degree conformity of these implementing rules are included.| Nothing to customize |
|Conditions for Access and Use| Provides information on any fees necessary to access and use the WFS| [`arlas-inspire.access_and_use_conditions` in ARLAS-server configuration](arlas-server-configuration.md/#inspire).  |
|Limitation on public access | Describes access constraints applied to assure the protection of privacy or intellectual property, and any special restrictions or limitations on obtaining the Inspire compliant WFS.| [`inspire.inspire_limitation_access` node in CollectionReference model](arlas-collection-model.md/#inspire) |
|Responsible Organisation|Name and contact info of the organization responsible for providing and maintaining the WFS and its metadata| [`arlas-ogc` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)    |
|Metadata Point Of Contact|Name and email of the organization responsible for maintaining the WFS and its metadata| [`arlas-ogc.serviceProviderName` and `arlas-ogc.serviceContactMail` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)    |
|Metadata Date|The date which specifies when the metadata record was created or updated| Nothing to customize |
|Metadata Language|The language in which the metadata elements are expressed. The value domain of this metadata element is limited to the official languages of the European Community expressed in conformity with ISO 639-2. | [`dublin_core_element_name.language` node in CollectionReference model](arlas-collection-model.md/#dublin-core) |
|Unique resource identifier|A value uniquely identifying the resource | Same identifier of the data set it serves* : [`inspire.inspire_uri` node in CollectionReference model](arlas-collection-model.md/#inspire) |
|Coupled Resource| Link to all the INSPIRE metadata elements in GMD format respecting the ISO 19139 | Nothing to customize |

*) Each data set is served by a WFS endpoint : `{ogcConfiguration.serverUri}/ogc/wfs/{collectionName}?service=WFS`. 

All this INSPIRE metadata elements are also accessible via a **link** to a Download Service metadata record given in `<inspire_common:MetadataURL>` in the extended capabilities section.

This link describes INSPIRE metadata elements in GMD format that respects the ISO 19139.

### WFS GetCapabilities Request

#### Language parameter

The GetCapabilities request accepts a __`language`__ parameter. 

- If the requested language is contained in the list of supported languages, then natural language metadata elements are returned in the requested language.  
- If the requested language is not supported by the WFS, then this parameter is ignored. 
- The value domain of this parameter is limited to the official languages of the European Community expressed in conformity with ISO 639-2.

## INSPIRE compliant CSW - Discovery Services

### CSW GetCapabilities Response

CSW GetCapabilities Response also updates `<csw:GetCapabilities>` existing elements and contains a new XML extension `<inspire_dls:ExtendedCapabilities>` that makes all INSPIRE metadata elements about CSW available.

The table below sums up these metadata elements and how to customize them through the [ARLAS-server configuration](arlas-server-configuration.md)

| INSPIRE Metadata elements | Description | Customization |
| --- | --- | --- |
|Resource Title| Title of the CSW service| [`arlas-csw.serviceIdentificationTitle` in ARLAS-server configuration](arlas-server-configuration.md/#csw) |
|Resource Abstract|Description of the CSW service| [`arlas-csw.serviceIdentificationAbstract` in ARLAS-server configuration](arlas-server-configuration.md/#csw)  |
|Resource Type| `service`| Nothing to customize |
|Resource Locator| Link to the GetCapabilities Request| Customize the server uri [`arlas-ogc.serverUri` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)  |
|Spatial Data Service Type| `discovery` | Nothing to customize |
|Keyword| `infoCatalogService`.  Keywords of all data sets are also provided. | Nothing to customize |
|Date Of Creation| Corresponds to the creation date of the CSW service| [`arlas-inspire.services_date_of_creation` in ARLAS-server configuration](arlas-server-configuration.md/#inspire).  |
|Conformity| List of the implementing rules to which  CSW & WFS conform. The date of publication and the degree conformity of these implementing rules are included.| Nothing to customize |
|Conditions for Access and Use| Provides information on any fees necessary to access and use the CSW| [`arlas-inspire.access_and_use_conditions` in ARLAS-server configuration](arlas-server-configuration.md/#inspire).  |
|Limitation on public access | Describes access constraints applied to assure the protection of privacy or intellectual property, and any special restrictions or limitations on obtaining the Inspire compliant CSW.| [`arlas-inspire.access_and_use_conditions` in ARLAS-server configuration](arlas-server-configuration.md/#inspire) |
|Responsible Organisation|Name and contact info of the organization responsible for providing and maintaining the CSW and its metadata| [`arlas-ogc` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)    |
|Metadata Point Of Contact|Name and email of the organization responsible for maintaining the CSW & WFS services and their metadata| [`arlas-ogc.serviceProviderName` and `arlas-ogc.serviceContactMail` in ARLAS-server configuration](arlas-server-configuration.md/#ogc)    |
|Metadata Date|The date which specifies when the metadata record was created or updated| Nothing to customize |
|Metadata Language|The language in which the metadata elements are expressed. The value domain of this metadata element is limited to the official languages of the European Community expressed in conformity with ISO 639-2. | [`arlas-csw.serviceIdentificationLanguage` in ARLAS-server configuration](arlas-server-configuration.md/#csw) |

### CSW GetCapabilities Request

#### Language parameter

The GetCapabilities request accepts a __`language`__ parameter. 

- If the requested language is contained in the list of supported languages, then natural language metadata elements (Title, Abstract, ...) are returned in the requested language.  
- If the requested language is not supported by the WFS, then this parameter is ignored. 
- The value domain of this parameter is limited to the official languages of the European Community expressed in conformity with ISO 639-2.

### Discover Metadata - GetRecords

Metadata elements of all data sets are searchable through the `GetRecords` request of **CSW**.

You can specify your metadata search query in the `constraint` parameter.

#### Constraint parameter

The `constraint` parameter accepts queries written in `OGC Filter 2.0` constraint language.

> Example: `constraint=<Filter xmlns:wfs="http://www.opengis.net/wfs/2.0"><PropertyIsEqualTo matchAction="Any" matchCase="true"><ValueReference>OrganisationName</ValueReference><Literal>Arlas</Literal></PropertyIsEqualTo></Filter>`

In this query :
- `ValueReference` corresponds to the **queryable** (`OrganisationName` in this example) that represents a metadata element. 
- `Literal` corresponds to the value of the queryable (`Arlas` in this example.)

The metadata elements listed in [Required INSPIRE metadata elements for Spatial data sets](#required-inspire-metadata-elements-for-spatial-data-sets-arlas-collections-references) are mapped to **queryables** defined by OGC and **queryables** defined by INSPIRE.

The table below maps metadata elements to the appropriate OGC (ISO) **queryables** :

| INSPIRE metadata element | OGC (ISO) queryables |
| --- | --- |
| Keyword | `Subject` |
| Topic category | `TopicCategory` |
| Spatial resolution | `SpatialResolution` |
| Responsible party | `OrganisationName` |
| Resource Title | `Title` |
| Resource Abstract | `Abstract` |
| Resource Type | `Type` |
| Unique resource identifier | `ResourceIdentifier` |
| Temporal Reference | `CreationDate` |
| Metadata language | `Language` |

!!! note
    **Geographic bounding box** element can be queried using `bbox` parameter of the CSW service.
    

The table below maps metadata elements to the additional **queryables** defined by INSPIRE :

| INSPIRE metadata element | INSPIRE queryables |
| --- | --- |
| Conformity specification title | `SpecificationTitle` |
| Conformity specification date | `SpecificationDate` |
| Conformity specification date type (publication, creation, ..) | `SpecificationDateType` |
| Conformity degree | `Degree` |
| Responsible party role | `ResponsiblePartyRole` |
| Lineage | `Lineage` |
| Conditions for Access and Use | `ConditionApplyingToAccessAndUse` |
| Limitations on public access :  access constraint | `AccessConstraints` |
| Limitations on public access :  other restrictions | `OtherConstraints` |
| Limitations on public access :  classification | `Classification` |

!!! note
    - All the OGC queryables are advertised in CSW GetCapabilities response in the section `SupportedISOQueryables`
    - All the INSPIRE queryables are advertised in CSW GetCapabilities response in the section `AdditionalQueryables`


#### Common response structure

To ensure a common response structure for a Discover Metadata request (GetRecords request), the value of the following request parameters shall be set as follows:

- `resultType` = results
- `outputFormat` = application/xml
- `outputSchema` = http://www.isotc211.org/2005/gmd
- `ElementSetName` = full