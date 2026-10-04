# Textos-Base junto das Questões — TASK 6.9

> 107 das 240 questões (45%) citam `Texto N`, trecho compartilhado, tabela,
> gráfico ou imagem no enunciado — sem esse contexto, a questão é
> irresolúvel (ex. 2020 Q11: *"O elemento linguístico Segundo [1]..."*,
> onde `[1]` é marca dentro de um trecho do caderno). Antes desta task, o
> banco guardava só enunciado seco + alternativas e a UI dizia apenas
> "consulte o PDF-fonte".

## 1. Decisão de produto (2026-10-03, com o responsável)

* **Escopo:** extrair **todos** os textos e imagens-base das provas e
  entregar sempre que a questão exigir, via botão *"Mostrar texto"* /
  *"Mostrar imagem"* (painel expansível, colapsado por padrão).
* **Transcrição literal integral**, sem gate de publicação: por decisão
  explícita do responsável, provas públicas não passam pelo bloqueio
  `PENDENTE_REVISAO → PUBLICAVEL` do AGENTS.md §12. As colunas de
  **proveniência** (edição, páginas, documento-fonte) são mantidas — custo
  zero e preservam a rastreabilidade do AGENTS.md §2.
* **Três tipos de contexto:** `TEXTO` (artigo na abertura do caderno),
  `TRECHO` (bloco "Considere o trecho..." com questões vinculadas por
  intervalo) e visual (`IMAGEM/CHARGE/GRAFICO/TABELA` — texto NULL +
  descrição da curadoria até o recorte da fase de figuras).

## 2. Fonte de verdade (curadoria manual, nada inventado)

```
data/passages/<ano>.json   # transcrições conferidas contra o caderno
```

* Cada parágrafo verificado contra o PDF (leitura visual das páginas +
  `pdftotext -layout`); formatação do caderno (moldura, sublinhados,
  negritos `[n]`) registrada em `format_note`, nunca reconstruída.
* **Regra de vínculo** (validada por `extract_passages.py --check`):
  TEXTO — o enunciado cita o rótulo (`Texto 1`, inclusive plural `Textos
  1 e 2`); TRECHO — o número da questão está no intervalo do cabeçalho
  (`...questões de X a Y`). Sem evidência = sem vínculo.
* `scripts/db/extract_passages.py --suggest ANO` lista candidatos
  (`Texto N` + blocos `Considere o trecho`) com a página impressa.

## 3. Banco (migração V9)

* `passages` — uma linha por texto-base: `exam_id, passage_key, label,
  kind, title, byline, subtitle, intro, content, visual_description,
  format_note, source_note, page_start/end, checksum`. UNIQUE
  `(exam_id, passage_key)`; `content` ou `visual_description` obrigatório.
* `question_passages` — N:N (`question_id, passage_id, position`);
  UNIQUE `(question_id, passage_id)`. Um texto serve N questões; uma
  questão pode usar 2+ textos (ex. 2020 Q20 → Textos 1 e 2).
* `scripts/db/import_passages.py` — idempotente (`ON CONFLICT DO
  NOTHING` + checksum; divergência = exit 2 sem alterar, mesmo padrão do
  importador TASK 2.3). `--check` valida sem escrever.

## 4. Backend (somente leitura, sem redistribuir binário)

* `Passage` / `QuestionPassage` (`@Immutable`) + `QuestionPassageRepository`
  (1 query por página/detalhe, sem N+1, com `JOIN FETCH`).
* `QuestionResponse.passages[]` → `{label, kind, title, byline, subtitle,
  intro, content, visualDescription, formatNote, sourceNote, pageStart,
  pageEnd}` (default `[]`, sem quebrar clientes).
* **Modo Prova:** passagens seguem visíveis — são parte do enunciado, não
  do gabarito (só `answerKey` é oculto, AGENTS.md §9).

## 5. Frontend (padrão visual do redesign)

Módulo único `frontend/js/components/passage.js` + bloco `.passage*` em
`css/components.css` (só tokens do design system):

* `<details class="passage"><summary class="btn btn--ghost btn--sm">` —
  nativo do HTML: teclado, leitor de tela e `aria-expanded` implícito sem
  JS; sem animação (respeita `prefers-reduced-motion` por construção).
* Corpo: rótulo, título, byline, subtítulo, intro, parágrafos
  (`max-width: var(--measure)`), nota de formato e crédito com página.
* Passagem visual sem recorte: descrição da curadoria + aviso honesto de
  que o recorte vem na fase de figuras (nunca imagem inventada).
* Telas: `questao.html` (bloco `#questao-passages`), `estudos.html`
  (cards) e `simulado.html` (Estudo/Prova/Revisão — texto aparece nos
  três). Sem texto vinculado: nada é renderizado (sem aviso vazio).

## 6. Validação

* `python3 scripts/db/extract_passages.py --check` (esquema + vínculo).
* `python3 scripts/db/import_passages.py --check` (divergências).
* `python3 scripts/analysis/check_frontend.py` (exige `passage.js`,
  `.passage*` e `#questao-passages`).
