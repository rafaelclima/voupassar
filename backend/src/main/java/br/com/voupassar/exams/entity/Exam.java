package br.com.voupassar.exams.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Edição de prova (TASK 3.2).
 *
 * <p>Leitura da tabela {@code exams} (DDL TASK 2.2, seed V2).
 * Entidade somente-leitura neste contexto: a escrita acontece apenas via
 * migrations e importador (TASK 2.3), nunca pela API.
 */
@Entity
@Table(name = "exams")
public class Exam {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Ano da edição. Coluna {@code year SMALLINT} (2021 ausente — jamais inserir sem fonte). */
  @Column(name = "year", nullable = false, unique = true)
  private Short year;

  @Column(name = "edital", nullable = false)
  private String edital;

  @Column(name = "duration_minutes", nullable = false)
  private Short durationMinutes;

  @Column(name = "objective_count", nullable = false)
  private Short objectiveCount;

  @Column(name = "lp_count", nullable = false)
  private Short lpCount;

  @Column(name = "mat_count", nullable = false)
  private Short matCount;

  @Column(name = "has_essay", nullable = false)
  private Boolean hasEssay;

  /**
   * Regra de pontuação. NULL = DESCONHECIDA (an uladas + discursiva, TASK 1.3 §4).
   */
  @Column(name = "scoring_rule")
  private String scoringRule;

  public Exam() {}

  public Long getId() {
    return id;
  }

  public Short getYear() {
    return year;
  }

  public String getEdital() {
    return edital;
  }

  public Short getDurationMinutes() {
    return durationMinutes;
  }

  public Short getObjectiveCount() {
    return objectiveCount;
  }

  public Short getLpCount() {
    return lpCount;
  }

  public Short getMatCount() {
    return matCount;
  }

  public Boolean getHasEssay() {
    return hasEssay;
  }

  public String getScoringRule() {
    return scoringRule;
  }
}
