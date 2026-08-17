# Auditoria Técnica — API Gateway / BFF Sebratel

**Repositório:** `dev_SpringBoot_APIGateway` (`br.com.sebratel:bff` 3.4.3-SNAPSHOT)
**Container:** `bff-java-service` · **Branch analisada:** `main` (commit `fbd83c8`)
**Data:** 2026-08-16
**Escopo:** ETAPA A (auditoria) — **nenhum arquivo foi modificado**

---

## FASE 1 — INVENTÁRIO DA APLICAÇÃO

### Stack

| Item | Valor |
|---|---|
| Java | 21 (Temurin) |
| Spring Boot | 3.4.3 (parent) |
| Spring Framework | 6.2.3 |
| Spring Security | 6.4.3 |
| Spring Cloud | **não usado** |
| Spring Cloud Gateway | **não usado** |
| Build | Maven (wrapper `mvnw` presente) |
| Módulos | monolito de módulo único |
| Servidor | Tomcat embedded 10.1.36 (Spring MVC, blocking) |

### Natureza real do serviço

**Este projeto não é um API Gateway no sentido de proxy/roteador.** É um **BFF (Backend-for-Frontend)**: 50 endpoints REST próprios que consultam 4 bancos de dados diretamente e chamam a API do ERP Elleven. Não há roteamento dinâmico, não há repasse genérico de requisições, não há `X-Forwarded-*` sendo interpretado pela aplicação.

Isso **elimina por construção** várias classes de risco listadas no escopo:
- request smuggling / HTTP desync → não há repasse de requisição bruta
- host header injection / bypass de proxy → não há roteamento por Host
- confiança em `X-Forwarded-For` / `X-Real-IP` → nenhum código lê esses headers (verificado por grep)
- SSRF por host controlado pelo usuário → **todas** as 14 URLs de saída são constantes literais

Ver a seção SSRF para a análise completa que sustenta essas conclusões.

### Estrutura

```
br.com.sebratel.bff
├── annotations/   TokenRetry
├── aspects/       TokenRetryAspect
├── config/        SecurityConfig, CorsConfig, WebClientConfig, SwaggerConfig,
│                  JacksonConfig, AuditConfig, AuditorAwareImpl,
│                  ErpDbConfig, RadiusDbConfig, UsuariosAfetadosDbConfig
├── controller/    7 controllers + controller/scripts/ (19 controllers de relatório)
├── dto/ enums/ exceptions/ mapper/ model/ repository/ service/ utils/
```

- **248** arquivos Java em `src/main`, **72** em `src/test`
- **50 endpoints**: 40 GET, 6 POST, 2 DELETE, 2 PATCH

### Componentes

| Componente | Situação |
|---|---|
| Filters custom | **nenhum** |
| Interceptors | **nenhum** |
| Proxy / roteamento | **nenhum** |
| Cliente HTTP | `WebClient` (WebFlux/Reactor Netty) — único cliente, usado em 12 services |
| RestTemplate / Feign / OkHttp / Apache HttpClient | não usados no código de aplicação |
| Bancos | **4 datasources**: ERP (PostgreSQL, `@Primary`), Afetados (MariaDB), Radius (PostgreSQL), DHO (MariaDB — **declarado nas properties mas sem `@Configuration`; datasource morto**) |
| Redis | **não existe** — `spring-boot-starter-cache` está no pom, mas não há `@EnableCaching`, não há `@Cacheable`, e não há dependência de Redis. As variáveis `REDIS_*` no `.env` são resíduo. |
| Filas | nenhuma |
| Autenticação | Google OIDC (resource server, JWT) **+ HTTP Basic com credencial estática compartilhada** |
| Tratamento global de exceções | `GlobalExceptionHandler` (`@RestControllerAdvice`) |
| Actuator | **ausente** — sem healthcheck, sem métricas |
| Migrations | Liquibase apenas em escopo `test` |
| Dockerfile | multi-stage (maven:3.9.6 → eclipse-temurin:21-jre-jammy) |
| docker-compose | `docker-compose.yml` (prod) + `docker-compose-stage.yml` |
| CI/CD | 3 workflows: `maven.yml`, `docker-image.yml`, `staging.yml` |
| Testes | 72 arquivos (JUnit 5, Mockito, Testcontainers, Allure, JaCoCo) |

---

## FASE 2 — AUDITORIA DE SEGURANÇA

### F-01 · Chave privada RSA versionada no Git e embarcada no JAR

```
ID              F-01
Severidade      CRITICAL
Status          CONFIRMED
Categoria       Secrets / Criptografia
Arquivo         src/main/resources/id_rsa_private.pem  (40 linhas, PKCS#8)
                src/main/resources/id_rsa_public.pem
Commit          b890577 "adicao de chave privada"
```

**Descrição.** A chave privada RSA usada por `QrCodeService` para cifrar/decifrar os dados de funcionário nos QR Codes está commitada no repositório e, por estar em `src/main/resources`, é **empacotada dentro do `app.jar`** e distribuída na imagem Docker.

**Impacto.** Qualquer pessoa com acesso ao repositório, a um clone, ao histórico Git, à imagem Docker ou ao container consegue extrair a chave e decifrar/forjar qualquer QR Code de funcionário. A chave está no histórico — remover o arquivo agora **não** a remove do histórico.

**Evidência.**
```
$ git ls-files | grep pem
src/main/resources/id_rsa_private.pem
src/main/resources/id_rsa_public.pem
$ git log --oneline --all -- src/main/resources/id_rsa_private.pem
b890577 adicao de chave privada
```
Uso em `QrCodeService.java:36` (`this.privateKeyPath = "id_rsa_private.pem"`), carregado via `ClassPathResource`.

**Explorabilidade.** Trivial para quem tem acesso ao repo/imagem. O algoritmo em si está correto (`RSA/ECB/OAEPWithSHA-256AndMGF1Padding` — sem padding oracle); o problema é exclusivamente a exposição da chave.

**Correção recomendada (mínima, sem refatoração):**
1. **Rotacionar o par de chaves** — a chave atual deve ser considerada comprometida. Este passo é obrigatório e independe do código.
2. Trocar `ClassPathResource` por leitura de caminho externo configurável, com fallback para o classpath preservando o comportamento atual:
   `qrcode.private-key-path=${QRCODE_PRIVATE_KEY_PATH:}` — se vazio, mantém o classpath.
3. Montar a chave nova como Docker secret / volume read-only, fora da imagem.
4. Remover os `.pem` do tracking (`git rm --cached`) e adicionar ao `.gitignore`.
5. Limpeza de histórico (`git filter-repo`) — decisão à parte, exige coordenação com o time (reescreve hashes).

**Risco da correção.** Baixo no código (uma property com fallback). O risco real está na rotação: QR Codes já emitidos com a chave antiga deixam de ser decifráveis. **Precisa de decisão do negócio** sobre período de transição (ex.: aceitar as duas chaves na decifragem durante N dias).

**Teste necessário.** `QrCodeServiceTest` já existe e usa o construtor com paths — estender com caso de path externo + caso de chave ausente.

---

### F-02 · HTTP Basic com credencial estática dá acesso total a todos os 50 endpoints

```
ID              F-02
Severidade      CRITICAL
Status          CONFIRMED
Categoria       Autenticação
Arquivo         src/main/java/br/com/sebratel/bff/config/SecurityConfig.java:43, 78-87
```

**Descrição.** A cadeia de filtros habilita `httpBasic(Customizer.withDefaults())` junto com o resource server OIDC. O `UserDetailsService` cria **um único usuário in-memory** com senha em `{noop}` (texto puro) vinda de `SECURITY_PASS`.

```java
.httpBasic(Customizer.withDefaults())          // linha 43
...
.password("{noop}" + password)                 // linha 82
.roles("USER")
```

Toda regra `.authenticated()` — incluindo `.anyRequest().authenticated()` — é satisfeita por esse Basic. Ou seja: **a restrição de domínio `@sebratel.com.br` validada no JWT é completamente contornável** por quem tiver essa credencial.

