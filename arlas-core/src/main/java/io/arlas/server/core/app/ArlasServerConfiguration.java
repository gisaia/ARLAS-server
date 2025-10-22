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

package io.arlas.server.core.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.commons.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ArlasServerConfiguration extends ArlasBaseConfiguration {
    Logger logger = LoggerFactory.getLogger(ArlasServerConfiguration.class);

    @JsonProperty("arlas-wfs")
    public WFSConfiguration wfsConfiguration;

    @JsonProperty("arlas-ogc")
    public OGCConfiguration ogcConfiguration;

    @JsonProperty("arlas-inspire")
    public InspireConfiguration inspireConfiguration;

    @JsonProperty("arlas-csw")
    public CSWConfiguration cswConfiguration;

    @JsonProperty("arlas-index")
    public String arlasIndex;

    @JsonProperty("arlas-base-uri")
    public String arlasBaseUri;

    @JsonProperty("arlas-cache-size")
    public int arlasCacheSize;

    @JsonProperty("arlas-rest-cache-timeout")
    public int arlasRestCacheTimeout;

    @JsonProperty("arlas-service-collections-enabled")
    public Boolean arlasServiceCollectionsEnabled;

    @JsonProperty("arlas-service-explore-enabled")
    public Boolean arlasServiceExploreEnabled;

    @JsonProperty("arlas-service-wfs-enabled")
    public Boolean arlasServiceWFSEnabled;

    @JsonProperty("arlas-service-opensearch-enabled")
    public Boolean arlasServiceOPENSEARCHEnabled;

    @JsonProperty("arlas-service-csw-enabled")
    public Boolean arlasServiceCSWEnabled;

    @JsonProperty("arlas-service-raster-tiles-enabled")
    public Boolean arlasServiceRasterTileEnabled;

    @JsonProperty("arlas_service_stac_enabled")
    public Boolean arlasServiceSTACEnabled;

    @JsonProperty("arlas_stac")
    public STACConfiguration stacConfiguration;

    @JsonProperty("collection-auto-discover")
    public CollectionAutoDiscoverConfiguration collectionAutoDiscoverConfiguration;

    public void check() throws ArlasConfigurationException {
        super.check();

        if (arlasBaseUri != null) {
            try {
                new URI(arlasBaseUri);
            } catch (URISyntaxException e) {
                throw new ArlasConfigurationException("The arlas-base-uri is invalid.");
            }
        }
        if (StringUtil.isNullOrEmpty(arlasIndex)) {
            arlasIndex = ".arlas";
        }
        if (arlasCacheSize < 0) {
            arlasCacheSize = 1000;
        }
        if (arlasCacheTimeout < 0) {
            arlasCacheTimeout = 60;
        }
        if (arlasServiceCollectionsEnabled == null) {
            arlasServiceCollectionsEnabled = true;
        }
        if (arlasServiceExploreEnabled == null) {
            arlasServiceExploreEnabled = true;
        }
        if (inspireConfiguration == null) {
            inspireConfiguration = new InspireConfiguration();
            inspireConfiguration.enabled = false;
            inspireConfiguration.publicAccessLimitations = "no limitations apply";
            inspireConfiguration.accessAndUseConditions = "no conditions apply";
            inspireConfiguration.servicesDateOfCreation = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            if (inspireConfiguration.servicesDateOfCreation == null ) {
                inspireConfiguration.servicesDateOfCreation = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            if (inspireConfiguration.enabled == null) {
                inspireConfiguration.enabled = false;
            }
            if (inspireConfiguration.publicAccessLimitations == null) {
                inspireConfiguration.publicAccessLimitations = "no limitations apply";
            }
            if (inspireConfiguration.accessAndUseConditions == null) {
                inspireConfiguration.accessAndUseConditions = "no conditions apply";
            }
        }
        if (arlasServiceWFSEnabled == null) {
            arlasServiceWFSEnabled = false;
        }
        if (arlasServiceOPENSEARCHEnabled == null) {
            arlasServiceOPENSEARCHEnabled = true;
        }
        if (arlasServiceCSWEnabled == null) {
            arlasServiceCSWEnabled = false;
        }
        if (arlasServiceRasterTileEnabled == null){
            arlasServiceRasterTileEnabled = false;
        }
        if (collectionAutoDiscoverConfiguration == null) {
            collectionAutoDiscoverConfiguration = new CollectionAutoDiscoverConfiguration();
            collectionAutoDiscoverConfiguration.schedule = 0;
        }

        if (arlasServiceSTACEnabled == null) {
            arlasServiceSTACEnabled = false;
        }
        if(!arlasServiceSTACEnabled && swaggerBundleConfiguration.getResourcePackage().contains("io.arlas.server.stac")){
            logger.warn("STAC service is disabled but STAC resources are present in Swagger configuration.");
        }
        if(arlasServiceSTACEnabled && !swaggerBundleConfiguration.getResourcePackage().contains("io.arlas.server.stac")){
            logger.warn("STAC service is enabled but STAC resources are missing in Swagger configuration.");
        }
    }
}
