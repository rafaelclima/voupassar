/* VouPassar — evidência do roteiro (TASK 18.3)
 *
 * Lógica pura e compartilhada da linha "por que estudar isso" dos itens do
 * roteiro (`GET /recommendations/plan`, `evidenceJson` rico da TASK 18.2).
 * Usada por `views/dashboard.js` (lista do roteiro + "estude agora"),
 * `views/estudos.js` (hero + "no seu roteiro") e `views/perfil.js` (metas):
 * o mesmo motivo, sem duplicar formatação.
 *
 * Regras vindas do backend (nunca reinventadas aqui):
 * - `evidenceJson`: `{topic_id, historicalQuestions, editionsCount,
 *   editions[], sampleQuestionIds[], accuracy, attempts, lastAttemptAt,
 *   algorithmVersion}` (`api-roteiro.md`, schema TASK 18.2);
 * - `sampleQuestionIds[]` (máx 5) só traz questões OFICIAIS (regra Fase 15);
 * - `accuracy`/`attempts`/`lastAttemptAt` são o retrato do aluno no momento
 *   da geração do plano (podem estar atrás do diagnóstico ao vivo);
 * - `lastAttemptAt = null` sem tentativas; assunto sem questão oficial
 *   carrega `sampleQuestionIds: []` honesto.
 *
 * Sem DOM nem fetch: cada view monta os elementos via `el()` e traduz
 * códigos via `vocab.js`. Nenhum número é estimado aqui — o que falta é
 * omitido da frase, nunca zerado (AGENTS.md §4).
 */

/** Normaliza o `evidenceJson` de um item do plano.
 *  Aceita string (forma da API), objeto já convertido ou ausência.
 *  Devolve o objeto normalizado ou `null` quando não há evidência
 *  aproveitável (JSON inválido, tipos errados). */
export function parseEvidence(item) {
  const raw = item?.evidenceJson ?? item?.evidence_json ?? null;
  if (raw === null || raw === undefined || raw === "") return null;
  let ev;
  if (typeof raw === "string") {
    try {
      ev = JSON.parse(raw);
    } catch {
      return null;
    }
  } else if (typeof raw === "object") {
    ev = raw;
  } else {
    return null;
  }
  if (!ev || typeof ev !== "object" || Array.isArray(ev)) return null;

  const num = (v) => {
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  };
  const historicalQuestions = num(ev.historicalQuestions) ?? 0;
  const editionsCount = num(ev.editionsCount) ?? 0;
  const editions = Array.isArray(ev.editions)
    ? ev.editions.map((y) => Number(y)).filter((y) => Number.isInteger(y))
    : [];
  const sampleQuestionIds = Array.isArray(ev.sampleQuestionIds)
    ? ev.sampleQuestionIds.map((id) => Number(id)).filter((id) => Number.isInteger(id) && id > 0)
    : [];
  const attemptsRaw = num(ev.attempts);
  const attempts = attemptsRaw === null ? 0 : Math.max(0, Math.floor(attemptsRaw));
  const accuracy = ev.accuracy === null || ev.accuracy === undefined ? null : num(ev.accuracy);
  const lastAttemptAt = typeof ev.lastAttemptAt === "string" && ev.lastAttemptAt ? ev.lastAttemptAt : null;

  return {
    topicId: ev.topic_id ?? ev.topicId ?? item?.topicId ?? null,
    historicalQuestions: Math.max(0, Math.floor(historicalQuestions)),
    editionsCount: Math.max(0, Math.floor(editionsCount)),
    editions,
    sampleQuestionIds,
    accuracy,
    attempts,
    lastAttemptAt,
    algorithmVersion: typeof ev.algorithmVersion === "string" ? ev.algorithmVersion : null,
  };
}

/** Dias desde um ISO-8601 até `nowMs` (padrão: agora).
 *  Devolve inteiro ≥ 0 ou `null` quando a data é inválida/ausente. */
export function daysSince(iso, nowMs = Date.now()) {
  if (!iso) return null;
  const t = new Date(iso).getTime();
  if (Number.isNaN(t)) return null;
  const diff = Math.floor((nowMs - t) / 86400000);
  if (!Number.isFinite(diff) || diff < 0) return null;
  return diff;
}

/** "respondida hoje" / "há 1 dia" / "há N dias" (ou "" sem data). */
export function formatRecency(days) {
  if (days === null || days === undefined) return "";
  if (days <= 0) return "respondida hoje";
  if (days === 1) return "há 1 dia";
  return `há ${days} dias`;
}

/** Percentual inteiro de um `accuracy` 0–1 (ou `null` sem sinal). */
export function formatEvidencePercent(accuracy) {
  if (accuracy === null || accuracy === undefined) return null;
  const n = Number(accuracy);
  if (!Number.isFinite(n)) return null;
  return Math.round(n * 100);
}

/** "2020, 2022 e 2024" (máx 3 + "…" quando houver mais). */
export function formatEditionsShort(editions) {
  const years = [...new Set(editions || [])].sort((a, b) => a - b);
  if (!years.length) return "";
  if (years.length <= 3) {
    if (years.length === 1) return String(years[0]);
    return `${years.slice(0, -1).join(", ")} e ${years[years.length - 1]}`;
  }
  return `${years.slice(0, 3).join(", ")}…`;
}

