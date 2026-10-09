# Mapa do Conteúdo Cobrado (EAJ) — TASK D.2

> Agregação das 130 classificações da TASK D.1
> (`docs/content-analysis/per-edition/eaj-{2021,2022,2025}.json`), gerada por
> `scripts/analysis/build_content_map.py --institution EAJ` (determinístico;
> agregados máquina-legíveis em
> `docs/content-analysis/content-map-eaj.json`).
> "Tendência" é DESCRITIVA (apenas 3 edições, sem teste estatístico;
> oscilações de 1–2 questões são ruído — mesma regra do mapa IFRN, ver
> regra no script). Confiança BAIXA = assunto NÃO CONFIRMADO.
>
> Escopo: processo EAJ/UFRN (banca Comperve). O mapa IFRN
> (`docs/content-map.md`, 240 questões) NÃO foi reescrito — a comparação
> entre processos vive na §4 abaixo e é só descritiva (LP/MAT).

Reproduzir:

```bash
python3 scripts/analysis/build_content_map.py --institution EAJ \
  --json-out docs/content-analysis/content-map-eaj.json
```

## 0. Sem normalização nesta agregação (JSONs D.1 já normalizados)

| Item | Situação | Motivo |
|---|---|---|
| LP/MAT (110) | usados como classificados | já estão na taxonomia v1.1 nos JSONs D.1 |
| CN/CH (20, só EAJ-2021) | 11 códigos novos D.1, só o observado | ECOLOGIA, NUTRICAO_SAUDE, AGROPECUARIA, BIOLOGIA_CELULAR, QUIMICA_GERAL, FISICA_GERAL, HISTORIA_BRASIL, CULTURA_SOCIEDADE, CARTOGRAFIA, GEOGRAFIA_BRASIL, GEOPOLITICA, cada um com 1+ evidência |
| EAJ-2025 Q40 | GEOMETRIA / OUTRO mantido | ângulos (supl./compl./replem.) sem subcódigo v1.1; sub OUTRO justificado na observação |
| EAJ-2025 Q23 (anulada) | ARITMETICA / FRACOES, conta no mapa | NULA declarada no `.md`; conta como conteúdo que apareceu (coluna "Anul.") |
| EAJ-2025 Q22/Q39 | contam no mapa + coluna "Nota visual" | `NECESSITA_REVISAO` com trava D.3: classificadas aqui, só entram no banco após conferência visual |

Séries: SOMENTE as 3 edições existentes (2021–2022–2025). Não há EAJ
2023/2024/2026 — nada foi interpolado, nenhum zero inventado. Zeros em
2022/2025 para assuntos CN/CH são zeros REAIS: essas edições têm estrutura
própria de 40Q em 2 áreas (LP 01–20, MAT 21–40), sem seção CN/CH — a
estrutura de cada edição vale só para ela (AGENTS.md §10).

## 1. Por assunto (130 questões; % do total; A/M/B = confiança ALTA/MEDIA/BAIXA)

