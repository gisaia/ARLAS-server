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

import com.ethlo.time.ITU;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.arlas.server.core.model.response.ArlasHit;
import io.swagger.v3.oas.annotations.media.Schema;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
// To override type=Class name in GeoJsonObject parent class
@JsonTypeInfo(
        property = "type",
        use = JsonTypeInfo.Id.NONE
)
public class Item extends Feature {

  private @Valid String stacVersion = null;

  private @Valid List<String> stacExtensions = null;

  private @Valid String type = "Feature";

  private @Valid List<StacLink> links = null;

  private @Valid String collection = null;

  private @Valid Map<String, Object> assets = null;

  private @Valid String catalog = null;

  private @Valid  ArrayList<Double> centroid = null;

  /**
   **/
  public Item stacVersion(String stacVersion) {
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
  public Item stacExtensions(List<String> stacExtensions) {
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
  public Item links(List<StacLink> links) {
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
   **/
  public Item catalog(String catalog) {
    this.catalog = catalog;
    return this;
  }

  @Schema()
  @JsonProperty("catalog")

  public String getCatalog() {
    return catalog;
  }
  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }


  /**
   **/
  public Item centroid(ArrayList<Double> centroid) {
    this.centroid = centroid;
    return this;
  }

  @Schema()
  @JsonProperty("centroid")

  public ArrayList<Double> getCentroid() {
    return centroid;
  }
  public void setCentroid(ArrayList<Double> centroid) {
    this.centroid = centroid;
  }

  /**
   **/
  public Item collection(String collection) {
    this.collection = collection;
    return this;
  }

  
  @Schema()
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

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("assets")
  @NotNull

  public Map<String, Object> getAssets() {
    return assets;
  }
  public void setAssets(Map<String, Object> assets) {
    this.assets = assets;
  }


  public Item itemStacModel(ArlasHit hit, String collection, double[] bbox ){
    Map<String, Object> data = hit.getDataAsMap();
    Map<String, Object> properties =  (Map<String, Object>) data.get("properties");
    Arrays.asList("datetime", "start_datetime", "end_datetime").forEach(date -> {
      Object datetime = properties.get(date);
      if(datetime instanceof Integer){
        properties.put(date, ITU.formatUtc(OffsetDateTime.ofInstant(Instant.ofEpochSecond((int) datetime), ZoneOffset.UTC)));
      }
    });
    return this.type("Feature")
            .collection(collection)
            .catalog((String) data.get("catalog"))
            .id((String) data.get("id"))
            .geometry(hit.md.geometry)
            .bbox(bbox)
            .centroid((ArrayList<Double>) data.get("centroid"))
            .assets(computeStacProperties((Map<String, Object>) data.get("assets")))
            .properties(computeStacProperties((Map<String, Object>) data.get("properties")));
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

  private String computeStacPropertyName(String propertyName){
    return propertyName.replaceFirst("__",":");
  }

  private  Map<String, Object> computeStacProperties(Map<String, Object> stacProperties) {
    Map<String, Object> modified = new LinkedHashMap<>();

    for (Map.Entry<String, Object> entry : stacProperties.entrySet()) {
      String computedKey = computeStacPropertyName(entry.getKey());
      Object value = entry.getValue();

      if (value instanceof Map) {
        value = computeStacProperties((Map<String, Object>) value);
      }

      modified.put(computedKey, value);
    }

    return modified;
  }


}
