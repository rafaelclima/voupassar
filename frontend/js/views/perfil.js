/* VouPassar — perfil (TASK 6.8)
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=perfil.html). Com sessão, busca
 * em paralelo: profile + stats (3.6), overview + evolution (4.1), diagnosis
 * (4.2), plan vigente (4.3/4.4, 404 vira vazio) e history (3.6, paginado).
 * Cada seção falha de forma isolada: o restante continua visível.
 * Conquistas: Fase 7 ainda não existe no backend — vazio honesto com
 * PENDENTE, nunca número inventado. Sem innerHTML (só textContent via el()).
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { logout } from "../api/auth.js";
import {
  fetchProfile,
  updateProfile,
  fetchProfileStats,
  fetchHistory,
  fetchOverview,
  fetchEvolution,
  fetchDiagnosis,
  fetchPlan,
} from "../api/perfil.js";
import {
  el,
  renderEmpty,
  renderErrorSummary,
  renderErrorWithRetry,
  setButtonLoading,
  setFieldError,
  toast,
} from "../components/ui.js";
import { renderAuthGuard, requireSessionOrGuard } from "./auth-shared.js";
import {
  disciplineLabel,
  topicLabel,
  translateTopicCodes,
} from "../vocab.js";

const guard = document.getElementById("perfil-guard");
const errorBox = document.getElementById("perfil-error");
const loadingBox = document.getElementById("perfil-loading");
const content = document.getElementById("perfil-content");
const subtitle = document.getElementById("perfil-subtitle");
const heroBox = document.getElementById("perfil-hero");
const dadosBox = document.getElementById("perfil-dados");
const form = document.getElementById("perfil-form");
const nameInput = document.getElementById("pf-nome");
const yearInput = document.getElementById("pf-ano");
const targetInput = document.getElementById("pf-alvo");
const goalInput = document.getElementById("pf-objetivo");
const goalCount = document.getElementById("pf-objetivo-count");
const saveBtn = document.getElementById("perfil-save");
const statsBox = document.getElementById("perfil-stats");
const levelBox = document.getElementById("perfil-level");
const modesBox = document.getElementById("perfil-modes");
const discBox = document.getElementById("perfil-disciplines");
const metasBox = document.getElementById("perfil-metas");
const strengthsBox = document.getElementById("perfil-strengths");
const weaknessesBox = document.getElementById("perfil-weaknesses");
const evoSelect = document.getElementById("evo-granularity");
const evoBox = document.getElementById("perfil-evolution");
const evoHeadline = document.getElementById("perfil-evo-headline");
const evoDelta = document.getElementById("perfil-evo-delta");
const historyBox = document.getElementById("perfil-history");
const historyCount = document.getElementById("perfil-history-count");
const moreBtn = document.getElementById("perfil-more");
const achievementsBox = document.getElementById("perfil-achievements");

let overviewCache = null;
let diagnosisCache = null;
let statsCache = null;
let lastSectionErrors = {};
let historyPage = 0;
const HISTORY_SIZE = 20;
let historyTotal = null;
let historyExhausted = false;

wireLogoutButtons();
wireGoalCounter();

main();

async function main() {
  const user = await requireSessionOrGuard(showGuard);
  if (!user) {
    return;
  }
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
        window.location.href = "./login.html?next=perfil.html";
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
  if (form) form.hidden = true;
  guard.hidden = false;
  renderAuthGuard(guard, {
    title: "Entre para ver seu perfil",
    description: "O perfil mostra seus dados, estatísticas e histórico. Ele precisa da sua sessão — entre ou crie uma conta para continuar.",
    next: "perfil.html",
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

function formatTimeSpent(seconds) {
  if (seconds === null || seconds === undefined) return null;
  const n = Number(seconds);
  if (!Number.isFinite(n) || n < 0) return null;
  if (n < 60) return `${n}s`;
  const min = Math.floor(n / 60);
  const rest = n % 60;
  return rest ? `${min}min ${rest}s` : `${min}min`;
}

function masteryBadge(level) {
  const map = {
    DOMINADO: "badge--success",
    CONSOLIDADO: "badge--success",
    FRAGIL: "badge--danger",
    INICIAL: "badge--danger",
    EM_DESENVOLVIMENTO: "badge--warning",
    EM_OBSERVACAO: "badge--info",
    NAO_AVALIADO: "",
    DESCONHECIDO: "",
  };
  return map[level] || "";
}

function masteryLabel(level) {
  const map = {
    DOMINADO: "Dominado",
    FRAGIL: "Frágil",
    EM_DESENVOLVIMENTO: "Em desenvolvimento",
    EM_OBSERVACAO: "Em observação",
    NAO_AVALIADO: "Não avaliado",
    DESCONHECIDO: "Desconhecido",
    INICIAL: "Inicial",
    CONSOLIDADO: "Consolidado",
  };
  return map[level] || String(level || "—");
}

function modeLabel(mode) {
  const map = { ESTUDO: "Estudo", PROVA: "Prova", REVISAO: "Revisão" };
  return map[mode] || String(mode || "—");
}

function orDash(value) {
  if (value === null || value === undefined || value === "") return "—";
  return String(value);
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
  return (words[0][0] + words[words.length - 1][0]).toUpperCase();
}

function avatarColor(key) {
  const palette = ["#0f5084", "#6d43cc", "#0b7a4b", "#b83e06", "#4c2e8f"];
  let h = 0;
  for (const c of String(key || "")) h = (h * 31 + c.charCodeAt(0)) % 997;
  return palette[h % palette.length];
}

/* ---------- carga ---------- */

