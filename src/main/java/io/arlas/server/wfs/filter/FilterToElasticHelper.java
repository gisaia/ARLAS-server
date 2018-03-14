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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.SI;


import io.arlas.server.exceptions.WFSException;
import io.arlas.server.exceptions.WFSExceptionCode;
import io.arlas.server.exceptions.WFSExceptionMessage;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.locationtech.spatial4j.shape.SpatialRelation;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Within;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.Polygon;
import org.slf4j.LoggerFactory;

class FilterToElasticHelper {
    org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FilterToElasticHelper.class);

    private double lat, lon, distance;

    private String key;

    private Literal geometry;

    private SpatialRelation shapeRelation;

    private Map<String,Object> shapeBuilder;

    private Boolean isCoordInvert =false;

    /**
     * Conversion factor from common units to meter
     */
    protected static final Map<String, Double> UNITS_MAP = new HashMap<String, Double>() {

        private static final long serialVersionUID = 1L;

        {
            put("kilometers", 1000.0);
            put("kilometer", 1000.0);
            put("mm", 0.001);
            put("millimeter", 0.001);
            put("mi", 1609.344);
            put("miles", 1609.344);
            put("NM", 1852d);
            put("feet", 0.3048);
            put("ft", 0.3048);
            put("in", 0.0254);
        }
    };

    protected static final Envelope WORLD = new Envelope(-180, 180, -90, 90);

    FilterToElastic delegate;

    public FilterToElasticHelper(FilterToElastic delegate) {
        this.delegate = delegate;
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) {

        if (filter instanceof DistanceBufferOperator) {
            visitDistanceSpatialOperator((DistanceBufferOperator) filter,
                    property, geometry, swapped, extraData);
        } else {
            visitComparisonSpatialOperator(filter, property, geometry,
                    swapped, extraData);
        }
        return extraData;
    }

    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter, Expression e1,
            Expression e2, Object extraData) {

        visitBinarySpatialOperator(filter, e1, e2, false, extraData);
        return extraData;
    }

    protected void visitDistanceSpatialOperator(DistanceBufferOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) {

        property.accept(delegate, extraData);
        key = (String) delegate.field;
        if(!key.equals(delegate.collectionReference.params.geometryPath)){
            List<WFSExceptionMessage> wfsExceptionMessages = new ArrayList<>();
            wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter"));
            wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE,key +" is not a valid geom field","filter"));
            delegate.wfsException = new WFSException(wfsExceptionMessages);
            throw new RuntimeException();
        }
        geometry.accept(delegate, extraData);
        final Geometry geo = delegate.currentGeometry;
        lat = geo.getCentroid().getY();
        lon = geo.getCentroid().getX();
        final double inputDistance = filter.getDistance();
        final String inputUnits = filter.getDistanceUnits();
        distance = Double.valueOf(toMeters(inputDistance, inputUnits));

        delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must", ElasticConstants.MATCH_ALL,
                "filter", ImmutableMap.of("geo_distance", 
                        ImmutableMap.of("distance", distance+SI.METER.toString(), key, ImmutableList.of(lon,lat)))));

        if ((filter instanceof DWithin && swapped)
                || (filter instanceof Beyond && !swapped)) {
            delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", delegate.queryBuilder));
        }
    }

    private String toMeters(double distance, String unit) {
        // only geography uses metric units
        if(isCurrentGeography()) {
            Double conversion = UNITS_MAP.get(unit);
            if(conversion != null) {
                return String.valueOf(distance * conversion);
            }
        }

        // in case unknown unit or not geography, use as-is
        return String.valueOf(distance);
    }

    void visitComparisonSpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped, Object extraData) {

        // if geography case, sanitize geometry first
        this.geometry = geometry;
        if(isCurrentGeography()) {
            this.geometry = clipToWorld(geometry);
        }

        visitBinarySpatialOperator(filter, (Expression)property, (Expression)this.geometry, swapped, extraData);

        // if geography case, sanitize geometry first
        if(isCurrentGeography()) {
            if(isWorld(this.geometry)) {
                // nothing to filter in this case
                delegate.queryBuilder = ElasticConstants.MATCH_ALL;
                return;
            } else if(isEmpty(this.geometry)) {
                if(!(filter instanceof Disjoint)) {
                    delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must_not", ElasticConstants.MATCH_ALL));
                } else {
                    delegate.queryBuilder = ElasticConstants.MATCH_ALL;
                }
                return;
            }
        }

        visitBinarySpatialOperator(filter, (Expression)property, (Expression)this.geometry, swapped, extraData);
    }

    void visitBinarySpatialOperator(BinarySpatialOperator filter, Expression e1, Expression e2, 
            boolean swapped, Object extraData) {

        // TODO set geometry type of arlas collection Point or GeoShape
        AttributeDescriptor attType;
        attType = (AttributeDescriptor)e1.evaluate(delegate.featureType);

        ElasticAttribute.ElasticGeometryType geometryType= ElasticAttribute.ElasticGeometryType.GEO_SHAPE;
        //geometryType = (ElasticAttribute.ElasticGeometryType) attType.getUserData().get(ElasticConstants.GEOMETRY_TYPE);

        if (geometryType == ElasticAttribute.ElasticGeometryType.GEO_POINT) {
            visitGeoPointBinarySpatialOperator(filter, e1, e2, swapped, extraData);                        
        } else {
            visitGeoShapeBinarySpatialOperator(filter, e1, e2, swapped, extraData);            
        }
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
            key = (String) delegate.field;
            if(key.contains(":")){
                key=key.split(":")[1];
            }
            if(key.equals("")){
                key = delegate.collectionReference.params.geometryPath;
            }
            if(!key.equals(delegate.collectionReference.params.geometryPath)){
                List<WFSExceptionMessage> wfsExceptionMessages = new ArrayList<>();
                wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter"));
                wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE,key +" is not a valid geom field","filter"));
                delegate.wfsException = new WFSException(wfsExceptionMessages);
                throw new RuntimeException();
            }
            e2.accept(delegate, extraData);
            shapeBuilder = delegate.currentShapeBuilder;
        }

        if (shapeRelation != null && shapeBuilder != null) {
            delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must", ElasticConstants.MATCH_ALL,
                    "filter", ImmutableMap.of("geo_shape", 
                            ImmutableMap.of(key, ImmutableMap.of("shape", shapeBuilder, "relation", shapeRelation)))));
        } else {
            delegate.queryBuilder = ElasticConstants.MATCH_ALL;
        }
    }

    void visitGeoPointBinarySpatialOperator(BinarySpatialOperator filter, Expression e1, Expression e2, 
            boolean swapped, Object extraData) {

        e1.accept(delegate, extraData);
        key = (String) delegate.field;
        if(key.contains(":")){
            key=key.split(":")[1];
        }
        if(key.equals("")){
            key = delegate.collectionReference.params.geometryPath;
        }
        if(!key.equals(delegate.collectionReference.params.geometryPath)){
            List<WFSExceptionMessage> wfsExceptionMessages = new ArrayList<>();
            wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Invalid Filter", "filter"));
            wfsExceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.INVALID_PARAMETER_VALUE,key +" is not a valid geom field","filter"));
            delegate.wfsException = new WFSException(wfsExceptionMessages);
            throw new RuntimeException();
        }
        e2.accept(delegate, extraData);

        final Geometry geometry = delegate.currentGeometry;

        if (geometry instanceof Polygon &&
                ((!swapped && filter instanceof Within) 
                        || (swapped && filter instanceof Contains)
                        || filter instanceof Intersects)) {
            final Polygon polygon = (Polygon) geometry;
            final List<List<Double>> points = new ArrayList<>();
            for (final Coordinate coordinate : polygon.getCoordinates()) {
                points.add(ImmutableList.of(coordinate.x, coordinate.y));
            }
            delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must", ElasticConstants.MATCH_ALL,
                    "filter", ImmutableMap.of("geo_polygon", 
                            ImmutableMap.of(key, ImmutableMap.of("points", points)))));
        } else if (filter instanceof BBOX) {
            final Envelope envelope = geometry.getEnvelopeInternal();
            final double minY = clipLat(envelope.getMinY());
            final double maxY = clipLat(envelope.getMaxY());
            final double minX, maxX;
            if (envelope.getWidth() < 360) {
                minX = clipLon(envelope.getMinX());
                maxX = clipLon(envelope.getMaxX());
            } else {
                minX = -180;
                maxX = 180;
            }
            delegate.queryBuilder = ImmutableMap.of("bool", ImmutableMap.of("must", ElasticConstants.MATCH_ALL,
                    "filter", ImmutableMap.of("geo_bounding_box", ImmutableMap.of(key, 
                            ImmutableMap.of("top_left", ImmutableList.of(minX, maxY), 
                                    "bottom_right", ImmutableList.of(maxX, minY))))));
        } else {LOGGER.debug(filter.getClass().getSimpleName()
                    + " is unsupported for geo_point types");
            delegate.fullySupported = false;
            delegate.queryBuilder = ElasticConstants.MATCH_ALL;
        }
    }


    boolean isCurrentGeography() {
        return true;
    }

    protected Literal clipToWorld(Literal geometry) {
        if(geometry != null) {
            Geometry g = geometry.evaluate(null, Geometry.class);
            if(g != null) {
                g.apply(new GeometryComponentFilter() {
                    @Override
                    public void filter(Geometry geom) {
                        if(!isCoordInvert){
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
                        isCoordInvert=true;
                    }
                });
                geometry = CommonFactoryFinder.getFilterFactory(null).literal(g);

            }
        }

        return geometry;
    }

    protected double clipLon(double lon) {
        double x = Math.signum(lon)*(Math.abs(lon)%360);
        return x = x>180 ? x-360 : (x<-180 ? x+360 : x);
    }

    protected double clipLat(double lat) {
        return Math.min(90, Math.max(-90, lat));
    }

    /**
     * Returns true if the geometry covers the entire world
     * @param geometry
     * @return
     */
    protected boolean isWorld(Literal geometry) {
        boolean result = false;
        if(geometry != null) {
            Geometry g = geometry.evaluate(null, Geometry.class);
            if(g != null) {
                result = JTS.toGeometry(WORLD).equalsTopo(g.union());
            }
        }
        return result;
    }

    /**
     * Returns true if the geometry is fully empty
     * @param geometry
     * @return
     */
    protected boolean isEmpty(Literal geometry) {
        boolean result = false;
        if(geometry != null) {
            Geometry g = geometry.evaluate(null, Geometry.class);
            result = g == null || g.isEmpty();
        }
        return result;
    }

}
