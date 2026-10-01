package br.com.voupassar.review.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta do caderno de revisão (TASK 5.4): {@code (study_session_id,
 * position)} — mesmo padrão de {@code SimulationQuestionId} (TASK 5.1).
 */
public class ReviewSessionQuestionId implements Serializable {

  private Long studySessionId;
  private Short position;

  public ReviewSessionQuestionId() {}

  public ReviewSessionQuestionId(Long studySessionId, Short position) {
    this.studySessionId = studySessionId;
    this.position = position;
  }

  public Long getStudySessionId() {
    return studySessionId;
  }

  public Short getPosition() {
    return position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReviewSessionQuestionId other)) {
      return false;
    }
    return Objects.equals(studySessionId, other.studySessionId)
        && Objects.equals(position, other.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(studySessionId, position);
  }
}
