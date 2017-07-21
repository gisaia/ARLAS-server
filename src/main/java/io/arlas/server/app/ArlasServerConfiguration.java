package io.arlas.server.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.server.exceptions.ArlasConfigurationException;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.elasticsearch.common.Strings;

public class ArlasServerConfiguration extends Configuration {
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
    
    @JsonProperty("arlas-cache-size")
    public int arlascachesize;
    
    @JsonProperty("arlas-cache-timeout")
    public int arlascachetimeout;

    @JsonProperty("arlas-cors-enabled")
    public Boolean arlascorsenabled;
    
    @JsonProperty("collection-auto-discover")
    public CollectionAutoDiscoverConfiguration collectionAutoDiscoverConfiguration;

    public void check() throws ArlasConfigurationException {
        if(Strings.isNullOrEmpty(elastichost) || elasticport<1
                || (!elastichost.equalsIgnoreCase("localhost") && Strings.isNullOrEmpty(elasticcluster))) {
            throw new ArlasConfigurationException("Elastic search configuration missing in config file.");
        }
        if(zipkinConfiguration == null) {
            throw new ArlasConfigurationException("Zipkin configuration missing in config file.");
        }
        if(swaggerBundleConfiguration == null) {
            throw new ArlasConfigurationException("Swagger configuration missing in config file.");
        }
        if(Strings.isNullOrEmpty(arlasindex)) {
            arlasindex = ".arlas";
        }
        if(arlascachesize<0) {
            arlascachesize = 1000;
        }
        if(arlascachetimeout<0) {
            arlascachetimeout = 60;
        }
        if(arlascorsenabled==null) {
            arlascorsenabled = false;
        }
        if(collectionAutoDiscoverConfiguration == null) {
            collectionAutoDiscoverConfiguration = new CollectionAutoDiscoverConfiguration();
            collectionAutoDiscoverConfiguration.schedule = 0;
        }
    }
}
