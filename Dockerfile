FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Otimização: Copia apenas o pom.xml primeiro para baixar as dependências
# Isso agiliza builds futuros se você não mudar as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código fonte e gera o JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (Container leve para rodar)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia apenas o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085

# Melhores práticas: define limites de memória para o Java no container
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xmx512m", "-jar", "app.jar"]
