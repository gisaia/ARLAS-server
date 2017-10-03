package io.arlas.server.rest.explore;

import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Projection;
import io.arlas.server.model.request.Size;
import io.arlas.server.model.request.Sort;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractXYZTiledTest extends AbstractSortedTest {
    @Before
    public void setUpSearch() {
        search.size = new Size();
        search.filter = new Filter();
        search.sort = new Sort();
        search.projection = new Projection();
        search.filter = new Filter();
    }

    @Test
    public void testXYZTile() throws Exception {
        handleXYZWithoutFilters(xyzTileGet(null, null, 2, 2, 1), "0,0", "66.6,90");
        handleXYZWithoutFilters(xyzTileGet(null, null, 2, 3, 0), "66.6,90", "86,180");

        search.filter.pwithin = "80,-30,60,50";
        handleXYZWithPwithin(xyzTileGet("pwithin", search.filter.pwithin, 2, 2, 0), "66.5,0", "80,50");

        search.filter.pwithin = "5,-5,0,0";
        handleXYZDisjointFromPwithin(xyzTileGet("pwithin", search.filter.pwithin, 2, 2, 0));
        search.filter.pwithin = null;

    }

    @Test
    public void testInvalidXYZTile() throws Exception {
        handleInvalidXYZ(xyzTileGet(null, null, 0, 1, 0));
        handleInvalidXYZ(xyzTileGet(null, null, 23, 1, 0));
    }

    protected abstract void handleXYZWithoutFilters(ValidatableResponse then, String bottomLeft, String topRight) throws Exception;
    protected abstract void handleXYZWithPwithin(ValidatableResponse then, String bottomLeft, String topRight) throws Exception;
    protected abstract void handleXYZDisjointFromPwithin(ValidatableResponse then) throws Exception;
    protected abstract void handleInvalidXYZ(ValidatableResponse then) throws Exception;

    private ValidatableResponse xyzTileGet(String param,Object paramValue, int z, int x, int y){
        if (param == null && paramValue == null) {
            return givenFilterableRequestParams()
                    .when().get(getXYZUrlPath("geodata", z, x, y))
                    .then();
        } else {
            return givenFilterableRequestParams().param(param, paramValue)
                    .when().get(getXYZUrlPath("geodata", z, x, y))
                    .then();
        }

    }

    protected abstract String getXYZUrlPath(String collection, int z, int x, int y);

}
