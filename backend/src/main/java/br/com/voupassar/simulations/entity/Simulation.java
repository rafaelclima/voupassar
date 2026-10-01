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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Definição de simulado (TASK 5.1) — a configuração salva, não a execução.
 *
 * <p>Mapeia {@code simulations} (DDL V1). A TASK 5.1 cria só o tipo {@code
 * BY_DISCIPLINE} ({@code exam_id} NULL); {@code REAL_EDITION} é a TASK 5.5 e
 * nunca é gerado aqui. Cada execução do aluno vive em {@link
 * SimulationAttempt} com o caderno congelado em {@link SimulationQuestion}.
 *
 * <p>Regras de evidência: {@code title} e {@code filter_json} são gerados
 * deterministicamente dos parâmetros validados (disciplina, quantidade,
 * dificuldade, modo) — nunca inventados; {@code filter_json} é auditável
 * (reproduz os critérios contra o banco disponível).
 */
@Entity
@Table(name = "simulations")
public class Simulation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_user_id")
  private User owner;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "exam_id")
  private Long examId;

  @Column(name = "filter_json", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String filterJson;

  @Column(name = "title", nullable = false)
  private String title;

  public Simulation() {}

  public Long getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Long getExamId() {
    return examId;
  }

  public void setExamId(Long examId) {
    this.examId = examId;
  }

  public String getFilterJson() {
    return filterJson;
  }

  public void setFilterJson(String filterJson) {
    this.filterJson = filterJson;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
