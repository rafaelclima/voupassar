package br.com.voupassar.review.entity;

import br.com.voupassar.exams.entity.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Caderno congelado da sessão de revisão (TASK 5.4): posição + questão.
 *
 * <p>Mapeia {@code review_session_questions} (DDL V5). A ordem (posições
 * 1..N) é o top-N da fila da TASK 4.5 no momento da criação — determinística
 * e auditável. Sem {@code frozen_answer_key} (diferença intencional para
 * {@code simulation_questions}, TASK 5.1): a revisão responde via
 * {@code POST /attempts} com {@code mode=REVISAO}, cuja correção usa o
 * gabarito vigente (TASK 3.7); congelar gabarito aqui divergiria do
 * {@code is_correct} persistido no fato imutável.
 *
 * <p>Revisão não gera simulado (ERD §2.6): esta tabela referencia
 * {@code study_sessions} (modo {@code REVISAO}), nunca {@code simulations}.
 */
@Entity
@IdClass(ReviewSessionQuestionId.class)
@Table(name = "review_session_questions")
public class ReviewSessionQuestion {

  @Id
  @Column(name = "study_session_id", nullable = false)
  private Long studySessionId;

  @Id
  @Column(name = "position", nullable = false)
  private Short position;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  public ReviewSessionQuestion() {}

  public Long getStudySessionId() {
    return studySessionId;
  }

  public void setStudySessionId(Long studySessionId) {
    this.studySessionId = studySessionId;
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
}
