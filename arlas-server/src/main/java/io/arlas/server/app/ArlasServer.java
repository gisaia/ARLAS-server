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

package io.arlas.server.app;

import brave.http.HttpTracing;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.server.auth.AuthenticationFilter;
import io.arlas.server.auth.AuthorizationFilter;
import io.arlas.server.exceptions.ArlasExceptionMapper;
import io.arlas.server.exceptions.ConstraintViolationExceptionMapper;
import io.arlas.server.exceptions.IllegalArgumentExceptionMapper;
import io.arlas.server.exceptions.JsonProcessingExceptionMapper;
import io.arlas.server.impl.elastic.exceptions.ElasticsearchExceptionMapper;
import io.arlas.server.managers.CollectionReferenceManager;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.CSWService;
import io.arlas.server.ogc.csw.writer.getrecords.AtomGetRecordsMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.getrecords.XmlGetRecordsMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.record.AtomRecordMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.record.XmlMDMetadataMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.record.XmlRecordMessageBodyBuilder;
import io.arlas.server.ogc.wfs.WFSHandler;
import io.arlas.server.ogc.wfs.WFSService;
import io.arlas.server.rest.collections.CollectionService;
import io.arlas.server.rest.explore.aggregate.AggregateRESTService;
import io.arlas.server.rest.explore.aggregate.GeoAggregateRESTService;
import io.arlas.server.rest.explore.compute.ComputeRESTService;
import io.arlas.server.rest.explore.count.CountRESTService;
import io.arlas.server.rest.explore.describe.DescribeCollectionRESTService;
import io.arlas.server.rest.explore.describe.DescribeRESTService;
import io.arlas.server.rest.explore.opensearch.AtomHitsMessageBodyWriter;
import io.arlas.server.rest.explore.opensearch.OpenSearchDescriptorService;
import io.arlas.server.rest.explore.raw.RawRESTService;
import io.arlas.server.rest.explore.search.GeoSearchRESTService;
import io.arlas.server.rest.explore.search.SearchRESTService;
import io.arlas.server.rest.explore.suggest.SuggestRESTService;
import io.arlas.server.rest.plugins.eo.TileRESTService;
import io.arlas.server.services.ExploreService;
import io.arlas.server.task.CollectionAutoDiscover;
import io.arlas.server.utils.PrettyPrintFilter;
import io.arlas.server.wfs.requestfilter.InsensitiveCaseFilter;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.lifecycle.setup.ScheduledExecutorServiceBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.apache.http.client.methods.HttpHead;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.core.HttpHeaders;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ArlasServer extends Application<ArlasServerConfiguration> {
    Logger LOGGER = LoggerFactory.getLogger(ArlasServer.class);

    public static void main(String... args) throws Exception {
        new ArlasServer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasServerConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
        bootstrap.addBundle(new SwaggerBundle<ArlasServerConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ArlasServerConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
        bootstrap.addBundle(new ZipkinBundle<ArlasServerConfiguration>(getName()) {
            @Override
            public ZipkinFactory getZipkinFactory(ArlasServerConfiguration configuration) {
                return configuration.zipkinConfiguration;
            }
        });
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    @Override
    public void run(ArlasServerConfiguration configuration, Environment environment) throws Exception {
        
        configuration.check();
        LOGGER.info("Checked configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));

        DatabaseToolsFactory dbToolFactory = (DatabaseToolsFactory) Class
                .forName(configuration.arlasDatabaseFactoryClass)
                .getConstructor(ArlasServerConfiguration.class)
                .newInstance(configuration);

        CollectionReferenceManager.getInstance().init(dbToolFactory.getCollectionReferenceDao());

        if (configuration.zipkinConfiguration != null) {
            Optional<HttpTracing> tracing = configuration.zipkinConfiguration.build(environment);
        }

        ExploreService exploration = dbToolFactory.getExploreService();
        environment.getObjectMapper().setSerializationInclusion(Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(new ArlasExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());
        environment.jersey().register(new ElasticsearchExceptionMapper());
        environment.jersey().register(new AtomHitsMessageBodyWriter(exploration));
        environment.jersey().register(new AtomGetRecordsMessageBodyWriter(configuration));
        environment.jersey().register(new XmlGetRecordsMessageBodyWriter());
        environment.jersey().register(new XmlMDMetadataMessageBodyWriter());
        environment.jersey().register(new XmlRecordMessageBodyBuilder());
        environment.jersey().register(new AtomRecordMessageBodyWriter());

        if (configuration.arlasServiceExploreEnabled) {
            environment.jersey().register(new CountRESTService(exploration));
            environment.jersey().register(new SearchRESTService(exploration));
            environment.jersey().register(new AggregateRESTService(exploration));
            environment.jersey().register(new GeoSearchRESTService(exploration));
            environment.jersey().register(new GeoAggregateRESTService(exploration));
            environment.jersey().register(new SuggestRESTService(exploration));
            environment.jersey().register(new DescribeRESTService(exploration));
            environment.jersey().register(new RawRESTService(exploration));
            environment.jersey().register(new DescribeCollectionRESTService(exploration));
            environment.jersey().register(new ComputeRESTService(exploration));
            LOGGER.info("Explore API enabled");
        } else {
            LOGGER.info("Explore API disabled");
        }

        // Auth
        if (configuration.arlasAuthConfiguration.enabled) {
            environment.jersey().register(new AuthenticationFilter(configuration.arlasAuthConfiguration));
            environment.jersey().register(new AuthorizationFilter(configuration.arlasAuthConfiguration));
        }

        if(configuration.arlasServiceCollectionsEnabled) {
            LOGGER.info("Collection API enabled");
            environment.jersey().register(new CollectionService(configuration, dbToolFactory.getCollectionReferenceDao()));
        } else {
            LOGGER.info("Collection API disabled");
        }

        if(configuration.arlasServiceWFSEnabled){
            LOGGER.info("WFS Service enabled");
            WFSHandler wfsHandler = new WFSHandler(configuration.wfsConfiguration, configuration.ogcConfiguration, configuration.inspireConfiguration, configuration.arlasBaseUri);
            environment.jersey().register(new WFSService(dbToolFactory.getCollectionReferenceDao(), dbToolFactory.getWFSToolService(), wfsHandler));
        } else {
            LOGGER.info("WFS Service disabled");
        }

        if(configuration.arlasServiceOPENSEARCHEnabled){
            LOGGER.info("OPENSEARCH Service enabled");
            OpensearchConfiguration opensearchConfiguration = configuration.opensearchConfiguration;
            environment.jersey().register(new OpenSearchDescriptorService(exploration, opensearchConfiguration));
        } else {
            LOGGER.info("OPENSEARCH Service disabled");
        }

        if (configuration.arlasServiceCSWEnabled) {
            LOGGER.info("CSW Service enabled");
            CSWHandler cswHandler = new CSWHandler(configuration.ogcConfiguration, configuration.cswConfiguration, configuration.inspireConfiguration, configuration.arlasBaseUri);
            environment.jersey().register(new CSWService(dbToolFactory.getCollectionReferenceDao(), dbToolFactory.getOGCCollectionReferenceDao(), cswHandler, configuration));
        } else {
            LOGGER.info("CSW Service disabled");
        }

        if(configuration.arlasServiceRasterTileEnabled){
            LOGGER.info("Raster Tile Service enabled");
            environment.jersey().register(new TileRESTService(exploration));
        }else{
            LOGGER.info("Raster Tile Service disabled");
        }

        //filters
        environment.jersey().register(PrettyPrintFilter.class);
        environment.jersey().register(InsensitiveCaseFilter.class);

        //tasks
        environment.admin().addTask(new CollectionAutoDiscover(dbToolFactory.getCollectionReferenceDao(), configuration));
        int scheduleAutoDiscover = configuration.collectionAutoDiscoverConfiguration.schedule;
        if (scheduleAutoDiscover > 0) {
            String nameFormat = "collection-auto-discover-%d";
            ScheduledExecutorServiceBuilder sesBuilder = environment.lifecycle().scheduledExecutorService(nameFormat);
            ScheduledExecutorService ses = sesBuilder.build();
            Runnable autoDiscoverTask = new CollectionAutoDiscover(dbToolFactory.getCollectionReferenceDao(), configuration);
            ses.scheduleWithFixedDelay(autoDiscoverTask, 10, scheduleAutoDiscover, TimeUnit.SECONDS);
        }

        //healthchecks
        dbToolFactory.getHealthChecks().forEach((name, check) -> environment.healthChecks().register(name, check));

        //cors
        if (configuration.arlarsCorsConfiguration.enabled) {
            configureCors(environment,configuration.arlarsCorsConfiguration);
        }else{
            CrossOriginFilter filter = new CrossOriginFilter();
            final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CrossOriginFilter", filter);
            // Expose always HttpHeaders.WWW_AUTHENTICATE to authentify on client side a non public uri call
            cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, HttpHeaders.WWW_AUTHENTICATE);
        }
    }

    private void configureCors(Environment environment, ArlasCorsConfiguration configuration) {
        CrossOriginFilter filter = new CrossOriginFilter();
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CrossOriginFilter", filter);
        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, configuration.allowedOrigins);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, configuration.allowedHeaders);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, configuration.allowedMethods);
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, String.valueOf(configuration.allowedCredentials));
        String exposedHeader = configuration.exposedHeaders;
        // Expose always HttpHeaders.WWW_AUTHENTICATE to authentify on client side a non public uri call
        if(configuration.exposedHeaders.indexOf(HttpHeaders.WWW_AUTHENTICATE)<0){
             exposedHeader = configuration.exposedHeaders.concat(",").concat(HttpHeaders.WWW_AUTHENTICATE);
        }
        cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, exposedHeader);

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