**Impacto.**
- Credencial única, compartilhada, sem expiração, sem rotação por usuário, sem rastreabilidade (todo acesso via Basic é o mesmo principal).
- Dá acesso a `DELETE /api/v1/afetados/protocol/{protocol}` (remoção de dados), aos endpoints de abertura/finalização de massivas no ERP de produção, ao Swagger e a `/v3/api-docs`.
- `JwtInformation.retrieveUserData()` retorna `Employee("", "")` quando o principal não é JWT → as ações ficam registradas **sem autor** no ERP (`AdicionarMassivaNoEllevenApiService:49` concatena nome/e-mail vazios na descrição do protocolo).
- Sem rate limiting nem lockout → a senha é força-brutável.

**Explorabilidade.** Alta *condicionada* à obtenção da credencial (`.env` no host, Portainer, CI, máquina de desenvolvedor). Não há barreira adicional depois disso.

**Correção recomendada.** Duas opções, em ordem de preferência:

*Opção A (menor risco, recomendada):* manter o Basic mas **restringi-lo a um conjunto explícito de rotas** que comprovadamente precisam dele (integrações máquina-a-máquina), e exigir JWT no resto. Requer levantar quem consome via Basic hoje.

*Opção B:* remover `httpBasic` inteiramente.

**Não aplicar nenhuma das duas sem antes mapear os consumidores.** Se algum sistema (n8n, script, app Flutter) autentica via Basic, remover quebra produção. Sugestão: **primeiro instrumentar** — logar qual mecanismo de autenticação foi usado por rota durante 1–2 semanas (mudança de risco zero), depois decidir com dados.

**Risco da correção.** Alto se feita às cegas; baixo se precedida da instrumentação.

**Teste necessário.** Teste de integração provando que rota protegida rejeita Basic (após a decisão) e aceita JWT válido.

---

### F-03 · JWT sem validação de `audience` — token de qualquer app Google é aceito

```
ID              F-03
Severidade      HIGH
Status          CONFIRMED
Categoria       Autenticação
Arquivo         src/main/java/br/com/sebratel/bff/config/SecurityConfig.java:67-76
```

**Descrição.**
```java
OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://accounts.google.com");
OAuth2TokenValidator<Jwt> sebratelValidator = new JwtClaimValidator<String>("email",
        email -> email != null && email.endsWith("@sebratel.com.br"));
```

`createDefaultWithIssuer` aplica **apenas** `JwtTimestampValidator` (exp/nbf) + `JwtIssuerValidator` (iss). **Não valida `aud`.** A assinatura é verificada corretamente contra o JWKS do Google, e o algoritmo é restrito pelo `NimbusJwtDecoder` — esses pontos estão OK.

**Impacto (confused deputy).** Qualquer ID token emitido pelo Google, para **qualquer** OAuth client ID no mundo, cujo `email` termine em `@sebratel.com.br`, é aceito. Cenário concreto: um funcionário Sebratel faz "Login com Google" em um site de terceiros; o operador daquele site recebe um ID token válido e pode **replayá-lo contra este gateway** obtendo acesso completo.

Também não há validação de `email_verified`, permitindo (em fluxos onde o Google não verifica) tokens com e-mail não confirmado.

**Correção recomendada (pequena e localizada).** Adicionar dois validadores ao `DelegatingOAuth2TokenValidator` existente:
```java
new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(googleClientId))
new JwtClaimValidator<Boolean>("email_verified", Boolean.TRUE::equals)
```
`GOOGLE_CLIENT_ID` já existe como variável de ambiente e já está mapeada em `spring.security.oauth2.client.registration.google.client-id`.

**Risco da correção.** **Médio — atenção.** Se o front-end web e o app mobile usam client IDs Google **diferentes** (o normal), validar contra um único `aud` derruba um dos dois. A correção deve aceitar uma **lista** de audiences (`GOOGLE_ALLOWED_AUDIENCES`, separada por vírgula) e o valor precisa ser levantado antes do deploy. Rollback: remover o validador.

**Teste necessário.** Teste unitário do bean `jwtDecoder` com token de `aud` permitido (aceita) e `aud` estranho (rejeita) — usando chave de teste local, sem tocar no Google.

---

### F-04 · Endpoint público `GET /api/v1/matrix` — oráculo de CPF não autenticado

```
ID              F-04
Severidade      HIGH
Status          CONFIRMED
Categoria       Autorização / Privacidade (LGPD) / Disponibilidade
Arquivo         SecurityConfig.java:49  ·  MatrixController.java:22-29  ·  MatrixService.java:29
```

**Descrição.**
```java
.requestMatchers(HttpMethod.GET, "/api/v1/matrix").permitAll()
```
```java
@GetMapping
public ApiResponse<MatrixMassiveOutputDTO> getMassiveInfo(@RequestBody MatrixMassiveInputDTO inputDTO) {
    ... matrixService.getContractInfoByCPF(inputDTO.getCpf()) ...
}
```

Qualquer pessoa na internet envia um GET com corpo `{"cpf":"..."}` e recebe `status: "client_found"` ou `"not_found_client"`.

**Impacto.**
1. **Oráculo de enumeração de clientes.** Permite confirmar se um CPF é cliente Sebratel. Dado que CPFs são enumeráveis/vazados em massa, isso permite construir listas de clientes — tratamento de dado pessoal sem base legal (LGPD).
2. **Amplificação de carga não autenticada.** Cada chamada dispara 2 queries no banco ERP de **produção** (`findByTxId` + `findContractsByCPF`) mais N consultas de massiva. Sem rate limiting, é um vetor de DoS contra o banco do ERP.
3. Vaza também tempo estimado de restauração de massivas.

**Nota de precisão.** O `MatrixMassiveOutputDTO` **não** retorna nome, endereço, telefone nem dados de contrato. O vazamento é de *existência* + status de massiva, não um dump de PII. Por isso HIGH e não CRITICAL.

**Correção recomendada.** Determinar por que é público. Se for consumido por um chatbot/URA anônimo (o nome "matrix" e o formato sugerem isso), a correção mínima é autenticar com uma credencial de serviço dedicada. Se não houver consumidor anônimo legítimo, trocar `permitAll()` por `authenticated()` — mudança de uma linha.

**Risco da correção.** Depende inteiramente do consumidor. **Não alterar sem identificá-lo** — logar `User-Agent`/origem nesse endpoint por alguns dias é o caminho de risco zero.

**Teste necessário.** Teste de integração: sem credencial → 401.

---

### F-05 · Endpoint público `GET /api/v1/afetados/contract/{id}` — enumeração de contratos

```
ID              F-05
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Autorização / IDOR
Arquivo         SecurityConfig.java:48  ·  AffectedUserController.java:131
```

**Descrição.** `contractId` é `Long` sequencial, sem qualquer verificação de propriedade (BOLA clássico), e a rota é `permitAll()`. Qualquer um itera de 1 a N e mapeia quais contratos têm massiva ativa e a previsão de restauração.

**Nota.** O controller responde em 3 paths (`/impacted-users`, `/usuario-afetado`, `/afetados`); apenas a variante `/afetados/contract/**` em GET é pública. As demais exigem autenticação. Os dados retornados (`reason`, `estimateTimeOfRestoration`, `estimatedTimeHour`) não incluem PII direta.

**Correção.** Mesma abordagem do F-04: identificar o consumidor (provavelmente o app do cliente final consultando a própria massiva) antes de fechar. Se for o app do cliente, o correto é autenticar o cliente e validar que o contrato é dele — mas isso é **mudança arquitetural** e fica para P2/P3, não para agora.

---

### F-06 · Token Bearer de integração escrito em log em texto puro

```
ID              F-06
Severidade      HIGH
Status          CONFIRMED
Categoria       Logging / Secrets
Arquivo         src/main/java/br/com/sebratel/bff/service/massivas/FinalizarMassivaNoEllevenApiService.java:40
```

```java
String token = recuperarTokenDoUsuarioIntegradorEllevenService.executar().accessToken();
log.info(token);
```

**Impacto.** O access token do **usuário integrador do ERP** (credencial de máquina com permissão de abrir/finalizar protocolos) é gravado em `logs/bff.log` (volume `bff-logs`) a cada finalização de massiva, em nível INFO. Quem lê o log — ou qualquer pipeline futuro que o envie para ELK/APM — obtém a credencial. Este é também o motivo pelo qual o log **não pode** ser encaminhado ao Elastic antes desta correção.

