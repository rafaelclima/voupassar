# Acessibilidade — auditoria e correções (TASK 8.1)

> Escopo: as 11 páginas de `frontend/` (`index`, `design-system`, `login`,
> `cadastro`, `recuperar-senha`, `redefinir-senha`, `dashboard`, `estudos`,
> `questao`, `simulado`, `perfil`) + `css/` + `js/components/ui.js` +
> `js/main.js` + `js/views/*`. Método: leitura estática do HTML/CSS/JS,
> contagem de marcos/rótulos e cálculo de contraste WCAG — sem inventar
> teste com leitor de tela ou navegador que não foi executado.
> Validação com navegador real (MCP `chrome-devtools`, AGENTS.md §31) fica
> como pendência registrada no §5.

## 1. Base já existente (mantida)

* `lang="pt-BR"`, `viewport`, `skip-link`, `header/main/nav/footer`, `h1`
  único por página (após correção do §2), `aria-label` na navegação e em
  `aria-current="page"`.
* Foco visível (`base.css` `:focus-visible` + `--focus-ring`),
  `prefers-reduced-motion` desligando animação/transição/scroll suave.
* Formulários com `<label>` real, `aria-invalid`/`aria-describedby` via
  `setFieldError()`, resumo de erros com `role="alert"` + foco (WCAG 3.3.1).
* Toast em `role="status"`/`aria-live="polite"`; erros de API em
  `role="alert"` com `traceId`; tabelas em `role="region"` + `tabindex="0"`
  + `caption` + `scope="col"`; `<dialog>` nativo (Esc nativo, backdrop).
* Contraste AA em todos os pares texto/fundo verificados (§4).

## 2. Achados e correções (nesta task, sem quebrar 6.1–6.8)

| # | Achado (evidência) | Correção | Arquivos |
|---|---|---|---|
| 1 | `design-system.html` tinha **2× `<h1>`** (título da página + demo de tipografia) — quebra hierarquia de títulos | Demo virou `<p>` estilizado como H1, com nota de que a página usa um único `h1` real | `design-system.html` |
| 2 | `.btn--sm` com `min-height: 36px` (`components.css`) — abaixo do próprio token `--touch-min: 44px` (WCAG 2.5.8) | `min-height: var(--touch-min)`; `--sm` agora diferencia só por padding/fonte | `css/components.css` |
| 3 | Botão fechar do toast com override inline `32px` (`ui.js`) — alvo menor que 44px | Removido o override; herdado `.icon-btn` 44px | `js/components/ui.js` |
| 4 | Toggle do menu mobile com `aria-label="Abrir menu"` fixo (`main.js` + todas as páginas) — SR não sabe quando está aberto | `aria-label` alterna Abrir/Fechar junto com `aria-expanded`; Esc fecha e devolve foco ao botão (só no layout mobile) | `js/main.js` |
| 5 | `#questao-feedback` com `role="status"` mas **sem `tabindex`** — `questao.js` chamava `feedbackBox.focus()` sem efeito | Adicionado `tabindex="-1"`; foco programático agora anuncia o feedback | `questao.html` |
| 6 | 6 páginas sem `<noscript>`: `index`, `login`, `cadastro`, `recuperar-senha`, `redefinir-senha`, `design-system` | Adicionado aviso honesto por página (auth/API exigem JS; landing/DS seguem legíveis) | 6 HTML |
| 7 | Menu mobile escondido via CSS (`display:none` sem JS) — sem JS e em tela pequena, navegação some | `<style>` dentro de cada `<noscript>`: mostra `.site-nav__links`, esconde o toggle | 11 HTML (5 existentes + 6 novos) |
| 8 | `openModal()` focava o diálogo mas **não devolvia o foco** ao fechar (Esc/backdrop) | Guarda `lastModalTrigger` e restaura no evento `close` (`{ once: true }`) | `js/components/ui.js` |
| 9 | Diálogo `sim-confirm` aberto via `showModal()` direto: Esc (evento `cancel`) não devolvia foco; resultado não recebia foco ao concluir | Listener `cancel` → foco de volta em `#sim-submit` (se ainda aberto); após `fetchResult`, foco em `#sim-result-t` (novo `tabindex="-1"`) | `js/views/simulado.js`, `simulado.html` |

