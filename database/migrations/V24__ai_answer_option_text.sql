-- V24__ai_answer_option_text.sql — TASK A.3 (Programa Prova-IA, FASE A).
-- Corrige a limitação da DDL V23 (mesmo defeito da V1, mesmo reparo da V4):
-- ai_generation_answers.selected_option era CHAR(1) mas o CHECK admite
-- 'BLANK' (5 chars, resposta em branco) — valor impossível de persistir no
-- Postgres (ERROR "value too long for type character(1)").
-- Troca para TEXT (mesmo tipo de mode/status na V23); o CHECK de domínio é
-- preservado e continua válido sobre TEXT. Sem perda de dados ('A'..'D'
-- convertem implicitamente; a coluna nasceu vazia na V23 — sem linhas
-- afetadas). Descoberto em smoke de escrita antes de qualquer deploy da
-- V23 fora do DEV (VPS segue em V22): nenhuma resposta-IA existia.
--
-- Forward-only (padrão do projeto: V3–V23 sem down); rollback = restore de
-- backup (docs/deploy-vps.md). NUNCA edita V1–V23 (checksums Flyway).

ALTER TABLE ai_generation_answers ALTER COLUMN selected_option TYPE TEXT;

-- Trava de estado final (fail-high; passa na 1ª e na N-ésima): tipo TEXT
-- + CHECK de domínio com BLANK presentes. A escrita do domínio completo
-- (A–D + BLANK) é exercitada pela suíte da A.3 contra o banco.
DO $$
DECLARE
  v_type TEXT;
BEGIN
  SELECT data_type INTO v_type
    FROM information_schema.columns
   WHERE table_name = 'ai_generation_answers' AND column_name = 'selected_option';
  IF v_type <> 'text' THEN
    RAISE EXCEPTION 'V24: selected_option deveria ser text, achado %', v_type;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint
      WHERE conrelid = 'ai_generation_answers'::regclass AND contype = 'c'
        AND pg_get_constraintdef(oid) LIKE '%BLANK%') THEN
    RAISE EXCEPTION 'V24: CHECK de domínio de selected_option ausente';
  END IF;
END
$$;
