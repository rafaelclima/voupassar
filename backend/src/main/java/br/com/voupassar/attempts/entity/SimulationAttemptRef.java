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
import org.hibernate.annotations.Immutable;

/**
 * Referência somente-leitura a {@code simulation_attempts} (TASK 3.7).
 *
 * <p>A criação e a submissão de simulados são a Fase 5 — aqui a entidade
 * existe só para validar o vínculo opcional {@code simulationAttemptId} da
 * tentativa: existir, pertencer ao dono do token, estar {@code IN_PROGRESS}
 * e ter modo coerente. {@code @Immutable} impede escrita por este contexto.
 */
@Entity
@Immutable
@Table(name = "simulation_attempts")
public class SimulationAttemptRef {

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

  public SimulationAttemptRef() {}

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getMode() {
    return mode;
  }

  public String getStatus() {
    return status;
  }
}
