/* VouPassar — API de autenticação (TASK 6.3)
 * Envolve js/api/client.js com os endpoints de docs/api-auth.md (TASK 3.5).
 * Regras: e-mail normalizado (trim + lowercase, como o backend); senha
 * nunca em log; envelope de erro preservado como ApiError.
 */

import {
  request,
  setAccessToken as setClientToken,
  getAccessToken as getClientToken,
} from "./client.js";
import {
  setSession,
  setSessionUser,
  clearSession,
  getRefreshToken,
  loadPersistedSession,
} from "../state/session.js";
import { setUser } from "../state/store.js";

export function normalizeEmail(value) {
  return String(value ?? "").trim().toLowerCase();
}

function toSessionPayload(data) {
  return {
    accessToken: data?.accessToken || null,
    refreshToken: data?.refreshToken || null,
    user: data?.user || null,
  };
}

export async function register(payload, { remember = false } = {}) {
  const data = await request("/api/v1/auth/register", {
    method: "POST",
    body: {
      email: normalizeEmail(payload.email),
      password: payload.password,
      displayName: String(payload.displayName ?? "").trim(),
      ...(payload.schoolYear?.trim()
        ? { schoolYear: payload.schoolYear.trim() }
        : {}),
      ...(payload.targetYear ? { targetYear: payload.targetYear } : {}),
      ...(payload.studyGoal?.trim()
        ? { studyGoal: payload.studyGoal.trim() }
        : {}),
    },
  });
  return setSession(toSessionPayload(data), remember);
}

export async function login({ email, password }, { remember = false } = {}) {
  const data = await request("/api/v1/auth/login", {
    method: "POST",
    body: { email: normalizeEmail(email), password },
  });
  return setSession(toSessionPayload(data), remember);
}

export async function refreshSession() {
  const persisted = getRefreshToken() || loadPersistedSession().refreshToken;
  if (!persisted) throw new Error("Sem sessão para renovar.");
  const remember = (() => {
    try {
      return Boolean(window.localStorage.getItem("voupassar.refreshToken"));
    } catch {
      return false;
    }
  })();
  const data = await request("/api/v1/auth/refresh", {
    method: "POST",
    body: { refreshToken: persisted },
  });
  return setSession(toSessionPayload(data), remember);
}

export async function logout() {
  const rt = getRefreshToken() || loadPersistedSession().refreshToken;
  try {
    if (rt) {
      await request("/api/v1/auth/logout", {
        method: "POST",
        body: { refreshToken: rt },
      });
    }
  } finally {
    clearSession();
  }
}

export async function logoutAll() {
  await request("/api/v1/auth/logout-all", { method: "POST" });
  clearSession();
}

export async function forgotPassword(email) {
  // Resposta do servidor é sempre genérica (anti-enumeração) — ver docs/api-auth.md.
  return request("/api/v1/auth/password/forgot", {
    method: "POST",
    body: { email: normalizeEmail(email) },
  });
}

export async function resetPassword({ token, newPassword }) {
  return request("/api/v1/auth/password/reset", {
    method: "POST",
    body: { token: String(token ?? "").trim(), newPassword },
  });
}

export async function changePassword({ currentPassword, newPassword }) {
  const data = await request("/api/v1/auth/password/change", {
    method: "POST",
    body: { currentPassword, newPassword },
  });
  // Backend devolve par novo e o cliente segue logado.
  const remember = (() => {
    try {
      return Boolean(window.localStorage.getItem("voupassar.refreshToken"));
    } catch {
      return false;
    }
  })();
  if (data?.accessToken) return setSession(toSessionPayload(data), remember);
  return data;
}

export async function fetchMe() {
  const me = await request("/api/v1/auth/me");
  setSessionUser(me);
  return me;
}

/**
 * Tenta GET /me; se o access expirou e há refresh, renova uma vez e repete.
 * Retorna o usuário ou null (sem sessão válida). Não lança em 401.
 */
export async function restoreSession() {
  loadPersistedSession();
  // Sem access em memória, mas com refresh: renova antes de desistir.
  if (!getClientToken()) {
    try {
      await refreshSession();
    } catch (err) {
      if (err?.status === 401) clearSession();
      return null;
    }
  }
  try {
    return await fetchMe();
  } catch (err) {
    if (err?.status === 401) {
      try {
        await refreshSession();
        return await fetchMe();
      } catch {
        clearSession();
        setUser(null);
        return null;
      }
    }
    throw err;
  }
}

export { setClientToken };
