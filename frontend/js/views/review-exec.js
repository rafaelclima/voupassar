/* VouPassar — execução da sessão de revisão (TASK 17.3)
 * Dono do `?review=<sessionId>` em simulado.html: caderno congelado da
 * sessão (`GET /review/sessions/{id}`) respondido via `POST /attempts`
 * com `mode=REVISAO` + `studySessionId`, com resultado em
 * `GET …/result` após encerrar (`POST /study-sessions/{id}/finish`).
 *
 * Chamado por views/simulado.js quando a URL traz ?review= (sem ?id=).
 * Reutiliza o visual do caderno do simulado (cartão, passagens, figuras,
 * matemática) e os mesmos componentes — só o fluxo é próprio da revisão:
 * - REVISAO nunca oculta: cada resposta mostra acerto/erro na hora (o POST
 *   devolve `isCorrect` revelado; a letra correta vem do detalhe da questão
 *   — `GET /questions/{id}` não mascara fora de PROVA em andamento — e só é
 *   usada *após* responder a posição, nunca como cola);
 * - `BLANK` conta como erro quando a questão não é anulada (regra do
 *   servidor, igual ao Estudo); anulada fica fora do aproveitamento;
 * - sem resposta = pendente, nunca erro inventado;
 * - ao final, "Voltar à fila" (a fila é recalculada no servidor — item
 *   consolidado some sozinho, sem recarregar nada aqui além de navegar).
 *
 * Sem innerHTML (só textContent via el()). `notes` da API é trilha de
 * auditoria: nunca vai para a tela (ver check_frontend.py). Erros com
 * traceId + retry por seção (padrão TASK 16.3). 401 → guard (o interceptor
 * do client.js já tentou 1 refresh silencioso).
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import {
  getSession,
  getSessionResult,
  submitReviewAttempt,
  finishSession,
  fetchQuestion,
} from "../api/review.js";
import { mountExpandableFigure } from "../components/figure.js";
import { renderPassages } from "../components/passage.js";
import { expressionNode } from "../components/math.js";
import {
  el,
  renderEmpty,
  renderErrorSummary,
  setButtonLoading,
  toast,
} from "../components/ui.js";
import {
  disciplineLabel,
  reviewCategoryLabel,
  reviewCategoryHint,
  choiceLabel,
  statusLabel,
} from "../vocab.js";
import {
  attemptVerdict,
  resultItemVerdict,
  formatReviewAccuracy,
  reviewProgressLabel,
  canFinishReview,
} from "./review-logic.js";

const errorBox = document.getElementById("sim-error");
const reviewTitle = document.getElementById("sim-review-title");
const reviewMeta = document.getElementById("sim-review-meta");
const reviewBody = document.getElementById("sim-review-body");

/* Estado da execução corrente (uma sessão por carga de página). */
const state = {
  sessionId: null,
  session: null,
  details: new Map(),
  answeredLocal: new Map(),
  startTimes: new Map(),
  showGuard: null,
  finishing: false,
};

/* Entrada (chamada por simulado.js). `showGuard` exibe o painel de acesso. */
export async function loadReviewExecution(sessionId, { showGuard } = {}) {
  state.sessionId = sessionId;
  state.showGuard = typeof showGuard === "function" ? showGuard : null;
  state.details = new Map();
  state.answeredLocal = new Map();
  state.startTimes = new Map();
  state.finishing = false;
  reviewMeta.textContent = "Carregando sessão…";
  reviewBody.textContent = "";
  try {
    const session = await getSession(sessionId);
    state.session = session;
    await renderExecution();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      state.showGuard?.();
      return;
    }
    reviewMeta.textContent = "Falha ao carregar a sessão.";
    const missing = err instanceof ApiError && err.status === 404;
    renderErrorSummary(errorBox, {
      title: missing ? "Sessão de revisão não encontrada" : "Não foi possível carregar a sessão de revisão",
      items: [missing
        ? "Ela pode ser de outra conta ou ter sido criada em outro fluxo. Volte à fila e inicie uma nova revisão."
        : friendlyMessage(err)],
      traceId: err instanceof ApiError ? err.traceId : null,
    });
    reviewBody.textContent = "";
    reviewBody.appendChild(backToQueueGroup(true));
    const retry = el("button", { className: "btn btn--secondary btn--sm", text: "Tentar de novo", attrs: { type: "button" } });
    retry.addEventListener("click", () => {
      errorBox.textContent = "";
      loadReviewExecution(state.sessionId, { showGuard: state.showGuard });
    });
    reviewBody.appendChild(retry);
  }
}