async function loadAll() {
  errorBox.textContent = "";
  loadingBox.hidden = false;
  content.hidden = true;

  const [profile, stats, overview, diagnosis, plan, evolution, history] =
    await Promise.allSettled([
      fetchProfile(),
      fetchProfileStats(),
      fetchOverview(),
      fetchDiagnosis(),
      fetchPlan().catch((err) => {
        if (err instanceof ApiError && err.code === "NO_ACTIVE_PLAN") return null;
        throw err;
      }),
      fetchEvolution(evoSelect?.value || "WEEK"),
      fetchHistory(0, HISTORY_SIZE),
    ]);

  // Sessão expirada no meio do caminho: volta para o painel de acesso.
  const unauthorized = [profile, stats, overview, diagnosis, plan, evolution, history].some(
    (r) => r.status === "rejected" && r.reason instanceof ApiError && r.reason.status === 401,
  );
  if (unauthorized) {
    showGuard();
    guard.querySelector("p")?.replaceChildren(
      document.createTextNode("Sua sessão expirou. Entre novamente para ver o perfil."),
    );
    return;
  }

  const failures = [profile, stats, overview, diagnosis, evolution, history]
    .filter((r) => r.status === "rejected")
    .map((r) => r.reason);
  // Falha do plano (fora 404) não bloqueia o resto: vira estado local.
  if (plan.status === "rejected") failures.push(plan.reason);
  if (failures.length) {
    const first = failures[0];
    renderErrorSummary(errorBox, {
      title: "Parte dos dados falhou — o restante segue abaixo.",
      items: [friendlyMessage(first)],
      traceId: first instanceof ApiError ? first.traceId : null,
    });
  }

  const profileData = profile.status === "fulfilled" ? profile.value : null;
  statsCache = stats.status === "fulfilled" ? stats.value : null;
  overviewCache = overview.status === "fulfilled" ? overview.value : null;
  diagnosisCache = diagnosis.status === "fulfilled" ? diagnosis.value : null;
  const planData = plan.status === "fulfilled" ? plan.value : null;
  lastSectionErrors.profile = profile.status === "rejected" ? profile.reason : null;
  lastSectionErrors.stats = stats.status === "rejected" ? stats.reason : null;
  lastSectionErrors.overview = overview.status === "rejected" ? overview.reason : null;
  lastSectionErrors.diagnosis = diagnosis.status === "rejected" ? diagnosis.reason : null;
  lastSectionErrors.plan = plan.status === "rejected" ? plan.reason : null;
  lastSectionErrors.evolution = evolution.status === "rejected" ? evolution.reason : null;
  lastSectionErrors.history = history.status === "rejected" ? history.reason : null;

  const firstName = (profileData?.displayName || "").split(" ")[0];
  if (profileData) {
    subtitle.textContent = firstName
      ? `Olá, ${firstName} — este é o seu retrato como estudante: dados, desempenho, evolução e histórico.`
      : "Este é o seu retrato como estudante: dados, desempenho, evolução e histórico.";
  }

  renderHero(profileData, profile.status === "rejected" ? profile.reason : null);
  renderDados(profileData, profile.status === "rejected" ? profile.reason : null);
  renderStats(statsCache, overviewCache, diagnosisCache);
  renderLevel(statsCache, overviewCache, diagnosisCache);
  renderModes(statsCache);
  renderDisciplines(statsCache, overviewCache);
  renderMetas(profileData, planData, plan.status === "rejected" ? plan.reason : null);
  renderStrengths(diagnosisCache);
  renderWeaknesses(diagnosisCache);
  renderEvolution(
    evolution.status === "fulfilled" ? evolution.value : null,
    evolution.status === "rejected" ? evolution.reason : null,
  );
  historyPage = 0;
  historyTotal = null;
  historyExhausted = false;
  renderHistoryPage(
    history.status === "fulfilled" ? history.value : null,
    history.status === "rejected" ? history.reason : null,
    { reset: true },
  );
  renderAchievements();

  loadingBox.hidden = true;
  content.hidden = false;
}

/* ---------- retentativa por seção (TASK 16.3) ---------- */

async function retryProfileSection() {
  const profile = await fetchProfile();
  lastSectionErrors.profile = null;
  errorBox.textContent = "";
  renderHero(profile, null);
  renderDados(profile, null);
}

async function retryStatsSections() {
  const [stats, overview, diagnosis] = await Promise.all([
    fetchProfileStats(),
    fetchOverview(),
    fetchDiagnosis(),
  ]);
  statsCache = stats;
  overviewCache = overview;
  diagnosisCache = diagnosis;
  lastSectionErrors.stats = null;
  lastSectionErrors.overview = null;
  lastSectionErrors.diagnosis = null;
  errorBox.textContent = "";
  renderStats(statsCache, overviewCache, diagnosisCache);
  renderLevel(statsCache, overviewCache, diagnosisCache);
  renderModes(statsCache);
  renderDisciplines(statsCache, overviewCache);
  renderStrengths(diagnosisCache);
  renderWeaknesses(diagnosisCache);
}

async function retryEvolutionSection() {
  const data = await fetchEvolution(evoSelect?.value || "WEEK");
  lastSectionErrors.evolution = null;
  renderEvolution(data, null);
}

async function retryHistorySection() {
  const page = await fetchHistory(0, HISTORY_SIZE);
  lastSectionErrors.history = null;
  historyPage = 0;
  historyTotal = null;
  historyExhausted = false;
  renderHistoryPage(page, null, { reset: true });
}

