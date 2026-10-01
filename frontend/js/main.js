/* VouPassar — bootstrap global (TASK 6.1)
 * Importado por todas as páginas. Sem framework, sem tracking.
 */
import { wireModalTriggers } from "./components/ui.js";

function setYear() {
  document
    .querySelectorAll("[data-current-year]")
    .forEach((n) => {
      n.textContent = String(new Date().getFullYear());
    });
}

function wireNav() {
  const btn = document.querySelector("[data-nav-toggle]");
  const nav = document.querySelector("[data-nav]");
  if (!btn || !nav) return;
  const syncLabel = (open) => {
    btn.setAttribute("aria-expanded", String(open));
    btn.setAttribute("aria-label", open ? "Fechar menu" : "Abrir menu");
  };
  btn.addEventListener("click", () => {
    const open = nav.getAttribute("data-open") === "true";
    nav.setAttribute("data-open", String(!open));
    nav.classList.toggle("is-open", !open);
    syncLabel(!open);
  });
  // Esc fecha o menu mobile e devolve o foco ao botão (teclado/SR).
  document.addEventListener("keydown", (e) => {
    if (e.key !== "Escape") return;
    if (nav.getAttribute("data-open") !== "true") return;
    // Só no layout mobile, onde o toggle está visível.
    if (btn.offsetParent === null) return;
    nav.setAttribute("data-open", "false");
    nav.classList.remove("is-open");
    syncLabel(false);
    btn.focus();
  });
}

setYear();
wireNav();
wireModalTriggers(document);