function reviewStatusLabel(status) {
  if (String(status || "").toUpperCase() === "FINISHED") return "Encerrada";
  return statusLabel(status) || "—";
}

function sessionRef(item) {
  if (item.sourceYear && item.sourceQuestionNumber) return `${item.sourceYear} Q${item.sourceQuestionNumber}`;
  return `Questão #${item.questionId}`;
}

function backToQueueGroup(primary) {
  const actions = el("div", { className: "btn-group mt-4" });
  actions.appendChild(el("a", {
    className: primary ? "btn btn--primary btn--sm" : "btn btn--secondary btn--sm",
    text: "Voltar à fila",
    attrs: { href: "./estudos.html?aba=revisao" },
  }));
  return actions;
}

function questionHref(questionId) {
  const back = `./simulado.html?review=${encodeURIComponent(String(state.sessionId))}`;
  return `./questao.html?id=${encodeURIComponent(String(questionId))}&voltar=${encodeURIComponent(back)}`;
}

async function renderExecution() {
  const s = state.session;
  const total = s?.totalItems ?? s?.items?.length ?? 0;
  const answered = countAnswered();
  const pending = Math.max(0, total - answered);
  reviewTitle.textContent = `Sessão de revisão #${s?.sessionId ?? state.sessionId}`;
  reviewMeta.textContent = `${total} ${total === 1 ? "questão" : "questões"} · ${answered} respondidas · ${pending} pendentes · ${reviewStatusLabel(s?.status)}`;
  document.title = `Revisão #${s?.sessionId ?? state.sessionId} — VouPassar`;

  reviewBody.textContent = "";
  const items = [...(s?.items ?? [])].sort((a, b) => a.position - b.position);
  if (items.length === 0) {
    renderEmpty(reviewBody, {
      title: "Sessão sem questões",
      description: "Esta sessão não trouxe o caderno congelado. Volte à fila e inicie uma nova revisão.",
      actionLabel: "Voltar à fila",
      onAction: () => {
        window.location.href = "./estudos.html?aba=revisao";
      },
    });
    return;
  }

  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Buscando enunciados do caderno…" }));
  reviewBody.appendChild(loading);

  const open = canFinishReview(s?.status);
  const settled = await Promise.allSettled(items.map((it) => fetchQuestion(it.questionId)));
  reviewBody.textContent = "";
  const caderno = el("div", { className: "sim-caderno" });
  settled.forEach((r, i) => {
    const item = items[i];
    if (r.status === "fulfilled") {
      state.details.set(item.questionId, r.value);
      state.startTimes.set(item.position, Date.now());
      caderno.appendChild(renderReviewCard(item, r.value, open));
    } else {
      caderno.appendChild(renderMissingReviewCard(item, r.reason, open));
    }
  });
  reviewBody.appendChild(caderno);

  if (open) {
    reviewBody.appendChild(renderProgressPanel(total));
    const note = el("p", { className: "muted mt-2" });
    note.appendChild(el("small", {
      text: "Revisão nunca oculta o resultado: cada resposta mostra acerto ou erro na hora. Encerrar libera o placar final.",
    }));
    reviewBody.appendChild(note);
  } else {
    await renderReviewResult();
  }
  reviewTitle.focus({ preventScroll: true });
}

