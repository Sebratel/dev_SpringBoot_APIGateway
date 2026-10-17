# Passos de execução: BFF → API Gateway

Complementa [API_GATEWAY.md](API_GATEWAY.md). Cada fase termina com um critério de saída verificável
e é reversível. Referência de rotas: [API_GATEWAY_ENDPOINTS.md](API_GATEWAY_ENDPOINTS.md).

## Fase 0 — Preparação e rede de segurança

> **Estado:** itens 2 e 4 prontos (manifesto das 156 rotas gerado do código e guardado por teste; harness no módulo `contract-tests` do repositório `dev_SpringBoot_Gateway`). Falta: golden files (item 3, exigem stage), item 1, spike (5) e topologia de bancos (6).

1. Responder as questões em aberto (§13 da spec) e fechar as decisões D1–D8.
2. **Gerar o manifesto de rotas**: exportar `/v3/api-docs` do monólito atual para `contract-tests/manifest.json` e conferir contra o inventário (156 rotas). Qualquer diferença corrige o inventário.
3. **Gravar golden files**: capturar requisição/resposta reais (stage) de cada rota, anonimizadas, em `contract-tests/golden/`.
4. Escrever o **teste de contrato** que executa o manifesto e os golden files contra uma URL base (hoje o monólito). Deve passar 100 % antes de qualquer mudança: é a linha de base.
5. Spike: dependências cruzadas (`MatrixService` → `AffectedUserService`/`ContractDataService`, `ContractProjection`, `PersonRepository`) e quais classes vão para `common-*`.
6. Verificar topologia de bancos e corrigir `specs/TEMPLATE.md`.

**Saída:** teste de contrato verde contra o monólito; mapa de dependências aprovado.

## Fase 1 — Repositórios e base compartilhada
1. Criar os repositórios (`dev_SpringBoot_Gateway`, `dev_SpringBoot_<Servico>Service` x6 e `dev_Maven_Commons`), cada um com o esqueleto, o CI e os templates do `sebratel-parent`. **Feito em 2026-10-17.**
2. O monólito **não muda de lugar**: continua em `dev_SpringBoot_APIGateway` e faz o papel de `legacy-bff` até a fase 4.
3. Extrair para `dev_Maven_Commons` o que é compartilhado (`ApiResponse`, `ApiError`, `GlobalExceptionHandler`, `JacksonConfig`, JWT validator, `GetToken`, `JwtInformation`, cliente Elleven) e publicar no GitHub Packages. Serviços e gateway passam a depender dele quando houver código a usar.
4. O CI de cada repositório lê o parent (`sebratel-java-parent`, de `dev_Maven_JavaParent`) com o `GITHUB_TOKEN`. Por isso **todos os repositórios são públicos**: a organização só permite a um owner mudar a visibilidade ou conceder acesso a pacotes privados, então os repositórios foram recriados como públicos e as cópias privadas anteriores ficaram arquivadas com o sufixo `-privado-antigo` (um owner pode excluí-las).

**Saída:** os 8 repositórios constroem no CI (verde em 2026-10-17); o monólito segue verde e publicado como hoje.

## Fase 2 — Gateway “transparente”
1. Criar o módulo `gateway` (Spring Cloud Gateway) com **uma rota coringa para o `legacy-bff`**: tudo passa, nada muda.
2. Portar para o gateway: segurança JWT/Basic e `permitAll` (RF4), CORS, `AuthAuditFilter`, `RequestIdFilter`, mensagem 401 atual.
3. Configurar timeouts (600 s onde aplicável), propagação de `Authorization` e GET com corpo.
4. Subir no compose na frente do monólito (porta pública nova, **sem trocar** a porta atual).
5. Rodar o teste de contrato **via gateway**. Deve passar 100 %.
6. Apontar um consumidor de baixo risco (ex.: planilhas/relatórios) para o gateway em stage; depois em produção.

**Saída:** 156 rotas verdes via gateway; consumidores trocam só o host/porta. Gateway já traz CB, rate limit e métricas sobre o monólito.

