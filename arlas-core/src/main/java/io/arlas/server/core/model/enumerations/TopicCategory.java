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

package io.arlas.server.core.model.enumerations;

public enum TopicCategory {

    FARMING("farming"),
    BIOTA("biota"),
    BOUNDARIES("boundaries"),
    CLIMATOLOGY_METEOROLOGY_ATMOSPHERE("climatologyMeteorologyAtmosphere"),
    ECONOMY("economy"),
    ELEVATION("elevation"),
    ENVIRONMENT("environment"),
    GEOSCIENTIFIC_INFORMATION("geoscientificInformation"),
    HEALTH("health"),
    IMAGERY_BASE_MAPS_EARTH_COVER("imageryBaseMapsEarthCover"),
    INTELLIGENCE_MILITARY("intelligenceMilitary"),
    INLAND_WATERS("inlandWaters"),
    LOCATION("location"),
    OCEANS("oceans"),
    PLANNING_CADASTRE("planningCadastre"),
    SOCIETY("society"),
    STRUCTURE("structure"),
    TRANSPORTATION("transportation"),
    UTILITIES_COMMUNICATION("utilitiesCommunication");
    private final String value;

    TopicCategory(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TopicCategory fromValue(String v) {
        for (TopicCategory c: TopicCategory.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}