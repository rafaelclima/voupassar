package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.QuestionPassage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Vínculos questão ↔ texto-base (TASK 6.9). Consultas em lote (sem N+1):
 * uma query por página de questões ou por detalhe.
 */
@Repository
public interface QuestionPassageRepository extends JpaRepository<QuestionPassage, Long> {

  @Query(
      "SELECT qp FROM QuestionPassage qp JOIN FETCH qp.passage "
          + "WHERE qp.questionId = :questionId ORDER BY qp.position ASC")
  List<QuestionPassage> findByQuestionIdOrdered(long questionId);

  @Query(
      "SELECT qp FROM QuestionPassage qp JOIN FETCH qp.passage "
          + "WHERE qp.questionId IN :questionIds ORDER BY qp.questionId ASC, qp.position ASC")
  List<QuestionPassage> findByQuestionIdsOrdered(List<Long> questionIds);
}
