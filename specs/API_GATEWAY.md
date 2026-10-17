# Spec: Transformar o BFF em API Gateway

- **Status:** Proposta (rascunho para revisão)
- **Autor:** Israel da Rosa Barcelos / Claude
- **Escopo:** repositório `Sebratel/dev_SpringBoot_APIGateway` (artefato atual `br.com.sebratel:bff`)
- **Documentos relacionados:** [Inventário de endpoints](API_GATEWAY_ENDPOINTS.md) ·
  [Passos de execução](API_GATEWAY_PASSOS.md) · [CHANGELOG](../CHANGELOG.md)

## 1. Problema

Hoje o `bff` é um monólito: 24 controllers, 156 rotas, 3 datasources (ERP, Afetados, Radius),
integrações com Elleven/Native e a regra de negócio de todos os domínios no mesmo deploy.
Ele se chama gateway, mas executa a lógica. Consequências:

- Qualquer mudança (ex.: relatório comercial) exige deploy do pacote inteiro, inclusive das massivas, que são críticas.
- Uma consulta pesada de Radius ou do ERP consome o mesmo pool de threads/conexões que as massivas.
- Os timeouts de 600 s, necessários para relatórios, ficam aplicados a tudo.
- Segurança, CORS, auditoria e lógica de domínio estão misturados, dificultando mudar um sem tocar o outro.

## 2. Objetivo

1. Mover **toda** a lógica de domínio para serviços independentes.
2. Deixar o projeto atual **apenas** como API Gateway: roteamento, autenticação, políticas transversais e observabilidade.
3. Manter os **156 endpoints atuais** idênticos para os consumidores (apps, n8n, chatbot, planilhas), sem mudança de URL, payload ou status.

### Não-objetivos
- Redesenhar a API, remover aliases PT/EN ou versionar para `v2` (fica para uma fase posterior, com depreciação).
- Trocar bancos, consolidar schemas ou migrar dados.
- Reescrever regras de negócio: o código é **movido**, não reimplementado.
- Introduzir service mesh, Kubernetes ou mensageria nesta fase.

## 3. Responsabilidades

### 3.1 O que o Gateway faz (e só isso)
| Capacidade | Detalhe |
|---|---|
| Roteamento | Rota por método + path + sufixo para o serviço dono (ver §5). Inclui aliases PT/EN |
| Autenticação | Valida JWT do Google (issuer + domínio `@sebratel.com.br`) e Basic legado; `permitAll` apenas nas 2 rotas atuais |
| Propagação de identidade | Repassa `Authorization` ao serviço e injeta `X-Request-Id`/`X-Correlation-Id` |
| CORS | Centralizado aqui (hoje `*`); serviços não expõem CORS |
| Resiliência | Timeout por rota, retry só em GET idempotente, circuit breaker por serviço, fallback 503 padronizado |
| Rate limiting | Por usuário/IP, limites por rota (relatórios pesados mais restritos) |
| Observabilidade | Logs estruturados com request-id, métricas por rota, tracing, healthcheck agregado |
| Auditoria | Porta o `AuthAuditFilter` atual |
| Documentação | Agrega os OpenAPI dos serviços em `/v3/api-docs` e `/swagger-ui.html` |
| Compatibilidade | Preserva GET com corpo, `Idempotency-Key` e timeouts longos |

### 3.2 O que o Gateway **não** faz
Nenhum acesso a banco, nenhum `WebClient` para Elleven/ERP, nenhum DTO de domínio, nenhuma
regra de negócio, nenhum `@TokenRetry`. O gateway não tem `spring-boot-starter-data-jpa`
nem drivers JDBC.

## 4. Arquitetura-alvo

```
 Apps / n8n / chatbot
          │  (mesmas URLs de hoje)
          ▼
 ┌────────────────────────────────────────────┐
 │ api-gateway  (Spring Cloud Gateway)        │
 │  auth · CORS · rate-limit · CB · retry     │
 │  request-id · audit · swagger agregado     │
 └──┬─────┬─────┬─────┬─────┬─────┬───────────┘
    ▼     ▼     ▼     ▼     ▼     ▼
  MAS    SPL   COM   RAD   EST   PES   ← serviços Spring Boot independentes
   │      │     │     │     │     │
 Afet.  Elleven ERP  Radius ERP   ERP
 +ERP   +ERP
 +Elleven/Native
```