/* ---------- 1. hero de identidade ---------- */

function renderHero(profile, loadError) {
  heroBox.textContent = "";
  const card = el("div", { className: "next-step" });
  if (loadError || !profile) {
    card.classList.add("next-step--empty");
    card.appendChild(el("p", { className: "next-step__eyebrow", text: "Seu perfil" }));
    card.appendChild(el("h3", { className: "next-step__title", text: "Não deu para carregar seus dados" }));
    card.appendChild(
      el("p", {
        className: "next-step__why",
        text: loadError ? friendlyMessage(loadError) : "Tente novamente em alguns instantes.",
      }),
    );
    if (loadError) {
      const retry = el("button", { className: "btn btn--secondary btn--sm mt-2", text: "Tentar de novo", attrs: { type: "button" } });
      retry.addEventListener("click", async () => {
        retry.disabled = true;
        try {
          await retryProfileSection();
        } catch (err2) {
          toast(friendlyMessage(err2), "info");
          retry.disabled = false;
        }
      });
      card.appendChild(retry);
    }
    heroBox.appendChild(card);
    return;
  }
  const name = profile.displayName || "Estudante";
  card.appendChild(el("p", { className: "next-step__eyebrow", text: "Seu perfil" }));

  const row = el("div", { className: "id-hero__row" });
  const avatar = el("span", { className: "id-avatar", text: initialsOf(name), attrs: { "aria-hidden": "true" } });
  avatar.style.background = `linear-gradient(135deg, ${avatarColor(name)}, #6d43cc)`;
  row.appendChild(avatar);
  const who = el("div");
  who.appendChild(el("h3", { className: "id-hero__name", text: name }));
  if (profile.email) who.appendChild(el("p", { className: "id-hero__mail", text: profile.email }));
  row.appendChild(who);
  card.appendChild(row);

  const chips = el("div", { className: "next-step__meta" });
  if (profile.schoolYear) chips.appendChild(el("span", { className: "chip", text: profile.schoolYear }));
  if (profile.targetYear) chips.appendChild(el("span", { className: "chip", text: `Meta ${profile.targetYear}` }));
  const level = diagnosisCache?.overallLevel;
  if (level) chips.appendChild(el("span", { className: "chip", text: masteryLabel(level) }));
  const total = statsCache?.totalAttempts ?? overviewCache?.totalAttempts ?? 0;
  if (total) chips.appendChild(el("span", { className: "chip", text: `${total} ${total === 1 ? "questão respondida" : "questões respondidas"}` }));
  if (chips.children.length) card.appendChild(chips);

  const actions = el("div", { className: "next-step__actions" });
  actions.appendChild(
    el("a", { className: "btn btn--primary", text: "Editar meus dados", attrs: { href: "#sec-dados-t" } }),
  );
  actions.appendChild(
    el("a", { className: "btn btn--secondary", text: "Ver meu painel", attrs: { href: "./dashboard.html" } }),
  );
  card.appendChild(actions);
  heroBox.appendChild(card);
}

/* ---------- dados ---------- */

function renderDados(profile, loadError) {
  dadosBox.textContent = "";
  if (loadError || !profile) {
    if (loadError) {
      renderErrorWithRetry(dadosBox, {
        title: "Dados indisponíveis. ",
        description: friendlyMessage(loadError),
        traceId: loadError instanceof ApiError ? loadError.traceId : null,
        onRetry: retryProfileSection,
      });
    } else {
      renderEmpty(dadosBox, {
        title: "Dados indisponíveis",
        description: "Não foi possível carregar seus dados agora.",
      });
    }
    if (form) form.hidden = true;
    return;
  }
  if (form) form.hidden = false;
  const list = el("dl", { className: "perfil-dados__list" });
  const rows = [
    ["Nome", profile.displayName],
    ["E-mail", profile.email],
    ["Ano escolar", profile.schoolYear],
    ["Ano-alvo", profile.targetYear],
    ["Objetivo", profile.studyGoal],
  ];
  for (const [label, value] of rows) {
    const row = el("div", { className: "perfil-dados__row" });
    row.appendChild(el("dt", { className: "perfil-dados__label", text: label }));
    const dd = el("dd", { className: "perfil-dados__value" });
    dd.textContent = orDash(value);
    if (value === null || value === undefined || value === "") dd.classList.add("muted");
    row.appendChild(dd);
    list.appendChild(row);
  }
  dadosBox.appendChild(list);

  // Preenche o formulário de edição (PUT completo: ausente vira NULL no servidor).
  if (nameInput) nameInput.value = profile.displayName || "";
  if (yearInput) yearInput.value = profile.schoolYear || "";
  if (targetInput) targetInput.value = profile.targetYear ?? "";
  if (goalInput) {
    goalInput.value = profile.studyGoal || "";
    updateGoalCounter();
  }
}

function wireGoalCounter() {
  goalInput?.addEventListener("input", updateGoalCounter);
}

function updateGoalCounter() {
  if (!goalInput || !goalCount) return;
  const left = 500 - (goalInput.value || "").length;
  goalCount.textContent = `${Math.max(left, 0)} caracteres restantes.`;
}

function fieldErrorFor(details, field) {
  if (!details) return null;
  if (typeof details === "string") return null;
  if (Array.isArray(details)) {
    const hit = details.find((d) => String(d?.field || d?.name || "").toLowerCase() === field);
    return hit ? String(hit.message || hit.error || "") : null;
  }
  if (typeof details === "object") {
    const v = details[field] ?? details[`${field}Request`];
    if (typeof v === "string") return v;
    if (Array.isArray(v)) return v.map(String).join(" ");
  }
  return null;
}

