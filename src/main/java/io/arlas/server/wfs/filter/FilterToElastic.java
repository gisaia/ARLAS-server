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

package io.arlas.server.wfs.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import io.arlas.server.exceptions.WFSException;
import io.arlas.server.exceptions.WFSExceptionCode;
import io.arlas.server.exceptions.WFSExceptionMessage;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.wfs.utils.WFSCheckParam;
import io.arlas.server.wfs.utils.XmlUtils;
import org.elasticsearch.common.joda.Joda;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.Capabilities;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.util.ConverterFactory;
import org.geotools.util.Converters;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.*;
import org.opengis.filter.expression.*;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.*;
import org.opengis.filter.temporal.*;
import org.opengis.temporal.Period;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * Encodes an OGC Filter and creates a filter for an Elasticsearch query.
 * Based on org.geotools.data.jdbc.FilterToSQL in the GeoTools library/jdbc module.
 */
public class FilterToElastic implements FilterVisitor, ExpressionVisitor {

    /** Standard java logger */
    org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FilterToElastic.class);

    /** filter factory */
    private static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final ObjectReader mapReader = mapper.readerWithView(Map.class).forType(HashMap.class);

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = Joda.forPattern("date_optional_time").printer();

    /** The filter types that this class can encode */
    private Capabilities capabilities = null;

    /** the schmema the encoder will use */
    SimpleFeatureType featureType;

    Geometry currentGeometry;

    Object field;

    Map<String,Object> currentShapeBuilder;

    Boolean fullySupported;

    Map<String,Object> queryBuilder;

    Map<String,Object> nativeQueryBuilder;

    Map<String,Map<String,Map<String,Object>>> aggregations;

    public WFSException wfsException;

    private FilterToElasticHelper helper;

    private String key;

    private Object lower;

    private Object upper;

    private Boolean nested;

    private String path;

    private String pattern;

    private Boolean analyzed;

    private String type;

    private List<String> ids;

    private Period period;

    private String op;

    private Object begin;

    private Object end;

    private Map<String,String> parameters;

    private Boolean nativeOnly;

    private DateTimeFormatter dateFormatter;

    public static final String NESTED = "nested";
    public static final String ANALYZED = "analyzed";
    public static final String DATE_FORMAT = "date_format";



    public CollectionReferenceDescription collectionReference;

    public FilterToElastic( CollectionReferenceDescription collectionReference) {
        queryBuilder = FilterToElasticHelper.MATCH_ALL;
        nativeQueryBuilder = ImmutableMap.of("match_all", Collections.EMPTY_MAP);
        this.collectionReference = collectionReference;
        helper = new FilterToElasticHelper(this);

    }

    /**
     * Performs the encoding.
     *
     * @param filter the Filter to be encoded.
     *
     */
    public void encode(Filter filter) {
        fullySupported = getCapabilities().fullySupports(filter);
        filter.accept(this, null);

    }



    /**
     * Sets the featuretype the encoder is encoding for.
     * <p>
     * This is used for context for attribute expressions. 
     * </p>
     * 
     * @param featureType
     */
    public void setFeatureType(SimpleFeatureType featureType) {
        this.featureType = featureType;
    }

    /**
     * Sets the capabilities of this filter.
     *
     * @return Capabilities for this Filter
     */
    protected Capabilities createCapabilities() {
        return new ElasticCapabilities();
    }

    /**
     * Describes the capabilities of this encoder.
     * 
     * <p>
     * Performs lazy creation of capabilities.
     * </p>
     * 
     * If you're extending this class, override {@link #createCapabilities()} to declare which capabilities you 
     * support.  Don't use this method.
     *
     * @return The capabilities supported by this encoder.
     */
    public synchronized final Capabilities getCapabilities() {
        if (capabilities == null) {
            capabilities = createCapabilities();
        }

        return capabilities; //maybe clone?  Make immutable somehow
    }


    // BEGIN IMPLEMENTING org.opengis.filter.FilterVisitor METHODS

    /**
     * Writes the FilterBuilder for the ExcludeFilter.
     * 
     * @param filter the filter to be visited
     */
    public Object visit(ExcludeFilter filter, Object extraData) {
        queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", FilterToElasticHelper.MATCH_ALL));
        return extraData;
    }

    /**
     * Writes the FilterBuilder for the IncludeFilter.
     * 
     * @param filter the filter to be visited
     *  
     */
    public Object visit(IncludeFilter filter, Object extraData) {
        queryBuilder = FilterToElasticHelper.MATCH_ALL;
        return extraData;
    }

    /**
     * Writes the FilterBuilder for the PropertyIsBetween Filter.
     *
     * @param filter the Filter to be visited.
     *
     */
    public Object visit(PropertyIsBetween filter, Object extraData) {
        LOGGER.debug("exporting PropertyIsBetween");

        Expression expr = filter.getExpression();
        Expression lowerbounds =  filter.getLowerBoundary();
        Expression upperbounds =  filter.getUpperBoundary();

        Class<?> context;
        nested = false;
        AttributeDescriptor attType = (AttributeDescriptor)expr.evaluate(featureType);
        if (attType != null) {
            context = attType.getType().getBinding();
            if (attType.getUserData().containsKey(NESTED)) {
                nested = (Boolean) attType.getUserData().get(NESTED);
            }
            if (Date.class.isAssignableFrom(context)) {
                updateDateFormatter(attType);
            }
        } else {
            //assume it's a string?
            context = String.class;
        }

        expr.accept( this, extraData);
        key = (String) field;
        lowerbounds.accept(this, context);
        lower = field;
        upperbounds.accept( this, context);
        upper = field;
        if(nested) {
            path = extractNestedPath(key);
        }

        queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("gte", lower, "lte", upper)));
        if(nested) {
            queryBuilder = ImmutableMap.of("nested", ImmutableMap.of("path", path, "query", queryBuilder));
        }

        return extraData;
    }


    /**
     * Writes the FilterBuilder for the Like Filter.
     *
     * @param filter the filter to be visited
     *
     */
    public Object visit(PropertyIsLike filter, Object extraData) {
        char esc = filter.getEscape().charAt(0);
        char multi = filter.getWildCard().charAt(0);
        char single = filter.getSingleChar().charAt(0);
        boolean matchCase = false;
        if (filter.isMatchingCase()) {
            LOGGER.debug("Case sensitive search not supported");
        }

        String literal = filter.getLiteral();
        Expression att = filter.getExpression();

        AttributeDescriptor attType = (AttributeDescriptor) att.evaluate(featureType);
        analyzed = false;
        nested = false;
        if (attType != null) {
            if (attType.getUserData().containsKey(ANALYZED)) {
                analyzed = (Boolean) attType.getUserData().get(ANALYZED);
            }
            if (attType.getUserData().containsKey(NESTED)) {
                nested = (Boolean) attType.getUserData().get(NESTED);
            }
            if (Date.class.isAssignableFrom(attType.getType().getBinding())) {
                updateDateFormatter(attType);
            }
        }

        att.accept(this, extraData);
        key = (String) XmlUtils.retrievePointPath((String) field);
        if(!WFSCheckParam.isFieldInMapping(collectionReference,key)){
            List<WFSExceptionMessage> wfsExceptionMessages = new ArrayList<>();
            wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter"));
            wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE,"Unable to find "+field+"  in "+collectionReference.collectionName+".","filter"));
            wfsException = new WFSException(wfsExceptionMessages);
            throw new RuntimeException();
        }

        if (analyzed) {
            // use query string query post filter for analyzed fields
            pattern = convertToQueryString(esc, multi, single, matchCase, literal);
        } else {
            // default to regexp filter
            pattern = convertToRegex(esc, multi, single, matchCase, literal);
        }
        if (nested) {
            path = extractNestedPath(key);
        }

        if (analyzed) {
            // use query string query for analyzed fields
            queryBuilder = ImmutableMap.of("query_string", ImmutableMap.of("query", pattern, "default_field", key));
        } else {
            // default to regexp query
            queryBuilder = ImmutableMap.of("regexp", ImmutableMap.of(key, pattern));
        }
        if (nested) {
            queryBuilder = ImmutableMap.of("nested", ImmutableMap.of("path", path, "query", queryBuilder));
        }

        return extraData;
    }

    /**
     * Write the FilterBuilder for an And filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(And filter, Object extraData) {
        return visit((BinaryLogicOperator)filter, "AND");
    }

    /**
     * Write the FilterBuilder for a Not filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(Not filter, Object extraData) {
        if(filter.getFilter() instanceof PropertyIsNull) {
            Expression expr = ((PropertyIsNull) filter.getFilter()).getExpression();
            expr.accept( this, extraData);
        } else {
            filter.getFilter().accept(this, extraData);
        }

        if(filter.getFilter() instanceof PropertyIsNull) {
            queryBuilder = ImmutableMap.of("exists", ImmutableMap.of("field", field));
        } else {
            queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", queryBuilder));
        }
        return extraData;
    }

    /**
     * Write the FilterBuilder for an Or filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(Or filter, Object extraData) {
        return visit((BinaryLogicOperator)filter, "OR");
    }

    /**
     * Common implementation for BinaryLogicOperator filters.  This way
     * they're all handled centrally.
     *
     * @param filter the logic statement.
     * @param extraData extra filter data.  Not modified directly by this method.
     */
    protected Object visit(BinaryLogicOperator filter, Object extraData) {
        LOGGER.debug("exporting LogicFilter");

        final List<Map<String,Object>> filters = new ArrayList<>();
        for (final Filter child : filter.getChildren()) {
            child.accept(this, extraData);
            filters.add(queryBuilder);
        }
        if (extraData.equals("AND")) {
            queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must", filters));
        } else if (extraData.equals("OR")) {
            queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("should", filters));
        }
        return extraData;
    }



    /**
     * Write the FilterBuilder for this kind of filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        visitBinaryComparisonOperator((BinaryComparisonOperator)filter, "=");
        return extraData;
    }

    /**
     * Write the FilterBuilder for this kind of filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        visitBinaryComparisonOperator((BinaryComparisonOperator)filter, ">=");
        return extraData;
    }

    /**
     * Write the FilterBuilder for this kind of filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        visitBinaryComparisonOperator((BinaryComparisonOperator)filter, ">");
        return extraData;
    }

    /**
     * Write the FilterBuilder for this kind of filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(PropertyIsLessThan filter, Object extraData) {
        visitBinaryComparisonOperator((BinaryComparisonOperator)filter, "<");
        return extraData;
    }

    /**
     * Write the FilterBuilder for this kind of filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        visitBinaryComparisonOperator((BinaryComparisonOperator)filter, "<=");
        return extraData;
    }

    /**
     * Write the FilterBuilder for this kind of filter
     * 
     * @param filter the filter to visit
     * @param extraData extra data (unused by this method)
     * 
     */
    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        visitBinaryComparisonOperator((BinaryComparisonOperator)filter, "!=");
        return extraData;
    }

    /**
     * Common implementation for BinaryComparisonOperator filters.
     *
     * @param filter the comparison.
     *
     */
    protected void visitBinaryComparisonOperator(BinaryComparisonOperator filter, Object extraData)  {
        LOGGER.debug("exporting FilterBuilder ComparisonFilter");

        Expression left = filter.getExpression1();
        Expression right = filter.getExpression2();
        if (isBinaryExpression(left) || isBinaryExpression(right)) {
            throw new UnsupportedOperationException("Binary expressions not supported");
        }

        AttributeDescriptor attType = null;
        Class<?> leftContext = null, rightContext = null;
        if (left instanceof PropertyName) {
            // It's a propertyname, we should get the class and pass it in
            // as context to the tree walker.
            attType = (AttributeDescriptor)left.evaluate(featureType);
            if (attType != null) {
                rightContext = attType.getType().getBinding();
            }
        }

        if (right instanceof PropertyName) {
            attType = (AttributeDescriptor)right.evaluate(featureType);
            if (attType != null) {
                leftContext = attType.getType().getBinding();
            }
        }

        nested = false;
        if (attType != null) {
            if (attType.getUserData().containsKey(NESTED)) {
                nested = (Boolean) attType.getUserData().get(NESTED);
            }
            if (Date.class.isAssignableFrom(attType.getType().getBinding())) {
                updateDateFormatter(attType);
            }
        }

        //case sensitivity
        if ( !filter.isMatchingCase() ) {
            //we only do for = and !=
            if ( filter instanceof PropertyIsEqualTo || 
                    filter instanceof PropertyIsNotEqualTo ) {
                //and only for strings
                if ( String.class.equals( leftContext ) 
                        || String.class.equals( rightContext ) ) {
                    //matchCase = false;
                    LOGGER.debug("Case insensitive filter not supported");
                }
            }
        }

        type = (String) extraData;

        if (left instanceof PropertyName) {
            left.accept(this, null);
            key = (String) field;
            right.accept(this, rightContext);

        } else {
            right.accept( this, null);
            key = (String) XmlUtils.retrievePointPath((String) field);
            if(!WFSCheckParam.isFieldInMapping(collectionReference,key)){
                List<WFSExceptionMessage> wfsExceptionMessages = new ArrayList<>();
                wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter"));
                wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE,"Unable to find "+field+"  in "+collectionReference.collectionName+".","filter"));
                wfsException = new WFSException(wfsExceptionMessages);
                throw new RuntimeException();
            }            left.accept(this, leftContext);
        }
        if (nested) {
            path = extractNestedPath(key);
        }

        if(field.equals("")){
            wfsException = new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter");
            throw new RuntimeException();
        }

        if (type.equals("=")) {
            queryBuilder = ImmutableMap.of("term", ImmutableMap.of(key, field));
        } else if (type.equals("!=")) {
            queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", ImmutableMap.of("term", ImmutableMap.of(key, field))));
        } else if (type.equals(">")) {
            queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("gt", field)));
        } else if (type.equals(">=")) {
            queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("gte", field)));
        } else if (type.equals("<")) {
            queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("lt", field)));
        } else if (type.equals("<=")) {
            queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("lte", field)));
        }

        if (nested) {
            queryBuilder = ImmutableMap.of("nested", ImmutableMap.of("path", path, "query", queryBuilder));
        }
    }

    /*
     * determines if the function is a binary expression
     */
    boolean isBinaryExpression(Expression e) {
        return e instanceof BinaryExpression;
    }

    /**
     * Writes the FilterBuilder for the Null Filter.
     *
     * @param filter the null filter.
     *
     */
    public Object visit(PropertyIsNull filter, Object extraData) {
        LOGGER.debug("exporting NullFilter");

        Expression expr = filter.getExpression();

        expr.accept((org.opengis.filter.expression.ExpressionVisitor) this, extraData);

        queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", ImmutableMap.of("exists", ImmutableMap.of("field", field))));

        return extraData;
    }

    public Object visit(PropertyIsNil filter, Object extraData) {
        throw new UnsupportedOperationException("isNil not supported");
    }

    /**
     * Encodes an Id filter
     *
     * @param filter the
     *
     */
    public Object visit(Id filter, Object extraData) {
        final List<String> idList = new ArrayList<>();
        for (final Identifier id : filter.getIdentifiers()) {
            idList.add(id.toString());
        }
        ids = idList;

        queryBuilder = ImmutableMap.of("ids", ImmutableMap.of("values", ids));

        return extraData;
    }

    public Object visit(BBOX filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Beyond filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Contains filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Crosses filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Disjoint filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(DWithin filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Equals filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Intersects filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Overlaps filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Touches filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }
    public Object visit(Within filter, Object extraData) {
        return visitBinarySpatialOperator((BinarySpatialOperator)filter, extraData);
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            Object extraData) {
        // basic checks
        if (filter == null)
            throw new NullPointerException(
                    "Filter to be encoded cannot be null");

        // extract the property name and the geometry literal
        BinarySpatialOperator op = (BinarySpatialOperator) filter;
        Expression e1 = op.getExpression1();
        Expression e2 = op.getExpression2();

        if (e1 instanceof Literal && e2 instanceof PropertyName) {
            e1 = (PropertyName) op.getExpression2();
            e2 = (Literal) op.getExpression1();
        }

        if (e1 instanceof PropertyName && e2 instanceof Literal) {
            //call the "regular" method
            return visitBinarySpatialOperator(filter, (PropertyName)e1, (Literal)e2, filter
                    .getExpression1() instanceof Literal, extraData);
        }
        else {
            //call the join version
            return visitBinarySpatialOperator(filter, e1, e2, extraData);
        }

    }

    protected Object visitBinaryTemporalOperator(BinaryTemporalOperator filter,
            Object extraData) {
        if (filter == null) {
            throw new NullPointerException("Null filter");
        }

        Expression e1 = filter.getExpression1();
        Expression e2 = filter.getExpression2();

        if (e1 instanceof Literal && e2 instanceof PropertyName) {
            e1 = (PropertyName) filter.getExpression2();
            e2 = (Literal) filter.getExpression1();
        }

        if (e1 instanceof PropertyName && e2 instanceof Literal) {
            //call the "regular" method
            return visitBinaryTemporalOperator(filter, (PropertyName)e1, (Literal)e2, 
                    filter.getExpression1() instanceof Literal, extraData);
        }
        else {
            //call the join version
            return visitBinaryTemporalOperator(filter, e1, e2, extraData);
        }
    }

    /**
     * Handles the common case of a PropertyName,Literal geometry binary temporal operator.
     * <p>
     * Subclasses should override if they support more temporal operators than what is handled in 
     * this base class. 
     * </p>
     */
    protected Object visitBinaryTemporalOperator(BinaryTemporalOperator filter, 
            PropertyName property, Literal temporal, boolean swapped, Object extraData) { 

        AttributeDescriptor attType = (AttributeDescriptor)property.evaluate(featureType);

        Class<?> typeContext = null;
        nested = false;
        if (attType != null) {
            typeContext = attType.getType().getBinding();
            if (attType.getUserData().containsKey(NESTED)) {
                nested = (Boolean) attType.getUserData().get(NESTED);
            }
            updateDateFormatter(attType);
        }

        //check for time period
        period = null;
        if (temporal.evaluate(null) instanceof Period) {
            period = (Period) temporal.evaluate(null);
        }

        //verify that those filters that require a time period have one
        if ((filter instanceof Begins || filter instanceof BegunBy || filter instanceof Ends ||
                filter instanceof EndedBy || filter instanceof During || filter instanceof TContains) &&
                period == null) {
            if (period == null) {
                throw new IllegalArgumentException("Filter requires a time period");
            }
        }
        if (filter instanceof TEquals && period != null) {
            throw new IllegalArgumentException("TEquals filter does not accept time period");
        }

        //ensure the time period is the correct argument
        if ((filter instanceof Begins || filter instanceof Ends || filter instanceof During) && 
                swapped) {
            throw new IllegalArgumentException("Time period must be second argument of Filter");
        }
        if ((filter instanceof BegunBy || filter instanceof EndedBy || filter instanceof TContains) && 
                !swapped) {
            throw new IllegalArgumentException("Time period must be first argument of Filter");
        }

        key = "";
        if (filter instanceof After || filter instanceof Before) {
            op = filter instanceof After ? " > " : " < ";

            if (period != null) {
                property.accept( this, extraData);
                key = (String) field;

                visitBegin(period, extraData);
                begin = field;
                visitEnd(period, extraData);
                end = field;
            }
            else {
                property.accept( this, extraData);
                key = (String) field;
                temporal.accept( this, typeContext);
            }
        }
        else if (filter instanceof Begins || filter instanceof Ends || 
                filter instanceof BegunBy || filter instanceof EndedBy ) {
            property.accept( this, extraData);
            key = (String) field;

            if (filter instanceof Begins || filter instanceof BegunBy) {
                visitBegin(period, extraData);
            }
            else {
                visitEnd(period, extraData);
            }
        }
        else if (filter instanceof During || filter instanceof TContains){
            property.accept( this, extraData);
            key = (String) field;

            visitBegin(period, extraData);
            lower = field;
            visitEnd(period, extraData);
        }
        else if (filter instanceof TEquals) {
            property.accept( this, extraData);
            key = (String) field;
            temporal.accept( this, typeContext);
        }
        if (nested) {
            path = extractNestedPath(key);
        }

        if (filter instanceof After || filter instanceof Before) {
            if (period != null) {
                if ((op.equals(" > ") && !swapped) || (op.equals(" < ") && swapped)) {
                    queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("gt", end)));
                } else {
                    queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("lt", begin)));
                }
            }
            else {
                if (op.equals(" < ") || swapped) {
                    queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("lt", field)));
                } else {
                    queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("gt", field)));
                }
            }
        }
        else if (filter instanceof Begins || filter instanceof Ends || 
                filter instanceof BegunBy || filter instanceof EndedBy ) {

            queryBuilder = ImmutableMap.of("term", ImmutableMap.of(key, field));
        }
        else if (filter instanceof During || filter instanceof TContains){
            queryBuilder = ImmutableMap.of("range", ImmutableMap.of(key, ImmutableMap.of("gt", lower, "lt", field)));
        }
        else if (filter instanceof TEquals) {
            queryBuilder = ImmutableMap.of("term", ImmutableMap.of(key, field));
        }

        if (nested) {
            queryBuilder = ImmutableMap.of("nested", ImmutableMap.of("path", path, "query", queryBuilder));
        }

        return extraData;
    }

    void visitBegin(Period p, Object extraData) {
        filterFactory.literal(p.getBeginning().getPosition().getDate()).accept((org.opengis.filter.expression.ExpressionVisitor) this, extraData);
    }

    void visitEnd(Period p, Object extraData) {
        filterFactory.literal(p.getEnding().getPosition().getDate()).accept((org.opengis.filter.expression.ExpressionVisitor) this, extraData);
    }

    /**
     * Handles the general case of two expressions in a binary temporal filter.
     * <p>
     * Subclasses should override if they support more temporal operators than what is handled in 
     * this base class. 
     * </p>
     */
    protected Object visitBinaryTemporalOperator(BinaryTemporalOperator filter, Expression e1, 
            Expression e2, Object extraData) {
        throw new UnsupportedOperationException("Join version of binary temporal operator not supported");
    }

    /**
     * Encodes a null filter value.  The current implementation
     * does exactly nothing.
     * @param extraData extra data to be used to evaluate the filter
     * @return the untouched extraData parameter
     */
    public Object visitNullFilter(Object extraData) {
        return extraData;
    }

    // END IMPLEMENTING org.opengis.filter.FilterVisitor METHODS


    // START IMPLEMENTING org.opengis.filter.ExpressionVisitor METHODS

    /**
     * Writes the FilterBuilder for the attribute Expression.
     * 
     * @param expression the attribute.
     *
     */
    @Override
    public Object visit(PropertyName expression, Object extraData) {
        LOGGER.debug("exporting PropertyName");

        SimpleFeatureType featureType = this.featureType;

        Class<?> target = null;
        if(extraData instanceof Class) {
            target = (Class<?>) extraData;
        }

        //first evaluate expression against feature type get the attribute, 
        AttributeDescriptor attType = (AttributeDescriptor) expression.evaluate(featureType);

        String encodedField; 
        if ( attType != null ) {
            encodedField = attType.getLocalName();
            if(target != null && target.isAssignableFrom(attType.getType().getBinding())) {
                // no need for casting, it's already the right type
                target = null;
            }
        } else {
            // fall back to just encoding the property name
            encodedField = expression.getPropertyName();
        }

        if (target != null) {
            LOGGER.debug("PropertyName type casting not implemented");
        }
        field = encodedField;

        return extraData;
    }

    /**
     * Export the contents of a Literal Expresion
     *
     * @param expression
     * the Literal to export
     *
     */
    @Override
    public Object visit(Literal expression, Object context) {
        // type to convert the literal to
        Class<?> target = null;
        if ( context instanceof Class ) {
            target = (Class<?>) context;
        }
            //evaluate the expression
            Object literal = evaluateLiteral( expression, target );
            // handle geometry case
            if (literal instanceof Geometry) {
                // call this method for backwards compatibility with subclasses
                try {
                    visitLiteralGeometry(filterFactory.literal(literal));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                // write out the literal allowing subclasses to override this
                // behaviour (for writing out dates and the like using the BDMS custom functions)
                writeLiteral(literal);
            }
        return context;
    }

    protected Object evaluateLiteral(Literal expression, Class<?> target )  {
        Object literal = null;

        // HACK: let expression figure out the right value for numbers,
        // since the context is almost always improperly set and the
        // numeric converters try to force floating points to integrals
        // JD: the above is no longer true, so instead do a safe conversion
        if(target != null) {
            // use the target type
            if (Number.class.isAssignableFrom(target)) {
                literal = safeConvertToNumber(expression, target);
                if (literal == null) {
                    literal = safeConvertToNumber(expression, Number.class);
                }
            }
            else {
                literal = expression.evaluate(null, target);
            }
        }

        //check for conversion to number
        if (target == null) {
            // we don't know the target type, check for a conversion to a number
            Number number = safeConvertToNumber(expression, Number.class);
            if (number != null && !String.valueOf(number).equals("") ) {
                literal = number;
            }
        }

        // if the target was not known, of the conversion failed, try the
        // type guessing dance literal expression does only for the following
        // method call
        if(literal == null)
            literal = expression.evaluate(null);

        // if that failed as well, grab the value as is
        if(literal == null)
            literal = expression.getValue();
        return literal;
    }

    /**
     * Writes out a non null, non geometry literal. The base class properly handles
     * null, numeric and booleans (true|false), and turns everything else into a string.
     * Subclasses are expected to override this shall they need a different treatment
     * (e.g. for dates)
     * @param literal
     */
    protected void writeLiteral(Object literal) {
        field = literal;

        if (Date.class.isAssignableFrom(literal.getClass())) {
            field = dateFormatter.print(((Date) literal).getTime());
        }
    } 

    protected void visitLiteralTimePeriod(Period expression) {
        throw new UnsupportedOperationException("Time periods not supported, subclasses must implement this " +
                "method to support encoding timeperiods");
    }

    public Object visit(Add expression, Object extraData) {
        throw new UnsupportedOperationException("Add expressions not supported");
    }
    public Object visit(Divide expression, Object extraData) {
        throw new UnsupportedOperationException("Divide expressions not supported");
    }
    public Object visit(Multiply expression, Object extraData) {
        throw new UnsupportedOperationException("Multiply expressions not supported");
    }
    public Object visit(Subtract expression, Object extraData) {
        throw new UnsupportedOperationException("Subtract expressions not supported");
    }

    public Object visit(NilExpression expression, Object extraData) {
        field = null;
        return extraData;
    }

    //temporal filters, not supported
    public Object visit(After after, Object extraData) {
        return visitBinaryTemporalOperator(after, extraData);
    }
    public Object visit(AnyInteracts anyInteracts, Object extraData) {
        return visitBinaryTemporalOperator(anyInteracts, extraData);
    }
    public Object visit(Before before, Object extraData) {
        return visitBinaryTemporalOperator(before, extraData);
    }
    public Object visit(Begins begins, Object extraData) {
        return visitBinaryTemporalOperator(begins, extraData);
    }
    public Object visit(BegunBy begunBy, Object extraData) {
        return visitBinaryTemporalOperator(begunBy, extraData);
    }
    public Object visit(During during, Object extraData) {
        return visitBinaryTemporalOperator(during, extraData);
    }
    public Object visit(EndedBy endedBy, Object extraData) {
        return visitBinaryTemporalOperator(endedBy, extraData);
    }
    public Object visit(Ends ends, Object extraData) {
        return visitBinaryTemporalOperator(ends, extraData);
    }
    public Object visit(Meets meets, Object extraData) {
        return visitBinaryTemporalOperator(meets, extraData);
    }
    public Object visit(MetBy metBy, Object extraData) {
        return visitBinaryTemporalOperator(metBy, extraData);
    }
    public Object visit(OverlappedBy overlappedBy, Object extraData) {
        return visitBinaryTemporalOperator(overlappedBy, extraData);
    }
    public Object visit(TContains contains, Object extraData) {
        return visitBinaryTemporalOperator(contains, extraData);
    }
    public Object visit(TEquals equals, Object extraData) {
        return visitBinaryTemporalOperator(equals, extraData);
    }
    public Object visit(TOverlaps contains, Object extraData) {
        return visitBinaryTemporalOperator(contains, extraData);
    }

    protected void visitLiteralGeometry(Literal expression) throws IOException {
        // evaluate the literal and store it for later
        currentGeometry  = (Geometry) evaluateLiteral(expression, Geometry.class);

        if ( currentGeometry instanceof LinearRing ) {
            // convert LinearRing to LineString
            final GeometryFactory factory = currentGeometry.getFactory();
            final LinearRing linearRing = (LinearRing) currentGeometry;
            final CoordinateSequence coordinates;
            coordinates = linearRing.getCoordinateSequence();
            currentGeometry = factory.createLineString(coordinates);
        }

        String geoJson = new GeometryJSON().toString(currentGeometry);

        currentShapeBuilder = mapReader.readValue(geoJson);
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) {
        return helper.visitBinarySpatialOperator(filter, property, geometry,
                swapped, extraData);
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter, Expression e1, 
            Expression e2, Object extraData) {
        return helper.visitBinarySpatialOperator(filter, e1, e2, extraData);
    }

    @Override
    public Object visit(Function function, Object extraData) {
        throw new UnsupportedOperationException("Function support not implemented");
    }

    // END IMPLEMENTING org.opengis.filter.ExpressionVisitor METHODS

    protected void updateDateFormatter(AttributeDescriptor attType) {
        dateFormatter = DEFAULT_DATE_FORMATTER;
        if (attType != null) {
            final String format = (String) attType.getUserData().get(DATE_FORMAT);
            if (format != null) {
                dateFormatter = Joda.forPattern(format).printer();
            }
        }
    }

    /*
     * helper to do a safe convesion of expression to a number
     */
    Number safeConvertToNumber(Expression expression, Class<?> target) {
        return (Number) Converters.convert(expression.evaluate(null), target, 
                new Hints(ConverterFactory.SAFE_CONVERSION, true));
    }

    public static String convertToQueryString(char escape, char multi, char single, 
            boolean matchCase, String pattern ) {

        StringBuffer result = new StringBuffer(pattern.length()+5);
        for (int i = 0; i < pattern.length(); i++) {
            char chr = pattern.charAt(i);
            if (chr == escape) {
                // emit the next char and skip it
                if (i!= (pattern.length()-1) ) {
                    result.append("\\");
                    result.append( pattern.charAt(i+1) );
                }
                i++; // skip next char
            } else if (chr == single) {
                result.append('?');
            } else if (chr == multi) {
                result.append('*');
            } else { 
                result.append(chr);
            }
        }

        return result.toString();
    }

    public static String convertToRegex(char escape, char multi, char single, 
            boolean matchCase, String pattern) {

        StringBuffer result = new StringBuffer(pattern.length()+5);
        for (int i = 0; i < pattern.length(); i++) {
            char chr = pattern.charAt(i);
            if (chr == escape) {
                // emit the next char and skip it
                if (i!= (pattern.length()-1) ) {
                    result.append("\\");
                    result.append( pattern.charAt(i+1) );
                }
                i++; // skip next char
            } else if (chr == single) {
                result.append('.');
            } else if (chr == multi) {
                result.append(".*");
            } else { 
                result.append(chr);
            }
        }

        return result.toString();
    }

    private static String extractNestedPath(String field) {
        final String[] parts = field.split("\\.");
        final String base = parts[parts.length-1];
        return field.replace("." + base, "");
    }

    public Boolean getFullySupported() {
        return fullySupported;
    }

    public Map<String,Object> getNativeQueryBuilder() {
        return nativeQueryBuilder;
    }

    public Map<String,Object> getQueryBuilder() {
        final Map<String,Object> queryBuilder;
        if (nativeQueryBuilder.equals(FilterToElasticHelper.MATCH_ALL)) {
            queryBuilder = this.queryBuilder;
        } else if (this.queryBuilder.equals(FilterToElasticHelper.MATCH_ALL)) {
            queryBuilder = nativeQueryBuilder;
        } else {
            queryBuilder = ImmutableMap.of("bool", 
                    ImmutableMap.of("must", ImmutableList.of(nativeQueryBuilder, this.queryBuilder)));
        }
        return queryBuilder;
    }

    public Map<String,Map<String,Map<String,Object>>> getAggregations() {
        return aggregations;
    }


}