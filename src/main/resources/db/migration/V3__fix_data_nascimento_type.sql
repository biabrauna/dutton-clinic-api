-- ============================================================
-- V3 — Corrige tipo da coluna data_nascimento em cadastro_pac
--
-- Contexto: no Railway o Hibernate criou as tabelas via
-- ddl-auto=update com SpringPhysicalNamingStrategy, que converte
-- CadastroPac -> cadastro_pac e dataNascimento -> data_nascimento.
-- O banco armazenou data_nascimento como VARCHAR; o validate
-- exige DATE. Este script corrige o tipo de forma idempotente:
-- só executa se a coluna ainda for do tipo VARCHAR/TEXT.
-- ============================================================

-- Usa stored procedure para poder verificar o tipo antes de alterar,
-- evitando falha em ambientes onde a coluna já é DATE.
DROP PROCEDURE IF EXISTS fix_data_nascimento;

CREATE PROCEDURE fix_data_nascimento()
BEGIN
  DECLARE col_type VARCHAR(64) DEFAULT NULL;

  -- Verifica se a coluna existe e qual é o seu tipo atual
  SELECT DATA_TYPE INTO col_type
  FROM   information_schema.COLUMNS
  WHERE  TABLE_SCHEMA = DATABASE()
    AND  TABLE_NAME   = 'cadastro_pac'
    AND  COLUMN_NAME  = 'data_nascimento';

  -- Só altera se a coluna existir e ainda for do tipo texto
  IF col_type IN ('varchar', 'text', 'char', 'tinytext', 'mediumtext', 'longtext') THEN
    SET SESSION sql_mode = '';

    -- Nulifica valores que não seguem o padrão YYYY-MM-DD
    UPDATE cadastro_pac
    SET    data_nascimento = NULL
    WHERE  data_nascimento IS NOT NULL
      AND  data_nascimento NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$';

    -- Converte VARCHAR → DATE
    ALTER TABLE cadastro_pac
        MODIFY COLUMN data_nascimento DATE NULL;
  END IF;
END;

CALL fix_data_nascimento();

DROP PROCEDURE IF EXISTS fix_data_nascimento;
