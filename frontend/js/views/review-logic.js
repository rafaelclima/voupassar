/* VouPassar — lógica pura da execução de revisão (TASK 17.3)
 *
 * Vereditos e resumos da sessão de revisão sem DOM nem fetch, para que a
 * regra exibida na tela tenha teste automatizado (`node
 * scripts/analysis/test_review_logic.mjs`). Nenhuma regra de negócio nova:
 * só traduz o fato do servidor em tom/selo/texto em português.
 *
 * Regras vindas do backend (nunca reinventadas aqui):
 * - `BLANK` conta como erro quando a questão não é anulada (AttemptService);
 * - anulada fica fora do aproveitamento (`wasAnnulled`, `isCorrect` NULL);
 * - sem resposta = pendente, nunca erro inventado;
 * - `accuracy` NULL = sem tentativa pontuável (nunca zero inventado).
 */

/** Veredito de uma resposta REVISAO (POST /attempts ou item de sessão).
 *  `isCorrect` NULL fora de anulada não deveria acontecer (REVISAO nunca
 *  oculta) — se chegar, sai como correção indisponível, nunca chute. */
export function attemptVerdict({ wasAnnulled = false, isCorrect = null } = {}) {
  if (wasAnnulled) {
    return {
      tone: "warning",
      badge: "Anulada",
      title: "Questão anulada — fora do aproveitamento.",
    };
  }
  if (isCorrect === true) {
    return { tone: "success", badge: "Acertou", title: "Você acertou." };
  }
  if (isCorrect === false) {
    return { tone: "danger", badge: "Errou", title: "Não foi dessa vez." };
  }
  return {
    tone: "muted",
    badge: "Sem correção",
    title: "Correção indisponível no momento.",
  };
}

/** Veredito de uma posição do resultado (`GET …/review/sessions/{id}/result`).
 *  Sem resposta (`unanswered`) é pendência explícita, nunca erro. */
export function resultItemVerdict({ wasAnnulled = false, unanswered = false, isCorrect = null } = {}) {
  if (wasAnnulled) {
    return {
      tone: "warning",
      badge: "Anulada",
      title: "Anulada — fora do aproveitamento.",
    };
  }
  if (unanswered) {
    return {
      tone: "pending",
      badge: "Pendente",
      title: "Não respondida.",
    };
  }
  if (isCorrect === true) {
    return { tone: "hit", badge: "Acertou", title: "Acertou." };
  }
  if (isCorrect === false) {
    return { tone: "miss", badge: "Errou", title: "Errou." };
  }
  return {
    tone: "pending",
    badge: "Pendente",
    title: "Sem correção registrada.",
  };
}

/** Aproveitamento do placar: NULL/NaN vira "—" (sem pontuável, sem número
 *  inventado). Espelha o `formatPercent` do simulado. */
export function formatReviewAccuracy(acc) {
  if (acc === null || acc === undefined) return "—";
  const n = Number(acc);
  if (!Number.isFinite(n)) return "—";
  return `${(n * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%`;
}

/** "N de M respondidas" do progresso da sessão. */
export function reviewProgressLabel(answered, total) {
  const a = Number(answered) || 0;
  const t = Number(total) || 0;
  return `${a} de ${t} respondidas`;
}

/** A sessão aceita encerramento apenas em andamento. */
export function canFinishReview(status) {
  return String(status || "").toUpperCase() === "IN_PROGRESS";
}
