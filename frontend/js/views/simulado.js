/* VouPassar — simulado (TASK 6.7)
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=simulado.html).
 *
 * Cobre TASKS.md 6.7 (interface de prova real):
 * - Hub (sem ?id=): criar por disciplina (5.1: disciplina, quantidade 1–100,
 *   dificuldade opcional, modo) e por edição real (5.5: edição, modo), além
 *   do histórico de execuções (mais recentes primeiro).
 * - Execução (?id=<attemptId>): caderno congelado em ordem de posição,
 *   respostas via POST /attempts com simulationAttemptId (3.7), feedback
 *   imediato por posição no Modo Estudo (5.2), resultado oculto no Modo
 *   Prova até encerrar (5.3), concluir/abandonar com placar do servidor e
 *   resultado com correção por posição.
 *
 * Sem innerHTML (só textContent via el()). Enunciados vêm de
 * GET /questions/{id} (3.4) — mascarados em PROVA em andamento (5.3).
 * 2021 nunca aparece: edições vêm do backend (ausente do dataset, §3).
 *
 * TASK 17.2 (revisão guiada): os selects de modo ganham REVISAO, que não
 * cria simulação (o backend devolveria 400 INVALID_MODE) — roteia para a
 * fila em estudos.html?aba=revisao. ?review=<sessionId> executa a sessão
 * (TASK 17.3, dono js/views/review-exec.js): caderno congelado com resposta
 * imediata em modo REVISAO + resultado ao encerrar + volta à fila.
 *
 * TASK 19.1 (raio-X pós-atividade): ao final (`renderFinishedState`) o
 * bloco "O que fazer agora" (`js/components/result-next.js`) resume os
 * piores assuntos com o caderno em cache + até 3 CTAs (revisar via
 * `POST /review/sessions`, praticar com recorte, atualizar o plano).
 *
 * TASK 21.1 (ritmo em simulado, só exibição): durante a execução o
 * `#sim-pace` mostra "questão i/N · tempo decorrido total · média por
 * questão vs. referência da edição" (`js/components/pace.js`, puro +
 * testado em `scripts/analysis/test_pace.mjs`). É cronômetro informativo —
 * idêntico em ESTUDO e PROVA, sem entrar em placar, ranking ou roteiro.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { logout } from "../api/auth.js";
import { sanitizeInternalPath, renderAuthGuard, requireSessionOrGuard } from "./auth-shared.js";
import { mountExpandableFigure, preloadManifest } from "../components/figure.js";
import { renderPassages } from "../components/passage.js";
import { expressionNode } from "../components/math.js";
import {
  fetchDisciplines,
  fetchEditions,
  createByDiscipline,
  createByEdition,
  listAttempts,
  fetchAttempt,
  submitSimulation,
  abandonSimulation,
  fetchResult,
  fetchFeedback,
  fetchQuestion,
  submitAttempt,
} from "../api/simulado.js";
import { loadReviewExecution } from "./review-exec.js";
import { createSession } from "../api/review.js";
import { generatePlan } from "../api/dashboard.js";
import { el, renderEmpty, renderErrorSummary, setButtonLoading, toast } from "../components/ui.js";
import { summarizeResult, renderResultNext } from "../components/result-next.js";
import { summarizePace, executionElapsedSeconds } from "../components/pace.js";
import { disciplineLabel, modeLabel, statusLabel, difficultyLabel, choiceLabel, simulationTitle, plural, sourceTypeLabel } from "../vocab.js";

const PAGE_SIZE = 20;

const guard = document.getElementById("sim-guard");
const errorBox = document.getElementById("sim-error");
const loadingBox = document.getElementById("sim-loading");
const content = document.getElementById("sim-content");
const subtitle = document.getElementById("sim-subtitle");

const hub = document.getElementById("sim-hub");
const execBox = document.getElementById("sim-exec");
const reviewBox = document.getElementById("sim-review");

// Hub
const formDisc = document.getElementById("sim-create-discipline");
const selDisc = document.getElementById("s-disc-disciplina");
const inpQtd = document.getElementById("s-disc-qtd");
const selDiff = document.getElementById("s-disc-dificuldade");
const selDiscOrigin = document.getElementById("s-disc-origem");
const selDiscMode = document.getElementById("s-disc-modo");
const btnDisc = document.getElementById("sim-disc-submit");
const formEd = document.getElementById("sim-create-edition");
const selEd = document.getElementById("s-ed-edicao");
const selEdMode = document.getElementById("s-ed-modo");
const btnEd = document.getElementById("sim-ed-submit");
const historyBox = document.getElementById("sim-history");
const historyCount = document.getElementById("sim-history-count");
const btnMore = document.getElementById("sim-more");

// Execução
const modeBadge = document.getElementById("sim-mode-badge");
const execTitle = document.getElementById("sim-exec-title");
const execMeta = document.getElementById("sim-exec-meta");
const execBadges = document.getElementById("sim-exec-badges");
const progressText = document.getElementById("sim-progress-text");
const progressBar = document.getElementById("sim-progress-bar");
const progressFill = document.getElementById("sim-progress-fill");
const paceEl = document.getElementById("sim-pace");
const hiddenNote = document.getElementById("sim-hidden-note");
const questionsBox = document.getElementById("sim-questions");
const btnSubmit = document.getElementById("sim-submit");
const btnAbandon = document.getElementById("sim-abandon");
const resultSection = document.getElementById("sim-result-section");
const resultBox = document.getElementById("sim-result");
const confirmDlg = document.getElementById("sim-confirm");
const confirmText = document.getElementById("sim-confirm-text");
const confirmYes = document.getElementById("sim-confirm-yes");
const confirmNo = document.getElementById("sim-confirm-no");


const state = {
  user: null,
  attemptId: null,
  attempt: null,
  details: new Map(),
  answeredLocal: new Map(),
  startTimes: new Map(),
  historyPage: 0,
  historyLast: true,
  historyItems: [],
  confirmAction: null,
  paceTimer: null,
};

wireLogoutButtons();
main();

async function main() {
  const user = await requireSessionOrGuard(showGuard);
  if (!user) {
    return;
  }
  state.user = user;
  preloadManifest().catch(() => null);
  subtitle.textContent = `Olá, ${user.displayName || "estudante"} — monte por disciplina ou reproduza uma edição real, responda como em prova e receba a correção ao final.`;
  showLogoutButtons();
  const id = readAttemptId();
  if (id !== null) {
    state.attemptId = id;
    hub.hidden = true;
    if (reviewBox) reviewBox.hidden = true;
    execBox.hidden = false;
    loadingBox.hidden = true;
    content.hidden = false;
    await loadExecution(id);
    return;
  }
  // Revisão guiada (TASK 17.3): ?review=<sessionId> criado pela fila em
  // Estudos. A execução (caderno + resultado) mora em review-exec.js e usa
  // este mesmo endereço; sem ?review= cai no hub abaixo.
  const reviewId = readReviewId();
  if (reviewId !== null) {
    hub.hidden = true;
    execBox.hidden = true;
    if (reviewBox) reviewBox.hidden = false;
    loadingBox.hidden = true;
    content.hidden = false;
    await loadReviewExecution(reviewId, { showGuard });
    return;
  }
  execBox.hidden = true;
  if (reviewBox) reviewBox.hidden = true;
  hub.hidden = false;
  await loadHub();
}

function wireLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      btn.setAttribute("disabled", "");
      try {
        await logout();
      } finally {
        const next = state.attemptId ? `simulado.html?id=${encodeURIComponent(String(state.attemptId))}` : "simulado.html";
        window.location.href = `./login.html?next=${encodeURIComponent(next)}`;
      }
    });
  });
}

function showLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.hidden = false;
  });
}

function showGuard() {
  loadingBox.hidden = true;
  content.hidden = true;
  guard.hidden = false;
  // Guarda usa o validador único (TASK 16.3): o ?next= construído aqui
  // passa pelo mesmo sanitizeInternalPath do login/questão.
  const rawNext = readAttemptId() !== null ? `simulado.html?id=${readAttemptId()}` : "simulado.html";
  const next = sanitizeInternalPath(rawNext, "simulado.html");
  renderAuthGuard(guard, {
    title: "Entre para fazer simulados",
    description: "O simulado monta um caderno, registra suas respostas e corrige no servidor. Ele precisa da sua sessão — entre ou crie uma conta para continuar.",
    next,
  });
}

function readAttemptId() {
  let raw = "";
  try {
    raw = new URLSearchParams(window.location.search).get("id") || "";
  } catch {
    raw = "";
  }
  const n = Number.parseInt(String(raw).trim(), 10);
  return Number.isFinite(n) && n > 0 ? n : null;
}

function readReviewId() {
  let raw = "";
  try {
    raw = new URLSearchParams(window.location.search).get("review") || "";
  } catch {
    raw = "";
  }
  const n = Number.parseInt(String(raw).trim(), 10);
  return Number.isFinite(n) && n > 0 ? n : null;
}

/* ================= HUB ================= */

