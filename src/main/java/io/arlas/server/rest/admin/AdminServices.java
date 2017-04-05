package io.arlas.server.rest.admin;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;


@Path("/arlas-admin")
@Api(value="/arlas-admin")
@SwaggerDefinition(
		info=@Info(
				contact=@Contact(email="contact@gisaia.com", name="Gisaia", url="http://www.gisaia.com/")
				,title="ARLAS Administration API"
				,description="Management of the ARLAS collections"
				,license=@License(name="Apache 2.0", url="https://www.apache.org/licenses/LICENSE-2.0.html"),
				version="V0.1.0"
				)
		)

public abstract class AdminServices {
	Logger LOGGER = LoggerFactory.getLogger(AdminServices.class);
	public static final String UTF8JSON=MediaType.APPLICATION_JSON+";charset=utf-8";
}
