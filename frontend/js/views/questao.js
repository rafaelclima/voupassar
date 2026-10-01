/* VouPassar — tela de questão dedicada (TASK 6.6)
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=questao.html?id=...).
 *
 * Cobre TASKS.md 6.6 (leitura, seleção, confirmação, feedback, explicação):
 * - Leitura: detalhe integral via GET /questions/{id} (3.4) — enunciado,
 *   alternativas, proveniência e assunto vigente (taxonomia v1.1).
 * - Seleção: radios nativos em fieldset/legend, alvos ≥44px.
 * - Confirmação: Responder / Responder em branco (BLANK), com loading e
 *   validação de seleção vazia (foco + toast, nunca envio silencioso).
 * - Feedback: correção IMEDIATA do servidor via POST /attempts (3.7,
 *   modo ESTUDO, AGENTS.md §9) — acerto/erro, resposta correta, assunto.
 * - Explicação: seção dedicada com o texto redigido ou NECESSITA REVISÃO
 *   (nunca texto inventado).
 *
 * URL: ?id=<long> obrigatório; ?voltar= aceita só caminho relativo ./…
 * (whitelist anti-open-redirect; default ./estudos.html).
 * Sem innerHTML (só textContent via el()). 404 vira erro visível com
 * traceId + links de saída, nunca página vazia silenciosa.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { restoreSession, logout } from "../api/auth.js";
import { fetchQuestion, openStudySession, submitAttempt } from "../api/questao.js";
import { el, renderErrorSummary, setButtonLoading, toast } from "../components/ui.js";

const SESSION_KEY = "voupassar.studySessionId";

const guard = document.getElementById("questao-guard");
const errorBox = document.getElementById("questao-error");
const loadingBox = document.getElementById("questao-loading");
const content = document.getElementById("questao-content");
const backLink = document.getElementById("questao-back");

const titleEl = document.getElementById("questao-title");
const metaEl = document.getElementById("questao-meta");
const badgesEl = document.getElementById("questao-badges");
const statementEl = document.getElementById("questao-statement");
const figureEl = document.getElementById("questao-figure");
const topicEl = document.getElementById("questao-topic");

const form = document.getElementById("questao-form");
const fieldset = document.getElementById("questao-options");
const hintEl = document.getElementById("questao-hint");
const btnSubmit = document.getElementById("questao-submit");
const btnBlank = document.getElementById("questao-blank");
const btnReset = document.getElementById("questao-reset");

const feedbackBox = document.getElementById("questao-feedback");
const feedbackEmpty = document.getElementById("questao-feedback-empty");
const explanationBox = document.getElementById("questao-explanation");
const sourceBox = document.getElementById("questao-source");
const notesBox = document.getElementById("questao-notes");

const state = {
  user: null,
  questionId: null,
  question: null,
  studySessionId: null,
  startedAt: Date.now(),
  answered: false,
};

wireLogoutButtons();
main();

async function main() {
  applyBackLink();
  const id = readQuestionId();
  if (id === null) {
    loadingBox.hidden = true;
    renderErrorSummary(errorBox, {
      title: "Questão não informada",
      items: ["Abra esta página com ?id= — por exemplo, a partir da área de estudos."],
    });
    appendErrorExit();
    return;
  }
  state.questionId = id;

  const user = await restoreSession().catch(() => null);
  if (!user) {
    showGuard(id);
    return;
  }
  state.user = user;
  showLogoutButtons();
  state.studySessionId = readStoredSession();
  await loadQuestion(id);
}

function wireLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      btn.setAttribute("disabled", "");
      try {
        await logout();
      } finally {
        window.location.href = `./login.html?next=${encodeURIComponent(nextParam())}`;
      }
    });
  });
}

function showLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.hidden = false;
  });
}

function nextParam() {
  return state.questionId ? `questao.html?id=${encodeURIComponent(String(state.questionId))}` : "questao.html";
}

function showGuard(id) {
  loadingBox.hidden = true;
  content.hidden = true;
  guard.hidden = false;
  guard.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: "Entre para resolver questões" }));
  box.appendChild(
    el("p", {
      text: "A tela de questão mostra o enunciado e registra sua resposta com correção imediata. Ela precisa da sua sessão — entre ou crie uma conta para continuar.",
    }),
  );
  const actions = el("div", { className: "btn-group", attrs: { style: "justify-content:center" } });
  actions.appendChild(
    el("a", {
      className: "btn btn--primary",
      text: "Entrar",
      attrs: { href: `./login.html?next=${encodeURIComponent(`questao.html?id=${id}`)}` },
    }),
  );
  actions.appendChild(
    el("a", {
      className: "btn btn--secondary",
      text: "Criar conta",
      attrs: { href: "./cadastro.html" },
    }),
  );
  box.appendChild(actions);
  guard.appendChild(box);
}

/* ---------- URL ---------- */

