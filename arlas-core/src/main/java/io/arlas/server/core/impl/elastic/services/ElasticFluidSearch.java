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

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.exceptions.*;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.server.core.impl.elastic.utils.ElasticTool;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.*;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.FieldType;
import io.arlas.server.core.model.response.TimestampType;
import io.arlas.server.core.services.FluidSearchService;
import io.arlas.server.core.utils.CheckParams;
import io.arlas.server.core.utils.GeoUtil;
import io.arlas.server.core.utils.ParamsParser;
import io.arlas.server.core.utils.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.Orientation;
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
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static io.arlas.server.core.utils.CheckParams.GEO_AGGREGATION_TYPE_ENUMS;

public class ElasticFluidSearch extends FluidSearchService {
    private static Logger LOGGER = LoggerFactory.getLogger(ElasticFluidSearch.class);

    private ElasticClient client;
    private SearchRequest request;
    private SearchSourceBuilder searchSourceBuilder;
    private BoolQueryBuilder boolQueryBuilder;

    public ElasticFluidSearch(CollectionReference collectionReference) {
        super(collectionReference);
        request = new SearchRequest(collectionReference.params.indexName);
        boolQueryBuilder = QueryBuilders.boolQuery();
        searchSourceBuilder = new SearchSourceBuilder();
    }

    public ElasticClient getClient() {
        return client;
    }

    public ElasticFluidSearch setClient(ElasticClient client) {
        this.client = client;
        return this;
    }

    public BoolQueryBuilder getBoolQueryBuilder() {
        return boolQueryBuilder;
    }

    public SearchResponse exec() throws ArlasException {
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.trackTotalHits(true);

        Pair<String[], String[]> includeExclude = computeIncludeExclude(false);
        searchSourceBuilder = searchSourceBuilder.fetchSource(includeExclude.getLeft(), includeExclude.getRight());

        //Get Elasticsearch response
        LOGGER.debug("QUERY : " + searchSourceBuilder.toString());
        SearchResponse result = null;
        request.source(searchSourceBuilder);
        result = client.search(request);
        LOGGER.debug("RESULT : " + result.toString());
        return result;
    }

