package io.arlas.server.rest.collections;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;


@Path("/collections")
@Api(value="/collections")
@SwaggerDefinition(
		info=@Info(
				contact=@Contact(email="contact@gisaia.com", name="Gisaia", url="http://www.gisaia.com/")
				,title="ARLAS Collection API"
				,description="Management of the ARLAS collections"
				,license=@License(name="Apache 2.0", url="https://www.apache.org/licenses/LICENSE-2.0.html"),
				version="V0.1.0"
				)
		)

public abstract class CollectionRESTServices {
	Logger LOGGER = LoggerFactory.getLogger(CollectionRESTServices.class);
	public static final String UTF8JSON=MediaType.APPLICATION_JSON+";charset=utf-8";
}
