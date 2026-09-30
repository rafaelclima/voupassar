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

/**
 * Tentativa de resposta (TASK 3.6 — leitura; TASK 3.7 — escrita).
 *
 * <p>Fato imutável por resposta (trigger {@code trg_attempts_immutable}): a
 * API nunca atualiza nem apaga linha aqui; correção só via nova tentativa.
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
   * Alternativa marcada ({@code A/B/C/D/BLANK}, onde {@code BLANK} = resposta
   * em branco). DDL V1 declarava {@code CHAR(1)} — incompatível com {@code
   * BLANK}; a migração V4 (TASK 3.7) troca para {@code TEXT} mantendo o mesmo
   * {@code CHECK} de domínio, por isso o mapeamento aqui é texto simples.
   */
  @Column(name = "selected_option", nullable = false)
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

  public void setUser(User user) {
    this.user = user;
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public Long getStudySessionId() {
    return studySessionId;
  }

  public void setStudySessionId(Long studySessionId) {
    this.studySessionId = studySessionId;
  }

  public Long getSimulationAttemptId() {
    return simulationAttemptId;
  }

  public void setSimulationAttemptId(Long simulationAttemptId) {
    this.simulationAttemptId = simulationAttemptId;
  }

  public String getSelectedOption() {
    return selectedOption;
  }

  public void setSelectedOption(String selectedOption) {
    this.selectedOption = selectedOption;
  }

  public Boolean getCorrect() {
    return correct;
  }

  public void setCorrect(Boolean correct) {
    this.correct = correct;
  }

  public boolean isAnnulled() {
    return annulled;
  }

  public void setAnnulled(boolean annulled) {
    this.annulled = annulled;
  }

  public Integer getTimeSpentSeconds() {
    return timeSpentSeconds;
  }

  public void setTimeSpentSeconds(Integer timeSpentSeconds) {
    this.timeSpentSeconds = timeSpentSeconds;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public OffsetDateTime getAnsweredAt() {
    return answeredAt;
  }

  public void setAnsweredAt(OffsetDateTime answeredAt) {
    this.answeredAt = answeredAt;
  }
}
