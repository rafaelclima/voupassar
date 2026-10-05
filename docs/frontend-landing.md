# Landing page — VouPassar (TASK 6.2)

> Página pública em `frontend/index.html` (+ `css/landing.css`).
> Reutiliza tokens e componentes do design system (TASK 6.1) — nada foi
> reinventado. Hero navy preservado + 5 seções claras abaixo com as
> imagens geradas em `frontend/assets/landing/` (desk, hero, numbers,
> simulation, students, vector — ~190 KB no total, otimizadas via ffmpeg
> para webp real). A paleta segue a marca navy; sem aparência infantil.

## 1. Objetivos (TASKS.md) → hero preservado + seções que explicam

| Objetivo | Bloco |
|---|---|
| Explicar propósito | `#hero-t` + lead ("com base em provas reais organizadas por assunto"), genérico por decisão de produto + `#o-que-e` (3 pilares com imagem) |
| Transmitir confiança | `#hero-brand` (faixa com logo) + `#de-onde-vem` (foto de estudantes + texto honesto origem/visão) |
| Demonstrar funcionalidades | `#hero-visual` (arte gerada em full-bleed, nenhum enunciado exibido) + `#como-praticar` (3 modos + 2 simulados) |
| Apresentar proposta de valor | `#como-funciona` (cadeia prova real → organização → roteiro → evolução, AGENTS.md §32) |
| Direcionar cadastro/login | CTAs "Começar agora" → `cadastro.html`, "Entrar" → `login.html` (hero + `#cta-final` + `#de-onde-vem`) |

## 2. Honestidade (AGENTS.md §4 — nada inventado)

* A mensagem da landing é genérica por decisão de produto (a plataforma
  mira IFs, ENEM e concursos): sem prender a um exame, sem contagens
  do acervo, sem promessa de cobertura. A seção `#de-onde-vem` declara
  a visão multi-exame com badge `EM EXPANSÃO` — novas trilhas só com
  acervo real classificado.
* A evidência do acervo (240 questões, 6 edições, 2021 ausente, 5
  anuladas, 20+20+1 por edição) continua verificada nas telas do app
  (`estudos`/`simulado`), na área admin e em `docs/` — não na landing.
* Sem preço/planos ("grátis" é termo barrado no `check_frontend.py`),
  sem "garantia", sem "milhares", sem "todas as edições".
* Imagens geradas por IA são ilustrativas (sem texto legível, sem
  enunciado real) e têm `alt` honesto.

## 3. Ofício visual

O hero segue a referência PIXORA: canvas navy de borda a borda, header
com pill de navegação branca ao centro e CTA à direita, headline
gigante com linha de acento. A arte é servida de forma **responsiva**
(`<picture>` + `srcset`, sem espelho em CSS em nenhum breakpoint):

| Breakpoint | Layout | Fonte |
|---|---|---|
| `≥ 64rem` (desktop, tablet landscape) | Full-bleed: `absolute inset: 0`, `cover`, `object-position: 75% 50%`, texto à esquerda por cima (`z-index` maior, sem mudar copy/ordem/estilo) | `hero.webp` (1200w) / `hero-2400.webp` (2400w), `sizes="100vw"` — a 2400 cobre telas grandes sem upscale |
| `< 64rem` (mobile, tablet retrato) | Empilhado: texto sobre o navy limpo + arte como **cartão contido** abaixo (`border-radius`, sem overlay, sem corte lateral) | `hero-portrait.webp` (900×1125, 4:5 — crop do terço direito da fonte, já enquadra estudantes, degraus e cronômetro) |

A arte (gerada via prompt em `docs/hero-art-prompt.md`) já nasce com os
dois estudantes — menino e menina, ambos com mochila — no terço direito,
degraus ao centro e cronômetro no alto à direita; a metade esquerda é só
fundo navy, a área do texto. Por isso nenhum espelho em CSS é
necessário: a orientação original já coloca cada elemento no seu lugar.

Não há véu/scrim sobre a arte em nenhum breakpoint: no desktop a
legibilidade vem de **sombra de texto** no H1 e no lead
(`0 2px 24px rgb(6 20 36 / 0.9)`); no empilhado o texto nem toca a arte.
`alt` vazio (decorativa — a mensagem está no H1 + CTAs), `width`/`height`
declarados (sem CLS), `fetchpriority="high"`.

Detalhe de implementação: o grid da hero é **estático** de propósito —
o `inset: 0` da arte usa a `section` (que é `relative` + `overflow:
hidden`) como referência. Se o grid voltasse a ser `relative`, o
full-bleed encolheria para o tamanho do grid. O texto sobe via
`.landing-hero__copy { position: relative; z-index: 1 }`.
As seções abaixo são
claras (tokens `surface`/`bg-100`, que adaptam ao
dark) com grid de 3 cards (`desk`/`numbers`/`simulation`), passos
numerados via CSS counter, modos de estudo, feature 2 colunas
(`students.webp` 1200px) e banda CTA navy com `vector.webp` ao fundo
(1600px, `opacity: 0.55` + texto com `z-index`).

Imagens: `width`/`height` declarados (sem CLS), `loading="lazy"`,
`aspect-ratio` + `object-fit: cover` para cartões uniformes.
AGENTS.md §6: só navy + azul-claro + branco, sem infantil.

Recursos: canvas navy fixo nos dois temas (gradiente próprio),
2 colunas a 64rem, `body` em flex com `min-height: 100dvh` (header +
hero flexível + rodapé), anéis decorativos. Botões
sobre navy usam cor fixa `#0d2b45` (tokens primary invertem no dark e
quebravam o contraste para ~1,5:1). A pill usa `#0d2b45` sobre branco
(~14:1); o acento `#7fb4e4` sobre navy mede ~7:1.

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
* A marca sobre navy usa sempre a versão clara (`brand__mark--inverse`
  e `logo-full` com `brightness(0) invert(1)`): a arte é navy e ficaria
  invisível sobre navy no tema claro.

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
| `check_frontend.py` | OK — 58 arquivos, 8 blocos da landing (`hero-t`, `hero-brand`, `hero-visual`, `o-que-e`, `como-funciona`, `como-praticar`, `de-onde-vem`, `cta-final`) |

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
