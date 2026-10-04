# Relatório de Cobertura — TASK 11.2

> Data: 2026-10-02. Fontes: PostgreSQL local (`questions=240`, `question_classifications=240`
> v1.1 `PENDING`) + `docs/content-analysis/content-map.json` (agregação v1.1) +
> `docs/content-analysis/per-edition/*.json` (classificação v1) + `docs/auditoria-questoes.md`
> (TASK 11.1) + `docs/content-map.md` (TASK 1.5). "Confirmada" abaixo significa
> **revisão/aprovação humana** — não confundir com vinculação ao gabarito (essa é 100%).
> Nada inventado: lacunas marcadas como `PENDENTE` / `DESCONHECIDO`.

## 0. Resumo executivo

| Indicador | Valor |
|---|---|
| Questões no banco | 240 (6 edições × 40) |
| Por disciplina | LP 120 (50%) · MAT 120 (50%); 20+20 em **todas** as edições |
| Assuntos distintos (banco v1.1) | 10 (2 LP + 8 MAT) |
| Subassuntos distintos | 42 |
| Vinculadas ao gabarito | 240/240 (100%); anuladas 5 |
| Classificações com revisão humana `APPROVED` | **0/240 confirmadas** — 200 `APPROVED` nesta data são pré-curadoria LLM (referendo humano pendente); 39 `REVIEWED`; 1 `REJECTED` |
| Classificações `NECESSITA_REVISAO` na origem | 38/240 (15,8%) |
| Confiança do assunto | ALTA 170 (70,8%) · MEDIA 46 (19,2%) · BAIXA 24 (10%) |
| Publicáveis | 0 — 240 `PENDENTE_REVISAO` (curadoria final TASK 12.2 pendente; pré-curadoria aplicada 2026-10-02) |
| Edição 2021 | ausente do dataset (documentado; séries pulam 2021, nada interpolado) |

## 1. Por edição

| Edição | Total | LP | MAT | Anuladas | Confirmadas (A–D) | Confiança A/M/B |
|---|---|---|---|---|---|---|
| 2020 | 40 | 20 | 20 | 2 (Q7, Q26) | 38 | 31/2/7 |
| 2022 | 40 | 20 | 20 | 0 | 40 | 26/7/7 |
| 2023 | 40 | 20 | 20 | 1 (Q21) | 39 | 28/7/5 |
| 2024 | 40 | 20 | 20 | 1 (Q17) | 39 | 27/12/1 |
| 2025 | 40 | 20 | 20 | 0 | 40 | 26/12/2 |
| 2026 | 40 | 20 | 20 | 1 (Q37) | 39 | 32/6/2 |
| **Total** | **240** | **120** | **120** | **5** | **235** | **170/46/24** |

## 2. Por disciplina

**Língua Portuguesa (120):** `INTERPRETACAO_TEXTUAL` 60 (50%) · `GRAMATICA_NORMA` 60 (50%).
Divisão meio a meio em todas as 6 edições — padrão mais estável do dataset.

**Matemática (120):** `RAZAO_PROPORCAO` 25 (20,8%) · `ARITMETICA` 22 (18,3%) ·
`ALGEBRA` 18 (15,0%) · `GEOMETRIA` 18 (15,0%) · `ESTATISTICA_DADOS` 17 (14,2%) ·
`PORCENTAGEM` 12 (10,0%) · `MATEMATICA_FINANCEIRA` 5 (4,2%) · `GRANDEZAS_MEDIDAS` 3 (2,5%).

## 3. Por assunto (banco v1.1; % do total de 240)

| Assunto | n | % | Edições presentes | Conf A/M/B | Anul. | Em revisão* |
|---|---|---|---|---|---|---|
| GRAMATICA_NORMA | 60 | 25,0% | 6/6 | 48/9/3 | 2 | 3 |
| INTERPRETACAO_TEXTUAL | 60 | 25,0% | 6/6 | 35/17/8 | 0 | 14 |
| RAZAO_PROPORCAO | 25 | 10,4% | 6/6 | 18/6/1 | 1 | 1 |
| ARITMETICA | 22 | 9,2% | 6/6 | 18/3/1 | 1 | 1 |
| ALGEBRA | 18 | 7,5% | 6/6 | 14/4/0 | 0 | 0 |
| GEOMETRIA | 18 | 7,5% | 6/6 | 10/3/5 | 0 | 7 |
| ESTATISTICA_DADOS | 17 | 7,1% | 6/6 | 10/2/5 | 0 | 9 |
| PORCENTAGEM | 12 | 5,0% | 6/6 | 10/1/1 | 1 | 3 |
| MATEMATICA_FINANCEIRA | 5 | 2,1% | 5/6 (ausente 2020) | 5/0/0 | 0 | 0 |
| GRANDEZAS_MEDIDAS | 3 | 1,2% | 3/6 (2023, 2024, 2026) | 2/1/0 | 0 | 0 |

