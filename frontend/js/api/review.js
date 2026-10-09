/* VouPassar — API da revisão guiada (TASK 17.1)
 * Envolve js/api/client.js com os endpoints já prontos no backend:
 * fila (TASK 4.5, GET /review/queue) e sessões (TASK 5.4,
 * POST/GET /review/sessions).
 * Espelho fino de js/api/simulado.js: nenhuma regra de negócio aqui —
 * só busca e repasse. Filtros, ordenação e motivos vivem no backend
 * (ReviewService/ReviewSessionService).
 * Erros seguem como ApiError com envelope preservado (code/traceId).
 */

import { request } from "./client.js";

export function getQueue({ limit, discipline, topicId, onlyErrors, institution } = {}) {
  return request("/api/v1/review/queue", {
    query: { limit, discipline, topicId, onlyErrors, institution },
  });
}

/* Cria a sessão congelando o top-N da fila (filtros de 4.5, corpo de 5.4).
 * `questionIds` está na assinatura como reserva para a TASK 19.2 (sessão a
 * partir de ids escolhidos no raio-X): o backend 17.1 ainda não aceita esse
 * campo (CreateReviewSessionRequest só tem limit/discipline/topicId/
 * onlyErrors), então passar ids falha no cliente com mensagem explícita —
 * nunca envia campo desconhecido nem ignora a seleção em silêncio. */
export function createSession({ questionIds, limit, discipline, topicId, onlyErrors } = {}) {
  if (questionIds !== undefined && questionIds !== null) {
    throw new Error(
      "Revisão por questões escolhidas ainda não disponível: " +
        "esta versão congela o top-N da fila via filtros " +
        "(limit/discipline/topicId/onlyErrors). " +
        "A seleção por ids chega com a TASK 19.2.",
    );
  }
  return request("/api/v1/review/sessions", {
    method: "POST",
    body: {
      ...(limit !== undefined && limit !== null ? { limit } : {}),
      ...(discipline ? { discipline } : {}),
      ...(topicId !== undefined && topicId !== null ? { topicId } : {}),
      ...(onlyErrors !== undefined && onlyErrors !== null ? { onlyErrors } : {}),
    },
  });
}

export function getSession(id) {
  return request(`/api/v1/review/sessions/${encodeURIComponent(String(id))}`);
}

export function getSessionResult(id) {
  return request(`/api/v1/review/sessions/${encodeURIComponent(String(id))}/result`);
}

/* Enunciado de uma posição do caderno (TASK 3.4). O gabarito (`answerKey`)
 * vem cheio em REVISAO — a máscara da TASK 5.3 vale só para PROVA em
 * andamento — mas a tela só usa a letra *após* responder a posição
 * (feedback imediato, nunca cola antes da resposta). */
export function fetchQuestion(id) {
  return request(`/api/v1/questions/${encodeURIComponent(String(id))}`);
}

/* Resposta de uma posição do caderno (TASK 17.3): fato REVISAO vinculado à
 * sessão (`studySessionId`), nunca a simulado. `BLANK` conta como erro
 * quando a questão não é anulada (regra do backend, igual ao Estudo).
 * REVISAO nunca oculta: o POST devolve `isCorrect` revelado. */
export function submitReviewAttempt({ questionId, selectedOption, studySessionId, timeSpentSeconds }) {
  return request("/api/v1/attempts", {
    method: "POST",
    body: {
      questionId,
      selectedOption,
      mode: "REVISAO",
      ...(timeSpentSeconds !== undefined && timeSpentSeconds !== null
        ? { timeSpentSeconds }
        : {}),
      ...(studySessionId ? { studySessionId } : {}),
    },
  });
}

/* Encerra a sessão (IN_PROGRESS → FINISHED) para liberar o resultado
 * (`GET …/result`; antes disso o backend responde 409 REVIEW_NOT_FINISHED). */
export function finishSession(id) {
  return request(`/api/v1/study-sessions/${encodeURIComponent(String(id))}/finish`, {
    method: "POST",
  });
}
