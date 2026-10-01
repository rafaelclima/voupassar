/* VouPassar — cliente HTTP (TASK 6.1)
 * Fetch API + envelope do backend {code, message, details, traceId}.
 * Access token vive SÓ em memória (architecture.md §5); refresh segue
 * o fluxo do backend (cookie HttpOnly ou corpo — ver docs/api-auth.md).
 * Sem innerHTML com dados do servidor (XSS: AGENTS.md §15).
 */

import { API_BASE_URL } from "../config.js";

let accessToken = null;

export function setAccessToken(token) {
  accessToken = token || null;
}

export function getAccessToken() {
  return accessToken;
}

export function clearAccessToken() {
  accessToken = null;
}

export class ApiError extends Error {
  constructor({ status, code, message, details, traceId, path }) {
    super(message || "Erro inesperado. Tente novamente.");
    this.name = "ApiError";
    this.status = status ?? 0;
    this.code = code || "NETWORK_ERROR";
    this.details = details || null;
    this.traceId = traceId || null;
    this.path = path || null;
  }
}

/** Mensagem amigável em pt-BR a partir do envelope ou da rede. */
export function friendlyMessage(err) {
  if (!(err instanceof ApiError)) return "Erro inesperado. Tente novamente.";
  switch (err.code) {
    case "NETWORK_ERROR":
      return "Sem conexão com o servidor. Verifique sua internet e tente de novo.";
    case "UNAUTHORIZED":
      return "Sua sessão expirou. Entre novamente.";
    case "FORBIDDEN":
      return "Você não tem permissão para esta ação.";
    case "EDITION_NOT_FOUND":
      return "Edição não encontrada. A edição de 2021 não existe no acervo (fonte ausente).";
    case "NOT_FOUND":
    case "QUESTION_NOT_FOUND":
    case "DISCIPLINE_NOT_FOUND":
    case "TOPIC_NOT_FOUND":
    case "SUBTOPIC_NOT_FOUND":
      return "Conteúdo não encontrado.";
    default:
      if (err.status === 429) return "Muitas tentativas. Aguarde um pouco e tente de novo.";
      if (err.status >= 500) return "O servidor falhou. Tente novamente em instantes.";
      return err.message || "Erro inesperado. Tente novamente.";
  }
}

function buildUrl(path, query) {
  const url = new URL(path, API_BASE_URL);
  if (query) {
    for (const [k, v] of Object.entries(query)) {
      if (v === undefined || v === null || v === "") continue;
      url.searchParams.set(k, String(v));
    }
  }
  return url;
}

/**
 * request("/api/v1/health") → JSON ou null (204).
 * Lança ApiError com envelope preservado (code/traceId para suporte).
 *
 * `credentials` padrão é "same-origin" (TASK 6.3): o MVP trafega o refresh
 * no corpo JSON (docs/api-auth.md — sem cookie HttpOnly), e o backend não
 * responde `Access-Control-Allow-Credentials`, de modo que "include"
 * quebra o preflight cross-origin (Pages × API). Quando o refresh passar
 * a cookie HttpOnly + CORS com allowCredentials, usar `credentials:
 * "include"` na chamada específica.
 */
export async function request(path, { method = "GET", body, query, signal, timeoutMs = 15000, credentials = "same-origin" } = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  const combined = signal
    ? (() => {
        const c = new AbortController();
        const onAbort = () => c.abort(signal.reason);
        signal.addEventListener("abort", onAbort, { once: true });
        controller.signal.addEventListener("abort", () => c.abort(controller.signal.reason));
        return c.signal;
      })()
    : controller.signal;

  let res;
  try {
    res = await fetch(buildUrl(path, query), {
      method,
      signal: combined,
      credentials,
      headers: {
        Accept: "application/json",
        ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    clearTimeout(timer);
    if (e?.name === "AbortError") {
      throw new ApiError({ status: 0, code: "NETWORK_ERROR", message: "Tempo esgotado. Tente novamente." });
    }
    throw new ApiError({ status: 0, code: "NETWORK_ERROR", message: "Sem conexão com o servidor." });
  } finally {
    clearTimeout(timer);
  }

  const traceId = res.headers.get("X-Trace-Id");
  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }

  if (res.ok) return data;

  throw new ApiError({
    status: res.status,
    code: data?.code || (res.status === 401 ? "UNAUTHORIZED" : res.status === 404 ? "NOT_FOUND" : "REQUEST_FAILED"),
    message: data?.message || `Falha na requisição (${res.status}).`,
    details: data?.details ?? null,
    traceId: data?.traceId || traceId,
    path: data?.path || path,
  });
}

export const api = {
  health: () => request("/api/v1/health"),
  editions: () => request("/api/v1/editions"),
  disciplines: () => request("/api/v1/disciplines"),
  questions: (params = {}) => request("/api/v1/questions", { query: params }),
};
