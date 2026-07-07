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

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.server.stac.model.SearchBody;
import org.geotools.api.filter.And;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.Not;
import org.geotools.api.filter.Or;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.filter.expression.Literal;
import org.geotools.api.filter.spatial.Beyond;
import org.geotools.api.filter.spatial.BinarySpatialOperator;
import org.geotools.api.filter.spatial.Contains;
import org.geotools.api.filter.spatial.Crosses;
import org.geotools.api.filter.spatial.Disjoint;
import org.geotools.api.filter.spatial.DWithin;
import org.geotools.api.filter.spatial.Equals;
import org.geotools.api.filter.spatial.Intersects;
import org.geotools.api.filter.spatial.Overlaps;
import org.geotools.api.filter.spatial.Touches;
import org.geotools.api.filter.spatial.Within;
import org.geotools.factory.CommonFactoryFinder;
import org.geootols.filter.text.cql_2.CQL2;
import org.geotools.filter.text.cqljson.CQL2Json;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility methods for parsing CQL2 filters and applying a targeted post-processing step
 * for BBOX-derived polygons.
 *
 * <p>This class keeps GeoTools {@link CQL2#toFilter(String)} as the single parser so
 * complex filters such as nested AND/OR/NOT expressions are still fully supported.
 *
 * <p>The post-processing is intentionally restricted:
 * <ul>
 *   <li>It only runs if the original CQL2 text contains a {@code BBOX(...)} constructor.</li>
 *   <li>It only rewrites literal geometries that are simple axis-aligned rectangles.</li>
 *   <li>It only changes clockwise polygons into counter-clockwise polygons.</li>
 * </ul>
 *
 * <p>This avoids modifying user-provided geometries such as explicit
 * {@code POLYGON(...)} literals.
 */
public final class CQL2FilterUtils {

    private static final FilterFactory FF = CommonFactoryFinder.getFilterFactory();
    private static final Pattern BBOX_PATTERN = Pattern.compile("\\bBBOX\\s*\\(", Pattern.CASE_INSENSITIVE);

    private CQL2FilterUtils() {
    }

    /**
     * Parse the provided CQL2 string with GeoTools and, if the original text contains
     * a {@code BBOX(...)} constructor, normalize BBOX-derived polygon literals to
     * counter-clockwise orientation.
     *
     * @param cql2 the original CQL2 string
     * @param lang the original lang cql-json or cql-text
     * @return the parsed filter, optionally rewritten for BBOX polygon orientation
     * @throws Exception if the CQL2 parser fails
     */
    public static Filter toFilterWithBboxCcwCorrection(String cql2, String lang) throws ArlasException {
        Filter cql2_filter = null;
        if(lang.equals(SearchBody.CQL2_TEXT_STRING)){
            try {
                cql2_filter = CQL2.toFilter(rewriteInToOr(cql2));
            } catch (Exception e) {
                throw new InvalidParameterException("Invalid CQL2-TEXT filter: " + e.getMessage());
            }
        }else if(lang.equals(SearchBody.CQL2_JSON_STRING)){
            try {
                cql2_filter = CQL2Json.toFilter(cql2);
            } catch (Exception e) {
                throw new InvalidParameterException("Invalid CQL2-JSON filter: " + e.getMessage());
            }
        }else{
            throw new InvalidParameterException("Unsupported filter-lang: " + lang);
        }
        if (!containsBboxConstructor(cql2)) {
            return cql2_filter;
        }
        return rewriteFilter(cql2_filter);
    }

    /**
     * Detect whether the original CQL2 text contains a {@code BBOX(...)} constructor.
     *
     * <p>This is used as the primary safety gate before any geometry rewriting happens.
     *
     * @param cql2 the original CQL2 string
     * @return true if the text contains a BBOX constructor
     */
    static boolean containsBboxConstructor(String cql2) {
        return cql2 != null && BBOX_PATTERN.matcher(cql2).find();
    }

    /**
     * Recursively rewrite the filter tree.
     *
     * @param filter the input filter
     * @return the rewritten filter
     */
    private static Filter rewriteFilter(Filter filter) {
        if (filter == null) {
            return null;
        }

        if (filter instanceof And andFilter) {
            List<Filter> children = new ArrayList<>();
            for (Filter child : andFilter.getChildren()) {
                children.add(rewriteFilter(child));
            }
            return FF.and(children);
        }

        if (filter instanceof Or orFilter) {
            List<Filter> children = new ArrayList<>();
            for (Filter child : orFilter.getChildren()) {
                children.add(rewriteFilter(child));
            }
            return FF.or(children);
        }

        if (filter instanceof Not notFilter) {
            return FF.not(rewriteFilter(notFilter.getFilter()));
        }

        if (filter instanceof BinarySpatialOperator spatial) {
            return rewriteSpatialFilter(spatial);
        }

        return filter;
    }

    /**
     * Rewrite a spatial filter if one side contains a literal rectangle polygon
     * that looks like it was produced from a BBOX constructor.
     *
     * @param spatial the spatial filter to inspect
     * @return the same filter or a rewritten one
     */
    private static Filter rewriteSpatialFilter(BinarySpatialOperator spatial) {
        Expression left = spatial.getExpression1();
        Expression right = spatial.getExpression2();

        Expression newLeft = rewriteExpressionIfNeeded(left);
        Expression newRight = rewriteExpressionIfNeeded(right);

        if (newLeft == left && newRight == right) {
            return spatial;
        }

        return rebuildSpatialFilter(spatial, newLeft, newRight);
    }

    /**
     * Rewrite an expression only if it is a literal polygon matching the BBOX rectangle pattern
     * and currently oriented clockwise.
     *
     * @param expression the expression to inspect
     * @return the original expression or a corrected literal
     */
    private static Expression rewriteExpressionIfNeeded(Expression expression) {
        if (!(expression instanceof Literal literal)) {
            return expression;
        }

        Object value = literal.getValue();
        if (!(value instanceof Polygon polygon)) {
            return expression;
        }

        if (!isAxisAlignedRectangle(polygon)) {
            return expression;
        }

        if (isCounterClockwise(polygon)) {
            return expression;
        }

        Polygon corrected = forceCounterClockwise(polygon);
        return FF.literal(corrected);
    }

    /**
     * Rebuild a spatial filter with the same operator type and updated expressions.
     *
     * @param original the original spatial filter
     * @param left the left expression
     * @param right the right expression
     * @return a rebuilt filter of the same type
     */
    private static Filter rebuildSpatialFilter(BinarySpatialOperator original, Expression left, Expression right) {
        if (original instanceof Intersects) {
            return FF.intersects(left, right);
        }
        if (original instanceof Within) {
            return FF.within(left, right);
        }
        if (original instanceof Contains) {
            return FF.contains(left, right);
        }
        if (original instanceof Overlaps) {
            return FF.overlaps(left, right);
        }
        if (original instanceof Crosses) {
            return FF.crosses(left, right);
        }
        if (original instanceof Touches) {
            return FF.touches(left, right);
        }
        if (original instanceof Disjoint) {
            return FF.disjoint(left, right);
        }
        if (original instanceof Equals) {
            return FF.equal(left, right);
        }
        if (original instanceof DWithin dWithin) {
            return FF.dwithin(left, right, dWithin.getDistance(), dWithin.getDistanceUnits());
        }
        if (original instanceof Beyond beyond) {
            return FF.beyond(left, right, beyond.getDistance(), beyond.getDistanceUnits());
        }

        return original;
    }

    /**
     * Detect whether a polygon is a simple axis-aligned rectangle.
     *
     * <p>This is a secondary safety check used to identify the typical geometry
     * generated from a BBOX constructor.
     *
     * @param polygon the polygon to inspect
     * @return true if the polygon is a closed 4-corner axis-aligned rectangle
     */
    static boolean isAxisAlignedRectangle(Polygon polygon) {
        if (polygon == null || polygon.isEmpty() || polygon.getNumInteriorRing() > 0) {
            return false;
        }

        Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();
        if (coordinates.length != 5) {
            return false;
        }

        Coordinate p0 = coordinates[0];
        Coordinate p1 = coordinates[1];
        Coordinate p2 = coordinates[2];
        Coordinate p3 = coordinates[3];
        Coordinate p4 = coordinates[4];

        if (!sameCoordinate(p0, p4)) {
            return false;
        }

        double minX = min(p0.x, p1.x, p2.x, p3.x);
        double maxX = max(p0.x, p1.x, p2.x, p3.x);
        double minY = min(p0.y, p1.y, p2.y, p3.y);
        double maxY = max(p0.y, p1.y, p2.y, p3.y);

        boolean[] matched = new boolean[4];
        for (int i = 0; i < 4; i++) {
            Coordinate c = coordinates[i];
            if (sameCoordinate(c, new Coordinate(minX, minY))) matched[0] = true;
            else if (sameCoordinate(c, new Coordinate(maxX, minY))) matched[1] = true;
            else if (sameCoordinate(c, new Coordinate(maxX, maxY))) matched[2] = true;
            else if (sameCoordinate(c, new Coordinate(minX, maxY))) matched[3] = true;
            else return false;
        }

        return matched[0] && matched[1] && matched[2] && matched[3];
    }

    /**
     * Return true if the polygon exterior ring is counter-clockwise.
     *
     * <p>This implementation delegates orientation detection to JTS.
     *
     * @param polygon the polygon to inspect
     * @return true if the exterior ring is counter-clockwise
     */
    static boolean isCounterClockwise(Polygon polygon) {
        Coordinate[] coordinates = polygon.getExteriorRing().getCoordinates();
        return Orientation.isCCW(coordinates);
    }

    /**
     * Return a copy of the polygon whose exterior ring is counter-clockwise.
     *
     * <p>Interior rings are preserved as-is because the target use case is a simple
     * BBOX-derived rectangle with no holes.
     *
     * @param polygon the polygon to normalize
     * @return a polygon with counter-clockwise exterior ring
     */
    static Polygon forceCounterClockwise(Polygon polygon) {
        LinearRing shell = (LinearRing) polygon.getExteriorRing();
        Coordinate[] shellCoordinates = shell.getCoordinates();

        Coordinate[] reversed = new Coordinate[shellCoordinates.length];
        for (int i = 0; i < shellCoordinates.length; i++) {
            reversed[i] = shellCoordinates[shellCoordinates.length - 1 - i];
        }

        LinearRing correctedShell = polygon.getFactory().createLinearRing(reversed);
        return polygon.getFactory().createPolygon(correctedShell, null);
    }

    private static boolean sameCoordinate(Coordinate a, Coordinate b) {
        return nearlyEqual(a.x, b.x) && nearlyEqual(a.y, b.y);
    }

    private static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) < 1e-12;
    }

    private static double min(double... values) {
        double result = values[0];
        for (double value : values) {
            result = Math.min(result, value);
        }
        return result;
    }

    private static double max(double... values) {
        double result = values[0];
        for (double value : values) {
            result = Math.max(result, value);
        }
        return result;
    }

    //Geotools CQL2 parser does not support IN operator, so we rewrite it to OR operator before parsing
    private static String rewriteInToOr(String cql2Input) {
        // Captures the attribute, detects the case-insensitive "IN", and captures the list within parentheses
        Pattern pattern = Pattern.compile("([\\w\\.]+)\\s+(?i)IN\\s*\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(cql2Input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String attribute = matcher.group(1);
            String[] values = matcher.group(2).split(",\\s*");
            // Transform to (attr = 'val1' OR attr = 'val2')
            String orClause = Arrays.stream(values)
                    .map(val -> attribute + " = " + val.trim())
                    .collect(Collectors.joining(" OR ", "(", ")"));

            matcher.appendReplacement(sb, Matcher.quoteReplacement(orClause));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}