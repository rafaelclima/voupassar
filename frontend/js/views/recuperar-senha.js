/* VouPassar — recuperar senha (TASK 6.3)
 * POST /api/v1/auth/password/forgot. Resposta sempre genérica
 * (anti-enumeração): o sucesso exibido é idêntico exista ou não a conta.
 */

import { forgotPassword } from "../api/auth.js";
import { el, setButtonLoading, setFieldError, renderErrorSummary } from "../components/ui.js";
import {
  validateEmail,
  renderAuthError,
  clearFieldErrors,
} from "./auth-shared.js";

const form = document.getElementById("forgot-form");
const feedback = document.getElementById("auth-feedback");
const email = document.getElementById("forgot-email");
const submit = document.getElementById("forgot-submit");

function showGenericSuccess() {
  form.hidden = true;
  feedback.textContent = "";
  const wrap = el("div", { className: "auth-success" });
  const alert = el("div", { className: "alert alert--success", attrs: { role: "status" } });
  const inner = el("div");
  inner.appendChild(el("strong", { text: "Pedido recebido." }));
  inner.appendChild(
    el("p", {
      text: "Se houver uma conta ativa com esse e-mail, um link de redefinição foi emitido (uso único, válido por 60 minutos).",
    })
  );
  alert.appendChild(inner);
  wrap.appendChild(alert);
  const actions = el("div", { className: "btn-group" });
  const reset = el("a", {
    className: "btn btn--secondary",
    text: "Ir para redefinir senha",
    attrs: { href: "./redefinir-senha.html" },
  });
  const login = el("a", {
    className: "btn btn--ghost",
    text: "Voltar para entrar",
    attrs: { href: "./login.html" },
  });
  actions.appendChild(reset);
  actions.appendChild(login);
  wrap.appendChild(actions);
  feedback.appendChild(wrap);
}

form?.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearFieldErrors({ email });
  feedback.textContent = "";

  const emailErr = validateEmail(email.value);
  if (emailErr) {
    setFieldError(email, emailErr);
    renderErrorSummary(feedback, { title: "Verifique os campos", items: [emailErr] });
    email.focus();
    return;
  }

  setButtonLoading(submit, true, "Enviando…");
  try {
    await forgotPassword(email.value);
    showGenericSuccess();
  } catch (err) {
    // Erro de validação do servidor (e-mail malformado) cai aqui;
    // qualquer outro vira resumo com rastreio — nunca vaza existência.
    renderAuthError(feedback, err, { email });
  } finally {
    setButtonLoading(submit, false);
  }
});
