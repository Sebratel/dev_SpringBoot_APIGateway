# syntax=docker/dockerfile:1
# --- Estágio 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Versão do Elastic APM Java Agent. Verificada como a release atual (não uma
# versão antiga assumida às cegas) e compatível com Java 21 / Spring Boot 3.4.
ARG ELASTIC_APM_AGENT_VERSION=1.56.0

# Copia apenas o POM primeiro para garantir o cache das dependências
COPY pom.xml .

# CORREÇÃO: Comando ajustado com os goals corretos separados por espaço
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline dependency:resolve-plugins -B

# Baixa o agente APM pelo próprio Maven, aproveitando o mesmo cache. Preferido a
# um curl solto: a resolução é determinística e passa pelo repositório já usado
# no build, sem depender de rede externa adicional em runtime.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:copy \
        -Dartifact=co.elastic.apm:elastic-apm-agent:${ELASTIC_APM_AGENT_VERSION} \
        -DoutputDirectory=/apm \
        -Dmdep.stripVersion=true

COPY src ./src

# Executa o package usando o mesmo cache. Tiramos o "-o" por segurança,
# caso o seu projeto use algum plugin dinâmico gerado durante o build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# --- Estágio 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV TZ=America/Sao_Paulo

COPY --from=build /apm/elastic-apm-agent.jar /apm-agent.jar
COPY --from=build /app/target/*.jar app.jar

# ---------------------------------------------------------------------------
# Elastic APM
# ---------------------------------------------------------------------------
# Comunicação pela rede Docker interna (elk_es_network). Não usa
# https://apm.sebratel.net.br: esse endereço é para acesso externo e faria o
# tráfego sair e voltar pelo proxy sem necessidade.
ENV ELASTIC_APM_SERVICE_NAME=api-gateway
ENV ELASTIC_APM_SERVER_URL=http://apm-server:8200
ENV ELASTIC_APM_ENVIRONMENT=production
# Package raiz REAL do projeto. O código usa br.com.sebratel, não com.sebratel.
ENV ELASTIC_APM_APPLICATION_PACKAGES=br.com.sebratel
ENV ELASTIC_APM_ENABLED=true

# --- Proteção de dados sensíveis no APM ---
# capture_body=off: os corpos de requisição contêm CPF (MatrixMassiveInputDTO),
# cookieString (sessão do ERP) e dados de funcionário.
ENV ELASTIC_APM_CAPTURE_BODY=off
# capture_headers tem default TRUE no agente, o que enviaria Authorization e
# Cookie para o APM. Desligado explicitamente.
ENV ELASTIC_APM_CAPTURE_HEADERS=false
# ATENÇÃO: definir sanitize_field_names SUBSTITUI a lista padrão do agente, por
# isso os valores padrão estão repetidos aqui junto das adições específicas
# deste projeto (cookiestring, syndata, cpf, client_secret).
ENV ELASTIC_APM_SANITIZE_FIELD_NAMES="password,passwd,pwd,secret,*key,*token*,*session*,*credit*,*card*,*auth*,set-cookie,pw,cc,cvv,cvc,cookiestring,syndata,client_secret,cpf"

# Amostragem: 1.0 durante a validação inicial, para garantir que toda transação
# apareça. Reduzir depois de confirmado o volume (ex.: 0.2 em produção).
ENV ELASTIC_APM_TRANSACTION_SAMPLE_RATE=1.0

# ---------------------------------------------------------------------------
# Usuario nao-root
# ---------------------------------------------------------------------------
# Rodar como root no container significa que uma execucao remota de codigo na
# aplicacao ja comeca com privilegio maximo dentro do namespace. Achado do SAST
# (regra missing-user-entrypoint).
#
# UID 10001 foi escolhido para nao colidir com usuario existente na imagem base
# Ubuntu: 1000 e o primeiro UID que uma distro atribui e pode estar ocupado
# dependendo da tag do temurin.
#
# /app/logs e criado e chowneado AQUI porque o Docker copia a propriedade do
# diretorio da imagem ao inicializar um volume nomeado NOVO. Volume que ja
# existe mantem a propriedade antiga -- ver docs/CI-PRE-REQUISITOS.md.
RUN groupadd --gid 10001 bff \
 && useradd --uid 10001 --gid 10001 --home-dir /app --shell /usr/sbin/nologin bff \
 && mkdir -p /app/logs \
 && chown -R bff:bff /app \
 && chmod 0644 /apm-agent.jar

USER bff

ENTRYPOINT ["java", \
            "-javaagent:/apm-agent.jar", \
            "-Duser.timezone=America/Sao_Paulo", \
            "-jar", "app.jar"]
