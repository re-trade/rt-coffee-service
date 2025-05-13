FROM openjdk:17 AS base
USER app
WORKDIR /application

FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /application-builder
COPY . .
RUN mvn clean package -pl :main -am -DskipTests

FROM base AS deploy
WORKDIR /application
COPY --from=builder /application-builder/services/main/target/*.jar ./app.jar
USER app
ENTRYPOINT ["java", "-jar", "app.jar"]