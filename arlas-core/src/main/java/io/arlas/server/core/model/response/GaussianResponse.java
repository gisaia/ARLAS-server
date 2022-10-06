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

package io.arlas.server.core.model.response;

import java.util.HashMap;
import java.util.Map;

public class GaussianResponse {
    public double weight;
    public Map<String, Double> mean;

    private static double equalityMargin = 0.05;

    public GaussianResponse(double weight, Map<String, Double> mean) {
        this.weight = weight;
        this.mean = new HashMap<>(mean);
    }

    public GaussianResponse(Map<String, Object> map) {
        this.weight = (Double) map.get("weight");
        this.mean = (Map<String, Double>) map.get("mean");
    }

    public void setEqualityMargin(double equalityMargin) {
        GaussianResponse.equalityMargin = equalityMargin;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof GaussianResponse gaussianResponse)) {
            return false;
        }

        boolean weightEquality =
                Math.abs(this.weight - gaussianResponse.weight)
                        < equalityMargin * this.weight;

        boolean meanEquality = true;
        for (String key: this.mean.keySet()) {
            if (!mean.containsKey(key)) {
                return false;
            }
            meanEquality = meanEquality && (Math.abs(this.mean.get(key) - mean.get(key))
                        < Math.abs(equalityMargin * this.mean.get(key)));
        }

        return weightEquality && meanEquality;
    }
}
