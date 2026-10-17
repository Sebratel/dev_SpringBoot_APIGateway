# Uso dos endpoints do BFF em staging (Kibana)

Levantado em 2026-10-17 no Kibana (`https://kibana.sebratel.net.br`), data view `filebeat-*`, container `bff-java-service`.
A lista completa, do menos para o mais usado, está em [`uso-endpoints-elk.csv`](uso-endpoints-elk.csv).

## Como foi medido e o que isso limita
- **Fonte:** linhas `AUTH_AUDIT` do `AuthAuditFilter` (uma por requisição que **passou** pela autorização, incluindo as 2 rotas públicas).
  Requisições recusadas com 401 **não** entram na conta.
- **Janela real: 2026-08-19 a 2026-10-17 (~2 meses), não 3.** Os logs do container só existem no Elasticsearch desde 19/08.
  O APM do serviço `api-gateway` só existe desde 07/10 (só 10 dias), por isso não serve para os 3 meses.
- **Agrupamento:** cada caminho foi casado com o manifesto das 150 rotas (`platform/contract-tests/.../manifest.json`),
  trocando os ids por `{variavel}`. Total casado: **226.054 requisições**; 15 ficaram fora do manifesto (abaixo).
- O ambiente consultado é o do Kibana do stage; o APM mostra que o serviço `api-gateway` reporta só o ambiente `production`.
  Confirme com o time se esse `bff-java-service` é de fato o stage ou a produção antes de decidir qualquer remoção.

## Resumo
- **17 das 150 rotas** receberam tráfego. **133 rotas, 89%, não tiveram nenhuma requisição.**
- **4 rotas concentram 92%** do tráfego: `matrix` (93.547), `is-corporativo` (56.650), `massivas/recuperar-pelo-banco` (44.669) e `afetados/contract/{id}` (13.542).
- Cada rota usada o foi por **um único alias**. Nenhum consumidor usa `impacted-users`, `usuario-afetado`, `massive-incidents`, `massivas-eleven`,
  `contrato-*`, `contratos`, `reports`, `vendedores`, etc.

## Rotas usadas, do menos para o mais usado
| Requisições | Serviço | Rota |
|---:|---|---|
| 3 | comercial | `GET /api/v1/relatorios/clientes-nomes-duplicados` |
| 9 | radius | `GET /api/v1/radius/duplicate-calling-stations` |
| 9 | radius | `GET /api/v1/radius/duplicate-prefixes` |
| 64 | massivas | `GET /api/v1/sites/search` |
| 110 | splitters | `POST /api/v1/splitters/alertas-protocolo-pendente` |
| 113 | massivas | `PATCH /api/v1/afetados/protocol/{protocol}` |
| 179 | massivas | `POST /api/v1/massivas/abrir-protocolo-infra-via-api` |
| 505 | massivas | `DELETE /api/v1/massivas/finalizar-chamado-via-api` |
| 527 | massivas | `POST /api/v1/afetados` |
| 576 | massivas | `POST /api/v1/massivas/salvar-massiva-via-api` |
| 2.055 | pessoas | `GET /api/v1/employee/get-person-id-by-email` |
| 3.098 | splitters | `GET /api/v1/splitters/solicitacoes/cliente/{clientId}` |
| 10.398 | massivas | `DELETE /api/v1/afetados/protocol/{protocol}` |
| 13.542 | massivas | `GET /api/v1/afetados/contract/{contractId}` |
| 44.669 | massivas | `GET /api/v1/massivas/recuperar-pelo-banco` |
| 56.650 | pessoas | `GET /api/v1/employee/is-corporativo` |
| 93.547 | massivas | `GET /api/v1/matrix` |

## Fora do manifesto (15 requisições)
| Requisições | Rota | Observação |
|---:|---|---|
| 1 | `GET /api/v1/auto_login` | sondagem de scanner |
| 1 | `POST /api/v1/auto_login` | sondagem de scanner |
| 1 | `DELETE /api/v1/gmud/encerrar-via-api` | GMUD (branch staging, fora do manifesto) |
| 3 | `GET /api/v1/.env` | sondagem de scanner |
| 3 | `POST /api/v1/gmud/relato-via-api` | GMUD (branch staging, fora do manifesto) |
| 6 | `POST /api/v1/gmud/abrir-via-api` | GMUD (branch staging, fora do manifesto) |

- **GMUD existe e está em uso, mas não está no inventário.** `GmudEllevenController` (`/api/v1/gmud/abrir-via-api`, `relato-via-api`,
  `encerrar-via-api`, mais os aliases `open-via-api`, `report-via-api`, `finalizar-via-api`) está nas branches `origin/staging` e
  `origin/feat/gmud`, **não** na `main` de onde a branch `apigateway` foi criada. São **6 rotas a mais** (156 no total) e a migração precisa
  incluí-las: rebasear a `apigateway` sobre a `staging` e regenerar o manifesto.
