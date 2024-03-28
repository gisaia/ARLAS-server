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

public class ExtentTemporal   {

  private @Valid List<List<String>> interval = new ArrayList<>();

public enum TrsEnum {

    HTTP_WWW_OPENGIS_NET_DEF_UOM_ISO_8601_0_GREGORIAN(String.valueOf("http://www.opengis.net/def/uom/ISO-8601/0/Gregorian"));


    private String value;

    TrsEnum (String v) {
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
    public static TrsEnum fromValue(String v) {
        for (TrsEnum b : TrsEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}
  private @Valid TrsEnum trs = TrsEnum.HTTP_WWW_OPENGIS_NET_DEF_UOM_ISO_8601_0_GREGORIAN;

  /**
   * One or more time intervals that describe the temporal extent of the dataset.  The first time interval describes the overall temporal extent of the data. All subsequent time intervals describe  more precise time intervals, e.g., to identify clusters of data. Clients only interested in the overall extent will only need to access the first item in each array.
   **/
  public ExtentTemporal interval(List<List<String>> interval) {
    this.interval = interval;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "One or more time intervals that describe the temporal extent of the dataset.  The first time interval describes the overall temporal extent of the data. All subsequent time intervals describe  more precise time intervals, e.g., to identify clusters of data. Clients only interested in the overall extent will only need to access the first item in each array.")
  @JsonProperty("interval")
  @NotNull
 @Size(min=1)
  public List<List<String>> getInterval() {
    return interval;
  }
  public void setInterval(List<List<String>> interval) {
    this.interval = interval;
  }

  /**
   * Coordinate reference system of the coordinates in the temporal extent (property &#x60;interval&#x60;). The default reference system is the Gregorian calendar. In the Core this is the only supported temporal reference system. Extensions may support additional temporal reference systems and add additional enum values.
   **/
  public ExtentTemporal trs(TrsEnum trs) {
    this.trs = trs;
    return this;
  }

  
  @Schema(description = "Coordinate reference system of the coordinates in the temporal extent (property `interval`). The default reference system is the Gregorian calendar. In the Core this is the only supported temporal reference system. Extensions may support additional temporal reference systems and add additional enum values.")
  @JsonProperty("trs")

  public TrsEnum getTrs() {
    return trs;
  }
  public void setTrs(TrsEnum trs) {
    this.trs = trs;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExtentTemporal extentTemporal = (ExtentTemporal) o;
    return Objects.equals(interval, extentTemporal.interval) &&
        Objects.equals(trs, extentTemporal.trs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interval, trs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExtentTemporal {\n");
    
    sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
    sb.append("    trs: ").append(toIndentedString(trs)).append("\n");
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
