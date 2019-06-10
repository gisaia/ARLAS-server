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
import io.arlas.server.exceptions.*;
import io.arlas.server.health.ElasticsearchHealthCheck;
import io.arlas.server.kafka.TagKafkaProducer;
import io.arlas.server.rest.collections.ElasticCollectionService;
import io.arlas.server.rest.tag.TagRESTService;
import io.arlas.server.rest.tag.TagStatusRESTService;
import io.arlas.server.service.ManagedKafkaConsumers;
import io.arlas.server.services.UpdateServices;
import io.arlas.server.utils.PrettyPrintFilter;
import io.arlas.server.wfs.requestfilter.InsensitiveCaseFilter;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
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

public class ArlasTagger extends Application<ArlasServerConfiguration> {
    Logger LOGGER = LoggerFactory.getLogger(ArlasTagger.class);

    public static void main(String... args) throws Exception {
        new ArlasTagger().run(args);
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
            Optional<HttpTracing> tracing = configuration.zipkinConfiguration.build(environment);
        }

        UpdateServices updateServices = new UpdateServices(client, configuration);
        TagKafkaProducer tagKafkaProducer = TagKafkaProducer.build(configuration);
        ManagedKafkaConsumers consumersManager = new ManagedKafkaConsumers(configuration, tagKafkaProducer, updateServices);
        environment.lifecycle().manage(consumersManager);

        environment.getObjectMapper().setSerializationInclusion(Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(new ArlasExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());
        environment.jersey().register(new ElasticsearchExceptionMapper());
        environment.jersey().register(new TagRESTService(tagKafkaProducer));
        environment.jersey().register(new TagStatusRESTService());

        environment.jersey().register(new ElasticCollectionService(client, configuration));

        //filters
        environment.jersey().register(PrettyPrintFilter.class);
        environment.jersey().register(InsensitiveCaseFilter.class);

        //healthchecks
        environment.healthChecks().register("elasticsearch", new ElasticsearchHealthCheck(client));

        //cors
        if (configuration.arlascorsenabled) {
            configureCors(environment);
        }
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest();
        nodesInfoRequest.clear().jvm(false).os(false).process(true);
        ActionFuture<NodesInfoResponse> nodesInfoResponseActionFuture = client.admin().cluster().nodesInfo(nodesInfoRequest);
        LOGGER.info("Number of  Node : ".concat(String.valueOf(nodesInfoResponseActionFuture.actionGet().getNodes().size())));
        nodesInfoResponseActionFuture.actionGet().getNodes().forEach(nodeInfo -> {
            DiscoveryNode node = nodeInfo.getNode();
            LOGGER.info("Node Name : ".concat(node.getName()));
            LOGGER.info("Node Id : ".concat(node.getId()));
            LOGGER.info("Node EphemeralId : ".concat(node.getEphemeralId()));
            LOGGER.info("Node Host address : ".concat(node.getHostAddress()));
            LOGGER.info("Node Host name : ".concat(node.getHostName()));
            LOGGER.info("Node Transport address : ".concat(node.getAddress().getAddress()));
            LOGGER.info("Node role : ".concat(node.getRoles().toString()));
        });
        LOGGER.info("Number of Connected Node : ".concat(String.valueOf(transportClient.connectedNodes().size())));
        transportClient.connectedNodes().forEach(node -> {
            LOGGER.info("Connected Name : ".concat(node.getName()));
            LOGGER.info("Connected Id : ".concat(node.getId()));
            LOGGER.info("Connected EphemeralId : ".concat(node.getEphemeralId()));
            LOGGER.info("Connected Host address : ".concat(node.getHostAddress()));
            LOGGER.info("Connected Host name : ".concat(node.getHostName()));
            LOGGER.info("Connected Transport address : ".concat(node.getAddress().getAddress()));
            LOGGER.info("Connected role : ".concat(node.getRoles().toString()));
        });
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