### Decisões de arquitetura
| # | Decisão | Motivo | Alternativa descartada |
|---|---|---|---|
| D1 | **Spring Cloud Gateway** (compatível com Spring Boot 3.5) | Mesmo ecossistema/stack da equipe; filtros nativos para CB, retry, rate limit | Kong/NGINX: mais operação nova; Zuul: legado |
| D2 | Rotas **declarativas em YAML**, com um `RouteLocator` Java só para casos especiais | Rota nova vira configuração, não código | Tudo em Java |
| D3 | Serviços **também validam o JWT** (resource server) | Os serviços usam claims do token (solicitante por `personId`, `GetToken`/`JwtInformation`); evita confiar cegamente na rede interna | Header interno confiável: quebra a resolução de solicitante e é frágil |
| D4 | Serviços **só acessíveis pela rede interna** do Docker (sem porta publicada) | O gateway é o único ponto de entrada | Portas expostas por serviço |
| D5 | **Um repositório por componente** (gateway, cada serviço, bibliotecas comuns) | Ciclo de release, deploy, permissões e CI independentes por serviço; o monólito segue no repositório atual | Monorepo multi-módulo (era a decisão original, abandonada em 2026-10-17) |
| D6 | **Strangler**: gateway nasce com *todas* as rotas apontando para o monólito, que é migrado domínio a domínio trocando apenas o `uri` da rota | Rollback = reverter 1 linha de config; sem big-bang | Reescrita e virada única |
| D7 | Cada serviço mantém o **mesmo envelope de erro** (`ApiError`/`ApiResponse`) em módulo `common` | Contrato de erro idêntico | Cada serviço com seu formato |
| D8 | Gateway **reativo (WebFlux)**: não há JPA nele, então não há bloqueio | Requisito do Spring Cloud Gateway padrão | Gateway MVC: aceito se a equipe preferir, mas ainda menos maduro |

## 5. Decomposição em serviços

Critério: um serviço só reúne endpoints que **compartilham dado, regra e motivo para mudar**. Endpoints que
só coincidem em prefixo de URL ou no banco ERP ficam em serviços separados.

| Sigla | Serviço | Conteúdo (origem no código atual) | Dados / integrações | Porta | Rotas |
|---|---|---|---|---|---|
| MAS | `massivas-service` | Usuários afetados, massivas (Elleven/ERP/Native), histórico de massivas, GMUD, Matrix, sites de autenticação; `service/massivas/*`, `MassivaIdempotencyStore` | Afetados DB, ERP, Elleven, Native | 8101 | 74 |
| SPL | `splitters-service` | Splitters, OLTs, conexões, solicitações de cliente, alertas de protocolo pendente (`SplittersMapper`) | Elleven, ERP | 8102 | 8 |
| COM | `comercial-service` | Vendas (vendedores ativos, aquisição, primeiro pagante mensal, relatório semanal de apoio) e contratos (bloqueados, ativação, ativação c/ fatura pendente, sem fatura, primeiro pagamento, clientes com nome duplicado); `dto/comercial` | ERP | 8103 | 45 |
| RAD | `radius-service` | Consumo alto, última conexão, primeira autenticação, calling-station e prefixo duplicados | Radius DB (+ leitura de plano no ERP) | 8104 | 14 |
| EST | `estoque-service` | Estoque por técnico, movimentação, patrimônio pendente | ERP | 8105 | 8 |
| PES | `pessoas-service` | Colaborador por token/email/CPF/contrato, corporativo, QR Code do funcionário (chaves RSA) | ERP | 8106 | 7 |

Módulos compartilhados (bibliotecas, sem endpoints): `common-web` (envelope/erros), `common-security`
(JWT, `GetToken`, `JwtInformation`) e `common-elleven` (cliente Elleven, token, `@TokenRetry`; usado por
`MAS` e `SPL`, que são os únicos que falam com o Elleven).

### Separações feitas por falta de coesão
| Antes (agrupamento óbvio) | Decisão | Motivo |
|---|---|---|
| “Comercial” e “Contratos” como serviços separados | **Unidos** em `COM` | `WeeklyReportService` (vendas) e `ContractActivationService` (contrato) compartilham o `ContractDataService`; separar duplicaria a regra ou criaria chamada entre serviços. Ambos são só leitura do ERP |
| `/api/v1/sites` junto de pessoas | **`MAS`** | É lookup de POP/DC consumido na abertura de massiva (`authenticationSiteCode`) |
| `QrCode` junto de relatórios | **`PES`** | Identidade do funcionário e chaves RSA; nada a ver com relatórios |
| `last-connections` sob `/contracts` | **`RAD`** | Dado vem do Radius; o prefixo `/contracts` é só herança de URL |
| `/massivas-splitters` perto de splitters | **`MAS`** | É o histórico de massivas (banco de afetados); só o nome do alias lembra splitters |
| `consumo` (Radius + plano do ERP) | **`RAD`** | Fonte principal é Radius; o ERP entra só como leitura de plano |
| Elleven duplicado em dois serviços | **`common-elleven`** | `MAS` e `SPL` compartilham cliente/token em vez de copiar |

