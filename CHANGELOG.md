# Change Log

## [v26.0.8](https://github.com/gisaia/ARLAS-server/tree/v26.0.8) (2025-02-21)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v27.0.0...v26.0.8)

## [v27.0.0](https://github.com/gisaia/ARLAS-server/tree/v27.0.0) (2025-01-31)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.7...v27.0.0)

## [v26.0.7](https://github.com/gisaia/ARLAS-server/tree/v26.0.7) (2025-01-08)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.6...v26.0.7)

**New stuff:**

- Allow Geotile/Gohash/Geohex aggregation on geo-shape fields [\#969](https://github.com/gisaia/ARLAS-server/issues/969) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

## [v26.0.6](https://github.com/gisaia/ARLAS-server/tree/v26.0.6) (2024-11-25)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.4...v26.0.6)

## [v26.0.4](https://github.com/gisaia/ARLAS-server/tree/v26.0.4) (2024-11-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.3...v26.0.4)

## [v26.0.3](https://github.com/gisaia/ARLAS-server/tree/v26.0.3) (2024-10-24)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.1...v26.0.3)

**New stuff:**

- IAM: index\_path of a collection should be prefixed by the organisation name of the user at creation/edition [\#985](https://github.com/gisaia/ARLAS-server/issues/985)
- add delete verb for aproc/jobs in datasets and downloader roles [\#981](https://github.com/gisaia/ARLAS-server/issues/981)
- Add a new endpoint to patch the display\_name of a given collection [\#978](https://github.com/gisaia/ARLAS-server/issues/978) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Remove the node "organisations" in PATCH organisation endpoint [\#977](https://github.com/gisaia/ARLAS-server/issues/977) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

## [v26.0.1](https://github.com/gisaia/ARLAS-server/tree/v26.0.1) (2024-08-30)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.1-rc.1...v26.0.1)

## [v26.0.1-rc.1](https://github.com/gisaia/ARLAS-server/tree/v26.0.1-rc.1) (2024-08-23)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v26.0.0...v26.0.1-rc.1)

## [v26.0.0](https://github.com/gisaia/ARLAS-server/tree/v26.0.0) (2024-08-21)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.1.0...v26.0.0)

## [v25.1.0](https://github.com/gisaia/ARLAS-server/tree/v25.1.0) (2024-07-04)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0...v25.1.0)

**New stuff:**

- Extend applying geobbox and geocentroid metrics to geo-shape fields [\#967](https://github.com/gisaia/ARLAS-server/issues/967) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

**Fixed bugs:**

- Apply an OR operation between different Partition filters. [\#964](https://github.com/gisaia/ARLAS-server/issues/964)
- BBOX with e-notation is misinterpreted as a WKT [\#915](https://github.com/gisaia/ARLAS-server/issues/915)

**Miscellaneous:**

- Update documentation of partition-filters [\#966](https://github.com/gisaia/ARLAS-server/issues/966) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v25.0.0](https://github.com/gisaia/ARLAS-server/tree/v25.0.0) (2024-05-15)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-rc.2...v25.0.0)

## [v25.0.0-rc.2](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-rc.2) (2024-05-05)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-rc.1...v25.0.0-rc.2)

## [v25.0.0-rc.1](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-rc.1) (2024-05-04)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-beta.6...v25.0.0-rc.1)

**Miscellaneous:**

- Upgrade Dropwizard to version 4.x [\#884](https://github.com/gisaia/ARLAS-server/issues/884)

## [v25.0.0-beta.6](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-beta.6) (2024-03-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-beta.5...v25.0.0-beta.6)

## [v25.0.0-beta.5](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-beta.5) (2024-03-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-beta.4...v25.0.0-beta.5)

**Fixed bugs:**

- Released versions of ARLAS have their swagger file pointing 23.0.1 always [\#904](https://github.com/gisaia/ARLAS-server/issues/904)
- swagger2markdown documentation is generated in every sub-module of ARLAS-server [\#894](https://github.com/gisaia/ARLAS-server/issues/894) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

**Miscellaneous:**

- Use an alternative to convertSwagger2markup for openapi spec \(v3\) [\#953](https://github.com/gisaia/ARLAS-server/issues/953)

## [v25.0.0-beta.4](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-beta.4) (2024-03-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-beta.3...v25.0.0-beta.4)

## [v25.0.0-beta.3](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-beta.3) (2024-03-20)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v25.0.0-beta.2...v25.0.0-beta.3)

**Miscellaneous:**

- Dependencies version updates [\#950](https://github.com/gisaia/ARLAS-server/issues/950)
- Upgrade JWT dependency version [\#830](https://github.com/gisaia/ARLAS-server/issues/830)

## [v25.0.0-beta.2](https://github.com/gisaia/ARLAS-server/tree/v25.0.0-beta.2) (2024-03-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.1.2...v25.0.0-beta.2)

## [v24.1.2](https://github.com/gisaia/ARLAS-server/tree/v24.1.2) (2024-03-06)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.1.1...v24.1.2)

**Miscellaneous:**

- API: Add a mean to update collection visibility to organisations and public visibility [\#937](https://github.com/gisaia/ARLAS-server/issues/937) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

## [v24.1.1](https://github.com/gisaia/ARLAS-server/tree/v24.1.1) (2024-02-14)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.1.0...v24.1.1)

## [v24.1.0](https://github.com/gisaia/ARLAS-server/tree/v24.1.0) (2023-12-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.0.3...v24.1.0)

## [v24.0.3](https://github.com/gisaia/ARLAS-server/tree/v24.0.3) (2023-09-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.0.2...v24.0.3)

**Fixed bugs:**

- Dates should be given in timestamp if dateformat is not specified [\#910](https://github.com/gisaia/ARLAS-server/issues/910)

## [v24.0.2](https://github.com/gisaia/ARLAS-server/tree/v24.0.2) (2023-06-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.0.1...v24.0.2)

**Fixed bugs:**

- Paginating to previous pages throws an unhandled operation exception [\#900](https://github.com/gisaia/ARLAS-server/issues/900)

## [v24.0.1](https://github.com/gisaia/ARLAS-server/tree/v24.0.1) (2023-04-21)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v24.0.0...v24.0.1)

**Miscellaneous:**

- \[es\] Migrate to new Java ES client [\#765](https://github.com/gisaia/ARLAS-server/issues/765)

## [v24.0.0](https://github.com/gisaia/ARLAS-server/tree/v24.0.0) (2023-04-19)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.9...v24.0.0)

**New stuff:**

- Show only public collections to anonymous users [\#889](https://github.com/gisaia/ARLAS-server/issues/889)

## [v23.0.9](https://github.com/gisaia/ARLAS-server/tree/v23.0.9) (2023-02-10)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.8-doc.2...v23.0.9)

**New stuff:**

- Support multiple Partition filter [\#869](https://github.com/gisaia/ARLAS-server/issues/869)
- Filter collections on organisation name [\#864](https://github.com/gisaia/ARLAS-server/issues/864)
- Empty column filter should forbid all collections [\#860](https://github.com/gisaia/ARLAS-server/issues/860)
- \[fetch\_hits\]\[raw\_geometries\] share the same `top\_hits` call  [\#854](https://github.com/gisaia/ARLAS-server/issues/854)
- \[fetch\_hits\]\[raw\_geometries\] make sort on chosen fields optional [\#853](https://github.com/gisaia/ARLAS-server/issues/853)

## [v23.0.8-doc.2](https://github.com/gisaia/ARLAS-server/tree/v23.0.8-doc.2) (2022-12-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.8-doc...v23.0.8-doc.2)

## [v23.0.8-doc](https://github.com/gisaia/ARLAS-server/tree/v23.0.8-doc) (2022-12-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.8...v23.0.8-doc)

## [v23.0.8](https://github.com/gisaia/ARLAS-server/tree/v23.0.8) (2022-12-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.8-beta.1...v23.0.8)

## [v23.0.8-beta.1](https://github.com/gisaia/ARLAS-server/tree/v23.0.8-beta.1) (2022-11-28)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.7...v23.0.8-beta.1)

## [v23.0.7](https://github.com/gisaia/ARLAS-server/tree/v23.0.7) (2022-11-28)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.2...v23.0.7)

## [v22.0.2](https://github.com/gisaia/ARLAS-server/tree/v22.0.2) (2022-11-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.6...v22.0.2)

## [v23.0.6](https://github.com/gisaia/ARLAS-server/tree/v23.0.6) (2022-11-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.5...v23.0.6)

## [v23.0.5](https://github.com/gisaia/ARLAS-server/tree/v23.0.5) (2022-11-16)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.4...v23.0.5)

**Fixed bugs:**

- NPE on stac collection endpoint if no data in the collection [\#840](https://github.com/gisaia/ARLAS-server/issues/840)

**Miscellaneous:**

- Remove Zipkin support [\#843](https://github.com/gisaia/ARLAS-server/issues/843) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)] [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v23.0.4](https://github.com/gisaia/ARLAS-server/tree/v23.0.4) (2022-10-14)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.3...v23.0.4)

## [v23.0.3](https://github.com/gisaia/ARLAS-server/tree/v23.0.3) (2022-10-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.2...v23.0.3)

## [v23.0.2](https://github.com/gisaia/ARLAS-server/tree/v23.0.2) (2022-09-13)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v23.0.0...v23.0.2)

**Fixed bugs:**

- Fix broken STAC /api endpoint [\#821](https://github.com/gisaia/ARLAS-server/issues/821)
- Upgrade maven plugins in java-client pom.xml [\#812](https://github.com/gisaia/ARLAS-server/issues/812)

## [v23.0.0](https://github.com/gisaia/ARLAS-server/tree/v23.0.0) (2022-07-08)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.1...v23.0.0)

**Fixed bugs:**

- Use the base uri of the request if ARLAS\_BASE\_URI is not set [\#770](https://github.com/gisaia/ARLAS-server/issues/770)

**Miscellaneous:**

- Upgrade dependencies [\#809](https://github.com/gisaia/ARLAS-server/issues/809)
- Migrate to ES client 7.17  [\#804](https://github.com/gisaia/ARLAS-server/issues/804)
- add trivy action in github CI to detect CVEs [\#793](https://github.com/gisaia/ARLAS-server/issues/793)
- Handle polygons orientation on ARLAS-server side [\#778](https://github.com/gisaia/ARLAS-server/issues/778)

## [v22.0.1](https://github.com/gisaia/ARLAS-server/tree/v22.0.1) (2022-05-24)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.0...v22.0.1)

## [v22.0.0](https://github.com/gisaia/ARLAS-server/tree/v22.0.0) (2022-05-23)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.0-beta.4...v22.0.0)

## [v22.0.0-beta.4](https://github.com/gisaia/ARLAS-server/tree/v22.0.0-beta.4) (2022-05-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.0-beta.3...v22.0.0-beta.4)

## [v22.0.0-beta.3](https://github.com/gisaia/ARLAS-server/tree/v22.0.0-beta.3) (2022-05-10)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.0-beta.2...v22.0.0-beta.3)

## [v22.0.0-beta.2](https://github.com/gisaia/ARLAS-server/tree/v22.0.0-beta.2) (2022-05-06)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v22.0.0-beta.1...v22.0.0-beta.2)

## [v22.0.0-beta.1](https://github.com/gisaia/ARLAS-server/tree/v22.0.0-beta.1) (2022-05-06)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v21.0.0...v22.0.0-beta.1)

**New stuff:**

- Forbid access if no column filter is provided [\#782](https://github.com/gisaia/ARLAS-server/issues/782)

**Fixed bugs:**

- `checkFields` query param is ignored when PUTing a new collection [\#784](https://github.com/gisaia/ARLAS-server/issues/784)
- H3 aggregations with geometries as bbox  returns invalid geojson [\#780](https://github.com/gisaia/ARLAS-server/issues/780)

## [v21.0.0](https://github.com/gisaia/ARLAS-server/tree/v21.0.0) (2022-04-05)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v20.7.0...v21.0.0)

**New stuff:**

- Add the possibility to declare an alias \(name\) for the collection [\#764](https://github.com/gisaia/ARLAS-server/issues/764)
- Add a new query param that allows to choose orientation of passed WKT [\#760](https://github.com/gisaia/ARLAS-server/issues/760) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Enhance shape export service with key,value label/field map collection [\#759](https://github.com/gisaia/ARLAS-server/issues/759)
- Add key,value map in collection definition to define human readable label for mapping field [\#758](https://github.com/gisaia/ARLAS-server/issues/758)
- Support multi-collection in `partition-filter` [\#749](https://github.com/gisaia/ARLAS-server/issues/749)

**Fixed bugs:**

- Documentation misuses the terms 'right' and 'left' for wkt orientation [\#761](https://github.com/gisaia/ARLAS-server/issues/761) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- `\_search` and `\_geosearch` don't return the same results when applying a `before` pagination  [\#755](https://github.com/gisaia/ARLAS-server/issues/755)
- Doc for 'f' parameter is truncated [\#693](https://github.com/gisaia/ARLAS-server/issues/693) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Boolean search: wrong exception saying that field must be numeric [\#380](https://github.com/gisaia/ARLAS-server/issues/380)
- Throw appropriate exception when applying filters on non-indexed fields [\#357](https://github.com/gisaia/ARLAS-server/issues/357)

**Miscellaneous:**

- Fix properly the warnings at Arlas-server startup [\#428](https://github.com/gisaia/ARLAS-server/issues/428)

## [v20.7.0](https://github.com/gisaia/ARLAS-server/tree/v20.7.0) (2022-01-07)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.8...v20.7.0)

**New stuff:**

- Add a new parameter in `putCollection` that makes validation of geometrical-fields optional [\#748](https://github.com/gisaia/ARLAS-server/issues/748)
- Make referencing centroid\_path and geometry\_path mandatory again [\#747](https://github.com/gisaia/ARLAS-server/issues/747)

**Fixed bugs:**

- Column filter with multiple predicates is faulty [\#742](https://github.com/gisaia/ARLAS-server/issues/742)
- Tests fail [\#392](https://github.com/gisaia/ARLAS-server/issues/392)

**Miscellaneous:**

- Migrate to java 17 [\#707](https://github.com/gisaia/ARLAS-server/issues/707)

## [v19.7.8](https://github.com/gisaia/ARLAS-server/tree/v19.7.8) (2021-12-02)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.7...v19.7.8)

## [v19.7.7](https://github.com/gisaia/ARLAS-server/tree/v19.7.7) (2021-10-20)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.6...v19.7.7)

**Fixed bugs:**

- Export Shapefile on geohash aggregation fails [\#736](https://github.com/gisaia/ARLAS-server/issues/736)

## [v19.7.6](https://github.com/gisaia/ARLAS-server/tree/v19.7.6) (2021-10-08)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.5...v19.7.6)

**Miscellaneous:**

- Update Elasticsearch to 7.14 [\#735](https://github.com/gisaia/ARLAS-server/issues/735)

## [v19.7.5](https://github.com/gisaia/ARLAS-server/tree/v19.7.5) (2021-10-05)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.4...v19.7.5)

## [v19.7.4](https://github.com/gisaia/ARLAS-server/tree/v19.7.4) (2021-10-04)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.3...v19.7.4)

## [v19.7.3](https://github.com/gisaia/ARLAS-server/tree/v19.7.3) (2021-10-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.1...v19.7.3)

## [v19.7.1](https://github.com/gisaia/ARLAS-server/tree/v19.7.1) (2021-09-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v19.7.0...v19.7.1)

**Fixed bugs:**

- Adding "distribution Management to java-client pom" command is not mac compatible  [\#730](https://github.com/gisaia/ARLAS-server/issues/730)
- Building typescript client of api doesn't work [\#729](https://github.com/gisaia/ARLAS-server/issues/729)
- Check if collection path is not null [\#728](https://github.com/gisaia/ARLAS-server/issues/728)

## [v19.7.0](https://github.com/gisaia/ARLAS-server/tree/v19.7.0) (2021-09-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v18.7.1...v19.7.0)

## [v18.7.1](https://github.com/gisaia/ARLAS-server/tree/v18.7.1) (2021-09-10)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v18.7.0...v18.7.1)

**Breaking changes:**

- Remove ARLAS\_OPENSEARCH\_URL\_TEMPLATE env variable [\#581](https://github.com/gisaia/ARLAS-server/issues/581) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- Remove ARLAS\_OGC\_SERVER\_URI env variable [\#580](https://github.com/gisaia/ARLAS-server/issues/580) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]

**New stuff:**

- Collections Cache should be updated regularly in order to follow changes on indices [\#696](https://github.com/gisaia/ARLAS-server/issues/696)
- Incorrect error message on badly formatted `ARLAS\_ELASTIC\_NODES` [\#594](https://github.com/gisaia/ARLAS-server/issues/594) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- H3 aggregation [\#713](https://github.com/gisaia/ARLAS-server/pull/713) ([alainbodiguel](https://github.com/alainbodiguel))
- H3 aggregation [\#711](https://github.com/gisaia/ARLAS-server/pull/711) ([alainbodiguel](https://github.com/alainbodiguel))

**Fixed bugs:**

- Applying a filter with Polygons that have multiple points on a node throw Exception [\#712](https://github.com/gisaia/ARLAS-server/issues/712)
- Public uri sends 403 if we provide an invalid token [\#710](https://github.com/gisaia/ARLAS-server/issues/710) [[security](https://github.com/gisaia/ARLAS-server/labels/security)]
- Creation of a collection with typo in geometry\_path properties key does not raise errors [\#681](https://github.com/gisaia/ARLAS-server/issues/681)
- OGC Spatial Filters \(within, BBOX, ...\) throws Invalid parameter exception for valid geomtry fields [\#582](https://github.com/gisaia/ARLAS-server/issues/582) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Problem with "size" param support in geohash aggregation [\#42](https://github.com/gisaia/ARLAS-server/issues/42)

**Miscellaneous:**

- SWAGGER Documentation is not up to date [\#684](https://github.com/gisaia/ARLAS-server/issues/684) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- \[Elasticsearch Java API\] cobined datehistogram interval is deprecated [\#545](https://github.com/gisaia/ARLAS-server/issues/545) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- testGwithinFilter tests fails when bumping Elasticsearch to 6.6.1  [\#411](https://github.com/gisaia/ARLAS-server/issues/411)
- CI : Add an \[ERROR\] tag in CI when Disclaimer is missing [\#306](https://github.com/gisaia/ARLAS-server/issues/306)

## [v18.7.0](https://github.com/gisaia/ARLAS-server/tree/v18.7.0) (2021-07-07)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v16.7.1...v18.7.0)

## [v16.7.1](https://github.com/gisaia/ARLAS-server/tree/v16.7.1) (2021-05-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v17.7.1...v16.7.1)

**Fixed bugs:**

- \_shapeaggregate return code 500 [\#708](https://github.com/gisaia/ARLAS-server/issues/708)

## [v17.7.1](https://github.com/gisaia/ARLAS-server/tree/v17.7.1) (2021-05-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.5.2...v17.7.1)

## [v12.7.5.2](https://github.com/gisaia/ARLAS-server/tree/v12.7.5.2) (2021-05-21)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v17.7.0...v12.7.5.2)

## [v17.7.0](https://github.com/gisaia/ARLAS-server/tree/v17.7.0) (2021-05-18)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v16.7.0...v17.7.0)

**New stuff:**

- \_\(geo\)aggregate :  Add geotile aggregation type [\#449](https://github.com/gisaia/ARLAS-server/issues/449) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- GeoTile Aggregation [\#694](https://github.com/gisaia/ARLAS-server/pull/694) ([alainbodiguel](https://github.com/alainbodiguel))

**Fixed bugs:**

- \[WFS\] wrong lat/long order in geometry point [\#703](https://github.com/gisaia/ARLAS-server/issues/703) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Fix check params for Geotile intervalValue [\#702](https://github.com/gisaia/ARLAS-server/pull/702) ([alainbodiguel](https://github.com/alainbodiguel))
- Supporting again GEOHASH and GEOHASH\_CENTER Aggregated geometries values [\#697](https://github.com/gisaia/ARLAS-server/pull/697) ([alainbodiguel](https://github.com/alainbodiguel))

## [v16.7.0](https://github.com/gisaia/ARLAS-server/tree/v16.7.0) (2021-03-18)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v15.7.0...v16.7.0)

## [v15.7.0](https://github.com/gisaia/ARLAS-server/tree/v15.7.0) (2021-02-18)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v15.7.0-beta.1...v15.7.0)

**New stuff:**

- Support shapefile export [\#680](https://github.com/gisaia/ARLAS-server/issues/680) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Export Shapefiles [\#686](https://github.com/gisaia/ARLAS-server/pull/686) ([alainbodiguel](https://github.com/alainbodiguel))

**Fixed bugs:**

- The results of fetch\_hits parameter in an aggregation are not flattened when 'flat=true' [\#692](https://github.com/gisaia/ARLAS-server/issues/692)
- Unable to create a collection with index template and without index created [\#674](https://github.com/gisaia/ARLAS-server/issues/674)

## [v15.7.0-beta.1](https://github.com/gisaia/ARLAS-server/tree/v15.7.0-beta.1) (2021-02-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.2...v15.7.0-beta.1)

## [v14.7.2](https://github.com/gisaia/ARLAS-server/tree/v14.7.2) (2021-02-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.1...v14.7.2)

## [v14.7.1](https://github.com/gisaia/ARLAS-server/tree/v14.7.1) (2021-01-15)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.1-beta.0...v14.7.1)

**Fixed bugs:**

- Fix sort on geo-fields in 'fetch\_hits' parameter [\#688](https://github.com/gisaia/ARLAS-server/issues/688)

## [v14.7.1-beta.0](https://github.com/gisaia/ARLAS-server/tree/v14.7.1-beta.0) (2020-12-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0...v14.7.1-beta.0)

**Fixed bugs:**

- Multiple header value fix [\#678](https://github.com/gisaia/ARLAS-server/pull/678) ([alainbodiguel](https://github.com/alainbodiguel))

**Miscellaneous:**

- Upgrade APM to 1.19.0 [\#682](https://github.com/gisaia/ARLAS-server/issues/682)

## [v14.7.0](https://github.com/gisaia/ARLAS-server/tree/v14.7.0) (2020-11-09)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-rc.3...v14.7.0)

**New stuff:**

- Add an information about whether the field is actually indexed in ES when describing the collection [\#670](https://github.com/gisaia/ARLAS-server/issues/670)
- Put mapping in cache [\#666](https://github.com/gisaia/ARLAS-server/issues/666)
- Arlas Auth: support multiple for header permission [\#664](https://github.com/gisaia/ARLAS-server/issues/664)
- allow a difference of 3 between '\_geoaggregate/{geohash}' precision and geohash aggregation precision [\#656](https://github.com/gisaia/ARLAS-server/issues/656)
- Add env variable to set ALLOWED\_ORIGINS\_PARAM [\#560](https://github.com/gisaia/ARLAS-server/issues/560) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- ColumnFilter update to support external modules [\#676](https://github.com/gisaia/ARLAS-server/pull/676) ([alainbodiguel](https://github.com/alainbodiguel))
- Allow a difference of 3 between '\_geoaggregate/{geohash}'  precision … [\#658](https://github.com/gisaia/ARLAS-server/pull/658) ([alainbodiguel](https://github.com/alainbodiguel))
- Bump to elasticsearch 7.9.2 [\#662](https://github.com/gisaia/ARLAS-server/pull/662) ([sfalquier](https://github.com/sfalquier))

**Fixed bugs:**

- Tiled geoaggregations counts data on the border of geohashes twice [\#673](https://github.com/gisaia/ARLAS-server/issues/673)
- Taggable fields are not returned correctly in \_describe [\#671](https://github.com/gisaia/ARLAS-server/issues/671)
- Fix template matching for a given collection index pattern [\#661](https://github.com/gisaia/ARLAS-server/pull/661) ([sfalquier](https://github.com/sfalquier))

**Miscellaneous:**

- Add documentation about security of ARLAS-server [\#672](https://github.com/gisaia/ARLAS-server/issues/672) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v14.7.0-rc.3](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-rc.3) (2020-10-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-rc.2...v14.7.0-rc.3)

## [v14.7.0-rc.2](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-rc.2) (2020-10-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-rc.1...v14.7.0-rc.2)

**Fixed bugs:**

- Invalidate the geo-fields types when the collection cache is invalidated [\#547](https://github.com/gisaia/ARLAS-server/issues/547)

## [v14.7.0-rc.1](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-rc.1) (2020-09-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.8...v14.7.0-rc.1)

## [v14.7.0-beta.8](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.8) (2020-09-15)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.7...v14.7.0-beta.8)

## [v14.7.0-beta.7](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.7) (2020-09-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.6...v14.7.0-beta.7)

## [v14.7.0-beta.6](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.6) (2020-09-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.5...v14.7.0-beta.6)

## [v14.7.0-beta.5](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.5) (2020-09-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.4...v14.7.0-beta.5)

## [v14.7.0-beta.4](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.4) (2020-09-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.3...v14.7.0-beta.4)

## [v14.7.0-beta.3](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.3) (2020-09-11)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-test-doc...v14.7.0-beta.3)

## [v14.7.0-test-doc](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-test-doc) (2020-09-09)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v14.7.0-beta.1...v14.7.0-test-doc)

## [v14.7.0-beta.1](https://github.com/gisaia/ARLAS-server/tree/v14.7.0-beta.1) (2020-08-25)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v13.7.2...v14.7.0-beta.1)

**Breaking changes:**

- Feature/add cors conf [\#649](https://github.com/gisaia/ARLAS-server/pull/649) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)] ([mbarbet](https://github.com/mbarbet))

**New stuff:**

- Fix jwtVerifier [\#645](https://github.com/gisaia/ARLAS-server/pull/645) ([mbarbet](https://github.com/mbarbet))
- No longer escaping replacement value in Auth header variables. [\#643](https://github.com/gisaia/ARLAS-server/pull/643) ([alainbodiguel](https://github.com/alainbodiguel))

**Fixed bugs:**

- CollectionServiceIT randomly fails at time of merge [\#642](https://github.com/gisaia/ARLAS-server/issues/642)
- CollectionServiceIT is faulty [\#637](https://github.com/gisaia/ARLAS-server/issues/637)
- Fix silent error in \_import [\#650](https://github.com/gisaia/ARLAS-server/pull/650) ([mbarbet](https://github.com/mbarbet))

## [v13.7.2](https://github.com/gisaia/ARLAS-server/tree/v13.7.2) (2020-07-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.5.1...v13.7.2)

**New stuff:**

- Collection filter [\#635](https://github.com/gisaia/ARLAS-server/issues/635)
- Add variable 'user' to authorisation rules [\#634](https://github.com/gisaia/ARLAS-server/issues/634)
- Authentication new headers [\#641](https://github.com/gisaia/ARLAS-server/pull/641) ([alainbodiguel](https://github.com/alainbodiguel))
- Collection filter [\#636](https://github.com/gisaia/ARLAS-server/pull/636) ([alainbodiguel](https://github.com/alainbodiguel))

## [v12.7.5.1](https://github.com/gisaia/ARLAS-server/tree/v12.7.5.1) (2020-06-30)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v13.7.1...v12.7.5.1)

## [v13.7.1](https://github.com/gisaia/ARLAS-server/tree/v13.7.1) (2020-06-02)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v13.7.0...v13.7.1)

## [v13.7.0](https://github.com/gisaia/ARLAS-server/tree/v13.7.0) (2020-05-28)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.8...v13.7.0)

**Breaking changes:**

- Remove \_range endpoint [\#464](https://github.com/gisaia/ARLAS-server/issues/464) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

**New stuff:**

- \[Authentication\] Public URIs as regex [\#628](https://github.com/gisaia/ARLAS-server/issues/628)
- Extend applying geobbox and geocentroid metrics to any geo-point field [\#606](https://github.com/gisaia/ARLAS-server/issues/606)
- Add parameter in aggregate endpoint that allows to return multiple geometries [\#604](https://github.com/gisaia/ARLAS-server/issues/604) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

## [v12.7.8](https://github.com/gisaia/ARLAS-server/tree/v12.7.8) (2020-04-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.7...v12.7.8)

## [v12.7.7](https://github.com/gisaia/ARLAS-server/tree/v12.7.7) (2020-03-26)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.6...v12.7.7)

**New stuff:**

- Generate a Java client api and push it in maven repo [\#486](https://github.com/gisaia/ARLAS-server/issues/486)

## [v12.7.6](https://github.com/gisaia/ARLAS-server/tree/v12.7.6) (2020-03-24)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.5...v12.7.6)

**New stuff:**

- Using New Rest High Level Client for ES [\#612](https://github.com/gisaia/ARLAS-server/pull/612) ([alainbodiguel](https://github.com/alainbodiguel))

## [v12.7.5](https://github.com/gisaia/ARLAS-server/tree/v12.7.5) (2020-03-24)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.4...v12.7.5)

**Fixed bugs:**

- NullPointer exception thrown when writing xml after a GetFeature or GetFeatureById request [\#616](https://github.com/gisaia/ARLAS-server/issues/616) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- WFS GetFeature and GetFeatureById can return an invalid XML without throwing an error [\#615](https://github.com/gisaia/ARLAS-server/issues/615) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]

## [v12.7.4](https://github.com/gisaia/ARLAS-server/tree/v12.7.4) (2020-02-06)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.6.5...v12.7.4)

**New stuff:**

- \[Tiled Geoaggregate\] Set the geohash tile on which the geoaggregation is excetuded in the response [\#599](https://github.com/gisaia/ARLAS-server/issues/599)
- \[Tiled Geoaggregate\] return all buckets resulting from a tiled geoaggregate query [\#598](https://github.com/gisaia/ARLAS-server/issues/598)
- Apply FGA \(column filtering\) to v12 [\#597](https://github.com/gisaia/ARLAS-server/issues/597)
- Make FGA header compatible with multiple collections [\#586](https://github.com/gisaia/ARLAS-server/issues/586)
- Fine grain access \(column filtering\) [\#558](https://github.com/gisaia/ARLAS-server/issues/558)

**Fixed bugs:**

- OGC Spatial Filters : Handle invalid geometries exception [\#583](https://github.com/gisaia/ARLAS-server/issues/583) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- OGC filter with unparsable XML throws NPE [\#573](https://github.com/gisaia/ARLAS-server/issues/573) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]

## [v11.6.5](https://github.com/gisaia/ARLAS-server/tree/v11.6.5) (2020-01-21)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.3...v11.6.5)

**Fixed bugs:**

- responseContainer of \_list endpoint is invalid [\#585](https://github.com/gisaia/ARLAS-server/issues/585) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Hide column-filter header un SearchRESTService [\#578](https://github.com/gisaia/ARLAS-server/issues/578)

## [v12.7.3](https://github.com/gisaia/ARLAS-server/tree/v12.7.3) (2020-01-02)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.6.4...v12.7.3)

**New stuff:**

-  `\_compute` the bounding or the geocentroid [\#561](https://github.com/gisaia/ARLAS-server/issues/561)

**Miscellaneous:**

- Too many logs on CI [\#577](https://github.com/gisaia/ARLAS-server/issues/577)

## [v11.6.4](https://github.com/gisaia/ARLAS-server/tree/v11.6.4) (2019-12-23)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.2...v11.6.4)

**Fixed bugs:**

- Describe does not return properly the list of taggable fields [\#568](https://github.com/gisaia/ARLAS-server/issues/568)
- Describe does not return properly the list of taggable fields [\#569](https://github.com/gisaia/ARLAS-server/pull/569) ([alainbodiguel](https://github.com/alainbodiguel))

## [v12.7.2](https://github.com/gisaia/ARLAS-server/tree/v12.7.2) (2019-11-25)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.1...v12.7.2)

**New stuff:**

- Support array of geohashes for geo-point fields [\#556](https://github.com/gisaia/ARLAS-server/issues/556)

## [v12.7.1](https://github.com/gisaia/ARLAS-server/tree/v12.7.1) (2019-11-21)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v12.7.0...v12.7.1)

**Fixed bugs:**

- \[GEOAGGREGATE\] apply fetch\_geometry strategy on the geo-point on which we aggregate [\#552](https://github.com/gisaia/ARLAS-server/issues/552)

## [v12.7.0](https://github.com/gisaia/ARLAS-server/tree/v12.7.0) (2019-11-15)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.7.2...v12.7.0)

**Breaking changes:**

- Remove tag api [\#535](https://github.com/gisaia/ARLAS-server/issues/535) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- \[Multi-geometry\] Support multi\_geometries in geometric filters [\#465](https://github.com/gisaia/ARLAS-server/issues/465) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

**New stuff:**

- Add HTTPS in the swagger definition [\#513](https://github.com/gisaia/ARLAS-server/issues/513)
- \[auth-4\] Variable support in rules and headers [\#497](https://github.com/gisaia/ARLAS-server/issues/497) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- \[auth-3\] Authorization mode: headers processing [\#496](https://github.com/gisaia/ARLAS-server/issues/496) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- \[auth-2\] Authorization mode: rules processing [\#495](https://github.com/gisaia/ARLAS-server/issues/495) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- \[auth-1\] Authentication mode support [\#494](https://github.com/gisaia/ARLAS-server/issues/494) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- \[Multi-geometry\] return one or multiple geometries in \_geosearch responses [\#466](https://github.com/gisaia/ARLAS-server/issues/466) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add \_compute endpoint [\#463](https://github.com/gisaia/ARLAS-server/issues/463) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

**Fixed bugs:**

- TimestampTypeMapper unit test fails [\#548](https://github.com/gisaia/ARLAS-server/issues/548)
- ARLAS Rest services: header keys must be case insensitive [\#472](https://github.com/gisaia/ARLAS-server/issues/472) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

**Miscellaneous:**

- Deprecate \_range endpoint [\#550](https://github.com/gisaia/ARLAS-server/issues/550) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

## [v11.7.2](https://github.com/gisaia/ARLAS-server/tree/v11.7.2) (2019-11-08)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.7.1...v11.7.2)

**Fixed bugs:**

- Fix Datehistogram key bug due to bump to ES7 [\#541](https://github.com/gisaia/ARLAS-server/pull/541) ([MohamedHamouGisaia](https://github.com/MohamedHamouGisaia))

**Miscellaneous:**

- \[Server\] Generate API documentation [\#542](https://github.com/gisaia/ARLAS-server/issues/542) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v11.7.1](https://github.com/gisaia/ARLAS-server/tree/v11.7.1) (2019-10-29)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.6.3...v11.7.1)

**New stuff:**

- Add support for Elastic Cloud [\#530](https://github.com/gisaia/ARLAS-server/issues/530)

**Fixed bugs:**

- Remove redundant execution of range query [\#533](https://github.com/gisaia/ARLAS-server/pull/533) ([MohamedHamouGisaia](https://github.com/MohamedHamouGisaia))

## [v11.6.3](https://github.com/gisaia/ARLAS-server/tree/v11.6.3) (2019-10-22)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.7.0...v11.6.3)

**New stuff:**

- Add support for Elastic Cloud [\#537](https://github.com/gisaia/ARLAS-server/issues/537)

**Fixed bugs:**

- Make sure to remove before in next and after in previous  [\#529](https://github.com/gisaia/ARLAS-server/issues/529)
- Remove redundant execution of range query [\#539](https://github.com/gisaia/ARLAS-server/issues/539)
- Make sure to remove before in next and after in previous [\#538](https://github.com/gisaia/ARLAS-server/issues/538)

## [v11.7.0](https://github.com/gisaia/ARLAS-server/tree/v11.7.0) (2019-09-19)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.6.2...v11.7.0)

**New stuff:**

- Make `type\_name` optional in CollectionReference  [\#521](https://github.com/gisaia/ARLAS-server/issues/521)

**Miscellaneous:**

- Add a warning in logs saying that `type\_name` is deprecated [\#522](https://github.com/gisaia/ARLAS-server/issues/522)
- Fix docker-compose to fit README.md [\#493](https://github.com/gisaia/ARLAS-server/issues/493) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Bump Elasticsearch to 7.x.x [\#468](https://github.com/gisaia/ARLAS-server/issues/468)

## [v11.6.2](https://github.com/gisaia/ARLAS-server/tree/v11.6.2) (2019-09-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.6.1...v11.6.2)

**New stuff:**

- Publish arlas-admin module in cloudsmith [\#511](https://github.com/gisaia/ARLAS-server/issues/511)
- Pwithin request should support WKT geometry [\#500](https://github.com/gisaia/ARLAS-server/issues/500)
- Add HTTPS in the swagger definition [\#515](https://github.com/gisaia/ARLAS-server/pull/515) ([alainbodiguel](https://github.com/alainbodiguel))

**Fixed bugs:**

- gwithin : WKT or BBOX orientation is only considered when the smallest polygon crosses the antimeridian [\#514](https://github.com/gisaia/ARLAS-server/issues/514)
- WKT : validate each element of a MULTIGEOMETRY separately  [\#512](https://github.com/gisaia/ARLAS-server/issues/512)

**Miscellaneous:**

- Partition-filter docs contains typo [\#473](https://github.com/gisaia/ARLAS-server/issues/473) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v11.6.1](https://github.com/gisaia/ARLAS-server/tree/v11.6.1) (2019-09-06)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v11.6.0...v11.6.1)

**Breaking changes:**

- Change auth configuration to snake case [\#507](https://github.com/gisaia/ARLAS-server/pull/507) ([alainbodiguel](https://github.com/alainbodiguel))

**New stuff:**

- \[Auth-6\] Add more tests to auth module [\#508](https://github.com/gisaia/ARLAS-server/issues/508)
- \[auth-5\] Change configuration to snake case [\#498](https://github.com/gisaia/ARLAS-server/issues/498) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Support geometries that are indexed as WKT [\#510](https://github.com/gisaia/ARLAS-server/issues/510)
- Push arlas jars in maven repos [\#485](https://github.com/gisaia/ARLAS-server/issues/485)
- Add more tests to auth module  [\#509](https://github.com/gisaia/ARLAS-server/pull/509) ([alainbodiguel](https://github.com/alainbodiguel))
- Variable support in rules and headers  [\#506](https://github.com/gisaia/ARLAS-server/pull/506) ([alainbodiguel](https://github.com/alainbodiguel))
- Authorization mode: headers processing [\#505](https://github.com/gisaia/ARLAS-server/pull/505) ([alainbodiguel](https://github.com/alainbodiguel))
- Authorization mode: rules processing [\#504](https://github.com/gisaia/ARLAS-server/pull/504) ([alainbodiguel](https://github.com/alainbodiguel))

**Fixed bugs:**

- Geometries that are indexed with WKT format are systematically considered as geo\_points [\#482](https://github.com/gisaia/ARLAS-server/issues/482)

## [v11.6.0](https://github.com/gisaia/ARLAS-server/tree/v11.6.0) (2019-07-23)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v10.6.2...v11.6.0)

**New stuff:**

- Add 'previous' link in search results [\#488](https://github.com/gisaia/ARLAS-server/issues/488)
- Add 'before' parameter in \_search/\_geosearch [\#477](https://github.com/gisaia/ARLAS-server/issues/477) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Feature/fix \#477 [\#479](https://github.com/gisaia/ARLAS-server/pull/479) [[API](https://github.com/gisaia/ARLAS-server/labels/API)] ([mbarbet](https://github.com/mbarbet))

**Miscellaneous:**

- Deprecate /write API [\#491](https://github.com/gisaia/ARLAS-server/issues/491) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Bump Elasticsearch to 6.8.0 [\#467](https://github.com/gisaia/ARLAS-server/issues/467)

## [v10.6.2](https://github.com/gisaia/ARLAS-server/tree/v10.6.2) (2019-07-19)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v10.6.1...v10.6.2)

**New stuff:**

- \_search : Allow geodistance sort along with 'after' parameter  [\#481](https://github.com/gisaia/ARLAS-server/issues/481)

**Fixed bugs:**

- \_search : Error 500 caused by applying a geosort along with a sort on id when returning \_next link in search response  [\#475](https://github.com/gisaia/ARLAS-server/issues/475)
- \[tiled geosearch\]\[tiled geoaggreagte\] pwithin filter breaks due to shared borders with tiles [\#456](https://github.com/gisaia/ARLAS-server/issues/456)
- Taggable fields can contain duplicate tags [\#266](https://github.com/gisaia/ARLAS-server/issues/266)

## [v10.6.1](https://github.com/gisaia/ARLAS-server/tree/v10.6.1) (2019-05-17)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v10.6.0...v10.6.1)

**New stuff:**

- Add a new configuration variable to configure the base uri of the server [\#451](https://github.com/gisaia/ARLAS-server/issues/451)
- \_search : Add link.next when search request is sorted by collectionReference.idPath [\#436](https://github.com/gisaia/ARLAS-server/issues/436)

**Fixed bugs:**

- DOC: Fix link to inspire configuration in arlas-inspire.md [\#442](https://github.com/gisaia/ARLAS-server/issues/442) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- \_search : handle the case of descending 'sort' with 'after'  [\#440](https://github.com/gisaia/ARLAS-server/issues/440)
- \_search: search after a date not formatted as timestamp throws a parse exception [\#439](https://github.com/gisaia/ARLAS-server/issues/439)
- \_search : configure the address of ARLAS-server in link "next&self" [\#437](https://github.com/gisaia/ARLAS-server/issues/437)
- \_search : link to the last page throws ArrayIndexOutOfBoundsException [\#435](https://github.com/gisaia/ARLAS-server/issues/435)

**Miscellaneous:**

- Deprecate \[arlas-ogc.serverUrl\] configuration variable [\#454](https://github.com/gisaia/ARLAS-server/issues/454) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- Deprecate \[opensearch.url-template-prefix\] configuration variable [\#453](https://github.com/gisaia/ARLAS-server/issues/453) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]
- \_tag: In logstash \(/elasticsearch?\), taggable field path should not contain `tags`  [\#448](https://github.com/gisaia/ARLAS-server/issues/448) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- \_tag : Taggable fields have to be initialized with null value at indexation [\#447](https://github.com/gisaia/ARLAS-server/issues/447) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

## [v10.6.0](https://github.com/gisaia/ARLAS-server/tree/v10.6.0) (2019-04-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v9.6.2...v10.6.0)

**Breaking changes:**

- make Explore API request & response models definitions in snake\_case [\#432](https://github.com/gisaia/ARLAS-server/issues/432) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Refactor `Size` and `Sort` classes: both are combined into `Page` class [\#405](https://github.com/gisaia/ARLAS-server/issues/405) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Remove ARLAS\_ELASTIC\_HOST and ARLAS\_ELASTIC\_PORT from configuration. [\#254](https://github.com/gisaia/ARLAS-server/issues/254) [[conf](https://github.com/gisaia/ARLAS-server/labels/conf)]

**New stuff:**

- Aggregations : Extend fetch\_geometry option to all types of aggregations [\#417](https://github.com/gisaia/ARLAS-server/issues/417) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Check validity of WKT passed to geographic filters \(gintersect, gwithin, notgintersect, notgwithin\) [\#407](https://github.com/gisaia/ARLAS-server/issues/407)
- Add \_link object to specify next page for search end point [\#403](https://github.com/gisaia/ARLAS-server/issues/403) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- extend the `flat` option to Aggregate REST service [\#397](https://github.com/gisaia/ARLAS-server/issues/397) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add a the possibility to fetch an attribute \(first, last, default, ...\) in aggregation responses [\#391](https://github.com/gisaia/ARLAS-server/issues/391) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add `dateformat` option in \[range, lt, gt, lte, gte\] queries for date fields [\#389](https://github.com/gisaia/ARLAS-server/issues/389) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- extend the `flat` option to Search and Raw REST services [\#388](https://github.com/gisaia/ARLAS-server/issues/388) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add `md` in `\_geosearch` response [\#387](https://github.com/gisaia/ARLAS-server/issues/387)
- Implement search\_after query parameter in search request. [\#289](https://github.com/gisaia/ARLAS-server/issues/289) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add search after param [\#402](https://github.com/gisaia/ARLAS-server/pull/402) [[API](https://github.com/gisaia/ARLAS-server/labels/API)] ([mbarbet](https://github.com/mbarbet))

**Fixed bugs:**

- Python client of ARLAS-api returns incorrect geometries [\#429](https://github.com/gisaia/ARLAS-server/issues/429) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Release script fails to clean root-owned previously generated files [\#421](https://github.com/gisaia/ARLAS-server/issues/421)
- Release process breaks on root-generated file [\#412](https://github.com/gisaia/ARLAS-server/issues/412)
- Fix false-positive geohashes returned in `\_geoaggregate/{geohash}` service [\#395](https://github.com/gisaia/ARLAS-server/issues/395)
- Aggregation and Search responses are snake\_case while it's camelCase in typescript generated api [\#381](https://github.com/gisaia/ARLAS-server/issues/381) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Fix Atom search tests when content type is application/atom+xml [\#394](https://github.com/gisaia/ARLAS-server/pull/394) ([MohamedHamouGisaia](https://github.com/MohamedHamouGisaia))

**Miscellaneous:**

- Fix warnings logs arlas server startup [\#419](https://github.com/gisaia/ARLAS-server/issues/419)
- arlas-api-tutorial : Aggregation examples are not updated to the latest versions of arlas-api [\#385](https://github.com/gisaia/ARLAS-server/issues/385) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Update arlas-api-tutorial : Elasticsearch 6.x no longer supports enabling \[\_all\] for mapping  [\#384](https://github.com/gisaia/ARLAS-server/issues/384) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Bump Elasticsearch dependency to 6.5 [\#383](https://github.com/gisaia/ARLAS-server/issues/383)
- Use configurable character for replacing the dot within the field path [\#305](https://github.com/gisaia/ARLAS-server/issues/305)

## [v9.6.2](https://github.com/gisaia/ARLAS-server/tree/v9.6.2) (2019-03-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v8.6.2...v9.6.2)

## [v8.6.2](https://github.com/gisaia/ARLAS-server/tree/v8.6.2) (2019-03-01)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v9.6.1...v8.6.2)

**New stuff:**

- Add a new service \_countDistinct to the explore api [\#390](https://github.com/gisaia/ARLAS-server/issues/390) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

## [v9.6.1](https://github.com/gisaia/ARLAS-server/tree/v9.6.1) (2018-12-14)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v9.6.0...v9.6.1)

**Fixed bugs:**

- Exclude geometry in Geosearch : handle geometry paths that don't contain '.' [\#377](https://github.com/gisaia/ARLAS-server/issues/377)
- Initialize InspireConfiguration when it is not initialized in yaml conf [\#376](https://github.com/gisaia/ARLAS-server/issues/376)

**Miscellaneous:**

- INFO Logs at startup as well as Admin REST API displays the list of ES nodes [\#378](https://github.com/gisaia/ARLAS-server/issues/378)
- Remove `Strings.isNullOrEmpty` from logic tier [\#370](https://github.com/gisaia/ARLAS-server/issues/370)

## [v9.6.0](https://github.com/gisaia/ARLAS-server/tree/v9.6.0) (2018-11-27)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v8.6.1...v9.6.0)

**New stuff:**

- Support multi-polygon for WFS [\#364](https://github.com/gisaia/ARLAS-server/issues/364) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Collection : Add the possibility to define a filter in collection declaration [\#363](https://github.com/gisaia/ARLAS-server/issues/363) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Make WFS and CSW Inspire compliant [\#356](https://github.com/gisaia/ARLAS-server/issues/356) [[API](https://github.com/gisaia/ARLAS-server/labels/API)] [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
- Add a filter in Collection param [\#366](https://github.com/gisaia/ARLAS-server/pull/366) [[API](https://github.com/gisaia/ARLAS-server/labels/API)] ([mbarbet](https://github.com/mbarbet))
- Support multipolygon, multipoint, multilinestring in WFS result [\#365](https://github.com/gisaia/ARLAS-server/pull/365) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)] ([mbarbet](https://github.com/mbarbet))

**Miscellaneous:**

- Document the CSW and WFS endpoints [\#295](https://github.com/gisaia/ARLAS-server/issues/295) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Support TimeFilter on WFS [\#192](https://github.com/gisaia/ARLAS-server/issues/192) [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]

## [v8.6.1](https://github.com/gisaia/ARLAS-server/tree/v8.6.1) (2018-11-04)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v8.6.0...v8.6.1)

**Fixed bugs:**

- Geosearch : Check if geometry exists before removing it from properties of geojson [\#361](https://github.com/gisaia/ARLAS-server/issues/361)
- filter Q doesn't support field values containing ":" [\#359](https://github.com/gisaia/ARLAS-server/issues/359)

## [v8.6.0](https://github.com/gisaia/ARLAS-server/tree/v8.6.0) (2018-10-16)

[Full Changelog](https://github.com/gisaia/ARLAS-server/compare/v7.6.4...v8.6.0)

**Breaking changes:**

- Modify \_geoaggregates' geometry fetching strategy [\#346](https://github.com/gisaia/ARLAS-server/issues/346) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]

**New stuff:**

- Crop 256x256 on raster tiles [\#343](https://github.com/gisaia/ARLAS-server/pull/343) ([sylvaingaudan](https://github.com/sylvaingaudan))
- Remove duplicate geo in geojson [\#333](https://github.com/gisaia/ARLAS-server/pull/333) ([mbarbet](https://github.com/mbarbet))

**Fixed bugs:**

- GeoSearch : remove the duplicated geometry in the geojson properties [\#103](https://github.com/gisaia/ARLAS-server/issues/103)
- Geo query not work with bbox on  Bering Detroit [\#347](https://github.com/gisaia/ARLAS-server/issues/347)
- Some of Aggregation model attributes are not checked for validity when using POST for \_aggregate [\#341](https://github.com/gisaia/ARLAS-server/issues/341)
- Fix shape orientation Bbox [\#348](https://github.com/gisaia/ARLAS-server/pull/348) ([mbarbet](https://github.com/mbarbet))

**Miscellaneous:**

- API Documentation points on github instead of gitlab. [\#353](https://github.com/gisaia/ARLAS-server/issues/353) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]
- Typo error in collection model doc [\#345](https://github.com/gisaia/ARLAS-server/issues/345) [[documentation](https://github.com/gisaia/ARLAS-server/labels/documentation)]

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

**New stuff:**

- Add cardinality aggregation to ARLAS server [\#148](https://github.com/gisaia/ARLAS-server/issues/148) [[API](https://github.com/gisaia/ARLAS-server/labels/API)]
- Add Dublin Core element name to a collection [\#251](https://github.com/gisaia/ARLAS-server/issues/251)
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
- Implement header partition filter for WFS [\#189](https://github.com/gisaia/ARLAS-server/issues/189) [[security](https://github.com/gisaia/ARLAS-server/labels/security)] [[OGC](https://github.com/gisaia/ARLAS-server/labels/OGC)]
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