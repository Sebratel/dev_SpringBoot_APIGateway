# Inventário de endpoints — contrato que o Gateway deve preservar

Levantado do código em `src/main/java/br/com/sebratel/bff/controller` (`main` + `staging`, com o GMUD;
versão `3.4.3-SNAPSHOT`). **Toda rota abaixo deve responder igual depois da migração**:
mesmo método, mesmo path, mesmos parâmetros, mesmo status e mesmo formato de corpo.

Como o Spring combina o `@RequestMapping` da classe com o do método, cada linha com várias
bases e vários sufixos gera o **produto cartesiano** (ex.: 3 bases × 2 sufixos = 6 rotas).
Total: **155 rotas HTTP**, em 23 controllers e 6 serviços de destino.

Legenda de autenticação: 🔒 JWT Google (`@sebratel.com.br`) ou Basic · 🌐 `permitAll`.
Legenda de destino: `MAS` massivas-service (74) · `SPL` splitters-service (8) · `COM` comercial-service (45) ·
`RAD` radius-service (14) · `EST` estoque-service (8) · `PES` pessoas-service (7).

## 1. Usuários afetados — destino `MAS`
Bases (3): `/api/v1/impacted-users`, `/api/v1/usuario-afetado`, `/api/v1/afetados`

| Método | Sufixo | Entrada | Auth | Rotas |
|---|---|---|---|---|
| GET | `` | — | 🔒 | 3 |
| POST | `` | body `CreateImpactedUsersInputDTO` | 🔒 | 3 |
| GET | `/pppoe/{pppoe}` | path | 🔒 | 3 |
| GET | `/contract/{contractId}` | path | 🌐 só em `/api/v1/afetados/contract/**`; nas outras 2 bases 🔒 | 3 |
| GET | `/protocol/{protocol}` | path | 🔒 | 3 |
| DELETE | `/protocol/{protocol}` | path | 🔒 | 3 |
| PATCH | `/protocol/{protocol}` | query `finishDate` (LocalDateTime) | 🔒 | 3 |

## 2. Massivas (Elleven + ERP) — destino `MAS`
Bases (3): `/api/v1/massive-incidents`, `/api/v1/massivas-eleven`, `/api/v1/massivas`

| Método | Sufixo(s) | Entrada | Rotas |
|---|---|---|---|
| POST | `` | body `CriacaoDeMassivaInputDTO` | 3 |
| GET | `/getAllMassives` | — | 3 |
| GET | `/recover-via-database` · `/recuperar-pelo-banco` | — | 6 |
| GET | `/estimated-end/contract/{contractNumber}` · `/previsao-finalizacao/contrato/{contractNumber}` | path | 6 |
| POST | `/save-massive-via-api` · `/salvar-massiva-via-api` | header `Idempotency-Key` (opcional) + body `AberturaRegistroMassivoInputDTO` | 6 |
| POST | `/open-infra-solicitation-via-api` · `/abrir-protocolo-infra-via-api` | header `Idempotency-Key` (opcional) + body `AberturaProtocoloInfraInputDTO` | 6 |
| DELETE | `/finalize-ticket-via-api` · `/finalizar-chamado-via-api` | body `FinalizaRegistroMassivoInputDTO` | 6 |

## 2b. GMUD (Elleven) — destino `MAS`
Base: `/api/v1/gmud`. Mesma regra de idempotência e cliente Elleven das massivas, por isso fica no `massivas-service`.
Veio da branch `staging` (`GmudEllevenController`), não existia na `main`.

| Método | Sufixo(s) | Entrada | Rotas |
|---|---|---|---|
| POST | `/abrir-via-api` · `/open-via-api` | header `Idempotency-Key` (opcional) + body `AberturaRegistroMassivoInputDTO` | 2 |
| DELETE | `/encerrar-via-api` · `/finalizar-via-api` | body `FinalizaRegistroMassivoInputDTO` | 2 |
| POST | `/relato-via-api` · `/report-via-api` | body `FinalizaRegistroMassivoInputDTO` | 2 |

## 3. Histórico de massivas — destino `MAS`
Bases (2): `/api/v1/massivas-history`, `/api/v1/massivas-splitters`

