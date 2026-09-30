package br.com.voupassar.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Subassunto controlado (TASK 3.3).
 *
 * <p>Só os observados nas provas entram no seed V2; códigos sem ocorrência
 * entram por migração futura quando (e se) observados — nada inventado.
 */
@Entity
@Table(name = "subtopics")
public class Subtopic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "topic_id", nullable = false)
  private Topic topic;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  public Subtopic() {}

  public Long getId() {
    return id;
  }

  public Topic getTopic() {
    return topic;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return active;
  }
}
