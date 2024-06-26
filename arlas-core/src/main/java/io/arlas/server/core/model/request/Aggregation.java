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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.arlas.server.core.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.core.model.enumerations.AggregationTypeEnum;
import io.arlas.server.core.model.enumerations.Order;
import io.arlas.server.core.model.enumerations.OrderOn;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Aggregation {
    public AggregationTypeEnum type;
    public String field;
    public Interval interval;
    public String format;
    public List<Metric> metrics;
    public Order order;
    public OrderOn on;
    public String size;
    public String include;
    public List<RawGeometry> rawGeometries;
    public List<AggregatedGeometryEnum> aggregatedGeometries;
    public HitsFetcher fetchHits;
    @JsonIgnore
    public int index; // index of the agg in the list of aggregations

    public Aggregation() {
    }
}