| Método | Sufixo(s) | Entrada | Rotas |
|---|---|---|---|
| GET | `/recover-via-database` · `/recuperar-pelo-banco` | — | 4 |
| PATCH | `/{protocol}/finalize` · `/{protocol}/encerrar` | path + body opcional `MassivaHistoryEncerramentoInputDTO` | 4 |

## 4. Matrix — destino `MAS`
| Método | Path | Entrada | Auth |
|---|---|---|---|
| GET | `/api/v1/matrix` | **body** `MatrixMassiveInputDTO` (GET com corpo) | 🌐 |
| POST | `/api/v1/matrix/telefone` | body `MatrixTelefoneInputDTO` (`telefone`) | 🔒 |

## 5. Splitters / OLTs (Elleven) — destino `SPL`
Base: `/api/v1/splitters`

| Método | Sufixo | Entrada |
|---|---|---|
| GET | `/recuperarToken` | — |
| GET | `/listarConnections` | — |
| GET | `/listarSplitters` | — |
| GET | `/{splitterId}` | path |
| GET | `/listarSplitters-paginado` | query `inicio`=0, `quantidade`=20 |
| GET | `/listarOlts` | — |
| GET | `/solicitacoes/cliente/{clientId}` | path |
| POST | `/alertas-protocolo-pendente` | body `{ "pppoes": [...] }` |

## 6. Comercial: vendas, contratos e relatórios do ERP — destino `COM`

Vendas (vendedores, aquisição, primeiro pagante, apoio semanal) e ciclo de vida do contrato (bloqueio, ativação, fatura, pagamento, duplicados) ficam juntos: `WeeklyReportService` e `ContractActivationService` compartilham o `ContractDataService`.

### 6a. Vendas
| Método | Bases | Sufixo(s) | Rotas |
|---|---|---|---|
| GET | `/api/v1/acquisitions` · `/api/v1/aquisicoes` | `/recover-acquisition-orders` · `/recuperar-pedidos-de-aquisicao` | 4 |
| GET | `/api/v1/sellers` · `/api/v1/vendedores` | `/active` · `/ativos` · `/vendedores-ativos` | 6 |
| GET | `/api/v1/reports` · `/api/v1/primeiro-pagante-mensal` · `/api/v1/relatorios` | `/first-monthly-payer` · `/primeiro-pagante-mensal` | 6 |
| GET | `/api/v1/weekly-reports` · `/api/v1/apoio-semanal` | `/vendedor` — **body** `ActiveSellersInputDTO` | 2 |

### 6b. Contratos e qualidade do cadastro
| Método | Bases | Sufixo(s) | Rotas |
|---|---|---|---|
| GET | `/api/v1/contracts` · `/api/v1/contrato-bloqueado` · `/api/v1/contratos` | `/blocked` · `/bloqueados` | 6 |
| GET | `/api/v1/contract-activations` · `/api/v1/contratos` | `/report` | 2 |
| GET | `/api/v1/contracts` · `/api/v1/contrato-ativacao-fatura` · `/api/v1/contratos` | `/pending-activation-invoice` · `/ativacao-pendente-fatura` | 6 |
| GET | `/api/v1/contracts` · `/api/v1/contrato-sem-fatura` · `/api/v1/contratos` | `/without-invoice` · `/sem-fatura` | 6 |
| GET | `/api/v1/contract-payments` | `/first-activation` | 1 |
| GET | `/api/v1/reports` · `/api/v1/relatorio-cliente-nome-duplicado` · `/api/v1/relatorios` | `/duplicate-client-names` · `/clientes-nomes-duplicados` | 6 |

## 7. Radius — destino `RAD`
| Método | Path / bases | Entrada | Rotas |
|---|---|---|---|
| GET | `/api/v1/radius/duplicate-calling-stations` | — | 1 |
| GET | `/api/v1/radius/duplicate-prefixes` | — | 1 |
| GET | `/api/v1/radius/first-authentications` | — | 1 |
| GET | `/api/v1` + (`/high-consumption` · `/consumo` · `/consumo-alto`) | — | 3 |
| GET | `/api/v1` + (`/high-consumption-paged` · `/consumo-alto-paginado`) | query `page`=0, `size`=20 | 2 |
| GET | `/api/v1/contracts` · `/api/v1/ultima-conexao` · `/api/v1/contratos` + (`/last-connections` · `/ultimas-conexoes`) | — | 6 |

