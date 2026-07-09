# syntax=docker/dockerfile:1
# --- Estágio 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia apenas o POM primeiro para garantir o cache das dependências
COPY pom.xml .

# CORREÇÃO: Comando ajustado com os goals corretos separados por espaço
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline dependency:resolve-plugins -B

COPY src ./src

# Executa o package usando o mesmo cache. Tiramos o "-o" por segurança,
# caso o seu projeto use algum plugin dinâmico gerado durante o build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# --- Estágio 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV TZ=America/Sao_Paulo

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-Duser.timezone=America/Sao_Paulo","-jar","app.jar"]