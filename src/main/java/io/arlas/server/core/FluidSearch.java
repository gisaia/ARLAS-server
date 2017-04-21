package io.arlas.server.core;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
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
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
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
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FluidSearch {

    private static final String INVALID_PARAMETER_F = "Parameter f does not respect operation expression. ";
    private static final String INVALID_OPERATOR = "Operand does not equal one of the following values : 'gte', 'gt', 'lte' or 'lt'. ";
    private static final String INVALID_VALUE_TYPE = "Operand must be a numeric value. ";
    private static final String INVALID_WKT = "Invalid WKT geometry. ";
    private static final String INVALID_POLYGON_TYPE = "The geometry type is not a simple polygon. ";
    private static final String DATEHISTOGRAM_AGGREGATION_NAME = "Datehistogram";
    private static final String HISTOGRAM_AGGREGATION_NAME = "Histogram";
    private static final String GEOHASH_AGGREGATION_NAME = "Geohash";
    private static final String TERM_AGGREGATION_NAME = "Term";


    private TransportClient client;
    private SearchRequestBuilder searchRequestBuilder;
    private BoolQueryBuilder boolQueryBuilder;
    private CollectionReference collectionReference;

    public FluidSearch(TransportClient client) {
        //TODO: initialize collectionReference
        this.client = client;
        boolQueryBuilder = QueryBuilders.boolQuery();
    }

    public FluidSearch(TransportClient client, CollectionReference collectionReference) {
        //TODO: initialize collectionReference
        this.client = client;
        this.collectionReference = collectionReference;
        searchRequestBuilder = client.prepareSearch(collectionReference.params.indexName);
        boolQueryBuilder = QueryBuilders.boolQuery();

    }


    public SearchResponse exec() {
        SearchResponse result = searchRequestBuilder.setQuery(boolQueryBuilder).get();
        return result;
    }

    public FluidSearch filter(List<String> f) throws ArlasException {
         for (int i = 0; i < f.size(); i++) {
            String operands[] = f.get(i).split(":");
            int operandsNumber = operands.length;
            if (operandsNumber<2 || operandsNumber>3){
                throw new InvalidParameterException(INVALID_PARAMETER_F);
            }
            else if (operandsNumber == 2) {
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
                }
                else throw new InvalidParameterException(INVALID_VALUE_TYPE);
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

    public FluidSearch filterPWithin(List<String> geometry) throws ArlasException, IOException {
        for (int i=0; i<geometry.size(); i++){
            Geometry polygon = readWKT(geometry.get(i));
            if(polygon != null){
                if (CheckParams.isSimplePolygon(polygon)){
                    PointBuilder pointBuilder = new PointBuilder();
                    pointBuilder.coordinate(((Polygon)polygon).getCentroid().getCoordinate());
                    boolQueryBuilder = boolQueryBuilder.filter(
                            QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, pointBuilder)
                    );
                }
                else {
                    throw new InvalidParameterException(INVALID_POLYGON_TYPE);
                }
            }
        }
        return this;
    }

    public FluidSearch filterNotPWithin(List<String> geometry) throws ArlasException, IOException {
        for (int i=0; i<geometry.size(); i++){
            Geometry polygon = readWKT(geometry.get(i));
            if(polygon != null){
                if (CheckParams.isSimplePolygon(polygon)){
                    PointBuilder pointBuilder = new PointBuilder();
                    pointBuilder.coordinate(((Polygon)polygon).getCentroid().getCoordinate());
                    boolQueryBuilder = boolQueryBuilder.mustNot(
                            QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, pointBuilder)
                    );
                }
                else {
                    throw new InvalidParameterException(INVALID_POLYGON_TYPE);
                }
            }
        }
        return this;
    }

    public FluidSearch filterGWithin(List<String> geometry) throws ArlasException, IOException {
        for (int i=0; i<geometry.size(); i++){
            Geometry polygon = readWKT(geometry.get(i));
            if (polygon != null) {
                if (CheckParams.isSimplePolygon(polygon)) {
                    PolygonBuilder polygonBuilder = createPolygonBuilder((Polygon) polygon);
                    boolQueryBuilder = boolQueryBuilder.filter(
                            QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, polygonBuilder)
                    );
                } else {
                    throw new InvalidParameterException(INVALID_POLYGON_TYPE);
                }
            }
        }
        return this;
    }

    public FluidSearch filterNotGWithin(List<String> geometry) throws ArlasException, IOException {
        for (int i=0; i<geometry.size(); i++){
            Geometry polygon = readWKT(geometry.get(i));
            if (polygon != null) {
                if (CheckParams.isSimplePolygon(polygon)) {
                    PolygonBuilder polygonBuilder = createPolygonBuilder((Polygon) polygon);
                    boolQueryBuilder = boolQueryBuilder.mustNot(
                            QueryBuilders.geoWithinQuery(collectionReference.params.geometryPath, polygonBuilder)
                    );
                } else {
                    throw new InvalidParameterException(INVALID_POLYGON_TYPE);
                }
            }
        }
        return this;
    }

    public FluidSearch filterGIntersect(List<String> geometry) throws ArlasException, IOException {
        for (int i=0; i<geometry.size(); i++){
            Geometry polygon = readWKT(geometry.get(i));
            if(polygon != null){
                if (CheckParams.isSimplePolygon(polygon)){
                    PolygonBuilder polygonBuilder = createPolygonBuilder((Polygon)polygon);
                    boolQueryBuilder = boolQueryBuilder.filter(
                            QueryBuilders.geoIntersectionQuery(collectionReference.params.geometryPath, polygonBuilder)
                    );
                }
                else {
                    throw new InvalidParameterException(INVALID_POLYGON_TYPE);
                }
            }
        }
        return this;
    }

    public FluidSearch filterNotGIntersect(List<String> geometry) throws ArlasException, IOException {
        for (int i=0; i<geometry.size(); i++){
            Geometry polygon = readWKT(geometry.get(i));
            if(polygon != null){
                if (CheckParams.isSimplePolygon(polygon)){
                    PolygonBuilder polygonBuilder = createPolygonBuilder((Polygon)polygon);
                    boolQueryBuilder = boolQueryBuilder.filter(
                            QueryBuilders.geoDisjointQuery(collectionReference.params.geometryPath, polygonBuilder)
                    );
                }
                else {
                    throw new InvalidParameterException(INVALID_POLYGON_TYPE);
                }
            }
        }
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

    public FluidSearch sort (String[] sort) throws ArlasException{
        if(sort.length>0){
            for (int i=0; i<sort.length; i++){
                String[] sortOperands = CheckParams.checkSortParam(sort[i]);
                if (sortOperands[2].equals(SortOrder.ASC)){
                    searchRequestBuilder = searchRequestBuilder.addSort(sortOperands[0],SortOrder.ASC);
                }
                else if (sortOperands[2].equals(SortOrder.DESC)){
                    searchRequestBuilder = searchRequestBuilder.addSort(sortOperands[0],SortOrder.DESC);
                }
            }
        }
        return this;
    }
    //TODO: finish aggregation implementation
    /*public FluidSearch aggregate(List<String> aggregations) throws ArlasException{
        GlobalAggregationBuilder globalAggregationBuilder = AggregationBuilders.global("agg");

        for (String agg : aggregations){
            //check the agg syntax is correct
            if (CheckParams.isAggregationParamValid(agg)){
                String[] aggParts = agg.split(":");
                AggregationBuilder aggregationBuilder;
                switch (aggParts[0]){
                    case AggregationType.DATEHISTOGRAM : aggregationBuilder = buildDateHistogramAggregation(aggParts);
                        ;
                    case AggregationType.GEOHASH : ;
                    case AggregationType.HISTOGRAM : ;
                    case AggregationType.TERM : ;
                }
                

            };
           // DateHistogramAggregationBuilder dateHistogramAggregationBuilder = constructDateHistogramAggregation(aggField,aggInterval,aggFormat);
           // globalAggregationBuilder = globalAggregationBuilder.subAggregation(dateHistogramAggregationBuilder

            //);
        }
        searchRequestBuilder =searchRequestBuilder.addAggregation(globalAggregationBuilder);

        return this;
    }*/
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


   /* private DateHistogramAggregationBuilder buildDateHistogramAggregation(String[] aggParts) throws ArlasException{
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(DATEHISTOGRAM_AGGREGATION_NAME);

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
    }*/
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
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        coordinatesBuilder.coordinates(polygon.getCoordinates());
        return new PolygonBuilder( coordinatesBuilder, ShapeBuilder.Orientation.LEFT);
    }

    public CollectionReference getCollectionReference() {
        return collectionReference;
    }

    public void setCollectionReference(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
    }
}
