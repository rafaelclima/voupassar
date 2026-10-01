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

function wireTheme() {
  // Inserir CSS dark se ainda não existe
  if (!document.querySelector('link[href="./css/dark.css"]')) {
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = "./css/dark.css";
    document.head.appendChild(link);
  }

  const html = document.documentElement;
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "theme-toggle";
  btn.setAttribute("aria-label", "Alternar tema escuro/claro");
  btn.setAttribute("title", "Alternar tema");
  btn.textContent = "☾";

  // Inserir na extrema direita do header, após a navegação.
  const headerInner = document.querySelector(".site-header__inner");
  if (headerInner) {
    headerInner.appendChild(btn);
  } else {
    const navLinks = document.querySelector(".site-nav__links");
    if (navLinks) {
      navLinks.insertAdjacentElement("beforebegin", btn);
    } else {
      const nav = document.querySelector(".site-nav");
      if (nav) nav.insertAdjacentElement("afterbegin", btn);
    }
  }

  const getPref = () => {
    const stored = localStorage.getItem("theme");
    if (stored === "dark" || stored === "light") return stored;
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  };

  const apply = (theme) => {
    html.setAttribute("data-theme", theme === "dark" ? "dark" : "light");
    btn.setAttribute("aria-pressed", String(theme === "dark"));
    btn.textContent = theme === "dark" ? "☀" : "☾";
  };

  const saved = getPref();
  apply(saved);

  btn.addEventListener("click", () => {
    const current = html.getAttribute("data-theme") === "dark" ? "dark" : "light";
    const next = current === "dark" ? "light" : "dark";
    localStorage.setItem("theme", next);
    apply(next);
  });
}

setYear();
wireNav();
wireTheme();
wireModalTriggers(document);
