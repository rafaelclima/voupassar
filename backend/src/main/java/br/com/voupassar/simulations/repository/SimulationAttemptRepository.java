package br.com.voupassar.simulations.repository;

import br.com.voupassar.simulations.entity.SimulationAttempt;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Execuções de simulado com escrita (TASK 5.1).
 *
 * <p>Toda leitura é escopada ao dono do token (anti-enumeração, mesmo padrão
 * da TASK 3.7: inexistente ou de outro aluno → 404 sem distinguir).
 */
public interface SimulationAttemptRepository extends JpaRepository<SimulationAttempt, Long> {

  Optional<SimulationAttempt> findByIdAndUserId(Long id, Long userId);

  Page<SimulationAttempt> findByUserIdOrderByStartedAtDescIdDesc(Long userId, Pageable pageable);
}
