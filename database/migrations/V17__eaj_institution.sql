-- V17__eaj_institution.sql — TASK C.1 (Programa EAJ, FASE C — trava D e E).
-- Dimensão institution em exams + contagens por área CN/CH.
--
-- O que muda:
-- 1. exams.institution TEXT NOT NULL DEFAULT 'IFRN'
--    CHECK (institution IN ('IFRN','EAJ')) — backfill explícito: as 6 edições
--    IFRN existentes recebem 'IFRN' (UPDATE abaixo). EAJ entra só na C.3.
-- 2. UNIQUE(year) → UNIQUE(institution, year): ano sozinho nunca identifica a
--    edição (EAJ-2022 ≠ IFRN-2022, EAJ-2025 ≠ IFRN-2025; TASKS.md regra 3).
--    A constraint original (nome auto-gerado exams_year_key pelo UNIQUE
--    inline da V1) é derrubada; a nova chama-se uq_exams_institution_year.
-- 3. CHECK de anos ampliado para incluir 2021: 2021 só faz sentido com
--    institution = 'EAJ' (EAJ-2021, 50Q/4 áreas — inventário A.2).
--    IFRN-2021 segue AUSENTE: nenhuma linha (IFRN,2021) é inserida aqui nem
--    na C.3 (AGENTS.md §3; 404 de IFRN-2021 preservado até a E.1, que passa
--    a endereçar edições por (institution, year)).
-- 4. Novas colunas cn_count/ch_count SMALLINT NOT NULL DEFAULT 0
--    (contagens por área de Ciências da Natureza/Humanas daquela edição;
--    0 nas 6 edições IFRN — só LP/MAT até a C.2 semear CN/CH).
-- 5. duration_minutes e has_essay JÁ variam por edição desde a V1 (colunas
--    por linha, sem DDL nesta migration): as linhas EAJ da C.3 usam 180 e
--    FALSE, valores confirmados na A.2 (capa: "três horas", sem discursiva).
--
-- Forward-only (padrão do projeto: V3–V16 sem down): sem migração de
-- reversão; rollback = restore de backup (docs/deploy-vps.md).
--
-- Nota de ordem: a V2 usa ON CONFLICT (year) DO NOTHING — válida porque em
-- builds limpos a V2 roda ANTES da V17 (UNIQUE(year) ainda existe). Reexecução
-- manual do seed APÓS a V17 deve usar ON CONFLICT (institution, year).

-- ---- 1. institution com backfill explícito ----
ALTER TABLE exams ADD COLUMN institution TEXT;
UPDATE exams SET institution = 'IFRN' WHERE institution IS NULL;
ALTER TABLE exams ALTER COLUMN institution SET NOT NULL;
ALTER TABLE exams ALTER COLUMN institution SET DEFAULT 'IFRN';
ALTER TABLE exams ADD CONSTRAINT exams_institution_check
  CHECK (institution IN ('IFRN', 'EAJ'));

-- ---- 2. UNIQUE(year) → UNIQUE(institution, year) ----
ALTER TABLE exams DROP CONSTRAINT IF EXISTS exams_year_key;
ALTER TABLE exams ADD CONSTRAINT uq_exams_institution_year
  UNIQUE (institution, year);

-- ---- 3. anos: inclui 2021 (só faz sentido com EAJ; ver cabeçalho) ----
ALTER TABLE exams DROP CONSTRAINT IF EXISTS exams_year_check;
ALTER TABLE exams ADD CONSTRAINT exams_year_check
  CHECK (year IN (2020, 2021, 2022, 2023, 2024, 2025, 2026));

-- ---- 4. contagens CN/CH por edição ----
ALTER TABLE exams ADD COLUMN cn_count SMALLINT NOT NULL DEFAULT 0
  CONSTRAINT exams_cn_count_check CHECK (cn_count >= 0);
ALTER TABLE exams ADD COLUMN ch_count SMALLINT NOT NULL DEFAULT 0
  CONSTRAINT exams_ch_count_check CHECK (ch_count >= 0);
