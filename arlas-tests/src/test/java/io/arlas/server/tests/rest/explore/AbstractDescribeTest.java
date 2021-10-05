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

package io.arlas.server.tests.rest.explore;

import io.arlas.server.tests.AbstractTestWithCollection;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import java.util.Optional;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static io.restassured.RestAssured.given;

public abstract class AbstractDescribeTest extends AbstractTestWithCollection {

    private static final String PARAMS_FIELD_EXCEPT_DUBLIN_CORE = "['index_name', 'type_name', 'id_path', 'geometry_path', 'centroid_path','h3_path', 'timestamp_path', 'exclude_fields', 'update_max_hits', 'taggable_fields', " +
            "'exclude_wfs_fields', 'custom_params', 'inspire', 'raster_tile_url', 'raster_tile_width', 'raster_tile_height', 'filter']";
    private static final String DUBLIN_CORE_FIELDS_EXCEPT_DATE = "['title', 'creator', 'subject', 'description', 'publisher', 'contributor', 'type', 'format', 'identifier', 'source', 'language', 'bbox', 'coverage', " +
            "'coverage_centroid']";

    @Test
    public void testDescribeFeature() throws Exception {
        handleMatchingResponse(get(Optional.empty()), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
    }

    @Test
    public void testDescribeFeatureWithEmptyColumFilter() throws Exception {
        handleMatchingResponse(get(Optional.of("")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
    }

    @Test
    public void testDescribeFeatureWithFullnameAndParamsInColumFilter() throws Exception {
        handleMatchingResponse(get(Optional.of("fullname,params,,geo_params.wktgeomet,geo_params.h3")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("*fullname*,params.*,geo_params.metry,geo_params.h3")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("geo_params.h3,fullname,params.country,params.not_indexed,params.not_enabled,params.weight,params.job,params.age,params.tags,params.keywords,params.stopdate")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("geo_params.h3,fullnam*,param*")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("geo_params.h3,fullname,*.country,*arams.weight,param*.job,*age,*ags,params.*eywor*,*arams.stopdate*,*enabled,*indexed")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("geo_params.h3,fullname,params.*ountry,params.weigh*,params.*o*,*aram*.age,params.tags,params.keywords*,params.stopdate")),
                new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
    }

    @Test
    public void testDescribeFeatureWithColumFilter() throws Exception {
        handleMatchingResponse(get(Optional.of("*ullname,*arams")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("*ullnam*,*aram*")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("*ullnam*,*aram*.*")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("*")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
    }

    @Test
    public void testDescribeFeatureWithSpecificColumFilter() throws Exception {
        handleNotMatchingResponse(get(Optional.of("*.*")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleMatchingResponse(get(Optional.of("fullname,*.*")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));
        handleNotMatchingResponse(get(Optional.of("fullname,params.")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getDescribeResultPath())));

        //anti regression - this used to work
        handleNotMatchingResponse(get(Optional.of("fullname.unknown,params,,geo_params.wktgeomet")), new JsonPath(this.getClass().getClassLoader().getResourceAsStream(getFilteredDescribeResultPath())));
    }

    @Test
    abstract public void testDescribeFeatureWithCollectionBasedColumFiltering() throws Exception;

    /**
     * Path to expected `describe` result
     * @return
     */
    protected abstract String getDescribeResultPath();

    /**
     * Path to expected filtered `describe` result
     * @return
     */
    protected abstract String getFilteredDescribeResultPath();

    /**
     * Compare the response to the given json
     * @param response
     * @param jsonPath
     */
    protected abstract void handleMatchingResponse(ValidatableResponse response, JsonPath jsonPath);

    protected abstract void handleNotMatchingResponse(ValidatableResponse response, JsonPath jsonPath);

    protected ValidatableResponse get(Optional<String> columnFilter) {
        return given()
                .header("column-filter", columnFilter.orElse(""))
                .when()
                .get(getUrlPath("geodata"))
                .then();
    }

    /**
     * Cannot compare the whole document because there is a dynamic `params.dublin_core_element_name.date` field in.
     * So we compare each field of group of fields one by one to avoid this dynamic one.
     * @param response
     * @param jsonPath
     * @param index
     */
    protected void compare(ValidatableResponse response, JsonPath jsonPath, Optional<Integer> index, boolean areParamsEqual) {
        String jsonIndex = index.map(i -> "[" + i + "].").orElse("");
        response
                .body(
                        jsonIndex + "properties",
                        areParamsEqual ? equalTo(jsonPath.getMap(jsonIndex + "properties")) : not(equalTo(jsonPath.getMap(jsonIndex + "properties"))))
                .and()
                .body(
                        jsonIndex + "collection_name",
                        equalTo(jsonPath.getString(jsonIndex + "collection_name")))
                .and()
                .body(
                        jsonIndex + "params" + PARAMS_FIELD_EXCEPT_DUBLIN_CORE,
                        equalTo(jsonPath.getMap(jsonIndex + "params" + PARAMS_FIELD_EXCEPT_DUBLIN_CORE)))
                .and()
                .body(
                        jsonIndex + "params.dublin_core_element_name" + DUBLIN_CORE_FIELDS_EXCEPT_DATE,
                        equalTo(jsonPath.getMap(jsonIndex + "params.dublin_core_element_name" + DUBLIN_CORE_FIELDS_EXCEPT_DATE)));
    }

}