\* "Em revisão" = contagem do `content-map.json` v1.1 (itens `NECESSITA_REVISAO` por
assunto; total 38). Anuladas contam como conteúdo que apareceu na prova; regra de
pontuação de anuladas: DESCONHECIDA.

### 3.1 Matriz edição × assunto (banco v1.1)

| Ed. | INT_TEXT | GRAM | PORC | RAZ_PROP | ARIT | ALG | GEO | EST_DAT | MAT_FIN | GR_MED |
|---|---|---|---|---|---|---|---|---|---|---|
| 2020 | 11 | 9 | 2 | 4 | 5 | 4 | 2 | 3 | 0 | 0 |
| 2022 | 9 | 11 | 1 | 5 | 5 | 1 | 4 | 3 | 1 | 0 |
| 2023 | 8 | 12 | 2 | 2 | 4 | 4 | 3 | 3 | 1 | 1 |
| 2024 | 9 | 11 | 1 | 5 | 3 | 3 | 3 | 3 | 1 | 1 |
| 2025 | 12 | 8 | 4 | 6 | 1 | 2 | 3 | 3 | 1 | 0 |
| 2026 | 11 | 9 | 2 | 3 | 4 | 4 | 3 | 2 | 1 | 1 |

Nota de versão: `summary.md` (v1) mostra 2026 ARIT=3 + OUTRO=1 (Q21 numeração romana);
o banco e o mapa v1.1 aplicam o override `2026 Q21 → ARITMETICA/SISTEMAS_NUMERACAO`
(ARIT=4, OUTRO=0) e `2022 Q12, 2023 Q12, 2024 Q17 → GRAMATICA_NORMA/ACENTUACAO_GRAFICA`
(totais de GRAM inalterados). `content-map.json → v11_overrides` registra as 4 mudanças.

## 4. Por subassunto (banco v1.1; 42 linhas)

| Assunto | Subassunto | n |
|---|---|---|
| GRAMATICA_NORMA | MORFOLOGIA | 11 |
| GRAMATICA_NORMA | PONTUACAO | 10 |
| GRAMATICA_NORMA | SEMANTICA_VOCABULARIO | 10 |
| GRAMATICA_NORMA | COESAO_REFERENCIA | 8 |
| GRAMATICA_NORMA | SINTAXE_PERIODO | 7 |
| GRAMATICA_NORMA | SINTAXE_FUNCAO | 6 |
| GRAMATICA_NORMA | NORMA_PADRAO | 5 |
| GRAMATICA_NORMA | ACENTUACAO_GRAFICA | 3 |
| INTERPRETACAO_TEXTUAL | INFERENCIA | 20 |
| INTERPRETACAO_TEXTUAL | INFORMACAO_EXPLICITA | 15 |
| INTERPRETACAO_TEXTUAL | INTENCAO_COMUNICATIVA | 9 |
| INTERPRETACAO_TEXTUAL | GENERO_TEXTUAL | 7 |
| INTERPRETACAO_TEXTUAL | TIPO_TEXTUAL | 5 |
| INTERPRETACAO_TEXTUAL | FUNCAO_ELEMENTO_PARAGRAFO | 4 |
| RAZAO_PROPORCAO | REGRA_DE_TRES | 10 |
| RAZAO_PROPORCAO | RAZAO | 7 |
| RAZAO_PROPORCAO | PROPORCIONALIDADE | 5 |
| RAZAO_PROPORCAO | ESCALA | 3 |
| ARITMETICA | DIVISIBILIDADE_MMC_MDC | 7 |
| ARITMETICA | OPERACOES | 6 |
| ARITMETICA | FRACOES | 5 |
| ARITMETICA | NOTACAO_CIENTIFICA | 3 |
| ARITMETICA | SISTEMAS_NUMERACAO | 1 |
| ALGEBRA | EQUACOES | 8 |
| ALGEBRA | FUNCAO_AFIM | 6 |
| ALGEBRA | SISTEMAS | 2 |
| ALGEBRA | SEQUENCIAS | 2 |
| GEOMETRIA | AREA_PLANA | 8 |
| GEOMETRIA | POLIGONOS | 4 |
| GEOMETRIA | PITAGORAS_DISTANCIA | 3 |
| GEOMETRIA | UNIDADES_MEDIDA | 1 |
| GEOMETRIA | PERIMETRO_COMPRIMENTO | 1 |
| GEOMETRIA | VOLUME | 1 |
| ESTATISTICA_DADOS | PROBABILIDADE | 7 |
| ESTATISTICA_DADOS | MEDIA | 6 |
| ESTATISTICA_DADOS | LEITURA_GRAFICO_TABELA | 4 |
| PORCENTAGEM | CALCULO_DIRETO | 6 |
| PORCENTAGEM | REPRESENTACAO_FRACAO | 3 |
| PORCENTAGEM | VARIACAO | 3 |
| MATEMATICA_FINANCEIRA | JUROS_SIMPLES | 5 |
| GRANDEZAS_MEDIDAS | TEMPO | 2 |
| GRANDEZAS_MEDIDAS | VOLUME_CAPACIDADE | 1 |