**Correção.** Remover a linha. Correção de uma linha, risco zero, sem impacto funcional.

**Teste.** Não aplicável diretamente; cobrir com regra de SAST (Semgrep) que barre `log.*(token)`.

---

### F-07 · Detalhes de exceção e mensagens internas retornados ao cliente

```
ID              F-07
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Error handling / Information disclosure
Arquivo         GlobalExceptionHandler.java:40-62  ·  AffectedUserController.java (6 catch blocks)
```

**Descrição.** O handler genérico devolve no corpo da resposta o **nome da classe da exceção, a mensagem e a causa raiz**:

```java
details.add(ex.getClass().getName() + ": " + ex.getMessage());
details.add("Causa raiz: " + root.getClass().getName() + ": " + root.getMessage());
```

Agravante: os services de integração transformam o corpo de erro do ERP em mensagem de exceção —
`RecuperarTokenDoUsuarioIntegradorEllevenService:87` → `new RuntimeException("Falha na integração Elleven: " + body)`. Esse `body` (resposta bruta do ERP) chega ao cliente HTTP externo.

Além disso, 6 blocos `catch (Exception e)` no `AffectedUserController` fazem `.message("... " + e.getMessage())` diretamente.

**Impacto.** Vazamento de: nomes de classes internas, estrutura do schema do banco (via mensagens JDBC), hostnames/portas internas, respostas de erro do ERP. Facilita reconhecimento para um atacante.

**Correção recomendada.** Manter o `log.error` completo (troubleshooting preservado — o comentário no código deixa claro que essa era a intenção) e substituir o `details` da resposta por um **`traceId`** correlacionável com o log. Alteração localizada em `buildErrorDetails` + os catch blocks do controller.

**Risco.** Baixo, mas **muda o contrato de resposta de erro**. Se o front-end hoje exibe `details` ao operador, ele perde essa informação. Precisa de alinhamento com o time de front. Sugestão: manter `details` apenas para `MethodArgumentNotValidException` (erros de validação, que são seguros e úteis) e remover só do handler genérico.

---

### F-08 · CORS com origem `*` e bean `CorsConfig` morto

```
ID              F-08
Severidade      MEDIUM
Status          CONFIRMED
Categoria       HTTP Security / Configuração
Arquivo         SecurityConfig.java:35-41  ·  CorsConfig.java:16-32
```

**Descrição.** Dois problemas encadeados:

1. `SecurityConfig` registra um `CorsConfigurationSource` **inline** no `.cors(...)`, o que **sobrepõe** o bean `corsConfigurationSource` de `CorsConfig`. O arquivo `CorsConfig.java` inteiro é código morto — inclusive a property `app.cors.allowed-origin-patterns` (linha 16), que lista as origens legítimas e **nunca é aplicada**.
2. A configuração que efetivamente vale usa `setAllowedOrigins(List.of("*"))` com `allowedHeaders("*")`.

**Impacto.** Mitigado pelo fato de `allowCredentials` ser `false` (default) — não há roubo de sessão via cookie. Mas: qualquer site pode fazer requisições cross-origin ao gateway a partir do navegador da vítima e **ler a resposta**. Combinado com F-04/F-05 (endpoints públicos), permite que uma página maliciosa use o navegador de um funcionário como proxy para consultar o gateway.

**Correção.** Trocar o lambda inline em `SecurityConfig` por `cors.configurationSource(corsConfigurationSource)` (injetando o bean existente) e corrigir `CorsConfig` para usar `setAllowedOriginPatterns(...)` com a property que já está lá. Isso **ativa** a intenção original do autor.

**Risco.** Médio — se alguma origem legítima não estiver na lista da property, o front quebra. A lista atual (`app-splitters-sebratel-ceb1f.web.app`, `sebratel.native-infinity.com.br`, localhost) precisa ser validada contra as origens reais em produção antes de aplicar.

---

### F-09 · Ausência total de autorização (apenas autenticação)

```
ID              F-09
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Autorização
Arquivo         (transversal — 50 endpoints)
```

**Evidência.** Zero ocorrências de `@PreAuthorize`, `@Secured`, `@RolesAllowed`, `hasRole`, `hasAuthority` em todo `src/main/java`. Não há `@EnableMethodSecurity`.

**Impacto.** Qualquer funcionário com e-mail `@sebratel.com.br` autenticado tem acesso idêntico a **tudo**: relatórios financeiros (`ContractPaymentController`, `ContractActivationInvoiceController`), dados de RH/funcionários (`EmployeeController`, QR Codes), inventário, e operações destrutivas (`DELETE /api/v1/afetados/protocol/{id}`). Não há separação entre um estagiário do suporte e um administrador.

**Correção.** Esta é uma **mudança arquitetural** e está explicitamente fora do escopo de "correções pequenas". Documentada aqui como **P2**, exigindo:
- definição de papéis pelo negócio (mapeamento de claim Google → role);
- `@EnableMethodSecurity` + anotações graduais, começando pelos endpoints destrutivos e financeiros.

**Não recomendo implementar agora.** Requer aprovação e desenho prévio.

---

### F-10 · Ausência de rate limiting

```
ID              F-10
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Abuso / Disponibilidade
```

Nenhum mecanismo de rate limiting, throttling ou lockout em nenhuma camada da aplicação.

**Endpoints candidatos, por prioridade:**

| Endpoint | Por quê |
|---|---|
| `GET /api/v1/matrix` | público + 2+ queries no ERP de produção por chamada (F-04) |
| `GET /api/v1/afetados/contract/{id}` | público + enumerável (F-05) |
| HTTP Basic (global) | senha força-brutável sem lockout (F-02) |
| `POST /api/v1/massivas*` | dispara escrita no ERP de produção via API externa |
| `controller/scripts/*` (19 endpoints) | relatórios pesados, queries analíticas sem paginação |

**Solução de menor risco.** Não implementar em Java. O container já está atrás de uma rede `proxy` externa (Traefik/nginx) — **rate limiting deve ser feito no reverse proxy**, que é configuração declarativa, isolada da aplicação, com rollback trivial. Implementar bucket4j/Resilience4j dentro do BFF adicionaria dependência e estado sem necessidade.

**Impacto de não fazer nada:** enumeração e DoS contra o banco do ERP permanecem viáveis.

---

### F-11 · Sem limite de tamanho de payload JSON

```
ID              F-11
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Input validation / Disponibilidade
Arquivo         application.properties (ausência de configuração)
```

Spring MVC **não impõe limite de tamanho** para corpos `@RequestBody` JSON por padrão (o limite de 2MB do `max-http-form-post-size` aplica-se apenas a form-urlencoded). Endpoints como `POST /api/v1/impacted-users` aceitam uma lista de usuários afetados sem `@Size`, e `POST /api/v1/massivas` recebe arrays de `accessPointIds`.

**Impacto.** Um POST autenticado com um JSON de centenas de MB é desserializado inteiro em heap → OOM / GC thrashing. Com o Basic compartilhado (F-02), a barreira de autenticação é fraca.

**Correção de baixo risco.** Adicionar no reverse proxy um `client_max_body_size` e, na aplicação, `@Size(max=...)` nas listas dos DTOs de entrada. Ambas são localizadas e testáveis.

---

### F-12 · Credencial de sessão do ERP trafega no corpo da requisição do cliente

```
ID              F-12
Severidade      MEDIUM
Status          CONFIRMED (design)  ·  header injection: POTENTIAL (mitigado pela lib)
Categoria       Autenticação / Design
Arquivo         dto/massivas/CriacaoDeMassivaInputDTO.java:55
                service/massivas/AdicionarMassivaNoEllevenService.java:116, 142, 180
```

**Descrição.** O cliente envia `cookieString` no JSON, e o gateway o repassa cru como header `Cookie` para o ERP:
```java
.header(HttpHeaders.COOKIE, input.getCookieString())
```

**Impacto.** O gateway atua como proxy de credencial de sessão do ERP controlada pelo cliente. Consequências: a sessão do ERP transita por logs de aplicação/proxy; o gateway não consegue atribuir a ação a um usuário confiável; e um cliente pode injetar a sessão de **outro** usuário do ERP.

