package io.arlas.server.rest.explore;

import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractProjectedTest extends AbstractSizedTest {

    @Before
    public void setUpSearch() {
        search.size = new Size();
        search.filter = new Filter();
        search.projection = new Projection();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testIncludeExcludeFilter() throws Exception {
        search.projection.includes = "id,params,geo_params";
        handleHiddenParameter(post(search), "fullname");
        handleHiddenParameter(get("include", search.projection.includes), "fullname");

        search.projection.includes = null;
        search.projection.excludes = "fullname";
        handleHiddenParameter(post(search), "fullname");
        handleHiddenParameter(get("exclude", search.projection.excludes), "fullname");

        search.projection.excludes = null;
    }

    protected abstract void handleHiddenParameter(ValidatableResponse then, String hidden) throws Exception;
}
