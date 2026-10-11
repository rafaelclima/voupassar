-- V23__ai_generations.sql — TASK A.2 (Programa Prova-IA, FASE A).
-- Modelo de dados do caderno-IA efêmero: estrutura própria, sem FK em
-- questions/question_options/question_sources (TASKS.md regras 2–3).
--
-- O que muda:
-- 1. ai_generations — uma geração por linha: dono (user_id), formato
--    (institution), edição-modelo (edition_model, só as 9 presentes em
--    docs/ai-prova-schema.md §2 — nunca inventar), modo (ESTUDO/PROVA),
--    auditoria sem PII (prompt_version, model, repair_attempts,
--    failure_reason, timestamps) e status do ciclo
--    GENERATING → READY (caderno congelado, frozen_at) | FAILED.
--    Só formato/disciplina/tópicos saem para o provedor (TASKS.md regra 4);
--    nenhum dado do aluno vive aqui além do user_id dono.
-- 2. ai_generation_questions — caderno congelado normalizado (PK
--    (generation_id, position), espelho de simulation_questions): discipline,
--    topic (TEXT validado pré-freeze pelo AiProvaValidator contra
--    AiProvaSpec::TOPICS ou DESCONHECIDO — sem FK na taxonomia e sem CHECK
--    de 21 códigos para não duplicar a fonte canônica do vocabulário),
--    stem, options_json (exatamente A–D não-vazias, guarda estrutural no
--    banco) e frozen_answer_key (só A–D; IA nunca tem anulada X).
--    Congelado por construção (sem PUT/DELETE na API, mesmo padrão de
--    simulation_questions — V5 decidiu sem trigger anti-UPDATE para
--    cadernos; a A.3 não expõe mutação).
-- 3. ai_generation_answers — respostas-IA em tabela própria (espelho de
--    question_attempts, sem tocar nele): user_id, (generation_id, position)
--    com FK composta ao caderno congelado (resposta só para posição
--    congelada), simulation_attempt_id (execução no ciclo reutilizado da
--    A.3), selected_option (A–D/BLANK), is_correct NOT NULL (confronto com
--    o congelado; sem anulada-IA), mode, answered_at. Fato imutável via
--    trigger (mesmo padrão de question_attempts); limpeza de manutenção
--    desliga o trigger (padrão G.2), nunca deleção silenciosa.
-- 4. simulations: novo type AI_GENERATED_EDITION + coluna ai_generation_id
--    (FK → ai_generations ON DELETE CASCADE: geração é por aluno; wipe do
--    usuário cascateia sem travar no RESTRICT). CHECKs antigas
--    (simulations_type_check, simulations_check — nomes confirmados no
--    banco) substituídas por chk_simulations_type + chk_simulations_link:
--    IA exige ai_generation_id + exam_id NULL + owner NOT NULL;
--    BY_DISCIPLINE/REAL_EDITION exigem ai_generation_id NULL (linhas
--    existentes passam intactas). filter_json da IA carrega
--    {type, institution, editionModel, mode, promptVersion, model,
--    experimental: true} — convenção escrita pela A.3, não DDL.
-- 5. Concorrência/cota (base da C.2): UNIQUE parcial uq_ai_generations_busy
--    (user_id) WHERE GENERATING = 1 geração em voo por aluno no banco;
--    janela de 3/dia consulta (user_id, created_at) via
--    idx_ai_generations_user.
--
-- Por que sem FK em questions (AGENTS.md §13, TASKS.md regra 3):
-- diagnóstico, roteiro, revisão, performance, estatísticas e content-map
-- leem questions/question_attempts; sem caminho FK entre ai_* e o acervo,
-- contaminação exige leitura explícita das tabelas-IA (travada e testada
-- na B.1). Prova estrutural em
-- backend/src/test/java/br/com/voupassar/aiprova/AiProvaIsolationTest.java.
--
-- Forward-only (padrão do projeto: V3–V22 sem down); rollback = restore de
-- backup (docs/deploy-vps.md). NUNCA edita V1–V22 (checksums Flyway).

