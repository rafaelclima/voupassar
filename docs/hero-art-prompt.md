# Prompt de regeneração da arte da hero (`assets/landing/hero.webp`)

## Por que regenerar (e não espelhar em CSS)

O `hero.webp` atual tem o estudante no terço **esquerdo** do quadro. Com
`object-fit: cover` em full-bleed, a arte sempre mapeia esquerda→esquerda:
não existe `object-position` que coloque o estudante à direita. As duas
saídas possíveis seriam espelhar em CSS (`transform: scaleX(-1)`,
**proibido** nesta hero — inverteria a leitura da cena) ou **gerar a arte
já com os estudantes à direita**. Este prompt é a segunda via.

Além do reposicionamento, a arte passa a ter **duas pessoas** (menino e
menina, ambos com mochila), o que pede uma cena um pouco mais aberta para
que nenhum dos dois fique cortado pelo `cover`.

## Prompt (usar como está, em inglês)

```text
Modern flat-vector illustration, 16:9 widescreen, deep navy blue background
with a soft radial gradient (lighter blue glow in the upper-right corner,
#0a2138 → #10395e), no text, no logos, no watermark.

TWO school-age students, a boy and a girl, seen from BEHIND, both wearing
backpacks, standing in the RIGHT THIRD of the frame. The boy has dark
curly hair, a navy blue hoodie and a dark backpack; the girl stands
slightly beside and behind him, with dark hair in a ponytail, a light
blue top and a blue backpack. Both look toward a rising path of glowing
blue platforms/steps that ascend diagonally from lower-center toward the
upper right, with small floating UI cards (question cards, no readable
text) and a glowing circular timer/gauge icon near the top right.

The LEFT 50% of the image must stay almost entirely EMPTY smooth navy
background — only the background gradient. No people, no objects, no
text, no icons in that left area, so UI text can be overlaid there.

Style: clean modern 2D vector, soft glowing edges, subtle depth, gentle
blue rim light, calm and serious educational mood, not childish, not
cartoonish, no photorealism, no heavy 3D. Keep the palette to navy,
deep blue, soft light blue and a single warm accent color.

Negative: no text, no letters, no numbers, no UI mockups with text, no
watermark, no signature, no people on the left side, no duplicated
characters, no distorted hands, no frame or border, no vignette.
```

## Negativos obrigatórios (o motivo de cada um)

| Negativo | Motivo |
|---|---|
| "no text, no letters, no numbers" | Imagem gerada não pode exibir enunciado real (§11 do AGENTS.md) — só forma de cartão |
| "LEFT 50% … EMPTY" | O texto do hero (kicker, H1, lead, CTAs, faixa de marca) fica por cima da arte, à esquerda. Se a arte tiver conteúdo ali, o H1 perde leitura e não há véu/scrim para tapar |
| "no people on the left side" | Garante que nenhum estudante fique sob o texto |
| "no duplicated characters" | Modelos de imagem às vezes repetem o sujeito ao espelhar o layout |
| "no vignette / no frame" | Vinheta escurece as bordas e cria um "véu" involuntário, que apaga a arte |

## Especificação técnica

* Proporção **16:9** e saída final em **1200×675** (o `hero.webp` atual):
  é o que o `hero-art` declara em `width`/`height` no HTML. Se o modelo
  devolver outra proporção, cortar para 16:9 centralizado no conteúdo,
  preservando a faixa vazia à esquerda.
* Manter o webp com tamanho parecido ao atual (~33 KB) para não pesar o
  LCP da landing.
* Nível decompressionamento equivalente ao atual (`ffmpeg -q:v` baixo,
  tipo 4–6) para preservar os brilhos sem banding no navy.

## Depois de gerar

1. Salvar como `frontend/assets/landing/hero.webp` (mesmo caminho, 1200×675).
2. `python3 scripts/analysis/check_frontend.py` — deve continuar OK.
3. Conferir no navegador (MCP `chrome-devtools`), com
   `python3 -m http.server 8899 --directory frontend`:
   * 1440px: os dois estudantes (mochilas visíveis) à direita, o
     terço esquerdo só com fundo navy atrás do texto, console limpo;
   * 390px: sem overflow horizontal;
   * se algum brilho da arte vazar atrás das letras, ajustar
     `text-shadow` do H1/lead (nunca voltar véu sobre a arte).
4. Se `object-position` precisar de ajuste fino para manter os dois
   estudantes no crop, ajustar **só** o valor (a arte é gerada nesta
   orientação; `scaleX(-1)` continua proibido).
5. Remover esta pendência da seção 3 de `docs/frontend-landing.md`
   quando a arte nova entrar.
