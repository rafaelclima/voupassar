/* VouPassar — revisão guiada em Estudos (TASK 17.2)
 * Seção `#sec-revisao-t` do estudos.html: fila priorizada do aluno com
 * filtros (disciplina/assunto/só-erros) + "Iniciar revisão (N)".
 *
 * Dono: este módulo. O estudos.js só chama `initReviewSection()` depois de
 * autenticar e mostrar o conteúdo (sem ciclo: nada aqui importa estudos.js).
 *
 * Regras:
 * - Ordem e motivos vivem no backend (`GET /review/queue` via api/review.js);
 *   aqui só exibição, sem reordenar nem inventar prioridade.
 * - O `reason` bruto da API não é renderizado: ele carrega códigos internos
 *   (ex. `MATEMATICA`, `FRAGIL`); a tela monta a linha com rótulos de
 *   `vocab.js` + números do item (AGENTS.md §4 — nunca exibir enum cru).
 * - `notes` da API é trilha de auditoria: nunca vai para a tela (ver
 *   check_frontend.py). `daysSinceLastAttempt` é informativo.
 * - Sem innerHTML (só textContent via el()). Erros com traceId + retry por
 *   seção (padrão TASK 16.3). 401 → convite a entrar de novo (o interceptor
 *   do client.js já tentou 1 refresh silencioso).
 */

import { ApiError, friendlyMessage } from "../api/client.js";
import { getQueue, createSession } from "../api/review.js";
import { fetchTopics } from "../api/estudos.js";
import {
  el,
  renderEmpty,
  renderErrorWithRetry,
  setButtonLoading,
  toast,
} from "../components/ui.js";
import {
  disciplineLabel,
  masteryLabel,
  reviewCategoryLabel,
  reviewCategoryHint,
} from "../vocab.js";

const section = document.getElementById("sec-revisao-t");
const form = document.getElementById("revisao-filters");
const selDisc = document.getElementById("r-disciplina");
const selTopic = document.getElementById("r-topico");
const chkErrors = document.getElementById("r-so-erros");
const btnLimpar = document.getElementById("revisao-limpar");
const countNote = document.getElementById("revisao-count");
const listBox = document.getElementById("revisao-lista");
const btnIniciar = document.getElementById("revisao-iniciar");

const state = {
  bound: false,
  ready: false,
  /* Trilha do processo (TASK F.1): repassada a GET /review/queue e a
   * fetchTopics; null = legado global (nunca misturar na exibição). */
  institution: null,
  disciplines: [],
  topicsOfDisc: [],
  filters: { discipline: "", topicId: "", onlyErrors: false, fromHub: false },
  items: [],
  queue: null,
};

/* Chamado pelo estudos.js após auth + conteúdo visível. Idempotente. */
export async function initReviewSection({ disciplines = [], institution = null } = {}) {
  if (!section || !form) return;
  state.institution = institution || null;
  state.disciplines = Array.isArray(disciplines) ? disciplines : [];
  if (!state.bound) {
    state.bound = true;
    bindEvents();
  }
  fillDisciplineSelect();
  readUrlIntoReview();
  applyFiltersToForm();
  await resolveDisciplineForTopic();
  applyFiltersToForm();
  await refreshTopicSelect();
  await loadQueue({ scroll: state.filters.fromHub });
}

/* Troca de trilha IFRN↔EAJ (TASK F.1): atualiza catálogo da seção e
 * recarrega a fila sem mexer nos filtros que continuam válidos. */
export async function refreshReviewForInstitution({ disciplines = [], institution = null } = {}) {
  if (!section || !form) return;
  state.institution = institution || null;
  state.disciplines = Array.isArray(disciplines) ? disciplines : [];
  if (state.filters.discipline && !state.disciplines.some((d) => d.code === state.filters.discipline)) {
    state.filters.discipline = "";
    state.filters.topicId = "";
  }
  fillDisciplineSelect();
  applyFiltersToForm();
  await refreshTopicSelect();
  await loadQueue();
}

function bindEvents() {
  selDisc.addEventListener("change", async () => {
    state.filters.discipline = selDisc.value || "";
    state.filters.topicId = "";
    await refreshTopicSelect();
    await loadQueue();
  });
  selTopic.addEventListener("change", async () => {
    state.filters.topicId = selTopic.disabled ? "" : (selTopic.value || "");
    await loadQueue();
  });
  chkErrors.addEventListener("change", async () => {
    state.filters.onlyErrors = chkErrors.checked;
    await loadQueue();
  });
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    state.filters.discipline = selDisc.value || "";
    state.filters.topicId = selTopic.disabled ? "" : (selTopic.value || "");
    state.filters.onlyErrors = chkErrors.checked;
    await loadQueue();
    section.scrollIntoView({ block: "start" });
  });
  btnLimpar.addEventListener("click", async () => {
    state.filters = { discipline: "", topicId: "", onlyErrors: false };
    applyFiltersToForm();
    await refreshTopicSelect();
    await loadQueue();
  });
  btnIniciar.addEventListener("click", startReview);
}

