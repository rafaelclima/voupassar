/* VouPassar — API da área administrativa (TASK 12.1)
 * Envolve js/api/client.js com os endpoints de docs/api-admin.md (TASK 12.1):
 * fila de revisão, inconsistências, métricas e os dois PATCHes de curadoria.
 * Exige papel CURATOR ou ADMIN no servidor (403 em envelope sem o papel).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchReviewQueue({ validationStatus = "PENDING", classificationStatus, page = 0, size = 20 } = {}) {
  const query = { validationStatus, page, size };
  if (classificationStatus) {
    query.classificationStatus = classificationStatus;
  }
  return request("/api/v1/admin/review-queue", { query });
}

export function fetchInconsistencies() {
  return request("/api/v1/admin/inconsistencies");
}

export function fetchAdminMetrics() {
  return request("/api/v1/admin/metrics");
}

export function updateQuestionStatus(id, { validationStatus, publicationStatus }) {
  const body = {};
  if (validationStatus !== undefined && validationStatus !== null && validationStatus !== "") {
    body.validationStatus = validationStatus;
  }
  if (publicationStatus !== undefined && publicationStatus !== null && publicationStatus !== "") {
    body.publicationStatus = publicationStatus;
  }
  return request(`/api/v1/admin/questions/${encodeURIComponent(String(id))}/status`, {
    method: "PATCH",
    body,
  });
}

export function reviewClassification(id, { status, observation }) {
  const body = { status };
  if (observation !== undefined && observation !== null && String(observation).trim() !== "") {
    body.observation = String(observation).trim();
  }
  return request(`/api/v1/admin/classifications/${encodeURIComponent(String(id))}`, {
    method: "PATCH",
    body,
  });
}
