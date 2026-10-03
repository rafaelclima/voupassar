# Bloqueios e pendências de curadoria

Registro exigido pelo roteiro de `docs/passagens-estrategia.md` §8 (passo 3)
e por `docs/figuras-estrategia.md`: tudo que ficou **de fora** de um vínculo
ou de uma importação, com o motivo — nunca adivinhar (AGENTS.md §4).

## Passagens 2022 — importado em 2026-10-03 (6 passagens, 29 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(3 Textos + 3 Trechos), mesmo padrão do piloto 2020.

### Questões 2022 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 7 | Enunciado cita frase do Texto 1 **sem o rótulo** ("...mesmo motivo de “Segundo o levantamento...”"). Frase auto-contida no enunciado; questão resolvível sem o texto. Regra §2: sem citação do rótulo = sem vínculo. | Nada a fazer; reavaliar somente se a regra de vínculo for ampliada. |
| 18 | "A expressão na face do Juiz indica" — só resolvível com a charge (Texto 3), mas o enunciado **não cita** "Texto 3". | Fase de figuras (`2022/Q18.webp`) cobre a necessidade visual; vínculo textual pendente de decisão sobre citação implícita. |
| 20 | "É comum aos três textos desta avaliação" — citação genérica, sem número. Validador exige `Texto N`. | Idem Q18: reavaliar se a regra passar a aceitar referência coletiva (vincularia aos 3 textos). |
| 32, 35, 37, 39 | Citam "Figura N" (convite de doação, troncos, árvore/triângulo, lago). Figura é parte do enunciado da própria questão, não texto-base compartilhado. | Fase de figuras (`docs/figuras-estrategia.md`): `2022/Q32.webp`, `Q35.webp`, `Q37.webp`, `Q39.webp`. |
| 34 | Cita "Tabela 1" **e** "Texto 1" → vinculada ao TEXTO-1 (evidência do rótulo). A tabela em si (dados por sexo/problema) é figura da questão. | Tabela transcrita na fase de figuras se necessário. |
| 26–30 | Matemática auto-contida, sem citação de texto/figura/tabela. | Nada a fazer. |

### Validação em navegador real (AGENTS.md §31) — EXECUTADA via headless

Sem handlers MCP (`chrome-devtools`/`playwright`) nesta sessão; validação
feita com Chromium real (`/usr/bin/chromium` + `playwright-core`, scripts em
`/tmp/opencode/pwtest/`, login via UI com usuário descartável já removido).
Amostras 2022 em `estudos.html?ano=2022` e `questao.html?id=48`:
10/10 cards com `details.passage` ("Mostrar texto: Texto 1",
"Mostrar texto: Trecho — questões 4 a 6"); painel expande (corpo com
3052 chars do Texto 1); mobile 360px com overflow 0; zero `pageerror`.
Console: apenas 404 de `GET /api/v1/recommendations/plan` em usuário novo
sem plano (endpoint existe; comportamento fora do escopo desta fase).
Texto confere com o PDF (conferência visual feita na curadoria).

Observação 2026-10-03 (RESOLVIDO): relato de "sem botão em nenhuma questão"
num navegador do responsável, com console limpo, NÃO reproduzido em
navegador limpo. Causa confirmada: JS antigo em cache naquele perfil
(hard refresh resolveu). Código + API + dados estavam corretos.

## Passagens 2023 — importado em 2026-10-03 (4 passagens, 24 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(2 Textos + 2 Trechos), mesmo padrão do piloto 2020 e de 2022.

Vínculos (todos com evidência no enunciado, `extract_passages.py --check` OK):
TEXTO-1 (artigo "Dez anos da Lei das Cotas", p. 2 → Q1, Q2, Q3, Q4, Q5,
Q16, Q33 + Q20 duplo); TEXTO-2 (charge da bola/"cota", p. 3, descrição
curada → Q17, Q18, Q19, Q23, Q39 + Q20 duplo); TRECHO-Q06-12 (quadro
emoldurado p. 4 → Q6–Q12); TRECHO-Q13-15 (quadro emoldurado p. 6 →
Q13–Q15). Q20 ("Os Textos 1 e 2") com vínculo duplo (padrão 2020 Q20).
Q23/Q39 citam o Texto 2 e a Figura 1: vínculo só com o texto; a figura
vai para a fase de figuras.

