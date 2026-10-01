# Landing page — VouPassar (TASK 6.2)

> Página pública em `frontend/index.html` (+ `css/landing.css` +
> `js/views/landing.js`). Reutiliza todos os tokens/componentes da TASK 6.1 —
> nada foi reinventado nem duplicado.

## 1. Objetivos (TASKS.md) → seções

| Objetivo | Seção |
|---|---|
| Explicar propósito | hero (`#hero-t`) + `#proposta` |
| Transmitir confiança | selo “Provas reais”, números auditados, nota 2021 ausente, status vivo da API |
| Demonstrar funcionalidades | `#funcionalidades` (6 cards com selo “API pronta”) |
| Apresentar proposta de valor | `#proposta` (3 cards) + `#como-funciona` (4 passos do ciclo) |
| Direcionar cadastro/login | CTAs do hero e do nav → `#acesso` (cards Criar conta / Entrar) |

## 2. Honestidade (AGENTS.md §4 — nada inventado)

* Números só auditados: **240** questões (40 × 6), **120 + 120** (LP/MAT),
  assuntos da `docs/content-map.md` (Gramática 60, Interpretação 60…).
* **2021 ausente** citado no hero (badge), nas evidências e no FAQ.
* Anuladas listadas por edição (2020 Q7/Q26 · 2023 Q21 · 2024 Q17 · 2026 Q37)
  + “regra de pontuação desconhecida”.
* Classificações com “revisão humana pendente”; 20+20+1 descrito como
  configuração dos documentos do acervo, nunca regra universal.
* FAQ da redação: proposta registrada, correção automática fora do MVP.
* Sem preço/planos (“grátis” proibido no validador), sem “garantia”,
  sem “milhares”, sem “todas as edições”.
* Auth: backend JWT pronto e testado; **telas de login/cadastro chegam na
  TASK 6.3** — os CTAs apontam para `#acesso`, nenhuma rota inventada
  (`login.html`/`cadastro.html` não existem ainda, então não são linkados).

## 3. Estados, a11y, performance, Pages

* API status (`#api-status`, `role=status` + `aria-live`): loading →
  sucesso com latência → erro com retry. Conteúdo estático segue válido
  offline (dados auditados no repo).
* Semântico (`header/main/nav/section/table/details/dialog` futuro),
  `h1` único, `skip-link`, foco visível, contraste AA herdado, toque 44px,
  `prefers-reduced-motion` herdado; tabela com `caption` em região rolável.
* Zero dependências; +1 CSS e +1 JS modular pequenos; caminhos `./`
  relativos; dinâmica só via `fetch()` público (`GET /health`, sem auth).

## 4. Verificação

```bash
python3 scripts/analysis/check_frontend.py
node --check frontend/js/views/landing.js
timeout 20 python3 -m http.server 8899 --directory frontend
```

Critérios 6.2: 6 seções presentes; CTA → `#acesso`; “2021” citado; “240”
citado; nenhum termo proibido; `node --check` OK; 200 no serve.
