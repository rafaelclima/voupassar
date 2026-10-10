-- V22__eaj_2025_retranscricao.sql — G.2 pós-retranscrição (Programa EAJ).
-- Reconcilia o documento-fonte EAJ-2025 com a retranscrição Q22/Q39.
--
-- O que muda (uma linha, nenhum DDL):
-- 1. exam_documents OUTRO de (EAJ,2025) (`data/provas/EAJ/2025/questoes.md`):
--    sha256 `abbcf101…ff9527` → `c59416d0…b766c5`, com nota atualizada
--    (503 linhas; Q22/Q39 retranscritas do PDF p.10/p.14 e revalidadas em
--    B.1/B.2/D.1). O CADERNO (PDF `8bfaef8a…`, papel AUDITORIA) NÃO é tocado.
--
-- Por que (nada inventado, AGENTS.md §4):
-- 2. A V19 semeou o sha do `.md` pré-retranscrição (válido em 2026-10-09).
--    O commit `fe9e53f` retranscreveu Q22/Q39 no `.md` (18 linhas) a partir
--    do PDF renderizado, mudando seu sha256. O importador
--    (`scripts/db/import_questions.py`, carga EAJ) cruza o
--    `source_sha256` de `data/extracted/eaj/2025.json` e
--    `data/linked/eaj/2025.json` (ambos `c59416d0…`, manifest
--    `data/linked/eaj/manifest.json`) contra `exam_documents` e falha alto
--    quando não encontra — comportamento correto, nunca silenciado.
--    Sem esta V22, `--institution EAJ` trava antes de qualquer escrita.
-- 3. Q22/Q39 seguem `NEEDS_VISUAL_CHECK` nas fontes (auditoria em
--    `docs/blockers.md` programa EAJ); a liberação para o banco continua
--    sendo a flag `--allow-needs-visual-check` (curadoria, nunca inferência).
--    Esta migration NÃO importa questões e NÃO libera nada sozinha.
--
-- Coexistência e escopo (TASKS.md regras 1/3/4):
-- 4. Só a linha OUTRO de (EAJ,2025) é tocada (identificada por edição +
--    kind + sha antigo). 2021/2022 intactos (shas conferem com as fontes);
--    nenhuma linha IFRN é tocada.
--
-- Forward-only (padrão do projeto: V3–V21 sem down); rollback = restore de
-- backup. NUNCA edita V1–V21 (checksums Flyway). Idempotente: o UPDATE mira
-- o sha antigo (2ª execução = 0 linhas) e a trava de estado final passa nas
-- duas execuções.

-- ---- 1. reconciliação do sha (0 ou 1 linha; nunca mais) ----
UPDATE exam_documents SET
  sha256 = 'c59416d048f51479519d686e4eca9ac3ae72f9c478c9909d5a6c7b5376b766c5',
  note = 'Documento-fonte da transcrição (503 linhas, schema A.3; = source_sha256 de data/linked/eaj/2025.json e data/extracted/eaj/2025.json). TRANSCRIBED_FROM_MD; NÃO oficial. pages=1 placeholder (páginas N/A a .md). Q23 NULA transcrita; Q22/Q39 retranscritas do PDF (p.10/p.14) e revalidadas (B.1/B.2/D.1) em 2026-10-09 (commit fe9e53f) — importação com --allow-needs-visual-check.',
  updated_at = now()
WHERE kind = 'OUTRO'
  AND file_name = 'data/provas/EAJ/2025/questoes.md'
  AND sha256 = 'abbcf1015285a3f7fd33b6357220fea4315497cc753b39d67891c47f9cff9527'
  AND exam_version_id IN (
    SELECT ev.id FROM exam_versions ev JOIN exams e ON e.id = ev.exam_id
    WHERE e.institution = 'EAJ' AND e.year = 2025 AND ev.version_code = 'UNICA'
  );

-- Fail-high: estado final exato (trava parcialidade; passa na 1ª e na N-ésima).
DO $$
DECLARE
  v_outro_new INT;
  v_outro_old INT;
BEGIN
  SELECT count(*) INTO v_outro_new
  FROM exam_documents d
  JOIN exam_versions ev ON ev.id = d.exam_version_id
  JOIN exams e ON e.id = ev.exam_id
  WHERE e.institution = 'EAJ' AND e.year = 2025
    AND ev.version_code = 'UNICA'
    AND d.kind = 'OUTRO'
    AND d.file_name = 'data/provas/EAJ/2025/questoes.md'
    AND d.sha256 = 'c59416d048f51479519d686e4eca9ac3ae72f9c478c9909d5a6c7b5376b766c5';
  IF v_outro_new <> 1 THEN
    RAISE EXCEPTION 'V22: esperado 1 OUTRO (EAJ,2025) com o sha pós-retranscrição, achado %', v_outro_new;
  END IF;

  SELECT count(*) INTO v_outro_old
  FROM exam_documents
  WHERE sha256 = 'abbcf1015285a3f7fd33b6357220fea4315497cc753b39d67891c47f9cff9527';
  IF v_outro_old <> 0 THEN
    RAISE EXCEPTION 'V22: sha pré-retranscrição ainda presente (%)', v_outro_old;
  END IF;

  IF (SELECT count(*)
      FROM exam_documents d
      JOIN exam_versions ev ON ev.id = d.exam_version_id
      JOIN exams e ON e.id = ev.exam_id
      WHERE e.institution = 'EAJ') <> 6 THEN
    RAISE EXCEPTION 'V22: esperado 6 exam_documents EAJ (nenhuma linha criada/removida)';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM exam_documents d
    JOIN exam_versions ev ON ev.id = d.exam_version_id
    JOIN exams e ON e.id = ev.exam_id
    WHERE e.institution = 'EAJ' AND e.year = 2025
      AND d.kind = 'CADERNO'
      AND d.sha256 = '8bfaef8a188add78613b3dbe1a3d183b86d2a11d87172090c01a9e89986edb42'
  ) THEN
    RAISE EXCEPTION 'V22: CADERNO EAJ-2025 (auditoria) divergiu — nada além do OUTRO pode mudar';
  END IF;
END
$$;
