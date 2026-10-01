/* VouPassar — landing (TASK 6.2)
 * Único JS exclusivo da index.html: sonda pública GET /api/v1/health
 * e renderiza os 3 estados (loading / sucesso / erro com retry).
 * Sem auth, sem dados pessoais, sem innerHTML (textContent via ui.el).
 */
import { request } from "../api/client.js";

const box = document.getElementById("api-status");
if (box) {
  const label = box.querySelector("[data-api-label]");
  const retry = box.querySelector("[data-api-retry]");

  const setState = (state, text) => {
    box.dataset.state = state;
    if (label) label.textContent = text;
  };

  const check = async () => {
    setState("loading", "Verificando API…");
    if (retry) retry.hidden = true;
    const t0 = performance.now();
    try {
      await request("/api/v1/health");
      const ms = Math.round(performance.now() - t0);
      setState("ok", `API operacional · resposta em ${ms} ms`);
    } catch {
      setState("error", "API indisponível no momento — o conteúdo abaixo segue válido (dados auditados no repositório).");
      if (retry) retry.hidden = false;
    }
  };

  retry?.addEventListener("click", check);
  check();
}
