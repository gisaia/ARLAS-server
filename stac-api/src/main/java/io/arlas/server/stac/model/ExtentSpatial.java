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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtentSpatial   {

  private @Valid List<List<Double>> bbox = new ArrayList<>();

public enum CrsEnum {

    HTTP_WWW_OPENGIS_NET_DEF_CRS_OGC_1_3_CRS84(String.valueOf("http://www.opengis.net/def/crs/OGC/1.3/CRS84"));


    private String value;

    CrsEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CrsEnum fromValue(String v) {
        for (CrsEnum b : CrsEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}
  private @Valid CrsEnum crs = CrsEnum.HTTP_WWW_OPENGIS_NET_DEF_CRS_OGC_1_3_CRS84;

  /**
   * One or more bounding boxes that describe the spatial extent of the dataset.  The first bounding box describes the overall spatial extent of the data. All subsequent bounding boxes describe  more precise bounding boxes, e.g., to identify clusters of data. Clients only interested in the overall spatial extent will only need to access the first item in each array.
   **/
  public ExtentSpatial bbox(List<List<Double>> bbox) {
    this.bbox = bbox;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "One or more bounding boxes that describe the spatial extent of the dataset.  The first bounding box describes the overall spatial extent of the data. All subsequent bounding boxes describe  more precise bounding boxes, e.g., to identify clusters of data. Clients only interested in the overall spatial extent will only need to access the first item in each array.")
  @JsonProperty("bbox")
  @NotNull
 @Size(min=1)
  public List<List<Double>> getBbox() {
    return bbox;
  }
  public void setBbox(List<List<Double>> bbox) {
    this.bbox = bbox;
  }

  /**
   * Coordinate reference system of the coordinates in the spatial extent (property &#x60;bbox&#x60;). The default reference system is WGS 84 longitude/latitude. In the Core this is the only supported coordinate reference system. Extensions may support additional coordinate reference systems and add additional enum values.
   **/
  public ExtentSpatial crs(CrsEnum crs) {
    this.crs = crs;
    return this;
  }

  
  @Schema(description = "Coordinate reference system of the coordinates in the spatial extent (property `bbox`). The default reference system is WGS 84 longitude/latitude. In the Core this is the only supported coordinate reference system. Extensions may support additional coordinate reference systems and add additional enum values.")
  @JsonProperty("crs")

  public CrsEnum getCrs() {
    return crs;
  }
  public void setCrs(CrsEnum crs) {
    this.crs = crs;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExtentSpatial extentSpatial = (ExtentSpatial) o;
    return Objects.equals(bbox, extentSpatial.bbox) &&
        Objects.equals(crs, extentSpatial.crs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bbox, crs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExtentSpatial {\n");
    
    sb.append("    bbox: ").append(toIndentedString(bbox)).append("\n");
    sb.append("    crs: ").append(toIndentedString(crs)).append("\n");
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
