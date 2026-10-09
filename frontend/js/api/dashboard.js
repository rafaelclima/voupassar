/* VouPassar — API do dashboard (TASK 6.4)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * performance (4.1), diagnóstico (4.2), roteiro (4.3/4.4), tópicos (3.3)
 * e simulados (5.1). Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchOverview() {
  return request("/api/v1/performance/overview");
}

export function fetchEvolution(granularity = "WEEK") {
  return request("/api/v1/performance/evolution", { query: { granularity } });
}

export function fetchDiagnosis(institution) {
  return request("/api/v1/diagnosis", {
    query: institution ? { institution } : {},
  });
}

export function fetchPlan(institution) {
  return request("/api/v1/recommendations/plan", {
    query: institution ? { institution } : {},
  });
}

export function generatePlan(institution) {
  return request("/api/v1/recommendations", {
    method: "POST",
    query: institution ? { institution } : {},
  });
}

export function updatePlanItemStatus(itemId, status) {
  return request(
    `/api/v1/recommendations/items/${encodeURIComponent(String(itemId))}/status`,
    { method: "POST", query: { status } },
  );
}

export function fetchTopics() {
  return request("/api/v1/topics");
}

export function fetchRecentSimulations(size = 5) {
  return request("/api/v1/simulations/attempts", { query: { page: 0, size } });
}