function countAnswered() {
  const items = state.session?.items ?? [];
  let n = 0;
  for (const it of items) {
    if (state.answeredLocal.has(it.position) || it.answered) n += 1;
  }
  return n;
}

function updateProgressPanel() {
  const total = state.session?.totalItems ?? state.session?.items?.length ?? 0;
  const done = countAnswered();
  const text = document.getElementById("rev-progress-text");
  if (text) text.textContent = reviewProgressLabel(done, total);
  const fill = document.getElementById("rev-progress-fill");
  const bar = document.getElementById("rev-progress-bar");
  const pct = document.getElementById("rev-progress-pct");
  const ratio = total > 0 ? Math.round((done / total) * 100) : 0;
  if (fill) fill.style.width = `${ratio}%`;
  if (bar) bar.setAttribute("aria-valuenow", String(ratio));
  if (pct) pct.textContent = `${ratio}%`;
  const meta = reviewMeta.textContent;
  if (meta && !meta.startsWith("Falha")) {
    const pending = Math.max(0, total - done);
    reviewMeta.textContent = `${total} ${total === 1 ? "questão" : "questões"} · ${done} respondidas · ${pending} pendentes · ${reviewStatusLabel(state.session?.status)}`;
  }
}

/* ---------- cartão de revisão ---------- */

function cardHead(item, detail) {
  const head = el("div", { className: "sim-card__head" });
  head.appendChild(el("span", {
    className: "sim-card__pos",
    text: `Posição ${item.position}`,
    attrs: { id: `rev-p${item.position}-t` },
  }));
  head.appendChild(el("span", { className: "badge", text: sessionRef(item) }));
  const disc = disciplineLabel(item.disciplineCode, item.disciplineName || detail?.discipline?.name);
  if (disc) head.appendChild(el("span", { className: "badge", text: disc }));
  // Categoria da fila vigente (rótulo em português, nunca o enum cru). Fora
  // do top-100 vigente o item vem sem categoria — omite o selo em vez de
  // exibir "desconhecida" em cada cartão.
  if (item.reviewCategory) {
    head.appendChild(el("span", { className: "badge badge--primary", text: reviewCategoryLabel(item.reviewCategory) }));
  }
  if (item.wasAnnulled || detail?.annulled) {
    head.appendChild(el("span", { className: "badge badge--warning", text: "Anulada" }));
  }
  return head;
}

function topicLineOf(item, detail) {
  const name = item.topicName || detail?.topic?.name || "";
  if (!name) return "Assunto ainda sem classificação.";
  const sub = item.subtopicName || detail?.subtopic?.name || "";
  return sub ? `Conteúdo: ${name} · ${sub}.` : `Conteúdo: ${name}.`;
}

