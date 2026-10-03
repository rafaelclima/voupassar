# Landing page — VouPassar (TASK 6.2)

> Página pública em `frontend/index.html` (+ `css/landing.css`).
> Reutiliza tokens e componentes do design system (TASK 6.1) — nada foi
> reinventado. Referências de ofício: `frontend/assets/home_example.webp`
> (cartão navy em viewport único, headline gigante, stats) +
> `frontend/assets/home2_example.webp` (shell claro, prévia do produto
> com pílulas flutuantes). A paleta segue a marca navy; sem ilustração,
> sem perfumaria.

## 1. Objetivos (TASKS.md) → hero tela cheia (sem rolagem no desktop)

| Objetivo | Bloco do hero |
|---|---|
| Explicar propósito | `#hero-t` ("Questões, roteiro e simulados para a sua prova") + lead concreto (o que o cadastro entrega), genérico por decisão de produto |
| Transmitir confiança | `#hero-brand`: faixa com a logo e o nome do projeto (a fileira de números 3/2/1 foi removida — confusa) |
| Demonstrar funcionalidades | `#hero-visual`: marca em grande + mock em CSS do roteiro + pílulas "Roteiro que se explica" / "Simulado como a prova" (nenhum enunciado exibido) |
| Apresentar proposta de valor | o próprio mock: recomendação com motivo (prioridade + desempenho) |
| Direcionar cadastro/login | CTAs "Começar agora" → `cadastro.html`, "Entrar" → `login.html` (hero + pill + rodapé) |

## 2. Honestidade (AGENTS.md §4 — nada inventado)

* A mensagem da landing é genérica por decisão de produto (a plataforma
  pode crescer além de um exame específico): sem "IFRN", sem contagens
  do acervo, sem promessa de cobertura.
* A evidência do acervo (240 questões, 6 edições, 2021 ausente, 5
  anuladas, 20+20+1 por edição) continua verificada nas telas do app
  (`estudos`/`simulado`), na área admin e em `docs/` — o
  `check_frontend.py` deixou de exigi-la na landing, não no projeto.
* Sem preço/planos ("grátis" é termo barrado no `check_frontend.py`),
  sem "garantia", sem "milhares", sem "todas as edições".

## 3. Ofício visual

O redesenho segue a referência PIXORA de perto: página inteira sobre
o navy (sem cartão inset), header com pill de navegação branca ao
centro e CTA à direita, headline gigante com linha de acento, CTA em
pílula simples (a seta em círculo foi removida), stats com ícones SVG
e arte ocupando o lado direito. A arte é a marca do header
(`logo-mark-light.png`, 104px ≤ 107px nativos) angulada 12° sobre o
canto superior direito do mock — combinação pedida em vez de
ilustração genérica. Sem alternador de tema nesta tela (canvas idêntico
nos dois temas; `.theme-toggle` com `display:none`) e rodapé sem
Entrar/Criar conta (a página não rola, sem esforço para alcançar as
ações). Ícones são SVG inline
de forma reconhecível (lista, relógio, bússola, documento), nunca
glifos soltos. AGENTS.md §6 manda evitar
aparência infantil e excesso de cores: nada de laranja/teal da
referência, só navy + azul-claro + branco.

Recursos: canvas navy fixo nos dois temas (gradiente próprio),
2 colunas a 64rem, `body` em flex com `min-height: 100dvh` (header +
hero flexível + rodapé = zero rolagem no desktop), mock em CSS puro
sobre surface branca, pílulas flutuantes, anéis decorativos. Botões
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
| `check_frontend.py` | OK — 51 arquivos, 3 blocos da landing (`hero-t`, `hero-brand`, `hero-visual`) |

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
