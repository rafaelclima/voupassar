package br.com.voupassar.aiprova.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta de {@link AiGenerationQuestion}: {@code (generation_id,
 * position)} (DDL V23, espelho de {@code simulation_questions}).
 */
public class AiGenerationQuestionId implements Serializable {

  private Long generationId;
  private Short position;

  public AiGenerationQuestionId() {}

  public AiGenerationQuestionId(Long generationId, Short position) {
    this.generationId = generationId;
    this.position = position;
  }

  public Long getGenerationId() {
    return generationId;
  }

  public Short getPosition() {
    return position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AiGenerationQuestionId other)) {
      return false;
    }
    return Objects.equals(generationId, other.generationId)
        && Objects.equals(position, other.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(generationId, position);
  }
}
