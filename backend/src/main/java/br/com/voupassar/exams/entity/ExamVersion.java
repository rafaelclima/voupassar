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
import java.time.LocalDate;

/**
 * Versão de uma edição (preliminar / definitivo / final / retificação).
 *
 * <p>Só 2022 traz preliminar + definitivo no repo (idênticos 40/40);
 * demais edições têm só o definitivo/final — divergência fora do repo é
 * NÃO CONFIRMADA (docs/gabaritos-validation.md).
 */
@Entity
@Table(name = "exam_versions")
public class ExamVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(name = "version_code", nullable = false)
  private String versionCode;

  @Column(name = "published_at")
  private LocalDate publishedAt;

  @Column(name = "note")
  private String note;

  public ExamVersion() {}

  public Long getId() {
    return id;
  }

  public Exam getExam() {
    return exam;
  }

  public String getVersionCode() {
    return versionCode;
  }

  public LocalDate getPublishedAt() {
    return publishedAt;
  }

  public String getNote() {
    return note;
  }
}
