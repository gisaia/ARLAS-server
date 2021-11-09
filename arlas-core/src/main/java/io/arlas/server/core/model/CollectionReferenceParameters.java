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

package io.arlas.server.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.server.core.model.enumerations.GeoTypeEnum;
import io.arlas.server.core.model.request.Filter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionReferenceParameters implements Serializable {
    private static final long serialVersionUID = 7530916591832941958L;

    @NotEmpty
    @JsonProperty(value = "index_name", required = true)
    public String indexName;

    @NotEmpty
    @JsonProperty(value = "id_path", required = true)
    public String idPath;

    @JsonProperty(value = "geometry_path")
    public String geometryPath;

    @JsonProperty(value = "centroid_path")
    public String centroidPath;

    @JsonProperty(value = "h3_path")
    public String h3Path;

    @JsonProperty(value = "timestamp_path")
    public String timestampPath;

    @JsonProperty(value = "exclude_fields")
    public String excludeFields = null;

    @JsonProperty(value = "update_max_hits")
    public int updateMaxHits = Integer.MAX_VALUE;

    @JsonProperty(value = "taggable_fields")
    public String taggableFields = null;

    @JsonProperty(value = "exclude_wfs_fields")
    public String excludeWfsFields = null;

    @JsonProperty(value = "custom_params")
    public Map<String, String> customParams = null;

    @JsonProperty(value = "atom_feed")
    public Feed atomFeed = null;

    @JsonProperty(value = "open_search")
    public OpenSearch openSearch = null;

    @JsonProperty(value = "inspire")
    public Inspire inspire = new Inspire();

    @JsonProperty(value = "dublin_core_element_name")
    public DublinCoreElementName dublinCoreElementName = new DublinCoreElementName();

    @JsonProperty(value = "raster_tile_url")
    public RasterTileURL rasterTileURL = null;

    @JsonProperty(value = "raster_tile_width")
    public int rasterTileWidth = -1;

    @JsonProperty(value = "raster_tile_height")
    public int rasterTileHeight = -1;

    @JsonProperty(value = "filter")
    public Filter filter = null;

    @JsonIgnore
    private Map<String, GeoTypeEnum> geoTypes = new ConcurrentHashMap<>();

    public CollectionReferenceParameters() {
    }

    public void setGeometryType(String path, GeoTypeEnum type) {
        this.geoTypes.put(path, type);
    }

    public GeoTypeEnum getGeometryType(String path) {
        return path == null ? null : this.geoTypes.get(path);
    }

}

