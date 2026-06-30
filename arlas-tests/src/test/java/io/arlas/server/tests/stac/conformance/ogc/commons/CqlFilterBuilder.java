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

package io.arlas.server.tests.stac.conformance.ogc.commons;

import java.util.List;

public final class CqlFilterBuilder {

    private CqlFilterBuilder() {}

    public static String validNullCheck(String queryable) {
        return queryable + " IS NULL";
    }

    public static String validEqualCheck(String queryable) {
        return queryable + " = 0";
    }

    public static String sIntersectsBbox(String spatialQueryable, List<Double> bbox) {
        return "S_INTERSECTS(" + spatialQueryable + ",BBOX(" +
                bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + "," + bbox.get(3) + "))";
    }
}
