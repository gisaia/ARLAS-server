package io.arlas.server.core;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotAllowedException;
import io.arlas.server.model.AggregationModel;
import io.arlas.server.model.ArlasAggregation;
import io.arlas.server.model.ArlasMetric;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.rest.explore.enumerations.*;
import io.arlas.server.utils.CheckParams;
import io.arlas.server.utils.DateAggregationInterval;
import io.arlas.server.utils.ParamsParser;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
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

    public static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    public static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'gte', 'gt', 'lte' or 'lt'. ";
    public static final String INVALID_VALUE_TYPE = "Operand must be a numeric value.";
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
    public static final String ORDER_PARAM_NOT_ALLOWED = "Order is not allowd for geohash aggregation.";
    public static final String ORDER_ON_RESULT_NOT_ALLOWED = "'on-result' sorts 'collect_field' and 'collect_fct' results. Please specify 'collect_field' and 'collect_fct'.";



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

        try {
            result = searchRequestBuilder.get();
        }catch (ElasticsearchException e ){
            throw new InvalidParameterException(e.getRootCause().getMessage() );
        }
        return result;
    }

    public ArlasAggregation formatAggregationResult(MultiBucketsAggregation aggregation, ArlasAggregation arlasAggregation){
        arlasAggregation.name = aggregation.getName();
        arlasAggregation.elements = new ArrayList<ArlasAggregation>();
        List<MultiBucketsAggregation.Bucket> buckets = (List<MultiBucketsAggregation.Bucket>)aggregation.getBuckets();
        buckets.forEach(bucket -> {
            ArlasAggregation element = new ArlasAggregation();
            element.key = bucket.getKey();
            element.keyAsString = bucket.getKeyAsString();
            element.count = bucket.getDocCount();
            element.elements = new ArrayList<ArlasAggregation>();
            if (bucket.getAggregations().asList().size() == 0){
                element.elements = null;
                arlasAggregation.elements.add(element);
            }
            else {
                bucket.getAggregations().forEach(subAggregation -> {
                    ArlasAggregation subArlasAggregation = new ArlasAggregation();
                    subArlasAggregation.name = subAggregation.getName();
                    if (subAggregation.getName().equals(FluidSearch.DATEHISTOGRAM_AGG) || subAggregation.getName().equals(FluidSearch.GEOHASH_AGG) || subAggregation.getName().equals(FluidSearch.HISTOGRAM_AGG) ||subAggregation.getName().equals(FluidSearch.TERM_AGG)){
                        subArlasAggregation = formatAggregationResult(((MultiBucketsAggregation)subAggregation), subArlasAggregation);
                    } else{
                        subArlasAggregation.elements = null;
                        ArlasMetric arlasMetric = new ArlasMetric();
                        arlasMetric.type = subAggregation.getName();
                        arlasMetric.value = (Double)subAggregation.getProperty("value");
                        subArlasAggregation.metric = arlasMetric;
                    }
                    element.elements.add(subArlasAggregation);
                });
                arlasAggregation.elements.add(element);
            }
        });
        return arlasAggregation;
    }

    public FluidSearch filter(List<String> f) throws ArlasException {
        for (int i = 0; i < f.size(); i++) {
            if (f.get(i) != null && !f.get(i).isEmpty()) {
                String operands[] = f.get(i).split(":");
                int operandsNumber = operands.length;
                if (operandsNumber < 2 || operandsNumber > 3) {
                    throw new InvalidParameterException(INVALID_PARAMETER_F);
                } else if (operandsNumber == 2) {
                    boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.matchQuery(operands[0], operands[1]));
                } else if (operandsNumber == 3) {
                    Integer fieldValue = tryParse(operands[2]);
                    if (fieldValue != null) {
                        if (operands[1].equals("gte")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).gte(fieldValue));
                        } else if (operands[1].equals("gt")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).gt(fieldValue));
                        } else if (operands[1].equals("lte")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).lte(fieldValue));
                        } else if (operands[1].equals("lt")) {
                            boolQueryBuilder = boolQueryBuilder
                                    .filter(QueryBuilders.rangeQuery(operands[0]).lt(fieldValue));
                        } else
                            throw new InvalidParameterException(INVALID_OPERATOR);
                    } else
                        throw new InvalidParameterException(INVALID_VALUE_TYPE);
                }
            }
        }
        return this;
    }

    public FluidSearch filterQ(String q) {
        boolQueryBuilder = boolQueryBuilder
                .filter(QueryBuilders.simpleQueryStringQuery(q).defaultOperator(Operator.AND));
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

    private AggregationBuilder aggregateRecursive (List<String> aggregations, AggregationBuilder aggregationBuilder, Boolean isGeoAggregate, Integer counter) throws ArlasException{
        //check the agg syntax is correct
        if (aggregations.size()>0) {
            String agg = aggregations.get(0);
            if (CheckParams.isAggregationParamValid(agg)) {
                List<String> aggParameters = Arrays.asList(agg.split(":"));
                AggregationModel aggregationModel = ParamsParser.getAggregation(aggParameters);
                if (isGeoAggregate && counter == 0){
                    if (aggregationModel.aggType.equals(AggregationType.geohash.name())){
                        aggregationBuilder = buildGeohashAggregation(aggregationModel);
                    }
                    else throw new NotAllowedException(aggregationModel.aggType + NOT_ALLOWED_AS_MAIN_AGGREGATION_TYPE);
                }
                else {
                    switch (aggregationModel.aggType) {
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
        }
        return aggregationBuilder;
    }
    public FluidSearch aggregate(List<String> aggregations, Boolean isGeoAggregate) throws ArlasException{
        AggregationBuilder aggregationBuilder = null;
        aggregationBuilder = aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, 0);
        searchRequestBuilder =searchRequestBuilder.setSize(0).addAggregation(aggregationBuilder);
        return this;
    }

    private DateHistogramAggregationBuilder buildDateHistogramAggregation(AggregationModel aggregationModel) throws ArlasException{
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(DATEHISTOGRAM_AGG);
        // Get the interval
        DateAggregationInterval dateAggregationInterval = ParamsParser.getAggregationDateInterval(aggregationModel.aggInterval);
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
    private GeoGridAggregationBuilder buildGeohashAggregation(AggregationModel aggregationModel) throws ArlasException{
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid(GEOHASH_AGG);
        //get the precision
        Integer precision = ParamsParser.getAggregationGeohasPrecision(aggregationModel.aggInterval);
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoGridAggregationBuilder)setAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    // construct and returns the histogram aggregationModel builder
    private HistogramAggregationBuilder buildHistogramAggregation(AggregationModel aggregationModel) throws ArlasException {
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram(HISTOGRAM_AGG);
        // get the length
        Double length = ParamsParser.getAggregationHistogramLength(aggregationModel.aggInterval);
        histogramAggregationBuilder = histogramAggregationBuilder.interval(length);
        //get the field, format, collect_field, collect_fct, order, on
        histogramAggregationBuilder = (HistogramAggregationBuilder)setAggregationParameters(aggregationModel, histogramAggregationBuilder);
        return histogramAggregationBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private TermsAggregationBuilder buildTermsAggregation(AggregationModel aggregationModel) throws ArlasException {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(TERM_AGG);
        //get the field, format, collect_field, collect_fct, order, on
        if(aggregationModel.aggInterval != null){
            throw new BadRequestException(NO_TERM_INTERVAL);
        }
        termsAggregationBuilder = (TermsAggregationBuilder)setAggregationParameters(aggregationModel, termsAggregationBuilder);
        return termsAggregationBuilder;
    }

    private ValuesSourceAggregationBuilder setAggregationParameters (AggregationModel aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException{
        String aggField = aggregationModel.aggField;
        aggregationBuilder = aggregationBuilder.field(aggField);
        //Get the format
        String format = ParamsParser.getValidAggregationFormat(aggregationModel.aggFormat);
        if (aggregationBuilder instanceof DateHistogramAggregationBuilder){
            aggregationBuilder = aggregationBuilder.format(format);
        }
        else if (aggregationModel.aggFormat != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        // sub aggregate with a metric aggregationModel
        ValuesSourceAggregationBuilder.LeafOnly metricAggregation = null;
        if(aggregationModel.aggCollectField != null && aggregationModel.aggCollectFct != null) {
            switch (aggregationModel.aggCollectFct) {
                case MetricAggregationType.AVG:
                    metricAggregation = AggregationBuilders.avg("avg").field(aggregationModel.aggCollectField);
                    break;
                case MetricAggregationType.CARDINALITY:
                    metricAggregation = AggregationBuilders.cardinality("cardinality").field(aggregationModel.aggCollectField);
                    break;
                case MetricAggregationType.MAX:
                    metricAggregation = AggregationBuilders.max("max").field(aggregationModel.aggCollectField);
                    break;
                case MetricAggregationType.MIN:
                    metricAggregation = AggregationBuilders.min("min").field(aggregationModel.aggCollectField);
                    break;
                case MetricAggregationType.SUM:
                    metricAggregation = AggregationBuilders.sum("sum").field(aggregationModel.aggCollectField);
                    break;
                default: throw new InvalidParameterException(aggregationModel.aggCollectFct + " function is invalid.");
            }
            if (metricAggregation != null){
                aggregationBuilder.subAggregation(metricAggregation);
            }
        }
        else if (aggregationModel.aggCollectField != null && aggregationModel.aggCollectFct == null){
            throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
        }
        else if (aggregationModel.aggCollectField == null && aggregationModel.aggCollectFct != null){
            throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
        }
        if (aggregationModel.aggSize != null){
            Integer s = ParamsParser.getValidAggregationSize(aggregationModel.aggSize);
            if (aggregationBuilder instanceof TermsAggregationBuilder)
                aggregationBuilder = ((TermsAggregationBuilder) aggregationBuilder).size(s);
            else if (aggregationBuilder instanceof GeoGridAggregationBuilder)
                aggregationBuilder = ((GeoGridAggregationBuilder) aggregationBuilder).size(s);
            else
                throw new BadRequestException(NO_SIZE_TO_SPECIFY);
        }
        setOrder(aggregationModel,aggregationBuilder, metricAggregation);
        return aggregationBuilder;
    }

    private void setOrder(AggregationModel aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder, ValuesSourceAggregationBuilder.LeafOnly metricAggregation) throws ArlasException{
        String order = aggregationModel.aggOrder;
        String on = aggregationModel.aggOn;
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
        searchRequestBuilder = client.prepareSearch(collectionReference.params.indexName);
    }
}
