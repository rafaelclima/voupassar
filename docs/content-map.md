# Mapa do Conteúdo Cobrado — TASK 1.5

> Agregação das 240 classificações da TASK 1.4
> (`docs/content-analysis/per-edition/*.json`), gerada por
> `scripts/analysis/build_content_map.py` (determinístico; agregados
> máquina-legíveis em `docs/content-analysis/content-map.json`).
> "Tendência" é DESCRITIVA (6 edições, sem teste estatístico; oscilações de
> 1–2 questões são ruído — ver regra no script). Confiança BAIXA = assunto
> NÃO CONFIRMADO; sem carimbo humano desde 2026-10-06
> (`docs/plano-remocao-curadoria.md`).

## 0. Normalização v1.1 (só nesta agregação; JSONs per-edition congelados)

| Item | v1 (anotado) | v1.1 (mapa) | Motivo |
|---|---|---|---|
| 2022 Q12, 2023 Q12, 2024 Q17 | GRAMATICA_NORMA / NORMA_PADRAO ou OUTRO | GRAMATICA_NORMA / ACENTUACAO_GRAFICA | acentuação gráfica sem código v1 (lacuna G1) |
| 2026 Q21 | OUTRO / OUTRO | ARITMETICA / SISTEMAS_NUMERACAO | numeração romana, único OUTRO em 240 (lacuna G2) |

## 1. Por assunto (240 questões; % do total; A/M/B = confiança ALTA/MEDIA/BAIXA)

| Assunto | n | % total | Edições | Tendência | Conf (A/M/B) | Anul. | Nota visual |
|---|---|---|---|---|---|---|---|
| GRAMATICA_NORMA | 60 | 25.0% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 48/9/3 | 2 | 3 |
| INTERPRETACAO_TEXTUAL | 60 | 25.0% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 35/17/8 | 0 | 14 |
| RAZAO_PROPORCAO | 25 | 10.4% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 18/6/1 | 1 | 1 |
| ARITMETICA | 22 | 9.2% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 18/3/1 | 1 | 1 |
| ALGEBRA | 18 | 7.5% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 14/4/0 | 0 | 0 |
| GEOMETRIA | 18 | 7.5% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 10/3/5 | 0 | 7 |
| ESTATISTICA_DADOS | 17 | 7.1% | 2020,2022,2023,2024,2025,2026 | ESTAVEL | 10/2/5 | 0 | 9 |
| PORCENTAGEM | 12 | 5.0% | 2020,2022,2023,2024,2025,2026 | RECORRENTE_OSCILANTE | 10/1/1 | 1 | 3 |
| MATEMATICA_FINANCEIRA | 5 | 2.1% | 2022,2023,2024,2025,2026 | BAIXA_RECORRENCIA | 5/0/0 | 0 | 0 |
| GRANDEZAS_MEDIDAS | 3 | 1.2% | 2023,2024,2026 | ESPORADICO | 2/1/0 | 0 | 0 |

## 2. Leitura por disciplina (% dentro da disciplina, 120 + 120)

**Língua Portuguesa:** INTERPRETACAO_TEXTUAL 50% (60) · GRAMATICA_NORMA 50%
(60). Divisão meio a meio em todas as 6 edições — o padrão mais estável do
dataset.

**Matemática:** RAZAO_PROPORCAO 20,8% (25) · ARITMETICA 18,3% (22) ·
ALGEBRA 15,0% (18) · GEOMETRIA 15,0% (18) · ESTATISTICA_DADOS 14,2% (17) ·
PORCENTAGEM 10,0% (12) · MATEMATICA_FINANCEIRA 4,2% (5) ·
GRANDEZAS_MEDIDAS 2,5% (3).

## 3. Subassuntos (série 2020-2022-2023-2024-2025-2026)