- `/api/v1/auto_login` e `/api/v1/.env` são sondagens de scanner; nenhum consumidor legítimo.

## Rotas sem nenhuma requisição (133)
Agrupadas por serviço de destino; dentro de cada grupo, em ordem alfabética.


**comercial**

- `GET /api/v1/acquisitions/recover-acquisition-orders`
- `GET /api/v1/acquisitions/recuperar-pedidos-de-aquisicao`
- `GET /api/v1/apoio-semanal/vendedor`
- `GET /api/v1/aquisicoes/recover-acquisition-orders`
- `GET /api/v1/aquisicoes/recuperar-pedidos-de-aquisicao`
- `GET /api/v1/contract-activations/report`
- `GET /api/v1/contract-payments/first-activation`
- `GET /api/v1/contracts/ativacao-pendente-fatura`
- `GET /api/v1/contracts/blocked`
- `GET /api/v1/contracts/bloqueados`
- `GET /api/v1/contracts/pending-activation-invoice`
- `GET /api/v1/contracts/sem-fatura`
- `GET /api/v1/contracts/without-invoice`
- `GET /api/v1/contrato-ativacao-fatura/ativacao-pendente-fatura`
- `GET /api/v1/contrato-ativacao-fatura/pending-activation-invoice`
- `GET /api/v1/contrato-bloqueado/blocked`
- `GET /api/v1/contrato-bloqueado/bloqueados`
- `GET /api/v1/contrato-sem-fatura/sem-fatura`
- `GET /api/v1/contrato-sem-fatura/without-invoice`
- `GET /api/v1/contratos/ativacao-pendente-fatura`
- `GET /api/v1/contratos/blocked`
- `GET /api/v1/contratos/bloqueados`
- `GET /api/v1/contratos/pending-activation-invoice`
- `GET /api/v1/contratos/report`
- `GET /api/v1/contratos/sem-fatura`
- `GET /api/v1/contratos/without-invoice`
- `GET /api/v1/primeiro-pagante-mensal/first-monthly-payer`
- `GET /api/v1/primeiro-pagante-mensal/primeiro-pagante-mensal`
- `GET /api/v1/relatorio-cliente-nome-duplicado/clientes-nomes-duplicados`
- `GET /api/v1/relatorio-cliente-nome-duplicado/duplicate-client-names`
- `GET /api/v1/relatorios/duplicate-client-names`
- `GET /api/v1/relatorios/first-monthly-payer`
- `GET /api/v1/relatorios/primeiro-pagante-mensal`
- `GET /api/v1/reports/clientes-nomes-duplicados`
- `GET /api/v1/reports/duplicate-client-names`
- `GET /api/v1/reports/first-monthly-payer`
- `GET /api/v1/reports/primeiro-pagante-mensal`
- `GET /api/v1/sellers/active`
- `GET /api/v1/sellers/ativos`
- `GET /api/v1/sellers/vendedores-ativos`
- `GET /api/v1/vendedores/active`
- `GET /api/v1/vendedores/ativos`
- `GET /api/v1/vendedores/vendedores-ativos`
- `GET /api/v1/weekly-reports/vendedor`

**estoque**

- `GET /api/v1/assets/pendentes`
- `GET /api/v1/assets/pending`
- `GET /api/v1/estoque/movimentacao`
- `POST /api/v1/estoque/tecnico`
- `GET /api/v1/patrimonio-pendente/pendentes`
- `GET /api/v1/patrimonio-pendente/pending`
- `GET /api/v1/patrimonios/pendentes`
- `GET /api/v1/patrimonios/pending`

**massivas**