**Sobre header injection (CRLF).** Reactor Netty valida valores de header e rejeita CR/LF, então a injeção direta não se concretiza — classificado como POTENTIAL, não CONFIRMED. O problema é de design, não de injeção.

**Correção.** Migrar esses fluxos para o token de integração (`RecuperarTokenDoUsuarioIntegradorEllevenService`), que já existe e é usado pelos services `*ApiService`. Isso é **refatoração de fluxo de negócio** — vai para P2, com desenho prévio. Correção imediata de baixo risco: validar `cookieString` contra um regex conservador e nunca logá-lo.

---

### F-13 · Código morto apontando para o ERP de *staging* a partir da produção

```
ID              F-13
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Configuração / Ambiente
Arquivo         WebClientConfig.java:20  ·  GetAllMassivesService.java:17,29  ·  utils/GetToken.java
```

Três problemas relacionados:

1. **`WebClientConfig` tem `baseUrl("https://erp-staging.sebratel.net.br")` hardcoded.** Todas as chamadas com URI relativa (`/massive_incidents/*`, `/external/integrations/...` em `FinalizarMassivaNoEllevenApiService:52` e `FinishLinkedProtocolsService:65`) vão para **staging**, mesmo em produção. As chamadas com URL absoluta vão para produção. O comportamento é inconsistente e não configurável.
2. **`GetAllMassivesService`** usa `baseUrl("https://erp-staging.sebratel.net.br:45701")` e uma query string inteira hardcoded **com datas fixas de 2026-03-09**.
3. **`GetToken.retrieve()`** lê um arquivo `network_logs.json` do diretório de trabalho — arquivo que **não existe no container**. Retorna `null` → o header `Authorization` fica nulo. O código imprime em `System.err` (fora do logging estruturado).

**Impacto.** Funcionalidade quebrada ou operando contra o ambiente errado. `GetAllMassivesService` está efetivamente morto. Se `WebClientConfig` estiver realmente servindo produção via staging, há risco de dados de produção sendo gravados em staging (ou operações falhando silenciosamente).

**Isto exige investigação com o time antes de qualquer correção** — precisa-se saber quais desses fluxos estão em uso real. A correção segura é externalizar as base URLs em properties (`erp.base-url`, `erp.api-base-url`) mantendo os valores atuais como default, o que torna a configuração explícita sem mudar comportamento.

---

### F-14 · Bug no retry de token: o retry reenvia o token antigo

```
ID              F-14
Severidade      MEDIUM
Status          CONFIRMED
Categoria       Resiliência / Correção
Arquivo         src/main/java/br/com/sebratel/bff/aspects/TokenRetryAspect.java:37, 58-59
```

```java
return joinPoint.proceed();          // linha 37 — sem argumentos
...
Object[] args = joinPoint.getArgs();
updateTokenInArgs(joinPoint, args, newToken);   // muta o array local
continue;                                        // volta ao proceed() sem argumentos
```

`joinPoint.proceed()` **sem argumentos** reexecuta o método com os argumentos **originais**. A atualização do token em `args` é descartada. Portanto, após um 401, o retry reenvia exatamente o mesmo token expirado e falha de novo, até esgotar `maxAttempts`.

**Impacto.** O mecanismo de resiliência contra expiração de token não funciona. Cada 401 gera N tentativas inúteis contra o ERP (pequeno retry storm) e termina em erro para o usuário.

**Agravante de performance.** `Thread.sleep(delay)` (linha 50) bloqueia uma thread do Tomcat durante o retry.

**Correção.** Trocar `joinPoint.proceed()` por `joinPoint.proceed(args)` no caminho de retry. Uma linha.

**Risco.** Baixo, mas **muda comportamento**: o retry passa a de fato funcionar, o que significa que uma chamada que hoje falha pode passar a ter efeito no ERP. É a intenção original do código, mas merece teste explícito.

**Teste necessário.** Teste do aspect com mock que retorna 401 na 1ª chamada e 200 na 2ª, verificando que a 2ª recebeu o token novo.

---

### F-15 · Timeouts de 10 minutos e ausência de timeout no cliente HTTP

```
ID              F-15
Severidade      HIGH
Status          CONFIRMED
Categoria       Performance / Resiliência / Disponibilidade
Arquivo         application.properties:9-10  ·  application-stage.properties:7-8
                WebClientConfig.java (ausência de configuração)
```

```properties
spring.mvc.async.request-timeout=600000     # 10 minutos
server.tomcat.connection-timeout=600000     # 10 minutos
```

E o `WebClient` é construído **sem nenhum timeout**: sem `responseTimeout`, sem `CONNECT_TIMEOUT_MILLIS`, sem read/write timeout, sem connection pool dimensionado.

**Impacto — este é o principal risco de disponibilidade do serviço.** As 14 chamadas de saída usam `.block()` em threads do Tomcat (pool default: 200). Se o ERP ficar lento ou pendurado:
- não há timeout de resposta no WebClient → a thread bloqueia indefinidamente;
- `connection-timeout=600000` mantém conexões ociosas ocupando slots por 10 minutos;
- 200 requisições lentas simultâneas esgotam o pool → **o gateway inteiro para de responder**, inclusive nos endpoints que não dependem do ERP.

Não há circuit breaker, bulkhead, nem fallback em nenhum ponto (nenhuma dependência de Resilience4j no `pom.xml`).

**Correção recomendada (pequena, alto retorno).**
1. `WebClientConfig`: adicionar `HttpClient` com `responseTimeout(Duration.ofSeconds(30))` e `CONNECT_TIMEOUT_MILLIS=5000`, valores externalizados em properties.
2. Reduzir `server.tomcat.connection-timeout` para o default (20s) — os 600000 parecem ter sido copiados do `async.request-timeout` por engano; `connection-timeout` é o tempo de espera pela **linha de request**, não pela resposta, e 10 min não tem justificativa.
3. Manter `spring.mvc.async.request-timeout` por ora (pode haver relatórios longos legítimos) — validar quais endpoints realmente demoram.

**Risco.** Médio e requer medição. Se algum relatório dos 19 endpoints de `controller/scripts/` legitimamente leva mais de 30s no ERP, um timeout agressivo o quebra. **Esta é exatamente a correção que deve esperar pelos dados do APM** — medir a latência p99 real das chamadas de saída e só então definir o valor. Recomendo: instrumentar APM primeiro (Etapa E), medir por uma semana, depois aplicar o timeout.

---

### F-16 · Sem pool de conexões dimensionado nos 4 datasources

```
ID              F-16
Severidade      LOW
Status          CONFIRMED
Categoria       Performance
Arquivo         ErpDbConfig.java, RadiusDbConfig.java, UsuariosAfetadosDbConfig.java
```

Nenhum dos datasources HikariCP define `maximum-pool-size`, `connection-timeout` ou `leak-detection-threshold`. Com o default de 10 por pool e 3 pools ativos, são 30 conexões — provavelmente adequado, mas não intencional nem documentado, e sem detecção de vazamento.

Adicionalmente: o datasource **DHO** está declarado em `application.properties:24-27` mas **não tem classe `@Configuration`** e a string `dho` não aparece em nenhum arquivo Java. É configuração morta apontando para um IP interno (`10.0.11.171`) hardcoded como default — remover.

---

### F-17 · Testes de controller desabilitam a cadeia de segurança

```
ID              F-17
Severidade      HIGH (risco de processo, não de runtime)
Status          CONFIRMED
Categoria       Testes
Arquivo         src/test/java/br/com/sebratel/bff/BaseTest.java:10-14
```

```java
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class })
```

**Todos os 25 testes de controller rodam com a cadeia de filtros de segurança desligada.** Não existe **um único teste** que comprove que um endpoint protegido rejeita requisição sem credencial, que um token inválido é recusado, ou que um token expirado é recusado.

**Impacto.** É possível introduzir um `permitAll()` acidental, quebrar o `jwtDecoder` ou remover uma regra de autorização e **toda a suíte continua verde**. Os findings F-02, F-03, F-04 e F-05 existem hoje sem que nenhum teste os detecte.