async function loadHub() {
  loadingBox.hidden = false;
  content.hidden = true;
  errorBox.textContent = "";
  try {
    const settled = await Promise.allSettled([fetchDisciplines(), fetchEditions()]);
    const [discR, edR] = settled;
    if (discR.status === "rejected") throw discR.reason;
    if (edR.status === "rejected") throw edR.reason;
    fillDisciplines(asList(discR.value));
    fillEditions(asList(edR.value));
    bindHubForms();
    await loadHistory(true);
    loadingBox.hidden = true;
    content.hidden = false;
  } catch (err) {
    loadingBox.hidden = true;
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    renderErrorSummary(errorBox, {
      title: "Não foi possível carregar o simulado",
      items: [friendlyMessage(err)],
      traceId: err instanceof ApiError ? err.traceId : null,
    });
  }
}

function asList(v) {
  if (Array.isArray(v)) return v;
  if (Array.isArray(v?.content)) return v.content;
  return [];
}

function fillDisciplines(items) {
  selDisc.textContent = "";
  if (items.length === 0) {
    selDisc.appendChild(el("option", { text: "Nenhuma disciplina", attrs: { value: "" } }));
    return;
  }
  selDisc.appendChild(el("option", { text: "Escolha…", attrs: { value: "" } }));
  for (const d of items) {
    selDisc.appendChild(
      el("option", {
        text: `${d.name} (${d.questionCount} questões)`,
        attrs: { value: d.code },
      }),
    );
  }
}

function fillEditions(items) {
  selEd.textContent = "";
  const years = items.map((e) => e.year).sort((a, b) => a - b);
  if (years.length === 0) {
    selEd.appendChild(el("option", { text: "Nenhuma edição", attrs: { value: "" } }));
    return;
  }
  selEd.appendChild(el("option", { text: "Escolha…", attrs: { value: "" } }));
  for (const e of items) {
    const label = `${e.year} — ${e.objectiveCount} questões${e.hasEssay ? " + texto" : ""}`;
    selEd.appendChild(el("option", { text: label, attrs: { value: String(e.year) } }));
  }
}

