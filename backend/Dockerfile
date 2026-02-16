# 1. Build-Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
LABEL authors="PoebelPogge"
WORKDIR /app

# Abhängigkeiten laden (Caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Bauen
COPY src ./src
RUN mvn package -DskipTests

# 2. Runtime-Stage
FROM eclipse-temurin:21-jre-alpine
LABEL authors="PoebelPogge"
WORKDIR /app

# Nur das fertige JAR kopieren
COPY --from=build /app/target/*.jar app.jar

# Die App starten
ENTRYPOINT ["java", "-jar", "app.jar"]