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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StacFeatureCollection {

  private @Valid String stacVersion = null;

  private @Valid List<String> stacExtensions = null;

  private @Valid String type = "FeatureCollection";

  private @Valid List<Item> features = new ArrayList<>();

  private @Valid List<StacLink> links = null;

  private @Valid String timeStamp = null;

  private @Valid Integer numberMatched = null;

  private @Valid Integer numberReturned = null;

  private @Valid Map<String, Object> context = null;


  /**
   **/
  public StacFeatureCollection stacVersion(String stacVersion) {
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
  public StacFeatureCollection stacExtensions(List<String> stacExtensions) {
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
  public StacFeatureCollection type(String type) {
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
   **/
  public StacFeatureCollection featureCollectionGeoJSONFeatures(List<Item> featureCollectionGeoJSONFeatures) {
    this.features = featureCollectionGeoJSONFeatures;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("features")
  @NotNull

  public List<Item> getFeatures() {
    return features;
  }
  public void setFeatures(List<Item> featureCollectionGeoJSONFeatures) {
    this.features = featureCollectionGeoJSONFeatures;
  }

  /**
   **/
  public StacFeatureCollection links(List<StacLink> links) {
    this.links = links;
    return this;
  }

  
  @Schema()
  @JsonProperty("links")

  public List<StacLink> getLinks() {
    return links;
  }
  public void setLinks(List<StacLink> links) {
    this.links = links;
  }

  /**
   **/
  public StacFeatureCollection timeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  
  @Schema()
  @JsonProperty("timeStamp")

  public String getTimeStamp() {
    return timeStamp;
  }
  public void setTimeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   **/
  public StacFeatureCollection numberMatched(Integer numberMatched) {
    this.numberMatched = numberMatched;
    return this;
  }

  
  @Schema()
  @JsonProperty("numberMatched")

  public Integer getNumberMatched() {
    return numberMatched;
  }
  public void setNumberMatched(Integer numberMatched) {
    this.numberMatched = numberMatched;
  }

  /**
   **/
  public StacFeatureCollection numberReturned(Integer numberReturned) {
    this.numberReturned = numberReturned;
    return this;
  }

  
  @Schema()
  @JsonProperty("numberReturned")

  public Integer getNumberReturned() {
    return numberReturned;
  }
  public void setNumberReturned(Integer numberReturned) {
    this.numberReturned = numberReturned;
  }


  public StacFeatureCollection context(Map<String, Object> context) {
    this.context = context;
    return this;
  }


  @Schema(description = "Augments lists of resources with the number of returned and matches resource and the given limit for the request.")
  @JsonProperty("context")

  public Map<String, Object> getContext() {
    return context;
  }
  public void setContext(Map<String, Object> context) {
    this.context = context;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StacFeatureCollection featureCollectionGeoJSON = (StacFeatureCollection) o;
    return Objects.equals(features, featureCollectionGeoJSON.features) &&
        Objects.equals(links, featureCollectionGeoJSON.links) &&
        Objects.equals(timeStamp, featureCollectionGeoJSON.timeStamp) &&
        Objects.equals(numberMatched, featureCollectionGeoJSON.numberMatched) &&
        Objects.equals(numberReturned, featureCollectionGeoJSON.numberReturned) &&
        Objects.equals(context, featureCollectionGeoJSON.context);
  }

  @Override
  public int hashCode() {
    return Objects.hash(features, links, timeStamp, numberMatched, numberReturned);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FeatureCollectionGeoJSON {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    featureCollectionGeoJSONFeatures: ").append(toIndentedString(features)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
    sb.append("    numberMatched: ").append(toIndentedString(numberMatched)).append("\n");
    sb.append("    numberReturned: ").append(toIndentedString(numberReturned)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
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
