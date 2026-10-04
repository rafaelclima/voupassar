-- V11__drop_explanation.sql — remove passo a passo textual (decisão de produto 2026-10-04).
-- A plataforma é de teste de conhecimento, não de ensino: o feedback segue com
-- acerto/erro + resposta correta (gabarito oficial) + assunto. A coluna
-- questions.explanation estava sempre NULL (240/240) e nenhum fluxo de
-- redação/curadoria foi entregue, então a remoção é sem perda de dado.
-- Idempotente: só executa se a coluna ainda existir.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'questions' AND column_name = 'explanation'
  ) THEN
    ALTER TABLE questions DROP COLUMN explanation;
  END IF;
END
$$;
