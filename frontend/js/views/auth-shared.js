/* VouPassar — ajuda compartilhada das telas de auth (TASK 6.3)
 * Validação espelha as regras do backend (docs/api-auth.md): e-mail,
 * senha 8–72, nome 2–80, ano-alvo 2000–2100. Servidor é fonte da verdade;
 * aqui só falhamos rápido e com mensagens em pt-BR. Sem innerHTML.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { el, renderErrorSummary, setFieldError } from "../components/ui.js";
import { restoreSession, logout } from "../api/auth.js";
import { getState } from "../state/store.js";

const EMAIL_RE = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function validateEmail(value) {
  const v = String(value ?? "").trim();
  if (!v) return "Informe seu e-mail.";
  if (v.length > 254) return "E-mail deve ter até 254 caracteres.";
  if (!EMAIL_RE.test(v)) return "E-mail inválido.";
  return "";
}

export function validatePassword(value, { required = true } = {}) {
  const v = String(value ?? "");
  if (!v) return required ? "Informe sua senha." : "";
  if (v.length < 8 || v.length > 72) return "Senha deve ter de 8 a 72 caracteres.";
  return "";
}

export function validateDisplayName(value) {
  const v = String(value ?? "").trim();
  if (!v) return "Informe seu nome de exibição.";
  if (v.length < 2 || v.length > 80) return "Nome deve ter de 2 a 80 caracteres.";
  return "";
}

export function validateTargetYear(value) {
  const v = String(value ?? "").trim();
  if (!v) return "";
  const n = Number(v);
  if (!Number.isInteger(n) || n < 2000 || n > 2100) return "Ano da prova inválido (2000–2100).";
  return "";
}

/** Liga todos os [data-password-toggle] da página (Mostrar/Ocultar, acessível). */
export function wirePasswordToggles(root = document) {
  root.querySelectorAll("[data-password-toggle]").forEach((btn) => {
    const input = document.getElementById(btn.getAttribute("data-password-toggle"));
    if (!(input instanceof HTMLInputElement)) return;
    btn.addEventListener("click", () => {
      const show = input.type === "password";
      input.type = show ? "text" : "password";
      btn.textContent = show ? "Ocultar" : "Mostrar";
      btn.setAttribute("aria-label", show ? "Ocultar senha" : "Mostrar senha");
      input.focus();
    });
  });
}

/** Mensagem amigável por código do envelope (base: friendlyMessage). */
export function authErrorMessage(err) {
  if (!(err instanceof ApiError)) return friendlyMessage(err);
  switch (err.code) {
    case "INVALID_CREDENTIALS":
      return "E-mail ou senha inválidos.";
    case "EMAIL_IN_USE":
      return "Este e-mail já está cadastrado. Tente entrar ou recupere a senha.";
    case "INVALID_OR_EXPIRED_TOKEN":
      return "Token inválido, expirado ou já usado. Peça um novo link de recuperação.";
    case "REFRESH_REUSED":
      return "Sessão invalidada por segurança. Entre novamente.";
    case "SAME_PASSWORD":
      return "A nova senha deve ser diferente da atual.";
    case "RATE_LIMITED":
    case "TOO_MANY_REQUESTS":
      return "Muitas tentativas. Aguarde um pouco e tente de novo.";
    case "VALIDATION_ERROR":
      return "Verifique os campos destacados.";
    default:
      return friendlyMessage(err);
  }
}

/**
 * Renderiza o erro no container com resumo + traceId e distribui
 * "campo: mensagem" (formato do backend) nos inputs correspondentes.
 */
export function renderAuthError(container, err, fieldMap = {}) {
  const items =
    err instanceof ApiError && Array.isArray(err.details) && err.details.length
      ? err.details
      : [authErrorMessage(err)];
  renderErrorSummary(container, {
    title: authErrorMessage(err),
    items: items.length > 1 || items[0] !== authErrorMessage(err) ? items : undefined,
    traceId: err instanceof ApiError ? err.traceId : null,
  });
  // Distribui "campo: msg" nos inputs (ex.: "email: E-mail inválido.").
  if (err instanceof ApiError && Array.isArray(err.details)) {
    for (const d of err.details) {
      const sep = String(d).indexOf(":");
      if (sep < 0) continue;
      const field = String(d).slice(0, sep).trim();
      const msg = String(d).slice(sep + 1).trim();
      const input = fieldMap[field];
      if (input && !input.getAttribute("aria-invalid")) setFieldError(input, msg);
    }
  }
}

export function clearFieldErrors(inputs) {
  for (const input of Object.values(inputs)) {
    if (input) setFieldError(input, "");
  }
}

/** Validador único de caminho interno (TASK 16.3).
 * Une `safeNextParam` (login) + `sanitizeBack` (questao voltar): só aceita
 * relativo interno `./…`, absoluto de mesma origem `/…` (nunca `//`) ou
 * `pagina.html…`. Rejeita `https?:`, `\`, `//` embutido, espaços/`<>"/`,
 * comprimento > 400. Retorna `fallback` quando inválido. Usado por login
 * (?next=), voltar da questão (?voltar=) e guarda do simulado.
 */
