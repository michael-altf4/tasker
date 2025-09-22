FROM maven:3.9.6-eclipse-temurin-21 AS builder

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21

RUN apt-get update && apt-get install -y postgresql-client

COPY --from=builder target/tasker-0.0.1-SNAPSHOT.war /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]