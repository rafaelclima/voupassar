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
import { disciplineLabel, modeLabel, statusLabel, masteryLabel, topicLabel, plural, simulationTitle } from "../vocab.js";

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
const simBox = document.getElementById("dash-simulations");

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
  guard.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: "Entre para ver seu painel" }));
  box.appendChild(
    el("p", {
      text: "Seu painel mostra o que estudar agora e como você está indo. Entre com sua conta para continuar.",
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
  return new Date(Date.UTC(y, m - 1, d)).toLocaleDateString("pt-BR", { timeZone: "UTC" });
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

  loadingBox.hidden = true;
  content.hidden = false;
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

function statCard({ label, value, hint, empty = false, textual = false }) {
  const card = el("article", { className: "card", attrs: { role: "listitem" } });
  const body = el("div", { className: "card__body" });
  body.appendChild(el("p", { className: "eyebrow", text: label }));
  const v = el("div", { className: "stat-value", text: value });
  if (empty) v.classList.add("stat-value--empty");
  // Rótulo em vez de número ("Ainda sem dados") não deve usar o corpo
  // gigante dos números, ou quebra em duas linhas e desalinha os cartões.
  if (textual) v.classList.add("stat-value--text");
  body.appendChild(v);
  if (hint) {
    const h = el("p", { className: "stat-label" });
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
      title: "Não deu para carregar seu progresso",
      description: "Tente novamente em alguns instantes.",
    });
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
    statCard({
      label: "Aproveitamento",
      value: formatPercent(acc),
      empty: acc === null,
      hint:
        acc === null
          ? "Responda questões para calcular."
          : `${correct} acertos em ${scored} ${scored === 1 ? "questão" : "questões"}.`,
    }),
  );
  statsBox.appendChild(
    statCard({
      label: "Questões respondidas",
      value: String(total),
      hint: notScored ? `${notScored} sem valer nota.` : "Todas valem nota.",
    }),
  );
statsBox.appendChild(
    statCard({
      label: "Seu nível",
      value: masteryLabel(level),
      empty: !diagnosis,
      textual: true,
      hint: "Uma estimativa para você se orientar.",
    }),
  );
  statsBox.appendChild(
    statCard({
      label: "Última atividade",
      value: last ? formatDateTime(last) : "—",
      empty: !last,
      // Data e hora não são métrica: corpo de número quebraria em duas
      // linhas e desalinharia os quatro cartões.
      textual: true,
      hint: last ? "" : "Nada por aqui ainda.",
    }),
  );
}

/* ---------- 3. desempenho por disciplina ---------- */

function renderDisciplines(overview) {
  discBox.textContent = "";
  if (!overview) {
    renderEmpty(discBox, {
      title: "Ainda não deu para carregar",
      description: "Seu desempenho por disciplina aparece aqui.",
    });
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

/* ---------- 4. o que treinar ---------- */

function renderPriorities(diagnosis) {
  prioBox.textContent = "";
  if (!diagnosis) {
    renderEmpty(prioBox, {
      title: "Ainda não deu para carregar",
      description: "Aqui entram os assuntos que mais valem a pena treinar.",
    });
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
    const meta = el("p", { className: "disc-item__meta" });
    const bits = [disciplineLabel(p.disciplineCode, p.disciplineName)];
    if (p.historicalQuestions) bits.push(`${p.historicalQuestions} questões na prova`);
    if (p.editionsCount) {
      bits.push(`${p.editionsCount} ${p.editionsCount === 1 ? "edição" : "edições"}`);
    }
    meta.appendChild(el("small", { text: bits.join(" · ") }));
    li.appendChild(meta);
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
    renderEmpty(planBox, {
      title: "Roteiro indisponível",
      description: friendlyMessage(loadError),
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
        "O roteiro é a lista de assuntos na ordem que vale mais a pena studying agora para você. Ele muda conforme seu desempenho.",
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
      const row = el("li", {
        className: isDone ? "plan-items__row plan-items__row--done" : "plan-items__row",
      });
      const left = el("div");
      left.appendChild(el("span", { className: "plan-items__name", text: name }));
      if (m) left.appendChild(el("p", { className: "plan-items__meta", text: m }));
      const right = el("div", { className: "cluster" });
      if (statusLabel(item.status)) {
        right.appendChild(
          el("span", { className: `badge ${statusBadge(item.status)}`.trim(), text: statusLabel(item.status) }),
        );
      }
      if (!isDone) {
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
  const card = nextStepShell({
    eyebrow: "Estude agora",
    title: name,
    why: studentNextReason(next.topicId),
  });

  const chips = el("div", { className: "next-step__meta" });
  if (m) chips.appendChild(el("span", { className: "chip", text: m }));
  chips.appendChild(el("span", { className: "chip", text: `${next.priority}º no roteiro` }));
  if (statusLabel(next.status)) {
    chips.appendChild(el("span", { className: "chip", text: statusLabel(next.status) }));
  }
  card.appendChild(chips);

  const actions = el("div", { className: "next-step__actions" });
  actions.appendChild(
    el("a", {
      className: "btn btn--primary",
      text: "Praticar este assunto",
      attrs: { href: `./estudos.html?topico=${encodeURIComponent(next.topicId ?? "")}` },
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

function renderEvolution(data, loadError) {
  evoBox.textContent = "";
  if (loadError) {
    renderEmpty(evoBox, { title: "Sua evolução não carregou", description: friendlyMessage(loadError) });
    return;
  }
  const buckets = data?.buckets || [];
  if (!buckets.length) {
    renderEmpty(evoBox, {
      title: "Sem evolução ainda",
      description: "Responda questões para ver seu aproveitamento ao longo do tempo.",
    });
    return;
  }
  const list = el("ol", { className: "evo-bars" });
  for (const b of buckets) {
    const pct = formatPercentValue(b.accuracy) ?? 0;
    const when = formatBucketDate(b.bucketStart);
    const row = el("li", { className: "evo-bars__row" });
    row.appendChild(el("span", { className: "evo-bars__label", text: when }));
    const bar = el("div", {
      className: "progress",
      attrs: {
        role: "progressbar",
        "aria-valuenow": String(pct),
        "aria-valuemin": "0",
        "aria-valuemax": "100",
        "aria-label": `Aproveitamento em ${when}`,
      },
    });
    const fill = el("div", { className: "progress__bar" });
    fill.style.width = `${pct}%`;
    bar.appendChild(fill);
    row.appendChild(bar);
    row.appendChild(
      el("span", {
        className: "evo-bars__value",
        text: `${formatPercent(b.accuracy)} · ${b.correct}/${b.scored}`,
      }),
    );
    list.appendChild(row);
  }
  evoBox.appendChild(list);
}

/* ---------- 7. simulados ---------- */

function renderSimulations(page, loadError) {
  simBox.textContent = "";
  if (loadError) {
    renderEmpty(simBox, { title: "Seus simulados não carregaram", description: friendlyMessage(loadError) });
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
