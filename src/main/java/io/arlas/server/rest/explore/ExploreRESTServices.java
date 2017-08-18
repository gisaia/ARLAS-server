package io.arlas.server.rest.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.rest.ResponseCacheManager;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/explore")
@Api(value = "/explore")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS Exploration API",
                description = "Explore the content of ARLAS collections",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "API_VERSION"))

public abstract class ExploreRESTServices {

    protected static Logger LOGGER = LoggerFactory.getLogger(ExploreRESTServices.class);

    protected static ObjectMapper mapper = new ObjectMapper();

    public ExploreServices getExploreServices() {
        return exploreServices;
    }

    protected ExploreServices exploreServices;

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    public ExploreRESTServices(ExploreServices exploreServices) {
        this.exploreServices = exploreServices;
    }

    public Response cache(Response.ResponseBuilder response, Integer maxagecache) {
        return exploreServices.getResponseCacheManager().cache(response, maxagecache);
    }
}
