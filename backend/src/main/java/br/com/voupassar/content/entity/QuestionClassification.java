package br.com.voupassar.content.entity;

import br.com.voupassar.exams.entity.Question;
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
 * Classificação pedagógica versionada (TASK 3.3, projeção mínima).
 *
 * <p>Julgamento derivado da fonte ({@code CLASSIFICACAO_DERIVADA_FONTE}),
 * taxonomia v1.1, revisão humana PENDENTE. Só a {@code APPROVED} mais recente
 * alimenta recomendação (Fase 4); as estatísticas históricas aqui contam o que
 * está no banco sem filtrar confiança — com nota explícita de revisão
 * pendente, nunca como verdade oficial do IFRN.
 */
@Entity
@Immutable
@Table(name = "question_classifications")
public class QuestionClassification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(name = "taxonomy_version", nullable = false)
  private String taxonomyVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id")
  private Topic topic;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subtopic_id")
  private Subtopic subtopic;

  @Column(name = "confidence", nullable = false)
  private String confidence;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "origin", nullable = false)
  private String origin;

  @Column(name = "evidence", nullable = false)
  private String evidence;

  @Column(name = "observation")
  private String observation;

  public QuestionClassification() {}

  public Long getId() {
    return id;
  }

  public Question getQuestion() {
    return question;
  }

  public String getTaxonomyVersion() {
    return taxonomyVersion;
  }

  public Topic getTopic() {
    return topic;
  }

  public Subtopic getSubtopic() {
    return subtopic;
  }

  public String getConfidence() {
    return confidence;
  }

  public String getStatus() {
    return status;
  }

  public String getOrigin() {
    return origin;
  }

  public String getEvidence() {
    return evidence;
  }

  public String getObservation() {
    return observation;
  }
}