function bindHubForms() {
  if (!formDisc.dataset.bound) {
    formDisc.dataset.bound = "1";
    formDisc.addEventListener("submit", async (e) => {
      e.preventDefault();
      const disciplineCode = selDisc.value || "";
      const questionCount = Number.parseInt(inpQtd.value || "", 10);
      if (!disciplineCode) {
        toast("Escolha uma disciplina.", "info");
        selDisc.focus();
        return;
      }
      if (!Number.isFinite(questionCount) || questionCount < 1 || questionCount > 100) {
        toast("Quantidade deve estar entre 1 e 100.", "info");
        inpQtd.focus();
        return;
      }
      // Revisão (TASK 17.2): o backend não aceita REVISAO em simulados
      // (SimulationService MODES = ESTUDO/PROVA → 400 INVALID_MODE), então o
      // modo Revisão no hub não cria simulação — abre a fila em Estudar com
      // a disciplina já filtrada. Quantidade/dificuldade/origem não seguem:
      // a revisão tem filtros próprios (limit/disciplina/tópico/só-erros).
      if ((selDiscMode.value || "PROVA") === "REVISAO") {
        const q = new URLSearchParams({ aba: "revisao", disciplina: disciplineCode });
        toast("Abrindo a fila de revisão…", "info");
        window.location.href = `./estudos.html?${q.toString()}`;
        return;
      }
      setButtonLoading(btnDisc, true, "Sorteando…");
      try {
        const created = await createByDiscipline({
          disciplineCode,
          questionCount,
          difficulty: selDiff.value || undefined,
          // Origem (TASK 15.4): padrão Oficiais; o backend também assume
          // OFFICIAL quando ausente. Edição real não tem este campo.
          sourceType: selDiscOrigin?.value || "OFFICIAL",
          mode: selDiscMode.value || "PROVA",
        });
        window.location.href = `./simulado.html?id=${encodeURIComponent(String(created.attemptId))}`;
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          showGuard();
          return;
        }
        toast(friendlyMessage(err), "info");
        if (err instanceof ApiError && err.traceId) {
          renderErrorSummary(errorBox, {
            title: "Não foi possível criar o simulado por disciplina",
            items: [friendlyMessage(err)],
            traceId: err.traceId,
          });
        }
      } finally {
        setButtonLoading(btnDisc, false);
      }
    });
  }
  if (!formEd.dataset.bound) {
    formEd.dataset.bound = "1";
    formEd.addEventListener("submit", async (e) => {
      e.preventDefault();
      const editionYear = Number.parseInt(selEd.value || "", 10);
      if (!Number.isFinite(editionYear)) {
        toast("Escolha uma edição.", "info");
        selEd.focus();
        return;
      }
      // Revisão (TASK 17.2): mesmo roteamento do modo por disciplina — a
      // revisão não tem recorte por edição, então abre a fila sem filtro.
      if ((selEdMode.value || "PROVA") === "REVISAO") {
        toast("Abrindo a fila de revisão…", "info");
        window.location.href = "./estudos.html?aba=revisao";
        return;
      }
      setButtonLoading(btnEd, true, "Montando…");
      try {
        const created = await createByEdition({
          editionYear,
          mode: selEdMode.value || "PROVA",
        });
        window.location.href = `./simulado.html?id=${encodeURIComponent(String(created.attemptId))}`;
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          showGuard();
          return;
        }
        toast(friendlyMessage(err), "info");
        if (err instanceof ApiError && (err.status >= 400)) {
          renderErrorSummary(errorBox, {
            title: "Não foi possível criar o simulado da edição",
            items: [friendlyMessage(err)],
            traceId: err.traceId,
          });
        }
      } finally {
        setButtonLoading(btnEd, false);
      }
    });
  }
  if (!btnMore.dataset.bound) {
    btnMore.dataset.bound = "1";
    btnMore.addEventListener("click", () => loadHistory(false));
  }
}

async function loadHistory(reset) {
  if (reset) {
    state.historyPage = 0;
    state.historyItems = [];
    historyBox.textContent = "";
  }
  try {
    const page = await listAttempts({ page: state.historyPage, size: PAGE_SIZE });
    const items = page?.content ?? [];
    state.historyItems.push(...items);
    state.historyLast = Boolean(page?.last ?? items.length < PAGE_SIZE);
    renderHistory(page?.totalElements ?? state.historyItems.length);
    if (!state.historyLast) {
      state.historyPage += 1;
      btnMore.hidden = false;
    } else {
      btnMore.hidden = true;
    }
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    renderEmpty(historyBox, {
      title: "Histórico indisponível",
      description: friendlyMessage(err),
    });
    btnMore.hidden = true;
  }
}

function statusBadge(s) {
  if (s === "SUBMITTED") return "badge--success";
  if (s === "IN_PROGRESS") return "badge--warning";
  return "";
}

function initialsOf(label) {
  const words = String(label || "").trim().split(/\s+/).filter(Boolean);
  if (!words.length) return "•";
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return (words[0][0] + words[1][0]).toUpperCase();
}

function avatarColor(key) {
  const palette = ["#0f5084", "#6d43cc", "#0b7a4b", "#b83e06", "#4c2e8f"];
  let h = 0;
  for (const c of String(key || "")) h = (h * 31 + c.charCodeAt(0)) % 997;
  return palette[h % palette.length];
}

