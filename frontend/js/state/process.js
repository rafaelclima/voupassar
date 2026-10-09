/* VouPassar — seletor de processo (TASK E.2)
 *
 * Trilha do processo seletivo: IFRN (default, sem deslocar usuários atuais)
 * ou EAJ/UFRN. Persistido em localStorage; o wizard "Descobrir meu nível",
 * o diagnóstico, o roteiro e a revisão leem daqui quando a URL não traz
 * `?institution=` explícito. Nenhuma regra de negócio aqui — só guarda e
 * normaliza o rótulo (a filtragem vive no backend).
 */

const KEY = "voupassar.institution";

export const INSTITUTIONS = ["IFRN", "EAJ"];

export function normalizeInstitution(raw) {
  const v = String(raw || "").trim().toUpperCase();
  return v === "EAJ" ? "EAJ" : "IFRN";
}

export function getInstitution() {
  try {
    return normalizeInstitution(window.localStorage.getItem(KEY) || "IFRN");
  } catch {
    return "IFRN";
  }
}

export function setInstitution(raw) {
  const v = normalizeInstitution(raw);
  try {
    window.localStorage.setItem(KEY, v);
  } catch {
    /* armazenamento indisponível: segue em memória da sessão */
  }
  return v;
}

export function readInstitutionFromUrl() {
  try {
    const raw = new URLSearchParams(window.location.search).get("institution");
    return raw ? normalizeInstitution(raw) : null;
  } catch {
    return null;
  }
}

/* Trilha efetiva: URL vence o persistido; default IFRN (wizard E.2). */
export function currentInstitution() {
  return readInstitutionFromUrl() || getInstitution();
}
