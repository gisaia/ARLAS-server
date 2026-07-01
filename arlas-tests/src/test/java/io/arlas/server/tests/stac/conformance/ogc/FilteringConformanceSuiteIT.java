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

package io.arlas.server.tests.stac.conformance.ogc;

import io.arlas.server.tests.stac.conformance.ogc.featuresfilter.FeaturesFilterConformanceIT;
import io.arlas.server.tests.stac.conformance.ogc.filter.FilterConformanceIT;
import io.arlas.server.tests.stac.conformance.ogc.queryables.QueryablesConformanceIT;
import io.arlas.server.tests.stac.conformance.ogc.queryablesparams.QueryablesQueryParametersConformanceIT;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        QueryablesConformanceIT.class,
        QueryablesQueryParametersConformanceIT.class,
        FilterConformanceIT.class,
        FeaturesFilterConformanceIT.class
})
public class FilteringConformanceSuiteIT {
}