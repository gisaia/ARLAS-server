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

package io.arlas.server.ogc.wfs.filter;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.*;
import io.arlas.server.exceptions.OGCException;
import io.arlas.server.exceptions.OGCExceptionCode;
import io.arlas.server.exceptions.OGCExceptionMessage;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.wfs.utils.XmlUtils;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class FilterToElasticHelper {
    org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FilterToElasticHelper.class);

    public static final Map<String, Object> MATCH_ALL = ImmutableMap.of("match_all", Collections.EMPTY_MAP);


    private String key;

    private Literal geometry;

    private SpatialRelation shapeRelation;

    private Map<String, Object> shapeBuilder;

    private Boolean isCoordInvert = false;


    protected static final Envelope WORLD = new Envelope(-180, 180, -90, 90);

    FilterToElastic delegate;

    public FilterToElasticHelper(FilterToElastic delegate) {
        this.delegate = delegate;
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
                                                PropertyName property, Literal geometry, boolean swapped,
                                                Object extraData) {
        visitComparisonSpatialOperator(filter, property, geometry,
                swapped, extraData);
        return extraData;
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter, Expression e1,
                                                Expression e2, Object extraData) {

        visitBinarySpatialOperator(filter, e1, e2, false, extraData);
        return extraData;
    }

    void visitComparisonSpatialOperator(BinarySpatialOperator filter,
                                        PropertyName property, Literal geometry, boolean swapped, Object extraData) {

        // if geography case, sanitize geometry first
        this.geometry = geometry;
        if (isCurrentGeography()) {
            this.geometry = clipToWorld(geometry);
        }

        visitBinarySpatialOperator(filter, (Expression) property, (Expression) this.geometry, swapped, extraData);

        // if geography case, sanitize geometry first
        if (isCurrentGeography()) {
            if (isWorld(this.geometry)) {
                // nothing to filter in this case
                delegate.queryBuilder = MATCH_ALL;
                return;
            } else if (isEmpty(this.geometry)) {
                if (!(filter instanceof Disjoint)) {
                    delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", MATCH_ALL));
                } else {
                    delegate.queryBuilder = MATCH_ALL;
                }
                return;
            }
        }

        visitBinarySpatialOperator(filter, (Expression) property, (Expression) this.geometry, swapped, extraData);
    }

    void visitBinarySpatialOperator(BinarySpatialOperator filter, Expression e1, Expression e2,
                                    boolean swapped, Object extraData) {

        visitGeoShapeBinarySpatialOperator(filter, e1, e2, swapped, extraData);

    }

    void visitGeoShapeBinarySpatialOperator(BinarySpatialOperator filter, Expression e1, Expression e2,
                                            boolean swapped, Object extraData) {

        if (filter instanceof Disjoint) {
            shapeRelation = SpatialRelation.DISJOINT;
        } else if ((!swapped && filter instanceof Within) || (swapped && filter instanceof Contains)) {
            shapeRelation = SpatialRelation.WITHIN;
        } else if (filter instanceof Intersects || filter instanceof BBOX) {
            shapeRelation = SpatialRelation.INTERSECTS;
        } else {
            LOGGER.error(filter.getClass().getSimpleName()
                    + " is unsupported for geo_shape types");
            shapeRelation = null;
            delegate.fullySupported = false;
        }

        if (shapeRelation != null) {
            e1.accept(delegate, extraData);
            key = XmlUtils.retrievePointPath((String) delegate.field);
            if (key.contains(":")) {
                key = key.split(":")[1];
            }
            if (key.equals("")) {
                key = delegate.collectionReference.params.geometryPath;
            }
            if (!key.equals(delegate.collectionReference.params.geometryPath)) {
                List<OGCExceptionMessage> wfsExceptionMessages = new ArrayList<>();
                wfsExceptionMessages.add(new OGCExceptionMessage(OGCExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter"));
                wfsExceptionMessages.add(new OGCExceptionMessage(OGCExceptionCode.INVALID_PARAMETER_VALUE, key + " is not a valid geom field", "filter"));
                delegate.wfsException = new OGCException(wfsExceptionMessages, Service.WFS);
                throw new RuntimeException();
            }
            e2.accept(delegate, extraData);
            shapeBuilder = delegate.currentShapeBuilder;
        }

        if (shapeRelation != null && shapeBuilder != null) {
            delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must", MATCH_ALL,
                    "filter", ImmutableMap.of("geo_shape",
                            ImmutableMap.of(key, ImmutableMap.of("shape", shapeBuilder, "relation", shapeRelation)))));
        } else {
            delegate.queryBuilder = MATCH_ALL;
        }
    }

    boolean isCurrentGeography() {
        return true;
    }

    protected Literal clipToWorld(Literal geometry) {
        if (geometry != null) {
            Geometry g = geometry.evaluate(null, Geometry.class);
            if (g != null) {
                g.apply(new GeometryComponentFilter() {
                    @Override
                    public void filter(Geometry geom) {
                        if (!isCoordInvert) {
                            geom.apply(new CoordinateFilter() {
                                @Override
                                public void filter(Coordinate coord) {
                                    double oldX = coord.x;
                                    double oldY = coord.y;
                                    coord.x = clipLon(oldY);
                                    coord.y = clipLat(oldX);
                                }
                            });
                        }
                        isCoordInvert = true;
                    }
                });
                geometry = CommonFactoryFinder.getFilterFactory(null).literal(g);
            }
        }

        return geometry;
    }

    protected double clipLon(double lon) {
        double x = Math.signum(lon) * (Math.abs(lon) % 360);
        return x = x > 180 ? x - 360 : (x < -180 ? x + 360 : x);
    }

    protected double clipLat(double lat) {
        return Math.min(90, Math.max(-90, lat));
    }

    /**
     * Returns true if the geometry covers the entire world
     *
     * @param geometry
     * @return
     */
    protected boolean isWorld(Literal geometry) {
        boolean result = false;
        if (geometry != null) {
            Geometry g = geometry.evaluate(null, Geometry.class);
            if (g != null) {
                result = JTS.toGeometry(WORLD).equalsTopo(g.union());
            }
        }
        return result;
    }

    /**
     * Returns true if the geometry is fully empty
     *
     * @param geometry
     * @return
     */
    protected boolean isEmpty(Literal geometry) {
        boolean result = false;
        if (geometry != null) {
            Geometry g = geometry.evaluate(null, Geometry.class);
            result = g == null || g.isEmpty();
        }
        return result;
    }

}
