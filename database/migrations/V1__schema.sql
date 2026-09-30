-- V1__schema.sql — TASK 2.2
-- Implementa docs/database-erd.md §§0–5 (PostgreSQL 16).
-- Convenções: PK surrogate BIGINT, enums TEXT+CHECK, created_at/updated_at,
-- chaves naturais em UNIQUE (idempotência da TASK 2.3).
-- Compatível com Flyway (reuso direto no Spring Boot da TASK 3.1).

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============ helpers ============
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Imutabilidade do fato de resposta (ERD §5 item 5): sem UPDATE/DELETE.
CREATE OR REPLACE FUNCTION prevent_attempt_mutation() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'question_attempts é imutável (correção só via nova tentativa)';
END;
$$ LANGUAGE plpgsql;

-- Objetivas oficiais: exatamente 4 alternativas (ERD §5 item 4).
-- Constraint trigger DEFERRED: permite inserts linha a linha na mesma transação,
-- valida no COMMIT. Itens PENDING (ex. 2020 Q38) entram só após revisão.
CREATE OR REPLACE FUNCTION enforce_four_options() RETURNS trigger AS $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT q.id, q.kind, count(o.id) AS n
      FROM questions q LEFT JOIN question_options o ON o.question_id = q.id
     WHERE q.kind = 'OBJECTIVE'
       AND q.validation_status <> 'PENDING'
     GROUP BY q.id, q.kind
  LOOP
    IF r.n <> 4 THEN
      RAISE EXCEPTION 'questão objetiva % tem % alternativas (esperado 4)', r.id, r.n;
    END IF;
  END LOOP;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Coerência subtopic→topic em classificações e agregados.
CREATE OR REPLACE FUNCTION check_subtopic_topic() RETURNS trigger AS $$
DECLARE
  t BIGINT;
BEGIN
  IF NEW.subtopic_id IS NOT NULL THEN
    SELECT topic_id INTO t FROM subtopics WHERE id = NEW.subtopic_id;
    IF NOT FOUND THEN
      RAISE EXCEPTION 'subtopic_id % inexistente', NEW.subtopic_id;
    END IF;
    IF NEW.topic_id IS NULL THEN
      NEW.topic_id := t;
    ELSIF NEW.topic_id <> t THEN
      RAISE EXCEPTION 'subtópico % não pertence ao tópico %', NEW.subtopic_id, NEW.topic_id;
    END IF;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============ identidade / auth (AGENTS.md §14) ============
CREATE TABLE roles (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE CHECK (code IN ('STUDENT','CURATOR','ADMIN')),
  description TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email CITEXT NOT NULL UNIQUE CHECK (email ~ '^[^@]+@[^@]+$'),
  password_hash TEXT NOT NULL CHECK (password_hash <> ''),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  credential_version INT NOT NULL DEFAULT 1 CHECK (credential_version >= 1),
  last_login_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ NULL,
  replaced_by_id BIGINT NULL REFERENCES refresh_tokens(id) ON DELETE SET NULL,
  created_ip TEXT NULL,
  user_agent TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (revoked_at IS NULL OR revoked_at >= created_at)
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id, expires_at)
  WHERE revoked_at IS NULL;