function formatDateTime(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function renderHistory(total) {
  historyBox.textContent = "";
  historyCount.textContent = state.historyItems.length === 0
    ? "Nenhum simulado ainda — crie o primeiro acima."
    : `${total} ${total === 1 ? "execução" : "execuções"} · mostrando ${state.historyItems.length}.`;
  if (state.historyItems.length === 0) {
    renderEmpty(historyBox, {
      title: "Nenhum simulado ainda",
      description: "Seus simulados por disciplina e de edição real aparecem aqui.",
    });
    return;
  }
  const list = el("ol", { className: "sim-list" });
  for (const s of state.historyItems) {
    const title = simulationTitle(s.title, `Simulado #${s.attemptId}`);
    const li = el("li", { className: "sim-list__item" });
    const avatar = el("span", { className: "sim-list__avatar", text: initialsOf(title) });
    avatar.style.setProperty("--sa", avatarColor(s.attemptId ?? title));
    avatar.setAttribute("aria-hidden", "true");
    li.appendChild(avatar);
    const left = el("div", { className: "sim-list__main" });
    left.appendChild(el("p", { className: "sim-list__title", text: title }));
    const kind = disciplineLabel(s.disciplineCode) || "Edição real";
    const bits = [kind, modeLabel(s.mode) || "—", `${s.questionCount} questões`, `início ${formatDateTime(s.startedAt)}`];
    left.appendChild(el("p", { className: "sim-list__meta", text: bits.join(" · ") }));
    li.appendChild(left);
    const right = el("div", { className: "cluster" });
    if (statusLabel(s.status)) {
      right.appendChild(el("span", { className: `badge ${statusBadge(s.status)}`.trim(), text: statusLabel(s.status) }));
    }
    const openLabel = s.status === "IN_PROGRESS" ? "Retomar" : "Ver resultado";
    right.appendChild(
      el("a", {
        className: "btn btn--secondary btn--sm",
        text: openLabel,
        attrs: { href: `./simulado.html?id=${encodeURIComponent(String(s.attemptId))}` },
      }),
    );
    li.appendChild(right);
    list.appendChild(li);
  }
  historyBox.appendChild(list);
}

/* ================= EXECUÇÃO ================= */

function isProva() {
  return String(state.attempt?.mode || "").toUpperCase() === "PROVA";
}

function isOpen() {
  return state.attempt?.status === "IN_PROGRESS";
}

async function loadExecution(id) {
  loadingBox.hidden = false;
  content.hidden = true;
  errorBox.textContent = "";
  try {
    const attempt = await fetchAttempt(id);
    state.attempt = attempt;
    renderExecHeader(attempt);
    await loadCaderno(attempt);
    loadingBox.hidden = true;
    content.hidden = false;
    execTitle.focus({ preventScroll: true });
  } catch (err) {
    loadingBox.hidden = true;
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    const notFound = err instanceof ApiError && err.status === 404;
    renderErrorSummary(errorBox, {
      title: notFound ? "Simulado não encontrado" : "Não foi possível carregar a execução",
      items: [notFound ? "Esta execução não existe ou pertence a outro aluno." : friendlyMessage(err)],
      traceId: err instanceof ApiError ? err.traceId : null,
    });
    const actions = el("div", { className: "btn-group mt-4" });
    actions.appendChild(
      el("a", { className: "btn btn--secondary btn--sm", text: "Voltar aos simulados", attrs: { href: "./simulado.html" } }),
    );
    actions.appendChild(
      el("a", { className: "btn btn--ghost btn--sm", text: "Abrir dashboard", attrs: { href: "./dashboard.html" } }),
    );
    errorBox.appendChild(actions);
  }
}

function execKindLabel(a) {
  if (String(a?.type || "").toUpperCase() === "REAL_EDITION") return "Edição real";
  return "Por disciplina";
}

function renderExecHeader(a) {
  const title = simulationTitle(a.title, `Simulado #${a.attemptId}`);
  document.title = `${title} — VouPassar`;
  modeBadge.textContent = `Simulado · Modo ${modeLabel(a.mode) || "—"} · ${statusLabel(a.status)}`;
  execTitle.textContent = title;
  const bits = [
    execKindLabel(a),
    disciplineLabel(a.disciplineCode, a.disciplineName) || "caderno misto",
    `${a.questionCount} ${a.questionCount === 1 ? "questão" : "questões"}`,
    `início ${formatDateTime(a.startedAt)}`,
  ];
  if (a.submittedAt) bits.push(`encerrado ${formatDateTime(a.submittedAt)}`);
  execMeta.textContent = "";
  execMeta.appendChild(el("small", { text: bits.join(" · ") }));

  execBadges.textContent = "";
  execBadges.appendChild(el("span", { className: "badge", text: execKindLabel(a) }));
  execBadges.appendChild(el("span", { className: "badge", text: `Modo ${modeLabel(a.mode) || "—"}` }));
  execBadges.appendChild(el("span", { className: `badge ${statusBadge(a.status)}`.trim(), text: statusLabel(a.status) }));

  hiddenNote.textContent = "";
  if (isOpen() && isProva()) {
    hiddenNote.appendChild(
      el("small", { text: "Modo Prova: o resultado e o gabarito ficam ocultos durante a execução — a correção aparece ao concluir ou abandonar." }),
    );
  } else if (isOpen()) {
    hiddenNote.appendChild(
      el("small", { text: "Modo Estudo: cada resposta mostra o feedback imediato (acerto/erro, resposta correta e assunto)." }),
    );
  }
  updateProgress();
  bindFinishButtons();
  startPaceClock();
  const finishCard = btnSubmit.closest(".panel");
  if (finishCard) finishCard.hidden = !isOpen();
}

/* ---------- ritmo em simulado (TASK 21.1, só exibição) ----------
 * Cronômetro informativo: questão i/N · tempo decorrido total · média por
 * questão vs. referência da edição (4h nas capas do dataset, ver
 * `js/components/pace.js`). Idêntico em ESTUDO e PROVA — sem ramificação
 * por modo, sem entrar em placar/ranking/roteiro. Sem `role=status`: o
 * cronômetro atualiza a cada segundo e o leitor de tela não deve anunciar
 * cada tick (o progresso de respondidas continua em `role=status`).
 * Encerrada congela no `submittedAt` (placar final). */
function stopPaceClock() {
  if (state.paceTimer !== null && state.paceTimer !== undefined) {
    clearInterval(state.paceTimer);
    state.paceTimer = null;
  }
}

function currentElapsedSeconds() {
  try {
    return executionElapsedSeconds(state.attempt, Date.now());
  } catch {
    return 0;
  }
}

function updatePace() {
  if (!paceEl) return;
  const total = state.attempt?.questionCount ?? state.attempt?.questions?.length ?? 0;
  const done = answeredCount();
  try {
    const summary = summarizePace({
      attempt: state.attempt,
      answered: done,
      total,
      elapsedSeconds: currentElapsedSeconds(),
    });
    paceEl.textContent = `Ritmo: ${summary.line}`;
  } catch {
    paceEl.textContent = "Ritmo: informativo — tempo decorrido indisponível no momento.";
  }
}

function startPaceClock() {
  stopPaceClock();
  updatePace();
  // Só tica em andamento; encerrada fica congelada no tempo final.
  if (!isOpen()) return;
  state.paceTimer = setInterval(updatePace, 1000);
}

function answeredCount() {
  const items = state.attempt?.questions ?? [];
  let n = 0;
  for (const it of items) {
    if (state.answeredLocal.get(it.position) || it.answered) n += 1;
  }
  return n;
}

function updateProgress() {
  const total = state.attempt?.questionCount ?? state.attempt?.questions?.length ?? 0;
  const done = answeredCount();
  progressText.textContent = `${done} de ${total} respondidas`;
  const pct = total > 0 ? Math.round((done / total) * 100) : 0;
  progressFill.style.width = `${pct}%`;
  progressBar.setAttribute("aria-valuenow", String(pct));
  const pctEl = document.getElementById("sim-progress-pct");
  if (pctEl) pctEl.textContent = `${pct}%`;
  updatePace();
}

async function loadCaderno(attempt) {
  questionsBox.textContent = "";
  const items = [...(attempt.questions ?? [])].sort((a, b) => a.position - b.position);
  if (items.length === 0) {
    renderEmpty(questionsBox, {
      title: "Caderno vazio",
      description: "Esta execução não trouxe posições. Volte ao hub e crie um novo simulado.",
    });
    return;
  }
  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Buscando enunciados do caderno…" }));
  questionsBox.appendChild(loading);

  const settled = await Promise.allSettled(items.map((it) => fetchQuestion(it.questionId)));
  questionsBox.textContent = "";
  settled.forEach((r, i) => {
    const item = items[i];
    if (r.status === "fulfilled") {
      state.details.set(item.questionId, r.value);
      state.startTimes.set(item.position, Date.now());
      questionsBox.appendChild(renderSimCard(item, r.value));
    } else {
      questionsBox.appendChild(renderMissingCard(item, r.reason));
    }
  });

  if (!isOpen()) {
    await renderFinishedState();
  }
}

function positionTitle(item, detail) {
  const ref = item.sourceYear && item.sourceQuestionNumber
    ? `${item.sourceYear} Q${item.sourceQuestionNumber}`
    : (detail?.examYear && detail?.questionNumber
      ? `${detail.examYear} Q${detail.questionNumber}`
      : `Questão #${item.questionId}`);
  // disciplineLabel evita que o código cru (LP, MAT, MATEMATICA) apareça
  // como texto no cartão da questão.
  const disc = disciplineLabel(
    item.disciplineCode || detail?.discipline?.code,
    detail?.discipline?.name,
  );
  return { ref, disc };
}

function renderSimCard(item, detail) {
  const open = isOpen();
  const { ref, disc } = positionTitle(item, detail);
  const card = el("article", { className: "sim-card", attrs: { "aria-labelledby": `sim-p${item.position}-t` } });
  if (item.answered) card.dataset.answered = "1";
  const head = el("div", { className: "sim-card__head" });
  head.appendChild(el("span", { className: "sim-card__pos", text: `Posição ${item.position}`, attrs: { id: `sim-p${item.position}-t` } }));
  head.appendChild(el("span", { className: "badge", text: ref }));
  if (disc) head.appendChild(el("span", { className: "badge", text: disc }));
  // Selo de origem (TASK 15.4): no caderno misto cada cartão declara o que
  // é. Metadado de proveniência, não gabarito — visível também no Modo
  // Prova (mesmo critério de textos-base e figuras).
  if (detail?.sourceType) {
    const origin = sourceTypeLabel(detail.sourceType);
    head.appendChild(el("span", {
      className: "badge",
      text: detail.sourceType === "OFFICIAL" ? origin : `${origin} — não é questão do IFRN`,
    }));
  }
  if (item.wasAnnulled || detail?.annulled) {
    head.appendChild(el("span", { className: "badge badge--warning", text: "Anulada" }));
  }
  card.appendChild(head);

  card.appendChild(el("p", { className: "sim-card__statement", text: detail?.statement || "(enunciado ainda não conferido)" }));
  // Textos-base (TASK 6.9): parte do enunciado — visíveis também no Modo Prova.
  if (detail) renderPassages(detail, card);
  if (detail?.hasFigure || (Array.isArray(detail?.figures) && detail.figures.length > 0)) {
    // Figura expansível (também no Modo Prova: só o gabarito é oculto, AGENTS.md §9).
    card.appendChild(mountExpandableFigure(detail, "sim-card__figure"));
  }

  const fieldset = el("fieldset", { className: "sim-options" });
  fieldset.appendChild(el("legend", { text: isProva() && open ? "Sua resposta (resultado oculto até o final)" : "Sua resposta" }));
  const group = `sim-${state.attemptId}-p${item.position}`;
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
  const btnAnswer = el("button", { className: "btn btn--primary btn--sm", text: "Responder", attrs: { type: "button" } });
  const btnBlank = el("button", { className: "btn btn--ghost btn--sm", text: "Em branco", attrs: { type: "button" } });
  if (!open || options.length === 0) {
    btnAnswer.disabled = true;
    btnBlank.disabled = true;
  }
  if (item.answered && open) {
    // Já respondida antes (retomada): permite alterar — vale a última.
    btnAnswer.textContent = "Alterar resposta";
  }
  actions.appendChild(btnAnswer);
  actions.appendChild(btnBlank);
  card.appendChild(actions);

  const feedback = el("div", { className: "sim-feedback", attrs: { role: "status", hidden: "" } });
  card.appendChild(feedback);
  if (item.answered && open && isProva()) {
    feedback.hidden = false;
    feedback.dataset.tone = "muted";
    feedback.appendChild(el("p", { text: "Posição já respondida — o resultado segue oculto até concluir. Você pode alterar a resposta (vale a última)." }));
  }

  async function answer(choice) {
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
      const attempt = await submitAttempt({
        questionId: item.questionId,
        selectedOption: sel,
        mode: state.attempt.mode,
        timeSpentSeconds: elapsed,
        simulationAttemptId: state.attemptId,
      });
      state.answeredLocal.set(item.position, sel);
      item.answered = true;
      updateProgress();
      if (isProva()) {
        showHiddenFeedback(feedback, sel, attempt);
      } else {
        await showStudyFeedback(card, fieldset, feedback, item, detail, sel);
      }
      markAnsweredCard(card, fieldset, btnAnswer, btnBlank, actions, item, () => {
        state.startTimes.set(item.position, Date.now());
      });
      if (!isProva()) toast("Resposta registrada com feedback imediato.", "success");
      else toast("Resposta registrada — resultado oculto até concluir.", "success");
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        showGuard();
        return;
      }
      if (err instanceof ApiError && err.code === "SIMULATION_CLOSED") {
        toast("Esta execução já foi encerrada. Atualizando o resultado…", "info");
        await refreshToFinished();
        return;
      }
      toast(friendlyMessage(err), "info");
      btnBlank.disabled = false;
    } finally {
      setButtonLoading(btnAnswer, false);
      // setButtonLoading(false) sempre remove o disabled: numa questão já
      // respondida o "Responder" precisa continuar desativado (o caminho
      // correto é "Alterar resposta"), senão o aluno reenvia sem querer.
      if (state.answeredLocal.get(item.position) || item.answered) btnAnswer.disabled = true;
      if (state.answeredLocal.get(item.position) || item.answered) btnBlank.disabled = true;
    }
  }

  btnAnswer.addEventListener("click", () => {
    const checked = fieldset.querySelector("input:checked");
    answer(checked?.value || null);
  });
  btnBlank.addEventListener("click", () => answer("BLANK"));

  return card;
}

