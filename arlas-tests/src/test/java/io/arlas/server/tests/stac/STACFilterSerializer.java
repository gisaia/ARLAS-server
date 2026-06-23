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

package io.arlas.server.tests.stac;

import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.arlas.server.tests.stac.STACFilterModels.FilterClause;
import static io.arlas.server.tests.stac.STACFilterModels.FilterLang;
import static io.arlas.server.tests.stac.STACFilterModels.BetweenValue;
import static io.arlas.server.tests.stac.STACFilterModels.BboxValue;

import java.io.StringWriter;
public class STACFilterSerializer {

    public String serialize(List<FilterClause> clauses, FilterLang lang) throws ParseException, IOException {
        return switch (lang) {
            case CQL2_TEXT -> serializeText(clauses);
            case CQL2_JSON -> serializeJson(clauses);
        };
    }

    private String serializeText(List<FilterClause> clauses) {
        return clauses.stream()
                .map(this::serializeClauseToText)
                .collect(Collectors.joining(" AND "));
    }

    private String serializeJson(List<FilterClause> clauses) throws ParseException, IOException {
        if (clauses.size() == 1) {
            return serializeClauseToJson(clauses.get(0));
        }
        String args = clauses.stream()
                .map(this::serializeClauseToJsonUnchecked)
                .collect(Collectors.joining(","));

        return """
               {"op":"and","args":[%s]}
               """.formatted(args)
                .replace("\n", "")
                .trim();
    }

    private String serializeClauseToJsonUnchecked(FilterClause clause) {
        try {
            return serializeClauseToJson(clause);
        } catch (ParseException | IOException e) {
            throw new IllegalStateException("Unable to serialize clause: " + clause, e);
        }
    }

    private String serializeClauseToText(FilterClause clause) {
        return switch (clause.operator()) {
            case BETWEEN -> {
                BetweenValue between = asBetweenValue(clause);
                yield "%s BETWEEN %s AND %s".formatted(
                        clause.property(),
                        toCqlTextLiteral(between.lower()),
                        toCqlTextLiteral(between.upper())
                );
            }
            case ST_INTERSECTS -> "S_INTERSECTS(%s,%s)".formatted(
                    clause.property(),
                    toCqlSpatialArgument(clause.value())
            );
            case ST_WITHIN -> "S_WITHIN(%s,%s)".formatted(
                        clause.property(),
                        toCqlSpatialArgument(clause.value())
            );
            case BBOX -> {
                BboxValue bbox = asBboxValue(clause.value(), "BBOX");
                yield "S_INTERSECTS(%s,%s)".formatted(
                        clause.property(),
                        toCqlBboxLiteral(bbox)
                );
            }
            default -> "%s %s %s".formatted(
                    clause.property(),
                    clause.operator().symbol(),
                    toCqlTextLiteral(clause.value())
            );
        };
    }

    private  String serializeClauseToJson(FilterClause clause) throws ParseException, IOException {
        return switch (clause.operator()) {
            case BETWEEN -> {
                BetweenValue between = asBetweenValue(clause);
                yield """
                {"op":"between","args":[{"property":"%s"},%s,%s]}
                """.formatted(
                        clause.property(),
                        toJsonLiteral(between.lower()),
                        toJsonLiteral(between.upper())
                ).replace("\n", "").trim();
            }
            case ST_INTERSECTS -> """
            {"op":"s_intersects","args":[{"property":"%s"},%s]}
            """.formatted(
                    clause.property(),
                    toJsonSpatialArgument(clause.value())
            ).replace("\n", "").trim();

            case ST_WITHIN -> """
            {"op":"s_within","args":[{"property":"%s"},%s]}
            """.formatted(
                    clause.property(),
                    toJsonSpatialArgument(clause.value())
            ).replace("\n", "").trim();

            case BBOX -> {
                BboxValue bbox = asBboxValue(clause.value(), "BBOX");
                yield """
                {"op":"s_intersects","args":[{"property":"%s"},%s]}
                """.formatted(
                        clause.property(),
                        toJsonBboxLiteral(bbox)
                ).replace("\n", "").trim();
            }
            default -> """
            {"op":"%s","args":[{"property":"%s"},%s]}
            """.formatted(
                    clause.operator().symbol(),
                    clause.property(),
                    toJsonLiteral(clause.value())
            ).replace("\n", "").trim();
        };
    }

    private String toJsonLiteral(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "\"" + s
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"") + "\"";
        }
        return String.valueOf(value);
    }

    private String toCqlTextLiteral(Object value) {
        if (value instanceof String) {
            return "'" + escapeCqlText(value) + "'";
        }
        return escapeCqlText(value);
    }

    private String escapeCqlText(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return s.replace("'", "''");
        }
        return String.valueOf(value);
    }

    private BetweenValue asBetweenValue(FilterClause clause) {
        if (clause.value() instanceof BetweenValue between) {
            return between;
        }
        throw new IllegalArgumentException(
                "BETWEEN operator requires a BetweenValue for property: " + clause.property()
        );
    }

    private String toCqlSpatialArgument(Object value) {
        if (value instanceof BboxValue bbox) {
            return toCqlBboxLiteral(bbox);
        }
        return toCqlTextLiteral(value);
    }

    private String toCqlBboxLiteral(BboxValue bbox) {
        return "BBOX(%s,%s,%s,%s)".formatted(
                bbox.minX(),
                bbox.minY(),
                bbox.maxX(),
                bbox.maxY()
        );
    }
    private String toJsonBboxLiteral(BboxValue bbox) {
        return """
        {"type":"Polygon","coordinates":[[
          [%s,%s],
          [%s,%s],
          [%s,%s],
          [%s,%s],
          [%s,%s]
        ]]}
        """.formatted(
                bbox.minX(), bbox.minY(),
                bbox.maxX(), bbox.minY(),
                bbox.maxX(), bbox.maxY(),
                bbox.minX(), bbox.maxY(),
                bbox.minX(), bbox.minY()
        ).replaceAll("\\s+", "");
    }

    private BboxValue asBboxValue(Object value, String operator) {
        if (value instanceof BboxValue bbox) {
            return bbox;
        }
        throw new IllegalArgumentException(operator + " requires a BboxValue");
    }

    private String toJsonSpatialArgument(Object value) throws ParseException, IOException {
        if (value instanceof BboxValue bbox) {
            return toJsonBboxLiteral(bbox);
        }
        if (value instanceof String s && looksLikeWkt(s)) {
            return wktToGeoJsonLiteral(s);
        }
        return toJsonLiteral(value);
    }

    private static boolean looksLikeWkt(String value) {
        String trimmed = value == null ? "" : value.trim().toUpperCase();
        return trimmed.startsWith("POINT")
                || trimmed.startsWith("LINESTRING")
                || trimmed.startsWith("POLYGON")
                || trimmed.startsWith("MULTIPOINT")
                || trimmed.startsWith("MULTILINESTRING")
                || trimmed.startsWith("MULTIPOLYGON")
                || trimmed.startsWith("GEOMETRYCOLLECTION");
    }

    private static String wktToGeoJsonLiteral(String wkt) throws ParseException, IOException {
        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read(wkt);
        GeometryJSON geometryJson = new GeometryJSON();
        StringWriter writer = new StringWriter();
        geometryJson.write(geometry, writer);
        return writer.toString();
    }
}