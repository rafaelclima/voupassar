/* VouPassar — área de estudos (TASK 6.5)
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=estudos.html).
 *
 * Cobre TASKS.md 6.5 (conteúdo, questões, progresso, navegação):
 * - Conteúdo: disciplinas + assuntos/subassuntos observados (3.3, taxonomia
 *   v1.1) com contagem histórica e aproveitamento do aluno (4.1).
 * - Questões: lista paginada com filtros (3.4, ordem fixa ano/número/id).
 *   Filtro Origem (TASK 15.4: Todas/Oficiais/Autorais, `?fonte=`, selo via
 *   `sourceTypeLabel` + resumo do recorte traduzido, nunca enum cru).
 * - Progresso: retrato geral + recorte atual (4.1) e item do roteiro (4.4).
 * - Navegação: filtros sincronizados na URL, paginação e atalhos do
 *   navegador de conteúdo para a lista. Prática livre em modo ESTUDO (3.7)
 *   com correção imediata do servidor (AGENTS.md §9 Modo Estudo).
 * - Revisão guiada (TASK 17.2, seção `#sec-revisao-t`): fila priorizada com
 *   filtros disciplina/assunto/só-erros + "Iniciar revisão (N)". Dono é
 *   `views/revisao.js` (este arquivo só chama `initReviewSection()` após
 *   auth); atalho `?aba=revisao` rola até a seção.
 *
 * Sem innerHTML (só textContent via el()). Filtros inválidos (404 do
 * backend) viram erro visível com traceId, nunca página vazia silenciosa.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { logout } from "../api/auth.js";
import { renderAuthGuard, requireSessionOrGuard } from "./auth-shared.js";
import { mountExpandableFigure, preloadManifest } from "../components/figure.js";
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
import { parseEvidence, evidenceLine, revisaoHref, sampleHref } from "../components/plan-evidence.js";
import { summarizeResult, renderResultNext } from "../components/result-next.js";
import { createSession } from "../api/review.js";
import { generatePlan } from "../api/dashboard.js";
import { initReviewSection, refreshReviewForInstitution } from "./revisao.js";
import { currentInstitution, getInstitution, setInstitution } from "../state/process.js";
import { mountProcessSelector } from "../components/process-selector.js";

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
const selOrigem = document.getElementById("f-origem");
const btnClear = document.getElementById("btn-clear");
const btnFilter = document.getElementById("btn-filter");

const browserBox = document.getElementById("study-browser");
const browserCount = document.getElementById("study-browser-count");
const heroBox = document.getElementById("study-hero");
const ajustarLink = document.getElementById("study-ajustar");
const filtrosTitle = document.getElementById("sec-filtros-t");
const recortePill = document.getElementById("study-recorte");
const recorteNum = document.getElementById("study-recorte-num");
const listBox = document.getElementById("study-list");
const countNote = document.getElementById("study-count");
const pagerBox = document.getElementById("study-pagination");
const pagerTop = document.getElementById("study-pagination-top");
const recorteResumo = document.getElementById("study-recorte-resumo");
const progressBox = document.getElementById("study-progress");
const planBox = document.getElementById("study-plan");

const state = {
  user: null,
  /* Trilha IFRN/EAJ (TASK F.1): default IFRN; URL ?institution= vence o
   * persistido (ver currentInstitution). Tudo — catálogo, questões,
   * diagnóstico, roteiro, revisão — segue esta trilha. */
  institution: "IFRN",
  disciplines: [],
  allTopics: [],
  topicsOfDisc: [],
  subtopicsOfTopic: [],
  overview: null,
  diagnosis: null,
  plan: null,
  planMissing: false,
  filters: { disciplineCode: "", topicId: "", subtopicId: "", year: "", difficulty: "", sourceType: "", page: 0 },
  // Limite visível de assuntos por disciplina no navegador ("Mostrar mais").
  // Padrão 12 para a primeira pintura não virar lista infinita; expandir é
  // só UI (sem novo fetch), então não inventa conteúdo — ver renderBrowser().
  browserLimits: {},
  // Origem da navegação ("painel" quando o aluno toca em "Praticar este
  // assunto" no dashboard). Não é filtro de questões: serve só para o hero
  // priorizar o assunto escolhido em vez do item genérico do roteiro.
  // Limpa assim que o aluno navega por conta própria (filtrar, limpar ou
  // tocar em "Estudar" no navegador) — ver bindFilterEvents/renderBrowser.
  origin: "",
  pageData: null,
  studySessionId: null,
  // Raio-X da sessão (TASK 19.2): respostas desta página/sessão de estudo,
  // sem inventar histórico. Por questão: última correção + se já acertou
  // antes nesta sessão ("sem acerto prévio" = nunca acertou aqui).
  raiox: new Map(),
};

wireLogoutButtons();
main();

