FROM front:latest AS FRONT_IMAGE

FROM maven:3.5.2-jdk-8-alpine AS MAVEN_BUILD

MAINTAINER Younes Boukanoucha

WORKDIR /app/

# copy the project files
#COPY ./pom.xml ./pom.xml
#COPY ./server/pom.xml ./server/pom.xml
#COPY ./front/pom.xml ./front/pom.xml
# build all dependencies and pack up a blank api jar since webapp depends on it
#RUN mvn dependency:go-offline -B
# copy other files
#COPY ./server/src ./src/server/src
#COPY ./front ./src/front

COPY . .

# copy target from front image
COPY --from=FRONT_IMAGE /app/target /app/front/target

# build for release
RUN mvn package

FROM openjdk:8-jre-alpine

WORKDIR /app

COPY --from=MAVEN_BUILD /app/server/target/Drop.jar /app/

ENTRYPOINT ["java", "-jar", "Drop.jar"]
