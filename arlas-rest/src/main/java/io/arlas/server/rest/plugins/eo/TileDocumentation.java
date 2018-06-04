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

package io.arlas.server.rest.plugins.eo;

public class TileDocumentation {
    public static final String TILE_SAMPLING = "Size of the sampling for testing transparency: 1: test every pixel, 10: test 1 pixel every 10 pixels, etc.";
    public static final String TILE_COVERAGE = "Percentage (]0-100]) of acceptable transparent pixels. Higher the percentage, more tiles could be used for filling the tile";
}