form?.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!nameInput || !saveBtn) return;
  [nameInput, yearInput, targetInput, goalInput].forEach((i) => i && setFieldError(i, null));

  const displayName = nameInput.value.trim();
  const schoolYear = yearInput?.value.trim() || null;
  const targetRaw = targetInput?.value.trim() || "";
  const studyGoal = goalInput?.value.trim() || null;

  let hasClientError = false;
  if (displayName.length < 2 || displayName.length > 80) {
    setFieldError(nameInput, "Nome deve ter de 2 a 80 caracteres.");
    hasClientError = true;
  }
  if (schoolYear && schoolYear.length > 40) {
    setFieldError(yearInput, "Ano escolar deve ter até 40 caracteres.");
    hasClientError = true;
  }
  let targetYear = null;
  if (targetRaw) {
    targetYear = Number(targetRaw);
    if (!Number.isInteger(targetYear) || targetYear < 2000 || targetYear > 2100) {
      setFieldError(targetInput, "Ano-alvo inválido (2000 a 2100).");
      hasClientError = true;
    }
  }
  if (studyGoal && studyGoal.length > 500) {
    setFieldError(goalInput, "Objetivo deve ter até 500 caracteres.");
    hasClientError = true;
  }
  if (hasClientError) {
    nameInput.focus();
    return;
  }

  setButtonLoading(saveBtn, true, "Salvando…");
  try {
    const updated = await updateProfile({ displayName, schoolYear, targetYear, studyGoal });
    renderHero(updated, null);
    renderDados(updated, null);
    renderMetas(updated, null, null);
    const firstName = (updated?.displayName || "").split(" ")[0];
    if (subtitle && firstName) {
      subtitle.textContent = `Olá, ${firstName} — este é o seu retrato como estudante: dados, desempenho, evolução e histórico.`;
    }
    toast("Dados atualizados.", "success");
  } catch (err) {
    if (err instanceof ApiError && err.status === 400) {
      const dn = fieldErrorFor(err.details, "displayname");
      const sy = fieldErrorFor(err.details, "schoolyear");
      const ty = fieldErrorFor(err.details, "targetyear");
      const sg = fieldErrorFor(err.details, "studygoal");
      if (dn) setFieldError(nameInput, dn);
      if (sy && yearInput) setFieldError(yearInput, sy);
      if (ty && targetInput) setFieldError(targetInput, ty);
      if (sg && goalInput) setFieldError(goalInput, sg);
      if (!(dn || sy || ty || sg)) {
        renderErrorSummary(errorBox, { title: friendlyMessage(err), traceId: err.traceId });
      } else {
        toast(friendlyMessage(err), "info");
      }
    } else {
      renderErrorSummary(errorBox, {
        title: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
      });
    }
  } finally {
    setButtonLoading(saveBtn, false);
  }
});

/* ---------- 2. estatísticas + nível ---------- */

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

function renderStats(stats, overview, diagnosis) {
  statsBox.textContent = "";
  if (!stats && !overview && !diagnosis) {
    const err = lastSectionErrors.stats || lastSectionErrors.overview || lastSectionErrors.diagnosis;
    if (err) {
      renderErrorWithRetry(statsBox, {
        title: "Sem dados de desempenho. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retryStatsSections,
      });
    } else {
      renderEmpty(statsBox, {
        title: "Sem dados de desempenho",
        description: "Não foi possível carregar suas estatísticas agora. Tente novamente em instantes.",
      });
    }
    return;
  }
  const total = stats?.totalAttempts ?? overview?.totalAttempts ?? diagnosis?.totalAttempts ?? 0;
  const scored = stats?.scoredAttempts ?? overview?.scoredAttempts ?? diagnosis?.scoredAttempts ?? 0;
  const correct = stats?.correct ?? overview?.correct ?? diagnosis?.correct ?? 0;
  const acc = stats?.accuracy ?? overview?.accuracy ?? diagnosis?.accuracy ?? null;
  const level = diagnosis?.overallLevel || null;
  const last = stats?.lastAttemptAt || overview?.lastAttemptAt || diagnosis?.lastAttemptAt || null;
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
      label: "Nível estimado",
      value: level ? masteryLabel(level) : "—",
      empty: !level,
      textual: true,
      accent: TILE_ACCENTS[2],
      hint: "Estimativa para você se orientar.",
    }),
  );
  statsBox.appendChild(
    statTile({
      label: "Última atividade",
      value: last ? formatDateTime(last) : "—",
      empty: !last,
      textual: true,
      accent: TILE_ACCENTS[3],
      hint: last ? "Última vez que você respondeu." : "Nenhuma tentativa ainda.",
    }),
  );
}

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

function renderLevel(stats, overview, diagnosis) {
  levelBox.textContent = "";
  const acc = stats?.accuracy ?? overview?.accuracy ?? diagnosis?.accuracy ?? null;
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
    svg.appendChild(svgNode("path", { d: arc(180, end), fill: "none", stroke: "var(--color-success-600)", "stroke-width": "18", "stroke-linecap": "round" }));
    const [dx, dy] = polar(end);
    svg.appendChild(svgNode("circle", { cx: dx.toFixed(1), cy: dy.toFixed(1), r: "10", fill: "#fff", stroke: "var(--color-success-600)", "stroke-width": "4" }));
  }
  wrap.appendChild(svg);
  const num = el("p", { className: "gauge-num", text: `${score} ` });
  num.appendChild(el("small", { text: "/100" }));
  wrap.appendChild(num);
  wrap.appendChild(el("p", { className: "gauge-cap", text: label }));
  wrap.appendChild(
    el("p", {
      className: "stat-label",
      text: pct !== null
        ? "Seu aproveitamento geral nas questões que valem nota."
        : "Responda questões para calcularmos seu nível.",
    }),
  );
  levelBox.appendChild(wrap);
}

