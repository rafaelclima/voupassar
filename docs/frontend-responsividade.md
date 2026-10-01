# Responsividade — auditoria e correções (TASK 8.2)

> Escopo: as 11 páginas de `frontend/` + `css/` (mesmo conjunto da TASK 8.1).
> Método: navegador real via MCP `chrome-devtools` (emulação de viewport
> 360×800 mobile, 768×1024 tablet, 1280×800 desktop, com recarga limpa
> `ignoreCache` após cada edição de CSS) + leitura estática do CSS.
> Métrica de overflow: `documentElement.scrollWidth − clientWidth`
> (largura da janela `innerWidth` mascara o estouro; `clientWidth` não).
> Backend local (`:8080`) fora do ar durante a auditoria — os erros de
> `fetch` no console são o estado "API indisponível" já tratado pela UI,
> não regressão.

## 1. Base já existente (mantida)

* Mobile-first em todos os arquivos de página; 2 breakpoints: `48rem`
  (768px) e `64rem` (1024px); tokens de container/toque em `tokens.css`.
* Tabelas com `min-width: 560px` dentro de `.table-wrap` (`overflow-x: auto`,
  região rolável) — comportamento projetado, não defeito.
* `pre` com `overflow-x: auto`; `.btn` com `white-space: nowrap` mas com
  larguras que cabem em 328px de conteúdo (hero: `flex: 1 1 100%`).
* Rótulos longos com `ellipsis` (`.evo-bars__label`); `min-width: 0` já
  existia em `.study-main`/`.study-side`.

## 2. Achados e correções (nesta task, sem quebrar 6.1–6.8/8.1)

| # | Achado (evidência) | Correção | Arquivos |
|---|---|---|---|
| 1 | Estilos do menu responsivo (`.site-nav__toggle`, `.site-nav__links`, `.is-open`, linha/coluna por breakpoint) viviam **só em `landing.css`**, carregado apenas pela `index.html`. Nas outras 9 páginas com toggle, medido a 360px: toggle `display:block` sem estilo e links sempre visíveis (`linksDisplay:block`, `linksDir:row`); a 931px o "hamburger" continuava visível no desktop. Botão morto + navegação inconsistente | Bloco de navegação promovido para `base.css` (global, mesmos seletores/valores — a landing renderiza idêntico); duplicata removida de `landing.css`. A lógica de `main.js` (`offsetParent`, `aria-expanded` Abrir/Fechar, Esc) passa a valer em todas as páginas | `css/base.css`, `css/landing.css` |
| 2 | `design-system.html` a 360px: `scrollWidth 612 − clientWidth 360 = 252px` de rolagem horizontal. Causa: itens de grid têm `min-width:auto`; a `.table` de demonstração (`min-width:560px`) esticava a trilha `1fr` de `.docs-grid` (filhos medidos com 596px) | Guarda `min-width: 0` nos filhos diretos das grades utilitárias (`.card-grid`, `.docs-grid`, `.grid-2`) em `base.css`, com comentário e precedente (`.study-main`). Pós-fix: overflow 0; `.table-wrap` rola internamente como projetado | `css/base.css` |

## 3. Matriz verificada (pós-fix, com recarga limpa)

Mobile 360px — overflow `0` em 11/11; menu colapsado (`toggle:flex`,
`links:none`) e abrindo em coluna (`aria-expanded=true`, sem overflow):
`index` (botões hero 328px full-width), `dashboard` (abertura do menu
clicada), `estudos`, `questao`, `simulado`, `perfil` (abertura clicada),
`login` (auth-card 328px), `cadastro`, `recuperar-senha`,
`redefinir-senha`, `design-system`.

Tablet 768px — overflow `0`: `dashboard` (toggle oculto, links em linha),
`estudos` (filtros 2 col, grade 1 col — sidebar só a partir de 64rem),
`index` (steps 2 col, trust 3 col, hero 1 col), `simulado` (sim-grid 2 col).

Desktop 1280px — overflow `0`: `index` (hero 2 col, steps 4 col, toggle
oculto), `dashboard` (toggle oculto, links em linha), `perfil` (stats 4 col,
grade 2 col), `estudos` (filtros 3 col, lateral 320px), `design-system`
(docs-grid `256px 832px`).

Console: zero exceções JS; apenas os erros esperados de `fetch` ao backend
ausente (`localhost:8080`, CORS/ERR_FAILED), cobertos pelo estado
"API indisponível" da UI.

## 4. O que foi avaliado e NÃO precisou mudar (com motivo)

* Breakpoints 48/64rem: as transições medidas (filtros 1→2→3 col, stats
  1→2→4 col, hero/steps) ocorrem nos pontos projetados — sem zona morta.
* `.evo-bars__row` (`7rem 1fr 4.5rem`): cabe em 328px de conteúdo com
  `ellipsis` nos rótulos; sem overflow medido — sem ajuste.
* `.sim-score` (2 col mobile → 4 col desktop) e `.modal`
  (`min(92vw, 32rem)`): regras sãs; resultado do simulado exige backend,
  então validado por regra + seções estáticas, sem estado navegável.
* `design-system.html` (sem toggle): regras novas só casam onde há
  `.site-nav__links`/`.site-nav__toggle` — verificado sem regressão.

## 5. Verificação (executada)

```bash
python3 scripts/analysis/check_frontend.py
# check_frontend: OK (46 arquivos, 19 tokens, 27 componentes, 13 seções do DS,
# 6 seções da landing, 4 páginas de auth)
for f in frontend/js/main.js frontend/js/views/*.js frontend/js/components/*.js frontend/js/api/*.js; do node --check "$f"; done
# NODE-OK
python3 -m http.server 8899 --directory frontend  # 200 em 11/11 páginas
```

Critérios 8.2: desktop, tablet e mobile sem rolagem horizontal indevida;
menu mobile funcional e consistente nas 10 páginas com toggle (colapsa,
abre, rotula Abrir/Fechar); grades sem estouro de trilha; landing
pixel-equivalente; nenhum recurso 6.1–6.8/8.1 removido.
