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

package io.arlas.server.tests.stac.conformance.ogc.commons;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionFilteringMetadata {

    private String collectionId;
    private String itemsPath;
    private String queryablesUri;
    private String sampleQueryable;
    private String spatialQueryable;
    private boolean additionalProperties = true;
    private List<Double> wgs84Bbox = new ArrayList<>();

    public List<Map<String, Object>> getUnfilteredFeatures() {
        return unfilteredFeatures;
    }

    public void setUnfilteredFeatures(List<Map<String, Object>> unfilteredFeatures) {
        this.unfilteredFeatures = unfilteredFeatures;
    }

    private List<Map<String, Object>> unfilteredFeatures = new ArrayList<>();

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getItemsPath() {
        return itemsPath;
    }

    public void setItemsPath(String itemsPath) {
        this.itemsPath = itemsPath;
    }

    public String getQueryablesUri() {
        return queryablesUri;
    }

    public void setQueryablesUri(String queryablesUri) {
        this.queryablesUri = queryablesUri;
    }

    public String getSampleQueryable() {
        return sampleQueryable;
    }

    public void setSampleQueryable(String sampleQueryable) {
        this.sampleQueryable = sampleQueryable;
    }

    public String getSpatialQueryable() {
        return spatialQueryable;
    }

    public void setSpatialQueryable(String spatialQueryable) {
        this.spatialQueryable = spatialQueryable;
    }

    public boolean isAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public List<Double> getWgs84Bbox() {
        return wgs84Bbox;
    }

    public void setWgs84Bbox(List<Double> wgs84Bbox) {
        this.wgs84Bbox = wgs84Bbox == null ? new ArrayList<>() : new ArrayList<>(wgs84Bbox);
    }
}
