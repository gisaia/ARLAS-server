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
import jakarta.validation.Valid;
import org.geojson.GeoJsonObject;

import java.util.List;
import java.util.Objects;

public class SearchBody  {
  private @Valid List<Double> bbox;

  private @Valid String datetime = null;

  private @Valid GeoJsonObject intersects = null;

  private @Valid List<String> collections = null;

  private @Valid List<String> ids = null;

  private @Valid Integer limit = 10;

  private @Valid Integer from = 0;

  private @Valid String sortBy = null;

  private @Valid String after = null;

  private @Valid String before = null;

  /**
   **/
  public SearchBody datetime(String datetime) {
    this.datetime = datetime;
    return this;
  }

  
  @Schema()
  @JsonProperty("datetime")

  public String getDatetime() {
    return datetime;
  }
  public void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  /**
   **/
  public SearchBody intersects(GeoJsonObject intersects) {
    this.intersects = intersects;
    return this;
  }

  
  @Schema()
  @JsonProperty("intersects")

  public GeoJsonObject getIntersects() {
    return intersects;
  }
  public void setIntersects(GeoJsonObject intersects) {
    this.intersects = intersects;
  }

  /**
   **/
  public SearchBody bbox(List<Double> bbox) {
    this.bbox = bbox;
    return this;
  }

  
  @Schema()
  @JsonProperty("bbox")

  public List<Double> getBbox() {
    return bbox;
  }
  public void setBbox(List<Double> bbox) {
    this.bbox = bbox;
  }

  /**
   **/
  public SearchBody collections(List<String> collections) {
    this.collections = collections;
    return this;
  }


  @Schema()
  @JsonProperty("collections")

  public List<String> getCollections() {
    return collections;
  }
  public void setCollections(List<String> collections) {
    this.collections = collections;
  }

  /**
   **/
  public SearchBody ids(List<String> ids) {
    this.ids = ids;
    return this;
  }

  
  @Schema()
  @JsonProperty("ids")

  public List<String> getIds() {
    return ids;
  }
  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  /**
   **/
  public SearchBody limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  
  @Schema()
  @JsonProperty("limit")

  public Integer getLimit() {
    return limit;
  }
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  /**
   **/
  public SearchBody from(Integer from) {
    this.from = from;
    return this;
  }


  @Schema()
  @JsonProperty("from")

  public Integer getFrom() { return from; }
  public void setFrom(Integer from) { this.from = from; }

  /**
   **/
  public SearchBody sortBy(String sortBy) {
    this.sortBy = sortBy;
    return this;
  }


  @Schema()
  @JsonProperty("sortby")

  public String getSortBy() {
    return sortBy;
  }
  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  /**
   **/
  public SearchBody after(String after) {
    this.after = after;
    return this;
  }


  @Schema()
  @JsonProperty("after")

  public String getAfter() {
    return after;
  }
  public void setAfter(String after) {
    this.after = after;
  }

  /**
   **/
  public SearchBody before(String before) {
    this.before = before;
    return this;
  }


  @Schema()
  @JsonProperty("before")

  public String getBefore() {
    return before;
  }
  public void setBefore(String before) {
    this.before = before;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchBody searchBodyPost = (SearchBody) o;
    return Objects.equals(datetime, searchBodyPost.datetime) &&
        Objects.equals(intersects, searchBodyPost.intersects) &&
        Objects.equals(collections, searchBodyPost.collections) &&
        Objects.equals(ids, searchBodyPost.ids) &&
        Objects.equals(sortBy, searchBodyPost.sortBy) &&
        Objects.equals(limit, searchBodyPost.limit) &&
        Objects.equals(from, searchBodyPost.from) &&
        Objects.equals(after, searchBodyPost.after) &&
        Objects.equals(before, searchBodyPost.before);
  }

  @Override
  public int hashCode() {
    return Objects.hash(datetime, intersects, collections, ids, limit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchBodyPost {\n");
    sb.append("    bbox: ").append(toIndentedString(bbox)).append("\n");
    sb.append("    datetime: ").append(toIndentedString(datetime)).append("\n");
    sb.append("    intersects: ").append(toIndentedString(intersects)).append("\n");
    sb.append("    collections: ").append(toIndentedString(collections)).append("\n");
    sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    sortBy: ").append(toIndentedString(sortBy)).append("\n");
    sb.append("    from: ").append(toIndentedString(from)).append("\n");
    sb.append("    after: ").append(toIndentedString(after)).append("\n");
    sb.append("    before: ").append(toIndentedString(before)).append("\n");
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
