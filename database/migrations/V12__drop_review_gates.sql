-- V12__drop_review_gates.sql — remoção total do contrato de curadoria humana.
-- Decisão de produto 2026-10-06 (docs/plano-remocao-curadoria.md):
-- confiança = veredito do pipeline (checksum + gabarito + 4 alternativas +
-- páginas + question_sources). DROP dos gates humanos; adiciona carimbo
-- máquina-legível (pipeline_version / pipeline_verified_at).
-- Pré-requisito: V10 + V11 aplicadas (Flyway valida a cadeia).
-- NÃO edita V1..V11 já aplicadas (checksum do Flyway); instalação nova
-- chega ao mesmo estado final pela cadeia V1..V12.
-- Falha alto (sem IF EXISTS): estado inesperado deve travar, não passar
-- silenciosamente (AGENTS.md §4).

-- ============ 1. questions: remove gates humanos ============
DROP INDEX idx_questions_public;
ALTER TABLE questions DROP COLUMN validation_status;
ALTER TABLE questions DROP COLUMN publication_status;

-- ============ 2. question_figures: remove gate de figura ============
-- Servir figura passa a ser crédito + página, sem quarentena
-- (frontend figure.js e FigureResponse tratados na Task 2/Task 4).
ALTER TABLE question_figures DROP COLUMN publication_status;

-- ============ 3. question_classifications: remove carimbo humano ============
DROP INDEX uq_qclass_approved;
DROP INDEX idx_qclass_q;
ALTER TABLE question_classifications DROP COLUMN status;
ALTER TABLE question_classifications DROP COLUMN reviewed_by;
ALTER TABLE question_classifications DROP COLUMN reviewed_at;
CREATE INDEX idx_qclass_q ON question_classifications(question_id, taxonomy_version);

-- ============ 4. trigger: 4 alternativas para TODA objetiva ============
-- Antes: exceção para validation_status = 'PENDING' (ex. 2020 Q38 entrava
-- só após revisão). Sem gate humano, a regra volta a ser universal.
-- Estado atual confere: 240 questions x 4 options = 960 (baseline Task 0).
CREATE OR REPLACE FUNCTION enforce_four_options() RETURNS trigger AS $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT q.id, q.kind, count(o.id) AS n
      FROM questions q LEFT JOIN question_options o ON o.question_id = q.id
     WHERE q.kind = 'OBJECTIVE'
     GROUP BY q.id, q.kind
  LOOP
    IF r.n <> 4 THEN
      RAISE EXCEPTION 'questão objetiva % tem % alternativas (esperado 4)', r.id, r.n;
    END IF;
  END LOOP;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- ============ 5. carimbo do pipeline (fonte de confiança) ============
-- pipeline_version = versão do importador que produziu a linha.
-- 'importer-1.0.0' é o produtor real das 240 linhas atuais (honestidade de
-- proveniência); a Task 3 (reescrita do importador) sobe para 2.0.0 e
-- atualiza este carimbo na reimportação.
ALTER TABLE questions ADD COLUMN pipeline_version TEXT NOT NULL DEFAULT 'importer-1.0.0';
-- pipeline_verified_at = última verificação máquina (aqui: apply da V12,
-- precedido de import --check verde com 0 divergências — Task 0).
ALTER TABLE questions ADD COLUMN pipeline_verified_at TIMESTAMPTZ NOT NULL DEFAULT now();