| Assunto | Subassunto | n | Série 20-22-23-24-25-26 |
|---|---|---|---|
| GRAMATICA_NORMA | MORFOLOGIA | 11 | 2-1-2-3-2-1 |
| GRAMATICA_NORMA | PONTUACAO | 10 | 2-1-2-2-1-2 |
| GRAMATICA_NORMA | SEMANTICA_VOCABULARIO | 10 | 2-3-2-0-2-1 |
| GRAMATICA_NORMA | COESAO_REFERENCIA | 8 | 1-1-1-3-1-1 |
| GRAMATICA_NORMA | SINTAXE_PERIODO | 7 | 1-1-1-1-1-2 |
| GRAMATICA_NORMA | SINTAXE_FUNCAO | 6 | 1-1-2-0-1-1 |
| GRAMATICA_NORMA | NORMA_PADRAO | 5 | 0-2-1-1-0-1 |
| GRAMATICA_NORMA | ACENTUACAO_GRAFICA | 3 | 0-1-1-1-0-0 |
| INTERPRETACAO_TEXTUAL | INFERENCIA | 20 | 4-3-1-2-6-4 |
| INTERPRETACAO_TEXTUAL | INFORMACAO_EXPLICITA | 15 | 4-2-2-2-2-3 |
| INTERPRETACAO_TEXTUAL | INTENCAO_COMUNICATIVA | 9 | 1-2-2-2-1-1 |
| INTERPRETACAO_TEXTUAL | GENERO_TEXTUAL | 7 | 1-1-1-2-1-1 |
| INTERPRETACAO_TEXTUAL | TIPO_TEXTUAL | 5 | 1-1-1-0-1-1 |
| INTERPRETACAO_TEXTUAL | FUNCAO_ELEMENTO_PARAGRAFO | 4 | 0-0-1-1-1-1 |
| RAZAO_PROPORCAO | REGRA_DE_TRES | 10 | 0-1-1-3-3-2 |
| RAZAO_PROPORCAO | RAZAO | 7 | 2-3-0-1-1-0 |
| RAZAO_PROPORCAO | PROPORCIONALIDADE | 5 | 1-0-1-0-2-1 |
| RAZAO_PROPORCAO | ESCALA | 3 | 1-1-0-1-0-0 |
| ARITMETICA | DIVISIBILIDADE_MMC_MDC | 7 | 2-2-1-1-0-1 |
| ARITMETICA | OPERACOES | 6 | 1-1-1-1-0-2 |
| ARITMETICA | FRACOES | 5 | 1-1-1-1-1-0 |
| ARITMETICA | NOTACAO_CIENTIFICA | 3 | 1-1-1-0-0-0 |
| ARITMETICA | SISTEMAS_NUMERACAO | 1 | 0-0-0-0-0-1 |
| ALGEBRA | EQUACOES | 8 | 3-0-2-0-1-2 |
| ALGEBRA | FUNCAO_AFIM | 6 | 1-1-1-1-1-1 |
| ALGEBRA | SEQUENCIAS | 2 | 0-0-1-1-0-0 |
| ALGEBRA | SISTEMAS | 2 | 0-0-0-1-0-1 |
| GEOMETRIA | AREA_PLANA | 8 | 2-1-1-1-2-1 |
| GEOMETRIA | POLIGONOS | 4 | 0-2-1-0-0-1 |
| GEOMETRIA | PITAGORAS_DISTANCIA | 3 | 0-0-0-1-1-1 |
| GEOMETRIA | VOLUME | 1 | 0-1-0-0-0-0 |
| GEOMETRIA | PERIMETRO_COMPRIMENTO | 1 | 0-0-1-0-0-0 |
| GEOMETRIA | UNIDADES_MEDIDA | 1 | 0-0-0-1-0-0 |
| ESTATISTICA_DADOS | PROBABILIDADE | 7 | 1-1-1-1-2-1 |
| ESTATISTICA_DADOS | MEDIA | 6 | 1-1-1-1-1-1 |
| ESTATISTICA_DADOS | LEITURA_GRAFICO_TABELA | 4 | 1-1-1-1-0-0 |
| PORCENTAGEM | CALCULO_DIRETO | 6 | 1-0-1-0-2-2 |
| PORCENTAGEM | REPRESENTACAO_FRACAO | 3 | 1-0-1-0-1-0 |
| PORCENTAGEM | VARIACAO | 3 | 0-1-0-1-1-0 |
| MATEMATICA_FINANCEIRA | JUROS_SIMPLES | 5 | 0-1-1-1-1-1 |
| GRANDEZAS_MEDIDAS | TEMPO | 2 | 0-0-1-1-0-0 |
| GRANDEZAS_MEDIDAS | VOLUME_CAPACIDADE | 1 | 0-0-0-0-0-1 |

Destaques (descritivos): FUNCAO_AFIM e MEDIA aparecem exatamente 1× por
edição (6/6); INFERENCIA é o subassunto isolado mais frequente (20);
JUROS_SIMPLES é o único conteúdo de matemática financeira observado.

## 4. Confiança e limites (ler antes de usar para recomendações)

* Confiança ALTA em 70,8% dos itens; 38 itens (15,8%) em NECESSITA_REVISAO,
  quase todos por figura/gráfico ausente do texto — ver
  `docs/content-analysis/summary.md §3`.
* Questões anuladas (5) contam aqui como conteúdo que apareceu na prova;
  assinaladas na coluna "Anul." — a regra de pontuação de anuladas é
  DESCONHECIDA (pendência TASK 1.3).
* Dificuldade estimada NÃO entra neste mapa (sempre confiança BAIXA, sem
  dados de desempenho) — será calibrada na Fase 4.
* Ausência da edição 2021 no dataset: séries pulam 2021; nada foi
  interpolado.
* Este mapa alimenta a TASK 2.3 (importação) e a Fase 4 (recomendação):
  usar sempre com os filtros de confiança (`assunto_confianca`,
  `status`), nunca como verdade absoluta.
