FROM maven:3.9.6-jdk-21 AS builder

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:23-jdk

RUN apt-get update && apt-get install -y postgresql-client

COPY --from=builder target/tasker-0.0.1-SNAPSHOT.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]