-- V25__simulations_link_owner.sql — TASK A.3 (Programa Prova-IA).
-- Remove a cláusula owner_user_id IS NOT NULL da chk_simulations_link
-- para AI_GENERATED_EDITION (dono segue obrigatório pela aplicação).
--
-- Por que (achado em wipe de usuário de smoke, nunca em produção — VPS em
-- V22): simulations.owner_user_id é ON DELETE SET NULL (templates oficiais
-- sobrevivem ao wipe, ERD §2.6), mas a V23 exigia dono NOT NULL para IA. No
-- wipe, o SET NULL roda antes do CASCADE que apagaria a linha
-- (users → ai_generations.user_id → CASCADE → simulations.ai_generation_id
-- → CASCADE) e o CHECK imediato aborta tudo. CHECKs no Postgres não aceitam
-- DEFERRABLE (tentativa registrada: ERROR 0A000), logo a exigência de dono
-- não pode conviver com o wipe em DDL.
-- Sem a cláusula, o wipe passa (SET NULL + CASCADE apagam a linha) e
-- nenhum órfão persiste: o único caminho que anula o dono é o DELETE do
-- usuário, que sempre cascateia a geração e com ela a simulação-IA (FK
-- ai_generation_id ON DELETE CASCADE). UPDATE direto de owner não existe
-- em nenhum serviço (simulations nunca é atualizada após a criação).
-- A A.3 sempre cria simulação-IA com dono (= aluno da geração).
--
-- Forward-only (padrão do projeto); rollback = restore de backup
-- (docs/deploy-vps.md). NUNCA edita V1–V24 (checksums Flyway).

ALTER TABLE simulations DROP CONSTRAINT chk_simulations_link;
ALTER TABLE simulations ADD CONSTRAINT chk_simulations_link
  CHECK (
    (type = 'REAL_EDITION' AND exam_id IS NOT NULL AND ai_generation_id IS NULL)
    OR (type = 'BY_DISCIPLINE' AND ai_generation_id IS NULL)
    OR (type = 'AI_GENERATED_EDITION' AND ai_generation_id IS NOT NULL
        AND exam_id IS NULL)
  );

-- Trava de estado final (fail-high; passa na 1ª e na N-ésima).
DO $$
DECLARE
  v_def TEXT;
BEGIN
  SELECT pg_get_constraintdef(oid) INTO v_def FROM pg_constraint
   WHERE conname = 'chk_simulations_link' AND conrelid = 'simulations'::regclass;
  IF v_def IS NULL OR v_def LIKE '%owner_user_id%' THEN
    RAISE EXCEPTION 'V25: chk_simulations_link ainda exige dono (%)', v_def;
  END IF;
  IF v_def NOT LIKE '%AI_GENERATED_EDITION%' THEN
    RAISE EXCEPTION 'V25: chk_simulations_link sem o tipo-IA (%)', v_def;
  END IF;

  -- Nenhum CHECK antigo sem IA pode restar (herdado da V23).
  IF EXISTS (SELECT 1 FROM pg_constraint
      WHERE conrelid = 'simulations'::regclass AND contype = 'c'
        AND pg_get_constraintdef(oid) LIKE '%BY_DISCIPLINE%'
        AND pg_get_constraintdef(oid) NOT LIKE '%AI_GENERATED_EDITION%') THEN
    RAISE EXCEPTION 'V25: CHECK antiga de simulations sem AI_GENERATED_EDITION ainda presente';
  END IF;
END
$$;
