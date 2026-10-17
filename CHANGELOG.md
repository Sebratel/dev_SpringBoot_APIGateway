# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o
versionamento segue [Semantic Versioning](https://semver.org/lang/pt-BR/).

## Contrato de versionamento

A "API pública" versionada aqui são os **endpoints HTTP expostos pelo BFF** e o
formato dos seus payloads de requisição e resposta. Portanto:

- **MAJOR** — remoção de endpoint, remoção/renomeação de campo de resposta sem alias,
  ou mudança incompatível no esquema de autenticação.
- **MINOR** — novo endpoint, novo campo opcional, novo alias de rota, ou mudança
  interna relevante (troca de infraestrutura, cache, banco) sem quebra de contrato.
- **PATCH** — correção de comportamento, log, timezone, arredondamento ou build.

## [Não lançado]

### Obsoleto
- **134 das 154 rotas** passam a responder com o header `Deprecation: true` (RFC 9745), mais
  `Link: <rota-sucessora>; rel="successor-version"` quando o mesmo handler tem um alias em uso (36
  rotas), e aparecem como `deprecated` no Swagger/OpenAPI. São as rotas **sem nenhuma requisição** em
  staging no Kibana (2026-08-19 a 2026-10-17: os logs do BFF só existem desde 19/08, cerca de 2 meses, e
  não 3). Nenhuma rota foi removida e nenhum status ou corpo mudou. A lista está em
  `src/main/resources/deprecated-routes.txt` e um teste garante que ela é exatamente o conjunto de rotas
  com 0 requisições em `docs/uso-endpoints-elk.csv`.
- Cada chamada a uma rota obsoleta gera uma linha no log `DEPRECATED_ROUTE` (método, rota, sucessora),
  para localizar consumidores esquecidos antes de qualquer remoção.
- `Deprecation`, `Sunset` e `Link` passam a ser expostos no CORS, para o front poder lê-los.
- Propriedades opcionais: `deprecation.since` (data ISO; vira `Deprecation: @epoch`) e `deprecation.sunset`
  (data ISO; emite o header `Sunset`). Padrão: **`deprecation.since=2026-10-17`** e **`deprecation.sunset=2027-01-17`**
  (3 meses depois), ajustáveis por `DEPRECATION_SINCE` / `DEPRECATION_SUNSET` nos `docker-compose*.yml`. A rota só pode ser
  removida depois do `Sunset`, e uma remoção é MAJOR.

### Removido
- **Endpoint de QR Code** (`GET /api/v1/qr-code/gerar` e `POST /api/v1/qr-code/recuperar`), o serviço, os DTOs, a
  dependência `zxing` e o par de chaves RSA (`id_rsa_private.pem` e `id_rsa_public.pem`) de `src/main/resources`.
  Eram 2 das 156 rotas, ambas sem nenhuma requisição no período levantado (Kibana, 2026-08-19 a 2026-10-17). Mudança
  **MAJOR** pelo contrato acima. A chave privada continua no histórico do Git (finding F-01): rotacionar.

### Adicionado
- Especificação da transformação do BFF em API Gateway: `specs/API_GATEWAY.md`
  (arquitetura, decisões, estrutura de pastas, riscos), `specs/API_GATEWAY_PASSOS.md`
  (fases, ordem de extração, checklist) e `specs/API_GATEWAY_ENDPOINTS.md` (inventário
  das 156 rotas que devem permanecer idênticas). Somente documentação; nenhum
  comportamento mudou.
- Fase 0 da migração: `RouteManifestTest` no monólito gera por reflexão o manifesto das 156 rotas
  (`docs/contract/manifest.json`) e falha se uma rota mudar sem o manifesto ser atualizado. O manifesto é o
  contrato que o gateway precisa preservar (ver `docs/contract/README.md`).
- O gateway e cada serviço de domínio vivem em **repositórios próprios** (lista em `specs/API_GATEWAY.md`, seção 6);
  este repositório continua sendo o monólito. Nenhum comportamento do monólito mudou.

- `apigateway` incorpora `origin/staging` (GMUD: `/api/v1/gmud/*`, 6 rotas, em uso e fora do inventário
  inicial); o manifesto passa a ter **156 rotas** (`massivas` 74), com o `GmudEllevenController` no
  `massivas-service`.
- `docs/USO-ENDPOINTS-ELK.md` e `docs/uso-endpoints-elk.csv`: uso dos endpoints no Kibana (ago a out/2026).

### Planejado (ver `specs/API_GATEWAY_PASSOS.md`)
- `3.4.3` — reestruturação multi-módulo, sem mudança de comportamento.
- `3.5.0` — gateway à frente do monólito (circuit breaker, rate limit, request-id).
- `3.5.x`/`3.6.0` — extração de `comercial`, `estoque`, `pessoas`, `radius`,
  `splitters` e `massivas` para serviços próprios, mantendo as mesmas URLs e payloads.
- `3.7.0` — remoção do `legacy-bff` e endurecimento de segurança (F-03).

## [3.4.2] - 2026-07-30

### Modificado
- `GET /api/v1/matrix` passou a consultar a massiva de cada contrato pelo
  `AffectedUserService` — o mesmo que atende `GET /api/v1/afetados/contract/{id}` —
  em vez de acessar o repositório direto. Os dois endpoints respondem a partir da
  mesma fonte. A semântica é a de antes: se qualquer contrato do CPF estiver em
  massiva, responde `client_found`, parando no primeiro encontrado.
- `estimateTimeOfRestoration` do `AffectedUserService` passou a arredondar para
  cima, com fallback de 1 hora para massiva vencida (antes: `floor` via
  `ChronoUnit.HOURS.between` e fallback 2). Alinha o cálculo ao que a `3.1.1`
  estabeleceu para o `MatrixService` e elimina a divergência entre os dois
  endpoints. **Consumidores de `/api/v1/afetados/**` podem ver valores até 1 hora
  maiores**; informar 1h para uma massiva com 1h50 restantes subestimava a
  previsão passada ao cliente.

### Corrigido
- Um contrato sem massiva não interrompe mais a varredura dos contratos seguintes.
  O `ResourceNotFoundException` é tratado dentro do laço, e não pelo `catch`
  genérico do `getContractInfoByCPF`.

## [3.4.1] - 2026-07-30

### Corrigido
- `GET /api/v1/matrix` reportava `not_found_client` para CPF com mais de um
  contrato. A query `findContractByCPF` devolvia `Optional<ContractProjection>`,
  então mais de uma linha estourava `IncorrectResultSizeDataAccessException`; a
  exceção era engolida pelo `catch (Exception)` do `MatrixService` e o cliente
  afetado saía como não encontrado. A query passou a devolver `List`, com
  `DISTINCT` (o join com `authentication_contracts` duplicava linhas do mesmo
  contrato) e `ORDER BY` para resultado determinístico.
- O `MatrixService` agora percorre todos os contratos do CPF e responde
  `client_found` no primeiro com massiva ativa, em vez de considerar apenas um.

## [3.4.0] - 2026-07-30

### Corrigido
- `/error` liberado no `SecurityConfig`. O dispatch de erro do Spring passa pelo
  filtro de segurança e caía em `.anyRequest().authenticated()`, então um 500 real
  era convertido em `401 "Acesso restrito"` e a causa original se perdia. Afetava
  em especial os endpoints públicos `GET /api/v1/afetados/contract/**` e
  `GET /api/v1/matrix`, que não têm token para o dispatch de erro carregar.
- Log do Spring Security deixou de ser `DEBUG` fixo e passou a respeitar
  `${SECURITY_LOG_LEVEL:INFO}`. As duas linhas sobrescreviam `logging.level.root`
  sem condição, então baixar `LOG_LEVEL` não silenciava o `FilterChainProxy` —
  que despejava `DEBUG ... Securing GET /error` em produção.

### Modificado
- Os quatro endpoints de busca de usuários impactados (`GET /`, `/pppoe/{pppoe}`,
  `/contract/{contractId}`, `/protocol/{protocol}`) passam a responder `404` em vez
  de `500` quando a busca não encontra usuários, e as mensagens deixaram de começar
  com "Error" — o texto anterior fazia buscas full-text no Kibana classificarem
  buscas vazias como erro. `POST`, `DELETE` e `PATCH` seguem retornando 400/500,
  já que falha de escrita não é ausência de dado.

## [3.3.1] - 2026-07-27

### Corrigido
- Níveis e mensagens de log `warn`/`error` da aplicação.
- `affectedUser` não encontrado deixou de ser logado como erro.

## [3.3.0] - 2026-07-22

### Adicionado
- Endpoint de leitura de `massiva_history` com reconciliação Splitters × Voalle.
- Reconciliação bidirecional de massivas (fecha o registro nos dois lados).
- Endpoint de previsão de finalização de massiva por contrato (Voalle).

### Modificado
- A previsão por contrato passou a devolver `ImpactedUsersOutputDTO`, o mesmo
  formato do endpoint de usuário afetado, para que a Native leia a resposta sem
  alteração no template.

## [3.2.0] - 2026-07-15

### Adicionado
- Consulta de employee por contrato.

### Modificado
- Nome de employee passou a priorizar o primeiro nome.
- Removida mensagem indesejada do gateway no Kibana.

## [3.1.1] - 2026-07-09

### Corrigido
- `resolutionTime` do `MatrixService` passou a ser arredondado para cima.

## [3.1.0] - 2026-07-09

### Adicionado
- Detalhes do erro nas respostas HTTP 500.

### Modificado
- Redis e a camada de cache removidos; o token de integração Elleven passou a
  usar cache em memória.

## [3.0.2] - 2026-07-08

### Corrigido
- Timezone GMT-3 forçado no nível da JVM, eliminando a dependência do relógio
  do servidor.

## [3.0.1] - 2026-07-07

### Corrigido
- Timezone GMT-3 no endpoint de matrix.

## [3.0.0] - 2026-07-02

### Removido
- **BREAKING** — endpoint `inactivate-account`, junto de controller, DTO, entity,
  repositories e testes. Ele existia apenas para publicar/consumir eventos Kafka.
- **BREAKING** — integração Kafka por completo: dependência `spring-kafka`,
  configuração e serviços `kafka`/`zookeeper` do docker-compose. O Kafka não
  estava em execução em nenhum ambiente e poluía o log com reconexões.

### Corrigido
- SQL inválido e incompatibilidade de tipo em `EmployeeRepository.findByTxId`.

## [2.1.1] - 2026-06-29

### Modificado
- Ajustes no `docker-compose.yml`.

## [2.1.0] - 2026-05-14

### Adicionado
- Basic auth obrigatório para acesso ao Swagger UI.

### Corrigido
- Swagger UI voltou a funcionar após atualização de dependências e da
  configuração de security.
- `spring-boot-starter-webflux` readicionado para restaurar as classes `WebClient`.

## [2.0.0] - 2026-05-08

### Removido
- **BREAKING** — endpoints DHO removidos do BFF (`DhoProxyController`, `DhoClient`,
  `EmployeeEntity`, `EmployeeRepository`); a responsabilidade passou para uma API
  dedicada.
- Suíte de testes Cucumber e sua configuração.

### Modificado
- Reorganização dos controllers e das configurações de security.
- Senhas retiradas do `application.properties`.
- `findFirstByContractId` adotado na busca de contratos.

### Corrigido
- `NullPointerException` quando `affectedUsers` era nulo em
  `AdicionarMassivaNoEllevenApiService`.
- Erros do AspectJ weaver e configurações de teste ausentes.

## [1.6.0] - 2026-04-30

### Adicionado
- Descrição nas massivas recuperadas por banco.
- Eventos finalizados incluídos na ordenação.

### Corrigido
- Otimização da query de massivas.

## [1.5.0] - 2026-04-22

### Modificado
- DHO extraído para microserviço dedicado, com o BFF atuando como proxy.

### Adicionado
- Testes de integração para as rotas DHO.

## [1.4.0] - 2026-04-17

### Adicionado
- Entidades, repositories, services e controllers do schema `DHO_Application`.
- `FinishLinkedProtocolsService`, integrado ao fluxo de finalização.

### Corrigido
- Tratamento de erro na finalização de massivas.
- Conflito de definição de bean em `EmployeeRepository`.
- Patch na configuração de CORS.

## [1.3.0] - 2026-04-14

### Adicionado
- Integração DHO: opportunities, users e settings.
- Filtragem e formatação de erros de banco.

### Corrigido
- Constraint `created_by` nula e mapeamento de DTO.
- Mensagens do global exception handler.

## [1.2.0] - 2026-04-10

### Adicionado
- Rollback dos serviços Voalle quando `userCount` é 0 ou ocorre qualquer erro.
- Filas para o fluxo de inativação de conta (producer/consumer).
- `clientType` na requisição de usuários afetados.

### Modificado
- Rotas de affected-users em inglês, mantendo os aliases em português
  (`/api/v1/impacted-users`, `/api/v1/usuario-afetado`, `/api/v1/afetados`).

## [1.1.0] - 2026-04-06

### Adicionado
- Rotas em inglês para todos os controllers, **mantendo os aliases em português** —
  mudança retrocompatível.
- `status` nas respostas das chamadas de matrix.
- Cobertura de testes ampliada (controllers, 80% em services) e suíte Cucumber.

### Corrigido
- Campos de matrix em erros de massiva.

## [1.0.0] - 2026-04-02

Primeira versão estável em produção.

### Adicionado
- Pipeline de CI/CD para a branch `staging` com verificação de versão, Maven
  wrapper e JDK 21.
- Nome e e-mail do solicitante na criação e exclusão de eventos massivos.
- Recuperação de todos os tipos de massiva.

### Modificado
- Containers renomeados com os nomes de produção; docker-compose alinhado ao
  Portainer para stage e produção.
- Relatório semanal traduzido para inglês.

---

Versões `0.x` correspondem ao desenvolvimento inicial, quando o contrato dos
endpoints ainda mudava livremente.

## [0.10.0] - 2026-03-31
- Endpoint de matrix, basic auth e cache desabilitado para serviços em tempo real.
- Correção do tempo estimado de restauração dos usuários impactados.

## [0.9.0] - 2026-03-18
- Autenticação baseada em OAuth2; e-mail retirado do token, com o `idPeople`
  resolvido a partir dele.
- Paginação nos Splitters, SLA nas massivas por banco e dados de employee.

## [0.8.0] - 2026-03-13
- Integração Splitters: OLTs, recuperação de token, consulta de solicitações.
- CRUD de usuários afetados no MariaDB.
- Criação e finalização de registro massivo via API Elleven.
- Configuração de CORS e preflight para os endpoints web de massivas.

## [0.7.0] - 2026-03-10
- Deploy com Docker/Portainer, scheduling e volumes para logs.
- Redis e listagem de massivas com recuperação de token.

## [0.6.0] - 2026-03-04
- Criação de massivas na Elleven, com configuração dedicada de `WebClient`.
- Hierarquia de exceptions e `GlobalExceptionHandler`.
- `ApiResponse` padronizando as respostas.

## [0.5.0] - 2026-02-27
- Geração de QR Code.

## [0.4.0] - 2026-02-26
- Base de testes de controllers, services e utils, com relatórios Allure.
- Correções apontadas pelo Sonar e ajuste das variáveis de ambiente.
- Spring Boot fixado em 3.4.x.

## [0.3.0] - 2026-02-24
- Multi-datasource: separação entre banco primário (ERP) e secundário (Radius).
- Relatórios operacionais: aquisição, contratos bloqueados, contratos sem fatura,
  contratos ativos com títulos vencidos, vendedores ativos, clientes com nome
  duplicado, consumo acima de 1 TB, última conexão PPPoE.
- Swagger.

## [0.2.0] - 2026-02-19
- Apoio semanal com cache.
- Movimentação de estoque para o n8n.

## [0.1.0] - 2026-02-13
- Base do projeto: Spring Boot, configuração de security e endpoint de Estoque.
