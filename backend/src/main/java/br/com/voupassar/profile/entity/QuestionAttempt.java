package br.com.voupassar.profile.entity;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.exams.entity.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Tentativa de resposta (TASK 3.6 — leitura; escrita na TASK 3.7).
 *
 * <p>Leitura da tabela {@code question_attempts} (DDL TASK 2.2, V1). Fato
 * imutável por resposta (trigger {@code trg_attempts_immutable}): a API nunca
 * atualiza nem apaga linha aqui; correção só via nova tentativa.
 *
 * <p>Regras de evidência: {@code isCorrect} NULL quando {@code wasAnnulled}
 * (pontuação de anuladas DESCONHECIDA, TASK 1.3 §4) — anuladas contam como
 * conteúdo respondido, nunca entram no cálculo de aproveitamento.
 */
@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(name = "study_session_id")
  private Long studySessionId;

  @Column(name = "simulation_attempt_id")
  private Long simulationAttemptId;

  /**
   * Alternativa marcada. DDL V1 declara {@code CHAR(1)} (mesmo padrão de
   * {@code questions.answer_key} na TASK 3.4) — por isso o mapeamento usa
   * {@code CHAR} para passar no {@code ddl-auto: validate}.
   *
   * <p><b>Limitação conhecida da DDL (NECESSITA REVISÃO na TASK 3.7):</b> o
   * {@code CHECK} admite {@code 'BLANK'} (5 chars), mas {@code CHAR(1)} não
   * comporta esse valor no Postgres — a escrita de {@code BLANK} precisa de
   * migração corretiva antes da TASK 3.7.
   */
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "selected_option", nullable = false, length = 1)
  private String selectedOption;

  /** NULL quando anulada (ver {@code wasAnnulled}). */
  @Column(name = "is_correct")
  private Boolean correct;

  @Column(name = "was_annulled", nullable = false)
  private boolean annulled;

  @Column(name = "time_spent_seconds")
  private Integer timeSpentSeconds;

  @Column(name = "mode", nullable = false)
  private String mode;

  @Column(name = "answered_at", nullable = false)
  private OffsetDateTime answeredAt;

  public QuestionAttempt() {}

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public Question getQuestion() {
    return question;
  }

  public Long getStudySessionId() {
    return studySessionId;
  }

  public Long getSimulationAttemptId() {
    return simulationAttemptId;
  }

  public String getSelectedOption() {
    return selectedOption;
  }

  public Boolean getCorrect() {
    return correct;
  }

  public boolean isAnnulled() {
    return annulled;
  }

  public Integer getTimeSpentSeconds() {
    return timeSpentSeconds;
  }

  public String getMode() {
    return mode;
  }

  public OffsetDateTime getAnsweredAt() {
    return answeredAt;
  }
}
