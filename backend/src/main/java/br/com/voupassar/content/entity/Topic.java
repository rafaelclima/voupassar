package br.com.voupassar.content.entity;

import br.com.voupassar.exams.entity.Discipline;
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
 * Assunto controlado (TASK 3.3).
 *
 * <p>Vocabulário que emergiu das provas (taxonomia v1.1 em
 * {@code docs/content-map.md}, seed V2). Somente leitura neste contexto:
 * a escrita acontece via migrations + importador (TASK 2.3), nunca pela API.
 */
@Entity
@Table(name = "topics")
public class Topic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "discipline_id", nullable = false)
  private Discipline discipline;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  public Topic() {}

  public Long getId() {
    return id;
  }

  public Discipline getDiscipline() {
    return discipline;
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