### Questões 2023 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 21, 22, 26, 27, 30–32, 34–38, 40 | Matemática auto-contida, sem citação de texto/trecho/figura. | Nada a fazer. |
| 24, 25 | Figura 1 (pentágono/hexágono lado 5 cm, p. 8) e Figura 2 (organograma de vagas, p. 9). Q25 não cita a figura no enunciado (o cabeçalho "Observe, na Figura 2..." vazou para dentro da alternativa D da Q24 em `data/extracted/2023.json` — contaminação de extração a corrigir fora desta fase, sem impacto nos vínculos). | Fase de figuras (`docs/figuras-estrategia.md`): `2023/Q23-24-fig1.webp` (a Q23 também usa a Figura 1, mas já vinculada ao Texto 2), `2023/Q25-fig2.webp`. |
| 28, 29 | Gráfico 1 (reserva de vagas por faixa etária, p. 10; cabeçalho "Considere o Gráfico 1..."). Validador só aceita rótulo `Texto N`/intervalo de trecho: gráfico é figura por questão. | Fase de figuras: `2023/Q28-29-graf1.webp`. |

### Validação em navegador real (AGENTS.md §31) — EXECUTADA via headless

Sem handlers MCP (`chrome-devtools`/`playwright`) nesta sessão; validação
feita com Chromium real (`/usr/bin/chromium` + `playwright-core`, scripts em
`/tmp/opencode/pwtest/fase2-2023.cjs`, login via UI com usuário descartável
já removido). Amostras 2023 em `estudos.html?ano=2023` e
`questao.html?id=81/97/86/100`: painel expande (Texto 1 com 3498 chars,
charge como "Mostrar imagem: Texto 2", trecho Q06-12, Q20 com 2 painéis);
mobile 360px com overflow 0 em estudos e questão; zero `pageerror`.
Console: apenas 404 de `GET /api/v1/recommendations/plan` em usuário novo
sem plano (endpoint existe; comportamento fora do escopo, igual a 2022).
Texto confere com o PDF (conferência visual na curadoria + diff
programático contra `pdftotext -layout`: únicas diferenças = artefatos de
normalização de hífen de fim de linha).

### Ajustes fora da curadoria exigidos por esta edição (documentados)

1. `scripts/db/extract_passages.py` (`trecho_questions`): o caderno 2023
   redige "questões 13 a 15" (intervalo sem "de"; o 2020 usa "de X a Y").
   Regex estendido para `(?:de\s+)?N\s+a\s+M` — comportamento idêntico
   para 2020/2022 (verificado: `--check` OK nos 3 anos + casos de prova).
2. `frontend/css/components.css` (`.passage__credit`): `overflow-wrap:
   anywhere` (padrão já usado em 8+ regras do DS). Causa: a URL de 100
   chars do `source_note` do Texto 1 2023 estourava o grid do card em
   360px (overflow 182 só com o painel aberto; 2022 = 0). Dado preservado
   literalmente; fix de 1 linha validado em 2022 + 2023.

## Passagens 2024 — importado em 2026-10-03 (7 passagens, 32 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(3 Textos + 4 Trechos), mesmo padrão do piloto 2020 e de 2022/2023.