/* ---------- 3. por modo (faixa) ---------- */

function renderModes(stats) {
  modesBox.textContent = "";
  const rows = stats?.byMode || null;
  if (!rows) {
    const err = lastSectionErrors.stats;
    if (err) {
      renderErrorWithRetry(modesBox, {
        title: "Dados indisponíveis. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retryStatsSections,
      });
    } else {
      renderEmpty(modesBox, {
        title: "Dados indisponíveis",
        description: "Não foi possível carregar este recorte agora.",
      });
    }
    return;
  }
  if (!rows.length) {
    renderEmpty(modesBox, {
      title: "Nenhuma tentativa por modo",
      description: "Responda questões nos modos Estudo, Prova ou Revisão para ver este recorte.",
    });
    return;
  }
  for (const m of rows) {
    const name = modeLabel(m.mode);
    const pct = formatPercentValue(m.accuracy) ?? 0;
    const card = el("div", { className: "focus-card", attrs: { role: "listitem" } });
    const avatar = el("span", { className: "focus-card__avatar", text: initialsOf(name), attrs: { "aria-hidden": "true" } });
    const modeKey = m.mode || "";
    avatar.style.setProperty("--fc", avatarColor(modeKey));
    card.appendChild(avatar);
    const mid = el("div", { className: "focus-card__body" });
    mid.appendChild(el("p", { className: "focus-card__name", text: name }));
    mid.appendChild(el("p", { className: "focus-card__value", text: formatPercent(m.accuracy) }));
    mid.appendChild(
      el("p", {
        className: "stat-label",
        text: `${m.scored ?? 0} pontuáveis · ${m.attempts ?? 0} respondidas`,
      }),
    );
    const meter = el("div", {
      className: "mode-meter",
      attrs: {
        role: "progressbar",
        "aria-valuenow": String(pct),
        "aria-valuemin": "0",
        "aria-valuemax": "100",
        "aria-label": `Aproveitamento no modo ${name}`,
      },
    });
    const fill = el("div", { className: "mode-meter__fill" });
    fill.style.width = `${pct}%`;
    meter.appendChild(fill);
    mid.appendChild(meter);
    card.appendChild(mid);
    modesBox.appendChild(card);
  }
}

/* ---------- por disciplina (barras) ---------- */

function renderDisciplines(stats, overview) {
  discBox.textContent = "";
  const rows = stats?.byDiscipline ?? overview?.byDiscipline ?? null;
  if (!rows) {
    const err = lastSectionErrors.stats || lastSectionErrors.overview;
    if (err) {
      renderErrorWithRetry(discBox, {
        title: "Dados indisponíveis. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retryStatsSections,
      });
    } else {
      renderEmpty(discBox, {
        title: "Dados indisponíveis",
        description: "Não foi possível carregar este recorte agora.",
      });
    }
    return;
  }
  if (!rows.length) {
    renderEmpty(discBox, {
      title: "Nenhuma tentativa por disciplina",
      description: "Responda questões para ver seu aproveitamento em Língua Portuguesa e Matemática.",
    });
    return;
  }
  const palette = ["linear-gradient(180deg,#e2571e,#b83e06)", "linear-gradient(180deg,#f5c518,#d99a00)", "linear-gradient(180deg,#3d8fd1,#0f5084)"];
  const bars = el("div", { className: "alloc-bars", attrs: { role: "img", "aria-label": "Aproveitamento por disciplina em barras" } });
  rows.slice(0, 4).forEach((d, i) => {
    const pct = formatPercentValue(d.accuracy) ?? 0;
    const cell = el("div", { className: "alloc-bar" });
    const track = el("div", { className: "alloc-bar__track" });
    const fill = el("div", { className: "alloc-bar__fill", text: `${pct}%` });
    fill.style.setProperty("--ab", palette[i % palette.length]);
    fill.style.height = `${Math.max(18, pct)}%`;
    /* Mesmo ajuste do dashboard: amarelo (índice 1) e azul-claro
     * (índice 2) com texto navy; só o laranja mantém o branco. */
    if (i === 1) fill.style.color = "#0d2b45";
    else if (i === 2) fill.style.color = "#081f33";
    track.appendChild(fill);
    cell.appendChild(track);
    cell.appendChild(el("p", { className: "alloc-bar__name", text: d.disciplineName || disciplineLabel(d.disciplineCode) }));
    cell.appendChild(el("p", { className: "alloc-bar__meta", text: `${d.correct ?? 0}/${d.scored ?? d.attempts ?? 0} acertos` }));
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
        text: d.disciplineName || disciplineLabel(d.disciplineCode),
      }),
    );
    top.appendChild(
      el("span", {
        className: pct === null ? "disc-item__value disc-item__value--empty" : "disc-item__value",
        text: formatPercent(d.accuracy),
      }),
    );
    li.appendChild(top);
    if (pct !== null) {
      const track = el("div", {
        className: "disc-item__track",
        attrs: {
          role: "progressbar",
          "aria-valuenow": String(pct),
          "aria-valuemin": "0",
          "aria-valuemax": "100",
          "aria-label": `Aproveitamento em ${d.disciplineName || disciplineLabel(d.disciplineCode)}`,
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
        text: `${d.correct ?? 0} acertos em ${d.scored ?? d.attempts ?? 0} pontuáveis`,
      }),
    );
    list.appendChild(li);
  }
  discBox.appendChild(list);
}

