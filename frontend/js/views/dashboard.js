/* VouPassar — dashboard (TASK 6.4)
 * Página protegida: exige sessão (restoreSession). Sem sessão → painel de
 * acesso com link seguro para login (?next=dashboard.html). Com sessão,
 * busca em paralelo: overview (4.1), diagnosis (4.2), plan (4.3/4.4),
 * topics (3.3, para nomear itens do plano), simulations (5.1) e evolution
 * (4.1, com seletor DAY/WEEK/MONTH). Cada seção falha de forma isolada:
 * o restante continua visível. Sem innerHTML (só textContent via el()).
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { restoreSession, logout } from "../api/auth.js";
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
import { el, renderEmpty, renderErrorSummary, setButtonLoading, toast } from "../components/ui.js";

const guard = document.getElementById("dash-guard");
const errorBox = document.getElementById("dash-error");
const loadingBox = document.getElementById("dash-loading");
const content = document.getElementById("dash-content");
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
const simBox = document.getElementById("dash-simulations");
const notesBox = document.getElementById("dash-notes");

let topicById = new Map();
let currentPlan = null;
let overviewCache = null;
let diagnosisCache = null;

wireLogoutButtons();

main();

async function main() {
  const user = await restoreSession().catch(() => null);
  if (!user) {
    showGuard();
    return;
  }
  subtitle.textContent = `Olá, ${user.displayName || "estudante"} — onde você está, o que precisa estudar, como está evoluindo e o que fazer agora.`;
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
  guard.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: "Entre para ver seu dashboard" }));
  box.appendChild(
    el("p", {
      text: "O dashboard mostra seu desempenho e roteiro. Ele precisa da sua sessão — entre ou crie uma conta para continuar.",
    }),
  );
  const actions = el("div", { className: "btn-group", attrs: { style: "justify-content:center" } });
  const login = el("a", {
    className: "btn btn--primary",
    text: "Entrar",
    attrs: { href: "./login.html?next=dashboard.html" },
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

function statusLabel(status) {
  const map = {
    SUBMITTED: "Concluído",
    IN_PROGRESS: "Em andamento",
    ABANDONED: "Abandonado",
    TODO: "A fazer",
    DOING: "Em andamento",
    DONE: "Concluído",
    SKIPPED: "Pulado",
  };
  return map[status] || String(status || "—");
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
      document.createTextNode("Sua sessão expirou. Entre novamente para ver o dashboard."),
    );
    return;
  }

  const failures = [overview, diagnosis, topics, simulations, evolution]
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

  if (topics.status === "fulfilled") {
    topicById = new Map(
      (topics.value || []).map((t) => [Number(t.id), t]),
    );
  }

  overviewCache = overview.status === "fulfilled" ? overview.value : null;
  diagnosisCache = diagnosis.status === "fulfilled" ? diagnosis.value : null;

  renderStats(overviewCache, diagnosisCache);
  renderDisciplines(overviewCache);
  renderPriorities(diagnosisCache);
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
  renderNotes(overviewCache, diagnosisCache);

  loadingBox.hidden = true;
  content.hidden = false;
}

/* ---------- 1. resumo ---------- */

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

