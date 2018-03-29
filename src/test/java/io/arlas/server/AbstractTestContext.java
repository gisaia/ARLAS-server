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

public abstract class AbstractTestContext {
    static Logger LOGGER = LoggerFactory.getLogger(AbstractTestContext.class);

    protected static String arlasPath;
    
    public AbstractTestContext() {
    }

    static {
        String arlasHost = Optional.ofNullable(System.getenv("ARLAS_HOST")).orElse("localhost");
        int arlasPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_PORT")).orElse("9999"));
        RestAssured.baseURI = "http://"+arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = "";
        LOGGER.info(arlasHost+":"+arlasPort);
        String arlasPrefix = Optional.ofNullable(System.getenv("ARLAS_PREFIX")).orElse("/arlas");
        String arlasAppPath = Optional.ofNullable(System.getenv("ARLAS_APP_PATH")).orElse("/");
        if(arlasAppPath.endsWith("/"))
            arlasAppPath = arlasAppPath.substring(0,arlasAppPath.length()-1);
        arlasPath = arlasAppPath + arlasPrefix;
        if(arlasAppPath.endsWith("//"))
            arlasPath = arlasPath.substring(0,arlasPath.length()-1);
        if(!arlasAppPath.endsWith("/"))
            arlasPath = arlasPath + "/";

    }

    protected abstract String getUrlPath(String collection);
}