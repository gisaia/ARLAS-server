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
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import co.elastic.clients.util.ObjectBuilder;
import io.arlas.commons.exceptions.*;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.ArlasBaseConfiguration;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.*;

import java.util.*;

import static io.arlas.server.core.utils.CheckParams.GEO_AGGREGATION_TYPE_ENUMS;

public class ElasticFluidSearch extends FluidSearchService {

    public static final String COORDINATES = "coordinates";
    public static final String POLYGON = "Polygon";
    public static final String MULTI_POLYGON = "MultiPolygon";
    public static final String ORDER_SIGN_REGEX = "^[+-]";
    private ElasticClient client;

    private final int elasticMaxPrecisionThreshold;
    private SearchRequest.Builder requestBuilder;
    private BoolQuery.Builder boolQueryBuilder;
    private BoolQuery.Builder boolPartitionQueryBuilder;


    public ElasticFluidSearch(CollectionReference collectionReference, int elasticMaxPrecisionThreshold) {
        super(collectionReference);
        this.elasticMaxPrecisionThreshold = elasticMaxPrecisionThreshold;
        requestBuilder = new SearchRequest.Builder()
                .index(collectionReference.params.indexName)
                .trackTotalHits(b -> b.enabled(true));
        boolQueryBuilder = new BoolQuery.Builder();
        boolPartitionQueryBuilder = new BoolQuery.Builder();

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
    public BoolQuery.Builder getBoolPartitionQueryBuilder() {
        return boolPartitionQueryBuilder;
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
                .query(boolQueryBuilder.must(boolPartitionQueryBuilder.build()._toQuery()).build()._toQuery())
                .build();

        // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/reading.html#_reading_raw_json
        return client.search(request);
    }

    @Override
    public FluidSearchService filter(MultiValueFilter<Expression> f, String dateFormat, Boolean rightHand) throws ArlasException {
        List<Query> queries = new ArrayList<>();
        for (Expression fFilter : f) {
            queries.add(filter(fFilter, dateFormat));
        }
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.bool().should(queries).minimumShouldMatch("1").build()._toQuery());
        return this;
    }

    @Override
    public FluidSearchService partitionFilter(List<Filter> filters) throws ArlasException {
        List<Query> finalQueries = new ArrayList<>();
        for (Filter filter : filters){
            // OR LEVEL
            BoolQuery.Builder builder = new BoolQuery.Builder();
            if(filter.f != null){
                for (MultiValueFilter<Expression> f : filter.f) {
                    // AND LEVEL
                    List<Query> queries = new ArrayList<>();
                    for (Expression fFilter : f) {
                        //OR LEVEL
                        queries.add(filter(fFilter, filter.dateformat));
                    }
                    builder = builder.filter(QueryBuilders.bool().should(queries).minimumShouldMatch("1").build()._toQuery());
                }
            }
            if(filter.q != null){
                for (MultiValueFilter<String> q : filter.q) {
                    BoolQuery.Builder orBoolQueryBuilder = getFilterQBuilder(q);
                    builder = builder.filter(orBoolQueryBuilder.build()._toQuery());
                }
            }
            //AND OF OR
            finalQueries.add(builder.build()._toQuery());
        }
        // OR OF (AND OF OR)
        boolPartitionQueryBuilder = boolPartitionQueryBuilder.should(finalQueries).minimumShouldMatch("1");
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
                ret = getEqFilter(fieldValues, ret, field, value);
                break;
            case gte,gt,lte,lt:
                ret = getCompareFilter(dateFormat, field, value, ret,op, this::getCompareQuery);
                break;
            case like:
                if (isTextField(field)) {
                    ret = ret.filter(QueryBuilders.matchPhrasePrefix().field(field).query(value).build()._toQuery());
                } else {
                    ret = ret.filter(QueryBuilders.regexp().field(field).value(".*" + value + ".*").build()._toQuery());
                }
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
                ret = getGeoFilter(field, value, ret, true);
                break;
            case notwithin:
                ret = getGeoFilter(field, value, ret, false);
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

    private BoolQuery.Builder getGeoFilter(String field, String value, BoolQuery.Builder ret, Boolean within) throws ArlasException {
        FieldType wType = collectionReferenceManager.getType(collectionReference, field, true);
        BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder();

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
                if(Boolean.TRUE.equals(within) ) {
                    throw new ArlasException("'within' op on field '" + field + "' of type '" + wType + "' is not supported");
                } else {
                    throw new ArlasException("'not within' op on field '" + field + "' of type '" + wType + "' is not supported");
                }
        }
        if(Boolean.TRUE.equals(within) ) {
            ret = ret.filter(orBoolQueryBuilder.minimumShouldMatch("1").build()._toQuery());
        } else {
            ret = ret.mustNot(orBoolQueryBuilder.build()._toQuery());
        }
        return ret;
    }

    @FunctionalInterface
    interface Operation {
        RangeQuery.Builder execute(String field, String value, OperatorEnum operator) throws ArlasException;
    }

    private RangeQuery.Builder getCompareQuery(String field, String value, OperatorEnum operator) throws ArlasException {
        RangeQuery.Builder builder = QueryBuilders.range().field(field);
        return switch (operator) {
            case lte -> builder.lte(JsonData.of(value));
            case gte -> builder.gte(JsonData.of(value));
            case lt -> builder.lt(JsonData.of(value));
            case gt -> builder.gt(JsonData.of(value));
            default -> throw new ArlasException("Unexpected value: " + operator);
        };
    }