Pontos a validar na fase 0 (spike):
- `MatrixService` usa `PersonRepository` + `AffectedUserService` (só `MAS` os usa). `ContractDataService` é usado só dentro de `COM`, o que confirma a união. `PlanoRepository` (ERP) é usado só pelo `ConsumoService`, em `RAD`: leitura, sem serviço compartilhado.
- `MAS` fala com **dois** bancos (Afetados e ERP) e com Elleven; mantê-los no mesmo serviço evita transação distribuída.
- Entidades/repositórios ERP usados por mais de um serviço (`PersonRepository`, `ContractProjection`) ficam em módulo de leitura, nunca com escrita compartilhada.
- `application.properties` documenta Afetados como MariaDB e Radius como PostgreSQL, enquanto `specs/TEMPLATE.md` diz o contrário. **Verificar** e corrigir o template.

## 6. Repositórios e estrutura

Cada componente vive no **seu próprio repositório** (decisão D5, revista em 2026-10-17). Todos são privados, herdam
de `br.com.sebratel:sebratel-parent` (`Sebratel/dev_Maven_Parent`) e seguem os mesmos templates de repositório.

| Repositório | Conteúdo | Porta |
|---|---|---|
| `dev_SpringBoot_APIGateway` (este) | O **monólito atual** (`legacy-bff`), as specs da migração e o manifesto das rotas (`docs/contract/manifest.json`) | 8085 |
| `dev_SpringBoot_Gateway` | `api-gateway` (Spring Cloud Gateway) e o módulo `contract-tests` | 8085 |
| `dev_SpringBoot_MassivasService` | `massivas-service` (nome sem sufixo já era usado por outro repositório) | 8101 |
| `dev_SpringBoot_SplittersService` | `splitters-service` | 8102 |
| `dev_SpringBoot_ComercialService` | `comercial-service` (vendas + contratos) | 8103 |
| `dev_SpringBoot_RadiusService` | `radius-service` | 8104 |
| `dev_SpringBoot_EstoqueService` | `estoque-service` | 8105 |
| `dev_SpringBoot_PessoasService` | `pessoas-service` | 8106 |
| `dev_Maven_Commons` | Bibliotecas `common-web`, `common-security` e `common-elleven`, publicadas no GitHub Packages | n/a |

Fluxo de dependência: `dev_Maven_Parent` → `dev_Maven_Commons` → serviços e gateway. Os serviços só passam a depender
dos `common-*` quando houver código a compartilhar (hoje são esqueletos sem lógica).

Cada serviço mantém o layout atual (`controller → service → repository`, DTOs, projections). Pacote base:
`br.com.sebratel.<servico>`. O arquivo `contract/routes.json` de cada repositório lista as rotas que ele deve atender,
extraídas do manifesto.

Ainda a criar: `legacy-bff` como alias do monólito, `deploy/` (compose com rede interna) e `common-erp` (se o spike
confirmar entidades ERP compartilhadas).

## 7. Exemplo de configuração de rota

```yaml
# gateway/src/main/resources/routes/comercial.yml
spring.cloud.gateway.routes:
  - id: contratos-bloqueados
    uri: ${COMERCIAL_URL:http://comercial-service:8103}
    predicates:
      - Method=GET
      - Path=/api/v1/{base:contracts|contratos|contrato-bloqueado}/{sufixo:blocked|bloqueados}
    metadata:
      response-timeout: 600000
    filters:
      - name: CircuitBreaker
        args: { name: comercial, fallbackUri: forward:/fallback/comercial }
      - name: Retry
        args: { retries: 2, methods: GET, statuses: BAD_GATEWAY, backoff: { firstBackoff: 200ms } }
```

Regras gerais: `Retry` apenas para GET; **nunca** para POST/PATCH/DELETE (massivas têm efeito colateral
no ERP/Elleven; a idempotência fica no `Idempotency-Key`). Rotas são geradas pela tabela do inventário e
verificadas pelo teste de contrato; nada é roteado por prefixo amplo (ver armadilha 2 e 3 do inventário).

## 8. Requisitos e critérios de aceite

