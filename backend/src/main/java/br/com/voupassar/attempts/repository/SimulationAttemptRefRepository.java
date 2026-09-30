package br.com.voupassar.attempts.repository;

import br.com.voupassar.attempts.entity.SimulationAttemptRef;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Validação do vínculo opcional com simulado (TASK 3.7).
 *
 * <p>Só leitura de existência/posse/status/modo: a execução de simulados é
 * a Fase 5. Tentativa vinculada a simulado alheio ou inexistente responde
 * 404 sem distinguir os casos.
 */
public interface SimulationAttemptRefRepository extends JpaRepository<SimulationAttemptRef, Long> {

  Optional<SimulationAttemptRef> findByIdAndUserId(Long id, Long userId);
}
