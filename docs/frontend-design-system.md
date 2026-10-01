# Design system — VouPassar (TASK 6.1)

> Fundação visual e de código para toda a Fase 6. Showcase vivo em
> `frontend/design-system.html`. Nada aqui é landing final (TASK 6.2) nem
> tela de produto (6.3+) — é o contrato que elas devem seguir.

## 1. Arquivos

```
frontend/
├── index.html              # placeholder intencional (vira landing na 6.2)
├── design-system.html      # galeria de todos os componentes (esta doc viva)
├── css/
│   ├── tokens.css          # ÚNICA fonte de cor/tipo/espaço/raio/sombra
│   ├── base.css            # reset, tipografia, foco, header/footer, reduced-motion
│   ├── components.css      # botões, inputs, cards, tabelas, badges, alerts,
│   │                       # modal, empty, loading, erros, molde de questão
│   └── utilities.css       # container, grid, cluster, visually-hidden…
├── js/
│   ├── config.js           # ÚNICO lugar com API_BASE_URL (meta → global → ?api=)
│   ├── api/client.js       # Fetch + envelope {code,message,traceId} + ApiError
│   ├── state/store.js      # pub/sub mínimo (user, lastError)
│   ├── components/ui.js    # el/textContent, toast, modal, loading, empty, erros
│   ├── main.js             # bootstrap global (ano, nav, modais)
│   └── views/              # reservado p/ 6.2+ (uma pasta por tela)
└── assets/favicon.svg
```

Regras (AGENTS.md §19): páginas importam os 4 CSS via `<link>` e JS via
`type="module"`. Proibido `script.js` gigante, regra de negócio em view,
`innerHTML` com dados, ou novo token fora de `tokens.css`.

## 2. Princípios (AGENTS.md §6)

Seriedade e clareza: azul-petróleo `#164a73/#0f2e47` + neutros; texto
sempre com contraste AA; sem gradientes, sem animação decorativa, sem
gamificação visual. Hierarquia por tipografia e espaço, não por cor.

## 3. Tokens

| Grupo | Tokens | Uso |
|---|---|---|
| Cor | `--color-primary-900/700/100/50`, `--color-ink-900/700/500`, `--color-line-300/200`, `--color-bg-100/50`, `--color-surface`, `success/danger/warning/info 700+50+200` | só esses; feedback sempre par texto-fundo (ex. `#067647` sobre `#ecfdf3`) |
| Tipo | `--font-sans` (sistema, zero webfont), `--font-mono`, `--text-xs…4xl`, `--leading-body/heading`, `--measure: 68ch` | base 16px, corpo 1.5, títulos 1.25 |
| Espaço | `--space-1/2/3/4/6/8/12/16` (4–64px) | nada de px mágicos nas páginas |
| Forma | `--radius-sm/md/lg/pill`, `--shadow-sm/md`, `--focus-ring`, `--touch-min: 44px`, `--container-max: 72rem` | foco visível sempre; toque ≥ 44px |
| Movimento | `--motion-fast/base` | desligado sob `prefers-reduced-motion` |

## 4. Componentes (classes)

