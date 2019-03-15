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

package io.arlas.server.core;


import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.utils.ElasticTool;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import io.arlas.server.exceptions.*;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.*;
import io.arlas.server.model.request.Aggregation;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.Metric;
import io.arlas.server.model.request.MultiValueFilter;
import io.arlas.server.model.response.TimestampType;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.ParamsParser;
import io.arlas.server.utils.StringUtil;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.*;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class FluidSearch {

    public static final String INVALID_FILTER = "Invalid filter parameter.";
    public static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    public static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'eq', gte', 'gt', 'lte', 'lt', 'like' or 'ne'. ";
    public static final String INVALID_Q_FILTER = "Invalid parameter. Please specify the text to search directly or '{fieldname}:{text to search}'. ";
    public static final String INVALID_WKT = "Invalid WKT geometry.";
    public static final String INVALID_WKT_RANGE = "Invalid WKT geometry.Coordinate out of range";
    public static final String INVALID_BBOX = "Invalid BBOX";
    public static final String INVALID_SIZE = "Invalid size parameter.";
    public static final String INVALID_FROM = "Invalid from parameter: should be a positive integer.";
    public static final String INVALID_DATE_UNIT = "Invalid date unit.";
    public static final String INVALID_GEOSORT_LAT_LON = "'lat lon' must be numeric values separated by a space";
    public static final String INVALID_GEOSORT_LABEL = "To sort by geo_distance, please specifiy the point, from which the distances are calculated, as following 'geodistance:lat lon'";
    public static final String INVALID_TIMESTAMP_RANGE = "Timestamp range values must be a timestamp in millisecond or a date expression. Otherwise, please set the `dateformat` parameter if your date value has a custom format";

    public static final String DATEHISTOGRAM_AGG = "Datehistogram aggregation";
    public static final String HISTOGRAM_AGG = "Histogram aggregation";
    public static final String TERM_AGG = "Term aggregation";
    public static final String GEOHASH_AGG = "Geohash aggregation";
    public static final String FETCH_HITS_AGG = "fetched_hits";
    public static final String GEOHASH_AGG_WITH_GEOASH_STRATEGY = "Geohash aggregation with geoash strategy";
    public static final String GEO_DISTANCE = "geodistance";
    public static final String NOT_ALLOWED_AS_MAIN_AGGREGATION_TYPE = " aggregation type is not allowed as main aggregation. Please make sure that geohash or term is the main aggregation or use '_aggregate' service instead.";
    public static final String NO_INCLUDE_TO_SPECIFY = "'include-' should not be specified for this aggregation";
    public static final String NO_FORMAT_TO_SPECIFY = "'format-' should not be specified for this aggregation.";
    public static final String NO_SIZE_TO_SPECIFY = "'size-' should not be specified for this aggregation.";
    public static final String NO_ORDER_ON_TO_SPECIFY = "'order-' and 'on-' should not be specified for this aggregation.";
    public static final String COLLECT_FCT_NOT_SPECIFIED = "The aggregation function 'collect_fct' is not specified.";
    public static final String COLLECT_FIELD_NOT_SPECIFIED = "The aggregation field 'collect_field' is not specified.";
    public static final String BAD_COLLECT_FIELD_FOR_GEO_METRICS = "For GeoBBOX and GeoCentroid, 'collect_field' should be the centroid path";
    public static final String NOT_SUPPORTED_BBOX_INTERSECTION = "Unsupported case : pwithin bbox intersects the tile/geohash twice.";
    public static final String ORDER_NOT_SPECIFIED = "'order-' is not specified.";
    public static final String ON_NOT_SPECIFIED = "'on-' is not specified.";
    public static final String ORDER_PARAM_NOT_ALLOWED = "Order is not allowed for geohash aggregation.";
    public static final String ORDER_ON_RESULT_NOT_ALLOWED = "'on-result' sorts 'collect_field' and 'collect_fct' results. Please specify 'collect_field' and 'collect_fct'.";
    public static final String ORDER_ON_GEO_RESULT_NOT_ALLOWED = "Ordering on 'result' is not allowed for geo-box neither geo-centroid metric aggregation. ";
    public static final String SIZE_NOT_IMPLEMENTED = "Size is not implemented for geohash.";
    public static final String RANGE_ALIASES_CHARACTER = "$";
    public static final String TIMESTAMP_ALIAS = "timestamp";

    public static final String FIELD_MIN_VALUE = "field_min_value";
    public static final String FIELD_MAX_VALUE = "field_max_value";

    public static final String RANDOM_GEOMETRY = "random_geometry";
    public static final String FIRST_GEOMETRY = "first_geometry";
    public static final String LAST_GEOMETRY = "last_geometry";

    private static Logger LOGGER = LoggerFactory.getLogger(FluidSearch.class);

    private Client client;
    private SearchRequestBuilder searchRequestBuilder;
    private BoolQueryBuilder boolQueryBuilder;
    private CollectionReference collectionReference;

    private List<String> include = new ArrayList<>();
    private List<String> exclude = new ArrayList<>();

    public FluidSearch(Client client) {
        this.client = client;
        boolQueryBuilder = QueryBuilders.boolQuery();
    }

    protected Client getClient(){return client;}

    public BoolQueryBuilder getBoolQueryBuilder() {
        return boolQueryBuilder;
    }

    public SearchResponse exec() throws ArlasException {
        searchRequestBuilder.setQuery(boolQueryBuilder);

        if (collectionReference.params.excludeFields != null && !collectionReference.params.excludeFields.isEmpty()) {
            if (exclude.isEmpty()) {
                exclude(collectionReference.params.excludeFields);
            } else {
                Set<String> excludeSet = new HashSet<>();
                excludeSet.addAll(exclude);
                excludeSet.addAll(Arrays.asList(collectionReference.params.excludeFields.split(",")));
                exclude = new ArrayList<>(excludeSet);
            }
        }
        List<String> includeFieldList = new ArrayList<>();
        if (!include.isEmpty()) {
            for (String includeField : include) {
                includeFieldList.add(includeField);
            }
            for (String path : getCollectionPaths()) {
                boolean alreadyIncluded = false;
                for (String includeField : include) {
                    if (includeField.equals("*") || path.startsWith(includeField)) {
                        alreadyIncluded = true;
                    }
                }
                if (!alreadyIncluded) {
                    includeFieldList.add(path);
                }
            }
        }
        String[] includeFields = includeFieldList.toArray(new String[includeFieldList.size()]);
        if (includeFields.length == 0) {
            includeFields = new String[]{"*"};
        }
        String[] excludeFields = exclude.toArray(new String[exclude.size()]);
        if (excludeFields.length == 0) {
            excludeFields = null;
        }
        searchRequestBuilder = searchRequestBuilder.setFetchSource(includeFields, excludeFields);

        //Get Elasticsearch response
        LOGGER.debug("QUERY : " + searchRequestBuilder.toString());
        SearchResponse result = null;
        result = searchRequestBuilder.get();
        return result;
    }

    public String getCountDistinctKey(String field) {
        return "distinct-" + field + "-values";
    }

    public FluidSearch filter(MultiValueFilter<Expression> f, String dateFormat) throws ArlasException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (Expression fFilter : f) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(filter(fFilter, dateFormat));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return this;
    }

    private BoolQueryBuilder filter(Expression expression, String dateFormat) throws ArlasException {
        BoolQueryBuilder ret = QueryBuilders.boolQuery();
        if (Strings.isNullOrEmpty(expression.field) || expression.op == null || Strings.isNullOrEmpty(expression.value)) {
            throw new InvalidParameterException(INVALID_PARAMETER_F);
        }
        String field = expression.field;
        OperatorEnum op = expression.op;
        String value = expression.value;
        String fieldValues[] = value.split(",");
        switch (op) {
            case eq:
                if (fieldValues.length > 1) {
                    BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
                    for (String valueInValues : fieldValues) {
                        orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery(field, valueInValues));
                    }
                    ret = ret.filter(orBoolQueryBuilder);
                } else {
                    ret = ret.filter(QueryBuilders.matchQuery(field, value));
                }
                break;
            case gte:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQueryBuilder gteRangeQuery = QueryBuilders.rangeQuery(field).gte(value);
                applyFormatOnRangeQuery(field, value, gteRangeQuery);
                ret = ret.filter(gteRangeQuery);
                break;
            case gt:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQueryBuilder gtRangeQuery = QueryBuilders.rangeQuery(field).gt(value);
                applyFormatOnRangeQuery(field, value, gtRangeQuery);
                ret = ret.filter(gtRangeQuery);
                break;
            case lte:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQueryBuilder lteRangeQuery = QueryBuilders.rangeQuery(field).lte(value);
                applyFormatOnRangeQuery(field, value, lteRangeQuery);
                ret = ret.filter(lteRangeQuery);
                break;
            case lt:
                if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(value, dateFormat);
                }
                RangeQueryBuilder ltRangeQuery = QueryBuilders.rangeQuery(field).lt(value);
                applyFormatOnRangeQuery(field, value, ltRangeQuery);
                ret = ret.filter(ltRangeQuery);
                break;
            case like:
                //TODO: if field type is fullText, use matchPhraseQuery instead of regexQuery
                ret = ret
                        .filter(QueryBuilders.regexpQuery(field, ".*" + value + ".*"));
                break;
            case ne:
                for (String valueInValues : fieldValues) {
                    ret = ret
                            .mustNot(QueryBuilders.matchQuery(field, valueInValues));
                }
                break;
            case range:
                field = ParamsParser.getFieldFromFieldAliases(field, collectionReference);
                if (fieldValues.length > 1) {
                    BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
                    for (String valueInValues : fieldValues) {
                        CheckParams.checkRangeValidity(valueInValues);
                        orBoolQueryBuilder = orBoolQueryBuilder.should(getRangeQueryBuilder(field, valueInValues, dateFormat));
                    }
                    ret = ret.filter(orBoolQueryBuilder);
                } else {
                    CheckParams.checkRangeValidity(value);
                    ret = ret.filter(getRangeQueryBuilder(field, value, dateFormat));
                }
                break;
            default:
                throw new InvalidParameterException(INVALID_OPERATOR);
        }
        return ret;
    }

    public void applyFormatOnRangeQuery(String field, String value, RangeQueryBuilder rangeQuery) throws ArlasException {
        if (field.equals(collectionReference.params.timestampPath)) {
            CheckParams.checkTimestampFormatValidity(value);
            rangeQuery = rangeQuery.format(TimestampType.epoch_millis.name());
        }
    }

    protected RangeQueryBuilder getRangeQueryBuilder(String field, String value, String dateFormat) throws ArlasException {
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
        RangeQueryBuilder ret = QueryBuilders.rangeQuery(field);
        if (incMin) {
            ret = ret.gte(min);
        } else {
            ret = ret.gt(min);
        }
        if (incMax) {
            ret = ret.lte(max);
        } else {
            ret = ret.lt(max);
        }
        if (field.equals(collectionReference.params.timestampPath)) {
            ret = ret.format(TimestampType.epoch_millis.name());
        }
        return ret;

    }

    public FluidSearch filterQ(MultiValueFilter<String> q) throws ArlasException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String qFilter : q) {
            String operands[] = qFilter.split(":",2);
            if (operands.length == 2) {
                orBoolQueryBuilder = orBoolQueryBuilder
                        .should((QueryBuilders.simpleQueryStringQuery(operands[1]).defaultOperator(Operator.AND).field(operands[0])));
            } else if (operands.length == 1) {
                orBoolQueryBuilder = orBoolQueryBuilder
                        .should((QueryBuilders.simpleQueryStringQuery(operands[0]).defaultOperator(Operator.AND)));
            } else {
                throw new InvalidParameterException(INVALID_Q_FILTER);
            }
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return this;
    }

    public FluidSearch filterPWithin(MultiValueFilter<String> pwithin) throws IOException, ArlasException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String pwithinFilter : pwithin) {
            double[] tlbr = CheckParams.toDoubles(pwithinFilter);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(filterPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return this;
    }

    private GeoBoundingBoxQueryBuilder filterPWithin(double west, double south, double east, double north)
            throws ArlasException, IOException {
        GeoPoint topLeft = new GeoPoint(north, west);
        GeoPoint bottomRight = new GeoPoint(south, east);
        return QueryBuilders
                .geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(topLeft, bottomRight);
    }

    public FluidSearch filterNotPWithin(MultiValueFilter<String> notpwithin) throws IOException, ArlasException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String notpwithinFilter : notpwithin) {
            double[] tlbr = CheckParams.toDoubles(notpwithinFilter);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(filterNotPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(notpwithin.size());
        boolQueryBuilder = boolQueryBuilder.mustNot(orBoolQueryBuilder);
        return this;
    }

    private GeoBoundingBoxQueryBuilder filterNotPWithin(double west, double south, double east, double north)
            throws ArlasException, IOException {
        GeoPoint topLeft = new GeoPoint(north, west);
        GeoPoint bottomRight = new GeoPoint(south, east);
        return QueryBuilders
                .geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(topLeft, bottomRight);
    }

    public FluidSearch filterGWithin(MultiValueFilter<String> gwithin) throws ArlasException, IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String geometry : gwithin) {
            ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, shapeBuilder));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return this;
    }

    public FluidSearch filterNotGWithin(MultiValueFilter<String> notgwithin) throws ArlasException, IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String geometry : notgwithin) {
            ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, shapeBuilder));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(notgwithin.size());
        boolQueryBuilder = boolQueryBuilder.mustNot(orBoolQueryBuilder);
        return this;
    }

    public FluidSearch filterGIntersect(MultiValueFilter<String> gintersect) throws ArlasException, IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String geometry : gintersect) {
            ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoIntersectionQuery(collectionReference.params.geometryPath, shapeBuilder));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return this;
    }

    public FluidSearch filterNotGIntersect(MultiValueFilter<String> notgintersect) throws ArlasException, IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (String geometry : notgintersect) {
            ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoIntersectionQuery(collectionReference.params.geometryPath, shapeBuilder));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(notgintersect.size());
        boolQueryBuilder = boolQueryBuilder.mustNot(orBoolQueryBuilder);
        return this;
    }

    public FluidSearch include(String include) {
        if (include != null) {
            String includeFieldArray[] = include.split(",");
            for (String field : includeFieldArray) {
                this.include.add(field);
            }
        }
        return this;
    }

    public FluidSearch exclude(String exclude) {
        if (exclude != null) {
            String excludeFieldArray[] = exclude.split(",");
            for (String field : excludeFieldArray) {
                this.exclude.add(field);
            }
        }
        return this;
    }

    public FluidSearch filterSize(Integer size, Integer from) {
        searchRequestBuilder = searchRequestBuilder.setSize(size).setFrom(from);
        return this;
    }


    public FluidSearch searchAfter(String after) {
        searchRequestBuilder = searchRequestBuilder.searchAfter(after.split(","));
        return this;
    }


    public FluidSearch sort(String sort) throws ArlasException {
        List<String> fieldList = Arrays.asList(sort.split(","));
        String field;
        SortOrder sortOrder;
        for (String signedField : fieldList) {
            if (!signedField.equals("")) {
                if (signedField.substring(0, 1).equals("-")) {
                    field = signedField.substring(1);
                    sortOrder = SortOrder.DESC;
                } else {
                    field = signedField;
                    sortOrder = SortOrder.ASC;
                }
                if (field.split(" ").length > 1) {
                    geoDistanceSort(field, sortOrder);
                } else {
                    searchRequestBuilder = searchRequestBuilder.addSort(field, sortOrder);
                }
            }
        }
        return this;
    }

    public FluidSearch countDistinct(String field) throws ArlasException {
        ValuesSourceAggregationBuilder countDistinctAggregationBuilder = AggregationBuilders.cardinality(getCountDistinctKey(field)).field(field);
        searchRequestBuilder = searchRequestBuilder.setSize(0).addAggregation(countDistinctAggregationBuilder);
        return this;
    }

    private void geoDistanceSort(String geoSort, SortOrder sortOrder) throws ArlasException {
        GeoPoint sortOnPoint = ParamsParser.getGeoSortParams(geoSort);
        String geoSortField = collectionReference.params.centroidPath;
        searchRequestBuilder = searchRequestBuilder.addSort(SortBuilders.geoDistanceSort(geoSortField, sortOnPoint.lat(), sortOnPoint.lon())
                .order(sortOrder).geoDistance(GeoDistance.PLANE));
    }

    private AggregationBuilder aggregateRecursive(List<Aggregation> aggregations, AggregationBuilder aggregationBuilder, Boolean isGeoAggregate, Integer counter) throws ArlasException {
        //check the agg syntax is correct
        Aggregation aggregationModel = aggregations.get(0);
        if (isGeoAggregate && counter == 0) {
            if (aggregationModel.type != AggregationTypeEnum.geohash && aggregationModel.fetchGeometry == null) {
                throw new NotAllowedException("'" + aggregationModel.type.name() +"' aggregation type is not allowed in _geoaggregate service if fetchGeometry strategy is not specified");
            }
        }
        switch (aggregationModel.type) {
            case datehistogram:
                aggregationBuilder = buildDateHistogramAggregation(aggregationModel);
                break;
            case geohash:
                aggregationBuilder = buildGeohashAggregation(aggregationModel);
                break;
            case histogram:
                aggregationBuilder = buildHistogramAggregation(aggregationModel);
                break;
            case term:
                aggregationBuilder = buildTermsAggregation(aggregationModel);
                break;
        }
        aggregations.remove(0);
        if (aggregations.size() == 0) {
            return aggregationBuilder;
        }
        counter++;
        return aggregationBuilder.subAggregation(aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, counter));
    }

    public FluidSearch aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException {
        AggregationBuilder aggregationBuilder = null;
        aggregationBuilder = aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, 0);
        searchRequestBuilder = searchRequestBuilder.setSize(0).addAggregation(aggregationBuilder);
        return this;
    }

    public FluidSearch getFieldRange(String field) {
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.existsQuery(field));
        MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min(FIELD_MIN_VALUE).field(field);
        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max(FIELD_MAX_VALUE).field(field);
        searchRequestBuilder = searchRequestBuilder.setSize(0).addAggregation(minAggregationBuilder).addAggregation(maxAggregationBuilder);
        return this;
    }

    private DateHistogramAggregationBuilder buildDateHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        if (Strings.isNullOrEmpty(aggregationModel.field)) {
            aggregationModel.field = collectionReference.params.timestampPath;
        }
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(DATEHISTOGRAM_AGG);
        if (aggregationModel.interval.unit.equals(UnitEnum.year)
                || aggregationModel.interval.unit.equals(UnitEnum.month)
                || aggregationModel.interval.unit.equals(UnitEnum.quarter)
                || aggregationModel.interval.unit.equals(UnitEnum.week)) {
            if ((Integer)aggregationModel.interval.value > 1)
                throw new NotAllowedException("The size must be equal to 1 for the unit " + aggregationModel.interval.unit + ".");
        }
        DateHistogramInterval intervalUnit = null;
        switch (aggregationModel.interval.unit) {
            case year:
                intervalUnit = DateHistogramInterval.YEAR;
                break;
            case quarter:
                intervalUnit = DateHistogramInterval.QUARTER;
                break;
            case month:
                intervalUnit = DateHistogramInterval.MONTH;
                break;
            case week:
                intervalUnit = DateHistogramInterval.WEEK;
                break;
            case day:
                intervalUnit = DateHistogramInterval.days((Integer)aggregationModel.interval.value);
                break;
            case hour:
                intervalUnit = DateHistogramInterval.hours((Integer)aggregationModel.interval.value);
                break;
            case minute:
                intervalUnit = DateHistogramInterval.minutes((Integer)aggregationModel.interval.value);
                break;
            case second:
                intervalUnit = DateHistogramInterval.seconds((Integer)aggregationModel.interval.value);
                break;
            default:
                throw new InvalidParameterException(INVALID_DATE_UNIT);
        }
        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(intervalUnit);
        //get the field, format, collect_field, collect_fct, order, on
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setAggregationParameters(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setHitsToFetch(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setAggeragatedGeometryStrategy(aggregationModel, dateHistogramAggregationBuilder);
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregationModel builder
    private GeoGridAggregationBuilder buildGeohashAggregation(Aggregation aggregationModel) throws ArlasException {
        String geohashAggName = Optional.ofNullable(aggregationModel.fetchGeometry).map(fg -> fg.strategy).filter(strategy -> strategy == AggregatedGeometryStrategyEnum.geohash)
                .map(s -> GEOHASH_AGG_WITH_GEOASH_STRATEGY).orElse(GEOHASH_AGG);
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid(geohashAggName);
        //get the precision
        Integer precision = (Integer)aggregationModel.interval.value;
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setAggeragatedGeometryStrategy(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setHitsToFetch(aggregationModel, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    // construct and returns the histogram aggregationModel builder
    private HistogramAggregationBuilder buildHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram(HISTOGRAM_AGG);
        histogramAggregationBuilder = histogramAggregationBuilder.interval((Double)aggregationModel.interval.value);
        //get the field, format, collect_field, collect_fct, order, on
        histogramAggregationBuilder = (HistogramAggregationBuilder) setAggregationParameters(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregationBuilder) setHitsToFetch(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregationBuilder) setAggeragatedGeometryStrategy(aggregationModel, histogramAggregationBuilder);
        return histogramAggregationBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private TermsAggregationBuilder buildTermsAggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(TERM_AGG);
        //get the field, format, collect_field, collect_fct, order, on
        termsAggregationBuilder = (TermsAggregationBuilder) setAggregationParameters(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregationBuilder) setAggeragatedGeometryStrategy(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregationBuilder) setHitsToFetch(aggregationModel, termsAggregationBuilder);
        if (aggregationModel.include != null && !aggregationModel.include.isEmpty()) {
            String[] includeList = aggregationModel.include.split(",");
            IncludeExclude includeExclude;
            if (includeList.length > 1) {
                includeExclude = new IncludeExclude(includeList, null);
            } else {
                includeExclude = new IncludeExclude(includeList[0], null);
            }
            termsAggregationBuilder = termsAggregationBuilder.includeExclude(includeExclude);
        }
        return termsAggregationBuilder;
    }

    private ValuesSourceAggregationBuilder setAggregationParameters(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException {
        String aggField = aggregationModel.field;
        aggregationBuilder = aggregationBuilder.field(aggField);
        //Get the format
        String format = ParamsParser.getValidAggregationFormat(aggregationModel.format);
        if (aggregationBuilder instanceof DateHistogramAggregationBuilder) {
            aggregationBuilder = aggregationBuilder.format(format);
        } else if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        // firstMetricAggregationBuilder is the aggregation builder on which the order aggregation will be applied
        ValuesSourceAggregationBuilder firstMetricAggregationBuilder = null;
        // collect_field must be a centroid for geocentroid and geobbox, so it's set automatically to collectionReference.params.centroidPath
        if (aggregationModel.metrics != null) {
            for (Metric m: aggregationModel.metrics) {
                ValuesSourceAggregationBuilder metricAggregationBuilder = null;
                if (m.collectField != null && m.collectFct == null) {
                    throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
                } else if (m.collectField == null && m.collectFct != null) {
                    throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
                }
                String collectField = m.collectField.replace(".", ArlasServerConfiguration.FLATTEN_CHAR);
                switch (m.collectFct) {
                    case AVG:
                        metricAggregationBuilder = AggregationBuilders.avg("avg:" + collectField).field(m.collectField);
                        break;
                    case CARDINALITY:
                        metricAggregationBuilder = AggregationBuilders.cardinality("cardinality:" + collectField).field(m.collectField);
                        break;
                    case MAX:
                        metricAggregationBuilder = AggregationBuilders.max("max:" + collectField).field(m.collectField);
                        break;
                    case MIN:
                        metricAggregationBuilder = AggregationBuilders.min("min:" + collectField).field(m.collectField);
                        break;
                    case SUM:
                        metricAggregationBuilder = AggregationBuilders.sum("sum:" + collectField).field(m.collectField);
                        break;
                    case GEOCENTROID:
                        setGeoMetricAggregationCollectField(m);
                        // This suffix will be used in the AggregationResponse construction in order to distinguish the case when the centroid
                        // should be provided as metrics only and when it should be the geometry of the geoagragation
                        String centroidSuffix = ":" + collectionReference.params.centroidPath.replace(".", ArlasServerConfiguration.FLATTEN_CHAR);
                        if (aggregationModel.fetchGeometry != null && aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.centroid) {
                            centroidSuffix = "-bucket";
                        }
                        metricAggregationBuilder = AggregationBuilders.geoCentroid(CollectionFunction.GEOCENTROID.name().toLowerCase() + centroidSuffix).field(m.collectField);
                        break;
                    case GEOBBOX:
                        setGeoMetricAggregationCollectField(m);
                        String bboxSuffix = ":" + collectionReference.params.centroidPath.replace(".", ArlasServerConfiguration.FLATTEN_CHAR);
                        if (aggregationModel.fetchGeometry != null && aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.bbox) {
                            bboxSuffix = "-bucket";
                        }
                        metricAggregationBuilder = AggregationBuilders.geoBounds(CollectionFunction.GEOBBOX.name().toLowerCase() + bboxSuffix).field(m.collectField);
                        break;
                }
                aggregationBuilder.subAggregation(metricAggregationBuilder);

                // Getting the first metric aggregation builder that is different from GEOBBOX and GEOCENTROID, on which the order will be applied
                if (firstMetricAggregationBuilder == null && m.collectFct != CollectionFunction.GEOBBOX &&  m.collectFct != CollectionFunction.GEOCENTROID) {
                    firstMetricAggregationBuilder = metricAggregationBuilder;
                }
            }
        }

        if (aggregationModel.size != null) {
            Integer s = ParamsParser.getValidAggregationSize(aggregationModel.size);
            if (aggregationBuilder instanceof TermsAggregationBuilder)
                aggregationBuilder = ((TermsAggregationBuilder) aggregationBuilder).size(s);
            else if (aggregationBuilder instanceof GeoGridAggregationBuilder)
                throw new NotImplementedException(SIZE_NOT_IMPLEMENTED);
            else
                throw new BadRequestException(NO_SIZE_TO_SPECIFY);
        }

        setOrder(aggregationModel, aggregationBuilder, firstMetricAggregationBuilder);
        return aggregationBuilder;
    }

    private ValuesSourceAggregationBuilder setAggeragatedGeometryStrategy(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException {
        if (aggregationModel.fetchGeometry != null) {
            if (aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.bbox) {
                // Check if geobbox is not already asked as a sub-aggregation
                if ((aggregationModel.metrics != null && aggregationModel.metrics.stream().map(m -> m.collectFct).filter(collectFct -> collectFct.name().equals(CollectionFunction.GEOBBOX.name())).count() == 0) || aggregationModel.metrics == null) {
                    ValuesSourceAggregationBuilder metricAggregation = AggregationBuilders.geoBounds(CollectionFunction.GEOBBOX.name().toLowerCase() + "-bucket").field(collectionReference.params.centroidPath);
                    aggregationBuilder.subAggregation(metricAggregation);
                }
            } else if (aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.centroid) {
                // if geocentroid is not already asked as a sub-aggregation
                if ((aggregationModel.metrics != null && aggregationModel.metrics.stream().map(m -> m.collectFct).filter(collectFct -> collectFct.name().equals(CollectionFunction.GEOCENTROID.name())).count() == 0) || aggregationModel.metrics == null) {
                    ValuesSourceAggregationBuilder metricAggregation = AggregationBuilders.geoCentroid(CollectionFunction.GEOCENTROID.name().toLowerCase() + "-bucket").field(collectionReference.params.centroidPath);
                    aggregationBuilder.subAggregation(metricAggregation);
                }
            } else if (aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.geohash) {
                // aggregationModel.type is necesseraly AggregationTypeEnum.geohash. We already return the centroid of each geohash by default => nothing to implement here => create geohash geometry at response stage
            } else if (aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.byDefault) {
                if (aggregationModel.type !=  AggregationTypeEnum.geohash) {
                    String[] includes = {collectionReference.params.geometryPath, collectionReference.params.centroidPath};
                    TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(RANDOM_GEOMETRY).size(1).fetchSource(includes, null);
                    aggregationBuilder.subAggregation(topHitsAggregationBuilder);
                }
                // if aggregationModel.type ==  AggregationTypeEnum.geohash then we already return the centroid of each geohash by default => nothing to implement
            } else {
                String[] includes = {collectionReference.params.geometryPath, collectionReference.params.centroidPath};
                String sortField = (aggregationModel.fetchGeometry.field != null) ? aggregationModel.fetchGeometry.field : collectionReference.params.timestampPath;
                if (aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.first) {
                    TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(FIRST_GEOMETRY).size(1).sort(sortField, SortOrder.ASC).fetchSource(includes, null);
                    aggregationBuilder.subAggregation(topHitsAggregationBuilder);
                } else if (aggregationModel.fetchGeometry.strategy == AggregatedGeometryStrategyEnum.last) {
                    TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(LAST_GEOMETRY).size(1).sort(sortField, SortOrder.DESC).fetchSource(includes, null);
                    aggregationBuilder.subAggregation(topHitsAggregationBuilder);
                }
            }
        }
        return aggregationBuilder;
    }

    private ValuesSourceAggregationBuilder setHitsToFetch(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException {
        if (aggregationModel.fetchHits != null) {
            TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(FETCH_HITS_AGG);
            Integer size = Optional.ofNullable(aggregationModel.fetchHits.size).orElse(1);
            topHitsAggregationBuilder.size(size);
            List<String> includes = new ArrayList<>();
            if (aggregationModel.fetchHits.include != null) {
                for (String field : aggregationModel.fetchHits.include) {
                    String unsignedField = (field.startsWith("+") || field.startsWith("-")) ? field.substring(1) : field;
                    ElasticTool.checkAliasMappingFields(client, collectionReference.params.indexName, collectionReference.params.typeName, unsignedField);
                    includes.add(unsignedField);
                    if (field.startsWith("+")) {
                        topHitsAggregationBuilder.sort(unsignedField, SortOrder.ASC);
                    } else if (field.startsWith("-")) {
                        topHitsAggregationBuilder.sort(unsignedField, SortOrder.DESC);
                    }
                }
                String[] hitsToInclude = includes.toArray(new String[includes.size()]);
                topHitsAggregationBuilder.fetchSource(hitsToInclude, null);
            }
            aggregationBuilder.subAggregation(topHitsAggregationBuilder);
        }
        return aggregationBuilder;
    }

    private void setGeoMetricAggregationCollectField(Metric metric) throws ArlasException {
        if (!metric.collectField.equals(collectionReference.params.centroidPath)) {
            throw new BadRequestException(BAD_COLLECT_FIELD_FOR_GEO_METRICS);
        }
    }

    private void setOrder(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder, ValuesSourceAggregationBuilder metricAggregation) throws ArlasException {
        Order order = aggregationModel.order;
        OrderOn on = aggregationModel.on;
        if (order != null && on != null) {
            if (!(aggregationBuilder instanceof GeoGridAggregationBuilder)) {
                Boolean asc = true;
                BucketOrder bucketOrder = null;
                if (order.equals(Order.asc))
                    asc = true;
                else if (order.equals(Order.desc))
                    asc = false;
                if (on.equals(OrderOn.field)) {
                    bucketOrder = BucketOrder.key(asc);
                } else if (on.equals(OrderOn.count)) {
                    bucketOrder = BucketOrder.count(asc);
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
                switch (aggregationBuilder.getName()) {
                    case DATEHISTOGRAM_AGG:
                        aggregationBuilder = ((DateHistogramAggregationBuilder) aggregationBuilder).order(bucketOrder);
                        break;
                    case HISTOGRAM_AGG:
                        aggregationBuilder = ((HistogramAggregationBuilder) aggregationBuilder).order(bucketOrder);
                        break;
                    case TERM_AGG:
                        aggregationBuilder = ((TermsAggregationBuilder) aggregationBuilder).order(bucketOrder);
                        break;
                    default:
                        throw new NotAllowedException(NO_ORDER_ON_TO_SPECIFY);
                }
            } else {
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            }
        } else if (order != null && on == null) {
            if (aggregationBuilder instanceof GeoGridAggregationBuilder)
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ON_NOT_SPECIFIED);
        } else if (order == null && on != null) {
            if (aggregationBuilder instanceof GeoGridAggregationBuilder)
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ORDER_NOT_SPECIFIED);
        }
    }

    private Geometry readWKT(String geometry) throws ArlasException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Envelope affectedBounds = new Envelope(-360, 360, -180, 180);
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry polygon = null;
        try {
            polygon = wkt.read(geometry);
            List<Coordinate> filteredCoord = Arrays.stream(polygon.getCoordinates()).filter(coordinate -> affectedBounds.contains(coordinate)).collect(Collectors.toList());
            if(filteredCoord.size() != polygon.getCoordinates().length){
                throw new InvalidParameterException(INVALID_WKT_RANGE);
            }
            IsValidOp vaildOp = new IsValidOp(polygon);
            TopologyValidationError err = vaildOp.getValidationError();
            if (err != null)
            {
                throw new InvalidParameterException(INVALID_WKT);
            }
        } catch (ParseException ex) {
            throw new InvalidParameterException(INVALID_WKT);
        }
        return polygon;
    }

    private PolygonBuilder createPolygonBuilder(Polygon polygon) {
        // TODO: add interior holes
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        List<Coordinate> coordinates = Arrays.asList(polygon.getCoordinates());
        coordinatesBuilder.coordinates(coordinates);
        return new PolygonBuilder(coordinatesBuilder, ShapeBuilder.Orientation.LEFT);
    }

    private PolygonBuilder createPolygonBuilder(double[] bbox) {
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        coordinatesBuilder.coordinate(bbox[2], bbox[1]);
        coordinatesBuilder.coordinate(bbox[2], bbox[3]);
        coordinatesBuilder.coordinate(bbox[0], bbox[3]);
        coordinatesBuilder.coordinate(bbox[0], bbox[1]);
        coordinatesBuilder.coordinate(bbox[2], bbox[1]);
        // NB : In ES api LEFT is clockwise and RIGHT anticlockwise
        return new PolygonBuilder(coordinatesBuilder, ShapeBuilder.Orientation.RIGHT);
    }

    private MultiPolygonBuilder createMultiPolygonBuilder(MultiPolygon multiPolygon) {
        MultiPolygonBuilder multiPolygonBuilder = new MultiPolygonBuilder(ShapeBuilder.Orientation.LEFT);
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            multiPolygonBuilder.polygon(createPolygonBuilder((Polygon) multiPolygon.getGeometryN(i)));
        }
        return multiPolygonBuilder;
    }

    private LineStringBuilder createLineStringBuilder(LineString lineString) {
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        coordinatesBuilder.coordinates(lineString.getCoordinates());
        return new LineStringBuilder(coordinatesBuilder);
    }

    private PointBuilder createPointBuilder(Point point) {
        PointBuilder pointBuilder = new PointBuilder();
        pointBuilder.coordinate(point.getCoordinate());
        return pointBuilder;
    }

    private ShapeBuilder getShapeBuilder(String geometry) throws ArlasException {
        // test if geometry is west, south, east, north commat separated
        if (isBboxMatch(geometry)) {
            CheckParams.checkBbox(geometry);
            return createPolygonBuilder((double[]) CheckParams.toDoubles(geometry));
        } else {
            // TODO: multilinestring
            Geometry wktGeometry = readWKT(geometry);
            if (wktGeometry != null) {
                String geometryType = wktGeometry.getGeometryType().toUpperCase();
                switch (geometryType) {
                    case "POLYGON":
                        return createPolygonBuilder((Polygon) wktGeometry);
                    case "MULTIPOLYGON":
                        return createMultiPolygonBuilder((MultiPolygon) wktGeometry);
                    case "LINESTRING":
                        return createLineStringBuilder((LineString) wktGeometry);
                    case "POINT":
                        return createPointBuilder((Point) wktGeometry);
                    default:
                        throw new InvalidParameterException("The given geometry is not handled.");
                }
            }
            throw new InvalidParameterException("The given geometry is invalid.");
        }
    }

    private boolean isBboxMatch(String geometry) {
        String floatPattern = "[-+]?[0-9]*\\.?[0-9]+";
        String bboxPattern = floatPattern + "," + floatPattern + "," + floatPattern + "," + floatPattern;
        return Pattern.compile("^" + bboxPattern + "$").matcher(geometry).matches();
    }

    public boolean isDateField(String field) throws ArlasException {
        return ElasticTool.isDateField(field, client, collectionReference.params.indexName, collectionReference.params.typeName);
    }

    public void setCollectionReference(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
        searchRequestBuilder = client.prepareSearch(collectionReference.params.indexName).setTypes(collectionReference.params.typeName);
    }

    public CollectionReference getCollectionReference() {
        return collectionReference;
    }

    public List<String> getCollectionPaths() {
        return Arrays.asList(collectionReference.params.idPath,
                collectionReference.params.geometryPath,
                collectionReference.params.centroidPath,
                collectionReference.params.timestampPath);
    }
}
