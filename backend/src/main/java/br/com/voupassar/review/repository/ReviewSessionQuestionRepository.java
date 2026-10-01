package br.com.voupassar.review.repository;

import br.com.voupassar.review.entity.ReviewSessionQuestion;
import br.com.voupassar.review.entity.ReviewSessionQuestionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Caderno congelado da sessão de revisão (TASK 5.4).
 *
 * <p>Todo acesso é escopado pela sessão do dono (validada no serviço por
 * {@code (id, userId)}): item de sessão alheia responde 404 como se não
 * existisse (sem enumeração entre alunos).
 */
public interface ReviewSessionQuestionRepository
    extends JpaRepository<ReviewSessionQuestion, ReviewSessionQuestionId> {

  List<ReviewSessionQuestion> findByStudySessionIdOrderByPositionAsc(Long studySessionId);
}