* **Botões:** `.btn` + `--primary/secondary/ghost/danger`, `--sm/lg/block`, `[disabled]`. Loading via `setButtonLoading()` (preserva rótulo p/ SR).
* **Forms:** `.field > .field__label + .input/.select/.textarea + .field__hint/.field__error`; erro com `aria-invalid + aria-describedby`; resumo com `renderErrorSummary()` e foco (WCAG 3.3.1). Checkbox/radio em `.check-row/.radio-row` (44px).
* **Cards:** `.card > __header/__body/__footer`; `--stat` com `.stat-value/.stat-label`; grids `.card-grid--2/3` (1 col no mobile).
* **Tabelas:** `.table-wrap[tabindex=0][role=region]` + `.table` com `caption` e `scope`; `.num` à direita tabular.
* **Badges:** `.badge--primary/success/danger/warning/info` + `__dot`. Vocabulário: `OFFICIAL`, `PUBLICÁVEL`, `PENDENTE_REVISÃO`, `ANULADA`, confiança.
* **Feedbacks:** `.alert--success/danger/warning/info` (`role=status/alert/note`) + `.toast-region` (`aria-live=polite`) via `toast()`.
* **Modal:** `<dialog class="modal" data-modal>` + `__header/__body/__footer`; abrir com `data-open-modal="id"` ou `openModal()`; fechar com `data-close-modal`; `Esc` nativo.
* **Vazio:** `.empty + __icon` ou `renderEmpty()`; sempre título + descrição + ação.
* **Loading:** `.spinner(--sm)`, `.skeleton`, `.loading-block[role=status]`, `.progress[role=progressbar]`.
* **Erros:** `.error-summary[role=alert]` + `.envelope code` p/ `traceId`; API traduzida por `friendlyMessage()` (401→re-login, 2021→ausência explicada, 429/5xx…).
* **Questão (molde):** `.question > __meta/__body/__options/__option(--correct/--wrong) + __key`. Conteúdo real só via `GET /api/v1/questions` (TASK 3.4); exemplo da galeria é fictício e sinalizado.

## 5. JS foundation

* `config.js`: `API_BASE_URL` de `<meta name="voupassar-api">` (build injeta em 10.1) → `window.__VOUPASSAR_CONFIG__` → `?api=` (dev) → default `http://localhost:8080`.
* `api/client.js`: `request(path, {method, body, query, timeoutMs, credentials})`, `credentials` padrão `"same-origin"` (ajuste 6.3: o MVP trafega refresh no JSON e o backend não envia `Allow-Credentials`, então `"include"` quebra o preflight Pages × API — usar `"include"` só quando o refresh for cookie HttpOnly); timeout 15s; `ApiError{status, code, details, traceId, path}`; `friendlyMessage()` em pt-BR; atalhos `api.health/editions/disciplines/questions`.
* `ui.js`: `el()` (textContent), `toast()`, `openModal/closeModal/wireModalTriggers`, `setButtonLoading()`, `renderEmpty()`, `setFieldError()`, `renderErrorSummary()`.
* Segurança (§15): token só em memória; `credentials` padrão `same-origin` no MVP sem cookies (ver `js/api/client.js`); nunca `innerHTML` com dados; `escapeHtml()` só p/ template estático.
* `store.js`: `getState/setState/subscribe/setUser/setLastError` — telas 6.4+ assinam sem acoplamento.

## 6. Acessibilidade (WCAG prática, §17)

HTML semântico (`header/main/nav/section/table/fieldset/dialog`), `lang="pt-BR"`,
`skip-link`, rótulos reais, `aria-live` em toast/loading/API, foco visível,
contraste AA, `prefers-reduced-motion`, toque 44px, tabela com `caption`.
Auditoria completa na TASK 8.1; este sistema já entrega a base.

## 7. Performance (§18) e Pages

Zero dependências, zero webfonts, CSS ~4 arquivos pequenos, JS por módulo
(`main.js` global + imports por página). Imagens: só `favicon.svg`.
Compatível com GitHub Pages: caminhos relativos `./`, sem SSR, sem segredo,
dinâmica só via `fetch()` (TASK 10.1 injeta `API_BASE_URL` de produção).

## 8. Verificação (TASK 6.1)

```bash
python3 -m http.server 8000 --directory frontend
# http://localhost:8000/ e /design-system.html
python3 scripts/analysis/check_frontend.py  # tokens, a11y básica, sem segredos
```

Critérios: todos os 13 itens da TASK visíveis na galeria; HTML `lang` +
`viewport`; foco visível; mobile 360px sem rolagem horizontal (só tabelas
rolam internamente); nenhum `innerHTML` com dados; nenhuma URL de API
hardcoded fora de `config.js`.

## 9. O que fica para as próximas TASKs

6.2 landing · 6.3 auth · 6.4 dashboard · 6.5 estudos · 6.6 questão ·
6.7 simulado · 6.8 perfil — todas em `js/views/*` + seções em `index.html`
ou páginas próprias, reutilizando estes tokens/componentes. Nada de
reinventar botão/input/card.
