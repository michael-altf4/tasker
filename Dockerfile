FROM eclipse-temurin:21-jdk AS builder

ENV GRADLE_VERSION=8.11

RUN mkdir /gradle && \
    cd /tmp && \
    curl -sLO https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -q gradle-${GRADLE_VERSION}-bin.zip -d /gradle && \
    rm gradle-${GRADLE_VERSION}-bin.zip

ENV PATH="/gradle/gradle-${GRADLE_VERSION}/bin:${PATH}"

COPY build.gradle settings.gradle ./
COPY gradlew gradle ./
COPY src ./src

RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

COPY --from=builder build/libs/tasker-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]