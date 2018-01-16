#####################
# COMPILATION STAGE #
#####################
FROM maven:3.5-jdk-8-alpine as build
WORKDIR /opt/app

# selectively add the POM file
ADD pom.xml /opt/app/
# get all the downloads out of the way
RUN mvn verify clean --fail-never

# build all project
COPY . /opt/app
RUN mvn install

###################
# PACKAGING STAGE #
###################
FROM openjdk:8-jre-alpine

# install script dependencies
RUN apk add --update netcat-openbsd curl && rm -rf /var/cache/apk/*

# application placed into /opt/app
WORKDIR /opt/app
COPY --from=build /opt/app/target/arlas-server-*.jar /opt/app/arlas-server.jar
COPY --from=build /opt/app/conf/configuration.yaml /opt/app
COPY --from=build /opt/app/scripts/wait-for-elasticsearch.sh /opt/app
COPY --from=build /opt/app/scripts/start.sh /opt/app/
EXPOSE 9999

CMD /opt/app/start.sh
