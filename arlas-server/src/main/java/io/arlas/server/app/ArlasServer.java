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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kristofa.brave.Brave;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.server.exceptions.*;
import io.arlas.server.health.ElasticsearchHealthCheck;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.ElasticCSWService;
import io.arlas.server.ogc.csw.writer.getrecords.AtomGetRecordsMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.getrecords.XmlGetRecordsMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.record.AtomRecordMessageBodyWriter;
import io.arlas.server.ogc.csw.writer.record.XmlRecordMessageBodyWriter;
import io.arlas.server.ogc.wfs.WFSHandler;
import io.arlas.server.ogc.wfs.WFSService;
import io.arlas.server.utils.PrettyPrintFilter;
import io.arlas.server.rest.collections.ElasticCollectionService;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.rest.explore.aggregate.AggregateRESTService;
import io.arlas.server.rest.explore.aggregate.GeoAggregateRESTService;
import io.arlas.server.rest.explore.count.CountRESTService;
import io.arlas.server.rest.explore.describe.DescribeCollectionRESTService;
import io.arlas.server.rest.explore.describe.DescribeRESTService;
import io.arlas.server.rest.explore.opensearch.AtomHitsMessageBodyWriter;
import io.arlas.server.rest.explore.opensearch.OpenSearchDescriptorService;
import io.arlas.server.rest.explore.range.RangeRESTService;
import io.arlas.server.rest.explore.raw.RawRESTService;
import io.arlas.server.rest.explore.search.GeoSearchRESTService;
import io.arlas.server.rest.explore.search.SearchRESTService;
import io.arlas.server.rest.explore.suggest.SuggestRESTService;
import io.arlas.server.rest.tag.TagRESTService;
import io.arlas.server.services.UpdateServices;
import io.arlas.server.task.CollectionAutoDiscover;
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
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.net.InetAddress;
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

        bootstrap.addBundle(new AssetsBundle("/src/main/resources/assets/", "/", "index.html"));
    }

    @Override
    public void run(ArlasServerConfiguration configuration, Environment environment) throws Exception {
        LOGGER.info("Raw configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));
        configuration.check();
        LOGGER.info("Checked configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));

        Settings.Builder settingsBuilder = Settings.builder();
        if(configuration.elasticsniffing) {
            settingsBuilder.put("client.transport.sniff", true);
        }
        if(!Strings.isNullOrEmpty(configuration.elasticcluster)) {
            settingsBuilder.put("cluster.name", configuration.elasticcluster);
        }
        Settings settings = settingsBuilder.build();

        PreBuiltTransportClient transportClient = new PreBuiltTransportClient(settings);
        for(Pair<String,Integer> node : configuration.getElasticNodes()) {
            transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(node.getLeft()),
                    node.getRight()));
        }
        Client client = transportClient;

        if (configuration.zipkinConfiguration != null) {
            Optional<Brave> brave = configuration.zipkinConfiguration.build(environment);
        }

        ExploreServices exploration = new ExploreServices(client, configuration);
        UpdateServices updateServices = new UpdateServices(client, configuration);
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
        environment.jersey().register(new XmlRecordMessageBodyWriter());
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
            environment.jersey().register(new RangeRESTService(exploration));
            LOGGER.info("Explore API enabled");
        } else {
            LOGGER.info("Explore API disabled");
        }

        if(configuration.arlasServiceCollectionsEnabled) {
            LOGGER.info("Collection API enabled");
            environment.jersey().register(new ElasticCollectionService(client, configuration));
        } else {
            LOGGER.info("Collection API disabled");
        }

        if(configuration.arlasServiceWFSEnabled){
            LOGGER.info("WFS Service enabled");
            WFSHandler wfsHandler = new WFSHandler(configuration.wfsConfiguration, configuration.ogcConfiguration);
            environment.jersey().register(new WFSService(exploration, wfsHandler));
        } else {
            LOGGER.info("WFS Service disabled");
        }

        if(configuration.arlasServiceOPENSEARCHEnabled){
            LOGGER.info("OPENSEARCH Service enabled");
            environment.jersey().register(new OpenSearchDescriptorService(exploration));
        } else {
            LOGGER.info("OPENSEARCH Service disabled");
        }

        if (configuration.arlasServiceCSWEnabled) {
            LOGGER.info("CSW Service enabled");
            CSWHandler cswHandler = new CSWHandler(configuration.ogcConfiguration,configuration.cswConfiguration);
            environment.jersey().register(new ElasticCSWService(cswHandler,client,configuration));
        } else {
            LOGGER.info("CSW Service disabled");
        }

        if(configuration.arlasServiceTagEnabled){
            LOGGER.info("Tag Service enabled");
            environment.jersey().register(new TagRESTService(updateServices));
        }else{
            LOGGER.info("Tag Service disabled");
        }
        //filters
        environment.jersey().register(PrettyPrintFilter.class);
        environment.jersey().register(InsensitiveCaseFilter.class);

        //tasks
        environment.admin().addTask(new CollectionAutoDiscover(client, configuration));
        int scheduleAutoDiscover = configuration.collectionAutoDiscoverConfiguration.schedule;
        if (scheduleAutoDiscover > 0) {
            String nameFormat = "collection-auto-discover-%d";
            ScheduledExecutorServiceBuilder sesBuilder = environment.lifecycle().scheduledExecutorService(nameFormat);
            ScheduledExecutorService ses = sesBuilder.build();
            Runnable autoDiscoverTask = new CollectionAutoDiscover(client, configuration);
            ses.scheduleWithFixedDelay(autoDiscoverTask, 10, scheduleAutoDiscover, TimeUnit.SECONDS);
        }

        //healthchecks
        environment.healthChecks().register("elasticsearch", new ElasticsearchHealthCheck(client));

        //cors
        if (configuration.arlascorsenabled) {
            configureCors(environment);
        }
    }

    private void configureCors(Environment environment) {
        CrossOriginFilter filter = new CrossOriginFilter();
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CrossOriginFilter", filter);

        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        //cors.setInitParameter(CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM, "");
        cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
