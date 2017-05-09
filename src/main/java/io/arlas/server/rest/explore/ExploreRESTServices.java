package io.arlas.server.rest.explore;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.utils.CheckParams;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import java.io.IOException;
import java.util.List;

@Path("/explore")
@Api(value = "/explore")
@SwaggerDefinition(info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"), title = "ARLAS Exploration API", description = "Explore the content of ARLAS collections", license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"), version = "V0.1.0"))

public abstract class ExploreRESTServices {

    protected ExploreServices exploreServices;
    Logger LOGGER = LoggerFactory.getLogger(ExploreRESTServices.class);

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    public ExploreRESTServices(ExploreServices exploreServices) {
        this.exploreServices = exploreServices;
    }

    public SearchHits search(
            CollectionReference collectionReference,
            List<String> f,
            String q,
            LongParam before,
            LongParam after,
            String pwithin,
            String gwithin,
            String gintersect,
            String notpwithin,
            String notgwithin,
            String notgintersect,
            Boolean pretty,
            Boolean human,
            String include,
            String exclude,
            IntParam size,
            IntParam from,
            String sort
    ) throws ArlasException, IOException {

        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        fluidSearch.setCollectionReference(collectionReference);

        if (f != null && !f.isEmpty()) {
            fluidSearch = fluidSearch.filter(f);
        }
        if (q != null) {
            fluidSearch = fluidSearch.filterQ(q);
        }
        if(before != null || after != null) {
            if((before!=null && before.get()<0) || (after != null && after.get()<0)
                    || (before != null && after != null && before.get() < after.get()))
                throw new InvalidParameterException(FluidSearch.INVALID_BEFORE_AFTER);
        }
        if (after != null) {
            fluidSearch = fluidSearch.filterAfter(after.get());
        }
        if (before != null) {
            fluidSearch = fluidSearch.filterBefore(before.get());
        }
        if (pwithin != null && !pwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(pwithin);
            if (tlbr.length == 4 && tlbr[0]>tlbr[2] && tlbr[2]<tlbr[3]) {
                fluidSearch = fluidSearch.filterPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (gwithin != null && !gwithin.isEmpty()) {
            fluidSearch = fluidSearch.filterGWithin(gwithin);
        }
        if (gintersect != null && !gintersect.isEmpty()) {
            fluidSearch = fluidSearch.filterGIntersect(gintersect);
        }
        if (notpwithin != null && !notpwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(notpwithin);
            if (tlbr.length == 4 && tlbr[0]>tlbr[2] && tlbr[2]<tlbr[3]) {
                fluidSearch = fluidSearch.filterNotPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (notgwithin != null && !notgwithin.isEmpty()) {
            fluidSearch = fluidSearch.filterNotGWithin(notgwithin);
        }
        if (notgintersect != null && !notgintersect.isEmpty()) {
            fluidSearch = fluidSearch.filterNotGIntersect(notgintersect);
        }
        if (include != null) {
            fluidSearch = fluidSearch.include(include);
        }
        if (exclude != null) {
            fluidSearch = fluidSearch.exclude(exclude);
        }
        if (size != null && size.get() > 0) {
            if (from != null) {
                if(from.get() < 0) {
                    throw new InvalidParameterException(FluidSearch.INVALID_FROM);
                } else {
                    fluidSearch = fluidSearch.filterSize(size.get(), from.get());
                }
            } else {
                fluidSearch = fluidSearch.filterSize(size.get(), 0);
            }
        } else {
            throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
        }
        if (sort != null) {
            fluidSearch = fluidSearch.sort(sort);
        }

        return fluidSearch.exec().getHits();
    }
}
