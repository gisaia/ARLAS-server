package io.arlas.server.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.LineStringBuilder;
import org.elasticsearch.common.geo.builders.MultiPolygonBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotAllowedException;
import io.arlas.server.exceptions.NotImplementedException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Aggregation;
import io.arlas.server.rest.explore.enumerations.AggregationOn;
import io.arlas.server.rest.explore.enumerations.AggregationOrder;
import io.arlas.server.rest.explore.enumerations.AggregationType;
import io.arlas.server.rest.explore.enumerations.DateInterval;
import io.arlas.server.rest.explore.enumerations.MetricAggregationType;
import io.arlas.server.utils.DateAggregationInterval;
import io.arlas.server.utils.ParamsParser;


public class FluidSearch {

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

    public FluidSearch(TransportClient client) {
        this.client = client;
        boolQueryBuilder = QueryBuilders.boolQuery();
    }

    public SearchResponse exec() throws ArlasException {
        searchRequestBuilder.setQuery(boolQueryBuilder);
        LOGGER.debug("QUERY : "+searchRequestBuilder.toString());
        SearchResponse result = null;

        result = searchRequestBuilder.get();

        return result;
    }

    public FluidSearch filter(List<String> f) throws ArlasException {
        for (int i = 0; i < f.size(); i++) {
            if (f.get(i) != null && !f.get(i).isEmpty()) {
                String operands[] = f.get(i).split(":");
                int operandsNumber = operands.length;
                if (operandsNumber != 3) {
                    throw new InvalidParameterException(INVALID_PARAMETER_F);
                } else {
                    //Means it's an gte, lte, like, ... operation
                    if (operands[2] != null) {
                        if (operands[1].equals("eq")){
                            String fieldValues[] = operands[2].split(",");
                            if (fieldValues.length>1){
                                BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
                                for (String value : fieldValues){
                                    orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery(operands[0], value));
                                }
                                boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
                            }
                            else {
                                boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.matchQuery(operands[0], operands[2]));
                            }
                        } else if (operands[1].equals("gte")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).gte(operands[2]));
                        } else if (operands[1].equals("gt")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).gt(operands[2]));
                        } else if (operands[1].equals("lte")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).lte(operands[2]));
                        } else if (operands[1].equals("lt")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).lt(operands[2]));
                        } else if (operands[1].equals("like")) {
                            //TODO: if field type is fullText, use matchPhraseQuery instead of regexQuery
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.regexpQuery(operands[0], ".*" + operands[2] + ".*"));
                        } else if (operands[1].equals("ne")) {
                            String fieldValues[] = operands[2].split(",");
                            for (String value : fieldValues) {
                                boolQueryBuilder = boolQueryBuilder
                                        .mustNot(QueryBuilders.matchQuery(operands[0], value));
                            }
                        }
                        else {
                            throw new InvalidParameterException(INVALID_OPERATOR);
                        }
                    }
                }
            }
        }
        return this;
    }

    public FluidSearch filterQ(String q) throws ArlasException{
        String operands[] = q.split(":");
        if (operands.length == 2){
            boolQueryBuilder = boolQueryBuilder
                    .filter(QueryBuilders.simpleQueryStringQuery(operands[1]).defaultOperator(Operator.AND).field(operands[0]));
        }else if (operands.length == 1){
            boolQueryBuilder = boolQueryBuilder
                    .filter(QueryBuilders.simpleQueryStringQuery(operands[0]).defaultOperator(Operator.AND));
        }else {
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
        GeoPoint topLeft = new GeoPoint(top,left);
        GeoPoint bottomRight = new GeoPoint(bottom,right);
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders
                .geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(topLeft,bottomRight));
        return this;
    }

    public FluidSearch filterNotPWithin(double top, double left, double bottom, double right)
            throws ArlasException, IOException {
        GeoPoint topLeft = new GeoPoint(top,left);
        GeoPoint bottomRight = new GeoPoint(bottom,right);
        boolQueryBuilder = boolQueryBuilder.mustNot(QueryBuilders
                .geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(topLeft,bottomRight));
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
            String includeFields[] = include.split(",");
            searchRequestBuilder = searchRequestBuilder.setFetchSource(includeFields, null);
        }
        return this;
    }

    public FluidSearch exclude(String exclude) {
        if (exclude != null) {
            String excludeFields[] = exclude.split(",");
            searchRequestBuilder = searchRequestBuilder.setFetchSource(null, excludeFields);
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

    private AggregationBuilder aggregateRecursive (List<Aggregation> aggregations, AggregationBuilder aggregationBuilder, Boolean isGeoAggregate, Integer counter) throws ArlasException{
        //check the agg syntax is correct
        Aggregation aggregationModel = aggregations.get(0);
        if (isGeoAggregate && counter == 0){
            if (aggregationModel.type.equals(AggregationType.geohash.name())){
                aggregationBuilder = buildGeohashAggregation(aggregationModel);
            }
            else throw new NotAllowedException(aggregationModel.type + NOT_ALLOWED_AS_MAIN_AGGREGATION_TYPE);
        }
        else {
            switch (aggregationModel.type) {
                case AggregationType.DATEHISTOGRAM:
                    aggregationBuilder = buildDateHistogramAggregation(aggregationModel);
                    break;
                case AggregationType.GEOHASH:
                    aggregationBuilder = buildGeohashAggregation(aggregationModel);
                    break;
                case AggregationType.HISTOGRAM:
                    aggregationBuilder = buildHistogramAggregation(aggregationModel);
                    break;
                case AggregationType.TERM:
                    aggregationBuilder = buildTermsAggregation(aggregationModel);
                    break;
            }
        }
        aggregations.remove(0);
        if (aggregations.size() == 0){
            return aggregationBuilder;
        }
        counter++;
        return aggregationBuilder.subAggregation(aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, counter));
    }
    public FluidSearch aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException{
        AggregationBuilder aggregationBuilder = null;
        aggregationBuilder = aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, 0);
        searchRequestBuilder =searchRequestBuilder.setSize(0).addAggregation(aggregationBuilder);
        return this;
    }

    private DateHistogramAggregationBuilder buildDateHistogramAggregation(Aggregation aggregationModel) throws ArlasException{
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(DATEHISTOGRAM_AGG);
        // Get the interval
        DateAggregationInterval dateAggregationInterval = ParamsParser.getAggregationDateInterval(aggregationModel.interval);
        DateHistogramInterval dateHistogramInterval = null;
        Integer aggsize = dateAggregationInterval.aggsize;
        if(dateAggregationInterval.aggunit.equals(DateInterval.YEAR) || dateAggregationInterval.aggunit.equals(DateInterval.QUARTER) ||dateAggregationInterval.aggunit.equals(DateInterval.DAY) ){
            if(aggsize>1) throw new NotAllowedException("The size must be equal to 1 for the unit " + dateAggregationInterval.aggunit + ".");
        }
        switch (dateAggregationInterval.aggunit){
            case DateInterval.YEAR : dateHistogramInterval = DateHistogramInterval.YEAR; break;
            case DateInterval.QUARTER : dateHistogramInterval = DateHistogramInterval.QUARTER; break;
            case DateInterval.MONTH : dateHistogramInterval = DateHistogramInterval.MONTH; break;
            case DateInterval.WEEK : dateHistogramInterval = DateHistogramInterval.weeks(aggsize); break;
            case DateInterval.DAY : dateHistogramInterval = DateHistogramInterval.days(aggsize); break;
            case DateInterval.HOUR : dateHistogramInterval = DateHistogramInterval.hours(aggsize); break;
            case DateInterval.MINUTE : dateHistogramInterval = DateHistogramInterval.minutes(aggsize); break;
            case DateInterval.SECOND : dateHistogramInterval = DateHistogramInterval.seconds(aggsize); break;
            default : throw new InvalidParameterException(INVALID_DATE_UNIT);
        }
        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(dateHistogramInterval);
        //get the field, format, collect_field, collect_fct, order, on
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder)setAggregationParameters(aggregationModel, dateHistogramAggregationBuilder);
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregationModel builder
    private GeoGridAggregationBuilder buildGeohashAggregation(Aggregation aggregationModel) throws ArlasException{
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid(GEOHASH_AGG);
        //get the precision
        Integer precision = ParamsParser.getAggregationGeohasPrecision(aggregationModel.interval);
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoGridAggregationBuilder)setAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    // construct and returns the histogram aggregationModel builder
    private HistogramAggregationBuilder buildHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram(HISTOGRAM_AGG);
        // get the length
        Double length = ParamsParser.getAggregationHistogramLength(aggregationModel.interval);
        histogramAggregationBuilder = histogramAggregationBuilder.interval(length);
        //get the field, format, collect_field, collect_fct, order, on
        histogramAggregationBuilder = (HistogramAggregationBuilder)setAggregationParameters(aggregationModel, histogramAggregationBuilder);
        return histogramAggregationBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private TermsAggregationBuilder buildTermsAggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(TERM_AGG);
        //get the field, format, collect_field, collect_fct, order, on
        if(aggregationModel.interval != null){
            throw new BadRequestException(NO_TERM_INTERVAL);
        }
        termsAggregationBuilder = (TermsAggregationBuilder)setAggregationParameters(aggregationModel, termsAggregationBuilder);
        return termsAggregationBuilder;
    }

    private ValuesSourceAggregationBuilder setAggregationParameters (Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException{
        String aggField = aggregationModel.field;
        aggregationBuilder = aggregationBuilder.field(aggField);
        //Get the format
        String format = ParamsParser.getValidAggregationFormat(aggregationModel.format);
        if (aggregationBuilder instanceof DateHistogramAggregationBuilder){
            aggregationBuilder = aggregationBuilder.format(format);
        }
        else if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        // sub aggregate with a metric aggregationModel
        ValuesSourceAggregationBuilder.LeafOnly metricAggregation = null;
        if(aggregationModel.collectField != null && aggregationModel.collectFct != null) {
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
                default: throw new InvalidParameterException(aggregationModel.collectFct + " function is invalid.");
            }
            if (metricAggregation != null){
                aggregationBuilder.subAggregation(metricAggregation);
            }
        }
        else if (aggregationModel.collectField != null && aggregationModel.collectFct == null){
            throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
        }
        else if (aggregationModel.collectField == null && aggregationModel.collectFct != null){
            throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
        }
        if (aggregationModel.size != null){
            Integer s = ParamsParser.getValidAggregationSize(aggregationModel.size);
            if (aggregationBuilder instanceof TermsAggregationBuilder)
                aggregationBuilder = ((TermsAggregationBuilder) aggregationBuilder).size(s);
            else if (aggregationBuilder instanceof GeoGridAggregationBuilder)
                throw new NotImplementedException(SIZE_NOT_IMPLEMENTED);
            else
                throw new BadRequestException(NO_SIZE_TO_SPECIFY);
        }
        setOrder(aggregationModel,aggregationBuilder, metricAggregation);
        return aggregationBuilder;
    }

    private void setOrder(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder, ValuesSourceAggregationBuilder.LeafOnly metricAggregation) throws ArlasException{
        String order = aggregationModel.order;
        String on = aggregationModel.on;
        if (order != null && on != null) {
            if (!(aggregationBuilder instanceof GeoGridAggregationBuilder)){
                Boolean asc;
                Histogram.Order histogramOrder = null;
                Terms.Order termsOrder = null;
                if (order.equals(AggregationOrder.asc.name()))
                    asc = true;
                else if (order.equals(AggregationOrder.desc.name()))
                    asc = false;
                else
                    throw new InvalidParameterException(INVALID_ORDER_VALUE + order);

                if (on.equals(AggregationOn.field.name())) {
                    termsOrder = Terms.Order.term(asc);
                    if (asc)
                        histogramOrder = Histogram.Order.KEY_ASC;
                    else
                        histogramOrder = Histogram.Order.KEY_DESC;
                } else if (on.equals(AggregationOn.count.name())) {
                    termsOrder = Terms.Order.count(asc);
                    if (asc)
                        histogramOrder = Histogram.Order.COUNT_ASC;
                    else
                        histogramOrder = Histogram.Order.COUNT_DESC;
                } else if (on.equals(AggregationOn.result.name())) {
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
            }else {
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            }
        }
        else if (order != null && on == null){
            if (aggregationBuilder instanceof GeoGridAggregationBuilder)
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ON_NOT_SPECIFIED);
        }
        else if (order == null && on != null){
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
        if(collectionReference.params.includeFields != null && !collectionReference.params.includeFields.isEmpty()) {
            include(collectionReference.params.includeFields);
        }
        if(collectionReference.params.excludeFields != null && !collectionReference.params.excludeFields.isEmpty()) {
            exclude(collectionReference.params.excludeFields);
        }
    }
}
