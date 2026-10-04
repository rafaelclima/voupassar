/* VouPassar — área administrativa (TASK 12.1)
 * Página restrita a CURATOR/ADMIN: fila de revisão, inconsistências,
 * métricas e curadoria (dois PATCHes auditáveis). Backend em
 * docs/api-admin.md; regras de negócio no servidor — aqui só busca,
 * exibição e repasse. Sem innerHTML (só textContent via el()).
 *
 * Guarda em duas camadas: sem sessão → painel de acesso
 * (login.html?next=admin.html); com sessão mas sem papel → painel 403
 * honesto (concessão é SQL por ADMIN, sem autopromoção — docs/api-admin.md
 * §0). 401/403 no meio da carga caem nos mesmos painéis.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { restoreSession, logout } from "../api/auth.js";
import {
  fetchReviewQueue,
  fetchInconsistencies,
  fetchAdminMetrics,
  updateQuestionStatus,
  reviewClassification,
} from "../api/admin.js";
import { el, renderEmpty, renderErrorSummary, setButtonLoading, toast } from "../components/ui.js";

const guard = document.getElementById("admin-guard");
const forbiddenBox = document.getElementById("admin-forbidden");
const errorBox = document.getElementById("admin-error");
const loadingBox = document.getElementById("admin-loading");
const content = document.getElementById("admin-content");
const subtitle = document.getElementById("admin-subtitle");
const metricsBox = document.getElementById("admin-metrics");
const metricsTables = document.getElementById("admin-metrics-tables");
const incBox = document.getElementById("admin-inconsistencies");
const filtersForm = document.getElementById("admin-filters");
const validationSelect = document.getElementById("f-validation");
const classificationSelect = document.getElementById("f-classification");
const clearBtn = document.getElementById("btn-clear");
const queueCount = document.getElementById("admin-queue-count");
const queueBox = document.getElementById("admin-queue");
const prevBtn = document.getElementById("admin-prev");
const nextBtn = document.getElementById("admin-next");
const pageInfo = document.getElementById("admin-page-info");
const notesBox = document.getElementById("admin-notes");

const PAGE_SIZE = 20;
const VALIDATION_OPTIONS = ["PENDING", "REVIEWED", "APPROVED", "REJECTED"];
const PUBLICATION_OPTIONS = ["PENDENTE_REVISAO", "SOMENTE_REFERENCIA", "PUBLICAVEL", "NAO_PUBLICAVEL"];
const CLASSIFICATION_OPTIONS = ["REVIEWED", "APPROVED", "REJECTED"];

let queueState = {
  validationStatus: "PENDING",
  classificationStatus: "",
  page: 0,
  size: PAGE_SIZE,
  totalPages: 0,
  totalElements: 0,
};

wireLogoutButtons();

main();

async function main() {
  const user = await restoreSession().catch(() => null);
  if (!user) {
    showGuard();
    return;
  }
  showLogoutButtons();
  const roles = Array.isArray(user.roles) ? user.roles : [];
  subtitle.textContent = `Olá, ${user.displayName || "curador(a)"} — fila de revisão, inconsistências, métricas e curadoria do banco de questões.`;
  if (!roles.includes("CURATOR") && !roles.includes("ADMIN")) {
    showForbidden(false);
    return;
  }
  await loadAll();
}

function wireLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      btn.setAttribute("disabled", "");
      try {
        await logout();
      } finally {
        window.location.href = "./login.html?next=admin.html";
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

function showGuard(expired = false) {
  loadingBox.hidden = true;
  content.hidden = true;
  forbiddenBox.hidden = true;
  guard.hidden = false;
  guard.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h2", { text: "Entre para acessar a administração" }));
  box.appendChild(
    el("p", {
      text: expired
        ? "Sua sessão expirou. Entre novamente para continuar."
        : "A área administrativa é restrita a curadores e gestores (CURATOR/ADMIN). Entre com uma conta autorizada.",
    }),
  );
  const actions = el("div", { className: "btn-group", attrs: { style: "justify-content:center" } });
  actions.appendChild(
    el("a", {
      className: "btn btn--primary",
      text: "Entrar",
      attrs: { href: "./login.html?next=admin.html" },
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

function showForbidden(revoked) {
  loadingBox.hidden = true;
  content.hidden = true;
  guard.hidden = true;
  forbiddenBox.hidden = false;
  forbiddenBox.textContent = "";
  const box = el("div", { className: "alert alert--warning", attrs: { role: "alert" } });
  const inner = el("div");
  inner.appendChild(el("strong", { text: "Acesso restrito à curadoria" }));
  inner.appendChild(
    el("p", {
      text: revoked
        ? "Seu papel de curadoria foi revogado ou o token ainda não carrega o papel novo. Saia e entre de novo; se o problema persistir, peça a um ADMIN para conferir sua concessão."
        : "Sua conta é de estudante (STUDENT) e não tem papel CURATOR nem ADMIN. A concessão é feita por um ADMIN direto no banco (INSERT em user_roles) — não há autopromoção por aqui, por segurança.",
    }),
  );
  const actions = el("div", { className: "btn-group mt-2" });
  actions.appendChild(
    el("a", {
      className: "btn btn--secondary btn--sm",
      text: "Voltar ao dashboard",
      attrs: { href: "./dashboard.html" },
    }),
  );
  inner.appendChild(actions);
  box.appendChild(inner);
  forbiddenBox.appendChild(box);
}

/* ---------- carga ---------- */

