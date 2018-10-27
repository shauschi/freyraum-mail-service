FROM openjdk:8-jdk-alpine
MAINTAINER Stefan Hauschildt <stefan.h@uschildt.de>
ADD build/libs/freyraum-mail-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]