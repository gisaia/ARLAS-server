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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import io.arlas.server.exceptions.*;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.*;
import io.arlas.server.rest.explore.enumerations.MetricAggregationType;
import io.arlas.server.utils.ParamsParser;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FluidSearch {

    public static final String INVALID_FILTER = "Invalid filter parameter.";
    public static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    public static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'eq', gte', 'gt', 'lte', 'lt', 'like' or 'ne'. ";
    public static final String INVALID_Q_FILTER = "Invalid parameter. Please specify the text to search directly or '{fieldname}:{text to search}'. ";
    public static final String INVALID_WKT = "Invalid WKT geometry.";
    public static final String INVALID_BBOX = "Invalid BBOX";
    public static final String INVALID_BEFORE_AFTER = "Invalid date parameters : before and after must be positive and before must be greater than after.";
    public static final String INVALID_SIZE = "Invalid size parameter.";
    public static final String INVALID_FROM = "Invalid from parameter.";
    public static final String INVALID_DATE_UNIT = "Invalid date unit.";
    public static final String INVALID_ON_VALUE = "Invalid 'on-' value ";
    public static final String INVALID_ORDER_VALUE = "Invalid 'on-' value ";
    public static final String DATEHISTOGRAM_AGG = "Datehistogram aggregation";
    public static final String HISTOGRAM_AGG = "Histogram aggregation";
    public static final String TERM_AGG = "Term aggregation";
    public static final String GEOHASH_AGG = "Geohash aggregation";
    public static final String NOT_ALLOWED_AGGREGATION_TYPE = " aggregation type is not allowed. Please use '_geoaggregate' service instead.";
    public static final String NOT_ALLOWED_AS_MAIN_AGGREGATION_TYPE = " aggregation type is not allowed as main aggregation. Please make sure that geohash is the main aggregation or use '_aggregate' service instead.";
    public static final String INTREVAL_NOT_SPECIFIED = "Interval parameter 'interval-' is not specified.";
    public static final String NO_TERM_INTERVAL = "'interval-' should not be specified for term aggregation.";
    public static final String NO_FORMAT_TO_SPECIFY = "'format-' should not be specified for this aggregation.";
    public static final String NO_SIZE_TO_SPECIFY = "'size-' should not be specified for this aggregation.";
    public static final String NO_ORDER_ON_TO_SPECIFY = "'order-' and 'on-' should not be specified for this aggregation.";
    public static final String COLLECT_FCT_NOT_SPECIFIED = "The aggregation function 'collect_fct' is not specified.";
    public static final String COLLECT_FIELD_NOT_SPECIFIED = "The aggregation field 'collect_field' is not specified.";
    public static final String ORDER_NOT_SPECIFIED = "'order-' is not specified.";
    public static final String ON_NOT_SPECIFIED = "'on-' is not specified.";
    public static final String ORDER_PARAM_NOT_ALLOWED = "Order is not allowed for geohash aggregation.";
    public static final String ORDER_ON_RESULT_NOT_ALLOWED = "'on-result' sorts 'collect_field' and 'collect_fct' results. Please specify 'collect_field' and 'collect_fct'.";
    public static final String SIZE_NOT_IMPLEMENTED = "Size is not implemented for geohash.";

    private static Logger LOGGER = LoggerFactory.getLogger(FluidSearch.class);

    private TransportClient client;
    private SearchRequestBuilder searchRequestBuilder;
    private BoolQueryBuilder boolQueryBuilder;
    private CollectionReference collectionReference;

    private List<String> include = new ArrayList<>();
    private List<String> exclude = new ArrayList<>();

    public FluidSearch(TransportClient client) {
        this.client = client;
        boolQueryBuilder = QueryBuilders.boolQuery();
    }

    public SearchResponse exec() throws ArlasException {
        searchRequestBuilder.setQuery(boolQueryBuilder);

        //apply include and exclude filters
        if (include.isEmpty() && collectionReference.params.includeFields != null && !collectionReference.params.includeFields.isEmpty()) {
            include(collectionReference.params.includeFields);
        }
        if (exclude.isEmpty() && collectionReference.params.excludeFields != null && !collectionReference.params.excludeFields.isEmpty()) {
            exclude(collectionReference.params.excludeFields);
        }
        List<String> includeFieldList = new ArrayList<>();
        if(!include.isEmpty()) {
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
        if(includeFields.length == 0) {
            includeFields = new String[]{"*"};
        }
        String[] excludeFields = exclude.toArray(new String[exclude.size()]);
        if(excludeFields.length == 0) {
            excludeFields = null;
        }
        searchRequestBuilder = searchRequestBuilder.setFetchSource(includeFields, excludeFields);

        //Get Elasticsearch response
        LOGGER.debug("QUERY : " + searchRequestBuilder.toString());
        SearchResponse result = null;
        result = searchRequestBuilder.get();
        return result;
    }

    public FluidSearch filter(List<Expression> expressions) throws ArlasException {
        for (Expression expression : expressions) {
            if (Strings.isNullOrEmpty(expression.field) || expression.op == null ||Strings.isNullOrEmpty(expression.value)) {
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
                        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
                    } else {
                        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.matchQuery(field, value));
                    }
                    break;
                case gte:
                    boolQueryBuilder = boolQueryBuilder
                            .filter(QueryBuilders.rangeQuery(field).gte(value));
                    break;
                case gt:
                    boolQueryBuilder = boolQueryBuilder
                            .filter(QueryBuilders.rangeQuery(field).gt(value));
                    break;
                case lte:
                    boolQueryBuilder = boolQueryBuilder
                            .filter(QueryBuilders.rangeQuery(field).lte(value));
                    break;
                case lt:
                    boolQueryBuilder = boolQueryBuilder
                            .filter(QueryBuilders.rangeQuery(field).lt(value));
                    break;
                case like:
                    //TODO: if field type is fullText, use matchPhraseQuery instead of regexQuery
                    boolQueryBuilder = boolQueryBuilder
                            .filter(QueryBuilders.regexpQuery(field, ".*" + value + ".*"));
                    break;
                case ne:
                    for (String valueInValues : fieldValues) {
                        boolQueryBuilder = boolQueryBuilder
                                .mustNot(QueryBuilders.matchQuery(field, valueInValues));
                    }
                    break;
                default:
                    throw new InvalidParameterException(INVALID_OPERATOR);
            }

        }
        return this;
    }

    public FluidSearch filterQ(String q) throws ArlasException {
        String operands[] = q.split(":");
        if (operands.length == 2) {
            boolQueryBuilder = boolQueryBuilder
                    .filter(QueryBuilders.simpleQueryStringQuery(operands[1]).defaultOperator(Operator.AND).field(operands[0]));
        } else if (operands.length == 1) {
            boolQueryBuilder = boolQueryBuilder
                    .filter(QueryBuilders.simpleQueryStringQuery(operands[0]).defaultOperator(Operator.AND));
        } else {
            throw new InvalidParameterException(INVALID_Q_FILTER);
        }
        return this;
    }

    public FluidSearch filterAfter(Long after) {
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.rangeQuery(collectionReference.params.timestampPath).gte(after));
        return this;
    }

    public FluidSearch filterBefore(Long before) {
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.rangeQuery(collectionReference.params.timestampPath).lte(before));
        return this;
    }

    public FluidSearch filterAfterBefore(Long after, Long before) {
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.rangeQuery(collectionReference.params.timestampPath).gte(after).lte(before));
        return this;
    }

    public FluidSearch filterPWithin(double top, double left, double bottom, double right)
            throws ArlasException, IOException {
        GeoPoint topLeft = new GeoPoint(top, left);
        GeoPoint bottomRight = new GeoPoint(bottom, right);
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders
                .geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(topLeft, bottomRight));
        return this;
    }

    public FluidSearch filterNotPWithin(double top, double left, double bottom, double right)
            throws ArlasException, IOException {
        GeoPoint topLeft = new GeoPoint(top, left);
        GeoPoint bottomRight = new GeoPoint(bottom, right);
        boolQueryBuilder = boolQueryBuilder.mustNot(QueryBuilders
                .geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(topLeft, bottomRight));
        return this;
    }

    public FluidSearch filterGWithin(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, shapeBuilder));
        return this;
    }

    public FluidSearch filterNotGWithin(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder
                .mustNot(QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, shapeBuilder));
        return this;
    }

    public FluidSearch filterGIntersect(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.geoIntersectionQuery(collectionReference.params.geometryPath, shapeBuilder));
        return this;
    }

    public FluidSearch filterNotGIntersect(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.geoDisjointQuery(collectionReference.params.geometryPath, shapeBuilder));
        return this;
    }

    public FluidSearch include(String include) {
        if (include != null) {
            String includeFieldArray[] = include.split(",");
            for(String field : includeFieldArray) {
                this.include.add(field);
            }
        }
        return this;
    }

    public FluidSearch exclude(String exclude) {
        if (exclude != null) {
            String excludeFieldArray[] = exclude.split(",");
            for(String field : excludeFieldArray) {
                this.exclude.add(field);
            }
        }
        return this;
    }

    public FluidSearch filterSize(Integer size, Integer from) {
        searchRequestBuilder = searchRequestBuilder.setSize(size).setFrom(from);
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
                searchRequestBuilder = searchRequestBuilder.addSort(field, sortOrder);

            }
        }
        return this;
    }

    private AggregationBuilder aggregateRecursive(List<Aggregation> aggregations, AggregationBuilder aggregationBuilder, Boolean isGeoAggregate, Integer counter) throws ArlasException {
        //check the agg syntax is correct
        Aggregation aggregationModel = aggregations.get(0);
        if (isGeoAggregate && counter == 0) {
            if (aggregationModel.type.equals(AggregationTypeEnum.geohash)) {
                aggregationBuilder = buildGeohashAggregation(aggregationModel);
            } else throw new NotAllowedException(aggregationModel.type + NOT_ALLOWED_AS_MAIN_AGGREGATION_TYPE);
        } else {
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

    private DateHistogramAggregationBuilder buildDateHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        if(Strings.isNullOrEmpty(aggregationModel.field)) {
            aggregationModel.field = collectionReference.params.timestampPath;
        }
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(DATEHISTOGRAM_AGG);
        if (aggregationModel.interval.unit.equals(UnitEnum.year) || aggregationModel.interval.unit.equals(UnitEnum.quarter) || aggregationModel.interval.unit.equals(UnitEnum.day)) {
            if (aggregationModel.interval.value > 1)
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
                intervalUnit = DateHistogramInterval.weeks(aggregationModel.interval.value);
                break;
            case day:
                intervalUnit = DateHistogramInterval.days(aggregationModel.interval.value);
                break;
            case hour:
                intervalUnit = DateHistogramInterval.hours(aggregationModel.interval.value);
                break;
            case minute:
                intervalUnit = DateHistogramInterval.minutes(aggregationModel.interval.value);
                break;
            case second:
                intervalUnit = DateHistogramInterval.seconds(aggregationModel.interval.value);
                break;
            default:
                throw new InvalidParameterException(INVALID_DATE_UNIT);
        }
        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(intervalUnit);
        //get the field, format, collect_field, collect_fct, order, on
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setAggregationParameters(aggregationModel, dateHistogramAggregationBuilder);
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregationModel builder
    private GeoGridAggregationBuilder buildGeohashAggregation(Aggregation aggregationModel) throws ArlasException {
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid(GEOHASH_AGG);
        //get the precision
        Integer precision = ParamsParser.getAggregationGeohasPrecision(aggregationModel.interval);
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    // construct and returns the histogram aggregationModel builder
    private HistogramAggregationBuilder buildHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram(HISTOGRAM_AGG);
        // get the length
        if(aggregationModel.interval==null || aggregationModel.interval.value==null){
            throw new InvalidParameterException("Interval must be provided. Currently null.");
        }
        histogramAggregationBuilder = histogramAggregationBuilder.interval(aggregationModel.interval.value);
        //get the field, format, collect_field, collect_fct, order, on
        histogramAggregationBuilder = (HistogramAggregationBuilder) setAggregationParameters(aggregationModel, histogramAggregationBuilder);
        return histogramAggregationBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private TermsAggregationBuilder buildTermsAggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(TERM_AGG);
        //get the field, format, collect_field, collect_fct, order, on
        if (aggregationModel.interval != null) {
            throw new BadRequestException(NO_TERM_INTERVAL);
        }
        termsAggregationBuilder = (TermsAggregationBuilder) setAggregationParameters(aggregationModel, termsAggregationBuilder);
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
        // sub aggregate with a metric aggregationModel
        ValuesSourceAggregationBuilder.LeafOnly metricAggregation = null;
        if (aggregationModel.collectField != null && aggregationModel.collectFct != null) {
            switch (aggregationModel.collectFct) {
                case MetricAggregationType.AVG:
                    metricAggregation = AggregationBuilders.avg("avg").field(aggregationModel.collectField);
                    break;
                case MetricAggregationType.CARDINALITY:
                    metricAggregation = AggregationBuilders.cardinality("cardinality").field(aggregationModel.collectField);
                    break;
                case MetricAggregationType.MAX:
                    metricAggregation = AggregationBuilders.max("max").field(aggregationModel.collectField);
                    break;
                case MetricAggregationType.MIN:
                    metricAggregation = AggregationBuilders.min("min").field(aggregationModel.collectField);
                    break;
                case MetricAggregationType.SUM:
                    metricAggregation = AggregationBuilders.sum("sum").field(aggregationModel.collectField);
                    break;
                default:
                    throw new InvalidParameterException(aggregationModel.collectFct + " function is invalid.");
            }
            if (metricAggregation != null) {
                aggregationBuilder.subAggregation(metricAggregation);
            }
        } else if (aggregationModel.collectField != null && aggregationModel.collectFct == null) {
            throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
        } else if (aggregationModel.collectField == null && aggregationModel.collectFct != null) {
            throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
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
        setOrder(aggregationModel, aggregationBuilder, metricAggregation);
        return aggregationBuilder;
    }

    private void setOrder(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder, ValuesSourceAggregationBuilder.LeafOnly metricAggregation) throws ArlasException {
        AggregationOrderEnum order = aggregationModel.order;
        AggregationOnEnum on = aggregationModel.on;
        if (order != null && on != null) {
            if (!(aggregationBuilder instanceof GeoGridAggregationBuilder)) {
                Boolean asc;
                Histogram.Order histogramOrder = null;
                Terms.Order termsOrder = null;
                if (order.equals(AggregationOrderEnum.asc))
                    asc = true;
                else if (order.equals(AggregationOrderEnum.desc))
                    asc = false;
                else
                    throw new InvalidParameterException(INVALID_ORDER_VALUE + order);

                if (on.equals(AggregationOnEnum.field)) {
                    termsOrder = Terms.Order.term(asc);
                    if (asc)
                        histogramOrder = Histogram.Order.KEY_ASC;
                    else
                        histogramOrder = Histogram.Order.KEY_DESC;
                } else if (on.equals(AggregationOnEnum.count)) {
                    termsOrder = Terms.Order.count(asc);
                    if (asc)
                        histogramOrder = Histogram.Order.COUNT_ASC;
                    else
                        histogramOrder = Histogram.Order.COUNT_DESC;
                } else if (on.equals(AggregationOnEnum.result)) {
                    if (metricAggregation != null) {
                        termsOrder = Terms.Order.aggregation(metricAggregation.getName(), asc);
                        histogramOrder = Histogram.Order.aggregation(metricAggregation.getName(), asc);
                    } else {
                        throw new BadRequestException(ORDER_ON_RESULT_NOT_ALLOWED);
                    }
                } else {
                    throw new InvalidParameterException(INVALID_ON_VALUE + on);
                }
                switch (aggregationBuilder.getName()) {
                    case DATEHISTOGRAM_AGG:
                        aggregationBuilder = ((DateHistogramAggregationBuilder) aggregationBuilder).order(histogramOrder);
                        break;
                    case HISTOGRAM_AGG:
                        aggregationBuilder = ((HistogramAggregationBuilder) aggregationBuilder).order(histogramOrder);
                        break;
                    case TERM_AGG:
                        aggregationBuilder = ((TermsAggregationBuilder) aggregationBuilder).order(termsOrder);
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

    private Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Geometry readWKT(String geometry) throws ArlasException {
        WKTReader wkt = new WKTReader();
        Geometry polygon = null;
        try {
            polygon = wkt.read(geometry);
        } catch (ParseException ex) {
            throw new InvalidParameterException(INVALID_WKT);
        }
        return polygon;
    }

    private PolygonBuilder createPolygonBuilder(Polygon polygon) {
        // TODO: add interior holes
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        coordinatesBuilder.coordinates(polygon.getCoordinates());
        return new PolygonBuilder(coordinatesBuilder, ShapeBuilder.Orientation.LEFT);
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

    public void setCollectionReference(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
        searchRequestBuilder = client.prepareSearch(collectionReference.params.indexName).setTypes(collectionReference.params.typeName);
    }

    public List<String> getCollectionPaths() {
        return Arrays.asList(collectionReference.params.idPath,
                collectionReference.params.geometryPath,
                collectionReference.params.centroidPath,
                collectionReference.params.timestampPath);
    }
}