async function loadAll() {
  errorBox.textContent = "";
  loadingBox.hidden = false;
  content.hidden = true;

  const [metrics, inconsistencies, queue] = await Promise.allSettled([
    fetchAdminMetrics(),
    fetchInconsistencies(),
    fetchReviewQueue({
      validationStatus: queueState.validationStatus,
      classificationStatus: queueState.classificationStatus,
      page: 0,
      size: queueState.size,
    }),
  ]);

  const unauthorized = [metrics, inconsistencies, queue].some(
    (r) => r.status === "rejected" && r.reason instanceof ApiError && r.reason.status === 401,
  );
  if (unauthorized) {
    showGuard(true);
    return;
  }
  const forbidden = [metrics, inconsistencies, queue].some(
    (r) => r.status === "rejected" && r.reason instanceof ApiError && r.reason.status === 403,
  );
  if (forbidden) {
    showForbidden(true);
    return;
  }

  const failures = [metrics, inconsistencies, queue]
    .filter((r) => r.status === "rejected")
    .map((r) => r.reason);
  if (failures.length) {
    const first = failures[0];
    renderErrorSummary(errorBox, {
      title: "Parte dos dados falhou — o restante segue abaixo.",
      items: [friendlyMessage(first)],
      traceId: first instanceof ApiError ? first.traceId : null,
    });
  }

  renderMetrics(
    metrics.status === "fulfilled" ? metrics.value : null,
    metrics.status === "rejected" ? metrics.reason : null,
  );
  renderInconsistencies(
    inconsistencies.status === "fulfilled" ? inconsistencies.value : null,
    inconsistencies.status === "rejected" ? inconsistencies.reason : null,
  );
  if (queue.status === "fulfilled") {
    queueState.page = queue.value?.number ?? 0;
    queueState.totalPages = queue.value?.totalPages ?? 0;
    queueState.totalElements = queue.value?.totalElements ?? 0;
    renderQueue(queue.value);
  } else {
    renderQueue(null, queue.reason);
  }
  renderNotes();

  loadingBox.hidden = true;
  content.hidden = false;
}

async function reloadQueue() {
  queueBox.textContent = "";
  queueCount.textContent = "Carregando fila…";
  try {
    const page = await fetchReviewQueue({
      validationStatus: queueState.validationStatus,
      classificationStatus: queueState.classificationStatus,
      page: queueState.page,
      size: queueState.size,
    });
    queueState.totalPages = page?.totalPages ?? 0;
    queueState.totalElements = page?.totalElements ?? 0;
    renderQueue(page);
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard(true);
      return;
    }
    if (err instanceof ApiError && err.status === 403) {
      showForbidden(true);
      return;
    }
    renderQueue(null, err);
  }
}

