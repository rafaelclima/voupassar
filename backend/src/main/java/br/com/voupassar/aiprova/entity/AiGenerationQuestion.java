package br.com.voupassar.aiprova.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Caderno-IA congelado (TASK A.2/A.3): posição + conteúdo + gabarito vigente
 * no momento da geração.
 *
 * <p>Mapeia {@code ai_generation_questions} (DDL V23). Espelho de {@code
 * simulation_questions}, sem {@code question_id}: o conteúdo-IA vive aqui
 * (inline na API), nunca no acervo oficial. Congelado por construção (a A.3
 * não expõe mutação). {@code topic} é código do vocabulário vigente ou
 * {@code DESCONHECIDO} (validado pré-freeze pelo {@code AiProvaValidator});
 * IA nunca tem anulada (só A–D, sem {@code X}).
 */
@Entity
@IdClass(AiGenerationQuestionId.class)
@Table(name = "ai_generation_questions")
public class AiGenerationQuestion {

  @Id
  @Column(name = "generation_id", nullable = false)
  private Long generationId;

  @Id
  @Column(name = "position", nullable = false)
  private Short position;

  @Column(name = "discipline", nullable = false)
  private String discipline;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Column(name = "stem", nullable = false)
  private String stem;

  @Column(name = "options_json", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String optionsJson;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "frozen_answer_key", nullable = false, length = 1)
  private String frozenAnswerKey;

  public AiGenerationQuestion() {}

  public Long getGenerationId() {
    return generationId;
  }

  public void setGenerationId(Long generationId) {
    this.generationId = generationId;
  }

  public Short getPosition() {
    return position;
  }

  public void setPosition(Short position) {
    this.position = position;
  }

  public String getDiscipline() {
    return discipline;
  }

  public void setDiscipline(String discipline) {
    this.discipline = discipline;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getStem() {
    return stem;
  }

  public void setStem(String stem) {
    this.stem = stem;
  }

  public String getOptionsJson() {
    return optionsJson;
  }

  public void setOptionsJson(String optionsJson) {
    this.optionsJson = optionsJson;
  }

  public String getFrozenAnswerKey() {
    return frozenAnswerKey;
  }

  public void setFrozenAnswerKey(String frozenAnswerKey) {
    this.frozenAnswerKey = frozenAnswerKey;
  }
}