export function sanitizeInternalPath(raw, fallback = "") {
  const v = String(raw || "").trim();
  if (!v || v.length > 400) return fallback;
  if (v.includes("\\") || /[\s<>"]/.test(v) || /https?:/i.test(v)) return fallback;
  // Absoluto de mesma origem: /… mas nunca //…
  if (v.startsWith("/")) {
    if (v.startsWith("//")) return fallback;
    if (v.includes("//")) return fallback;
    return v;
  }
  // Relativo ./… (sem // embutido).
  if (v.startsWith("./")) {
    if (v.includes("//")) return fallback;
    return v;
  }
  // Curto tipo pagina.html?x#y (sem barra inicial).
  if (/^[a-z0-9-]+\.html(\?.*)?(#.*)?$/i.test(v)) return v;
  return fallback;
}

/** ?next= só aceita caminho relativo interno (anti open-redirect). */
export function safeNextParam(fallback = "") {
  try {
    const next = new URLSearchParams(window.location.search).get("next") || "";
    return sanitizeInternalPath(next, fallback);
  } catch {
    // Parâmetro ilegível: sem redirecionamento.
  }
  return fallback;
}

/** ?voltar= da questão: só relativo ./… (usa o validador único). */
export function sanitizeBack(raw, fallback = "./estudos.html") {
  const clean = sanitizeInternalPath(raw, "");
  // Voltar nunca usa absoluto /…: só ./… ou pagina.html curtos.
  if (!clean) return fallback;
  if (clean.startsWith("/") && !clean.startsWith("./")) return fallback;
  return clean;
}

/** Guarda de acesso compartilhada (TASK 16.3).
 * Monta o painel "entre para continuar" sem duplicar markup nas views:
 * título + descrição + Entrar (com ?next= seguro) + Criar conta.
 * `next` já deve vir sanitizado (ex.: safeNextParam ou literal interno).
 */
export function renderAuthGuard(container, { title, description, next, registerHref = "./cadastro.html" } = {}) {
  container.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: title || "Entre para continuar" }));
  box.appendChild(el("p", { text: description || "Esta área precisa da sua sessão — entre ou crie uma conta para continuar." }));
  const actions = el("div", { className: "btn-group", attrs: { style: "justify-content:center" } });
  const loginHref = next ? `./login.html?next=${encodeURIComponent(next)}` : "./login.html";
  actions.appendChild(el("a", { className: "btn btn--primary", text: "Entrar", attrs: { href: loginHref } }));
  actions.appendChild(el("a", { className: "btn btn--secondary", text: "Criar conta", attrs: { href: registerHref } }));
  box.appendChild(actions);
  container.appendChild(box);
  return box;
}

/** Restaura a sessão ou mostra a guarda (TASK 16.3).
 * Centraliza o `restoreSession().catch(() => null)` repetido nas views:
 * retorna o usuário ou null (após exibir a guarda). Não muda o
 * comportamento visível — só remove duplicação.
 */
export async function requireSessionOrGuard(showGuard) {
  try {
    const { restoreSession: restore } = await import("../api/auth.js");
    const user = await restore().catch(() => null);
    if (!user) showGuard?.();
    return user;
  } catch {
    showGuard?.();
    return null;
  }
}

/** Painel "você já está conectado" — esconde o form, oferece sair/continuar. */
export function showAlreadyLoggedIn({ alreadyBox, form, feedbackBox, onLogout }) {
  const user = getState().user;
  if (!user) return false;
  if (form) form.hidden = true;
  if (feedbackBox) feedbackBox.textContent = "";
  alreadyBox.hidden = false;
  alreadyBox.textContent = "";
  const wrap = el("div", { className: "auth-success" });
  const alert = el("div", { className: "alert alert--success", attrs: { role: "status" } });
  const inner = el("div");
  inner.appendChild(el("strong", { text: "Você já está conectado." }));
  inner.appendChild(
    el("p", { text: `${user.displayName || "Estudante"} · ${user.email || ""}`.trim() })
  );
  const note = el("p", { className: "auth-meta" });
  note.textContent = "Continue para o dashboard com desempenho, prioridades e roteiro.";
  alert.appendChild(inner);
  wrap.appendChild(alert);
  wrap.appendChild(note);
  const actions = el("div", { className: "btn-group" });
  const dash = el("a", {
    className: "btn btn--primary",
    text: "Ir para o dashboard",
    attrs: { href: "./dashboard.html" },
  });
  const home = el("a", {
    className: "btn btn--secondary",
    text: "Voltar ao início",
    attrs: { href: "./index.html" },
  });
  const out = el("button", {
    className: "btn btn--ghost",
    text: "Sair desta conta",
    attrs: { type: "button" },
  });
  out.addEventListener("click", async () => {
    out.setAttribute("disabled", "");
    try {
      await logout();
    } finally {
      onLogout?.();
    }
  });
  actions.appendChild(dash);
  actions.appendChild(home);
  actions.appendChild(out);
  wrap.appendChild(actions);
  alreadyBox.appendChild(wrap);
  return true;
}

/**
 * Se houver sessão persistida, tenta restaurar e exibe o painel logado.
 * Retorna true quando o form deve ser ignorado (já autenticado).
 */
export async function restoreIfLoggedIn({ alreadyBox, form, feedbackBox }) {
  try {
    await restoreSession();
  } catch {
    return false;
  }
  return showAlreadyLoggedIn({
    alreadyBox,
    form,
    feedbackBox,
    onLogout: () => window.location.reload(),
  });
}
