
FROM openjdk:23-jre-slim


RUN apt-get update && apt-get install -y postgresql-client


COPY target/tasker-0.0.1-SNAPSHOT.war /app.jar


ENTRYPOINT ["java", "-jar", "/app.jar"]