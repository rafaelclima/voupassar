# Landing page — VouPassar (TASK 6.2)

> Página pública em `frontend/index.html` (+ `css/landing.css`).
> Reutiliza tokens e componentes do design system (TASK 6.1) — nada foi
> reinventado. A referência de ofício visual é
> `docs/brand/exemplo-referencia.png`.

## 1. Objetivos (TASKS.md) → seções

| Objetivo | Seção |
|---|---|
| Explicar propósito | hero (`#hero-t`) + `#proposta` |
| Transmitir confiança | selo "Provas reais", números auditados no painel de marca, ausência de 2021, anuladas citadas |
| Demonstrar funcionalidades | `#funcionalidades` (6 cards com selo "API pronta") |
| Apresentar proposta de valor | `#proposta` (3 cards) + `#como-funciona` (4 passos do ciclo) |
| Direcionar cadastro/login | CTAs do hero e do nav → `#acesso` (cards Criar conta / Entrar) |

## 2. Honestidade (AGENTS.md §4 — nada inventado)

* Números só auditados: **240** questões (40 × 6), **6** edições,
  **120 + 120** (LP/MAT), assuntos da `docs/content-map.md`.
* **2021 ausente** citado nas evidências e no FAQ.
* Anuladas listadas por edição (2020 Q7/Q26 · 2023 Q21 · 2024 Q17 ·
  2026 Q37) + "regra de pontuação desconhecida".
* Classificações com "revisão humana em curso"; 20+20+1 descrito como
  configuração dos documentos do acervo, nunca regra universal.
* FAQ da redação: proposta registrada, correção automática fora do MVP.
* Sem preço/planos ("grátis" é termo barrado no `check_frontend.py`),
  sem "garantia", sem "milhares", sem "todas as edições".

## 3. Ofício visual

O redesenho adota o **nível de acabamento** da referência (ritmo de
seções, respiro, cabeçalho de seção centralizado com eyebrow, cards com
ícone colorido, rodapé em colunas, indicador de abertura desenhado em
CSS no FAQ) e mantém a **paleta navy/azul da marca** e o tom sério.
A referência é roxa e decorativa; AGENTS.md §6 manda evitar aparência
infantil, excesso de cores e excesso de gradientes.

Recursos: hero em duas colunas (64rem), seções alternando surface e
`--color-bg-100` para dar ritmo, painel de marca com a logo completa e
os três números de evidência, quatro passos com conector horizontal.

### Blocos de marca nunca usam `--color-primary-9xx`

A escala `primary` é **invertida** no tema escuro: `--color-primary-900`
passa de navy para azul claro. Usá-la como fundo de um bloco com texto
branco deixa o texto ilegível. Foi exatamente o defeito da antiga
`.cta-band` (fundo claro, texto branco, contraste 1,03:1). Fundos de
marca usam a família **navy** ou os tokens **hero**, que não são
invertidos. O mesmo vale para texto de apoio: `--color-primary-200` vira
azul-escuro no dark, então a banda usa `--color-hero-muted`.

## 4. Estado

* **landing.js foi removido.** O bloco `#api-status` saiu do HTML no
  commit `0dad815`, e o script ficou órfão (o `if (box)` impedia
  qualquer ação) — um request morto na página pública. O
  `check_frontend.py` deixou de exigir o arquivo.
* A página é 100% estática: sem `fetch`, sem estado, legível offline.
  Os dados auditados estão no HTML.
* O painel de marca tem superfície clara **fixa** nos dois temas
  (`--color-brandcard-*`, declarado só em `:root`): a arte é navy e
  ficaria invisível sobre navy. A logo ocupa um painel claro, como uma
  peça impressa sobre a página.

## 5. Verificação

```bash
python3 scripts/analysis/check_frontend.py
node --check frontend/js/main.js
python3 -m http.server 8899 --directory frontend
```

Chrome DevTools MCP, conferido neste redesenho:

| Item | Resultado |
|---|---|
| Lighthouse a11y / best practices / SEO | 100 / 100 / 100 nos dois temas |
| Console (erro + warning) | limpo |
| Marca no header a 390px | wordmark completo (bug de flex-shrink corrigido em `base.css`) |
| Menu mobile | painel suspenso abaixo do header, largura total |
| `check_frontend.py` | OK — 49 arquivos, 6 seções da landing |

Correções que saíram da validação e afetam **todas** as telas, feitas
em `base.css`:

* `.brand` com `flex-shrink: 0` — sem isso o wordmark transbordava 34px
  por baixo do botão de menu a 390px.
* `.site-nav` sem `width: 100%`; menu aberto vira painel absoluto
  (`top: 100%`) em vez de descer por wrap, que empurrava o botão de
  tema para uma segunda linha.
* `.site-footer small` com cor própria — `small` é seletor de tipo e
  vencia a cor herdada do rodapé, ficando em 3,37:1.
* `.eyebrow` passou a `--color-primary-700`: `-600` rendia 4,33:1 no
  dark (abaixo de 4,5:1). `-700` é mais escuro no claro e mais claro no
  escuro, então os dois temas ganham.
