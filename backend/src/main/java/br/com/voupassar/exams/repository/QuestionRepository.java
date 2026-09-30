package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Question;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  /**
   * Busca paginada do banco de questões (TASK 3.4).
   *
   * <p>Todos os filtros são opcionais ({@code null} = sem filtro). Filtros por
   * assunto/subassunto usam {@code EXISTS} sobre classificações não-rejeitadas
   * (mesmo critério das contagens da TASK 3.3). Ordem fixa e determinística:
   * ano-fonte crescente, número da questão, id — nunca inventar ordenação por
   * relevância sem algoritmo auditável (Fase 4).
   */
  @Query(
      value = """
          SELECT q FROM Question q
          JOIN q.discipline d
          LEFT JOIN q.exam e
          WHERE (:disciplineCode IS NULL OR d.code = :disciplineCode)
            AND (:year IS NULL OR e.year = :year)
            AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
            AND (:sourceType IS NULL OR q.sourceType = :sourceType)
            AND (:topicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.topic.id = :topicId AND c.status <> 'REJECTED'))
            AND (:subtopicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.subtopic.id = :subtopicId AND c.status <> 'REJECTED'))
          ORDER BY q.sourceYear ASC NULLS LAST, q.sourceQuestionNumber ASC NULLS LAST, q.id ASC
          """,
      countQuery = """
          SELECT COUNT(q) FROM Question q
          JOIN q.discipline d
          LEFT JOIN q.exam e
          WHERE (:disciplineCode IS NULL OR d.code = :disciplineCode)
            AND (:year IS NULL OR e.year = :year)
            AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
            AND (:sourceType IS NULL OR q.sourceType = :sourceType)
            AND (:topicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.topic.id = :topicId AND c.status <> 'REJECTED'))
            AND (:subtopicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.subtopic.id = :subtopicId AND c.status <> 'REJECTED'))
          """)
  Page<Question> search(
      @Param("disciplineCode") String disciplineCode,
      @Param("topicId") Long topicId,
      @Param("subtopicId") Long subtopicId,
      @Param("year") Short year,
      @Param("difficulty") String difficulty,
      @Param("sourceType") String sourceType,
      Pageable pageable);
}