async function reloadMetrics() {
  try {
    const metrics = await fetchAdminMetrics();
    renderMetrics(metrics, null);
  } catch {
    // Métricas ficam como estão; a fila já mostra o estado novo.
  }
}

/* ---------- 1. métricas ---------- */

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

function renderMetrics(metrics, loadError) {
  metricsBox.textContent = "";
  metricsTables.textContent = "";
  if (loadError) {
    renderEmpty(metricsBox, {
      title: "Métricas indisponíveis",
      description: friendlyMessage(loadError),
    });
    return;
  }
  if (!metrics) {
    renderEmpty(metricsBox, {
      title: "Sem métricas",
      description: "O servidor não devolveu a fotografia do banco.",
    });
    return;
  }
  metricsBox.appendChild(
    statCard({ label: "questões no banco", value: String(metrics.questionsTotal ?? "—"), hint: "240 oficiais (6 edições × 40) no estado auditado." }),
  );
  metricsBox.appendChild(
    statCard({ label: "anuladas", value: String(metrics.questionsAnnulled ?? "—"), hint: "Contam como conteúdo; regra de pontuação DESCONHECIDA." }),
  );
  metricsBox.appendChild(
    statCard({ label: "com figura", value: String(metrics.questionsWithFigure ?? "—"), hint: "Figura no PDF-fonte; texto extraído pode estar ilegível." }),
  );
  metricsBox.appendChild(
    statCard({ label: "classificações", value: String(metrics.classificationsTotal ?? "—"), hint: "1 ativa por questão (v1.1); APPROVED de pré-curadoria ainda exige referendo humano." }),
  );

  const groups = [
    ["Por validação da questão", metrics.questionsByValidation],
    ["Por publicação", metrics.questionsByPublication],
    ["Por status da classificação", metrics.classificationsByStatus],
  ];
  for (const [title, map] of groups) {
    const wrap = el("div", {
      className: "table-wrap mt-4",
      attrs: { tabindex: "0", role: "region", "aria-label": title },
    });
    const table = el("table", { className: "table" });
    table.appendChild(el("caption", { text: title }));
    const thead = el("thead");
    const hr = el("tr");
    for (const [text, num] of [["Status", false], ["Quantidade", true]]) {
      const th = el("th", { text });
      if (num) th.classList.add("num");
      th.setAttribute("scope", "col");
      hr.appendChild(th);
    }
    thead.appendChild(hr);
    table.appendChild(thead);
    const tbody = el("tbody");
    const entries = Object.entries(map || {});
    if (!entries.length) {
      const tr = el("tr");
      tr.appendChild(el("td", { text: "Sem dados" }));
      tr.appendChild(el("td", { className: "num", text: "—" }));
      tbody.appendChild(tr);
    }
    for (const [status, count] of entries) {
      const tr = el("tr");
      const th = el("th", { text: status });
      th.setAttribute("scope", "row");
      tr.appendChild(th);
      tr.appendChild(el("td", { className: "num", text: String(count ?? 0) }));
      tbody.appendChild(tr);
    }
    table.appendChild(tbody);
    wrap.appendChild(table);
    metricsTables.appendChild(wrap);
  }
}

/* ---------- 2. inconsistências ---------- */

