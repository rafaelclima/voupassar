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
  btn.addEventListener("click", () => {
    const open = nav.getAttribute("data-open") === "true";
    nav.setAttribute("data-open", String(!open));
    btn.setAttribute("aria-expanded", String(!open));
    nav.classList.toggle("is-open", !open);
  });
}

setYear();
wireNav();
wireModalTriggers(document);
