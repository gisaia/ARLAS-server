/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.core.impl.elastic.services;

import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import io.arlas.commons.exceptions.*;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.*;
import io.arlas.server.core.model.request.Aggregation;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.FieldType;
import io.arlas.server.core.model.response.TimestampType;
import io.arlas.server.core.services.FluidSearchService;
import io.arlas.server.core.utils.CheckParams;
import io.arlas.server.core.utils.CollectionUtil;
import io.arlas.server.core.utils.GeoUtil;
import io.arlas.server.core.utils.ParamsParser;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.arlas.server.core.utils.CheckParams.GEO_AGGREGATION_TYPE_ENUMS;

public class ElasticFluidSearch extends FluidSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFluidSearch.class);

    private ElasticClient client;
    private SearchRequest.Builder requestBuilder;
    private BoolQuery.Builder boolQueryBuilder;


    public ElasticFluidSearch(CollectionReference collectionReference) {
        super(collectionReference);
        requestBuilder = new SearchRequest.Builder()
                .index(collectionReference.params.indexName)
                .trackTotalHits(b -> b.enabled(true));
        boolQueryBuilder = new BoolQuery.Builder();
    }

    public ElasticClient getClient() {
        return client;
    }

    public ElasticFluidSearch setClient(ElasticClient client) {
        this.client = client;
        return this;
    }

    public BoolQuery.Builder getBoolQueryBuilder() {
        return boolQueryBuilder;
    }

    public SearchResponse<Map> exec() throws ArlasException {
        Pair<String[], String[]> includeExclude = computeIncludeExclude(false);

        SearchRequest request = requestBuilder
                .source(s -> s
                        .filter(f -> f
                                .includes(Arrays.asList(includeExclude.getLeft()))
                                .excludes(Arrays.asList(includeExclude.getRight()))
                        )
                )
                .query(boolQueryBuilder.build()._toQuery())
                .build();

        // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/reading.html#_reading_raw_json
        return client.search(request);
    }

    @Override
    public FluidSearchService filter(MultiValueFilter<Expression> f, String dateFormat) throws ArlasException {
        List<Query> queries = new ArrayList<>();
        for (Expression fFilter : f) {
            queries.add(filter(fFilter, dateFormat));
        }
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.bool().should(queries).minimumShouldMatch("1").build()._toQuery());
        return this;
    }

    private Query filter(Expression expression, String dateFormat) throws ArlasException {
        BoolQuery.Builder ret = new BoolQuery.Builder();
        if (StringUtil.isNullOrEmpty(expression.field) || expression.op == null || StringUtil.isNullOrEmpty(expression.value)) {
            throw new InvalidParameterException(INVALID_PARAMETER_F);
        }
        final String field = expression.field;
        OperatorEnum op = expression.op;
        String value = expression.value;
        String[] fieldValues = value.split(",");
        switch (op) {
            case eq:
                if (fieldValues.length > 1) {
                    ret = ret.filter(QueryBuilders.bool().should(
                                    Arrays.stream(fieldValues)
                                            .map(valueInValues -> QueryBuilders.match()
                                                    .field(field)
                                                    .query(valueInValues)
                                                    .build()
                                                    ._toQuery())
                                            .collect(Collectors.toList())
                                    ).build()._toQuery()
                    );
                } else {
                    ret = ret.filter(QueryBuilders.match().field(field).query(value).build()._toQuery());
                }
                break;
            case gte:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQuery.Builder gteRangeQuery = QueryBuilders.range().field(field).gte(JsonData.of(value));
                applyFormatOnRangeQuery(field, value, gteRangeQuery);
                ret = ret.filter(gteRangeQuery.build()._toQuery());
                break;
            case gt:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQuery.Builder gtRangeQuery = QueryBuilders.range().field(field).gt(JsonData.of(value));
                applyFormatOnRangeQuery(field, value, gtRangeQuery);
                ret = ret.filter(gtRangeQuery.build()._toQuery());
                break;
            case lte:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQuery.Builder lteRangeQuery = QueryBuilders.range().field(field).lte(JsonData.of(value));
                applyFormatOnRangeQuery(field, value, lteRangeQuery);
                ret = ret.filter(lteRangeQuery.build()._toQuery());
                break;
            case lt:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQuery.Builder ltRangeQuery = QueryBuilders.range().field(field).lt(JsonData.of(value));
                applyFormatOnRangeQuery(field, value, ltRangeQuery);
                ret = ret.filter(ltRangeQuery.build()._toQuery());
                break;
            case like:
                // TODO: if field type is fullText, use matchPhraseQuery instead of regexQuery
                ret = ret.filter(QueryBuilders.regexp().field(field).value(".*" + value + ".*").build()._toQuery());
                break;
            case ne:
                for (String valueInValues : fieldValues) {
                    ret = ret.mustNot(QueryBuilders.match().field(field).query(valueInValues).build()._toQuery());
                }
                break;
            case range:
                final String newField = ParamsParser.getFieldFromFieldAliases(field, collectionReference);
                if (fieldValues.length > 1) {
                    BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder();
                    for (String valueInValues : fieldValues) {
                        CheckParams.checkRangeValidity(valueInValues);
                        orBoolQueryBuilder = orBoolQueryBuilder.should(getRangeQueryBuilder(newField, valueInValues, dateFormat).build()._toQuery());
                    }
                    ret = ret.filter(orBoolQueryBuilder.build()._toQuery());
                } else {
                    CheckParams.checkRangeValidity(value);
                    ret = ret.filter(getRangeQueryBuilder(newField, value, dateFormat).build()._toQuery());
                }
                break;
            case within:
                FieldType wType = collectionReferenceManager.getType(collectionReference, field, true);
                BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder().minimumShouldMatch("1");
                switch (wType) {
                    case GEO_POINT:
                        for (Query q : filterPWithin(field, value)) {
                            orBoolQueryBuilder = orBoolQueryBuilder.should(q);
                        }
                        break;
                    case GEO_SHAPE:
                        orBoolQueryBuilder = orBoolQueryBuilder.should(filterGWithin(field, value));
                        break;
                    default:
                        throw new ArlasException("'within' op on field '" + field + "' of type '" + wType + "' is not supported");
                }
                ret = ret.filter(orBoolQueryBuilder.build()._toQuery());
                break;
            case notwithin:
                FieldType type = collectionReferenceManager.getType(collectionReference, field, true);
                BoolQuery.Builder orBoolQueryBuilder2 = new BoolQuery.Builder();
                switch (type) {
                    case GEO_POINT:
                        for (Query q : filterNotPWithin(field, value)) {
                            orBoolQueryBuilder2 = orBoolQueryBuilder2.should(q);
                        }
                        break;
                    case GEO_SHAPE:
                        orBoolQueryBuilder2 = orBoolQueryBuilder2.should(filterGWithin(field, value));
                        break;
                    default:
                        throw new ArlasException("'notwithin' op on field '" + field + "' of type '" + type + "' is not supported");
                }
                ret = ret.mustNot(orBoolQueryBuilder2.build()._toQuery());
                break;
            case intersects:
                ret = ret.filter(filterGIntersect(field, value));
                break;
            case notintersects:
                ret = ret.mustNot(filterGIntersect(field, value));
                break;
            default:
                throw new InvalidParameterException(INVALID_OPERATOR);
        }
        return ret.build()._toQuery();
    }


    public void applyFormatOnRangeQuery(String field, String value, RangeQuery.Builder rangeQuery) throws ArlasException {
        if (field.equals(collectionReference.params.timestampPath)) {
            CheckParams.checkTimestampFormatValidity(value);
            rangeQuery.format(TimestampType.epoch_millis.name());
        }
    }

    protected RangeQuery.Builder getRangeQueryBuilder(String field, String value, String dateFormat) throws ArlasException {
        boolean incMin = value.startsWith("[");
        boolean incMax = value.endsWith("]");
        String min = value.substring(1, value.lastIndexOf("<"));
        String max = value.substring(value.lastIndexOf("<") + 1, value.length() - 1);

        if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
            min = ParamsParser.parseDate(min, dateFormat);
            max = ParamsParser.parseDate(max, dateFormat);
        }

        if (field.equals(collectionReference.params.timestampPath)) {
            CheckParams.checkTimestampFormatValidity(min);
            CheckParams.checkTimestampFormatValidity(max);
        }
        RangeQuery.Builder ret = QueryBuilders.range().field(field);
        if (incMin) {
            ret.gte(JsonData.of(min));
        } else {
            ret.gt(JsonData.of(min));
        }
        if (incMax) {
            ret.lte(JsonData.of(max));
        } else {
            ret.lt(JsonData.of(max));
        }
        if (field.equals(collectionReference.params.timestampPath)) {
            ret.format(TimestampType.epoch_millis.name());
        }
        return ret;
    }

    @Override
    public FluidSearchService filterQ(MultiValueFilter<String> q) throws ArlasException {
        BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder().minimumShouldMatch("1");
        for (String qFilter : q) {
            String[] operands = qFilter.split(":",2);
            if (operands.length == 2) {
                orBoolQueryBuilder = orBoolQueryBuilder
                        .should(QueryBuilders.simpleQueryString().query(operands[1]).defaultOperator(Operator.And).fields(operands[0]).build()._toQuery());
            } else if (operands.length == 1) {
                orBoolQueryBuilder = orBoolQueryBuilder
                        .should(QueryBuilders.simpleQueryString().query(operands[0]).defaultOperator(Operator.And).build()._toQuery());
            } else {
                throw new InvalidParameterException(INVALID_Q_FILTER);
            }
        }
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder.build()._toQuery());
        return this;
    }

    public List<Query> filterPWithin(String field, String pwithinFilter) throws ArlasException {
        List<Query> builderList = new ArrayList<>();
        if (CheckParams.isBboxMatch(pwithinFilter)) {
            double[] tlbr = CheckParams.toDoubles(pwithinFilter);
            builderList.add(filterPWithin(field, tlbr[0], tlbr[1], tlbr[2], tlbr[3]));
        } else {
            Geometry p = GeoUtil.readWKT(pwithinFilter);
            String geometryType = p.getGeometryType();
            if (geometryType.equals("Polygon") || geometryType.equals("MultiPolygon")) {
                // If the polygon is not a rectangle, ES provides `geoPolygonQuery` that allows to search geo-points that are within a polygon formed by list of points
                // ==> we can't pass polygons with holes nor multipolygons (for multipolygons we can split them)
                // !!! ISSUE ES 6.X: points on the edge of a polygon are not considered as within. Fixed in 7.X
                for (int i = 0; i< p.getNumGeometries(); i++) {
                    List<GeoLocation> geoPoints = Arrays.stream(p.getGeometryN(i).getCoordinates())
                            .map(c -> GeoLocation.of(b1 -> b1.latlon(b2 -> b2.lat(c.y).lon(c.x))))
                            .collect(Collectors.toList());
                    builderList.add(QueryBuilders.geoPolygon()
                            .field(field)
                            .polygon(b -> b.points(geoPoints))
                            .build()._toQuery()
                    );
                }
            } else {
                throw new NotImplementedException("WKT is not supported for 'within' op on field '" + field + "' of type '" + geometryType + "'");
            }
        }
        return builderList;
    }

    private Query filterPWithin(String field, double west, double south, double east, double north) {
        return QueryBuilders.geoBoundingBox()
                .field(field)
                .boundingBox(b1 -> b1
                        .tlbr(b2 -> b2
                                .topLeft(b3 -> b3.coords(Arrays.asList(north, west)))
                                .bottomRight(b3 -> b3.coords(Arrays.asList(south, east)))
                        )
                ).build()._toQuery();
    }

    public List<Query> filterNotPWithin(String field, String notpwithinFilter) throws ArlasException {
        List<Query> builderList = new ArrayList<>();
        if (CheckParams.isBboxMatch(notpwithinFilter)) {
            double[] tlbr = CheckParams.toDoubles(notpwithinFilter);
            builderList.add(filterPWithin(field, tlbr[0], tlbr[1], tlbr[2], tlbr[3]));
        } else {
            Geometry p = GeoUtil.readWKT(notpwithinFilter);
            String geometryType = p.getGeometryType();
            if (geometryType.equals("Polygon") || geometryType.equals("MultiPolygon")) {
                // If the polygon is not a rectangle, ES provides `geoPolygonQuery` that allows to search geo-points that are within a polygon formed by list of points
                // ==> we can't pass polygons with holes nor multipolygons (for multipolygons we can split them)
                // !!! ISSUE ES 6.X: points on the edge of a polygon are not considered as within. Fixed in 7.X
                BoolQuery.Builder andQueryBuilder = new BoolQuery.Builder();
                for (int i = 0; i< p.getNumGeometries(); i++) {
                    List<GeoLocation> geoPoints = Arrays.stream(p.getGeometryN(i).getCoordinates())
                            .map(c -> GeoLocation.of(b1 -> b1.latlon(b2 -> b2.lat(c.y).lon(c.x))))
                            .collect(Collectors.toList());
                    // `andQueryBuilder` will allow us to consider a multipolygon as one entity when we apply notpwithin query
                    andQueryBuilder = andQueryBuilder.should(QueryBuilders.geoPolygon()
                            .field(field)
                            .polygon(b -> b.points(geoPoints))
                            .build()._toQuery()
                    );
                }
                builderList.add(andQueryBuilder.build()._toQuery());
            } else {
                throw new NotImplementedException(geometryType + " WKT is not supported for `notpwithin`");
            }
        }
        return builderList;
    }

    public Query filterGWithin(String field, String geometry) throws ArlasException {
        try {
            JSONObject shapeBuilder = getShapeObject(geometry);
            return QueryBuilders.geoShape()
                    .field(field)
                    .shape(s -> s
                            .relation(GeoShapeRelation.Within)
                            .shape(JsonData.of(shapeBuilder))
                    ).build()._toQuery();
        } catch (Exception e) {
            throw new ArlasException("Exception while building geoWithinQuery: " + e.getMessage());
        }
    }

    public Query filterGIntersect(String field, String geometry) throws ArlasException {
        try {
            JSONObject shapeObject = getShapeObject(geometry);
            return QueryBuilders.geoShape()
                    .field(field)
                    .shape(s -> s
                            .relation(GeoShapeRelation.Intersects)
                            .shape(JsonData.of(shapeObject))
                    ).build()._toQuery();
        } catch (Exception e) {
            throw new ArlasException("Exception while building geoIntersectionQuery: " + e.getMessage());
        }
    }

    @Override
    public FluidSearchService filterSize(Integer size, Integer from) {
        requestBuilder.size(size).from(from);
        return this;
    }

    @Override
    public FluidSearchService searchAfter(Page page, String after) {
        requestBuilder.searchAfter(Arrays.asList(after.split(",")));
        return this;
    }


    @Override
    public FluidSearchService sort(String sort) throws ArlasException {
        String[] fieldList = sort.split(",");
        String field;
        SortOrder sortOrder;
        for (String signedField : fieldList) {
            if (!signedField.equals("")) {
                if (signedField.charAt(0) == '-') {
                    field = signedField.substring(1);
                    sortOrder = SortOrder.Desc;
                } else {
                    field = signedField;
                    sortOrder = SortOrder.Asc;
                }
                if (field.split(" ").length > 1) {
                    geoDistanceSort(field, sortOrder);
                } else {
                    requestBuilder = requestBuilder.sort(new SortOptions.Builder()
                            .field(new FieldSort.Builder()
                                    .field(field)
                                    .order(sortOrder)
                                    .build())
                            .build());
                }
            }
        }
        return this;
    }

    private void geoDistanceSort(String geoSort, SortOrder sortOrder) throws ArlasException {
        Pair<Double, Double> latLon = ParamsParser.getGeoSortParamsAsLatLon(geoSort);
        GeoLocation sortOnPoint = GeoLocation.of(b1 -> b1.latlon(b2 -> b2.lat(latLon.getLeft()).lon(latLon.getRight())));
        String geoSortField = collectionReference.params.centroidPath;
        requestBuilder = requestBuilder.sort(b -> b
                .geoDistance(d -> d
                        .field(geoSortField)
                        .distanceType(GeoDistanceType.Plane)
                        .location(sortOnPoint)
                        .order(sortOrder)
                )
        );
    }

    // TODO deal with this subAggregation stuff
    private Function<co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder, ObjectBuilder<co.elastic.clients.elasticsearch._types.aggregations.Aggregation>> aggregateRecursive(List<Aggregation> aggregations , Function<co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder, ObjectBuilder<co.elastic.clients.elasticsearch._types.aggregations.Aggregation>>	 aggregation,
                                                                                                                                                                                                   Boolean isGeoAggregate, Integer counter) throws ArlasException {
        //check the agg syntax is correct
        Aggregation aggregationModel = aggregations.get(0);
        if (isGeoAggregate && counter == 0) {
            if (!GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type)
                    && !aggregationModel.type.equals(AggregationTypeEnum.h3)
                    && aggregationModel.rawGeometries == null
                    && aggregationModel.aggregatedGeometries == null) {
                throw new NotAllowedException("'" + aggregationModel.type.name() +"' aggregation type is not allowed in _geoaggregate service if at least `aggregated_geometries` or `raw_geometries` parameters are not specified");
            }
        }
        switch (aggregationModel.type) {
            case datehistogram:
                //aggregation = buildDateHistogramAggregation(aggregationModel).build();
                break;
            case geohash:
                // aggregation = buildGeohashAggregation(aggregationModel).build();
                break;
            case geotile:
                //aggregation = buildGeotileAggregation(aggregationModel).build();
                break;
            case histogram:
                //aggregation = buildHistogramAggregation(aggregationModel).build();
                break;
            case term:
                //aggregation = buildTermsAggregation(aggregationModel).build();
                break;
            case h3:
                //aggregation = buildH3Aggregation(aggregationModel).build();
                break;
        }
        aggregations.remove(0);
        if (aggregations.size() == 0) {
            return aggregation;
        }
        counter++;

        return aggregation.subAgregtaion(aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, counter));

    }

    @Override
    public FluidSearchService aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException {
        // co.elastic.clients.elasticsearch._types.aggregations.Aggregation agg = aggregateRecursive(new ArrayList<>(aggregations), null, isGeoAggregate, 0);
        // TODO deal with this subAggregation stuff
        requestBuilder = requestBuilder.size(0).aggregations("agg", agg);
        return this;
    }

    @Override
    public FluidSearchService compute(String field, ComputationEnum metric) {
        boolQueryBuilder = boolQueryBuilder.filter(builder -> builder.exists(builder1 -> builder1.field(field)));
        switch (metric) {
            case AVG:
                AverageAggregation.Builder avgAggregationBuilder = AggregationBuilders.avg().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_AVG_VALUE,avgAggregationBuilder.build()._toAggregation());
                break;
            case MAX:
                MaxAggregation.Builder maxAggregationBuilder = AggregationBuilders.max().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_MAX_VALUE,maxAggregationBuilder.build()._toAggregation());
                break;
            case MIN:
                MinAggregation.Builder minAggregationBuilder = AggregationBuilders.min().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_MIN_VALUE,minAggregationBuilder.build()._toAggregation());
                break;
            case SUM:
                SumAggregation.Builder sumAggregationBuilder = AggregationBuilders.sum().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_SUM_VALUE,sumAggregationBuilder.build()._toAggregation());

            case CARDINALITY:
                CardinalityAggregation.Builder cardinalityAggregationBuilder = AggregationBuilders.cardinality().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_CARDINALITY_VALUE,cardinalityAggregationBuilder.build()._toAggregation());
                break;
            case SPANNING:
                minAggregationBuilder = AggregationBuilders.min().field(field);
                maxAggregationBuilder = AggregationBuilders.max().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_MIN_VALUE,minAggregationBuilder.build()._toAggregation())
                        .aggregations(FIELD_MAX_VALUE,maxAggregationBuilder.build()._toAggregation());
                break;
            case GEOBBOX:
                GeoBoundsAggregation.Builder geoBoundsAggregationBuilder = AggregationBuilders.geoBounds().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_GEOBBOX_VALUE,geoBoundsAggregationBuilder.build()._toAggregation());
                break;
            case GEOCENTROID:
                GeoCentroidAggregation.Builder geoCentroidAggregationBuilder = AggregationBuilders.geoCentroid().field(field);
                requestBuilder = requestBuilder.size(0).aggregations(FIELD_GEOCENTROID_VALUE,geoCentroidAggregationBuilder.build()._toAggregation());
                break;
        }
        return this;
    }



    private DateHistogramAggregation.Builder buildDateHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        if (StringUtil.isNullOrEmpty(aggregationModel.field)) {
            aggregationModel.field = collectionReference.params.timestampPath;
        }
        DateHistogramAggregation.Builder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram();
        if (aggregationModel.interval.unit.equals(UnitEnum.year)
                || aggregationModel.interval.unit.equals(UnitEnum.month)
                || aggregationModel.interval.unit.equals(UnitEnum.quarter)
                || aggregationModel.interval.unit.equals(UnitEnum.week)) {
            if ((Integer)aggregationModel.interval.value > 1)
                throw new NotAllowedException("The size must be equal to 1 for the unit " + aggregationModel.interval.unit + ".");
        }
        CalendarInterval intervalUnit;
        Time intervalTime = null;
        switch (aggregationModel.interval.unit) {
            case year -> intervalUnit = CalendarInterval.Year;
            case quarter -> intervalUnit = CalendarInterval.Quarter;
            case month -> intervalUnit = CalendarInterval.Month;
            case week -> intervalUnit = CalendarInterval.Week;
            case day -> {
                intervalUnit = CalendarInterval.Day;
                intervalTime = Time.of(builder -> builder.time(aggregationModel.interval.value.toString() + "d"));
            }
            case hour -> {
                intervalUnit = CalendarInterval.Hour;
                intervalTime = Time.of(builder -> builder.time(aggregationModel.interval.value.toString() + "h"));
            }
            case minute -> {
                intervalUnit = CalendarInterval.Minute;
                intervalTime = Time.of(builder -> builder.time(aggregationModel.interval.value.toString() + "m"));
            }
            case second -> {
                intervalUnit = CalendarInterval.Second;
                intervalTime = Time.of(builder -> builder.time(aggregationModel.interval.value.toString() + "s"));
            }
            default -> throw new InvalidParameterException(INVALID_DATE_UNIT);
        }
        if ((Integer)aggregationModel.interval.value > 1) {
            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.fixedInterval(intervalTime);
        } else {
            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.calendarInterval(intervalUnit);
        }
        //get the field, format, collect_field, collect_fct, order, on
        dateHistogramAggregationBuilder = (DateHistogramAggregation.Builder) setAggregationParameters(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregation.Builder) setHitsToFetch(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregation.Builder) setAggregatedGeometries(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregation.Builder) setRawGeometries(aggregationModel, dateHistogramAggregationBuilder);
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregationModel builder
    private GeoHashGridAggregation.Builder buildGeohashAggregation(Aggregation aggregationModel) throws ArlasException {
        GeoHashGridAggregation.Builder geoHashAggregationBuilder = AggregationBuilders.geohashGrid();
        //get the precision
        GeoHashPrecision precision = GeoHashPrecision.of(builder -> builder.geohashLength(aggregationModel.interval.value));
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoHashGridAggregation.Builder) setAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoHashGridAggregation.Builder) setAggregatedGeometries(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoHashGridAggregation.Builder) setRawGeometries(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoHashGridAggregation.Builder) setHitsToFetch(aggregationModel, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    // construct and returns the geotile aggregationModel builder
    private GeoTileGridAggregation.Builder buildGeotileAggregation(Aggregation aggregationModel) throws ArlasException {
        GeoTileGridAggregation.Builder geoTileAggregationBuilder = AggregationBuilders.geotileGrid();
        //get the precision
        Integer precision = (Integer)aggregationModel.interval.value;
        geoTileAggregationBuilder = geoTileAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoTileAggregationBuilder = (GeoTileGridAggregation.Builder) setAggregationParameters(aggregationModel, geoTileAggregationBuilder);
        geoTileAggregationBuilder = (GeoTileGridAggregation.Builder) setAggregatedGeometries(aggregationModel, geoTileAggregationBuilder);
        geoTileAggregationBuilder = (GeoTileGridAggregation.Builder) setRawGeometries(aggregationModel, geoTileAggregationBuilder);
        geoTileAggregationBuilder = (GeoTileGridAggregation.Builder) setHitsToFetch(aggregationModel, geoTileAggregationBuilder);
        return geoTileAggregationBuilder;
    }

    // construct and returns the h3 aggregationModel builder
    private TermsAggregation.Builder buildH3Aggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregation.Builder termsAggregationBuilder = AggregationBuilders.terms();
        aggregationModel.field = aggregationModel.field + "." + aggregationModel.interval.value;
        if (aggregationModel.size == null) {
            aggregationModel.size = "10000"; // by default
        }
        //get the field, format, collect_field, collect_fct, order, on
        termsAggregationBuilder = (TermsAggregation.Builder) setAggregationParameters(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregation.Builder) setAggregatedGeometries(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregation.Builder) setRawGeometries(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregation.Builder) setHitsToFetch(aggregationModel, termsAggregationBuilder);
        if (!StringUtil.isNullOrEmpty(aggregationModel.include)) {
            String[] includeList = aggregationModel.include.split(",");
            termsAggregationBuilder = termsAggregationBuilder.include(builder -> builder.terms(Arrays.asList(includeList)));
        }
        return termsAggregationBuilder;
    }

    // construct and returns the histogram aggregationModel builder
    private HistogramAggregation.Builder buildHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        HistogramAggregation.Builder histogramAggregationBuilder = AggregationBuilders.histogram();
        histogramAggregationBuilder = histogramAggregationBuilder.interval((Double)aggregationModel.interval.value);
        //get the field, format, collect_field, collect_fct, order, on
        histogramAggregationBuilder = (HistogramAggregation.Builder) setAggregationParameters(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregation.Builder) setHitsToFetch(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregation.Builder) setAggregatedGeometries(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregation.Builder) setRawGeometries(aggregationModel, histogramAggregationBuilder);
        return histogramAggregationBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private TermsAggregation.Builder buildTermsAggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregation.Builder termsAggregationBuilder = AggregationBuilders.terms();
        //get the field, format, collect_field, collect_fct, order, on
        termsAggregationBuilder = (TermsAggregation.Builder) setAggregationParameters(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregation.Builder) setAggregatedGeometries(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregation.Builder) setRawGeometries(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregation.Builder) setHitsToFetch(aggregationModel, termsAggregationBuilder);
        if (!StringUtil.isNullOrEmpty(aggregationModel.include)) {
            String[] includeList = aggregationModel.include.split(",");
            termsAggregationBuilder = termsAggregationBuilder.include(builder -> builder.terms(Arrays.asList(includeList)));
        }
        return termsAggregationBuilder;
    }

    private ObjectBuilder  setAggregationParameters(Aggregation aggregationModel, final ObjectBuilder aggregationBuilder) throws ArlasException {
        aggregationBuilder = aggregationBuilder.field(aggregationModel.field);
        //Get the format
        String format = ParamsParser.getValidAggregationFormat(aggregationModel.format);
        if (aggregationBuilder instanceof DateHistogramAggregation.Builder) {
            // aggregationBuilder = ((DateHistogramAggregation.Builder) aggregationBuilder).format(format);
        } else if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        // firstMetricAggregationBuilder is the aggregation builder on which the order aggregation will be applied
        ObjectBuilder firstMetricAggregationBuilder = null;
        if (aggregationModel.metrics != null) {
            for (Metric m: aggregationModel.metrics) {
                ObjectBuilder metricAggregationBuilder = null;
                if (m.collectField != null && m.collectFct == null) {
                    throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
                } else if (m.collectField == null && m.collectFct != null) {
                    throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
                }
                switch (m.collectFct) {
                    case AVG -> metricAggregationBuilder = AggregationBuilders.avg().field(m.collectField);
                    case CARDINALITY ->
                            metricAggregationBuilder = AggregationBuilders.cardinality().field(m.collectField);
                    case MAX -> metricAggregationBuilder = AggregationBuilders.max().field(m.collectField);
                    case MIN -> metricAggregationBuilder = AggregationBuilders.min().field(m.collectField);
                    case SUM -> metricAggregationBuilder = AggregationBuilders.sum().field(m.collectField);
                    case GEOCENTROID -> {
                        setGeoMetricAggregationCollectField(m);
                        /** We calculate this metric only if it wasn't requested as a geometry to return in `aggregatedGeometries` parameter **/
                        if (!(aggregationModel.aggregatedGeometries != null && aggregationModel.aggregatedGeometries.contains(AggregatedGeometryEnum.CENTROID) && aggregationModel.field.equals(m.collectField))) {
                            metricAggregationBuilder = AggregationBuilders.geoCentroid().field(m.collectField);
                        }
                    }
                    case GEOBBOX -> {
                        setGeoMetricAggregationCollectField(m);
                        /** We calculate this metric only if it wasn't requested as a geometry to return in `aggregatedGeometries` parameter **/
                        if (!(aggregationModel.aggregatedGeometries != null && aggregationModel.aggregatedGeometries.contains(AggregatedGeometryEnum.BBOX) && aggregationModel.field.equals(m.collectField))) {
                            metricAggregationBuilder = AggregationBuilders.geoBounds().field(m.collectField);
                        }
                    }
                }
                if (metricAggregationBuilder != null) {
                    aggregationBuilder.subAggregation(metricAggregationBuilder);
                }

                // Getting the first metric aggregation builder that is different from GEOBBOX and GEOCENTROID, on which the order will be applied
                if (firstMetricAggregationBuilder == null && m.collectFct != CollectionFunction.GEOBBOX &&  m.collectFct != CollectionFunction.GEOCENTROID) {
                    firstMetricAggregationBuilder = metricAggregationBuilder;
                }
            }
        }

        if (aggregationModel.size != null) {
            Integer s = ParamsParser.getValidAggregationSize(aggregationModel.size);
            if (aggregationBuilder instanceof TermsAggregation.Builder)
                aggregationBuilder = ((TermsAggregation.Builder) aggregationBuilder).size(s);
            else if (aggregationBuilder instanceof GeoHashGridAggregation.Builder || aggregationBuilder instanceof GeoTileGridAggregation.Builder )
                throw new NotImplementedException(SIZE_NOT_IMPLEMENTED);
            else
                throw new BadRequestException(NO_SIZE_TO_SPECIFY);
        }

        setOrder(aggregationModel, aggregationBuilder, firstMetricAggregationBuilder);
        return aggregationBuilder;
    }

    private ObjectBuilder setAggregatedGeometries(Aggregation aggregationModel, ObjectBuilder aggregationBuilder) {
        if (aggregationModel.aggregatedGeometries != null) {
            String aggregationGeoField = GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type) ? aggregationModel.field : collectionReference.params.centroidPath;
            aggregationModel.aggregatedGeometries.forEach(ag -> {
                ObjectBuilder metricAggregation;
                switch (ag) {
                    case BBOX -> {
                        metricAggregation = AggregationBuilders.geoBounds().field(aggregationGeoField);
                        aggregationBuilder.subAggregation(metricAggregation);
                    }
                    case CENTROID -> {
                        metricAggregation = AggregationBuilders.geoCentroid().field(aggregationGeoField);
                        aggregationBuilder.subAggregation(metricAggregation);
                    }
                }
            });
        }
        return aggregationBuilder;
    }

    private ObjectBuilder setRawGeometries(Aggregation aggregationModel, ObjectBuilder aggregationBuilder) throws ArlasException {
        if (aggregationModel.rawGeometries != null) {
            Map<String, Set<String>> rgs = new HashMap<>();
            aggregationModel.rawGeometries.forEach(rg -> {
                Set<String> geos = rgs.get(rg.sort);
                if (geos == null) geos = new HashSet<>();
                geos.add(rg.geometry);
                rgs.put(rg.sort, geos);
            });
            for (String sort: rgs.keySet()) {
                String[] includes = rgs.get(sort).toArray(String[]::new);
                TopHitsAggregation.Builder topHitsAggregationBuilder = AggregationBuilders.topHits().size(1).fetchSource(includes, null);
                for (String field : sort.split(",")) {
                    String unsignedField = (field.startsWith("+") || field.startsWith("-")) ? field.substring(1) : field;
                    CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                    if (field.startsWith("+") || !field.startsWith("-")) {
                        topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Asc))));
                    } else {
                        topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Desc))));
                    }
                }
                aggregationBuilder
                        .subAggregation(topHitsAggregationBuilder);
            }

        }
        return aggregationBuilder;
    }

    private ObjectBuilder setHitsToFetch(Aggregation aggregationModel, ObjectBuilder aggregationBuilder) throws ArlasException {
        if (aggregationModel.fetchHits != null) {
            TopHitsAggregation.Builder topHitsAggregationBuilder = AggregationBuilders.topHits();
            Integer size = Optional.ofNullable(aggregationModel.fetchHits.size).orElse(1);
            topHitsAggregationBuilder.size(size);
            List<String> includes = new ArrayList<>();
            if (aggregationModel.fetchHits.include != null) {
                for (String field : aggregationModel.fetchHits.include) {
                    String unsignedField = (field.startsWith("+") || field.startsWith("-")) ? field.substring(1) : field;
                    CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                    includes.add(unsignedField);
                    /** For geo-fields, we don't sort them. Sorting geo-fields need to be according a given point to calculate a geo-distance
                     * which is not supported in the syntax of fetch_hits*/
                    if (CollectionReferenceManager.getInstance().getType(collectionReference, unsignedField, false) != FieldType.GEO_POINT && CollectionReferenceManager.getInstance().getType(collectionReference, unsignedField, false) != FieldType.GEO_SHAPE) {
                        if (field.startsWith("+") || !field.startsWith("-")) {
                            topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Asc))));
                        } else {
                            topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Desc))));
                        }
                    }
                }
                String[] hitsToInclude = includes.toArray(new String[0]);
                topHitsAggregationBuilder.fetchSource(hitsToInclude, null);
            }
            aggregationBuilder.subAggregation(topHitsAggregationBuilder);
        }
        return aggregationBuilder;
    }

    private void setGeoMetricAggregationCollectField(Metric metric) throws ArlasException {
        FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, metric.collectField, true);
        if (fieldType != FieldType.GEO_POINT) {
            throw new InvalidParameterException("collect_field: `" + metric.collectField + "` is not a geo-point field. " + "`" + metric.collectFct.name() + "` is applied to geo-points only.");
        }
    }

    private void setOrder(Aggregation aggregationModel, ObjectBuilder aggregationBuilder, ObjectBuilder metricAggregation) throws ArlasException {
        Order order = aggregationModel.order;
        OrderOn on = aggregationModel.on;
        if (order != null && on != null) {
            if (!(aggregationBuilder instanceof GeoHashGridAggregation.Builder) && !(aggregationBuilder instanceof GeoTileGridAggregation.Builder)) {
                Boolean asc = order.equals(Order.asc);
                HistogramOrder histogramOrder = null;
                if (on.equals(OrderOn.field)) {
                    histogramOrder = HistogramOrder.of(builder -> builder.key(SortOrder.Asc));
                } else if (on.equals(OrderOn.count)) {
                    histogramOrder = HistogramOrder.of(builder -> builder.count(SortOrder.Asc));
                } else if (on.equals(OrderOn.result)) {
                    if (metricAggregation != null) {
                        // ORDER ON RESULT IS NOT ALLOWED ON COORDINATES (CENTROID) OR BOUNDING BOX
                        if (!metricAggregation.getName().equals(CollectionFunction.GEOBBOX.name().toLowerCase()) && !metricAggregation.getName().equals(CollectionFunction.GEOCENTROID.name().toLowerCase())) {
                            bucketOrder = BucketOrder.aggregation(metricAggregation.getName(), asc);
                        } else {
                            throw new BadRequestException(ORDER_ON_GEO_RESULT_NOT_ALLOWED);
                        }
                    } else {
                        throw new BadRequestException(ORDER_ON_RESULT_NOT_ALLOWED);
                    }
                }
                HistogramOrder.of(builder -> builder.count(SortOrder.Asc));
                switch (aggregationBuilder.getName()) {
                    case DATEHISTOGRAM_AGG:
                        aggregationBuilder = ((DateHistogramAggregation.Builder) aggregationBuilder).order(histogramOrder);
                        break;
                    case HISTOGRAM_AGG:
                        aggregationBuilder = ((HistogramAggregation.Builder) aggregationBuilder).order(histogramOrder);
                        break;
                    case TERM_AGG:
                        // TODO
                        // aggregationBuilder = ((TermsAggregation.Builder) aggregationBuilder).order();
                        break;
                    default:
                        throw new NotAllowedException(NO_ORDER_ON_TO_SPECIFY);
                }
            } else {
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            }
        } else if (order != null && on == null) {
            if (aggregationBuilder instanceof GeoHashGridAggregation.Builder || aggregationBuilder instanceof GeoTileGridAggregation.Builder )
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ON_NOT_SPECIFIED);
        } else if (order == null && on != null) {
            if (aggregationBuilder instanceof GeoHashGridAggregation.Builder || aggregationBuilder instanceof GeoTileGridAggregation.Builder )
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ORDER_NOT_SPECIFIED);
        }
    }

    private JSONObject createPolygon(org.locationtech.jts.geom.Polygon geomPolygon) {
        Coordinate[] exteriorRing = geomPolygon.getExteriorRing().getCoordinates();
        // TODO : deal with interior ring too
        // int nInteriorRing = geomPolygon.getNumInteriorRing();
        // geomPolygon.getInteriorRingN(nInteriorRing);
        JSONObject polygon = new JSONObject();
        JSONArray jsonArayExt = new JSONArray();
        Arrays.asList(exteriorRing).forEach(coordinate -> {
            JSONArray jsonArayLngLat = new JSONArray();
            jsonArayLngLat.add(0, coordinate.x);
            jsonArayLngLat.add(1, coordinate.y);
            jsonArayExt.add(jsonArayLngLat);
        });
        JSONArray jsonAray = new JSONArray();
        jsonAray.add(jsonArayExt);
        polygon.put("type", "Polygon");
        polygon.put("coordinates", jsonAray);
        return polygon;
    }

    private JSONObject createPolygonFromBbox(double[] bbox) {
        double west = bbox[0];
        double east = bbox[2];
        double south = bbox[1];
        double north = bbox[3];
        /** In ARLAS-api west and east are necessarily between -180 and 180**/
        /** In case of west > east, it means the bbox crosses the dateline (antiméridien) => a translation of west or east by 360 is necessary to be
         * correctly interpreted by geoWithinQuery and geoIntersectsQuery*/
        if (west > east) {
            if (west >= 0) {
                west -= 360;
            } else {
                /** east is necessarily < 0 */
                east += 360;
            }
        }
        Polygon polygonGeometry = new Polygon();
        List<LngLatAlt> exteriorRing = new ArrayList<>();
        exteriorRing.add(new LngLatAlt(east, south));
        exteriorRing.add(new LngLatAlt(east, north));
        exteriorRing.add(new LngLatAlt(west, north));
        exteriorRing.add(new LngLatAlt(west, south));
        exteriorRing.add(new LngLatAlt(east, south));
        polygonGeometry.setExteriorRing(exteriorRing);
        JSONObject polygon = new JSONObject();
        JSONArray jsonArayExt = new JSONArray();
        polygonGeometry.getExteriorRing().forEach(lngLatAlt -> {
            JSONArray jsonArayLngLat = new JSONArray();
            jsonArayLngLat.add(0, lngLatAlt.getLongitude());
            jsonArayLngLat.add(1, lngLatAlt.getLatitude());
            jsonArayExt.add(jsonArayLngLat);
        });
        JSONArray jsonAray = new JSONArray();
        jsonAray.add(jsonArayExt);
        polygon.put("type", "Polygon");
        polygon.put("coordinates", jsonAray);
        return polygon;
    }

    private JSONObject createMultiPolygon(MultiPolygon geomMultiPolygon) {

        int nPolygon = geomMultiPolygon.getNumGeometries();
        JSONObject multiPolygon = new JSONObject();
        JSONArray coordinates = new JSONArray();
        for (int i = 0; i < nPolygon ; i++) {
            org.locationtech.jts.geom.Polygon geomPolygon = (org.locationtech.jts.geom.Polygon) geomMultiPolygon.getGeometryN(i);
            JSONArray jsonArayExt = new JSONArray();
            // TODO : deal with interior ring too
            Coordinate[] exteriorRing = geomPolygon.getExteriorRing().getCoordinates();
            Arrays.asList(exteriorRing).forEach(coordinate -> {
                JSONArray jsonArayLngLat = new JSONArray();
                jsonArayLngLat.add(0, coordinate.x);
                jsonArayLngLat.add(1, coordinate.y);
                jsonArayExt.add(jsonArayLngLat);
            });
            JSONArray jsonAray = new JSONArray();
            jsonAray.add(jsonArayExt);
            coordinates.add(jsonAray);
        }
        multiPolygon.put("type", "MultiPolygon");
        multiPolygon.put("coordinates", coordinates);
        return multiPolygon;
    }

    private JSONObject createLineString(LineString geomLineString) {
        JSONObject lineString = new JSONObject();
        JSONArray jsonAray = new JSONArray();
        Coordinate[] lineSequence = geomLineString.getCoordinates();
        Arrays.asList(lineSequence).forEach(coordinate -> {
            JSONArray jsonArayLngLat = new JSONArray();
            jsonArayLngLat.add(0, coordinate.x);
            jsonArayLngLat.add(1, coordinate.y);
            jsonAray.add(jsonArayLngLat);
        });
        lineString.put("type", "LineString");
        lineString.put("coordinates", jsonAray);
        return lineString;
    }

    private JSONObject createPoint(Point geomPoint) {
        JSONObject point = new JSONObject();
        JSONArray jsonAray = new JSONArray();
        jsonAray.add(0, geomPoint.getCoordinate().x);
        jsonAray.add(1, geomPoint.getCoordinate().y);
        point.put("type", "Point");
        point.put("coordinates", jsonAray);
        return point;
    }

    private JSONObject getShapeObject(String geometry, Boolean righthand) throws ArlasException {
        // test if geometry is 'west,south,east,north' or wkt string
        if (CheckParams.isBboxMatch(geometry)) {
            return createPolygonFromBbox(CheckParams.toDoubles(geometry));
        } else {
            // TODO: multilinestring
            Geometry wktGeometry = GeoUtil.readWKT(geometry);
            if (wktGeometry != null) {
                String geometryType = wktGeometry.getGeometryType().toUpperCase();
                return switch (geometryType) {
                    case "POLYGON" -> createPolygon((org.locationtech.jts.geom.Polygon) wktGeometry, righthand);
                    case "MULTIPOLYGON" -> createMultiPolygon((MultiPolygon) wktGeometry, righthand);
                    case "LINESTRING" -> createLineString((LineString) wktGeometry);
                    case "POINT" -> createPoint((Point) wktGeometry);
                    default -> throw new InvalidParameterException("The given geometry is not handled.");
                };
            }
            throw new InvalidParameterException("The given geometry is invalid.");
        }
    }
}