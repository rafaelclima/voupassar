package br.com.voupassar.profile.entity;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Agregado materializado por aluno×conteúdo (TASK 3.6 — leitura).
 *
 * <p>Leitura da tabela {@code student_topic_performance} (DDL TASK 2.2, V1).
 * Escrita por job da Fase 4 (TASK 4.1), nunca pelo cliente: esta API só lê.
 * {@code accuracy} é coluna gerada ({@code hits/attempts}, NULL quando zero
 * tentativas). Linha com {@code subtopic} NULL = nível tópico.
 */
@Entity
@Table(name = "student_topic_performance")
public class StudentTopicPerformance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "topic_id", nullable = false)
  private Topic topic;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subtopic_id")
  private Subtopic subtopic;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "hits", nullable = false)
  private int hits;

  /** Coluna gerada no banco — somente leitura. */
  @Column(name = "accuracy", insertable = false, updatable = false)
  private BigDecimal accuracy;

  @Column(name = "last_attempt_at")
  private OffsetDateTime lastAttemptAt;

  public StudentTopicPerformance() {}

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Topic getTopic() {
    return topic;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
  }

  public Subtopic getSubtopic() {
    return subtopic;
  }

  public void setSubtopic(Subtopic subtopic) {
    this.subtopic = subtopic;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public int getHits() {
    return hits;
  }

  public void setHits(int hits) {
    this.hits = hits;
  }

  public BigDecimal getAccuracy() {
    return accuracy;
  }

  public OffsetDateTime getLastAttemptAt() {
    return lastAttemptAt;
  }

  public void setLastAttemptAt(OffsetDateTime lastAttemptAt) {
    this.lastAttemptAt = lastAttemptAt;
  }
}