| Assunto | n | % total | Edições | Tendência | Conf (A/M/B) | Anul. | Nota visual |
|---|---|---|---|---|---|---|---|
| INTERPRETACAO_TEXTUAL | 28 | 21.5% | EAJ-2021,EAJ-2022,EAJ-2025 | RECORRENTE_OSCILANTE | 16/12/0 | 0 | 0 |
| GRAMATICA_NORMA | 27 | 20.8% | EAJ-2021,EAJ-2022,EAJ-2025 | CRESCENTE | 23/4/0 | 0 | 0 |
| GEOMETRIA | 15 | 11.5% | EAJ-2021,EAJ-2022,EAJ-2025 | RECORRENTE_OSCILANTE | 10/5/0 | 0 | 0 |
| ARITMETICA | 11 | 8.5% | EAJ-2021,EAJ-2022,EAJ-2025 | BAIXA_RECORRENCIA | 9/2/0 | 1 | 0 |
| RAZAO_PROPORCAO | 10 | 7.7% | EAJ-2021,EAJ-2022,EAJ-2025 | BAIXA_RECORRENCIA | 8/1/1 | 0 | 1 |
| PORCENTAGEM | 8 | 6.2% | EAJ-2021,EAJ-2022,EAJ-2025 | BAIXA_RECORRENCIA | 8/0/0 | 0 | 1 |
| ALGEBRA | 7 | 5.4% | EAJ-2021,EAJ-2022,EAJ-2025 | BAIXA_RECORRENCIA | 6/1/0 | 0 | 0 |
| ESTATISTICA_DADOS | 4 | 3.1% | EAJ-2021,EAJ-2022,EAJ-2025 | BAIXA_RECORRENCIA | 2/2/0 | 0 | 0 |
| FISICA_GERAL | 3 | 2.3% | EAJ-2021 | ESPORADICO | 3/0/0 | 0 | 0 |
| HISTORIA_BRASIL | 3 | 2.3% | EAJ-2021 | ESPORADICO | 2/1/0 | 0 | 0 |
| QUIMICA_GERAL | 3 | 2.3% | EAJ-2021 | ESPORADICO | 2/1/0 | 0 | 0 |
| ECOLOGIA | 2 | 1.5% | EAJ-2021 | ESPORADICO | 2/0/0 | 0 | 0 |
| GEOPOLITICA | 2 | 1.5% | EAJ-2021 | ESPORADICO | 2/0/0 | 0 | 0 |
| NUTRICAO_SAUDE | 2 | 1.5% | EAJ-2021 | ESPORADICO | 1/1/0 | 0 | 0 |
| AGROPECUARIA | 1 | 0.8% | EAJ-2021 | ESPORADICO | 0/1/0 | 0 | 0 |
| BIOLOGIA_CELULAR | 1 | 0.8% | EAJ-2021 | ESPORADICO | 1/0/0 | 0 | 0 |
| CARTOGRAFIA | 1 | 0.8% | EAJ-2021 | ESPORADICO | 1/0/0 | 0 | 0 |
| CULTURA_SOCIEDADE | 1 | 0.8% | EAJ-2021 | ESPORADICO | 1/0/0 | 0 | 0 |
| GEOGRAFIA_BRASIL | 1 | 0.8% | EAJ-2021 | ESPORADICO | 0/1/0 | 0 | 0 |

## 2. Leitura por área (% dentro da área; séries 2021-2022-2025)

**Língua Portuguesa (55: 15-20-20):** INTERPRETACAO_TEXTUAL 50,9% (28) ·
GRAMATICA_NORMA 49,1% (27). Divisão meio a meio nas 3 edições — mesmo
padrão estável do IFRN (§4), leitura descritiva.

**Matemática (55: 15-20-20):** GEOMETRIA 27,3% (15) · ARITMETICA 20,0%
(11) · RAZAO_PROPORCAO 18,2% (10) · PORCENTAGEM 14,5% (8) · ALGEBRA 12,7%
(7) · ESTATISTICA_DADOS 7,3% (4). MATEMATICA_FINANCEIRA e
GRANDEZAS_MEDIDAS: 0 no observado (3 edições — ausência observada, nunca
"conteúdo que não cai").

**Ciências da Natureza (12: 12-0-0, só EAJ-2021):** QUIMICA_GERAL 25,0%
(3) · FISICA_GERAL 25,0% (3) · ECOLOGIA 16,7% (2) · NUTRICAO_SAUDE 16,7%
(2) · AGROPECUARIA 8,3% (1) · BIOLOGIA_CELULAR 8,3% (1).

**Ciências Humanas (8: 8-0-0, só EAJ-2021):** HISTORIA_BRASIL 37,5% (3) ·
GEOPOLITICA 25,0% (2) · CULTURA_SOCIEDADE 12,5% (1) · CARTOGRAFIA 12,5%
(1) · GEOGRAFIA_BRASIL 12,5% (1).

CN/CH existem SÓ na EAJ-2021 (estrutura daquela edição); 2022/2025 não
têm seção CN/CH. Qualquer share CN/CH "no EAJ como um todo" mistura
estruturas diferentes — usar sempre com o filtro da edição.

## 3. Subassuntos (série 2021-2022-2025)

