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

import com.neovisionaries.i18n.LanguageAlpha3Code;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.BadRequestException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.exceptions.NotAllowedException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Inspire;
import io.arlas.server.core.model.Keyword;
import io.arlas.server.core.model.enumerations.*;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.FieldType;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static io.arlas.server.core.services.FluidSearchService.*;

public class CheckParams {

    private static final String POLYGON_TYPE = "POLYGON";
    private static final String INVALID_XYZ_PARAMETER = "Z must be between 0 and 28. X and Y must be between 0 and (2^Z-1)";
    private static final String INVALID_DATE_MATH_UNIT = "Invalid date math unit. Please use the following list : y, M, w, d, h, H, m, s. ";
    private static final String INVALID_DATE_MATH_VALUE = "Invalid date math value. Please specify an integer. ";
    private static final String INVALID_DATE_MATH_OPERATOR = "Invalid date math operator. Please use the following list : /, +, -";
    private static final String MISSING_DATE_MATH_UNIT = "Missing date math unit";
    private static final String INVALID_DATE_MATH_EXPRESSION = "Invalid date math expression";
    private static final String INVALID_AGGREGATION_PARAMETER = "Invalid aggregation syntax. Must start with {type}:{field}:...";
    private static final String INVALID_AGGREGATION = "Invalid aggregation parameters. Type and field must be specified";
    private static final String INVALID_AGGREGATION_TYPE = "Invalid aggregation TYPE. Must be datehistogram, geohash, histogram or terms ";
    private static final String INVALID_COMPUTE_FIELD = "The field name/path should not be null.";
    private static final String INVALID_COMPUTE_METRIC = "The metric value should not be null.";
    private static final String INVALID_COMPUTE_REQUEST = "Invalid compute request : ";
    private static final String INVALID_ORDER_VALUE = "Invalid 'order-' value : ";
    private static final String REDUNDANT_COLLECT_FIELD_COLLECT_FCT = "Bad request : the same 'collect-fct' is applied to the same 'collect-field' twice or more.";
    private static final String INVALID_ON_VALUE = "Invalid 'on-' value : ";
    private static final String BAD_COLLECT_FCT_COLLECT_FIELD_NUMBERS = "'collect_field' and 'collect_fct' occurrences should be even.";
    private static final String DATE_NOW = "now";
    private static final String AGGREGATED_GEOMETRY_NOT_SUPPORTED = "'cell' & 'cell_center' are only supported for geohash and geotile aggregation type.";

    public static final String INTERVAL_NOT_SPECIFIED = "Interval parameter is not specified.";
    public static final String INTERVAL_VALUE_NOT_SPECIFIED = "Interval value is missing.";
    public static final String INTERVAL_UNIT_NOT_SPECIFIED = "Interval unit is missing.";
    public static final String NO_INTERVAL_UNIT_FOR_GEOHASH_NOR_HISTOGRAM = "Interval unit must not be specified for geohash/geotile nor histogram aggregations.";
    public static final String NO_TERM_INTERVAL = "'Interval' should not be specified for term aggregation.";
    public static final String RAW_GEOMETRIES_NULL_OR_EMPTY = "'geometries' should not be null nor empty";

    public static final List<AggregationTypeEnum> GEO_AGGREGATION_TYPE_ENUMS = Arrays.asList(AggregationTypeEnum.geohash, AggregationTypeEnum.geotile, AggregationTypeEnum.geohex);

    public CheckParams() {
    }

    public static void checkAggregationRequest(Request request, CollectionReference collectionReference) throws ArlasException {
        if (request == null || !(request instanceof AggregationsRequest) || ((AggregationsRequest) request).aggregations == null)
            throw new BadRequestException("Aggregation should not be null");
        else if (request != null) {
            checkAggregations((AggregationsRequest) request, collectionReference);
        }
    }

