-- =====================================================================================================
-- assignment_clients: coluna `origin` (MASSIVA | GMUD) + indices de consulta
-- Banco: DB_Massives (MySQL do app Splitters, o mesmo de massiva_history)
--
-- A presenca de uma linha nesta tabela e o gatilho do aviso ao cliente. `origin` so diz DE QUAL
-- tipo de evento a linha veio. Linhas existentes ficam 'MASSIVA' (DEFAULT) - nenhum dado e reescrito.
--
-- ORDEM DE DEPLOY: aplicar este SQL ANTES de subir o gateway que mapeia `origin`
-- (sem a coluna, o INSERT do gateway novo falha). O gateway antigo ignora a coluna: pode rodar depois.
-- NAO foi aplicado em nenhum ambiente. Testar primeiro em staging.
-- =====================================================================================================

-- 1) Conferir antes (esperado: nenhuma linha). Se ja existir, NAO rodar o ALTER.
SELECT COLUMN_NAME
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'assignment_clients' AND COLUMN_NAME = 'origin';

-- 2) Coluna de origem (aditivo; tabela pequena: centenas de linhas)
ALTER TABLE assignment_clients
  ADD COLUMN origin VARCHAR(10) NOT NULL DEFAULT 'MASSIVA' AFTER corporate_client;

-- 3) Indices: hoje so existe a chave primaria. A checagem "cliente ja esta em outro evento" e as
--    consultas do Matrix/Native filtram por contrato e por protocolo.
CREATE INDEX idx_assignment_clients_contract ON assignment_clients (contract_id);
CREATE INDEX idx_assignment_clients_protocol ON assignment_clients (protocol_id);

-- 4) Conferir depois
SELECT origin, COUNT(*) AS linhas FROM assignment_clients GROUP BY origin;   -- tudo 'MASSIVA'
SHOW INDEX FROM assignment_clients;

-- =====================================================================================================
-- ROLLBACK (so se necessario; o gateway novo deixa de funcionar sem a coluna, volte o gateway antes)
-- DROP INDEX idx_assignment_clients_protocol ON assignment_clients;
-- DROP INDEX idx_assignment_clients_contract ON assignment_clients;
-- ALTER TABLE assignment_clients DROP COLUMN origin;
-- =====================================================================================================
