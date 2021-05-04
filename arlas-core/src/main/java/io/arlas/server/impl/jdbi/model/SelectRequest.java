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

package io.arlas.server.impl.jdbi.model;

import io.arlas.server.app.ArlasBaseConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.impl.jdbi.clause.*;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.services.FluidSearchService;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static io.arlas.server.utils.StringUtil.concat;

public abstract class SelectRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectRequest.class);
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String QUOTE = "\"";
    public static final String LP = "(";
    public static final String RP = ")";
    public static final String OR = " or ";
    public static final String AND = " and ";
    public static final String IN = " in ";
    public static final String NOTIN = " not in ";
    public static final String NOT = " not ";
    public static final String GT = ">";
    public static final String GTE = ">=";
    public static final String LT = "<";
    public static final String LTE = "<=";
    public static final String EQ = "=";
    public static final String NE = "<>";
    public static final String DIV = "/";
    public static final String LIKE = " like ";
    public static final String AVG = "avg";
    public static final String SUM = "sum";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String COUNT = "count";
    public static final String AS = " as ";
    public static final String WHERE = " where ";
    public static final String LIMIT = " limit ";
    public static final String OFFSET = " offset ";
    public static final String GROUP_BY = " group by ";
    public static final String ORDER_BY = " order by ";
    public static final String DESC = " desc";
    public static final String ASC = " asc";
    public static final String ST_ASTEXT = "st_astext";
    public static final String ST_COVERS = "st_covers";
    public static final String ST_GEOHASH = "st_geohash";
    public static final String ST_EXTENT = "st_extent";
    public static final String ST_CENTROID = "st_centroid";
    public static final String ST_DISTANCE = "st_distance";
    public static final String ST_INTERSECTS = "st_intersects";
    public static final String QKEY = "key";
    public static final String QCOUNT = "count";
    public static final String US /* UnderScore */ = ArlasBaseConfiguration.FLATTEN_CHAR;
    public static final String SRID = "SRID=4326;";
    public static final String IS_NOT_NULL = " is not null";
    public static final String FLOOR_LP = "FLOOR(";
    public static final String FLOOR_RP = ") * ";

    protected final LinkedList<String> selectClauses = new LinkedList<>();
    protected final List<String> whereAndConditions = new ArrayList<>();
    protected final Map<String, ClauseParam> whereParams = new HashMap<>();
    protected final List<String> groupBy = new ArrayList<>();
    protected final List<String> orderBy = new ArrayList<>();
    public Integer limit = 10000; // default limit as in ES
    public Integer offset = 0;

    public SelectRequest() {
    }

    // Adds the value in a map and returns ":index" (e.g. ":1", ":2"...) with the "index" of the inserted value in the map
    public String mapWhereParam(ClauseParam v) {
        String k = String.valueOf(whereParams.size());
        whereParams.put(k, v);
        return concat(COLON, k);
    }

    public Map<String, ClauseParam> getWhereParams() {
        LOGGER.debug(concat("getWhereParams() returns: ", whereParams.toString()));
        return whereParams;
    }

    public void addWhereFieldIsNotNull(String field) {
        whereAndConditions.add(concat(field, IS_NOT_NULL));
    }

    public void addOrWhereClauses(List<String> orConditions) {
        whereAndConditions.add(concat(LP, formatOrClauses(orConditions), RP));
    }

    public String formatOrClauses(List<String> orConditions) {
        return String.join(OR, orConditions);
    }

    public String getWhereClause() {
        String where = whereAndConditions.isEmpty() ? "" : concat(WHERE, String.join(AND, whereAndConditions));
        LOGGER.debug(concat("getWhereCondition() returns: ", where));
        return where;
    }

    public String getGroupClause() {
        String ret = groupBy.isEmpty() ? "" : concat(GROUP_BY, String.join(COMMA, groupBy));
        LOGGER.debug(concat("getGroupClause() returns: ", ret));
        return ret;
    }

    public String getLimitClause() {
        String ret = limit == 0 ? "" : concat(LIMIT, limit.toString(), OFFSET, offset.toString());
        LOGGER.debug(concat("getLimitClause() returns: ", ret));
        return ret;
    }

    public void addOrderClause(String field, boolean asc) {
        orderBy.add(concat(field, asc ? ASC : DESC));
        LOGGER.debug(concat("addOrderClause() : ", orderBy.get(orderBy.size()-1)));
    }

    public String getOrderClause() {
        String ret = orderBy.isEmpty() ? "" : concat(ORDER_BY, String.join(COMMA, orderBy));
        LOGGER.debug(concat("getOrderClause() returns: ", ret));
        return ret;
    }

    public String getSelectClause() {
        String select = selectClauses.isEmpty() ? "" : String.join(COMMA, selectClauses);
        LOGGER.debug(concat("getSelectPredicates() returns: ", select));
        return select;
    }

    public abstract String getDateFieldAsEpochMilli(String field);

    // ------

    public void addSelectClause(MetricSelectClause c) {
        selectClauses.add(concat(c.function, LP, c.columnName, RP, AS, c.asName));
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }
    public abstract void addSelectClause(AggregatedGeoSelectClause c);

    public abstract void addSelectClause(DateAggregationSelectClause c);

    public abstract void addSelectClause(GeotileAggregationSelectClause c);

    public abstract void addSelectClause(FluidSearchService fluidSearchService, String[] includes) throws ArlasException;

    public void addSelectClause(GeohashAggregationSelectClause c) {
        selectClauses.add(concat(ST_GEOHASH, LP, c.columnName, COMMA, c.precision, RP, AS, c.asName,
                COMMA, COUNT, LP, c.columnName, RP, AS, c.asNameCount));
        groupBy.add(c.asName);
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    public void addSelectClause(HistogramAggregationSelectClause c) {
        // select FLOOR(field/interval)*interval
        selectClauses.add(concat(FLOOR_LP, c.columnName, DIV, String.valueOf(c.intervalValue), FLOOR_RP, String.valueOf(c.intervalValue), AS, c.asName,
                COMMA, COUNT, LP, c.columnName, RP, AS, c.asNameCount));
        groupBy.add(c.asName);
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    public void addSelectClause(TermAggregationSelectClause c) {
        // select field, count(field)
        selectClauses.add(concat(c.columnName, AS, c.asName, COMMA, COUNT, LP, c.columnName, RP, AS, c.asNameCount));
        groupBy.add(c.asName);
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    // ------

    public String formatInCondition(String field, String[] fieldValues) {
        if (fieldValues.length > 1) {
            return concat(field, IN, LP, Arrays.stream(fieldValues)
                    .map(v -> mapWhereParam(new ClauseParam(field, v))).collect(Collectors.joining(COMMA)), RP);
        } else {
            return concat(field, EQ, mapWhereParam(new ClauseParam(field, fieldValues[0])));
        }
    }

    public String formatNotInCondition(String field, String[] fieldValues) {
        return concat(field, NOTIN, LP, Arrays.stream(fieldValues).map(v -> mapWhereParam(new ClauseParam(field, v))).collect(Collectors.joining(COMMA)), RP);
    }

    public String formatGTECondition(String field, String value) {
        return concat(field, GTE, mapWhereParam(new ClauseParam(field, value)));
    }

    public String formatGTCondition(String field, String value) {
        return concat(field, GT, mapWhereParam(new ClauseParam(field, value)));
    }

    public String formatLTECondition(String field, String value) {
        return concat(field, LTE, mapWhereParam(new ClauseParam(field, value)));
    }

    public String formatLTCondition(String field, String value) {
        return concat(field, LT, mapWhereParam(new ClauseParam(field, value)));
    }

    public String formatLikeCondition(String field, String value) {
        // !! WARNING : never a good thing on a SQL database !! To be discussed
        return concat(field, "::text", LIKE, mapWhereParam(new ClauseParam("", concat("%", value, "%"))));
    }

    public String formatRangeCondition(String field, boolean incMin, String min, boolean incMax, String max) {
        return concat(LP, field, incMin ? GTE : GT, mapWhereParam(new ClauseParam(field, min)),
                AND, field, incMax ? LTE : LT, mapWhereParam(new ClauseParam(field, max)), RP);
    }

    public String formatGeoCondition(String field, OperatorEnum op, Geometry jtsGeo) throws ArlasException {
        switch (op) {
            case notintersects:
                return formatGeoIntersectCondition(field, jtsGeo, true);
            case notwithin:
                return formatGeoWithinCondition(field, jtsGeo, true);
            case intersects:
                return formatGeoIntersectCondition(field, jtsGeo, false);
            case within:
                return formatGeoWithinCondition(field, jtsGeo, false);
            default:
                throw new ArlasException(op + " op on geo field '" + field + " is not supported");
        }
    }

    public String formatGeoWithinCondition(String field, Geometry jtsGeo, boolean not) throws ArlasException {
        // ST_COVERS includes the borders while ST_WITHIN excludes them
        return concat(not ? NOT : "", ST_COVERS, LP, mapWhereParam(new ClauseParam(field, jtsToDBgeo(jtsGeo))), COMMA, field, RP);
    }

    public String formatGeoIntersectCondition(String field, Geometry jtsGeo, boolean not) throws ArlasException {
        return concat(not ? NOT : "", ST_INTERSECTS, LP, mapWhereParam(new ClauseParam(field, jtsToDBgeo(jtsGeo))), COMMA, field, RP);
    }

    public String formatGeoDistance(String field, Geometry jtsGeo) throws ArlasException {
        return concat(ST_DISTANCE, LP, field, mapWhereParam(new ClauseParam(field, jtsToDBgeo(jtsGeo))), RP);
    }

    protected abstract Object jtsToDBgeo(Geometry jtsGeo) throws ArlasException;
}
