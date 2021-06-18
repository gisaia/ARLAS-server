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

package io.arlas.server.tests.utils;

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.utils.GeoUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GeoUtilTest {
    @Test
    public void testBboxToWKT() throws ArlasException {
        String canonicalBBOX = "-150,-10,150,10"; // longitudes between -180, 180
        String datelineBBOX = "150,-10,-150,10"; // crosses the dateline
        assertTrue(GeoUtil.bboxToWKT(canonicalBBOX).equals("POLYGON((-150.0 -10.0,-150.0 10.0,150.0 10.0,150.0 -10.0,-150.0 -10.0))"));
        assertTrue(GeoUtil.bboxToWKT(datelineBBOX).equals("MULTIPOLYGON(((-180 -10.0,-180 10.0,-150.0 10.0,-150.0 -10.0,-180 -10.0)),((180 -10.0,180 10.0,150.0 10.0,150.0 -10.0,180 -10.0)))"));
    }

    @Test
    public void testToCanonicalLongitudes() throws ArlasException {
        Geometry nonCanonicalLongitudes = GeoUtil.readWKT("POLYGON((210.0 -10.0,210.0 10.0,150.0 10.0,150.0 -10.0,210.0 -10.0))");
        Envelope nonCanonicalEnvelope = nonCanonicalLongitudes.getEnvelopeInternal();
        assertEquals(nonCanonicalEnvelope.getMaxX(), 210.0, 0);
        assertEquals(nonCanonicalEnvelope.getMinX(), 150.0, 0);
        Geometry canonicalLongitudes = GeoUtil.toCanonicalLongitudes(nonCanonicalLongitudes);
        Envelope canonicalEnvelope = GeoUtil.readWKT(canonicalLongitudes.toString()).getEnvelopeInternal();
        assertEquals(canonicalEnvelope.getMaxX(), 150.0, 0);
        assertEquals(canonicalEnvelope.getMinX(), -150.0, 0);
    }

    @Test
    public void testTranslateLongitudes() throws ArlasException {
        Geometry geo = GeoUtil.readWKT("POLYGON((210.0 -10.0,210.0 10.0,150.0 10.0,150.0 -10.0,210.0 -10.0))");
        Envelope envelope = geo.getEnvelopeInternal();
        assertEquals(envelope.getMaxX(), 210.0, 0);
        assertEquals(envelope.getMinX(), 150.0, 0);
        GeoUtil.translateLongitudesWithCondition(geo, 10, false, 200);
        assertEquals(geo.toString(), "POLYGON ((200 -10, 200 10, 150 10, 150 -10, 200 -10))");
        GeoUtil.translateLongitudes(geo, 20, true);
        assertEquals(geo.toString(), "POLYGON ((220 -10, 220 10, 170 10, 170 -10, 220 -10))");
    }

    @Test
    public void testSplitPolygon() throws ArlasException {
        /** east > 180 & west is canonical**/
        Geometry geo = (Polygon) GeoUtil.readWKT("POLYGON((210 -10,210 10,150 10,150 -10,210 -10))");
        List<Polygon> geometries = GeoUtil.splitPolygon((Polygon)geo)._1();
        assertTrue(geometries.size() == 2);
        List<String> geometriesString = geometries.stream().map(geometry -> geometry.toString()).collect(Collectors.toList());
        assertThat(geometriesString, CoreMatchers.hasItems("POLYGON ((180 10, 180 -10, 150 -10, 150 10, 180 10))", "POLYGON ((-180 -10, -180 10, -150 10, -150 -10, -180 -10))"));

        /** west < -180 & east is canonical**/
        geo = GeoUtil.readWKT("POLYGON((-320 -10,-320 10,-40 10,-40 -10,-320 -10))");
        geometries = GeoUtil.splitPolygon((Polygon)geo)._1();
        assertTrue(geometries.size() == 2);
        geometriesString = geometries.stream().map(geometry -> geometry.toString()).collect(Collectors.toList());
        assertThat(geometriesString, CoreMatchers.hasItems("POLYGON ((180 10, 180 -10, 40 -10, 40 10, 180 10))", "POLYGON ((-180 -10, -180 10, -40 10, -40 -10, -180 -10))"));

        /** west and east are canonical*/
        geo = GeoUtil.readWKT("POLYGON((-80 -10,-80 10,-40 10,-40 -10,-80 -10))");
        geometries = GeoUtil.splitPolygon((Polygon)geo)._1();
        assertTrue(geometries.size() == 1);
        geometriesString = geometries.stream().map(geometry -> geometry.toString()).collect(Collectors.toList());
        assertThat(geometriesString, CoreMatchers.hasItems("POLYGON ((-80 -10, -80 10, -40 10, -40 -10, -80 -10))"));

        /** west and east are < -180*/
        geo = GeoUtil.readWKT("POLYGON((-360 -10,-360 10,-200 10,-200 -10,-360 -10))");
        geometries = GeoUtil.splitPolygon((Polygon)geo)._1();
        assertTrue(geometries.size() == 1);
        geometriesString = geometries.stream().map(geometry -> geometry.toString()).collect(Collectors.toList());
        assertThat(geometriesString, CoreMatchers.hasItems("POLYGON ((0 -10, 0 10, 160 10, 160 -10, 0 -10))"));

        /** west and east are > 180*/
        geo = GeoUtil.readWKT("POLYGON((200 -10,200 10,360 10,360 -10,200 -10))");
        geometries = GeoUtil.splitPolygon((Polygon)geo)._1();
        assertTrue(geometries.size() == 1);
        geometriesString = geometries.stream().map(geometry -> geometry.toString()).collect(Collectors.toList());
        assertThat(geometriesString, CoreMatchers.hasItems("POLYGON ((-160 -10, -160 10, 0 10, 0 -10, -160 -10))"));
    }
}
