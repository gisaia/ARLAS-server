# Change Log

## [v7.6.4](https://github.com/gisaia/ARLAS-server/tree/v7.6.4) (2018-09-07)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v7.6.3...v7.6.4)

**New stuff:**

- README CI badge: explicitly mention branch [\#332](https://github.com/gisaia/ARLAS-server/issues/332) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Aggregation Interval : allow decimals for histogram aggegation's interval [\#335](https://github.com/gisaia/ARLAS-server/issues/335)

**Fixed bugs:**

- Describe collection generates inconsistant warn logs [\#338](https://github.com/gisaia/ARLAS-server/issues/338)
- Documentation links given in README.md are dead links \(404\) [\#337](https://github.com/gisaia/ARLAS-server/issues/337) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v7.6.3](https://github.com/gisaia/ARLAS-server/tree/v7.6.3) (2018-07-13)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v7.6.2...v7.6.3)

**New stuff:**

- TiledGeosearch : increase max zoom level [\#326](https://github.com/gisaia/ARLAS-server/issues/326)
- make `scripts/wait-for-elasticsearch` recognize environment value `ARLAS\_ELASTIC\_NODES` [\#323](https://github.com/gisaia/ARLAS-server/issues/323)
- Made `scripts/wait-for-elasticsearch.sh` support environment variable… [\#328](https://github.com/gisaia/ARLAS-server/pull/328) ([elouanKeryell-Even](https://github.com/elouanKeryell-Even))
- Increase max zoom for tiled geosearch [\#330](https://github.com/gisaia/ARLAS-server/pull/330) ([mbarbet](https://github.com/mbarbet))

**Fixed bugs:**

- RangeService : check if the field exists before calculating its values range [\#325](https://github.com/gisaia/ARLAS-server/issues/325)

## [v7.6.2](https://github.com/gisaia/ARLAS-server/tree/v7.6.2) (2018-06-25)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v7.6.1...v7.6.2)

**Fixed bugs:**

- Flat : NullPointerException thrown when an attribute has a null value [\#321](https://github.com/gisaia/ARLAS-server/issues/321)
- Flat : remove attributes that has null value [\#322](https://github.com/gisaia/ARLAS-server/pull/322) ([MohamedHamouGisaia](https://github.com/MohamedHamouGisaia))

## [v7.6.1](https://github.com/gisaia/ARLAS-server/tree/v7.6.1) (2018-06-20)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v6.6.0...v7.6.1)

**New stuff:**

- \_geosearch : centroid/geometry is not in the returned geojson data when property map is flatted [\#311](https://github.com/gisaia/ARLAS-server/issues/311)
- GeoAggregate/Geosearch : flat the propreties of geojson [\#296](https://github.com/gisaia/ARLAS-server/issues/296) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Generate python api [\#287](https://github.com/gisaia/ARLAS-server/issues/287)
- OPENSEARCH: add url\_template\_prefix in ARLAS server configuration instread of collection reference definition [\#286](https://github.com/gisaia/ARLAS-server/issues/286) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- RASTER Tile Service for ARLAS [\#261](https://github.com/gisaia/ARLAS-server/issues/261) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- GeoAggregate and GeoSearch: add feature-type property [\#212](https://github.com/gisaia/ARLAS-server/issues/212)
- Aggregate and Geoaggregate must allow multiple metrics [\#106](https://github.com/gisaia/ARLAS-server/issues/106) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Filters: Implement date math values in range, lt and gt operators [\#87](https://github.com/gisaia/ARLAS-server/issues/87) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Feat/\#287 python api generation [\#288](https://github.com/gisaia/ARLAS-server/pull/288) ([TamerGisaia](https://github.com/TamerGisaia))

**Fixed bugs:**

- release.sh : pip publication command line fails [\#312](https://github.com/gisaia/ARLAS-server/issues/312)
- When coordinates are Long/Integer, the GEOJSON is not built. [\#284](https://github.com/gisaia/ARLAS-server/issues/284)

**Miscellaneous:**

- Multiple Metrics within a single aggregation [\#313](https://github.com/gisaia/ARLAS-server/issues/313) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- OGC URL prefix for OGC related services [\#294](https://github.com/gisaia/ARLAS-server/issues/294) [[API](https://github.com/gisaia/ARLAS-server/labels/API)] [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)] [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Documentation : update range operator documentation [\#290](https://github.com/gisaia/ARLAS-server/issues/290) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v6.6.0](https://github.com/gisaia/ARLAS-server/tree/v6.6.0) (2018-05-16)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v5.6.2...v6.6.0)

**New stuff:**

- Implement a `\_range` service that calculates the min and max values of a given field [\#269](https://github.com/gisaia/ARLAS-server/issues/269) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Exclude in search and geosearch : check that md properties are not included in the exclude patterns [\#184](https://github.com/gisaia/ARLAS-server/issues/184)
- Helm packaging [\#264](https://github.com/gisaia/ARLAS-server/pull/264) ([elouanKeryell-Even](https://github.com/elouanKeryell-Even))

**Miscellaneous:**

- Simplify `GeoSearchRESTService.getFeatures\(\)` [\#273](https://github.com/gisaia/ARLAS-server/issues/273)
- Useless `main` in `GeoTypeMapper` [\#271](https://github.com/gisaia/ARLAS-server/issues/271)
- Documentation : add documentation of Collection Model [\#144](https://github.com/gisaia/ARLAS-server/issues/144) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v5.6.2](https://github.com/gisaia/ARLAS-server/tree/v5.6.2) (2018-04-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v5.6.1...v5.6.2)

## [v5.6.1](https://github.com/gisaia/ARLAS-server/tree/v5.6.1) (2018-04-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v5.6.0...v5.6.1)

**New stuff:**

- Implements Atom Response for CSW GetRecords [\#260](https://github.com/gisaia/ARLAS-server/issues/260)

## [v5.6.0](https://github.com/gisaia/ARLAS-server/tree/v5.6.0) (2018-04-20)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v4.6.0...v5.6.0)

**Breaking changes:**

- Add Dublin Core element name to a collection [\#251](https://github.com/gisaia/ARLAS-server/issues/251)

**New stuff:**

- Add cardinality aggregation to ARLAS server [\#148](https://github.com/gisaia/ARLAS-server/issues/148) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add Csw GetCapabilities Operation [\#232](https://github.com/gisaia/ARLAS-server/issues/232) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Enable ogc bbox for geo filter [\#225](https://github.com/gisaia/ARLAS-server/issues/225) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Geo OPENSEARCH: allow bbox search [\#208](https://github.com/gisaia/ARLAS-server/issues/208) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Add a new API that tags the result of a query [\#140](https://github.com/gisaia/ARLAS-server/issues/140) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Support elasticsearch aliases as collection's index name [\#259](https://github.com/gisaia/ARLAS-server/pull/259) ([sfalquier](https://github.com/sfalquier))
- Allow multiple elasticsearch nodes configuration for TransportClient. [\#242](https://github.com/gisaia/ARLAS-server/pull/242) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)] ([sfalquier](https://github.com/sfalquier))
- Enable sniffing for elasticsearch client [\#238](https://github.com/gisaia/ARLAS-server/pull/238) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)] ([sfalquier](https://github.com/sfalquier))

**Fixed bugs:**

- DateHistogram : day unit should not be limited to 1 but month and week units should be [\#245](https://github.com/gisaia/ARLAS-server/issues/245)

**Miscellaneous:**

- Check if exclude field contains md field [\#215](https://github.com/gisaia/ARLAS-server/issues/215)
- release.sh : the building process of ARLAS server JAR must start with a clean [\#209](https://github.com/gisaia/ARLAS-server/issues/209)
- Fake mode for release script [\#206](https://github.com/gisaia/ARLAS-server/issues/206)
- Add License check in CI [\#203](https://github.com/gisaia/ARLAS-server/issues/203)
- README.md: remove all the text that is provided in the documentation [\#200](https://github.com/gisaia/ARLAS-server/issues/200) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Generate changelog based on the issues of the milestone [\#68](https://github.com/gisaia/ARLAS-server/issues/68) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v4.6.0](https://github.com/gisaia/ARLAS-server/tree/v4.6.0) (2018-04-04)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v3.5.1...v4.6.0)

**Breaking changes:**

- Upgrade to Elasticsearch 6.2.3 [\#137](https://github.com/gisaia/ARLAS-server/issues/137)

**New stuff:**

- Add all include/exclude filter in WFS query [\#188](https://github.com/gisaia/ARLAS-server/issues/188) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Add include/exclue fields in Atom/Opensearch [\#218](https://github.com/gisaia/ARLAS-server/issues/218) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Add include/exclue fields in ElasticDocument GetSource [\#217](https://github.com/gisaia/ARLAS-server/issues/217)

## [v3.5.1](https://github.com/gisaia/ARLAS-server/tree/v3.5.1) (2018-03-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v3.5.0...v3.5.1)

**New stuff:**

- Add wfs exclude field in CollectionReferenceParameters [\#187](https://github.com/gisaia/ARLAS-server/issues/187) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
-   Configuration: applicationContextPath can be customized in a container via an environment variable [\#158](https://github.com/gisaia/ARLAS-server/issues/158) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]

**Fixed bugs:**

- GeoSearch : geometry attribute of the geojson result doesn't not return the centroid when the geometry field is null or absent [\#216](https://github.com/gisaia/ARLAS-server/issues/216)

## [v3.5.0](https://github.com/gisaia/ARLAS-server/tree/v3.5.0) (2018-03-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v2.5.7...v3.5.0)

**New stuff:**

- Generate arlas-api with typescript-fetch [\#199](https://github.com/gisaia/ARLAS-server/issues/199)
- Plug OGC wfs test suite docker in ARLAS Server CI [\#172](https://github.com/gisaia/ARLAS-server/issues/172)
- Implement Complex Feature in WFS Service [\#170](https://github.com/gisaia/ARLAS-server/issues/170) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Implement Basic WFS with Simple Feature. [\#169](https://github.com/gisaia/ARLAS-server/issues/169) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Enable/Disable services [\#149](https://github.com/gisaia/ARLAS-server/issues/149)

**Fixed bugs:**

- Support All ARLAS geometry for WFS [\#190](https://github.com/gisaia/ARLAS-server/issues/190)
- Filter f : cannot filter on fields values containing ':' as a caracter [\#146](https://github.com/gisaia/ARLAS-server/issues/146)
- number types are not recognized in service 'arlas/explore/{collection}/\_describe' [\#145](https://github.com/gisaia/ARLAS-server/issues/145)

**Miscellaneous:**

- Avoid "." in xml element name of a WFS response [\#194](https://github.com/gisaia/ARLAS-server/issues/194) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Implement header partition filter for WFS [\#189](https://github.com/gisaia/ARLAS-server/issues/189) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)] [[security](https://github.com/gisaia/ARLAS-server/labels/security)]
- Refactor ElasticCollectionReferenceDaoImpl with jackson serialize/marshall [\#178](https://github.com/gisaia/ARLAS-server/issues/178)

## [v2.5.7](https://github.com/gisaia/ARLAS-server/tree/v2.5.7) (2018-03-07)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v2.5.6...v2.5.7)

**New stuff:**

- WFS configuration [\#176](https://github.com/gisaia/ARLAS-server/issues/176) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)] [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]

**Fixed bugs:**

- Range queries on date fields : values should be in milliseconds  [\#174](https://github.com/gisaia/ARLAS-server/issues/174)

## [v2.5.6](https://github.com/gisaia/ARLAS-server/tree/v2.5.6) (2018-03-02)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v2.5.5...v2.5.6)

**New stuff:**

- Type centroid and geometry properties in MD model [\#164](https://github.com/gisaia/ARLAS-server/issues/164)
- Implement Simple WFS with simple Feature [\#168](https://github.com/gisaia/ARLAS-server/issues/168) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]

**Fixed bugs:**

- mkDocs.sh fails to build the doc on 2.5.4 and 2.5.5 [\#166](https://github.com/gisaia/ARLAS-server/issues/166)

## [v2.5.5](https://github.com/gisaia/ARLAS-server/tree/v2.5.5) (2018-02-19)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v2.5.4...v2.5.5)

## [v2.5.4](https://github.com/gisaia/ARLAS-server/tree/v2.5.4) (2018-02-16)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v2.5.3...v2.5.4)

**New stuff:**

- Max number of hits / aggregations [\#147](https://github.com/gisaia/ARLAS-server/issues/147)



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*