/* ---------- 4. metas ---------- */

function renderMetas(profile, plan, planError) {
  metasBox.textContent = "";
  if (!profile && planError) {
    renderErrorWithRetry(metasBox, {
      title: "Metas indisponíveis. ",
      description: friendlyMessage(planError),
      traceId: planError instanceof ApiError ? planError.traceId : null,
      onRetry: retryProfileSection,
    });
    return;
  }
  const items = plan?.items || plan?.recommendations || [];
  const done = items.filter((i) => i.status === "DONE").length;

  if (profile?.studyGoal) {
    metasBox.appendChild(el("p", { className: "meta-goal", text: profile.studyGoal }));
  } else {
    metasBox.appendChild(el("p", { className: "meta-goal", text: "Defina seu objetivo" }));
  }

  const chips = el("div", { className: "meta-chips" });
  chips.appendChild(el("span", { className: "badge", text: `Ano-alvo: ${profile?.targetYear ?? "—"}` }));
  chips.appendChild(el("span", { className: "badge", text: `Ano escolar: ${profile?.schoolYear || "—"}` }));
  metasBox.appendChild(chips);

  if (items.length) {
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
    fill.style.width = `${Math.round((done / items.length) * 100)}%`;
    bar.appendChild(fill);
    progress.appendChild(bar);
    progress.appendChild(
      el("p", { className: "stat-label", text: `Roteiro: ${done} de ${items.length} concluídos` }),
    );
    metasBox.appendChild(progress);
  } else {
    metasBox.appendChild(
      el("p", { className: "stat-label", text: "Nenhum roteiro vigente. Monte o seu no dashboard." }),
    );
  }

  if (!profile?.studyGoal && !profile?.targetYear) {
    metasBox.appendChild(
      el("p", {
        className: "stat-label",
        text: "Defina objetivo e ano-alvo em “Seus dados” — são as suas metas declaradas nesta versão.",
      }),
    );
  }
  metasBox.appendChild(
    el("p", {
      className: "stat-label",
      text: "Metas com número (sequência de estudos, pontuação) ainda não estão disponíveis.",
    }),
  );
  metasBox.appendChild(
    el("a", { className: "btn btn--secondary btn--sm mt-4", text: "Ver roteiro no dashboard", attrs: { href: "./dashboard.html" } }),
  );
}

/* ---------- 5. dominados + atenção ---------- */

function topicTitle(item) {
  return (
    item?.topicName ||
    topicLabel(item?.topicCode) ||
    `Assunto #${item?.topicId ?? "?"}`
  );
}

function topicMeta(item) {
  const parts = [];
  if (item?.disciplineName) parts.push(item.disciplineName);
  else {
    const discLabel = item?.disciplineCode ? disciplineLabel(item.disciplineCode) : "";
    if (discLabel) parts.push(discLabel);
  }
  const topic = topicLabel(item?.topicCode);
  if (topic) parts.push(topic);
  return parts.join(" · ");
}

function practiceLink(item) {
  if (item?.topicId === null || item?.topicId === undefined) return null;
  return el("a", {
    className: "btn btn--ghost btn--sm",
    text: "Praticar",
    attrs: { href: `./estudos.html?topico=${encodeURIComponent(String(item.topicId))}` },
  });
}

function renderTopicList(container, items, { emptyTitle, emptyDescription, badgeFor }) {
  container.textContent = "";
  if (!items) {
    const err = lastSectionErrors.diagnosis;
    if (err) {
      renderErrorWithRetry(container, {
        title: "Diagnóstico indisponível. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retryStatsSections,
      });
    } else {
      renderEmpty(container, { title: "Diagnóstico indisponível", description: "Não foi possível carregar este recorte agora." });
    }
    return;
  }
  if (!items.length) {
    renderEmpty(container, { title: emptyTitle, description: emptyDescription });
    return;
  }
  const list = el("ol", { className: "mastery-list" });
  for (const item of items) {
    const li = el("li", { className: "mastery-list__item" });
    const top = el("div", { className: "mastery-list__top" });
    top.appendChild(el("strong", { text: topicTitle(item) }));
    const level = badgeFor(item);
    top.appendChild(
      el("span", { className: `badge ${masteryBadge(level)}`.trim(), text: `${formatPercent(item.accuracy)} · ${item.scored ?? 0} pontuáveis · ${masteryLabel(level)}` }),
    );
    li.appendChild(top);
    li.appendChild(el("p", { className: "mastery-list__reason", text: translateTopicCodes(item.reason) || "Motivo auditável no diagnóstico." }));
    const metaText = topicMeta(item);
    const foot = el("div", { className: "insight-foot" });
    if (metaText) foot.appendChild(el("span", { className: "badge", text: metaText }));
    const link = practiceLink(item);
    if (link) foot.appendChild(link);
    if (foot.children.length) li.appendChild(foot);
    list.appendChild(li);
  }
  container.appendChild(list);
}

