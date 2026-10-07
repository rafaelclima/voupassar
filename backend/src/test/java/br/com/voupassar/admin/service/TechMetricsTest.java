package br.com.voupassar.admin.service;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

/**
 * Contadores técnicos (TASK 22.2): incrementam e aparecem no snapshot,
 * sem PII e sem tracking de comportamento.
 */
class TechMetricsTest {

  @Test
  void countersIncrementAndSnapshot() {
    TechMetrics metrics = new TechMetrics(new SimpleMeterRegistry());

    assertTrue(metrics.snapshot().isEmpty());

    metrics.plansGenerated();
    metrics.plansGenerated();
    metrics.diagnosesGenerated();
    metrics.attemptsSubmitted();
    metrics.simulationsSubmitted();
    metrics.reviewSessionsCreated();

    var snap = metrics.snapshot();
    assertEquals(2.0, snap.get(TechMetrics.PLANS_GENERATED));
    assertEquals(1.0, snap.get(TechMetrics.DIAGNOSES_GENERATED));
    assertEquals(1.0, snap.get(TechMetrics.ATTEMPTS_SUBMITTED));
    assertEquals(1.0, snap.get(TechMetrics.SIMULATIONS_SUBMITTED));
    assertEquals(1.0, snap.get(TechMetrics.REVIEW_SESSIONS_CREATED));
  }
}
