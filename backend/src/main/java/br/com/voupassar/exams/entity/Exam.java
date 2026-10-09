package br.com.voupassar.exams.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Edição de prova (TASK 3.2; dimensão {@code institution} na TASK C.1).
 *
 * <p>Leitura da tabela {@code exams} (DDL TASK 2.2, seed V2, V17 EAJ).
 * Entidade somente-leitura neste contexto: a escrita acontece apenas via
 * migrations e importador (TASK 2.3), nunca pela API.
 *
 * <p>Ano sozinho nunca identifica a edição: a unicidade é
 * {@code (institution, year)} (EAJ-2022 ≠ IFRN-2022, EAJ-2025 ≠ IFRN-2025).
 * O ano 2021 só existe com {@code institution = 'EAJ'} (EAJ-2021, 50Q/4
 * áreas); IFRN-2021 segue ausente do dataset (AGENTS.md §3).
 */
@Entity
@Table(
    name = "exams",
    uniqueConstraints =
        @UniqueConstraint(name = "uq_exams_institution_year", columnNames = {"institution", "year"}))
public class Exam {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Processo seletivo da edição: IFRN ou EAJ (V17; backfill 'IFRN' nas 6
   * edições existentes; EAJ semeado na C.3).
   */
  @Column(name = "institution", nullable = false)
  private String institution = "IFRN";

  /** Ano da edição. 2021 só com {@code institution = 'EAJ'} (IFRN-2021 ausente). */
  @Column(name = "year", nullable = false)
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

  /**
   * Contagens por área de Ciências da Natureza / Ciências Humanas daquela
   * edição (V17; 0 nas edições IFRN — só LP/MAT; disciplinas CN/CH na C.2).
   */
  @Column(name = "cn_count", nullable = false)
  private Short cnCount = 0;

  @Column(name = "ch_count", nullable = false)
  private Short chCount = 0;

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

  public String getInstitution() {
    return institution;
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

  public Short getCnCount() {
    return cnCount;
  }

  public Short getChCount() {
    return chCount;
  }

  public Boolean getHasEssay() {
    return hasEssay;
  }

  public String getScoringRule() {
    return scoringRule;
  }
}
