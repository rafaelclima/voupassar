-- V14__zero_has_figure_text_only.sql — Curadoria manual 2026-10-07:
-- 3 questões oficiais marcadas has_figure=TRUE pelo importador (sinal
-- NECESSITA_REVISAO) não dependem de figura para serem resolvidas:
--   2020 Q26 — só texto (percentuais encadeados; sem visual no caderno);
--   2025 Q33 — sem figura;
--   2025 Q36 — o "percentual" (83%) está no texto do bloco da questão,
--              não exige imagem.
-- (2025 Q39, citada na mesma revisão, já é has_figure=FALSE — status OK
-- na classificação; nada a fazer aqui.)
-- Pré-requisito: V1..V13 aplicadas. NÃO edita V1..V13 (checksum Flyway).
-- Idempotente: UPDATE/DELETE condicionais, seguro repetir.

UPDATE questions
SET has_figure = FALSE, updated_at = now()
WHERE source_type = 'OFFICIAL'
  AND has_figure IS TRUE
  AND ((source_year = 2020 AND source_question_number = 26)
    OR (source_year = 2025 AND source_question_number IN (33, 36)));

DELETE FROM question_tag_map
WHERE tag_id IN (SELECT id FROM question_tags WHERE code = 'FIGURA')
  AND question_id IN (
    SELECT id FROM questions
    WHERE source_type = 'OFFICIAL'
      AND ((source_year = 2020 AND source_question_number = 26)
        OR (source_year = 2025 AND source_question_number IN (33, 36)))
  );
