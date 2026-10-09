-- V21__study_plans_institution.sql — TASK E.2 (Programa EAJ, FASE E).
-- Trilha por processo no roteiro de estudos.
--
-- O que muda:
-- 1. study_plans.institution TEXT NOT NULL DEFAULT 'IFRN'
--    CHECK (institution IN ('IFRN','EAJ')) — backfill explícito: roteiros
--    existentes (só trilha IFRN até a E.1) recebem 'IFRN'. A trilha EAJ nasce
--    vazia e honesta (sem plano até o aluno gerar — GET devolve
--    NO_ACTIVE_PLAN, nunca plano IFRN disfarçado).
-- 2. UNIQUE parcial (user_id) WHERE is_active → (user_id, institution)
--    WHERE is_active: um roteiro vigente por trilha. Gerar o plano EAJ não
--    desativa o IFRN (e vice-versa) — sem contaminação entre processos.
--
-- Forward-only (padrão do projeto): sem down; rollback = restore de backup.
-- NUNCA editar V1–V20 (checksums Flyway).

-- ---- 1. institution com backfill explícito ----
ALTER TABLE study_plans ADD COLUMN institution TEXT;
UPDATE study_plans SET institution = 'IFRN' WHERE institution IS NULL;
ALTER TABLE study_plans ALTER COLUMN institution SET NOT NULL;
ALTER TABLE study_plans ALTER COLUMN institution SET DEFAULT 'IFRN';
ALTER TABLE study_plans ADD CONSTRAINT study_plans_institution_check
  CHECK (institution IN ('IFRN', 'EAJ'));

-- ---- 2. um vigente por (usuário, trilha) ----
DROP INDEX IF EXISTS uq_study_plans_active;
CREATE UNIQUE INDEX uq_study_plans_active ON study_plans(user_id, institution) WHERE is_active;
