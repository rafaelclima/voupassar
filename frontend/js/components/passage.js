/* VouPassar — componente de texto-base (TASK 6.9 + docs/passagens-estrategia.md + F.2 EAJ)
 * Renderiza os textos-base de uma questão oficial (q.passages[] da API)
 * como painéis expansíveis (<details> nativo: teclado + leitor de tela
 * sem JS extra, sem animação — respeita prefers-reduced-motion por
 * construção). Colapsado por padrão; o aluno abre quando precisa.
 * Sem textContent inventado: só transcrição literal da API; quando a
 * passagem é puramente visual, exibe a descrição verificada + fonte.
 * Parte do enunciado: aparece nos modos Estudo, Prova e Revisão.
 * Trilha EAJ (F.2): as passagens EAJ chegam no mesmo formato (B.4/D.4),
 * mas sem source_note/páginas no `.md` (página DESCONHECIDA, §4) — o
 * crédito usa fallback honesto EAJ/UFRN (Comperve) em vez de omitir a fonte.
 */

import { el } from "./ui.js";

const KIND_TEXTUAL = new Set(["TEXTO", "TRECHO", "TABELA", "GRAFICO"]);

function toggleLabel(p) {
  const what = p.content && KIND_TEXTUAL.has(p.kind) ? "texto" : "imagem";
  return `Mostrar ${what}: ${p.label || "texto-base"}`;
}

function pageRef(p) {
  if (typeof p.pageStart !== "number") return null;
  return p.pageEnd && p.pageEnd !== p.pageStart
    ? `páginas ${p.pageStart}–${p.pageEnd} do caderno`
    : `página ${p.pageStart} do caderno`;
}

/* Processo da questão (mesma regra do figure.js): EAJ em qualquer caixa,
 * default IFRN para registros legados sem institution. */
function passageInstitution(q) {
  const v = String(q?.institution || "").trim().toUpperCase();
  return v === "EAJ" ? "EAJ" : "IFRN";
}

/* Crédito de fallback quando a API não traz sourceNote nem páginas
 * (caso das passagens EAJ: `.md` sem página, B.4 — nunca inventar número).
 * IFRN mantém o comportamento legado (sem linha quando sem fonte);
 * EAJ exibe a fonte honesta com página DESCONHECIDA. Exportado p/ teste. */
export function passageCreditFallback(q) {
  if (passageInstitution(q) !== "EAJ") return null;
  const year = q?.examYear ?? "—";
  return `EAJ/UFRN (Comperve) — Caderno EAJ-${year}, página DESCONHECIDA`;
}

function appendParas(body, content) {
  for (const chunk of String(content).split(/\n\s*\n/)) {
    const text = chunk.trim();
    if (text) body.appendChild(el("p", { className: "passage__para", text }));
  }
}

/* Cria um <details> por passagem e anexa ao container. Sem passagens: null. */
export function renderPassages(q, container) {
  const list = Array.isArray(q?.passages) ? q.passages : [];
  if (!container || list.length === 0) return null;
  const wrap = el("div", { className: "passage-list" });
  for (const p of list) {
    const details = el("details", { className: "passage" });
    details.appendChild(el("summary", { className: "btn btn--ghost btn--sm passage__toggle", text: toggleLabel(p) }));
    const body = el("div", { className: "passage__body" });
    body.appendChild(el("p", { className: "passage__label", text: p.label || "Texto-base" }));
    if (p.title) body.appendChild(el("p", { className: "passage__title", text: p.title }));
    if (p.byline) body.appendChild(el("p", { className: "passage__byline muted", text: p.byline }));
    if (p.subtitle) body.appendChild(el("p", { className: "passage__subtitle", text: p.subtitle }));
    if (p.intro) body.appendChild(el("p", { className: "passage__intro", text: p.intro }));
    if (p.content) {
      appendParas(body, p.content);
    } else if (p.visualDescription) {
      body.appendChild(el("p", { className: "passage__para", text: p.visualDescription }));
      body.appendChild(
        el("p", {
          className: "muted",
          text: "O recorte da imagem original do caderno aparece na fase de figuras; acima está a descrição verificada.",
        }),
      );
    }
    if (p.formatNote) body.appendChild(el("p", { className: "muted", text: `No caderno: ${p.formatNote}` }));
    const ref = pageRef(p);
    const credit = [p.sourceNote, ref ? `(${ref})` : null].filter(Boolean).join(" ");
    const fallback = !credit ? passageCreditFallback(q) : null;
    if (credit) body.appendChild(el("p", { className: "passage__credit muted", text: `Fonte: ${credit}` }));
    else if (fallback) body.appendChild(el("p", { className: "passage__credit muted", text: `Fonte: ${fallback}` }));
    details.appendChild(body);
    wrap.appendChild(details);
  }
  container.appendChild(wrap);
  return wrap;
}
