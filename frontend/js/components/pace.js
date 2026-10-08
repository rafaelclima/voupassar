/* VouPassar — ritmo em simulado (TASK 21.1)
 *
 * Indicador de ritmo SÓ exibição: cronômetro informativo dentro da sessão
 * de simulado ("estou no ritmo da prova real?"). Não entra em placar,
 * ranking, desempenho, diagnóstico, recomendação, roteiro ou revisão
 * (decisão 2026-10-07: P5 com escopo reduzido).
 *
 * Fonte da referência: `docs/provas-inventario.md §1` — todas as capas
 * presentes no dataset (2020, 2022, 2023, 2024, 2025, 2026) declaram
 * duração máxima de 4h para 20 LP + 20 MAT + 1 produção textual. Cada valor
 * abaixo pertence àquela edição; ano ausente do mapa = DESCONHECIDO
 * ("ritmo informativo, sem tempo oficial confirmado") — nunca inventar.
 *
 * Sinais usados: `startedAt`/`submittedAt` da execução (âncora do total
 * decorrido, que sobrevive a reload/retomada) + contagem de respondidas.
 * O `time_spent_seconds` por questão já é coletado no POST /attempts para
 * auditoria do fato — o ritmo NÃO cria coleta nova, só exibe o agregado
 * da sessão. Nenhum fetch aqui; a view injeta os números e renderiza texto.
 *
 * Sem innerHTML (só textContent via el() na view). Sem enum cru.
 */

import { el } from "./ui.js";

/** Duração oficial por edição em minutos (capa do caderno — ver cabeçalho). */
export const OFFICIAL_DURATION_MINUTES_BY_YEAR = Object.freeze({
  2020: 240,
  2022: 240,
  2023: 240,
  2024: 240,
  2025: 240,
  2026: 240,
});

/** Extrai o ano da edição de um `SimulationAttemptResponse`.
 * Ordem: título "Simulado Edição YYYY…" → ano-fonte mais frequente do
 * caderno → null (DESCONHECIDO). Só vale para REAL_EDITION; por disciplina
 * sempre retorna null (recorte sem tempo oficial). */
export function resolveEditionYear(attempt) {
  if (String(attempt?.type || "").toUpperCase() !== "REAL_EDITION") return null;
  const title = String(attempt?.title || "");
  const fromTitle = title.match(/edi[cç][aã]o\s+(\d{4})/i);
  if (fromTitle) {
    const year = Number.parseInt(fromTitle[1], 10);
    if (Number.isFinite(year)) return year;
  }
  const counts = new Map();
  for (const q of attempt?.questions ?? []) {
    const y = Number(q?.sourceYear);
    if (Number.isFinite(y) && y > 0) counts.set(y, (counts.get(y) || 0) + 1);
  }
  let best = null;
  let bestCount = 0;
  for (const [year, count] of counts) {
    if (count > bestCount) {
      best = year;
      bestCount = count;
    }
  }
  return best;
}

/** Segundos decorridos entre um ISO e `nowMs` (nunca negativo, nunca NaN). */
export function elapsedSecondsSince(startedAtIso, nowMs) {
  const start = new Date(startedAtIso).getTime();
  const now = Number(nowMs);
  if (!Number.isFinite(start) || !Number.isFinite(now)) return 0;
  return Math.max(0, Math.floor((now - start) / 1000));
}

/** Segundos decorridos da execução: encerrada usa `submittedAt` como fim
 * (placar congelado); em andamento usa `nowMs`. */
export function executionElapsedSeconds(attempt, nowMs) {
  const closed = String(attempt?.status || "").toUpperCase() !== "IN_PROGRESS";
  if (closed && attempt?.submittedAt) {
    const start = new Date(attempt.startedAt).getTime();
    const end = new Date(attempt.submittedAt).getTime();
    if (Number.isFinite(start) && Number.isFinite(end)) {
      return Math.max(0, Math.floor((end - start) / 1000));
    }
  }
  return elapsedSecondsSince(attempt?.startedAt, nowMs);
}

/** Referência em segundos/questão (duração oficial ÷ total) ou null. */
export function referenceSecondsPerQuestion(total, editionYear) {
  const minutes = OFFICIAL_DURATION_MINUTES_BY_YEAR[editionYear];
  if (!Number.isFinite(minutes) || !Number.isFinite(total) || total <= 0) return null;
  return (minutes * 60) / total;
}

/** "00:23:10" (sempre HH:MM:SS, tabular para o cronômetro não pular). */
export function formatElapsed(totalSeconds) {
  const s = Math.max(0, Math.floor(Number(totalSeconds) || 0));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const rest = s % 60;
  const pad = (n) => String(n).padStart(2, "0");
  return `${pad(h)}:${pad(m)}:${pad(rest)}`;
}

/** "6,0 min/questão" em pt-BR ou "—" quando sem base. */
export function formatMinutesPerQuestion(secondsPerQuestion) {
  if (!Number.isFinite(secondsPerQuestion) || secondsPerQuestion <= 0) return "—";
  const minutes = secondsPerQuestion / 60;
  return `${minutes.toLocaleString("pt-BR", { maximumFractionDigits: 1 })} min/questão`;
}

/** Resumo puro do ritmo. Nunca inventa referência: sem ano no mapa,
 * `hasReference` é false e a linha carrega o aviso honesto. */
export function summarizePace({ attempt, answered, total, elapsedSeconds }) {
  const safeTotal = Number.isFinite(total) && total > 0 ? total : 0;
  const safeAnswered = Number.isFinite(answered) && answered > 0
    ? Math.min(Math.floor(answered), safeTotal || Math.floor(answered))
    : 0;
  const safeElapsed = Math.max(0, Math.floor(Number(elapsedSeconds) || 0));
  const editionYear = resolveEditionYear(attempt);
  const reference = referenceSecondsPerQuestion(safeTotal, editionYear);
  const average = safeAnswered > 0 ? safeElapsed / safeAnswered : null;
  const hasReference = Number.isFinite(reference) && reference > 0;

  let line;
  const head = `Questão ${safeAnswered} de ${safeTotal} · ${formatElapsed(safeElapsed)} decorridos`;
  const avgText = average === null
    ? "sem média ainda"
    : `sua média ${formatMinutesPerQuestion(average)}`;
  if (hasReference) {
    line = `${head} · ${avgText} · referência da edição ${editionYear}: ${formatMinutesPerQuestion(reference)}`;
  } else {
    line = `${head} · ${avgText} · ritmo informativo, sem tempo oficial confirmado`;
  }
  return {
    answered: safeAnswered,
    total: safeTotal,
    elapsedSeconds: safeElapsed,
    editionYear,
    hasReference,
    referenceSecondsPerQuestion: hasReference ? reference : null,
    averageSecondsPerQuestion: average,
    line,
  };
}

/** Renderiza o ritmo num `<p>`/`<small>` já existente (só textContent).
 * Devolve o resumo para a view (útil em testes manuais no console). */
export function renderPace(target, input) {
  if (!target) return null;
  const summary = summarizePace(input);
  target.textContent = "";
  // Linha principal (cronômetro) + detalhe honesto em <small> quando o alvo
  // for um <p>: a view passa o <small id="sim-pace">, então aqui basta texto.
  target.appendChild(el("span", { text: summary.line }));
  return summary;
}
