package br.com.voupassar.aiprova.entity;

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

/**
 * Uma geração de prova-IA (TASK A.2/A.3) — o cabeçalho; o caderno congelado
 * vive em {@link AiGenerationQuestion} e as respostas em {@link
 * AiGenerationAnswer}.
 *
 * <p>Mapeia {@code ai_generations} (DDL V23). Auditoria sem PII: só
 * formato/disciplina/tópicos saem para o provedor (TASKS.md regra 4);
 * nenhum dado do aluno além do dono. Ciclo {@code GENERATING → READY |
 * FAILED}; a A.3 persiste direto em {@code READY} (cota/concorrência são a
 * C.2). Sem FK em {@code questions} — isolamento da cadeia de evidência
 * (TASKS.md regras 2–3).
 */
@Entity
@Table(name = "ai_generations")
public class AiGeneration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "institution", nullable = false)
  private String institution;

  @Column(name = "edition_model", nullable = false)
  private String editionModel;

  @Column(name = "mode", nullable = false)
  private String mode;

  @Column(name = "prompt_version", nullable = false)
  private String promptVersion;

  @Column(name = "model", nullable = false)
  private String model;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "repair_attempts", nullable = false)
  private int repairAttempts;

  @Column(name = "failure_reason")
  private String failureReason;

  @Column(name = "frozen_at")
  private OffsetDateTime frozenAt;

  public AiGeneration() {}

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public String getEditionModel() {
    return editionModel;
  }

  public void setEditionModel(String editionModel) {
    this.editionModel = editionModel;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getPromptVersion() {
    return promptVersion;
  }

  public void setPromptVersion(String promptVersion) {
    this.promptVersion = promptVersion;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getRepairAttempts() {
    return repairAttempts;
  }

  public void setRepairAttempts(int repairAttempts) {
    this.repairAttempts = repairAttempts;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public OffsetDateTime getFrozenAt() {
    return frozenAt;
  }

  public void setFrozenAt(OffsetDateTime frozenAt) {
    this.frozenAt = frozenAt;
  }
}
