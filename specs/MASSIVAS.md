# Especificação: Incidentes Massivos (Elleven API)

## 1. Classificação de Incidentes
O sistema decide automaticamente o tipo de incidente no Elleven com base no impacto:

### Evento Massivo (MASSIVE_EVENT)
- [x] **Condição**: Quantidade de usuários > 15 **OU** presença de pelo menos um contrato B2B.
- [x] **Configurações**: `IncidentTypeId: 1257`, `CatalogServiceId: 1173`, `Category: MASSIVAS - 001`.

### Evento Normal (NORMAL_EVENT)
- [x] **Condição**: Quantidade de usuários <= 15 **E** ausência de contratos B2B.
- [x] **Configurações**: `IncidentTypeId: 1265`, `CatalogServiceId: 1179`, `Category: MASSIVAS - 002`.

## 2. Integração e Fluxos da API
- **Endpoint de Criação (API)**: `/external/integrations/thirdparty/opendetailedsolicitation`
- **Fluxos de Entrada**:
    - [x] `POST /`: Fluxo legado/Flutter que utiliza cookies e formulários (MultiValueMap).
    - [x] `POST /save-massive-via-api`: Novo fluxo JSON direto para a API do Elleven.
- **Finalização**: `DELETE /finalize-ticket-via-api` (Endpoint: `/projects/createsolicitationreport`).
    - [x] **Protocolos Vinculados**: Antes de finalizar a massiva principal, o sistema busca e encerra todos os protocolos vinculados via `FinishLinkedProtocolsService`.
    - [x] **Resiliência**: Se falhar na API externa, retorna `502 Bad Gateway`.

## 3. Autenticação e Resiliência
- [x] **Token**: Bearer Token gerado via usuário integrador a cada requisição.
- [x] **Resiliência de Token**: Em caso de erro `401`, o `TokenRetryAspect` obtém um novo token e refaz a chamada.
- [x] **Delay de Criação**: O fluxo legacy possui um delay configurável (`elleven.creation-delay`, padrão 3s) entre a criação e a adição de pontos de impacto.

## 4. Auditoria e Regras Pendentes
- [x] **Rastreabilidade**: Descrição sufixada com Nome/Email do usuário logado.
- [ ] **Native Integration**: O serviço `EnviarListaDeAfetadosParaNativeService` está atualmente **vazio/não funcional** (apenas um bloco try-catch vazio).
- [ ] **Notificações**: As notificações de e-mail, SMS e Push estão hardcoded como desativadas (`0`) no serviço `AdicionarMassivaNoEllevenService`.

## 5. Camadas Técnicas
- **Controller**: `MassiveElevenController`.
- **Services**: `AdicionarMassivaNoEllevenService` (Legacy), `AdicionarMassivaNoEllevenApiService` (New), `FinalizarMassivaNoEllevenApiService`, `FinishLinkedProtocolsService`.
