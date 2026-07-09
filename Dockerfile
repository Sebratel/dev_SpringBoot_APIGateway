# syntax=docker/dockerfile:1
# --- Estágio 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B


FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV TZ=America/Sao_Paulo
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-Duser.timezone=America/Sao_Paulo","-jar","app.jar"]