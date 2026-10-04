/* VouPassar — área de estudos (TASK 6.5)
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=estudos.html).
 *
 * Cobre TASKS.md 6.5 (conteúdo, questões, progresso, navegação):
 * - Conteúdo: disciplinas + assuntos/subassuntos observados (3.3, taxonomia
 *   v1.1) com contagem histórica e aproveitamento do aluno (4.1).
 * - Questões: lista paginada com filtros (3.4, ordem fixa ano/número/id).
 * - Progresso: retrato geral + recorte atual (4.1) e item do roteiro (4.4).
 * - Navegação: filtros sincronizados na URL, paginação e atalhos do
 *   navegador de conteúdo para a lista. Prática livre em modo ESTUDO (3.7)
 *   com correção imediata do servidor (AGENTS.md §9 Modo Estudo).
 *
 * Sem innerHTML (só textContent via el()). Filtros inválidos (404 do
 * backend) viram erro visível com traceId, nunca página vazia silenciosa.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { restoreSession, logout } from "../api/auth.js";
import { injectFigureNotice } from "../components/figure.js";
import { renderPassages } from "../components/passage.js";
import { expressionNode } from "../components/math.js";
import {
  fetchDisciplines,
  fetchTopics,
  fetchSubtopics,
  fetchQuestions,
  openStudySession,
  submitAttempt,
  fetchOverview,
  fetchDiagnosis,
  fetchPlan,
} from "../api/estudos.js";
import { el, renderEmpty, renderErrorSummary, setButtonLoading, toast } from "../components/ui.js";
import { sourceTypeLabel, difficultyLabel, statusLabel, choiceLabel } from "../vocab.js";

const PAGE_SIZE = 10;
const SESSION_KEY = "voupassar.studySessionId";

const guard = document.getElementById("study-guard");
const errorBox = document.getElementById("study-error");
const loadingBox = document.getElementById("study-loading");
const content = document.getElementById("study-content");
const subtitle = document.getElementById("study-subtitle");

const form = document.getElementById("study-filters");
const selDisc = document.getElementById("f-disciplina");
const selTopic = document.getElementById("f-topico");
const selSub = document.getElementById("f-subtopico");
const selYear = document.getElementById("f-ano");
const selDiff = document.getElementById("f-dificuldade");
const btnClear = document.getElementById("btn-clear");
const btnFilter = document.getElementById("btn-filter");

const browserBox = document.getElementById("study-browser");
const browserCount = document.getElementById("study-browser-count");
const heroBox = document.getElementById("study-hero");
const recortePill = document.getElementById("study-recorte");
const recorteNum = document.getElementById("study-recorte-num");
const listBox = document.getElementById("study-list");
const countNote = document.getElementById("study-count");
const pagerBox = document.getElementById("study-pagination");
const progressBox = document.getElementById("study-progress");
const planBox = document.getElementById("study-plan");

const state = {
  user: null,
  disciplines: [],
  allTopics: [],
  topicsOfDisc: [],
  subtopicsOfTopic: [],
  overview: null,
  diagnosis: null,
  plan: null,
  planMissing: false,
  filters: { disciplineCode: "", topicId: "", subtopicId: "", year: "", difficulty: "", page: 0 },
  pageData: null,
  studySessionId: null,
};

wireLogoutButtons();
main();

async function main() {
  const user = await restoreSession().catch(() => null);
  if (!user) {
    showGuard();
    return;
  }
  state.user = user;
  subtitle.textContent = `Olá, ${user.displayName || "estudante"} — escolha disciplina e assunto, pratique com correção imediata e veja seu progresso no recorte.`;
  showLogoutButtons();
  readUrlIntoFilters();
  bindFilterEvents();
  state.studySessionId = readStoredSession();
  await loadAll();
}

function wireLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      btn.setAttribute("disabled", "");
      try {
        await logout();
      } finally {
        window.location.href = "./login.html?next=estudos.html";
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
  guard.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: "Entre para estudar" }));
  box.appendChild(
    el("p", {
      text: "A área de estudos mostra conteúdos, questões e seu progresso. Ela precisa da sua sessão — entre ou crie uma conta para continuar.",
    }),
  );
  const actions = el("div", { className: "btn-group", attrs: { style: "justify-content:center" } });
  actions.appendChild(
    el("a", {
      className: "btn btn--primary",
      text: "Entrar",
      attrs: { href: "./login.html?next=estudos.html" },
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

function readUrlIntoFilters() {
  const q = new URLSearchParams(window.location.search);
  state.filters.disciplineCode = (q.get("disciplina") || "").trim().toUpperCase();
  state.filters.topicId = (q.get("topico") || "").trim();
  state.filters.subtopicId = (q.get("subtopico") || "").trim();
  state.filters.year = (q.get("ano") || "").trim();
  state.filters.difficulty = (q.get("dificuldade") || "").trim().toUpperCase();
  const p = Number.parseInt(q.get("pagina") || "0", 10);
  state.filters.page = Number.isFinite(p) && p >= 0 ? p : 0;
}

function syncUrl() {
  const q = new URLSearchParams();
  const f = state.filters;
  if (f.disciplineCode) q.set("disciplina", f.disciplineCode);
  if (f.topicId) q.set("topico", f.topicId);
  if (f.subtopicId) q.set("subtopico", f.subtopicId);
  if (f.year) q.set("ano", f.year);
  if (f.difficulty) q.set("dificuldade", f.difficulty);
  if (f.page > 0) q.set("pagina", String(f.page));
  const qs = q.toString();
  window.history.replaceState(null, "", qs ? `./estudos.html?${qs}` : "./estudos.html");
}

/* ---------- carga inicial ---------- */

