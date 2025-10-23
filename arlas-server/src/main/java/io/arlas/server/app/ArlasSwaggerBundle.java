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

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.views.common.ViewBundle;
import io.federecio.dropwizard.swagger.ConfigurationHelper;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.federecio.dropwizard.swagger.SwaggerResource;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

public abstract class ArlasSwaggerBundle<T extends ArlasServerConfiguration> implements ConfiguredBundle<T> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
        ModelConverters.getInstance().addConverter(new ModelResolver(bootstrap.getObjectMapper()));
    }
    @Override
    public void run(T configuration, Environment environment) throws Exception {
        SwaggerBundleConfiguration swaggerBundleConfiguration = this.getSwaggerBundleConfiguration(configuration);
        if (swaggerBundleConfiguration == null) {
            throw new IllegalStateException("You need to provide an instance of SwaggerBundleConfiguration");
        } else if (swaggerBundleConfiguration.isEnabled()) {
            ConfigurationHelper configurationHelper = new ConfigurationHelper(configuration, swaggerBundleConfiguration);
            (new AssetsBundle("/swagger-static", configurationHelper.getSwaggerUriPath(), (String)null, "swagger-assets")).run(configuration, environment);
            (new AssetsBundle("/swagger-static/oauth2-redirect.html", configurationHelper.getOAuth2RedirectUriPath(), (String)null, "swagger-oauth2-connect")).run(configuration, environment);
            SwaggerConfiguration oasConfiguration = swaggerBundleConfiguration.build();
            (new JaxrsOpenApiContextBuilder<>()).openApiConfiguration(oasConfiguration).buildContext(true);
            OpenApiResource openApiResource = new OpenApiResource();
            List<Server> servers = List.of(
                    new Server().url(configuration.arlasBaseUri)
            );
            openApiResource.setOpenApiConfiguration(
                    new SwaggerConfiguration().openAPI(oasConfiguration.getOpenAPI().servers(servers))
            );
            environment.jersey().register(openApiResource);
            environment.jersey().register(new SwaggerSerializers());
            if (swaggerBundleConfiguration.isIncludeSwaggerResource()) {
                environment.jersey().register(new SwaggerResource(configurationHelper.getUrlPattern(), swaggerBundleConfiguration.getSwaggerViewConfiguration(), swaggerBundleConfiguration.getSwaggerOAuth2Configuration(), swaggerBundleConfiguration.getContextRoot(), swaggerBundleConfiguration.getCustomJavascript()));
            }

        }
    }

    protected abstract SwaggerBundleConfiguration getSwaggerBundleConfiguration(T var1);
}
