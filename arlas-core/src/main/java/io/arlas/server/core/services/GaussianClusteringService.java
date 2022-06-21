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


import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.BadRequestException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Histogram2D;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.*;
import io.arlas.server.core.model.response.*;
import io.arlas.server.core.utils.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.geojson.*;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.locationtech.spatial4j.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.spark.sql.Dataset;

import java.util.ArrayList;
import java.util.List;

public class GaussianClusteringService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(GaussianClusteringService.class);

    private static final SparkSession spark = SparkSession.builder().getOrCreate();

    /**
     * Service to handle the Gaussian Clustering of data
     * **/
    public GaussianClusteringService() {
    }

    public AggregationResponse clusterData(AggregationResponse histogram2D) throws ArlasException {
        LOGGER.info("Starting the data clustering");

        for (AggregationResponse cellData : histogram2D.elements) {
            Dataset<Histogram2D> dataset = this._convertDataToDataset(cellData);
            LOGGER.debug(dataset.);
        }

        return histogram2D;
    }

    private Dataset<Histogram2D> _convertDataToDataset(AggregationResponse cellHistogram) {
        List<Histogram2D> listHistogram = new ArrayList<>();

        // Search for the lowest count for an angle bucket
        float valley = 0;
        long valleyCount = cellHistogram.count;

        for (AggregationResponse element : cellHistogram.elements) {
            for (AggregationResponse bucket_angle : element.elements) {
                if (bucket_angle.count < valleyCount) {
                    valley = (float) bucket_angle.key;
                    valleyCount = bucket_angle.count;
                }
                for (AggregationResponse bucket_flux : bucket_angle.elements) {
                    listHistogram.add(new Histogram2D((float) bucket_angle.key, (float) bucket_flux.key, (float)bucket_flux.count / cellHistogram.count));
                }
            }
        }

        // Shift dataset to have the valley at the start of the data
        for (Histogram2D row : listHistogram) {
            if (row.angle < valley) {
                // This is valid only for data that is in degrees, will introduce a different value later
                row.angle += 360;
            }
        }

        // If the shift created a duplicate in the Dataset, sum the probabilities for each concerned buckets
        if (listHistogram.get(0).angle == listHistogram.get(-1).angle) {
            int lengthFirstBucket = cellHistogram.elements.get(0).elements.size();
            int lengthLastBucket = cellHistogram.elements.get(-1).elements.size();

            // faire une liste d'entiers, plus simple pour checker, enelever, etc
            List<Histogram2D> dataToCheck = listHistogram.subList(cellHistogram.elements.size() - 1 - lengthLastBucket, cellHistogram.elements.size() - 1);
            for (int i=0; i < lengthFirstBucket; i++) {
                for (Histogram2D row : dataToCheck) {
                    if (listHistogram.get(i).flux == row.flux) {
                        listHistogram.get(i).probability += row.probability;
                        listHistogram.remove(row);
                        continue;
                    }
                }
            }
        }

        return spark.createDataset(listHistogram, Encoders.bean(Histogram2D.class));
    }
}
