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

import io.arlas.server.exceptions.ArlasException;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import static org.junit.Assert.assertEquals;
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
}