function renderReviewCard(item, detail, open) {
  const card = el("article", { className: "sim-card", attrs: { "aria-labelledby": `rev-p${item.position}-t` } });
  if (item.answered) card.dataset.answered = "1";
  card.appendChild(cardHead(item, detail));
  if (item.reviewCategory && reviewCategoryHint(item.reviewCategory)) {
    card.appendChild(el("p", { className: "review-item__hint", text: reviewCategoryHint(item.reviewCategory) }));
  }
  card.appendChild(el("p", { className: "sim-card__statement", text: detail?.statement || "(enunciado ainda não conferido)" }));
  if (detail) renderPassages(detail, card);
  if (detail?.hasFigure || (Array.isArray(detail?.figures) && detail.figures.length > 0)) {
    card.appendChild(mountExpandableFigure(detail, "sim-card__figure"));
  }

  const fieldset = el("fieldset", { className: "sim-options" });
  fieldset.appendChild(el("legend", { text: "Sua resposta (resultado imediato)" }));
  const group = `rev-${state.sessionId}-p${item.position}`;
  const options = Array.isArray(detail?.options) ? detail.options : [];
  if (options.length === 0) {
    fieldset.appendChild(el("p", { className: "muted", text: "Esta questão não tem alternativas registradas ainda." }));
  }
  for (const opt of options) {
    const label = el("label", { className: "sim-option" });
    const input = el("input", { attrs: { type: "radio", name: group, value: opt.label } });
    input.disabled = !open;
    label.appendChild(input);
    label.appendChild(el("span", { className: "sim-option__letter", text: `${opt.label})` }));
    label.appendChild(expressionNode(opt.text || ""));
    fieldset.appendChild(label);
  }
  card.appendChild(fieldset);

  const actions = el("div", { className: "btn-group" });
  const feedback = el("div", { className: "sim-feedback", attrs: { role: "status", hidden: "" } });
  if (open) {
    const btnAnswer = el("button", { className: "btn btn--primary btn--sm", text: "Responder", attrs: { type: "button" } });
    const btnBlank = el("button", { className: "btn btn--ghost btn--sm", text: "Em branco", attrs: { type: "button" } });
    if (options.length === 0) {
      btnAnswer.disabled = true;
      btnBlank.disabled = true;
    }
    actions.appendChild(btnAnswer);
    actions.appendChild(btnBlank);
    btnAnswer.addEventListener("click", () => {
      const checked = fieldset.querySelector("input:checked");
      answerPosition(card, fieldset, feedback, actions, item, detail, checked?.value || null, btnAnswer, btnBlank);
    });
    btnBlank.addEventListener("click", () => {
      answerPosition(card, fieldset, feedback, actions, item, detail, "BLANK", btnAnswer, btnBlank);
    });
    if (item.answered) {
      paintAnsweredState(card, fieldset, feedback, actions, item, detail, () => {
        state.startTimes.set(item.position, Date.now());
      });
    }
  } else {
    paintFinishedCard(card, fieldset, feedback, actions, item, detail);
  }
  card.appendChild(actions);
  card.appendChild(feedback);
  // Link de aprofundamento (questao.html mostra a questão com feedback de
  // estudo). Fica fora do fieldset/actions para não poluir o fluxo.
  const more = el("div", { className: "btn-group mt-2" });
  more.appendChild(el("a", {
    className: "btn btn--ghost btn--sm",
    text: "Abrir questão",
    attrs: { href: questionHref(item.questionId) },
  }));
  card.appendChild(more);
  return card;
}

function renderMissingReviewCard(item, reason, open) {
  const card = el("article", { className: "sim-card" });
  card.appendChild(el("span", { className: "sim-card__pos", text: `Posição ${item.position}` }));
  const box = el("div", { className: "alert alert--danger", attrs: { role: "alert" } });
  box.appendChild(el("strong", { text: "Não foi possível carregar esta questão. " }));
  box.appendChild(el("span", { text: friendlyMessage(reason) }));
  if (reason instanceof ApiError && reason.traceId) {
    box.appendChild(el("p", { className: "envelope mt-2", text: `Código de rastreio: ${reason.traceId}` }));
  }
  if (open) {
    const retry = el("button", { className: "btn btn--secondary btn--sm mt-2", text: "Tentar de novo", attrs: { type: "button" } });
    retry.addEventListener("click", async () => {
      retry.disabled = true;
      try {
        const detail = await fetchQuestion(item.questionId);
        state.details.set(item.questionId, detail);
        state.startTimes.set(item.position, Date.now());
        card.replaceWith(renderReviewCard(item, detail, true));
      } catch (err2) {
        toast(friendlyMessage(err2), "info");
        retry.disabled = false;
      }
    });
    box.appendChild(retry);
  }
  card.appendChild(box);
  return card;
}

/* Letra correta vinda do detalhe (A–D). "X"/ausente = sem letra a exibir:
 * nunca inventar gabarito na tela. */
function correctLetterOf(detail) {
  const key = String(detail?.answerKey || "").toUpperCase();
  return ["A", "B", "C", "D"].includes(key) ? key : "";
}