**Correção.** Não alterar o `BaseTest` (isso quebraria os 25 testes existentes e é justamente a refatoração massiva a evitar). Em vez disso, **adicionar uma classe nova** `SecurityIntegrationTest` com `@SpringBootTest` + `@AutoConfigureMockMvc` (com filtros ativos) cobrindo a matriz de autenticação. Aditivo, risco zero para o que existe.

---

## SSRF — Análise dedicada

**Conclusão: não há SSRF explorável neste código.**

Foram mapeadas as 14 chamadas de saída (`grep -rn "\.uri("`). Resultado:

| Local | URI | Controle do usuário |
|---|---|---|
| `RecuperarTokenDoUsuarioIntegradorEllevenService:66` | constante `https://erp.sebratel.net.br:45700/connect/token` | nenhum |
| `AdicionarMassivaNoEllevenApiService:57` | constante | nenhum |
| `AbrirProtocoloInfraNoEllevenApiService:70` | constante `OPEN_DETAILED_SOLICITATION_URL` | nenhum |
| `GetConnectionsService`, `ListarOltsService`, `ListarSplittersService` | constantes `erp.sebratel.net.br:45715/external/map/*` | nenhum |
| `RecuperarSolicitacoesDeUmUsuarioService:39` | constante | nenhum |
| `AdicionarMassivaNoEllevenService:115,141` | paths relativos literais | nenhum |
| `AdicionarMassivaNoEllevenService:179` | `"/massive_incidents/concludeMassiveIncident/" + id` | **path segment**, não host |
| `FinalizarMassivaNoEllevenApiService:52`, `FinishLinkedProtocolsService:65` | paths relativos literais | nenhum |
| `GetAllMassivesService:28` | query string constante | nenhum |
| `ListarSplittersService:73` | `uriBuilder` com `queryParam("page"/"pageSize")` | **valores numéricos** |

Respondendo ao checklist do escopo:

| Pergunta | Resposta |
|---|---|
| Usuário controla host? | **Não** — todos os hosts são literais no código |
| Usuário controla protocolo? | **Não** |
| Acessa IP interno / localhost / metadata? | **Não** |
| Altera porta? | **Não** |
| Existe allowlist? | Não é necessária — não há URL dinâmica |
| Redirects escapam da validação? | WebClient/Reactor Netty **não segue redirects por padrão** — comportamento seguro |

**Recomendação:** nenhuma correção de SSRF. Adicionar uma regra de SAST (Semgrep) que alerte se um `.uri()` passar a receber valor derivado de entrada de usuário — prevenção, custo zero.

O único ponto adjacente é o `id` concatenado em `AdicionarMassivaNoEllevenService:179`. Como é um path segment (não host) e o WebClient codifica o path, não permite mudança de destino. Vale um `@Positive` no DTO por higiene.

---

## SQL Injection

**Nenhuma injeção encontrada.** As ~25 queries `nativeQuery = true` usam exclusivamente parâmetros nomeados (`:protocol`, `:contractId`). Não há concatenação de entrada do usuário em SQL — as ocorrências de `" +` encontradas são apenas montagem de strings multi-linha estáticas.

---

## FASE 3 — PERFORMANCE

| Achado | Sev. | Detalhe |
|---|---|---|
| Sem timeout no WebClient (F-15) | HIGH | maior risco: esgotamento do pool de threads do Tomcat |
| `connection-timeout` de 10 min (F-15) | HIGH | mantém slots ocupados |
| `.block()` × 14 em threads Tomcat | MEDIUM | inerente ao MVC; aceitável **se** houver timeout. Não recomendo migrar para reativo — é exatamente a refatoração massiva a evitar |
| `Thread.sleep()` no retry aspect | LOW | bloqueia worker durante o backoff |
| Sem paginação nos 19 relatórios de `scripts/` | MEDIUM | queries analíticas retornando conjuntos completos |
| Pools Hikari sem dimensionamento (F-16) | LOW | defaults, sem leak detection |
| Sem connection pool configurado no Reactor Netty | MEDIUM | usa o pool default (500 conexões, 45s idle) |
| Sem cache | INFO | `spring-boot-starter-cache` presente mas inativo. **Não recomendo introduzir cache agora** — sem métricas, seria especulativo |
| Token de integração já tem cache in-memory | ✅ | `RecuperarTokenDoUsuarioIntegradorEllevenService` faz double-checked locking corretamente, com margem de expiração. Bem implementado |
| Sem N+1 aparente | ✅ | acesso via projections e queries nativas |
| JVM sem tuning | LOW | `ENTRYPOINT` sem `-XX:MaxRAMPercentage`; sem limites de memória no compose → o container pode consumir toda a RAM do host |
| Sem healthcheck | MEDIUM | ausência de Actuator impede health check no Docker e no reverse proxy |

**Nota metodológica.** Salvo o timeout do WebClient (que é risco de indisponibilidade, não de latência), **não recomendo nenhuma otimização de performance antes do APM estar coletando dados**. As demais são hipóteses, não gargalos medidos.

---

## FASE 4 — RESILIÊNCIA

| Padrão | Situação |
|---|---|
| Timeout | ❌ ausente no cliente HTTP (F-15) |
| Retry | ⚠️ existe (`TokenRetryAspect`) mas **quebrado** (F-14); sem backoff exponencial, sem jitter; retry apenas em 401 (correto — não repete POST em erro genérico ✅) |
| Circuit breaker | ❌ ausente |
| Bulkhead | ❌ ausente — todos os endpoints compartilham o mesmo pool de threads |
| Fallback | ❌ ausente (exceto `MatrixService`, que degrada para `not_found_client`) |
| Isolamento entre serviços | ❌ ERP lento derruba endpoints que só usam banco |
| Propagação de erro | ⚠️ vaza detalhes internos (F-07) |
| Cascata de falha | ❌ risco real: ERP → esgotamento de threads → gateway inteiro fora |

**Sobre retry storm:** o retry atual é limitado a 401 e a `maxAttempts`, então não há storm significativo hoje. **Não adicionar retries novos.** Corrigir o existente (F-14) e adicionar timeout (F-15) é suficiente por agora. Circuit breaker é P2 — introduz dependência (Resilience4j) e precisa de thresholds medidos.

---

## FASE 5 — TESTES (estado atual)

**Existente:** 72 arquivos de teste, JUnit 5 + Mockito, Testcontainers (PostgreSQL), Allure, JaCoCo com exclusões (`repository`, `dto`, `model`, `BffApplication`).

**Gaps críticos:**

| Gap | Impacto |
|---|---|
| **Zero testes de segurança** (F-17) | nenhuma prova de que autenticação/autorização funcionam |
| Filtros/security chain nunca exercitados | `addFilters = false` em todos os controller tests |
| `jwtDecoder` sem teste | validação de issuer/email/aud não coberta |
| `TokenRetryAspect` sem teste | é por isso que o bug F-14 passou despercebido |
| `GlobalExceptionHandler` parcialmente testado | existe `GlobalExceptionHandlerTest` |
| Sem teste de timeout / erro de integração | comportamento sob falha do ERP desconhecido |
| Cobertura real desconhecida | JaCoCo roda mas não há gate mínimo configurado |

**Estratégia proposta (aditiva, sem tocar nos testes existentes):**
1. `SecurityIntegrationTest` — matriz de autenticação: sem credencial → 401; JWT inválido → 401; JWT expirado → 401; JWT com `aud` errado → 401; JWT válido → 200; rotas públicas → 200.
2. `JwtDecoderTest` — validadores isolados, com par de chaves gerado em teste.
3. `TokenRetryAspectTest` — cobrir o cenário 401 → refresh → sucesso (prova a correção do F-14).
4. `WebClientTimeoutTest` — MockWebServer com resposta lenta, provando que o timeout dispara.
5. Gate JaCoCo mínimo — **começar sem falhar o build**, apenas reportando, para estabelecer a linha de base.

---

## FASE 6/7 — CI/CD E SECURITY CI (estado atual)

### Workflows existentes

| Workflow | Gatilho | O que faz | Problemas |
|---|---|---|---|
| `maven.yml` | push/PR em `staging` | `mvnw clean verify`, upload JaCoCo, dependency graph | ❌ não roda em `main`; ❌ sem gate de cobertura; ❌ nenhuma verificação de segurança |
| `docker-image.yml` | push/PR em `staging` | `docker build` com tag descartável | ❌ não roda em `main`; ❌ não escaneia a imagem; ❌ resultado não é usado |
| `staging.yml` | push em `staging` | valida versão vs. tag, roda `verify`, empacota | ❌ não roda em `main`; ❌ **não faz deploy** apesar do nome; ❌ compila duas vezes |

