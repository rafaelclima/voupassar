/* VouPassar — store mínimo (TASK 6.1)
 * Pub/sub sem framework. Telas futuras (dashboard, simulados) assinam
 * mudanças sem acoplamento. Estado de sessão nunca persiste token.
 */

const listeners = new Set();

const state = {
  user: null,
  lastError: null,
};

export function getState() {
  return { ...state };
}

export function setState(patch) {
  Object.assign(state, patch);
  for (const fn of listeners) {
    try {
      fn(getState());
    } catch (e) {
      console.error("[store] listener falhou", e);
    }
  }
}

export function subscribe(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export function setUser(user) {
  setState({ user });
}

export function setLastError(err) {
  setState({ lastError: err });
}
