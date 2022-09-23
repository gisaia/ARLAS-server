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

package io.arlas.server.core.services;

import io.arlas.gmm.data.DataPoint;
import io.arlas.gmm.data.DataSet;
import io.arlas.gmm.gaussian.GaussianDistribution;
import io.arlas.gmm.gaussian.GaussianMixtureModel;
import io.arlas.server.core.model.request.GMMRequest;
import io.arlas.server.core.model.response.AggregationResponse;
import io.arlas.server.core.model.response.GaussianResponse;
import io.arlas.server.core.model.response.ReturnedGeometry;
import io.arlas.server.core.utils.CheckParams;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.ojalgo.array.Array1D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.arlas.server.core.services.FluidSearchService.GMM_AGG;


public class GMMService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(GMMService.class);

    public static final String GMM_MAX_COMPONENTS = "" + 5;
    public static final int MAXIMUM_ANGLE_STD = 60;
    public static final String DEGREE_UNIT = "degree";
    public static final String RADIAN_UNIT = "radian";

    public GMMService() {
    }

    public AggregationResponse gmm(FeatureCollection histogramAggregation, GMMRequest gmmRequest, Boolean isGeoAggregation) {
        AggregationResponse gmmAggregation = new AggregationResponse();
        gmmAggregation.name = GMM_AGG;
        gmmAggregation.elements = new ArrayList<>(histogramAggregation.getFeatures().size());

        boolean isAngleClustering = Objects.equals(gmmRequest.abscissaUnit, DEGREE_UNIT) || Objects.equals(gmmRequest.abscissaUnit, RADIAN_UNIT);
        int dimension = gmmRequest.aggregations.size() - (isGeoAggregation ? 1 : 0);

        // GMM on each feature
        for(Feature feature: histogramAggregation.getFeatures()) {
            DataSet dataSet = new DataSet(dimension);

            long totalCount = feature.getProperty("count");
            double valley = Double.MAX_VALUE;

            // Search for the lowest count on angle bucket in case the aggregation is performed on flux data
            if (isAngleClustering) {
                long valleyCount = feature.getProperty("count");

                for (AggregationResponse elem : (ArrayList<AggregationResponse>) feature.getProperty("elements")) {
                    for (AggregationResponse bucketAngle : elem.elements) {
                        if (bucketAngle.count < valleyCount) {
                            valley = (double) bucketAngle.key;
                            valleyCount = bucketAngle.count;
                        }
                    }
                }
            }

            // Fill the data set
            for (AggregationResponse elem : (ArrayList<AggregationResponse>) feature.getProperty("elements")) {
                fillDataSetRecursively(dataSet, elem, new ArrayList<>());
            }

            List<Double> observedProbabilities = new ArrayList<>(dataSet.getWeights());

            dataSet.updateWeights((double) totalCount / dataSet.length, 2);
            observedProbabilities.replaceAll(aDouble -> aDouble / totalCount);

            // Shift dataset to have the valley at the start of the data
            if (isAngleClustering) {
                double angleOffset = Objects.equals(gmmRequest.abscissaUnit, DEGREE_UNIT) ? 360 : 2 * Math.PI;

                for (DataPoint data : dataSet.getDataPoints()) {
                    if (data.values.get(0) < valley) {
                        data.values.set(0, data.values.get(0) + angleOffset);
                    }
                }
            }

            GaussianMixtureModel model = new GaussianMixtureModel(Math.min(gmmRequest.maxGaussians, (int) Math.ceil(dataSet.length/10.)), dataSet);

            Array1D<Double> maxGaussianSpread = Array1D.PRIMITIVE64.copy(gmmRequest.maxSpread);
            Array1D<Double> minGaussianSpread = Array1D.PRIMITIVE64.copy(gmmRequest.minSpread);

            // Perform the clustering
            model.cluster(dataSet, maxGaussianSpread, minGaussianSpread);
            GaussianMixtureModel clusteredModel = model.mergeCloseGaussians(dataSet.getDataPoints(), observedProbabilities, minGaussianSpread);

            // Convert the result to an AggregationResponse
            AggregationResponse gmm = new AggregationResponse();
            gmm.count = totalCount;

            ReturnedGeometry geometry = new ReturnedGeometry();
            geometry.geometry = feature.getGeometry();
            gmm.geometries = List.of(geometry);

            gmm.gaussians = new ArrayList<>(clusteredModel.numberClusters);
            for (int i = 0; i < clusteredModel.numberClusters; i++) {
                GaussianDistribution gaussian = clusteredModel.getGaussian(i);
                gmm.gaussians.add(new GaussianResponse(gaussian.weight, gaussian.mean, gaussian.covariance));
            }
            gmmAggregation.elements.add(gmm);
        }
        return gmmAggregation;
    }

    /**
     * Explore recursively the aggregation tree in order to fill the dataSet with the data contained
     * @param dataSet the DataSet to fill
     * @param element the Feature element containing the aggregated data
     * @param numericalValues the list storing the information of the bucket
     */
    private void fillDataSetRecursively(DataSet dataSet, AggregationResponse element, List<Double> numericalValues) {
        if (element.elements == null || element.elements.size() == 0) {
            dataSet.addDataPoint(new DataPoint(numericalValues), (double) element.count);
            return;
        }

        for (AggregationResponse bucket : element.elements) {
            List<Double> treeExploration = new ArrayList<>(numericalValues);
            if (bucket.key != null)
                treeExploration.add((double) bucket.key);

            fillDataSetRecursively(dataSet, bucket, treeExploration);
        }
    }
}
