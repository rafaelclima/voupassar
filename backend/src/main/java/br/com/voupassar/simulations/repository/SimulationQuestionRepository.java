package br.com.voupassar.simulations.repository;

import br.com.voupassar.simulations.entity.SimulationQuestion;
import br.com.voupassar.simulations.entity.SimulationQuestionId;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Caderno congelado das tentativas (TASK 5.1) — escrita na criação (posições
 * 1..N), só leitura depois (o caderno nunca muda) — mais sondas do Modo Prova
 * (TASK 5.3: gabarito oculto durante a execução).
 */
public interface SimulationQuestionRepository
    extends JpaRepository<SimulationQuestion, SimulationQuestionId> {

  List<SimulationQuestion> findBySimulationAttemptIdOrderByPositionAsc(Long simulationAttemptId);

  List<SimulationQuestion> findBySimulationAttemptIdInOrderBySimulationAttemptIdAscPositionAsc(
      List<Long> simulationAttemptIds);

  /**
   * TASK 5.3: a questão está em algum simulado {@code PROVA} ainda
   * {@code IN_PROGRESS} deste aluno? Base da ocultação de gabarito em
   * {@code GET /api/v1/questions}.
   */
  @Query("""
      SELECT CASE WHEN COUNT(sq) > 0 THEN true ELSE false END
      FROM SimulationQuestion sq, SimulationAttempt sa
      WHERE sq.simulationAttemptId = sa.id
        AND sa.user.id = :userId
        AND sa.mode = 'PROVA'
        AND sa.status = 'IN_PROGRESS'
        AND sq.question.id = :questionId
      """)
  boolean existsInProvaInProgress(
      @Param("userId") Long userId, @Param("questionId") Long questionId);

  /**
   * TASK 5.3: dentre estes ids, quais estão em simulado {@code PROVA} ainda
   * {@code IN_PROGRESS} deste aluno? (1 query para a página, sem N+1).
   */
  @Query("""
      SELECT DISTINCT sq.question.id
      FROM SimulationQuestion sq, SimulationAttempt sa
      WHERE sq.simulationAttemptId = sa.id
        AND sa.user.id = :userId
        AND sa.mode = 'PROVA'
        AND sa.status = 'IN_PROGRESS'
        AND sq.question.id IN :questionIds
      """)
  Set<Long> findInProvaInProgress(
      @Param("userId") Long userId, @Param("questionIds") List<Long> questionIds);
}
