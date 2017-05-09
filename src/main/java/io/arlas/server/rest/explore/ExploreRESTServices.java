package io.arlas.server.rest.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/explore")
@Api(value = "/explore")
@SwaggerDefinition(info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"), title = "ARLAS Exploration API", description = "Explore the content of ARLAS collections", license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"), version = "V0.1.0"))

public abstract class ExploreRESTServices {

    protected static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
    }

    public ExploreServices getExploreServices() {
        return exploreServices;
    }

    protected ExploreServices exploreServices;
    Logger LOGGER = LoggerFactory.getLogger(ExploreRESTServices.class);

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    public ExploreRESTServices(ExploreServices exploreServices) {
        this.exploreServices = exploreServices;
    }
}
