-- V4__attempts_selected_option.sql — TASK 3.7 (Tentativas: escrita)
-- Corrige a limitação da DDL V1 documentada em docs/api-perfil.md §"Notas técnicas":
-- question_attempts.selected_option era CHAR(1) mas o CHECK admite 'BLANK'
-- (5 chars, resposta em branco) — valor impossível de persistir no Postgres.
-- Troca para TEXT (mesmo tipo de mode/status na V1); o CHECK de domínio é
-- preservado e continua válido sobre TEXT. Sem perda de dados ('A'..'D'
-- convertem implicitamente). Idempotente via Flyway (uma execução).
ALTER TABLE question_attempts ALTER COLUMN selected_option TYPE TEXT;
