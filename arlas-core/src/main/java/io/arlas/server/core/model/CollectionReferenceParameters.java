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
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.model.enumerations.GeoTypeEnum;
import io.arlas.server.core.model.request.Filter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionReferenceParameters implements Serializable {
    private static final long serialVersionUID = 7530916591832941958L;

    @NotEmpty
    @JsonProperty(value = CollectionReference.INDEX_NAME, required = true)
    public String indexName;

    @NotEmpty
    @JsonProperty(value = CollectionReference.ID_PATH, required = true)
    public String idPath;

    @NotEmpty
    @JsonProperty(value = CollectionReference.GEOMETRY_PATH, required = true)
    public String geometryPath;

    @NotEmpty
    @JsonProperty(value = CollectionReference.CENTROID_PATH, required = true)
    public String centroidPath;

    @JsonProperty(value = CollectionReference.H3_PATH)
    public String h3Path;

    @NotEmpty
    @JsonProperty(value = CollectionReference.TIMESTAMP_PATH, required = true)
    public String timestampPath;

    @JsonProperty(value = CollectionReference.EXCLUDE_FIELDS)
    public String excludeFields = null;

    @JsonProperty(value = "update_max_hits")
    public int updateMaxHits = Integer.MAX_VALUE;

    @JsonProperty(value = "taggable_fields")
    public String taggableFields = null;

    @JsonProperty(value = CollectionReference.EXCLUDE_WFS_FIELDS)
    public String excludeWfsFields = null;

    @JsonProperty(value = "custom_params")
    public Map<String, String> customParams = null;

    @JsonProperty(value = CollectionReference.DISPLAY_NAMES)
    public CollectionDisplayNames collectionDisplayNames = null;

    @JsonProperty(value = CollectionReference.ORGANISATIONS)
    public CollectionOrganisations collectionOrganisations = null;

    @JsonProperty(value = "atom_feed")
    public Feed atomFeed = null;

    @JsonProperty(value = "open_search")
    public OpenSearch openSearch = null;

    @JsonProperty(value = CollectionReference.INSPIRE_PATH)
    public Inspire inspire = new Inspire();

    @JsonProperty(value = CollectionReference.DUBLIN_CORE_PATH)
    public DublinCoreElementName dublinCoreElementName = new DublinCoreElementName();

    @JsonProperty(value = "raster_tile_url")
    public RasterTileURL rasterTileURL = null;

    @JsonProperty(value = "raster_tile_width")
    public int rasterTileWidth = -1;

    @JsonProperty(value = "raster_tile_height")
    public int rasterTileHeight = -1;

    @JsonProperty(value = "filter")
    public Filter filter = null;

    @JsonProperty(value = "license_name")
    public String licenseName = null;

    @JsonProperty(value = "license_urls")
    public List<String> licenseUrls = null;

    @JsonIgnore
    private Map<String, GeoTypeEnum> geoTypes = new ConcurrentHashMap<>();

    public CollectionReferenceParameters() {
    }

    public void setGeometryType(String path, GeoTypeEnum type) {
        this.geoTypes.put(path, type);
    }

    public GeoTypeEnum getGeometryType(String path) {
        // if the path is not defined, return null
        if (StringUtil.isNullOrEmpty(path)) {
            return null;
        }
        return this.geoTypes.get(path);
    }

}

