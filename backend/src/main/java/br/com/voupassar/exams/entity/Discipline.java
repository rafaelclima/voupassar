package br.com.voupassar.exams.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Disciplina controlada (somente as observadas nas provas: Língua Portuguesa
 * e Matemática no IFRN; Ciências da Natureza e Ciências Humanas semeadas na
 * TASK C.2 para o EAJ-2021 — tópicos/subtópicos CN/CH nascem só da evidência
 * D.1, nunca inventados). Entidade mínima para as estatísticas da TASK 3.2.
 */
@Entity
@Table(name = "disciplines")
public class Discipline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "name", nullable = false)
  private String name;

  public Discipline() {}

  public Discipline(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }
}