async function loadAll() {
  loadingBox.hidden = false;
  content.hidden = true;
  errorBox.textContent = "";
  try {
    const settled = await Promise.allSettled([
      fetchDisciplines(),
      fetchTopics(),
      fetchOverview(),
      fetchDiagnosis(),
      fetchPlan(),
    ]);
    const [discR, topicsR, overR, diagR, planR] = settled;

    if (discR.status === "fulfilled") {
      state.disciplines = Array.isArray(discR.value) ? discR.value : (discR.value?.content ?? []);
    } else {
      throw discR.reason;
    }
    state.allTopics = topicsR.status === "fulfilled"
      ? (Array.isArray(topicsR.value) ? topicsR.value : (topicsR.value?.content ?? []))
      : [];
    state.overview = overR.status === "fulfilled" ? overR.value : null;
    state.diagnosis = diagR.status === "fulfilled" ? diagR.value : null;
    if (planR.status === "fulfilled") {
      state.plan = planR.value;
      state.planMissing = false;
    } else if (planR.status === "rejected" && planR.reason?.status === 404) {
      state.plan = null;
      state.planMissing = true;
    } else {
      state.plan = null;
    }
    const warnings = settled
      .map((r, i) => ({ r, i }))
      .filter(({ r, i }) => r.status === "rejected" && i !== 0 && !(i === 4 && r.reason?.status === 404));
    if (warnings.length > 0 && !state.overview && state.allTopics.length === 0) {
      throw warnings[0].r.reason;
    }

    fillDisciplineSelect();
    applyFiltersToForm();
    await refreshDependentSelects();
    renderBrowser();
    renderProgress();
    renderPlan();
    // Sem conteúdo escolhido (nem na URL) não há caderno: a lista mostra
    // um estado-guia pedindo a escolha do conteúdo. O caderno só abre
    // depois que o aluno filtra ou toca em "Estudar" — ver hasRecorte().
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderHero();

    loadingBox.hidden = true;
    content.hidden = false;
  } catch (err) {
    loadingBox.hidden = true;
    console.error("[estudos] falha na carga inicial:", err);
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    renderErrorSummary(errorBox, {
      title: "Não foi possível carregar a área de estudos",
      items: [friendlyMessage(err)],
      traceId: err instanceof ApiError ? err.traceId : null,
    });
  }
}

/* ---------- filtros ---------- */

function fillDisciplineSelect() {
  selDisc.textContent = "";
  selDisc.appendChild(el("option", { text: "Todas", attrs: { value: "" } }));
  for (const d of state.disciplines) {
    selDisc.appendChild(
      el("option", {
        text: `${d.name} (${d.questionCount} questões)`,
        attrs: { value: d.code },
      }),
    );
  }
}

function applyFiltersToForm() {
  const codes = new Set(state.disciplines.map((d) => d.code));
  if (state.filters.disciplineCode && !codes.has(state.filters.disciplineCode)) {
    state.filters.disciplineCode = "";
  }
  selDisc.value = state.filters.disciplineCode || "";
  selYear.value = state.filters.year || "";
  selDiff.value = ["FACIL", "MEDIA", "DIFICIL"].includes(state.filters.difficulty)
    ? state.filters.difficulty
    : "";
  if (!["FACIL", "MEDIA", "DIFICIL"].includes(state.filters.difficulty)) {
    state.filters.difficulty = "";
  }
  if (state.filters.year && !["2020", "2022", "2023", "2024", "2025", "2026"].includes(state.filters.year)) {
    state.filters.year = "";
    selYear.value = "";
  }
}

function bindFilterEvents() {
  selDisc.addEventListener("change", async () => {
    state.filters.topicId = "";
    state.filters.subtopicId = "";
    state.filters.page = 0;
    await refreshDependentSelects();
    renderBrowser();
    renderHero();
  });
  selTopic.addEventListener("change", async () => {
    state.filters.subtopicId = "";
    state.filters.page = 0;
    await refreshDependentSelects();
  });
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    state.filters.disciplineCode = selDisc.value || "";
    state.filters.topicId = selTopic.disabled ? "" : (selTopic.value || "");
    state.filters.subtopicId = selSub.disabled ? "" : (selSub.value || "");
    state.filters.year = selYear.value || "";
    state.filters.difficulty = selDiff.value || "";
    state.filters.page = 0;
    syncUrl();
    setButtonLoading(btnFilter, true, "Filtrando…");
    try {
      if (hasRecorte()) {
        await loadQuestions();
      } else {
        renderQuestionsIdle();
      }
      renderProgress();
      renderPlan();
      renderHero();
    } finally {
      setButtonLoading(btnFilter, false);
    }
  });
  btnClear.addEventListener("click", async () => {
    selDisc.value = "";
    selTopic.value = "";
    selSub.value = "";
    selYear.value = "";
    selDiff.value = "";
    state.filters = { disciplineCode: "", topicId: "", subtopicId: "", year: "", difficulty: "", page: 0 };
    syncUrl();
    await refreshDependentSelects();
    renderBrowser();
    renderQuestionsIdle();
    renderProgress();
    renderPlan();
    renderHero();
  });
}