    @Override
    public FluidSearchService filter(MultiValueFilter<Expression> f, String dateFormat, Boolean righthand) throws ArlasException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        for (Expression fFilter : f) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(filter(fFilter, dateFormat, righthand));
        }
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return this;
    }

    private BoolQueryBuilder filter(Expression expression, String dateFormat, Boolean righthand) throws ArlasException {
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
                // TODO: if field type is fullText, use matchPhraseQuery instead of regexQuery
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
            case within:
                FieldType wType = collectionReferenceManager.getType(collectionReference, field, true);
                BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
                switch (wType) {
                    case GEO_POINT:
                        for (AbstractQueryBuilder q : filterPWithin(field, value)) {
                            orBoolQueryBuilder = orBoolQueryBuilder.should(q);
                        }
                        break;
                    case GEO_SHAPE:
                        orBoolQueryBuilder = orBoolQueryBuilder.should(filterGWithin(field, value, righthand));
                        break;
                    default:
                        throw new ArlasException("'within' op on field '" + field + "' of type '" + wType + "' is not supported");
                }
                orBoolQueryBuilder.minimumShouldMatch(1);
                ret = ret.filter(orBoolQueryBuilder);
                break;
            case notwithin:
                FieldType type = collectionReferenceManager.getType(collectionReference, field, true);
                BoolQueryBuilder orBoolQueryBuilder2 = QueryBuilders.boolQuery();
                switch (type) {
                    case GEO_POINT:
                        for (AbstractQueryBuilder q : filterNotPWithin(field, value)) {
                            orBoolQueryBuilder2 = orBoolQueryBuilder2.should(q);
                        }
                        break;
                    case GEO_SHAPE:
                        orBoolQueryBuilder2 = orBoolQueryBuilder2.should(filterGWithin(field, value, righthand));
                        break;
                    default:
                        throw new ArlasException("'notwithin' op on field '" + field + "' of type '" + type + "' is not supported");
                }
                ret = ret.mustNot(orBoolQueryBuilder2);
                break;
            case intersects:
                ret = ret.filter(filterGIntersect(field, value, righthand));
                break;
            case notintersects:
                ret = ret.mustNot(filterGIntersect(field, value, righthand));
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

    @Override
    public FluidSearchService filterQ(MultiValueFilter<String> q) throws ArlasException {
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

    public List<AbstractQueryBuilder> filterPWithin(String field, String pwithinFilter) throws ArlasException {
        List<AbstractQueryBuilder> builderList = new ArrayList<>();
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
                for(int i = 0; i< p.getNumGeometries(); i++) {
                    List<Coordinate> coordinates = Arrays.asList(p.getGeometryN(i).getCoordinates());
                    List<GeoPoint> geoPoints = new ArrayList<>();
                    coordinates.forEach(coordinate -> geoPoints.add(new GeoPoint(coordinate.y, coordinate.x)));
                    builderList.add(QueryBuilders.geoPolygonQuery(field, geoPoints));
                }
            } else {
                throw new NotImplementedException("WKT is not supported for 'within' op on field '" + field + "' of type '" + geometryType + "'");
            }
        }
        return builderList;
    }

    private GeoBoundingBoxQueryBuilder filterPWithin(String field, double west, double south, double east, double north) {
        GeoPoint topLeft = new GeoPoint(north, west);
        GeoPoint bottomRight = new GeoPoint(south, east);
        return QueryBuilders.geoBoundingBoxQuery(field).setCorners(topLeft, bottomRight);
    }

    public List<AbstractQueryBuilder> filterNotPWithin(String field, String notpwithinFilter) throws ArlasException {
        List<AbstractQueryBuilder> builderList = new ArrayList<>();
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
                BoolQueryBuilder andQueryBuilder = QueryBuilders.boolQuery();
                for(int i = 0; i< p.getNumGeometries(); i++) {
                    List<Coordinate> coordinates = Arrays.asList(p.getGeometryN(i).getCoordinates());
                    List<GeoPoint> geoPoints = new ArrayList<>();
                    coordinates.forEach(coordinate -> geoPoints.add(new GeoPoint(coordinate.y, coordinate.x)));
                    /** `andQueryBuilder` will allow us to consider a multipolygon as one entity when we apply notpwithin query*/
                    andQueryBuilder = andQueryBuilder.should(QueryBuilders.geoPolygonQuery(field, geoPoints));
                }
                builderList.add(andQueryBuilder);
            } else {
                throw new NotImplementedException(geometryType + " WKT is not supported for `notpwithin`");
            }
        }
        return builderList;
    }

    public GeoShapeQueryBuilder filterGWithin(String field, String geometry, Boolean righthand) throws ArlasException {
        try {
            ShapeBuilder shapeBuilder = getShapeBuilder(geometry, righthand);
            return QueryBuilders.geoWithinQuery(field, shapeBuilder);
        } catch (IOException e) {
            throw new ArlasException("Exception while building geoWithinQuery: " + e.getMessage());
        }
    }

    public GeoShapeQueryBuilder filterGIntersect(String field, String geometry, Boolean righthand) throws ArlasException {
        try {
            ShapeBuilder shapeBuilder = getShapeBuilder(geometry, righthand);
            return QueryBuilders.geoIntersectionQuery(field, shapeBuilder);
        } catch (IOException e) {
            throw new ArlasException("Exception while building geoIntersectionQuery: " + e.getMessage());
        }
    }

    @Override
    public FluidSearchService filterSize(Integer size, Integer from) {
        searchSourceBuilder = searchSourceBuilder.size(size).from(from);
        return this;
    }

    @Override
    public FluidSearchService searchAfter(Page page, String after) {
        searchSourceBuilder = searchSourceBuilder.searchAfter(after.split(","));
        return this;
    }


    @Override
    public FluidSearchService sort(String sort) throws ArlasException {
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
                    searchSourceBuilder = searchSourceBuilder.sort(field, sortOrder);
                }
            }
        }
        return this;
    }

    private void geoDistanceSort(String geoSort, SortOrder sortOrder) throws ArlasException {
        GeoPoint sortOnPoint = ParamsParser.getGeoSortParams(geoSort);
        String geoSortField = collectionReference.params.centroidPath;
        searchSourceBuilder = searchSourceBuilder.sort(SortBuilders.geoDistanceSort(geoSortField, sortOnPoint.lat(), sortOnPoint.lon())
                .order(sortOrder).geoDistance(GeoDistance.PLANE));
    }

    private AggregationBuilder aggregateRecursive(List<Aggregation> aggregations, AggregationBuilder aggregationBuilder, Boolean isGeoAggregate, Integer counter) throws ArlasException {
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
                aggregationBuilder = buildDateHistogramAggregation(aggregationModel);
                break;
            case geohash:
                aggregationBuilder = buildGeohashAggregation(aggregationModel);
                break;
            case geotile:
                aggregationBuilder = buildGeotileAggregation(aggregationModel);
                break;
            case histogram:
                aggregationBuilder = buildHistogramAggregation(aggregationModel);
                break;
            case term:
                aggregationBuilder = buildTermsAggregation(aggregationModel);
                break;
            case h3:
                aggregationBuilder = buildH3Aggregation(aggregationModel);
                break;
        }
        aggregations.remove(0);
        if (aggregations.size() == 0) {
            return aggregationBuilder;
        }
        counter++;
        return aggregationBuilder.subAggregation(aggregateRecursive(aggregations, aggregationBuilder, isGeoAggregate, counter));
    }

    @Override
    public FluidSearchService aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException {
        AggregationBuilder aggregationBuilder = aggregateRecursive(new ArrayList<>(aggregations), null, isGeoAggregate, 0);
        searchSourceBuilder = searchSourceBuilder.size(0).aggregation(aggregationBuilder);
        return this;
    }

    public FluidSearchService getFieldRange(String field) {
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.existsQuery(field));
        MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min(FIELD_MIN_VALUE).field(field);
        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max(FIELD_MAX_VALUE).field(field);
        searchSourceBuilder = searchSourceBuilder.size(0).aggregation(minAggregationBuilder).aggregation(maxAggregationBuilder);
        return this;
    }

    @Override
    public FluidSearchService compute(String field, ComputationEnum metric) {
        boolQueryBuilder = boolQueryBuilder.filter(QueryBuilders.existsQuery(field));
        switch (metric) {
            case AVG:
                AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg(FIELD_AVG_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(avgAggregationBuilder);
                break;
            case MAX:
                MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max(FIELD_MAX_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(maxAggregationBuilder);
                break;
            case MIN:
                MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min(FIELD_MIN_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(minAggregationBuilder);
                break;
            case SUM:
                SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum(FIELD_SUM_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(sumAggregationBuilder);
            case CARDINALITY:
                CardinalityAggregationBuilder cardinalityAggregationBuilder = AggregationBuilders.cardinality(FIELD_CARDINALITY_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(cardinalityAggregationBuilder);
                break;
            case SPANNING:
                minAggregationBuilder = AggregationBuilders.min(FIELD_MIN_VALUE).field(field);
                maxAggregationBuilder = AggregationBuilders.max(FIELD_MAX_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(minAggregationBuilder).aggregation(maxAggregationBuilder);
                break;
            case GEOBBOX:
                GeoBoundsAggregationBuilder geoBoundsAggregationBuilder = AggregationBuilders.geoBounds(FIELD_GEOBBOX_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(geoBoundsAggregationBuilder);
                break;
            case GEOCENTROID:
                GeoCentroidAggregationBuilder geoCentroidAggregationBuilder = AggregationBuilders.geoCentroid(FIELD_GEOCENTROID_VALUE).field(field);
                searchSourceBuilder = searchSourceBuilder.size(0).aggregation(geoCentroidAggregationBuilder);
                break;
        }
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
        if ((Integer)aggregationModel.interval.value > 1) {
            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.fixedInterval(intervalUnit);
        } else {
            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.calendarInterval(intervalUnit);
        }
        //get the field, format, collect_field, collect_fct, order, on
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setAggregationParameters(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setHitsToFetch(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setAggregatedGeometries(aggregationModel, dateHistogramAggregationBuilder);
        dateHistogramAggregationBuilder = (DateHistogramAggregationBuilder) setRawGeometries(aggregationModel, dateHistogramAggregationBuilder);
        return dateHistogramAggregationBuilder;
    }

    // construct and returns the geohash aggregationModel builder
    private GeoGridAggregationBuilder buildGeohashAggregation(Aggregation aggregationModel) throws ArlasException {
        GeoGridAggregationBuilder geoHashAggregationBuilder = AggregationBuilders.geohashGrid(GEOHASH_AGG);
        //get the precision
        Integer precision = (Integer)aggregationModel.interval.value;
        geoHashAggregationBuilder = geoHashAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setAggregationParameters(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setAggregatedGeometries(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setRawGeometries(aggregationModel, geoHashAggregationBuilder);
        geoHashAggregationBuilder = (GeoGridAggregationBuilder) setHitsToFetch(aggregationModel, geoHashAggregationBuilder);
        return geoHashAggregationBuilder;
    }

    // construct and returns the geotile aggregationModel builder
    private GeoGridAggregationBuilder buildGeotileAggregation(Aggregation aggregationModel) throws ArlasException {
        GeoGridAggregationBuilder geoTileAggregationBuilder = AggregationBuilders.geotileGrid(GEOTILE_AGG);
        //get the precision
        Integer precision = (Integer)aggregationModel.interval.value;
        geoTileAggregationBuilder = geoTileAggregationBuilder.precision(precision);
        //get the field, format, collect_field, collect_fct, order, on
        geoTileAggregationBuilder = (GeoGridAggregationBuilder) setAggregationParameters(aggregationModel, geoTileAggregationBuilder);
        geoTileAggregationBuilder = (GeoGridAggregationBuilder) setAggregatedGeometries(aggregationModel, geoTileAggregationBuilder);
        geoTileAggregationBuilder = (GeoGridAggregationBuilder) setRawGeometries(aggregationModel, geoTileAggregationBuilder);
        geoTileAggregationBuilder = (GeoGridAggregationBuilder) setHitsToFetch(aggregationModel, geoTileAggregationBuilder);
        return geoTileAggregationBuilder;
    }

    // construct and returns the h3 aggregationModel builder
    private TermsAggregationBuilder buildH3Aggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(H3_AGG);
        aggregationModel.field = aggregationModel.field + "." + aggregationModel.interval.value;
        if (aggregationModel.size == null) {
            aggregationModel.size = "10000"; // by default
        }
        //get the field, format, collect_field, collect_fct, order, on
        termsAggregationBuilder = (TermsAggregationBuilder) setAggregationParameters(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregationBuilder) setAggregatedGeometries(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregationBuilder) setRawGeometries(aggregationModel, termsAggregationBuilder);
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

    // construct and returns the histogram aggregationModel builder
    private HistogramAggregationBuilder buildHistogramAggregation(Aggregation aggregationModel) throws ArlasException {
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram(HISTOGRAM_AGG);
        histogramAggregationBuilder = histogramAggregationBuilder.interval((Double)aggregationModel.interval.value);
        //get the field, format, collect_field, collect_fct, order, on
        histogramAggregationBuilder = (HistogramAggregationBuilder) setAggregationParameters(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregationBuilder) setHitsToFetch(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregationBuilder) setAggregatedGeometries(aggregationModel, histogramAggregationBuilder);
        histogramAggregationBuilder = (HistogramAggregationBuilder) setRawGeometries(aggregationModel, histogramAggregationBuilder);
        return histogramAggregationBuilder;
    }

    // construct and returns the terms aggregationModel builder
    private TermsAggregationBuilder buildTermsAggregation(Aggregation aggregationModel) throws ArlasException {
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(TERM_AGG);
        //get the field, format, collect_field, collect_fct, order, on
        termsAggregationBuilder = (TermsAggregationBuilder) setAggregationParameters(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregationBuilder) setAggregatedGeometries(aggregationModel, termsAggregationBuilder);
        termsAggregationBuilder = (TermsAggregationBuilder) setRawGeometries(aggregationModel, termsAggregationBuilder);
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
        aggregationBuilder = aggregationBuilder.field(aggregationModel.field);
        //Get the format
        String format = ParamsParser.getValidAggregationFormat(aggregationModel.format);
        if (aggregationBuilder instanceof DateHistogramAggregationBuilder) {
            aggregationBuilder = aggregationBuilder.format(format);
        } else if (aggregationModel.format != null) {
            throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
        }
        // firstMetricAggregationBuilder is the aggregation builder on which the order aggregation will be applied
        ValuesSourceAggregationBuilder firstMetricAggregationBuilder = null;
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
                        /** We calculate this metric only if it wasn't requested as a geometry to return in `aggregatedGeometries` parameter **/
                        if (!(aggregationModel.aggregatedGeometries != null && aggregationModel.aggregatedGeometries.contains(AggregatedGeometryEnum.CENTROID) && aggregationModel.field.equals(m.collectField))) {
                            metricAggregationBuilder = AggregationBuilders.geoCentroid(CollectionFunction.GEOCENTROID.name().toLowerCase() + ":" + collectField).field(m.collectField);
                        }
                        break;
                    case GEOBBOX:
                        setGeoMetricAggregationCollectField(m);
                        /** We calculate this metric only if it wasn't requested as a geometry to return in `aggregatedGeometries` parameter **/
                        if (!(aggregationModel.aggregatedGeometries != null && aggregationModel.aggregatedGeometries.contains(AggregatedGeometryEnum.BBOX) && aggregationModel.field.equals(m.collectField))) {
                            metricAggregationBuilder = AggregationBuilders.geoBounds(CollectionFunction.GEOBBOX.name().toLowerCase() + ":" + collectField).field(m.collectField);
                        }
                        break;
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

    private ValuesSourceAggregationBuilder setAggregatedGeometries(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException {
        if (aggregationModel.aggregatedGeometries != null) {
            String aggregationGeoField = GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type) ? aggregationModel.field : collectionReference.params.centroidPath;
            aggregationModel.aggregatedGeometries.forEach(ag -> {
                ValuesSourceAggregationBuilder metricAggregation;
                switch (ag) {
                    case BBOX:
                        metricAggregation = AggregationBuilders.geoBounds(AggregatedGeometryEnum.BBOX.value() + AGGREGATED_GEOMETRY_SUFFIX).field(aggregationGeoField);
                        aggregationBuilder.subAggregation(metricAggregation);
                        break;
                    case CENTROID:
                        metricAggregation = AggregationBuilders.geoCentroid(AggregatedGeometryEnum.CENTROID.value() + AGGREGATED_GEOMETRY_SUFFIX).field(aggregationGeoField);
                        aggregationBuilder.subAggregation(metricAggregation);
                        break;
                }
            });
        }
        return aggregationBuilder;
    }

    private ValuesSourceAggregationBuilder setRawGeometries(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder) throws ArlasException {
        if (aggregationModel.rawGeometries != null) {
            Map<String, Set<String>> rgs = new HashMap<>();
            aggregationModel.rawGeometries.forEach(rg -> {
                Set<String> geos = rgs.get(rg.sort);
                if (geos == null) geos = new HashSet<>();
                geos.add(rg.geometry);
                rgs.put(rg.sort, geos);
            });
            for (String sort: rgs.keySet()) {
                String[] includes = rgs.get(sort).stream().toArray(String[]::new);
                TopHitsAggregationBuilder topHitsAggregationBuilder = AggregationBuilders.topHits(RAW_GEOMETRY_SUFFIX + sort).size(1).fetchSource(includes, null);
                for (String field : sort.split(",")) {
                    String unsignedField = (field.startsWith("+") || field.startsWith("-")) ? field.substring(1) : field;
                    ElasticTool.checkAliasMappingFields(client, collectionReference.params.indexName, unsignedField);
                    if (field.startsWith("+") || !field.startsWith("-")) {
                        topHitsAggregationBuilder.sort(unsignedField, SortOrder.ASC);
                    } else {
                        topHitsAggregationBuilder.sort(unsignedField, SortOrder.DESC);
                    }
                }
                aggregationBuilder.subAggregation(topHitsAggregationBuilder);
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
                    ElasticTool.checkAliasMappingFields(client, collectionReference.params.indexName, unsignedField);
                    includes.add(unsignedField);
                    /** For geo-fields, we don't sort them. Sorting geo-fields need to be according a given point to calculate a geo-distance
                     * which is not supported in the syntax of fetch_hits*/
                    if (CollectionReferenceManager.getInstance().getType(collectionReference, unsignedField, false) != FieldType.GEO_POINT && CollectionReferenceManager.getInstance().getType(collectionReference, unsignedField, false) != FieldType.GEO_SHAPE) {
                        if (field.startsWith("+") || !field.startsWith("-")) {
                            topHitsAggregationBuilder.sort(unsignedField, SortOrder.ASC);
                        } else {
                            topHitsAggregationBuilder.sort(unsignedField, SortOrder.DESC);
                        }
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
        FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, metric.collectField, true);
        if (fieldType != FieldType.GEO_POINT) {
            throw new InvalidParameterException("collect_field: `" + metric.collectField + "` is not a geo-point field. " + "`" + metric.collectFct.name() + "` is applied to geo-points only.");
        }
    }

    private void setOrder(Aggregation aggregationModel, ValuesSourceAggregationBuilder aggregationBuilder, ValuesSourceAggregationBuilder metricAggregation) throws ArlasException {
        Order order = aggregationModel.order;
        OrderOn on = aggregationModel.on;
        if (order != null && on != null) {
            if (!(aggregationBuilder instanceof GeoGridAggregationBuilder)) {
                Boolean asc = order.equals(Order.asc);
                BucketOrder bucketOrder = null;
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

    private PolygonBuilder createPolygonBuilder(Polygon polygon, Boolean righthand) {
        // TODO: add interior holes
        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
        List<Coordinate> coordinates = Arrays.asList(polygon.getCoordinates());
        coordinatesBuilder.coordinates(coordinates);

        return new PolygonBuilder(coordinatesBuilder, righthand == Boolean.TRUE ? Orientation.RIGHT : Orientation.LEFT);
    }

    private PolygonBuilder createPolygonBuilder(double[] bbox) {
        double west = bbox[0];
        double east = bbox[2];
        double south = bbox[1];
        double north = bbox[3];

        CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
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
        coordinatesBuilder.coordinate(east, south);
        coordinatesBuilder.coordinate(east, north);
        coordinatesBuilder.coordinate(west, north);
        coordinatesBuilder.coordinate(west, south);
        coordinatesBuilder.coordinate(east, south);
        // NB : In ES api LEFT is clockwise and RIGHT anticlockwise
        return new PolygonBuilder(coordinatesBuilder, Orientation.RIGHT);

    }

    private MultiPolygonBuilder createMultiPolygonBuilder(MultiPolygon multiPolygon, Boolean righthand) {
        MultiPolygonBuilder multiPolygonBuilder = new MultiPolygonBuilder(Orientation.LEFT);
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            multiPolygonBuilder.polygon(createPolygonBuilder((Polygon) multiPolygon.getGeometryN(i), righthand));
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

    private ShapeBuilder getShapeBuilder(String geometry, Boolean righthand) throws ArlasException {
        // test if geometry is 'west,south,east,north' or wkt string
        if (CheckParams.isBboxMatch(geometry)) {
            return createPolygonBuilder((double[]) CheckParams.toDoubles(geometry));
        } else {
            // TODO: multilinestring
            Geometry wktGeometry = GeoUtil.readWKT(geometry);
            if (wktGeometry != null) {
                String geometryType = wktGeometry.getGeometryType().toUpperCase();
                switch (geometryType) {
                    case "POLYGON":
                        return createPolygonBuilder((Polygon) wktGeometry, righthand);
                    case "MULTIPOLYGON":
                        return createMultiPolygonBuilder((MultiPolygon) wktGeometry, righthand);
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
}