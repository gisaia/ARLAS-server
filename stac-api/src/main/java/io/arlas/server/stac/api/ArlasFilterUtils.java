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

package io.arlas.server.stac.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import org.geojson.Polygon;
import org.geotools.api.filter.*;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.filter.expression.Literal;
import org.geotools.api.filter.expression.PropertyName;
import org.geotools.api.filter.spatial.BinarySpatialOperator;
import org.geotools.api.filter.spatial.Intersects;
import org.geotools.api.filter.spatial.Within;
import org.geotools.filter.IsNotEqualToImpl;
import org.geotools.filter.LikeFilterImpl;
import org.locationtech.jts.geom.Geometry;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.utils.StringUtil;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.GeometryCollection;

public class ArlasFilterUtils {

    private static final List<String> ROOT_STAC_FIELD = List.of("collection", "catalog", "id", "geometry", "bbox", "centroid", "type");
    private static final List<String> ROOT_STAC_KEY = List.of("properties.", "assets.");

    public static List<String> cql2toArlasFilterList(Filter filter, Boolean isStacModel, Set<String> allowedQueryables) throws ArlasException {
        List<String> filters = new ArrayList<>();
        if (filter == null) {
            return filters;
        }
        // AND: flatten children
        if (filter instanceof And) {
            And and = (And) filter;
            for (Filter child : and.getChildren()) {
                filters.addAll(cql2toArlasFilterList(child, isStacModel, allowedQueryables));
            }
            return filters;
        }

        // OR / NOT are not supported in this conversion (would require translation to ARLAS syntax supporting boolean logic)
        if (filter instanceof Or) {
            throw new InvalidParameterException("CQL2 OR filters are not supported by ARLAS STAC filter");
        }
        if (filter instanceof Not not) {
            Filter child = not.getFilter();
            // Support ONLY the GeoTools form generated from "<>":
            // NOT (property = literal)
            if (child instanceof BinaryComparisonOperator cmp && child instanceof PropertyIsEqualTo) {
                filters.add(toBinaryComparisonFilter(cmp, "ne", isStacModel, allowedQueryables));
                return filters;
            }

            throw new InvalidParameterException("CQL2 NOT filters are not supported by ARLAS STAC filter");
        }

        // Comparison operators
        if (filter instanceof BinaryComparisonOperator cmp) {
            if (filter instanceof PropertyIsEqualTo) {
                filters.add(toBinaryComparisonFilter(cmp, "eq", isStacModel, allowedQueryables));
                return filters;
            } else if (filter instanceof PropertyIsGreaterThanOrEqualTo) {
                filters.add(toBinaryComparisonFilter(cmp, "gte", isStacModel, allowedQueryables));
                return filters;
            } else if (filter instanceof PropertyIsLessThanOrEqualTo) {
                filters.add(toBinaryComparisonFilter(cmp, "lte", isStacModel, allowedQueryables));
                return filters;
            } else if (filter instanceof PropertyIsGreaterThan) {
                filters.add(toBinaryComparisonFilter(cmp, "gt", isStacModel, allowedQueryables));
                return filters;
            } else if (filter instanceof PropertyIsLessThan) {
                filters.add(toBinaryComparisonFilter(cmp, "lt", isStacModel, allowedQueryables));
                return filters;
            } else if (filter instanceof IsNotEqualToImpl) {
                filters.add(toBinaryComparisonFilter(cmp, "ne", isStacModel, allowedQueryables));
                return filters;
            }
        }

        if (filter instanceof LikeFilterImpl likeFilter) {
            filters.add(toLikeComparisonFilter(likeFilter, isStacModel, allowedQueryables));
            return filters;
        }

        // BETWEEN -> ARLAS range
        if (filter instanceof PropertyIsBetween between) {
            Expression expr = between.getExpression();
            Expression lower = between.getLowerBoundary();
            Expression upper = between.getUpperBoundary();

            if (!(expr instanceof PropertyName)) {
                throw new InvalidParameterException("Unsupported BETWEEN filter: left-hand expression is not a property");
            }
            String property = normalizeProperty(((PropertyName) expr).getPropertyName(), isStacModel, allowedQueryables);
            String low = literalToString(lower);
            String high = literalToString(upper);
            filters.add(StringUtil.concat(property, ":", "range", ":[", low, "<", high, "]"));
            return filters;
        }

        // Spatial operators: INTERSECTS, WITHIN
        if (filter instanceof Intersects || filter instanceof Within) {
            String opName = filter instanceof Within ? "within" : "intersects";
            Expression e1 = ((BinarySpatialOperator) filter).getExpression1();;
            Expression e2 =  ((BinarySpatialOperator) filter).getExpression2();
            if (e1 == null || e2 == null) {
                // fallback: use filter.toString() as last resort
                filters.add(opName + ":" + filter.toString());
                return filters;
            }
            String property;
            Expression geometryExpr;
            if (e1 instanceof PropertyName p) {
                property = normalizeProperty(p.getPropertyName(), isStacModel, allowedQueryables);
                geometryExpr = e2;
            } else if (e2 instanceof PropertyName p) {
                property = normalizeProperty(p.getPropertyName(), isStacModel, allowedQueryables);
                geometryExpr = e1;
            } else {
                throw new InvalidParameterException("Unsupported spatial filter: no property name found in filter " + filter);
            }
            if (filter instanceof Within) {
                validateWithinGeometry(geometryExpr);
            }
            String geomText = literalToGeometryText(geometryExpr);
            filters.add(StringUtil.concat(property, ":", opName, ":", geomText));
            return filters;
        }

        // If none matched, try to provide a helpful message
        throw new InvalidParameterException("Unsupported CQL2 filter type: " + filter.getClass().getSimpleName() + " (" + filter + ")");
    }


