package br.com.voupassar.exams.entity;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Questão — projeção mínima somente-leitura para as estatísticas da TASK 3.2.
 *
 * <p>Mapeia só as colunas necessárias aos agregados (edição, disciplina,
 * anulada). O conteúdo integral (enunciado, alternativas, gabarito) é objeto
 * da TASK 3.4 e nunca sai nestes endpoints. {@code @Immutable} impede updates
 * acidentais por este contexto.
 */
@Entity
@Immutable
@Table(name = "questions")
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id")
  private Exam exam;

  @Column(name = "source_year")
  private Short sourceYear;

  @Column(name = "source_question_number")
  private Short sourceQuestionNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "discipline_id", nullable = false)
  private Discipline discipline;

  @Column(name = "annulled", nullable = false)
  private boolean annulled;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "answer_key", nullable = false, length = 1)
  private String answerKey;

  public Question() {}

  public Long getId() {
    return id;
  }

  public Exam getExam() {
    return exam;
  }

  public Short getSourceYear() {
    return sourceYear;
  }

  public Short getSourceQuestionNumber() {
    return sourceQuestionNumber;
  }

  public Discipline getDiscipline() {
    return discipline;
  }

  public boolean isAnnulled() {
    return annulled;
  }

  public String getAnswerKey() {
    return answerKey;
  }
}