async function main() {
  const user = await requireSessionOrGuard(showGuard);
  if (!user) {
    return;
  }
  state.user = user;
  readUrlIntoFilters();
  const selector = mountProcessSelector(document.getElementById("study-process-mount"), {
    onChange: (next) => onInstitutionChange(next),
  });
  selector.refresh();
  preloadManifest().catch(() => null);
  subtitle.textContent = `Olá, ${user.displayName || "estudante"} — escolha disciplina e assunto, pratique com correção imediata e veja seu progresso no recorte.`;
  showLogoutButtons();
  bindFilterEvents();
  bindAjustarFiltros();
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
  renderAuthGuard(guard, {
    title: "Entre para estudar",
    description: "A área de estudos mostra conteúdos, questões e seu progresso. Ela precisa da sua sessão — entre ou crie uma conta para continuar.",
    next: "estudos.html",
  });
}

/* ---------- URL ---------- */

function readUrlIntoFilters() {
  const q = new URLSearchParams(window.location.search);
  const instRaw = (q.get("institution") || "").trim().toUpperCase();
  if (instRaw === "EAJ" || instRaw === "IFRN") {
    state.institution = instRaw;
    setInstitution(instRaw);
  } else state.institution = currentInstitution();
  state.filters.disciplineCode = (q.get("disciplina") || "").trim().toUpperCase();
  state.filters.topicId = (q.get("topico") || "").trim();
  state.filters.subtopicId = (q.get("subtopico") || "").trim();
  state.filters.year = (q.get("ano") || "").trim();
  state.filters.difficulty = (q.get("dificuldade") || "").trim().toUpperCase();
  state.filters.sourceType = (q.get("fonte") || "").trim().toUpperCase();
  const p = Number.parseInt(q.get("pagina") || "0", 10);
  state.filters.page = Number.isFinite(p) && p >= 0 ? p : 0;
  state.origin = (q.get("origem") || "").trim().toLowerCase();
}

function syncUrl() {
  const q = new URLSearchParams();
  const f = state.filters;
  if (f.disciplineCode) q.set("disciplina", f.disciplineCode);
  if (f.topicId) q.set("topico", f.topicId);
  if (f.subtopicId) q.set("subtopico", f.subtopicId);
  if (f.year) q.set("ano", f.year);
  if (f.difficulty) q.set("dificuldade", f.difficulty);
  if (f.sourceType) q.set("fonte", f.sourceType);
  if (state.institution && state.institution !== "IFRN") q.set("institution", state.institution);
  if (f.page > 0) q.set("pagina", String(f.page));
  // A origem ("painel") não volta para a URL: ela descreve como o aluno
  // chegou, não o recorte atual. Manter na URL faria o hero dizer
  // "escolhido no painel" mesmo depois de o aluno filtrar por conta própria.
  const qs = q.toString();
  window.history.replaceState(null, "", qs ? `./estudos.html?${qs}` : "./estudos.html");
}

/* ---------- troca de trilha (TASK F.1) ---------- */

/* Edição composta "INSTITUTION-YEAR" do select (ex. "EAJ-2022"):
 * separa processo e ano; valor legado só-ano assume a trilha atual. */
export function splitEditionValue(raw, fallbackInstitution) {
  const v = String(raw || "").trim().toUpperCase();
  const m = v.match(/^(IFRN|EAJ)-(\d{4})$/);
  if (m) return { institution: m[1], year: m[2] };
  if (/^\d{4}$/.test(v)) return { institution: fallbackInstitution, year: v };
  return { institution: fallbackInstitution, year: "" };
}

async function onInstitutionChange(next) {
  if (next === state.institution) return;
  state.institution = next;
  // Trilha nova, recorte novo: limpa disciplina/assunto/ano (podem não
  // existir na outra trilha) e recarrega tudo nela.
  state.filters = { disciplineCode: "", topicId: "", subtopicId: "", year: "", difficulty: state.filters.difficulty, sourceType: state.filters.sourceType, page: 0 };
  state.origin = "";
  state.browserLimits = {};
  await loadAll();
  await refreshReviewForInstitution({ disciplines: state.disciplines, institution: state.institution });
}

/* ---------- carga inicial ---------- */

