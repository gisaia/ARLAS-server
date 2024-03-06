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

package io.arlas.server.stac.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;


public class Collection extends Catalog {

  private @Valid String type = "Collection";

  private @Valid List<String> keywords = new ArrayList<>();

  private @Valid List<String> crs = Collections.singletonList("http://www.opengis.net/def/crs/OGC/1.3/CRS84");

  private @Valid String license = null;

  private @Valid Extent extent = null;

  private @Valid List<Provider> providers = null;

  private @Valid Map<String, Object> summaries = null;

  private @Valid Map<String, Object> assets = null;

  /**
   **/
  public Collection stacVersion(String stacVersion) {
    this.stacVersion = stacVersion;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("stac_version")
  @NotNull

  public String getStacVersion() {
    return stacVersion;
  }
  public void setStacVersion(String stacVersion) {
    this.stacVersion = stacVersion;
  }

  /**
   **/
  public Collection stacExtensions(List<String> stacExtensions) {
    this.stacExtensions = stacExtensions;
    return this;
  }

  
  @Schema()
  @JsonProperty("stac_extensions")

  public List<String> getStacExtensions() {
    return stacExtensions;
  }
  public void setStacExtensions(List<String> stacExtensions) {
    this.stacExtensions = stacExtensions;
  }

  /**
   **/
  public Collection type(String type) {
    this.type = type;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  @NotNull

  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * identifier of the collection used, for example, in URIs
   **/
  public Collection id(String id) {
    this.id = id;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "identifier of the collection used, for example, in URIs")
  @JsonProperty("id")
  @NotNull

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * human readable title of the collection
   **/
  public Collection title(String title) {
    this.title = title;
    return this;
  }

  
  @Schema(description = "human readable title of the collection")
  @JsonProperty("title")

  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Detailed multi-line description to fully explain the catalog or collection. [CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.
   **/
  public Collection description(String description) {
    this.description = description;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Detailed multi-line description to fully explain the catalog or collection. [CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.")
  @JsonProperty("description")
  @NotNull

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * List of keywords describing the collection.
   **/
  public Collection keywords(List<String> keywords) {
    this.keywords = keywords;
    return this;
  }

  
  @Schema(description = "List of keywords describing the collection.")
  @JsonProperty("keywords")

  public List<String> getKeywords() {
    return keywords;
  }
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  /**
   * List of crs describing the collection.
   **/
  public Collection crs(List<String> crs) {
    this.crs = crs;
    return this;
  }


  @Schema(description = "List of crs describing the collection.")
  @JsonProperty("crs")

  public List<String> getCrs() {
    return crs;
  }
  public void setCrs(List<String> crs) {
    this.crs = crs;
  }

  /**
   **/
  public Collection license(String license) {
    this.license = license;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("license")
  @NotNull

  public String getLicense() {
    return license;
  }
  public void setLicense(String license) {
    this.license = license;
  }

  /**
   **/
  public Collection extent(Extent extent) {
    this.extent = extent;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("extent")
  @NotNull

  public Extent getExtent() {
    return extent;
  }
  public void setExtent(Extent extent) {
    this.extent = extent;
  }

  /**
   **/
  public Collection providers(List<Provider> providers) {
    this.providers = providers;
    return this;
  }

  
  @Schema()
  @JsonProperty("providers")

  public List<Provider> getProviders() {
    return providers;
  }
  public void setProviders(List<Provider> providers) {
    this.providers = providers;
  }

  /**
   **/
  public Collection links(List<StacLink> links) {
    this.links = links;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("links")
  @NotNull

  public List<StacLink> getLinks() {
    return links;
  }
  public void setLinks(List<StacLink> links) {
    this.links = links;
  }

  /**
   * Summaries are either a unique set of all available values *or* statistics. Statistics by default only specify the range (minimum and maximum values), but can optionally be accompanied by additional statistical values. The range can specify the potential range of values, but it is recommended to be as precise as possible. The set of values must contain at least one element and it is strongly recommended to list all values. It is recommended to list as many properties as reasonable so that consumers get a full overview of the Collection. Properties that are covered by the Collection specification (e.g. &#x60;providers&#x60; and &#x60;license&#x60;) may not be repeated in the summaries.
   **/
  public Collection summaries(Map<String, Object> summaries) {
    this.summaries = summaries;
    return this;
  }

  
  @Schema(description = "Summaries are either a unique set of all available values *or* statistics. Statistics by default only specify the range (minimum and maximum values), but can optionally be accompanied by additional statistical values. The range can specify the potential range of values, but it is recommended to be as precise as possible. The set of values must contain at least one element and it is strongly recommended to list all values. It is recommended to list as many properties as reasonable so that consumers get a full overview of the Collection. Properties that are covered by the Collection specification (e.g. `providers` and `license`) may not be repeated in the summaries.")
  @JsonProperty("summaries")

  public Map<String, Object> getSummaries() {
    return summaries;
  }
  public void setSummaries(Map<String, Object> summaries) {
    this.summaries = summaries;
  }


  /**
   * This provides an optional mechanism to expose assets that don't make sense at the Item level.
   **/
  public Collection assets(Map<String, Object> assets) {
    this.assets = assets;
    return this;
  }


  @Schema(description = "This provides an optional mechanism to expose assets that don't make sense at the Item level.")
  @JsonProperty("assets")

  public Map<String, Object> getAssets() {
    return assets;
  }
  public void setAssets(Map<String, Object> assets) {
    this.assets = assets;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Collection collection = (Collection) o;
    return Objects.equals(stacVersion, collection.stacVersion) &&
        Objects.equals(stacExtensions, collection.stacExtensions) &&
        Objects.equals(type, collection.type) &&
        Objects.equals(id, collection.id) &&
        Objects.equals(title, collection.title) &&
        Objects.equals(description, collection.description) &&
        Objects.equals(keywords, collection.keywords) &&
        Objects.equals(license, collection.license) &&
        Objects.equals(extent, collection.extent) &&
        Objects.equals(providers, collection.providers) &&
        Objects.equals(links, collection.links) &&
        Objects.equals(summaries, collection.summaries) &&
        Objects.equals(assets, collection.assets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stacVersion, stacExtensions, type, id, title, description, keywords, license, extent, providers, links, summaries, assets);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Collection {\n");
    
    sb.append("    stacVersion: ").append(toIndentedString(stacVersion)).append("\n");
    sb.append("    stacExtensions: ").append(toIndentedString(stacExtensions)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    keywords: ").append(toIndentedString(keywords)).append("\n");
    sb.append("    license: ").append(toIndentedString(license)).append("\n");
    sb.append("    extent: ").append(toIndentedString(extent)).append("\n");
    sb.append("    providers: ").append(toIndentedString(providers)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    summaries: ").append(toIndentedString(summaries)).append("\n");
    sb.append("    assets: ").append(toIndentedString(assets)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
