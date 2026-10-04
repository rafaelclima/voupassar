/**
 * VouPassar — formatação básica de expressões matemáticas para questões oficiais.
 * Não inventa conteúdo: só converte texto simples (ex.: "121𝑥/50") para HTML básico
 * que o aluno consiga ler claramente. Se a expressão não seguir o padrão, retorna
 * o texto original sem alteração.
 */

/** Sanitiza uma string para permitir apenas tags matemáticas seguras. */
function sanitizeMathHtml(html) {
  const allowedTags = ["span", "sub", "sup", "br"];
  const temp = document.createElement("template");
  temp.innerHTML = html;
  const clean = (node) => {
    if (node.nodeType === Node.TEXT_NODE) return node.cloneNode();
    if (node.nodeType !== Node.ELEMENT_NODE) return null;
    if (!allowedTags.includes(node.tagName.toLowerCase())) {
      // Se não é uma tag permitida, retorna o texto dos filhos (sem a tag)
      const text = Array.from(node.childNodes)
        .map(clean)
        .filter(Boolean)
        .map((n) => (n.nodeType === Node.TEXT_NODE ? n.textContent : ""))
        .join("");
      return document.createTextNode(text);
    }
    const clone = node.cloneNode(false);
    node.childNodes.forEach((child) => {
      const c = clean(child);
      if (c) clone.appendChild(c);
    });
    return clone;
  };
  const wrapper = document.createElement("span");
  Array.from(temp.content.childNodes).forEach((n) => {
    const c = clean(n);
    if (c) wrapper.appendChild(c);
  });
  return wrapper.innerHTML;
}

/**
 * Interpreta uma expressão matemática simples (texto) e retorna HTML básico.
 * Padrões suportados (sem inventar):
 * - Fração simples: "numerador/denominador" -> <span class="fraction">...
 * - Variável com índice: "𝑥", "𝑦" preservadas como texto.
 * - Se não reconhecer o padrão, retorna o texto original.
 */
export function formatExpression(text) {
  if (typeof text !== "string") return String(text || "");
  const trimmed = text.trim();
  if (!trimmed.includes("/")) return trimmed;

  // Divide apenas no primeiro "/" que separa numerador e denominador.
  const [numPart, ...denParts] = trimmed.split("/");
  const denPart = denParts.join("/").trim();
  const numClean = numPart.trim();

  // Se não parece uma fração matemática legível, retorna original.
  if (!numClean || !denPart || numClean.length === 0 || denPart.length === 0) {
    return trimmed;
  }

  // Monta HTML simples: numerador sobre denominador com uma linha horizontal.
  return `<span class="fraction"><span class="fraction__num">${numClean}</span><span class="fraction__line"></span><span class="fraction__den">${denPart}</span></span>`;
}

/** Versão que retorna texto simples (sem HTML) se não reconhecer padrão. */
export function safeTextExpression(text) {
  return formatExpression(text);
}
