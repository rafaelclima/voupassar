/* VouPassar — componente de figura (TASK 6.6 + docs/figuras-estrategia.md)
 * Resolve a figura de uma questão oficial a partir do manifest.json.
 * Nunca inventa conteúdo: se o arquivo não existe, volta para o aviso.
 * Se `publicationStatus` é PENDENTE_REVISAO, ainda mostra a imagem
 * (se existir) com aviso de curadoria no caption — não bloqueia o aluno.
 */

import { el } from "./ui.js";

const MANIFEST_URL = "./assets/figures/manifest.json";
let manifestCache = null;

async function loadManifest() {
  if (manifestCache) return manifestCache;
  try {
    const res = await fetch(MANIFEST_URL, { cache: "no-cache" });
    if (!res.ok) return null;
    manifestCache = await res.json();
    return manifestCache;
  } catch {
    return null;
  }
}

function resolveKey(q) {
  // Convenção natural: (ano-fonte, número na prova oficial) → manifest
  if (q.examYear != null && q.questionNumber != null) {
    return `${q.examYear}-${q.questionNumber}`;
  }
  // Fallback para registro sem fonte oficial: usa id como referência
  // (não deve encontrar arquivo, mas preserva comportamento auditável)
  return null;
}

export function buildFigureUrl(manifest, key, index = 0) {
  if (!manifest || !key || !manifest.figures || !manifest.figures[key]) return null;
  const entry = manifest.figures[key];
  const files = Array.isArray(entry.files) ? entry.files : [];
  const file = files[index];
  if (!file) return null;
  const base = manifest.base || "./assets/figures";
  // Caminho relativo ao arquivo atual (assume que manifest.base é relativo)
  return base.replace("./assets/figures", "assets/figures") + "/" + file;
}

export function getFigureMeta(manifest, key) {
  if (!manifest || !key || !manifest.figures || !manifest.figures[key]) return null;
  return manifest.figures[key];
}

/* Cria o elemento <figure> para uma questão oficial com recorte publicado. */
export async function renderFigure(q, container) {
  if (!q || !q.hasFigure) {
    // Se não tem figura no banco: nada a exibir (mantém comportamento anterior)
    return null;
  }

  const key = resolveKey(q);
  const manifest = await loadManifest();
  const meta = getFigureMeta(manifest, key);
  const url = buildFigureUrl(manifest, key, 0);

  // Sempre renderiza algum feedback visual quando hasFigure=true,
  // mesmo que ainda não tenha arquivo publicado.
  const figureEl = el("figure", { className: "qfigure" });

  if (url) {
    const img = el("img", {
      className: "qfigure__img",
      attrs: {
        src: url,
        alt: meta?.alt || `Figura da questão ${q.questionNumber || q.id} (revisão visual pendente).`,
        loading: "lazy",
        "aria-describedby": `qfigure-caption-${q.id || ""}`,
      },
    });
    // Fallback se a imagem quebrar: volta para aviso, nunca deixa vazio
    img.addEventListener("error", () => {
      img.hidden = true;
      const fallback = el("p", {
        className: "muted",
        text: meta?.status === "PENDENTE_REVISAO"
          ? "Figura em curadoria: o recorte está disponível, mas passa por revisão visual antes de ser confirmado."
          : "Esta questão possui figura no caderno original (consulte o PDF-fonte).",
      });
      figureEl.appendChild(fallback);
    }, { once: true });

    figureEl.appendChild(img);
  } else {
    // Sem arquivo publicado: aviso explícito com referência de localização
    const msg = meta
      ? `Figura ainda em curadoria (${meta.status}): ver caderno ${q.examYear}, página ${meta.page || "—"}.`
      : `Esta questão possui figura no caderno original (prova ${q.examYear || "desconhecida"}, página ${q.pageStart || "—"} — consulte o PDF-fonte).`;
    const notice = el("p", { className: "qfigure__notice muted", text: msg });
    figureEl.appendChild(notice);
  }

  // Caption com proveniência e referência ao caderno (sempre presente)
  const captionText = meta?.credit || `Fonte: IFRN — Caderno ${q.examYear || "—"}, ` +
    `p. ${meta?.page || (q.pageStart ? String(q.pageStart) : "—")} (recorte para estudo).`;
  const caption = el("figcaption", {
    className: "qfigure__caption",
    attrs: { id: `qfigure-caption-${q.id || ""}` },
  });
  caption.appendChild(el("span", { text: captionText }));
  if (meta && meta.status === "PENDENTE_REVISAO") {
    caption.appendChild(el("span", {
      className: "badge badge--warning",
      text: "Revisão visual pendente",
    }));
  }
  figureEl.appendChild(caption);

  container.appendChild(figureEl);
  return figureEl;
}

/* Versão síncrona para telas que não querem await (ex.: estudos/simulado). */
export function injectFigureNotice(q, container) {
  if (!q || !q.hasFigure) return;
  const elToFind = container ? container.querySelector(".question-card__figure, .sim-card__figure, #questao-figure") : document.getElementById("questao-figure");
  const target = elToFind || container;
  if (!target) return;
  const manifest = manifestCache || null;
  const meta = manifest ? getFigureMeta(manifest, resolveKey(q)) : null;
  const msg = meta
    ? `Figura ainda em curadoria (${meta.status}): ver caderno ${q.examYear}, página ${meta.page || "—"}.`
    : `Esta questão possui figura no caderno original (prova ${q.examYear || "desconhecida"}, página ${q.pageStart || "—"}).`;
  if (target && target.nodeType === 1) {
    target.textContent = msg;
    target.hidden = false;
  } else if (target && target.nodeName === "P") {
    target.textContent = msg;
    target.hidden = false;
  }
}
