package io.arlas.server.core;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.rest.explore.enumerations.AggregationType;
import io.arlas.server.rest.explore.enumerations.DateInterval;
import io.arlas.server.utils.CheckParams;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.builders.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FluidSearch {

    public static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    public static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'gte', 'gt', 'lte' or 'lt'. ";
    public static final String INVALID_VALUE_TYPE = "Operand must be a numeric value. ";
    public static final String INVALID_WKT = "Invalid WKT geometry. ";
    public static final String INVALID_BBOX = "Invalid BBOX";

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
        //System.out.println("QUERY:"+searchRequestBuilder.toString()); // TODO : mettre des logs en debug pour les queries
        SearchResponse result = searchRequestBuilder.get();
        return result;
    }

    public FluidSearch filter(List<String> f) throws ArlasException {
         for (int i = 0; i < f.size(); i++) {
             if(f.get(i)!=null && !f.get(i).isEmpty()) {
                 String operands[] = f.get(i).split(":");
                 int operandsNumber = operands.length;
                 if (operandsNumber < 2 || operandsNumber > 3) {
                     throw new InvalidParameterException(INVALID_PARAMETER_F);
                 } else if (operandsNumber == 2) {
                     boolQueryBuilder = boolQueryBuilder.filter(
                             QueryBuilders.matchQuery(operands[0], operands[1])
                     );
                 } else if (operandsNumber == 3) {
                     //TODO: test if operands[2] is null
                     Integer fieldValue = tryParse(operands[2]);
                     if (fieldValue != null) {
                         if (operands[1].equals("gte")) {
                             boolQueryBuilder = boolQueryBuilder.filter(
                                     QueryBuilders.rangeQuery(operands[0])
                                             .gte(fieldValue)
                             );
                         } else if (operands[1].equals("gt")) {
                             boolQueryBuilder = boolQueryBuilder.filter(
                                     QueryBuilders.rangeQuery(operands[0])
                                             .gt(fieldValue)
                             );
                         } else if (operands[1].equals("lte")) {
                             boolQueryBuilder = boolQueryBuilder.filter(
                                     QueryBuilders.rangeQuery(operands[0])
                                             .lte(fieldValue)
                             );
                         } else if (operands[1].equals("lt")) {
                             boolQueryBuilder = boolQueryBuilder.filter(
                                     QueryBuilders.rangeQuery(operands[0])
                                             .lt(fieldValue)
                             );
                         } else throw new InvalidParameterException(INVALID_OPERATOR);
                     } else throw new InvalidParameterException(INVALID_VALUE_TYPE);
                 }
             }
        }
        return this;
    }

    public FluidSearch filterQ (String q){
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.simpleQueryStringQuery(q).defaultOperator(Operator.AND)
        );
        return this;
    }

    public FluidSearch filterAfter (Long after){
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.rangeQuery(collectionReference.params.timestampPath)
                        .gte(after)
        );
        return this;
    }

    public FluidSearch filterBefore (Long before){
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.rangeQuery(collectionReference.params.timestampPath)
                        .lte(before)
        );
        return this;
    }
    
    public FluidSearch filterPWithin(double top, double left, double bottom, double right) throws ArlasException, IOException {
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(top, left, bottom, right)
        );
        return this;
    }

    public FluidSearch filterNotPWithin(double top, double left, double bottom, double right) throws ArlasException, IOException {
        boolQueryBuilder = boolQueryBuilder.mustNot(
                QueryBuilders.geoBoundingBoxQuery(collectionReference.params.centroidPath).setCorners(top, left, bottom, right)
        );
        return this;
    }

    public FluidSearch filterGWithin(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, shapeBuilder)
        );
        return this;
    }

    public FluidSearch filterNotGWithin(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder.mustNot(
                QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, shapeBuilder)
        );
        return this;
    }

    public FluidSearch filterGIntersect(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.geoIntersectionQuery(collectionReference.params.geometryPath, shapeBuilder)
        );
        return this;
    }

    public FluidSearch filterNotGIntersect(String geometry) throws ArlasException, IOException {
        ShapeBuilder shapeBuilder = getShapeBuilder(geometry);
        boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.geoDisjointQuery(collectionReference.params.geometryPath, shapeBuilder)
        );
        return this;
    }

    public FluidSearch include(String include){
        if (include != null){
            String includeFields[] = include.split(",");
            searchRequestBuilder = searchRequestBuilder.setFetchSource(includeFields,null);
        }
        return this;
    }

    public FluidSearch exclude(String exclude){
        if (exclude != null){
            String excludeFields[] = exclude.split(",");
            searchRequestBuilder = searchRequestBuilder.setFetchSource(null,excludeFields);
        }
        return this;
    }

    public FluidSearch filterSize (Integer size, Integer from){
        searchRequestBuilder = searchRequestBuilder.setSize(size).setFrom(from);
        return this;
    }

    public FluidSearch sort (String sort) throws ArlasException{
        List<String> fieldList = Arrays.asList(sort.split(","));
        String field;
        SortOrder sortOrder;
        for (String signedField : fieldList){
            if (!signedField.equals("") ) {
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

    public FluidSearch aggregate(String agg, String[] aggField, String[] aggInterval, String aggFormat) throws ArlasException{
        if (agg.equals(AggregationType.datehistogram.toString())){
            DateHistogramAggregationBuilder dateHistogramAggregationBuilder = constructDateHistogramAggregation(aggField,aggInterval,aggFormat);
            searchRequestBuilder = searchRequestBuilder.addAggregation(dateHistogramAggregationBuilder);
        }
        else if (agg.equals(AggregationType.geohash.toString())){
            GeoGridAggregationBuilder geoHashAggregationBuilder = constructGeoHashAggregation(aggField,aggInterval);
            searchRequestBuilder = searchRequestBuilder.addAggregation(geoHashAggregationBuilder);
        }
        else if (agg.equals(AggregationType.histogram.toString())){
            HistogramAggregationBuilder histogramAggregationBuilder = construcHistogramAggregation(aggField, aggInterval);
            searchRequestBuilder = searchRequestBuilder.addAggregation(histogramAggregationBuilder);
        }
        return this;
    }

    // construct and returns the dateHistogram aggregation builder
    private DateHistogramAggregationBuilder constructDateHistogramAggregation(String[] aggField, String[] aggInterval, String aggFormat) throws ArlasException{
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("Agg");
        if(aggInterval.length != 0){
            for(int i=0; i<aggInterval.length; i++){
                Map<Integer,String> intervalDateMap = CheckParams.getValidAggDateInterval(aggInterval[i]);
                if(intervalDateMap != null) {
                    Integer size = (intervalDateMap.keySet()).iterator().next();
                    if (intervalDateMap.get(size).equals(DateInterval.year.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.YEAR);
                    } else if (intervalDateMap.get(size).equals(DateInterval.quarter.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.QUARTER);
                    } else if (intervalDateMap.get(size).equals(DateInterval.month.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.MONTH);
                    } else if (intervalDateMap.get(size).equals(DateInterval.week.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.weeks(size));
                    } else if (intervalDateMap.get(size).equals(DateInterval.day.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.days(size));
                    } else if (intervalDateMap.get(size).equals(DateInterval.hour.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.hours(size));
                    } else if (intervalDateMap.get(size).equals(DateInterval.minute.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.minutes(size));
                    } else if (intervalDateMap.get(size).equals(DateInterval.second.toString())) {
                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.dateHistogramInterval(DateHistogramInterval.seconds(size));
                    }
                }
            }
        }
        if (aggField.length != 0){
            for ( int i=0; i<aggField.length; i++){
                dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.field(aggField[i]);
            }
        }
        //TODO: checkParams Format
        if (!aggFormat.equals("")){
            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.format(aggFormat);
        }
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregation builder
    private GeoGridAggregationBuilder constructGeoHashAggregation(String[] aggField, String[] aggInterval) throws ArlasException{
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid("Aggregation");
        //TODO: interval (precision in this cas) not multiple in this case ?
        if(aggInterval.length != 0){
            for(int i=0; i<aggInterval.length; i++){
                Integer precision = CheckParams.getValidGeoHashPrecision(aggInterval[i]);
                if (precision != null){
                    geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
                }
            }
        }
        //TODO: field not multiple in this case ? only one geom ?
        if (aggField.length != 0){
            for ( int i=0; i<aggField.length; i++){
                geoHashAggregationBuilder = geoHashAggregationBuilder.field(aggField[i]);
            }
        }
        return geoHashAggregationBuilder;
    }

    // construct and returns the histogram aggregation builder
    private HistogramAggregationBuilder construcHistogramAggregation(String[] aggField, String[] aggInterval) throws ArlasException{
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram("Aggregation");
        //TODO: interval (precision in this cas) not multiple in this case ?
        if(aggInterval.length != 0){
            for(int i=0; i<aggInterval.length; i++){
                Double interval = CheckParams.getValidHistogramInterval(aggInterval[i]);
                if (interval != null){
                    histogramAggregationBuilder = histogramAggregationBuilder.interval(interval);
                }
            }
        }
        //TODO: field not multiple in this case ? only one geom ?
        if (aggField.length != 0){
            for ( int i=0; i<aggField.length; i++){
                histogramAggregationBuilder = histogramAggregationBuilder.field(aggField[i]);
            }
        }
        return histogramAggregationBuilder;
    }

    private Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Geometry readWKT(String geometry) throws ArlasException{
        WKTReader wkt  = new WKTReader();
        Geometry polygon = null;
        try {
            polygon = wkt.read(geometry);
        }catch (ParseException ex){
            throw new InvalidParameterException(INVALID_WKT);
        }
        return polygon;
    }

    private PolygonBuilder createPolygonBuilder( Polygon polygon){
        //TODO: add interior holes
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        coordinatesBuilder.coordinates(polygon.getCoordinates());
        return new PolygonBuilder( coordinatesBuilder, ShapeBuilder.Orientation.LEFT);
    }

    private MultiPolygonBuilder createMultiPolygonBuilder( MultiPolygon multiPolygon){
        MultiPolygonBuilder multiPolygonBuilder = new MultiPolygonBuilder(ShapeBuilder.Orientation.LEFT);
        for (int i = 0; i<multiPolygon.getNumGeometries(); i++){
            multiPolygonBuilder.polygon(createPolygonBuilder((Polygon)multiPolygon.getGeometryN(i)));
        }
        return multiPolygonBuilder;
    }

    private LineStringBuilder createLineStringBuilder(LineString lineString){
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        coordinatesBuilder.coordinates(lineString.getCoordinates());
        return new LineStringBuilder( coordinatesBuilder);
    }

    private PointBuilder createPointBuilder(Point point){
        PointBuilder pointBuilder = new PointBuilder();
        pointBuilder.coordinate(point.getCoordinate());
        return pointBuilder;
    }


    private ShapeBuilder getShapeBuilder(String geometry) throws ArlasException{
        //TODO: multilinestring
        Geometry wktGeometry = readWKT(geometry);
        if ( wktGeometry != null){
            String geometryType = wktGeometry.getGeometryType().toUpperCase();
            switch (geometryType){
                case "POLYGON" : return createPolygonBuilder((Polygon)wktGeometry);
                case "MULTIPOLYGON" : return createMultiPolygonBuilder((MultiPolygon) wktGeometry);
                case "LINESTRING" : return createLineStringBuilder((LineString)wktGeometry);
                case "POINT" : return createPointBuilder((Point)wktGeometry);
                default : throw new InvalidParameterException("The given geometry is not handled.");
            }
        }
        throw new InvalidParameterException("The given geometry is invalid.");
    }

    public void setCollectionReference(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
        searchRequestBuilder = client.prepareSearch(collectionReference.params.indexName);
    }
}