/* Busca original da carga, capturada antes de qualquer `replaceState`: o
 * estudos.js reescreve a URL com o recorte do navegador de questões
 * (`syncUrl`) antes de esta seção iniciar — ler o `location` ao vivo
 * perderia o `?aba=revisao` do "Voltar à fila" e herdaria a disciplina
 * padrão do navegador como se fosse pré-filtro escolhido. */
const INITIAL_SEARCH = (() => {
  try {
    return window.location.search || "";
  } catch {
    return "";
  }
})();

/* ?aba=revisao (atalho do hub do simulado em modo Revisão, "Voltar à fila"
 * da execução e CTAs "Revisar erros" do roteiro — TASK 18.3): rola até a
 * seção na primeira carga. ?disciplina= e ?topico= (só quando vieram na URL
 * original) pré-selecionam os filtros da fila (validados contra o catálogo;
 * inválido volta para Todas/Todos, nunca erro). */
function readUrlIntoReview() {
  let q;
  try {
    q = new URLSearchParams(INITIAL_SEARCH);
  } catch {
    return;
  }
  const aba = (q.get("aba") || "").trim().toLowerCase();
  state.filters.fromHub = aba === "revisao";
  const disc = (q.get("disciplina") || "").trim().toUpperCase();
  if (disc && state.disciplines.some((d) => d.code === disc)) {
    state.filters.discipline = disc;
  }
  const topicRaw = (q.get("topico") || "").trim();
  if (/^[1-9]\d*$/.test(topicRaw)) {
    state.filters.topicId = topicRaw;
  }
}

/* TASK 18.3: o CTA "Revisar erros" do roteiro pode chegar só com ?topico=
 * (sem disciplina). Deriva a disciplina do catálogo para o select
 * dependente conseguir listar os assuntos; assunto inexistente limpa o
 * filtro em vez de quebrar a fila. Só UI — sem novo conceito. */
async function resolveDisciplineForTopic() {
  if (!state.filters.topicId || state.filters.discipline) return;
  try {
    const data = await fetchTopics(undefined, state.institution || undefined);
    const all = Array.isArray(data) ? data : (data?.content ?? []);
    const hit = all.find((t) => String(t.id) === String(state.filters.topicId));
    if (hit?.disciplineCode && state.disciplines.some((d) => d.code === hit.disciplineCode)) {
      state.filters.discipline = hit.disciplineCode;
    } else {
      state.filters.topicId = "";
    }
  } catch {
    state.filters.topicId = "";
  }
}

function fillDisciplineSelect() {
  if (!selDisc) return;
  selDisc.textContent = "";
  selDisc.appendChild(el("option", { text: "Todas", attrs: { value: "" } }));
  for (const d of state.disciplines) {
    selDisc.appendChild(el("option", { text: d.name, attrs: { value: d.code } }));
  }
}

function applyFiltersToForm() {
  selDisc.value = state.filters.discipline || "";
  chkErrors.checked = state.filters.onlyErrors;
}

async function refreshTopicSelect() {
  const disc = state.filters.discipline || "";
  state.topicsOfDisc = [];
  selTopic.textContent = "";
  selTopic.appendChild(el("option", { text: "Todos", attrs: { value: "" } }));
  if (!disc) {
    selTopic.value = "";
    selTopic.disabled = true;
    state.filters.topicId = "";
    return;
  }
  try {
    const data = await fetchTopics(disc, state.institution || undefined);
    state.topicsOfDisc = Array.isArray(data) ? data : (data?.content ?? []);
  } catch {
    state.topicsOfDisc = [];
  }
  for (const t of state.topicsOfDisc) {
    selTopic.appendChild(el("option", { text: t.name, attrs: { value: String(t.id) } }));
  }
  const ids = new Set(state.topicsOfDisc.map((t) => String(t.id)));
  if (state.filters.topicId && !ids.has(String(state.filters.topicId))) {
    state.filters.topicId = "";
  }
  selTopic.value = state.filters.topicId || "";
  selTopic.disabled = state.topicsOfDisc.length === 0;
}

