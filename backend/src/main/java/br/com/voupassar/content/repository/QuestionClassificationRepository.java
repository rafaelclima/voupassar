package br.com.voupassar.content.repository;

import br.com.voupassar.content.entity.QuestionClassification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Agregados históricos sobre {@code question_classifications} (TASK 3.3).
 *
 * <p>Contam classificações com tópico presente (sem filtro de status desde
 * a decisão de produto 2026-10-06 — vigente é o maior id por questão).
 * Anuladas ({@code X} no gabarito) contam como conteúdo que
 * apareceu na prova — aqui só contamos, nunca pontuamos (regra de pontuação
 * DESCONHECIDA, TASK 1.3 §4).
 */
public interface QuestionClassificationRepository
    extends JpaRepository<QuestionClassification, Long> {

  /** Total de questões classificadas (tópico presente). */
  @Query("""
      SELECT COUNT(c) FROM QuestionClassification c
      WHERE c.topic IS NOT NULL 
      """)
  long countClassified();

  /** Versões de taxonomia presentes no banco (ex. {@code ["v1.1"]}). */
  @Query("SELECT DISTINCT c.taxonomyVersion FROM QuestionClassification c ORDER BY c.taxonomyVersion")
  List<String> distinctTaxonomyVersions();

  /** Retorna linhas {@code [topicId(Long), total(Long)]}. */
  @Query("""
      SELECT c.topic.id, COUNT(c) FROM QuestionClassification c
      WHERE c.topic IS NOT NULL 
      GROUP BY c.topic.id
      """)
  List<Object[]> countByTopic();

  /** Retorna linhas {@code [subtopicId(Long), total(Long)]}. */
  @Query("""
      SELECT c.subtopic.id, COUNT(c) FROM QuestionClassification c
      WHERE c.subtopic IS NOT NULL 
      GROUP BY c.subtopic.id
      """)
  List<Object[]> countBySubtopic();

  @Query("""
      SELECT COUNT(c) FROM QuestionClassification c
      WHERE c.topic.id = :topicId 
      """)
  long countByTopicId(@Param("topicId") Long topicId);

  @Query("""
      SELECT COUNT(c) FROM QuestionClassification c
      WHERE c.subtopic.id = :subtopicId 
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
      WHERE c.topic.id = :topicId 
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
      WHERE c.subtopic.id = :subtopicId 
      GROUP BY q.exam.year
      ORDER BY q.exam.year ASC
      """)
  List<Object[]> countSubtopicByYear(@Param("subtopicId") Long subtopicId);

  /**
   * Confiança por assunto. Retorna linhas {@code [confidence(String), total(Long)]}.
   */
  @Query("""
      SELECT c.confidence, COUNT(c) FROM QuestionClassification c
      WHERE c.topic.id = :topicId 
      GROUP BY c.confidence
      """)
  List<Object[]> confidenceByTopic(@Param("topicId") Long topicId);

  /**
   * Confiança por subassunto. Retorna linhas {@code [confidence(String), total(Long)]}.
   */
  @Query("""
      SELECT c.confidence, COUNT(c) FROM QuestionClassification c
      WHERE c.subtopic.id = :subtopicId 
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
      WHERE c.topic IS NOT NULL 
      GROUP BY c.topic.id
      """)
  List<Object[]> statsByTopic();

  /**
   * Edições em que um assunto aparece (anos distintos, crescente).
   */
  @Query("""
      SELECT DISTINCT q.exam.year FROM QuestionClassification c JOIN c.question q
      WHERE c.topic.id = :topicId 
      ORDER BY q.exam.year ASC
      """)
  List<Short> editionsByTopic(@Param("topicId") Long topicId);

  /**
   * Edições em que um subassunto aparece (anos distintos, crescente).
   */
  @Query("""
      SELECT DISTINCT q.exam.year FROM QuestionClassification c JOIN c.question q
      WHERE c.subtopic.id = :subtopicId 
      ORDER BY q.exam.year ASC
      """)
  List<Short> editionsBySubtopic(@Param("subtopicId") Long subtopicId);

  /**
   * Dificuldade por assunto (TASK 18.1) — mesma fonte das frequências.
   * Retorna linhas {@code [topicId(Long), difficultyEstimate(String, NULLável), total(Long)]}.
   *
   * <p>{@code difficulty_estimate} é palpite global BAIXA
   * ({@code Question.java:28}, {@code database-erd.md} §2.4) — o serviço
   * trata tópico sem sinal como neutro e documenta o peso.
   */
  @Query("""
      SELECT c.topic.id, q.difficultyEstimate, COUNT(c)
      FROM QuestionClassification c JOIN c.question q
      WHERE c.topic IS NOT NULL
      GROUP BY c.topic.id, q.difficultyEstimate
      """)
  List<Object[]> difficultyByTopic();

  /**
   * IDs de questões OFICIAIS de um assunto, ordem determinística
   * (ano, número na edição, id) — fonte da evidência do roteiro (TASK 18.2).
   *
   * <p>Só `source_type = 'OFFICIAL'`: autorais/adaptadas nunca produzem
   * evidência (regra Fase 15, AGENTS.md §11).
   */
  @Query("""
      SELECT q.id FROM QuestionClassification c JOIN c.question q
      WHERE c.topic.id = :topicId AND q.sourceType = 'OFFICIAL'
      ORDER BY q.sourceYear ASC NULLS LAST, q.sourceQuestionNumber ASC NULLS LAST, q.id ASC
      """)
  List<Long> officialQuestionIdsByTopic(@Param("topicId") Long topicId);

  /**
   * Classificações vigentes de um lote de questões (TASK 3.4).
   *
   * <p>Traz tópico + subassunto em fetch para montar a página sem N+1. Pode
   * haver mais de uma por questão (histórico); o serviço escolhe a
   * mais recente (maior id).
   */
  @Query("""
      SELECT c FROM QuestionClassification c
      LEFT JOIN FETCH c.topic t
      LEFT JOIN FETCH c.subtopic s
      WHERE c.question.id IN :ids 
      ORDER BY c.question.id ASC, c.id DESC
      """)
  List<QuestionClassification> findActiveByQuestionIds(@Param("ids") List<Long> ids);

  /**
   * Classificações vigentes de uma questão, mais recente primeiro.
   */
  @Query("""
      SELECT c FROM QuestionClassification c
      LEFT JOIN FETCH c.topic t
      LEFT JOIN FETCH c.subtopic s
      WHERE c.question.id = :questionId 
      ORDER BY c.id DESC
      """)
  List<QuestionClassification> findActiveByQuestionId(@Param("questionId") Long questionId);

  /**
   * Classificações ativas sem tópico (inconsistência — observabilidade).
   */
  @Query("""
      SELECT c.id FROM QuestionClassification c
      WHERE c.topic IS NULL 
      ORDER BY c.id ASC
      """)
  List<Long> findIdsWithoutTopic();
}