function markAnsweredCard(card, fieldset, btnAnswer, btnBlank, actions, item, onRetry) {
  card.dataset.answered = "1";
  fieldset.querySelectorAll("input").forEach((r) => {
    r.disabled = true;
  });
  btnAnswer.disabled = true;
  btnBlank.disabled = true;
  if (!actions.querySelector("[data-retry]")) {
    const retry = el("button", {
      className: "btn btn--secondary btn--sm",
      text: isProva() ? "Alterar resposta" : "Tentar novamente",
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
      btnAnswer.disabled = false;
      btnAnswer.textContent = isProva() ? "Alterar resposta" : "Responder";
      btnBlank.disabled = false;
      retry.remove();
      onRetry?.();
      fieldset.querySelector("input")?.focus();
    });
    actions.appendChild(retry);
  }
}

function showHiddenFeedback(box, choice, attempt) {
  box.textContent = "";
  box.hidden = false;
  box.dataset.tone = "muted";
  if (attempt?.wasAnnulled) {
    box.dataset.tone = "warning";
    box.appendChild(el("strong", { text: "Questão anulada — fora do aproveitamento." }));
    box.appendChild(el("p", { text: `Você marcou ${choiceLabel(choice)}. o IFRN não diz como pontuar questões anuladas.` }));
    return;
  }
  box.appendChild(el("p", { text: `Resposta ${choiceLabel(choice)} registrada — resultado oculto durante a prova. Conclua para ver a correção.` }));
}

