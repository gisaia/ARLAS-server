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
import java.util.Objects;


public class CollectionList {

  private @Valid List<StacLink> links = null;

  private @Valid List<Collection> collections = new ArrayList<>();

  /**
   **/
  public CollectionList links(List<StacLink> links) {
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
  public CollectionList collections(List<Collection> collections) {
    this.collections = collections;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("collections")
  @NotNull

  public List<Collection> getCollections() {
    return collections;
  }
  public void setCollections(List<Collection> collections) {
    this.collections = collections;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CollectionList collections = (CollectionList) o;
    return Objects.equals(links, collections.links) &&
        Objects.equals(collections, collections.collections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, collections);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Collections {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    collections: ").append(toIndentedString(collections)).append("\n");
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
