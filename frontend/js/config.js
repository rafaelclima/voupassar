/* VouPassar — config centralizada (TASK 6.1)
 * ÚNICO lugar com a URL da API (architecture.md §2).
 * Nas páginas: <meta name="voupassar-api" content="https://...">
 * ou window.__VOUPASSAR_CONFIG__ = { API_BASE_URL: "..." }.
 * Build de Pages injeta API_BASE_URL (TASK 10.1); default = localhost dev.
 */

const fromMeta = () =>
  document
    .querySelector('meta[name="voupassar-api"]')
    ?.getAttribute("content")
    ?.trim() || "";

const fromGlobal = () =>
  (typeof window !== "undefined" &&
    window.__VOUPASSAR_CONFIG__?.API_BASE_URL?.trim()) ||
  "";

const fromStorage = () => {
  // Override explícito de desenvolvimento via ?api=... (nunca em produção).
  try {
    return new URLSearchParams(location.search).get("api")?.trim() || "";
  } catch {
    return "";
  }
};

function normalizeBase(url) {
  if (!url) return "";
  return url.replace(/\/+$/, "");
}

export const API_BASE_URL =
  normalizeBase(fromStorage() || fromGlobal() || fromMeta()) ||
  "http://localhost:8080";

export const APP_VERSION = "0.1.0-design-system";

export const DEFAULT_PAGE_SIZE = 20;