async function showStudyFeedback(card, fieldset, box, item, detail, choice) {
  box.textContent = "";
  box.hidden = false;
  try {
    const fb = await fetchFeedback(state.attemptId, item.position);
    if (fb.wasAnnulled) {
      box.dataset.tone = "warning";
      box.appendChild(el("strong", { text: "Questão anulada — fora do aproveitamento." }));
      box.appendChild(el("p", { text: `Você marcou ${choiceLabel(fb.selectedOption)}. o IFRN não diz como pontuar questões anuladas.` }));
    } else if (fb.isCorrect === true) {
      box.dataset.tone = "success";
      box.appendChild(el("strong", { text: `Você acertou — alternativa ${fb.correctAnswer}.` }));
      box.appendChild(el("p", { text: `Sua resposta: ${choiceLabel(fb.selectedOption)}.` }));
    } else {
      box.dataset.tone = "danger";
      box.appendChild(el("strong", { text: `Não foi dessa vez — resposta correta: ${fb.correctAnswer}.` }));
      box.appendChild(el("p", { text: `Você marcou ${choiceLabel(fb.selectedOption)}.` }));
    }
    fieldset.querySelectorAll(".sim-option").forEach((row) => {
      const v = row.querySelector("input")?.value;
      row.classList.remove("sim-option--correct", "sim-option--wrong");
      if (v === fb.correctAnswer && !fb.wasAnnulled) row.classList.add("sim-option--correct");
      if (v === fb.selectedOption && fb.selectedOption !== fb.correctAnswer) row.classList.add("sim-option--wrong");
    });
    const topicLine = fb.topicName
      ? `Conteúdo: ${fb.topicName}${fb.subtopicName ? ` · ${fb.subtopicName}` : ""}.`
      : "Conteúdo: assunto ainda sem classificação.";
    box.appendChild(el("p", { text: topicLine }));
    // As `notes` que a API devolve são trilha de auditoria do servidor
    // (evidência/pontuação), não conteúdo de estudo — por isso não entram
    // na tela do aluno.
  } catch (err) {
    box.dataset.tone = "muted";
    box.appendChild(el("p", { text: `Resposta ${choiceLabel(choice)} registrada, mas o feedback falhou: ${friendlyMessage(err)}` }));
  }
  card.dataset.answered = "1";
}

function renderMissingCard(item, reason) {
  const card = el("article", { className: "sim-card" });
  card.appendChild(el("span", { className: "sim-card__pos", text: `Posição ${item.position}` }));
  const box = el("div", { className: "alert alert--danger", attrs: { role: "alert" } });
  box.appendChild(el("strong", { text: "Não foi possível carregar esta questão. " }));
  box.appendChild(el("span", { text: friendlyMessage(reason) }));
  if (reason instanceof ApiError && reason.traceId) {
    box.appendChild(el("p", { className: "envelope mt-2", text: `Código de rastreio: ${reason.traceId}` }));
  }
  const retry = el("button", { className: "btn btn--secondary btn--sm mt-2", text: "Tentar de novo", attrs: { type: "button" } });
  retry.addEventListener("click", async () => {
    retry.disabled = true;
    try {
      const detail = await fetchQuestion(item.questionId);
      state.details.set(item.questionId, detail);
      state.startTimes.set(item.position, Date.now());
      card.replaceWith(renderSimCard(item, detail));
    } catch (err2) {
      toast(friendlyMessage(err2), "info");
      retry.disabled = false;
    }
  });
  box.appendChild(retry);
  card.appendChild(box);
  return card;
}

/* ---------- encerrar ---------- */

function bindFinishButtons() {
  if (btnSubmit.dataset.bound) return;
  btnSubmit.dataset.bound = "1";
  btnSubmit.addEventListener("click", () => askConfirm("submit"));
  btnAbandon.addEventListener("click", () => askConfirm("abandon"));
  confirmNo.addEventListener("click", () => {
    confirmDlg.close();
    state.confirmAction = null;
    if (isOpen()) btnSubmit.focus?.();
  });
  // Esc (evento "cancel" do <dialog> nativo) não passa pelo botão Voltar:
  // devolve o foco ao encerrar para o teclado não cair em <body>.
  // O fluxo Confirmar usa close() programático (sem "cancel") e gerencia o
  // foco próprio (resultado), por isso não usamos "close" aqui.
  confirmDlg.addEventListener("cancel", () => {
    state.confirmAction = null;
    setTimeout(() => { if (isOpen()) btnSubmit.focus?.(); }, 0);
  });
  confirmYes.addEventListener("click", async () => {
    const action = state.confirmAction;
    confirmDlg.close();
    state.confirmAction = null;
    if (action === "submit") await doFinish("submit");
    else if (action === "abandon") await doFinish("abandon");
  });
}

function askConfirm(action) {
  if (!isOpen()) {
    toast("Esta execução já foi encerrada.", "info");
    return;
  }
  state.confirmAction = action;
  const done = answeredCount();
  const total = state.attempt?.questionCount ?? 0;
  const pending = Math.max(0, total - done);
  confirmText.textContent = action === "submit"
    ? `Concluir com ${plural(done, "respondida", "respondidas")} e ${plural(pending, "pendente", "pendentes")}? Não será possível responder após encerrar.`
    : `Abandonar com ${plural(done, "respondida", "respondidas")}? O placar parcial será registrado e não será possível responder.`;
  confirmYes.textContent = action === "submit" ? "Concluir agora" : "Abandonar mesmo assim";
  if (typeof confirmDlg.showModal === "function") {
    confirmDlg.showModal();
    confirmYes.focus();
  } else {
    doFinish(action);
  }
}