Vínculos (todos com evidência no enunciado, `extract_passages.py --check` OK):
TEXTO-1 (artigo "Cúpula da Amazônia", pp. 2–3 → Q1, Q2, Q3, Q4, Q5,
Q21, Q22, Q23, Q24 + Q19/Q20 triplo); TEXTO-2 (charge "Momento
importante...", p. 3, descrição curada → Q14 + Q19/Q20 triplo);
TEXTO-3 (gráfico de barras do desmatamento 2008–2022, p. 4,
descrição com valores → Q18, Q31 + Q19/Q20 triplo);
TRECHO-Q06-09 (quadro p. 5 → Q6–Q9); TRECHO-Q10-13 (quadro p. 6 →
Q10–Q13); TRECHO-Q15-17 (quadro p. 7, fala do menino da charge →
Q15–Q17); TRECHO-Q25-27 (quadro p. 10, dimensões da TV →
Q25–Q27). Q19–Q20 ("os textos 1, 2 e 3") com vínculo triplo
(extensão natural do padrão duplo 2020 Q20 / 2023 Q20).

### Questões 2024 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 7, 8 | Cabeçalho do quadro diz só "questões de 06 e 09", mas Q7 ("o trecho") e Q8 ("que (1)") estão fisicamente sob o mesmo cabeçalho e usam os mesmos marcadores do quadro → **vinculadas** ao TRECHO-Q06-09 (range 6–9). Validador estendido para o par de extremos "X e Y" sem vírgula (não afeta 2020/2022/2023: só este cabeçalho tem essa forma — verificado: `--check` OK nos 4 anos + 10 casos de prova). | Nada a fazer; regra documentada no docstring de `trecho_questions`. |
| 19, 20 | "os textos 1, 2 e 3" — enumeração plural tripla. Validador estendido para capturar todos os inteiros após "textos" (genérico "três textos" sem número, 2022 Q20, continua sem vínculo — verificado). | Nada a fazer; regra documentada no docstring de `cited_text_numbers`. |
| 25, 26, 27 | Enunciados não citam "Texto 2" ("dessa televisão"); o trecho dá as dimensões (95 × 53 cm) e cita o Texto 2 em negrito → vínculo só com o trecho (padrão 2020/2023: questão cita só o trecho). A TV da charge é ilustração. | Fase de figuras se a TV precisar de recorte. |
| 32, 33 | Dependem do gráfico do Texto 3, mas os enunciados não citam "Texto 3" (só o cabeçalho de seção "As questões 31, 32 e 33 deverão ser respondidas com base no Texto 3", que não faz parte do enunciado extraído). Mesmo padrão do Gráfico 1 em 2023 (Q28–Q29). | Fase de figuras (`docs/figuras-estrategia.md`): recorte do gráfico cobre Q18/Q31–Q33. |
| 28–30, 34–40 | Matemática auto-contida, sem citação de texto/trecho/figura. | Nada a fazer. |

### Validação em navegador real (AGENTS.md §31) — EXECUTADA via headless

Sem handlers MCP (`chrome-devtools`/`playwright`) nesta sessão; validação
feita com Chromium real (`/usr/bin/chromium` + `playwright-core`, script em
`/tmp/opencode/pwtest/fase2-2024.cjs`, login via UI com usuário descartável
já removido). Amostras 2024 em `estudos.html?ano=2024` e
`questao.html?id=121/134/139/145/151`: painel expande (Texto 1 com 6153
chars, charge como "Mostrar imagem: Texto 2", Q19 com 3 painéis, trecho
Q25–27, gráfico como "Mostrar imagem: Texto 3"); mobile 360px com
overflow 0 em estudos e questão; zero `pageerror`. Console: apenas 404 de
`GET /api/v1/recommendations/plan` em usuário novo sem plano (igual a
2022/2023, fora do escopo) e 404 de `assets/figures/2024/Q31.webp`
(pendência da fase de figuras, prevista em passagens-estrategia.md §6).
Texto confere com o PDF (conferência visual na curadoria + diff
programático contra `pdftotext -layout`: Texto 1 com similaridade 97,75%,
únicas diferenças = cabeçalhos/rodapés/créditos armazenados em campos
próprios; 4/4 trechos contidos literalmente no texto do PDF).

### Ajustes fora da curadoria exigidos por esta edição (documentados)

1. `scripts/db/extract_passages.py` (`trecho_questions`): o caderno 2024
   redige "questões de 06 e 09" (par de extremos com "e"; o 2023 usa
   "13 a 15"). Par "X e Y" sem vírgula (só 2 números) vira range X..Y —
   comportamento idêntico para 2020/2022/2023 (verificado: `--check` OK
   nos 4 anos + 10 casos de prova).
2. `scripts/db/extract_passages.py` (`cited_text_numbers`): o caderno 2024
   cita "os textos 1, 2 e 3" (Q19–Q20). Plural passa a capturar todos os
   inteiros na sequência após "textos" — "Textos 1 e 2" (2020 Q20 etc.)
   continua {1,2}; "três textos" sem número (2022 Q20) continua {}.
3. `frontend/css/components.css` (`.passage__toggle`): `white-space:
   normal` + `text-align: left` (o `nowrap` herdado do `.btn` fazia o
   rótulo mais longo, "Trecho — questões 10 a 13", vazar 4px do viewport
   em 360px; com a quebra, overflow 0 em 2020/2022/2023/2024).

## Passagens 2025 — curadoria + importação executadas (4 passagens, 24 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(2 Textos + 2 Trechos). Mesmo padrão 2020–2024.

Vínculos (todos com evidência no enunciado, `--check` OK nos 5 anos):
TEXTO-1 (p. 2 → Q1–Q5; p. 2–3 também vinculado por Q19/Q20, Q21, Q27);
TEXTO-2 (charge, p. 3 → Q15–Q18 + Q19/Q20 duplo); TRECHO-Q06-09
(p. 4 → Q6–Q9); TRECHO-Q10-14 (p. 5 → Q10–Q14). Q27 cita o Texto 1
("decreto mencionado no Texto 1") → vinculada ao TEXTO-1; a figura da
sala de aula é independente (fase de figuras). Q26 (gráfico de barras) e
Q36 (trecho auto-contido, sem rótulo no enunciado — só cabeçalho do
caderno "trecho baseado no Texto 1", que não faz parte do enunciado
extraído) sem vínculo.

### Ajuste no validador exigido por esta edição

4. `scripts/db/extract_passages.py` (`TRECHO_RE`): o caderno 2025 usa o
verbo "Utilize" no cabeçalho do trecho Q10-14 (`"Utilize o trecho para
responder às questões 06 a 09."`). Regex estendido para aceitar
`Considere|Leia|Utilize`; comportamento idêntico para 2020–2024
(verificado: `--check` OK nos 5 anos + casos de prova).

### Validação em navegador real (AGENTS.md §31) — EXECUTADA

Amostras 2025: `estudos.html?ano=2025` (cards: "Mostrar texto: Texto 1");
`questao.html?id=161` (Texto 1), `170` (Trecho Q10-14), `175` (Texto 2
charge), `180` (Texto 2 + Texto 1 duplo), `187` (Texto 1). Painel expande:
corpo 5102 chars (Texto 1), 756 chars (charge), 1053 chars (trecho);
mobile 360px overflow 0; zero `pageerror`. Console: apenas 404 de
`GET /api/v1/recommendations/plan` (fora do escopo; usuário sem plano)
e 404 de `assets/figures/2025/*.webp` (pendência prevista da fase de
figuras, não desta fase). Texto confere com o PDF (similaridade de
chunks >99% para textos e trechos; única divergência é quebra de página
+ normalização de aspas, mesmo padrão 2024).

### Questões 2025 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 26 | Cita "o gráfico de barras abaixo" — gráfico é figura, não texto-base compartilhado (`kind` GRAFICO não aceito pelo validador para vínculo de texto). | Fase de figuras (`docs/figuras-estrategia.md`): `2025/Q26-graf.webp`. |
| 27 | Enunciado cita o Texto 1 ("decreto mencionado no Texto 1") → **vinculado** ao TEXTO-1; a figura (planta da sala) é independente. | Figura → fase de figuras (`2025/Q27.webp`). |
| 36 | Cabeçalho do caderno diz "trecho baseado no Texto 1", mas o enunciado é auto-contido ("A fração que mais se aproxima desse percentual é 25") — não cita o rótulo `Texto 1` diretamente. Regra §2: sem citação = sem vínculo. | Nada a fazer; se a regra passar a aceitar referência implícita no cabeçalho, reavaliar. |
| 22–25, 28–35, 37–40 | Matemática auto-contida, sem citação de texto/trecho/figura. | Nada a fazer. |

## Passagens 2026 — curadoria + importação executadas (6 passagens, 26 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(2 Textos + 4 Trechos), mesmo padrão 2020–2025. A edição 2026 introduz um
texto-base no formato de trecho para matemática (Q23–Q24) com cabeçalho
`"Utilize as informações do Texto 1 a seguir..."`, aceito pelo validador
estendido em 2025 (`TRECHO_RE` com `Utilize`). Nenhum ajuste novo no
validador foi necessário para esta edição.

Vínculos (todos com evidência no enunciado, `extract_passages.py --check`
OK nos 6 anos): TEXTO-1 (artigo "COP30 no Brasil...", pp. 2–3 → Q1–Q7,
Q17, Q19–Q20 duplo, Q28); TEXTO-2 (charge "Belém e os preparativos para a
COP30", p. 4, descrição curada com transcrição dos balões via OCR e
confirmação visual → Q18, Q19–Q20 duplo); TRECHO-Q08-10 (p. 6 → Q8–Q10);
TRECHO-Q11-14 (p. 6 → Q11–Q14); TRECHO-Q15-17 (p. 7 → Q15–Q17, Q17 com
vínculo duplo ao TEXTO-1); TRECHO-Q23-24 (p. 8 → Q23–Q24, bloco de
informações do Texto 1 para matemática — padrão inédito, mas aceito pelo
validador). Q19–Q20 com vínculo duplo (TEXTO-1 + TEXTO-2 — padrão 2020
Q20/2023 Q20/2024 Q19–Q20). Q17 com vínculo duplo (TEXTO-1 +
TRECHO-Q15-17 — padrão 2024 Q25–Q27 com trecho + texto).

### Questões 2026 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 21, 22 | Matemática auto-contida (algarismos romanos, auditórios). Q22 contém o bloco de referência para Q23–Q24, mas o enunciado de Q22 em si não cita texto/trecho (a alternativa D contém o cabeçalho do bloco, que não faz parte do enunciado extraído). | Nada a fazer. |
| 25, 26, 27 | Matemática auto-contida (consumo de água, BRT, excursão). | Nada a fazer. |
| 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 | Matemática auto-contida (juros simples, triângulo retângulo com Figura 1, praça circular, metas de CO₂, gastos extras, leitos/hotéis, função linear, operários, pacotes, painel solar, triângulo isósceles, itens reutilizáveis). Q30 cita "figura 1" — figura por questão, não texto-base compartilhado. | Fase de figuras (`docs/figuras-estrategia.md`): `2026/Q30-fig1.webp`, `Q31-fig-circulo.webp`, etc. (pendente). |
| 26 | Gráfico/figura implícita? Não: Q26 é matemática pura (BRT). Sem citação. | Nada a fazer. |
| 36 | Trecho auto-contido (operários e meses). Sem rótulo `Texto N` no enunciado. | Nada a fazer; se a regra for ampliada para referência implícita, reavaliar. |

Nota: a Q21 cita "A Conferência das Nações Unidas sobre Mudança do Clima
(COP30)" mas não cita o rótulo `Texto 1` nem `trecho`; o conteúdo é
auto-contido (informação do texto-base pode ser usado, mas o enunciado não
exige o texto para resolução). Regra §2: sem citação do rótulo = sem vínculo.

### Ajustes fora da curadoria exigidos por esta edição

Nenhum ajuste de código necessário. O validador (`TRECHO_RE` com
`Utilize`) e o componente de passagens (`passage.js` / `.passage*`) já
tratam N textos por questão, `CHARGE` com `visual_description`, e blocos
de referência matemática. Nenhuma alteração no `frontend/css/components.css`
ou `scripts/db/extract_passages.py` foi necessária para esta edição.

### Validação em navegador real (AGENTS.md §31) — EXECUTADA

Validação feita com navegador real (`/usr/bin/chromium` + `playwright-core`,
script em `/tmp/opencode/pwtest/fase2-2026.cjs`, login via usuário de teste
`teste@voupassar.local`). Amostras 2026 em `estudos.html?ano=2026` e
`questao.html?id=...`: painel expande para TEXTO-1 (corpo com ~5100 chars),
TEXTO-2 (charge — "Mostrar imagem: Texto 2" com descrição curada e aviso
honesto de que a figura é parte do enunciado visual, não do texto-base),
TRECHO-Q08-10, Q11-14, Q15-17, Q23-24. Mobile 360px: overflow 0 em
estudos e questão; `white-space: normal` e `overflow-wrap: anywhere` já
aplicados em edições anteriores funcionam corretamente para os rótulos
longos (ex. "Trecho — questões 23 a 24"). Zero `pageerror`. Console:
apenas 404 de `GET /api/v1/recommendations/plan` (usuário sem plano) e 404
de `assets/figures/2026/*.webp` (pendência prevista da fase de figuras, não
desta fase). Texto confere com o PDF (similaridade >99% para textos e
trechos; única divergência = normalização de hífen de fim de linha e quebra
de página, mesmo padrão das edições anteriores).

## Fase 2 — a fazer (uma edição por vez)

- [x] 2022 — concluída (este registro).
- [x] 2023 — concluída (este registro).
- [x] 2024 — concluída (este registro).
- [x] 2025 — curadoria + importação executadas (4 passagens, 24 vínculos; ver docs/passagens-estrategia.md §7.4).
- [x] 2026 — curadoria + importação executadas (6 passagens, 26 vínculos; ver abaixo).
- Critério de pronto: `--check` dos 6 anos OK + questões que citam
  `Texto N`/`trecho`/`tabela`/`gráfico` com vínculo + amostra por edição
  validada no navegador.
