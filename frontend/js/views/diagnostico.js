/* VouPassar — diagnóstico inicial "Descubra seu nível" (TASK 20.2)
 *
 * Wizard de entrada: 12 questões oficiais em 2 blocos (6 LP + 6 MAT) no
 * Modo PROVA, reutilizando `POST /simulations/by-discipline` — nenhum
 * endpoint novo. A execução acontece na tela de simulado existente
 * (`simulado.html?id=&diag=`); ao concluir cada bloco, o gancho do wizard
 * em `views/simulado.js` encadeia o próximo e, no fim, gera o plano v1
 * (`POST /recommendations`) levando ao painel com `?origem=diagnostico`.
 *
 * Códigos de disciplina resolvidos via `GET /disciplines` (nunca fixos no
 * texto exibido): casa por nome contendo "portugues" ou "matemat".
 */

import { currentInstitution, setInstitution } from "../state/process.js";
import { mountProcessSelector } from "../components/process-selector.js";
import { ApiError, friendlyMessage } from "../api/client.js";
import { logout } from "../api/auth.js";
import { createByDiscipline, fetchDisciplines } from "../api/simulado.js";
import { el, renderErrorSummary, setButtonLoading } from "../components/ui.js";
import { renderAuthGuard, requireSessionOrGuard } from "./auth-shared.js";

const guard = document.getElementById("diag-guard");
const errorBox = document.getElementById("diag-error");
const loadingBox = document.getElementById("diag-loading");
const content = document.getElementById("diag-content");
const startBtn = document.getElementById("diag-start");

export const DIAG_QUESTION_COUNT = 6;

wireLogoutButtons();
main();

async function main() {
  const user = await requireSessionOrGuard(showGuard);
  if (!user) return;
  showLogoutButtons();
  // Seletor de trilha (TASK F.1): ?institution= vence o persistido e persiste.
  try {
    const raw = new URLSearchParams(window.location.search).get("institution") || "";
    const v = raw.trim().toUpperCase();
    if (v === "EAJ" || v === "IFRN") setInstitution(v);
  } catch { /* mantém a trilha persistida */ }
  const note = document.getElementById("diag-process-note");
  mountProcessSelector(document.getElementById("diag-process-mount"), {
    onChange: (next) => {
      if (note) note.textContent = `O diagnóstico monta 12 questões da trilha ${next === "EAJ" ? "EAJ/UFRN" : "IFRN"}.`;
    },
  });
  const inst = currentInstitution();
  if (note) note.textContent = `O diagnóstico monta 12 questões da trilha ${inst === "EAJ" ? "EAJ/UFRN" : "IFRN"}.`;
  loadingBox.hidden = true;
  content.hidden = false;
}

function wireLogoutButtons() {
  document.querySelectorAll("[data-logout]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      btn.setAttribute("disabled", "");
      try {
        await logout();
      } finally {
        window.location.href = "./login.html?next=diagnostico.html";
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
    title: "Entre para descobrir seu nível",
    description: "O diagnóstico monta 12 questões oficiais para situar seu ponto de partida. Entre com sua conta para começar.",
    next: "diagnostico.html",
  });
}

/** Resolve os códigos de LP e MAT pelo catálogo (tolerante a LP/MAT vs nomes longos). */
export async function resolveDiagDisciplines() {
  const institution = currentInstitution();
  const raw = await fetchDisciplines(institution);
  const list = Array.isArray(raw) ? raw : (raw?.content || raw?.items || []);
  const norm = (s) => String(s || "").toLowerCase();
  let lp = null;
  let mat = null;
  for (const d of list) {
    const code = d.code || d.disciplineCode || "";
    const name = d.name || d.disciplineName || "";
    const hay = `${norm(code)} ${norm(name)}`;
    if (!lp && (hay.includes("portugues") || /(^|[\s_-])lp($|[\s_-])/.test(hay))) lp = code;
    if (!mat && (hay.includes("matemat") || /(^|[\s_-])mat($|[\s_-])/.test(hay))) mat = code;
  }
  return { lp, mat };
}

startBtn?.addEventListener("click", async () => {
  errorBox.textContent = "";
  setButtonLoading(startBtn, true, "Montando Língua Portuguesa…");
  try {
    const { lp } = await resolveDiagDisciplines();
    if (!lp) {
      renderErrorSummary(errorBox, {
        title: "Não encontrei a disciplina de Língua Portuguesa no catálogo.",
        items: ["Tente novamente em alguns instantes."],
      });
      return;
    }
    const institution = currentInstitution();
    const created = await createByDiscipline({
      disciplineCode: lp,
      questionCount: DIAG_QUESTION_COUNT,
      mode: "PROVA",
      institution,
    });
    const id = created?.attemptId ?? created?.id;
    if (!id) {
      renderErrorSummary(errorBox, {
        title: "Diagnóstico criado, mas sem identificador — tente de novo.",
      });
      return;
    }
    window.location.href = `./simulado.html?id=${encodeURIComponent(String(id))}&diag=LP`;
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      showGuard();
      return;
    }
    const box = el("div");
    renderErrorSummary(box, {
      title: friendlyMessage(err),
      traceId: err instanceof ApiError ? err.traceId : null,
    });
    errorBox.textContent = "";
    errorBox.appendChild(box);
  } finally {
    setButtonLoading(startBtn, false);
  }
});
