/* VouPassar — API do simulado (TASK 6.7)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * conteúdos (3.3, disciplinas), edições (3.2), questões (3.4),
 * tentativas (3.7) e simulados (5.1 por disciplina, 5.5 edição real,
 * 5.2 feedback imediato, 5.3 ocultação no Modo Prova).
 * Nenhuma regra de negócio aqui — só busca e repasse.
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function fetchDisciplines() {
  return request("/api/v1/disciplines");
}

export function fetchEditions() {
  return request("/api/v1/editions");
}

export function createByDiscipline({ disciplineCode, questionCount, difficulty, sourceType, mode, institution }) {
  return request("/api/v1/simulations/by-discipline", {
    method: "POST",
    query: institution ? { institution } : {},
    body: {
      disciplineCode,
      questionCount,
      ...(difficulty ? { difficulty } : {}),
      ...(sourceType ? { sourceType } : {}),
      mode,
    },
  });
}

export function createByEdition({ editionYear, mode, institution }) {
  return request("/api/v1/simulations/by-edition", {
    method: "POST",
    body: {
      editionYear,
      mode,
      ...(institution ? { institution } : {}),
    },
  });
}

export function listAttempts({ page = 0, size = 20 } = {}) {
  return request("/api/v1/simulations/attempts", { query: { page, size } });
}

export function fetchAttempt(id) {
  return request(`/api/v1/simulations/attempts/${encodeURIComponent(String(id))}`);
}

export function submitSimulation(id) {
  return request(`/api/v1/simulations/attempts/${encodeURIComponent(String(id))}/submit`, {
    method: "POST",
  });
}

export function abandonSimulation(id) {
  return request(`/api/v1/simulations/attempts/${encodeURIComponent(String(id))}/abandon`, {
    method: "POST",
  });
}

export function fetchResult(id) {
  return request(`/api/v1/simulations/attempts/${encodeURIComponent(String(id))}/result`);
}

export function fetchFeedback(id, position) {
  return request(
    `/api/v1/simulations/attempts/${encodeURIComponent(String(id))}/feedback/${encodeURIComponent(String(position))}`,
  );
}

export function fetchQuestion(id) {
  return request(`/api/v1/questions/${encodeURIComponent(String(id))}`);
}

export function submitAttempt({ questionId, selectedOption, mode, timeSpentSeconds, simulationAttemptId }) {
  return request("/api/v1/attempts", {
    method: "POST",
    body: {
      questionId,
      selectedOption,
      mode,
      ...(timeSpentSeconds !== undefined && timeSpentSeconds !== null
        ? { timeSpentSeconds }
        : {}),
      ...(simulationAttemptId ? { simulationAttemptId } : {}),
    },
  });
}