## 8. Estoque / patrimônio — destino `EST`
| Método | Path / bases | Entrada | Rotas |
|---|---|---|---|
| POST | `/api/v1/estoque/tecnico` | body `InventoryRequestDTO` | 1 |
| GET | `/api/v1/estoque/movimentacao` | — | 1 |
| GET | `/api/v1/assets` · `/api/v1/patrimonio-pendente` · `/api/v1/patrimonios` + (`/pending` · `/pendentes`) | — | 6 |

## 9. Pessoas / identidade — destino `PES`
| Método | Path | Entrada | Rotas |
|---|---|---|---|
| GET | `/api/v1/employee/get-user-by-token` | JWT | 1 |
| GET | `/api/v1/employee/get-person-id-by-email` | query `email` | 1 |
| GET | `/api/v1/employee/get-person-name-by-cpf` | query `txId` | 1 |
| GET | `/api/v1/employee/get-cpf-by-contract` | query `contract` | 1 |
| GET | `/api/v1/employee/is-corporativo` | query `txId` | 1 |

## 9b. Sites de autenticação (POP/DC) — destino `MAS`
Lookup usado ao abrir massivas (o `title` vira `authenticationSiteCode`), por isso fica junto das massivas e não com pessoas.

| Método | Path | Entrada | Rotas |
|---|---|---|---|
| GET | `/api/v1/sites` | query `title` | 1 |
| GET | `/api/v1/sites/search` | query `q` | 1 |

## 10. Rotas de infraestrutura (hoje no BFF)
| Path | Auth | Observação |
|---|---|---|
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | 🔒 | O gateway deve **agregar** a documentação dos serviços |
| `/error` | 🌐 | Interno do Spring, não é rota de negócio |
| `/api/v1/token/google` | 🔒 | Há regra no `SecurityConfig`, mas **não existe controller**: rota morta; confirmar com os consumidores antes de descartar |

## Armadilhas de compatibilidade (o gateway precisa tratá-las)
1. **GET com corpo**: `/api/v1/matrix`, `/api/v1/weekly-reports/vendedor` (e `apoio-semanal`). O proxy não pode descartar o body nem converter o método.
2. **Colisão de prefixo entre domínios**: `/api/v1/contracts` e `/api/v1/contratos` misturam `COM` e `RAD`
   (`last-connections`); `/api/v1/reports` e `/api/v1/relatorios` ficam inteiros em `COM`. O
   roteamento tem que ser por **path + sufixo**, nunca só por prefixo.
3. **Mesmo prefixo, serviços diferentes**: `/api/v1/massivas-splitters` é `MAS`, enquanto
   `/api/v1/splitters` é `SPL`. Não rotear por `*splitters*`.
4. `Idempotency-Key` precisa chegar intacto ao `MAS` (o `MassivaIdempotencyStore` fica lá).
5. Sufixos declarados sem barra (`salvar-massiva-via-api`, `abrir-protocolo-infra-via-api`,
   `finalizar-chamado-via-api`, `movimentacao`) e base `api/v1/sites` sem barra inicial: o
   Spring normaliza; o gateway deve aceitar o path com `/`.
6. Timeout atual de **600 s** (`spring.mvc.async.request-timeout`, `server.tomcat.connection-timeout`):
   relatórios pesados dependem disso. O gateway deve manter o timeout por rota, não um global baixo.
7. Respostas de erro vêm do `GlobalExceptionHandler` (`ApiError`) e algumas usam o envelope
   `ApiResponse`; o gateway repassa status e corpo sem reescrever.
8. `permitAll` em `GET /api/v1/afetados/contract/**` e `GET /api/v1/matrix` (consumo sem token,
   p.ex. chatbot) deve continuar valendo, só nessas bases.