CREATE TABLE student_profiles (
  user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  display_name TEXT NOT NULL CHECK (display_name <> ''),
  school_year TEXT NULL,
  target_year SMALLINT NULL CHECK (target_year IS NULL OR target_year BETWEEN 2000 AND 2100),
  study_goal TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ taxonomia controlada (v1.1, emerge das provas) ============
CREATE TABLE disciplines (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE topics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  discipline_id BIGINT NOT NULL REFERENCES disciplines(id) ON DELETE RESTRICT,
  code TEXT NOT NULL,
  name TEXT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (discipline_id, code)
);

CREATE TABLE subtopics (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE RESTRICT,
  code TEXT NOT NULL,
  name TEXT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (topic_id, code)
);

-- ============ provas e documentos (Fase 1) ============
CREATE TABLE exams (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  year SMALLINT NOT NULL UNIQUE CHECK (year IN (2020,2022,2023,2024,2025,2026)),
  edital TEXT NOT NULL,
  duration_minutes SMALLINT NOT NULL DEFAULT 240 CHECK (duration_minutes > 0),
  objective_count SMALLINT NOT NULL DEFAULT 40 CHECK (objective_count > 0),
  lp_count SMALLINT NOT NULL DEFAULT 20 CHECK (lp_count >= 0),
  mat_count SMALLINT NOT NULL DEFAULT 20 CHECK (mat_count >= 0),
  has_essay BOOLEAN NOT NULL DEFAULT TRUE,
  scoring_rule TEXT NULL, -- DESCONHECIDA (an uladas + discursiva); NULL até fonte oficial
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exam_versions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
  version_code TEXT NOT NULL,
  published_at DATE NULL,
  note TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (exam_id, version_code)
);

CREATE TABLE exam_documents (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  exam_version_id BIGINT NOT NULL REFERENCES exam_versions(id) ON DELETE CASCADE,
  kind TEXT NOT NULL CHECK (kind IN
    ('CADERNO','GABARITO_PRELIMINAR','GABARITO_DEFINITIVO','GABARITO_FINAL','OFERTAS','OUTRO')),
  file_name TEXT NOT NULL,
  sha256 CHAR(64) NOT NULL UNIQUE,
  pages SMALLINT NOT NULL CHECK (pages > 0),
  generator TEXT NULL,
  note TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exam_essay_prompts (
  exam_id BIGINT PRIMARY KEY REFERENCES exams(id) ON DELETE CASCADE,
  genre TEXT NOT NULL,
  theme TEXT NOT NULL,
  pseudonym TEXT NOT NULL,
  proposal_excerpt TEXT NOT NULL,
  criteria_text TEXT NULL,
  page SMALLINT NOT NULL CHECK (page > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ questões e evidências ============
CREATE TABLE questions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  source_type TEXT NOT NULL CHECK (source_type IN
    ('OFFICIAL','AUTHORAL','ADAPTED','INTERNAL_REVIEW','EXPERIMENTAL')),
  exam_id BIGINT NULL REFERENCES exams(id) ON DELETE RESTRICT,
  exam_document_id BIGINT NULL REFERENCES exam_documents(id) ON DELETE RESTRICT,
  source_year SMALLINT NULL,
  source_question_number SMALLINT NULL,
  statement TEXT NOT NULL CHECK (statement <> ''),
  kind TEXT NOT NULL DEFAULT 'OBJECTIVE' CHECK (kind IN ('OBJECTIVE','DISCURSIVE')),
  discipline_id BIGINT NOT NULL REFERENCES disciplines(id) ON DELETE RESTRICT,
  page_start SMALLINT NOT NULL CHECK (page_start > 0),
  page_end SMALLINT NOT NULL,
  answer_key CHAR(1) NOT NULL CHECK (answer_key IN ('A','B','C','D','X')),
  annulled BOOLEAN NOT NULL DEFAULT FALSE,
  checksum CHAR(64) NOT NULL UNIQUE,
  has_figure BOOLEAN NOT NULL DEFAULT FALSE,
  difficulty_estimate TEXT NULL CHECK (difficulty_estimate IS NULL OR difficulty_estimate IN ('FACIL','MEDIA','DIFICIL')),
  explanation TEXT NULL,
  validation_status TEXT NOT NULL DEFAULT 'PENDING'
    CHECK (validation_status IN ('PENDING','REVIEWED','APPROVED','REJECTED')),
  publication_status TEXT NOT NULL DEFAULT 'PENDENTE_REVISAO'
    CHECK (publication_status IN ('PUBLICAVEL','NAO_PUBLICAVEL','PENDENTE_REVISAO','SOMENTE_REFERENCIA')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (page_end >= page_start),
  CHECK ((annulled AND answer_key = 'X') OR (NOT annulled AND answer_key <> 'X')),
  CHECK ((source_type = 'OFFICIAL' AND exam_id IS NOT NULL AND source_year IS NOT NULL
          AND source_question_number BETWEEN 1 AND 40)
         OR (source_type <> 'OFFICIAL')),
  UNIQUE (source_type, source_year, source_question_number, exam_document_id)
);

CREATE TABLE question_options (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  label CHAR(1) NOT NULL CHECK (label IN ('A','B','C','D')),
  option_text TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (question_id, label)
);
CREATE INDEX idx_question_options_q ON question_options(question_id);

CREATE TABLE question_sources (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  exam_document_id BIGINT NOT NULL REFERENCES exam_documents(id) ON DELETE RESTRICT,
  role TEXT NOT NULL CHECK (role IN ('PRIMARY','GABARITO','OFERTAS','COMPLEMENTAR')),
  page_start SMALLINT NULL CHECK (page_start IS NULL OR page_start > 0),
  page_end SMALLINT NULL,
  doc_sha256 CHAR(64) NOT NULL,
  note TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (page_end IS NULL OR page_start IS NULL OR page_end >= page_start)
);
CREATE INDEX idx_question_sources_q ON question_sources(question_id);

CREATE TABLE question_tags (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  description TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE question_tag_map (
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  tag_id BIGINT NOT NULL REFERENCES question_tags(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (question_id, tag_id)
);

CREATE TABLE question_classifications (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  taxonomy_version TEXT NOT NULL,
  topic_id BIGINT NULL REFERENCES topics(id) ON DELETE RESTRICT,
  subtopic_id BIGINT NULL REFERENCES subtopics(id) ON DELETE RESTRICT,
  skill TEXT NULL,
  reasoning_type TEXT NULL,
  confidence TEXT NOT NULL CHECK (confidence IN ('ALTA','MEDIA','BAIXA')),
  evidence TEXT NOT NULL CHECK (evidence <> ''),
  origin TEXT NOT NULL DEFAULT 'CLASSIFICACAO_DERIVADA_FONTE',
  status TEXT NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','REVIEWED','APPROVED','REJECTED')),
  reviewed_by BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
  reviewed_at TIMESTAMPTZ NULL,
  observation TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_qclass_q ON question_classifications(question_id, status, taxonomy_version);
CREATE INDEX idx_qclass_topic ON question_classifications(topic_id, subtopic_id);
-- vigente única: só uma APPROVED por questão (ERD §5 item 8)
CREATE UNIQUE INDEX uq_qclass_approved ON question_classifications(question_id)
  WHERE status = 'APPROVED';

-- ============ sessões, tentativas, simulados ============
CREATE TABLE study_sessions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  mode TEXT NOT NULL CHECK (mode IN ('ESTUDO','PROVA','REVISAO')),
  status TEXT NOT NULL DEFAULT 'IN_PROGRESS'
    CHECK (status IN ('IN_PROGRESS','FINISHED','ABANDONED')),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (finished_at IS NULL OR finished_at >= started_at)
);
CREATE INDEX idx_sessions_user ON study_sessions(user_id, started_at DESC);

CREATE TABLE simulations (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
  type TEXT NOT NULL CHECK (type IN ('BY_DISCIPLINE','REAL_EDITION')),
  exam_id BIGINT NULL REFERENCES exams(id) ON DELETE RESTRICT,
  filter_json JSONB NOT NULL DEFAULT '{}',
  title TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((type = 'REAL_EDITION' AND exam_id IS NOT NULL) OR (type = 'BY_DISCIPLINE'))
);

CREATE TABLE simulation_attempts (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  simulation_id BIGINT NOT NULL REFERENCES simulations(id) ON DELETE RESTRICT,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  mode TEXT NOT NULL CHECK (mode IN ('ESTUDO','PROVA')),
  status TEXT NOT NULL DEFAULT 'IN_PROGRESS'
    CHECK (status IN ('IN_PROGRESS','SUBMITTED','ABANDONED')),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  submitted_at TIMESTAMPTZ NULL,
  score_json JSONB NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (submitted_at IS NULL OR submitted_at >= started_at)
);
CREATE INDEX idx_sim_attempts_user ON simulation_attempts(user_id, started_at DESC);

CREATE TABLE simulation_questions (
  simulation_attempt_id BIGINT NOT NULL REFERENCES simulation_attempts(id) ON DELETE CASCADE,
  position SMALLINT NOT NULL CHECK (position > 0),
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE RESTRICT,
  frozen_answer_key CHAR(1) NOT NULL CHECK (frozen_answer_key IN ('A','B','C','D','X')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (simulation_attempt_id, position)
);

CREATE TABLE question_attempts (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE RESTRICT,
  study_session_id BIGINT NULL REFERENCES study_sessions(id) ON DELETE SET NULL,
  simulation_attempt_id BIGINT NULL REFERENCES simulation_attempts(id) ON DELETE SET NULL,
  selected_option CHAR(1) NOT NULL CHECK (selected_option IN ('A','B','C','D','BLANK')),
  is_correct BOOLEAN NULL,
  was_annulled BOOLEAN NOT NULL DEFAULT FALSE,
  time_spent_seconds INT NULL CHECK (time_spent_seconds IS NULL OR time_spent_seconds >= 0),
  mode TEXT NOT NULL CHECK (mode IN ('ESTUDO','PROVA','REVISAO')),
  answered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (study_session_id IS NOT NULL OR simulation_attempt_id IS NOT NULL),
  CHECK ((NOT was_annulled AND is_correct IS NOT NULL)
         OR (was_annulled AND is_correct IS NULL))
);
CREATE INDEX idx_attempts_user_q ON question_attempts(user_id, question_id, answered_at DESC);
CREATE INDEX idx_attempts_user_mode ON question_attempts(user_id, mode, answered_at DESC);
CREATE INDEX idx_attempts_question ON question_attempts(question_id);

-- ============ roteiro de estudos ============
CREATE TABLE study_plans (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  algorithm_version TEXT NOT NULL,
  generated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_study_plans_active ON study_plans(user_id) WHERE is_active;

CREATE TABLE study_plan_items (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  study_plan_id BIGINT NOT NULL REFERENCES study_plans(id) ON DELETE CASCADE,
  topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE RESTRICT,
  subtopic_id BIGINT NULL REFERENCES subtopics(id) ON DELETE RESTRICT,
  priority SMALLINT NOT NULL CHECK (priority BETWEEN 1 AND 5),
  reason TEXT NOT NULL,
  evidence_json JSONB NOT NULL CHECK (evidence_json <> '{}'::jsonb),
  status TEXT NOT NULL DEFAULT 'TODO'
    CHECK (status IN ('TODO','DOING','DONE','SKIPPED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_plan_items ON study_plan_items(study_plan_id, priority);

-- ============ agregados, gamificação, snapshots ============
CREATE TABLE student_topic_performance (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE RESTRICT,
  subtopic_id BIGINT NULL REFERENCES subtopics(id) ON DELETE RESTRICT,
  attempts INT NOT NULL DEFAULT 0 CHECK (attempts >= 0),
  hits INT NOT NULL DEFAULT 0 CHECK (hits >= 0),
  accuracy NUMERIC(5,4) GENERATED ALWAYS AS
    (CASE WHEN attempts = 0 THEN NULL ELSE hits::NUMERIC / attempts END) STORED,
  last_attempt_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (hits <= attempts),
  UNIQUE NULLS NOT DISTINCT (user_id, topic_id, subtopic_id)
);
CREATE INDEX idx_perf_user_acc ON student_topic_performance(user_id, accuracy);

CREATE TABLE achievements (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  description TEXT NULL,
  rule_json JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE student_achievements (
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  achievement_id BIGINT NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
  awarded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, achievement_id)
);

CREATE TABLE progress_snapshots (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  taken_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  metrics_json JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, taken_at)
);

-- ============ índices de busca pública (ERD §4) ============
CREATE INDEX idx_questions_disc ON questions(discipline_id, id);
CREATE INDEX idx_questions_exam ON questions(exam_id, source_question_number);
CREATE INDEX idx_questions_public ON questions(source_type, publication_status)
  WHERE publication_status = 'PUBLICAVEL';
CREATE INDEX idx_questions_trgm ON questions USING gin (statement gin_trgm_ops);

-- ============ triggers ============
-- updated_at em todas as tabelas com a coluna
DO $$
DECLARE t TEXT;
BEGIN
  FOREACH t IN ARRAY ARRAY[
    'roles','users','user_roles','refresh_tokens','student_profiles',
    'disciplines','topics','subtopics','exams','exam_versions','exam_documents',
    'exam_essay_prompts','questions','question_options','question_sources',
    'question_tags','question_tag_map','question_classifications',
    'study_sessions','simulations','simulation_attempts','simulation_questions',
    'question_attempts','study_plans','study_plan_items',
    'student_topic_performance','achievements','student_achievements','progress_snapshots']
  LOOP
    EXECUTE format(
      'CREATE TRIGGER trg_%I_updated BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION set_updated_at()', t, t);
  END LOOP;
END $$;

CREATE TRIGGER trg_attempts_immutable
  BEFORE UPDATE OR DELETE ON question_attempts
  FOR EACH ROW EXECUTE FUNCTION prevent_attempt_mutation();

CREATE CONSTRAINT TRIGGER trg_four_options
  AFTER INSERT OR DELETE ON question_options
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION enforce_four_options();

CREATE TRIGGER trg_qclass_coherence
  BEFORE INSERT OR UPDATE ON question_classifications
  FOR EACH ROW EXECUTE FUNCTION check_subtopic_topic();

CREATE TRIGGER trg_perf_coherence
  BEFORE INSERT OR UPDATE ON student_topic_performance
  FOR EACH ROW EXECUTE FUNCTION check_subtopic_topic();

CREATE TRIGGER trg_plan_item_coherence
  BEFORE INSERT OR UPDATE ON study_plan_items
  FOR EACH ROW EXECUTE FUNCTION check_subtopic_topic();
