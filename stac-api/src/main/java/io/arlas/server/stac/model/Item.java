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
import io.swagger.annotations.ApiModelProperty;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Item extends Feature {

  private @Valid String stacVersion = null;

  private @Valid List<String> stacExtensions = null;

  private @Valid String type = "Feature";

  private @Valid List<StacLink> links = null;

  private @Valid String collection = null;

  private @Valid Map<String, Object> assets = null;

  /**
   **/
  public Item stacVersion(String stacVersion) {
    this.stacVersion = stacVersion;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
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
  public Item stacExtensions(List<String> stacExtensions) {
    this.stacExtensions = stacExtensions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stac_extensions")

  public List<String> getStacExtensions() {
    return stacExtensions;
  }
  public void setStacExtensions(List<String> stacExtensions) {
    this.stacExtensions = stacExtensions;
  }

  /**
   **/
  public Item id(String id) {
    this.setId(id);
    return this;
  }


  /**
   **/
  public Item bbox(double[] bbox) {
    this.setBbox(bbox);
    return this;
  }

  /**
   **/
  public Item geometry(GeoJsonObject geometry) {
    this.setGeometry(geometry);
    return this;
  }

  /**
   **/
  public Item type(String type) {
    this.type = type;
    return this;
  }


  @ApiModelProperty(required = true, value = "")
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
  public Item links(List<StacLink> links) {
    this.links = links;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("links")
  @NotNull

  public List<StacLink> getLinks() {
    return links;
  }
  public void setLinks(List<StacLink> links) {
    this.links = links;
  }

  /**
   **/
  public Item collection(String collection) {
    this.collection = collection;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("collection")

  public String getCollection() {
    return collection;
  }
  public void setCollection(String collection) {
    this.collection = collection;
  }

  /**
   **/
  public Item properties(Map<String, Object> properties) {
    this.setProperties(properties);
    return this;
  }

  /**
   **/
  public Item assets(Map<String, Object> assets) {
    this.assets = assets;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("assets")
  @NotNull

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
    Item item = (Item) o;
    return Objects.equals(stacVersion, item.stacVersion) &&
        Objects.equals(stacExtensions, item.stacExtensions) &&
        Objects.equals(super.getId(), item.getId()) &&
        Objects.equals(super.getBbox(), item.getBbox()) &&
        Objects.equals(super.getGeometry(), item.getGeometry()) &&
//        Objects.equals(type, item.type) &&
        Objects.equals(links, item.links) &&
        Objects.equals(collection, item.collection) &&
        Objects.equals(super.getProperties(), item.getProperties()) &&
        Objects.equals(assets, item.assets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stacVersion, stacExtensions, getId(), getBbox(), getGeometry()/*, type*/, links, collection, getProperties(), assets);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Item {\n");
    
    sb.append("    stacVersion: ").append(toIndentedString(stacVersion)).append("\n");
    sb.append("    stacExtensions: ").append(toIndentedString(stacExtensions)).append("\n");
    sb.append("    id: ").append(toIndentedString(getId())).append("\n");
    sb.append("    bbox: ").append(toIndentedString(getBbox())).append("\n");
    sb.append("    geometry: ").append(toIndentedString(getGeometry())).append("\n");
//    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    collection: ").append(toIndentedString(collection)).append("\n");
    sb.append("    properties: ").append(toIndentedString(getProperties())).append("\n");
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
