package br.com.voupassar.simulations.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta de {@link SimulationQuestion}: {@code
 * (simulation_attempt_id, position)} (DDL V1).
 */
public class SimulationQuestionId implements Serializable {

  private Long simulationAttemptId;
  private Short position;

  public SimulationQuestionId() {}

  public SimulationQuestionId(Long simulationAttemptId, Short position) {
    this.simulationAttemptId = simulationAttemptId;
    this.position = position;
  }

  public Long getSimulationAttemptId() {
    return simulationAttemptId;
  }

  public Short getPosition() {
    return position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimulationQuestionId other)) {
      return false;
    }
    return Objects.equals(simulationAttemptId, other.simulationAttemptId)
        && Objects.equals(position, other.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(simulationAttemptId, position);
  }
}
