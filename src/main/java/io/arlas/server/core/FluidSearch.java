package io.arlas.server.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.arlas.server.model.Aggregation;
import io.arlas.server.rest.explore.enumerations.MetricAggregationType;
import io.arlas.server.utils.DateAggregationInterval;
import io.arlas.server.utils.ParamsParser;
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
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.rest.explore.enumerations.AggregationType;
import io.arlas.server.rest.explore.enumerations.DateInterval;
import io.arlas.server.utils.CheckParams;

public class FluidSearch {

    public static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    public static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'gte', 'gt', 'lte' or 'lt'. ";
    public static final String INVALID_VALUE_TYPE = "Operand must be a numeric value. ";
    public static final String INVALID_WKT = "Invalid WKT geometry. ";
    public static final String INVALID_BBOX = "Invalid BBOX";
    public static final String INVALID_BEFORE_AFTER = "Invalid date parameters : before and after must be positive and before must be greater than after.";
    public static final String INVALID_SIZE = "Invalid size parameter.";
    public static final String INVALID_FROM = "Invalid from parameter.";

    private TransportClient client;
    private SearchRequestBuilder searchRequestBuilder;
    private BoolQueryBuilder boolQueryBuilder;
    private CollectionReference collectionReference;

    public FluidSearch(TransportClient client) {
        this.client = client;
        boolQueryBuilder = QueryBuilders.boolQuery();
    }

    public SearchResponse exec() {
        searchRequestBuilder.setQuery(boolQueryBuilder);
        // System.out.println("QUERY:"+searchRequestBuilder.toString()); // TODO
        // : mettre des logs en debug pour les queries
        SearchResponse result = searchRequestBuilder.get();
        return result;
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
                    // TODO: test if operands[2] is null
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

    //TODO: finish aggregation implementation
    public FluidSearch aggregate(List<String> aggregations) throws ArlasException{
        GlobalAggregationBuilder globalAggregationBuilder = AggregationBuilders.global("agg");
        for (String agg : aggregations){
            //check the agg syntax is correct
            AggregationBuilder aggregationBuilder = null;
            if (CheckParams.isAggregationParamValid(agg)){
                List<String> aggParameters = Arrays.asList(agg.split(":"));
                Aggregation aggregation = ParamsParser.getAggregation(aggParameters);
                switch (aggregation.aggType){
                    case AggregationType.DATEHISTOGRAM : aggregationBuilder = buildDateHistogramAggregation(aggregation);break;
                    case AggregationType.GEOHASH : aggregationBuilder = buildGeohashAggregation(aggregation);break;
                }
            }
            globalAggregationBuilder = globalAggregationBuilder.subAggregation(aggregationBuilder);
        }
        searchRequestBuilder =searchRequestBuilder.addAggregation(globalAggregationBuilder);
        return this;
    }

    private DateHistogramAggregationBuilder buildDateHistogramAggregation(Aggregation aggregation) throws ArlasException{
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("DateHistogram Aggregation");

        // Get the interval
        DateAggregationInterval dateAggregationInterval = ParamsParser.getAggregationDateInterval(aggregation.aggInterval);
        DateHistogramInterval dateHistogramInterval = null;
        Integer aggsize = dateAggregationInterval.aggsize;
        switch (dateAggregationInterval.aggunit){
            case DateInterval.YEAR : dateHistogramInterval = DateHistogramInterval.YEAR; break;
            case DateInterval.QUARTER : dateHistogramInterval = DateHistogramInterval.QUARTER; break;
            case DateInterval.MONTH : dateHistogramInterval = DateHistogramInterval.MONTH; break;
            case DateInterval.WEEK : dateHistogramInterval = DateHistogramInterval.weeks(aggsize); break;
            case DateInterval.DAY : dateHistogramInterval = DateHistogramInterval.days(aggsize); break;
            case DateInterval.HOUR : dateHistogramInterval = DateHistogramInterval.hours(aggsize); break;
            case DateInterval.MINUTE : dateHistogramInterval = DateHistogramInterval.minutes(aggsize); break;
            case DateInterval.SECOND : dateHistogramInterval = DateHistogramInterval.seconds(aggsize); break;
            default : throw new InvalidParameterException("Invalid date unit");
        }
        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(dateHistogramInterval);
        //get the field, format, collect_field, collect_fct, order, on
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder)buildCommonAggregationParameters(aggregation, dateHistogramAggregationBuilder);
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregation builder
    private GeoGridAggregationBuilder buildGeohashAggregation(Aggregation aggregation) throws ArlasException{
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid("Geohashgrid Aggregation");
        //get the precision
        Integer precision = ParamsParser.getAggregationGeohasPrecision(aggregation.aggInterval);
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoGridAggregationBuilder)buildCommonAggregationParameters(aggregation, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    private ValuesSourceAggregationBuilder buildCommonAggregationParameters (Aggregation aggregation, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException{
        String aggField = aggregation.aggField;
        if (aggField != null){
            aggregationBuilder = aggregationBuilder.field(aggField);
        }
        else throw new InvalidParameterException("Date field is not specified");
        //Get the format
        String format = ParamsParser.getValidAggregationFormat(aggregation.aggFormat);
        aggregationBuilder = aggregationBuilder.format(format);
        // sub aggregate with a metric aggregation
        ValuesSourceAggregationBuilder.LeafOnly metricAggregation = null;
        switch (aggregation.aggCollectFct){
            case MetricAggregationType.AVG : metricAggregation = AggregationBuilders.avg("avg").field(aggregation.aggCollectField);break;
            case MetricAggregationType.CARDINALITY : metricAggregation = AggregationBuilders.cardinality("cardinality").field(aggregation.aggCollectField);break;
            case MetricAggregationType.MAX : metricAggregation = AggregationBuilders.max("max").field(aggregation.aggCollectField);break;
            case MetricAggregationType.MIN : metricAggregation = AggregationBuilders.min("min").field(aggregation.aggCollectField);break;
            case MetricAggregationType.SUM : metricAggregation = AggregationBuilders.avg("sum").field(aggregation.aggCollectField);break;
            default : //TODO: exception ?;
        }
        //TODO : order and on for later
        if (metricAggregation != null){
            aggregationBuilder.subAggregation(metricAggregation);
        }
        return aggregationBuilder;
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