### Lacunas de segurança no CI

| Controle | Situação |
|---|---|
| SAST | ❌ ausente |
| Dependency scanning | ❌ ausente (só há submissão do grafo, que **não bloqueia**) |
| Secret scanning | ❌ ausente — **é por isso que o F-01 chegou à `main`** |
| Container scanning | ❌ ausente |
| Security gate | ❌ inexistente |
| Cobertura mínima | ❌ inexistente |
| Proteção da branch `main` | ⚠️ nenhum workflow a cobre |

**Observação relevante:** a `main` — que é a branch analisada e aparentemente a de produção — **não tem nenhuma verificação automatizada**. Todos os três workflows atendem apenas `staging`.

---

## FASE 8 — ELASTIC APM READINESS

| Requisito | Situação |
|---|---|
| Rede `elk_es_network` declarada no compose | ❌ ausente — hoje só `proxy` e `default` |
| Java Agent | ❌ ausente |
| Variáveis de ambiente do APM | ❌ ausentes |
| Package raiz para `ELASTIC_APM_APPLICATION_PACKAGES` | ✅ **`br.com.sebratel`** (não `com.sebratel` — o exemplo do escopo precisa ser ajustado) |
| Service name sugerido | `api-gateway` (consistente com a stack `api-gateway-prod`) |
| Nome real do serviço no compose | **`bff-app`** (container `bff-java-service`) — é este que recebe as redes |
| Rede atual a preservar | `proxy` (external) + `default` (bridge) |

### Clientes HTTP a serem instrumentados

Um único cliente: **`WebClient` (Spring WebFlux sobre Reactor Netty)**. O agente Elastic APM instrumenta `spring-webclient` e adiciona `traceparent` automaticamente.

### Destinos externos e análise de `traceparent`

| Destino | Natureza | `traceparent` |
|---|---|---|
| `erp.sebratel.net.br:45700` (OAuth token) | **interno Sebratel** | desejável |
| `erp.sebratel.net.br:45715` (API integração) | **interno Sebratel** | desejável |
| `erp-staging.sebratel.net.br` / `:45701` | **interno Sebratel** (staging) | desejável |
| `www.googleapis.com/oauth2/v3/certs` (JWKS) | **terceiro (Google)** | **precisa validação** |

**Ponto de atenção sobre o Google.** A busca do JWKS é feita pelo `NimbusJwtDecoder`, que internamente usa `RestTemplate`/`java.net`, não o `WebClient`. O agente pode instrumentá-la e adicionar `traceparent`. O endpoint JWKS do Google ignora headers desconhecidos, então **não espero problema** — mas isso é uma expectativa, não um fato verificado, e deve ser confirmado empiricamente na validação (Etapa E) antes de considerar o APM concluído. **Não vou desabilitar instrumentação preventivamente** — sem evidência de problema, `disable_instrumentations` seria injustificado.

Todos os demais destinos são infraestrutura Sebratel, onde a propagação é o comportamento desejado.

### Riscos de coleta de dados sensíveis pelo APM

Antes de ligar o agente, três pontos precisam de configuração explícita:

1. **F-06 (token em log) precisa ser corrigido antes** — se os logs forem enviados ao Elastic, o token de integração vai junto.
2. `capture_body` deve permanecer `off` (default). Os corpos contêm CPF (`MatrixMassiveInputDTO`), `cookieString` (F-12) e dados de funcionário.
3. `sanitize_field_names` deve ser estendido para incluir `cookiestring`, `syndata`, `client_secret` além dos defaults (`password`, `authorization`, `set-cookie`, etc.).

---

## FASE 10 — THREAT MODEL

```
                         [ Internet ]
                              |
                              v
                    ┌──────────────────┐
                    │  Reverse Proxy   │  rede docker "proxy" (externa)
                    │  (Traefik/nginx) │  ← local correto p/ rate limit (F-10)
                    └────────┬─────────┘
                             │  TRUST BOUNDARY 1
                             v
              ┌──────────────────────────────┐
              │   bff-java-service  :8085    │
              │   Spring Boot 3.4.3 / MVC    │
              │                              │
              │  AuthN: Google OIDC JWT      │  ← F-03 (sem aud)
              │       + HTTP Basic estático  │  ← F-02 (bypass total)
              │  AuthZ: NENHUMA              │  ← F-09
              │  Rate limit: NENHUM          │  ← F-10
              │  Rotas públicas: 2           │  ← F-04, F-05
              └───┬──────┬──────┬──────┬─────┘
                  │      │      │      │
     TRUST BOUNDARY 2    │      │      │   TRUST BOUNDARY 3
                  │      │      │      │
      ┌───────────┘      │      │      └────────────────┐
      v                  v      v                       v
┌───────────┐     ┌──────────┐ ┌──────────┐   ┌──────────────────┐
│ ERP DB    │     │ Afetados │ │ Radius   │   │ ERP Elleven API  │
│ Postgres  │     │ MariaDB  │ │ Postgres │   │ :45700 / :45715  │
│ (PRODUÇÃO)│     │          │ │          │   │ + staging :45701 │
└───────────┘     └──────────┘ └──────────┘   └──────────────────┘
   ▲                                                  ▲
   │ F-04: alcançável SEM autenticação                │ F-15: sem timeout
   │      (amplificação de carga)                     │      → cascata de falha
   │                                                  │ F-12: cookie do cliente
   │                                                  │ F-13: staging vs prod
```

| Fluxo | AuthN | AuthZ | Dado sensível | SSRF | Vazamento | Indisponibilidade |
|---|---|---|---|---|---|---|
| Cliente → `/api/v1/matrix` | ❌ **nenhuma** | ❌ | CPF (entrada) | ❌ | 🔴 oráculo de CPF | 🔴 carga não autenticada no ERP DB |
| Cliente → `/api/v1/afetados/contract/**` | ❌ **nenhuma** | ❌ | ID de contrato | ❌ | 🟠 enumeração | 🟠 |
| Cliente → demais 48 endpoints | ⚠️ JWT **ou** Basic | ❌ nenhuma | financeiro, RH, inventário | ❌ | 🟠 F-07 | 🟠 |
| BFF → ERP DB / Radius / Afetados | credencial de serviço | n/a | PII de clientes | n/a | 🟡 via F-07 | 🟡 pool sem limite |
| BFF → ERP API (token integrador) | OAuth client_credentials | n/a | token bearer | ❌ | 🔴 F-06 (log) | 🔴 F-15 (sem timeout) |
| BFF → ERP API (cookie do cliente) | cookie do cliente | n/a | sessão do ERP | ❌ | 🟠 F-12 | 🟠 |
| BFF → Google JWKS | nenhuma (público) | n/a | nenhum | ❌ | ✅ | 🟡 falha no JWKS derruba toda a autenticação |

---

## FASE 11 — RESUMO DOS ACHADOS