function paintFeedback(box, fieldset, { verdict, selected, letter, topicLine }) {
  box.textContent = "";
  box.hidden = false;
  box.dataset.tone = verdict.tone === "hit" || verdict.tone === "success" ? "success"
    : verdict.tone === "miss" || verdict.tone === "danger" ? "danger"
    : verdict.tone === "warning" || verdict.tone === "annulled" ? "warning" : "muted";
  const head = verdict.title;
  if (verdict.badge === "Anulada") {
    box.appendChild(el("strong", { text: head }));
    box.appendChild(el("p", { text: `Você marcou ${choiceLabel(selected)}. o IFRN não diz como pontuar questões anuladas.` }));
  } else if (verdict.badge === "Acertou") {
    box.appendChild(el("strong", { text: letter ? `Você acertou — alternativa ${letter}.` : head }));
    box.appendChild(el("p", { text: `Sua resposta: ${choiceLabel(selected)}.` }));
  } else if (verdict.badge === "Errou") {
    box.appendChild(el("strong", { text: letter ? `Não foi dessa vez — resposta correta: ${letter}.` : head }));
    box.appendChild(el("p", { text: `Você marcou ${choiceLabel(selected)}.` }));
  } else {
    box.appendChild(el("p", { text: head }));
  }
  if (letter) {
    fieldset.querySelectorAll(".sim-option").forEach((row) => {
      const v = row.querySelector("input")?.value;
      row.classList.remove("sim-option--correct", "sim-option--wrong");
      if (v === letter) row.classList.add("sim-option--correct");
      if (v === selected && selected !== letter) row.classList.add("sim-option--wrong");
    });
  }
  box.appendChild(el("p", { text: topicLine }));
}

async function answerPosition(card, fieldset, feedback, actions, item, detail, choice, btnAnswer, btnBlank) {
  const sel = choice === "BLANK" ? "BLANK" : choice;
  if (!sel) {
    toast("Escolha uma alternativa ou responda em branco.", "info");
    fieldset.querySelector("input")?.focus();
    return;
  }
  setButtonLoading(btnAnswer, true, "Registrando…");
  btnBlank.disabled = true;
  try {
    const elapsed = Math.max(0, Math.round((Date.now() - (state.startTimes.get(item.position) || Date.now())) / 1000));
    const attempt = await submitReviewAttempt({
      questionId: item.questionId,
      selectedOption: sel,
      studySessionId: state.sessionId,
      timeSpentSeconds: elapsed,
    });
    state.answeredLocal.set(item.position, sel);
    item.answered = true;
    item.selectedOption = attempt?.selectedOption ?? sel;
    item.isCorrect = attempt?.isCorrect ?? null;
    item.wasAnnulled = Boolean(attempt?.wasAnnulled);
    updateProgressPanel();
    paintAnsweredState(card, fieldset, feedback, actions, item, detail, () => {
      state.startTimes.set(item.position, Date.now());
    });
    toast(item.wasAnnulled ? "Resposta registrada — questão anulada, fora do aproveitamento." : "Resposta registrada.", "success");
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      state.showGuard?.();
      return;
    }
    if (err instanceof ApiError && err.code === "SESSION_CLOSED") {
      toast("Esta sessão já foi encerrada. Mostrando o resultado…", "info");
      await refreshToFinished();
      return;
    }
    toast(friendlyMessage(err), "info");
    btnBlank.disabled = false;
  } finally {
    setButtonLoading(btnAnswer, false);
    if (state.answeredLocal.get(item.position) || item.answered) {
      btnAnswer.disabled = true;
      btnBlank.disabled = true;
    }
  }
}

/* Posição já respondida (retomada ou resposta agora): trava, mostra o
 * feedback imediato e oferece nova tentativa (vale a última). */
