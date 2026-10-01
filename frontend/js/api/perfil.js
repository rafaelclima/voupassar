/* VouPassar — API do perfil (TASK 6.8)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * perfil (3.6), desempenho (4.1), diagnóstico (4.2) e roteiro (4.3/4.4,
 * só leitura do plano vigente para as metas).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchProfile() {
  return request("/api/v1/profile");
}

export function updateProfile(payload) {
  return request("/api/v1/profile", { method: "PUT", body: payload });
}

export function fetchProfileStats() {
  return request("/api/v1/profile/stats");
}

export function fetchHistory(page = 0, size = 20) {
  return request("/api/v1/profile/history", { query: { page, size } });
}

export function fetchOverview() {
  return request("/api/v1/performance/overview");
}

export function fetchEvolution(granularity = "WEEK") {
  return request("/api/v1/performance/evolution", { query: { granularity } });
}

export function fetchDiagnosis() {
  return request("/api/v1/diagnosis");
}

export function fetchPlan() {
  return request("/api/v1/recommendations/plan");
}