**Paridade de contrato (bloqueante)**
- RF1. As 156 rotas do inventário respondem no gateway com o mesmo status e corpo que no monólito, para um conjunto de requisições gravadas (golden files).
- RF2. GET com corpo funciona (`matrix`, `weekly-reports/vendedor`, `qr-code/gerar`).
- RF3. `Idempotency-Key` é respeitado: duas chamadas iguais pelo gateway resultam em uma execução no `MAS`.
- RF4. `permitAll` continua restrito a `GET /api/v1/afetados/contract/**` e `GET /api/v1/matrix`.
- RF5. Rota inexistente no inventário retorna 404 (não é repassada ao acaso).

**Gateway**
- RG1. Sem JWT válido → 401 com o texto atual (“Acesso restrito: Token inválido ou não fornecido.”).
- RG2. Serviço fora do ar → 503 padronizado em até o timeout da rota; CB abre após limite configurado.
- RG3. Todo log tem `requestId` do gateway ao serviço.
- RG4. Nenhuma dependência de JPA/JDBC/WebClient de domínio no módulo `gateway`.

**Serviços**
- RS1. Cada serviço sobe e passa nos testes herdados do monólito (os 30+ testes de controller/repo migram junto).
- RS2. Serviço não é alcançável de fora da rede Docker.
- RS3. Latência p95 adicionada pelo gateway ≤ 30 ms em rota sem processamento.

## 9. Segurança
- O finding **F-03** (`aud` do JWT não validada, ver `docs/AUDITORIA-SEGURANCA.md`) continua pendente: na migração a validação de JWT passa para `common-security`; **não** corrigir F-03 na mesma entrega do refactor para não derrubar front-ends sem o levantamento de client IDs. Registrar como item seguinte.
- Segredos (`SECURITY_USER/PASSWORD`, `ELLEVEN_*`, `GOOGLE_*`, credenciais de banco) são distribuídos **por serviço**, só o que cada um usa; o gateway fica sem credenciais de banco/Elleven.
- `src/main/resources/id_rsa_*.pem` estão versionados no repositório; migrar para `pessoas-service` (QR Code) e tirar do controle de versão em seguida (segredo).
- `CORS: *` é mantido por compatibilidade na primeira fase, e centralizado só no gateway.
- Basic auth legado permanece até o `AuthAuditFilter` mostrar que nenhum consumidor o usa.

## 10. Riscos
| Risco | Impacto | Mitigação |
|---|---|---|
| Quebra silenciosa de rota/alias (156 rotas, muitos aliases) | Apps/n8n param | Gerar rotas a partir do inventário + teste de contrato em CI; comparar com `/v3/api-docs` do monólito |
| Proxy perde corpo em GET | Matrix/QR/relatório semanal quebram | Teste dedicado (RF2); fallback de `RouteLocator` Java |
| Timeouts curtos do gateway | Relatórios pesados falham | Timeout por rota ≥ 600 s onde hoje é assim |
| Divisão errada dos domínios (Matrix cruza afetados + contratos) | Dependência circular | Spike da fase 0; `MatrixService` fica no `MAS` |
| Duplicação de entidades ERP entre serviços | Divergência de schema | Módulo `common-erp` único, somente leitura |
| Retry duplicando efeito colateral | Massiva criada 2× | Retry só em GET; `Idempotency-Key` repassado |
| Mais peças para operar (7 containers) | Complexidade de deploy | Compose único, healthchecks, runbook; fases com rollback por rota |

## 11. Rollout e rollback
Migração por domínio, do menos crítico ao mais crítico (ver passos): relatórios → estoque/pessoas →
radius → splitters → massivas. Em cada domínio: serviço sobe, rota muda de `legacy-bff` para o serviço,
contrato verificado, observação por ≥ 1 semana. **Rollback** = apontar o `uri` da rota de volta ao
`legacy-bff` (variável de ambiente, sem redeploy de código).

## 12. Versionamento
Pelo contrato do `CHANGELOG.md` (API pública = endpoints HTTP), a migração sem quebra é **MINOR**
(`3.5.0`: mudança de infraestrutura sem quebra de contrato). Qualquer quebra detectada pelo teste de
contrato é bug de migração, não motivo de `4.0.0`.

## 13. Questões em aberto
1. Quem são todos os consumidores (apps, n8n, chatbot) e há tráfego fora da VPN que dependa de portas/hosts atuais?
2. Os serviços serão repositórios separados desde já ou só após estabilizar (D5)?
3. Há ambiente de stage com paridade para rodar o teste de contrato antes de produção?
4. O Basic auth ainda é usado por alguém (dados do `AuthAuditFilter`)?
5. `/api/v1/token/google` é realmente rota morta?
6. Confirmar a topologia de bancos (Afetados MariaDB vs Postgres) e quem escreve em `massiva_history`.