| ID | Sev. | Status | Título | Arquivo |
|---|---|---|---|---|
| F-01 | 🔴 CRITICAL | CONFIRMED | Chave privada RSA no Git e no JAR | `resources/id_rsa_private.pem` |
| F-02 | 🔴 CRITICAL | CONFIRMED | Basic estático dá acesso total (bypassa JWT) | `SecurityConfig.java:43,78` |
| F-03 | 🟠 HIGH | CONFIRMED | JWT sem validação de `audience` | `SecurityConfig.java:67` |
| F-04 | 🟠 HIGH | CONFIRMED | `/api/v1/matrix` público — oráculo de CPF | `SecurityConfig.java:49` |
| F-06 | 🟠 HIGH | CONFIRMED | Token bearer em log texto puro | `FinalizarMassivaNoEllevenApiService.java:40` |
| F-15 | 🟠 HIGH | CONFIRMED | Sem timeout no WebClient + 10 min no Tomcat | `WebClientConfig.java`, `application.properties:9` |
| F-17 | 🟠 HIGH | CONFIRMED | Testes desligam a cadeia de segurança | `BaseTest.java:10` |
| F-05 | 🟡 MEDIUM | CONFIRMED | `/afetados/contract/**` público (enumerável) | `SecurityConfig.java:48` |
| F-07 | 🟡 MEDIUM | CONFIRMED | Detalhes de exceção vazam ao cliente | `GlobalExceptionHandler.java:50` |
| F-08 | 🟡 MEDIUM | CONFIRMED | CORS `*` + `CorsConfig` morto | `SecurityConfig.java:37` |
| F-09 | 🟡 MEDIUM | CONFIRMED | Nenhuma autorização em 50 endpoints | transversal |
| F-10 | 🟡 MEDIUM | CONFIRMED | Sem rate limiting | transversal |
| F-11 | 🟡 MEDIUM | CONFIRMED | Sem limite de payload JSON | `application.properties` |
| F-12 | 🟡 MEDIUM | CONFIRMED | Cookie de sessão do ERP vindo do cliente | `AdicionarMassivaNoEllevenService.java:116` |
| F-13 | 🟡 MEDIUM | CONFIRMED | Código morto apontando para staging | `WebClientConfig.java:20` |
| F-14 | 🟡 MEDIUM | CONFIRMED | Retry reenvia token antigo (`proceed()` sem args) | `TokenRetryAspect.java:37` |
| F-16 | 🔵 LOW | CONFIRMED | Pools Hikari sem dimensionamento; DHO morto | `*DbConfig.java` |

---

## DEPENDÊNCIAS

Tree gerada com `mvnw dependency:tree` — 160 artefatos.

### D-01 · Selenium + WebDriverManager: dependências não utilizadas

```
Severidade    HIGH (revisada)  ·  Status: CONFIRMED
```

> **Revisão de 2026-08-17.** Esta severidade foi elevada de MEDIUM para HIGH depois que o
> Dependabot abriu o alerta #1 sobre `io.github.bonigarcia:webdrivermanager` 5.7.0:
> **XXE (Improper Restriction of XML External Entity Reference), CVSS 9.3 Critical**,
> corrigido em 6.1.0.
>
> A análise original citou apenas as CVEs *transitivas* do BouncyCastle e não identificou
> esta CVE *direta* no próprio WebDriverManager. É precisamente o caso que justifica a
> ressalva metodológica registrada no fim desta seção: a análise manual de versões não
> substitui um scanner, e o scanner é a fonte autoritativa.
>
> **Exploitabilidade neste contexto permanece nula** — a classe vulnerável
> (`WebDriverManager.java`) nunca é instanciada, porque nenhuma linha de código em
> `src/main` ou `src/test` referencia a biblioteca (verificado nas branches `staging` e
> `massivas-integracao`). Por isso HIGH e não CRITICAL: a CVE é crítica em si, mas não há
> caminho de execução até ela neste repositório.
>
> **A correção continua sendo a remoção, não o bump para 6.1.0.** Atualizar manteria ~25
> artefatos transitivos desnecessários e apenas trocaria a superfície de ataque de lugar.
> Já aplicado no commit `2dce938` (branch `staging`). O alerta permanece aberto até que a
> mudança alcance a branch default (`massivas-integracao`), que é a varrida pelo Dependabot.

`selenium-java` 4.18.1 e `webdrivermanager` 5.7.0 estão no `pom.xml` mas **não são referenciados por nenhuma linha de código** (`src/main` nem `src/test` — verificado por grep de `WebDriver|ChromeDriver|selenium`).

Consequências:
- ~25 artefatos transitivos desnecessários, incluindo **BouncyCastle 1.76** (que carrega CVEs conhecidas — CVE-2024-29857, CVE-2024-30171, CVE-2024-30172 — corrigidas em 1.78);
- conflito de versões: `selenium-java:4.18.1` convive com `selenium-api:4.25.0` no mesmo classpath;
- aumento significativo do tamanho da imagem e da superfície de ataque.

**Esta é a correção de dependência de melhor custo-benefício:** remover 2 linhas do `pom.xml` elimina ~25 artefatos e as CVEs do BouncyCastle sem qualquer risco funcional (código não referenciado não pode quebrar). Também elimina `com.beust:jcommander`, `dev.failsafe`, `org.brotli:dec` e o bloco OpenTelemetry 1.43.0 trazido pelo Selenium — este último, aliás, **pode conflitar com o agente Elastic APM** na Etapa E.

### D-02 · `logback-contrib` 0.1.5 abandonado e não utilizado

```
Severidade    LOW  ·  Status: CONFIRMED
```

`logback-json-classic` / `logback-jackson` 0.1.5 — último release em **2016**, projeto arquivado. Não há `logback-spring.xml` no projeto, então **não estão em uso**. Candidatos a remoção (ou, se o objetivo era log estruturado JSON para o ELK, substituir por `logstash-logback-encoder`, que é mantido — decisão para a Etapa E).

### D-03 · Tomcat embed 10.1.36 — CVEs de DoS aplicáveis

```
Severidade    MEDIUM  ·  Status: CONFIRMED (faixa de versão) / explorabilidade a validar
```

| CVE | Aplicável a 10.1.36? | Relevância aqui |
|---|---|---|
| CVE-2025-24813 (RCE, partial PUT) | ❌ corrigida em 10.1.35 | não afetado |
| CVE-2025-31650 (DoS, HTTP/2 priority) | ✅ afeta ≤10.1.39 | **relevante** — DoS remoto sem autenticação |
| CVE-2025-48988 (DoS, multipart) | ✅ afeta ≤10.1.42 | relevante se multipart estiver habilitado |
| CVE-2025-31651 (bypass, RewriteValve) | ✅ na faixa | ❌ não usa RewriteValve |

**Correção:** atualizar o parent Spring Boot de 3.4.3 para a última 3.4.x, que traz um Tomcat corrigido. Alternativa mais cirúrgica e de menor risco: sobrescrever apenas `<tomcat.version>` no `<properties>`, mantendo todo o resto do BOM idêntico.

### D-04 · Netty 4.1.118.Final

```
Severidade    LOW-MEDIUM  ·  Status: POTENTIAL
```

CVE-2025-24970 (crash no SslHandler) e CVE-2025-25193 **foram corrigidas exatamente em 4.1.118** → não afetado. Há advisories posteriores na família `netty-codec` / `netty-codec-http` (incluindo request smuggling via chunk extension) que afetam faixas anteriores a 4.1.125. **Relevância baixa neste projeto:** o Netty aqui atua apenas como *cliente* HTTP (WebClient); o servidor é Tomcat. Smuggling do lado cliente contra o ERP interno é um cenário remoto. Classificado como POTENTIAL — a confirmação deve vir do scanner, não da minha memória.

### D-05 · Spring Framework 6.2.3 / Spring Security 6.4.3

```
Severidade    LOW  ·  Status: POTENTIAL / não explorável
```

Há CVEs conhecidas em faixas 6.2.x anteriores à 6.2.7/6.2.8 (ex.: bypass de `disallowedFields` no `DataBinder`). Sobre as CVEs de Spring Security relacionadas a detecção de anotações com genéricos: **não são exploráveis aqui**, porque o projeto não usa nenhuma anotação de method security (ver F-09). Atualizar o parent para a última 3.4.x endereça o conjunto.

### Ressalva metodológica sobre CVEs

**Esta ressalva já se provou necessária na prática:** o Dependabot encontrou a XXE de
CVSS 9.3 no WebDriverManager (ver D-01), que a análise manual abaixo não identificou.
Trate a lista a seguir como ponto de partida, nunca como inventário completo.

As classificações acima vêm de análise de faixas de versão contra vulnerabilidades conhecidas até meu corte de conhecimento. **Não substituem um scanner.** Recomendo que o OWASP Dependency-Check ou o Dependabot, uma vez no CI (Etapa D), seja a **fonte autoritativa** — inclusive para revisar estas conclusões. Não classifiquei nada como CRITICAL sem caminho de exploração demonstrável, conforme a regra 4 do escopo.

### Componentes saudáveis (verificados, sem ação)

`jackson 2.18.2` · `logback 1.5.16` (posterior às CVE-2024-12798/12801) · `nimbus-jose-jwt 9.37.3` · `guava 33.3.0-jre` · `commons-compress 1.26.0` · `swagger-ui 5.18.3` · `hibernate 6.6.8`.

