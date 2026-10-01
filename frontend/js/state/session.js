/* VouPassar — sessão de autenticação (TASK 6.3)
 * Access token vive SÓ em memória (architecture.md §5, docs/api-auth.md).
 * Refresh opaco persiste em sessionStorage (padrão) ou localStorage
 * (quando "manter conectado"), pois o MVP devolve o par no JSON —
 * cookie HttpOnly fica para ADR quando houver domínio de produção.
 * Nenhuma senha é persistida. Sem innerHTML, sem logs de token.
 */

import { setAccessToken as setClientToken } from "../api/client.js";
import { setUser } from "./store.js";

const REFRESH_KEY = "voupassar.refreshToken";
const USER_KEY = "voupassar.user";

let accessToken = null;
let refreshToken = null;

function readPersistedRefresh() {
  try {
    return (
      window.sessionStorage.getItem(REFRESH_KEY) ||
      window.localStorage.getItem(REFRESH_KEY) ||
      null
    );
  } catch {
    return null;
  }
}

function readPersistedUser() {
  try {
    const raw =
      window.sessionStorage.getItem(USER_KEY) ||
      window.localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function writePersisted(refresh, user, remember) {
  try {
    // Limpa o outro compartimento para não duplicar sessão.
    window.sessionStorage.removeItem(REFRESH_KEY);
    window.sessionStorage.removeItem(USER_KEY);
    window.localStorage.removeItem(REFRESH_KEY);
    window.localStorage.removeItem(USER_KEY);
    const store = remember ? window.localStorage : window.sessionStorage;
    if (refresh) store.setItem(REFRESH_KEY, refresh);
    if (user) store.setItem(USER_KEY, JSON.stringify(user));
  } catch {
    // Armazenamento indisponível (modo privado): segue só em memória.
  }
}

/** Restaura refresh + usuário persistidos (sem restaurar access: memória). */
export function loadPersistedSession() {
  refreshToken = readPersistedRefresh();
  const user = readPersistedUser();
  if (user) setUser(user);
  return { refreshToken, user };
}

export function getAccessToken() {
  return accessToken;
}

export function getRefreshToken() {
  return refreshToken;
}

/** Guarda o par novo. `remember=true` → localStorage; senão sessionStorage. */
export function setSession({ accessToken: at, refreshToken: rt, user }, remember = false) {
  accessToken = at || null;
  refreshToken = rt || null;
  setClientToken(accessToken);
  if (user !== undefined) setUser(user || null);
  writePersisted(refreshToken, user ?? readPersistedUser(), remember);
  return { accessToken, refreshToken };
}

/** Troca só o usuário (ex.: após GET /me) mantendo o compartimento atual. */
export function setSessionUser(user) {
  const remember = (() => {
    try {
      return Boolean(window.localStorage.getItem(REFRESH_KEY));
    } catch {
      return false;
    }
  })();
  setUser(user || null);
  writePersisted(refreshToken, user || null, remember);
}

export function clearSession() {
  accessToken = null;
  refreshToken = null;
  setClientToken(null);
  setUser(null);
  try {
    window.sessionStorage.removeItem(REFRESH_KEY);
    window.sessionStorage.removeItem(USER_KEY);
    window.localStorage.removeItem(REFRESH_KEY);
    window.localStorage.removeItem(USER_KEY);
  } catch {
    // Sem armazenamento — nada a limpar.
  }
}

export function isAuthenticated() {
  return Boolean(accessToken);
}

/** Há sessão retomável (refresh persistido) mesmo sem access em memória. */
export function hasPersistedSession() {
  return Boolean(refreshToken || readPersistedRefresh());
}
