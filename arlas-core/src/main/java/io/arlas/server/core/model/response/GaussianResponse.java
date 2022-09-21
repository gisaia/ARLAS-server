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

import io.dropwizard.jackson.JsonSnakeCase;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.Primitive64Store;

@JsonSnakeCase
public class GaussianResponse {
    public double weight;
    public Array1D<Double> mean;
    public double[] covariance;

    public GaussianResponse(double weight, Array1D<Double> mean, Primitive64Store covariance) {
        this.weight = weight;
        this.mean = mean;
        this.covariance = covariance.data;
    }
}
