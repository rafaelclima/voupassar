package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Agregados somente-leitura para {@code GET /editions/{year}/stats}.
 *
 * <p>Contagens derivadas de {@code questions} (importador TASK 2.3).
 * Anuladas contam como conteúdo que apareceu na prova; regra de pontuação de
 * anuladas é DESCONHECIDA (TASK 1.3 §4) — aqui só contamos, nunca pontuamos.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

  long countByExamYear(Short year);

  long countByExamYearAndAnnulledTrue(Short year);

  long countByDisciplineCode(String disciplineCode);

  /**
   * Retorna linhas {@code [code(String), name(String), total(Long), annulled(Long)]}.
   */
  @Query("""
      SELECT d.code, d.name, COUNT(q),
             SUM(CASE WHEN q.annulled = true THEN 1 ELSE 0 END)
      FROM Question q JOIN q.discipline d
      WHERE q.exam.year = :year
      GROUP BY d.code, d.name
      """)
  List<Object[]> countByDiscipline(@Param("year") Short year);
}