    public static void checkAggregationModel(Aggregation aggregation, CollectionReference collectionReference) throws ArlasException {
        // Check 'type' & 'field'
        if (aggregation.type == null || (StringUtil.isNullOrEmpty(aggregation.field) && aggregation.type != AggregationTypeEnum.datehistogram)) {
            throw new InvalidParameterException(INVALID_AGGREGATION);
        }
        // Check 'order'
        if (aggregation.order != null && aggregation.order != Order.asc && aggregation.order != Order.desc) {
            throw new InvalidParameterException(INVALID_ORDER_VALUE + aggregation.order.name());
        }
        // Check 'on'
        if (aggregation.on != null && aggregation.on != OrderOn.count && aggregation.on != OrderOn.field && aggregation.on != OrderOn.result) {
            throw new InvalidParameterException(INVALID_ON_VALUE + aggregation.on.name());
        }
        // Check 'collect-field' and 'collect-fct' are not redundant
        if (aggregation.metrics != null && aggregation.metrics.stream().distinct().count() != aggregation.metrics.stream().count()) {
            throw new InvalidParameterException(REDUNDANT_COLLECT_FIELD_COLLECT_FCT);
        }
        // Check Interval parameter validity according to aggregation type
        checkAggregationIntervalParameter(aggregation);
        // Check include parameter validity according to aggregation type
        checkAggregationIncludeParameter(aggregation);
        // Check aggregated_geometries validity according to aggregation type
        checkAggregatedGeometryParameter(aggregation);
        // Check raw_geometries validity according to aggregation type
        checkRawGeometriesParameter(aggregation, collectionReference);
    }

