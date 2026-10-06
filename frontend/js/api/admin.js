/* VouPassar — API da área administrativa
 * Envolve js/api/client.js com os endpoints de docs/api-admin.md:
 * inconsistências e métricas (observabilidade, sem curadoria desde 2026-10-06).
 * Exige papel CURATOR ou ADMIN no servidor (403 em envelope sem o papel).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchInconsistencies() {
  return request("/api/v1/admin/inconsistencies");
}

export function fetchAdminMetrics() {
  return request("/api/v1/admin/metrics");
}
