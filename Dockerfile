# fetch basic image
FROM maven:3.5.0-jdk-8

# install netcat
RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install netcat -y

# application placed into /opt/app
RUN mkdir -p /opt/app
WORKDIR /opt/app
ADD target/arlas-server.jar /opt/app/arlas-server.jar
ADD conf/configuration.yaml /opt/app/configuration.yaml
ADD scripts/wait-for-elasticsearch.sh /opt/app/wait-for-elasticsearch.sh

# local application port
EXPOSE 9999

# execute it
CMD java -jar /opt/app/arlas-server.jar server configuration.yaml
