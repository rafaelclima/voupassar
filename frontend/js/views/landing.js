/* VouPassar — landing multi-processo (TASK F.1 + painel #processo)
 *
 * Monta o seletor IFRN/EAJ na seção #processo. Contagens estáticas
 * verificadas no repo (IFRN 240 questões / 6 edições via seed V2;
 * EAJ 130 / 3 edições pós go-live VPS, com Q22/Q39-2025 retranscritas).
 * Estáticas de propósito: GET /editions exige sessão e a landing é
 * pública (nada inventado, sem fetch anônimo).
 */

import { mountProcessSelector } from "../components/process-selector.js";

const mount = document.getElementById("process-selector-mount");
if (mount) {
  mountProcessSelector(mount, {
    onChange: () => {},
  });
}
