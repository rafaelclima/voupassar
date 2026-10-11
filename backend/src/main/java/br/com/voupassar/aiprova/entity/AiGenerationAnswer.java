package br.com.voupassar.aiprova.entity;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.simulations.entity.SimulationAttempt;
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
 * Resposta a questão-IA (TASK A.2/A.3) — fato próprio, nunca {@code
 * question_attempts}.
 *
 * <p>Mapeia {@code ai_generation_answers} (DDL V23). Espelho de {@code
 * question_attempts} sem o vínculo à questão oficial: a posição é resolvida
 * pela FK composta {@code (generation_id, position)} ao caderno congelado.
 * Fato imutável no banco (trigger); correção só via nova resposta — vale a
 * <b>última</b> por posição. {@code is_correct} é NOT NULL (confronto com o
 * congelado; IA nunca tem anulada).
 */
@Entity
@Table(name = "ai_generation_answers")
public class AiGenerationAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "generation_id", nullable = false)
  private Long generationId;

  @Column(name = "position", nullable = false)
  private Short position;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "simulation_attempt_id", nullable = false)
  private SimulationAttempt simulationAttempt;

  /**
   * Alternativa marcada ({@code A/B/C/D/BLANK}, onde {@code BLANK} = resposta
   * em branco, conta como erro). DDL V23 declarava {@code CHAR(1)} —
   * incompatível com {@code BLANK}; a migração V24 (TASK A.3) troca para
   * {@code TEXT} mantendo o mesmo {@code CHECK} de domínio (mesmo padrão da
   * V4 para {@code question_attempts}), por isso o mapeamento aqui é texto
   * simples.
   */
  @Column(name = "selected_option", nullable = false)
  private String selectedOption;

  @Column(name = "is_correct", nullable = false)
  private boolean correct;

  @Column(name = "time_spent_seconds")
  private Integer timeSpentSeconds;

  @Column(name = "mode", nullable = false)
  private String mode;

  @Column(name = "answered_at", nullable = false)
  private OffsetDateTime answeredAt;

  public AiGenerationAnswer() {}

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Long getGenerationId() {
    return generationId;
  }

  public void setGenerationId(Long generationId) {
    this.generationId = generationId;
  }

  public Short getPosition() {
    return position;
  }

  public void setPosition(Short position) {
    this.position = position;
  }

  public SimulationAttempt getSimulationAttempt() {
    return simulationAttempt;
  }

  public void setSimulationAttempt(SimulationAttempt simulationAttempt) {
    this.simulationAttempt = simulationAttempt;
  }

  public String getSelectedOption() {
    return selectedOption;
  }

  public void setSelectedOption(String selectedOption) {
    this.selectedOption = selectedOption;
  }

  public boolean isCorrect() {
    return correct;
  }

  public void setCorrect(boolean correct) {
    this.correct = correct;
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