## 3. O que foi avaliado e NÃO precisou mudar (com motivo)

* **Contraste:** todos os pares ≥ 4,5:1 (ver §4) — nenhum ajuste de cor.
* **Rótulos de formulário:** `questao.html` tem 0 `<label>` porque usa
  `fieldset/legend` + radios nativos (padrão correto); `index.html` tem 0
  porque não possui formulário. Nada a corrigir.
* **Validação do hub do simulado via `toast` (polite):** cada falha já move
  o foco para o campo (`selDisc.focus()` / `inpQtd.focus()`), o que compensa
  o `polite`; erros de API usam `renderErrorSummary` (`role="alert"`).
  Mantido por ser o comportamento testado da TASK 6.7.
* **Seletor `:focus-visible`:** cobre `a/button/input/select/textarea/
  [tabindex]/summary` — todos os interativos usados. Sem `[role=button]`
  no código, então nada a estender.
* **Imagens:** único `<img>` por página é o favicon decorativo (`alt=""`)
  dentro de link com `aria-label` — correto.

## 4. Contraste verificado (calculado desta auditoria, WCAG AA ≥ 4,5:1)

| Par | Razão | Resultado |
|---|---|---|
| `#164a73` sobre `#ffffff` (links/botões) | 9,29:1 | PASS |
| `#0f2e47` sobre `#ffffff` (títulos/header) | 13,98:1 | PASS |
| `#344054` sobre `#ffffff` (texto corpo) | 10,46:1 | PASS |
| `#667085` sobre `#ffffff` (muted/small) | 4,97:1 | PASS |
| `#667085` sobre `#f9fafb` (muted no fundo) | 4,76:1 | PASS |
| `#067647`/`#b42318`/`#b54708`/`#175cd3` sobre seus fundos `*-50` | 5,2–6,1:1 | PASS |
| `#ffffff` sobre `#0f2e47` (cta/header) | 13,98:1 | PASS |
| `#d0d5dd` sobre `#0f2e47` (parágrafo da cta-band) | 9,48:1 | PASS |

## 5. Pendências (NÃO inventadas, para 8.2/8.3 ou navegador real)

* Validação com navegador real via MCP `chrome-devtools` (AGENTS.md §31):
  guarda de auth, fluxo principal por tela, console sem erros, teste de
  teclado fim-a-fim (Tab/Esc/foco em diálogo e menus) — não executada
  nesta sessão por indisponibilidade do MCP; só validação estática abaixo.
* Auditoria automatizada externa (Lighthouse/axe) — fora do escopo desta
  task; quando executada, anexar relatório em vez de resumir de memória.
* `TASK 8.2` (responsividade tablet/mobile) e `8.3` (UX review como
  estudante) — próximas tasks, não antecipadas aqui.

## 6. Verificação (executada)

```bash
for f in frontend/js/components/ui.js frontend/js/main.js frontend/js/views/simulado.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
# check_frontend: OK (46 arquivos, 19 tokens, 27 componentes, 13 seções do DS,
# 6 seções da landing, 4 páginas de auth)
timeout 8 python3 -m http.server 8899 --directory frontend
# 200 em index/login/dashboard/questao/simulado/perfil/design-system
# Pós-fix: h1=1 em todas as 11 páginas; noscript em 11/11; skip-link em 11/11
```

Critérios 8.1: navegação por teclado preservada e melhorada (foco devolvido
em modal/diálogo/menu), foco visível mantido, contraste AA comprovado,
labels/semântica sem violações após o h1 único, leitores de tela atendidos
via regiões live e foco programático funcional, mobile sem perda de
navegação sem-JS. Nenhum recurso 6.1–6.8 removido; Fase 7 segue adiada
(`docs/fase-7-adiada.md`).
