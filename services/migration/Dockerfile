FROM eclipse-temurin:24-jdk AS base
WORKDIR /application
RUN groupadd -r retrade && useradd -r retrade -g  retrade
RUN chown -R retrade:retrade /application && chmod -R 755 /application
USER retrade

FROM maven:3.9.9-eclipse-temurin-24 AS builder
WORKDIR /application-builder
COPY . .
RUN mvn clean package -pl :migration -am -DskipTests

FROM base AS deploy
WORKDIR /application
COPY --chown=retrade:retrade --from=builder /application-builder/services/migration/target/*.jar ./app.jar
USER retrade
ENTRYPOINT ["java", "-jar", "app.jar"]