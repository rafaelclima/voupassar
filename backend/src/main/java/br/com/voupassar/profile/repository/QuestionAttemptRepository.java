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
 * <p>Só leitura neste contexto: a escrita (registro de tentativa) é a TASK
 * 3.7. Anuladas ({@code wasAnnulled=true}, {@code isCorrect} NULL) contam
 * como conteúdo respondido e nunca entram no aproveitamento — regra de
 * pontuação DESCONHECIDA (TASK 1.3 §4).
 */
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

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
