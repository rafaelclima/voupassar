package br.com.voupassar.questions.repository;

import br.com.voupassar.questions.entity.QuestionOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Alternativas para a API de questões (TASK 3.4) — somente leitura.
 *
 * <p>Carregadas em lote ({@code IN}) para montar a página sem N+1; a ordem
 * canônica é o rótulo (A–D).
 */
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

  @Query("""
      SELECT o FROM QuestionOption o
      WHERE o.question.id = :questionId
      ORDER BY o.label ASC
      """)
  List<QuestionOption> findByQuestionIdOrdered(@Param("questionId") Long questionId);

  @Query("""
      SELECT o FROM QuestionOption o
      WHERE o.question.id IN :ids
      ORDER BY o.question.id ASC, o.label ASC
      """)
  List<QuestionOption> findByQuestionIdsOrdered(@Param("ids") List<Long> ids);
}
