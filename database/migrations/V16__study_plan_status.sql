-- V16__study_plan_status.sql — flag PROVISORIO/PESSOAL do roteiro (TASK 20.1).
-- Conta nova sem tentativas pontuáveis recebe plano PROVISORIO (só
-- frequência histórica); com ≥3 pontuáveis (mesmo limiar do diagnóstico,
-- DiagnosisService.MIN_SCORED_FOR_SIGNAL) a regeneração marca PESSOAL.
-- Sem nova tabela de domínio: só 1 coluna + CHECK.
ALTER TABLE study_plans ADD COLUMN status TEXT NOT NULL DEFAULT 'PESSOAL'
  CHECK (status IN ('PROVISORIO', 'PESSOAL'));
