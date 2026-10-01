package br.com.voupassar.simulations.entity;

import br.com.voupassar.exams.entity.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Caderno congelado da tentativa (TASK 5.1): posição + questão + gabarito
 * vigente no momento da criação.
 *
 * <p>Mapeia {@code simulation_questions} (DDL V1). {@code frozen_answer_key}
 * protege a correção contra reclassificação posterior da questão: o resultado
 * do simulado usa o fato {@code question_attempts} (resposta do aluno)
 * confrontado com este gabarito congelado, nunca com o gabarito "atual".
 */
@Entity
@IdClass(SimulationQuestionId.class)
@Table(name = "simulation_questions")
public class SimulationQuestion {

  @Id
  @Column(name = "simulation_attempt_id", nullable = false)
  private Long simulationAttemptId;

  @Id
  @Column(name = "position", nullable = false)
  private Short position;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "frozen_answer_key", nullable = false, length = 1)
  private String frozenAnswerKey;

  public SimulationQuestion() {}

  public Long getSimulationAttemptId() {
    return simulationAttemptId;
  }

  public void setSimulationAttemptId(Long simulationAttemptId) {
    this.simulationAttemptId = simulationAttemptId;
  }

  public Short getPosition() {
    return position;
  }

  public void setPosition(Short position) {
    this.position = position;
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public String getFrozenAnswerKey() {
    return frozenAnswerKey;
  }

  public void setFrozenAnswerKey(String frozenAnswerKey) {
    this.frozenAnswerKey = frozenAnswerKey;
  }
}
