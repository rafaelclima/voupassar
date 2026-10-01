package br.com.voupassar.simulations.repository;

import br.com.voupassar.simulations.entity.SimulationQuestion;
import br.com.voupassar.simulations.entity.SimulationQuestionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Caderno congelado das tentativas (TASK 5.1) — escrita na criação (posições
 * 1..N), só leitura depois (o caderno nunca muda).
 */
public interface SimulationQuestionRepository
    extends JpaRepository<SimulationQuestion, SimulationQuestionId> {

  List<SimulationQuestion> findBySimulationAttemptIdOrderByPositionAsc(Long simulationAttemptId);

  List<SimulationQuestion> findBySimulationAttemptIdInOrderBySimulationAttemptIdAscPositionAsc(
      List<Long> simulationAttemptIds);
}
