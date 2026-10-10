/* VouPassar — componente de figura (TASK 6.6 + docs/figuras-estrategia.md + F.2 EAJ)
 * Resolve a figura de uma questão oficial a partir do manifest.json.
 * Chave namespaced por processo (TASK F.2): IFRN usa "<ano>-<n>"
 * (ex. "2022-15"); EAJ usa "EAJ-<ano>-<n>" (ex. "EAJ-2022-8" →
 * "eaj/2022/Q08.webp", ver B.3/D.4). Nunca inventa conteúdo: se o arquivo
 * não existe, volta para o aviso com a referência ao caderno-fonte
 * (EAJ: página DESCONHECIDA, AGENTS.md §4) — não bloqueia o aluno.
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

/* Processo da questão: "EAJ" (qualquer caixa) ou "IFRN" (default legado).
 * Questão autoral/sem edição cai no default IFRN — a chave resultante não
 * existe no manifest, o que preserva o aviso auditável sem inventar nada. */
export function figureInstitution(q) {
  const v = String(q?.institution || "").trim().toUpperCase();
  return v === "EAJ" ? "EAJ" : "IFRN";
}

export function resolveKey(q) {
  // Convenção natural: (processo, ano-fonte, número na prova oficial) → manifest.
  // IFRN: "<ano>-<n>" (ex. "2022-15"); EAJ: "EAJ-<ano>-<n>" (ex. "EAJ-2022-8").
  // O número sai sem zero à esquerda (manifest usa "8", o arquivo usa "Q08.webp").
  if (q.examYear != null && q.questionNumber != null) {
    const year = Number.parseInt(String(q.examYear), 10);
    const num = Number.parseInt(String(q.questionNumber), 10);
    if (!Number.isFinite(year) || !Number.isFinite(num)) return null;
    if (figureInstitution(q) === "EAJ") return `EAJ-${year}-${num}`;
    return `${year}-${num}`;
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
/* Figuras vindas da API (q.figures[]) têm prioridade sobre o manifest. */
function apiFigures(q) {
  const list = Array.isArray(q?.figures) ? q.figures : [];
  return list.filter((f) => f && (f.filePath || f.path));
}

function apiFileToUrl(f) {
  const raw = f.filePath || f.path || "";
  const clean = String(raw).replace(/^\.\//, "").replace(/^frontend\//, "");
  if (/^(assets\/figures\/|assets\/|https?:\/\/)/.test(clean)) return clean;
  return "assets/figures/" + clean.replace(/^\/+/, "");
}

export function preloadManifest() {
  return loadManifest();
}

/* Rótulo do caderno-fonte sem inventar página (AGENTS.md §4).
 * EAJ: páginas DESCONHECIDAS no manifest B.3 (string "DESCONHECIDA" → texto). */
export function figureProvenanceLabel(q, meta) {
  const inst = figureInstitution(q);
  const year = q?.examYear ?? meta?.year ?? null;
  if (inst === "EAJ") return `prova EAJ-${year ?? "desconhecida"}`;
  return `prova ${year ?? "desconhecida"}`;
}

function figurePageLabel(q, meta) {
  const raw = meta?.page ?? q?.pageStart ?? null;
  if (typeof raw === "number" && Number.isFinite(raw)) return String(raw);
  if (typeof raw === "string" && raw.trim() && raw.trim() !== "—") return raw.trim();
  // EAJ sem página: marca DESCONHECIDA (nunca inventa número — §4).
  // IFRN legado sem página: mantém o traço neutro.
  return figureInstitution(q) === "EAJ" ? "DESCONHECIDA" : "—";
}

/* Crédito do figcaption: o manifest manda (já traz EAJ/Comperve nas chaves
 * EAJ-*); sem entrada no manifest, fallback honesto por processo. */
export function figureCredit(q, meta) {
  if (meta?.credit) return meta.credit;
  const page = figurePageLabel(q, meta);
  if (figureInstitution(q) === "EAJ") {
    const year = q?.examYear ?? "—";
    return `Fonte: EAJ/UFRN (Comperve) — Caderno EAJ-${year}, página ${page} (recorte para estudo).`;
  }
  const year = q?.examYear ?? "—";
  return `Fonte: IFRN — Caderno ${year}, p. ${page} (recorte para estudo).`;
}

/* Aviso quando não há recorte publicado: referência ao caderno, nunca imagem
 * inventada (mesmo comportamento IFRN; EAJ com página DESCONHECIDA). */
export function figureNotice(q, meta) {
  if (meta) {
    return `Figura indisponível neste recorte: ver caderno ${figureInstitution(q) === "EAJ" ? `EAJ-${q.examYear}` : q.examYear}, página ${figurePageLabel(q, meta)}.`;
  }
  return `Esta questão possui figura no caderno original (${figureProvenanceLabel(q, meta)}, página ${figurePageLabel(q, meta)} — consulte o PDF-fonte).`;
}

export async function renderFigure(q, container) {
  if (!q || (!q.hasFigure && apiFigures(q).length === 0)) {
    // Se não tem figura no banco nem na API: nada a exibir
    return null;
  }

  const fromApi = apiFigures(q);
  const key = resolveKey(q);
  const manifest = fromApi.length > 0 ? await loadManifest().catch(() => null) : await loadManifest();
  const meta = getFigureMeta(manifest, key);
  const manifestUrl = buildFigureUrl(manifest, key, 0);
  const manifestFiles = Array.isArray(meta?.files) ? meta.files : [];
  const url = fromApi.length > 0 ? null : manifestUrl;

  // Monta a lista de imagens: API primeiro, depois todos os files do manifest.
  const base = (manifest && manifest.base) || "./assets/figures";
  const items = [];
  for (const f of fromApi) {
    items.push({
      src: apiFileToUrl(f),
      alt: f.altText || f.alt || meta?.alt || `Figura da questão ${q.questionNumber || q.id} (ver caderno-fonte).`,
      page: f.page ?? meta?.page ?? q.pageStart ?? null,
    });
  }
  if (items.length === 0) {
    manifestFiles.forEach((file) => {
      items.push({
        src: base.replace("./assets/figures", "assets/figures") + "/" + file,
        alt: meta?.alt || `Figura da questão ${q.questionNumber || q.id} (ver caderno-fonte).`,
        page: meta?.page ?? q.pageStart ?? null,
      });
    });
  }

  const figureEl = el("figure", { className: "qfigure" });
  if (items.length === 0) {
    figureEl.appendChild(el("p", { className: "qfigure__notice muted", text: figureNotice(q, meta) }));
  } else {
    items.forEach((item, i) => {
      const img = el("img", {
        className: "qfigure__img",
        attrs: {
          src: item.src,
          alt: item.alt,
          loading: "lazy",
          "aria-describedby": `qfigure-caption-${q.id || ""}`,
        },
      });
      img.addEventListener("error", () => {
        img.hidden = true;
        figureEl.appendChild(el("p", {
          className: "muted",
          text: figureNotice(q, meta),
        }));
      }, { once: true });
      figureEl.appendChild(img);
      if (i < items.length - 1) figureEl.appendChild(el("br", {}));
    });
  }

  const captionText = figureCredit(q, meta);
  const caption = el("figcaption", {
    className: "qfigure__caption",
    attrs: { id: `qfigure-caption-${q.id || ""}` },
  });
  caption.appendChild(el("span", { text: captionText }));
  figureEl.appendChild(caption);

  container.appendChild(figureEl);
  return figureEl;
}

/* Figura expansível ("Mostrar figura") para Estudos/Simulado: economiza scroll no mobile. */
export function mountExpandableFigure(q, className) {
  const details = el("details", { className: className || "qfigure-details" });
  const summary = document.createElement("summary");
  summary.className = "qfigure-details__summary";
  summary.textContent = "Mostrar figura";
  details.appendChild(summary);
  const body = el("div", { className: "qfigure-details__body" });
  body.appendChild(el("p", { className: "muted", text: "Carregando figura…" }));
  details.appendChild(body);
  renderFigure(q, body).then(() => {
    const loading = body.querySelector("p.muted");
    if (loading && body.querySelector("figure")) loading.remove();
  }).catch(() => {
    body.textContent = "Não foi possível carregar a figura agora.";
  });
  return details;
}

/* Versão síncrona para telas que não querem await (ex.: estudos/simulado). */
export function injectFigureNotice(q, container) {
  if (!q || !q.hasFigure) return;
  const elToFind = container ? container.querySelector(".question-card__figure, .sim-card__figure, #questao-figure") : document.getElementById("questao-figure");
  const target = elToFind || container;
  if (!target) return;
  const manifest = manifestCache || null;
  const meta = manifest ? getFigureMeta(manifest, resolveKey(q)) : null;
  const msg = figureNotice(q, meta);
  if (target && target.nodeType === 1) {
    target.textContent = msg;
    target.hidden = false;
  } else if (target && target.nodeName === "P") {
    target.textContent = msg;
    target.hidden = false;
  }
}