async function refreshDependentSelects() {
  const disc = selDisc.value || state.filters.disciplineCode || "";
  state.filters.disciplineCode = disc;

  // Assuntos: filtra por disciplina quando há, senão usa catálogo geral.
  let topics = state.allTopics;
  if (disc) {
    try {
      const data = await fetchTopics(disc);
      topics = Array.isArray(data) ? data : (data?.content ?? []);
    } catch {
      topics = state.allTopics.filter((t) => t.disciplineCode === disc);
    }
  }
  state.topicsOfDisc = topics;
  selTopic.textContent = "";
  selTopic.appendChild(el("option", { text: "Todos", attrs: { value: "" } }));
  for (const t of topics) {
    selTopic.appendChild(
      el("option", {
        text: `${t.name} (${t.questionCount})`,
        attrs: { value: String(t.id) },
      }),
    );
  }
  const topicIds = new Set(topics.map((t) => String(t.id)));
  if (state.filters.topicId && !topicIds.has(String(state.filters.topicId))) {
    state.filters.topicId = "";
    state.filters.subtopicId = "";
  }
  selTopic.value = state.filters.topicId || "";
  selTopic.disabled = topics.length === 0;

  // Subassuntos: só quando há assunto escolhido.
  const topicId = selTopic.value || "";
  state.filters.topicId = topicId;
  selSub.textContent = "";
  selSub.appendChild(el("option", { text: "Todos", attrs: { value: "" } }));
  if (!topicId) {
    selSub.value = "";
    selSub.disabled = true;
    state.subtopicsOfTopic = [];
    state.filters.subtopicId = "";
    return;
  }
  try {
    const data = await fetchSubtopics(topicId);
    const subs = Array.isArray(data) ? data : (data?.content ?? []);
    state.subtopicsOfTopic = subs;
    for (const s of subs) {
      selSub.appendChild(
        el("option", {
          text: `${s.name} (${s.questionCount})`,
          attrs: { value: String(s.id) },
        }),
      );
    }
    selSub.disabled = subs.length === 0;
    const subIds = new Set(subs.map((s) => String(s.id)));
    if (state.filters.subtopicId && !subIds.has(String(state.filters.subtopicId))) {
      state.filters.subtopicId = "";
    }
    selSub.value = state.filters.subtopicId || "";
  } catch (err) {
    selSub.disabled = true;
    state.subtopicsOfTopic = [];
    if (err instanceof ApiError && err.status === 404) {
      state.filters.topicId = "";
      selTopic.value = "";
      toast("Assunto não encontrado. Filtro de assunto removido.", "info");
    }
  }
}

/* ---------- navegador de conteúdo ---------- */

function topicAccuracy(topicId) {
  const rows = state.overview?.byTopic ?? [];
  return rows.find((r) => String(r.topicId) === String(topicId)) || null;
}

function disciplineAccuracy(code) {
  const rows = state.overview?.byDiscipline ?? [];
  return rows.find((r) => r.disciplineCode === code) || null;
}