    private static String literalToString(Expression expr) {
        if (expr instanceof Literal literal) {
            Object val = literal.getValue();
            return val == null ? "null" : val.toString();
        } else {
            // fallback to expression string form
            return expr == null ? "null" : expr.toString();
        }
    }

    private static String literalToGeometryText(Expression expr) {
        if (expr instanceof Literal literal) {
            Object val = literal.getValue();
            if (val instanceof Geometry geometry) {
                return geometry.toText();
            } else {
                return val == null ? "null" : val.toString();
            }
        } else {
            return expr == null ? "null" : expr.toString();
        }
    }

    private static String toLikeComparisonFilter(
            LikeFilterImpl cmp,
            Boolean isStacModel, Set<String> allowedQueryables
    ) throws InvalidParameterException {
        Expression e1 = cmp.getExpression();
        String e2 = cmp.getLiteral();
        // Validate LIKE literal: reject unescaped leading wildcard, allow escaped leading wildcard
        validateLikeLiteral(cmp);
        String property;
        String value;
        if (e1 instanceof PropertyName p) {
            property = normalizeProperty(p.getPropertyName(), isStacModel, allowedQueryables);
            value = e2;
        } else {
            throw new InvalidParameterException(
                    "Unsupported comparison: no property name found in filter " + cmp
            );
        }
        return StringUtil.concat(property, ":", "like", ":", value);
    }

    private static String toBinaryComparisonFilter(
            BinaryComparisonOperator cmp,
            String arlasOperator,
            Boolean isStacModel, Set<String> allowedQueryables
    ) throws InvalidParameterException {
        Expression e1 = cmp.getExpression1();
        Expression e2 = cmp.getExpression2();

        String property;
        String value;

        if (e1 instanceof PropertyName p) {
            property = normalizeProperty(p.getPropertyName(), isStacModel, allowedQueryables);
            value = literalToString(e2);
        } else if (e2 instanceof PropertyName p) {
            property = normalizeProperty(p.getPropertyName(), isStacModel, allowedQueryables);
            value = literalToString(e1);
        } else {
            throw new InvalidParameterException(
                    "Unsupported comparison: no property name found in filter " + cmp
            );
        }

        return StringUtil.concat(property, ":", arlasOperator, ":", value);
    }



    private static String normalizeProperty(String property, Boolean isStacModel, Set<String> allowedQueryables) throws InvalidParameterException {
        String normalized = property.replace('/', '.');
        if (Boolean.TRUE.equals(isStacModel)) {
            if(!ROOT_STAC_FIELD.contains(normalized) && ROOT_STAC_KEY.stream().noneMatch(normalized::startsWith)) {
                normalized = "properties." + normalized;
            }
        }
        //Check if property is queryable
        if (!allowedQueryables.contains(normalized)) {
            throw new InvalidParameterException("Unsupported queryable: " + normalized);
        }
        normalized = normalized.replaceFirst(":", "__");
        return normalized;
    }

    /**
     * Validates LIKE literal to reject unescaped leading wildcards.
     * - Exception if pattern contains with an unescaped wildcard (default '%').
     * - Allowed if the leading wildcard is escaped with the escape character.
     */
    private static void validateLikeLiteral(PropertyIsLike like) throws InvalidParameterException {
        String literal = like.getLiteral();
        if (literal == null || literal.isEmpty()) {
            return;
        }
        String wildCard = "%";
        String escape = like.getEscape();
        if (escape != null && !escape.isEmpty() && literal.contains(escape + wildCard)) {
            return;
        }
        if (literal.contains(wildCard)) {
            throw new InvalidParameterException("LIKE filters starting with an unescaped wildcard are not supported");
        }
    }

    /**
     * Validates geometry for CQL2 WITHIN filters.
     * Rejects:
     * - LineString
     * - MultiLineString
     * - GeometryCollection containing at least one LineString or MultiLineString
     */
    private static void validateWithinGeometry(Expression expr) throws InvalidParameterException {
        if (!(expr instanceof Literal literal)) {
            return;
        }
        Object val = literal.getValue();
        if (!(val instanceof Geometry geometry)) {
            return;
        }
        if (geometry instanceof LineString) {
            throw new InvalidParameterException("Unsupported spatial filter: within with LineString geometries");
        }
        if (geometry instanceof MultiLineString) {
            throw new InvalidParameterException("Unsupported spatial filter: within with MultiLineString geometries");
        }
        if (containsLine(geometry)) {
            throw new InvalidParameterException("Unsupported spatial filter: within with GeometryCollection containing LineString or MultiLineString");
        }
    }

    private static boolean containsLine(Geometry geometry) {
        if (geometry instanceof LineString) {
            return true;
        }

        if (geometry instanceof MultiLineString) {
            return true;
        }

        if (geometry instanceof GeometryCollection collection) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                if (containsLine(collection.getGeometryN(i))) {
                    return true;
                }
            }
        }

        return false;
    }
}