-- ---- 1. gerações (cabeçalho + auditoria sem PII) ----
CREATE TABLE ai_generations (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  institution TEXT NOT NULL CHECK (institution IN ('IFRN', 'EAJ')),
  edition_model TEXT NOT NULL CHECK (edition_model IN
    ('IFRN-2020', 'IFRN-2022', 'IFRN-2023', 'IFRN-2024', 'IFRN-2025',
     'IFRN-2026', 'EAJ-2021', 'EAJ-2022', 'EAJ-2025')),
  mode TEXT NOT NULL CHECK (mode IN ('ESTUDO', 'PROVA')),
  prompt_version TEXT NOT NULL CHECK (prompt_version <> ''),
  model TEXT NOT NULL CHECK (model <> ''),
  status TEXT NOT NULL DEFAULT 'GENERATING'
    CHECK (status IN ('GENERATING', 'READY', 'FAILED')),
  repair_attempts INT NOT NULL DEFAULT 0 CHECK (repair_attempts >= 0),
  failure_reason TEXT NULL,
  frozen_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((status = 'READY' AND frozen_at IS NOT NULL)
      OR (status <> 'READY' AND frozen_at IS NULL))
);
CREATE INDEX idx_ai_generations_user ON ai_generations(user_id, created_at DESC);
-- 1 geração em voo por aluno (C.2: 2ª concorrente → 409 no serviço).
CREATE UNIQUE INDEX uq_ai_generations_busy ON ai_generations(user_id)
  WHERE status = 'GENERATING';