function renderStrengths(diagnosis) {
  if (!diagnosis) {
    renderTopicList(strengthsBox, null, {});
    return;
  }
  renderTopicList(strengthsBox, diagnosis.strengths || [], {
    emptyTitle: "Nenhum conteúdo dominado ainda",
    emptyDescription: "Assuntos com ≥ 70% de aproveitamento e sinal suficiente (3+ pontuáveis) aparecem aqui. Continue praticando.",
    badgeFor: () => "DOMINADO",
  });
}

function renderWeaknesses(diagnosis) {
  weaknessesBox.textContent = "";
  if (!diagnosis) {
    const err = lastSectionErrors.diagnosis;
    if (err) {
      renderErrorWithRetry(weaknessesBox, {
        title: "Diagnóstico indisponível. ",
        description: friendlyMessage(err),
        traceId: err instanceof ApiError ? err.traceId : null,
        onRetry: retryStatsSections,
      });
    } else {
      renderEmpty(weaknessesBox, {
        title: "Diagnóstico indisponível",
        description: "Não foi possível carregar seus pontos de atenção agora.",
      });
    }
    return;
  }
  const weak = diagnosis.weaknesses || [];
  const prio = (diagnosis.priorities || []).slice(0, 5);
  if (!weak.length && !prio.length) {
    const box = el("div", { className: "alert alert--success", attrs: { role: "status" } });
    const inner = el("div");
    inner.appendChild(el("strong", { text: "Nada pendente por aqui." }));
    inner.appendChild(
      el("p", { text: "Nenhum ponto fraco e nenhuma prioridade aberta. Gere o roteiro no dashboard para manter a revisão." }),
    );
    box.appendChild(inner);
    weaknessesBox.appendChild(box);
    return;
  }
  if (weak.length) {
    weaknessesBox.appendChild(el("h3", { text: "Pontos fracos (< 50% com sinal suficiente)" }));
    const wrap = el("div", { className: "mt-2" });
    renderTopicList(wrap, weak, {
      emptyTitle: "Sem pontos fracos",
      emptyDescription: "Nenhum assunto abaixo de 50% com sinal suficiente.",
      badgeFor: () => "FRAGIL",
    });
    weaknessesBox.appendChild(wrap);
  }
  if (prio.length) {
    weaknessesBox.appendChild(el("h3", { text: "Fila de prioridades (top 5)" }));
    const list = el("ol", { className: "mastery-list mt-2" });
    for (const p of prio) {
      const li = el("li", { className: "mastery-list__item" });
      const top = el("div", { className: "mastery-list__top" });
      top.appendChild(el("span", { text: `#${p.rank}`, className: "priority-list__rank" }));
      top.appendChild(el("strong", { text: topicTitle(p) }));
      const level = (p.scored ?? 0) === 0 ? "NAO_AVALIADO" : "EM_DESENVOLVIMENTO";
      top.appendChild(
        el("span", { className: `badge ${masteryBadge(level)}`.trim(), text: `${formatPercent(p.accuracy)} · ${p.scored ?? 0} pontuáveis` }),
      );
      li.appendChild(top);
      li.appendChild(el("p", { className: "mastery-list__reason", text: translateTopicCodes(p.reason) || "Motivo auditável no diagnóstico." }));
      const link = practiceLink(p);
      if (link) {
        const foot = el("div", { className: "insight-foot" });
        foot.appendChild(link);
        li.appendChild(foot);
      }
      list.appendChild(li);
    }
    weaknessesBox.appendChild(list);
  }
}

/* ---------- 6. evolução ---------- */

