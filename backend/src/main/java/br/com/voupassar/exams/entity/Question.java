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
import java.time.OffsetDateTime;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Questão — modelo completo de leitura do banco de questões.
 *
 * <p>TASK 3.2 usava esta entidade como projeção mínima para agregados;
 * a TASK 3.4 a expande para o conteúdo integral (enunciado, alternativas via
 * {@code question_options}, gabarito, proveniência). {@code @Immutable} impede
 * updates acidentais por este contexto: a escrita acontece via migrations +
 * importador (TASK 2.3), nunca pela API.
 *
 * <p>Regras de evidência: {@code answerKey = "X"} ⟺ anulada (conta como
 * conteúdo, nunca pontua — regra de pontuação DESCONHECIDA, TASK 1.3 §4);
 * {@code difficultyEstimate} é palpite com confiança BAIXA global.
 */
@Entity
@Immutable
@Table(name = "questions")
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "source_type", nullable = false)
  private String sourceType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id")
  private Exam exam;

  @Column(name = "exam_document_id")
  private Long examDocumentId;

  @Column(name = "source_year")
  private Short sourceYear;

  @Column(name = "source_question_number")
  private Short sourceQuestionNumber;

  @Column(name = "statement", nullable = false)
  private String statement;

  @Column(name = "kind", nullable = false)
  private String kind;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "discipline_id", nullable = false)
  private Discipline discipline;

  // NULL em AUTHORAL (V13: sem documento-fonte; NULL = DESCONHECIDO).
  @Column(name = "page_start")
  private Short pageStart;

  // NULL em AUTHORAL (ver acima).
  @Column(name = "page_end")
  private Short pageEnd;

  @Column(name = "annulled", nullable = false)
  private boolean annulled;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "answer_key", nullable = false, length = 1)
  private String answerKey;

  @Column(name = "has_figure", nullable = false)
  private boolean hasFigure;

  @Column(name = "difficulty_estimate")
  private String difficultyEstimate;

  @Column(name = "pipeline_version", nullable = false)
  private String pipelineVersion;

  @Column(name = "pipeline_verified_at", nullable = false)
  private OffsetDateTime pipelineVerifiedAt;

  public Question() {}

  public Long getId() {
    return id;
  }

  public String getSourceType() {
    return sourceType;
  }

  public Exam getExam() {
    return exam;
  }

  public Long getExamDocumentId() {
    return examDocumentId;
  }

  public Short getSourceYear() {
    return sourceYear;
  }

  public Short getSourceQuestionNumber() {
    return sourceQuestionNumber;
  }

  public String getStatement() {
    return statement;
  }

  public String getKind() {
    return kind;
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

  public Short getPageStart() {
    return pageStart;
  }

  public Short getPageEnd() {
    return pageEnd;
  }

  public boolean isHasFigure() {
    return hasFigure;
  }

  public String getDifficultyEstimate() {
    return difficultyEstimate;
  }

  public String getPipelineVersion() {
    return pipelineVersion;
  }

  public OffsetDateTime getPipelineVerifiedAt() {
    return pipelineVerifiedAt;
  }
}