-- ---- 2. caderno congelado (linhas filhas, sem dualidade com JSON) ----
CREATE TABLE ai_generation_questions (
  generation_id BIGINT NOT NULL REFERENCES ai_generations(id) ON DELETE CASCADE,
  position SMALLINT NOT NULL CHECK (position > 0),
  discipline TEXT NOT NULL CHECK (discipline IN
    ('LINGUA_PORTUGUESA', 'MATEMATICA', 'CIENCIAS_NATUREZA', 'CIENCIAS_HUMANAS')),
  topic TEXT NOT NULL CHECK (topic <> ''),
  stem TEXT NOT NULL CHECK (stem <> ''),
  options_json JSONB NOT NULL CHECK (
    options_json ?& array['A', 'B', 'C', 'D']
    AND (options_json - 'A' - 'B' - 'C' - 'D') = '{}'::jsonb
    AND btrim(options_json ->> 'A') <> ''
    AND btrim(options_json ->> 'B') <> ''
    AND btrim(options_json ->> 'C') <> ''
    AND btrim(options_json ->> 'D') <> ''),
  frozen_answer_key CHAR(1) NOT NULL CHECK (frozen_answer_key IN ('A', 'B', 'C', 'D')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (generation_id, position)
);

-- ---- 3. respostas-IA (fato próprio, nunca question_attempts) ----
CREATE TABLE ai_generation_answers (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  generation_id BIGINT NOT NULL,
  position SMALLINT NOT NULL CHECK (position > 0),
  simulation_attempt_id BIGINT NOT NULL REFERENCES simulation_attempts(id) ON DELETE CASCADE,
  selected_option CHAR(1) NOT NULL CHECK (selected_option IN ('A', 'B', 'C', 'D', 'BLANK')),
  is_correct BOOLEAN NOT NULL,
  time_spent_seconds INT NULL CHECK (time_spent_seconds IS NULL OR time_spent_seconds >= 0),
  mode TEXT NOT NULL CHECK (mode IN ('ESTUDO', 'PROVA')),
  answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  FOREIGN KEY (generation_id, position)
    REFERENCES ai_generation_questions(generation_id, position) ON DELETE CASCADE
);
CREATE INDEX idx_ai_answers_user ON ai_generation_answers(user_id, generation_id, answered_at DESC);
CREATE INDEX idx_ai_answers_attempt ON ai_generation_answers(simulation_attempt_id, position, answered_at DESC);

-- Fato imutável (espelho de prevent_attempt_mutation/V1): correção só via
-- nova resposta. Limpeza de manutenção desliga o trigger (padrão G.2).
CREATE OR REPLACE FUNCTION prevent_ai_answer_mutation() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'ai_generation_answers é imutável (correção só via nova resposta)';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ai_answers_immutable
  BEFORE UPDATE OR DELETE ON ai_generation_answers
  FOR EACH ROW EXECUTE FUNCTION prevent_ai_answer_mutation();

-- updated_at nas 3 tabelas (padrão V1/V5).
CREATE TRIGGER trg_ai_generations_updated
  BEFORE UPDATE ON ai_generations FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_ai_generation_questions_updated
  BEFORE UPDATE ON ai_generation_questions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_ai_generation_answers_updated
  BEFORE UPDATE ON ai_generation_answers FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---- 4. vínculo simulation ↔ ai_generation ----
ALTER TABLE simulations
  ADD COLUMN ai_generation_id BIGINT NULL REFERENCES ai_generations(id) ON DELETE CASCADE;
ALTER TABLE simulations DROP CONSTRAINT IF EXISTS simulations_type_check;
ALTER TABLE simulations DROP CONSTRAINT IF EXISTS simulations_check;
ALTER TABLE simulations ADD CONSTRAINT chk_simulations_type
  CHECK (type IN ('BY_DISCIPLINE', 'REAL_EDITION', 'AI_GENERATED_EDITION'));
ALTER TABLE simulations ADD CONSTRAINT chk_simulations_link
  CHECK (
    (type = 'REAL_EDITION' AND exam_id IS NOT NULL AND ai_generation_id IS NULL)
    OR (type = 'BY_DISCIPLINE' AND ai_generation_id IS NULL)
    OR (type = 'AI_GENERATED_EDITION' AND ai_generation_id IS NOT NULL
        AND exam_id IS NULL AND owner_user_id IS NOT NULL)
  );

-- ---- 5. travas de estado final (fail-high; passam na 1ª e na N-ésima) ----
DO $$
DECLARE
  v_old INT;
  v_fk INT;
BEGIN
  IF to_regclass('public.ai_generations') IS NULL THEN
    RAISE EXCEPTION 'V23: ai_generations ausente';
  END IF;
  IF to_regclass('public.ai_generation_questions') IS NULL THEN
    RAISE EXCEPTION 'V23: ai_generation_questions ausente';
  END IF;
  IF to_regclass('public.ai_generation_answers') IS NULL THEN
    RAISE EXCEPTION 'V23: ai_generation_answers ausente';
  END IF;

  -- Nenhum CHECK antigo de simulations sem o tipo-IA pode restar.
  SELECT count(*) INTO v_old FROM pg_constraint
   WHERE conrelid = 'simulations'::regclass AND contype = 'c'
     AND pg_get_constraintdef(oid) LIKE '%BY_DISCIPLINE%'
     AND pg_get_constraintdef(oid) NOT LIKE '%AI_GENERATED_EDITION%';
  IF v_old <> 0 THEN
    RAISE EXCEPTION 'V23: CHECK antiga de simulations sem AI_GENERATED_EDITION ainda presente (%)', v_old;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint
      WHERE conname = 'chk_simulations_type' AND conrelid = 'simulations'::regclass) THEN
    RAISE EXCEPTION 'V23: chk_simulations_type ausente';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_constraint
      WHERE conname = 'chk_simulations_link' AND conrelid = 'simulations'::regclass) THEN
    RAISE EXCEPTION 'V23: chk_simulations_link ausente';
  END IF;

  -- Vínculo simulation → geração existe e mira ai_generations.
  SELECT count(*) INTO v_fk FROM pg_constraint
   WHERE conrelid = 'simulations'::regclass AND contype = 'f'
     AND confrelid = 'ai_generations'::regclass;
  IF v_fk <> 1 THEN
    RAISE EXCEPTION 'V23: esperado 1 FK simulations → ai_generations, achado %', v_fk;
  END IF;

  -- Isolamento: nenhuma FK das tabelas-IA para o acervo oficial.
  SELECT count(*) INTO v_fk FROM pg_constraint c
   JOIN pg_class src ON src.oid = c.conrelid
   JOIN pg_class dst ON dst.oid = c.confrelid
   WHERE c.contype = 'f' AND src.relname LIKE 'ai\_generation%'
     AND dst.relname IN ('questions', 'question_options', 'question_sources');
  IF v_fk <> 0 THEN
    RAISE EXCEPTION 'V23: isolamento violado — % FK(s) de ai_* para o acervo oficial', v_fk;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_ai_answers_immutable') THEN
    RAISE EXCEPTION 'V23: trg_ai_answers_immutable ausente';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = 'uq_ai_generations_busy') THEN
    RAISE EXCEPTION 'V23: uq_ai_generations_busy ausente';
  END IF;
END
$$;
