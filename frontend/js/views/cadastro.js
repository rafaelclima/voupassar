/* VouPassar — criar conta (TASK 6.3)
 * POST /api/v1/auth/register. 409 EMAIL_IN_USE orienta para login/recuperação.
 * Campos opcionais (ano escolar, ano da prova, objetivo) seguem o backend.
 */

import { ApiError } from "../api/client.js";
import { register } from "../api/auth.js";
import { el, setButtonLoading, setFieldError, renderErrorSummary } from "../components/ui.js";
import {
  validateEmail,
  validatePassword,
  validateDisplayName,
  validateTargetYear,
  wirePasswordToggles,
  authErrorMessage,
  renderAuthError,
  clearFieldErrors,
  restoreIfLoggedIn,
} from "./auth-shared.js";
import { getState } from "../state/store.js";

const form = document.getElementById("register-form");
const feedback = document.getElementById("auth-feedback");
const already = document.getElementById("auth-already");
const nameInput = document.getElementById("reg-name");
const email = document.getElementById("reg-email");
const password = document.getElementById("reg-password");
const school = document.getElementById("reg-school");
const target = document.getElementById("reg-target");
const goal = document.getElementById("reg-goal");
const submit = document.getElementById("register-submit");

wirePasswordToggles(document);
restoreIfLoggedIn({ alreadyBox: already, form, feedbackBox: feedback });

function showCreated() {
  const user = getState().user;
  form.hidden = true;
  feedback.textContent = "";
  const wrap = el("div", { className: "auth-success" });
  const alert = el("div", { className: "alert alert--success", attrs: { role: "status" } });
  const inner = el("div");
  inner.appendChild(el("strong", { text: "Conta criada com sucesso." }));
  inner.appendChild(
    el("p", { text: `Bem-vindo, ${user?.displayName || "estudante"}. Você já está conectado.` })
  );
  alert.appendChild(inner);
  wrap.appendChild(alert);
  const note = el("p", { className: "auth-meta" });
  note.textContent = "Sua conta já está ativa. Abra o dashboard para ver seu diagnóstico inicial e gerar o roteiro.";
  wrap.appendChild(note);
  const actions = el("div", { className: "btn-group" });
  const dash = el("a", {
    className: "btn btn--primary",
    text: "Ir para o dashboard",
    attrs: { href: "./dashboard.html" },
  });
  actions.appendChild(dash);
  wrap.appendChild(actions);
  feedback.appendChild(wrap);
}

form?.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearFieldErrors({ nameInput, email, password, school, target, goal });
  feedback.textContent = "";

  const errors = [];
  const nameErr = validateDisplayName(nameInput.value);
  if (nameErr) {
    setFieldError(nameInput, nameErr);
    errors.push(nameErr);
  }
  const emailErr = validateEmail(email.value);
  if (emailErr) {
    setFieldError(email, emailErr);
    errors.push(emailErr);
  }
  const passErr = validatePassword(password.value);
  if (passErr) {
    setFieldError(password, passErr);
    errors.push(passErr);
  }
  if (school.value.trim().length > 40) {
    setFieldError(school, "Ano escolar deve ter até 40 caracteres.");
    errors.push("Ano escolar inválido.");
  }
  const targetErr = validateTargetYear(target.value);
  if (targetErr) {
    setFieldError(target, targetErr);
    errors.push(targetErr);
  }
  if (goal.value.trim().length > 500) {
    setFieldError(goal, "Objetivo deve ter até 500 caracteres.");
    errors.push("Objetivo inválido.");
  }
  if (errors.length) {
    renderErrorSummary(feedback, { title: "Verifique os campos", items: errors });
    const first = [nameInput, email, password, school, target, goal].find((i) =>
      i.hasAttribute("aria-invalid")
    );
    first?.focus();
    return;
  }

  setButtonLoading(submit, true, "Criando conta…");
  try {
    await register(
      {
        email: email.value,
        password: password.value,
        displayName: nameInput.value,
        schoolYear: school.value,
        targetYear: target.value.trim() ? Number(target.value) : undefined,
        studyGoal: goal.value,
      },
      { remember: false }
    );
    password.value = "";
    showCreated();
  } catch (err) {
    if (err instanceof ApiError && err.code === "EMAIL_IN_USE") {
      setFieldError(email, "Este e-mail já está cadastrado.");
      renderErrorSummary(feedback, {
        title: authErrorMessage(err),
        items: ["Tente entrar ou recupere a senha."],
        traceId: err.traceId,
      });
      email.focus();
      return;
    }
    renderAuthError(
      feedback,
      err,
      { displayName: nameInput, email, password, schoolYear: school, targetYear: target, studyGoal: goal }
    );
  } finally {
    setButtonLoading(submit, false);
  }
});
