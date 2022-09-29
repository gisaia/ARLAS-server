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

import io.arlas.gmm.utils.MatrixVectorOps;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.List;
import java.util.Map;

public class GaussianResponse {
    public double weight;
    public Array1D<Double> mean;
    public Array1D<Double> covariance;

    private static double equalityMargin = 0.05;

    public GaussianResponse(double weight, Array1D<Double> mean, Primitive64Store covariance) {
        this.weight = weight;
        this.mean = mean;
        this.covariance = Array1D.PRIMITIVE64.copy(covariance.data);
    }

    public GaussianResponse(Map<String, Object> map) {
        this.weight = (Double) map.get("weight");
        this.mean = Array1D.PRIMITIVE64.copy((List<Double>) map.get("mean"));
        this.covariance = Array1D.PRIMITIVE64.copy((List<Double>) map.get("covariance"));
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

        boolean meanEquality =
                MatrixVectorOps.norm2(MatrixVectorOps.subtract(this.mean, gaussianResponse.mean))
                        < equalityMargin * MatrixVectorOps.norm2(this.mean);

        boolean covarianceEquality =
                MatrixVectorOps.norm2(MatrixVectorOps.subtract(this.covariance, gaussianResponse.covariance))
                        < equalityMargin * MatrixVectorOps.norm2(this.covariance);

        return weightEquality && meanEquality && covarianceEquality;
    }
}