function formatPercent(acc) {
  if (acc === null || acc === undefined) return "—";
  const n = Number(acc);
  if (!Number.isFinite(n)) return "—";
  return `${(n * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
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

function formatPercentValue(acc) {
  if (acc === null || acc === undefined) return null;
  const n = Number(acc);
  if (!Number.isFinite(n)) return null;
  return Math.round(n * 100);
}

function currentTopicName() {
  const id = state.filters.topicId;
  if (!id) return null;
  return state.allTopics.find((t) => String(t.id) === String(id))?.name
    || state.topicsOfDisc.find((t) => String(t.id) === String(id))?.name
    || null;
}

function currentDisciplineName() {
  const code = state.filters.disciplineCode;
  if (!code) return null;
  return state.disciplines.find((d) => d.code === code)?.name || null;
}

function planDoingItem() {
  const items = state.plan?.items ?? state.plan?.planItems ?? state.plan?.recommendations ?? [];
  if (!Array.isArray(items) || items.length === 0) return null;
  return items.find((it) => it.status === "DOING") || items.find((it) => it.status === "TODO") || items[0] || null;
}

function planTopicName(topicId) {
  return state.allTopics.find((t) => String(t.id) === String(topicId))?.name
    || state.topicsOfDisc.find((t) => String(t.id) === String(topicId))?.name
    || "Assunto do seu roteiro";
}

function renderHero() {
  if (!heroBox) return;
  heroBox.textContent = "";
  const total = state.pageData?.totalElements ?? null;
  if (recortePill && recorteNum) {
    if (total === null) {
      recortePill.hidden = true;
    } else {
      recortePill.hidden = false;
      recorteNum.textContent = String(total);
    }
  }
  const card = el("div", { className: "next-step" });
  const topicName = currentTopicName();
  const discName = currentDisciplineName();
  const doing = planDoingItem();
  const acc = state.overview?.accuracy ?? null;
  // Sem recorte não há caderno aberto: o hero convida a escolher o
  // conteúdo (a lista abaixo mostra o estado-guia, não questões).
  const idle = !hasRecorte();

  let title = "Pratique no seu ritmo";
  let why = "Escolha uma disciplina e um assunto para ver as questões oficiais daquele recorte, com correção na hora.";
  if (topicName && discName) {
    title = topicName;
    const ta = topicAccuracy(state.filters.topicId);
    why = ta && ta.scored > 0
      ? `Você acertou ${ta.correct} de ${ta.scored} neste assunto (${formatPercent(ta.accuracy)}). Continue praticando as questões abaixo.`
      : `Você ainda não respondeu nada deste assunto. As questões abaixo são o melhor ponto de partida.`;
  } else if (discName) {
    title = discName;
    const da = disciplineAccuracy(state.filters.disciplineCode);
    why = da && da.scored > 0
      ? `Seu aproveitamento aqui é ${formatPercent(da.accuracy)} (${da.correct} acertos em ${da.scored}). Escolha um assunto para refinar.`
      : "Escolha um assunto desta disciplina para começar a praticar com correção imediata.";
  } else if (doing) {
    const name = planTopicName(doing.topicId);
    title = name;
    if (idle) {
      why = "Este é o próximo item do seu roteiro. Toque em “Abrir assunto do roteiro” para ver as questões dele.";
    } else if (acc !== null && acc !== undefined) {
      why = `Este é o próximo item do seu roteiro e seu aproveitamento geral é ${formatPercent(acc)}. Pratique as questões deste assunto abaixo.`;
    } else {
      why = "Este é o próximo item do seu roteiro. Pratique as questões deste assunto abaixo.";
    }
  } else if (state.planMissing || !state.plan) {
    title = "Monte seu roteiro para estudar com ordem";
    why = "O roteiro coloca os assuntos na ordem que vale mais a pena para você. Gere no painel e volte aqui para praticar.";
  }
  card.appendChild(el("p", { className: "next-step__eyebrow", text: "Continue de onde parou" }));
  card.appendChild(el("h3", { className: "next-step__title", text: title }));
  card.appendChild(el("p", { className: "next-step__why", text: why }));

  const chips = el("div", { className: "next-step__meta" });
  if (discName) chips.appendChild(el("span", { className: "chip", text: discName }));
  if (topicName) chips.appendChild(el("span", { className: "chip", text: topicName }));
  if (total !== null) chips.appendChild(el("span", { className: "chip", text: total === 1 ? "1 questão no recorte" : `${total} questões no recorte` }));
  if (!discName && doing?.priority !== undefined && doing?.priority !== null) {
    chips.appendChild(el("span", { className: "chip", text: `${doing.priority}º no roteiro` }));
  }
  if (chips.childNodes.length > 0) card.appendChild(chips);

  const actions = el("div", { className: "next-step__actions" });
  const practice = idle
    ? el("a", {
      className: "btn btn--primary",
      text: "Escolher conteúdo",
      attrs: { href: "#sec-conteudo-t" },
    })
    : el("a", {
      className: "btn btn--primary",
      text: "Praticar questões abaixo",
      attrs: { href: "#sec-questoes-t" },
    });
  actions.appendChild(practice);
  if (!topicName && doing?.topicId) {
    const open = el("a", {
      className: "btn btn--secondary",
      text: "Abrir assunto do roteiro",
      attrs: { href: `./estudos.html?topico=${encodeURIComponent(String(doing.topicId))}` },
    });
    actions.appendChild(open);
  } else {
    const dash = el("a", {
      className: "btn btn--secondary",
      text: "Ver meu painel",
      attrs: { href: "./dashboard.html" },
    });
    actions.appendChild(dash);
  }
  card.appendChild(actions);
  heroBox.appendChild(card);
}

function renderBrowser() {
  browserBox.textContent = "";
  if (browserCount) browserCount.textContent = "";
  if (state.disciplines.length === 0) {
    browserBox.appendChild(el("p", { className: "muted", text: "Nenhuma disciplina no catálogo." }));
    return;
  }
  const visible = state.filters.disciplineCode
    ? state.disciplines.filter((d) => d.code === state.filters.disciplineCode)
    : state.disciplines;
  let topicTotal = 0;
  for (const d of visible) {
    const card = el("div", { className: "browser-disc" });
    const head = el("div", { className: "browser-disc__head" });
    const avatar = el("span", { className: "browser-disc__avatar", text: initialsOf(d.name) });
    avatar.style.setProperty("--bd", avatarColor(d.code || d.name));
    avatar.setAttribute("aria-hidden", "true");
    head.appendChild(avatar);
    const titleWrap = el("div", { className: "browser-disc__title" });
    titleWrap.appendChild(el("span", { className: "browser-disc__name", text: d.name }));
    const acc = disciplineAccuracy(d.code);
    const hasScore = acc && acc.scored > 0;
    const meta = hasScore
      ? `${d.questionCount} questões · ${acc.correct} acertos em ${acc.scored}`
      : `${d.questionCount} questões · sem tentativas pontuáveis`;
    titleWrap.appendChild(el("span", { className: "browser-disc__meta", text: meta }));
    head.appendChild(titleWrap);
    const accBadge = el("span", {
      className: hasScore ? "browser-disc__acc" : "browser-disc__acc browser-disc__acc--empty",
      text: hasScore ? formatPercent(acc.accuracy) : "—",
    });
    head.appendChild(accBadge);
    card.appendChild(head);

    const pct = hasScore ? (formatPercentValue(acc.accuracy) ?? 0) : 0;
    const track = el("div", {
      className: "browser-disc__track",
      attrs: {
        role: "progressbar",
        "aria-valuemin": "0",
        "aria-valuemax": "100",
        "aria-valuenow": String(pct),
        "aria-label": `Seu aproveitamento em ${d.name}`,
      },
    });
    const fill = el("div", { className: "browser-disc__fill" });
    fill.style.width = `${pct}%`;
    track.appendChild(fill);
    card.appendChild(track);

    const topics = (state.filters.disciplineCode ? state.topicsOfDisc : state.allTopics)
      .filter((t) => t.disciplineCode === d.code);
    topicTotal += topics.length;
    if (topics.length === 0) {
      card.appendChild(el("p", { className: "muted", text: "Sem assuntos neste recorte." }));
    } else {
      const ul = el("ul", { className: "browser-topics" });
      for (const t of topics.slice(0, 12)) {
        const li = el("li", { className: "browser-topics__row" });
        const left = el("div", { className: "browser-topics__main" });
        left.appendChild(el("div", { className: "browser-topics__name", text: t.name }));
        const ta = topicAccuracy(t.id);
        const tHas = ta && ta.scored > 0;
        const tmeta = tHas
          ? `${t.questionCount} questões · você ${formatPercent(ta.accuracy)} (${ta.correct}/${ta.scored})`
          : `${t.questionCount} questões no acervo`;
        left.appendChild(el("div", { className: "browser-topics__meta", text: tmeta }));
        if (tHas) {
          const meter = el("div", { className: "browser-topics__meter" });
          const mite = el("i");
          mite.style.width = `${formatPercentValue(ta.accuracy) ?? 0}%`;
          meter.appendChild(mite);
          left.appendChild(meter);
        }
        li.appendChild(left);
        const btn = el("button", {
          className: "btn btn--secondary btn--sm",
          text: "Estudar",
          attrs: { type: "button", "aria-label": `Estudar ${t.name}` },
        });
        btn.addEventListener("click", async () => {
          selDisc.value = d.code;
          state.filters.disciplineCode = d.code;
          await refreshDependentSelects();
          selTopic.value = String(t.id);
          state.filters.topicId = String(t.id);
          state.filters.subtopicId = "";
          state.filters.page = 0;
          syncUrl();
          await refreshDependentSelects();
          renderBrowser();
          await loadQuestions();
          renderProgress();
          renderPlan();
          renderHero();
          document.getElementById("sec-questoes-t").scrollIntoView({ block: "start" });
        });
        li.appendChild(btn);
        ul.appendChild(li);
      }
      card.appendChild(ul);
      if (topics.length > 12) {
        card.appendChild(
          el("p", {
            className: "muted",
            text: `Mostrando 12 de ${topics.length} assuntos — refine pelo filtro de assunto.`,
          }),
        );
      }
    }
    browserBox.appendChild(card);
  }
  if (browserCount) {
    const dLabel = visible.length === 1 ? "1 disciplina" : `${visible.length} disciplinas`;
    const tLabel = topicTotal === 1 ? "1 assunto" : `${topicTotal} assuntos`;
    browserCount.textContent = `${dLabel} · ${tLabel}`;
  }
}

/* ---------- questões + paginação ---------- */

// Há recorte quando o aluno escolheu algum conteúdo (disciplina, assunto,
// subassunto, edição ou dificuldade). Sem recorte não abrimos caderno: a
// página é "estudar por conteúdo" e despejar todas as questões de cara
// confunde. Nesse caso a lista mostra um estado-guia.
function hasRecorte() {
  const f = state.filters;
  return Boolean(f.disciplineCode || f.topicId || f.subtopicId || f.year || f.difficulty);
}

function renderQuestionsIdle() {
  state.pageData = null;
  listBox.textContent = "";
  pagerBox.textContent = "";
  renderEmpty(listBox, {
    title: "Escolha um conteúdo para começar",
    description: "Toque em “Estudar” num assunto acima ou use os filtros para escolher disciplina e assunto. As questões daquele conteúdo aparecem aqui.",
  });
  countNote.textContent = "Nenhum conteúdo selecionado — escolha acima para ver as questões.";
}

function questionQuery() {
  const f = state.filters;
  return {
    ...(f.disciplineCode ? { disciplineCode: f.disciplineCode } : {}),
    ...(f.topicId ? { topicId: f.topicId } : {}),
    ...(f.subtopicId ? { subtopicId: f.subtopicId } : {}),
    ...(f.year ? { year: f.year } : {}),
    ...(f.difficulty ? { difficulty: f.difficulty } : {}),
    page: f.page,
    size: PAGE_SIZE,
  };
}

async function loadQuestions() {
  listBox.textContent = "";
  pagerBox.textContent = "";
  countNote.textContent = "Buscando questões…";
  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Buscando questões…" }));
  listBox.appendChild(loading);
  try {
    const data = await fetchQuestions(questionQuery());
    state.pageData = data;
    renderQuestions(data);
    renderHero();
  } catch (err) {
    listBox.textContent = "";
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    const box = el("div", { className: "alert alert--danger", attrs: { role: "alert" } });
    box.appendChild(el("strong", { text: "Não foi possível listar as questões. " }));
    box.appendChild(el("span", { text: friendlyMessage(err) }));
    if (err instanceof ApiError && err.traceId) {
      box.appendChild(el("p", { className: "envelope mt-2", text: `Código de rastreio: ${err.traceId}` }));
    }
    if (err instanceof ApiError && [400, 404].includes(err.status)) {
      const reset = el("button", {
        className: "btn btn--secondary btn--sm mt-2",
        text: "Limpar filtros",
        attrs: { type: "button" },
      });
      reset.addEventListener("click", () => btnClear.click());
      box.appendChild(reset);
    }
    listBox.appendChild(box);
    countNote.textContent = "Falha ao buscar questões.";
  }
}

function renderQuestions(data) {
  listBox.textContent = "";
  pagerBox.textContent = "";
  const items = data?.content ?? [];
  const total = data?.totalElements ?? 0;
  if (total === 0) {
    renderEmpty(listBox, {
      title: "Nenhuma questão neste recorte",
      description: "Ajuste os filtros (outra disciplina, assunto ou edição). Origem válida sem linhas é página vazia legítima — nunca erro silencioso.",
    });
    countNote.textContent = "0 questões neste recorte.";
    return;
  }
  const page = data?.page ?? state.filters.page;
  const totalPages = data?.totalPages ?? 1;
  countNote.textContent = `${total} ${total === 1 ? "questão" : "questões"} · página ${page + 1} de ${totalPages} (ordem fixa: ano, número).`;
  for (const q of items) {
    listBox.appendChild(renderQuestionCard(q));
  }
  renderPager(data);
}

function questionTitle(q) {
  const parts = [];
  if (q.examYear && q.questionNumber) parts.push(`${q.examYear} Q${q.questionNumber}`);
  else if (q.questionNumber) parts.push(`Q${q.questionNumber}`);
  else parts.push(`Questão #${q.id}`);
  if (q.discipline?.name) parts.push(q.discipline.name);
  return parts.join(" · ");
}

