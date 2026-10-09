/* VouPassar — API da área de estudos (TASK 6.5)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * conteúdos (3.3), questões (3.4), sessões/tentativas (3.7),
 * desempenho (4.1), diagnóstico (4.2) e roteiro (4.3/4.4).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchDisciplines(institution) {
  return request("/api/v1/disciplines", {
    query: institution ? { institution } : {},
  });
}

export function fetchTopics(disciplineCode, institution) {
  return request("/api/v1/topics", {
    query: {
      ...(disciplineCode ? { disciplineCode } : {}),
      ...(institution ? { institution } : {}),
    },
  });
}

export function fetchSubtopics(topicId, institution) {
  return request("/api/v1/subtopics", {
    query: {
      ...(topicId ? { topicId } : {}),
      ...(institution ? { institution } : {}),
    },
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
