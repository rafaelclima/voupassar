-- V20__eaj_taxonomy_cn_ch.sql — TASK D.3/D.4 (Programa EAJ, FASE D).
-- Taxonomia CN/CH observada + páginas DESCONHECIDAS para o EAJ
-- (questões E passagens; figuras já usam NULL via manifest B.3).
--
-- O que muda:
-- 1. topics/subtopics CN/CH SÓ para o observado nas 20 questões CN/CH do
--    EAJ-2021 (D.1, 11 tópicos + 19 subtópicos, cada um com 1+ evidência).
--    Nenhum código além de validate_classification.py::CN_ASSUNTOS/CH_ASSUNTOS
--    e CN_SUBS/CH_SUBS entra aqui (TASKS.md regra 5 / C.2 §6: nada inventado
--    antes da evidência). LP/MAT reutilizam a taxonomia v1.1 da V2, intacta.
--    Nomes humanos abaixo são rótulos (não conteúdo de prova); códigos são
--    os mesmos da classificação D.1.
-- 2. Páginas oficiais NULL para o EAJ: DROP da
--    chk_questions_official_pages (V13, que exigia page_start/page_end NOT
--    NULL para TODO OFFICIAL). O `.md` EAJ não traz página por questão
--    (B.1: page_start/end NULL + page_status DESCONHECIDO — nunca inventar,
--    AGENTS.md §4). A exigência por edição passa ao importador (TASKS.md
--    regra 4): IFRN exige páginas NOT NULL (3 fontes trazem); EAJ exige NULL
--    (DESCONHECIDO). CHECK único no banco congelaria estrutura por edição,
--    violando a regra 4 para edições futuras (mesmo argumento da V18 §5).
-- 3. OUTRO (EAJ-2025 Q40, GEOMETRIA sem subcódigo v1.1): SEM linha nova.
--    Segue docs/database-erd.md §2.4 (NULL aceito quando OUTRO): topic_id =
--    GEOMETRIA, subtopic_id = NULL, observation com a justificativa da D.1.
--    Nenhum tópico/subtópico `OUTRO` permanente é criado (padrão V2/V11).
-- 4. Passagens EAJ com páginas DESCONHECIDAS (D.4): `passages.page_start` /
--    `page_end` (V9 NOT NULL) passam a NULL quando DESCONHECIDO. O `.md` EAJ
--    não traz página por passagem (B.4, mesmo padrão das questões). IFRN
--    segue NOT NULL por dados. Mesma regra 4: limite por edição no
--    importador, nunca em CHECK.
--
-- Forward-only (padrão do projeto): sem down; rollback = restore de backup.
-- Idempotente (ON CONFLICT DO NOTHING) para reexecução segura.
-- NUNCA edita V1–V19 (checksums Flyway). Trava do zero na V6 (pré-existente,
-- idem C.1/C.2/C.3) segue fora de escopo — cadeia incremental (caso da prod:
-- V19→V20) íntegra.
--
-- ---- 1. tópicos CN/CH (11, só o observado D.1) ----
INSERT INTO topics (discipline_id, code, name)
SELECT d.id, v.code, v.name FROM disciplines d
JOIN (VALUES
  ('CIENCIAS_NATUREZA','ECOLOGIA','Ecologia'),
  ('CIENCIAS_NATUREZA','NUTRICAO_SAUDE','Nutrição e saúde'),
  ('CIENCIAS_NATUREZA','AGROPECUARIA','Agropecuária'),
  ('CIENCIAS_NATUREZA','BIOLOGIA_CELULAR','Biologia celular'),
  ('CIENCIAS_NATUREZA','QUIMICA_GERAL','Química geral'),
  ('CIENCIAS_NATUREZA','FISICA_GERAL','Física geral'),
  ('CIENCIAS_HUMANAS','HISTORIA_BRASIL','História do Brasil'),
  ('CIENCIAS_HUMANAS','CULTURA_SOCIEDADE','Cultura e sociedade'),
  ('CIENCIAS_HUMANAS','CARTOGRAFIA','Cartografia'),
  ('CIENCIAS_HUMANAS','GEOGRAFIA_BRASIL','Geografia do Brasil'),
  ('CIENCIAS_HUMANAS','GEOPOLITICA','Geopolítica')
) AS v(dcode, code, name) ON d.code = v.dcode
ON CONFLICT DO NOTHING;