function paintAnsweredState(card, fieldset, feedback, actions, item, detail, onRetry) {
  card.dataset.answered = "1";
  fieldset.querySelectorAll("input").forEach((r) => {
    r.disabled = true;
    if (item.selectedOption && r.value === item.selectedOption) r.checked = true;
  });
  // Sem resposta vinculada ainda (retomada parcial): mantém os botões.
  if (!item.selectedOption && !state.answeredLocal.get(item.position)) return;
  const verdict = attemptVerdict({ wasAnnulled: item.wasAnnulled, isCorrect: item.isCorrect ?? null });
  paintFeedback(feedback, fieldset, {
    verdict,
    selected: item.selectedOption,
    letter: verdict.badge === "Anulada" ? "" : correctLetterOf(detail),
    topicLine: topicLineOf(item, detail),
  });
  actions.querySelectorAll("button").forEach((b) => {
    b.disabled = true;
  });
  if (!actions.querySelector("[data-retry]")) {
    const retry = el("button", {
      className: "btn btn--secondary btn--sm",
      text: "Tentar novamente",
      attrs: { type: "button", "data-retry": "1" },
    });
    retry.addEventListener("click", () => {
      fieldset.querySelectorAll("input").forEach((r) => {
        r.disabled = false;
        r.checked = false;
      });
      fieldset.querySelectorAll(".sim-option").forEach((row) => {
        row.classList.remove("sim-option--correct", "sim-option--wrong");
      });
      feedback.hidden = true;
      feedback.textContent = "";
      actions.querySelectorAll("button:not([data-retry])").forEach((b) => {
        b.disabled = false;
      });
      retry.remove();
      onRetry?.();
      fieldset.querySelector("input")?.focus();
    });
    actions.appendChild(retry);
  }
}

/* Cartão somente-leitura (sessão encerrada): mostra a última resposta e o
 * veredito, sem botões de resposta. */
function paintFinishedCard(card, fieldset, feedback, actions, item, detail) {
  card.dataset.answered = item.answered ? "1" : "";
  fieldset.querySelectorAll("input").forEach((r) => {
    r.disabled = true;
    if (item.selectedOption && r.value === item.selectedOption) r.checked = true;
  });
  if (!item.answered) {
    feedback.hidden = false;
    feedback.dataset.tone = "muted";
    feedback.appendChild(el("p", { text: "Não respondida nesta sessão (pendente, nunca erro inventado)." }));
    return;
  }
  const verdict = attemptVerdict({ wasAnnulled: item.wasAnnulled, isCorrect: item.isCorrect ?? null });
  paintFeedback(feedback, fieldset, {
    verdict,
    selected: item.selectedOption,
    letter: verdict.badge === "Anulada" ? "" : correctLetterOf(detail),
    topicLine: topicLineOf(item, detail),
  });
}

/* ---------- progresso + encerrar ---------- */

function renderProgressPanel(total) {
  const panel = el("section", { className: "panel mt-4", attrs: { "aria-label": "Progresso da revisão" } });
  const head = el("div", { className: "panel__head" });
  const titleWrap = el("div");
  titleWrap.appendChild(el("h2", { text: "Seu progresso" }));
  const status = el("p");
  status.appendChild(el("span", {
    text: reviewProgressLabel(countAnswered(), total),
    attrs: { role: "status", id: "rev-progress-text" },
  }));
  titleWrap.appendChild(status);
  head.appendChild(titleWrap);
  panel.appendChild(head);
  const bar = el("div", {
    className: "progress",
    attrs: { role: "progressbar", "aria-label": "Progresso da revisão", "aria-valuemin": "0", "aria-valuemax": "100", "aria-valuenow": "0", id: "rev-progress-bar" },
  });
  const fill = el("div", { className: "progress__bar", attrs: { id: "rev-progress-fill" } });
  fill.style.width = "0%";
  bar.appendChild(fill);
  panel.appendChild(bar);
  const pill = el("p", { className: "muted mt-2" });
  pill.appendChild(el("small", { text: "—", attrs: { id: "rev-progress-pct" } }));
  panel.appendChild(pill);
  const actions = el("div", { className: "btn-group mt-4", attrs: { id: "rev-finish-actions" } });
  const btnFinish = el("button", { className: "btn btn--primary btn--sm", text: "Concluir revisão", attrs: { type: "button", id: "rev-finish" } });
  btnFinish.addEventListener("click", () => askFinishConfirm(actions));
  actions.appendChild(btnFinish);
  actions.appendChild(el("a", {
    className: "btn btn--ghost btn--sm",
    text: "Voltar à fila",
    attrs: { href: "./estudos.html?aba=revisao" },
  }));
  panel.appendChild(actions);
  updateProgressPanel();
  return panel;
}

