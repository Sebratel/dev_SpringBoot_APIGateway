# Elastic APM — Configuração e Validação

Complemento operacional de [AUDITORIA-SEGURANCA.md](AUDITORIA-SEGURANCA.md).

## Configuração aplicada

| Item | Valor | Onde |
|---|---|---|
| Agente | Elastic APM Java Agent **1.56.0** | `Dockerfile` (ARG `ELASTIC_APM_AGENT_VERSION`) |
| Service name | `api-gateway` | `Dockerfile` / `docker-compose.yml` |
| Server URL | `http://apm-server:8200` | rede Docker interna |
| Environment | `production` / `staging` | por compose |
| Application packages | `br.com.sebratel` | package raiz **real** do projeto |
| Rede | `elk-network` → `elk_es_network` (external) | `docker-compose.yml` |
| Sample rate | `1.0` inicialmente | reduzir após medir volume |

A versão do agente foi verificada como a release atual, não assumida. Para atualizar:

```bash
docker compose build --build-arg ELASTIC_APM_AGENT_VERSION=<versao>
```

O agente é baixado via `mvn dependency:copy` no estágio de build, aproveitando o mesmo
cache `.m2` do resto da compilação.

## Rede Docker

O stack é GitOps: a rede está declarada no repositório, **não** criada à mão no Portainer.
As redes `proxy` e `default` foram preservadas — nada foi removido.

Verificação (já executada, retorna exit 0):

```bash
docker compose config
```

Saída esperada na seção `networks`:

```text
elk-network:
  name: elk_es_network
  external: true
proxy:
  name: proxy
  external: true
default:
  name: dev_springboot_apigateway_default
```

O serviço `bff-app` aparece anexado às três redes.

## Proteção de dados sensíveis

Três decisões deliberadas, todas no `Dockerfile`:

| Configuração | Valor | Motivo |
|---|---|---|
| `ELASTIC_APM_CAPTURE_BODY` | `off` | corpos contêm CPF (`MatrixMassiveInputDTO`), `cookieString` (sessão do ERP) e dados de funcionário |
| `ELASTIC_APM_CAPTURE_HEADERS` | `false` | o **default do agente é `true`**, o que enviaria `Authorization` e `Cookie` ao APM |
| `ELASTIC_APM_SANITIZE_FIELD_NAMES` | lista explícita | definir esta opção **substitui** a lista padrão, por isso os defaults estão repetidos junto das adições (`cookiestring`, `syndata`, `client_secret`, `cpf`) |

> **Pré-requisito cumprido:** o finding F-06 (`log.info(token)` em
> `FinalizarMassivaNoEllevenApiService`) foi corrigido antes de ligar o APM. Sem isso, o
> token bearer da integração Elleven seria enviado ao Elastic junto com os logs.

## Clientes HTTP e `traceparent`

O gateway usa **um único cliente HTTP**: `WebClient` (Spring WebFlux sobre Reactor Netty).
Não há RestTemplate, Feign, OkHttp nem Apache HttpClient no código de aplicação.

| Destino | Natureza | Propagação desejada? |
|---|---|---|
| `erp.sebratel.net.br:45700` (OAuth token) | interno Sebratel | sim |
| `erp.sebratel.net.br:45715` (API integração) | interno Sebratel | sim |
| `erp-staging.sebratel.net.br` / `:45701` | interno Sebratel | sim |
| `www.googleapis.com/oauth2/v3/certs` (JWKS) | **terceiro (Google)** | **validar** |

**Nenhuma instrumentação foi desabilitada.** Não há evidência técnica de problema, e
desligar preventivamente contrariaria o objetivo da instrumentação.

O único ponto a confirmar empiricamente é o JWKS do Google: a busca é feita pelo
`NimbusJwtDecoder` (não pelo `WebClient`), e o agente pode instrumentá-la adicionando
`traceparent`. A expectativa é que o Google ignore o header, mas **isso é expectativa, não
fato verificado** — o passo 7 da validação abaixo existe para confirmar. Se houver
problema, a correção é seletiva (uma instrumentação específica), nunca global.

## Roteiro de validação

Executar após o deploy. O APM **não deve ser considerado concluído** enquanto os 8 passos
não passarem.

### 1. Agente iniciou

```bash
docker logs bff-java-service 2>&1 | grep -i "elastic apm"
```
Esperado: linha de inicialização com a versão do agente. Nenhum `ERROR`.

### 2. Conectividade com o APM Server

```bash
docker exec bff-java-service sh -c "getent hosts apm-server"
```
Deve resolver para um IP da rede `elk_es_network`.

### 3. Gerar tráfego real

```bash
curl -i https://<host-do-gateway>/api/v1/afetados/contract/1
```
Rota pública — não exige credencial e não altera dados.

### 4. Serviço aparece no APM

Kibana → APM → Services. Deve existir `api-gateway`, environment `production`.

### 5. Transações, latência e throughput

Abrir `api-gateway` → aba Transactions. Confirmar:
- a transação da rota chamada aparece;
- latência (p95/p99) está sendo registrada;
- throughput contabiliza as chamadas.

### 6. Erros

Provocar um erro controlado (ex.: rota inexistente sob autenticação) e confirmar que
aparece em Errors, com stack trace.

### 7. Spans e propagação de `traceparent` — **passo crítico**

Disparar uma rota que chame o ERP (ex.: abertura de massiva em ambiente de teste) e, no
waterfall da transação:

- confirmar spans de saída do tipo `external/http`;
- confirmar que o span do ERP carrega o trace ID;
- **verificar o comportamento da chamada ao JWKS do Google**: se houver span para
  `googleapis.com`, confirmar que a chamada teve sucesso (HTTP 200). Qualquer falha de
  autenticação após ligar o agente aponta para o `traceparent` como suspeito.

Confirmação do lado do ERP (se houver acesso ao log do Elleven): verificar se o header
`traceparent` chegou e foi aceito.

### 8. Ausência de dados sensíveis

No Kibana, abrir o documento bruto de uma transação e confirmar que **não** existem:
`Authorization`, `Cookie`, `cookieString`, CPF, `client_secret`, corpo de requisição.

Se qualquer um aparecer, desligar (`ELASTIC_APM_ENABLED=false`), corrigir a sanitização e
só então religar.

## Rollback

```bash
# Desliga o agente sem rebuild nem redeploy da imagem
ELASTIC_APM_ENABLED=false docker compose up -d bff-app
```

O agente continua na imagem, apenas inativo. Para remover completamente, reverter o
`ENTRYPOINT` do `Dockerfile`.

## Após a validação

1. Reduzir `ELASTIC_APM_TRANSACTION_SAMPLE_RATE` (ex.: `0.2`) conforme o volume observado.
2. Usar os dados de latência das chamadas de saída para dimensionar o timeout do
   `WebClient` (finding **F-15**) — hoje não há timeout algum, e o valor correto deve vir
   da medição, não de um palpite.
3. Usar o `AuthAuditFilter` (findings **F-02** e **F-04**) em conjunto com o APM para
   mapear os consumidores reais antes de endurecer as regras de autenticação.
