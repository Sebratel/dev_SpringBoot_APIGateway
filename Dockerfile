# --- Estágio 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Estágio 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy

# 1. Instala dependências do sistema (Python, Pip, Cron)
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 python3-pip cron \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 2. COPIA o requirements.txt primeiro (melhor para o cache do Docker)
COPY requirements.txt .

# 3. INSTALA as bibliotecas do Python baseadas no arquivo
RUN pip3 install --no-cache-dir -r requirements.txt

# 4. Copia o restante dos arquivos
COPY --from=build /app/target/*.jar app.jar
COPY app.py .
COPY crontab.txt /etc/cron.d/meu-cron
COPY entrypoint.sh .

# 5. Configurações de permissão e Cron
RUN chmod 0644 /etc/cron.d/meu-cron && \
    crontab /etc/cron.d/meu-cron && \
    touch /var/log/cron.log && \
    chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]