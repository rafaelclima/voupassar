package br.com.voupassar.attempts.entity;

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
 * Sessão de estudo (TASK 3.7 — agrupador mínimo de tentativas).
 *
 * <p>Mapeia {@code study_sessions} (DDL V1): um bloco de estudo do aluno num
 * modo ({@code ESTUDO/PROVA/REVISAO}) com ciclo {@code
 * IN_PROGRESS → FINISHED/ABANDONED}. A V1 da API oferece só abrir, consultar
 * e encerrar — pausar/retomar e o vínculo com simulados completos ficam para
 * a Fase 5. Tentativas exigem sessão (ou simulado) por {@code CHECK} do ERD
 * §5 item 6: resposta órfã é proibida.
 */
@Entity
@Table(name = "study_sessions")
public class StudySession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "mode", nullable = false)
  private String mode;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "finished_at")
  private OffsetDateTime finishedAt;

  public StudySession() {}

  public Long getId() {
    return id;
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

  public OffsetDateTime getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(OffsetDateTime finishedAt) {
    this.finishedAt = finishedAt;
  }
}
