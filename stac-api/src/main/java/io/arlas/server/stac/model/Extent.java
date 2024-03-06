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
import java.util.Objects;

public class Extent   {

  private @Valid
  ExtentSpatial spatial = null;

  private @Valid
  ExtentTemporal temporal = null;

  /**
   **/
  public Extent spatial(ExtentSpatial spatial) {
    this.spatial = spatial;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("spatial")
  @NotNull

  public ExtentSpatial getSpatial() {
    return spatial;
  }
  public void setSpatial(ExtentSpatial spatial) {
    this.spatial = spatial;
  }

  /**
   **/
  public Extent temporal(ExtentTemporal temporal) {
    this.temporal = temporal;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("temporal")
  @NotNull

  public ExtentTemporal getTemporal() {
    return temporal;
  }
  public void setTemporal(ExtentTemporal temporal) {
    this.temporal = temporal;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Extent extent = (Extent) o;
    return Objects.equals(spatial, extent.spatial) &&
        Objects.equals(temporal, extent.temporal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spatial, temporal);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Extent {\n");
    
    sb.append("    spatial: ").append(toIndentedString(spatial)).append("\n");
    sb.append("    temporal: ").append(toIndentedString(temporal)).append("\n");
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
