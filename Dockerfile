# fetch basic image
FROM maven:3.5.0-jdk-8

# application placed into /opt/app
RUN mkdir -p /opt/app
WORKDIR /opt/app
ADD target/arlas-server.jar /opt/app/arlas-server.jar
ADD test/tests-integration-configuration.yaml /opt/app/configuration.yaml

# local application port
EXPOSE 9999

# execute it
CMD java -jar /opt/app/arlas-server.jar server configuration.yaml