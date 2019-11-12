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

package io.arlas.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.BadRequestException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotImplementedException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.*;
import io.arlas.server.model.request.*;
import io.dropwizard.jersey.params.IntParam;
import org.elasticsearch.common.geo.GeoPoint;
import org.joda.time.format.DateTimeFormat;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParamsParser {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String AGG_INTERVAL_PARAM = "interval-";
    private static final String AGG_FORMAT_PARAM = "format-";
    private static final String AGG_COLLECT_FIELD_PARAM = "collect_field-";
    private static final String AGG_COLLECT_FCT_PARAM = "collect_fct-";
    private static final String AGG_ORDER_PARAM = "order-";
    private static final String AGG_ON_PARAM = "on-";
    private static final String AGG_SIZE_PARAM = "size-";
    private static final String AGG_INCLUDE_PARAM = "include-";
    private static final String AGG_FETCHGEOMETRY_PARAM = "fetch_geometry";
    private static final String INVALID_DATE_MATH_EXPRESSION = "Invalid date math expression";
    private static final String AGG_FETCHHITS_PARAM = "fetch_hits-";

    private static final Pattern HITS_FETCHER_PATTERN = Pattern.compile("(\\d*)(\\()(.*)(\\))");

    public static final String RANGE_ALIASES_CHARACTER = "$";
    public static final String TIMESTAMP_ALIAS = "timestamp";
    public static final String BAD_FIELD_ALIAS = "This alias does not represent a collection configured field. ";


    public static List<Aggregation> getAggregations(List<String> agg) throws ArlasException {
        List<Aggregation> aggregations = new ArrayList<>();
        if (agg != null && agg.size() > 0) {
            for (String aggregation : agg) {
                Aggregation aggregationModel;
                if (CheckParams.isAggregationParamValid(aggregation)) {
                    List<String> aggParameters = Arrays.asList(aggregation.split(":"));
                    aggregationModel = getAggregationModel(aggParameters);
                    aggregations.add(aggregationModel);
                }
            }
        }
        return aggregations;
    }

    public static Aggregation getAggregationModel(List<String> agg) throws ArlasException {
        Aggregation aggregationModel = new Aggregation();
        aggregationModel.type = AggregationTypeEnum.valueOf(agg.get(0));
        for (String parameter : agg) {
            if (parameter.contains(AGG_INTERVAL_PARAM)) {
                if (aggregationModel.type.equals(AggregationTypeEnum.datehistogram)) {
                    aggregationModel.interval = getDatehistogramAggregationInterval(parameter.substring(AGG_INTERVAL_PARAM.length()));
                } else if (aggregationModel.type.equals(AggregationTypeEnum.geohash)) {
                    aggregationModel.interval = getGeohashAggregationInterval(parameter.substring(AGG_INTERVAL_PARAM.length()));
                } else if (aggregationModel.type.equals(AggregationTypeEnum.histogram)) {
                    aggregationModel.interval = getHistogramAggregationInterval(parameter.substring(AGG_INTERVAL_PARAM.length()));
                } else {
                    throw new BadRequestException(CheckParams.NO_TERM_INTERVAL);
                }
            } else if (parameter.contains(AGG_FORMAT_PARAM)) {
                aggregationModel.format = parameter.substring(AGG_FORMAT_PARAM.length());
            } else if (parameter.contains(AGG_ORDER_PARAM)) {
                aggregationModel.order = Order.valueOf(parameter.substring(AGG_ORDER_PARAM.length()));
            } else if (parameter.contains(AGG_ON_PARAM)) {
                aggregationModel.on = OrderOn.valueOf(parameter.substring(AGG_ON_PARAM.length()));
            } else if (parameter.contains(AGG_SIZE_PARAM)) {
                aggregationModel.size = parameter.substring(AGG_SIZE_PARAM.length());
            } else if (parameter.contains(AGG_INCLUDE_PARAM)) {
                aggregationModel.include = parameter.substring(AGG_INCLUDE_PARAM.length());
            } else if (parameter.contains(AGG_FETCHGEOMETRY_PARAM)) {
                aggregationModel.fetchGeometry = getAggregatedGeometry(parameter.substring(AGG_FETCHGEOMETRY_PARAM.length()));
            } else if (parameter.contains(AGG_FETCHHITS_PARAM)) {
                aggregationModel.fetchHits = getHitsFetcher(parameter.substring(AGG_FETCHHITS_PARAM.length()));
            } else if (parameter.equals(agg.get(1))) {
                aggregationModel.field = parameter;
            }
        }
        aggregationModel.metrics = getAggregationMetrics(agg);
        return aggregationModel;
    }

    private static HitsFetcher getHitsFetcher(String fetchHitsString) throws ArlasException {
        HitsFetcher hitsFetcher = new HitsFetcher();
        if (StringUtil.isNullOrEmpty(fetchHitsString)) {
            throw new BadRequestException("fetchHits should not be null nor empty");
        }
        Matcher matcher = HITS_FETCHER_PATTERN.matcher(fetchHitsString);
        if (!matcher.matches()) {
            throw new InvalidParameterException("Invalid fetchHits syntax. It should respect the following pattern : {size*}(+{field1}, -{field2}, {field3}, ...)");
        }
        hitsFetcher.size = Optional.ofNullable(ParamsParser.tryParseInteger(matcher.group(1))).orElse(1);
        hitsFetcher.include = Arrays.asList(matcher.group(3).split(","));
        return hitsFetcher;
    }

    private static AggregatedGeometry getAggregatedGeometry(String fetchGeometryString) throws ArlasException {
        AggregatedGeometry aggregatedGeometry = null;
        if (fetchGeometryString != null) {
            if (fetchGeometryString.contains("-")) {
                String[] fetchOptions = fetchGeometryString.split("-");
                if (fetchOptions.length == 2) {
                    String option = fetchOptions[1];
                    if (option.equals(AggregatedGeometryStrategyEnum.bbox.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.bbox);
                    } else if (option.equals(AggregatedGeometryStrategyEnum.centroid.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.centroid);
                    }  else if (option.equals(AggregatedGeometryStrategyEnum.byDefault.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.byDefault);
                    } else if (option.equals(AggregatedGeometryStrategyEnum.first.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.first);
                    } else if (option.equals(AggregatedGeometryStrategyEnum.last.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.last);
                    } else if (option.equals(AggregatedGeometryStrategyEnum.geohash.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.geohash);
                    } else {
                        throw new InvalidParameterException(CheckParams.INVALID_FETCHGEOMETRY);
                    }
                } else if (fetchOptions.length == 3) {
                    String field = fetchOptions[1];
                    // TODO check field existence ?
                    String option = fetchOptions[2];
                    if (option.equals(AggregatedGeometryStrategyEnum.first.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.first, field);
                    } else if (option.equals(AggregatedGeometryStrategyEnum.last.name())) {
                        aggregatedGeometry = new AggregatedGeometry(AggregatedGeometryStrategyEnum.last, field);
                    } else {
                        throw new InvalidParameterException(CheckParams.INVALID_FETCHGEOMETRY);
                    }
                } else {
                    throw new InvalidParameterException(CheckParams.INVALID_FETCHGEOMETRY);
                }
            } else {
                if (fetchGeometryString.equals("")) {
                    aggregatedGeometry =  new AggregatedGeometry(AggregatedGeometryStrategyEnum.byDefault);
                } else {
                    throw new InvalidParameterException(CheckParams.INVALID_FETCHGEOMETRY);
                }
            }

        }
        return aggregatedGeometry;
    }

    private static List<Metric> getAggregationMetrics(List<String> agg) throws ArlasException {
        List<Metric> metrics = new ArrayList<>();
        try {
            List<String> collectFields = agg.stream().filter(s -> s.contains(AGG_COLLECT_FIELD_PARAM))
                    .map(s -> s.substring(AGG_COLLECT_FIELD_PARAM.length()))
                    .collect(Collectors.toList());
            List<CollectionFunction> collectFcts = agg.stream().filter(s -> s.contains(AGG_COLLECT_FCT_PARAM))
                    .map(s -> CollectionFunction.valueOf(s.substring(AGG_COLLECT_FCT_PARAM.length()).toUpperCase()))
                    .collect(Collectors.toList());
            CheckParams.checkCollectionFunctionValidity(collectFields, collectFcts);
            metrics = IntStream.range(0, collectFcts.size())
                    .filter(i -> i < collectFcts.size())
                    .mapToObj(i -> new Metric(collectFields.get(i), collectFcts.get(i)))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException("Invalid collection function");
        }
        return metrics;
    }

    public static Interval getDatehistogramAggregationInterval(String intervalString) throws ArlasException {
        if (intervalString != null && !intervalString.equals("")) {
            String[] sizeAndUnit = intervalString.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=[a-zA-Z])");
            if (sizeAndUnit.length == 2) {
                Interval interval = new Interval(tryParseInteger(sizeAndUnit[0]), UnitEnum.valueOf(sizeAndUnit[1].toLowerCase()));
                return interval;
            } else throw new InvalidParameterException("The date interval '" + intervalString + "' is not valid");
        } else {
            return null;
        }
    }

    public static Interval getHistogramAggregationInterval(String intervalString) throws ArlasException {
        return new Interval(tryParseDouble(intervalString), null);
    }

    public static Interval getGeohashAggregationInterval(String intervalString) throws ArlasException {
        return new Interval(tryParseInteger(intervalString), null);
    }

    public static String getFieldFromFieldAliases(String fieldAlias, CollectionReference collectionReference) throws ArlasException {
        boolean isAlias = fieldAlias.startsWith(RANGE_ALIASES_CHARACTER);
        if (isAlias) {
            String alias = fieldAlias.substring(1, fieldAlias.length());
            if (alias.equals(TIMESTAMP_ALIAS)) {
                return collectionReference.params.timestampPath;
            } else {
                throw new BadRequestException(BAD_FIELD_ALIAS);
            }
        } else {
            return fieldAlias;
        }
    }

    public static String getValidAggregationFormat(String aggFormat) {
        //TODO: check if format is in DateTimeFormat (joda)
        if (aggFormat != null) {
            return aggFormat;
        } else {
            return "yyyy-MM-dd-HH:mm:ss";
        }
    }

    public static Filter getFilter(String serializedFilter) throws InvalidParameterException {
        if (serializedFilter != null) {
            try {
                return objectMapper.readValue(serializedFilter, Filter.class);
            } catch (IOException e) {
                throw new InvalidParameterException(FluidSearch.INVALID_FILTER + ": '" + serializedFilter + "'");
            }
        } else {
            return null;
        }
    }

    public static Filter getFilter(List<String> filters, List<String> q, List<String> pwithin, List<String> gwithin, List<String> gintersect, List<String> notpwithin, List<String> notgwithin, List<String> notgintersect, String dateFormat) throws ArlasException {
        Filter filter = new Filter();
        filter.f = new ArrayList<>();

        for (String multiF : filters) {
            MultiValueFilter<Expression> multiFilter = new MultiValueFilter<>();
            for (String f : getMultiFiltersFromSemiColonsSeparatedString(multiF)) {
                if (!StringUtil.isNullOrEmpty(f)) {
                    String operands[] = f.split(":");//not add if operand[0] not autorized
                    StringBuffer value = new StringBuffer();
                    if (operands.length < 3) {
                        throw new InvalidParameterException(FluidSearch.INVALID_PARAMETER_F + ": '" + f + "'");
                    } else {
                        for (int i = 2; i < operands.length; i++) {
                            if (value.length() > 0)
                                value.append(":");
                            value.append(operands[i]);
                        }
                    }
                    multiFilter.add(new Expression(operands[0], OperatorEnum.valueOf(operands[1]), value.toString()));
                }
            }
            filter.f.add(multiFilter);
        }
        filter.q = getStringMultiFilter(q);
        filter.pwithin = getValidGeoFilters(pwithin, true);
        filter.gwithin = getValidGeoFilters(gwithin);
        filter.gintersect = getValidGeoFilters(gintersect);
        filter.notpwithin = getValidGeoFilters(notpwithin, true);
        filter.notgwithin = getValidGeoFilters(notgwithin);
        filter.notgintersect = getValidGeoFilters(notgintersect);
        filter.dateformat = dateFormat;
        return filter;
    }

    public static List<MultiValueFilter<String>> getValidGeoFilters(List<String> geoStrings) throws ArlasException {
        return getValidGeoFilters(geoStrings, false);
    }

    public static List<MultiValueFilter<String>> getValidGeoFilters(List<String> geoStrings, boolean isPwithin) throws ArlasException{
        if (geoStrings != null) {
            List<MultiValueFilter<String>> geoFilters =  getStringMultiFilter(geoStrings);
            List<MultiValueFilter<String>> validGeoFilters = new ArrayList<>();
            if(geoFilters != null) {
                for (MultiValueFilter<String> geoFilter : geoFilters) {
                    MultiValueFilter<String> validGeoFilter = new MultiValueFilter<>();
                    for (String geo : geoFilter) {
                        if (CheckParams.isBboxMatch(geo)) {
                            CheckParams.checkBbox(geo);
                            validGeoFilter.add(geo);
                        } else {
                            Geometry wkt = getValidWKT(geo);
                            /** For the case of Polygon and MultiPolygon, a check of the coordinates orientation is necessary in order to correctly interpret the "desired" polygon **/
                            if (wkt.getGeometryType().equals("Polygon") || wkt.getGeometryType().equals("MultiPolygon")) {
                                for (int i = 0; i< wkt.getNumGeometries(); i++) {
                                    Geometry subWkt = wkt.getGeometryN(i);
                                    if (Orientation.isCCW(subWkt.getCoordinates())){
                                        // By convention the passed queryGeometry must be interpreted as CW.
                                        // If the orientation is CCW, we try to build the WKT that goes the other side of the planet.
                                        // If the topology of the resulted geometry is not valid, an exception is thrown
                                        Geometry tmpGeometry  = subWkt.copy();
                                        Envelope tmpEnvelope = tmpGeometry.getEnvelopeInternal();
                                        /** east is the minX and west is the maxX*/
                                        double east = tmpEnvelope.getMinX();
                                        double west = tmpEnvelope.getMaxX();
                                        if (west > east && ((east < -180 && west >= -180) ||  (west > 180 && east <= 180))) {
                                            /** It means west > 180 or east < -180 */
                                            if (west >= 180) {
                                                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, false, 180);
                                            } else if (east <= -180) {
                                                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, true , -180);
                                            }
                                        } else {
                                            if (west >= 0) {
                                                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, false, east);
                                            } else {
                                                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360,true, west);
                                            }
                                        }
                                        IsValidOp vaildOp = new IsValidOp(tmpGeometry);
                                        TopologyValidationError err = vaildOp.getValidationError();
                                        if (err != null)
                                        {
                                            throw new InvalidParameterException("A Polygon of the given WKT is right oriented. Unable to reverse the orientation of the polygon : " + err);
                                        }
                                        if (isPwithin) {
                                            /** split the polygon if it crosses the dateline */
                                            validGeoFilter.add(GeoUtil.splitGeometryOnDateline(GeoUtil.readWKT(tmpGeometry.toString()))._2().toString());
                                        } else {
                                            validGeoFilter.add(tmpGeometry.toString());
                                        }
                                    }else {
                                        Envelope e = subWkt.getEnvelopeInternal();
                                        double west = e.getMinX();
                                        double east = e.getMaxX();
                                        if ((east - west) == 360) {
                                            if (west < -180) {
                                                GeoUtil.translateLongitudes(subWkt, -west - 180, true);
                                            } else if (east > 180) {
                                                GeoUtil.translateLongitudes(subWkt, east -180, false);
                                            }
                                        }
                                        validGeoFilter.add(subWkt.toString());
                                    }
                                }
                            } else {
                                validGeoFilter.add(geo);
                            }
                        }
                    }
                    validGeoFilters.add(validGeoFilter);
                }
            }
            return validGeoFilters;
        }
        return null;
    }

    public static List<String> toSemiColonsSeparatedStringList(List<MultiValueFilter<String>> multiValueFilters) {
        List<String> strings = null;
        if (multiValueFilters != null) {
            strings = new ArrayList<>();
             for (MultiValueFilter<String> multiValueFilter: multiValueFilters){
                String semiColonsSeparatedString = "";
                for (String value : multiValueFilter) {
                    semiColonsSeparatedString += value + ";";
                }
                strings.add(semiColonsSeparatedString);
            }
        }
        return strings;
    }

    public static Geometry getValidWKT(String wktString) throws InvalidParameterException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Envelope affectedBounds = new Envelope(-360, 360, -180, 180);
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry geom = null;
        try {
            geom = wkt.read(wktString);
            List<Coordinate> filteredCoord = Arrays.stream(geom.getCoordinates()).filter(coordinate -> affectedBounds.contains(coordinate)).collect(Collectors.toList());
            if(filteredCoord.size() != geom.getCoordinates().length){
                throw new InvalidParameterException("Coordinates must be contained in the Envelope -360, 360, -180, 180");
            }
            for(int i = 0; i< geom.getNumGeometries(); i++) {
                IsValidOp validOp = new IsValidOp(geom.getGeometryN(i));
                TopologyValidationError err = validOp.getValidationError();
                if (err != null) {
                    throw new InvalidParameterException(GeoUtil.INVALID_WKT + ": " + err.getMessage());
                }
            }
        } catch (org.locationtech.jts.io.ParseException ex) {
            throw new InvalidParameterException("Invalid WKT: " + ex.getMessage());
        }
        return geom;
    }

    public static String parseDate(String dateValue, String dateFormat) throws ArlasException {
        String dateToParse = dateValue;
        String parsedDate = dateToParse;
        if (!StringUtil.isNullOrEmpty(dateFormat)) {
            // REMINDER : dateValue can be :
            // - a timestamp in millisecond OR a date in a custom format(*).
            // - a timestamp in millisecond OR a date in a custom format(*) followed by `||` and followed by a date operation (+1h, /M, -2y, ...)
            // - `now`
            // - `now` followed by a date operation (+1h, /M, -2y, ...)
            if (!dateToParse.equals("now")) {
                List<String> splitDate = Arrays.asList(dateValue.split("\\|\\|"));
                if (dateValue.contains("||")) {
                    if (splitDate.size() != 2) {
                        throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                    }
                    dateToParse = splitDate.get(0);
                }
                try {
                    parsedDate = String.valueOf(DateTimeFormat.forPattern(dateFormat).withZoneUTC().parseDateTime(dateToParse).getMillis());
                } catch (DateTimeParseException e) {
                    throw new InvalidParameterException(dateValue + " doesn't match the date format '" + dateFormat + "'. Reason : " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    throw new InvalidParameterException(dateValue + " doesn't match the date format '" + dateFormat + "'. Reason : " + e.getMessage());
                }
                if (dateValue.contains("||")) {
                    parsedDate += "||" + splitDate.get(1);
                }
            }
        }
        return parsedDate;
    }

    public static List<MultiValueFilter<String>> getStringMultiFilter(List<String> filters) {
        List<MultiValueFilter<String>> ret = null;
        if (filters != null && !filters.isEmpty()) {
            ret = new ArrayList<>();
            for (String multiFilterString : filters) {
                MultiValueFilter<String> multiFilter = new MultiValueFilter<>();
                for (String filter : getMultiFiltersFromSemiColonsSeparatedString(multiFilterString)) {
                    if (!StringUtil.isNullOrEmpty(filter)) {
                        multiFilter.add(filter);
                    }
                }
                ret.add(multiFilter);
            }
        }
        return ret;
    }

    private static String[] getMultiFiltersFromSemiColonsSeparatedString(String filters) {
        return filters.split(";");
    }

    public static Page getPage(IntParam size, IntParam from, String sort, String after, String before) throws ArlasException {
        Page page = new Page();
        page.size = size.get();
        page.from = from.get();
        page.sort = sort;
        page.after = after;
        page.before = before;

        return page;
    }

    public static GeoPoint getGeoSortParams(String geoSort) throws ArlasException {
        List<String> geoSortList = Arrays.asList(geoSort.split(":"));
        String geoDistance;
        String latLon;
        if (geoSortList.size() > 1) {
            geoDistance = geoSortList.get(0);
            latLon = geoSortList.get(1);
            if (!geoDistance.toLowerCase().equals(FluidSearch.GEO_DISTANCE)) {
                throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LABEL);
            }
        } else {
            throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LABEL);
        }
        String[] geoSortLatLon = latLon.split(" ");
        if (geoSortLatLon.length > 1) {
            Double lat = tryParseDouble(geoSortLatLon[0]);
            Double lon = tryParseDouble(geoSortLatLon[1]);
            if (lat != null && lon != null) {
                return new GeoPoint(lat, lon);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LAT_LON);
            }
        } else {
            throw new InvalidParameterException(FluidSearch.INVALID_GEOSORT_LAT_LON);
        }
    }

    public static Projection getProjection(String includes, String excludes) {
        Projection projObject = new Projection();
        projObject.includes = includes;
        projObject.excludes = excludes;
        return projObject;
    }

    public static Integer getValidAggregationSize(String size) throws ArlasException {
        Integer s = tryParseInteger(size);
        if (s != null) {
            return s;
        } else throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
    }

    public static void formatRangeValues(Long min, Long max, CollectionReference collectionReference) {
        String format = collectionReference.params.customParams.get(CollectionReference.TIMESTAMP_FORMAT);
        TimestampTypeMapper.formatDate(min, format);
        TimestampTypeMapper.formatDate(max, format);
    }

    /**
     *
     * @param geometries list of geometry strings : a bbox string 'west,south,east,north' or a WKT polygon string
     * @param bbox a BoundingBox object
     * @return the list of intersections of each passed geometry with the given bbox.
     * @throws ArlasException
     */
    public static List<String> simplifyPwithinAgainstBbox(List<String> geometries, BoundingBox bbox) throws ArlasException {
        List<String> simplifiedGeometries = new ArrayList<>();
        List<MultiValueFilter<String>> geoFilters = ParamsParser.getStringMultiFilter(geometries);
        if (geoFilters != null && !geoFilters.isEmpty()) {
            for (MultiValueFilter<String> geos : geoFilters) {
                StringBuffer buff = new StringBuffer();
                for (String geo : geos) {
                    Geometry simplifiedGeometry = GeoTileUtil.bboxIntersects(bbox, geo);
                    if (simplifiedGeometry != null) {
                        if (buff.length() > 0)
                            buff.append(";");
                        buff.append(simplifiedGeometry.toString());
                    }
                }
                if (buff.length() > 0) {
                    simplifiedGeometries.add(buff.toString());
                }
            }
        }
        return simplifiedGeometries;
    }

    public static Integer tryParseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double tryParseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long tryParseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