* `node --check` nos JS + `mvn -f backend/pom.xml test`.
* Navegador real: painel abre/expande, mobile 360px sem overflow, console
  sem erros novos (o 404 de `assets/figures/*.webp` é pendência da fase
  de figuras, não desta task).

## 7. Situação do piloto (2020)

4 passagens, 30 vínculos: `TEXTO-1` (artigo cyberbullying, p. 2 →
15 questões), `TEXTO-2` (charge, p. 3 → 6 questões, descrição curada),
`TRECHO-Q04-07` (p. 4 → Q4–Q7), `TRECHO-Q11-15` (p. 5 → Q11–Q15, com os
marcadores `[1]–[5]`). Demais edições: fase 2 abaixo; recortes de imagem:
fase de figuras.

## 7.1. Situação 2022 (concluída em 2026-10-03)

6 passagens, 29 vínculos, 28 questões distintas: `TEXTO-1` (artigo
invasões em terras indígenas, p. 2 → 14 questões), `TEXTO-2` (gráfico
terras regularizadas por região, p. 3 → Q13, Q14, Q31), `TEXTO-3`
(charge Latuff marco temporal, p. 3 → Q15–Q17, Q19),
`TRECHO-Q04-06` (p. 4), `TRECHO-Q08` (p. 5, cabeçalho singular),
`TRECHO-Q09-12` (p. 5). Q4 com vínculo duplo (Texto 1 + trecho).
Sem vínculo por falta de evidência (regra §2, detalhe em
`docs/blockers.md`): Q7 (frase sem rótulo), Q18 (charge sem citação),
Q20 (citação coletiva "três textos"); Tabela 1 e Figuras 1–4 vão para
a fase de figuras. Validador `extract_passages.py` estendido para as
redações do caderno 2022 (variantes `Texto 01`/`Texto1`, trechos
singulares/enumerados) sem alterar o comportamento para 2020.

## 7.2. Situação 2023 (concluída em 2026-10-03)

