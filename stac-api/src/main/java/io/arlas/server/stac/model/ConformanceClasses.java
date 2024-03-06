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


public class ConformanceClasses {

  private @Valid List<String> conformsTo = new ArrayList<>();

  /**
   * A list of all conformance classes implemented by the server. In addition to the STAC-specific conformance classes, all OGC-related conformance classes listed at &#x60;GET /conformances&#x60; must be listed here. This entry should mirror what &#x60;GET /conformances&#x60; returns, if implemented.
   **/
  public ConformanceClasses conformsTo(List<String> conformsTo) {
    this.conformsTo = conformsTo;
    return this;
  }

  
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "A list of all conformance classes implemented by the server. In addition to the STAC-specific conformance classes, all OGC-related conformance classes listed at `GET /conformances` must be listed here. This entry should mirror what `GET /conformances` returns, if implemented.")
  @JsonProperty("conformsTo")
  @NotNull

  public List<String> getConformsTo() {
    return conformsTo;
  }
  public void setConformsTo(List<String> conformsTo) {
    this.conformsTo = conformsTo;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConformanceClasses conformanceClasses = (ConformanceClasses) o;
    return Objects.equals(conformsTo, conformanceClasses.conformsTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conformsTo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConformanceClasses {\n");
    
    sb.append("    conformsTo: ").append(toIndentedString(conformsTo)).append("\n");
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