-- ---- 2. subtópicos CN/CH (19, só o observado D.1) ----
INSERT INTO subtopics (topic_id, code, name)
SELECT t.id, v.code, v.name FROM topics t
JOIN disciplines d ON d.id = t.discipline_id
JOIN (VALUES
  ('CIENCIAS_NATUREZA','ECOLOGIA','NIVEL_TROFICO','Nível trófico'),
  ('CIENCIAS_NATUREZA','ECOLOGIA','NUTRICAO_SERES_VIVOS','Nutrição dos seres vivos'),
  ('CIENCIAS_NATUREZA','NUTRICAO_SAUDE','ALIMENTOS_PROCESSADOS','Alimentos processados'),
  ('CIENCIAS_NATUREZA','AGROPECUARIA','PRODUCAO_ALIMENTOS','Produção de alimentos'),
  ('CIENCIAS_NATUREZA','BIOLOGIA_CELULAR','RESPIRACAO_CELULAR','Respiração celular'),
  ('CIENCIAS_NATUREZA','QUIMICA_GERAL','FENOMENOS_QUIMICOS','Fenômenos químicos'),
  ('CIENCIAS_NATUREZA','QUIMICA_GERAL','POLARIDADE_MISTURAS','Polaridade e misturas'),
  ('CIENCIAS_NATUREZA','QUIMICA_GERAL','RADIOATIVIDADE','Radioatividade'),
  ('CIENCIAS_NATUREZA','FISICA_GERAL','ENERGIA_CONSERVACAO','Energia e conservação'),
  ('CIENCIAS_NATUREZA','FISICA_GERAL','ESCALAS_TERMOMETRICAS','Escalas termométricas'),
  ('CIENCIAS_NATUREZA','FISICA_GERAL','LEIS_NEWTON','Leis de Newton'),
  ('CIENCIAS_HUMANAS','HISTORIA_BRASIL','MIGRACOES_REPUBLICA','Migrações na República'),
  ('CIENCIAS_HUMANAS','HISTORIA_BRASIL','ECONOMIA_COLONIAL','Economia colonial'),
  ('CIENCIAS_HUMANAS','HISTORIA_BRASIL','COLONIZACAO_CONTATO','Colonização e contato'),
  ('CIENCIAS_HUMANAS','CULTURA_SOCIEDADE','CULTURA_AFRO_BRASILEIRA','Cultura afro-brasileira'),
  ('CIENCIAS_HUMANAS','CARTOGRAFIA','ELEMENTOS_MAPA','Elementos do mapa'),
  ('CIENCIAS_HUMANAS','GEOGRAFIA_BRASIL','BIOMAS_IMPACTOS','Biomas e impactos'),
  ('CIENCIAS_HUMANAS','GEOPOLITICA','BLOCOS_ECONOMICOS','Blocos econômicos'),
  ('CIENCIAS_HUMANAS','GEOPOLITICA','ORDEM_MUNDIAL','Ordem mundial')
) AS v(dcode, tcode, code, name)
  ON d.code = v.dcode AND t.code = v.tcode
ON CONFLICT DO NOTHING;

-- Fail-high: 11 tópicos + 19 subtópicos EAJ exatos (trava parcialidade).
DO $$
BEGIN
  IF (SELECT count(*) FROM topics t JOIN disciplines d ON d.id = t.discipline_id
      WHERE d.code IN ('CIENCIAS_NATUREZA','CIENCIAS_HUMANAS')) <> 11 THEN
    RAISE EXCEPTION 'V20: esperado 11 topics CN/CH (só o observado D.1)';
  END IF;
  IF (SELECT count(*) FROM subtopics s JOIN topics t ON t.id = s.topic_id
      JOIN disciplines d ON d.id = t.discipline_id
      WHERE d.code IN ('CIENCIAS_NATUREZA','CIENCIAS_HUMANAS')) <> 19 THEN
    RAISE EXCEPTION 'V20: esperado 19 subtopics CN/CH (só o observado D.1)';
  END IF;
END
$$;

-- ---- 3. páginas DESCONHECIDAS para o EAJ (NULL = DESCONHECIDO) ----
-- V13 exigia pages NOT NULL para TODO OFFICIAL (as 240 IFRN têm). O `.md`
-- EAJ não traz página por questão (B.1, inventário A.2: nunca inventar).
-- O limite por edição pertence aos dados + importador (IFRN: exige NOT NULL;
-- EAJ: exige NULL), nunca a CHECK (TASKS.md regra 4; mesmo argumento V18 §5).
ALTER TABLE questions DROP CONSTRAINT IF EXISTS chk_questions_official_pages;
COMMENT ON COLUMN questions.page_start IS
  'TASK D.3 (V20): NULL = DESCONHECIDO (EAJ, .md sem página; nunca inventar). IFRN segue NOT NULL por dados, exigido no importador.';

-- ---- 4. passagens EAJ com páginas DESCONHECIDAS (D.4) ----
-- V9 exigia passages.page_start/page_end NOT NULL (as IFRN têm). Os `.md`
-- EAJ não trazem página por passagem (B.4: NULL + page_status DESCONHECIDO).
ALTER TABLE passages ALTER COLUMN page_start DROP NOT NULL;
ALTER TABLE passages ALTER COLUMN page_end DROP NOT NULL;
COMMENT ON COLUMN passages.page_start IS
  'TASK D.4 (V20): NULL = DESCONHECIDO (EAJ, .md sem página; nunca inventar). IFRN segue NOT NULL por dados.';
