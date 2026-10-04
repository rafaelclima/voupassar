# Correção das frações nas alternativas (2026-10-03/04)

> Rastreabilidade (AGENTS.md §4): valores conferidos questão a questão no
> caderno oficial em PDF. Nada aqui é inferência do extrator.

## 1. O que aconteceu

A extração achatou frações verticais do PDF em texto (`". 24 5"` etc.).
O script `scripts/analysis/check_math_options.py` converteu `". NUM DEN"`
em `"NUM/DEN"` assumindo que a ordem extraída era numerador primeiro —
**estava invertida**, gerando probabilidades/densidades > 1 (impossíveis).

## 2. Valores corretos (lidos no PDF)

| Questão | A | B | C | D |
|---|---|---|---|---|
| 2020 Q26 | 23/11 | 23/12 | 11/23 | 12/23 |
| 2020 Q37 | 121𝑥/50 | 127𝑥/20 | 117𝑥/10 | 157𝑥/50 |
| 2020 Q38 | 7/12 | 5/12 | 2/3 | 1/3 |
| 2022 Q23 | 5/24 | 5/2400 | 5/24000 | 5/240 |
| 2022 Q34 | 11/35 | 3/35 | 1/35 | 20/35 |
| 2022 Q39 | 𝐴𝐶̅·𝐸𝐶̅/𝐴𝐵̅ | 𝐴𝐵̅·𝐴𝐶̅/𝐶𝐸̅ | 𝐴𝐵̅·𝐶𝐷̅/𝐵𝐶̅ | 𝐵𝐶̅·𝐶𝐸̅/𝐴𝐶̅ |
| 2024 Q21 | 1/4 | 1/8 | 2/3 | 3/5 |
| 2024 Q24 | 2/5 | 4/5 | 2/7 | 4/7 |
| 2025 Q33 | 15/28 | 13/56 | 13/14 | 13/28 |
| 2025 Q36 | 25/30 | 732/900 | 741/900 | 24/30 |

Notas:

* 2020 Q37 já estava certa (expressão algébrica, sem inversão) — mantida,
  inclusive o `𝑥` (itálico matemático) original da extração.
* 2024 Q24-D veio contaminada com o enunciado da Q25 na extração;
  corrigida para `4/7` pelo PDF.
* 2022 Q34: alternativas contaminadas com o texto da Tabela 1;
  corrigidas para `11/35, 3/35, 1/35, 20/35` pelo PDF.
* 2022 Q39: geometria com sobrelinha (segmentos) embaralhada; gravada na
  convenção do enunciado (`̅̅̅̅` + letras em negrito matemático) e o
  renderer existente já empilha numerador/denominador (texto puro).
* `check_math_options.py` **não deve ser reusado sem correção**: a ordem
  dele está invertida e ele ignora o padrão `". NUM"` (alternativas D).

## 3. Onde a correção vive

* `data/extracted/<ano>.json` — fonte corrigida (commits `338b9f8`).
* `database/migrations/V10__fix_fraction_options.sql` — 40 UPDATEs em
  `question_options` (10 questões × A-D) + 10 `checksum` recalculados com
  a mesma função do `import_questions.py` (importador segue idempotente,
  sem divergência). Aplicada na produção via `psql` em 2026-10-04 (backup
  prévio em `/root/backups/voupassar-preV10-20261004.dump`); o Flyway a
  registra como no-op no próximo deploy (valores já conferem).
* `frontend/js/components/math.js` — `expressionNode()` renderiza
  `"NUM/DEN"` como fração empilhada (`.fraction`) via DOM seguro, sem
  `innerHTML` (XSS). Usado em `questao.js`, `estudos.js` e `simulado.js`.
* Gabaritos intactos (só texto das alternativas mudou) — histórico e
  aproveitamento dos alunos preservados.

## 4. Tema claro como padrão (mesma janela)

Decisão de produto 2026-10-04: sem escolha salva, o tema é `light`
(antes seguia `prefers-color-scheme`). Escuro só com troca manual
(`localStorage`). Vale para as 12 páginas + `main.js`. A landing segue
navy fixo nos dois temas por decisão de design (`landing.css`).
