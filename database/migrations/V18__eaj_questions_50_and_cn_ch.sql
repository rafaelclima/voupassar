-- V18__eaj_questions_50_and_cn_ch.sql — TASK C.2 (Programa EAJ, FASE C — trava D e E).
-- Questões oficiais até 50 + disciplinas CN/CH (SEM taxonomia inventada).
--
-- O que muda:
-- 1. questions.source_question_number (oficiais): 1–40 (V1) → 1–50.
--    A constraint original é TABLE-level sem nome explícito (auto-gerada
--    `questions_check2` — verificado em PG16 a partir da V1; V12/V13 não a
--    tocam). Derrubada com IF EXISTS + verificação fail-high de resíduo
--    abaixo; a nova chama-se chk_questions_official_number_range.
-- 2. Seed disciplines `CIENCIAS_NATUREZA` / `CIENCIAS_HUMANAS`
--    (ON CONFLICT DO NOTHING, padrão da V2). Exigidas como FK pela
--    classificação D.1 e pela importação D.3 do EAJ-2021 (CN 31–42, CH 43–50).
--
-- CHECK condicional DOCUMENTADO (não trigger — ver §5):
-- 3. IFRN segue 40/edição POR DADOS (6 edições 2020–2026, todas 40Q:
--    LP 01–20 + MAT 21–40). O teto 40 do IFRN permanece como REGRA DE
--    APLICAÇÃO no importador IFRN (scripts/db/import_questions.py,
--    load_sources: exige números 1–40 nas 3 fontes e disciplinas só
--    LP/MAT) — IFRN Q41 é rejeitada lá, antes do banco. O CHECK 1–50
--    aceitaria, mas o importador nunca emite; decisão registrada aqui
--    (critério C.2, alternativa "rejeitada por regra de aplicação").
-- 4. EAJ-2021 usa 41–50 POR DADOS (inventário A.2;
--    data/extracted/eaj/2021.json: LP 01–15, MAT 16–30, CN 31–42, CH 43–50).
--    A faixa 41–50 é habilitada por este CHECK. EAJ-2022/2025 têm 40Q
--    por dados (LP 01–20, MAT 21–40); nenhuma restrição por edição no
--    banco. Edições EAJ entram só na C.3.
-- 5. Por que CHECK único 1–50 em vez de trigger por institution: questions
--    não carrega institution (só exam_id → exams.institution); regra
--    condicional em trigger acoplaria DDL a join inter-tabela e congelaria
--    contagens por edição no banco, violando TASKS.md regra 4 ("estrutura
--    de cada edição vale só para ela") para edições futuras. O limite por
--    edição pertence aos dados da edição (capa) + importador (IFRN: 2.3;
--    EAJ: D.3), nunca a trigger.
--
-- Taxonomia CN/CH (nada inventado antes da evidência):
-- 6. NENHUM topic/subtopic CN/CH é criado aqui. Os códigos nascem SOMENTE
--    da classificação D.1 (20 questões CN/CH do EAJ-2021) e o
--    validate_classification.py é estendido na D.1. LP/MAT reutilizam a
--    taxonomia v1.1 da V2, intacta.
--
-- Notas de escopo:
-- 7. Sem explanation no banco (regra vigente do programa; coluna removida
--    na V11) — nada a fazer nesta migração.
-- 8. Forward-only (padrão do projeto: V3–V17 sem down); rollback = restore
--    de backup. NUNCA edita V1–V17 (checksums Flyway).
--
-- ---- 1. CHECK 1–40 → 1–50 (oficiais) ----
ALTER TABLE questions DROP CONSTRAINT IF EXISTS questions_check2;

-- Fail-high: se qualquer CHECK residual ainda limitar a 40, travar em vez
-- de seguir num estado meio-aplicado (novo CHECK usa 50, nunca casa aqui).
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conrelid = 'questions'::regclass
      AND pg_get_constraintdef(oid) LIKE '%source_question_number <= 40%'
  ) THEN
    RAISE EXCEPTION 'V18: CHECK 1-40 residual em questions (esperado questions_check2 removido)';
  END IF;
END
$$;

ALTER TABLE questions ADD CONSTRAINT chk_questions_official_number_range
  CHECK ((source_type = 'OFFICIAL' AND exam_id IS NOT NULL AND source_year IS NOT NULL
          AND source_question_number BETWEEN 1 AND 50)
         OR (source_type <> 'OFFICIAL'));

COMMENT ON CONSTRAINT chk_questions_official_number_range ON questions IS
  'TASK C.2: oficiais 1-50 (EAJ-2021 usa 41-50: CN 31-42, CH 43-50). IFRN segue 40/edicao por dados; teto 40 aplicado no importador IFRN (scripts/db/import_questions.py), nao neste CHECK.';

-- ---- 2. disciplinas CN/CH (FK da D.1/D.3; sem tópicos — ver §6) ----
INSERT INTO disciplines (code, name) VALUES
  ('CIENCIAS_NATUREZA', 'Ciências da Natureza'),
  ('CIENCIAS_HUMANAS', 'Ciências Humanas')
ON CONFLICT (code) DO NOTHING;
