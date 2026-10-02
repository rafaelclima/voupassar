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
import { restoreSession, logout } from "../api/auth.js";
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
  setButtonLoading,
  setFieldError,
  toast,
} from "../components/ui.js";
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
const dadosBox = document.getElementById("perfil-dados");
const form = document.getElementById("perfil-form");
const nameInput = document.getElementById("pf-nome");
const yearInput = document.getElementById("pf-ano");
const targetInput = document.getElementById("pf-alvo");
const goalInput = document.getElementById("pf-objetivo");
const goalCount = document.getElementById("pf-objetivo-count");
const saveBtn = document.getElementById("perfil-save");
const statsBox = document.getElementById("perfil-stats");
const modesBox = document.getElementById("perfil-modes");
const discBox = document.getElementById("perfil-disciplines");
const metasBox = document.getElementById("perfil-metas");
const strengthsBox = document.getElementById("perfil-strengths");
const weaknessesBox = document.getElementById("perfil-weaknesses");
const evoSelect = document.getElementById("evo-granularity");
const evoBox = document.getElementById("perfil-evolution");
const historyBox = document.getElementById("perfil-history");
const historyCount = document.getElementById("perfil-history-count");
const moreBtn = document.getElementById("perfil-more");
const achievementsBox = document.getElementById("perfil-achievements");

let overviewCache = null;
let diagnosisCache = null;
let statsCache = null;
let historyPage = 0;
const HISTORY_SIZE = 20;
let historyTotal = null;
let historyExhausted = false;

wireLogoutButtons();
wireGoalCounter();

main();

async function main() {
  const user = await restoreSession().catch(() => null);
  if (!user) {
    showGuard();
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
  guard.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: "Entre para ver seu perfil" }));
  box.appendChild(
    el("p", {
      text: "O perfil mostra seus dados, estatísticas e histórico. Ele precisa da sua sessão — entre ou crie uma conta para continuar.",
    }),
  );
  const actions = el("div", { className: "btn-group", attrs: { style: "justify-content:center" } });
  const login = el("a", {
    className: "btn btn--primary",
    text: "Entrar",
    attrs: { href: "./login.html?next=perfil.html" },
  });
  const register = el("a", {
    className: "btn btn--secondary",
    text: "Criar conta",
    attrs: { href: "./cadastro.html" },
  });
  actions.appendChild(login);
  actions.appendChild(register);
  box.appendChild(actions);
  guard.appendChild(box);
}

/* ---------- formato ---------- */

