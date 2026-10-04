/* VouPassar — expressões matemáticas simples (frações) nas alternativas.
 * Não inventa conteúdo: só apresenta o texto "numerador/denominador" como
 * fração empilhada (classes .fraction em components.css). Sem usar HTML
 * com dados em lugar nenhum: tudo via createElement + textContent
 * (AGENTS.md §15/XSS). Se não houver "/" com os dois lados preenchidos,
 * devolve o texto original como texto puro.
 */

/** Separa "numerador/denominador" (primeira barra) ou retorna null. */
function splitFraction(text) {
  const trimmed = String(text ?? "").trim();
  if (!trimmed.includes("/")) return null;
  const cut = trimmed.indexOf("/");
  const num = trimmed.slice(0, cut).trim();
  const den = trimmed.slice(cut + 1).trim();
  if (!num || !den) return null;
  return { num, den };
}

/** Monta <span class="fraction"> via DOM seguro (sem HTML com dados). */
function fractionNode(num, den) {
  const box = document.createElement("span");
  box.className = "fraction";
  const numEl = document.createElement("span");
  numEl.className = "fraction__num";
  numEl.textContent = num;
  const line = document.createElement("span");
  line.className = "fraction__line";
  line.setAttribute("aria-hidden", "true");
  const denEl = document.createElement("span");
  denEl.className = "fraction__den";
  denEl.textContent = den;
  box.appendChild(numEl);
  box.appendChild(line);
  box.appendChild(denEl);
  return box;
}

/** Retorna um <span> com a alternativa: fração montada ou texto puro. */
export function expressionNode(text) {
  const wrap = document.createElement("span");
  const frac = splitFraction(text);
  if (!frac) {
    wrap.textContent = String(text ?? "");
    return wrap;
  }
  wrap.appendChild(fractionNode(frac.num, frac.den));
  return wrap;
}
