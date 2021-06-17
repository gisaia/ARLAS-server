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

package io.arlas.server.core.impl.jdbi.postgis;

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.impl.jdbi.clause.AggregatedGeoSelectClause;
import io.arlas.server.core.impl.jdbi.clause.DateAggregationSelectClause;
import io.arlas.server.core.impl.jdbi.clause.GeotileAggregationSelectClause;
import io.arlas.server.core.impl.jdbi.model.ClauseParam;
import io.arlas.server.core.impl.jdbi.model.SelectRequest;
import io.arlas.server.core.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.core.services.FluidSearchService;
import org.locationtech.jts.geom.Geometry;
import org.postgis.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.arlas.server.core.utils.StringUtil.concat;

public class PostgisSelectRequest extends SelectRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgisSelectRequest.class);
    public static final String EPOCH_LP = "EXTRACT('epoch' FROM ";
    public static final String EPOCH_RP_MILLI = ") * 1000";
    public static final String G1 = "concat(";
    public static final String G2 = ", '/', lon2Xtile(st_x(st_transform(";
    public static final String G3 = ",4326)),";
    public static final String G4 = "), '/', lat2Ytile(st_y(st_transform(";
    public static final String G5 = "concat(";
    public static final String S1 = "to_tsvector(";
    public static final String S2 = ") @@ to_tsquery(";

    public String getDateFieldAsEpochMilli(String field) {
        return concat(EPOCH_LP, field, EPOCH_RP_MILLI);
    }

    @Override
    public void addSelectClause(DateAggregationSelectClause c) {
        String fct;
        Integer i = c.intervalValue;
        switch (c.intervalUnit) {
            case year:
            case quarter:
            case month:
            case week:
                fct = concat(EPOCH_LP, " date_trunc('", c.intervalUnit.name(), "',", c.columnName, ")", EPOCH_RP_MILLI);
                break;
            case day:
                i *= 24;
            case hour:
                i *= 60;
            case minute:
                i *= 60;
            case second:
                i *= 1000;
            default:
                fct = concat("FLOOR((", EPOCH_LP, c.columnName, EPOCH_RP_MILLI, " / ",
                        String.valueOf(i), " )) * ", String.valueOf(i));
        }
        selectClauses.add(concat(fct, AS, c.asName, COMMA, COUNT, LP, c.columnName, RP, AS, c.asNameCount));
        groupBy.add(c.asName);
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    @Override
    public void addSelectClause(GeotileAggregationSelectClause c) {
        // concat(<z>, '/', lon2Xtile(st_x(st_transform(<mygeo>,4326)),<z>), '/', lat2Ytile(st_y(st_transform(<mygeo>,4326)),<z>))
        selectClauses.add(concat(G1, c.precision, G2, c.columnName, G3, c.precision, G4, c.columnName, G3, c.precision, RP, RP, AS, c.asName,
                COMMA, COUNT, LP, c.columnName, RP, AS, c.asNameCount));
        groupBy.add(c.asName);
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    @Override
    public void addSelectClause(AggregatedGeoSelectClause c) {
        if (AggregatedGeometryEnum.BBOX.value().equals(c.function)) {
            //ST_Extent(mygeo)::geometry
            selectClauses.add(concat(ST_ASTEXT, LP, ST_EXTENT, LP, c.columnName, RP, RP, AS, c.asName));
        } else if (AggregatedGeometryEnum.CENTROID.value().equals(c.function)) {
            //ST_Centroid(ST_Extent(mygeo))
            selectClauses.add(concat(ST_ASTEXT, LP, ST_CENTROID, LP, ST_EXTENT, LP, c.columnName, RP, RP, RP, AS, c.asName));
        }
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    @Override
    public void addSelectClause(FluidSearchService fluidSearchService, String[] includes) throws ArlasException {
        List<String> selects = new ArrayList();
        for (String include : includes) {
            if (fluidSearchService.isGeoField(include)) {
                selects.add(concat(ST_ASTEXT, LP, include, RP, AS, include));
            } else {
                selects.add(include);
            }
        }
        selectClauses.add(String.join(COMMA, selects));
        LOGGER.debug("Adding selectClause:" + selectClauses.getLast());
    }

    @Override
    public String formatLikeCondition(String field, String value) {
        // https://www.postgresql.org/docs/13/textsearch-intro.html
        // https://www.postgresql.org/docs/13/textsearch-tables.html
        // to_tsvector(field) @@ to_tsquery('q');
        return concat(S1,field, S2, mapWhereParam(new ClauseParam("", value)), RP);
    }
        @Override
    protected Object jtsToDBgeo(Geometry jtsGeo) throws ArlasException {
        try {
            return new PGgeometry(concat(SRID, jtsGeo.toText()));
        } catch (SQLException e) {
            throw new ArlasException("Could not create PGgeometry from JTS geometry", e);
        }
    }
}