async function loadQueue({ scroll = false } = {}) {
  listBox.textContent = "";
  if (btnIniciar) btnIniciar.hidden = true;
  countNote.textContent = "Buscando fila…";
  const loading = el("div", { className: "loading-block", attrs: { role: "status" } });
  loading.appendChild(el("span", { className: "spinner", attrs: { "aria-hidden": "true" } }));
  loading.appendChild(el("span", { text: "Buscando fila…" }));
  listBox.appendChild(loading);
  try {
    const f = state.filters;
    const queue = await getQueue({
      ...(f.discipline ? { discipline: f.discipline } : {}),
      ...(f.topicId ? { topicId: f.topicId } : {}),
      ...(f.onlyErrors ? { onlyErrors: true } : {}),
      ...(state.institution ? { institution: state.institution } : {}),
    });
    state.queue = queue;
    state.items = Array.isArray(queue?.items) ? queue.items : [];
    state.ready = true;
    renderQueue(queue);
  } catch (err) {
    state.ready = true;
    listBox.textContent = "";
    countNote.textContent = "Falha ao buscar a fila.";
    if (err instanceof ApiError && err.status === 401) {
      renderEmpty(listBox, {
        title: "Sua sessão expirou",
        description: "Entre novamente para ver sua fila de revisão.",
        actionLabel: "Entrar",
        onAction: () => {
          window.location.href = "./login.html?next=estudos.html%3Faba%3Drevisao";
        },
      });
      return;
    }
    renderErrorWithRetry(listBox, {
      title: "Não foi possível carregar a fila de revisão. ",
      description: friendlyMessage(err),
      traceId: err instanceof ApiError ? err.traceId : null,
      onRetry: () => loadQueue(),
    });
  } finally {
    if (scroll) {
      section.scrollIntoView({ block: "start" });
      section.focus({ preventScroll: true });
    }
  }
}

function renderQueue(queue) {
  listBox.textContent = "";
  const items = state.items;
  const distinct = queue?.distinctQuestions ?? items.length;
  if (items.length === 0) {
    renderQueueEmpty(queue);
    return;
  }
  const extra = distinct > items.length ? ` · mostrando ${items.length} de ${distinct}` : "";
  const scope = state.filters.onlyErrors ? " (só erros)" : "";
  countNote.textContent = `${items.length} ${items.length === 1 ? "item" : "itens"} na fila${scope}${extra} · erros primeiro.`;
  for (const item of items) {
    listBox.appendChild(renderReviewItem(item));
  }
  if (btnIniciar) {
    btnIniciar.hidden = false;
    btnIniciar.textContent = `Iniciar revisão (${items.length})`;
  }
}

function renderQueueEmpty(queue) {
  if (btnIniciar) btnIniciar.hidden = true;
  const attempted = queue?.totalAttempts ?? 0;
  const filtered = Boolean(state.filters.discipline || state.filters.topicId || state.filters.onlyErrors);
  if (attempted === 0) {
    // Conta nova: fila ainda não existe (só questões já tentadas entram).
    // O "3+" é o sinal mínimo do diagnóstico (ReviewService.MIN_SCORED) —
    // a partir daí a fila prioriza melhor; nunca promessa de item oculto.
    countNote.textContent = "Nenhum item na fila ainda.";
    renderEmpty(listBox, {
      title: "Nada para revisar ainda",
      description:
        "Responda questões no modo Estudo ou em simulados — cada erro ou assunto frágil entra na sua fila. " +
        "Com 3+ respostas o diagnóstico ganha sinal e a fila prioriza melhor.",
      actionLabel: "Praticar questões",
      onAction: () => {
        document.getElementById("sec-questoes-t")?.scrollIntoView({ block: "start" });
      },
    });
    return;
  }
  countNote.textContent = "Nenhum item neste filtro.";
  renderEmpty(listBox, {
    title: filtered ? "Nada neste filtro" : "Fila vazia",
    description: filtered
      ? "Nenhum item da fila bate com disciplina, assunto ou só-erros. Ajuste o filtro para ver o restante."
      : "Sua fila está vazia no momento.",
    ...(filtered ? { actionLabel: "Limpar filtro da fila", onAction: () => btnLimpar.click() } : {}),
  });
}

function sourceLabel(item) {
  const bits = [];
  if (item.sourceYear && item.sourceQuestionNumber) bits.push(`${item.sourceYear} Q${item.sourceQuestionNumber}`);
  else if (item.sourceQuestionNumber) bits.push(`Q${item.sourceQuestionNumber}`);
  const disc = item.disciplineName || disciplineLabel(item.disciplineCode);
  if (disc) bits.push(disc);
  return bits.length > 0 ? bits.join(" · ") : `Questão #${item.questionId}`;
}

