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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Documento-fonte de uma edição (caderno, gabarito, ofertas).
 *
 * <p>{@code file_name} é o nome literal no repo (ex. o gabarito cujo nome diz
 * 2024 mas o conteúdo é 2025 — preservar literal + nota, nunca renomear
 * silenciosamente). {@code sha256} é a identidade auditável
 * (docs/gabaritos-validation.md §0).
 */
@Entity
@Table(name = "exam_documents")
public class ExamDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exam_version_id", nullable = false)
  private ExamVersion version;

  @Column(name = "kind", nullable = false)
  private String kind;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "sha256", nullable = false, unique = true, length = 64)
  private String sha256;

  @Column(name = "pages", nullable = false)
  private Short pages;

  @Column(name = "generator")
  private String generator;

  @Column(name = "note")
  private String note;

  public ExamDocument() {}

  public Long getId() {
    return id;
  }

  public ExamVersion getVersion() {
    return version;
  }

  public String getKind() {
    return kind;
  }

  public String getFileName() {
    return fileName;
  }

  public String getSha256() {
    return sha256;
  }

  public Short getPages() {
    return pages;
  }

  public String getGenerator() {
    return generator;
  }

  public String getNote() {
    return note;
  }
}
