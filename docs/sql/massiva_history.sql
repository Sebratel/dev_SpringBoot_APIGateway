-- ATENÇÃO: estas tabelas JÁ EXISTEM no MariaDB `afetados` (DB_Massives) e são populadas
-- pela aplicação de Splitters (source = 'nexaview-local'). O APIGateway apenas LÊ delas.
-- Este arquivo é apenas REFERÊNCIA do schema real (não executar para (re)criar).

-- massiva_history: 1 linha por protocolo/massiva. status: 'aberta' | 'encerrada'.
-- Reconciliação: `protocol`/`assignment_id` fazem o join com o lado Elleven;
-- `updated_at` (ON UPDATE CURRENT_TIMESTAMP) é o timestamp da última ação no lado Splitters.
CREATE TABLE massiva_history (
    id                          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    protocol                    BIGINT          NULL UNIQUE,
    assignment_id               BIGINT          NULL UNIQUE,
    access_point_code           VARCHAR(120)    NOT NULL,
    title                       VARCHAR(255)    NOT NULL,
    operator_email              VARCHAR(180)    NOT NULL,
    affected_clients            INT             NOT NULL DEFAULT 0,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'aberta',
    opened_at                   DATETIME        NULL,
    event_identified_at         DATETIME        NULL,
    expected_close_at           DATETIME        NULL,
    closed_at                   DATETIME        NULL,
    auto_closed_without_clients TINYINT(1)      NOT NULL DEFAULT 0,
    close_description           TEXT            NULL,
    source                      VARCHAR(40)     NOT NULL DEFAULT 'nexaview-local',
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    closed_by                   VARCHAR(255)    NULL,
    PRIMARY KEY (id)
);

-- massiva_history_splitters: detalhe de splitters afetados por massiva (não exposto no endpoint).
CREATE TABLE massiva_history_splitters (
    id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    massiva_history_id BIGINT UNSIGNED NOT NULL,
    splitter_code      VARCHAR(120)    NOT NULL,
    splitter_label     VARCHAR(255)    NOT NULL,
    created_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_massiva_history_splitters_history (massiva_history_id)
);
