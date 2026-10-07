-- V15__recommendation_v2.sql — versionamento do score v2 do roteiro (TASK 18.1).
-- SEM mudança de schema: só registra na flyway_schema_history que o motor
-- de recomendação passou a v2-deterministico (frequência histórica real +
-- recência + dificuldade + carga por meta/ano-alvo).
-- A versão por plano segue em study_plans.algorithm_version (linha a linha).
DO $$
BEGIN
  RAISE NOTICE 'recommendation v2-deterministico (TASK 18.1): sem DDL, só versionamento';
END
$$;