function readQuestionId() {
  let raw = "";
  try {
    raw = new URLSearchParams(window.location.search).get("id") || "";
  } catch {
    raw = "";
  }
  const n = Number.parseInt(String(raw).trim(), 10);
  return Number.isFinite(n) && n > 0 ? n : null;
}

/** Só aceita voltar relativo ./… (anti open-redirect); default ./estudos.html. */
function sanitizeBack(raw) {
  const v = String(raw || "").trim();
  if (!v.startsWith("./")) return "./estudos.html";
  if (v.includes("//") || v.includes("\\") || /https?:/i.test(v)) return "./estudos.html";
  if (/[\s<>"]/.test(v)) return "./estudos.html";
  if (v.length > 400) return "./estudos.html";
  return v;
}

function applyBackLink() {
  let voltar = "./estudos.html";
  try {
    voltar = sanitizeBack(new URLSearchParams(window.location.search).get("voltar"));
  } catch {
    voltar = "./estudos.html";
  }
  backLink.setAttribute("href", voltar);
}

/* ---------- carga ---------- */

async function loadQuestion(id) {
  loadingBox.hidden = false;
  content.hidden = true;
  errorBox.textContent = "";
  try {
    const q = await fetchQuestion(id);
    state.question = q;
    state.startedAt = Date.now();
    renderAll(q);
    loadingBox.hidden = true;
    content.hidden = false;
    titleEl.focus({ preventScroll: true });
  } catch (err) {
    loadingBox.hidden = true;
    if (err instanceof ApiError && err.status === 401) {
      showGuard(id);
      return;
    }
    const notFound = err instanceof ApiError && err.status === 404;
    renderErrorSummary(errorBox, {
      title: notFound ? "Questão não encontrada" : "Não foi possível carregar a questão",
      items: [friendlyMessage(err)],
      traceId: err instanceof ApiError ? err.traceId : null,
    });
    appendErrorExit();
  }
}

function appendErrorExit() {
  const actions = el("div", { className: "btn-group mt-4" });
  actions.appendChild(
    el("a", { className: "btn btn--secondary btn--sm", text: "Voltar para estudos", attrs: { href: "./estudos.html" } }),
  );
  actions.appendChild(
    el("a", { className: "btn btn--ghost btn--sm", text: "Abrir dashboard", attrs: { href: "./dashboard.html" } }),
  );
  errorBox.appendChild(actions);
}

/* ---------- render: leitura ---------- */

function questionTitle(q) {
  const parts = [];
  if (q.examYear && q.questionNumber) parts.push(`${q.examYear} Q${q.questionNumber}`);
  else if (q.questionNumber) parts.push(`Q${q.questionNumber}`);
  else parts.push(`Questão #${q.id}`);
  if (q.discipline?.name) parts.push(q.discipline.name);
  return parts.join(" · ");
}

function sourceLabel(q) {
  if (q.sourceType === "OFFICIAL") return "Oficial IFRN";
  if (q.sourceType) return `Não-oficial (${q.sourceType}) — nunca é do IFRN`;
  return "Origem desconhecida";
}

function renderAll(q) {
  document.title = `${questionTitle(q)} — VouPassar`;
  titleEl.textContent = questionTitle(q);

  const metaBits = [];
  if (q.examYear && q.questionNumber) metaBits.push(`Prova ${q.examYear}, questão ${q.questionNumber}`);
  else metaBits.push(`Registro #${q.id}`);
  if (q.discipline?.name) metaBits.push(q.discipline.name);
  if (typeof q.pageStart === "number") {
    metaBits.push(q.pageEnd && q.pageEnd !== q.pageStart ? `páginas ${q.pageStart}–${q.pageEnd} do PDF-fonte` : `página ${q.pageStart} do PDF-fonte`);
  }
  metaEl.textContent = "";
  metaEl.appendChild(el("small", { text: metaBits.join(" · ") }));

  badgesEl.textContent = "";
  badgesEl.appendChild(el("span", { className: "badge", text: sourceLabel(q) }));
  if (q.annulled) {
    badgesEl.appendChild(el("span", { className: "badge badge--warning", text: "Anulada" }));
  } else if (q.difficultyEstimate) {
    badgesEl.appendChild(el("span", { className: "badge", text: `Dificuldade ${q.difficultyEstimate} (estimativa, confiança BAIXA)` }));
  }
  if (q.classificationStatus) {
    badgesEl.appendChild(el("span", { className: "badge", text: `Classificação ${q.classificationStatus}` }));
  }

  statementEl.textContent = q.statement || "(enunciado ausente — NECESSITA REVISÃO)";
  figureEl.hidden = !q.hasFigure;

  topicEl.textContent = "";
  topicEl.appendChild(
    el("small", {
      text: q.topic?.name
        ? `Assunto: ${q.topic.name}${q.subtopic?.name ? ` · ${q.subtopic.name}` : ""} (classificação derivada, revisão humana pendente)`
        : "Assunto: NÃO CONFIRMADO — classificação pendente de revisão.",
    }),
  );

  renderOptions(q);
  renderExplanationPending(q);
  renderSource(q);
  renderNotes(q);
}

function renderOptions(q) {
  // Mantém a legend; reconstrói as alternativas.
  const legend = fieldset.querySelector("legend");
  fieldset.textContent = "";
  if (legend) fieldset.appendChild(legend);
  else fieldset.appendChild(el("legend", { text: "Selecione uma alternativa e confirme" }));

  const group = `questao-${q.id}-opt`;
  const options = Array.isArray(q.options) ? q.options : [];
  if (options.length === 0) {
    fieldset.appendChild(el("p", { className: "muted", text: "Sem alternativas registradas — NECESSITA REVISÃO." }));
  }
  for (const opt of options) {
    const label = el("label", { className: "questao-option" });
    const input = el("input", { attrs: { type: "radio", name: group, value: opt.label } });
    input.disabled = Boolean(q.annulled);
    label.appendChild(input);
    label.appendChild(el("span", { className: "questao-option__letter", text: `${opt.label})` }));
    label.appendChild(el("span", { text: opt.text || "" }));
    fieldset.appendChild(label);
  }
  if (q.annulled) {
    fieldset.appendChild(
      el("p", { className: "muted", text: "Questão anulada pelo gabarito: conta como conteúdo respondido e fica fora do aproveitamento (regra de pontuação DESCONHECIDA)." }),
    );
    btnSubmit.disabled = true;
    btnBlank.disabled = true;
    hintEl.textContent = "";
    hintEl.appendChild(el("small", { text: "Anulada — resposta desabilitada. Leia o enunciado e a origem abaixo." }));
  } else {
    btnSubmit.disabled = false;
    btnBlank.disabled = false;
  }
  btnReset.hidden = true;
  state.answered = false;
}

/* ---------- seleção + confirmação ---------- */

form.addEventListener("submit", (e) => {
  e.preventDefault();
  const checked = fieldset.querySelector("input:checked");
  if (!checked) {
    toast("Escolha uma alternativa ou responda em branco.", "info");
    hintEl.textContent = "";
    hintEl.appendChild(el("small", { text: "Nenhuma alternativa selecionada — escolha uma acima ou use “Responder em branco”." }));
    fieldset.querySelector("input")?.focus();
    return;
  }
  answer(checked.value);
});

btnBlank.addEventListener("click", () => answer("BLANK"));

btnReset.addEventListener("click", () => {
  fieldset.querySelectorAll("input").forEach((r) => {
    r.disabled = Boolean(state.question?.annulled);
    r.checked = false;
  });
  fieldset.querySelectorAll(".questao-option").forEach((row) => {
    row.classList.remove("questao-option--correct", "questao-option--wrong");
  });
  feedbackBox.hidden = true;
  feedbackBox.textContent = "";
  feedbackBox.removeAttribute("data-tone");
  feedbackEmpty.hidden = false;
  btnSubmit.disabled = Boolean(state.question?.annulled);
  btnBlank.disabled = Boolean(state.question?.annulled);
  btnReset.hidden = true;
  state.answered = false;
  state.startedAt = Date.now();
  renderExplanationPending(state.question);
  fieldset.querySelector("input")?.focus();
});

async function answer(selected) {
  const q = state.question;
  if (!q || state.answered || q.annulled) return;
  const choice = selected === "BLANK" ? "BLANK" : selected;
  setButtonLoading(btnSubmit, true, "Corrigindo…");
  btnBlank.disabled = true;
  try {
    const sessionId = await ensureStudySession();
    const elapsed = Math.max(0, Math.round((Date.now() - state.startedAt) / 1000));
    let attempt;
    try {
      attempt = await submitAttempt({
        questionId: q.id,
        selectedOption: choice,
        timeSpentSeconds: elapsed > 0 ? elapsed : 0,
        studySessionId: sessionId,
      });
    } catch (err) {
      if (err instanceof ApiError && err.code === "SESSION_CLOSED") {
        const fresh = await openStudySession();
        state.studySessionId = fresh?.id ?? null;
        persistSession(state.studySessionId);
        attempt = await submitAttempt({
          questionId: q.id,
          selectedOption: choice,
          timeSpentSeconds: elapsed > 0 ? elapsed : 0,
          studySessionId: state.studySessionId,
        });
      } else {
        throw err;
      }
    }
    state.answered = true;
    showFeedback(q, attempt, choice);
    fieldset.querySelectorAll("input").forEach((r) => {
      r.disabled = true;
    });
    btnSubmit.disabled = true;
    btnBlank.disabled = true;
    btnReset.hidden = false;
    btnReset.focus();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard(state.questionId);
      return;
    }
    toast(friendlyMessage(err), "info");
    btnBlank.disabled = false;
  } finally {
    setButtonLoading(btnSubmit, false);
    if (state.answered) btnBlank.disabled = true;
  }
}

/* ---------- feedback + explicação ---------- */

function showFeedback(q, attempt, choice) {
  feedbackBox.textContent = "";
  feedbackBox.hidden = false;
  feedbackEmpty.hidden = true;
  const correct = q.answerKey || "—";

  if (attempt?.wasAnnulled || q.annulled) {
    feedbackBox.dataset.tone = "warning";
    feedbackBox.appendChild(el("strong", { text: "Questão anulada — fora do aproveitamento." }));
    feedbackBox.appendChild(el("p", { text: `Você marcou ${choice}. O gabarito oficial traz X (anulada). Regra de pontuação DESCONHECIDA.` }));
  } else if (attempt?.isCorrect === true) {
    feedbackBox.dataset.tone = "success";
    feedbackBox.appendChild(el("strong", { text: `Você acertou — alternativa ${correct}.` }));
    feedbackBox.appendChild(el("p", { text: `Sua resposta: ${choice}.` }));
  } else {
    feedbackBox.dataset.tone = "danger";
    feedbackBox.appendChild(el("strong", { text: `Não foi dessa vez — resposta correta: ${correct}.` }));
    feedbackBox.appendChild(el("p", { text: `Você marcou ${choice}.` }));
  }

  markOptions(q, choice, correct);

  const topicLine = q.topic?.name
    ? `Conteúdo: ${q.topic.name}${q.subtopic?.name ? ` · ${q.subtopic.name}` : ""} (revisão humana pendente).`
    : "Conteúdo: assunto NÃO CONFIRMADO (revisão pendente).";
  feedbackBox.appendChild(el("p", { text: topicLine }));
  for (const n of (attempt?.notes ?? []).slice(0, 2)) {
    feedbackBox.appendChild(el("p", { text: n }));
  }
  feedbackBox.focus?.();

  renderExplanationDone(q);
}

function markOptions(q, choice, correct) {
  fieldset.querySelectorAll(".questao-option").forEach((row) => {
    const input = row.querySelector("input");
    const v = input?.value;
    row.classList.remove("questao-option--correct", "questao-option--wrong");
    if (v === correct && !q.annulled) row.classList.add("questao-option--correct");
    if (v === choice && choice !== correct) row.classList.add("questao-option--wrong");
  });
}

function renderExplanationPending(q) {
  explanationBox.textContent = "";
  explanationBox.appendChild(el("p", { className: "muted", text: "A explicação é revelada junto com o feedback." }));
  if (q && !q.explanation) {
    explanationBox.appendChild(el("p", { className: "muted", text: "Correção redigida ainda ausente no banco — NECESSITA REVISÃO (nunca inventada)." }));
  }
}

function renderExplanationDone(q) {
  explanationBox.textContent = "";
  if (q.explanation) {
    explanationBox.appendChild(el("p", { text: q.explanation }));
  } else {
    explanationBox.appendChild(el("p", { text: "Explicação ainda não redigida — NECESSITA REVISÃO." }));
    explanationBox.appendChild(
      el("p", { className: "muted", text: "O acerto/erro acima vem do gabarito oficial; o passo a passo textual aguarda curadoria e não foi inventado." }),
    );
  }
}

/* ---------- proveniência + notas ---------- */

function sourceRow(term, def) {
  const row = el("div", { className: "questao-source__row" });
  row.appendChild(el("dt", { className: "questao-source__term", text: term }));
  row.appendChild(el("dd", { className: "questao-source__def", text: def }));
  return row;
}

function renderSource(q) {
  sourceBox.textContent = "";
  const dl = el("dl", { className: "questao-source" });
  dl.appendChild(sourceRow("Identificador", `#${q.id}`));
  dl.appendChild(sourceRow("Fonte", q.sourceType === "OFFICIAL" ? "Oficial IFRN (prova real)" : `Não-oficial (${q.sourceType || "desconhecida"})`));
  dl.appendChild(
    sourceRow(
      "Edição / número",
      q.examYear && q.questionNumber ? `${q.examYear} · Q${q.questionNumber}` : "DESCONHECIDO — NECESSITA REVISÃO",
    ),
  );
  dl.appendChild(sourceRow("Disciplina", q.discipline?.name || q.discipline?.code || "NÃO CONFIRMADA"));
  dl.appendChild(
    sourceRow(
      "Páginas no PDF-fonte",
      typeof q.pageStart === "number"
        ? (q.pageEnd && q.pageEnd !== q.pageStart ? `${q.pageStart}–${q.pageEnd}` : String(q.pageStart))
        : "DESCONHECIDA",
    ),
  );
  dl.appendChild(sourceRow("Figura", q.hasFigure ? "Sim — consulte o PDF-fonte (não redistribuído)" : "Não"));
  dl.appendChild(
    sourceRow(
      "Assunto",
      q.topic?.name ? `${q.topic.name}${q.subtopic?.name ? ` · ${q.subtopic.name}` : ""}` : "NÃO CONFIRMADO",
    ),
  );
  dl.appendChild(
    sourceRow(
      "Classificação",
      `status ${q.classificationStatus || "DESCONHECIDO"} · confiança ${q.classificationConfidence || "DESCONHECIDA"} · taxonomia ${q.taxonomyVersion || "DESCONHECIDA"} (revisão humana pendente)`,
    ),
  );
  dl.appendChild(sourceRow("Publicação", q.publicationStatus || "DESCONHECIDA"));
  sourceBox.appendChild(dl);
}

function renderNotes(q) {
  notesBox.textContent = "";
  const items = [
    ...((q.notes ?? []).slice(0, 6)),
    "Modo Estudo: a correção é imediata e cada confirmação registra uma tentativa (fato imutável — correção só via nova tentativa).",
    "Questões anuladas contam como conteúdo respondido e ficam fora do aproveitamento — regra de pontuação DESCONHECIDA.",
    "Dificuldade estimada é palpite com confiança BAIXA (sem calibração). Explicação ausente = NECESSITA REVISÃO, nunca texto inventado.",
    "Assuntos e frequências são classificação derivada com revisão humana PENDENTE — nunca verdade oficial do IFRN.",
  ];
  const seen = new Set();
  for (const t of items) {
    if (!t || seen.has(t)) continue;
    seen.add(t);
    const card = el("div", { className: "card" });
    const body = el("div", { className: "card__body" });
    body.appendChild(el("p", { text: t, attrs: { style: "font-size:var(--text-sm)" } }));
    card.appendChild(body);
    notesBox.appendChild(card);
  }
}

/* ---------- sessão de estudo ---------- */

function readStoredSession() {
  try {
    const raw = window.sessionStorage.getItem(SESSION_KEY);
    const n = Number.parseInt(raw || "", 10);
    return Number.isFinite(n) && n > 0 ? n : null;
  } catch {
    return null;
  }
}

function persistSession(id) {
  try {
    if (id) window.sessionStorage.setItem(SESSION_KEY, String(id));
    else window.sessionStorage.removeItem(SESSION_KEY);
  } catch {
    // Armazenamento indisponível: segue só em memória.
  }
}

async function ensureStudySession() {
  if (state.studySessionId) return state.studySessionId;
  const s = await openStudySession();
  state.studySessionId = s?.id ?? null;
  persistSession(state.studySessionId);
  return state.studySessionId;
}
