# Especificação: Usuários Afetados

## 1. Fluxo de Criação e Persistência
- **Endpoints**: `/api/v1/impacted-users`, `/api/v1/usuario-afetado`, `/api/v1/afetados`.
- **Persistência**: Os dados são salvos no banco de dados PostgreSQL utilizando o `afetadosTransactionManager`.
- **Classificação de Cliente**: O sistema diferencia entre `CORPORATE` e `NORMAL` consultando se o contrato possui B2B via `EmployeeService.hasB2BinInput`.

## 2. Regras de Negócio e Resiliência
- [x] **Rollback Externo (Elleven)**: Se a persistência no banco local falhar durante a criação (`createImpactedUsersDTO`), o sistema **OBRIGATORIAMENTE** chama `finalizarMassivaNoEllevenApiService.executar` com status `8` (Cancelado/Erro).
- [x] **Validação de Lista**: Não é permitido criar uma massiva/lista de afetados sem nenhum usuário. Uma `DomainException` é lançada.
- [x] **Estimativa de Restauração**:
    - Calculada em horas entre `now` e `finishDate`.
    - Se o resultado for `<= 0`, o valor padrão retornado no DTO é **2 horas**.
- [x] **Data de Finalização**: Suporta atualização via `PATCH /protocol/{protocol}`. Se o protocolo não existir, retorna `404 Not Found`.

## 3. Consultas e Filtros
- [x] **Busca por Protocolo**: Retorna todos os usuários vinculados a um incidente.
- [x] **Busca por PPPoE**: Busca um usuário específico.
- [x] **Busca por Contract ID**: Busca o primeiro registro vinculado a um contrato.
- [x] **Deleção**: Permite remover todos os usuários de um protocolo específico via `DELETE`.

## 4. Tratamento de Erros
- [x] **Parser de Banco**: Erros de banco de dados são processados pelo `DatabaseErrorParser.parse()` para retornar mensagens amigáveis.
- [x] **Exceções**: Uso de `ResourceNotFoundException` para buscas falhas e `DomainException` para erros de negócio.

## 5. Camadas Técnicas
- **Controller**: `AffectedUserController`.
- **Service**: `AffectedUserService`.
- **Repositório**: `AffectedUserRepository` (PostgreSQL).
