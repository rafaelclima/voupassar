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
 * Vínculo questão ↔ texto-base (TASK 6.9) — leitura de {@code
 * question_passages} (DDL V9). Um texto serve N questões; uma questão pode
 * usar mais de um texto (ex. 2020 Q20 usa Textos 1 e 2); {@code position}
 * preserva a ordem de leitura no caderno.
 */
@Entity
@Immutable
@Table(name = "question_passages")
public class QuestionPassage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "question_id", nullable = false)
  private Long questionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "passage_id", nullable = false)
  private Passage passage;

  @Column(name = "position", nullable = false)
  private Short position;

  public QuestionPassage() {}

  public Long getId() {
    return id;
  }

  public Long getQuestionId() {
    return questionId;
  }

  public Passage getPassage() {
    return passage;
  }

  public Short getPosition() {
    return position;
  }
}
