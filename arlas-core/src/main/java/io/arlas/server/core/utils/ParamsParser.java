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

package io.arlas.server.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.BadRequestException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.*;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.FieldType;
import io.dropwizard.jersey.params.IntParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.format.DateTimeFormat;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.RepeatedPointTester;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.arlas.server.core.services.FluidSearchService.*;
import static io.arlas.server.core.utils.CheckParams.GEO_AGGREGATION_TYPE_ENUMS;

public class ParamsParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AGG_INTERVAL_PARAM = "interval-";
    private static final String AGG_FORMAT_PARAM = "format-";
    private static final String AGG_COLLECT_FIELD_PARAM = "collect_field-";
    private static final String AGG_COLLECT_FCT_PARAM = "collect_fct-";
    private static final String AGG_COLLECT_FCT_PRECISION_THRESHOLD = "precision_threshold-";
    private static final String AGG_ORDER_PARAM = "order-";
    private static final String AGG_ON_PARAM = "on-";
    private static final String AGG_SIZE_PARAM = "size-";
    private static final String AGG_INCLUDE_PARAM = "include-";
    public static final String AGG_RAW_GEOMETRIES_PARAM = "raw_geometries-";
    private static final String AGG_AGGREGATED_GEOMETRIES_PARAM = "aggregated_geometries-";
    private static final String INVALID_DATE_MATH_EXPRESSION = "Invalid date math expression";
    private static final String AGG_FETCHHITS_PARAM = "fetch_hits-";

    private static final Pattern HITS_FETCHER_PATTERN = Pattern.compile("(\\d*)(\\()(.*)(\\))");
    private static final Pattern SORT_FIELDS_PATTERN = Pattern.compile("\\((.*?)\\)$");

    private static final List<OperatorEnum> GEO_OP = Arrays.asList(OperatorEnum.within, OperatorEnum.notwithin, OperatorEnum.intersects, OperatorEnum.notintersects);
    private static final List<OperatorEnum> GEO_OP_WITHIN = Arrays.asList(OperatorEnum.within, OperatorEnum.notwithin);
    private static final  GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    public static final String INVALID_AGG_RETURNED_GEOMETRIES = "Invalid `returned_geometries` parameter. It should be `returned_geometries-{comma separated strategies and geo_fields}(+/-sort_field)`";

    public static final String RANGE_ALIASES_CHARACTER = "$";
    public static final String TIMESTAMP_ALIAS = "timestamp";
    public static final String BAD_FIELD_ALIAS = "This alias does not represent a collection configured field. ";


    public static List<Aggregation> getAggregations(CollectionReference collectionReference, List<String> agg) throws ArlasException {
        List<Aggregation> aggregations = new ArrayList<>();
        if (agg != null && agg.size() > 0) {
            for (String aggregation : agg) {
                Aggregation aggregationModel;
                if (CheckParams.isAggregationParamValid(aggregation)) {
                    List<String> aggParameters = Arrays.asList(aggregation.split(":"));
                    aggregationModel = getAggregationModel(collectionReference, aggParameters);
                    aggregations.add(aggregationModel);
                }
            }
        }
        return aggregations;
    }

    public static Aggregation getAggregationModel(CollectionReference collectionReference, List<String> agg) throws ArlasException {
        Aggregation aggregationModel = new Aggregation();
        aggregationModel.type = AggregationTypeEnum.valueOf(agg.get(0));
        for (String parameter : agg) {
            if (parameter.contains(AGG_INTERVAL_PARAM)) {
                if (aggregationModel.type.equals(AggregationTypeEnum.datehistogram)) {
                    aggregationModel.interval = getDatehistogramAggregationInterval(parameter.substring(AGG_INTERVAL_PARAM.length()));
                } else if (GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type)) {
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
            } else if (parameter.contains(AGG_RAW_GEOMETRIES_PARAM)) {
                aggregationModel.rawGeometries = getAggregationRawGeometries(parameter.substring(AGG_RAW_GEOMETRIES_PARAM.length()), collectionReference);
            } else if (parameter.contains(AGG_AGGREGATED_GEOMETRIES_PARAM)) {
                aggregationModel.aggregatedGeometries = getAggregatedGeometries(parameter.substring(AGG_AGGREGATED_GEOMETRIES_PARAM.length()));
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
            throw new BadRequestException("fetch_hits should not be null nor empty");
        }
        Matcher matcher = HITS_FETCHER_PATTERN.matcher(fetchHitsString);
        if (!matcher.matches()) {
            throw new InvalidParameterException("Invalid fetch_hits syntax. It should respect the following pattern : {size*}(+{field1}, -{field2}, {field3}, ...)");
        }
        hitsFetcher.size = Optional.ofNullable(ParamsParser.tryParseInteger(matcher.group(1))).orElse(1);
        hitsFetcher.include = Arrays.asList(matcher.group(3).split(","));
        return hitsFetcher;
    }
    
    private static List<RawGeometry> getAggregationRawGeometries(String rawGeometriesString, CollectionReference collectionReference) throws ArlasException {
        List<RawGeometry> rawGeometries = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(rawGeometriesString)) {
            for(String rg: rawGeometriesString.split(";")) {
                Matcher sortMatcher = SORT_FIELDS_PATTERN.matcher(rg);
                RawGeometry rawGeometry = new RawGeometry();
                if (sortMatcher.find()) {
                    rawGeometry.sort = sortMatcher.group(1);
                } else {
                    rawGeometry.sort = collectionReference.params.timestampPath;
                }
                String geometriesString = sortMatcher.replaceAll("");
                if (StringUtils.isBlank(geometriesString)) {
                    throw new InvalidParameterException(INVALID_AGG_RETURNED_GEOMETRIES);
                } else {
                    rawGeometry.geometry = geometriesString;
                }
                rawGeometries.add(rawGeometry);
            }
        }
        return rawGeometries;
    }
    private static List<AggregatedGeometryEnum> getAggregatedGeometries(String aggregatedGeometriesString) throws ArlasException {
        List<AggregatedGeometryEnum> aggregatedGeometries = null;
        if (!StringUtil.isNullOrEmpty(aggregatedGeometriesString)) {
            Set<String> aggregatedGeometriesStrings = new HashSet<>(Arrays.asList(aggregatedGeometriesString.split(",")));
            for (String ag : aggregatedGeometriesStrings) {
                if (aggregatedGeometries == null) {
                    aggregatedGeometries = new ArrayList<>();
                }
                aggregatedGeometries.add(AggregatedGeometryEnum.fromValue(ag));
            }
        }
        return aggregatedGeometries;
    }

    private static List<Metric> getAggregationMetrics(List<String> agg) throws ArlasException {
        List<Metric> metrics;
        try {
            List<String> collectFields = agg.stream().filter(s -> s.contains(AGG_COLLECT_FIELD_PARAM))
                    .map(s -> s.substring(AGG_COLLECT_FIELD_PARAM.length()))
                    .collect(Collectors.toList());
            List<CollectionFunction> collectFcts = agg.stream().filter(s -> s.contains(AGG_COLLECT_FCT_PARAM))
                    .map(s -> CollectionFunction.valueOf(s.substring(AGG_COLLECT_FCT_PARAM.length()).toUpperCase()))
                    .collect(Collectors.toList());
            /** This code can be kept without commenting it. */
            List<String> collectPrecisionThreshold = agg.stream()
                    .map(s -> {
                        if(s.contains(AGG_COLLECT_FCT_PRECISION_THRESHOLD)){
                            return s.substring(AGG_COLLECT_FCT_PRECISION_THRESHOLD.length());
                        } else {
                            //This value will be never use, just to fill the List to have the right size and to make work get(i)
                            return null;
                        }
                    }).collect(Collectors.toList());
            CheckParams.checkCollectionFunctionValidity(collectFields, collectFcts);
            metrics = IntStream.range(0, collectFcts.size())
                    .filter(i -> i < collectFcts.size())
                    .mapToObj(i -> {
                        return  new Metric(collectFields.get(i), collectFcts.get(i));

                         /**if(collectPrecisionThreshold.get(i) == null){
                          return  new Metric(collectFields.get(i), collectFcts.get(i));
                        }else{
                            return  new Metric(collectFields.get(i), collectFcts.get(i), Integer.parseInt(collectPrecisionThreshold.get(i)));
                        } */
                    }).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException("Invalid collection function");
        }
        return metrics;
    }

    public static Interval getDatehistogramAggregationInterval(String intervalString) throws ArlasException {
        if (!StringUtil.isNullOrEmpty(intervalString)) {
            String[] sizeAndUnit = intervalString.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=[a-zA-Z])");
            if (sizeAndUnit.length == 2) {
                return new Interval(tryParseInteger(sizeAndUnit[0]), UnitEnum.valueOf(sizeAndUnit[1].toLowerCase()));
            } else throw new InvalidParameterException("The date interval '" + intervalString + "' is not valid");
        } else {
            return null;
        }
    }

    public static Interval getHistogramAggregationInterval(String intervalString) {
        return new Interval(tryParseDouble(intervalString), null);
    }

    public static Interval getGeohashAggregationInterval(String intervalString) {
        return new Interval(tryParseInteger(intervalString), null);
    }

    public static String getFieldFromFieldAliases(String fieldAlias, CollectionReference collectionReference) throws ArlasException {
        boolean isAlias = fieldAlias.startsWith(RANGE_ALIASES_CHARACTER);
        if (isAlias) {
            String alias = fieldAlias.substring(1);
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
        // TODO: check if format is in DateTimeFormat (joda)
        return Objects.requireNonNullElse(aggFormat, "yyyy-MM-dd-HH:mm:ss");
    }

    public static Filter getFilter(CollectionReference collectionReference, String serializedFilter) throws InvalidParameterException {
        if (serializedFilter != null) {
            List<Filter> fList;
            String sf = "[" + serializedFilter + "]";
            try {
                List<Map<String, Filter>> pf = objectMapper.readValue(sf,
                        new TypeReference<List<Map<String, Filter>>>() {});
                fList = pf.stream()
                        .filter(m -> m.get(collectionReference.collectionName) != null)
                        .map(m -> m.get(collectionReference.collectionName)).toList();
            } catch (IOException e) {
                try {
                    fList = objectMapper.readValue(sf, new TypeReference<List<Filter>>() {});
                } catch (JsonProcessingException ex) {

                    throw new InvalidParameterException(INVALID_FILTER + ": '" + sf + "'", ex);
                }
            }
            // if not null and parsing ok then we have at least one filter
            Filter retFilter = fList.get(0);
            if (retFilter.righthand == null) {
                retFilter.righthand = Boolean.TRUE;
            }

            for (int i=1; i<fList.size(); i++) {
                // for now, a list of partition filters is combined with OR. TODO: support more complex combination
                retFilter.f.get(0).addAll(fList.get(i).f.get(0));
            }
            return retFilter;
        } else {
            return null;
        }
    }

    public static Filter getFilter(CollectionReference collectionReference,
                                   List<String> filters, List<String> q, String dateFormat, Boolean righthand) throws ArlasException {
        if (righthand == null) {
            righthand = Boolean.TRUE;
        }
        return getFilter(collectionReference, filters, q, dateFormat, righthand, null, null);
    }

    /**
     *
     * @param collectionReference collection reference is used to get the type of geometries in geofilters when tileBbox is specified. Can be set to null
     * @param filters list of f filters
     * @param q list of q filters
     * @param dateFormat format of dates values that are used in f and q filters to query dates
     * @param tileBbox bounding box of the tile. Used in tiled geosearch
     * @param pwithinBbox a `point-within-bbox` expression
     * @return Filter objet
     */
    public static Filter getFilter(CollectionReference collectionReference,
                                   List<String> filters, List<String> q, String dateFormat, Boolean righthand,
                                   BoundingBox tileBbox, Expression pwithinBbox) throws ArlasException {
        Filter filter = new Filter();
        filter.f = new ArrayList<>();
        for (String multiF : filters) {
            MultiValueFilter<Expression> multiFilter = new MultiValueFilter<>();
            for (String f : getMultiFiltersFromSemiColonsSeparatedString(multiF)) {
                if (!StringUtil.isNullOrEmpty(f)) {
                    String[] operands = f.split(":");
                    if (operands.length < 3) {
                        throw new InvalidParameterException(INVALID_PARAMETER_F + ": '" + f + "'");
                    }
                    // merge again last elements in case value contained a ':'
                    String value = String.join(":", Arrays.copyOfRange(operands, 2, operands.length));

                    if (GEO_OP.contains(OperatorEnum.valueOf(operands[1]))) {
                        value = getValidGeometry(value, righthand);
                        if(tileBbox != null && collectionReference != null) {
                            boolean isPwithin = isPwithin(collectionReference, operands[0], operands[1]);
                            if (isPwithin){
                                Geometry simplifiedGeometry = GeoTileUtil.bboxIntersects(tileBbox, value);
                                if (simplifiedGeometry != null) {
                                    value = simplifiedGeometry.toString();
                                }
                            }
                        }

                    }
                    if (value != null) {
                        Expression expression = new Expression(operands[0], OperatorEnum.valueOf(operands[1]), value);
                        multiFilter.add(expression);
                    }
                }
            }
            filter.f.add(multiFilter);
        }
        // add a pwithin query if there is no geo-filter inside the tile
        if (pwithinBbox != null) {
            filter.f.add(new MultiValueFilter<>(pwithinBbox));
        }
        filter.q = getStringMultiFilter(q);
        filter.dateformat = dateFormat;
        filter.righthand = righthand;
        return filter;
    }

    private static boolean isPwithin(CollectionReference collectionReference, String field, String op) throws ArlasException {
        if (GEO_OP_WITHIN.contains(OperatorEnum.valueOf(op))) {
            return CollectionReferenceManager.getInstance().getType(collectionReference, field, true) ==  FieldType.GEO_POINT;
        }
        return false;
    }

    private static void intersectsToWithin(CollectionReference collectionReference, Expression expression) throws ArlasException {
        if (OperatorEnum.notintersects.equals(expression.op) || OperatorEnum.intersects.equals(expression.op) ) {
            String field = getFieldFromFieldAliases(expression.field, collectionReference);
            if (isGeoPoint(collectionReference, field)) {
                if (OperatorEnum.notintersects.equals(expression.op)) {
                    expression.op = OperatorEnum.notwithin;
                } else if (OperatorEnum.intersects.equals(expression.op)) {
                    expression.op = OperatorEnum.within;
                }
            }
        }
    }

    private static boolean isGeoPoint(CollectionReference collectionReference, String field) throws ArlasException {
        return CollectionReferenceManager.getInstance().getType(collectionReference, field, true) ==  FieldType.GEO_POINT;
    }

    public static Filter getFilterWithValidGeos(CollectionReference collectionReference, Filter filter) throws ArlasException {
        Filter newFilter = new Filter();
        newFilter.q = filter.q;
        newFilter.dateformat = filter.dateformat;
        newFilter.righthand = filter.righthand;
        if (filter.f != null) {
            newFilter.f = new ArrayList<>();
            for (MultiValueFilter<Expression> orFiltersList : filter.f) {
                MultiValueFilter<Expression> newOrFiltersList = new MultiValueFilter<>();
                for (Expression orCond : orFiltersList) {
                    newOrFiltersList.add(getValidGeoFilter(collectionReference, orCond, newFilter.righthand ));
                }
                newFilter.f.add(newOrFiltersList);
            }
        }
        if (newFilter.righthand == null) {
            newFilter.righthand = Boolean.TRUE;
        }
        return newFilter;
    }

    public static Expression getValidGeoFilter(CollectionReference collectionReference, Expression expression, Boolean righthand) throws ArlasException {
        intersectsToWithin(collectionReference, expression);
        if (GEO_OP.contains(expression.op)) {
            expression.value = getValidGeometry(expression.value, righthand);
        }
        return expression;
    }

    public static String getValidGeometry(String geo, Boolean righthand) throws ArlasException {
        if (CheckParams.isBboxMatch(geo)) {
            CheckParams.checkBbox(geo);
            return geo;
        } else {
            Geometry wkt = getValidWKT(geo);
            // For the case of Polygon and MultiPolygon, a check of the coordinates orientation is necessary in order to correctly interpret the "desired" polygon
            // TODO Check if we really need this
            if ((wkt.getGeometryType().equals("Polygon") || wkt.getGeometryType().equals("MultiPolygon"))) {
                List<Polygon> polygonList = new ArrayList<>();
                for (int i = 0; i < wkt.getNumGeometries(); i++) {
                    Polygon subWkt = (Polygon) wkt.getGeometryN(i);
                    if (Orientation.isCCW(subWkt.getCoordinates())) {
                        if (righthand != Boolean.TRUE) {
                            // TODO RISE an exception ? if polygon is CCW the righthand must be true
                            polygonList.addAll(getClockwisePolygons(subWkt).stream().map(p -> (Polygon)GeoUtil.toCounterClockwise(p)).toList());
                        } else {
                            // TODO dont split systemically just if we cross -180/180
                            polygonList.addAll(GeoUtil.splitPolygon(subWkt)._1().stream().map(p -> (Polygon)GeoUtil.toCounterClockwise(p)).toList());
                        }
                    } else {
                        // the wkt is CW
                        if (righthand == Boolean.TRUE) {
                            // TODO RISE an exception ? if polygon is CW the righthand must be false
                            polygonList.addAll(getClockwisePolygons(subWkt).stream().map(p -> (Polygon)GeoUtil.toCounterClockwise(p)).toList());
                        } else {
                            // TODO dont split systemically just if we cross -180/180
                            polygonList.addAll(GeoUtil.splitPolygon(subWkt)._1().stream().map(p -> (Polygon)GeoUtil.toCounterClockwise(p)).toList());
                        }
                    }
                }
                if (polygonList.size() == 1) {
                    return polygonList.get(0).toString();
                } else {
                    return new MultiPolygon(polygonList.toArray(new Polygon[] {}), GEOMETRY_FACTORY).toString();
                }
            } else {
                return geo;
            }
        }
    }

    /** returns a list of CW WKT Polygons were longitudes are between -180 and 180*/
    private static List<Polygon> getClockwisePolygons(Polygon wkt) throws ArlasException {
        // the passed queryGeometry must be interpreted as CCW (righthand = true).
        // If the orientation is CW, we try to build the WKT that goes the other side of the planet.
        // If the topology of the resulted geometry is not valid, an exception is thrown
        Polygon tmpGeometry = (Polygon) wkt.copy();
        Envelope tmpEnvelope = tmpGeometry.getEnvelopeInternal();
        // east is the minX and west is the maxX
        double east = tmpEnvelope.getMinX();
        double west = tmpEnvelope.getMaxX();
        if (west > east && ((east < -180 && west >= -180) || (west > 180 && east <= 180))) {
            // It means west > 180 or east < -180
            if (west >= 180) {
                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, false, 180);
            } else if (east <= -180) {
                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, true, -180);
            }
        } else {
            if (west >= 0) {
                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, false, east);
            } else {
                GeoUtil.translateLongitudesWithCondition(tmpGeometry, 360, true, west);
            }
        }
        IsValidOp validOp = new IsValidOp(tmpGeometry);
        TopologyValidationError err = validOp.getValidationError();
        if (err != null) {
            throw new InvalidParameterException("A Polygon of the given WKT is right oriented. Unable to reverse the orientation of the polygon : " + err);
        }
        return GeoUtil.splitPolygon((Polygon) GeoUtil.readWKT(tmpGeometry.toString()))._1();
    }

    public static List<String> toSemiColonsSeparatedStringList(List<MultiValueFilter<String>> multiValueFilters) {
        return multiValueFilters == null ?  null :
                multiValueFilters.stream().map(multiValueFilter -> String.join(";", multiValueFilter)).collect(Collectors.toList());
    }

    public static Geometry getValidWKT(String wktString) throws InvalidParameterException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Envelope affectedBounds = new Envelope(-360, 360, -180, 180);
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry geom;
        try {
            geom = wkt.read(wktString);
            List<Coordinate> filteredCoord = Arrays.stream(geom.getCoordinates()).filter(affectedBounds::contains).toList();
            if(filteredCoord.size() != geom.getCoordinates().length){
                throw new InvalidParameterException("Coordinates must be contained in the Envelope -360, 360, -180, 180");
            }
            RepeatedPointTester tester = new RepeatedPointTester();
            for(int i = 0; i< geom.getNumGeometries(); i++) {
                IsValidOp validOp = new IsValidOp(geom.getGeometryN(i));
                TopologyValidationError err = validOp.getValidationError();
                if (err != null) {
                    throw new InvalidParameterException(GeoUtil.INVALID_WKT + ": " + err.getMessage());
                }
                if (tester.hasRepeatedPoint(geom.getGeometryN(i))) {
                    throw new InvalidParameterException(GeoUtil.INVALID_WKT + ": duplicate consecutive points detected in " + geom.getGeometryN(i).toText());
                }
            }
        } catch (org.locationtech.jts.io.ParseException ex) {
            throw new InvalidParameterException("Invalid WKT: " + ex.getMessage());
        }
        return geom;
    }

    public static String parseDate(String dateValue, String dateFormat) throws ArlasException {
        return parseDate(dateValue, dateFormat, true);
    }

    public static String parseDate(String dateValue, String dateFormat, boolean convertToMillis) throws ArlasException {
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
                    if (convertToMillis) {
                        parsedDate = String.valueOf(DateTimeFormat.forPattern(dateFormat).withZoneUTC().parseDateTime(dateToParse).getMillis());
                    }
                } catch (DateTimeParseException | IllegalArgumentException e) {
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

    public static Page getPage(IntParam size, IntParam from, String sort, String after, String before) {
        Page page = new Page();
        page.size = size.get();
        page.from = from.get();
        page.sort = sort;
        page.after = after;
        page.before = before;

        return page;
    }

    // returns Pair(lat, lon)
    public static Pair<Double, Double> getGeoSortParamsAsLatLon(String geoSort) throws ArlasException {
        List<String> geoSortList = Arrays.asList(geoSort.split(":"));
        String geoDistance;
        String latLon;
        if (geoSortList.size() > 1) {
            geoDistance = geoSortList.get(0);
            latLon = geoSortList.get(1);
            if (!geoDistance.equalsIgnoreCase(GEO_DISTANCE)) {
                throw new InvalidParameterException(INVALID_GEOSORT_LABEL);
            }
        } else {
            throw new InvalidParameterException(INVALID_GEOSORT_LABEL);
        }
        String[] geoSortLatLon = latLon.split(" ");
        if (geoSortLatLon.length > 1) {
            Double lat = tryParseDouble(geoSortLatLon[0]);
            Double lon = tryParseDouble(geoSortLatLon[1]);
            if (lat != null && lon != null) {
                return Pair.of(lat, lon);
            } else {
                throw new InvalidParameterException(INVALID_GEOSORT_LAT_LON);
            }
        } else {
            throw new InvalidParameterException(INVALID_GEOSORT_LAT_LON);
        }
    }

    public static Projection getProjection(String includes, String excludes) {
        Projection projObject = new Projection();
        projObject.includes = includes;
        projObject.excludes = excludes;
        return projObject;
    }

    /**
     * This method enriches the `includes` attribute of the given `projection` parameter with fields given in `returned_geometries`
     */
    public static Projection enrichIncludes(Projection projection, String returned_geometries) {
        if (returned_geometries != null) {
            List<String> includes = new ArrayList<>();
            if (projection.includes != null) Collections.addAll(includes, projection.includes.split(","));
            if (!includes.isEmpty()) {
                Collections.addAll(includes, returned_geometries.split(","));
            }
            projection.includes = String.join(",", includes);

        }
        return projection;
    }

    public static Integer getValidAggregationSize(String size) throws ArlasException {
        Integer s = tryParseInteger(size);
        if (s != null) {
            return s;
        } else throw new InvalidParameterException(INVALID_SIZE);
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
