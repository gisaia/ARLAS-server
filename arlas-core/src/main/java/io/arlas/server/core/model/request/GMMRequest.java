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

package io.arlas.server.core.model.request;

import io.arlas.server.core.utils.CheckParams;

import java.util.ArrayList;
import java.util.List;

public class GMMRequest extends AggregationsRequest {
    public String abscissaUnit;
    public Integer maxGaussians;
    public List<Double> maxSpread;
    public List<Double> minSpread;

    public GMMRequest(List<Aggregation> aggregations, String abscissaUnit, Integer maxGaussians, List<Double> maxSpread) {
        this.aggregations = aggregations;
        this.abscissaUnit = abscissaUnit;
        this.maxGaussians = maxGaussians;
        this.maxSpread = maxSpread;
    }
}