function renderInconsistencies(list, loadError) {
  incBox.textContent = "";
  if (loadError) {
    renderEmpty(incBox, { title: "Inconsistências indisponíveis", description: friendlyMessage(loadError) });
    return;
  }
  if (!Array.isArray(list) || !list.length) {
    renderEmpty(incBox, { title: "Sem checagens", description: "O servidor não devolveu as checagens." });
    return;
  }
  const ul = el("ol", { className: "inc-list" });
  for (const inc of list) {
    const li = el("li", { className: "inc-list__item" });
    const top = el("div", { className: "inc-list__top" });
    top.appendChild(el("strong", { text: inc.check || "CHECAGEM" }));
    const count = Number(inc.count ?? 0);
    top.appendChild(
      el("span", {
        className: `badge ${count === 0 ? "badge--success" : "badge--danger"}`.trim(),
        text: count === 0 ? "0 — saudável" : `${count} na fila`,
      }),
    );
    li.appendChild(top);
    li.appendChild(el("p", { text: inc.description || "Sem descrição." }));
    const sample = Array.isArray(inc.sampleIds) ? inc.sampleIds : [];
    const meta = el("p", { className: "muted" });
    meta.appendChild(
      el("small", {
        text: sample.length
          ? `Amostra de ids: ${sample.join(", ")}`
          : "Sem ids de amostra — nada a investigar.",
      }),
    );
    li.appendChild(meta);
    ul.appendChild(li);
  }
  incBox.appendChild(ul);
}

/* ---------- 3. fila ---------- */

function validationBadge(status) {
  const map = {
    PENDING: "",
    REVIEWED: "badge--info",
    APPROVED: "badge--success",
    REJECTED: "badge--danger",
  };
  return map[status] || "";
}

function publicationBadge(status) {
  const map = {
    PUBLICAVEL: "badge--success",
    NAO_PUBLICAVEL: "",
    PENDENTE_REVISAO: "badge--warning",
    SOMENTE_REFERENCIA: "badge--info",
  };
  return map[status] || "";
}

function renderQueue(page, loadError) {
  queueBox.textContent = "";
  if (loadError) {
    queueCount.textContent = "Fila indisponível.";
    renderEmpty(queueBox, { title: "Fila indisponível", description: friendlyMessage(loadError) });
    syncPagination();
    return;
  }
  const items = page?.content || [];
  queueState.totalPages = page?.totalPages ?? 0;
  queueState.totalElements = page?.totalElements ?? 0;
  const filtro = [
    `validação ${queueState.validationStatus}`,
    queueState.classificationStatus ? `classificação ${queueState.classificationStatus}` : null,
  ]
    .filter(Boolean)
    .join(" · ");
  queueCount.textContent =
    queueState.totalElements === 0
      ? `Nenhuma questão com ${filtro}.`
      : `${queueState.totalElements} questão(ões) com ${filtro} — página ${queueState.page + 1} de ${Math.max(queueState.totalPages, 1)}.`;
  if (!items.length) {
    renderEmpty(queueBox, {
      title: "Fila vazia",
      description: `Nenhuma questão com ${filtro}. Troque os filtros para continuar a curadoria.`,
    });
    syncPagination();
    return;
  }
  const ul = el("ol", { className: "queue-list" });
  for (const item of items) {
    ul.appendChild(queueItem(item));
  }
  queueBox.appendChild(ul);
  syncPagination();
}

function syncPagination() {
  const total = Math.max(queueState.totalPages, 1);
  pageInfo.textContent = `Página ${queueState.page + 1} de ${total}`;
  prevBtn.toggleAttribute("disabled", queueState.page <= 0);
  nextBtn.toggleAttribute("disabled", queueState.page + 1 >= queueState.totalPages || queueState.totalPages === 0);
}

