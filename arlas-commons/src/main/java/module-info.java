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

import io.arlas.filter.impl.NoPolicyEnforcer;
import io.arlas.filter.core.PolicyEnforcer;
import io.arlas.filter.impl.Auth0PolicyEnforcer;
import io.arlas.filter.impl.HTTPPolicyEnforcer;
import io.arlas.filter.impl.KeycloakPolicyEnforcer;

module arlas.commons {
    exports io.arlas.commons.cache;
    exports io.arlas.commons.config;
    exports io.arlas.commons.exceptions;
    exports io.arlas.commons.rest.response;
    exports io.arlas.commons.rest.utils;
    exports io.arlas.commons.utils;
    exports io.arlas.filter.impl;
    exports io.arlas.filter.config;
    exports io.arlas.filter.core;

    uses PolicyEnforcer;
    provides PolicyEnforcer with NoPolicyEnforcer, HTTPPolicyEnforcer, Auth0PolicyEnforcer, KeycloakPolicyEnforcer;

    requires co.elastic.apm.api;
    requires com.auth0.jwt;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.jaxrs.base;
    requires com.hazelcast.core;
    requires io.dropwizard.core;
    requires io.dropwizard.jackson;
    requires dropwizard.swagger;
    requires java.annotation;
    requires java.validation;
    requires java.ws.rs;
    requires keycloak.authz.client;
    requires keycloak.core;
    requires org.slf4j;
    requires java.servlet;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
}