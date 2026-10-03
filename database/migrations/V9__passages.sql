-- V9__passages.sql — TASK 6.9 / docs/passagens-estrategia.md
-- Textos-base compartilhados (Texto N, trechos, tabelas, gráficos, imagens)
-- transcritos literalmente do caderno oficial + vínculo N:N com questões.
-- Sem gate de publicação (decisão registrada em docs/passagens-estrategia.md §1):
-- só proveniência (edição, páginas, documento-fonte) para rastreabilidade.
-- Escrita só via importador scripts/db/import_passages.py (idempotente);
-- a API lê (entidades @Immutable neste contexto).
-- Idempotente: IF NOT EXISTS + ON CONFLICT DO NOTHING no importador.

CREATE TABLE IF NOT EXISTS passages (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
  passage_key TEXT NOT NULL CHECK (passage_key <> ''),
  label TEXT NOT NULL CHECK (label <> ''),
  kind TEXT NOT NULL
    CHECK (kind IN ('TEXTO', 'TRECHO', 'TABELA', 'GRAFICO', 'IMAGEM', 'CHARGE', 'TIRINHA')),
  title TEXT NULL,
  byline TEXT NULL,
  subtitle TEXT NULL,
  intro TEXT NULL,
  content TEXT NULL,
  visual_description TEXT NULL,
  format_note TEXT NULL,
  source_note TEXT NULL,
  page_start SMALLINT NOT NULL CHECK (page_start > 0),
  page_end SMALLINT NOT NULL CHECK (page_end >= page_start),
  checksum CHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (exam_id, passage_key),
  CHECK (content IS NOT NULL OR visual_description IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_passages_exam ON passages(exam_id, passage_key);

CREATE TABLE IF NOT EXISTS question_passages (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  passage_id BIGINT NOT NULL REFERENCES passages(id) ON DELETE CASCADE,
  position SMALLINT NOT NULL DEFAULT 1 CHECK (position > 0),
  UNIQUE (question_id, passage_id)
);

CREATE INDEX IF NOT EXISTS idx_question_passages_q ON question_passages(question_id, position);
