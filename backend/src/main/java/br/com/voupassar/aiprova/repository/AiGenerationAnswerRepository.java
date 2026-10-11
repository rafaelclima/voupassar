package br.com.voupassar.aiprova.repository;

import br.com.voupassar.aiprova.entity.AiGenerationAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Respostas-IA (TASK A.3) — fato próprio só-INSERT (imutável no banco);
 * leitura por execução para correção contra o congelado.
 */
public interface AiGenerationAnswerRepository extends JpaRepository<AiGenerationAnswer, Long> {

  List<AiGenerationAnswer> findBySimulationAttemptIdAndUserIdOrderByAnsweredAtAscIdAsc(
      Long simulationAttemptId, Long userId);
}
