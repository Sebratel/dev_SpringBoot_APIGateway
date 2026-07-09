# syntax=docker/dockerfile:1
# --- Estágio 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia apenas o POM primeiro para garantir o cache das dependências
COPY pom.xml .

# CORREÇÃO: Usamos o verify para baixar dependências E plugins em modo offline simulado
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:plugins--go-offline dependency:resolve-plugins -B

COPY src ./src

# Executa o package usando o mesmo cache e em modo offline (se possível) para acelerar
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B -o

# --- Estágio 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV TZ=America/Sao_Paulo

# Evita usar wildcard (*) se houver mais de um jar gerado (como o original + plain)
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-Duser.timezone=America/Sao_Paulo","-jar","app.jar"]