- `GET /api/v1/afetados`
- `GET /api/v1/afetados/pppoe/{pppoe}`
- `GET /api/v1/afetados/protocol/{protocol}`
- `GET /api/v1/impacted-users`
- `POST /api/v1/impacted-users`
- `GET /api/v1/impacted-users/contract/{contractId}`
- `GET /api/v1/impacted-users/pppoe/{pppoe}`
- `DELETE /api/v1/impacted-users/protocol/{protocol}`
- `GET /api/v1/impacted-users/protocol/{protocol}`
- `PATCH /api/v1/impacted-users/protocol/{protocol}`
- `POST /api/v1/massivas`
- `POST /api/v1/massivas-eleven`
- `POST /api/v1/massivas-eleven/abrir-protocolo-infra-via-api`
- `GET /api/v1/massivas-eleven/estimated-end/contract/{contractNumber}`
- `DELETE /api/v1/massivas-eleven/finalizar-chamado-via-api`
- `DELETE /api/v1/massivas-eleven/finalize-ticket-via-api`
- `GET /api/v1/massivas-eleven/getAllMassives`
- `POST /api/v1/massivas-eleven/open-infra-solicitation-via-api`
- `GET /api/v1/massivas-eleven/previsao-finalizacao/contrato/{contractNumber}`
- `GET /api/v1/massivas-eleven/recover-via-database`
- `GET /api/v1/massivas-eleven/recuperar-pelo-banco`
- `POST /api/v1/massivas-eleven/salvar-massiva-via-api`
- `POST /api/v1/massivas-eleven/save-massive-via-api`
- `GET /api/v1/massivas-history/recover-via-database`
- `GET /api/v1/massivas-history/recuperar-pelo-banco`
- `PATCH /api/v1/massivas-history/{protocol}/encerrar`
- `PATCH /api/v1/massivas-history/{protocol}/finalize`
- `GET /api/v1/massivas-splitters/recover-via-database`
- `GET /api/v1/massivas-splitters/recuperar-pelo-banco`
- `PATCH /api/v1/massivas-splitters/{protocol}/encerrar`
- `PATCH /api/v1/massivas-splitters/{protocol}/finalize`
- `GET /api/v1/massivas/estimated-end/contract/{contractNumber}`
- `DELETE /api/v1/massivas/finalize-ticket-via-api`
- `GET /api/v1/massivas/getAllMassives`
- `POST /api/v1/massivas/open-infra-solicitation-via-api`
- `GET /api/v1/massivas/previsao-finalizacao/contrato/{contractNumber}`
- `GET /api/v1/massivas/recover-via-database`
- `POST /api/v1/massivas/save-massive-via-api`
- `POST /api/v1/massive-incidents`
- `POST /api/v1/massive-incidents/abrir-protocolo-infra-via-api`
- `GET /api/v1/massive-incidents/estimated-end/contract/{contractNumber}`
- `DELETE /api/v1/massive-incidents/finalizar-chamado-via-api`
- `DELETE /api/v1/massive-incidents/finalize-ticket-via-api`
- `GET /api/v1/massive-incidents/getAllMassives`
- `POST /api/v1/massive-incidents/open-infra-solicitation-via-api`
- `GET /api/v1/massive-incidents/previsao-finalizacao/contrato/{contractNumber}`
- `GET /api/v1/massive-incidents/recover-via-database`
- `GET /api/v1/massive-incidents/recuperar-pelo-banco`
- `POST /api/v1/massive-incidents/salvar-massiva-via-api`
- `POST /api/v1/massive-incidents/save-massive-via-api`
- `GET /api/v1/sites`
- `GET /api/v1/usuario-afetado`
- `POST /api/v1/usuario-afetado`
- `GET /api/v1/usuario-afetado/contract/{contractId}`
- `GET /api/v1/usuario-afetado/pppoe/{pppoe}`
- `DELETE /api/v1/usuario-afetado/protocol/{protocol}`
- `GET /api/v1/usuario-afetado/protocol/{protocol}`
- `PATCH /api/v1/usuario-afetado/protocol/{protocol}`

**pessoas**

- `GET /api/v1/employee/get-cpf-by-contract`
- `GET /api/v1/employee/get-person-name-by-cpf`
- `GET /api/v1/employee/get-user-by-token`
- `GET /api/v1/qr-code/gerar`
- `POST /api/v1/qr-code/recuperar`

**radius**

- `GET /api/v1/consumo`
- `GET /api/v1/consumo-alto`
- `GET /api/v1/consumo-alto-paginado`
- `GET /api/v1/contracts/last-connections`
- `GET /api/v1/contracts/ultimas-conexoes`
- `GET /api/v1/contratos/last-connections`
- `GET /api/v1/contratos/ultimas-conexoes`
- `GET /api/v1/high-consumption`
- `GET /api/v1/high-consumption-paged`
- `GET /api/v1/radius/first-authentications`
- `GET /api/v1/ultima-conexao/last-connections`
- `GET /api/v1/ultima-conexao/ultimas-conexoes`

**splitters**

- `GET /api/v1/splitters/listarConnections`
- `GET /api/v1/splitters/listarOlts`
- `GET /api/v1/splitters/listarSplitters`
- `GET /api/v1/splitters/listarSplitters-paginado`
- `GET /api/v1/splitters/recuperarToken`
- `GET /api/v1/splitters/{splitterId}`

## O que fazer com isso
1. **Não remover nada agora.** O gateway deve manter as 150 rotas (decisão da spec), porque 2 meses sem uso não provam que nunca serão chamadas
   (relatórios mensais, scripts de planilha, n8n). Mas dá para **priorizar**: a extração e o teste de contrato devem começar pelas 17 rotas usadas.
2. **Aliases sem uso** são os candidatos naturais à depreciação (fase 5 da spec): as demais variantes de URL das rotas usadas não tiveram tráfego.
3. **Incluir o GMUD** no inventário antes de extrair o `massivas-service`.
4. As 18 rotas lentas conhecidas (consumo, última conexão, primeiro pagante) **também têm 0 uso**: reforça que o tratamento de timeout longo
   não é urgente e pode ficar para depois.

