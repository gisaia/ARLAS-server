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

import io.dropwizard.jackson.JsonSnakeCase;
import org.geojson.Point;
import org.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

@JsonSnakeCase
public class AggregationResponse extends OperationInfo {
    public String name;
    public Long count;
    public Long sumotherdoccounts;
    public Object key;
    public Object keyAsString;
    public List<AggregationResponse> elements;
    public List<AggregationMetric> metrics;
    public Polygon BBOX = null;
    public Point centroid = null;
}