async function loadAll() {
  loadingBox.hidden = false;
  content.hidden = true;
  errorBox.textContent = "";
  try {
    const settled = await Promise.allSettled([
      fetchDisciplines(state.institution),
      fetchTopics(undefined, state.institution),
      fetchOverview(),
      fetchDiagnosis(state.institution),
      fetchPlan(state.institution),
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
    // Sem recorte na URL, abrir direto num conteúdo útil em vez do
    // estado-guia vazio (U2): prefere o item DOING/TODO do roteiro, senão
    // a primeira disciplina do catálogo. Só UI — sem inventar questão.
    if (!hasRecorte()) {
      applyDefaultRecorte();
      applyFiltersToForm();
      await refreshDependentSelects();
      syncUrl();
    }
    renderBrowser();
    renderProgress();
    renderPlan();
    renderRecorte();
    // Com recorte (URL ou padrão acima) o caderno abre sozinho; sem
    // catálogo/disciplinas, cai no estado-guia — ver hasRecorte().
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderHero();

    loadingBox.hidden = true;
    content.hidden = false;
    // Revisão guiada (TASK 17.2): seção própria com dono próprio. Só inicia
    // após auth + conteúdo (a guarda acima já barrou sem-sessão; 401 aqui
    // vira erro com retry dentro da seção, nunca guarda duplicada).
    initReviewSection({ disciplines: state.disciplines, institution: state.institution }).catch((err) => {
      console.error("[estudos] falha na seção de revisão:", err);
    });
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
  // Origem (TASK 15.4): só os dois valores do filtro; "fonte" inválida na
  // URL volta para Todas, nunca para um enum cru (ver check_frontend.py).
  if (!["OFFICIAL", "AUTHORAL"].includes(state.filters.sourceType)) {
    state.filters.sourceType = "";
  }
  selOrigem.value = state.filters.sourceType || "";
  if (state.filters.year && !/^(IFRN|EAJ)-\d{4}$/.test(state.filters.year) && !/^\d{4}$/.test(state.filters.year)) {
    state.filters.year = "";
    selYear.value = "";
  }
  // Valor legado só-ano na URL vira composto da trilha atual.
  if (/^\d{4}$/.test(state.filters.year || "")) {
    state.filters.year = `${state.institution}-${state.filters.year}`;
    selYear.value = state.filters.year;
  }
}

function bindFilterEvents() {
  // U2: trocar disciplina/assunto/subassunto lista sozinho (1 passo).
  // Ano/dificuldade também listam sozinhos; o botão Filtrar segue como
  // ação explícita + rolagem até o caderno (teclado/AT).
  selDisc.addEventListener("change", async () => {
    state.filters.disciplineCode = selDisc.value || "";
    state.filters.topicId = "";
    state.filters.subtopicId = "";
    state.filters.page = 0;
    state.origin = "";
    await refreshDependentSelects();
    syncUrl();
    renderBrowser();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
  });
  selTopic.addEventListener("change", async () => {
    // U3 — captura ANTES de refreshDependentSelects(): ele reconstrói as
    // opções do assunto e leria a seleção nova como "" se deixássemos para
    // depois (mesmo padrão do subassunto abaixo).
    state.filters.topicId = selTopic.disabled ? "" : (selTopic.value || "");
    state.filters.subtopicId = "";
    state.filters.page = 0;
    state.origin = "";
    await refreshDependentSelects();
    syncUrl();
    renderBrowser();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
  });
  selSub.addEventListener("change", async () => {
    // U3 — mesma captura antecipada do assunto: o refresh reconstrói este
    // select a partir do assunto e apagaria a escolha nova.
    state.filters.subtopicId = selSub.disabled ? "" : (selSub.value || "");
    state.filters.page = 0;
    state.origin = "";
    syncUrl();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
  });
  selYear.addEventListener("change", async () => {
    state.filters.page = 0;
    state.origin = "";
    state.filters.year = selYear.value || "";
    syncUrl();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
  });
  selDiff.addEventListener("change", async () => {
    state.filters.page = 0;
    state.origin = "";
    state.filters.difficulty = selDiff.value || "";
    syncUrl();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
  });
  // Origem (TASK 15.4): mesmo padrão de ano/dificuldade — listar sozinha,
  // com o botão Filtrar como ação explícita + rolagem (teclado/AT).
  selOrigem.addEventListener("change", async () => {
    state.filters.page = 0;
    state.origin = "";
    state.filters.sourceType = selOrigem.value || "";
    syncUrl();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
  });
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    state.filters.disciplineCode = selDisc.value || "";
    state.filters.topicId = selTopic.disabled ? "" : (selTopic.value || "");
    state.filters.subtopicId = selSub.disabled ? "" : (selSub.value || "");
    state.filters.year = selYear.value || "";
    state.filters.difficulty = selDiff.value || "";
    state.filters.sourceType = selOrigem.value || "";
    state.filters.page = 0;
    state.origin = "";
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
      renderRecorte();
      renderHero();
      // O caderno fica no topo (antes dos filtros): após filtrar, sobe a
      // tela até as questões para o aluno vê-las sem precisar rolar.
      document.getElementById("sec-questoes-t").scrollIntoView({ block: "start" });
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
    selOrigem.value = "";
    state.filters = { disciplineCode: "", topicId: "", subtopicId: "", year: "", difficulty: "", sourceType: "", page: 0 };
    state.origin = "";
    state.browserLimits = {};
    // Limpar volta ao ponto de partida útil (mesmo padrão da carga
    // inicial), nunca ao estado-guia vazio — ver applyDefaultRecorte().
    applyDefaultRecorte();
    applyFiltersToForm();
    syncUrl();
    await refreshDependentSelects();
    renderBrowser();
    if (hasRecorte()) {
      await loadQuestions();
    } else {
      renderQuestionsIdle();
    }
    renderProgress();
    renderPlan();
    renderRecorte();
    renderHero();
    document.getElementById("sec-questoes-t").scrollIntoView({ block: "start" });
  });
}

// U4 — "Ajustar filtros" com foco gerenciado: a âncora nativa só rola, sem
// mover o foco (o h2 tem tabindex="-1" desde U3 mas ninguém focava). Com
// teclado/AT o aluno ficava no caderno sem saber onde caiu. Agora rola +
// foca o título dos filtros (mesmo padrão da paginação → #sec-questoes-t).
// Só UI: sem novo fetch, sem mudar filtros, sem inventar conteúdo.
function bindAjustarFiltros() {
  if (!ajustarLink || !filtrosTitle) return;
  ajustarLink.addEventListener("click", (e) => {
    // Deixa modificadores (nova aba) e botão não-primário com o navegador.
    if (e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    e.preventDefault();
    filtrosTitle.scrollIntoView({ block: "start" });
    filtrosTitle.focus({ preventScroll: true });
  });
}

async function refreshDependentSelects() {
  const disc = selDisc.value || state.filters.disciplineCode || "";
  state.filters.disciplineCode = disc;

  // Assuntos: filtra por disciplina quando há, senão usa catálogo geral.
  let topics = state.allTopics;
  if (disc) {
    try {
      const data = await fetchTopics(disc, state.institution);
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
    const data = await fetchSubtopics(topicId, state.institution);
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

/* ---------- recorte padrão (U2: abrir estudando) ---------- */

function applyDefaultRecorte() {
  if (hasRecorte()) return;
  if (state.disciplines.length === 0) return;
  // 1) Tenta o item DOING/TODO do roteiro (assunto + disciplina reais).
  const doing = planDoingItem();
  if (doing?.topicId) {
    const topic = state.allTopics.find((t) => String(t.id) === String(doing.topicId));
    if (topic?.disciplineCode) {
      state.filters.disciplineCode = topic.disciplineCode;
      state.filters.topicId = String(topic.id);
      state.filters.subtopicId = "";
      state.filters.page = 0;
      return;
    }
    // Roteiro aponta para assunto fora do catálogo: cai para a disciplina.
    const discCode = state.disciplines[0]?.code || "";
    if (discCode) {
      state.filters.disciplineCode = discCode;
      state.filters.page = 0;
    }
    return;
  }
  // 2) Sem roteiro: primeira disciplina do catálogo (ordem da API).
  const first = state.disciplines[0]?.code || "";
  if (first) {
    state.filters.disciplineCode = first;
    state.filters.page = 0;
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

function currentTopic() {
  const id = state.filters.topicId;
  if (!id) return null;
  return state.allTopics.find((t) => String(t.id) === String(id))
    || state.topicsOfDisc.find((t) => String(t.id) === String(id))
    || null;
}

function currentTopicName() {
  return currentTopic()?.name || null;
}

function currentDisciplineName() {
  const code = state.filters.disciplineCode;
  if (code) {
    return state.disciplines.find((d) => d.code === code)?.name || null;
  }
  // O painel manda disciplina + assunto, mas links antigos/manuais podem
  // trazer só ?topico=. Deriva a disciplina do catálogo para o hero e os
  // chips não caírem no item genérico do roteiro.
  const topic = currentTopic();
  const discCode = topic?.disciplineCode || "";
  if (!discCode) return null;
  return state.disciplines.find((d) => d.code === discCode)?.name
    || state.allTopics.find((t) => t.disciplineCode === discCode)?.disciplineName
    || null;
}

function currentSubtopicName() {
  const id = state.filters.subtopicId;
  if (!id) return null;
  return state.subtopicsOfTopic.find((s) => String(s.id) === String(id))?.name || null;
}

// U3 — resumo do recorte no topo do caderno ("onde estou e o que vou
// ver"). Só UI: deriva dos filtros + nomes do catálogo, sem novo fetch e
// sem inventar conteúdo. Edição e dificuldade usam rótulos traduzidos;
// código cru da API nunca vai para o texto (ver check_frontend.py).
function renderRecorte() {
  if (!recorteResumo) return;
  const parts = [];
  const discName = currentDisciplineName();
  if (discName) parts.push(discName);
  const topicName = currentTopicName();
  if (topicName) parts.push(topicName);
  const subName = currentSubtopicName();
  if (subName) parts.push(subName);
  if (state.filters.year) {
    const ed = splitEditionValue(state.filters.year, state.institution);
    parts.push(ed.year ? `Edição ${ed.year} · ${ed.institution}` : `Edição ${state.filters.year}`);
  }
  const diffLabel = difficultyLabel(state.filters.difficulty);
  if (diffLabel) parts.push(diffLabel);
  // Origem no resumo (TASK 15.4): traduzida via vocab, nunca o enum cru.
  if (state.filters.sourceType) parts.push(sourceTypeLabel(state.filters.sourceType));
  recorteResumo.textContent = parts.length > 0
    ? `Recorte atual: ${parts.join(" · ")}.`
    : "Recorte atual: todos os conteúdos.";
}

function planDoingItem() {
  const items = planItems();
  if (items.length === 0) return null;
  return items.find((it) => it.status === "DOING") || items.find((it) => it.status === "TODO") || items[0] || null;
}

function planTopicName(topicId) {
  return state.allTopics.find((t) => String(t.id) === String(topicId))?.name
    || state.topicsOfDisc.find((t) => String(t.id) === String(topicId))?.name
    || "Assunto do seu roteiro";
}

function planItems() {
  const items = state.plan?.items ?? state.plan?.planItems ?? state.plan?.recommendations ?? [];
  return Array.isArray(items) ? items : [];
}

/* TASK 18.3: o mesmo motivo do painel (`evidenceLine` compartilhado, a
 * partir do `evidenceJson` rico da TASK 18.2). Sem evidência aproveitável,
 * devolve "" e o chamador mantém o texto que já exibia (nunca vazio
 * inventado, nunca código cru do `reason` da API). */
function planMotive(item) {
  return evidenceLine(parseEvidence(item)) || "";
}

function disciplineOfTopic(topicId) {
  return state.allTopics.find((t) => String(t.id) === String(topicId))?.disciplineCode
    || state.topicsOfDisc.find((t) => String(t.id) === String(topicId))?.disciplineCode
    || "";
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
  const hasTopic = Boolean(state.filters.topicId);
  const hasDisc = Boolean(state.filters.disciplineCode);
  const fromPanel = state.origin === "painel" && !idle;

  // Precedência: o filtro da URL sempre vence o roteiro no hero. Antes o
  // assunto pedido só aparecia com disciplina + assunto juntos — como o
  // painel mandava só ?topico=, o hero caía no item do roteiro (outro
  // assunto) enquanto o caderno listava o assunto pedido.
  let eyebrow = "Continue de onde parou";
  let title = "Pratique no seu ritmo";
  let why = "Escolha uma disciplina e um assunto para ver as questões oficiais daquele recorte, com correção na hora.";
  if (hasTopic) {
    eyebrow = fromPanel ? "Praticando agora — escolhido no painel" : "Praticando agora";
    if (topicName) {
      title = topicName;
      // TASK 18.3: mesmo motivo do painel para o mesmo assunto (evidência
      // do roteiro); sem item de roteiro, mantém o retrato ao vivo.
      const match = planItems().find((it) => String(it.topicId) === String(state.filters.topicId));
      const motive = match ? planMotive(match) : "";
      if (motive) {
        why = `${motive} Continue praticando as questões abaixo.`;
      } else {
        const ta = topicAccuracy(state.filters.topicId);
        why = ta && ta.scored > 0
          ? `Você acertou ${ta.correct} de ${ta.scored} neste assunto (${formatPercent(ta.accuracy)}). Continue praticando as questões abaixo.`
          : `Você ainda não respondeu nada deste assunto. As questões abaixo são o melhor ponto de partida.`;
      }
    } else {
      title = "Assunto escolhido";
      why = "Mostrando abaixo as questões deste filtro, com correção na hora.";
    }
  } else if (hasDisc && discName) {
    eyebrow = fromPanel ? "Praticando agora — escolhido no painel" : "Praticando agora";
    title = discName;
    const da = disciplineAccuracy(state.filters.disciplineCode);
    why = da && da.scored > 0
      ? `Seu aproveitamento aqui é ${formatPercent(da.accuracy)} (${da.correct} acertos em ${da.scored}). Escolha um assunto para refinar.`
      : "Escolha um assunto desta disciplina para começar a praticar com correção imediata.";
  } else if (!idle) {
    eyebrow = fromPanel ? "Praticando agora — escolhido no painel" : "Praticando agora";
    title = "Questões do filtro atual";
    why = "Mostrando abaixo as questões deste filtro, com correção na hora. Refine por disciplina e assunto para focar.";
  } else if (doing) {
    const name = planTopicName(doing.topicId);
    title = name;
    // TASK 18.3: o motivo do item do roteiro acompanha o hero (mesma frase
    // do painel); sem evidência, mantém o texto genérico que já existia.
    const motive = planMotive(doing);
    if (idle) {
      why = motive
        ? `Este é o próximo item do seu roteiro. ${motive}`
        : "Este é o próximo item do seu roteiro. Toque em “Abrir assunto do roteiro” para ver as questões dele.";
    } else if (acc !== null && acc !== undefined) {
      why = motive
        ? `Este é o próximo item do seu roteiro. ${motive}`
        : `Este é o próximo item do seu roteiro e seu aproveitamento geral é ${formatPercent(acc)}. Pratique as questões deste assunto abaixo.`;
    } else {
      why = motive
        ? `Este é o próximo item do seu roteiro. ${motive}`
        : "Este é o próximo item do seu roteiro. Pratique as questões deste assunto abaixo.";
    }
  } else if (state.planMissing || !state.plan) {
    title = "Monte seu roteiro para estudar com ordem";
    why = "O roteiro coloca os assuntos na ordem que vale mais a pena para você. Gere no painel e volte aqui para praticar.";
  }
  card.appendChild(el("p", { className: "next-step__eyebrow", text: eyebrow }));
  card.appendChild(el("h3", { className: "next-step__title", text: title }));
  card.appendChild(el("p", { className: "next-step__why", text: why }));

  const chips = el("div", { className: "next-step__meta" });
  if (discName) chips.appendChild(el("span", { className: "chip", text: discName }));
  if (topicName) chips.appendChild(el("span", { className: "chip", text: topicName }));
  if (total !== null) chips.appendChild(el("span", { className: "chip", text: total === 1 ? "1 questão no recorte" : `${total} questões no recorte` }));
  if (fromPanel) chips.appendChild(el("span", { className: "chip", text: "escolhido no painel" }));
  if (!hasTopic && !hasDisc && doing?.priority !== undefined && doing?.priority !== null) {
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
  if (!hasTopic && doing?.topicId) {
    const doingTopic = state.allTopics.find((t) => String(t.id) === String(doing.topicId))
      || state.topicsOfDisc.find((t) => String(t.id) === String(doing.topicId));
    const hrefQ = new URLSearchParams();
    if (doingTopic?.disciplineCode) hrefQ.set("disciplina", doingTopic.disciplineCode);
    hrefQ.set("topico", String(doing.topicId));
    const open = el("a", {
      className: "btn btn--secondary",
      text: "Abrir assunto do roteiro",
      attrs: { href: `./estudos.html?${hrefQ.toString()}` },
    });
    actions.appendChild(open);
  } else {
    const dash = el("a", {
      className: "btn btn--secondary",
      text: fromPanel ? "Voltar ao painel" : "Ver meu painel",
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
      const DEFAULT_LIMIT = 12;
      const limit = state.browserLimits[d.code] ?? DEFAULT_LIMIT;
      const ul = el("ul", { className: "browser-topics" });
      for (const t of topics.slice(0, limit)) {
        const li = el("li", { className: "browser-topics__row" });
        // U3 — assunto ativo: o filtro atual ganha "onde estou" sem novo
        // fetch. Só UI (classe + rótulo traduzido); o botão vira "Atual"
        // desabilitado para não re-listar o mesmo recorte.
        const isActive = String(t.id) === String(state.filters.topicId);
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
          text: isActive ? "Atual" : "Estudar",
          attrs: {
            type: "button",
            "aria-label": isActive ? `Estudando ${t.name} (filtro atual)` : `Estudar ${t.name}`,
            ...(isActive ? { "aria-current": "true", disabled: "" } : {}),
          },
        });
        if (isActive) li.classList.add("browser-topics__row--active");
        btn.addEventListener("click", async () => {
          selDisc.value = d.code;
          state.filters.disciplineCode = d.code;
          await refreshDependentSelects();
          selTopic.value = String(t.id);
          state.filters.topicId = String(t.id);
          state.filters.subtopicId = "";
          state.filters.page = 0;
          state.origin = "";
          syncUrl();
          await refreshDependentSelects();
          renderBrowser();
          await loadQuestions();
          renderProgress();
          renderPlan();
          renderRecorte();
          renderHero();
          document.getElementById("sec-questoes-t").scrollIntoView({ block: "start" });
        });
        li.appendChild(btn);
        ul.appendChild(li);
      }
      card.appendChild(ul);
      if (topics.length > limit) {
        const remaining = topics.length - limit;
        const step = Math.min(12, remaining);
        const moreWrap = el("div", { className: "btn-group mt-4" });
        moreWrap.appendChild(
          el("p", {
            className: "muted",
            text: `Mostrando ${limit} de ${topics.length} assuntos.`,
          }),
        );
        const moreBtn = el("button", {
          className: "btn btn--secondary btn--sm",
          text: remaining === 1 ? "Mostrar 1 assunto restante" : `Mostrar mais ${step} assuntos`,
          attrs: { type: "button", "aria-expanded": "false", "aria-label": `Mostrar mais assuntos de ${d.name}` },
        });
        moreBtn.addEventListener("click", () => {
          state.browserLimits[d.code] = limit + 12;
          renderBrowser();
        });
        moreWrap.appendChild(moreBtn);
        card.appendChild(moreWrap);
      } else if (limit > DEFAULT_LIMIT && topics.length > DEFAULT_LIMIT) {
        const lessWrap = el("div", { className: "btn-group mt-4" });
        lessWrap.appendChild(
          el("p", {
            className: "muted",
            text: `Mostrando todos os ${topics.length} assuntos.`,
          }),
        );
        const lessBtn = el("button", {
          className: "btn btn--ghost btn--sm",
          text: "Mostrar menos",
          attrs: { type: "button", "aria-expanded": "true", "aria-label": `Recolher assuntos de ${d.name}` },
        });
        lessBtn.addEventListener("click", () => {
          state.browserLimits[d.code] = DEFAULT_LIMIT;
          renderBrowser();
        });
        lessWrap.appendChild(lessBtn);
        card.appendChild(lessWrap);
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

// Há recorte quando algum filtro de conteúdo está ativo (disciplina,
// assunto, subassunto, edição, dificuldade ou origem). A carga inicial
// aplica um recorte padrão (roteiro ou 1ª disciplina) para o caderno abrir
// sozinho; sem catálogo, cai no estado-guia — ver applyDefaultRecorte().
function hasRecorte() {
  const f = state.filters;
  return Boolean(f.disciplineCode || f.topicId || f.subtopicId || f.year || f.difficulty || f.sourceType);
}

function renderQuestionsIdle() {
  state.pageData = null;
  listBox.textContent = "";
  pagerBox.textContent = "";
  if (pagerTop) pagerTop.textContent = "";
  renderEmpty(listBox, {
    title: "Escolha um conteúdo para começar",
    description: "Toque em “Estudar” num assunto abaixo ou use os filtros para escolher disciplina e assunto. As questões daquele conteúdo aparecem aqui no topo.",
  });
  countNote.textContent = "Nenhum conteúdo selecionado — escolha abaixo para ver as questões.";
}

function questionQuery() {
  const f = state.filters;
  const ed = splitEditionValue(f.year, state.institution);
  return {
    ...(f.disciplineCode ? { disciplineCode: f.disciplineCode } : {}),
    ...(f.topicId ? { topicId: f.topicId } : {}),
    ...(f.subtopicId ? { subtopicId: f.subtopicId } : {}),
    ...(ed.year ? { year: ed.year } : {}),
    // Trilha sempre explícita: ano sozinho nunca decide a edição (E.1);
    // a edição composta do filtro vence a trilha global quando divergem.
    institution: ed.year ? ed.institution : state.institution,
    ...(f.difficulty ? { difficulty: f.difficulty } : {}),
    ...(f.sourceType ? { sourceType: f.sourceType } : {}),
    page: f.page,
    size: PAGE_SIZE,
  };
}

async function loadQuestions() {
  state.raiox.clear();
  document.getElementById("study-raiox")?.replaceChildren();
  listBox.textContent = "";
  pagerBox.textContent = "";
  if (pagerTop) pagerTop.textContent = "";
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
      box.appendChild(el("p", { className: "envelope mt-2", text: `Se precisar de ajuda, anote este código: ${err.traceId}` }));
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
  if (pagerTop) pagerTop.textContent = "";
  const items = data?.content ?? [];
  const total = data?.totalElements ?? 0;
  if (total === 0) {
    renderEmpty(listBox, {
      title: "Nenhuma questão neste recorte",
      description: "Tente outra disciplina, assunto ou edição. Se o filtro parece certo, limpe e escolha de novo.",
      actionLabel: "Limpar filtros",
      onAction: () => btnClear.click(),
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
  if (q.hasFigure || (Array.isArray(q.figures) && q.figures.length > 0)) {
    // Figura expansível: o recorte publicado aparece ao abrir "Mostrar figura".
    card.appendChild(mountExpandableFigure(q, "question-card__figure"));
  }
  const topicLine = q.topic?.name
    ? `Assunto: ${q.topic.name}${q.subtopic?.name ? ` · ${q.subtopic.name}` : ""}`
    : "Assunto ainda sem classificação.";
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
      recordRaioxAnswer(q, attempt);
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
    box.appendChild(el("p", { text: `Você marcou ${choiceText}. O gabarito oficial marcou esta questão como anulada. O IFRN não informa como pontuar questões anuladas.` }));
  } else if (attempt?.isCorrect === true) {
    box.dataset.tone = "success";
    box.appendChild(el("strong", { text: `Você acertou — alternativa ${correct}.` }));
    box.appendChild(el("p", { text: `Sua resposta: ${choiceText}.` }));
  } else {
    box.dataset.tone = "danger";
    box.appendChild(el("strong", { text: `Não foi dessa vez — resposta correta: ${correct}.` }));
    box.appendChild(el("p", { text: `Você marcou ${choiceText}.` }));
  }
  const topicLine = question.topic?.name
    ? `Conteúdo: ${question.topic.name}${question.subtopic?.name ? ` · ${question.subtopic.name}` : ""}.`
    : "Conteúdo: assunto ainda sem classificação.";
  box.appendChild(el("p", { text: topicLine }));
  // attempt.notes é trilha de auditoria do servidor — ver comentário acima.
}

/* ---------- raio-X da sessão (TASK 19.2) ----------
 * Fim do caderno: "nesta sessão você errou N (M sem acerto prévio nesta
 * sessão)" + mesmos CTAs do 19.1 via `renderResultNext` (máx. 3). O assunto
 * vem da questão listada (`q.topic`/`q.discipline`); sem classificação, o
 * componente agrupa por disciplina — nunca inventa assunto. A revisão
 * congela o topo da fila no assunto que mais pesou (filtro por tópico, como
 * no simulado): NÃO estende `POST /review/sessions` com `{questionIds[]}`
 * (opcional da task, dispensado — o recorte por tópico já cobre os erros da
 * sessão sem mexer no backend). */
function recordRaioxAnswer(q, attempt) {
  if (!q || attempt?.wasAnnulled || q.annulled) return;
  const id = q.id;
  const prev = state.raiox.get(id) || { everCorrect: false, detail: null };
  const correct = attempt?.isCorrect === true;
  state.raiox.set(id, {
    everCorrect: prev.everCorrect || correct,
    lastCorrect: correct,
    detail: {
      topic: q.topic ? { id: q.topic.id ?? null, code: q.topic.code ?? null, name: q.topic.name ?? null } : null,
      discipline: q.discipline ? { code: q.discipline.code ?? null } : null,
    },
    sourceYear: q.examYear ?? null,
    sourceQuestionNumber: q.questionNumber ?? null,
    disciplineCode: q.discipline?.code ?? null,
  });
  renderRaiox();
}

function renderRaiox() {
  const mount = document.getElementById("study-raiox");
  if (!mount) return;
  mount.textContent = "";
  if (state.raiox.size === 0) return;
  const items = [];
  const detailsMap = new Map();
  let pos = 0;
  let freshErrors = 0;
  for (const [qid, rec] of state.raiox) {
    pos += 1;
    detailsMap.set(qid, rec.detail);
    detailsMap.set(String(qid), rec.detail);
    const isErr = rec.lastCorrect === false;
    if (isErr && !rec.everCorrect) freshErrors += 1;
    items.push({
      position: pos,
      questionId: qid,
      disciplineCode: rec.disciplineCode,
      sourceYear: rec.sourceYear,
      sourceQuestionNumber: rec.sourceQuestionNumber,
      isCorrect: rec.lastCorrect,
      wasAnnulled: false,
      unanswered: false,
    });
  }
  let summary = null;
  try {
    summary = summarizeResult({ items, scored: items.length, correct: items.filter((i) => i.isCorrect).length }, detailsMap);
  } catch {
    return;
  }
  if (!summary) return;
  const lead = el("p", {
    className: "muted",
    text: `Nesta sessão você errou ${summary.errorCount} (${freshErrors} sem acerto prévio nesta sessão).`,
  });
  mount.appendChild(lead);
  const block = el("div");
  mount.appendChild(block);
  try {
    renderResultNext(block, summary, { onReview: startReviewFromRaiox, onPlan: refreshPlanFromRaiox });
  } catch {
    block.remove();
  }
}

async function startReviewFromRaiox(summary, button) {
  const n = summary?.errorCount ?? 0;
  if (!n) {
    toast("Nenhum erro nesta sessão para revisar.", "info");
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

async function refreshPlanFromRaiox(_summary, button) {
  setButtonLoading(button, true, "Atualizando plano…");
  try {
    await generatePlan();
    toast("Plano atualizado com esta sessão. Abrindo seu painel.", "success");
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

// U3 — paginação espelhada topo/base: o caderno tem 10 cartões longos e
// a troca de página morava só no rodapé. O topo é compacto (mesma ação,
// sem novo conceito); o anúncio de página continua só no rodapé
// (role=status no countNote + info da base) para não duplicar no AT.
function fillPager(container, data, { announce = false } = {}) {
  if (!container) return;
  container.textContent = "";
  const page = data?.page ?? 0;
  const totalPages = data?.totalPages ?? 1;
  const info = el("span", {
    className: "study-pagination__info",
    text: `Página ${page + 1} de ${totalPages}`,
    ...(announce ? { attrs: { role: "status" } } : {}),
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
  container.appendChild(info);
  container.appendChild(group);
}

function renderPager(data) {
  fillPager(pagerBox, data, { announce: true });
  fillPager(pagerTop, data);
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
    planBox.appendChild(el("p", {
      className: "muted",
      text: "Este recorte não está no seu roteiro atual. Você pode praticar mesmo assim — o roteiro só ordena o que vale mais a pena para você.",
    }));
    const actions = el("div", { className: "btn-group mt-4" });
    actions.appendChild(el("a", {
      className: "btn btn--secondary btn--sm",
      text: "Ver roteiro completo",
      attrs: { href: "./dashboard.html" },
    }));
    // TASK 18.3: mesmo fora do roteiro, o assunto atual tem fila de
    // revisão própria (TASK 17.2) — o filtro vai por assunto, não por plano.
    if (f.topicId) {
      actions.appendChild(el("a", {
        className: "btn btn--ghost btn--sm",
        text: "Revisar erros deste assunto",
        attrs: {
          href: revisaoHref({
            disciplineCode: state.filters.disciplineCode || disciplineOfTopic(f.topicId),
            topicId: f.topicId,
          }),
        },
      }));
    }
    const doing = items.find((it) => it.status === "DOING") || items.find((it) => it.status === "TODO");
    if (doing?.topicId) {
      const doingTopic = state.allTopics.find((t) => String(t.id) === String(doing.topicId));
      const hrefQ = new URLSearchParams();
      if (doingTopic?.disciplineCode) hrefQ.set("disciplina", doingTopic.disciplineCode);
      hrefQ.set("topico", String(doing.topicId));
      actions.appendChild(el("a", {
        className: "btn btn--ghost btn--sm",
        text: "Abrir assunto do roteiro",
        attrs: { href: `./estudos.html?${hrefQ.toString()}` },
      }));
    }
    planBox.appendChild(actions);
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
  // TASK 18.3: o mesmo motivo do painel para este item (evidência), mais o
  // atalho para a fila de revisão filtrada por este assunto.
  const motive = planMotive(match);
  if (motive) {
    left.appendChild(el("p", { className: "plan-items__meta", text: motive }));
  }
  row.appendChild(left);
  list.appendChild(row);
  planBox.appendChild(list);
  const actions = el("div", { className: "btn-group mt-4" });
  actions.appendChild(el("a", {
    className: "btn btn--secondary btn--sm",
    text: "Revisar erros deste assunto",
    attrs: {
      href: revisaoHref({
        disciplineCode: state.filters.disciplineCode || disciplineOfTopic(match.topicId),
        topicId: match.topicId ?? "",
      }),
    },
  }));
  const matchEv = parseEvidence(match);
  if (matchEv && matchEv.sampleQuestionIds.length > 0) {
    actions.appendChild(el("a", {
      className: "btn btn--ghost btn--sm",
      text: "Ver exemplo oficial",
      attrs: { href: sampleHref(matchEv.sampleQuestionIds[0]) },
    }));
  }
  actions.appendChild(el("a", {
    className: "btn btn--ghost btn--sm",
    text: "Ver roteiro completo",
    attrs: { href: "./dashboard.html" },
  }));
  planBox.appendChild(actions);
}

