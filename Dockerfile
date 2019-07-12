FROM maven:3.3-jdk-8
WORKDIR /usr/src/movieMoreBE
CMD mvn package && java -jar target/movieMoreBE-1.0-SNAPSHOT.jar server config.yml