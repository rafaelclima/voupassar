/* VouPassar — API da tela de questão dedicada (TASK 6.6)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * detalhe da questão (3.4) + sessões/tentativas em modo ESTUDO (3.7).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

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