    public static void checkComputationRequest(Request request, CollectionReference collectionReference) throws ArlasException {
        if (request == null || !(request instanceof ComputationRequest)) {
            throw new BadRequestException("Compute request should not be null");
        } else {
            ComputationRequest computationRequest = (ComputationRequest) request;
            if (StringUtil.isNullOrEmpty(computationRequest.field)) {
                throw new InvalidParameterException(INVALID_COMPUTE_REQUEST + INVALID_COMPUTE_FIELD);
            }
            if (computationRequest.metric == null) {
                throw new InvalidParameterException(INVALID_COMPUTE_REQUEST + INVALID_COMPUTE_METRIC);
            }
            if (computationRequest.metric.equals(ComputationEnum.GEOBBOX) || computationRequest.metric.equals(ComputationEnum.GEOCENTROID)) {
                FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, computationRequest.field, false);
                if (!FieldType.GEO_POINT.equals(fieldType) && !FieldType.GEO_SHAPE.equals(fieldType) && !FieldType.UNKNOWN.equals(fieldType)) {
                    throw new InvalidParameterException(INVALID_COMPUTE_REQUEST + "`" + computationRequest.metric + "` must be applied on a geo-point or a geo-shape field");
                }
            } else if (!computationRequest.metric.equals(ComputationEnum.CARDINALITY)) {
                // Except for CARDINALITY, GEOBBOX and GEOCENTROID, the field on which the metric is computed should be numeric or date
                FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, computationRequest.field, false);
                if (!FieldType.getComputableTypes().contains(fieldType)) {
                    throw new InvalidParameterException(INVALID_COMPUTE_REQUEST + "`" + computationRequest.metric + "` must be applied on a numeric or date field");
                }
            }

        }
    }

    public static void checkGeoFilter(List<MultiValueFilter<String>> geoStrings) throws ArlasException{
        if (geoStrings != null && !geoStrings.isEmpty()) {
            for (MultiValueFilter<String> geos : geoStrings) {
                for (String geo : geos) {
                    if (isBboxMatch(geo)) {
                        checkBbox(geo);
                    } else {
                        checkWKT(geo);
                    }
                }
            }
        }
    }


    public static void checkFilter(Filter filter) throws ArlasException {
        if ((filter.f == null || filter.f.isEmpty()) && !StringUtil.isNullOrEmpty(filter.dateformat)) {
            throw new BadRequestException("Date format is specified but no date field is queried in f filter");
        }
        checkDateFormat(filter.dateformat);
    }

    public static void checkDateFormat(String dateFormat) throws ArlasException {
        if (!StringUtil.isNullOrEmpty(dateFormat)) {
            if (dateFormat.contains("||")) {
                throw new NotAllowedException("'||' are not allowed for date formats");
            }
            try {
                DateTimeFormat.forPattern(dateFormat);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException("Invalid date format '" + dateFormat + "'. Reason : " + e.getMessage());
            }
        }
    }

    public static boolean isBboxMatch(String geometry) {
        String floatPattern = "[-+]?[0-9]*\\.?[0-9]+";
        String bboxPattern = floatPattern + "," + floatPattern + "," + floatPattern + "," + floatPattern;
        return Pattern.compile("^" + bboxPattern + "$").matcher(geometry).matches();
    }

    public static void checkBbox(String bbox) throws InvalidParameterException {
        double[] tlbr = CheckParams.toDoubles(bbox);
        // west, south, east, north
        if (!(tlbr.length == 4 && isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new InvalidParameterException(INVALID_BBOX);
        }
    }

    public static void checkWKT(String wktString) throws InvalidParameterException {
        GeoUtil.checkWKT(wktString);
    }

    public static void checkAggregationIncludeParameter(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.include != null && aggregationModel.type != AggregationTypeEnum.term) {
            throw new BadRequestException(NO_INCLUDE_TO_SPECIFY);
        }
    }

    public static void checkRawGeometriesParameter(Aggregation aggregationModel, CollectionReference collectionReference) throws ArlasException {
        if (aggregationModel.rawGeometries != null) {
            for (RawGeometry rg : aggregationModel.rawGeometries) {
                if (StringUtils.isBlank(rg.geometry)) {
                    throw new BadRequestException(RAW_GEOMETRIES_NULL_OR_EMPTY);
                } else {
                    FieldType fieldType = CollectionReferenceManager.getInstance().getType(collectionReference, rg.geometry, true); // will throw ArlasException if not existing
                    if (fieldType != FieldType.GEO_POINT && fieldType != FieldType.GEO_SHAPE) {
                        throw new InvalidParameterException("`" + rg.geometry + "` is not a geo-point or a geo-shape field");
                    }
                    if (StringUtils.isBlank(rg.sort)) {
                        rg.sort = collectionReference.params.timestampPath;
                    }
                }
            }
        }
    }
    public static void checkAggregatedGeometryParameter(Aggregation aggregationModel) throws ArlasException {
        if (aggregationModel.aggregatedGeometries != null) {
            List<AggregatedGeometryEnum> geometries = aggregationModel.aggregatedGeometries;
            if ((geometries.contains(AggregatedGeometryEnum.CELL) || geometries.contains(AggregatedGeometryEnum.CELLCENTER) ||
                    geometries.contains(AggregatedGeometryEnum.GEOHASH) || geometries.contains(AggregatedGeometryEnum.GEOHASH_CENTER))
                    && !GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type)) {
                throw new NotAllowedException(AGGREGATED_GEOMETRY_NOT_SUPPORTED);
            }
            if (GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type)
                    && geometries.isEmpty() && aggregationModel.rawGeometries == null) {
                aggregationModel.aggregatedGeometries = new ArrayList<>();
                aggregationModel.aggregatedGeometries.add(AggregatedGeometryEnum.CELLCENTER);
            }
        } else if (GEO_AGGREGATION_TYPE_ENUMS.contains(aggregationModel.type)
                && aggregationModel.rawGeometries == null) {
            aggregationModel.aggregatedGeometries = new ArrayList<>();
            aggregationModel.aggregatedGeometries.add(AggregatedGeometryEnum.CELLCENTER);
        }
    }

    public static void checkAggregationIntervalParameter(Aggregation aggregationModel) throws ArlasException {
        // - Aggregation Interval must be specified for geohash, histogram and datehistogram only
        // - Interval value must be a positive Integer for geohash and datehistogram aggregations and positive decimal for histogram aggregation
        // - Interval unit must be specified for datehistogram aggregation only
        if (aggregationModel.type == AggregationTypeEnum.term) {
            if (aggregationModel.interval != null) {
                throw new BadRequestException(NO_TERM_INTERVAL);
            }
        } else {
            Interval interval = aggregationModel.interval;
            if (interval == null) {
                throw new BadRequestException(INTERVAL_NOT_SPECIFIED);
            } else {
                if (interval.value == null) {
                    throw new BadRequestException(INTERVAL_VALUE_NOT_SPECIFIED);
                } else {
                    Number intervalValue = 0;
                    switch (aggregationModel.type) {
                        case datehistogram:
                        case geohash:
                        case geotile:
                        case geohex:
                            intervalValue = ParamsParser.tryParseInteger(interval.value.toString());
                            break;
                        case histogram:
                            intervalValue = ParamsParser.tryParseDouble(interval.value.toString());
                            break;
                    }
                    if (intervalValue == null || intervalValue.doubleValue() <= 0) {
                        switch (aggregationModel.type) {
                            case datehistogram:
                                throw new InvalidParameterException("The datehistogram interval must be a positive integer.");
                            case geotile:
                            case geohex:
                                if (intervalValue.doubleValue() < 0) {
                                    throw new InvalidParameterException("The geotile precision is not valid. It must be a positive integer.");
                                }
                                break;
                            case geohash:
                                throw new InvalidParameterException("The geohash precision is not valid. It must be an integer between 1 and 12.");
                            case histogram:
                                throw new InvalidParameterException("The histogram interval is not valid. It must be a positive decimal number.");
                        }
                    }
                    if (intervalValue != null && aggregationModel.type == AggregationTypeEnum.geohash && ParamsParser.tryParseInteger(interval.value.toString()) >= 13) {
                        throw new InvalidParameterException("The geohash precision is not valid. It must be an integer between 1 and 12.");
                    } else if (intervalValue != null && aggregationModel.type == AggregationTypeEnum.geotile && ParamsParser.tryParseInteger(interval.value.toString()) > 29) {
                        throw new InvalidParameterException("The geotile precision is not valid. It must be an integer between 0 and 29.");
                    } else if (intervalValue != null && aggregationModel.type == AggregationTypeEnum.geohex && ParamsParser.tryParseInteger(interval.value.toString()) > 15) {
                        throw new InvalidParameterException("The geohex precision is not valid. It must be an integer between 0 and 15.");
                    } else if (intervalValue != null) {
                        aggregationModel.interval.value = intervalValue;
                    }
                }
                if (interval.unit != null && aggregationModel.type != AggregationTypeEnum.datehistogram) {
                    throw new BadRequestException(NO_INTERVAL_UNIT_FOR_GEOHASH_NOR_HISTOGRAM);
                }
                if (interval.unit == null && aggregationModel.type == AggregationTypeEnum.datehistogram) {
                    throw new BadRequestException(INTERVAL_UNIT_NOT_SPECIFIED);
                }
            }
        }
    }

    public static void checkPageSize(Page page) throws ArlasException {
        if (page != null) {
            if (page.size == null || page.size <= 0){
                throw new InvalidParameterException(INVALID_SIZE);
            }
        }
    }

    public static void checkPageFrom(Page page) throws ArlasException {
        if (page != null) {
            if (page.from == null || page.from < 0) {
                throw new InvalidParameterException(INVALID_FROM);
            }
        }
    }

    public static void checkPageAfter(Page page, String idCollectionField) throws ArlasException {
        if(page != null && page.after != null && page.before != null){
            throw new BadRequestException("'after' parameter cannot be used with 'before' parameter ");
        }
        List<String> afterList;
        if (page != null && ( page.after != null || page.before != null)) {
            String mode = "";
            if(page.after != null){
                mode="'after'";
                afterList= Arrays.asList(page.after.split(","));
            }else{
                mode="'before'";
                afterList = Arrays.asList(page.before.split(","));
            }
            String message = "";
            // check compatibility between after with from
            if (page.from != null && page.from != 0) {
                message = "%s parameter cannot be used if 'from' parameter is higher than 0. If you want to use %s, please set 'from' to 0 or keep it empty";
                throw new BadRequestException(String.format(message,mode, mode));
            }
            // check compatibility between after and sort parameters
            int afterSize = afterList.size();
            if (page.sort == null) {
                message = "%s parameter cannot be used without setting 'sort' parameter.";
                throw new BadRequestException(String.format(message,mode));
            }
            String[] sortList = page.sort.split(",");
            int sortSize = sortList.length;
            if (afterSize != sortSize){
                message = "The number of %s elements must be equal to the number of 'sort' elements";
                throw new BadRequestException(String.format(message,mode));
            }
            String lastSortElement = sortList[sortSize-1];
            if(lastSortElement.startsWith(("-"))){
                lastSortElement = lastSortElement.substring(1);
            }
            if(lastSortElement.compareTo(idCollectionField) != 0){
                message = "If %s parameter is set, the last element of 'sort' must be equal to {collection.params.idPath} and the corresponding value for %s must be the one returned in {md.id}";
                throw new BadRequestException(String.format(message,mode));
            }
        }
    }

    public static void checkRangeValidity(String range) {
        if ((range.isEmpty() || !(range.startsWith("[") || range.startsWith("]")) ||
                !(range.endsWith("[") || range.endsWith("]")) ||
                !(range.contains("<")))) {
            throw new java.security.InvalidParameterException(INVALID_PARAMETER_F);
        }
    }

    public static void checkTimestampFormatValidity(String timestamp) throws ArlasException {
        if (ParamsParser.tryParseLong(timestamp) == null) {
            // Check date math validity
            if (timestamp.length() >= 3) {
                // Check if the anchor date is equal to "now"
                if (timestamp.substring(0, 3).equals(DATE_NOW)) {
                    if (timestamp.length() > 3) {
                        String postAnchor = timestamp.substring(3);
                        checkPostAnchorValidity(postAnchor);
                    }
                } else {
                    // If the anchor date is not equal to "now", then it should be a millisecond timestamp ending with "||"
                    if (timestamp.contains("||")) {
                        String[] operands = timestamp.split("\\|\\|");
                        if (ParamsParser.tryParseLong(operands[0]) == null) {
                            throw new InvalidParameterException(INVALID_TIMESTAMP_RANGE);
                        } else {
                            if (operands.length == 1) {
                                throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                            } else {
                                checkPostAnchorValidity(operands[1]);
                            }
                        }
                    } else {
                        throw new InvalidParameterException(INVALID_TIMESTAMP_RANGE);
                    }
                }
            } else {
                throw new InvalidParameterException(INVALID_TIMESTAMP_RANGE);
            }
        }
    }

    public static Boolean isPolygon(Geometry geometry) {
        String geometryType = geometry.getGeometryType().toUpperCase();
        if (geometryType.equals(POLYGON_TYPE))
            return true;
        else
            return false;
    }

    public static Boolean isSimplePolygon(Geometry geometry) {
        String geometryType = geometry.getGeometryType().toUpperCase();
        if (isPolygon(geometry)) {
            Integer interiorRings = ((Polygon) geometry).getNumInteriorRing();
            return interiorRings == 0;
        } else
            return false;
    }

    // Verify if agg parameter contains at least type:field and verify that type
    // matches : datehistogram, geohash, histogram or terms
    public static Boolean isAggregationParamValid(String agg) throws ArlasException {
        String[] aggParts = agg.split(":");
        if (aggParts.length > 1) {
            try {
                AggregationTypeEnum.valueOf(aggParts[0]);
                return true;
            } catch (Exception e) {
                throw new InvalidParameterException(INVALID_AGGREGATION_TYPE);
            }
        } else {
            throw new InvalidParameterException(INVALID_AGGREGATION_PARAMETER);
        }
    }

    public static void checkCollectionFunctionValidity(List<String> collectField, List<CollectionFunction> collectFct) throws ArlasException {
        if (collectFct.size() != collectField.size()) {
            throw new BadRequestException(BAD_COLLECT_FCT_COLLECT_FIELD_NUMBERS);
        }
    }

    private static void checkAggregations(AggregationsRequest aggregations, CollectionReference collectionReference) throws ArlasException {
        if (aggregations != null && aggregations.aggregations != null && aggregations.aggregations.size() > 0) {
            for (Aggregation aggregationModel : aggregations.aggregations) {
                checkAggregationModel(aggregationModel, collectionReference);
            }
        }
    }

    public static void checkXYZTileValidity(int x, int y, int z) throws ArlasException {
        if (z >= 0 && z <= 28) {
            if (!isIntegerInXYZRange(x, z) || !isIntegerInXYZRange(x, z)) {
                throw new InvalidParameterException(INVALID_XYZ_PARAMETER);
            }
        } else {
            throw new InvalidParameterException(INVALID_XYZ_PARAMETER);
        }
    }

    public static boolean isBboxLatLonInCorrectRanges(double[] tlbr) {
        // west, south, east, north
        return tlbr[1] >= -90 && tlbr[3] >= -90 && tlbr[1] <= 90 && tlbr[3] <= 90 &&
                tlbr[0] >= -180 && tlbr[2] >= -180 && tlbr[0] <= 180 && tlbr[2] <= 180;
    }

    public static void checkExcludeField(List<String> excludeFields, List<String> fields) throws NotAllowedException {
        ArrayList<Pattern> excludeFieldsPattern = new ArrayList<>();
        excludeFields.forEach(field ->
                excludeFieldsPattern.add(Pattern.compile("^" + field.replace(".", "\\.").replace("*", ".*") + ".*$"))
        );
        boolean excludePath;
        for (String field : fields) {
            excludePath = excludeFieldsPattern.stream().anyMatch(pattern -> pattern.matcher(field).matches());
            if (excludePath)
                throw new NotAllowedException("Unable to exclude field " + field + " used for id, geometry, centroid or timestamp.");
        }
    }

    public static void checkMissingInspireParameters(CollectionReference collectionReference) throws ArlasException {
        Inspire collectionReferenceInspire = collectionReference.params.inspire;
        if (collectionReferenceInspire == null) {
            throw new BadRequestException("Inspire node must be set in Collection Reference parameters");
        }
        // check keywords
        if (collectionReferenceInspire.keywords == null || collectionReferenceInspire.keywords.size() == 0) {
            throw new BadRequestException("Missing keywords");
        } else {
            for (Keyword k : collectionReferenceInspire.keywords) {
                if (k.value == null || k.value.equals("")) {
                    throw new BadRequestException("Keyword value must not be null nor empty");
                }
            }
        }
        //Check if topic category is set
        if (collectionReferenceInspire.topicCategories == null || collectionReferenceInspire.topicCategories.isEmpty()) {
            throw new BadRequestException("inspire.topic_categories must not be null nor empty");
        }
        // Check Lineage
        if (collectionReferenceInspire.lineage == null || collectionReferenceInspire.lineage.equals("")) {
            throw new BadRequestException("inspire.lineage must not be null or empty");
        }
        // Check limitation access
        if (collectionReferenceInspire.inspireLimitationAccess == null) {
            throw new BadRequestException("inspire.inspire_limitation_access must not be null");
        }
    }

    public static void checkInvalidDublinCoreElementsForInspire(CollectionReference collectionReference) throws ArlasException {
        // check title
        if (collectionReference.params.dublinCoreElementName.title == null || collectionReference.params.dublinCoreElementName.title.equals("")) {
            throw new BadRequestException("dublin_core_element_name.title must not be null nor empty");
        }
        // check description/abstract
        if (collectionReference.params.dublinCoreElementName.description == null || collectionReference.params.dublinCoreElementName.description.equals("")) {
            throw new BadRequestException("dublin_core_element_name.description must not be null nor empty");
        }
        // check language
        if (collectionReference.params.dublinCoreElementName.language == null || collectionReference.params.dublinCoreElementName.language.equals("")) {
            throw new BadRequestException("dublin_core_element_name.language must not be null nor empty");
        } else {
            try {
                InspireSupportedLanguages.valueOf(collectionReference.params.dublinCoreElementName.language);
            } catch (IllegalArgumentException e) {
                String listOfLanguages = "";
                for (InspireSupportedLanguages sl : InspireSupportedLanguages.values()) {
                    listOfLanguages += "'" + sl.name() + "', ";
                }
                throw new InvalidParameterException("'dublin_core_element_name.language : " + collectionReference.params.dublinCoreElementName.language + "' is not a valid language. Metadata languages must be one of the 24 Official languages of the EU in ISO 639-2 (B) : " + listOfLanguages);
            }
        }
    }

    public static void checkInvalidInspireParameters(CollectionReference collectionReference) throws ArlasException {
        Inspire collectionReferenceInspire = collectionReference.params.inspire;

        // check keywords
        if (collectionReferenceInspire != null && collectionReferenceInspire.keywords != null) {
            for (Keyword k : collectionReferenceInspire.keywords) {
                if (k.dateOfPublication != null) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        df.parse(k.dateOfPublication);
                    } catch (ParseException e) {
                        throw new InvalidParameterException("'dateOfPublication' of the keyword '" + k.value + "' must be a date which format is yyyy-MM-dd");
                    }
                }
            }
        }
        //Check if topic category is set
        if (collectionReferenceInspire != null && collectionReferenceInspire.topicCategories != null) {
            for (String topicCategory : collectionReferenceInspire.topicCategories) {
                try {
                    TopicCategory.fromValue(topicCategory);
                } catch (IllegalArgumentException e) {
                    String listOfTopicCategories = "";
                    for (TopicCategory tc : TopicCategory.values()) {
                        listOfTopicCategories += "'" + tc.value() + "', ";
                    }
                    throw new BadRequestException("Invalid topic category " + topicCategory + ". Must be one of : " + listOfTopicCategories);
                }
            }

        }
        // check languages
        if (collectionReferenceInspire != null && collectionReferenceInspire.languages != null) {
            for (String language : collectionReferenceInspire.languages) {
                if (LanguageAlpha3Code.getByCode(language) == null) {
                    throw new InvalidParameterException(language + " is not a valid language. Languages must be one of the languages expressed in ISO 639-2");
                }
            }
        }
        // check spatial resolution
        if (collectionReferenceInspire != null && collectionReferenceInspire.spatialResolution != null) {
            if (collectionReferenceInspire.spatialResolution.value == null) {
                throw new BadRequestException("The spatial resolution value must not be null nor empty");
            }
            if (collectionReferenceInspire.spatialResolution.unitOfMeasure == null) {
                try {
                    Integer.parseInt(collectionReferenceInspire.spatialResolution.value.toString());
                } catch (IllegalArgumentException e) {
                    throw new InvalidParameterException("The equivalent scale must be an Integer. If you meant to specify a resolution distance, then please set the unit of measure.");
                }
            } else {
                try {
                    Double.parseDouble(collectionReferenceInspire.spatialResolution.value.toString());
                } catch (IllegalArgumentException e) {
                    throw new InvalidParameterException("The resolution distance should be a decimal number");
                }
            }
        }

        // Check limitation access
        if (collectionReferenceInspire != null && collectionReferenceInspire.inspireLimitationAccess != null) {
            try {
                AccessConstraintEnum.valueOf(collectionReferenceInspire.inspireLimitationAccess.accessConstraints);
            } catch (IllegalArgumentException e) {
                String listOfAccessConstraintEnum = "";
                for (AccessConstraintEnum ace : AccessConstraintEnum.values()) {
                    listOfAccessConstraintEnum += "'" + ace.name() + "', ";
                }
                throw new InvalidParameterException("accessConstraints is invalid. Please choose one of : " + listOfAccessConstraintEnum);
            }
            try {
                InspireAccessClassificationEnum.valueOf(collectionReferenceInspire.inspireLimitationAccess.classification);
            } catch (IllegalArgumentException e) {
                String listOfClassificationEnum = "";
                for (InspireAccessClassificationEnum iace : InspireAccessClassificationEnum.values()) {
                    listOfClassificationEnum += "'" + iace.name() + "', ";
                }
                throw new InvalidParameterException("Inspire Access Classification is invalid. Please choose one of : " + listOfClassificationEnum);
            }
        }

    }

    public static double[] toDoubles(String doubles) throws InvalidParameterException {
        try {
            return Arrays.stream(doubles.split(",")).mapToDouble(Double::parseDouble).toArray();
        } catch (Exception e) {
            throw new InvalidParameterException(INVALID_BBOX);
        }
    }

    private static void checkPostAnchorValidity(String postAnchor) throws ArlasException {
        // Check if it starts with an operator
        // "/" operator is for rounding the date up or down
        String op = postAnchor.substring(0, 1);
        if (!op.equals("/") && !op.equals("-") && !op.equals("+")) {
            throw new InvalidParameterException(INVALID_DATE_MATH_OPERATOR);
        } else {
            if (op.equals("/")) {
                // If the operator is "/", it should be followed by one character : a date math unit,
                if (postAnchor.length() == 2) {
                    checkDateMathUnit(postAnchor.substring(1, 2));
                } else {
                    throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                }
            } else {
                if (op.equals("+") || op.equals("-")) {
                    // example of postAnchor value : -2h/M
                    if (postAnchor.length() > 1) {
                        String[] operands = postAnchor.substring(1).split("/");
                        //translationDuration == 2
                        String translationDuration = operands[0].substring(0, operands[0].length() - 1);
                        if (ParamsParser.tryParseInteger(translationDuration) == null) {
                            throw new InvalidParameterException(INVALID_DATE_MATH_VALUE);
                        }
                        //translationUnit == h
                        String translationUnit = operands[0].substring(operands[0].length() - 1);
                        checkDateMathUnit(translationUnit);
                        if (operands.length == 2) {
                            // roundingUnit == M
                            String roundingUnit = operands[1];
                            checkDateMathUnit(roundingUnit);
                        } else {
                            if (postAnchor.substring(1).contains("/")) {
                                throw new InvalidParameterException(MISSING_DATE_MATH_UNIT);
                            }
                        }
                    } else {
                        throw new InvalidParameterException(INVALID_DATE_MATH_EXPRESSION);
                    }

                }
            }
        }
    }

    private static void checkDateMathUnit(String unit) throws ArlasException {
        try {
            DateUnitEnum.valueOf(unit);
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException(INVALID_DATE_MATH_UNIT);
        }
    }

    private static boolean isIntegerInXYZRange(int n, int z) {
        long minRange = 0;
        long maxRange = (long) (Math.pow(2, z) - 1);
        return (n >= minRange && n <= maxRange);
    }

    public static void checkReturnedGeometries(CollectionReference collectionReference, String include, String exclude, String returned_geometries) throws ArlasException {
        if (!StringUtil.isNullOrEmpty(exclude)) {
            List<String> fields = new ArrayList<>();
            if (collectionReference.params.idPath != null)
                fields.add(collectionReference.params.idPath);
            if (collectionReference.params.geometryPath != null)
                fields.add(collectionReference.params.geometryPath);
            if (collectionReference.params.centroidPath != null)
                fields.add(collectionReference.params.centroidPath);
            if (collectionReference.params.timestampPath != null)
                fields.add(collectionReference.params.timestampPath);
            List<String> excludeField = Arrays.asList(exclude.split(","));
            CheckParams.checkExcludeField(excludeField, fields);
        }

        if (returned_geometries != null) {
            List<String> excludes = new ArrayList<>();
            if (exclude != null) Collections.addAll(excludes, exclude.split(","));

            for (String geo : returned_geometries.split(",")) {
                CollectionReferenceManager.getInstance().getType(collectionReference, geo, true); // will throw ArlasException if not existing
                if (excludes.contains(geo)) {
                    throw new ArlasException("Returned geometry '" + geo + "' should not be in the exclude list: '" + exclude + "'");
                }
            }
        }
    }
}

