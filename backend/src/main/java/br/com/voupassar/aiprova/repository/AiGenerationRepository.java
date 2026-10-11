package br.com.voupassar.aiprova.repository;

import br.com.voupassar.aiprova.entity.AiGeneration;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Gerações de prova-IA (TASK A.3) — escrita na geração (caderno congelado),
 * leitura na execução (caderno + auditoria).
 */
public interface AiGenerationRepository extends JpaRepository<AiGeneration, Long> {}
