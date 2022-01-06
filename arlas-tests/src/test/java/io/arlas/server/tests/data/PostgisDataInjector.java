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

package io.arlas.server.tests.data;

import com.codahale.metrics.MetricRegistry;
import io.arlas.server.tests.Data;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import net.postgis.jdbc.PGgeometry;
import net.postgis.jdbc.geometry.LinearRing;
import net.postgis.jdbc.geometry.Point;
import org.geojson.Polygon;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.guava.GuavaPlugin;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.jodatime2.JodaTimePlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.Instant;

public class PostgisDataInjector extends AbstractDataInjector {

    private static Jdbi jdbi;

    static {
        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setUrl("jdbc:postgresql_postGIS://localhost:5432/arlas");
        dataSourceFactory.setUser("postgres");
        dataSourceFactory.setPassword("postgres");
        dataSourceFactory.setDriverClass("org.postgis.DriverWrapper");
        ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(),"jdbi");
        jdbi = Jdbi.create(dataSource)
                .installPlugin(new PostgresPlugin())
                .installPlugin(new Jackson2Plugin())
                .installPlugin(new SqlObjectPlugin())
                .installPlugin(new JodaTimePlugin())
                .installPlugin(new GuavaPlugin());
    }

    @Override
    public void createDataSink(String dataSinkName) {
        jdbi.withExtension(FlattenedDataDao.class, dao -> {
            dao.createTable(dataSinkName);
            return true;
        });
    }

    @Override
    public void writeData(String dataSinkName, Data data) {
        jdbi.withExtension(FlattenedDataDao.class, dao -> {
            dao.insertBean(dataSinkName, new FlattenedData(data));
            return true;
        });
    }

    @Override
    public void clearDataSink(String dataSinkName) {
        jdbi.withExtension(FlattenedDataDao.class, dao -> {
            dao.dropTable(dataSinkName);
            return true;
        });
    }

    public interface FlattenedDataDao {
        @SqlUpdate("CREATE TABLE <table_name>(" +
                DATASET_ID_PATH + " VARCHAR PRIMARY KEY," +
                "fullname VARCHAR, " +
                "params_job VARCHAR, " +
                "params_age INTEGER, " +
                "params_weight INTEGER, " +
                "params_city VARCHAR, " +
                "params_country VARCHAR, " +
                DATASET_TIMESTAMP_PATH + " TIMESTAMP, " +
                "params_stopdate TIMESTAMP, " +
                DATASET_GEOMETRY_PATH + " GEOMETRY(Polygon,4326), " +
                DATASET_WKT_GEOMETRY_PATH + " VARCHAR, " +
                DATASET_CENTROID_PATH + " GEOMETRY(Point,4326), " +
                "geo_params_second_geometry GEOMETRY(Polygon,4326), " +
                "geo_params_other_geopoint GEOMETRY(Point,4326)" +
                ");")
        void createTable(@Define("table_name") String tableName);

        @SqlUpdate("DROP TABLE IF EXISTS <table_name>")
        void dropTable(@Define("table_name") String tableName);

        @SqlUpdate("INSERT INTO <table_name>("+DATASET_ID_PATH+", fullname, params_job, params_age, params_weight, params_city, params_country, "+DATASET_TIMESTAMP_PATH+", params_stopdate, " +
                DATASET_GEOMETRY_PATH+", "+DATASET_WKT_GEOMETRY_PATH+", "+DATASET_CENTROID_PATH+", geo_params_second_geometry, geo_params_other_geopoint)" +
                " VALUES ( :id, :fullname, :job, :age, :weight, :city, :country, :startdate, :stopdate, " +
                ":geometry, :wktgeometry, :centroid, :second_geometry, :other_geopoint)")
        void insertBean(@Define("table_name") String tableName, @BindBean FlattenedData data);
    }

    public class FlattenedData {
        public String id;
        public String fullname;
        public String job;
        public int age;
        public Integer weight;
        public String city;
        public String country;
        public Instant startdate;
        public Instant stopdate;
        public PGgeometry geometry;
        public String wktgeometry;
        public PGgeometry centroid;
        public PGgeometry second_geometry;
        public PGgeometry other_geopoint;

        public FlattenedData(Data data) {
            id = data.id;
            fullname = data.fullname;
            job = data.params.job;
            age = data.params.age;
            weight = data.params.weight;
            city = data.params.city;
            country = data.params.country;
            startdate = Instant.ofEpochSecond(data.params.startdate);
            stopdate = Instant.ofEpochSecond(data.params.stopdate);
            geometry = getPGgeometry(data.geo_params.geometry);
            wktgeometry = data.geo_params.wktgeometry;
            centroid = getPGgeometry(data.geo_params.centroid);
            second_geometry = getPGgeometry(data.geo_params.second_geometry);
            other_geopoint = getPGgeometry(data.geo_params.other_geopoint);
        }

        private PGgeometry getPGgeometry(Polygon polygon) {
            Point[] coords = polygon.getExteriorRing().stream().map(coord -> new Point(coord.getLongitude(), coord.getLatitude())).toArray(Point[]::new);
            net.postgis.jdbc.geometry.Polygon geo = new net.postgis.jdbc.geometry.Polygon(new LinearRing[] {new LinearRing(coords)});
            return new PGgeometry(geo);
        }

        private PGgeometry getPGgeometry(String geoPoint) {
            String[] coords = geoPoint.split(",");
            return new PGgeometry(new Point(Double.valueOf(coords[1]), Double.valueOf(coords[0])));
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public Instant getStartdate() {
            return startdate;
        }

        public void setStartdate(Instant startdate) {
            this.startdate = startdate;
        }

        public Instant getStopdate() {
            return stopdate;
        }

        public void setStopdate(Instant stopdate) {
            this.stopdate = stopdate;
        }

        public PGgeometry getGeometry() {
            return geometry;
        }

        public void setGeometry(PGgeometry geometry) {
            this.geometry = geometry;
        }

        public String getWktgeometry() {
            return wktgeometry;
        }

        public void setWktgeometry(String wktgeometry) {
            this.wktgeometry = wktgeometry;
        }

        public PGgeometry getCentroid() {
            return centroid;
        }

        public void setCentroid(PGgeometry centroid) {
            this.centroid = centroid;
        }

        public PGgeometry getSecond_geometry() {
            return second_geometry;
        }

        public void setSecond_geometry(PGgeometry second_geometry) {
            this.second_geometry = second_geometry;
        }

        public PGgeometry getOther_geopoint() {
            return other_geopoint;
        }

        public void setOther_geopoint(PGgeometry other_geopoint) {
            this.other_geopoint = other_geopoint;
        }
    }
}
