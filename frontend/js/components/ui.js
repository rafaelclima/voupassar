/* VouPassar — componentes JS (TASK 6.1)
 * Helpers acessíveis sem framework: toast, modal (<dialog>), loading,
 * empty-state, erros de formulário. Sempre textContent (nunca innerHTML
 * com dados) — AGENTS.md §15 (XSS).
 */

/** Cria elemento com texto seguro. */
export function el(tag, { text, className, attrs = {} } = {}) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined && text !== null) node.textContent = text;
  for (const [k, v] of Object.entries(attrs)) {
    if (v === null || v === undefined || v === false) continue;
    node.setAttribute(k, v === true ? "" : String(v));
  }
  return node;
}

/** Escapa para os raros casos em que string HTML é inevitável (nunca dados). */
export function escapeHtml(s) {
  return String(s ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;",
  })[c]);
}

// ---------- Toast ----------
let toastRegion = null;

function ensureToastRegion() {
  if (toastRegion && document.contains(toastRegion)) return toastRegion;
  toastRegion = document.createElement("div");
  toastRegion.className = "toast-region";
  toastRegion.setAttribute("role", "status");
  toastRegion.setAttribute("aria-live", "polite");
  document.body.appendChild(toastRegion);
  return toastRegion;
}

/** toast("Simulado salvo", "success") — some sozinho em 4s. */
export function toast(message, type = "info") {
  const region = ensureToastRegion();
  const node = el("div", { className: "toast", text: String(message) });
  node.dataset.type = type;
  const close = el("button", {
    text: "×",
    className: "icon-btn",
    attrs: { type: "button", "aria-label": "Fechar aviso" },
  });
  // Alvo de toque herdado de .icon-btn (44px, WCAG 2.5.8): sem override
  // menor aqui. Só empurra para a borda do toast.
  close.style.cssText = "margin-left:auto;color:#fff;border-color:transparent;background:transparent";
  close.addEventListener("click", () => node.remove());
  node.appendChild(close);
  region.appendChild(node);
  setTimeout(() => node.remove(), 4000);
  return node;
}

// ---------- Modal (dialog nativo) ----------
let lastModalTrigger = null;

/** Abre <dialog id> com foco no primeiro botão; Esc fecha (nativo). */
export function openModal(id) {
  const dlg = document.getElementById(id);
  if (!(dlg instanceof HTMLDialogElement)) {
    console.error(`[ui] modal #${id} não encontrado`);
    return null;
  }
  lastModalTrigger = document.activeElement instanceof HTMLElement ? document.activeElement : null;
  if (!dlg.open) dlg.showModal();
  const focusable = dlg.querySelector("button, [href], input, select, textarea, [tabindex]:not([tabindex='-1'])");
  focusable?.focus();
  // Devolve o foco a quem abriu quando o diálogo fechar (Esc, backdrop ou
  // botão): sem isso o foco cai para <body> e o teclado "perde" a posição.
  dlg.addEventListener("close", () => {
    lastModalTrigger?.focus?.();
    lastModalTrigger = null;
  }, { once: true });
  return dlg;
}

export function closeModal(id) {
  const dlg = document.getElementById(id);
  if (dlg instanceof HTMLDialogElement && dlg.open) dlg.close();
}

export function wireModalTriggers(root = document) {
  root.querySelectorAll("[data-open-modal]").forEach((btn) => {
    btn.addEventListener("click", () => openModal(btn.getAttribute("data-open-modal")));
  });
  root.querySelectorAll("dialog[data-modal] [data-close-modal]").forEach((btn) => {
    btn.addEventListener("click", () => btn.closest("dialog")?.close());
  });
}

// ---------- Loading em botão ----------
/** Alterna estado "carregando" preservando o rótulo para leitores de tela. */
export function setButtonLoading(button, loading, loadingText = "Carregando…") {
  if (!(button instanceof HTMLElement)) return;
  if (loading) {
    if (!button.dataset.label) button.dataset.label = button.textContent.trim();
    button.setAttribute("disabled", "");
    button.setAttribute("aria-busy", "true");
    button.textContent = "";
    const spin = el("span", { className: "spinner spinner--sm", attrs: { "aria-hidden": "true" } });
    button.appendChild(spin);
    button.appendChild(el("span", { text: ` ${loadingText}` }));
  } else {
    button.removeAttribute("disabled");
    button.removeAttribute("aria-busy");
    button.textContent = button.dataset.label || "Enviar";
    delete button.dataset.label;
  }
}

// ---------- Estado vazio ----------
export function renderEmpty(container, { title, description, actionLabel, onAction } = {}) {
  container.textContent = "";
  const box = el("div", { className: "empty" });
  box.appendChild(el("h3", { text: title || "Nada por aqui ainda" }));
  if (description) box.appendChild(el("p", { text: description }));
  if (actionLabel) {
    const btn = el("button", { className: "btn btn--primary", text: actionLabel, attrs: { type: "button" } });
    btn.addEventListener("click", () => onAction?.());
    box.appendChild(btn);
  }
  container.appendChild(box);
  return box;
}

// ---------- Erros de formulário ----------
/** Marca input inválido + mensagem (usa aria-describedby + aria-invalid). */
export function setFieldError(input, message) {
  if (!(input instanceof HTMLElement)) return;
  const field = input.closest(".field") || input.parentElement;
  let err = field?.querySelector(".field__error");
  if (message) {
    input.setAttribute("aria-invalid", "true");
    if (!input.id) input.id = `field-${Math.random().toString(36).slice(2, 8)}`;
    if (!err) {
      err = el("p", { className: "field__error" });
      err.id = `${input.id}-error`;
      field?.appendChild(err);
    }
    err.textContent = message;
    input.setAttribute("aria-describedby", err.id);
  } else {
    input.removeAttribute("aria-invalid");
    input.removeAttribute("aria-describedby");
    err?.remove();
  }
}

/** Resumo de erros no topo do form (foco vai para ele — WCAG 3.3.1). */
export function renderErrorSummary(container, { title, items, traceId } = {}) {
  container.textContent = "";
  const box = el("div", {
    className: "error-summary",
    attrs: { role: "alert", tabindex: "-1" },
  });
  box.appendChild(el("h2", { text: title || "Verifique os campos" }));
  if (items?.length) {
    const ul = el("ul");
    for (const item of items) ul.appendChild(el("li", { text: item }));
    box.appendChild(ul);
  }
  if (traceId) {
    const p = el("p", { className: "envelope mt-2" });
    p.appendChild(el("span", { text: "Código de rastreio: " }));
    p.appendChild(el("code", { text: traceId }));
    box.appendChild(p);
  }
  container.appendChild(box);
  box.focus();
  return box;
}