function renderStats(overview, diagnosis) {
  statsBox.textContent = "";
  if (!overview && !diagnosis) {
    renderEmpty(statsBox, {
      title: "Sem dados de desempenho",
      description: "Não foi possível carregar seu retrato agora. Tente novamente em instantes.",
    });
    return;
  }
  const total = overview?.totalAttempts ?? diagnosis?.totalAttempts ?? 0;
  const scored = overview?.scoredAttempts ?? diagnosis?.scoredAttempts ?? 0;
  const correct = overview?.correct ?? diagnosis?.correct ?? 0;
  const acc = overview?.accuracy ?? diagnosis?.accuracy ?? null;
  const level = diagnosis?.overallLevel || "DESCONHECIDO";
  const last = overview?.lastAttemptAt || diagnosis?.lastAttemptAt || null;

  statsBox.appendChild(
    statCard({
      label: "aproveitamento geral (pontuáveis)",
      value: formatPercent(acc),
      hint: acc === null ? "Sem tentativas pontuáveis — DESCONHECIDO, nunca zero inventado." : `${correct} corretas em ${scored} pontuáveis.`,
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

/* ---------- 2. disciplinas ---------- */

function renderDisciplines(overview) {
  discBox.textContent = "";
  const rows = overview?.byDiscipline || [];
  if (!overview) {
    renderEmpty(discBox, {
      title: "Desempenho indisponível",
      description: "Não foi possível carregar o recorte por disciplina agora.",
    });
    return;
  }
  if (!rows.length) {
    renderEmpty(discBox, {
      title: "Nenhuma tentativa por disciplina",
      description: "Responda questões para ver seu aproveitamento em Língua Portuguesa e Matemática.",
    });
    return;
  }
  const wrap = el("div", {
    className: "table-wrap",
    attrs: { tabindex: "0", role: "region", "aria-label": "Aproveitamento por disciplina" },
  });
  const table = el("table", { className: "table" });
  const caption = el("caption", {
    text: "Aproveitamento por disciplina (factual, via discipline_id da questão)",
  });
  table.appendChild(caption);
  const thead = el("thead");
  const hr = el("tr");
  for (const [text, num] of [["Disciplina", false], ["Respondidas", true], ["Pontuáveis", true], ["Corretas", true], ["Aproveitamento", true]]) {
    const th = el("th", { text });
    if (num) th.classList.add("num");
    th.setAttribute("scope", "col");
    hr.appendChild(th);
  }
  thead.appendChild(hr);
  table.appendChild(thead);
  const tbody = el("tbody");
  for (const d of rows) {
    const tr = el("tr");
    const name = el("th", { text: d.disciplineName || d.disciplineCode });
    name.setAttribute("scope", "row");
    tr.appendChild(name);
    for (const v of [d.attempts, d.scored, d.correct]) {
      const td = el("td", { className: "num", text: String(v ?? 0) });
      tr.appendChild(td);
    }
    tr.appendChild(el("td", { className: "num", text: formatPercent(d.accuracy) }));
    tbody.appendChild(tr);
  }
  table.appendChild(tbody);
  wrap.appendChild(table);
  discBox.appendChild(wrap);
}

/* ---------- 3. prioridades ---------- */

function renderPriorities(diagnosis) {
  prioBox.textContent = "";
  if (!diagnosis) {
    renderEmpty(prioBox, {
      title: "Diagnóstico indisponível",
      description: "Não foi possível carregar a fila de atenção agora.",
    });
    return;
  }
  const items = (diagnosis.priorities || []).slice(0, 5);
  if (!items.length) {
    const box = el("div", { className: "alert alert--success", attrs: { role: "status" } });
    const inner = el("div");
    inner.appendChild(el("strong", { text: "Nada pendente por aqui." }));
    inner.appendChild(
      el("p", { text: "Todos os assuntos avaliados estão dominados (>= 70% com sinal suficiente). Gere o roteiro para manter a revisão." }),
    );
    box.appendChild(inner);
    prioBox.appendChild(box);
    return;
  }
  const list = el("ol", { className: "priority-list" });
  for (const p of items) {
    const li = el("li", { className: "priority-list__item" });
    const top = el("div", { className: "priority-list__top" });
    top.appendChild(el("span", { className: "priority-list__rank", text: `#${p.rank}` }));
    top.appendChild(el("strong", { text: p.topicName || p.topicCode }));
    const badge = el("span", {
      className: `badge ${masteryBadge(levelOf(p))}`.trim(),
      text: `${formatPercent(p.accuracy)} · ${p.scored} pontuáveis`,
    });
    top.appendChild(badge);
    li.appendChild(top);
    li.appendChild(
      el("p", { className: "priority-list__reason", text: p.reason || "Motivo auditável no diagnóstico." }),
    );
    const meta = el("p", { className: "muted" });
    meta.appendChild(
      el("small", {
        text: `${p.disciplineName || p.disciplineCode} · ${p.historicalQuestions} questões no banco · ${p.editionsCount} edições`,
      }),
    );
    li.appendChild(meta);
    list.appendChild(li);
  }
  prioBox.appendChild(list);
}

function levelOf(priorityItem) {
  // A fila mistura FRÁGIL/EM_DESENVOLVIMENTO/NAO_AVALIADO/EM_OBSERVACAO;
  // o motivo textual carrega o detalhe — aqui só diferenciamos lacuna.
  if ((priorityItem?.scored ?? 0) === 0) return "NAO_AVALIADO";
  return "EM_DESENVOLVIMENTO";
}

/* ---------- 4. próximo estudo + plano ---------- */

function topicNameOf(item) {
  const t = topicById.get(Number(item?.topicId));
  if (t) return { name: t.name, meta: `${t.disciplineName || t.disciplineCode} · ${t.code}` };
  return { name: `Assunto #${item?.topicId ?? "?"}`, meta: "Nome DESCONHECIDO no catálogo — NECESSITA REVISÃO." };
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
    renderEmpty(planBox, {
      title: "Roteiro indisponível",
      description: friendlyMessage(loadError),
    });
    renderEmpty(nextBox, {
      title: "Próximo estudo desconhecido",
      description: "Não foi possível carregar o roteiro agora.",
    });
    planRegenerateBtn.hidden = true;
    return;
  }

  if (!plan) {
    planRegenerateBtn.hidden = true;
    planGenerateBtn.hidden = false;
    renderEmpty(planBox, {
      title: "Nenhum roteiro vigente",
      description: "Gere seu roteiro: o motor determinístico (v1-deterministico) ordena todos os assuntos por prioridade explicável.",
    });
    renderEmpty(nextBox, {
      title: "Nenhum próximo estudo",
      description: "Gere o roteiro para descobrir por onde começar.",
    });
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
      description: "O plano vigente não trouxe itens. Gere novamente.",
    });
  } else {
    const done = items.filter((i) => i.status === "DONE").length;
    const bar = el("div", { className: "progress", attrs: { role: "progressbar", "aria-valuenow": String(done), "aria-valuemin": "0", "aria-valuemax": String(items.length), "aria-label": "Progresso do roteiro" } });
    const fill = el("div", { className: "progress__bar" });
    fill.style.width = `${items.length ? Math.round((done / items.length) * 100) : 0}%`;
    bar.appendChild(fill);
    planBox.appendChild(bar);
    const meta = el("p", { className: "muted mt-2" });
    meta.appendChild(el("small", { text: `${done} de ${items.length} concluídos · algoritmo ${plan.algorithmVersion || "v1-deterministico"} · gerado em ${formatDateTime(plan.generatedAt)}` }));
    planBox.appendChild(meta);

    const list = el("ol", { className: "plan-items mt-4" });
    for (const item of items.slice(0, 8)) {
      const { name, meta: m } = topicNameOf(item);
      const row = el("li", { className: "plan-items__row" });
      const left = el("div");
      left.appendChild(el("span", { className: "plan-items__name", text: `P${item.priority} · ${name}` }));
      const sub = el("p", { className: "plan-items__meta", text: m });
      left.appendChild(sub);
      const right = el("div", { className: "cluster" });
      right.appendChild(el("span", { className: `badge ${statusBadge(item.status)}`.trim(), text: statusLabel(item.status) }));
      if (item.status !== "DONE") {
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
      const more = el("p", { className: "muted mt-2" });
      more.appendChild(el("small", { text: `Mostrando 8 de ${items.length} — o roteiro completo cobre toda a taxonomia observada.` }));
      planBox.appendChild(more);
    }
  }

  // Próximo estudo em destaque.
  const next = nextStudyOf(plan);
  if (!next) {
    renderEmpty(nextBox, {
      title: "Roteiro concluído",
      description: "Todos os itens estão concluídos ou pulados. Gere novamente após mais tentativas para atualizar as prioridades.",
    });
    return;
  }
  const { name, meta: m } = topicNameOf(next);
  const card = el("div", { className: "plan-next" });
  const title = el("p");
  title.appendChild(el("strong", { text: `${name}` }));
  card.appendChild(title);
  card.appendChild(el("p", { className: "plan-items__meta", text: `${m} · prioridade P${next.priority} · ${statusLabel(next.status)}` }));
  card.appendChild(el("p", { text: next.reason || "Motivo auditável no item do roteiro." }));
  const actions = el("div", { className: "btn-group" });
  if (next.status === "TODO") {
    const start = el("button", { className: "btn btn--primary btn--sm", text: "Começar agora", attrs: { type: "button" } });
    start.addEventListener("click", () => changeItemStatus(next, "DOING", start, "Em andamento — bom estudo."));
    actions.appendChild(start);
  }
  if (next.status !== "DONE") {
    const done = el("button", { className: "btn btn--secondary btn--sm", text: "Marcar como concluído", attrs: { type: "button" } });
    done.addEventListener("click", () => changeItemStatus(next, "DONE", done, "Item concluído. O próximo estudo foi atualizado."));
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
  setButtonLoading(planGenerateBtn, true, "Gerando…");
  try {
    currentPlan = await generatePlan();
    renderPlan(currentPlan, null);
    toast("Roteiro gerado a partir do seu desempenho.", "success");
  } catch (err) {
    renderErrorSummary(errorBox, { title: friendlyMessage(err), traceId: err instanceof ApiError ? err.traceId : null });
  } finally {
    setButtonLoading(planGenerateBtn, false);
  }
});

planRegenerateBtn?.addEventListener("click", async () => {
  setButtonLoading(planRegenerateBtn, true, "Gerando…");
  try {
    currentPlan = await generatePlan();
    renderPlan(currentPlan, null);
    toast("Roteiro atualizado. O anterior foi preservado no histórico.", "success");
  } catch (err) {
    renderErrorSummary(errorBox, { title: friendlyMessage(err), traceId: err instanceof ApiError ? err.traceId : null });
  } finally {
    setButtonLoading(planRegenerateBtn, false);
  }
});

/* ---------- 5. evolução ---------- */

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

/* ---------- 6. simulados ---------- */

function renderSimulations(page, loadError) {
  simBox.textContent = "";
  if (loadError) {
    renderEmpty(simBox, { title: "Simulados indisponíveis", description: friendlyMessage(loadError) });
    return;
  }
  const items = page?.content || [];
  if (!items.length) {
    renderEmpty(simBox, {
      title: "Nenhum simulado ainda",
      description: "Seus simulados por disciplina e de edição real aparecem aqui.",
    });
    simBox.appendChild(
      el("a", {
        className: "btn btn--primary btn--sm mt-4",
        text: "Fazer simulado",
        attrs: { href: "./simulado.html" },
      }),
    );
    return;
  }
  const list = el("ol", { className: "sim-list" });
  for (const s of items) {
    const li = el("li", { className: "sim-list__item" });
    const left = el("div");
    left.appendChild(el("p", { className: "sim-list__title", text: s.title || `Simulado #${s.attemptId}` }));
    left.appendChild(
      el("p", {
        className: "sim-list__meta",
        text: `${s.disciplineCode || "—"} · ${s.mode || "—"} · ${s.questionCount} questões · início ${formatDateTime(s.startedAt)}`,
      }),
    );
    const right = el("div", { className: "cluster" });
    right.appendChild(el("span", { className: `badge ${statusBadge(s.status)}`.trim(), text: statusLabel(s.status) }));
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

/* ---------- notas ---------- */

function renderNotes(overview, diagnosis) {
  notesBox.textContent = "";
  const notes = [
    ...(overview?.notes || []),
    ...(diagnosis?.notes || []),
  ];
  const fixed = [
    "Classificações de assunto são derivadas e aguardam curadoria humana (revisão PENDENTE) — nunca são verdade oficial do IFRN.",
    "A edição de 2021 não existe no acervo e jamais é preenchida com dados inventados.",
    "Questões anuladas contam como conteúdo respondido e ficam fora do aproveitamento (regra de pontuação DESCONHECIDA).",
  ];
  const seen = new Set();
  for (const n of [...notes, ...fixed]) {
    if (!n || seen.has(n)) continue;
    seen.add(n);
    const alert = el("div", { className: "alert alert--info", attrs: { role: "note" } });
    const inner = el("div");
    inner.appendChild(el("p", { text: n }));
    alert.appendChild(inner);
    notesBox.appendChild(alert);
  }
}
