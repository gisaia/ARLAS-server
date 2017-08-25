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

package io.arlas.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;

import java.util.Optional;

public abstract class AbstractTest {
    static Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    
    protected String arlasPrefix;
    
    public AbstractTest() {
        arlasPrefix = Optional.ofNullable(System.getenv("ARLAS_PREFIX")).orElse("/arlas/");
    }

    static {
        String arlasHost = Optional.ofNullable(System.getenv("ARLAS_HOST")).orElse("localhost");
        int arlasPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_PORT")).orElse("9999"));
        RestAssured.baseURI = "http://"+arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = "";
        LOGGER.info(arlasHost+":"+arlasPort);
    }

    protected abstract String getUrlPath(String collection);
}