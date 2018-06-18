# Change Log

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