/** Frase "por que estudar isso" em linguagem de estudante.
 *  Ex.: "Você acertou 1 de 5 (20%). Caiu em 70 questões de 6 edições
 *  (2020, 2022 e 2024). Última tentativa há 10 dias."
 *  Sem tentativas: "Você ainda não respondeu nada deste assunto. Caiu em
 *  …". Sem peso histórico: a parte da prova é omitida, nunca zerada. */
export function evidenceLine(ev, nowMs = Date.now()) {
  if (!ev) return "";
  const parts = [];

  if (!ev.attempts) {
    parts.push("Você ainda não respondeu nada deste assunto.");
  } else {
    const pct = formatEvidencePercent(ev.accuracy);
    const correct = pct === null ? null : Math.round((ev.accuracy * ev.attempts));
    if (pct === null || correct === null) {
      parts.push(`Você tem ${ev.attempts} ${ev.attempts === 1 ? "tentativa" : "tentativas"} neste assunto.`);
    } else {
      parts.push(
        `Você acertou ${correct} de ${ev.attempts} (${pct}%).`,
      );
    }
  }

  if (ev.historicalQuestions > 0 || ev.editionsCount > 0 || ev.editions.length > 0) {
    const editions = ev.editionsCount || ev.editions.length;
    let hist = "";
    if (ev.historicalQuestions > 0 && editions > 0) {
      hist = `Caiu em ${ev.historicalQuestions} ${ev.historicalQuestions === 1 ? "questão" : "questões"} de ${editions} ${editions === 1 ? "edição" : "edições"}`;
    } else if (ev.historicalQuestions > 0) {
      hist = `Caiu em ${ev.historicalQuestions} ${ev.historicalQuestions === 1 ? "questão" : "questões"}`;
    } else {
      hist = `Caiu em ${editions} ${editions === 1 ? "edição" : "edições"}`;
    }
    const short = formatEditionsShort(ev.editions);
    parts.push(short ? `${hist} (${short}).` : `${hist}.`);
  }

  const recency = formatRecency(daysSince(ev.lastAttemptAt, nowMs));
  if (recency) {
    parts.push(ev.attempts ? `Última tentativa ${recency}.` : `Última atividade ${recency}.`);
  }

  return parts.join(" ");
}

/** Link para a área de estudos com o recorte já aplicado.
 *  `origin` informa de onde o aluno veio (`painel`, `perfil`…) para o hero
 *  de estudos priorizar o assunto; não é filtro de questões. */
export function estudosHref({ disciplineCode = "", topicId = "", subtopicId = "", origin = "" } = {}) {
  const q = new URLSearchParams();
  if (disciplineCode) q.set("disciplina", disciplineCode);
  if (topicId !== "" && topicId !== null && topicId !== undefined) q.set("topico", String(topicId));
  if (subtopicId !== "" && subtopicId !== null && subtopicId !== undefined) q.set("subtopico", String(subtopicId));
  if (origin) q.set("origem", origin);
  const qs = q.toString();
  return qs ? `./estudos.html?${qs}` : "./estudos.html";
}

/** Link para a fila de revisão já filtrada por assunto (`aba=revisao` rola
 *  até a seção; `disciplina`/`topico` pré-selecionam os filtros da fila —
 *  ver `views/revisao.js readUrlIntoReview`). */
export function revisaoHref({ disciplineCode = "", topicId = "" } = {}) {
  const q = new URLSearchParams();
  q.set("aba", "revisao");
  if (disciplineCode) q.set("disciplina", disciplineCode);
  if (topicId !== "" && topicId !== null && topicId !== undefined) q.set("topico", String(topicId));
  return `./estudos.html?${q.toString()}`;
}

/** Link para um exemplo oficial citado na evidência (`questao.html?id=`). */
export function sampleHref(questionId) {
  return `./questao.html?id=${encodeURIComponent(String(questionId))}`;
}

/** Resolve disciplina/assunto/subassunto de um item para os links de
 *  prática, a partir do catálogo (`GET /topics`). Sem catálogo, o recorte
 *  leva só o que o item já traz — nunca nome inventado. */
export function practiceTarget(item, topicsById) {
  const topic = topicsById?.get?.(Number(item?.topicId)) || null;
  return {
    disciplineCode: topic?.disciplineCode || "",
    topicId: item?.topicId ?? "",
    subtopicId: item?.subtopicId ?? "",
  };
}

/** Próximo item aberto do roteiro (menor `priority` entre TODO/DOING;
 *  sem aberto, o menor entre todos). Tolera `items`, `planItems` e
 *  `recommendations` (formatos das três telas). */
export function nextStudyOf(plan) {
  const items = plan?.items || plan?.planItems || plan?.recommendations || [];
  if (!Array.isArray(items) || !items.length) return null;
  const open = items.filter((i) => i?.status === "TODO" || i?.status === "DOING");
  const pool = open.length ? open : items;
  return [...pool].sort((a, b) => (a.priority - b.priority) || ((a.id ?? 0) - (b.id ?? 0)))[0] || null;
}
