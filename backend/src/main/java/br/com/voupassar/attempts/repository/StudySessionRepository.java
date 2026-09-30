package br.com.voupassar.attempts.repository;

import br.com.voupassar.attempts.entity.StudySession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Sessões do dono do token (TASK 3.7).
 *
 * <p>Todo acesso é escopado por {@code (id, userId)}: sessão alheia responde
 * 404 como se não existisse (sem enumeração entre alunos).
 */
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

  Optional<StudySession> findByIdAndUserId(Long id, Long userId);
}
