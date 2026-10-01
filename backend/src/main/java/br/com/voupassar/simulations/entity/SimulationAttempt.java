package br.com.voupassar.simulations.entity;

import br.com.voupassar.auth.entity.User;
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
 * Uma execução de simulado por usuário (TASK 5.1) — o cabeçalho; o detalhe
 * (respostas) vive no fato imutável {@code question_attempts} (TASK 3.7) e o
 * caderno congelado em {@link SimulationQuestion}.
 *
 * <p>Mapeia {@code simulation_attempts} (DDL V1) com escrita. A entidade
 * {@code SimulationAttemptRef} (pacote {@code attempts}) continua existindo
 * como visão somente-leitura para validar o vínculo da tentativa (TASK 3.7) —
 * duas entidades sobre a mesma tabela, sem conflito: esta escreve, aquela só
 * lê existência/posse/status/modo.
 *
 * <p>Ciclo: {@code IN_PROGRESS → SUBMITTED} (concluir) ou {@code IN_PROGRESS →
 * ABANDONED} (desistir). Não existe estado de pausa no DDL: "pausar" é manter
 * {@code IN_PROGRESS} e retomar via {@code GET} — documentado na API.
 */
@Entity
@Table(name = "simulation_attempts")
public class SimulationAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "simulation_id", nullable = false)
  private Simulation simulation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "mode", nullable = false)
  private String mode;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "submitted_at")
  private OffsetDateTime submittedAt;

  @Column(name = "score_json")
  @JdbcTypeCode(SqlTypes.JSON)
  private String scoreJson;

  public SimulationAttempt() {}

  public Long getId() {
    return id;
  }

  public Simulation getSimulation() {
    return simulation;
  }

  public void setSimulation(Simulation simulation) {
    this.simulation = simulation;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public OffsetDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(OffsetDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public String getScoreJson() {
    return scoreJson;
  }

  public void setScoreJson(String scoreJson) {
    this.scoreJson = scoreJson;
  }
}