function queueItem(item) {
  const li = el("li", { className: "queue-list__item" });
  const top = el("div", { className: "queue-list__top" });
  top.appendChild(
    el("span", {
      className: "queue-list__title",
      text: `${item.sourceYear ?? "?"} Q${item.sourceQuestionNumber ?? "?"} · ${item.disciplineCode || "—"}`,
    }),
  );
  top.appendChild(el("span", { className: `badge ${validationBadge(item.validationStatus)}`.trim(), text: item.validationStatus || "—" }));
  top.appendChild(el("span", { className: `badge ${publicationBadge(item.publicationStatus)}`.trim(), text: item.publicationStatus || "—" }));
  if (item.annulled) top.appendChild(el("span", { className: "badge badge--warning", text: "Anulada (X)" }));
  if (item.hasFigure) top.appendChild(el("span", { className: "badge badge--info", text: "Com figura" }));
  li.appendChild(top);

  const meta = el("p", { className: "queue-list__meta" });
  meta.textContent = [
    `id ${item.id}`,
    `resposta ${item.answerKey || "—"}`,
    `assunto ${item.topicCode || "NÃO CONFIRMADO"}${item.subtopicCode ? `/${item.subtopicCode}` : ""}`,
    `confiança ${item.classificationConfidence || "—"}`,
    `classificação ${item.classificationStatus || "—"}`,
  ].join(" · ");
  li.appendChild(meta);

  if (item.classificationObservation) {
    const obs = el("p", { className: "queue-list__meta" });
    obs.textContent = `Observação IA: ${item.classificationObservation}`;
    li.appendChild(obs);
  }
  if (item.classificationEvidence) {
    const ev = el("p", { className: "queue-list__meta" });
    ev.textContent = `Evidência: ${String(item.classificationEvidence).slice(0, 180)}${item.classificationEvidence.length > 180 ? "…" : ""}`;
    li.appendChild(ev);
  }

  const actions = el("div", { className: "btn-group" });
  actions.appendChild(
    el("a", {
      className: "btn btn--ghost btn--sm",
      text: "Ver questão",
      attrs: { href: `./questao.html?id=${encodeURIComponent(String(item.id))}` },
    }),
  );
  li.appendChild(actions);

  const curadoria = el("div", { className: "queue-list__curadoria" });
  curadoria.appendChild(questionStatusForm(item));
  curadoria.appendChild(classificationForm(item));
  li.appendChild(curadoria);
  return li;
}

function selectField({ id, label, options, current }) {
  const wrap = el("div", { className: "field", attrs: { style: "margin-bottom:0" } });
  wrap.appendChild(el("label", { className: "field__label", text: label, attrs: { for: id } }));
  const select = el("select", { className: "select", attrs: { id } });
  let matched = false;
  for (const opt of options) {
    const o = el("option", { text: opt, attrs: { value: opt } });
    if (opt === current) {
      o.selected = true;
      matched = true;
    }
    select.appendChild(o);
  }
  // Estado vigente fora da lista de transições (ex.: classificação já
  // APPROVED): mostra o estado real e o bloqueia — nunca Select vazio.
  if (current && !matched) {
    const o = el("option", { text: `${current} (vigente)`, attrs: { value: current } });
    o.selected = true;
    o.disabled = true;
    select.insertBefore(o, select.firstChild);
  }
  wrap.appendChild(select);
  return { wrap, select };
}

function questionStatusForm(item) {
  const form = el("form", { className: "curadoria-form", attrs: { "aria-label": `Curadoria da questão ${item.sourceYear} Q${item.sourceQuestionNumber}` } });
  form.appendChild(el("strong", { text: "Status da questão" }));
  const v = selectField({
    id: `qval-${item.id}`,
    label: "Validação",
    options: VALIDATION_OPTIONS,
    current: item.validationStatus,
  });
  const p = selectField({
    id: `qpub-${item.id}`,
    label: "Publicação",
    options: PUBLICATION_OPTIONS,
    current: item.publicationStatus,
  });
  form.appendChild(v.wrap);
  form.appendChild(p.wrap);
  const hint = el("p", { className: "curadoria-form__hint" });
  hint.textContent = "PUBLICAVEL exige APPROVED. APPROVED só sai para REJECTED.";
  form.appendChild(hint);
  const save = el("button", { className: "btn btn--primary btn--sm", text: "Salvar questão", attrs: { type: "submit" } });
  form.appendChild(save);
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    setButtonLoading(save, true, "Salvando…");
    try {
      await updateQuestionStatus(item.id, {
        validationStatus: v.select.value,
        publicationStatus: p.select.value,
      });
      toast("Questão atualizada.", "success");
      await reloadQueue();
      await reloadMetrics();
    } catch (err) {
      toast(friendlyMessage(err), "info");
    } finally {
      setButtonLoading(save, false);
    }
  });
  return form;
}

