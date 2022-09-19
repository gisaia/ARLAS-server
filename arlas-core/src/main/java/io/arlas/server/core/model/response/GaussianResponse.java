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
