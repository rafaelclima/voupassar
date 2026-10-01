-- V5__review_session_questions.sql — TASK 5.4 (Modo Revisão: experiência dedicada)
-- Caderno congelado da sessão de revisão: ordem determinística (posições 1..N)
-- derivada do top-N da fila da TASK 4.5 no momento da criação.
--
-- Por que tabela nova (e não só study_sessions)?
-- study_sessions (V1) não guarda itens nem filtros: sem esta tabela o "retomar"
-- não reconstruiria a ordem nem o progresso (respondida/pendente por posição).
-- Segue o padrão de simulation_questions (TASK 5.1), mas SEM frozen_answer_key:
-- a revisão responde via POST /attempts com mode=REVISAO, cuja correção usa o
-- gabarito vigente (TASK 3.7); congelar gabarito aqui divergiria do is_correct
-- persistido no fato imutável. A ordem congelada basta para progresso, resume
-- e resultado auditáveis. Revisão continua sem gerar simulado (ERD §2.6):
-- esta tabela referencia study_sessions, nunca simulations.
-- Idempotente via Flyway (uma execução).
CREATE TABLE review_session_questions (
  study_session_id BIGINT NOT NULL REFERENCES study_sessions(id) ON DELETE CASCADE,
  position SMALLINT NOT NULL CHECK (position > 0),
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (study_session_id, position)
);
CREATE INDEX idx_review_sq_session ON review_session_questions(study_session_id, position);
CREATE INDEX idx_review_sq_question ON review_session_questions(question_id);
DO $$
BEGIN
  EXECUTE format(
    'CREATE TRIGGER trg_%I_updated BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION set_updated_at()',
    'review_session_questions', 'review_session_questions');
END $$;
-- Registra a tabela no trigger de imutabilidade? Não: só question_attempts é
-- imutável (ERD §5 item 5). O caderno de revisão é congelado por construção
-- (sem PUT/DELETE na API), mas sem trigger anti-UPDATE — igual a
-- simulation_questions.