function classificationForm(item) {
  const form = el("form", { className: "curadoria-form", attrs: { "aria-label": `Revisão da classificação da questão ${item.sourceYear} Q${item.sourceQuestionNumber}` } });
  form.appendChild(el("strong", { text: "Classificação" }));
  if (item.classificationId === null || item.classificationId === undefined) {
    const note = el("div", { className: "alert alert--warning", attrs: { role: "note" } });
    const inner = el("div");
    inner.appendChild(el("p", { text: "Sem classificação ativa — inconsistência (CLASSIFICATION_WITHOUT_TOPIC ou fila de importação). Nada a revisar aqui." }));
    note.appendChild(inner);
    form.appendChild(note);
    return form;
  }
  const s = selectField({
    id: `cstatus-${item.classificationId}`,
    label: "Revisão",
    options: CLASSIFICATION_OPTIONS,
    current: item.classificationStatus,
  });
  form.appendChild(s.wrap);
  const obsWrap = el("div", { className: "field", attrs: { style: "margin-bottom:0" } });
  const obsId = `cobs-${item.classificationId}`;
  obsWrap.appendChild(el("label", { className: "field__label", text: "Observação (opcional, até 2000 caracteres)", attrs: { for: obsId } }));
  const obs = el("textarea", { className: "textarea", attrs: { id: obsId, rows: "2", maxlength: "2000" } });
  obsWrap.appendChild(obs);
  form.appendChild(obsWrap);
  const hint = el("p", { className: "curadoria-form__hint" });
  hint.textContent = "Carimba revisor e instante. APPROVED só sai para REJECTED.";
  form.appendChild(hint);
  const save = el("button", { className: "btn btn--secondary btn--sm", text: "Salvar classificação", attrs: { type: "submit" } });
  form.appendChild(save);
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    setButtonLoading(save, true, "Salvando…");
    try {
      await reviewClassification(item.classificationId, {
        status: s.select.value,
        observation: obs.value,
      });
      toast("Classificação revisada.", "success");
      await reloadQueue();
      await reloadMetrics();
    } catch (err) {
      toast(friendlyMessage(err), "info");
    } finally {
      setButtonLoading(save, false);
    }
  });
  return form;
}

filtersForm?.addEventListener("submit", async (e) => {
  e.preventDefault();
  queueState.validationStatus = validationSelect.value;
  queueState.classificationStatus = classificationSelect.value;
  queueState.page = 0;
  await reloadQueue();
});

clearBtn?.addEventListener("click", async () => {
  validationSelect.value = "PENDING";
  classificationSelect.value = "";
  queueState.validationStatus = "PENDING";
  queueState.classificationStatus = "";
  queueState.page = 0;
  await reloadQueue();
});

prevBtn?.addEventListener("click", async () => {
  if (queueState.page <= 0) return;
  queueState.page -= 1;
  await reloadQueue();
});

nextBtn?.addEventListener("click", async () => {
  if (queueState.page + 1 >= queueState.totalPages) return;
  queueState.page += 1;
  await reloadQueue();
});

/* ---------- notas ---------- */

function renderNotes() {
  notesBox.textContent = "";
  const notes = [
    "Curadoria revisa, nunca inventa: sem enunciado, gabarito ou assunto criados aqui — só status e revisão com evidência no caderno.",
    "Nada é publicado sem revisão: PUBLICAVEL exige questão APPROVED; nenhuma questão sai como PUBLICAVEL silenciosamente.",
    "Positivo nas inconsistências é fila de trabalho, nunca deleção automática.",
    "A edição de 2021 não existe no acervo e jamais é preenchida com dados inventados.",
    "Dificuldade estimada é palpite (confiança BAIXA global).",
  ];
  for (const n of notes) {
    const alert = el("div", { className: "alert alert--info", attrs: { role: "note" } });
    const inner = el("div");
    inner.appendChild(el("p", { text: n }));
    alert.appendChild(inner);
    notesBox.appendChild(alert);
  }
}
