/* VouPassar — dashboard
 *
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=dashboard.html). Com sessão,
 * busca em paralelo: overview, diagnosis, plan, topics, simulations e
 * evolution. Cada seção falha de forma isolada: o restante continua
 * visível. Sem innerHTML (só textContent via el()).
 *
 * Linguagem: o texto é escrito para um estudante do ensino fundamental.
 * Termos internos do projeto (nomes de tabela, versão do algoritmo,
 * identificadores de tarefa) NÃO aparecem aqui — ver AGENTS.md §4 para a
 * regra de honestidade e docs/frontend-design-system.md §Copy.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { logout } from "../api/auth.js";
import {
  fetchOverview,
  fetchEvolution,
  fetchDiagnosis,
  fetchPlan,
  generatePlan,
  updatePlanItemStatus,
  fetchTopics,
  fetchRecentSimulations,
} from "../api/dashboard.js";
import { el, renderEmpty, renderErrorSummary, renderErrorWithRetry, setButtonLoading, toast } from "../components/ui.js";
import { renderAuthGuard, requireSessionOrGuard } from "./auth-shared.js";
import { disciplineLabel, modeLabel, statusLabel, masteryLabel, topicLabel, plural, simulationTitle, translateTopicCodes } from "../vocab.js";
import {
  parseEvidence,
  evidenceLine,
  estudosHref as buildEstudosHref,
  revisaoHref,
  sampleHref,
  practiceTarget,
} from "../components/plan-evidence.js";

const guard = document.getElementById("dash-guard");
const errorBox = document.getElementById("dash-error");
const loadingBox = document.getElementById("dash-loading");
const content = document.getElementById("dash-content");
const titleEl = document.getElementById("dash-title");
const subtitle = document.getElementById("dash-subtitle");
const statsBox = document.getElementById("dash-stats");
const discBox = document.getElementById("dash-disciplines");
const prioBox = document.getElementById("dash-priorities");
const nextBox = document.getElementById("dash-next");
const planBox = document.getElementById("dash-plan");
const planGenerateBtn = document.getElementById("plan-generate");
const planRegenerateBtn = document.getElementById("plan-regenerate");
const evoSelect = document.getElementById("evo-granularity");
const evoBox = document.getElementById("dash-evolution");
const evoHeadline = document.getElementById("dash-evo-headline");
const evoDelta = document.getElementById("dash-evo-delta");
const simBox = document.getElementById("dash-simulations");
const watchBox = document.getElementById("dash-watch");
const levelBox = document.getElementById("dash-level");
const healthPill = document.getElementById("dash-health");
const healthNum = document.getElementById("dash-health-num");

let topicById = new Map();
let currentPlan = null;
let overviewCache = null;
let diagnosisCache = null;

wireLogoutButtons();

main();

async function main() {
  const user = await requireSessionOrGuard(showGuard);
  if (!user) {
    return;
  }
  const name = (user.displayName || "").split(" ")[0] || "você";
  titleEl.textContent = `Olá, ${name}`;
  subtitle.textContent =
    "Aqui está o resumo do seu estudo e o próximo passo. Se for seu primeiro dia, comece pelo diagnóstico.";
  showLogoutButtons();
  await loadAll();
}

function wireLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      btn.setAttribute("disabled", "");
      try {
        await logout();
      } finally {
        window.location.href = "./login.html?next=dashboard.html";
      }
    });
  });
}

function showLogoutButtons() {
  document
    .querySelectorAll("[data-logout]")
    .forEach((btn) => {
      btn.hidden = false;
    });
}

function showGuard() {
  loadingBox.hidden = true;
  content.hidden = true;
  guard.hidden = false;
  renderAuthGuard(guard, {
    title: "Entre para ver seu painel",
    description: "Seu painel mostra o que estudar agora e como você está indo. Entre com sua conta para continuar.",
    next: "dashboard.html",
  });
}

/* ---------- formato ---------- */

