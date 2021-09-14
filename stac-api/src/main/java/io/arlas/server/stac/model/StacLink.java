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
import io.arlas.server.core.model.Link;

import java.util.Map;


public class StacLink extends Link {

  @JsonProperty(value = "rel", required = true)
  public String rel = null;

  @JsonProperty(value = "type")
  public String type = null;

  @JsonProperty(value = "title")
  public String title = null;

  @JsonProperty(value = "headers")
  public Map headers = null;

  @JsonProperty(value = "merge")
  private Boolean merge = null;

  /**
   * The location of the resource
   **/
  public StacLink href(String href) {
    this.href = href;
    return this;
  }

  /**
   * Relation type of the link
   **/
  public StacLink rel(String rel) {
    this.rel = rel;
    return this;
  }

  /**
   * The media type of the resource
   **/
  public StacLink type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Title of the resource
   **/
  public StacLink title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Specifies the HTTP method that the resource expects
   **/
  public StacLink method(String method) {
    this.method = method;
    return this;
  }

  /**
   * Object key values pairs they map to headers
   **/
  public StacLink headers(Map headers) {
    this.headers = headers;
    return this;
  }
  /**
   * For POST requests, the resource can specify the HTTP body as a JSON object.
   **/
  public StacLink body(Object body) {
    this.body = body;
    return this;
  }

  /**
   * This is only valid when the server is responding to POST request.  If merge is true, the client is expected to merge the body value into the current request body before following the link. This avoids passing large post bodies back and forth when following links, particularly for navigating pages through the &#x60;POST /search&#x60; endpoint.  NOTE: To support form encoding it is expected that a client be able to merge in the key value pairs specified as JSON &#x60;{\&quot;next\&quot;: \&quot;token\&quot;}&#x60; will become &#x60;&amp;next&#x3D;token&#x60;.
   **/
  public StacLink merge(Boolean merge) {
    this.merge = merge;
    return this;
  }
}
