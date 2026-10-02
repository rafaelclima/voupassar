-- V8__question_figures.sql — TASK 6.6 / docs/figuras-estrategia.md
-- Tabela para metadados de figuras oficiais (não o binário, que fica no frontend/assets/figures/).
-- Referência ao banco de questões (per-edition manifest) sem redistribuir conteúdo.
-- Idempotente: ON CONFLICT DO NOTHING + trigger de atualização.

CREATE TABLE IF NOT EXISTS question_figures (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  position SMALLINT NOT NULL DEFAULT 1 CHECK (position > 0),
  file_path TEXT NOT NULL CHECK (file_path <> ''),
  alt_text TEXT NOT NULL CHECK (alt_text <> ''),
  page SMALLINT NULL CHECK (page IS NULL OR page > 0),
  publication_status TEXT NOT NULL DEFAULT 'PENDENTE_REVISAO'
    CHECK (publication_status IN ('PENDENTE_REVISAO', 'PUBLICAVEL')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (question_id, position)
);
CREATE INDEX idx_question_figures_q ON question_figures(question_id, position);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'questions' AND column_name = 'has_figure'
  ) THEN
    -- Se a coluna ainda não existir (não esperado após V1), adiciona.
    ALTER TABLE questions ADD COLUMN has_figure BOOLEAN NOT NULL DEFAULT FALSE;
  END IF;
END $$;
