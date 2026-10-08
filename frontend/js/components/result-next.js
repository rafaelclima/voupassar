/* VouPassar — raio-X pós-atividade (TASK 19.1)
 *
 * Bloco reutilizável de "próximos passos" após uma atividade corrigida
 * (simulado por disciplina/edição real; Estudos na TASK 19.2 reutiliza o
 * mesmo componente). Recebe o `result` do servidor
 * (`SimulationResultResponse`: placar + `items` por posição) e renderiza:
 * (a) piores assuntos da atividade, (b) erros sem acerto nesta atividade,
 * (c) no máximo 3 CTAs: "Revisar estes N agora" (`POST /review/sessions`
 * via handler da view), "Praticar <assunto>" (link para os estudos com
 * recorte) e "Atualizar meu plano" (`POST /recommendations` via handler).
 *
 * Regras vindas do backend (nunca reinventadas aqui):
 * - o `result` NÃO traz assunto: o enriquecimento vem do `detailsMap`
 *   (caderno já carregado pela view: `GET /questions/{id}` traz
 *   `topic {id, code, name}`); sem detalhe, agrupa por disciplina; sem nem
 *   isso, cai no balde honesto "sem classificação" — nunca inventa assunto;
 * - erro = `!wasAnnulled && !unanswered && isCorrect === false` (o servidor
 *   já marca `BLANK` não-anulado como erro, igual ao Estudo);
 * - a sessão de revisão congela o topo da fila com o filtro do assunto
 *   (limite = nº de erros), não necessariamente as mesmas posições — o
 *   microtexto sob o botão diz isso em português claro.
 *
 * Sem fetch aqui: a view injeta `onReview`/`onPlan` (que chamam a API e
 * navegam). Sem innerHTML (só textContent via el()). Códigos da API passam
 * por `vocab.js` — nunca enum cru na tela (ver check_frontend.py).
 */

import { el } from "./ui.js";
import { disciplineLabel, topicLabel } from "../vocab.js";
import { estudosHref } from "./plan-evidence.js";

/** Máximo de CTAs do bloco (risco de sobrecarga — TASK 19.1). */
export const MAX_NEXT_ACTIONS = 3;

/** Itens-erro de um `result` (anuladas e pendentes fora, nunca erro inventado). */
export function errorItemsOf(result) {
  const items = Array.isArray(result?.items) ? result.items : [];
  return items.filter(
    (it) => it && !it.wasAnnulled && !it.unanswered && it.isCorrect === false,
  );
}

/** "Posição 3 · 2024 Q12" (só referência, sem enunciado duplicado). */
export function errorRef(item) {
  const pos = `Posição ${item?.position ?? "?"}`;
  if (item?.sourceYear && item?.sourceQuestionNumber) {
    return `${pos} · ${item.sourceYear} Q${item.sourceQuestionNumber}`;
  }
  return pos;
}

/* Resolve o assunto de um item via caderno em cache (tolerante a chave
 * numérica ou string). Devolve {topicId, topicCode, topicName,
 * disciplineCode} — nulos quando o detalhe não existe ou não classifica. */
function resolveSubject(item, detailsMap) {
  const qid = item?.questionId;
  const detail = detailsMap instanceof Map
    ? (detailsMap.get(qid) ?? detailsMap.get(String(qid)) ?? null)
    : null;
  const topic = detail?.topic || null;
  const disc = item?.disciplineCode || detail?.discipline?.code || null;
  return {
    topicId: topic?.id ?? null,
    topicCode: topic?.code ?? null,
    topicName: topic?.name || topicLabel(topic?.code) || null,
    disciplineCode: disc,
  };
}

/** Nome exibido de um grupo (português; nunca código cru nem "null"). */
export function groupDisplayName(group) {
  if (group?.topicName) return group.topicName;
  const disc = disciplineLabel(group?.disciplineCode);
  if (disc) return disc;
  return "Sem classificação";
}

/** Resumo puro do raio-X. Devolve `null` quando o `result` não tem itens. */
export function summarizeResult(result, detailsMap) {
  const items = Array.isArray(result?.items) ? [...result.items] : [];
  if (items.length === 0) return null;
  const errors = errorItemsOf(result);
  const byKey = new Map();
  for (const it of items) {
    const scored = !it?.wasAnnulled && !it?.unanswered;
    const subj = resolveSubject(it, detailsMap);
    const key = subj.topicId !== null && subj.topicId !== undefined
      ? `topic:${subj.topicId}`
      : (subj.disciplineCode ? `disc:${subj.disciplineCode}` : "unknown");
    let group = byKey.get(key);
    if (!group) {
      group = {
        key,
        topicId: subj.topicId,
        topicCode: subj.topicCode,
        topicName: subj.topicName,
        disciplineCode: subj.disciplineCode,
        errors: 0,
        total: 0,
      };
      byKey.set(key, group);
    }
    group.total += 1;
    if (scored && it.isCorrect === false) group.errors += 1;
  }
  // Determinístico: mais erros primeiro, depois mais questões, depois nome.
  const groups = [...byKey.values()].sort((a, b) =>
    (b.errors - a.errors)
    || (b.total - a.total)
    || groupDisplayName(a).localeCompare(groupDisplayName(b), "pt-BR"),
  );
  const errorCount = errors.length;
  const unansweredCount = items.filter((it) => it && !it.wasAnnulled && it.unanswered).length;
  const annulledCount = items.filter((it) => it?.wasAnnulled).length;
  return {
    total: items.length,
    scored: Number(result?.scored ?? items.length - annulledCount),
    correct: Number(result?.correct ?? 0),
    errorCount,
    unansweredCount,
    annulledCount,
    accuracy: result?.accuracy ?? null,
    errors: errors
      .slice()
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
      .map((it) => {
        const subj = resolveSubject(it, detailsMap);
        return {
          position: it.position,
          questionId: it.questionId,
          ref: errorRef(it),
          ...subj,
        };
      }),
    groups,
    topGroup: groups.find((g) => g.errors > 0) || null,
  };
}

