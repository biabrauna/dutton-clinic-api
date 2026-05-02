-- ============================================================
-- V3 — Corrige tipo da coluna dataNascimento em CadastroPac
-- Contexto: coluna foi criada como VARCHAR pelo ddl-auto=update
-- antes da adoção de migrations Flyway. O Hibernate (validate)
-- exige DATE. MySQL converte os valores YYYY-MM-DD para DATE.
-- ============================================================

ALTER TABLE CadastroPac
    MODIFY COLUMN dataNascimento DATE NULL;
