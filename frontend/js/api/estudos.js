/* VouPassar — API da área de estudos (TASK 6.5)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * conteúdos (3.3), questões (3.4), sessões/tentativas (3.7),
 * desempenho (4.1), diagnóstico (4.2) e roteiro (4.3/4.4).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchDisciplines() {
  return request("/api/v1/disciplines");
}

export function fetchTopics(disciplineCode) {
  return request("/api/v1/topics", {
    query: disciplineCode ? { disciplineCode } : {},
  });
}

export function fetchSubtopics(topicId) {
  return request("/api/v1/subtopics", {
    query: topicId ? { topicId } : {},
  });
}

export function fetchQuestions(params = {}) {
  return request("/api/v1/questions", { query: params });
}

export function fetchQuestion(id) {
  return request(`/api/v1/questions/${encodeURIComponent(String(id))}`);
}

export function openStudySession() {
  return request("/api/v1/study-sessions", {
    method: "POST",
    body: { mode: "ESTUDO" },
  });
}

export function submitAttempt({ questionId, selectedOption, timeSpentSeconds, studySessionId }) {
  const body = {
    questionId,
    selectedOption,
    mode: "ESTUDO",
    ...(timeSpentSeconds !== undefined && timeSpentSeconds !== null
      ? { timeSpentSeconds }
      : {}),
    ...(studySessionId ? { studySessionId } : {}),
  };
  return request("/api/v1/attempts", { method: "POST", body });
}

export function fetchOverview() {
  return request("/api/v1/performance/overview");
}

export function fetchDiagnosis() {
  return request("/api/v1/diagnosis");
}

export function fetchPlan() {
  return request("/api/v1/recommendations/plan");
}