`spring-boot-devtools`: presente como `runtime`/`optional`, mas o `spring-boot-maven-plugin` o exclui do JAR repackaged por padrão → **não vai para produção**. Sem ação.

---

## ETAPA B — PRIORIZAÇÃO

### P0 — corrigir antes do próximo deploy

| ID | Ação | Risco da mudança | Precisa de decisão? |
|---|---|---|---|
| F-06 | Remover `log.info(token)` | **nenhum** | não |
| F-01 | Externalizar path da chave + `.gitignore` + **rotacionar** | baixo (código) / alto (rotação) | 🔶 **sim** — janela de transição dos QR Codes |
| F-02 | Restringir ou remover `httpBasic` | **alto se às cegas** | 🔶 **sim** — mapear consumidores primeiro |
| F-04 | Fechar ou autenticar `/api/v1/matrix` | alto se houver consumidor anônimo | 🔶 **sim** — identificar consumidor |

### P1 — alto risco, correções pequenas

| ID | Ação | Risco |
|---|---|---|
| F-03 | Validar `aud` + `email_verified` (aceitando **lista** de audiences) | médio — precisa levantar os client IDs |
| F-15 | Timeout no WebClient + reduzir `connection-timeout` | médio — **valor deve vir do APM** |
| F-14 | `joinPoint.proceed(args)` | baixo |
| F-17 | Adicionar `SecurityIntegrationTest` (classe nova) | **nenhum** — aditivo |
| D-01 | Remover Selenium + WebDriverManager | **nenhum** — código não referenciado |
| F-07 | Substituir `details` por `traceId` no handler genérico | baixo — alinhar com front |
| — | Secret scanning + dependency scanning no CI | nenhum |

### P2 — melhorias importantes (exigem desenho)

F-05 (autorização em `/afetados/contract`), F-08 (CORS — validar origens reais), F-09 (**modelo de autorização — arquitetural, requer aprovação**), F-10 (rate limit no reverse proxy), F-11 (limite de payload), F-12 (**eliminar cookie do cliente — arquitetural**), F-13 (externalizar base URLs), D-03 (bump do Tomcat), Actuator + healthcheck, workflows cobrindo `main`.

### P3 — futuro

F-16 (tuning de pools, remover DHO morto), D-02 (logback-contrib), circuit breaker, paginação nos relatórios, limites de memória no compose, gate de cobertura JaCoCo.

---

## ETAPAS C/D/E — plano de execução proposto

### C — Correções

Começaria por um lote **de risco praticamente nulo**, que não depende de nenhuma decisão externa:

1. F-06 — remover uma linha de log
2. D-01 — remover 2 dependências não utilizadas do `pom.xml`
3. F-14 — `proceed(args)` + teste do aspect
4. F-17 — `SecurityIntegrationTest` (classe nova, aditiva) — que **documenta em testes** o estado atual dos findings F-02/F-03/F-04
5. F-16 (parcial) — remover o datasource DHO morto

Os itens que mexem em autenticação (F-02, F-03, F-04) ficam **bloqueados aguardando as três decisões** listadas em P0.

### D — CI

Workflows a criar em `.github/workflows/`:

| Workflow | Valida | Bloqueia? |
|---|---|---|
| `ci.yml` | build + testes unitários e de integração em PR para `main`/`staging` | ✅ falha de teste |
| `security-deps.yml` | OWASP Dependency-Check + `dependency-review-action` | ✅ CVSS ≥ 7 sem supressão justificada |
| `security-sast.yml` | CodeQL (Java) + Semgrep (regras Spring) | ✅ finding HIGH/CRITICAL confirmado |
| `security-secrets.yml` | Gitleaks (histórico + diff) | ✅ qualquer secret detectado |
| `security-gate.yml` | agrega os anteriores | ✅ ver abaixo |

**Security gate — o que bloqueia:** vulnerabilidade CRITICAL; HIGH com caminho explorável; secret detectado; falha em qualquer teste de segurança; finding SAST HIGH/CRITICAL confirmado.
**O que registra sem bloquear:** LOW/INFO; falsos positivos com supressão **justificada por escrito** em `dependency-check-suppressions.xml`; dependência vulnerável sem caminho explorável, documentada.

Um arquivo de supressões inicial será necessário para que o gate não fique vermelho permanentemente no dia 1 — cada supressão com justificativa e data de revisão. Nada será silenciado sem registro.

### E — APM

1. `docker-compose.yml`: adicionar a rede externa preservando `proxy` e `default`
   ```yaml
   networks:
     proxy:      { external: true }
     default:    { driver: bridge }
     elk-network:
       name: ${ELK_NETWORK_NAME:-elk_es_network}
       external: true
   ```
   e anexar ao serviço **`bff-app`** (nome real) as três redes.
2. `Dockerfile`: baixar o agente no estágio de build (verificando a versão atual compatível — o agente 1.x mais recente suporta Spring Boot 3.4/Java 21; **vou confirmar a versão no momento da implementação em vez de fixar uma versão antiga às cegas**), e ajustar o `ENTRYPOINT` com `-javaagent`.
3. Variáveis: `ELASTIC_APM_SERVICE_NAME=api-gateway`, `ELASTIC_APM_SERVER_URL=http://apm-server:8200`, `ELASTIC_APM_ENVIRONMENT=production`, **`ELASTIC_APM_APPLICATION_PACKAGES=br.com.sebratel`** (package real), `ELASTIC_APM_CAPTURE_BODY=off`, `sanitize_field_names` estendido.
4. `docker compose config` → confirmar `elk-network → elk_es_network`.
5. Validação: chamada real → transação no APM → latência → erros → spans → chamadas de saída → **propagação de `traceparent` verificada empiricamente**, incluindo o caso do JWKS do Google → confirmar ausência de dados sensíveis.

**Pré-requisito obrigatório:** F-06 deve estar corrigido antes de qualquer envio de log/trace ao Elastic.

---

## RISCOS REMANESCENTES (fora do escopo desta rodada)

1. **Chave RSA no histórico Git** — remover do HEAD não basta; a limpeza do histórico reescreve hashes e precisa de coordenação com o time.
2. **Modelo de autorização (F-09)** — mudança arquitetural, requer definição de papéis pelo negócio.
3. **Cookie de sessão do ERP vindo do cliente (F-12)** — requer redesenho do fluxo de massivas.
4. **Rate limiting** — pertence ao reverse proxy, que está fora deste repositório.
5. **Branch `main` sem CI** — os workflows novos resolvem, mas branch protection é configuração do GitHub, não do repo.
6. **Rotação das demais credenciais** — `SECURITY_PASS`, `ELLEVEN_CLIENT_SECRET`, `GOOGLE_CLIENT_SECRET` e senhas de banco estão em `.env` no host. O `.env` **não está versionado** (verificado: `git ls-files` e `git log --all -- .env` vazios) ✅, mas a gestão via arquivo em disco no host permanece um risco operacional.
7. **`server.tomcat.connection-timeout`** — a redução só deve ocorrer após medição no APM.

---

## O QUE NÃO FOI ENCONTRADO (verificado, sem achado)

Registrado para que a ausência seja intencional e não omissão:

- **SSRF** — nenhuma URL de saída é controlável pelo usuário (14 chamadas auditadas)
- **SQL injection** — todas as ~25 queries nativas usam parâmetros nomeados
- **Request smuggling / HTTP desync / host header injection** — não há proxy de requisição bruta
- **Confiança em `X-Forwarded-*` / `X-Real-IP`** — nenhum código lê esses headers
- **Path traversal** — nenhuma manipulação de path a partir de entrada do usuário
- **Desserialização insegura** — apenas Jackson com tipos concretos; sem polymorphic typing habilitado
- **ReDoS** — nenhum regex construído a partir de entrada do usuário
- **Secrets hardcoded no código Java** — nenhum (o único secret é o `.pem`, F-01)
- **`.env` versionado** — não está
- **Padding oracle no RSA** — usa OAEP com SHA-256, correto
- **Redirects abertos** — não há; o WebClient não segue redirects por padrão
- **Actuator exposto** — não existe Actuator no projeto
```
