-- V19__seed_eaj.sql — TASK C.3 (Programa EAJ, FASE C — trava D e E).
-- Seed das 3 edições EAJ + versões + documentos-fonte.
--
-- O que entra:
-- 1. exams: (EAJ,2021), (EAJ,2022), (EAJ,2025). Duração 180 e has_essay FALSE
--    por edição (capa: "no máximo, três horas"; full-scan sem seção
--    discursiva — inventário A.2 §3, vale SÓ para o EAJ observado, nunca
--    regra geral). Contagens por área POR EDIÇÃO (TASKS.md regra 4):
--    2021 = 50Q (LP 15 / MAT 15 / CN 12 / CH 8);
--    2022/2025 = 40Q (LP 20 / MAT 20, CN/CH 0).
--    scoring_rule NULL = DESCONHECIDA (capa não declara pontos; nunca
--    importar a tabela do IFRN). Nenhuma questão é inserida aqui (D.3).
-- 2. exam_versions: uma 'UNICA' por edição EAJ (só há um caderno no repo;
--    'UNICA' ≠ 'FINAL'/'DEFINITIVO': não há PDF de gabarito oficial EAJ).
-- 3. exam_documents: 2 por edição (6 no total), com kind/nota separando os
--    papéis (TASKS.md C.3):
--    - CADERNO = eaj_*.pdf (papel: AUDITORIA; TASKS.md regra 1);
--    - OUTRO   = questoes.md (papel: FONTE DE EXTRAÇÃO da transcrição —
--      enunciados + alternativas + respostas TRANSCRITAS pela curadoria,
--      provenance TRANSCRIBED_FROM_MD, decisão 2026-10-09; NUNCA documento
--      oficial — docs/blockers.md). 'OUTRO' é o único kind honesto do CHECK
--      da V1: GABARITO_* mentiria sobre oficialidade.
--
-- Decisões de honestidade (nada inventado, AGENTS.md §4):
-- 4. exams.edital = 'DESCONHECIDO' nas 3 linhas. A capa cita a banca
--    Comperve como organizadora, mas NENHUM número de edital foi localizado
--    (0 ocorrências de 'Edital' no full-scan — inventário A.2 §2). Banca ≠
--    edital: nunca reutilizar editais do IFRN (041/2021, 23/2024) nem
--    gravar 'Comperve' na coluna do número. A banca vai na note da versão.
-- 5. pages do .md = 1 (placeholder): a coluna é NOT NULL CHECK (pages > 0)
--    e páginas NÃO SE APLICAM a .md. A note registra as linhas reais
--    (2021: 504, 2022: 412, 2025: 507 — schema A.3) para auditoria.
-- 6. SHAs literais abaixo = arquivos vigentes no repo em 2026-10-09:
--    PDFs idênticos ao inventário A.2 §2; .md pós-normalização A.3
--    (iguais aos source_sha256 de data/linked/eaj/*.json, Fase B.2).
--    A conferência DB × arquivo é prova pós-migrate (critério C.3).
-- 7. Achados NÃO CONFIRMADOS NÃO entram como dados (só ponteiros nas
--    notes): EAJ-2022 40/40 'A' transcritos; 2025 Q22/Q39 NEEDS_VISUAL_CHECK
--    (trava D.3); páginas EAJ por questão = DESCONHECIDAS até a B.1/D.3.
--
-- Coexistência (TASKS.md regra 3):
-- 8. UNIQUE(institution,year) da V17: (EAJ,2022)/(EAJ,2025) coexistem com
--    (IFRN,2022)/(IFRN,2025); ano sozinho nunca identifica a edição.
--    (IFRN,2021) segue AUSENTE por política (AGENTS.md §3) — o CHECK de anos
--    permitiria, mas NENHUMA linha (IFRN,2021) é inserida aqui.
--
-- Efeito conhecido até a E.1 (sem código nesta migration):
-- 9. Consultas legadas por ano (ExamService/ExamRepository findByYear,
--    countByExamYear em versions/documents/questions) agregam IFRN+EAJ em
--    2022/2025. A religação por (institution,year) é a TASK E.1 (critério:
--    'EAJ-2022 ≠ IFRN-2022 em todos os endpoints'); até lá, o detalhe por ano
--    desses dois anos é ambíguo e a listagem soma contadores. Nenhum
--    comportamento IFRN de 2020/2023/2024/2026 muda (anos sem colisão).
--
-- Forward-only (padrão do projeto): sem down; rollback = restore de backup.
-- Idempotente (ON CONFLICT DO NOTHING, padrão da V2) para reexecução segura.
-- NUNCA edita V1–V18 (checksums Flyway). A cadeia do zero segue com a
-- trava pré-existente na V6 (questões importadas) — caso da prod
-- (incremental V16→V17→V18→V19) íntegro; ver observação C.2.

-- ---- 1. edições EAJ ----
INSERT INTO exams (institution, year, edital, duration_minutes, objective_count,
  lp_count, mat_count, cn_count, ch_count, has_essay, scoring_rule) VALUES
  ('EAJ', 2021, 'DESCONHECIDO', 180, 50, 15, 15, 12, 8, FALSE, NULL),
  ('EAJ', 2022, 'DESCONHECIDO', 180, 40, 20, 20, 0, 0, FALSE, NULL),
  ('EAJ', 2025, 'DESCONHECIDO', 180, 40, 20, 20, 0, 0, FALSE, NULL)
ON CONFLICT (institution, year) DO NOTHING;
-- scoring_rule NULL nas 3: pontuação DESCONHECIDA (nunca default inventado).

-- Fail-high: as 3 linhas EAJ com a tupla exata esperada (trava parcialidade).
DO $$
BEGIN
  IF (SELECT count(*) FROM exams WHERE institution = 'EAJ') <> 3 THEN
    RAISE EXCEPTION 'V19: esperado 3 edicoes EAJ (2021/2022/2025)';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM exams WHERE institution = 'EAJ' AND year = 2021
      AND edital = 'DESCONHECIDO' AND duration_minutes = 180 AND objective_count = 50
      AND lp_count = 15 AND mat_count = 15 AND cn_count = 12 AND ch_count = 8
      AND has_essay IS FALSE AND scoring_rule IS NULL) THEN
    RAISE EXCEPTION 'V19: tupla (EAJ,2021) divergiu do esperado (50Q 15/15/12/8, 180min, sem discursiva)';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM exams WHERE institution = 'EAJ' AND year = 2022
      AND edital = 'DESCONHECIDO' AND duration_minutes = 180 AND objective_count = 40
      AND lp_count = 20 AND mat_count = 20 AND cn_count = 0 AND ch_count = 0
      AND has_essay IS FALSE AND scoring_rule IS NULL) THEN
    RAISE EXCEPTION 'V19: tupla (EAJ,2022) divergiu do esperado (40Q 20/20, 180min, sem discursiva)';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM exams WHERE institution = 'EAJ' AND year = 2025
      AND edital = 'DESCONHECIDO' AND duration_minutes = 180 AND objective_count = 40
      AND lp_count = 20 AND mat_count = 20 AND cn_count = 0 AND ch_count = 0
      AND has_essay IS FALSE AND scoring_rule IS NULL) THEN
    RAISE EXCEPTION 'V19: tupla (EAJ,2025) divergiu do esperado (40Q 20/20, 180min, sem discursiva)';
  END IF;
END
$$;

-- ---- 2. versões (uma 'UNICA' por edição EAJ) ----
INSERT INTO exam_versions (exam_id, version_code, published_at, note)
SELECT e.id, v.code, v.pub::date, v.note FROM exams e
JOIN (VALUES
  ('EAJ', 2021, 'UNICA', NULL,
   'Caderno único no repo (19 p). Banca Comperve citada na capa como organizadora; número de edital DESCONHECIDO (inventário A.2 §2). Sem PDF de gabarito oficial: respostas = transcrição em data/provas/EAJ/2021/questoes.md (TRANSCRIBED_FROM_MD, docs/blockers.md).'),
  ('EAJ', 2022, 'UNICA', NULL,
   'Caderno único no repo (17 p). Banca Comperve citada na capa; número de edital DESCONHECIDO. Sem PDF de gabarito oficial (transcrição no questoes.md). Achado NÃO CONFIRMADO: 40/40 respostas transcritas = A (docs/blockers.md) — conferência pendente do gabarito oficial (Fase G).'),
  ('EAJ', 2025, 'UNICA', NULL,
   'Caderno único no repo (15 p). Banca Comperve citada na capa; número de edital DESCONHECIDO. Sem PDF de gabarito oficial (transcrição no questoes.md + tabela compilada transcrita). Q23 NULA transcrita; Q22/Q39 NEEDS_VISUAL_CHECK — trava de importação D.3.')
) AS v(inst, yr, code, pub, note) ON e.institution = v.inst AND e.year = v.yr
ON CONFLICT DO NOTHING;

-- ---- 3. documentos (caderno para auditoria + .md fonte da transcrição) ----
INSERT INTO exam_documents (exam_version_id, kind, file_name, sha256, pages, generator, note)
SELECT ev.id, v.kind, v.fname, v.sha, v.pages, v.gen, v.note
FROM exam_versions ev JOIN exams e ON e.id = ev.exam_id
JOIN (VALUES
  ('EAJ', 2021, 'CADERNO', 'data/provas/EAJ/2021/eaj_2021.pdf',
   'a86957757c3d48e76c75e505fcd6553193dcc9312a33a48e398ac119c96481fc', 19, 'Microsoft® Word 2013',
   'Caderno EAJ/UFRN Seleção 2021 (capa: 50 objetivas 01–15 Português, 16–30 Matemática, 31–42 Ciências da Natureza, 43–50 Ciências Humanas; 3h). Papel: AUDITORIA — fonte de extração do pipeline é o questoes.md (TASKS.md regra 1).'),
  ('EAJ', 2021, 'OUTRO', 'data/provas/EAJ/2021/questoes.md',
   '9d1a1e2ef717ad1ef914e542fe65b21611c2315da1dc250262941ca96ff5d785', 1, NULL,
   'Documento-fonte da transcrição (504 linhas, schema A.3; = source_sha256 de data/linked/eaj/2021.json): enunciados + alternativas + respostas TRANSCRITAS pela curadoria (TRANSCRIBED_FROM_MD, decisão 2026-10-09). NÃO é documento oficial. pages=1 placeholder (coluna NOT NULL CHECK pages>0; páginas NÃO APLICÁVEL a .md).'),
  ('EAJ', 2022, 'CADERNO', 'data/provas/EAJ/2022/eaj_2022.pdf',
   '1674e89c210cbcd315ff19abe4eb69bad00463b9f47f9874f30013a4b9bff581', 17, 'Chromium (pdf-lib)',
   'Caderno EAJ/UFRN Seleção 2022 (capa: 40 objetivas 01–20 Português, 21–40 Matemática; 3h). Papel: AUDITORIA — fonte de extração é o questoes.md. Nunca confundir com IFRN-2022 (041/2021, FUNCERN, com discursiva).'),
  ('EAJ', 2022, 'OUTRO', 'data/provas/EAJ/2022/questoes.md',
   'fe5dea2bb0277a4c4df9eafe122471167b822fb85c8eed52714d2abfd829ca0e', 1, NULL,
   'Documento-fonte da transcrição (412 linhas, schema A.3; = source_sha256 de data/linked/eaj/2022.json). TRANSCRIBED_FROM_MD; NÃO oficial. pages=1 placeholder (páginas N/A a .md). Achado NÃO CONFIRMADO: 40/40 respostas transcritas = A — nunca corrigir por inferência (Fase G).'),
  ('EAJ', 2025, 'CADERNO', 'data/provas/EAJ/2025/eaj_2025.pdf',
   '8bfaef8a188add78613b3dbe1a3d183b86d2a11d87172090c01a9e89986edb42', 15, 'Microsoft® Word para Microsoft 365',
   'Caderno UFRN–EAJ Seleção 2025 (capa: 40 objetivas 01–20 Língua Portuguesa, 21–40 Matemática; 3h). Papel: AUDITORIA. Nunca confundir com IFRN-2025 (23/2024, com discursiva).'),
  ('EAJ', 2025, 'OUTRO', 'data/provas/EAJ/2025/questoes.md',
   'abbcf1015285a3f7fd33b6357220fea4315497cc753b39d67891c47f9cff9527', 1, NULL,
   'Documento-fonte da transcrição (507 linhas, schema A.3; = source_sha256 de data/linked/eaj/2025.json). TRANSCRIBED_FROM_MD; NÃO oficial. pages=1 placeholder (páginas N/A a .md). Q23 NULA transcrita; Q22/Q39 NEEDS_VISUAL_CHECK (trava D.3).')
) AS v(inst, yr, kind, fname, sha, pages, gen, note)
  ON e.institution = v.inst AND e.year = v.yr AND ev.version_code = 'UNICA'
ON CONFLICT (sha256) DO NOTHING;

-- Fail-high: 3 versões + 6 documentos EAJ (trava parcialidade).
DO $$
BEGIN
  IF (SELECT count(*) FROM exam_versions ev JOIN exams e ON e.id = ev.exam_id
      WHERE e.institution = 'EAJ') <> 3 THEN
    RAISE EXCEPTION 'V19: esperado 3 exam_versions EAJ (uma UNICA por edicao)';
  END IF;
  IF (SELECT count(*) FROM exam_documents d JOIN exam_versions ev ON ev.id = d.exam_version_id
      JOIN exams e ON e.id = ev.exam_id WHERE e.institution = 'EAJ') <> 6 THEN
    RAISE EXCEPTION 'V19: esperado 6 exam_documents EAJ (caderno + .md por edicao)';
  END IF;
END
$$;
