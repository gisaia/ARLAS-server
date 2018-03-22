package io.arlas.server.rest.explore;

import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Projection;
import io.arlas.server.model.request.Size;
import io.arlas.server.model.request.Sort;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractSortedTest extends AbstractProjectedTest {
    @Before
    public void setUpSearch() {
        search.size = new Size();
        search.filter = new Filter();
        search.sort = new Sort();
        search.projection = new Projection();
        search.filter = new Filter();
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testSortParameter() throws Exception {
        search.sort.sort = "-params.job";
        handleSortParameter(post(search), "Dancer");
        handleSortParameter(get("sort", search.sort.sort), "Dancer");

        search.sort.sort = "geodistance:-50 -110";
        handleGeoSortParameter(post(search), "-50,-110");
        handleGeoSortParameter(get("sort", search.sort.sort), "-50,-110");

    }


    @Test
    public void testInvalidSizeParameters() throws Exception {
        search.sort.sort = "-50 -110";
        handleInvalidGeoSortParameter(post(search));
        handleInvalidGeoSortParameter(get("sort", search.sort.sort));
    }

    protected abstract void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception;
    protected abstract void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception;
    protected abstract void handleInvalidGeoSortParameter(ValidatableResponse then) throws Exception;
}
