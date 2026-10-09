package br.com.voupassar.profile.repository;

import br.com.voupassar.profile.entity.QuestionAttempt;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Leituras do fato de respostas por aluno (TASK 3.6).
 *
 * <p>Leituras agregadas e histórico são a TASK 3.6; a escrita (registro de
 * tentativa) e a leitura unitária escopada ao dono são a TASK 3.7. Anuladas
 * ({@code wasAnnulled=true}, {@code isCorrect} NULL) contam como conteúdo
 * respondido e nunca entram no aproveitamento — regra de pontuação
 * DESCONHECIDA (TASK 1.3 §4).
 */
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

  /** Tentativa do dono do token (TASK 3.7 — nunca vaza tentativa alheia). */
  Optional<QuestionAttempt> findByIdAndUserId(Long id, Long userId);

  long countByUserId(Long userId);

  /** Tentativas pontuáveis (não-anuladas). */
  long countByUserIdAndAnnulledFalse(Long userId);

  /** Acertos pontuáveis (não-anulada + correta). */
  long countByUserIdAndAnnulledFalseAndCorrectTrue(Long userId);

  /** Anuladas respondidas (contam como conteúdo, sem pontuar). */
  long countByUserIdAndAnnulledTrue(Long userId);

  Optional<QuestionAttempt> findTopByUserIdOrderByAnsweredAtDescIdDesc(Long userId);

  /**
   * Aproveitamento por modo. Linhas {@code [mode(String), total(Long),
   * scored(Long), correct(Long), annulled(Long)]}.
   */
  @Query("""
      SELECT a.mode, COUNT(a),
             SUM(CASE WHEN a.annulled = false THEN 1 ELSE 0 END),
             SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END),
             SUM(CASE WHEN a.annulled = true THEN 1 ELSE 0 END)
      FROM QuestionAttempt a
      WHERE a.user.id = :userId
      GROUP BY a.mode
      """)
  List<Object[]> statsByMode(@Param("userId") Long userId);

  /**
   * Aproveitamento por disciplina (via questão). Linhas {@code [code(String),
   * name(String), total(Long), scored(Long), correct(Long),
   * annulled(Long)]}, ordem de código.
   */
  @Query("""
      SELECT d.code, d.name, COUNT(a),
             SUM(CASE WHEN a.annulled = false THEN 1 ELSE 0 END),
             SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END),
             SUM(CASE WHEN a.annulled = true THEN 1 ELSE 0 END)
      FROM QuestionAttempt a JOIN a.question q JOIN q.discipline d
      WHERE a.user.id = :userId
      GROUP BY d.code, d.name
      ORDER BY d.code ASC
      """)
  List<Object[]> statsByDiscipline(@Param("userId") Long userId);

  /** Última atividade do aluno (NULL quando sem tentativas). */
  @Query("SELECT MAX(a.answeredAt) FROM QuestionAttempt a WHERE a.user.id = :userId")
  Optional<OffsetDateTime> lastAttemptAt(@Param("userId") Long userId);

  /**
   * Todas as tentativas do aluno com questão + disciplina em fetch (TASK 4.1).
   *
   * <p>Base do cálculo de desempenho determinístico (overview, evolução e
   * rebuild do agregado): uma única leitura ordenada cronologicamente, sem
   * N+1. A classificação vigente (tópico/subassunto) é resolvida em seguida
   * via {@code QuestionClassificationRepository#findActiveByQuestionIds}.
   */
  @Query("""
      SELECT a FROM QuestionAttempt a
      LEFT JOIN FETCH a.question q
      LEFT JOIN FETCH q.discipline d
      WHERE a.user.id = :userId
      ORDER BY a.answeredAt ASC, a.id ASC
      """)
  List<QuestionAttempt> findAllByUserIdWithQuestion(@Param("userId") Long userId);

  /**
   * Todas as tentativas com questão + disciplina + edição em fetch (TASK E.2).
   *
   * <p>Base dos recortes por processo (diagnóstico/recomendação/revisão na
   * trilha EAJ): o serviço filtra em memória por
   * {@code question.exam.institution} (autorais sem exame contam em ambas as
   * trilhas — não-oficiais, sem frequência histórica). Fetch de {@code exam}
   * evita N+1 ao classificar a trilha.
   */
  @Query("""
      SELECT a FROM QuestionAttempt a
      LEFT JOIN FETCH a.question q
      LEFT JOIN FETCH q.discipline d
      LEFT JOIN FETCH q.exam e
      WHERE a.user.id = :userId
      ORDER BY a.answeredAt ASC, a.id ASC
      """)
  List<QuestionAttempt> findAllByUserIdWithQuestionAndExam(@Param("userId") Long userId);

  /**
   * Tentativas pontuáveis (não-anuladas) de um processo (TASK E.2 —
   * limiar PROVISORIO/PESSOAL por trilha).
   *
   * <p>Autorais (sem exame) contam em ambas as trilhas (compatibilidade com
   * o fato global legado).
   */
  @Query("""
      SELECT COUNT(a) FROM QuestionAttempt a
      JOIN a.question q LEFT JOIN q.exam e
      WHERE a.user.id = :userId AND a.annulled = false
        AND (e.institution = :institution OR e IS NULL)
      """)
  long countScoredByUserIdAndInstitution(
      @Param("userId") Long userId, @Param("institution") String institution);

  /**
   * Tentativas do aluno vinculadas a uma execução de simulado (TASK 5.1).
   *
   * <p>Base do placar do simulado: o resultado considera a <b>última</b>
   * tentativa por questão (maior {@code answeredAt}, desempate por maior
   * {@code id}) — o fato é imutável, correção só via nova tentativa.
   */
  List<QuestionAttempt> findBySimulationAttemptIdAndUserId(Long simulationAttemptId, Long userId);

  /**
   * Tentativas do aluno vinculadas a uma sessão de estudo (TASK 5.4).
   *
   * <p>Base do progresso e do resultado da sessão de revisão: vale a
   * <b>última</b> tentativa por questão (maior {@code answeredAt}, desempate
   * por maior {@code id}) — o fato é imutável, correção só via nova tentativa.
   */
  List<QuestionAttempt> findByStudySessionIdAndUserId(Long studySessionId, Long userId);

  /**
   * Histórico paginado (mais recentes primeiro), com questão + disciplina em
   * fetch para montar a página sem N+1.
   */
  @Query(
      value = """
          SELECT a FROM QuestionAttempt a
          LEFT JOIN FETCH a.question q
          LEFT JOIN FETCH q.discipline d
          WHERE a.user.id = :userId
          ORDER BY a.answeredAt DESC, a.id DESC
          """,
      countQuery = "SELECT COUNT(a) FROM QuestionAttempt a WHERE a.user.id = :userId")
  Page<QuestionAttempt> findHistory(@Param("userId") Long userId, Pageable pageable);
}
