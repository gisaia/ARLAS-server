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

package io.arlas.server.model.response;

import java.util.Arrays;
import java.util.List;

public enum ElasticType {
    TEXT("text"), KEYWORD("keyword"),
    LONG("long"), INTEGER("integer"), SHORT("short"), BYTE("byte"), DOUBLE("double"), FLOAT("float"),
    DATE("date"),
    BOOLEAN("boolean"),
    BINARY("binary"),
    INT_RANGE("integer_range"), FLOAT_RANGE("float_range"), LONG_RANGE("long_range"), DOUBLE_RANGE("double_range"), DATE_RANGE("date_range"),
    OBJECT("object"), NESTED("nested"),
    GEO_POINT("geo_point"), GEO_SHAPE("geo_shape"),
    IP("ip"),
    COMPLETION("completion"), TOKEN_COUNT("token_count"), MAPPER_MURMUR3("murmur3"),
    UNKNOWN("unknown");

    public final String elasticType;

    ElasticType(String elasticType) {
        this.elasticType = elasticType;
    }

    public static ElasticType getType(Object type) {
        ElasticType ret = UNKNOWN;
        for (ElasticType t : ElasticType.values()) {
            if (t.elasticType.equals(type.toString())) {
                ret = t;
                break;
            }
        }
        return ret;
    }

    public static List<ElasticType> getComputableTypes() {
        return Arrays.asList(SHORT, INTEGER, LONG, DOUBLE, FLOAT, DATE, UNKNOWN);
    }

    @Override
    public String toString() {
        return elasticType.toString();
    }
}