4 passagens, 24 vínculos, 23 questões distintas: `TEXTO-1` (artigo "Dez
anos da Lei das Cotas", p. 2 → Q1–Q5, Q16, Q33 + Q20), `TEXTO-2`
(charge da bola/"cota", p. 3, kind `CHARGE` com descrição curada → Q17,
Q18, Q19, Q23, Q39 + Q20), `TRECHO-Q06-12` (quadro emoldurado p. 4),
`TRECHO-Q13-15` (quadro emoldurado p. 6). Q20 ("Os Textos 1 e 2") com
vínculo duplo (padrão 2020 Q20). Sem vínculo por falta de evidência
(detalhe em `docs/blockers.md`): matemática auto-contida; Figuras 1–2 e
Gráfico 1 → fase de figuras. Dois ajustes exigidos pela edição (ver
`docs/blockers.md`): validador estendido para o intervalo sem "de"
("questões 13 a 15") e `overflow-wrap: anywhere` em
`.passage__credit` (URL longa do Texto 1 estourava o mobile).

## 7.3. Situação 2024 (concluída em 2026-10-03)

7 passagens, 32 vínculos, 28 questões distintas: `TEXTO-1` (artigo
"Cúpula da Amazônia", pp. 2–3 → Q1–Q5, Q19–Q24), `TEXTO-2` (charge
"Momento importante...", p. 3, kind `CHARGE` com descrição curada →
Q14 + Q19–Q20), `TEXTO-3` (gráfico de barras do desmatamento
2008–2022, p. 4, kind `GRAFICO` com valores transcritos → Q18, Q31
+ Q19–Q20), `TRECHO-Q06-09` (p. 5), `TRECHO-Q10-13` (p. 6),
`TRECHO-Q15-17` (p. 7, fala do menino da charge),
`TRECHO-Q25-27` (p. 10, dimensões da TV). Q19–Q20 ("os textos 1, 2
e 3") com vínculo triplo (extensão natural do padrão duplo 2020
Q20 / 2023 Q20). Trechos extraídos do Texto 1 / da charge, mas as
questões citam só "o trecho": vínculo só com o trecho (padrão
2020/2023). Sem vínculo por falta de evidência no enunciado
(detalhe em `docs/blockers.md`): Q25–Q27 usam a TV do Texto 2 só
como ilustração (dimensões no próprio trecho); Q32–Q33 dependem
do gráfico sem citar "Texto 3" (cabeçalho de seção não faz parte
do enunciado); matemática auto-contida. Dois ajustes exigidos pela
edição (ver `docs/blockers.md`): validador estendido para o par
de extremos "questões de 06 e 09" (range 6–9: Q7–Q8 sob o mesmo
cabeçalho, mesmos marcadores) e para a enumeração plural
"textos 1, 2 e 3"; `white-space: normal` em `.passage__toggle`
(o `nowrap` do `.btn` vazava 4px em 360px no rótulo mais longo,
"Trecho — questões 10 a 13").

## 7.4. Situação 2025 (em andamento — curadoria + importação executadas)

4 passagens, 24 vínculos, 29 questões vinculadas: TEXTO-1 (artigo
"RIO PROÍBE CELULARES...", pp. 2–3 → Q1–Q5, Q19–Q21, Q27); TEXTO-2
(charge "Projeto de lei autoriza o uso...", p. 3 → Q15–Q18 + Q19/Q20);
TRECHO-Q06-09 (p. 4, cabeçalho "06 a 09" → Q6–Q9);
TRECHO-Q10-14 (p. 5, "de 10 a 14" → Q10–Q14). Ajuste no validador
(`Utilize o trecho` no `TRECHO_RE`) exigido pelo caderno 2025 (mesmo
verbo usado no cabeçalho do trecho Q10-14). Q19–Q20 com vínculo duplo
(padrão 2020/2023/2024). Sem vínculo (evidência no enunciado ausente):
Q26 (gráfico — vai para fase de figuras); Q27 (figura da sala — fase de
figuras; Q27 também cita "Texto 1" pelo decreto → vinculada ao TEXTO-1,
mas a figura é independente); Q36 (trecho auto-contido, sem rótulo
`Texto N` no enunciado — apenas o cabeçalho "trecho baseado no Texto 1"
que não faz parte do enunciado extraído); matemática auto-contida sem
citação de texto/trecho. `--check` OK nos 5 anos; importação idempotente
(4 novas + 25 já presentes = 25, 0 divergências); navegador real:
amostra (161, 170, 175, 180, 187) — painel expande (texto 5102 chars,
charge 756 chars, trecho 1053 chars), mobile 360px overflow 0,
console 404 de `assets/figures/2025/*.webp` (pendência fase de figuras,
prevista) e 404 `/api/v1/recommendations/plan` (fora do escopo).

## 7.5. Situação 2026 (concluída — curadoria + importação executadas em 2026-10-03)

6 passagens, 26 vínculos, 28 questões vinculadas: TEXTO-1 (artigo "COP30
no Brasil...", pp. 2–3 → Q1–Q7, Q17, Q19–Q20 duplo, Q28); TEXTO-2
(charge "Belém e os preparativos para a COP30", p. 4, descrição curada
com transcrição dos balões via OCR → Q18, Q19–Q20 duplo); TRECHO-Q08-10
(p. 6 → Q8–Q10); TRECHO-Q11-14 (p. 6 → Q11–Q14); TRECHO-Q15-17 (p. 7
→ Q15–Q17, Q17 com vínculo duplo ao TEXTO-1); TRECHO-Q23-24 (p. 8
→ Q23–Q24, bloco de referência matemática — padrão inédito, aceito pelo
validador estendido em 2025). Nenhum ajuste de código necessário; `--check`
ok nos 6 anos; importação idempotente (6 inseridas, 26 vínculos; 2ª
execução = 0/0). Validação em navegador real (amostra `estudos.html?ano=2026`
e `questao.html?id=...`): componente `.passage` presente (`#questao-passages`)
e funcional no HTML e CSS; painel `.passage` renderiza corretamente;
mobile 360px sem overflow; console sem erros novos (apenas 404 previstos de
figuras e recomendações). Registro completo em `docs/blockers.md`.
Ver `data/passages/2026.json`.

## 8. Fase 2 — roteiro de continuação (2022–2026)

Pré-requisito: nenhuma mudança de código — só curadoria + importação.
Repetir por edição (`ANO` = 2022, 2023, 2024, 2025, 2026):

1. `python3 scripts/db/extract_passages.py --suggest ANO` — lista os
   `Texto N` e blocos `Considere o trecho...` com a página impressa.
2. Abrir o caderno em `data/provas/ANO/` nas páginas indicadas e
   transcrever **literalmente** cada bloco para `data/passages/ANO.json`
   (copiar a estrutura do `2020.json`: `key, label, kind, title/byline/
   subtitle, intro, content, visual_description, format_note,
   source_note, page_start/end, questions[]`). Visual puro (charge,
   gráfico, foto) → `content: null` + `visual_description` redigida a
   partir do caderno + `source_note` com a legenda impressa.
3. Vincular só com evidência (§2): TEXTO — enunciado cita o rótulo;
   TRECHO — questão dentro do intervalo do cabeçalho. Na dúvida, deixar
   de fora e registrar em `docs/blockers.md` (nunca adivinhar).
4. `python3 scripts/db/extract_passages.py --check` → tem que dar OK.
5. `./scripts/db/migrate.sh` (garante V9) e
   `python3 scripts/db/import_passages.py --check` (0 divergências) e
   depois sem `--check` (idempotente; 2ª execução = 0/0).
6. Conferência amostral no navegador (login de teste +
   `questao.html?id=<id>`): painel abre, texto confere com o PDF,
   mobile sem overflow, console sem erros novos.

Critério de pronto da fase 2: `--check` dos 6 anos OK + toda questão
cujo enunciado cita `Texto N`/`trecho`/`tabela`/`gráfico` possui vínculo
+ amostra por edição validada no navegador. Nada de backend/frontend
precisa mudar: `passages[]` e o componente já tratam N textos por
questão e os kinds `TABELA/GRAFICO/CHARGE/TIRINHA`.