| Assunto | Subassunto | n | Série 21-22-25 |
|---|---|---|---|
| INTERPRETACAO_TEXTUAL | INFERENCIA | 12 | 4-4-4 |
| INTERPRETACAO_TEXTUAL | INTENCAO_COMUNICATIVA | 6 | 0-4-2 |
| INTERPRETACAO_TEXTUAL | GENERO_TEXTUAL | 5 | 2-2-1 |
| INTERPRETACAO_TEXTUAL | TIPO_TEXTUAL | 3 | 2-0-1 |
| INTERPRETACAO_TEXTUAL | INFORMACAO_EXPLICITA | 2 | 0-1-1 |
| GRAMATICA_NORMA | SEMANTICA_VOCABULARIO | 8 | 4-2-2 |
| GRAMATICA_NORMA | MORFOLOGIA | 5 | 1-3-1 |
| GRAMATICA_NORMA | COESAO_REFERENCIA | 4 | 0-1-3 |
| GRAMATICA_NORMA | ACENTUACAO_GRAFICA | 3 | 1-1-1 |
| GRAMATICA_NORMA | SINTAXE_FUNCAO | 3 | 1-0-2 |
| GRAMATICA_NORMA | PONTUACAO | 2 | 0-1-1 |
| GRAMATICA_NORMA | NORMA_PADRAO | 1 | 0-1-0 |
| GRAMATICA_NORMA | SINTAXE_PERIODO | 1 | 0-0-1 |
| GEOMETRIA | AREA_PLANA | 6 | 1-5-0 |
| GEOMETRIA | PITAGORAS_DISTANCIA | 3 | 2-1-0 |
| GEOMETRIA | VOLUME | 3 | 0-1-2 |
| GEOMETRIA | POLIGONOS | 1 | 0-1-0 |
| GEOMETRIA | PERIMETRO_COMPRIMENTO | 1 | 0-1-0 |
| GEOMETRIA | OUTRO | 1 | 0-0-1 |
| ARITMETICA | OPERACOES | 6 | 1-1-4 |
| ARITMETICA | FRACOES | 4 | 2-1-1 |
| ARITMETICA | NOTACAO_CIENTIFICA | 1 | 0-1-0 |
| RAZAO_PROPORCAO | PROPORCIONALIDADE | 5 | 3-1-1 |
| RAZAO_PROPORCAO | REGRA_DE_TRES | 3 | 1-1-1 |
| RAZAO_PROPORCAO | RAZAO | 2 | 0-0-2 |
| PORCENTAGEM | CALCULO_DIRETO | 5 | 3-0-2 |
| PORCENTAGEM | REPRESENTACAO_FRACAO | 2 | 0-1-1 |
| PORCENTAGEM | VARIACAO | 1 | 0-0-1 |
| ALGEBRA | EQUACOES | 5 | 1-2-2 |
| ALGEBRA | SISTEMAS | 1 | 0-1-0 |
| ALGEBRA | FUNCAO_AFIM | 1 | 0-1-0 |
| ESTATISTICA_DADOS | MEDIA | 4 | 1-1-2 |
| FISICA_GERAL | ENERGIA_CONSERVACAO | 1 | 1-0-0 |
| FISICA_GERAL | ESCALAS_TERMOMETRICAS | 1 | 1-0-0 |
| FISICA_GERAL | LEIS_NEWTON | 1 | 1-0-0 |
| HISTORIA_BRASIL | MIGRACOES_REPUBLICA | 1 | 1-0-0 |
| HISTORIA_BRASIL | ECONOMIA_COLONIAL | 1 | 1-0-0 |
| HISTORIA_BRASIL | COLONIZACAO_CONTATO | 1 | 1-0-0 |
| QUIMICA_GERAL | FENOMENOS_QUIMICOS | 1 | 1-0-0 |
| QUIMICA_GERAL | POLARIDADE_MISTURAS | 1 | 1-0-0 |
| QUIMICA_GERAL | RADIOATIVIDADE | 1 | 1-0-0 |
| ECOLOGIA | NIVEL_TROFICO | 1 | 1-0-0 |
| ECOLOGIA | NUTRICAO_SERES_VIVOS | 1 | 1-0-0 |
| GEOPOLITICA | BLOCOS_ECONOMICOS | 1 | 1-0-0 |
| GEOPOLITICA | ORDEM_MUNDIAL | 1 | 1-0-0 |
| NUTRICAO_SAUDE | ALIMENTOS_PROCESSADOS | 2 | 2-0-0 |
| AGROPECUARIA | PRODUCAO_ALIMENTOS | 1 | 1-0-0 |
| BIOLOGIA_CELULAR | RESPIRACAO_CELULAR | 1 | 1-0-0 |
| CARTOGRAFIA | ELEMENTOS_MAPA | 1 | 1-0-0 |
| CULTURA_SOCIEDADE | CULTURA_AFRO_BRASILEIRA | 1 | 1-0-0 |
| GEOGRAFIA_BRASIL | BIOMAS_IMPACTOS | 1 | 1-0-0 |

