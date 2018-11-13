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

package io.arlas.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.server.model.request.Filter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionReferenceParameters {

    @NotEmpty
    @JsonProperty(value = "index_name", required = true)
    public String indexName;

    @NotEmpty
    @JsonProperty(value = "type_name", required = true)
    public String typeName;

    @JsonProperty(value = "id_path", required = true)
    public String idPath;

    @JsonProperty(value = "geometry_path", required = true)
    public String geometryPath;

    @JsonProperty(value = "centroid_path", required = true)
    public String centroidPath;

    @JsonProperty(value = "timestamp_path", required = true)
    public String timestampPath;

    @JsonProperty(value = "exclude_fields", required = false)
    public String excludeFields = null;

    @JsonProperty(value = "update_max_hits", required = false)
    public int update_max_hits = Integer.MAX_VALUE;

    @JsonProperty(value = "taggable_fields", required = false)
    public String taggableFields = null;

    @JsonProperty(value = "exclude_wfs_fields", required = false)
    public String excludeWfsFields = null;

    @JsonProperty(value = "custom_params", required = false)
    public Map<String, String> customParams = null;

    @JsonProperty(value = "atom_feed", required = false)
    public Feed atomFeed = null;

    @JsonProperty(value = "open_search", required = false)
    public OpenSearch openSearch = null;

    @JsonProperty(value = "inspire", required = false)
    public Inspire inspire = new Inspire();

    @JsonProperty(value = "dublin_core_element_name", required = false)
    public DublinCoreElementName dublinCoreElementName = new DublinCoreElementName();

    @JsonProperty(value = "raster_tile_url", required = false)
    public RasterTileURL rasterTileURL = null;

    @JsonProperty(value = "raster_tile_width", required = false)
    public int rasterTileWidth = -1;

    @JsonProperty(value = "raster_tile_height", required = false)
    public int rasterTileHeight = -1;

    @JsonProperty(value = "filter", required = false)
    public Filter filter = null;

    public CollectionReferenceParameters() {
    }
}