function renderQuestionCard(q) {
  const card = el("article", { className: "question-card", attrs: { "aria-labelledby": `q-${q.id}-t` } });
  const head = el("div", { className: "question-card__head" });
  head.appendChild(el("span", { className: "question-card__id", text: questionTitle(q), attrs: { id: `q-${q.id}-t` } }));
  head.appendChild(el("span", {
    className: "badge",
    text: sourceTypeLabel(q.sourceType),
  }));
  if (q.annulled) {
    head.appendChild(el("span", { className: "badge badge--warning", text: "Anulada" }));
  } else if (difficultyLabel(q.difficultyEstimate)) {
    head.appendChild(
      el("span", {
        className: "badge",
        text: `Dificuldade estimada: ${difficultyLabel(q.difficultyEstimate)}`,
      }),
    );
  }
  card.appendChild(head);

  card.appendChild(el("p", { className: "question-card__statement", text: q.statement || "(enunciado ainda não conferido)" }));
  // Textos-base (TASK 6.9): expansíveis logo após o enunciado, em todos os modos.
  renderPassages(q, card);
  if (q.hasFigure) {
    // Usa componente para aviso com referência de curadoria + página
    const noticeP = el("p", { className: "question-card__figure" });
    card.appendChild(noticeP);
    // Deixa a mensagem inicial; o componente pode enriquecer se o manifest estiver disponível
    injectFigureNotice(q, card);
  }
  const topicLine = q.topic?.name
    ? `Assunto: ${q.topic.name}${q.subtopic?.name ? ` · ${q.subtopic.name}` : ""}`
    : "Assunto ainda sem classificação — passamos por revisão antes de mostrar.";
  card.appendChild(el("p", { className: "question-card__figure", text: topicLine }));

  const startedAt = Date.now();
  const fieldset = el("fieldset", { className: "question-options" });
  fieldset.appendChild(el("legend", { text: "Sua resposta (modo Estudo: correção imediata)" }));
  const group = `q-${q.id}-opt`;
  const radios = [];
  for (const opt of (q.options ?? [])) {
    const label = el("label", { className: "option-row" });
    const input = el("input", { attrs: { type: "radio", name: group, value: opt.label } });
    input.disabled = q.annulled;
    label.appendChild(input);
    label.appendChild(el("span", { className: "option-row__letter", text: `${opt.label})` }));
    label.appendChild(expressionNode(opt.text || ""));
    fieldset.appendChild(label);
    radios.push(input);
  }
  if (q.annulled) {
    fieldset.appendChild(
      el("p", { className: "muted", text: "Questão anulada pelo gabarito: conta como conteúdo respondido e fica fora do aproveitamento." }),
    );
  }
  card.appendChild(fieldset);

  const actions = el("div", { className: "btn-group" });
  const btnAnswer = el("button", {
    className: "btn btn--primary btn--sm",
    text: "Responder",
    attrs: { type: "button" },
  });
  const btnBlank = el("button", {
    className: "btn btn--ghost btn--sm",
    text: "Responder em branco",
    attrs: { type: "button" },
  });
  if (q.annulled) {
    btnAnswer.disabled = true;
    btnBlank.disabled = true;
  }
  actions.appendChild(btnAnswer);
  actions.appendChild(btnBlank);
  try {
    const voltar = `./estudos.html${window.location.search || ""}`;
    actions.appendChild(
      el("a", {
        className: "btn btn--secondary btn--sm",
        text: "Abrir em tela dedicada",
        attrs: {
          href: `./questao.html?id=${encodeURIComponent(String(q.id))}&voltar=${encodeURIComponent(voltar)}`,
        },
      }),
    );
  } catch {
    actions.appendChild(
      el("a", {
        className: "btn btn--secondary btn--sm",
        text: "Abrir em tela dedicada",
        attrs: { href: `./questao.html?id=${encodeURIComponent(String(q.id))}` },
      }),
    );
  }
  card.appendChild(actions);

  const feedback = el("div", { className: "question-feedback", attrs: { role: "status", hidden: "" } });
  card.appendChild(feedback);

  // q.notes traz a trilha de auditoria do backend ("curadoria TASK 12.2",
  // "NECESSITA REVISÃO"): é documentação interna, não conteúdo de estudo,
  // então não é exibida. O que importa para o aluno é a explicação.
  if (!q.explanation) {
    const notes = el("ul", { className: "question-notes" });
    notes.appendChild(
      el("li", { text: "A explicação desta questão ainda não foi escrita." }),
    );
    card.appendChild(notes);
  }

  async function answer(selected) {
    const choice = selected === "BLANK" ? "BLANK" : selected;
    if (!choice) {
      toast("Escolha uma alternativa ou responda em branco.", "info");
      fieldset.querySelector("input")?.focus();
      return;
    }
    setButtonLoading(btnAnswer, true, "Corrigindo…");
    btnBlank.disabled = true;
    try {
      const sessionId = await ensureStudySession();
      const elapsed = Math.max(0, Math.round((Date.now() - startedAt) / 1000));
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
      showFeedback(feedback, q, attempt, choice);
      for (const r of radios) r.disabled = true;
      btnAnswer.disabled = true;
      btnBlank.disabled = true;
      const retry = el("button", {
        className: "btn btn--secondary btn--sm",
        text: "Tentar novamente",
        attrs: { type: "button" },
      });
      retry.addEventListener("click", () => {
        for (const r of radios) {
          r.disabled = q.annulled;
          r.checked = false;
        }
        feedback.hidden = true;
        feedback.textContent = "";
        btnAnswer.disabled = q.annulled;
        btnBlank.disabled = q.annulled;
        retry.remove();
        fieldset.querySelector("input")?.focus();
      });
      actions.appendChild(retry);
      refreshProgressSoon();
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        showGuard();
        return;
      }
      toast(friendlyMessage(err), "info");
    } finally {
      setButtonLoading(btnAnswer, false);
      if (!btnAnswer.disabled) btnBlank.disabled = false;
    }
  }

  btnAnswer.addEventListener("click", () => {
    const checked = fieldset.querySelector("input:checked");
    answer(checked?.value || null);
  });
  btnBlank.addEventListener("click", () => answer("BLANK"));

  return card;
}

