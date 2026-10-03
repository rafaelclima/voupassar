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

/**
 * Texto-base compartilhado (TASK 6.9) — leitura da tabela {@code passages}
 * (DDL V9, carga via {@code scripts/db/import_passages.py}).
 *
 * <p>Transcrição literal do caderno oficial (nunca parafraseada); quando o
 * conteúdo é puramente visual, {@code content} é NULL e {@code
 * visualDescription} descreve o que a curadoria viu no PDF-fonte. Sem gate
 * de publicação (decisão em docs/passagens-estrategia.md §1): só
 * proveniência (edição, páginas, nota de fonte) para rastreabilidade.
 * {@code @Immutable} impede updates acidentais por este contexto.
 */
@Entity
@Immutable
@Table(name = "passages")
public class Passage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(name = "passage_key", nullable = false)
  private String passageKey;

  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "kind", nullable = false)
  private String kind;

  @Column(name = "title")
  private String title;

  @Column(name = "byline")
  private String byline;

  @Column(name = "subtitle")
  private String subtitle;

  @Column(name = "intro")
  private String intro;

  @Column(name = "content")
  private String content;

  @Column(name = "visual_description")
  private String visualDescription;

  @Column(name = "format_note")
  private String formatNote;

  @Column(name = "source_note")
  private String sourceNote;

  @Column(name = "page_start", nullable = false)
  private Short pageStart;

  @Column(name = "page_end", nullable = false)
  private Short pageEnd;

  public Passage() {}

  public Long getId() {
    return id;
  }

  public String getPassageKey() {
    return passageKey;
  }

  public String getLabel() {
    return label;
  }

  public String getKind() {
    return kind;
  }

  public String getTitle() {
    return title;
  }

  public String getByline() {
    return byline;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getIntro() {
    return intro;
  }

  public String getContent() {
    return content;
  }

  public String getVisualDescription() {
    return visualDescription;
  }

  public String getFormatNote() {
    return formatNote;
  }

  public String getSourceNote() {
    return sourceNote;
  }

  public Short getPageStart() {
    return pageStart;
  }

  public Short getPageEnd() {
    return pageEnd;
  }
}
