package io.arlas.server.app;

import java.net.InetAddress;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kristofa.brave.Brave;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;

import io.arlas.server.rest.ArlasExceptionMapper;
import io.arlas.server.rest.ConstraintViolationExceptionMapper;
import io.arlas.server.rest.IllegalArgumentExceptionMapper;
import io.arlas.server.rest.JsonProcessingExceptionMapper;
import io.arlas.server.rest.collections.ElasticCollectionService;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.rest.explore.aggregate.AggregateRESTService;
import io.arlas.server.rest.explore.aggregate.GeoAggregateRESTService;
import io.arlas.server.rest.explore.count.CountRESTService;
import io.arlas.server.rest.explore.describe.DescribeCollectionRESTService;
import io.arlas.server.rest.explore.describe.DescribeRESTService;
import io.arlas.server.rest.explore.raw.RawRESTService;
import io.arlas.server.rest.explore.search.GeoSearchRESTService;
import io.arlas.server.rest.explore.search.SearchRESTService;
import io.arlas.server.rest.explore.suggest.SuggestRESTService;
import io.arlas.server.task.CollectionAutoDiscover;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.lifecycle.setup.ScheduledExecutorServiceBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

public class ArlasServer extends Application<ArlasServerConfiguration> {
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
    }

    @Override
    public void run(ArlasServerConfiguration configuration, Environment environment) throws Exception {
        Settings settings;
        if ("localhost".equals(configuration.elastichost.toLowerCase())) {
            settings = Settings.EMPTY;
        } else {
            settings = Settings.builder().put("cluster.name", configuration.elasticcluster).build();
        }

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(configuration.elastichost),
                        configuration.elasticport));

        if (configuration.zipkinConfiguration != null) {
            Optional<Brave> brave = configuration.zipkinConfiguration.build(environment);
        }

        ExploreServices exploration = new ExploreServices(client, configuration);
        environment.getObjectMapper().setSerializationInclusion(Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        environment.jersey().register(new ArlasExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());
        environment.jersey().register(new CountRESTService(exploration));
        environment.jersey().register(new SearchRESTService(exploration));
        environment.jersey().register(new AggregateRESTService(exploration));
        environment.jersey().register(new GeoSearchRESTService(exploration));
        environment.jersey().register(new GeoAggregateRESTService(exploration));
        environment.jersey().register(new SuggestRESTService(exploration));
        environment.jersey().register(new DescribeRESTService(exploration));
        environment.jersey().register(new DescribeCollectionRESTService(exploration));
        environment.jersey().register(new RawRESTService(exploration));
        environment.jersey().register(new ElasticCollectionService(client, configuration));
        
        //tasks
        environment.admin().addTask(new CollectionAutoDiscover(client, configuration));
        int scheduleAutoDiscover = configuration.collectionAutoDiscoverConfiguration.schedule;
        if(scheduleAutoDiscover > 0) {
            String nameFormat = "collection-auto-discover-%d";
            ScheduledExecutorServiceBuilder sesBuilder = environment.lifecycle().scheduledExecutorService(nameFormat);
            ScheduledExecutorService ses = sesBuilder.build();
            Runnable autoDiscoverTask = new CollectionAutoDiscover(client, configuration);
            ses.scheduleWithFixedDelay(autoDiscoverTask, 10, scheduleAutoDiscover, TimeUnit.SECONDS);
        }

        //cors
        if(configuration.arlascorsenabled) {
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