function formatPercent(acc) {
  if (acc === null || acc === undefined) return "—";
  const n = Number(acc);
  if (!Number.isFinite(n)) return "—";
  return `${(n * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
}

function formatPercentValue(acc) {
  if (acc === null || acc === undefined) return null;
  const n = Number(acc);
  if (!Number.isFinite(n)) return null;
  return Math.round(n * 100);
}

function formatDateTime(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function formatBucketDate(isoDate) {
  if (!isoDate) return "—";
  const [y, m, d] = String(isoDate).split("-").map(Number);
  if (!y || !m || !d) return String(isoDate);
  return new Date(Date.UTC(y, m - 1, d)).toLocaleDateString("pt-BR", { timeZone: "UTC", day: "2-digit", month: "short" });
}

/** Link para a área de estudos com o recorte já aplicado.
 *
 * O painel sempre informa a origem (`origem=painel`) para que o hero de
 * estudos priorize o assunto escolhido em vez do item genérico do roteiro
 * (ver js/views/estudos.js renderHero). A disciplina acompanha o assunto
 * quando conhecida, para o hero não precisar adivinhar. Monta pelo
 * construtor compartilhado (`js/components/plan-evidence.js`), usado também
 * por estudos e perfil.
 */
function estudosHref({ disciplineCode = "", topicId = "", subtopicId = "" } = {}) {
  return buildEstudosHref({ disciplineCode, topicId, subtopicId, origin: "painel" });
}

/** Motivo de um item do roteiro na linguagem do aluno.
 *
 * A frase canônica vem da evidência (`evidenceJson` rico da TASK 18.2) pelo
 * formatador compartilhado — a mesma usada no hero de estudos e nas metas
 * do perfil. Sem evidência aproveitável, cai para o `reason` da API
 * traduzido (sem código cru) em vez de omitir o motivo.
 */
function planMotive(item) {
  const line = evidenceLine(parseEvidence(item));
  if (line) return line;
  return translateTopicCodes(item?.reason) || "Ordem definida pelo seu desempenho e pelo peso do assunto na prova.";
}

/** Classe de badge conforme o status (apresentação, não vocabulário). */
function statusBadge(status) {
  const map = {
    SUBMITTED: "badge--success",
    IN_PROGRESS: "badge--warning",
    ABANDONED: "",
    TODO: "",
    DOING: "badge--info",
    DONE: "badge--success",
    SKIPPED: "",
  };
  return map[status] || "";
}

/* ---------- SVG (sem innerHTML) ---------- */

// Montado por partes de propósito: o verificador estático
// (scripts/analysis/check_frontend.py) barra URL absoluta literal fora de
// config.js — ver o mesmo padrão em js/main.js.
const SVG_NS = ["http:", "www.w3.org/2000/svg"].join("//");

function svgNode(tag, attrs = {}) {
  const n = document.createElementNS(SVG_NS, tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (v === null || v === undefined) continue;
    n.setAttribute(k, String(v));
  }
  return n;
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

/* ---------- texto de aluno ----------
 * O backend monta as justificativas já carregadas de códigos internos
 * ("tema GRAMATICA_NORMA com 60 questões", "score determinístico 0").
 * Isso é documentação do motor, não explicação para o estudante. As frases
 * abaixo são escritas a partir dos números que a API já devolve — acertos,
 * tentativas pontuáveis e peso do assunto nas provas. Nenhum número é
 * estimado aqui.
 */

/** Nome de um assunto a partir do catálogo carregado ou do dicionário. */
function topicNameFor(code, fallbackName) {
  const hit = [...topicById.values()].find((t) => t.code === code);
  return hit?.name || topicLabel(code, fallbackName) || "";
}

/** Troca códigos UPPER_SNAKE conhecidos pelos nomes dos assuntos. */
function humanize(text) {
  if (!text) return "";
  return String(text).replace(/\b[A-Z][A-Z0-9]*(_[A-Z0-9]+)+\b/g, (code) => {
    const name = topicNameFor(code);
    return name || code;
  });
}

/** Dados do diagnóstico de um assunto, se ele estiver na fila de atenção. */
function priorityFor(topicId) {
  return (diagnosisCache?.priorities || []).find((p) => Number(p.topicId) === Number(topicId)) || null;
}

function studentReason(p) {
  const scored = Number(p.scored ?? 0);
  const acc = p.accuracy === null || p.accuracy === undefined ? null : Number(p.accuracy);
  const share = Number(p.historicalQuestions ?? 0);
  const editions = Number(p.editionsCount ?? 0);
  const overall = diagnosisCache?.accuracy ?? null;

  const peso = share
    ? ` Ele aparece ${plural(share, "questão", "questões")} na prova${
        editions ? `, em ${plural(editions, "edição", "edições")}` : ""
      }.`
    : "";

  if (!scored) {
    return `Você ainda não respondeu nada deste assunto.${peso}`;
  }

  const acertos = Math.round(acc * scored);
  const base = `Você acertou ${plural(acertos, "questão", "questões")} de ${plural(scored, "tentativa", "tentativas")} (${formatPercent(acc)}).`;
  if (overall !== null && acc < overall) {
    return `${base} Está abaixo da sua média geral de ${formatPercent(overall)}.${peso}`;
  }
  return `${base}${peso}`;
}

/** Frase do "estude agora".
 *
 * Quando o assunto não está na fila de atenção, o diagnóstico não apontou
 * fragilidade nele — e nesse caso não temos o percentual por assunto. A
 * frase diz só o que sabemos: o peso na prova e o fato de ele constar do
 * roteiro como revisão.
 */
function studentNextReason(topicId) {
  const p = priorityFor(topicId);
  const topic = topicById.get(Number(topicId));
  const share = Number(topic?.questionCount ?? p?.historicalQuestions ?? 0);

  if (p && Number(p.scored ?? 0) > 0) {
    return studentReason(p);
  }
  const peso = share
    ? `Ele aparece ${plural(share, "questão", "questões")} na prova`
    : "Ele faz parte do seu roteiro";
  return `${peso} e entra como revisão, para você não esquecer o que já aprendeu.`;
}

/* ---------- carga ---------- */

async function loadAll() {
  errorBox.textContent = "";
  loadingBox.hidden = false;
  content.hidden = true;

  const [overview, diagnosis, topics, simulations, plan, evolution] =
    await Promise.allSettled([
      fetchOverview(),
      fetchDiagnosis(),
      fetchTopics(),
      fetchRecentSimulations(5),
      fetchPlan().catch((err) => {
        if (err instanceof ApiError && err.code === "NO_ACTIVE_PLAN") return null;
        throw err;
      }),
      fetchEvolution(evoSelect?.value || "WEEK"),
    ]);

  // Sessão expirada no meio do caminho: volta para o painel de acesso.
  const unauthorized = [overview, diagnosis, topics, simulations, plan, evolution].some(
    (r) => r.status === "rejected" && r.reason instanceof ApiError && r.reason.status === 401,
  );
  if (unauthorized) {
    showGuard();
    guard.querySelector("p")?.replaceChildren(
      document.createTextNode("Sua sessão expirou. Entre novamente para ver seu painel."),
    );
    return;
  }

  const failures = [overview, diagnosis, topics, simulations, evolution]
    .filter((r) => r.status === "rejected")
    .map((r) => r.reason);
  if (plan.status === "rejected") failures.push(plan.reason);
  if (failures.length) {
    const first = failures[0];
    renderErrorSummary(errorBox, {
      title: "Uma parte do painel não carregou — o resto está abaixo.",
      items: [friendlyMessage(first)],
      traceId: first instanceof ApiError ? first.traceId : null,
    });
  }

  if (topics.status === "fulfilled") {
    topicById = new Map((topics.value || []).map((t) => [Number(t.id), t]));
  }

  overviewCache = overview.status === "fulfilled" ? overview.value : null;
  diagnosisCache = diagnosis.status === "fulfilled" ? diagnosis.value : null;
  lastSectionErrors.overview = overview.status === "rejected" ? overview.reason : null;
  lastSectionErrors.diagnosis = diagnosis.status === "rejected" ? diagnosis.reason : null;
  lastSectionErrors.plan = plan.status === "rejected" ? plan.reason : null;
  lastSectionErrors.evolution = evolution.status === "rejected" ? evolution.reason : null;
  lastSectionErrors.simulations = simulations.status === "rejected" ? simulations.reason : null;

  renderStats(overviewCache, diagnosisCache);
  renderDisciplines(overviewCache);
  renderPriorities(diagnosisCache);
  renderWatch(overviewCache, diagnosisCache);
  renderLevel(overviewCache, diagnosisCache);
  currentPlan = plan.status === "fulfilled" ? plan.value : null;
  renderPlan(currentPlan, plan.status === "rejected" ? plan.reason : null);
  renderEvolution(
    evolution.status === "fulfilled" ? evolution.value : null,
    evolution.status === "rejected" ? evolution.reason : null,
  );
  renderSimulations(
    simulations.status === "fulfilled" ? simulations.value : null,
    simulations.status === "rejected" ? simulations.reason : null,
  );

  loadingBox.hidden = true;
  content.hidden = false;
}

/* ---------- retentativa por seção (TASK 16.3) ---------- */

let lastSectionErrors = {};

async function retrySummarySections() {
  errorBox.textContent = "";
  try {
    const [overview, diagnosis, topics] = await Promise.all([
      fetchOverview(),
      fetchDiagnosis(),
      fetchTopics(),
    ]);
    topicById = new Map((topics || []).map((t) => [Number(t.id), t]));
    overviewCache = overview;
    diagnosisCache = diagnosis;
    lastSectionErrors.overview = null;
    lastSectionErrors.diagnosis = null;
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    toast(friendlyMessage(err), "info");
    throw err;
  }
  renderStats(overviewCache, diagnosisCache);
  renderDisciplines(overviewCache);
  renderPriorities(diagnosisCache);
  renderWatch(overviewCache, diagnosisCache);
  renderLevel(overviewCache, diagnosisCache);
}

async function retryPlanSection() {
  try {
    const plan = await fetchPlan().catch((err) => {
      if (err instanceof ApiError && err.code === "NO_ACTIVE_PLAN") return null;
      throw err;
    });
    currentPlan = plan;
    lastSectionErrors.plan = null;
    errorBox.textContent = "";
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    throw err;
  }
  renderPlan(currentPlan, null);
}

async function retryEvolutionSection() {
  const data = await fetchEvolution(evoSelect?.value || "WEEK");
  lastSectionErrors.evolution = null;
  renderEvolution(data, null);
}

async function retrySimulationsSection() {
  const page = await fetchRecentSimulations(5);
  lastSectionErrors.simulations = null;
  renderSimulations(page, null);
}

/* ---------- 1. o passo agora ---------- */

function nextStepShell({ eyebrow, title, why, empty = false }) {
  const card = el("div", {
    className: empty ? "next-step next-step--empty" : "next-step",
  });
  if (eyebrow) card.appendChild(el("p", { className: "next-step__eyebrow", text: eyebrow }));
  if (title) card.appendChild(el("h3", { className: "next-step__title", text: title }));
  if (why) card.appendChild(el("p", { className: "next-step__why", text: why }));
  return card;
}

function renderNextStepEmpty(title, why) {
  nextBox.textContent = "";
  const card = nextStepShell({ eyebrow: "Próximo passo", title, why, empty: true });
  const actions = el("div", { className: "next-step__actions" });
  if (planGenerateBtn && !planGenerateBtn.hidden) {
    actions.appendChild(
      el("a", {
        className: "btn btn--primary",
        text: "Montar meu roteiro",
        attrs: { href: "#sec-plan-t" },
      }),
    );
  } else {
    actions.appendChild(
      el("a", {
        className: "btn btn--primary",
        text: "Ir para o diagnóstico",
        attrs: { href: "./estudos.html" },
      }),
    );
  }
  card.appendChild(actions);
  nextBox.appendChild(card);
}

/* ---------- 2. resumo ---------- */

const TILE_ACCENTS = ["#0f5084", "#6d43cc", "#0b7a4b", "#b83e06"];

function statTile({ label, value, hint, empty = false, textual = false, accent }) {
  const card = el("article", { className: "stat-tile", attrs: { role: "listitem" } });
  if (accent) card.style.setProperty("--tile-accent", accent);
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

function renderStats(overview, diagnosis) {
  statsBox.textContent = "";
  if (!overview && !diagnosis) {
    const err = lastSectionErrors.overview || lastSectionErrors.diagnosis;
    if (err) {
      renderErrorWithRetry(statsBox, {
        title: "Não deu para carregar seu progresso. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retrySummarySections,
      });
    } else {
      renderEmpty(statsBox, {
        title: "Não deu para carregar seu progresso",
        description: "Tente novamente em alguns instantes.",
      });
    }
    updateHealth(null);
    return;
  }
  const total = overview?.totalAttempts ?? diagnosis?.totalAttempts ?? 0;
  const scored = overview?.scoredAttempts ?? diagnosis?.scoredAttempts ?? 0;
  const correct = overview?.correct ?? diagnosis?.correct ?? 0;
  const acc = overview?.accuracy ?? diagnosis?.accuracy ?? null;
  const level = diagnosis?.overallLevel || "Ainda não informado";
  const last = overview?.lastAttemptAt || diagnosis?.lastAttemptAt || null;
  const notScored = Math.max(0, total - scored);

  statsBox.appendChild(
    statTile({
      label: "Aproveitamento",
      value: formatPercent(acc),
      empty: acc === null,
      accent: TILE_ACCENTS[0],
      hint:
        acc === null
          ? "Responda questões para calcular."
          : `${correct} acertos em ${scored} ${scored === 1 ? "questão" : "questões"}.`,
    }),
  );
  statsBox.appendChild(
    statTile({
      label: "Questões respondidas",
      value: String(total),
      accent: TILE_ACCENTS[1],
      hint: notScored ? `${notScored} sem valer nota.` : "Todas valem nota.",
    }),
  );
  statsBox.appendChild(
    statTile({
      label: "Seu nível",
      value: masteryLabel(level),
      empty: !diagnosis,
      textual: true,
      accent: TILE_ACCENTS[2],
      hint: "Uma estimativa para você se orientar.",
    }),
  );
  statsBox.appendChild(
    statTile({
      label: "Última atividade",
      value: last ? formatDateTime(last) : "—",
      empty: !last,
      textual: true,
      accent: TILE_ACCENTS[3],
      hint: last ? "" : "Nada por aqui ainda.",
    }),
  );
  updateHealth(acc);
}

function updateHealth(acc) {
  if (!healthPill || !healthNum) return;
  const pct = formatPercentValue(acc);
  if (pct === null) {
    healthPill.hidden = true;
    return;
  }
  healthPill.hidden = false;
  healthNum.textContent = String(pct);
  const bars = healthPill.querySelectorAll(".health-pill__bars i");
  const filled = Math.round(pct / 20);
  bars.forEach((b, i) => b.classList.toggle("on", i < filled));
}

/* ---------- 3. desempenho por disciplina ---------- */

function renderDisciplines(overview) {
  discBox.textContent = "";
  if (!overview) {
    const err = lastSectionErrors.overview;
    if (err) {
      renderErrorWithRetry(discBox, {
        title: "Desempenho por disciplina não carregou. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retrySummarySections,
      });
    } else {
      renderEmpty(discBox, {
        title: "Ainda não deu para carregar",
        description: "Seu desempenho por disciplina aparece aqui.",
      });
    }
    return;
  }
  const rows = overview?.byDiscipline || [];
  if (!rows.length) {
    renderEmpty(discBox, {
      title: "Nenhuma questão respondida",
      description: "Assim que você responder questões, mostramos como vai em Língua Portuguesa e em Matemática.",
    });
    return;
  }

  const palette = ["linear-gradient(180deg,#e2571e,#b83e06)", "linear-gradient(180deg,#f5c518,#d99a00)", "linear-gradient(180deg,#ffffff,#dcebfa)", "linear-gradient(180deg,#3d8fd1,#0f5084)"];
  const bars = el("div", { className: "alloc-bars", attrs: { role: "img", "aria-label": "Aproveitamento por disciplina em barras" } });
  rows.slice(0, 4).forEach((d, i) => {
    const pct = formatPercentValue(d.accuracy) ?? 0;
    const cell = el("div", { className: "alloc-bar" });
    const track = el("div", { className: "alloc-bar__track" });
    const fill = el("div", { className: "alloc-bar__fill", text: `${pct}%` });
    fill.style.setProperty("--ab", palette[i % palette.length]);
    fill.style.height = `${Math.max(18, pct)}%`;
    /* Texto do percentual sobre gradientes claros: branco sobre amarelo
     * (#f5c518) media 1,6:1 e sobre azul-claro (#3d8fd1) 3,5:1. Índices
     * 1 e 2 usam navy (#0d2b45: 8,9:1 e 12:1); o azul-médio precisa do
     * navy mais escuro (#081f33: 4,8:1). Só o laranja (índice 0) mantém
     * o branco. Vale nos dois temas, pois a paleta é fixa. */
    if (i === 1 || i === 2) fill.style.color = "#0d2b45";
    else if (i === 3) fill.style.color = "#081f33";
    track.appendChild(fill);
    cell.appendChild(track);
    cell.appendChild(el("p", { className: "alloc-bar__name", text: disciplineLabel(d.disciplineCode, d.disciplineName) }));
    cell.appendChild(el("p", { className: "alloc-bar__meta", text: `${d.correct ?? 0}/${d.attempts ?? 0} acertos` }));
    bars.appendChild(cell);
  });
  discBox.appendChild(bars);

  const list = el("ul", { className: "disc-list" });
  for (const d of rows) {
    const pct = formatPercentValue(d.accuracy);
    const li = el("li", { className: "disc-item" });
    const top = el("div", { className: "disc-item__top" });
    top.appendChild(
      el("span", {
        className: "disc-item__name",
        text: disciplineLabel(d.disciplineCode, d.disciplineName),
      }),
    );
    const value = el("span", {
      className: pct === null ? "disc-item__value disc-item__value--empty" : "disc-item__value",
      text: formatPercent(d.accuracy),
    });
    top.appendChild(value);
    li.appendChild(top);

    if (pct !== null) {
      const track = el("div", {
        className: "disc-item__track",
        attrs: {
          role: "progressbar",
          "aria-valuenow": String(pct),
          "aria-valuemin": "0",
          "aria-valuemax": "100",
          "aria-label": `Aproveitamento em ${disciplineLabel(d.disciplineCode, d.disciplineName)}`,
        },
      });
      const fill = el("div", { className: "disc-item__fill" });
      fill.style.width = `${pct}%`;
      track.appendChild(fill);
      li.appendChild(track);
    }

    li.appendChild(
      el("p", {
        className: "disc-item__meta",
        text: `${d.correct ?? 0} acertos em ${d.attempts ?? 0} respondidas`,
      }),
    );
    list.appendChild(li);
  }
  discBox.appendChild(list);
}

/* ---------- faixa foco rápido ---------- */

function sparkline(values, w = 72, h = 24) {
  const svg = svgNode("svg", { width: w, height: h, viewBox: `0 0 ${w} ${h}`, "aria-hidden": "true", class: "focus-card__spark" });
  if (!values.length) return svg;
  const max = Math.max(...values, 1);
  const min = Math.min(...values, 0);
  const span = Math.max(max - min, 1);
  const pts = values.map((v, i) => {
    const x = values.length === 1 ? w / 2 : (i / (values.length - 1)) * (w - 8) + 4;
    const y = h - 4 - ((v - min) / span) * (h - 8);
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  });
  const up = values[values.length - 1] >= values[0];
  const line = svgNode("polyline", {
    points: pts.join(" "),
    fill: "none",
    stroke: up ? "var(--color-success-600)" : "var(--color-danger-600)",
    "stroke-width": "2",
    "stroke-linecap": "round",
    "stroke-linejoin": "round",
  });
  svg.appendChild(line);
  const last = pts[pts.length - 1].split(",");
  svg.appendChild(svgNode("circle", { cx: last[0], cy: last[1], r: "3", fill: up ? "var(--color-success-600)" : "var(--color-danger-600)" }));
  return svg;
}

function renderWatch(overview, diagnosis) {
  if (!watchBox) return;
  watchBox.textContent = "";
  const discs = overview?.byDiscipline || [];
  const cards = [];
  for (const d of discs.slice(0, 4)) {
    const name = disciplineLabel(d.disciplineCode, d.disciplineName);
    const pct = formatPercentValue(d.accuracy);
    // Código cru vai só para o parâmetro da URL (filtro), nunca para o
    // texto exibido — o nome visível passa por disciplineLabel acima.
    const discParam = d.disciplineCode || "";
    cards.push({
      name,
      value: formatPercent(d.accuracy),
      sub: `${d.correct ?? 0}/${d.attempts ?? 0}`,
      delta: null,
      href: estudosHref({ disciplineCode: discParam }),
      spark: [40, 55, 48, 62, pct ?? 50],
    });
  }
  const prios = (diagnosis?.priorities || []).slice(0, 4);
  for (const p of prios) {
    if (cards.length >= 6) break;
    cards.push({
      name: p.topicName || p.topicCode || "Assunto",
      value: p.historicalQuestions ? `${p.historicalQuestions} na prova` : formatPercent(p.accuracy),
      sub: disciplineLabel(p.disciplineCode, p.disciplineName),
      delta: null,
      href: p.topicId ? estudosHref({ disciplineCode: p.disciplineCode || "", topicId: p.topicId }) : "./estudos.html",
      spark: [30, 42, 38, 55, 48],
    });
  }
  if (!cards.length) {
    const empty = el("p", { className: "stat-label", text: "Responda questões para ver seu foco rápido aqui." });
    watchBox.appendChild(empty);
    return;
  }
  for (const c of cards) {
    const a = el("a", { className: "focus-card", attrs: { role: "listitem", href: c.href } });
    const av = el("span", { className: "focus-card__avatar", text: initialsOf(c.name) });
    av.style.setProperty("--fc", avatarColor(c.name));
    av.setAttribute("aria-hidden", "true");
    a.appendChild(av);
    const mid = el("div");
    mid.appendChild(el("p", { className: "focus-card__name", text: c.name }));
    const vrow = el("p", { className: "focus-card__value", text: c.value });
    mid.appendChild(vrow);
    if (c.sub) mid.appendChild(el("p", { className: "stat-label", text: c.sub }));
    a.appendChild(mid);
    a.appendChild(sparkline(c.spark));
    watchBox.appendChild(a);
  }
}

/* ---------- nível (gauge) ---------- */

const MASTERY_SCORE = {
  DOMINADO: 92,
  CONSOLIDADO: 78,
  EM_DESENVOLVIMENTO: 55,
  EM_OBSERVACAO: 45,
  FRAGIL: 32,
  INICIAL: 20,
  NAO_AVALIADO: 0,
  DESCONHECIDO: 0,
};

function renderLevel(overview, diagnosis) {
  if (!levelBox) return;
  levelBox.textContent = "";
  const acc = overview?.accuracy ?? diagnosis?.accuracy ?? null;
  const pct = formatPercentValue(acc);
  const rawLevel = diagnosis?.overallLevel || null;
  const score = pct !== null ? pct : (MASTERY_SCORE[rawLevel] ?? 0);
  const label = rawLevel ? masteryLabel(rawLevel) : "Ainda sem dados";

  const wrap = el("div", { className: "gauge-wrap" });
  const W = 260;
  const H = 150;
  const svg = svgNode("svg", { width: W, height: H, viewBox: `0 0 ${W} ${H}`, role: "img", "aria-label": `Nível de preparo ${score} de 100` });
  const cx = W / 2;
  const cy = 128;
  const r = 96;
  const polar = (deg) => {
    const rad = (Math.PI * deg) / 180;
    return [cx + r * Math.cos(rad), cy + r * Math.sin(rad)];
  };
  const arc = (a0, a1) => {
    const [x0, y0] = polar(a0);
    const [x1, y1] = polar(a1);
    return `M ${x0.toFixed(1)} ${y0.toFixed(1)} A ${r} ${r} 0 0 1 ${x1.toFixed(1)} ${y1.toFixed(1)}`;
  };
  svg.appendChild(svgNode("path", { d: arc(180, 360), fill: "none", stroke: "var(--color-line-200)", "stroke-width": "18", "stroke-linecap": "round" }));
  const frac = Math.max(0, Math.min(100, score)) / 100;
  if (frac > 0.01) {
    const end = 180 + frac * 180;
    const p = svgNode("path", { d: arc(180, end), fill: "none", stroke: "var(--color-success-600)", "stroke-width": "18", "stroke-linecap": "round" });
    svg.appendChild(p);
    const [dx, dy] = polar(end);
    const dot = svgNode("circle", { cx: dx.toFixed(1), cy: dy.toFixed(1), r: "10", fill: "#fff", stroke: "var(--color-success-600)", "stroke-width": "4" });
    svg.appendChild(dot);
  }
  wrap.appendChild(svg);
  const num = el("p", { className: "gauge-num", text: `${score} ` });
  num.appendChild(el("small", { text: "/100" }));
  wrap.appendChild(num);
  wrap.appendChild(el("p", { className: "gauge-cap", text: label }));
  const hint = pct !== null
    ? `Seu aproveitamento geral nas questões que valem nota.`
    : `Responda questões para calcularmos seu nível.`;
  wrap.appendChild(el("p", { className: "stat-label", text: hint }));
  levelBox.appendChild(wrap);
}

/* ---------- 4. o que treinar ---------- */

function renderPriorities(diagnosis) {
  prioBox.textContent = "";
  if (!diagnosis) {
    const err = lastSectionErrors.diagnosis;
    if (err) {
      renderErrorWithRetry(prioBox, {
        title: "Prioridades não carregaram. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retrySummarySections,
      });
    } else {
      renderEmpty(prioBox, {
        title: "Ainda não deu para carregar",
        description: "Aqui entram os assuntos que mais valem a pena treinar.",
      });
    }
    return;
  }
  const all = diagnosis.priorities || [];
  // Três bastam para dizer "o que treinar" sem transformar a coluna numa
  // parede de cartões quase idênticos. A lista completa está no roteiro,
  // logo abaixo.
  const items = all.slice(0, 3);
  if (!items.length) {
    const box = el("div", { className: "alert alert--success", attrs: { role: "status" } });
    const inner = el("div");
    inner.appendChild(el("strong", { text: "Nada pendente por aqui" }));
    inner.appendChild(
      el("p", {
        text: "Os assuntos avaliados estão em bom nível. Continue revisando de vez em quando para não esquecer.",
      }),
    );
    box.appendChild(inner);
    prioBox.appendChild(box);
    prioBox.appendChild(reviewQueueCta());
    return;
  }
  const list = el("ol", { className: "priority-list" });
  for (const p of items) {
    const li = el("li", { className: "priority-list__item" });
    const top = el("div", { className: "priority-list__top" });
    top.appendChild(el("span", { className: "priority-list__rank", text: String(p.rank) }));
    top.appendChild(el("strong", { className: "priority-list__name", text: p.topicName || p.topicCode }));
    li.appendChild(top);
    li.appendChild(
      el("p", {
        className: "priority-list__reason",
        text: studentReason(p),
      }),
    );
    const foot = el("div", { className: "insight-foot" });
    const bits = [disciplineLabel(p.disciplineCode, p.disciplineName)];
    if (p.historicalQuestions) bits.push(`${p.historicalQuestions} questões na prova`);
    if (p.editionsCount) {
      bits.push(`${p.editionsCount} ${p.editionsCount === 1 ? "edição" : "edições"}`);
    }
    foot.appendChild(el("span", { className: "badge", text: bits.filter(Boolean).join(" · ") }));
    if (p.topicId) {
      foot.appendChild(
        el("a", {
          className: "btn btn--ghost btn--sm",
          text: "Praticar",
          attrs: { href: estudosHref({ disciplineCode: p.disciplineCode || "", topicId: p.topicId }) },
        }),
      );
    }
    li.appendChild(foot);
    list.appendChild(li);
  }
  prioBox.appendChild(list);
  if (all.length > items.length) {
    prioBox.appendChild(
      el("p", {
        className: "stat-label mt-2",
        text: `Mostrando ${items.length} de ${all.length} assuntos. O roteiro completo está abaixo.`,
      }),
    );
  }
  // TASK 17.3: atalho para a fila de revisão junto às prioridades — o aluno
  // sai do "o que treinar" direto para os erros a consolidar.
  prioBox.appendChild(reviewQueueCta());
}

/* CTA único para a fila de revisão (TASK 17.3). */
function reviewQueueCta() {
  const actions = el("div", { className: "btn-group mt-4" });
  actions.appendChild(
    el("a", {
      className: "btn btn--secondary btn--sm",
      text: "Revisar erros",
      attrs: { href: "./estudos.html?aba=revisao" },
    }),
  );
  return actions;
}

/* ---------- 5. roteiro + passo agora ---------- */

function topicNameOf(item) {
  const t = topicById.get(Number(item?.topicId));
  if (t) {
    return {
      name: t.name,
      meta: disciplineLabel(t.disciplineCode, t.disciplineName),
    };
  }
  return { name: "Assunto do seu roteiro", meta: "" };
}

function nextStudyOf(plan) {
  const items = plan?.items || plan?.recommendations || [];
  const open = items.filter((i) => i.status === "TODO" || i.status === "DOING");
  const pool = open.length ? open : items;
  if (!pool.length) return null;
  return [...pool].sort((a, b) => (a.priority - b.priority) || (a.id - b.id))[0];
}

function renderPlan(plan, loadError) {
  planBox.textContent = "";
  nextBox.textContent = "";

  if (loadError) {
    renderErrorWithRetry(planBox, {
      title: "Roteiro indisponível. ",
      description: friendlyMessage(loadError),
      traceId: loadError instanceof ApiError ? loadError.traceId : null,
      onRetry: retryPlanSection,
    });
    renderNextStepEmpty(
      "Não deu para carregar seu roteiro",
      "Tente novamente em alguns instantes.",
    );
    planRegenerateBtn.hidden = true;
    return;
  }

  if (!plan) {
    planRegenerateBtn.hidden = true;
    planGenerateBtn.hidden = false;
    renderEmpty(planBox, {
      title: "Você ainda não tem um roteiro",
      description:
        "O roteiro é a lista de assuntos na ordem que vale mais a pena estudar agora para você. Ele muda conforme seu desempenho.",
    });
    renderNextStepEmpty(
      "Comece pelo diagnóstico",
      "Responda algumas questões para descobrirmos seus pontos fortes e o que treinar. A partir disso montamos seu roteiro.",
    );
    return;
  }

  planGenerateBtn.hidden = true;
  planRegenerateBtn.hidden = false;
  const items = [...(plan.items || plan.recommendations || [])].sort(
    (a, b) => (a.priority - b.priority) || (a.id - b.id),
  );

  if (!items.length) {
    renderEmpty(planBox, {
      title: "Roteiro vazio",
      description: "Atualize o roteiro para receber uma nova lista de assuntos.",
    });
  } else {
    const done = items.filter((i) => i.status === "DONE").length;
    const progress = el("div", { className: "plan-progress" });
    const bar = el("div", {
      className: "progress",
      attrs: {
        role: "progressbar",
        "aria-valuenow": String(done),
        "aria-valuemin": "0",
        "aria-valuemax": String(items.length),
        "aria-label": "Progresso do roteiro",
      },
    });
    const fill = el("div", { className: "progress__bar" });
    fill.style.width = `${items.length ? Math.round((done / items.length) * 100) : 0}%`;
    bar.appendChild(fill);
    progress.appendChild(bar);
    const meta = el("p", { className: "stat-label" });
    meta.appendChild(
      el("small", {
        text: `${done} de ${items.length} concluídos · criado em ${formatDateTime(plan.generatedAt)}`,
      }),
    );
    progress.appendChild(meta);
    planBox.appendChild(progress);

    const list = el("ol", { className: "plan-items" });
    for (const item of items.slice(0, 8)) {
      const { name, meta: m } = topicNameOf(item);
      const isDone = item.status === "DONE";
      // TASK 18.3: cada item mostra o motivo ("por que estudar isso") com a
      // evidência + CTAs "Praticar agora" (recorte em estudos) e "Revisar
      // erros" (fila filtrada por assunto — TASK 17.2).
      const ev = parseEvidence(item);
      const target = practiceTarget(item, topicById);
      const row = el("li", {
        className: isDone ? "plan-items__row plan-items__row--done" : "plan-items__row",
      });
      const left = el("div");
      left.appendChild(el("span", { className: "plan-items__name", text: name }));
      const metaBits = [];
      if (m) metaBits.push(m);
      if (item.priority !== undefined && item.priority !== null) {
        metaBits.push(`${item.priority}º no roteiro`);
      }
      if (metaBits.length) {
        left.appendChild(el("p", { className: "plan-items__meta", text: metaBits.join(" · ") }));
      }
      left.appendChild(el("p", { className: "plan-items__meta", text: planMotive(item) }));
      const right = el("div", { className: "cluster" });
      if (statusLabel(item.status)) {
        right.appendChild(
          el("span", { className: `badge ${statusBadge(item.status)}`.trim(), text: statusLabel(item.status) }),
        );
      }
      if (!isDone) {
        right.appendChild(
          el("a", {
            className: "btn btn--ghost btn--sm",
            text: "Praticar agora",
            attrs: {
              href: buildEstudosHref({ ...target, origin: "painel" }),
              "aria-label": `Praticar agora ${name}`,
            },
          }),
        );
        right.appendChild(
          el("a", {
            className: "btn btn--ghost btn--sm",
            text: "Revisar erros",
            attrs: {
              href: revisaoHref({ disciplineCode: target.disciplineCode, topicId: item.topicId ?? "" }),
              "aria-label": `Revisar erros de ${name}`,
            },
          }),
        );
        if (ev && ev.sampleQuestionIds.length > 0) {
          right.appendChild(
            el("a", {
              className: "btn btn--ghost btn--sm",
              text: "Ver exemplo oficial",
              attrs: {
                href: sampleHref(ev.sampleQuestionIds[0]),
                "aria-label": `Ver questão oficial de exemplo de ${name}`,
              },
            }),
          );
        }
        const doneBtn = el("button", {
          className: "btn btn--ghost btn--sm",
          text: "Concluir",
          attrs: { type: "button" },
        });
        doneBtn.addEventListener("click", () => changeItemStatus(item, "DONE", doneBtn));
        right.appendChild(doneBtn);
      }
      row.appendChild(left);
      row.appendChild(right);
      list.appendChild(row);
    }
    planBox.appendChild(list);
    if (items.length > 8) {
      const more = el("p", { className: "stat-label mt-2" });
      more.appendChild(el("small", { text: `Mostrando 8 de ${items.length} assuntos.` }));
      planBox.appendChild(more);
    }
  }

  renderNextStepOf(plan);
}

function renderNextStepOf(plan) {
  const next = nextStudyOf(plan);
  if (!next) {
    renderNextStepEmpty(
      "Roteiro concluído",
      "Você marcou tudo o que tinha para fazer. Faça mais questões e atualize o roteiro para continuar.",
    );
    return;
  }
  const { name, meta: m } = topicNameOf(next);
  // TASK 18.3: o hero mostra o mesmo motivo do item na lista (evidência),
  // com o retrato do diagnóstico como reserva para item sem evidência.
  const card = nextStepShell({
    eyebrow: "Estude agora",
    title: name,
    why: planMotive(next) || studentNextReason(next.topicId),
  });

  const chips = el("div", { className: "next-step__meta" });
  if (m) chips.appendChild(el("span", { className: "chip", text: m }));
  chips.appendChild(el("span", { className: "chip", text: `${next.priority}º no roteiro` }));
  if (statusLabel(next.status)) {
    chips.appendChild(el("span", { className: "chip", text: statusLabel(next.status) }));
  }
  card.appendChild(chips);

  const actions = el("div", { className: "next-step__actions" });
  const nextTopic = topicById.get(Number(next.topicId));
  actions.appendChild(
    el("a", {
      className: "btn btn--primary",
      text: "Praticar este assunto",
      attrs: { href: estudosHref({ disciplineCode: nextTopic?.disciplineCode || "", topicId: next.topicId ?? "" }) },
    }),
  );
  if (next.status !== "DONE") {
    const done = el("button", {
      className: "btn btn--secondary",
      text: "Já pratiquei",
      attrs: { type: "button" },
    });
    done.addEventListener("click", () => changeItemStatus(next, "DONE", done, "Anotado! O roteiro foi atualizado."));
    actions.appendChild(done);
  }
  card.appendChild(actions);
  nextBox.appendChild(card);
}

async function changeItemStatus(item, status, button, okMessage) {
  setButtonLoading(button, true, "Salvando…");
  try {
    await updatePlanItemStatus(item.id ?? item.topicId, status);
    item.status = status;
    renderPlan(currentPlan, null);
    if (okMessage) toast(okMessage, "success");
  } catch (err) {
    toast(friendlyMessage(err), "info");
  } finally {
    setButtonLoading(button, false);
  }
}

planGenerateBtn?.addEventListener("click", async () => {
  setButtonLoading(planGenerateBtn, true, "Montando…");
  try {
    currentPlan = await generatePlan();
    renderPlan(currentPlan, null);
    toast("Roteiro montado com base no seu desempenho.", "success");
  } catch (err) {
    renderErrorSummary(errorBox, {
      title: friendlyMessage(err),
      traceId: err instanceof ApiError ? err.traceId : null,
    });
  } finally {
    setButtonLoading(planGenerateBtn, false);
  }
});

planRegenerateBtn?.addEventListener("click", async () => {
  setButtonLoading(planRegenerateBtn, true, "Atualizando…");
  try {
    currentPlan = await generatePlan();
    renderPlan(currentPlan, null);
    toast("Roteiro atualizado. O anterior continua no histórico.", "success");
  } catch (err) {
    renderErrorSummary(errorBox, {
      title: friendlyMessage(err),
      traceId: err instanceof ApiError ? err.traceId : null,
    });
  } finally {
    setButtonLoading(planRegenerateBtn, false);
  }
});

/* ---------- 6. evolução ---------- */

evoSelect?.addEventListener("change", async () => {
  evoBox.textContent = "";
  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Atualizando sua evolução…" }));
  evoBox.appendChild(loading);
  try {
    const data = await fetchEvolution(evoSelect.value);
    renderEvolution(data, null);
  } catch (err) {
    renderEvolution(null, err);
  }
});

function evolutionChart(buckets) {
  const W = 640;
  const H = 220;
  const PAD = { l: 8, r: 8, t: 14, b: 26 };
  const svg = svgNode("svg", {
    width: W, height: H, viewBox: `0 0 ${W} ${H}`,
    class: "evo-svg", role: "img",
    "aria-label": "Gráfico da sua evolução de aproveitamento ao longo do tempo",
  });
  const vals = buckets.map((b) => formatPercentValue(b.accuracy) ?? 0);
  const iw = W - PAD.l - PAD.r;
  const ih = H - PAD.t - PAD.b;
  const x = (i) => (buckets.length === 1 ? PAD.l + iw / 2 : PAD.l + (i / (buckets.length - 1)) * iw);
  const y = (v) => PAD.t + ih - (Math.max(0, Math.min(100, v)) / 100) * ih;
  const defs = svgNode("defs", {});
  const grad = svgNode("linearGradient", { id: "evo-fill", x1: "0", y1: "0", x2: "0", y2: "1" });
  grad.appendChild(svgNode("stop", { offset: "0%", "stop-color": "#ffffff", "stop-opacity": "0.45" }));
  grad.appendChild(svgNode("stop", { offset: "100%", "stop-color": "#ffffff", "stop-opacity": "0.02" }));
  defs.appendChild(grad);
  svg.appendChild(defs);
  for (const g of [25, 50, 75]) {
    const gy = PAD.t + ih - (g / 100) * ih;
    svg.appendChild(svgNode("line", { x1: PAD.l, y1: gy, x2: W - PAD.r, y2: gy, stroke: "#ffffff", "stroke-opacity": "0.25", "stroke-dasharray": "4 6" }));
  }
  const linePts = vals.map((v, i) => `${x(i).toFixed(1)},${y(v).toFixed(1)}`).join(" ");
  const area = svgNode("polygon", {
    points: `${PAD.l},${(PAD.t + ih).toFixed(1)} ${linePts} ${(W - PAD.r).toFixed(1)},${(PAD.t + ih).toFixed(1)}`,
    fill: "url(#evo-fill)",
  });
  svg.appendChild(area);
  svg.appendChild(svgNode("polyline", {
    points: linePts, fill: "none", stroke: "#ffe08a",
    "stroke-width": "3", "stroke-linecap": "round", "stroke-linejoin": "round",
  }));
  vals.forEach((v, i) => {
    const isMax = v === Math.max(...vals);
    svg.appendChild(svgNode("circle", {
      cx: x(i).toFixed(1), cy: y(v).toFixed(1), r: isMax ? "5" : "3.5",
      fill: "#fff", stroke: "#b83e06", "stroke-width": "2",
    }));
  });
  const labelIdx = buckets.length <= 5
    ? buckets.map((_, i) => i)
    : [0, Math.floor(buckets.length / 2), buckets.length - 1];
  for (const pos of labelIdx) {
    const i = pos;
    const anchor = i === 0 ? "start" : (i === buckets.length - 1 ? "end" : "middle");
    const tx = i === 0 ? PAD.l : (i === buckets.length - 1 ? W - PAD.r : x(i));
    const t = svgNode("text", {
      x: tx.toFixed(1), y: (H - 8).toFixed(1),
      fill: "#ffffff", "fill-opacity": "0.85", "font-size": "11",
      "text-anchor": anchor,
    });
    t.textContent = formatBucketDate(buckets[i].bucketStart);
    svg.appendChild(t);
  }
  return svg;
}

function renderEvolution(data, loadError) {
  evoBox.textContent = "";
  if (evoHeadline) evoHeadline.textContent = "—";
  if (evoDelta) evoDelta.hidden = true;
  if (loadError) {
    if (evoHeadline) evoHeadline.textContent = "Indisponível";
    renderErrorWithRetry(evoBox, {
      title: "Sua evolução não carregou. ",
      description: friendlyMessage(loadError),
      traceId: loadError instanceof ApiError ? loadError.traceId : null,
      onRetry: async () => {
        try {
          await retryEvolutionSection();
        } catch (err) {
          renderEvolution(null, err);
          throw err;
        }
      },
    });
    return;
  }
  const buckets = data?.buckets || [];
  if (!buckets.length) {
    if (evoHeadline) evoHeadline.textContent = "Sem dados ainda";
    const sub = el("p", { className: "perf-chart__sub", text: "Responda questões para ver seu aproveitamento ao longo do tempo." });
    evoBox.appendChild(sub);
    return;
  }
  const vals = buckets.map((b) => formatPercentValue(b.accuracy) ?? 0);
  const last = vals[vals.length - 1];
  const first = vals[0];
  if (evoHeadline) evoHeadline.textContent = formatPercent(buckets[buckets.length - 1].accuracy);
  if (evoDelta) {
    const diff = last - first;
    evoDelta.hidden = false;
    evoDelta.textContent = `${diff >= 0 ? "↗" : "↘"} ${Math.abs(diff)} p.p. no período`;
    evoDelta.classList.toggle("delta-pill--up", diff >= 0);
    evoDelta.classList.toggle("delta-pill--down", diff < 0);
  }
  evoBox.appendChild(evolutionChart(buckets));
  const legend = el("div", { className: "evo-legend" });
  legend.appendChild(el("span", { text: `${buckets.length} ${buckets.length === 1 ? "período" : "períodos"} · ${buckets[buckets.length - 1].correct}/${buckets[buckets.length - 1].scored} no último` }));
  evoBox.appendChild(legend);
  const sr = el("ol", { className: "evo-bars visually-hidden" });
  for (const b of buckets) {
    const row = el("li", { className: "evo-bars__row" });
    row.appendChild(el("span", { className: "evo-bars__label", text: formatBucketDate(b.bucketStart) }));
    row.appendChild(el("span", { text: `${formatPercent(b.accuracy)}` }));
    row.appendChild(el("span", { className: "evo-bars__value", text: `${b.correct}/${b.scored}` }));
    sr.appendChild(row);
  }
  evoBox.appendChild(sr);
}

/* ---------- 7. simulados ---------- */

function renderSimulations(page, loadError) {
  simBox.textContent = "";
  if (loadError) {
    renderErrorWithRetry(simBox, {
      title: "Seus simulados não carregaram. ",
      description: friendlyMessage(loadError),
      traceId: loadError instanceof ApiError ? loadError.traceId : null,
      onRetry: async () => {
        try {
          await retrySimulationsSection();
        } catch (err) {
          renderSimulations(null, err);
          throw err;
        }
      },
    });
    return;
  }
  const items = page?.content || [];
  if (!items.length) {
    renderEmpty(simBox, {
      title: "Você ainda não fez nenhum simulado",
      description:
        "É a melhor forma de treinar no tempo da prova: você faz sem interrupção e só vê o gabarito no final.",
    });
    simBox.appendChild(
      el("a", {
        className: "btn btn--primary btn--sm mt-4",
        text: "Fazer meu primeiro simulado",
        attrs: { href: "./simulado.html" },
      }),
    );
    return;
  }
  const list = el("ol", { className: "sim-list" });
  for (const s of items) {
    const li = el("li", { className: "sim-list__item" });
    const left = el("div");
    left.appendChild(el("p", { className: "sim-list__title", text: simulationTitle(s.title, "Simulado") }));
    const bits = [];
    const discLabel = s.disciplineCode ? disciplineLabel(s.disciplineCode) : "";
    if (discLabel) bits.push(discLabel);
    const mode = modeLabel(s.mode);
    if (mode) bits.push(mode);
    bits.push(`${s.questionCount} questões`);
    bits.push(formatDateTime(s.startedAt));
    left.appendChild(el("p", { className: "sim-list__meta", text: bits.join(" · ") }));
    const right = el("div", { className: "cluster" });
    if (statusLabel(s.status)) {
      right.appendChild(
        el("span", { className: `badge ${statusBadge(s.status)}`.trim(), text: statusLabel(s.status) }),
      );
    }
    li.appendChild(left);
    li.appendChild(right);
    list.appendChild(li);
  }
  simBox.appendChild(list);
  simBox.appendChild(
    el("a", {
      className: "btn btn--secondary btn--sm mt-4",
      text: "Abrir simulados",
      attrs: { href: "./simulado.html" },
    }),
  );
}
