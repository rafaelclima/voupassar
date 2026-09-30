# Sumário da classificação — TASK 1.4

> 240/240 questões classificadas (6 edições × 40). Validação:
> `python3 scripts/analysis/validate_classification.py` → OK nas 6 edições.
> Revisão humana: PENDENTE.

## 1. Por edição × assunto

| Ed. | INT_TEXT | GRAM | PORC | RAZ_PROP | ARIT | ALG | GEO | EST_DAT | MAT_FIN | GR_MED | OUTRO |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 2020 | 11 | 9 | 2 | 4 | 5 | 4 | 2 | 3 | 0 | 0 | 0 |
| 2022 | 9 | 11 | 1 | 5 | 5 | 1 | 4 | 3 | 1 | 0 | 0 |
| 2023 | 8 | 12 | 2 | 2 | 4 | 4 | 3 | 3 | 1 | 1 | 0 |
| 2024 | 9 | 11 | 1 | 5 | 3 | 3 | 3 | 3 | 1 | 1 | 0 |
| 2025 | 12 | 8 | 4 | 6 | 1 | 2 | 3 | 3 | 1 | 0 | 0 |
| 2026 | 11 | 9 | 2 | 3 | 3 | 4 | 3 | 2 | 1 | 1 | 1 |
| **Total** | **60** | **60** | **12** | **25** | **21** | **18** | **18** | **17** | **5** | **3** | **1** |

Leituras (indicativas, não conclusivas — classificação com revisão pendente):
* LP divide-se ~50/50 entre interpretação e gramática em TODAS as edições.
* Matemática concentra-se em razão/proporção, aritmética, álgebra,
  geometria e leitura de dados; matemática financeira e grandezas são
  residuais (8/120).
* Dificuldade estimada: FACIL 108, MEDIA 121, DIFICIL 11 — palpite, não medida.

## 2. Confiança do assunto

ALTA 170 (70,8%) · MEDIA 46 (19,2%) · BAIXA 24 (10%).
Toda dificuldade: confiança BAIXA (sem dados de desempenho).

## 3. Fila de revisão humana (38 itens)

**Figuras/gráficos/charges ilegíveis no texto (35):**
2020 Q16–Q20 (Texto 2 imagem), Q35, Q36 (Gráfico 1) · 2022 Q15–Q19
(charge Texto 3), Q31 (gráfico Texto 2), Q37, Q39 (figuras) · 2023 Q23,
Q24 (Figura 1), Q25 (Figura 2), Q28, Q29 (Gráfico 1) · 2024 Q18, Q31,
Q32, Q33 (gráfico Texto 3) · 2025 Q15, Q16 (Texto 2), Q26 (gráfico),
Q27 (figura), Q33, Q36 (frações) · 2026 Q18–Q20 (Texto 2), Q30
(Figura 1), Q39 (frações).

**Casos conceituais (3):**
* 2020 Q26 (anulada) — percentuais encadeados; verificar se a anulação
  decorre de defeito de formulação.
* 2024 Q17 (anulada) — tonicidade/acentuação; possível defeito (coerente
  com a anulação, mas a causa oficial é DESCONHECIDA).
* 2023 Q40 — MMC(2,3,4)=12 sugere 2032, ausente nas opções; gabarito
  oficial A (2044). NÃO reinterpretar sem o PDF renderizado.

Motivo integral de cada item em
`per-edition/<ed>.json → classificacoes[].observacao`.
