package io.arlas.server.app;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class ArlasServerConfiguration extends Configuration{
    @JsonProperty("zipkin")
    public ZipkinFactory zipkinConfiguration;

	@JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @JsonProperty("elastic-host")
    public String elastichost;

    @JsonProperty("elastic-port")
    public Integer elasticport;

    @JsonProperty("elastic-cluster")
    public String elasticcluster;

    @JsonProperty("arlas-index")
    public String arlasindex;
}
