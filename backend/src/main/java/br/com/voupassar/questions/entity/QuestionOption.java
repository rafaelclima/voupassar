package br.com.voupassar.questions.entity;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Alternativa de questão objetiva (TASK 3.4) — somente leitura.
 *
 * <p>Objetivas oficiais têm exatamente 4 alternativas (constraint trigger
 * {@code trg_four_options} no DDL); a API de leitura não impõe a regra, só
 * devolve o que o banco contém — divergência aparece como dado, nunca como
 * erro silencioso.
 */
@Entity
@Immutable
@Table(name = "question_options")
public class QuestionOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "label", nullable = false, length = 1)
  private String label;

  @Column(name = "option_text", nullable = false)
  private String optionText;

  public QuestionOption() {}

  public Long getId() {
    return id;
  }

  public Question getQuestion() {
    return question;
  }

  public String getLabel() {
    return label;
  }

  public String getOptionText() {
    return optionText;
  }
}
