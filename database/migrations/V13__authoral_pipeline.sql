-- V13__authoral_pipeline.sql — Fase 15: pipeline das questões autorais.
-- Decisão de produto 2026-10-06 (TASKS.md Fase 15): expandir o banco com
-- questões não oficiais derivadas do perfil observado nas 6 edições, sem
-- fila de curadoria humana — o gate é o veredito do pipeline (validadores
-- determinísticos + checksum). Autoral consome evidência oficial, nunca a
-- produz. Pré-requisito: V12 aplicada. NÃO edita V1..V12 (checksum Flyway).
-- Falha alto (sem IF EXISTS): estado inesperado deve travar (AGENTS.md §4).
--
-- Contrato de dados da autoral (lido por scripts/db/import_authoral.py):
--   source_type = 'AUTHORAL',
--   exam_id / exam_document_id / source_year / source_question_number NULL
--     (sem edição-fonte; o CHECK da V1 já permite),
--   page_start / page_end NULL (sem documento-fonte; NULL = DESCONHECIDO,
--     nunca 1/1 inventado),
--   classifications: taxonomy_version = 'v1.1',
--     origin = 'AUTORAL_DERIVADA_PERFIL'.
-- Sem linhas em question_sources (exige exam_document_id NOT NULL: só faz
-- sentido para fontes oficiais); a proveniência da autoral vive em
-- classifications.observation (spec + refs oficiais) + questions.checksum +
-- questions.pipeline_version.

-- ============ 1. páginas NULL quando não há documento-fonte ============
ALTER TABLE questions ALTER COLUMN page_start DROP NOT NULL;
ALTER TABLE questions ALTER COLUMN page_end DROP NOT NULL;

-- ============ 2. contrato oficial × autoral ============
-- Oficiais seguem exigindo páginas (as 240 têm; importador 2.3 exige).
ALTER TABLE questions ADD CONSTRAINT chk_questions_official_pages
  CHECK (source_type <> 'OFFICIAL'
         OR (page_start IS NOT NULL AND page_end IS NOT NULL));

-- Autorais não carregam nenhum vínculo de edição (rastreabilidade honesta).
ALTER TABLE questions ADD CONSTRAINT chk_questions_authoral_nulls
  CHECK (source_type <> 'AUTHORAL'
         OR (exam_id IS NULL AND exam_document_id IS NULL
             AND source_year IS NULL AND source_question_number IS NULL
             AND page_start IS NULL AND page_end IS NULL));
