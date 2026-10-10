/* VouPassar — seletor de processo IFRN/EAJ (TASK F.1)
 *
 * Segmented control persistente: IFRN (default, sem deslocar usuários
 * atuais) ou EAJ/UFRN. Persiste via js/state/process.js (localStorage);
 * a filtragem vive no backend (?institution=) — aqui só guarda, rotula e
 * avisa via onChange para a view recarregar na trilha.
 */

import { getInstitution, setInstitution } from "../state/process.js";
import { el } from "./ui.js";

export const INSTITUTION_LABELS = {
  IFRN: "IFRN",
  EAJ: "EAJ · UFRN",
};

/* Monta o seletor dentro de `mount`. Retorna { root, refresh }. */
export function mountProcessSelector(mount, { id = "process-selector", onChange } = {}) {
  const current = getInstitution();
  const group = el("div", { attrs: { class: "process-selector", id, role: "group", "aria-label": "Processo seletivo" } });
  for (const value of ["IFRN", "EAJ"]) {
    const btn = el("button", {
      attrs: {
        type: "button",
        class: value === current ? "process-selector__btn is-active" : "process-selector__btn",
        "data-institution": value,
        "aria-pressed": String(value === current),
      },
      text: INSTITUTION_LABELS[value],
    });
    btn.addEventListener("click", () => {
      const next = setInstitution(value);
      group.querySelectorAll(".process-selector__btn").forEach((b) => {
        const active = b.getAttribute("data-institution") === next;
        b.classList.toggle("is-active", active);
        b.setAttribute("aria-pressed", String(active));
      });
      if (typeof onChange === "function") onChange(next);
    });
    group.appendChild(btn);
  }
  mount.textContent = "";
  mount.appendChild(group);
  return {
    root: group,
    refresh() {
      const v = getInstitution();
      group.querySelectorAll(".process-selector__btn").forEach((b) => {
        const active = b.getAttribute("data-institution") === v;
        b.classList.toggle("is-active", active);
        b.setAttribute("aria-pressed", String(active));
      });
    },
  };
}

/* Rótulo de edição com processo: "2022 · EAJ" / "2022 · IFRN" (TASK F.1). */
export function editionOptionLabel(edition) {
  const inst = String(edition?.institution || "IFRN").toUpperCase();
  return `${edition.year} · ${inst}`;
}
