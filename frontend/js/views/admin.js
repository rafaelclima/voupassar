/* VouPassar — área administrativa
 * Página restrita a CURATOR/ADMIN: saúde do banco de questões
 * (métricas + inconsistências). Backend em docs/api-admin.md; regras de
 * negócio no servidor — aqui só busca e exibição. Sem innerHTML
 * (só textContent via el()). Sem nenhuma escrita: desde a decisão de
 * produto 2026-10-06 não há fila de revisão nem PATCH de curadoria.
 *
 * Guarda em duas camadas: sem sessão → painel de acesso
 * (login.html?next=admin.html); com sessão mas sem papel → painel 403
 * honesto (concessão é SQL por ADMIN, sem autopromoção — docs/api-admin.md
 * §0). 401/403 no meio da carga caem nos mesmos painéis.
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { restoreSession, logout } from "../api/auth.js";
import {
  fetchInconsistencies,
  fetchAdminMetrics,
} from "../api/admin.js";
import { el, renderEmpty, renderErrorSummary } from "../components/ui.js";

const guard = document.getElementById("admin-guard");
const forbiddenBox = document.getElementById("admin-forbidden");
const errorBox = document.getElementById("admin-error");
const loadingBox = document.getElementById("admin-loading");
const content = document.getElementById("admin-content");
const subtitle = document.getElementById("admin-subtitle");
const metricsBox = document.getElementById("admin-metrics");
const incBox = document.getElementById("admin-inconsistencies");
const notesBox = document.getElementById("admin-notes");

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
  subtitle.textContent = `Olá, ${user.displayName || "gestor(a)"} — saúde do banco de questões: métricas e inconsistências (somente leitura).`;
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
        : "A área administrativa é restrita a gestores (CURATOR/ADMIN). Entre com uma conta autorizada.",
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
  inner.appendChild(el("strong", { text: "Acesso restrito à administração" }));
  inner.appendChild(
    el("p", {
      text: revoked
        ? "Seu papel foi revogado ou o token ainda não carrega o papel novo. Saia e entre de novo; se o problema persistir, peça a um ADMIN para conferir sua concessão."
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

  const [metrics, inconsistencies] = await Promise.allSettled([
    fetchAdminMetrics(),
    fetchInconsistencies(),
  ]);

  const unauthorized = [metrics, inconsistencies].some(
    (r) => r.status === "rejected" && r.reason instanceof ApiError && r.reason.status === 401,
  );
  if (unauthorized) {
    showGuard(true);
    return;
  }
  const forbidden = [metrics, inconsistencies].some(
    (r) => r.status === "rejected" && r.reason instanceof ApiError && r.reason.status === 403,
  );
  if (forbidden) {
    showForbidden(true);
    return;
  }

  const failures = [metrics, inconsistencies]
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
  renderNotes();

  loadingBox.hidden = true;
  content.hidden = false;
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
    statCard({ label: "classificações", value: String(metrics.classificationsTotal ?? "—"), hint: "1 vigente por questão (taxonomia v1.1, veredito do pipeline)." }),
  );
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
        text: count === 0 ? "0 — saudável" : `${count} a verificar`,
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

/* ---------- notas ---------- */

function renderNotes() {
  notesBox.textContent = "";
  const notes = [
    "Somente leitura: esta área observa o banco, nunca altera questões, assuntos ou publicação.",
    "Confiança = pipeline: checksum estável + vínculo ao gabarito + páginas válidas (sem carimbo humano).",
    "Positivo nas inconsistências é observação, nunca deleção automática.",
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