## Fase 3 — Extração dos serviços (um por vez)
Para **cada** serviço, na ordem abaixo:
1. Criar o módulo, mover (`git mv`) controllers, services, repositories, DTOs, config de datasource e **os testes correspondentes** do `legacy-bff`.
2. Habilitar resource server (JWT) no serviço e o OpenAPI próprio.
3. Subir no compose na rede interna, sem porta publicada.
4. Gerar as rotas do domínio em `gateway/.../routes/<dominio>.yml` a partir do inventário e trocar o `uri` do monólito para o serviço.
5. Rodar o teste de contrato do domínio + smoke em stage.
6. Remover o código movido do `legacy-bff`.
7. Observar ≥ 1 semana (erros, latência, CB). Rollback: `uri` volta ao `legacy-bff`.

| Ordem | Serviço | Por quê nesta posição | Rotas |
|---|---|---|---|
| 3.1 | `comercial-service` | Só leitura sobre o ERP, sem integração externa, risco baixo | 45 |
| 3.2 | `estoque-service` | Pequeno, só consulta | 8 |
| 3.3 | `pessoas-service` | Identidade/QR Code; as chaves RSA saem do repositório | 7 |
| 3.4 | `radius-service` | Banco próprio, consultas pesadas (isola o pool) | 14 |
| 3.5 | `splitters-service` | Primeiro uso do `common-elleven` + `@TokenRetry` | 8 |
| 3.6 | `massivas-service` | O mais crítico: escrita, idempotência, Elleven, Native, 2 bancos. Por último, com mais observação. Inclui o GMUD | 74 |

Contagem de rotas por destino (45 + 8 + 7 + 14 + 8 + 74 = 156), conferir contra o manifesto na fase 0.

## Fase 4 — Limpeza e endurecimento
1. Remover `legacy-bff` quando não restar nenhuma rota apontando para ele.
2. Remover o `/api/v1/token/google` do `SecurityConfig` se confirmada rota morta.
3. Retirar do repositório as chaves `id_rsa_*.pem` (rotacionar) e mover segredos para variáveis de ambiente.
4. Tratar o **F-03** (validar `aud` do JWT) com o levantamento de client IDs, já em `common-security`.
5. Decidir sobre o Basic auth com base nos dados do `AuthAuditFilter`.
6. Opcional: separar cada serviço em repositório próprio; documentar runbook e ADRs.

**Saída:** o repositório contém apenas gateway + serviços; nenhum código de domínio no gateway (RG4).

## Fase 5 — Evolução (fora do escopo desta entrega)
Descontinuar aliases PT/EN com aviso, introduzir `/api/v2`, cache no gateway para relatórios, mensageria para massivas.

## Checklist de entrega por serviço
- [ ] Controllers/services/repositories/DTOs movidos com histórico (`git mv`)
- [ ] Testes migrados e verdes
- [ ] Mesmo envelope de erro (`common-web`)
- [ ] JWT validado no serviço; segredos só os necessários
- [ ] Healthcheck + OpenAPI
- [ ] Rotas no gateway geradas do inventário
- [ ] Teste de contrato do domínio verde
- [ ] Sem porta publicada
- [ ] Rollback testado em stage
- [ ] CHANGELOG atualizado

## Changelog planejado (resumo por marco)
| Marco | Versão alvo | Conteúdo |
|---|---|---|
| Fase 1 | `3.4.3` | Reestruturação multi-módulo, sem mudança de comportamento (PATCH) |
| Fase 2 | `3.5.0` | Gateway na frente do monólito: CB, rate limit, request-id (MINOR) |
| Fase 3.1–3.5 | `3.5.x` | Cada serviço extraído (PATCH, mesmo contrato) |
| Fase 3.6 | `3.6.0` | Massivas extraído; monólito sem rotas de negócio (MINOR) |
| Fase 4 | `3.7.0` | Remoção do `legacy-bff`, limpeza de segurança (MINOR; MAJOR só se algum endpoint for removido) |
