package br.com.voupassar.content.repository;

import br.com.voupassar.content.entity.QuestionClassification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Agregados históricos sobre {@code question_classifications} (TASK 3.3).
 *
 * <p>Contam classificações não-rejeitadas ({@code status <> 'REJECTED'} com
 * tópico presente). Anuladas ({@code X} no gabarito) contam como conteúdo que
 * apareceu na prova — aqui só contamos, nunca pontuamos (regra de pontuação
 * DESCONHECIDA, TASK 1.3 §4).
 */
public interface QuestionClassificationRepository
    extends JpaRepository<QuestionClassification, Long> {

  /** Total de questões classificadas (tópico presente, não-rejeitada). */
  @Query("""
      SELECT COUNT(c) FROM QuestionClassification c
      WHERE c.topic IS NOT NULL AND c.status <> 'REJECTED'
      """)
  long countClassified();

  boolean existsByStatus(String status);

  /** Versões de taxonomia presentes no banco (ex. {@code ["v1.1"]}). */
  @Query("SELECT DISTINCT c.taxonomyVersion FROM QuestionClassification c ORDER BY c.taxonomyVersion")
  List<String> distinctTaxonomyVersions();

  /** Retorna linhas {@code [topicId(Long), total(Long)]}. */
  @Query("""
      SELECT c.topic.id, COUNT(c) FROM QuestionClassification c
      WHERE c.topic IS NOT NULL AND c.status <> 'REJECTED'
      GROUP BY c.topic.id
      """)
  List<Object[]> countByTopic();

  /** Retorna linhas {@code [subtopicId(Long), total(Long)]}. */
  @Query("""
      SELECT c.subtopic.id, COUNT(c) FROM QuestionClassification c
      WHERE c.subtopic IS NOT NULL AND c.status <> 'REJECTED'
      GROUP BY c.subtopic.id
      """)
  List<Object[]> countBySubtopic();

  @Query("""
      SELECT COUNT(c) FROM QuestionClassification c
      WHERE c.topic.id = :topicId AND c.status <> 'REJECTED'
      """)
  long countByTopicId(@Param("topicId") Long topicId);

  @Query("""
      SELECT COUNT(c) FROM QuestionClassification c
      WHERE c.subtopic.id = :subtopicId AND c.status <> 'REJECTED'
      """)
  long countBySubtopicId(@Param("subtopicId") Long subtopicId);

  /**
   * Distribuição por edição de um assunto.
   * Retorna linhas {@code [year(Short), total(Long), annulled(Long)]}.
   */
  @Query("""
      SELECT q.exam.year, COUNT(c),
             SUM(CASE WHEN q.annulled = true THEN 1 ELSE 0 END)
      FROM QuestionClassification c JOIN c.question q
      WHERE c.topic.id = :topicId AND c.status <> 'REJECTED'
      GROUP BY q.exam.year
      ORDER BY q.exam.year ASC
      """)
  List<Object[]> countTopicByYear(@Param("topicId") Long topicId);

  /**
   * Distribuição por edição de um subassunto.
   * Retorna linhas {@code [year(Short), total(Long), annulled(Long)]}.
   */
  @Query("""
      SELECT q.exam.year, COUNT(c),
             SUM(CASE WHEN q.annulled = true THEN 1 ELSE 0 END)
      FROM QuestionClassification c JOIN c.question q
      WHERE c.subtopic.id = :subtopicId AND c.status <> 'REJECTED'
      GROUP BY q.exam.year
      ORDER BY q.exam.year ASC
      """)
  List<Object[]> countSubtopicByYear(@Param("subtopicId") Long subtopicId);

  /**
   * Confiança por assunto. Retorna linhas {@code [confidence(String), total(Long)]}.
   */
  @Query("""
      SELECT c.confidence, COUNT(c) FROM QuestionClassification c
      WHERE c.topic.id = :topicId AND c.status <> 'REJECTED'
      GROUP BY c.confidence
      """)
  List<Object[]> confidenceByTopic(@Param("topicId") Long topicId);

  /**
   * Confiança por subassunto. Retorna linhas {@code [confidence(String), total(Long)]}.
   */
  @Query("""
      SELECT c.confidence, COUNT(c) FROM QuestionClassification c
      WHERE c.subtopic.id = :subtopicId AND c.status <> 'REJECTED'
      GROUP BY c.confidence
      """)
  List<Object[]> confidenceBySubtopic(@Param("subtopicId") Long subtopicId);

  /**
   * Agregado por assunto para o panorama global.
   * Retorna {@code [topicId, total, annulled]} (anuladas contam como conteúdo).
   */
  @Query("""
      SELECT c.topic.id, COUNT(c),
             SUM(CASE WHEN q.annulled = true THEN 1 ELSE 0 END)
      FROM QuestionClassification c JOIN c.question q
      WHERE c.topic IS NOT NULL AND c.status <> 'REJECTED'
      GROUP BY c.topic.id
      """)
  List<Object[]> statsByTopic();

  /**
   * Edições em que um assunto aparece (anos distintos, crescente).
   */
  @Query("""
      SELECT DISTINCT q.exam.year FROM QuestionClassification c JOIN c.question q
      WHERE c.topic.id = :topicId AND c.status <> 'REJECTED'
      ORDER BY q.exam.year ASC
      """)
  List<Short> editionsByTopic(@Param("topicId") Long topicId);

  /**
   * Edições em que um subassunto aparece (anos distintos, crescente).
   */
  @Query("""
      SELECT DISTINCT q.exam.year FROM QuestionClassification c JOIN c.question q
      WHERE c.subtopic.id = :subtopicId AND c.status <> 'REJECTED'
      ORDER BY q.exam.year ASC
      """)
  List<Short> editionsBySubtopic(@Param("subtopicId") Long subtopicId);
}
