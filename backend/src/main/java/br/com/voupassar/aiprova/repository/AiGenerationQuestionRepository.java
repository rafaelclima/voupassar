package br.com.voupassar.aiprova.repository;

import br.com.voupassar.aiprova.entity.AiGenerationQuestion;
import br.com.voupassar.aiprova.entity.AiGenerationQuestionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Caderno-IA congelado (TASK A.3) — escrita na geração (posições 1..N), só
 * leitura depois (o caderno nunca muda).
 */
public interface AiGenerationQuestionRepository
    extends JpaRepository<AiGenerationQuestion, AiGenerationQuestionId> {

  List<AiGenerationQuestion> findByGenerationIdOrderByPositionAsc(Long generationId);
}
