# Pré-requisitos externos ao repositório

A esteira de CI e a instrumentação de APM estão versionadas — mas parte da configuração
vive **fora** deste repositório, em plataformas de terceiros ou na interface do GitHub.
Este documento é a lista completa dessas dependências externas.

Sem elas o pipeline não fica verde, e o motivo não é óbvio olhando só o código.

| # | Plataforma | O que configurar | Obrigatório? | Bloqueia |
|---|---|---|---|---|
| 1 | **NVD** (NIST) | Chave de API → secret `NVD_API_KEY` | Sim | `Dependency Security` |
| 2 | **GitHub** | Dependabot alerts + security updates | Recomendado | nada |
| 3 | **GitHub** | Branch protection com checks obrigatórios | Recomendado | nada |
| 4 | **Host Docker** | Rede externa `elk_es_network` | Sim, para o APM | subida do container |
| 5 | **Proxy reverso** | Rate limiting (finding F-10) | Pendente | nada |

---

## 1. NVD — chave de API

O OWASP Dependency-Check consulta a base de vulnerabilidades do NIST. Sem chave, a API
aplica *rate limit* agressivo: a atualização da base passa de 40 minutos e termina em erro.

### Solicitar

<https://nvd.nist.gov/developers/request-an-api-key>

É **gratuito**. O formulário pede apenas três campos:

| Campo | Valor |
|---|---|
| Organization Name | `Sebratel` |
| Email Address | um e-mail acessível no momento — o link chega nele |
| Organization Type | `Commercial` |

### Ativar

O NVD envia um e-mail com um **link de uso único** que exibe a chave. Dois detalhes que
custam uma nova solicitação se passarem batido:

- o link **expira em 7 dias**;
- é **uso único** — copie a chave assim que ela aparecer.

### Cadastrar

No GitHub: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`.

- **Name:** `NVD_API_KEY` — exatamente assim, é o nome que o workflow procura
- **Secret:** o valor copiado

> **A chave vai do e-mail do NVD direto para o cofre de Secrets do GitHub.**
> Não colocar em `.env`, `docker-compose.yml`, código, chat ou ticket. Os termos de uso do
> NVD dizem explicitamente que a chave não deve ser compartilhada além de quem solicitou.

### Sintoma quando falta

O job `Dependency Security` falha em poucos segundos — de propósito — com as instruções
no resumo do run. O comportamento é deliberado: uma varredura de dependências
silenciosamente ignorada produz uma garantia que não existe.

Se for necessário desbloquear o time antes de a chave chegar, troque o `exit 1` do passo
`Exigir NVD_API_KEY` por um `::warning::`. É uma linha — mas registre a decisão, porque
enquanto isso não há cobertura de CVE em dependências.

### Primeira execução

Mesmo com a chave, a primeira execução baixa a base inteira e demora. As seguintes são
rápidas: o job persiste a base em cache com `if: always()`, portanto ela sobrevive mesmo
quando o gate bloqueia por encontrar CVE.

---

## 2. GitHub — Dependabot

`Settings` → `Advanced Security` (ou `Code security`) → habilitar **Dependabot alerts** e
**Dependabot security updates**.

O grafo de dependências é alimentado pelo job de CI
(`advanced-security/maven-dependency-submission-action`), que roda apenas em `push`.

### Duas armadilhas

**O Dependabot varre a branch default.** Hoje é `massivas-integracao`. Uma correção feita
em `staging` não fecha o alerta até chegar lá.

**A correção proposta pelo Dependabot não é sempre a correta.** Caso real deste
repositório: o alerta sobre `io.github.bonigarcia:webdrivermanager` (XXE, CVSS 9.3)
propunha subir para `6.1.0`. A dependência **não era usada por nenhuma linha de código** —
o certo foi removê-la, junto com ~25 artefatos transitivos. Atualizar teria mantido a
superfície de ataque e apenas movido o problema de lugar.

Antes de aceitar um bump, verifique se a dependência é usada:

```bash
git grep -l -iE "NomeDaClassePrincipal|pacote.da.lib" -- 'src/**/*.java'
```

Sem resultado, a resposta é remover, não atualizar.

---

## 3. GitHub — branch protection

`Settings` → `Branches` → regra para `main` e `staging`.

Checks sugeridos como obrigatórios:

- `CI / Build e Testes`
- `Security / Security Gate`

O `Security Gate` já agrega as quatro verificações de segurança, então exigir só ele
cobre secret scanning, dependências, SAST e testes de segurança.

> Sem branch protection os workflows rodam mas **não impedem** o merge. O gate só tem
> efeito real quando é exigido aqui.

---

## 4. Host Docker — rede do ELK

O `docker-compose.yml` declara a rede como **externa**:

```yaml
networks:
  elk-network:
    name: ${ELK_NETWORK_NAME:-elk_es_network}
    external: true
```

Ou seja, ela precisa **já existir** no host — é a rede do stack do ELK, criada por ele,
não por este projeto. Se não existir, o container não sobe.

Verificar:

```bash
docker network ls | grep elk_es_network
```

E confirmar a resolução no compose:

```bash
docker compose config
```

Deve mostrar `elk-network` com `name: elk_es_network` e `external: true`, mantendo também
`proxy` e `default`. O roteiro completo de validação do APM está em
[APM-VALIDACAO.md](APM-VALIDACAO.md).

---

## 5. Proxy reverso — rate limiting

Finding **F-10**: não existe rate limiting em nenhuma camada. Não deve ser implementado em
Java — o container já está atrás de uma rede `proxy` externa, e o lugar certo é o reverse
proxy, onde é configuração declarativa com rollback trivial.

Endpoints prioritários, em ordem:

| Endpoint | Motivo |
|---|---|
| `GET /api/v1/matrix` | público, dispara 2+ queries no ERP de produção por chamada |
| `GET /api/v1/afetados/contract/{id}` | público e enumerável |
| HTTP Basic (global) | senha força-brutável, sem lockout |
| `POST /api/v1/massivas*` | escreve no ERP de produção |

Também vale um `client_max_body_size` no proxy: o Spring MVC **não impõe limite** de
tamanho para corpos JSON (finding F-11).

---

## Checklist de implantação

- [ ] `NVD_API_KEY` cadastrado como secret
- [ ] Job `Dependency Security` verde após re-run
- [ ] Dependabot alerts habilitado
- [ ] Dependabot security updates habilitado
- [ ] Branch protection em `main` exigindo `CI / Build e Testes` e `Security / Security Gate`
- [ ] Branch protection em `staging`
- [ ] Rede `elk_es_network` existe no host
- [ ] `docker compose config` resolve `elk-network` corretamente
- [ ] Rate limiting configurado no proxy reverso
- [ ] Rotação da chave RSA (finding F-01) — depende de decisão de negócio