Destaques (descritivos, 3 edições): INFERENCIA é o subassunto isolado mais
frequente (12, 4-4-4 — o único perfeitamente estável do dataset EAJ);
SEMANTICA_VOCABULARIO lidera a gramática (8); AREA_PLANA concentra a
geometria (6, com pico 5 em 2022); MEDIA é o único subassunto de
estatística observado (4); FUNCAO_AFIM — onipresente no IFRN (1×/edição)
— aparece 1× em 130 no EAJ.

## 4. Comparabilidade IFRN × EAJ (só descritiva; só LP/MAT)

> CN/CH fora desta seção: o dataset IFRN não tem CN/CH. Qualquer
> comparação EAJ×IFRN em CN/CH seria contra vazio — NÃO FAZER.
> Bases: IFRN 240 (6 edições) vs EAJ 130 (3 edições). Shares com
> denominadores e coberturas diferentes; nada aqui é teste estatístico,
> causa ou previsão.

| Recorte | IFRN (observado) | EAJ (observado) | Leitura descritiva |
|---|---|---|---|
| LP: Interpretação × Gramática | 50,0% × 50,0% (60/60 de 120) | 50,9% × 49,1% (28/27 de 55) | divisão meio a meio nos dois processos |
| MAT: assunto líder | RAZAO_PROPORCAO 20,8% (25/120) | GEOMETRIA 27,3% (15/55) | liderança diferente no observado |
| MAT: geometria | 15,0% (18/120) | 27,3% (15/55) | share maior no EAJ observado |
| MAT: MAT_FINANCEIRA + GRAND_MEDIDAS | 6,7% (8/120) | 0% (0/55) | ausentes no EAJ observado (3 edições) |
| MAT: ESTATISTICA_DADOS | 14,2% (17/120) | 7,3% (4/55, só MEDIA) | share menor e menos diverso no EAJ observado |
| LP: INFERENCIA (top sub) | 20 em 240 (8,3%) | 12 em 130 (9,2%), 4-4-4 | top sub nos dois; estável no EAJ |

## 5. Confiança e limites (ler antes de usar para recomendações — E.2)

* Confiança do assunto: ALTA 74,6% (97), MEDIA 24,6% (32), BAIXA 0,8%
  (1: EAJ-2025 Q39, também em NECESSITA_REVISAO). Confiança BAIXA =
  assunto NÃO CONFIRMADO.
* 2 itens (1,5%) em NECESSITA_REVISAO — EAJ-2025 Q22 e Q39, trava D.3
  (conferência visual pendente; contam no mapa como conteúdo, nunca como
  dado pronto para o banco).
* Questão anulada EAJ-2025 Q23 conta aqui como conteúdo que apareceu na
  prova (coluna "Anul."); a regra de pontuação de anuladas é
  DESCONHECIDA.
* Dificuldade estimada NÃO entra neste mapa (confiança BAIXA global, sem
  dados de desempenho — D.1) — será calibrada com desempenho real.
* Tendências com 3 pontos são frágeis por construção ("CRESCENTE" de
  GRAMATICA_NORMA = 7-9-11, descritivo puro). Não interpolar edições
  inexistentes; não projetar a próxima edição.
* Respostas transcritas NÃO CONFIRMADAS (ex. EAJ-2022 40/40 A) são
  proveniência de gabarito (`data/linked/eaj/`), fora do escopo deste
  mapa — aqui só a classificação do conteúdo.
* Este mapa alimenta a D.3 (importação) e a E.2 (roteiro da trilha EAJ):
  usar sempre com os filtros de confiança (`assunto_confianca`,
  `status`), nunca como verdade absoluta. Frequência EAJ nunca contamina
  o perfil IFRN e vice-versa.
