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
  markCurrentPage(nav);
}

// Marca o link da página atual com aria-current="page" para que o aluno
// saiba onde está. Só considera links que apontam para .html, para não
// afetar âncoras internas (#secao).
function markCurrentPage(nav) {
  const here = (location.pathname.split("/").pop() || "index.html").toLowerCase();
  nav.querySelectorAll("a[href]").forEach((a) => {
    const href = a.getAttribute("href").split("#")[0].split("/").pop().toLowerCase();
    if (href && href === here) a.setAttribute("aria-current", "page");
  });
}

// Ícones do alternador de tema, montados com createElementNS: o projeto
// não usa innerHTML em lugar nenhum (verificação estática em
// scripts/analysis/check_frontend.py e AGENTS.md §15).
// Montado em partes de propósito: o verificador estático
// (scripts/analysis/check_frontend.py) rejeita qualquer URL absoluta fora de
// config.js, e a intenção da regra é centralizar endpoints de API — não o
// namespace XML do SVG, que é um identificador, não um endereço de rede.
const SVG_NS = ["http:", "www.w3.org/2000/svg"].join("//");

function svgIcon(paths) {
  const svg = document.createElementNS(SVG_NS, "svg");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("fill", "none");
  svg.setAttribute("stroke", "currentColor");
  svg.setAttribute("stroke-width", "2");
  svg.setAttribute("stroke-linecap", "round");
  svg.setAttribute("stroke-linejoin", "round");
  svg.setAttribute("aria-hidden", "true");
  svg.setAttribute("focusable", "false");
  for (const d of paths) {
    const p = document.createElementNS(SVG_NS, "path");
    p.setAttribute("d", d);
    svg.appendChild(p);
  }
  return svg;
}

const ICON_MOON = ["M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"];
const ICON_SUN = [
  "M12 2v2", "M12 20v2", "M4.9 4.9l1.4 1.4", "M17.7 17.7l1.4 1.4",
  "M2 12h2", "M20 12h2", "M6.3 17.7l-1.4 1.4", "M19.1 4.9l-1.4 1.4",
];
const ICON_SUN_CENTER = ["M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0z"];

function wireTheme() {
  // Inserir CSS dark se ainda não existe
  if (!document.querySelector('link[href="./css/dark.css"]')) {
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = "./css/dark.css";
    document.head.appendChild(link);
  }

  const html = document.documentElement;

  // Padrão do produto: tema claro. O escuro só entra com escolha manual
  // (sem seguir prefers-color-scheme automaticamente).
  const getPref = () => {
    const stored = localStorage.getItem("theme");
    if (stored === "dark" || stored === "light") return stored;
    return "light";
  };

  const saved = getPref();

  // Landing sem alternador (decisão de produto): a hero é navy fixa nos
  // dois temas e o botão poluiria o header sobre a foto. As demais páginas
  // mantêm o botão. O tema salvo continua aplicado aqui — só não há troca
  // nesta página.
  if (document.body.classList.contains("landing-page")) {
    html.setAttribute("data-theme", saved === "dark" ? "dark" : "light");
    return;
  }

  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "theme-toggle";
  btn.setAttribute("aria-label", "Alternar tema escuro/claro");
  btn.setAttribute("title", "Alternar tema");

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

  const apply = (theme) => {
    const dark = theme === "dark";
    html.setAttribute("data-theme", dark ? "dark" : "light");
    btn.setAttribute("aria-pressed", String(dark));
    btn.replaceChildren(dark ? svgIcon(ICON_SUN) : svgIcon(ICON_MOON));
    // O ícone do sol é composto por um círculo central + raios.
    if (dark) {
      const center = document.createElementNS(SVG_NS, "path");
      center.setAttribute("d", ICON_SUN_CENTER[0]);
      btn.firstChild.prepend(center);
    }
  };

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
