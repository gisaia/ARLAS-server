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

import cyclops.data.tuple.Tuple3;
import io.arlas.server.model.request.MultiValueFilter;
import io.arlas.server.model.request.Request;
import io.arlas.server.ns.ATOM;
import io.arlas.server.ns.OPENSEARCH;
import io.arlas.server.rest.explore.opensearch.AtomHitsMessageBodyWriter;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.swagger.models.auth.In;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.config.XmlConfig.xmlConfig;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertThat;

public class ATOMSearchServiceIT extends AbstractProjectedTest {

    @Override
    public String getUrlPath(String collection) {
        return arlasPath + "explore/" + collection + "/_search";
    }

    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("0"));
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(0));
        }
    }

    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------

    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        given().config(RestAssured.config().xmlConfig(xmlConfig().declareNamespace(ATOM.XML_PREFIX, ATOM.XML_NS)));
        given().config(RestAssured.config().xmlConfig(xmlConfig().declareNamespace(OPENSEARCH.XML_PREFIX, OPENSEARCH.XML_NS)));
        return given().accept(ATOM.APPLICATION_ATOM_XML);
    }

    @Override
    protected RequestSpecification givenFilterableRequestBody() {
        return given().contentType("application/json");
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("1"))
                    .body(ATOM.XML_PREFIX + ":feed.entry[0].content.params.job", equalTo("Architect"))
                    .body(ATOM.XML_PREFIX + ":feed.entry[0].content.params.startdate", equalTo("1009800"))
                    .body(ATOM.XML_PREFIX + ":feed.entry[0].content.geo_params.centroid", equalTo("20,-10"))
                    .body(ATOM.XML_PREFIX + ":feed.entry[0].content.params.city.size()", equalTo(0))

                    .body(ATOM.XML_PREFIX + ":feed.entry[0].update", equalTo(AtomHitsMessageBodyWriter.dateFormater.format(new Date(1009800))));
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(1))
                    .body("hits[0].data.params.job", equalTo("Architect"))
                    .body("hits[0].data.params.city", isEmptyOrNullString())

                    .body("hits[0].data.params.startdate", equalTo(1009800))
                    .body("hits[0].data.geo_params.centroid", equalTo("20,-10"))
                    .body("hits[0].md.timestamp", equalTo(1009800));
        }
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("" + nbResults))
                    .body(ATOM.XML_PREFIX + ":feed.entry.content.params.job", everyItem(isOneOf(values)));
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(nbResults))
                    .body("hits.data.params.job", everyItem(isOneOf(values)));
        }
    }

    @Override
    protected void handleFieldFilter(ValidatableResponse then, int nbResults) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("" + nbResults));
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(nbResults));
        }
    }

    //----------------------------------------------------------------
    //----------------------- TEXT QUERY -----------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then, int nbResults) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("" + nbResults));
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(nbResults));
        }
    }

    @Override
    protected void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end, int nbResults) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("" + nbResults))
                    .extract().xmlPath().getList("atom:feed.entry.content.params.startdate").stream()
                    .mapToInt(n -> Integer.parseInt((String) n)).forEach(stamp -> {
                assertThat(stamp, allOf(greaterThan(start), lessThan(end)));
            });
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(nbResults))
                    .body("hits.data.params.startdate", everyItem(greaterThan(start)))
                    .body("hits.data.params.startdate", everyItem(lessThan(end)));
        }
    }

    @Override
    protected void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end, int nbResults) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200)
                    .body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("" + nbResults))
                    .extract().xmlPath().getList("atom:feed.entry.content.params.job").stream()
                    .forEach(stamp -> {
                        assertThat((String) stamp, allOf(greaterThan(start), lessThan(end)));
                    });
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(nbResults))
                    .body("hits.data.params.job", everyItem(greaterThan(start)))
                    .body("hits.data.params.job", everyItem(lessThan(end)));
        }
    }

    @Override
    protected void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher centroidMatcher) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            then.statusCode(200).body(ATOM.XML_PREFIX + ":feed.totalResults", equalTo("" + nbResults));
            assertThat(then.statusCode(200).extract().xmlPath().getList("atom:feed.entry.content.geo_params.centroid"),
                    centroidMatcher);
        } else {
            then.statusCode(200)
                    .body("totalnb", equalTo(nbResults))
                    .body("hits.data.geo_params.centroid", centroidMatcher);
        }
    }

    //----------------------------------------------------------------
    //----------------------- FORM PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected RequestSpecification givenFlattenRequestParams() {
        return givenFilterableRequestParams();
    }

    @Override
    protected Request flattenRequestParamsPost(Request request) {
        return request;
    }

    @Override
    protected List<String> getFlattenedItems() {
        List<String> flattenedItems = new ArrayList<>();
        flattenedItems.add("params" + FLATTEN_CHAR + "age");
        flattenedItems.add("params" + FLATTEN_CHAR + "country");
        flattenedItems.add("params" + FLATTEN_CHAR + "job");
        flattenedItems.add("params" + FLATTEN_CHAR + "startdate");
        flattenedItems.add("params" + FLATTEN_CHAR + "stopdate");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "centroid");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "type");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "0");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "1");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "1" + FLATTEN_CHAR + "0");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "1" + FLATTEN_CHAR + "1");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "2" + FLATTEN_CHAR + "0");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "2" + FLATTEN_CHAR + "1");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "3" + FLATTEN_CHAR + "0");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "3" + FLATTEN_CHAR + "1");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "4" + FLATTEN_CHAR + "0");
        flattenedItems.add("geo_params" + FLATTEN_CHAR + "geometry" + FLATTEN_CHAR + "coordinates" + FLATTEN_CHAR + "0" + FLATTEN_CHAR + "4" + FLATTEN_CHAR + "1");
        return flattenedItems;
    }

    @Override
    protected void handleFlatFormatRequest(ValidatableResponse then, List<String> flattenedItems) {
        flattenedItems.forEach(flattenedItem -> {
            if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
                assertThat(then.statusCode(200).extract().xmlPath().getList("atom:feed.entry.content." + flattenedItem), everyItem(not(isEmptyOrNullString())));
            } else {
                then.statusCode(200)
                        .body("hits.data", hasItem(hasKey(flattenedItem)));
            }
        });
    }
    //----------------------------------------------------------------
    //----------------------- SIZE PART ------------------------------
    //----------------------------------------------------------------
    @Override
    protected RequestSpecification givenBigSizedRequestParams() {
        return given().param("q", "My name is");
    }

    @Override
    protected RequestSpecification givenBigSizedRequestParamsPost() {
        search.filter.q = Arrays.asList(new MultiValueFilter<>("My name is"));
        return given().contentType("application/json");
    }

    @Override
    protected int getBigSizedResponseSize() {
        return 595;
    }

    @Override
    protected void handleSizeParameter(ValidatableResponse then, int nbResults) throws Exception {
        if (nbResults > 0) {
            if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
                assertThat(then.statusCode(200).extract().xmlPath().getList(ATOM.XML_PREFIX + ":feed.entry").size(), equalTo(nbResults));
            } else {
                then.statusCode(200)
                        .body("nbhits", equalTo(nbResults))
                        .body("hits.size()", equalTo(nbResults));
            }
        } else {
            if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
                assertThat(then.statusCode(200).extract().xmlPath().getList(ATOM.XML_PREFIX + ":feed.entry").size(), equalTo(nbResults));
            } else {
                then.statusCode(200)
                        .body("nbhits", equalTo(nbResults));
            }
        }
    }

    //----------------------------------------------------------------
    //----------------------- PROJECTION PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleHiddenParameter(ValidatableResponse then, List<String> hidden) throws Exception {

        then.statusCode(200);
        if (!then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            for (String key : hidden) {
                String path = "hits.data";
                String lastKey = key;
                if (key.contains(".")) {
                    path += ("." + key.substring(0, key.lastIndexOf(".")));
                    lastKey = key.substring(key.lastIndexOf(".") + 1, key.length());
                }
                then.body(path, everyItem(not(hasKey(lastKey))));
            }
        }
    }

    @Override
    protected void handleDisplayedParameter(ValidatableResponse then, List<String> displayed) throws Exception {
        then.statusCode(200);
        if (!then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            for (String key : displayed) {
                String path = "hits.data";
                String lastKey = key;
                if (key.contains(".")) {
                    path += ("." + key.substring(0, key.lastIndexOf(".")));
                    lastKey = key.substring(key.lastIndexOf(".") + 1, key.length());
                }
                then.body(path, everyItem(hasKey(lastKey)));
            }
        }
    }

    //----------------------------------------------------------------
    //----------------------- SORT PART ------------------------------
    //----------------------------------------------------------------

    @Override
    protected void handleSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            assertThat(then.statusCode(200).extract().xmlPath().get("atom:feed.entry[0].content.params.job"), equalTo(firstElement));
        } else {
            then.statusCode(200)
                    .body("hits[0].data.params.job", equalTo(firstElement));
        }
    }

    @Override
    protected void handleGeoSortParameter(ValidatableResponse then, String firstElement) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            assertThat(then.statusCode(200).extract().xmlPath().get("atom:feed.entry[0].content.geo_params.centroid"), equalTo(firstElement));
        } else {
            then.statusCode(200)
                    .body("hits[0].data.geo_params.centroid", equalTo(firstElement));
        }
    }

    @Override
    protected Integer getDateAfterFirstSearch(ExtractableResponse response) throws Exception {
        if (response.contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            return Integer.parseInt(response.path("atom:feed.entry[0].content.params.startdate"));
        } else {
            return response.path("hits[0].data.params.startdate");
        }
    }

    @Override
    protected Tuple3 getIdsAfterFirstSearch(ExtractableResponse response) throws Exception {
        if (response.contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            return new Tuple3(response.path("atom:feed.entry[0].content.id"), response.path("atom:feed.entry[1].content.id"), response.path("atom:feed.entry[2].content.id"));
        } else {
            return new Tuple3(response.path("hits[0].data.id"), response.path("hits[1].data.id"), response.path("hits[2].data.id"));
        }
    }

    @Override
    protected void handleSortAndAfterParameters(ValidatableResponse then, String id1, String id2) throws Exception {
        if (then.extract().contentType().equals(ATOM.APPLICATION_ATOM_XML)) {
            assertThat(then.statusCode(200).extract().xmlPath().get("atom:feed.entry[0].content.id"), equalTo(id1));
            assertThat(then.statusCode(200).extract().xmlPath().get("atom:feed.entry[1].content.id"), equalTo(id2));
        } else {
            then.statusCode(200)
                    .body("hits[0].data.id", equalTo(id1))
                    .body("hits[1].data.id", equalTo(id2));
        }
    }

    @Override
    protected void handleInvalidGeoSortParameter(ValidatableResponse then) {
        then.statusCode(400);
    }

    @Override
    protected void handleInvalidSortWithAfterParameters(ValidatableResponse then) {
        then.statusCode(400);
    }
}