/* Confirmação inline (sem dialog): evita encerrar com pendentes sem querer. */
function askFinishConfirm(actions) {
  const total = state.session?.totalItems ?? state.session?.items?.length ?? 0;
  const done = countAnswered();
  const pending = Math.max(0, total - done);
  actions.textContent = "";
  actions.appendChild(el("span", {
    text: pending > 0
      ? `Concluir com ${done} ${done === 1 ? "respondida" : "respondidas"} e ${pending} ${pending === 1 ? "pendente" : "pendentes"}? Pendentes não contam como erro.`
      : `Concluir com ${done} ${done === 1 ? "respondida" : "respondidas"}?`,
  }));
  const yes = el("button", { className: "btn btn--primary btn--sm", text: "Concluir agora", attrs: { type: "button" } });
  const no = el("button", { className: "btn btn--ghost btn--sm", text: "Continuar respondendo", attrs: { type: "button" } });
  yes.addEventListener("click", () => doFinish(yes, no));
  no.addEventListener("click", () => {
    actions.textContent = "";
    const btn = el("button", { className: "btn btn--primary btn--sm", text: "Concluir revisão", attrs: { type: "button", id: "rev-finish" } });
    btn.addEventListener("click", () => askFinishConfirm(actions));
    actions.appendChild(btn);
    actions.appendChild(el("a", {
      className: "btn btn--ghost btn--sm",
      text: "Voltar à fila",
      attrs: { href: "./estudos.html?aba=revisao" },
    }));
    btn.focus();
  });
  actions.appendChild(yes);
  actions.appendChild(no);
  yes.focus();
}

async function doFinish(yes, no) {
  setButtonLoading(yes, true, "Corrigindo…");
  no.disabled = true;
  try {
    await finishSession(state.sessionId);
    toast("Revisão concluída. Resultado abaixo.", "success");
    await refreshToFinished();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      state.showGuard?.();
      return;
    }
    if (err instanceof ApiError && err.code === "SESSION_CLOSED") {
      await refreshToFinished();
      return;
    }
    toast(friendlyMessage(err), "info");
    setButtonLoading(yes, false);
    no.disabled = false;
  }
}

async function refreshToFinished() {
  try {
    state.session = await getSession(state.sessionId);
    await renderExecution();
  } catch (err) {
    toast(friendlyMessage(err), "info");
  }
}

/* ---------- resultado ---------- */

async function renderReviewResult() {
  const section = el("section", {
    className: "panel panel--result mt-4",
    attrs: { "aria-labelledby": "rev-result-t" },
  });
  const head = el("div", { className: "panel__head" });
  const titleWrap = el("div");
  titleWrap.appendChild(el("h2", { text: "Resultado", attrs: { id: "rev-result-t", tabindex: "-1" } }));
  titleWrap.appendChild(el("p", { text: "Correção do servidor para esta sessão." }));
  head.appendChild(titleWrap);
  head.appendChild(el("span", { className: "panel__icon", attrs: { "aria-hidden": "true" }, text: "★" }));
  section.appendChild(head);
  const box = el("div", { attrs: { role: "status" } });
  box.appendChild(el("span", { text: "Calculando resultado no servidor…" }));
  section.appendChild(box);
  reviewBody.appendChild(section);
  try {
    const res = await getSessionResult(state.sessionId);
    box.textContent = "";
    renderReviewScore(box, res);
    renderReviewResultItems(box, res);
    const note = el("p", { className: "muted mt-2" });
    note.appendChild(el("small", {
      text: "A fila foi recalculada com estas respostas: volte para ver os próximos itens (erros consolidados perdem prioridade e somem da visão só-erros).",
    }));
    box.appendChild(note);
    box.appendChild(backToQueueGroup(true));
    section.scrollIntoView({ block: "start" });
    section.querySelector("#rev-result-t")?.focus?.({ preventScroll: true });
  } catch (err) {
    box.textContent = "";
    if (err instanceof ApiError && err.code === "REVIEW_NOT_FINISHED") {
      box.appendChild(el("p", { text: "Sessão ainda em andamento: conclua a revisão para ver o placar final." }));
      box.appendChild(backToQueueGroup(false));
      return;
    }
    const alert = el("div", { className: "alert alert--danger", attrs: { role: "alert" } });
    alert.appendChild(el("strong", { text: "Não foi possível carregar o resultado. " }));
    alert.appendChild(el("span", { text: friendlyMessage(err) }));
    if (err instanceof ApiError && err.traceId) {
      alert.appendChild(el("p", { className: "envelope mt-2", text: `Código de rastreio: ${err.traceId}` }));
    }
    box.appendChild(alert);
    box.appendChild(backToQueueGroup(false));
  }
}