/** Descritores dos CTAs (no máximo 3). Puro e testável, sem DOM. */
export function buildNextActions(summary) {
  if (!summary || summary.total === 0) return [];
  const actions = [];
  if (summary.errorCount > 0) {
    actions.push({
      id: "review",
      kind: "button",
      label: `Revisar estes ${summary.errorCount} agora`,
    });
    const top = summary.topGroup;
    if (top) {
      actions.push({
        id: "practice",
        kind: "link",
        label: `Praticar ${groupDisplayName(top)}`,
        href: estudosHref({
          disciplineCode: top.disciplineCode || "",
          topicId: top.topicId ?? "",
        }),
      });
    }
  }
  actions.push({ id: "plan", kind: "button", label: "Atualizar meu plano" });
  return actions.slice(0, MAX_NEXT_ACTIONS);
}

/* Linha "X erros em Y" de um grupo (números da atividade, sem inventar %). */
function groupLine(group) {
  const errs = `${group.errors} ${group.errors === 1 ? "erro" : "erros"}`;
  if (group.total > group.errors) {
    return `${errs} em ${group.total} desta atividade`;
  }
  return `${errs} nesta atividade`;
}

/** Renderiza o bloco no `container`. Devolve o `<section>` ou `null`. */
export function renderResultNext(container, summary, { onReview, onPlan } = {}) {
  if (!container || !summary || summary.total === 0) return null;
  container.textContent = "";
  const section = el("section", {
    className: "result-next",
    attrs: { "aria-labelledby": "result-next-t" },
  });
  section.appendChild(el("h3", { text: "O que fazer agora", attrs: { id: "result-next-t" } }));

  if (summary.errorCount === 0) {
    section.appendChild(el("p", {
      className: "muted",
      text: summary.unansweredCount > 0
        ? `Nenhum erro nesta atividade — restaram ${summary.unansweredCount} ${summary.unansweredCount === 1 ? "pendente" : "pendentes"} sem resposta.`
        : "Nenhum erro nesta atividade. Bom trabalho — consolide atualizando seu plano.",
    }));
  } else {
    const top = summary.topGroup;
    if (top) {
      const lead = el("p", { className: "result-next__lead" });
      lead.appendChild(el("strong", { text: `Assunto que mais pesou: ${groupDisplayName(top)} — ` }));
      lead.appendChild(el("span", { text: groupLine(top) + "." }));
      section.appendChild(lead);
    }
    const others = summary.groups.filter((g) => g.errors > 0 && g !== summary.topGroup).slice(0, 2);
    if (others.length > 0) {
      const list = el("ul", { className: "result-next__topics" });
      for (const g of others) {
        const li = el("li");
        li.appendChild(el("span", { className: "badge", text: groupDisplayName(g) }));
        li.appendChild(el("span", { text: ` ${groupLine(g)}` }));
        list.appendChild(li);
      }
      section.appendChild(list);
    }
    const errLine = el("p", { className: "muted" });
    const refs = summary.errors.slice(0, 6).map((e) => e.ref).join(" · ");
    errLine.appendChild(el("span", {
      text: summary.errors.length <= 6
        ? `Erros desta atividade (${summary.errors.length}): ${refs}.`
        : `Erros desta atividade (${summary.errors.length}, mostrando 6): ${refs}…`,
    }));
    section.appendChild(errLine);
  }

  const actions = buildNextActions(summary);
  const group = el("div", { className: "btn-group mt-4" });
  for (const action of actions) {
    if (action.kind === "link") {
      group.appendChild(el("a", {
        className: "btn btn--secondary btn--sm",
        text: action.label,
        attrs: { href: action.href },
      }));
    } else if (action.id === "review") {
      const btn = el("button", {
        className: "btn btn--primary btn--sm",
        text: action.label,
        attrs: { type: "button" },
      });
      btn.addEventListener("click", () => onReview?.(summary, btn));
      group.appendChild(btn);
    } else {
      const btn = el("button", {
        className: "btn btn--ghost btn--sm",
        text: action.label,
        attrs: { type: "button" },
      });
      btn.addEventListener("click", () => onPlan?.(summary, btn));
      group.appendChild(btn);
    }
  }
  section.appendChild(group);
  if (summary.errorCount > 0) {
    section.appendChild(el("p", {
      className: "muted mt-2",
      text: "A revisão congela o topo da sua fila neste assunto — nem sempre as mesmas posições, sempre os erros mais urgentes.",
    }));
  }
  container.appendChild(section);
  return section;
}
