package br.com.voupassar.exams.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Prompt da produção textual de uma edição (1:1 com {@code exams}).
 *
 * <p>Só proposta + critérios impressos no caderno. Correção automática da
 * discursiva = DESCONHECIDA / fora do MVP.
 */
@Entity
@Table(name = "exam_essay_prompts")
public class ExamEssayPrompt {

  /** PK compartilhada com {@code exams.id}. */
  @Id
  @Column(name = "exam_id")
  private Long examId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", insertable = false, updatable = false)
  private Exam exam;

  @Column(name = "genre", nullable = false)
  private String genre;

  @Column(name = "theme", nullable = false)
  private String theme;

  @Column(name = "pseudonym", nullable = false)
  private String pseudonym;

  @Column(name = "proposal_excerpt", nullable = false)
  private String proposalExcerpt;

  @Column(name = "criteria_text")
  private String criteriaText;

  @Column(name = "page", nullable = false)
  private Short page;

  public ExamEssayPrompt() {}

  public Long getExamId() {
    return examId;
  }

  public Exam getExam() {
    return exam;
  }

  public String getGenre() {
    return genre;
  }

  public String getTheme() {
    return theme;
  }

  public String getPseudonym() {
    return pseudonym;
  }

  public String getProposalExcerpt() {
    return proposalExcerpt;
  }

  public String getCriteriaText() {
    return criteriaText;
  }

  public Short getPage() {
    return page;
  }
}