    private BoolQuery.Builder getCompareFilter(String dateFormat, String field, String value, BoolQuery.Builder ret, OperatorEnum operator, Operation operation) throws ArlasException {
        if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
            value = ParamsParser.parseDate(value, dateFormat);
        }
        RangeQuery.Builder rangeQuery = operation.execute(field,value,operator);
        applyFormatOnRangeQuery(field, value, rangeQuery);
        ret = ret.filter(rangeQuery.build()._toQuery());
        return ret;
    }

    private static BoolQuery.Builder getEqFilter(String[] fieldValues, BoolQuery.Builder ret, String field, String value) {
        if (fieldValues.length > 1) {
            ret = ret.filter(QueryBuilders.bool().should(
                            Arrays.stream(fieldValues)
                                    .map(valueInValues -> QueryBuilders.match()
                                            .field(field)
                                            .query(valueInValues)
                                            .build()
                                            ._toQuery())
                                    .toList()
                            ).build()._toQuery()
            );
        } else {
            ret = ret.filter(QueryBuilders.match().field(field).query(value).build()._toQuery());
        }
        return ret;
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

        if (isDateField(field) && (field.equals(collectionReference.params.timestampPath) || StringUtil.isNullOrEmpty(dateFormat))) {
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
        if (isDateField(field) && (field.equals(collectionReference.params.timestampPath) || StringUtil.isNullOrEmpty(dateFormat))) {
            ret.format(TimestampType.epoch_millis.name());
        }
        return ret;
    }

    @Override
    public FluidSearchService filterQ(MultiValueFilter<String> q) throws ArlasException {
        BoolQuery.Builder orBoolQueryBuilder = getFilterQBuilder(q);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder.build()._toQuery());
        return this;
    }

    private BoolQuery.Builder getFilterQBuilder(MultiValueFilter<String> q) throws InvalidParameterException {
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
        return orBoolQueryBuilder;
    }

    public List<Query> filterPWithin(String field, String pwithinFilter) throws ArlasException {
        List<Query> builderList = new ArrayList<>();
        if (CheckParams.isBboxMatch(pwithinFilter)) {
            double[] tlbr = CheckParams.toDoubles(pwithinFilter);
            builderList.add(filterPWithin(field, tlbr[0], tlbr[1], tlbr[2], tlbr[3]));
        } else {
            Geometry p = GeoUtil.readWKT(pwithinFilter);
            String geometryType = p.getGeometryType();
            if (geometryType.equals(POLYGON) || geometryType.equals(MULTI_POLYGON)) {
                for (int i = 0; i< p.getNumGeometries(); i++) {
                    JSONObject shapeObject = getShapeObject(p.getGeometryN(i));
                    GeoShapeQuery.Builder andQueryBuilder = QueryBuilders.geoShape()
                            .field(field)
                            .shape(s -> s
                                    .relation(GeoShapeRelation.Within)
                                    .shape(JsonData.of(shapeObject)));
                    builderList.add(andQueryBuilder.build()._toQuery());
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
                                .topLeft(b3 -> b3.coords(Arrays.asList(west, north)))
                                .bottomRight(b3 -> b3.coords(Arrays.asList(east, south)))
                        )
                ).build()._toQuery();
    }

    public Query filterGWithin(String field, String geometry ) throws ArlasException {
        try {
            JSONObject shapeObject = getShapeObject(geometry);
            return QueryBuilders.geoShape()
                    .field(field)
                    .shape(s -> s
                            .relation(GeoShapeRelation.Within)
                            .shape(JsonData.of(shapeObject))
                    ).build()._toQuery();
        } catch (Exception e) {
            throw new ArlasException("Exception while building geoWithinQuery: " + e.getMessage());
        }
    }

    public Query filterGIntersect(String field, String geometry ) throws ArlasException {
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
        requestBuilder.searchAfter(Arrays.stream(after.split(",")).map(FieldValue::of).toList());
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

    private Builder.ContainerBuilder buildAggregation(List<Aggregation> aggregations , Boolean isGeoAggregate)
            throws ArlasException {


        //Analyse the first aggregation
        Aggregation firsAggregationModel = aggregations.get(0);
        if ( Boolean.TRUE.equals(isGeoAggregate) && !GEO_AGGREGATION_TYPE_ENUMS.contains(firsAggregationModel.type)
                    && firsAggregationModel.rawGeometries == null
                    && firsAggregationModel.aggregatedGeometries == null) {
            throw new NotAllowedException("'" + firsAggregationModel.type.name() +"' aggregation type is not allowed in _geoaggregate service if at least `aggregated_geometries` or `raw_geometries` parameters are not specified");
        }
        Builder.ContainerBuilder aggContainerBuilder = switch (firsAggregationModel.type) {
            case datehistogram -> buildDateHistogramAggregation(firsAggregationModel);
            case geohash -> buildGeohashAggregation(firsAggregationModel);
            case geohex -> buildGeoHexAggregation(firsAggregationModel);
            case geotile -> buildGeoTileAggregation(firsAggregationModel);
            case histogram -> buildHistogramAggregation(firsAggregationModel);
            case term -> buildTermsAggregation(firsAggregationModel);
        };
        //add sub aggregation
        for (int i = 1; i < aggregations.size(); i++) {
            Aggregation aggregationModel = aggregations.get(i);
            aggContainerBuilder = switch (aggregationModel.type) {
                case datehistogram ->
                        aggContainerBuilder.aggregations(DATEHISTOGRAM_AGG + i, buildDateHistogramAggregation(aggregationModel).build());
                case geohash ->
                        aggContainerBuilder.aggregations(GEOHASH_AGG + i, buildGeohashAggregation(aggregationModel).build());
                case geohex ->
                        aggContainerBuilder.aggregations(GEOHEX_AGG + i, buildGeoHexAggregation(aggregationModel).build());
                case geotile ->
                        aggContainerBuilder.aggregations(GEOTILE_AGG + i, buildGeoTileAggregation(aggregationModel).build());
                case histogram ->
                        aggContainerBuilder.aggregations(HISTOGRAM_AGG + i, buildHistogramAggregation(aggregationModel).build());
                case term ->
                        aggContainerBuilder.aggregations(TERM_AGG + i, buildTermsAggregation(aggregationModel).build());
            };

        }
        return aggContainerBuilder;

    }

    @Override
    public FluidSearchService aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException {
        requestBuilder = requestBuilder.size(0).aggregations("mainAgg", buildAggregation(aggregations,  isGeoAggregate).build());
        return this;
    }

    @Override
    public FluidSearchService compute(String field, ComputationEnum metric, int precisionThreshold) {
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
                break;
            case CARDINALITY:
                CardinalityAggregation.Builder cardinalityAggregationBuilder = AggregationBuilders.cardinality().field(field)
                        .precisionThreshold(Math.min(Optional.ofNullable(precisionThreshold).orElse(3000), elasticMaxPrecisionThreshold));
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

    private Builder.ContainerBuilder buildDateHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        if (StringUtil.isNullOrEmpty(aggregationModel.field)) {
            aggregationModel.field = collectionReference.params.timestampPath;
        }
        UnitEnum unit = aggregationModel.interval.unit;
        if (unit.equals(UnitEnum.year) && ((Integer) aggregationModel.interval.value > 1) || unit.equals(UnitEnum.month) && ((Integer) aggregationModel.interval.value > 1) || unit.equals(UnitEnum.quarter) && ((Integer) aggregationModel.interval.value > 1) || unit.equals(UnitEnum.week) && ((Integer) aggregationModel.interval.value > 1)) {
            throw new NotAllowedException("The size must be equal to 1 for the unit " + unit + ".");
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

        String format = ParamsParser.getValidAggregationFormat(aggregationModel.format);
        DateHistogramAggregation.Builder  dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram()
                .field(aggregationModel.field)
                .format(format);
        if ((Integer)aggregationModel.interval.value > 1) {
            dateHistogramAggregationBuilder.fixedInterval(intervalTime);
        } else {
            dateHistogramAggregationBuilder.calendarInterval(intervalUnit);
        }
        //get collect_field, collect_fct, order, on
        Map<String,co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = getAggregationParameters(aggregationModel, dateHistogramAggregationBuilder);
        Builder.ContainerBuilder dateHistogramContainerBuilder = new Builder().dateHistogram(dateHistogramAggregationBuilder.build());
        dateHistogramContainerBuilder.aggregations(metricsAggregation);
        setAggregatedGeometries(aggregationModel, dateHistogramContainerBuilder);
        setRawGeometriesAndFetch(aggregationModel, dateHistogramContainerBuilder);
        return dateHistogramContainerBuilder;
    }

    // construct and returns the geohash aggregationModel builder
    private Builder.ContainerBuilder buildGeohashAggregation(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        //get the precision
        GeoHashPrecision precision = GeoHashPrecision.of(builder -> builder.geohashLength(aggregationModel.interval.value));
        GeoHashGridAggregation.Builder geoHashAggregationBuilder = AggregationBuilders.geohashGrid().precision(precision).field(aggregationModel.field);
        //get collect_field, collect_fct, order, on
        Map<String,co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = getAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        Builder.ContainerBuilder geoHashAggregationContainerBuilder = new Builder().geohashGrid(geoHashAggregationBuilder.build());
        geoHashAggregationContainerBuilder.aggregations(metricsAggregation);
        setAggregatedGeometries(aggregationModel, geoHashAggregationContainerBuilder);
        setRawGeometriesAndFetch(aggregationModel, geoHashAggregationContainerBuilder);
        return geoHashAggregationContainerBuilder;
    }

    // construct and returns the geoHex aggregationModel builder
    private Builder.ContainerBuilder buildGeoHexAggregation(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        //get the precision
        Integer precision = (Integer)aggregationModel.interval.value;
        GeohexGridAggregation.Builder geoHexAggregationBuilder = AggregationBuilders.geohexGrid().precision(precision).field(aggregationModel.field);
        //get collect_field, collect_fct, order, on
        Map<String,co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = getAggregationParameters(aggregationModel, geoHexAggregationBuilder);
        Builder.ContainerBuilder geoHexAggregationContainerBuilder = new Builder().geohexGrid(geoHexAggregationBuilder.build());
        geoHexAggregationContainerBuilder.aggregations(metricsAggregation);
        setAggregatedGeometries(aggregationModel, geoHexAggregationContainerBuilder);
        setRawGeometriesAndFetch(aggregationModel, geoHexAggregationContainerBuilder);
        return geoHexAggregationContainerBuilder;
    }

    // construct and returns the geoTile aggregationModel builder
    private Builder.ContainerBuilder buildGeoTileAggregation(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        //get the precision
        Integer precision = (Integer)aggregationModel.interval.value;
        GeoTileGridAggregation.Builder geoTileAggregationBuilder = AggregationBuilders.geotileGrid().precision(precision).field(aggregationModel.field);
        //get collect_field, collect_fct, order, on
        Map<String,co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = getAggregationParameters(aggregationModel, geoTileAggregationBuilder);
        Builder.ContainerBuilder geoTileAggregationContainerBuilder = new Builder().geotileGrid(geoTileAggregationBuilder.build());
        geoTileAggregationContainerBuilder.aggregations(metricsAggregation);
        setAggregatedGeometries(aggregationModel, geoTileAggregationContainerBuilder);
        setRawGeometriesAndFetch(aggregationModel, geoTileAggregationContainerBuilder);
        return geoTileAggregationContainerBuilder;
    }

    // construct and returns the histogram aggregationModel builder
    private Builder.ContainerBuilder buildHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        HistogramAggregation.Builder histogramAggregationBuilder = AggregationBuilders.histogram().interval((Double)aggregationModel.interval.value).field(aggregationModel.field);
        //get collect_field, collect_fct, order, on
        Map<String,co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = getAggregationParameters(aggregationModel, histogramAggregationBuilder);
        Builder.ContainerBuilder histogramAggregationContainerBuilder = new Builder().histogram(histogramAggregationBuilder.build());
        histogramAggregationContainerBuilder.aggregations(metricsAggregation);
        setAggregatedGeometries(aggregationModel, histogramAggregationContainerBuilder);
        setRawGeometriesAndFetch(aggregationModel, histogramAggregationContainerBuilder);
        return histogramAggregationContainerBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private Builder.ContainerBuilder buildTermsAggregation(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        TermsAggregation.Builder termsAggregationBuilder = AggregationBuilders.terms();
        if (!StringUtil.isNullOrEmpty(aggregationModel.include)) {
            termsAggregationBuilder = termsAggregationBuilder.include(builder -> builder.regexp(aggregationModel.include));
        }
        termsAggregationBuilder.field(aggregationModel.field);
        //get collect_field, collect_fct, order, on
        Map<String,co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = getAggregationParameters(aggregationModel, termsAggregationBuilder);
        Builder.ContainerBuilder termsAggregationContainerBuilder = new Builder().terms(termsAggregationBuilder.build());
        termsAggregationContainerBuilder.aggregations(metricsAggregation);
        setAggregatedGeometries(aggregationModel, termsAggregationContainerBuilder);
        setRawGeometriesAndFetch(aggregationModel, termsAggregationContainerBuilder);
        return termsAggregationContainerBuilder;
    }



    private <T> Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregation> getAggregationParameters(Aggregation aggregationModel, ObjectBuilder<T> aggregationBuilder) throws ArlasException {
        String firstMetricAggregation = null;
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregation> metricsAggregation = new HashMap<>();
        if (aggregationModel.metrics != null) {
            for (Metric m : aggregationModel.metrics) {
                validateMetric(m);
                co.elastic.clients.elasticsearch._types.aggregations.Aggregation metricAggregation = buildMetricAggregation(m, aggregationModel);
                if (metricAggregation != null) {
                    String collectField = m.collectField.replace(".", ArlasBaseConfiguration.FLATTEN_CHAR);
                    String metricName = m.collectFct.name().toLowerCase() + ":" + collectField;
                    metricsAggregation.put(metricName, metricAggregation);
                    if (firstMetricAggregation == null && !isSpatial(m.collectFct)) {
                        firstMetricAggregation = metricName;
                    }
                }
            }
        }
        setSizeAggregation(aggregationModel, aggregationBuilder);
        setOrder(aggregationModel, aggregationBuilder, firstMetricAggregation);
        return metricsAggregation;
    }

    private void validateMetric(Metric m) throws BadRequestException {
        if (m.collectField != null && m.collectFct == null) {
            throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
        } else if (m.collectField == null && m.collectFct != null) {
            throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
        }
    }

    private co.elastic.clients.elasticsearch._types.aggregations.Aggregation buildMetricAggregation(Metric m, Aggregation aggregationModel) throws ArlasException {
        return switch (m.collectFct) {
            case AVG -> AggregationBuilders.avg().field(m.collectField).build()._toAggregation();
            case CARDINALITY -> AggregationBuilders.cardinality().field(m.collectField)
                    .precisionThreshold(Math.min(Optional.ofNullable(m.precisionThreshold).orElse(3000), elasticMaxPrecisionThreshold))
                    .build()._toAggregation();
            case MAX -> AggregationBuilders.max().field(m.collectField).build()._toAggregation();
            case MIN -> AggregationBuilders.min().field(m.collectField).build()._toAggregation();
            case SUM -> AggregationBuilders.sum().field(m.collectField).build()._toAggregation();
            case GEOCENTROID -> buildSpatialAggregation(m, aggregationModel, AggregatedGeometryEnum.CENTROID, AggregationBuilders.geoCentroid());
            case GEOBBOX -> buildSpatialAggregation(m, aggregationModel, AggregatedGeometryEnum.BBOX, AggregationBuilders.geoBounds());
        };
    }

    private <T>co.elastic.clients.elasticsearch._types.aggregations.Aggregation buildSpatialAggregation(Metric m, Aggregation aggregationModel,
                                                  AggregatedGeometryEnum geometryType,
                                                  ObjectBuilder<T> builder) throws ArlasException {
        setGeoMetricAggregationCollectField(m);
        boolean skip = aggregationModel.aggregatedGeometries != null
                && aggregationModel.aggregatedGeometries.contains(geometryType)
                && aggregationModel.field.equals(m.collectField);
        co.elastic.clients.elasticsearch._types.aggregations.Aggregation agg = null;
        if (builder instanceof GeoCentroidAggregation.Builder b)
            agg = b.field(m.collectField).build()._toAggregation();
        else if (builder instanceof GeoBoundsAggregation.Builder b)
            agg = b.field(m.collectField).build()._toAggregation();
        return skip ? null : agg;
    }

    private boolean isSpatial(CollectionFunction collectFct) {
        return collectFct == CollectionFunction.GEOBBOX || collectFct == CollectionFunction.GEOCENTROID;
    }

    private <T> void setSizeAggregation(Aggregation aggregationModel, ObjectBuilder<T> aggregationBuilder) throws ArlasException {
        if (aggregationModel.size != null) {
            Integer s = ParamsParser.getValidAggregationSize(aggregationModel.size);
            if (aggregationBuilder instanceof TermsAggregation.Builder  builder)
                builder.size(s);
            else if (Boolean.TRUE.equals(isGeoAgg(aggregationBuilder)))
                throw new NotImplementedException(SIZE_NOT_IMPLEMENTED);
            else
                throw new BadRequestException(NO_SIZE_TO_SPECIFY);
        }
    }

    private void setAggregatedGeometries(Aggregation aggregationModel, Builder.ContainerBuilder containerBuilder) {
        if (aggregationModel.aggregatedGeometries != null) {
            String aggregationGeoField = GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type) ? aggregationModel.field : collectionReference.params.centroidPath;
            aggregationModel.aggregatedGeometries.forEach(ag -> {
                if (Objects.requireNonNull(ag) == AggregatedGeometryEnum.BBOX) {
                    GeoBoundsAggregation metricAggregation = AggregationBuilders.geoBounds().field(aggregationGeoField).build();
                    containerBuilder.aggregations(AggregatedGeometryEnum.BBOX.value() + AGGREGATED_GEOMETRY_SUFFIX, metricAggregation._toAggregation());
                } else if (ag == AggregatedGeometryEnum.CENTROID) {
                    GeoCentroidAggregation metricAggregation = AggregationBuilders.geoCentroid().field(aggregationGeoField).build();
                    containerBuilder.aggregations(AggregatedGeometryEnum.CENTROID.value() + AGGREGATED_GEOMETRY_SUFFIX, metricAggregation._toAggregation());
                }
            });
        }
    }

    private void setRawGeometriesAndFetch(Aggregation aggregationModel, Builder.ContainerBuilder containerBuilder) throws ArlasException {
        if (aggregationModel.rawGeometries != null && aggregationModel.fetchHits != null) {
             handleRawGeometriesWithFetch(aggregationModel, containerBuilder);
        }
        if (aggregationModel.rawGeometries != null) {
             setRawGeometries(aggregationModel, containerBuilder);
        }
        if (aggregationModel.fetchHits != null) {
             setHitsToFetch(aggregationModel, containerBuilder);
        }
    }

    private Builder.ContainerBuilder handleRawGeometriesWithFetch(Aggregation aggregationModel, Builder.ContainerBuilder containerBuilder) throws ArlasException {
        int fetchSize = Optional.ofNullable(aggregationModel.fetchHits.size).orElse(1);
        List<String> signedFetchIncludes = getSignedFetchIncludes(aggregationModel.fetchHits.include);
        List<RawGeometry> mergeableRS = new ArrayList<>();
        for (RawGeometry rg : aggregationModel.rawGeometries) {
            if (hasSameSignedSort(rg.sort, signedFetchIncludes)) {
                rg.setSignedSort(String.join(",", signedFetchIncludes));
                rg.setInclude(aggregationModel.fetchHits.include);
                mergeableRS.add(rg);
            }
        }

        if (mergeableRS.isEmpty() || fetchSize != 1) {
            setRawGeometries(aggregationModel,containerBuilder);
            setHitsToFetch(aggregationModel,containerBuilder);
            return containerBuilder;
        }

        return mergeIntoTopHits(containerBuilder, mergeableRS);
    }

    private List<String> getSignedFetchIncludes(List<String> includes) throws ArlasException {
        if (includes == null) return Collections.emptyList();
        List<String> signedIncludes = new ArrayList<>();
        for (String field : includes) {
            String unsignedField = field.replaceFirst(ORDER_SIGN_REGEX, "");
            CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
            if (field.startsWith("+") || field.startsWith("-")) {
                signedIncludes.add(field);
            }
        }
        return signedIncludes;
    }
    private boolean hasSameSignedSort(String sort, List<String> signedIncludes) {
        List<String> signedSortFields = Arrays.stream(sort.split(","))
                .filter(f -> f.startsWith("+") || f.startsWith("-"))
                .toList();
        return String.join(",", signedSortFields).equals(String.join(",", signedIncludes));
    }

    private Builder.ContainerBuilder mergeIntoTopHits(Builder.ContainerBuilder containerBuilder, List<RawGeometry> mergeableRS) throws ArlasException {
        Map<String, Set<String>> rgs = new HashMap<>();
        for (RawGeometry rg : mergeableRS) {
            rgs.computeIfAbsent(rg.signedSort, k -> new HashSet<>()).addAll(rg.include);
            rgs.get(rg.signedSort).add(rg.geometry);
        }
        for (Map.Entry<String, Set<String>> entry : rgs.entrySet()) {
            String sort = entry.getKey();
            String[] includes = entry.getValue().toArray(String[]::new);
            TopHitsAggregation.Builder topHitsBuilder = AggregationBuilders.topHits().size(1)
                    .source(s -> s.filter(f -> f.includes(Arrays.asList(includes))));
            for (String field : sort.split(",")) {
                String unsignedField = field.replaceFirst(ORDER_SIGN_REGEX, "");
                CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                if(field.startsWith("+") || field.startsWith("-")) {
                    SortOrder order = field.startsWith("+") ? SortOrder.Asc : SortOrder.Desc;
                    topHitsBuilder.sort(s -> s.field(FieldSort.of(f -> f.field(unsignedField).order(order))));
                }
            }
            containerBuilder.aggregations(RAW_GEOMETRY_SUFFIX + FETCH_HITS_AGG + sort, topHitsBuilder.build()._toAggregation());
        }
        return containerBuilder;
    }
    private void setRawGeometries(Aggregation aggregationModel, Builder.ContainerBuilder containerBuilder) throws ArlasException {
            Map<String, Set<String>> rgs = new HashMap<>();
            aggregationModel.rawGeometries.forEach(rg -> {
                Set<String> geos = rgs.get(rg.sort);
                if (geos == null) geos = new HashSet<>();
                geos.add(rg.geometry);
                rgs.put(rg.sort, geos);
            });
            for (Map.Entry<String,Set<String>> entry : rgs.entrySet()) {
                String sort = entry.getKey();
                String[] includes = rgs.get(sort).toArray(String[]::new);
                TopHitsAggregation.Builder topHitsAggregationBuilder = AggregationBuilders.topHits().size(1)
                        .source(builder -> builder.filter(builder1 -> builder1.includes(Arrays.stream(includes).toList())));
                for (String field : sort.split(",")) {
                    String unsignedField = field.replaceFirst(ORDER_SIGN_REGEX, "");
                    CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                    CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                    if (field.startsWith("+")) {
                        CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                        topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Asc))));
                    } else if(field.startsWith("-")) {
                        CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                        topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Desc))));
                    }
                }
                containerBuilder
                        .aggregations(RAW_GEOMETRY_SUFFIX + sort,topHitsAggregationBuilder.build()._toAggregation());
            }
    }

    private void setHitsToFetch(Aggregation aggregationModel, Builder.ContainerBuilder containerBuilder) throws ArlasException {
            TopHitsAggregation.Builder topHitsAggregationBuilder = AggregationBuilders.topHits();
            Integer size = Optional.ofNullable(aggregationModel.fetchHits.size).orElse(1);
            topHitsAggregationBuilder.size(size);
            List<String> includes = new ArrayList<>();
            if (aggregationModel.fetchHits.include != null) {
                for (String field : aggregationModel.fetchHits.include) {
                    String unsignedField = field.replaceFirst(ORDER_SIGN_REGEX, "");
                    CollectionUtil.checkAliasMappingFields(client.getMappings(collectionReference.params.indexName), unsignedField);
                    includes.add(unsignedField);
                    /** For geo-fields, we don't sort them. Sorting geo-fields need to be according a given point to calculate a geo-distance
                     * which is not supported in the syntax of fetch_hits*/
                    if (CollectionReferenceManager.getInstance().getType(collectionReference, unsignedField, false) != FieldType.GEO_POINT && CollectionReferenceManager.getInstance().getType(collectionReference, unsignedField, false) != FieldType.GEO_SHAPE) {
                        if (field.startsWith("+") ) {
                            topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Asc))));
                        } else if(field.startsWith("-")) {
                            topHitsAggregationBuilder.sort(builder -> builder.field(FieldSort.of(builder1 -> builder1.field(unsignedField).order(SortOrder.Desc))));
                        }
                    }
                }
                String[] hitsToInclude = includes.toArray(new String[0]);
                topHitsAggregationBuilder.source(builder -> builder.filter(builder1 -> builder1.includes(Arrays.stream(hitsToInclude).toList())));
            }
            containerBuilder.aggregations(FETCH_HITS_AGG,topHitsAggregationBuilder.build()._toAggregation());
    }

    private void setGeoMetricAggregationCollectField(Metric metric) throws ArlasException {
        FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, metric.collectField, true);
        if (fieldType != FieldType.GEO_POINT) {
            throw new InvalidParameterException("collect_field: `" + metric.collectField + "` is not a geo-point field. " + "`" + metric.collectFct.name() + "` is applied to geo-points only.");
        }
    }

    private <T>void setOrder(Aggregation aggregationModel, ObjectBuilder<T> aggregationBuilder, String metricAggregation) throws ArlasException {
        Order order = aggregationModel.order;
        OrderOn on = aggregationModel.on;
        boolean isGeo = Boolean.TRUE.equals(isGeoAgg(aggregationBuilder));
        if (order != null && on != null) {
            if (isGeo) {
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            }
            NamedValue<SortOrder> bucketOrder = getSortOrderNamedValue(metricAggregation, order, on);
            orderIfSupported(aggregationBuilder, bucketOrder);
        } else if (order != null || on != null) {
            if (isGeo) {
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            }
            if (order == null) {
                throw new BadRequestException(ORDER_NOT_SPECIFIED);
            } else {
                throw new BadRequestException(ON_NOT_SPECIFIED);
            }
        }
    }

    private static <T> void orderIfSupported(ObjectBuilder<T> aggregationBuilder, NamedValue<SortOrder> bucketOrder) throws NotAllowedException {
        if (aggregationBuilder instanceof DateHistogramAggregation.Builder builder ) {
            builder.order(bucketOrder);
        } else if (aggregationBuilder instanceof HistogramAggregation.Builder builder) {
            builder.order(bucketOrder);
        } else if (aggregationBuilder instanceof TermsAggregation.Builder builder) {
            builder.order(bucketOrder);
        } else {
            throw new NotAllowedException(NO_ORDER_ON_TO_SPECIFY);
        }
    }

    private static NamedValue<SortOrder> getSortOrderNamedValue(String metricAggregation, Order order, OrderOn on) throws BadRequestException {
        SortOrder sort = SortOrder.Asc;
        if(order.equals(Order.desc)){
            sort= SortOrder.Desc;
        }
        NamedValue<SortOrder> bucketOrder = null;
        if (on.equals(OrderOn.field)) {
            bucketOrder = NamedValue.of("_key",sort);
        } else if (on.equals(OrderOn.count)) {
            bucketOrder = NamedValue.of("_count",sort);
        } else if (on.equals(OrderOn.result)) {
            if (metricAggregation != null) {
                // ORDER ON RESULT IS NOT ALLOWED ON COORDINATES (CENTROID) OR BOUNDING BOX
                if (!metricAggregation.split(":")[0].equalsIgnoreCase(CollectionFunction.GEOBBOX.name())
                        && !metricAggregation.split(":")[0].equalsIgnoreCase(CollectionFunction.GEOCENTROID.name())) {
                    bucketOrder = NamedValue.of(metricAggregation,sort);
                } else {
                    throw new BadRequestException(ORDER_ON_GEO_RESULT_NOT_ALLOWED);
                }
            } else {
                throw new BadRequestException(ORDER_ON_RESULT_NOT_ALLOWED);
            }
        }
        return bucketOrder;
    }

    private JSONObject createPolygon(Polygon geomPolygon) {
        Coordinate[] exteriorRing = geomPolygon.getExteriorRing().getCoordinates();
        // TODO : deal with interior ring too
        JSONObject polygon = new JSONObject();
        JSONArray jsonArrayExt = new JSONArray();
        Arrays.asList(exteriorRing).forEach(coordinate -> {
            JSONArray jsonArayLngLat = new JSONArray();
            jsonArayLngLat.add(0, coordinate.x);
            jsonArayLngLat.add(1, coordinate.y);
            jsonArrayExt.add(jsonArayLngLat);
        });
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonArrayExt);
        polygon.put("type", POLYGON);
        polygon.put(COORDINATES, jsonArray);
        setOrientation(polygon);
        return polygon;
    }

    private JSONObject createPolygonFromBbox(double[] bbox) {

        double west = bbox[0];
        double south = bbox[1];
        double east = bbox[2];
        double north = bbox[3];

        JSONObject polygon = new JSONObject();
        JSONArray jsonArrayCoord = new JSONArray();
        JSONArray jsonArrayBottomLeft = new JSONArray();
        JSONArray jsonArrayUpRight = new JSONArray();

        jsonArrayBottomLeft.add(0,west);
        jsonArrayBottomLeft.add(1,north);

        jsonArrayUpRight.add(0,east);
        jsonArrayUpRight.add(1,south);


        jsonArrayCoord.add(jsonArrayBottomLeft);
        jsonArrayCoord.add(jsonArrayUpRight);

        polygon.put("type", "envelope");
        polygon.put(COORDINATES, jsonArrayCoord);
        return polygon;
    }

    private JSONObject createMultiPolygon(MultiPolygon geomMultiPolygon ) {

        int nPolygon = geomMultiPolygon.getNumGeometries();
        JSONObject multiPolygon = new JSONObject();
        JSONArray coordinates = new JSONArray();
        for (int i = 0; i < nPolygon ; i++) {
            Polygon geomPolygon = (Polygon) geomMultiPolygon.getGeometryN(i);
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
        multiPolygon.put("type", MULTI_POLYGON);
        multiPolygon.put(COORDINATES, coordinates);
        setOrientation(multiPolygon);
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
        lineString.put(COORDINATES, jsonAray);
        return lineString;
    }

    private JSONObject createPoint(Point geomPoint) {
        JSONObject point = new JSONObject();
        JSONArray jsonAray = new JSONArray();
        jsonAray.add(0, geomPoint.getCoordinate().x);
        jsonAray.add(1, geomPoint.getCoordinate().y);
        point.put("type", "Point");
        point.put(COORDINATES, jsonAray);
        return point;
    }

    private JSONObject createMultiPoint(MultiPoint geomMultiPoint) {
        String wkt = geomMultiPoint.toText();
        // remove parenthesis
        String pointsString = wkt.substring(wkt.indexOf('(')).replaceAll("[()]", "");
        String[] pointPairs = pointsString.split(",");
        JSONArray coordinates = new JSONArray();
        for (String point : pointPairs) {
            String[] coords = point.trim().split("\\s+");
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            JSONArray coord = new JSONArray();
            coord.add(x);
            coord.add(y);
            coordinates.add(coord);
        }
        JSONObject multiPoint = new JSONObject();
        multiPoint.put("type", "MultiPoint");
        multiPoint.put(COORDINATES, coordinates);
        return multiPoint;
    }

    private JSONObject createMultiLine(MultiLineString geomMultiLine) {
        String wkt = geomMultiLine.toText();
        // Get between first parenthesis
        String inner = wkt.substring(wkt.indexOf('('));
        // Remove double parenthesis
        inner = inner.replaceAll("^\\(\\(", "").replaceAll("\\)\\)$", "");
        // Get lines
        String[] lineStrings = inner.split("\\)\\s*,\\s*\\(");
        JSONArray multiLineCoordinates = new JSONArray();
        for (String line : lineStrings) {
            String[] points = line.trim().split(",");
            JSONArray lineArray = new JSONArray();
            for (String point : points) {
                String[] coords = point.trim().split("\\s+");
                JSONArray coord = new JSONArray();
                coord.add(Double.parseDouble(coords[0]));
                coord.add(Double.parseDouble(coords[1]));
                lineArray.add(coord);
            }
            multiLineCoordinates.add(lineArray);
        }
        JSONObject multiLine = new JSONObject();
        multiLine.put("type", "MultiLineString");
        multiLine.put(COORDINATES, multiLineCoordinates);

        return multiLine;
    }

    private JSONObject createGeometryCollection(GeometryCollection geometryCollection) throws ArlasException {
        String wkt = geometryCollection.toText();
        String inner = wkt.substring(wkt.indexOf('(') + 1, wkt.lastIndexOf(')')).trim();
        List<String> geometries = splitGeometries(inner);
        JSONArray geometriesArray = new JSONArray();
        for (String geometry : geometries) {
            geometriesArray.add(parseGeometry(geometry.trim()));
        }
        JSONObject geoCollection = new JSONObject();
        geoCollection.put("type", "GeometryCollection");
        geoCollection.put("geometries", geometriesArray);
        return geoCollection;
    }

    private static List<String> splitGeometries(String wktPart) {
        List<String> result = new ArrayList<>();
        int level = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < wktPart.length(); i++) {
            char c = wktPart.charAt(i);
            if (c == '(') level++;
            if (c == ')') level--;
            if (c == ',' && level == 0) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private JSONObject parseGeometry(String wkt) throws ArlasException {
        Geometry wktGeometry = GeoUtil.readWKT(wkt);
        String type = wkt.substring(0, wkt.indexOf('(')).trim().toUpperCase();
        return switch (type) {
            case "POLYGON" -> createPolygon((Polygon) wktGeometry);
            case "MULTIPOLYGON" -> createMultiPolygon((MultiPolygon) wktGeometry);
            case "LINESTRING" -> createLineString((LineString) wktGeometry);
            case "POINT" -> createPoint((Point) wktGeometry);
            case "MULTIPOINT" -> createMultiPoint((MultiPoint) wktGeometry);
            case "MULTILINESTRING" -> createMultiLine((MultiLineString) wktGeometry);
            default -> throw new InvalidParameterException("The given geometry is not handled.");
        };
    }
    private JSONObject getShapeObject(String geometry) throws ArlasException {
        // test if geometry is 'west,south,east,north' or wkt string
        if (CheckParams.isBboxMatch(geometry)) {
            return createPolygonFromBbox(CheckParams.toDoubles(geometry));
        } else {
            Geometry wktGeometry = GeoUtil.readWKT(geometry);
            return getShapeObject(wktGeometry);
        }
    }

    private JSONObject getShapeObject(Geometry wktGeometry) throws ArlasException {
        if (wktGeometry != null) {
                String geometryType = wktGeometry.getGeometryType().toUpperCase();
                return switch (geometryType) {
                    case "POLYGON" -> createPolygon((Polygon) wktGeometry);
                    case "MULTIPOLYGON" -> createMultiPolygon((MultiPolygon) wktGeometry);
                    case "LINESTRING" -> createLineString((LineString) wktGeometry);
                    case "POINT" -> createPoint((Point) wktGeometry);
                    case "MULTIPOINT" -> createMultiPoint((MultiPoint) wktGeometry);
                    case "MULTILINESTRING" -> createMultiLine((MultiLineString) wktGeometry);
                    case "GEOMETRYCOLLECTION" -> createGeometryCollection((GeometryCollection) wktGeometry);
                    default -> throw new InvalidParameterException("The given geometry is not handled.");
                };
            }
            throw new InvalidParameterException("The given geometry is invalid.");
    }

    private JSONObject setOrientation(JSONObject jsonObject){
            jsonObject.put("orientation","RIGHT");
        return jsonObject;
    }

    private <T> Boolean isGeoAgg(T aggregationBuilder){
        return aggregationBuilder instanceof GeoHashGridAggregation.Builder ||
                aggregationBuilder instanceof GeoTileGridAggregation.Builder ||
                aggregationBuilder instanceof GeohexGridAggregation.Builder;
    }
}