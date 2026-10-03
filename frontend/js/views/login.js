/* VouPassar — entrar (TASK 6.3)
 * POST /api/v1/auth/login. Falha sempre genérica (anti-enumeração).
 * Pós-login: segue ?next= seguro ou exibe painel logado (dashboard: 6.4).
 */

import { ApiError } from "../api/client.js";
import { login } from "../api/auth.js";
import { setButtonLoading, setFieldError, renderErrorSummary } from "../components/ui.js";
import {
  validateEmail,
  wirePasswordToggles,
  authErrorMessage,
  renderAuthError,
  clearFieldErrors,
  safeNextParam,
  restoreIfLoggedIn,
} from "./auth-shared.js";

const form = document.getElementById("login-form");
const feedback = document.getElementById("auth-feedback");
const already = document.getElementById("auth-already");
const email = document.getElementById("login-email");
const password = document.getElementById("login-password");
const submit = document.getElementById("login-submit");

wirePasswordToggles(document);
restoreIfLoggedIn({ alreadyBox: already, form, feedbackBox: feedback });

function showLoggedSuccess() {
  // Login verificado segue direto para o dashboard (ou ?next= seguro).
  // Só erro aparece nesta tela.
  const next = safeNextParam();
  window.location.href = next || "./dashboard.html";
}

form?.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearFieldErrors({ email, password });
  feedback.textContent = "";

  let firstInvalid = null;
  const emailErr = validateEmail(email.value);
  if (emailErr) {
    setFieldError(email, emailErr);
    firstInvalid = firstInvalid || email;
  }
  if (!password.value) {
    setFieldError(password, "Informe sua senha.");
    firstInvalid = firstInvalid || password;
  }
  if (firstInvalid) {
    renderErrorSummary(feedback, {
      title: "Verifique os campos",
      items: ["Corrija os campos destacados e tente de novo."],
    });
    firstInvalid.focus();
    return;
  }

  const remember = form.elements.namedItem("remember")?.checked ?? false;
  setButtonLoading(submit, true, "Entrando…");
  try {
    await login({ email: email.value, password: password.value }, { remember });
    showLoggedSuccess();
  } catch (err) {
    if (err instanceof ApiError && err.code === "INVALID_CREDENTIALS") {
      // Genérico de propósito: não marcar campo específico.
      renderErrorSummary(feedback, { title: authErrorMessage(err), traceId: err.traceId });
      password.value = "";
      password.focus();
      return;
    }
    renderAuthError(feedback, err, { email, password, newPassword: password });
  } finally {
    setButtonLoading(submit, false);
  }
});
