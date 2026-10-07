package br.com.voupassar.admin.service;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/**
 * Contadores técnicos do produto (TASK 22.2) — sem PII, sem tracking invasivo.
 *
 * <p>Só eventos de negócio já visíveis ao servidor (geração, conclusão,
 * registro); nenhum clique, dwell ou fingerprint (AGENTS.md §16). Os
 * contadores vivem no {@code MeterRegistry} em memória (por instância, como
 * o rate limit) e são expostos em snapshot no diagnóstico autenticado
 * ({@code GET /api/v1/admin/diagnostics}). Clques/CTR, conclusão de wizard e
 * D7 dependem das trilhas de frontend (17.x/19.x/20.2) e seguem pendentes —
 * ver {@code docs/task-10.3.md}.
 */
@Component
public class TechMetrics {

  public static final String PLANS_GENERATED = "voupassar.plans.generated";
  public static final String DIAGNOSES_GENERATED = "voupassar.diagnoses.generated";
  public static final String ATTEMPTS_SUBMITTED = "voupassar.attempts.submitted";
  public static final String SIMULATIONS_SUBMITTED = "voupassar.simulations.submitted";
  public static final String REVIEW_SESSIONS_CREATED = "voupassar.review.sessions.created";

  private final MeterRegistry registry;

  public TechMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  public void plansGenerated() {
    registry.counter(PLANS_GENERATED).increment();
  }

  public void diagnosesGenerated() {
    registry.counter(DIAGNOSES_GENERATED).increment();
  }

  public void attemptsSubmitted() {
    registry.counter(ATTEMPTS_SUBMITTED).increment();
  }

  public void simulationsSubmitted() {
    registry.counter(SIMULATIONS_SUBMITTED).increment();
  }

  public void reviewSessionsCreated() {
    registry.counter(REVIEW_SESSIONS_CREATED).increment();
  }

  /** Snapshot ordenado nome→contagem (só contadores `voupassar.*`). */
  public Map<String, Double> snapshot() {
    Map<String, Double> out = new TreeMap<>();
    registry.getMeters().forEach(m -> {
      if (m instanceof io.micrometer.core.instrument.Counter c
          && m.getId().getName().startsWith("voupassar.")) {
        out.put(m.getId().getName(), c.count());
      }
    });
    return out;
  }
}
