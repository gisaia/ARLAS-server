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
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Provider {

  private @Valid String name = null;

  private @Valid String description = null;

public enum RolesEnum {

    PRODUCER(String.valueOf("producer")), LICENSOR(String.valueOf("licensor")), PROCESSOR(String.valueOf("processor")), HOST(String.valueOf("host"));


    private String value;

    RolesEnum (String v) {
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
    public static RolesEnum fromValue(String v) {
        for (RolesEnum b : RolesEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}
  private @Valid List<RolesEnum> roles = new ArrayList<>();

  private @Valid String url = null;

  /**
   * The name of the organization or the individual.
   **/
  public Provider name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The name of the organization or the individual.")
  @JsonProperty("name")
  @NotNull

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Multi-line description to add further provider information such as processing details for processors and producers, hosting details for hosts or basic contact information.  [CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.
   **/
  public Provider description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Multi-line description to add further provider information such as processing details for processors and producers, hosting details for hosts or basic contact information.  [CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.")
  @JsonProperty("description")

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Roles of the provider.  The provider&#x27;s role(s) can be one or more of the following elements:  * licensor: The organization that is licensing the dataset under   the license specified in the collection&#x27;s license field. * producer: The producer of the data is the provider that   initially captured and processed the source data, e.g. ESA for   Sentinel-2 data. * processor: A processor is any provider who processed data to a   derived product. * host: The host is the actual provider offering the data on their   storage. There should be no more than one host, specified as last   element of the list.
   **/
  public Provider roles(List<RolesEnum> roles) {
    this.roles = roles;
    return this;
  }

  
  @ApiModelProperty(value = "Roles of the provider.  The provider's role(s) can be one or more of the following elements:  * licensor: The organization that is licensing the dataset under   the license specified in the collection's license field. * producer: The producer of the data is the provider that   initially captured and processed the source data, e.g. ESA for   Sentinel-2 data. * processor: A processor is any provider who processed data to a   derived product. * host: The host is the actual provider offering the data on their   storage. There should be no more than one host, specified as last   element of the list.")
  @JsonProperty("roles")

  public List<RolesEnum> getRoles() {
    return roles;
  }
  public void setRoles(List<RolesEnum> roles) {
    this.roles = roles;
  }

  /**
   * Homepage on which the provider describes the dataset and publishes contact information.
   **/
  public Provider url(String url) {
    this.url = url;
    return this;
  }

  
  @ApiModelProperty(value = "Homepage on which the provider describes the dataset and publishes contact information.")
  @JsonProperty("url")

  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Provider provider = (Provider) o;
    return Objects.equals(name, provider.name) &&
        Objects.equals(description, provider.description) &&
        Objects.equals(roles, provider.roles) &&
        Objects.equals(url, provider.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, roles, url);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Provider {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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