function showFeedback(box, question, attempt, choice) {
  box.textContent = "";
  box.hidden = false;
  const correct = question.answerKey || "—";
  const choiceText = choiceLabel(choice);
  if (attempt?.wasAnnulled || question.annulled) {
    box.dataset.tone = "warning";
    box.appendChild(el("strong", { text: "Questão anulada — fora do aproveitamento." }));
    box.appendChild(el("p", { text: `Você marcou ${choiceText}. O gabarito oficial traz X (anulada). o IFRN não diz como pontuar questões anuladas.` }));
  } else if (attempt?.isCorrect === true) {
    box.dataset.tone = "success";
    box.appendChild(el("strong", { text: `Você acertou — alternativa ${correct}.` }));
    box.appendChild(el("p", { text: `Sua resposta: ${choiceText}.` }));
  } else {
    box.dataset.tone = "danger";
    box.appendChild(el("strong", { text: `Não foi dessa vez — resposta correta: ${correct}.` }));
    box.appendChild(el("p", { text: `Você marcou ${choiceText}.` }));
  }
  if (question.explanation) {
    box.appendChild(el("p", { text: `Explicação: ${question.explanation}` }));
  } else {
    box.appendChild(el("p", { text: "A explicação desta questão ainda não foi escrita." }));
  }
  const topicLine = question.topic?.name
    ? `Conteúdo: ${question.topic.name}${question.subtopic?.name ? ` · ${question.subtopic.name}` : ""}.`
    : "Conteúdo: assunto ainda sem classificação (passa por revisão).";
  box.appendChild(el("p", { text: topicLine }));
  // attempt.notes é trilha de auditoria do servidor — ver comentário acima.
}

