#####################
# COMPILATION STAGE #
#####################
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /opt/build

# selectively add the POM file
COPY pom.xml /opt/build/
# get all the downloads out of the way
RUN mvn verify clean --fail-never

# build all project
COPY . /opt/build/
RUN mvn install \
    && mv /opt/build/arlas-server/target/arlas-server-*.jar /opt/build/arlas-server.jar

###################
# PACKAGING STAGE #
###################
FROM gisaia/arlas-openjdk-17-distroless:20250505175408

# application placed into /opt/app
WORKDIR /opt/app
COPY --from=build /opt/build/arlas-server.jar /opt/app/
COPY --from=build /opt/build/conf/configuration.yaml /opt/app/
EXPOSE 9999

ENV JDK_JAVA_OPTIONS="-Xmx1g -XX:+ExitOnOutOfMemoryError"
CMD ["arlas-server.jar", "server", "/opt/app/configuration.yaml"]
