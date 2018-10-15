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

package io.arlas.server.model.request;

import io.arlas.server.model.enumerations.AggregatedGeometryStrategyEnum;

public class AggregatedGeometry {
    public AggregatedGeometryStrategyEnum strategy;
    public String field;

    public AggregatedGeometry() {
    }

    public AggregatedGeometry(AggregatedGeometryStrategyEnum strategy, String field) {
        this.strategy = strategy;
        this.field = field;
    }

    public AggregatedGeometry(AggregatedGeometryStrategyEnum strategy) {
        this.strategy = strategy;
    }

    public String flatten() {
        if (field != null) {
            return field + '-' + strategy.name();
        } else {
            return strategy.name();
        }
    }
}
