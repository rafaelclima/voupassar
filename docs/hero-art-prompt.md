# Arte da hero (`assets/landing/hero*.webp`)

## Derivados responsivos (EM USO)

A mestre é `assets/landing/hero_full.png` (1672×941, 16:9 gerada no Nano
Banana 2 para este cenário: esquerda vazia em navy para o texto, cena à
direita). Dela saem 3 derivados via ffmpeg (só scale proporcional),
qualidade 80–82:

```bash
ffmpeg -y -i hero_full.png -vf "scale=960:-2"  -c:v libwebp -quality 80 frontend/assets/landing/hero-960.webp    # ~15 KB
ffmpeg -y -i hero_full.png -vf "scale=1280:-2" -c:v libwebp -quality 80 frontend/assets/landing/hero-1280.webp   # ~21 KB
ffmpeg -y -i hero_full.png -vf "scale=1672:-2" -c:v libwebp -quality 82 frontend/assets/landing/hero-1672.webp   # ~33 KB
cp frontend/assets/landing/hero-1280.webp frontend/assets/landing/hero.webp  # fallback / compat
```

O HTML usa `<picture>` com `media="(min-width: 64rem)"` + `srcset`/`sizes`
(`(min-width: 120rem) 120rem, 100vw`): o mobile nem baixa os arquivos
grandes e cada viewport recebe resolução próxima da sua densidade. A arte
entra em **tela cheia** (`absolute inset-0`, `cover`) atrás de tudo, com
sombra sutil só à esquerda via CSS (texto legível, cena intacta).
`check_frontend.py` exige os 4 arquivos (`hero.webp` + 3 derivados).

Nota: a mestre tem 1672px — em viewport 1920 (DPR 1) há upscale leve de
~1,15×, imperceptível nesta arte vetorial sem texto. Se um dia houver
mestre 2560+, basta acrescentar o derivado e o `srcset`.
Histórico: a variante retrato (`hero_portrait_45.jpg` + derivados
768/1100/1600/1856) foi removida quando a paisagem chegou — metade
direita prendia a arte numa coluna em vez da tela toda.

## Variante atual: retrato full-bleed (EM USO)

A hero é um **cartão arredondado** (referência `assets/home_hero_example.png`):
copy à esquerda, arte sangrando até as bordas direita/topo/fundo. No
mobile a mesma arte aparece **inteira** abaixo da copy; no desktop ela
preenche a metade direita via `cover` (caixa ~0.85). Por isso a arte
precisa nascer em **retrato** — a 16:9 anterior sangrava demais.

### Prompt (usar como está, em inglês)

```text
Modern flat-vector illustration, VERTICAL portrait orientation (4:5),
deep navy blue background with a soft radial gradient (lighter blue glow
in the upper area, #0a2138 → #123a5e), no text, no logos, no watermark.

TWO school-age students, a boy and a girl, seen from BEHIND, both wearing
backpacks, standing side by side in the LOWER-RIGHT half of the frame,
fully visible from head to feet. The boy has dark curly hair, a navy blue
hoodie and a dark backpack; the girl stands slightly beside him, with dark
hair in a ponytail, a light blue top and a blue backpack. Both look toward
a rising path of glowing blue platforms/steps that ascend diagonally from
lower-center toward the upper area, with small floating UI cards (question
cards, abstract lines only, no readable text) and a glowing circular
timer/gauge icon in the upper area.

Keep faces, hands, feet and the timer at least 8% away from every edge of
the frame (bleed-safe: edges get slightly trimmed where the art meets the
card). The LEFT edge of the image must fade into plain deep navy #0a2138
so it meets the text side seamlessly. The upper two thirds stay calm and
airy (mostly gradient + glow), the characters anchor the lower right.

Style: clean modern 2D vector, soft glowing edges, subtle depth, gentle
blue rim light, calm and serious educational mood, not childish, not
cartoonish, no photorealism, no heavy 3D. Palette: navy, deep blue, soft
light blue and a single warm accent color only.

Negative: no text, no letters, no numbers, no UI mockups with text, no
watermark, no signature, no cropped heads/hands/feet, no duplicated
characters, no distorted anatomy, no frame or border, no vignette.
```