function formatPercent(acc) {
  if (acc === null || acc === undefined) return "—";
  const n = Number(acc);
  if (!Number.isFinite(n)) return "—";
  return `${(n * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
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
  return new Date(Date.UTC(y, m - 1, d)).toLocaleDateString("pt-BR", { timeZone: "UTC" });
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

  if (profileData) {
    subtitle.textContent = `Olá, ${profileData.displayName || "estudante"} — seus dados, estatísticas, evolução, histórico, conteúdos dominados e pontos de atenção.`;
  }

  renderDados(profileData, profile.status === "rejected" ? profile.reason : null);
  renderStats(statsCache, overviewCache, diagnosisCache);
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

/* ---------- 1. dados ---------- */

function renderDados(profile, loadError) {
  dadosBox.textContent = "";
  if (loadError || !profile) {
    renderEmpty(dadosBox, {
      title: "Dados indisponíveis",
      description: loadError ? friendlyMessage(loadError) : "Não foi possível carregar seus dados agora.",
    });
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
    renderDados(updated, null);
    renderMetas(updated, null, null);
    if (subtitle && updated?.displayName) {
      subtitle.textContent = `Olá, ${updated.displayName} — seus dados, estatísticas, evolução, histórico, conteúdos dominados e pontos de atenção.`;
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

/* ---------- 2. estatísticas ---------- */

function statCard({ label, value, hint }) {
  const card = el("article", { className: "card", attrs: { role: "listitem" } });
  const body = el("div", { className: "card__body" });
  const v = el("div", { className: "stat-value" });
  v.textContent = value;
  if (value === "—") v.classList.add("stat-value--empty");
  body.appendChild(v);
  body.appendChild(el("div", { className: "stat-label", text: label }));
  if (hint) {
    const h = el("p", { className: "muted" });
    h.appendChild(el("small", { text: hint }));
    body.appendChild(h);
  }
  card.appendChild(body);
  return card;
}

function renderStats(stats, overview, diagnosis) {
  statsBox.textContent = "";
  if (!stats && !overview && !diagnosis) {
    renderEmpty(statsBox, {
      title: "Sem dados de desempenho",
      description: "Não foi possível carregar suas estatísticas agora. Tente novamente em instantes.",
    });
    return;
  }
  const total = stats?.totalAttempts ?? overview?.totalAttempts ?? diagnosis?.totalAttempts ?? 0;
  const scored = stats?.scoredAttempts ?? overview?.scoredAttempts ?? diagnosis?.scoredAttempts ?? 0;
  const correct = stats?.correct ?? overview?.correct ?? diagnosis?.correct ?? 0;
  const acc = stats?.accuracy ?? overview?.accuracy ?? diagnosis?.accuracy ?? null;
  const level = diagnosis?.overallLevel || "Ainda não informado";
  const last = stats?.lastAttemptAt || overview?.lastAttemptAt || diagnosis?.lastAttemptAt || null;

  statsBox.appendChild(
    statCard({
      label: "aproveitamento geral (pontuáveis)",
      value: formatPercent(acc),
      hint: acc === null ? "Responda questões para calcular." : `${correct} acertos em ${scored} ${scored === 1 ? "questão" : "questões"}.`,
    }),
  );
  statsBox.appendChild(
    statCard({
      label: "questões respondidas",
      value: String(total),
      hint: scored === total ? "Todas pontuáveis." : `${total - scored} anulada(s) fora do cálculo (regra desconhecida).`,
    }),
  );
  statsBox.appendChild(
    statCard({
      label: "nível estimado",
      value: masteryLabel(level),
      hint: "Estimativa inicial por limiares explícitos — nunca verdade oficial do IFRN.",
    }),
  );
  statsBox.appendChild(
    statCard({
      label: "última atividade",
      value: last ? formatDateTime(last) : "—",
      hint: last ? "Horário UTC registrado pelo servidor." : "Nenhuma tentativa registrada ainda.",
    }),
  );
}

function statsTable(container, { caption, columns, rows, emptyTitle, emptyDescription }) {
  container.textContent = "";
  if (!rows) {
    renderEmpty(container, { title: "Dados indisponíveis", description: "Não foi possível carregar este recorte agora." });
    return;
  }
  if (!rows.length) {
    renderEmpty(container, { title: emptyTitle, description: emptyDescription });
    return;
  }
  const wrap = el("div", {
    className: "table-wrap",
    attrs: { tabindex: "0", role: "region", "aria-label": caption },
  });
  const table = el("table", { className: "table" });
  table.appendChild(el("caption", { text: caption }));
  const thead = el("thead");
  const hr = el("tr");
  for (const [text, num] of columns) {
    const th = el("th", { text });
    if (num) th.classList.add("num");
    th.setAttribute("scope", "col");
    hr.appendChild(th);
  }
  thead.appendChild(hr);
  table.appendChild(thead);
  const tbody = el("tbody");
  for (const cells of rows) {
    const tr = el("tr");
    cells.forEach(([text, opts], idx) => {
      const cell = idx === 0 ? el("th", { text }) : el("td", { text });
      if (idx === 0) cell.setAttribute("scope", "row");
      if (opts?.num) cell.classList.add("num");
      tr.appendChild(cell);
    });
    tbody.appendChild(tr);
  }
  table.appendChild(tbody);
  wrap.appendChild(table);
  container.appendChild(wrap);
}

function renderModes(stats) {
  statsTable(modesBox, {
    caption: "Aproveitamento por modo de resposta",
    columns: [["Modo", false], ["Respondidas", true], ["Pontuáveis", true], ["Corretas", true], ["Aproveitamento", true]],
    rows: stats
      ? (stats.byMode || []).map((m) => [
          [modeLabel(m.mode), {}],
          [String(m.attempts ?? 0), { num: true }],
          [String(m.scored ?? 0), { num: true }],
          [String(m.correct ?? 0), { num: true }],
          [formatPercent(m.accuracy), { num: true }],
        ])
      : null,
    emptyTitle: "Nenhuma tentativa por modo",
    emptyDescription: "Responda questões nos modos Estudo, Prova ou Revisão para ver este recorte.",
  });
}

function renderDisciplines(stats, overview) {
  const rows = stats?.byDiscipline ?? overview?.byDiscipline ?? null;
  statsTable(discBox, {
    caption: "Seu aproveitamento em cada disciplina",
    columns: [["Disciplina", false], ["Respondidas", true], ["Pontuáveis", true], ["Corretas", true], ["Aproveitamento", true]],
    rows: rows
      ? rows.map((d) => [
          [d.disciplineName || d.disciplineCode, {}],
          [String(d.attempts ?? 0), { num: true }],
          [String(d.scored ?? 0), { num: true }],
          [String(d.correct ?? 0), { num: true }],
          [formatPercent(d.accuracy), { num: true }],
        ])
      : null,
    emptyTitle: "Nenhuma tentativa por disciplina",
    emptyDescription: "Responda questões para ver seu aproveitamento em Língua Portuguesa e Matemática.",
  });
}

/* ---------- 3. metas ---------- */

function renderMetas(profile, plan, planError) {
  metasBox.textContent = "";
  if (!profile && planError) {
    renderEmpty(metasBox, { title: "Metas indisponíveis", description: friendlyMessage(planError) });
    return;
  }
  const items = plan?.items || plan?.recommendations || [];
  const done = items.filter((i) => i.status === "DONE").length;
  const card = el("div", { className: "card" });
  const body = el("div", { className: "card__body" });
  body.appendChild(el("p", { text: `Objetivo: ${profile?.studyGoal || "—"}` }));
  const meta = el("p", { className: "muted" });
  meta.appendChild(
    el("small", {
      text: `Ano-alvo: ${profile?.targetYear ?? "—"} · Ano escolar: ${profile?.schoolYear || "—"} · Roteiro: ${items.length ? `${done} de ${items.length} concluídos` : "nenhum roteiro vigente"}`,
    }),
  );
  body.appendChild(meta);
  if (!profile?.studyGoal && !profile?.targetYear) {
    const hint = el("p", { className: "muted" });
    hint.appendChild(el("small", { text: "Defina objetivo e ano-alvo no formulário de dados acima — são as suas metas declaradas nesta versão." }));
    body.appendChild(hint);
  }
  const note = el("p", { className: "muted" });
  note.appendChild(
    el("small", {
      text: "Metas com número (sequência de estudos, pontuação) ainda não estão disponíveis.",
    }),
  );
  body.appendChild(note);
  const actions = el("div", { className: "btn-group" });
  actions.appendChild(
    el("a", { className: "btn btn--secondary btn--sm", text: "Ver roteiro no dashboard", attrs: { href: "./dashboard.html" } }),
  );
  body.appendChild(actions);
  card.appendChild(body);
  metasBox.appendChild(card);
}

/* ---------- 4. dominados + 5. atenção ---------- */

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

function renderTopicList(container, items, { emptyTitle, emptyDescription, badgeFor }) {
  container.textContent = "";
  if (!items) {
    renderEmpty(container, { title: "Diagnóstico indisponível", description: "Não foi possível carregar este recorte agora." });
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
    if (metaText) {
      const meta = el("p", { className: "muted" });
      meta.appendChild(el("small", { text: metaText }));
      li.appendChild(meta);
    }
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
    renderEmpty(weaknessesBox, {
      title: "Diagnóstico indisponível",
      description: "Não foi possível carregar seus pontos de atenção agora.",
    });
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

function renderEvolution(data, loadError) {
  evoBox.textContent = "";
  if (loadError) {
    renderEmpty(evoBox, { title: "Evolução indisponível", description: friendlyMessage(loadError) });
    return;
  }
  const buckets = data?.buckets || [];
  if (!buckets.length) {
    renderEmpty(evoBox, {
      title: "Sem evolução ainda",
      description: "Responda questões para ver sua curva de aproveitamento por período.",
    });
    return;
  }
  const list = el("ol", { className: "evo-bars" });
  for (const b of buckets) {
    const row = el("li", { className: "evo-bars__row" });
    row.appendChild(el("span", { className: "evo-bars__label", text: formatBucketDate(b.bucketStart) }));
    const bar = el("div", {
      className: "progress",
      attrs: { role: "progressbar", "aria-valuenow": String(Math.round((b.accuracy ?? 0) * 100)), "aria-valuemin": "0", "aria-valuemax": "100", "aria-label": `Aproveitamento em ${formatBucketDate(b.bucketStart)}` },
    });
    const fill = el("div", { className: "progress__bar" });
    fill.style.width = `${Math.round((b.accuracy ?? 0) * 100)}%`;
    bar.appendChild(fill);
    row.appendChild(bar);
    row.appendChild(
      el("span", { className: "evo-bars__value", text: `${formatPercent(b.accuracy)} · ${b.correct}/${b.scored}` }),
    );
    list.appendChild(row);
  }
  evoBox.appendChild(list);
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
    if (reset) renderEmpty(historyBox, { title: "Histórico indisponível", description: friendlyMessage(loadError) });
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
  const box = el("div", { className: "empty" });
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