function renderReviewScore(box, res) {
  const grid = el("div", { className: "sim-score" });
  const cells = [
    [`${res.correct ?? 0}/${res.scored ?? 0}`, "corretas / pontuáveis"],
    [formatReviewAccuracy(res.accuracy), "aproveitamento"],
    [`${res.answered ?? 0}/${res.total ?? 0}`, "respondidas / total"],
    [`${res.annulled ?? 0}`, "anuladas (fora do cálculo)"],
  ];
  for (const [value, label] of cells) {
    const cell = el("div", { className: "sim-score__cell" });
    cell.appendChild(el("span", { className: "sim-score__value", text: value }));
    cell.appendChild(el("span", { className: "sim-score__label", text: label }));
    grid.appendChild(cell);
  }
  box.appendChild(grid);
  const note = el("p", { className: "muted mt-2" });
  note.appendChild(el("small", {
    text: "Vale a última resposta em cada questão. Questão anulada pelo IFRN não entra no aproveitamento.",
  }));
  box.appendChild(note);
}

function renderReviewResultItems(box, res) {
  const items = [...(res.items ?? [])].sort((a, b) => a.position - b.position);
  if (items.length === 0) return;
  box.appendChild(el("h3", { text: "Correção por posição", attrs: { style: "font-size:var(--text-md)", class: "mt-4" } }));
  for (const it of items) {
    const verdict = resultItemVerdict({ wasAnnulled: it.wasAnnulled, unanswered: it.unanswered, isCorrect: it.isCorrect ?? null });
    const toneClass = verdict.tone === "hit" ? "sim-result-item--hit"
      : verdict.tone === "miss" ? "sim-result-item--miss"
      : verdict.tone === "warning" ? "sim-result-item--annulled" : "sim-result-item--pending";
    const badgeClass = verdict.tone === "hit" ? "badge badge--success"
      : verdict.tone === "miss" ? "badge badge--danger"
      : verdict.tone === "warning" ? "badge badge--warning" : "badge";
    const row = el("div", { className: `sim-result-item ${toneClass}` });
    const title = it.sourceYear && it.sourceQuestionNumber
      ? `Posição ${it.position} · ${it.sourceYear} Q${it.sourceQuestionNumber}`
      : `Posição ${it.position}`;
    row.appendChild(el("strong", { text: title }));
    row.appendChild(el("span", { className: badgeClass, text: verdict.badge }));
    let detail = verdict.title;
    if (!it.unanswered && !it.wasAnnulled && it.selectedOption) {
      detail += ` Você marcou ${choiceLabel(it.selectedOption)}.`;
    } else if (it.wasAnnulled && it.selectedOption) {
      detail += ` Você marcou ${choiceLabel(it.selectedOption)}.`;
    }
    row.appendChild(el("span", { text: detail }));
    box.appendChild(row);
  }
}