### Por que cada exigência

| Exigência | Motivo |
|---|---|
| Retrato 4:5 | A sangria do desktop (~0.85) + o mobile inteira pedem altura; 16:9 sangrava demais |
| Sujeitos 8% para dentro | O `cover` do desktop apara as bordas — rostos/pés/cronômetro não podem encostar nelas |
| Borda esquerda em navy puro | Encontra a copy sem emenda aparente |
| Sem texto legível | Imagem gerada não pode exibir enunciado real (AGENTS.md §11) |

### Pipeline (sem esticar, só scale proporcional)

```bash
# Exportar a mestre em ~1100px de largura, mantendo a proporção
# (ex. hero45 1856×2304 → 1100×1366)
ffmpeg -y -i hero_full.jpg -vf "scale=1100:-2" \
  -c:v libwebp -quality 80 frontend/assets/landing/hero.webp
```

Referência: ~27 KB. Atualizar `width`/`height` do `<img>` no HTML para
as dimensões finais.

---

## Histórico: variante 16:9 (substituída)

> A 16:9 serviu às heroes full-bleed e vitrine. Com o cartão PIXORA ela
> sangrava demais na metade direita — por isso a variante retrato acima.

## Por que regenerar (e não espelhar em CSS) — contexto original

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

## Depois de gerar (pipeline de derivados — sem esticar, só crop + scale)

A fonte em alta (ex.: `hero_full.jpg`, 2752×1536) **não** entra no repo.
Dela saem 3 derivados via ffmpeg (nunca `scale` com distorção: só crop
ou scale proporcional):

```bash
# Desktop padrão (1200×675) — 10px de crop à esquerda preservam a direita
ffmpeg -y -i hero_full.jpg \
  -vf "scale=1210:675,crop=1200:675:10:0,scale=1200:675" \
  -c:v libwebp -quality 80 frontend/assets/landing/hero.webp
# Desktop grande (2400px; altura proporcional, sem crop)
ffmpeg -y -i hero_full.jpg -vf "scale=2400:1339" \
  -c:v libwebp -quality 78 frontend/assets/landing/hero-2400.webp
```

Tamanhos de referência: `hero.webp` ~19 KB, `hero-2400.webp` ~43 KB.
A arte entra **inteira** na página (sem `cover`, sem crop no CSS) nas
duas variantes — por isso só existem esses dois derivados.

1. Salvar os 2 arquivos nos caminhos acima (o HTML referencia os dois
   via `srcset`; ambos seguem exigidos pelo `check_frontend.py`).
2. `python3 scripts/analysis/check_frontend.py` — deve continuar OK.
3. Conferir no navegador (MCP `chrome-devtools`), com
   `python3 -m http.server 8899 --directory frontend`:
   * 1920px e 1440px: full-bleed nítido (fonte 2400, sem upscale — medir
     `clientWidth / naturalWidth ≤ 1` no console), os dois estudantes
     (mochilas visíveis) à direita, texto legível à esquerda;
   * 1024×768 landscape: overlay com estudantes inteiros;
   * 768×1024 retrato e 390px: cartão empilhado com a variante retrato,
     texto sobre navy limpo, sem overflow horizontal, console limpo;
   * se algum brilho da arte vazar atrás das letras no desktop, ajustar
     `text-shadow` do H1/lead (nunca voltar véu sobre a arte).
4. Se `object-position` precisar de ajuste fino para manter os dois
   estudantes no crop do desktop, ajustar **só** o valor (a arte é gerada
   nesta orientação; `scaleX(-1)` continua proibido).
5. Cuidado com o containing block: o grid da hero é estático de
   propósito — o `inset: 0` do full-bleed referencia a `section`. Ver
   `docs/frontend-landing.md` §3.
