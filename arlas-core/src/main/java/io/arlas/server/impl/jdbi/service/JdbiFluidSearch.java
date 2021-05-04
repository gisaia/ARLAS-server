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

package io.arlas.server.impl.jdbi.service;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotAllowedException;
import io.arlas.server.impl.jdbi.clause.*;
import io.arlas.server.impl.jdbi.model.ColumnQualifier;
import io.arlas.server.impl.jdbi.model.RequestFactory;
import io.arlas.server.impl.jdbi.model.SelectRequest;
import io.arlas.server.managers.CollectionReferenceManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.*;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.FieldType;
import io.arlas.server.services.FluidSearchService;
import io.arlas.server.utils.*;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.arlas.server.impl.jdbi.model.SelectRequest.*;
import static io.arlas.server.model.enumerations.ComputationEnum.GEOBBOX;
import static io.arlas.server.model.enumerations.ComputationEnum.GEOCENTROID;
import static io.arlas.server.utils.CheckParams.GEO_AGGREGATION_TYPE_ENUMS;
import static io.arlas.server.utils.StringUtil.concat;
import static io.arlas.server.utils.StringUtil.isNullOrEmpty;

public class JdbiFluidSearch extends FluidSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbiFluidSearch.class);

    private final SelectRequest req;

    public JdbiFluidSearch(CollectionReference collectionReference, RequestFactory requestFactory) throws ArlasException {
        super(collectionReference);
        this.req = requestFactory.getNewRequest();
    }

    public SelectRequest getRequest() throws ArlasException {
        return getRequest(false);
    }

    public SelectRequest getRequest(boolean setSelectFields) throws ArlasException {
        if (setSelectFields) {
            Pair<String[], String[]> includeExclude = computeIncludeExclude(true);
            req.addSelectClause(this, includeExclude.getLeft());
        }
        return req;
    }

    @Override
    public FluidSearchService filter(MultiValueFilter<Expression> f, String dateFormat) throws ArlasException {
        List<String> orConditions = new ArrayList<>();
        for (Expression fFilter : f) {
            orConditions.add(filter(fFilter, dateFormat));
        }
        req.addOrWhereClauses(orConditions);
        return this;
    }

    private String filter(Expression e, String dateFormat) throws ArlasException {
        if (isNullOrEmpty(e.field) || e.op == null || isNullOrEmpty(e.value)) {
            throw new InvalidParameterException(INVALID_PARAMETER_F);
        }
        String field = e.field;
        String value = e.value;
        String[] fieldValues = value.split(COMMA);
        switch (e.op) {
            case eq:
                return req.formatInCondition(e.field, fieldValues);
            case gte:
                if (isDateField(field) && !isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(e.value, dateFormat, false);
                }
                return req.formatGTECondition(e.field, value);
            case gt:
                if (isDateField(field) && !isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(e.value, dateFormat, false);
                }
                return req.formatGTCondition(e.field, value);
            case lte:
                if (isDateField(field) && !isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(e.value, dateFormat, false);
                }
                return req.formatLTECondition(e.field, value);
            case lt:
                if (isDateField(field) && !isNullOrEmpty(dateFormat)) {
                    value = ParamsParser.parseDate(e.value, dateFormat, false);
                }
                return req.formatLTCondition(e.field, value);
            case like:
                return req.formatLikeCondition(e.field, value);
            case ne:
                return req.formatNotInCondition(e.field, fieldValues);
            case range:
                field = ParamsParser.getFieldFromFieldAliases(field, collectionReference);
                if (fieldValues.length > 1) {
                    List<String> orCond = new ArrayList<>();
                    for (String valueInValues : fieldValues) {
                        CheckParams.checkRangeValidity(valueInValues);
                        orCond.add(getRangeCondition(field, valueInValues, dateFormat));
                    }
                    return req.formatOrClauses(orCond);
                } else {
                    CheckParams.checkRangeValidity(value);
                    return getRangeCondition(field, value, dateFormat);
                }
            case notintersects:
            case notwithin:
            case intersects:
            case within:
                FieldType wType = collectionReferenceManager.getType(collectionReference, field, true);
                switch (wType) {
                    case POINT:
                    case LINESTRING:
                    case POLYGON:
                    case MULTIPOINT:
                    case MULTILINESTRING:
                    case MULTIPOLYGON:
                    case GEOMETRYCOLLECTION:
                    case GEO_SHAPE:
                    case GEO_POINT:
                        Geometry geo;
                        if (CheckParams.isBboxMatch(value)) {
                            double[] tlbr = CheckParams.toDoubles(value);
                            geo = GeoTileUtil.toPolygon(new BoundingBox(tlbr[3], tlbr[1], tlbr[0], tlbr[2]));
                        } else {
                            geo = GeoUtil.readWKT(value);
                        }
                        return req.formatGeoCondition(e.field, e.op, geo);
                    default:
                        throw new ArlasException(e.op + " op on field '" + field + "' of type '" + wType + "' is not supported");
                }
            default:
                throw new InvalidParameterException(INVALID_OPERATOR);
        }
    }

    @Override
    public FluidSearchService filterQ(MultiValueFilter<String> q) throws ArlasException {
        List<String> orConditions = new ArrayList<>();
        for (String qFilter : q) {
            String[] operands = qFilter.split(":",2);
            if (operands.length == 2) {
                orConditions.add(req.formatLikeCondition(operands[0], operands[1]));
            } else if (operands.length == 1) {
                throw new ArlasException("Text search without specifying a field is not supported in SQL");
            } else {
                throw new InvalidParameterException(INVALID_Q_FILTER);
            }
        }
        req.addOrWhereClauses(orConditions);
        return this;
    }

    @Override
    public FluidSearchService sort(String sort) throws ArlasException {
        String[] fieldList = sort.split(",");
        String field;
        boolean orderAsc;
        for (String signedField : fieldList) {
            if (!signedField.equals("")) {
                if (signedField.charAt(0) == '-') {
                    field = signedField.substring(1);
                    orderAsc = false;
                } else {
                    field = signedField;
                    orderAsc = true;
                }
                if (field.split(" ").length > 1) {
                    geoDistanceSort(field, orderAsc);
                } else {
                    req.addOrderClause(field, orderAsc);
                }
            }
        }
        return this;
    }

    @Override
    public FluidSearchService filterSize(Integer size, Integer from) {
        req.limit = size;
        req.offset = from;
        return this;
    }

    @Override
    public FluidSearchService searchAfter(Page page, String after) {
        String[] fieldList = page.sort.split(",");
        String[] valueList = after.split(",");
        for (int i=0; i<fieldList.length; i++) {
            String signedField = fieldList[i];
            String value = valueList[i];
            if (signedField.startsWith("-")) {
                req.addOrWhereClauses(Collections.singletonList(req.formatLTCondition(signedField.substring(1), value)));
            } else {
                req.addOrWhereClauses(Collections.singletonList(req.formatGTCondition(signedField, value)));
            }
        }
        return this;
    }

    private void geoDistanceSort(String geoSort, boolean orderAsc) throws ArlasException {
        Pair<Double, Double> latLon = ParamsParser.getGeoSortParamsAsLatLon(geoSort);
        String geoSortField = collectionReference.params.centroidPath;
        req.addOrderClause(req.formatGeoDistance(geoSortField, GeoUtil.getPoint(latLon.getRight(), latLon.getLeft())), orderAsc);
    }

    @Override
    public FluidSearchService aggregate(List<Aggregation> aggregations, Boolean isGeoAggregate) throws ArlasException {
        for (int i=0; i < aggregations.size(); i++) {
            //check the agg syntax is correct
            Aggregation aggregationModel = aggregations.get(i);
            aggregationModel.index = i;
            if (isGeoAggregate && i == 0) {
                if (!GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type)
                        && aggregationModel.rawGeometries == null
                        && aggregationModel.aggregatedGeometries == null) {
                    throw new NotAllowedException("'" + aggregationModel.type.name() +"' aggregation type is not allowed in _geoaggregate service if at least `aggregated_geometries` or `raw_geometries` parameters are not specified");
                }
            }
            if (aggregationModel.type != AggregationTypeEnum.datehistogram
                    && aggregationModel.format != null) {
                throw new BadRequestException(NO_FORMAT_TO_SPECIFY);
            }
            // ex: datehistogram_0
            String aggName = concat(aggregationModel.type.name(), US, String.valueOf(aggregationModel.index));
            switch (aggregationModel.type) {
                case datehistogram:
                    buildDateHistogramAggregation(aggName, aggregationModel);
                    break;
                case geohash:
                    buildGeohashAggregation(aggName, aggregationModel);
                    break;
                case geotile:
                    buildGeotileAggregation(aggName, aggregationModel);
                    break;
                case histogram:
                    buildHistogramAggregation(aggName, aggregationModel);
                    break;
                case term:
                    buildTermsAggregation(aggName, aggregationModel);
                    break;
            }
        }

        return this;
    }

    @Override
    public FluidSearchService compute(String f, ComputationEnum metric) throws ArlasException {
        req.addWhereFieldIsNotNull(f);
        String field = isDateField(f) ? req.getDateFieldAsEpochMilli(f) : f;
        req.addSelectClause(new MetricSelectClause(field, COUNT, COUNT));
        switch (metric) {
            case AVG:
                req.addSelectClause(new MetricSelectClause(field, AVG, AVG));
                break;
            case MAX:
                req.addSelectClause(new MetricSelectClause(field, MAX, MAX));
                break;
            case MIN:
                req.addSelectClause(new MetricSelectClause(field, MIN, MIN));
                break;
            case SUM:
                req.addSelectClause(new MetricSelectClause(field, SUM, SUM));
            case SPANNING:
                req.addSelectClause(new MetricSelectClause(field, MIN, MIN));
                req.addSelectClause(new MetricSelectClause(field, MAX, MAX));
                break;
            case GEOBBOX:
                req.addSelectClause(new AggregatedGeoSelectClause(field, GEOBBOX.value(), AggregatedGeometryEnum.BBOX.value()));
                break;
            case GEOCENTROID:
                req.addSelectClause(new AggregatedGeoSelectClause(field, GEOCENTROID.value(), AggregatedGeometryEnum.CENTROID.value()));
                break;
        }
        return this;
    }

    private void buildDateHistogramAggregation(String aggName, Aggregation aggregationModel) throws ArlasException {
        if (StringUtil.isNullOrEmpty(aggregationModel.field)) {
            aggregationModel.field = collectionReference.params.timestampPath;
        }

        DateAggregationSelectClause sp = new DateAggregationSelectClause(aggregationModel.field,
                aggName, aggregationModel.interval.unit, (Integer) aggregationModel.interval.value,
                ParamsParser.getValidAggregationFormat(aggregationModel.format));
        req.addSelectClause(sp);

        //get the field, format, collect_field, collect_fct, order, on
        setAggregationParameters(aggName, aggregationModel, sp);
        setHitsToFetch(aggName, aggregationModel, sp);
        setAggregatedGeometries(aggName, aggregationModel, sp);
        setRawGeometries(aggName, aggregationModel, sp);
    }

    private void buildGeohashAggregation(String aggName, Aggregation aggregationModel) throws ArlasException {
        GeohashAggregationSelectClause sp = new GeohashAggregationSelectClause(aggregationModel.field,
                aggName, aggregationModel.interval.value.toString());
        req.addSelectClause(sp);

        //get the field, format, collect_field, collect_fct, order, on
        setAggregationParameters(aggName, aggregationModel, sp);
        setAggregatedGeometries(aggName, aggregationModel, sp);
        setRawGeometries(aggName, aggregationModel, sp);
        setHitsToFetch(aggName, aggregationModel, sp);
    }

    private void buildGeotileAggregation(String aggName, Aggregation aggregationModel) throws ArlasException {
        GeotileAggregationSelectClause sp = new GeotileAggregationSelectClause(aggregationModel.field,
                aggName, aggregationModel.interval.value.toString());
        req.addSelectClause(sp);

        //get the field, format, collect_field, collect_fct, order, on
        setAggregationParameters(aggName, aggregationModel, sp);
        setAggregatedGeometries(aggName, aggregationModel, sp);
        setRawGeometries(aggName, aggregationModel, sp);
        setHitsToFetch(aggName, aggregationModel, sp);
    }

    private void buildHistogramAggregation(String aggName, Aggregation aggregationModel) throws ArlasException {
        HistogramAggregationSelectClause sp = new HistogramAggregationSelectClause(aggregationModel.field,
                aggName, (Double) aggregationModel.interval.value);
        req.addSelectClause(sp);

        //get the field, format, collect_field, collect_fct, order, on
        setAggregationParameters(aggName, aggregationModel, sp);
        setHitsToFetch(aggName, aggregationModel, sp);
        setAggregatedGeometries(aggName, aggregationModel, sp);
        setRawGeometries(aggName, aggregationModel, sp);
    }

    private void buildTermsAggregation(String aggName, Aggregation aggregationModel) throws ArlasException {
        TermAggregationSelectClause sp = new TermAggregationSelectClause(aggregationModel.field, aggName);

        if (aggregationModel.include != null && !aggregationModel.include.isEmpty()) {
            req.addOrWhereClauses(Collections.singletonList(req.formatInCondition(aggregationModel.field, aggregationModel.include.split(COMMA))));
        }
        req.addSelectClause(sp);
        //get the field, format, collect_field, collect_fct, order, on
        setAggregationParameters(aggName, aggregationModel, sp);
        setHitsToFetch(aggName, aggregationModel, sp);
        setAggregatedGeometries(aggName, aggregationModel, sp);
        setRawGeometries(aggName, aggregationModel, sp);
    }

    private void setAggregationParameters(String aggName, Aggregation aggregationModel, SelectClause sp) throws ArlasException {
        // firstMetricAggregationBuilder is the aggregation builder on which the order aggregation will be applied
        MetricSelectClause firstMetric = null;
        if (aggregationModel.metrics != null) {
            for (Metric m: aggregationModel.metrics) {
                if (m.collectFct == null) {
                    throw new BadRequestException(COLLECT_FCT_NOT_SPECIFIED);
                } else if (m.collectField == null) {
                    throw new BadRequestException(COLLECT_FIELD_NOT_SPECIFIED);
                }
                MetricSelectClause metricPredicate = null;
                switch (m.collectFct) {
                    case AVG:
                        metricPredicate = new MetricSelectClause(
                                m.collectField,
                                concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.AVG, m.collectField)),
                                AVG);
                        break;
                    case CARDINALITY:
                        metricPredicate = new MetricSelectClause(
                                m.collectField,
                                concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.CARDINALITY, m.collectField)),
                                COUNT);
                        break;
                    case MAX:
                        metricPredicate = new MetricSelectClause(
                                m.collectField,
                                concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.MAX, m.collectField)),
                                MAX);
                        break;
                    case MIN:
                        metricPredicate = new MetricSelectClause(
                                m.collectField,
                                concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.MIN, m.collectField)),
                                MIN);
                        break;
                    case SUM:
                        metricPredicate = new MetricSelectClause(
                                m.collectField,
                                concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.SUM, m.collectField)),
                                SUM);
                        break;
                    case GEOCENTROID:
                        setGeoMetricAggregationCollectField(m);
                        // We calculate this metric only if it wasn't requested as a geometry to return in `aggregatedGeometries` parameter
                        if (!(aggregationModel.aggregatedGeometries != null
                                && aggregationModel.aggregatedGeometries.contains(AggregatedGeometryEnum.CENTROID)
                                && aggregationModel.field.equals(m.collectField))) {
                            metricPredicate = new AggregatedGeoSelectClause(
                                    m.collectField,
                                    concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.GEOCENTROID, m.collectField)),
                                    AggregatedGeometryEnum.CENTROID.value());
                        }
                        break;
                    case GEOBBOX:
                        setGeoMetricAggregationCollectField(m);
                        // We calculate this metric only if it wasn't requested as a geometry to return in `aggregatedGeometries` parameter
                        if (!(aggregationModel.aggregatedGeometries != null
                                && aggregationModel.aggregatedGeometries.contains(AggregatedGeometryEnum.BBOX)
                                && aggregationModel.field.equals(m.collectField))) {
                            metricPredicate = new AggregatedGeoSelectClause(
                                    m.collectField,
                                    concat(aggName, US, ColumnQualifier.getQualifier(CollectionFunction.GEOBBOX, m.collectField)),
                                    AggregatedGeometryEnum.BBOX.value());
                        }
                        break;
                }
                if (metricPredicate != null) {
                    req.addSelectClause(metricPredicate);
                    // Getting the first metric aggregation builder that is different from GEOBBOX and GEOCENTROID, on which the order will be applied
                    if (firstMetric == null
                            && m.collectFct != CollectionFunction.GEOBBOX
                            && m.collectFct != CollectionFunction.GEOCENTROID) {
                        firstMetric = metricPredicate;
                    }
                }
            }
        }

        if (aggregationModel.size != null) {
            this.req.limit = ParamsParser.getValidAggregationSize(aggregationModel.size);
        }

        setOrder(aggregationModel, firstMetric, sp);
    }

    private void setGeoMetricAggregationCollectField(Metric metric) throws ArlasException {
        FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, metric.collectField, true);
        if (!Arrays.asList(FieldType.POINT, FieldType.GEO_POINT).contains(fieldType)) {
            throw new InvalidParameterException("collect_field: `" + metric.collectField + "` is not a point field. " + "`" + metric.collectFct.name() + "` is applied to points only.");
        }
    }

    private void setAggregatedGeometries(String aggName, Aggregation aggregationModel, SelectClause sp) {
        if (aggregationModel.aggregatedGeometries != null) {
            String aggregationGeoField = GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type) ? aggregationModel.field : collectionReference.params.centroidPath;
            aggregationModel.aggregatedGeometries.forEach(ag -> {
                if (ag == AggregatedGeometryEnum.BBOX || ag == AggregatedGeometryEnum.CENTROID) {
                    req.addSelectClause(new AggregatedGeoSelectClause(aggregationGeoField,
                            concat(aggName, US, ColumnQualifier.getQualifier(ag, aggregationGeoField)), ag.value()));
                }
            });
        }
    }

    private void setRawGeometries(String aggName, Aggregation aggregationModel, SelectClause sp) throws ArlasException {
        if (aggregationModel.rawGeometries != null) {
            LOGGER.warn("Fetching raw geometries is not possible in a SQL aggregation"); // TODO: investigate further
        }
    }

    private void setHitsToFetch(String aggName, Aggregation aggregationModel, SelectClause sp) throws ArlasException {
        if (aggregationModel.fetchHits != null) {
            LOGGER.warn("Fetching hits is not possible in a SQL aggregation"); // TODO: investigate further
        }
    }

    private void setOrder(Aggregation aggregationModel, MetricSelectClause metricAggregation, SelectClause sp) throws ArlasException {
        Order order = aggregationModel.order;
        OrderOn on = aggregationModel.on;
        boolean isGeo = GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type);
        if (order == null && on == null && !isGeo) {
            if (aggregationModel.type == AggregationTypeEnum.term) {
                order = Order.desc;
                on = OrderOn.count;
            } else {
                order = Order.asc;
                on = OrderOn.field;
            }
        }
        boolean orderAsc = order == Order.asc;
        if (order != null && on != null) {
            if (!isGeo) {
                switch (on) {
                    case field:
                        req.addOrderClause(sp.asName, orderAsc);
                        break;
                    case count:
                        req.addOrderClause(((AggregationSelectClause)sp).asNameCount, orderAsc);
                        break;
                    case result:
                        if (metricAggregation != null) {
                            // ORDER ON RESULT IS NOT ALLOWED ON COORDINATES (CENTROID) OR BOUNDING BOX
                            if (!metricAggregation.isGeo()) {
                                req.addOrderClause(metricAggregation.asName, orderAsc);
                            } else {
                                throw new BadRequestException(ORDER_ON_GEO_RESULT_NOT_ALLOWED);
                            }
                        } else {
                            throw new BadRequestException(ORDER_ON_RESULT_NOT_ALLOWED);
                        }
                        break;
                }
            } else {
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            }
        } else if (order != null) {
            if (isGeo)
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ON_NOT_SPECIFIED);
        } else if (on != null) {
            if (isGeo)
                throw new NotAllowedException(ORDER_PARAM_NOT_ALLOWED);
            else
                throw new BadRequestException(ORDER_NOT_SPECIFIED);
        }
    }

    protected String getRangeCondition(String field, String value, String dateFormat) throws ArlasException {
        boolean incMin = value.startsWith("[");
        boolean incMax = value.endsWith("]");
        String min = value.substring(1, value.lastIndexOf("<"));
        String max = value.substring(value.lastIndexOf("<") + 1, value.length() - 1);

        if (isDateField(field) && !StringUtil.isNullOrEmpty(dateFormat)) {
            min = ParamsParser.parseDate(min, dateFormat, false);
            max = ParamsParser.parseDate(max, dateFormat, false);
        }

        return req.formatRangeCondition(field, incMin, min, incMax, max);
    }
}