Destaques descritivos (não conclusivos): `FUNCAO_AFIM` e `MEDIA` 1× por edição (6/6);
`INFERENCIA` é o subassunto isolado mais frequente (20); `JUROS_SIMPLES` é o único
conteúdo de matemática financeira observado. Séries por edição em `content-map.md §3`.

## 5. Percentual de classificação confirmada

| Nível | Valor | O que significa |
|---|---|---|
| Vinculação ao gabarito | 240/240 (100%) | toda questão tem resposta oficial transcrita |
| Classificação com `status=APPROVED` no banco | 200/240 (83,3%) — pré-curadoria LLM 2026-10-02; 39 REVIEWED; 1 REJECTED; **0 confirmadas por humano** |
| Classificação `PENDING` | 0/240 | pré-curadoria esvaziou a fila |
| `NECESSITA_REVISAO` na origem | 38/240 (15,8%) | 35 figuras/gráficos/charges + 3 conceituais (`summary.md §3`) |
| Confiança ALTA do assunto | 170/240 (70,8%) | usável com filtro; não é aprovação |
| Dificuldade estimada | FACIL 108 · MEDIA 121 · DIFICIL 11, **todas confiança BAIXA** | palpite sem dados de desempenho; fora do mapa |
| Explicações redigidas | 0/240 em 2026-10-02; coluna removida em 2026-10-04 (V11, decisão de produto) | `explanation` existia e estava NULL em tudo — nada inventado |
| Questões `PUBLICAVEL` | 0/240 | 240 `PENDENTE_REVISAO` — publicação bloqueada até curadoria |

## 6. Pendências (herdadas, não bloqueiam a 11.2)

1. **Curadoria humana final** das 39 classificações em `REVIEWED` e do
   referendo das 200 em `APPROVED` (pré-curadoria LLM de 2026-10-02 — ver
   `docs/curadoria.md`). As 36 questões `has_figure` e os itens conceituais
   seguem exigindo o caderno em mãos.
2. Conferência visual amostral das grades e das 36 questões `has_figure` no PDF
   renderizado (ex. 2020 Q38 frações achatadas).
3. Casos conceituais: 2020 Q26 e 2024 Q17 (anuladas, causa oficial DESCONHECIDA);
   2023 Q40 (MMC sugere 2032, gabarito oficial A=2044 — classificação marcada
   `REJECTED` em 2026-10-02, não reinterpretar sem o PDF).
4. Decisão de curadoria: nome do gabarito 2025 (`…2024…` no nome, conteúdo 2025).
5. Cobertura fina: `MATEMATICA_FINANCEIRA` (5, ausente 2020) e `GRANDEZAS_MEDIDAS`
   (3, só 2023/2024/2026) são esparsos — fato do dataset, não falha de importação.
6. Passo a passo textual fora do produto desde 2026-10-04 (coluna removida
   na V11) — publicação `PUBLICAVEL` depende só do status de curadoria.

## 7. Veredito

Cobertura **completa em extensão** (240/240 questões, 6 edições, 20+20 por edição,
10 assuntos / 42 subassuntos presentes no banco e no mapa v1.1) e **pendente em
confirmação humana** (200/240 `APPROVED` por pré-curadoria LLM ainda sem
referendo, 39 `REVIEWED`, 1 `REJECTED`, 15,8% `NECESSITA_REVISAO` na origem,
0% publicável). Nenhum buraco de importação: os assuntos esparsos e as
ausências (2021, MAT_FIN 2020, GR_MED em 3 edições) são características do
dataset, não perda de dados. A TASK 12.2 entregou a curadoria assistida com
decisão item a item auditável (`docs/curadoria.md`); falta o referendo humano
com o PDF. (Nota 2026-10-04: redação de passo a passo removida do escopo —
V11.)