async function doFinish(action) {
  const btn = action === "submit" ? btnSubmit : btnAbandon;
  setButtonLoading(btn, true, action === "submit" ? "Corrigindo…" : "Abandonando…");
  try {
    await (action === "submit" ? submitSimulation(state.attemptId) : abandonSimulation(state.attemptId));
    toast(action === "submit" ? "Simulado concluído. Correção disponível abaixo." : "Simulado abandonado com placar parcial.", "success");
    await refreshToFinished();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    if (err instanceof ApiError && err.code === "SIMULATION_CLOSED") {
      await refreshToFinished();
      return;
    }
    toast(friendlyMessage(err), "info");
  } finally {
    setButtonLoading(btn, false);
  }
}

async function refreshToFinished() {
  try {
    state.attempt = await fetchAttempt(state.attemptId);
    renderExecHeader(state.attempt);
    questionsBox.textContent = "";
    const items = [...(state.attempt.questions ?? [])].sort((a, b) => a.position - b.position);
    for (const item of items) {
      const detail = state.details.get(item.questionId) || null;
      if (detail) questionsBox.appendChild(renderSimCard(item, detail));
      else questionsBox.appendChild(renderMissingCard(item, new Error("Enunciado indisponível.")));
    }
    await renderFinishedState();
  } catch (err) {
    toast(friendlyMessage(err), "info");
  }
}