evoSelect?.addEventListener("change", async () => {
  evoBox.textContent = "";
  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Atualizando evolução…" }));
  evoBox.appendChild(loading);
  try {
    const data = await fetchEvolution(evoSelect.value);
    renderEvolution(data, null);
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
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
  const grad = svgNode("linearGradient", { id: "perfil-evo-fill", x1: "0", y1: "0", x2: "0", y2: "1" });
  grad.appendChild(svgNode("stop", { offset: "0%", "stop-color": "#ffffff", "stop-opacity": "0.45" }));
  grad.appendChild(svgNode("stop", { offset: "100%", "stop-color": "#ffffff", "stop-opacity": "0.02" }));
  defs.appendChild(grad);
  svg.appendChild(defs);
  for (const g of [25, 50, 75]) {
    const gy = PAD.t + ih - (g / 100) * ih;
    svg.appendChild(svgNode("line", { x1: PAD.l, y1: gy, x2: W - PAD.r, y2: gy, stroke: "#ffffff", "stroke-opacity": "0.25", "stroke-dasharray": "4 6" }));
  }
  const linePts = vals.map((v, i) => `${x(i).toFixed(1)},${y(v).toFixed(1)}`).join(" ");
  svg.appendChild(svgNode("polygon", {
    points: `${PAD.l},${(PAD.t + ih).toFixed(1)} ${linePts} ${(W - PAD.r).toFixed(1)},${(PAD.t + ih).toFixed(1)}`,
    fill: "url(#perfil-evo-fill)",
  }));
  svg.appendChild(svgNode("polyline", {
    points: linePts, fill: "none", stroke: "#ffe08a",
    "stroke-width": "3", "stroke-linecap": "round", "stroke-linejoin": "round",
  }));
  const max = Math.max(...vals);
  vals.forEach((v, i) => {
    svg.appendChild(svgNode("circle", {
      cx: x(i).toFixed(1), cy: y(v).toFixed(1), r: v === max ? "5" : "3.5",
      fill: "#fff", stroke: "#b83e06", "stroke-width": "2",
    }));
  });
  const labelIdx = buckets.length <= 5
    ? buckets.map((_, i) => i)
    : [0, Math.floor(buckets.length / 2), buckets.length - 1];
  for (const i of labelIdx) {
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
      title: "Evolução indisponível. ",
      description: friendlyMessage(loadError),
      traceId: loadError instanceof ApiError ? loadError.traceId : null,
      onRetry: retryEvolutionSection,
    });
    return;
  }
  const buckets = data?.buckets || [];
  if (!buckets.length) {
    if (evoHeadline) evoHeadline.textContent = "Sem dados ainda";
    evoBox.appendChild(
      el("p", { className: "perf-chart__sub", text: "Responda questões para ver sua curva de aproveitamento por período." }),
    );
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

/* ---------- 7. histórico ---------- */

function historyTitle(item) {
  if (item?.sourceYear && item?.sourceNumber) return `Questão ${item.sourceYear} Nº ${item.sourceNumber}`;
  return `Questão #${item?.questionId ?? "?"}`;
}

function historyResult(item) {
  if (item?.wasAnnulled) return { label: "Anulada", cls: "badge--warning" };
  if (item?.isCorrect === true) return { label: "Acertou", cls: "badge--success" };
  if (item?.isCorrect === false) return { label: "Errou", cls: "badge--danger" };
  return { label: "Registrada", cls: "" };
}

function historyItemNode(item) {
  const li = el("li", { className: "history-list__item" });
  const left = el("div");
  const link = el("a", {
    className: "history-list__title",
    text: historyTitle(item),
    attrs: { href: `./questao.html?id=${encodeURIComponent(String(item.questionId))}` },
  });
  left.appendChild(link);
  const bits = [
    item.disciplineName || item.disciplineCode || "—",
    modeLabel(item.mode),
    `marcou ${item.selectedOption || "—"}`,
  ];
  const spent = formatTimeSpent(item.timeSpentSeconds);
  if (spent) bits.push(spent);
  bits.push(formatDateTime(item.answeredAt));
  left.appendChild(el("p", { className: "history-list__meta", text: bits.join(" · ") }));
  const right = el("div", { className: "cluster" });
  const res = historyResult(item);
  right.appendChild(el("span", { className: `badge ${res.cls}`.trim(), text: res.label }));
  li.appendChild(left);
  li.appendChild(right);
  return li;
}

function renderHistoryPage(page, loadError, { reset = false } = {}) {
  if (reset) historyBox.textContent = "";
  if (loadError) {
    if (reset) {
      renderErrorWithRetry(historyBox, {
        title: "Histórico indisponível. ",
        description: friendlyMessage(loadError),
        traceId: loadError instanceof ApiError ? loadError.traceId : null,
        onRetry: retryHistorySection,
      });
    }
    if (moreBtn) moreBtn.hidden = true;
    return;
  }
  if (!page) {
    if (reset) renderEmpty(historyBox, { title: "Histórico indisponível", description: "Não foi possível carregar seu histórico agora." });
    if (moreBtn) moreBtn.hidden = true;
    return;
  }
  historyTotal = page.totalElements ?? historyTotal;
  const items = page.content || [];
  if (reset && !items.length) {
    renderEmpty(historyBox, {
      title: "Nenhuma tentativa ainda",
      description: "Suas respostas nos modos Estudo, Prova e Revisão aparecem aqui, das mais recentes para as mais antigas.",
    });
    historyBox.appendChild(
      el("a", { className: "btn btn--primary btn--sm mt-4", text: "Praticar na área de estudos", attrs: { href: "./estudos.html" } }),
    );
    if (historyCount) historyCount.textContent = "Nenhuma tentativa registrada ainda.";
    if (moreBtn) moreBtn.hidden = true;
    historyExhausted = true;
    return;
  }
  let list = historyBox.querySelector("ol.history-list");
  if (!list) {
    list = el("ol", { className: "history-list" });
    historyBox.appendChild(list);
  }
  for (const item of items) list.appendChild(historyItemNode(item));
  const shown = list.children.length;
  if (historyCount) {
    historyCount.textContent =
      historyTotal !== null && historyTotal !== undefined
        ? `${shown} de ${historyTotal} tentativas (mais recentes primeiro).`
        : `${shown} tentativas (mais recentes primeiro).`;
  }
  historyExhausted = page.last === true || items.length === 0;
  if (moreBtn) moreBtn.hidden = historyExhausted;
}

moreBtn?.addEventListener("click", async () => {
  setButtonLoading(moreBtn, true, "Carregando…");
  try {
    const next = await fetchHistory(historyPage + 1, HISTORY_SIZE);
    historyPage += 1;
    renderHistoryPage(next, null, {});
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    toast(friendlyMessage(err), "info");
  } finally {
    setButtonLoading(moreBtn, false);
  }
});

/* ---------- 8. conquistas ---------- */

function renderAchievements() {
  achievementsBox.textContent = "";
  const box = el("div", { className: "achv-locked" });
  box.appendChild(el("span", { className: "achv-locked__icon", text: "★", attrs: { "aria-hidden": "true" } }));
  box.appendChild(el("h3", { text: "Conquistas em construção" }));
  box.appendChild(
    el("p", {
      text: "Pontuação, conquistas e sequência de estudos ainda não estão disponíveis. Nada é exibido como selo ou ponto antes de existir uma regra clara por trás.",
    }),
  );
  box.appendChild(
    el("a", {
      className: "btn btn--secondary btn--sm",
      text: "Acompanhar progresso no dashboard",
      attrs: { href: "./dashboard.html" },
    }),
  );
  achievementsBox.appendChild(box);
}

/* ---------- notas ---------- */
