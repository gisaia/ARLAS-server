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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;


public class Catalog {

  protected @Valid String stacVersion = null;

  protected @Valid List<String> stacExtensions = null;

  private @Valid String type = "Catalog";

  protected @Valid String id = null;

  protected @Valid String title = null;

  protected @Valid String description = null;

  protected @Valid List<StacLink> links = null;

  /**
   **/
  public Catalog stacVersion(String stacVersion) {
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
  public Catalog stacExtensions(List<String> stacExtensions) {
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
  public Catalog type(String type) {
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
  public Catalog id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("id")
  @NotNull

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public Catalog title(String title) {
    this.title = title;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("title")

  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   **/
  public Catalog description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("description")
  @NotNull

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public Catalog links(List<StacLink> links) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Catalog catalog = (Catalog) o;
    return Objects.equals(stacVersion, catalog.stacVersion) &&
        Objects.equals(stacExtensions, catalog.stacExtensions) &&
        Objects.equals(type, catalog.type) &&
        Objects.equals(id, catalog.id) &&
        Objects.equals(title, catalog.title) &&
        Objects.equals(description, catalog.description) &&
        Objects.equals(links, catalog.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stacVersion, stacExtensions, type, id, title, description, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Catalog {\n");
    
    sb.append("    stacVersion: ").append(toIndentedString(stacVersion)).append("\n");
    sb.append("    stacExtensions: ").append(toIndentedString(stacExtensions)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
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