function pctOf(correct, attempts) {
  if (!attempts) return "—";
  return `${Math.round((correct / attempts) * 100)}%`;
}

function renderReviewItem(item) {
  const card = el("article", { className: "review-item", attrs: { "aria-labelledby": `rev-${item.questionId}-t` } });
  const head = el("div", { className: "review-item__head" });
  head.appendChild(el("span", { className: "review-item__rank", text: `#${item.rank}`, attrs: { id: `rev-${item.questionId}-t` } }));
  head.appendChild(el("span", { className: "badge badge--primary", text: reviewCategoryLabel(item.reviewCategory) }));
  card.appendChild(head);

  card.appendChild(el("p", { className: "review-item__source", text: sourceLabel(item) }));
  card.appendChild(el("p", { className: "review-item__hint", text: reviewCategoryHint(item.reviewCategory) }));

  const facts = [];
  facts.push(`${item.correct}/${item.attempts} nesta questão (${pctOf(item.correct, item.attempts)})`);
  facts.push(item.lastCorrect ? "última correta" : "última incorreta");
  if (item.daysSinceLastAttempt !== null && item.daysSinceLastAttempt !== undefined) {
    const n = item.daysSinceLastAttempt;
    facts.push(n === 0 ? "respondida hoje" : n === 1 ? "há 1 dia" : `há ${n} dias`);
  }
  card.appendChild(el("p", { className: "review-item__meta", text: facts.join(" · ") }));

  const topicBits = [];
  if (item.topicName) {
    topicBits.push(`Assunto: ${item.topicName}${item.subtopicName ? ` · ${item.subtopicName}` : ""}`);
    const mastery = masteryLabel(item.topicMastery);
    if (mastery) topicBits.push(mastery);
    if (item.topicAccuracy !== null && item.topicAccuracy !== undefined && item.topicScored) {
      topicBits.push(`você ${pctOf(Math.round(item.topicAccuracy * item.topicScored), item.topicScored)} em ${item.topicScored}`);
    }
  } else {
    topicBits.push("Assunto ainda sem classificação.");
  }
  card.appendChild(el("p", { className: "review-item__meta", text: topicBits.join(" · ") }));

  const actions = el("div", { className: "btn-group" });
  actions.appendChild(
    el("a", {
      className: "btn btn--ghost btn--sm",
      text: "Abrir questão",
      attrs: {
        href: `./questao.html?id=${encodeURIComponent(String(item.questionId))}&voltar=${encodeURIComponent("./estudos.html?aba=revisao")}`,
      },
    }),
  );
  card.appendChild(actions);
  return card;
}

async function startReview() {
  const items = state.items;
  if (items.length === 0) return;
  setButtonLoading(btnIniciar, true, "Criando…");
  try {
    const f = state.filters;
    // Congela exatamente o top-N exibido, nos mesmos filtros e na mesma
    // ordem da fila (o backend compõe o ReviewService — sem reordenar aqui).
    const session = await createSession({
      limit: items.length,
      ...(f.discipline ? { discipline: f.discipline } : {}),
      ...(f.topicId ? { topicId: f.topicId } : {}),
      ...(f.onlyErrors ? { onlyErrors: true } : {}),
    });
    toast(`Sessão de revisão criada com ${items.length} ${items.length === 1 ? "questão" : "questões"}.`, "success");
    window.location.href = `./simulado.html?review=${encodeURIComponent(String(session.sessionId))}`;
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      renderEmpty(listBox, {
        title: "Sua sessão expirou",
        description: "Entre novamente para iniciar a revisão.",
        actionLabel: "Entrar",
        onAction: () => {
          window.location.href = "./login.html?next=estudos.html%3Faba%3Drevisao";
        },
      });
      if (btnIniciar) btnIniciar.hidden = true;
      return;
    }
    // A fila pode ter mudado entre ver e iniciar (ex. 400 NO_REVIEW_ITEMS):
    // recarrega em vez de travar num erro morto.
    if (err instanceof ApiError && (err.status === 400 || err.status === 404)) {
      toast(`${friendlyMessage(err)} Atualizando a fila…`, "info");
      await loadQueue();
      return;
    }
    toast(friendlyMessage(err), "info");
  } finally {
    setButtonLoading(btnIniciar, false);
    if (state.items.length > 0 && btnIniciar) {
      btnIniciar.hidden = false;
      btnIniciar.textContent = `Iniciar revisão (${state.items.length})`;
    }
  }
}