function formatPercent(acc) {
  if (acc === null || acc === undefined) return "—";
  const n = Number(acc);
  if (!Number.isFinite(n)) return "—";
  return `${(n * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
}

async function renderFinishedState() {
  resultSection.hidden = false;
  resultBox.textContent = "";
  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Calculando resultado no servidor…" }));
  resultBox.appendChild(loading);
  try {
    const res = await fetchResult(state.attemptId);
    resultBox.textContent = "";
    renderScoreGrid(res);
    renderResultItems(res);
    renderNextSteps(res);
    resultSection.scrollIntoView({ block: "start" });
    document.getElementById("sim-result-t")?.focus?.({ preventScroll: true });
  } catch (err) {
    resultBox.textContent = "";
    if (err instanceof ApiError && err.code === "SIMULATION_NOT_FINISHED") {
      resultSection.hidden = true;
      return;
    }
    const box = el("div", { className: "alert alert--danger", attrs: { role: "alert" } });
    box.appendChild(el("strong", { text: "Não foi possível carregar o resultado. " }));
    box.appendChild(el("span", { text: friendlyMessage(err) }));
    if (err instanceof ApiError && err.traceId) {
      box.appendChild(el("p", { className: "envelope mt-2", text: `Código de rastreio: ${err.traceId}` }));
    }
    resultBox.appendChild(box);
  }
}

function renderScoreGrid(res) {
  const grid = el("div", { className: "sim-score" });
  const cells = [
    [`${res.correct}/${res.scored}`, "corretas / pontuáveis"],
    [formatPercent(res.accuracy), "aproveitamento"],
    [`${res.answered}/${res.total}`, "respondidas / total"],
    [String(res.annulled ?? 0), "anuladas (fora do cálculo)"],
  ];
  for (const [value, label] of cells) {
    const cell = el("div", { className: "sim-score__cell" });
    cell.appendChild(el("span", { className: "sim-score__value", text: value }));
    cell.appendChild(el("span", { className: "sim-score__label", text: label }));
    grid.appendChild(cell);
  }
  resultBox.appendChild(grid);
  const note = el("p", { className: "muted mt-2" });
  note.appendChild(
    el("small", {
      text: "O placar considera sua última resposta em cada questão. Questão anulada pelo IFRN não entra no aproveitamento.",
    }),
  );
  resultBox.appendChild(note);
  // As `notes` que a API devolve são trilha de auditoria do servidor
  // (evidência/pontuação), não conteúdo de estudo — por isso não entram
  // na tela do aluno.
}

function renderResultItems(res) {
  const items = [...(res.items ?? [])].sort((a, b) => a.position - b.position);
  if (items.length === 0) return;
  const head = el("h3", { text: "Correção por posição", attrs: { style: "font-size:var(--text-md)", class: "mt-4" } });
  resultBox.appendChild(head);
  for (const it of items) {
    let tone = "sim-result-item--pending";
    let verdict;
    let badgeClass = "badge";
    let badgeText = "Pendente";
    if (it.wasAnnulled) {
      tone = "sim-result-item--annulled";
      verdict = "Anulada — fora do aproveitamento.";
      badgeClass = "badge badge--warning";
      badgeText = "Anulada";
    } else if (it.unanswered) {
      verdict = "Não respondida (pendente, nunca erro inventado).";
    } else if (it.isCorrect === true) {
      tone = "sim-result-item--hit";
      verdict = `Acertou — você marcou ${choiceLabel(it.selectedOption)}, gabarito ${it.frozenAnswerKey}.`;
      badgeClass = "badge badge--success";
      badgeText = "Acertou";
    } else {
      tone = "sim-result-item--miss";
      verdict = `Errou — você marcou ${choiceLabel(it.selectedOption)}, gabarito ${it.frozenAnswerKey}.`;
      badgeClass = "badge badge--danger";
      badgeText = "Errou";
    }
    const row = el("div", { className: `sim-result-item ${tone}` });
    const title = it.sourceYear && it.sourceQuestionNumber
      ? `Posição ${it.position} · ${it.sourceYear} Q${it.sourceQuestionNumber}`
      : `Posição ${it.position}`;
    row.appendChild(el("strong", { text: title }));
    row.appendChild(el("span", { className: badgeClass, text: badgeText }));
    row.appendChild(el("span", { text: verdict }));
    resultBox.appendChild(row);
  }
}

/* ---------- raio-X pós-atividade (TASK 19.1) ---------- */

// Bloco "O que fazer agora" após a correção por posição: piores assuntos
// da atividade + erros + até 3 CTAs (revisar, praticar, atualizar plano).
// O assunto vem do caderno em cache (`state.details`); sem detalhe, o
// componente agrupa por disciplina — nunca inventa assunto.
function renderNextSteps(res) {
  let summary = null;
  try {
    summary = summarizeResult(res, state.details);
  } catch {
    return;
  }
  if (!summary) return;
  // TASK 20.2 (wizard "Descubra seu nível"): quando a execução faz parte do
  // diagnóstico (`?diag=LP|MAT`), o cartão do wizard abre o bloco — em mount
  // próprio ANTES dos CTAs genéricos (renderResultNext limpa o container que
  // recebe, então dividir o mount é obrigatório), para o aluno não se perder
  // no meio do caminho.
  renderDiagWizardStep(resultBox, res);
  const mount = el("div", { className: "mt-4" });
  resultBox.appendChild(mount);
  try {
    renderResultNext(mount, summary, { onReview: startReviewFromNext, onPlan: refreshPlanFromNext });
  } catch {
    mount.remove();
  }
}

/* ---------- wizard "Descubra seu nível" (TASK 20.2) ----------
 * Encadeia os 2 blocos do diagnóstico (6 LP + 6 MAT, Modo PROVA) e, ao fim,
 * gera o plano v1 automaticamente levando ao painel (`?origem=diagnostico`).
 * Códigos de disciplina resolvidos pelo catálogo; nada de código fixo.
 * Fora do wizard (`?diag=` ausente) esta função não renderiza nada. */
function readDiagStep() {
  let raw = "";
  try {
    raw = new URLSearchParams(window.location.search).get("diag") || "";
  } catch {
    raw = "";
  }
  const step = String(raw).trim().toUpperCase();
  return step === "LP" || step === "MAT" ? step : null;
}

async function resolveDiagCode(want) {
  const raw = await fetchDisciplines();
  const list = Array.isArray(raw) ? raw : (raw?.content || raw?.items || []);
  const norm = (s) => String(s || "").toLowerCase();
  for (const d of list) {
    const code = d.code || d.disciplineCode || "";
    const name = d.name || d.disciplineName || "";
    const hay = `${norm(code)} ${norm(name)}`;
    if (want === "LP" && (hay.includes("portugues") || /(^|[\s_-])lp($|[\s_-])/.test(hay))) return code;
    if (want === "MAT" && (hay.includes("matemat") || /(^|[\s_-])mat($|[\s_-])/.test(hay))) return code;
  }
  return null;
}

function renderDiagWizardStep(mount, res) {
  const step = readDiagStep();
  if (!step) return;
  const scored = Number(res?.scored ?? 0);
  const correct = Number(res?.correct ?? 0);
  const card = el("div", { className: "alert alert--success" });
  const inner = el("div");
  if (step === "LP") {
    inner.appendChild(el("strong", { text: `Etapa 1 concluída: ${correct} de ${scored} em Língua Portuguesa.` }));
    inner.appendChild(el("p", { text: "Falta só a etapa 2 (Matemática, 6 questões) para montarmos seu ponto de partida." }));
    const actions = el("div", { className: "btn-group mt-4" });
    const next = el("button", { className: "btn btn--primary", text: "Continuar: Matemática (6 questões)", attrs: { type: "button" } });
    next.addEventListener("click", async () => {
      setButtonLoading(next, true, "Montando Matemática…");
      try {
        const code = await resolveDiagCode("MAT");
        if (!code) {
          toast("Não encontrei Matemática no catálogo — tente de novo.", "info");
          return;
        }
        const institution = window.localStorage.getItem("voupassar.institution") || null;
        const created = await createByDiscipline({ disciplineCode: code, questionCount: 6, mode: "PROVA", institution });
        const id = created?.attemptId ?? created?.id;
        if (!id) {
          toast("Bloco criado, mas sem identificador — volte ao diagnóstico.", "info");
          return;
        }
        window.location.href = `./simulado.html?id=${encodeURIComponent(String(id))}&diag=MAT`;
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          showGuard();
          return;
        }
        toast(friendlyMessage(err), "info");
      } finally {
        setButtonLoading(next, false);
      }
    });
    actions.appendChild(next);
    inner.appendChild(actions);
  } else {
    inner.appendChild(el("strong", { text: `Diagnóstico concluído: ${correct} de ${scored} em Matemática.` }));
    inner.appendChild(el("p", { text: "Vamos juntar as 12 questões e montar seu ponto de partida com o roteiro. É uma estimativa inicial — fica mais precisa conforme você estuda." }));
    const actions = el("div", { className: "btn-group mt-4" });
    const finish = el("button", { className: "btn btn--primary", text: "Ver meu ponto de partida", attrs: { type: "button" } });
    finish.addEventListener("click", async () => {
      setButtonLoading(finish, true, "Montando seu roteiro…");
      try {
        const institution = window.localStorage.getItem("voupassar.institution") || null;
        await generatePlan(institution);
        window.location.href = `./dashboard.html?origem=diagnostico${institution ? `&institution=${encodeURIComponent(institution)}` : ""}`;
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          showGuard();
          return;
        }
        toast(friendlyMessage(err), "info");
      } finally {
        setButtonLoading(finish, false);
      }
    });
    actions.appendChild(finish);
    inner.appendChild(actions);
  }
  card.appendChild(inner);
  // Mount próprio com prepend: o cartão abre o bloco de resultado (antes do
  // placar), em vez de disputar o container que o result-next limpa.
  const wrap = el("div", { className: "mt-4" });
  wrap.appendChild(card);
  mount.prepend(wrap);
}

// "Revisar estes N agora": congela o topo da fila no assunto que mais
// pesou (topicId quando o caderno classificou, senão disciplina) com
// `onlyErrors` e abre a sessão em `?review=`. 401 → guard (o interceptor
// do client.js já tentou 1 refresh silencioso).
async function startReviewFromNext(summary, button) {
  const n = summary?.errorCount ?? 0;
  if (!n) {
    toast("Nenhum erro nesta atividade para revisar.", "info");
    return;
  }
  const top = summary.topGroup;
  setButtonLoading(button, true, "Criando revisão…");
  try {
    const session = await createSession({
      limit: Math.min(Math.max(n, 1), 100),
      ...(top?.topicId ? { topicId: top.topicId } : {}),
      ...(top?.disciplineCode ? { discipline: top.disciplineCode } : {}),
      onlyErrors: true,
    });
    const id = session?.id ?? session?.sessionId;
    if (!id) {
      toast("Revisão criada, mas sem identificador — abra a fila em Estudar.", "info");
      return;
    }
    window.location.href = `./simulado.html?review=${encodeURIComponent(String(id))}`;
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    toast(friendlyMessage(err), "info");
  } finally {
    setButtonLoading(button, false);
  }
}

// "Atualizar meu plano": regenera o roteiro (`POST /recommendations`) com
// os erros frescos e leva ao painel, onde o motivo atualizado aparece.
async function refreshPlanFromNext(_summary, button) {
  setButtonLoading(button, true, "Atualizando plano…");
  try {
    await generatePlan();
    toast("Plano atualizado com esta atividade. Abrindo seu painel.", "success");
    window.location.href = "./dashboard.html";
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    toast(friendlyMessage(err), "info");
  } finally {
    setButtonLoading(button, false);
  }
}

/* ---------- notas ---------- */

