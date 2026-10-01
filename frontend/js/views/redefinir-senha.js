/* VouPassar — redefinir senha (TASK 6.3)
 * POST /api/v1/auth/password/reset. Token vem de ?token= (link do e-mail)
 * ou colado manualmente. Confirmação de senha é só local (não enviada).
 * Sucesso derruba todas as sessões — usuário entra de novo no login.
 */

import { ApiError } from "../api/client.js";
import { resetPassword } from "../api/auth.js";
import { el, setButtonLoading, setFieldError, renderErrorSummary } from "../components/ui.js";
import {
  validatePassword,
  wirePasswordToggles,
  authErrorMessage,
  renderAuthError,
  clearFieldErrors,
} from "./auth-shared.js";

const form = document.getElementById("reset-form");
const feedback = document.getElementById("auth-feedback");
const token = document.getElementById("reset-token");
const password = document.getElementById("reset-password");
const confirm = document.getElementById("reset-confirm");
const submit = document.getElementById("reset-submit");

wirePasswordToggles(document);

// Preenche ?token= automaticamente (link do e-mail de recuperação).
try {
  const fromUrl = new URLSearchParams(window.location.search).get("token")?.trim() || "";
  if (fromUrl && !token.value) token.value = fromUrl;
} catch {
  // URL ilegível: usuário cola manualmente.
}

function showResetSuccess() {
  form.hidden = true;
  feedback.textContent = "";
  const wrap = el("div", { className: "auth-success" });
  const alert = el("div", { className: "alert alert--success", attrs: { role: "status" } });
  const inner = el("div");
  inner.appendChild(el("strong", { text: "Senha redefinida." }));
  inner.appendChild(
    el("p", { text: "Todas as sessões foram encerradas por segurança. Entre com a nova senha." })
  );
  alert.appendChild(inner);
  wrap.appendChild(alert);
  const actions = el("div", { className: "btn-group" });
  const login = el("a", {
    className: "btn btn--primary",
    text: "Ir para entrar",
    attrs: { href: "./login.html" },
  });
  actions.appendChild(login);
  wrap.appendChild(actions);
  feedback.appendChild(wrap);
}

form?.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearFieldErrors({ token, password, confirm });
  feedback.textContent = "";

  const errors = [];
  if (!token.value.trim()) {
    setFieldError(token, "Informe o token de recuperação.");
    errors.push("Token obrigatório.");
  }
  const passErr = validatePassword(password.value);
  if (passErr) {
    setFieldError(password, passErr);
    errors.push(passErr);
  }
  if (!confirm.value) {
    setFieldError(confirm, "Repita a nova senha.");
    errors.push("Confirmação obrigatória.");
  } else if (confirm.value !== password.value) {
    setFieldError(confirm, "As senhas não coincidem.");
    errors.push("As senhas não coincidem.");
  }
  if (errors.length) {
    renderErrorSummary(feedback, { title: "Verifique os campos", items: errors });
    const first = [token, password, confirm].find((i) => i.hasAttribute("aria-invalid"));
    first?.focus();
    return;
  }

  setButtonLoading(submit, true, "Redefinindo…");
  try {
    await resetPassword({ token: token.value, newPassword: password.value });
    password.value = "";
    confirm.value = "";
    showResetSuccess();
  } catch (err) {
    if (err instanceof ApiError && err.code === "INVALID_OR_EXPIRED_TOKEN") {
      setFieldError(token, "Token inválido, expirado ou já usado.");
      renderErrorSummary(feedback, {
        title: authErrorMessage(err),
        items: ["Peça um novo link em Recuperar senha."],
        traceId: err.traceId,
      });
      token.focus();
      return;
    }
    renderAuthError(feedback, err, { token, newPassword: password });
  } finally {
    setButtonLoading(submit, false);
  }
});
