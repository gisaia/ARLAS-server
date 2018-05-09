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

package io.arlas.server.rest.explore;

import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
        handleXYZ(xyzTileGet(null, null, 2, 2, 1), "0,0", "66.6,90");
        handleXYZ(xyzTileGet(null, null, 2, 3, 0), "66.6,90", "86,180");

        search.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-30,60,50,80"));
        handleXYZ(xyzTileGet("pwithin", Arrays.asList(search.filter.pwithin.get(0).get(0).toString()), 2, 2, 0), "66.5,0", "80,50");

        search.filter.pwithin = Arrays.asList(new MultiValueFilter<>("180,-67,-1,-5"));
        // inverted the order of bottomLeft and topRight parameters because of the negative values
        handleXYZ(xyzTileGet("pwithin", Arrays.asList(search.filter.pwithin.get(0).get(0).toString()), 2, 1, 2), "-10,-10", "-60,-90");

        search.filter.pwithin = Arrays.asList(new MultiValueFilter<>(Arrays.asList("-5,5,15,20", "-5,-5,15,5")));
        handleXYZ(xyzTileGet("pwithin", Arrays.asList(search.filter.pwithin.get(0).get(0).toString() + ";" + search.filter.pwithin.get(0).get(1).toString()),
                4, 8, 7), "0,0", "20,10");

        search.filter.pwithin = Arrays.asList(new MultiValueFilter<>(Arrays.asList("-5,-5,15,20")), new MultiValueFilter<>(Arrays.asList("-5,-5,15,5")));
        handleXYZ(xyzTileGet("pwithin", Arrays.asList(search.filter.pwithin.get(0).get(0).toString(), search.filter.pwithin.get(1).get(0).toString()),
                4, 8, 7), "0,0", "0,10");

        search.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-5,0,0,5"));
        handleXYZDisjointFromPwithin(xyzTileGet("pwithin", Arrays.asList(search.filter.pwithin.get(0).get(0).toString()), 2, 2, 0));
        search.filter.pwithin = null;

    }

    @Test
    public void testInvalidXYZTile() throws Exception {
        handleInvalidXYZ(xyzTileGet(null, null, 0, 1, 0));
        handleInvalidXYZ(xyzTileGet(null, null, 23, 1, 0));
    }

    protected abstract void handleXYZ(ValidatableResponse then, String bottomLeft, String topRight) throws Exception;

    protected abstract void handleXYZDisjointFromPwithin(ValidatableResponse then) throws Exception;

    protected abstract void handleInvalidXYZ(ValidatableResponse then) throws Exception;

    private ValidatableResponse xyzTileGet(String param, List<Object> paramValues, int z, int x, int y) {
        if (param == null && paramValues == null) {
            return givenFilterableRequestParams()
                    .when().get(getXYZUrlPath("geodata", z, x, y))
                    .then();
        } else {
            RequestSpecification req = givenFilterableRequestParams();
            for (Object paramValue : paramValues) {
                req = req.param(param, paramValue);
            }
            return req.when().get(getXYZUrlPath("geodata", z, x, y))
                    .then();
        }

    }

    protected abstract String getXYZUrlPath(String collection, int z, int x, int y);

}
