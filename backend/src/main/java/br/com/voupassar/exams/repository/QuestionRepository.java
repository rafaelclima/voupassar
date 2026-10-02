package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Question;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

  /**
   * Candidatas ao simulado por disciplina (TASK 5.1).
   *
   * <p>Anuladas ficam fora da seleção: com {@code isCorrect} NULL quebrariam
   * o aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4). O recorte por
   * dificuldade herda a limitação da estimativa (palpite com confiança BAIXA
   * global): questões sem estimativa saem só no recorte sem filtro.
   * Ordem fixa por id — o sorteio sem reposição acontece no serviço.
   */
  @Query("""
      SELECT q.id FROM Question q
      JOIN q.discipline d
      WHERE d.code = :disciplineCode
        AND q.annulled = false
        AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
      ORDER BY q.id ASC
      """)
  List<Long> findCandidateIdsByDiscipline(
      @Param("disciplineCode") String disciplineCode,
      @Param("difficulty") String difficulty);

  /**
   * Caderno integral de uma edição real (TASK 5.5).
   *
   * <p>Difere da seleção por disciplina (TASK 5.1): aqui não há sorteio nem
   * filtro de dificuldade, e as anuladas <b>participam</b> nas posições
   * originais (fidelidade à prova; ficam fora do aproveitamento, pontuação
   * DESCONHECIDA, TASK 1.3 §4). Ordem canônica = número da questão na edição.
   */
  @Query("""
      SELECT q FROM Question q
      JOIN q.exam e
      WHERE e.year = :year
      ORDER BY q.sourceQuestionNumber ASC NULLS LAST, q.id ASC
      """)
  List<Question> findByEditionYearOrdered(@Param("year") Short year);

  /**
   * Fila de curadoria (TASK 12.1): questões por status de validação.
   *
   * <p>Opcionalmente também pelo status da classificação ativa
   * ({@code classificationStatus} = {@code null} = sem filtro) — é o que
   * permite ao curador achar, por exemplo, as 39 classificações em REVIEWED
   * espalhadas pelas 240 questões. O filtro é por {@code EXISTS} sobre
   * {@code question_classifications}, então a paginação e a contagem seguem
   * o conjunto filtrado (nunca pós-paginação, que mentiria no total).
   *
   * <p>Ordenação determinística pela chamadora (ano-fonte, número, id).
   * Entidade segue {@code @Immutable} para leitura; a escrita da curadoria
   * usa {@link #updateStatuses} abaixo (UPDATE dirigido, auditável).
   */
  @Query(
      value = """
          SELECT q FROM Question q
          WHERE q.validationStatus = :validationStatus
            AND (:classificationStatus IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.status = :classificationStatus))
          """,
      countQuery = """
          SELECT COUNT(q) FROM Question q
          WHERE q.validationStatus = :validationStatus
            AND (:classificationStatus IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.status = :classificationStatus))
          """)
  Page<Question> findByValidationStatus(
      @Param("validationStatus") String validationStatus,
      @Param("classificationStatus") String classificationStatus,
      Pageable pageable);

  long countByValidationStatus(String validationStatus);

  long countByPublicationStatus(String publicationStatus);

  long countByAnnulledTrue();

  long countByHasFigureTrue();

  /**
   * Escrita da curadoria (TASK 12.1): atualiza os dois status sem carregar a
   * entidade imutável. Retorna linhas afetadas (0 = questão inexistente).
   */
  @Modifying
  @Query(
      value =
          "UPDATE questions SET validation_status = :validationStatus,"
              + " publication_status = :publicationStatus WHERE id = :id",
      nativeQuery = true)
  int updateStatuses(
      @Param("id") long id,
      @Param("validationStatus") String validationStatus,
      @Param("publicationStatus") String publicationStatus);

  /**
   * Questões objetivas com contagem de alternativas diferente de 4
   * (inconsistência — TASK 12.1). Retorna ids em ordem crescente.
   */
  @Query("""
      SELECT q.id FROM Question q
      WHERE (SELECT COUNT(o) FROM QuestionOption o WHERE o.question = q) <> 4
      ORDER BY q.id ASC
      """)
  List<Long> findIdsWithOptionCountMismatch();

  /**
   * Não-anuladas cuja resposta não está entre as alternativas
   * (inconsistência — TASK 12.1).
   */
  @Query("""
      SELECT q.id FROM Question q
      WHERE q.annulled = false
        AND NOT EXISTS (
          SELECT 1 FROM QuestionOption o WHERE o.question = q AND o.label = q.answerKey)
      ORDER BY q.id ASC
      """)
  List<Long> findIdsWithAnswerNotInOptions();

  /**
   * Enunciados vazios ou só-espaço (inconsistência — TASK 12.1).
   */
  @Query("SELECT q.id FROM Question q WHERE TRIM(BOTH FROM q.statement) = '' ORDER BY q.id ASC")
  List<Long> findIdsWithEmptyStatement();
}