function renderPager(data) {
  pagerBox.textContent = "";
  const page = data?.page ?? 0;
  const totalPages = data?.totalPages ?? 1;
  const info = el("span", {
    className: "study-pagination__info",
    text: `Página ${page + 1} de ${totalPages}`,
    attrs: { role: "status" },
  });
  const group = el("div", { className: "btn-group" });
  const prev = el("button", {
    className: "btn btn--secondary btn--sm",
    text: "← Anterior",
    attrs: { type: "button" },
  });
  const next = el("button", {
    className: "btn btn--secondary btn--sm",
    text: "Próxima →",
    attrs: { type: "button" },
  });
  prev.disabled = data?.first ?? page <= 0;
  next.disabled = data?.last ?? page >= totalPages - 1;
  prev.addEventListener("click", async () => {
    if (state.filters.page <= 0) return;
    state.filters.page -= 1;
    syncUrl();
    await loadQuestions();
    document.getElementById("sec-questoes-t").focus?.();
    document.getElementById("sec-questoes-t").scrollIntoView({ block: "start" });
  });
  next.addEventListener("click", async () => {
    state.filters.page += 1;
    syncUrl();
    await loadQuestions();
    document.getElementById("sec-questoes-t").scrollIntoView({ block: "start" });
  });
  group.appendChild(prev);
  group.appendChild(next);
  pagerBox.appendChild(info);
  pagerBox.appendChild(group);
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

/* ---------- progresso + roteiro + notas ---------- */

let progressTimer = null;
function refreshProgressSoon() {
  if (progressTimer) return;
  progressTimer = setTimeout(async () => {
    progressTimer = null;
    try {
      state.overview = await fetchOverview();
      renderProgress();
      renderBrowser();
      renderHero();
    } catch {
      // Mantém o retrato anterior; a próxima resposta tenta de novo.
    }
  }, 400);
}

function statTile({ label, value, hint, empty = false, textual = false }) {
  const card = el("article", { className: "stat-tile", attrs: { role: "listitem" } });
  card.appendChild(el("p", { className: "stat-tile__label", text: label }));
  const v = el("div", {
    className: textual ? "stat-tile__value stat-tile__value--text" : "stat-tile__value",
    text: value,
  });
  if (empty) v.classList.add("stat-tile__value--empty");
  card.appendChild(v);
  if (hint) card.appendChild(el("p", { className: "stat-tile__hint", text: hint }));
  return card;
}

function renderProgress() {
  progressBox.textContent = "";
  const over = state.overview;
  if (!over) {
    progressBox.appendChild(el("p", { className: "muted", text: "Desempenho indisponível no momento." }));
    return;
  }
  // O overview devolve scoredAttempts (não scored); usar o campo errado
  // imprimia "99/undefined" para o aluno.
  const scored = over.scoredAttempts ?? 0;
  const correct = over.correct ?? 0;
  const tiles = el("div", { className: "study-tiles", attrs: { role: "list", "aria-label": "Resumo de desempenho" } });
  tiles.appendChild(statTile({
    label: "Aproveitamento",
    value: formatPercent(over.accuracy),
    empty: over.accuracy === null || over.accuracy === undefined,
    hint: `${correct} acertos em ${scored} que valem nota.`,
  }));
  tiles.appendChild(statTile({
    label: "Anuladas",
    value: String(over.annulled ?? 0),
    hint: "Aparecem, mas ficam fora do cálculo.",
  }));
  progressBox.appendChild(tiles);

  const pct = formatPercentValue(over.accuracy) ?? 0;
  const meter = el("div", {
    className: "study-meter",
    attrs: {
      role: "progressbar",
      "aria-valuemin": "0",
      "aria-valuemax": "100",
      "aria-valuenow": String(pct),
      "aria-label": "Aproveitamento geral",
    },
  });
  const mfill = el("div", { className: "study-meter__fill" });
  mfill.style.width = `${pct}%`;
  meter.appendChild(mfill);
  progressBox.appendChild(meter);

  const stats = el("div", { className: "progress-stats mt-4" });
  const f = state.filters;
  if (f.disciplineCode) {
    const row = disciplineAccuracy(f.disciplineCode);
    const name = state.disciplines.find((d) => d.code === f.disciplineCode)?.name || "Disciplina";
    stats.appendChild(statRow(`${name}`, row && row.scored > 0 ? `${formatPercent(row.accuracy)} (${row.correct}/${row.scored})` : "sem tentativas neste filtro"));
  }
  if (f.topicId) {
    const row = topicAccuracy(f.topicId);
    const name = currentTopicName() || "Assunto atual";
    stats.appendChild(statRow(`${name}`, row && row.scored > 0 ? `${formatPercent(row.accuracy)} (${row.correct}/${row.scored})` : "sem tentativas neste filtro"));
  }
  if (!f.disciplineCode && !f.topicId) {
    stats.appendChild(statRow("Recorte atual", "geral (sem filtro)"));
  }
  progressBox.appendChild(stats);
  const link = el("a", {
    className: "btn btn--ghost btn--sm mt-4",
    text: "Ver painel completo",
    attrs: { href: "./dashboard.html" },
  });
  progressBox.appendChild(link);
}

function statRow(label, value) {
  const row = el("div", { className: "progress-stats__row" });
  row.appendChild(el("span", { text: label }));
  row.appendChild(el("span", { className: "progress-stats__value", text: value }));
  return row;
}

function renderPlan() {
  planBox.textContent = "";
  if (state.planMissing || !state.plan) {
    planBox.appendChild(el("p", {
      className: "muted",
      text: "Sem roteiro vigente. Gere seu roteiro no painel para ver aqui o item do assunto atual.",
    }));
    planBox.appendChild(el("a", {
      className: "btn btn--secondary btn--sm mt-4",
      text: "Abrir meu painel",
      attrs: { href: "./dashboard.html" },
    }));
    return;
  }
  const items = [...(state.plan.items ?? state.plan.planItems ?? state.plan.recommendations ?? [])]
    .sort((a, b) => (a.priority - b.priority) || ((a.id ?? 0) - (b.id ?? 0)));
  const f = state.filters;
  const match = f.topicId
    ? items.find((it) => String(it.topicId) === String(f.topicId))
    : (items.find((it) => it.status === "DOING") || items[0]);
  if (!match) {
    planBox.appendChild(el("p", { className: "muted", text: "Este recorte não está no roteiro vigente." }));
    planBox.appendChild(el("a", {
      className: "btn btn--ghost btn--sm mt-2",
      text: "Ver roteiro completo",
      attrs: { href: "./dashboard.html" },
    }));
    return;
  }
  const done = items.filter((i) => i.status === "DONE").length;
  const bar = el("div", {
    className: "progress",
    attrs: {
      role: "progressbar",
      "aria-valuemin": "0",
      "aria-valuemax": String(items.length),
      "aria-valuenow": String(done),
      "aria-label": "Progresso do roteiro",
    },
  });
  const fill = el("div", { className: "progress__bar" });
  fill.style.width = `${items.length ? Math.round((done / items.length) * 100) : 0}%`;
  bar.appendChild(fill);
  planBox.appendChild(bar);
  planBox.appendChild(el("p", {
    className: "stat-label",
    text: `${done} de ${items.length} concluídos no roteiro`,
  }));

  const list = el("ol", { className: "plan-items mt-4" });
  const row = el("li", {
    className: match.status === "DONE" ? "plan-items__row plan-items__row--done" : "plan-items__row",
  });
  const left = el("div");
  left.appendChild(el("span", { className: "study-plan-name", text: `${match.priority}º · ${planTopicName(match.topicId)}` }));
  if (statusLabel(match.status)) {
    left.appendChild(el("p", { className: "plan-items__meta", text: statusLabel(match.status) }));
  }
  row.appendChild(left);
  list.appendChild(row);
  planBox.appendChild(list);
  planBox.appendChild(el("a", {
    className: "btn btn--ghost btn--sm mt-4",
    text: "Ver roteiro completo",
    attrs: { href: "./dashboard.html" },
  }));
}

