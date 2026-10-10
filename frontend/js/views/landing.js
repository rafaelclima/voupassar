/* VouPassar — landing multi-processo (TASK F.1)
 *
 * Monta o seletor IFRN/EAJ na seção #processo. Sem sessão: contagens
 * estáticas verificadas no repo (IFRN 240 questões / 6 edições via seed V2;
 * EAJ 128 importadas / 3 edições via report-eaj.json — Q22/Q39-2025 fora,
 * aguardando retranscrição). Com sessão: tenta os totais vivos de
 * GET /editions e mantém o estático como fallback honesto.
 */

import { mountProcessSelector } from "../components/process-selector.js";

const mount = document.getElementById("process-selector-mount");
if (mount) {
  mountProcessSelector(mount, {
    onChange: () => {},
  });
}
