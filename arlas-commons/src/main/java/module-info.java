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

import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.commons.rest.auth.PolicyEnforcer;

module arlas.commons {
    exports io.arlas.commons.config;
    exports io.arlas.commons.exceptions;
    exports io.arlas.commons.rest.auth;
    exports io.arlas.commons.rest.response;
    exports io.arlas.commons.rest.utils;
    uses PolicyEnforcer;
    uses ArlasAuthConfiguration;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.jaxrs.base;
    requires dropwizard.core;
    requires dropwizard.jackson;
    requires dropwizard.swagger;
    requires java.annotation;
    requires java.validation;
    requires java.ws.rs;
    requires org.slf4j;
    requires zipkin.core;
}