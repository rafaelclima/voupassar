package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Question;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Agregados somente-leitura para {@code GET /editions/{year}/stats} (TASK 3.2)
 * e filtro por processo (TASK E.1).
 *
 * <p>Contagens derivadas de {@code questions} (importador TASK 2.3).
 * Anuladas contam como conteúdo que apareceu na prova; regra de pontuação de
 * anuladas é DESCONHECIDA (TASK 1.3 §4) — aqui só contamos, nunca pontuamos.
 *
 * <p>Ano sozinho nunca decide a edição (EAJ-2022 ≠ IFRN-2022): os métodos
 * legados por ano seguem para compatibilidade em anos sem colisão; o caminho
 * E.1 usa sempre {@code (institution, year)}.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

  long countByExamYear(Short year);

  long countByExamYearAndAnnulledTrue(Short year);

  long countByDisciplineCode(String disciplineCode);

  long countByExamInstitutionAndExamYear(String institution, Short year);

  long countByExamInstitutionAndExamYearAndAnnulledTrue(String institution, Short year);

  /**
   * Total de questões de um processo (TASK E.2 — panorama por trilha).
   */
  long countByExamInstitution(String institution);

  /**
   * Questões de uma disciplina dentro de um processo (TASK E.2).
   *
   * <p>Factual via {@code questions.discipline_id} + {@code exams.institution}.
   * Autorais sem exame ficam fora do recorte por processo (só no global).
   */
  @Query("""
      SELECT COUNT(q) FROM Question q
      WHERE q.exam.institution = :institution AND q.discipline.code = :disciplineCode
      """)
  long countByInstitutionAndDisciplineCode(
      @Param("institution") String institution, @Param("disciplineCode") String disciplineCode);

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
   * Mesmo agregado por edição composta (TASK E.1): {@code (institution, year)}.
   *
   * <p>Retorna linhas {@code [code(String), name(String), total(Long), annulled(Long)]}.
   */
  @Query("""
      SELECT d.code, d.name, COUNT(q),
             SUM(CASE WHEN q.annulled = true THEN 1 ELSE 0 END)
      FROM Question q JOIN q.discipline d
      WHERE q.exam.institution = :institution AND q.exam.year = :year
      GROUP BY d.code, d.name
      """)
  List<Object[]> countByDisciplineForInstitution(
      @Param("institution") String institution, @Param("year") Short year);

  /**
   * Busca paginada do banco de questões (TASK 3.4 + filtro {@code institution} na E.1).
   *
   * <p>Todos os filtros são opcionais ({@code null} = sem filtro). Filtros por
   * assunto/subassunto usam {@code EXISTS} sobre as classificações vigentes
   * (maior id por questão — sem carimbo humano desde a decisão de produto
   * 2026-10-06). Ordem fixa e determinística:
   * ano-fonte crescente, número da questão, id — nunca inventar ordenação por
   * relevância sem algoritmo auditável (Fase 4).
   *
   * <p>Ano sozinho nunca decide a edição: {@code year} sem {@code institution}
   * retorna ambas as instituições daquele ano (EAJ-2022 + IFRN-2022); com
   * {@code institution} o recorte é por {@code (institution, year)}.
   */
  @Query(
      value = """
          SELECT q FROM Question q
          JOIN q.discipline d
          LEFT JOIN q.exam e
          WHERE (:disciplineCode IS NULL OR d.code = :disciplineCode)
            AND (:year IS NULL OR e.year = :year)
            AND (:institution IS NULL OR e.institution = :institution)
            AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
            AND (:sourceType IS NULL OR q.sourceType = :sourceType)
            AND (:topicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.topic.id = :topicId))
            AND (:subtopicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.subtopic.id = :subtopicId))
          ORDER BY q.sourceYear ASC NULLS LAST, q.sourceQuestionNumber ASC NULLS LAST, q.id ASC
          """,
      countQuery = """
          SELECT COUNT(q) FROM Question q
          JOIN q.discipline d
          LEFT JOIN q.exam e
          WHERE (:disciplineCode IS NULL OR d.code = :disciplineCode)
            AND (:year IS NULL OR e.year = :year)
            AND (:institution IS NULL OR e.institution = :institution)
            AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
            AND (:sourceType IS NULL OR q.sourceType = :sourceType)
            AND (:topicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.topic.id = :topicId))
            AND (:subtopicId IS NULL OR EXISTS (
              SELECT 1 FROM QuestionClassification c
              WHERE c.question = q AND c.subtopic.id = :subtopicId))
          """)
  Page<Question> search(
      @Param("disciplineCode") String disciplineCode,
      @Param("topicId") Long topicId,
      @Param("subtopicId") Long subtopicId,
      @Param("year") Short year,
      @Param("institution") String institution,
      @Param("difficulty") String difficulty,
      @Param("sourceType") String sourceType,
      Pageable pageable);

  /**
   * Candidatas ao simulado por disciplina (TASK 5.1 + filtro de origem 15.4).
   *
   * <p>Anuladas ficam fora da seleção: com {@code isCorrect} NULL quebrariam
   * o aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4). O recorte por
   * dificuldade herda a limitação da estimativa (palpite com confiança BAIXA
   * global): questões sem estimativa saem só no recorte sem filtro.
   * {@code sourceType} NULL = sem filtro de origem (caderno misto); o padrão
   * OFFICIAL é aplicado no serviço (TASK 15.4).
   * Ordem fixa por id — o sorteio sem reposição acontece no serviço.
   */
  @Query("""
      SELECT q.id FROM Question q
      JOIN q.discipline d
      WHERE d.code = :disciplineCode
        AND q.annulled = false
        AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
        AND (:sourceType IS NULL OR q.sourceType = :sourceType)
      ORDER BY q.id ASC
      """)
  List<Long> findCandidateIdsByDiscipline(
      @Param("disciplineCode") String disciplineCode,
      @Param("difficulty") String difficulty,
      @Param("sourceType") String sourceType);

  /**
   * Candidatas ao simulado por disciplina dentro de um processo (TASK E.2 —
   * wizard "Descobrir meu nível" parametrizável).
   *
   * <p>Mesmas regras da seleção global (só não-anuladas, ordem por id —
   * sorteio no serviço), mas restritas a {@code exams.institution}.
   * {@code null} aqui nunca significa global: o serviço chama o método
   * legado quando sem filtro (compatibilidade).
   */
  @Query("""
      SELECT q.id FROM Question q
      JOIN q.discipline d
      JOIN q.exam e
      WHERE d.code = :disciplineCode
        AND e.institution = :institution
        AND q.annulled = false
        AND (:difficulty IS NULL OR q.difficultyEstimate = :difficulty)
        AND (:sourceType IS NULL OR q.sourceType = :sourceType)
      ORDER BY q.id ASC
      """)
  List<Long> findCandidateIdsByDisciplineForInstitution(
      @Param("disciplineCode") String disciplineCode,
      @Param("difficulty") String difficulty,
      @Param("sourceType") String sourceType,
      @Param("institution") String institution);

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
   * Caderno integral por edição composta (TASK E.1): {@code (institution, year)}.
   *
   * <p>Estrutura dirigida pelos dados daquela edição (EAJ-2021: 50Q 15/15/12/8;
   * EAJ-2022/2025: 40Q 20/20; IFRN: 40Q 20/20): total, divisão por área e
   * discursiva lidos de {@code exams}, nunca de regra universal.
   */
  @Query("""
      SELECT q FROM Question q
      JOIN q.exam e
      WHERE e.institution = :institution AND e.year = :year
      ORDER BY q.sourceQuestionNumber ASC NULLS LAST, q.id ASC
      """)
  List<Question> findByEditionOrdered(
      @Param("institution") String institution, @Param("year") Short year);

  long countByAnnulledTrue();

  long countByHasFigureTrue();

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
