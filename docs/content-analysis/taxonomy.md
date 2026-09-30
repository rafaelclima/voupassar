# Taxonomia v1 — assuntos observados nas provas (TASK 1.4)

> Os tópicos abaixo EMERGIRAM da leitura das 240 questões, não de lista
> externa. Códigos congelados: os JSONs em `per-edition/` os referenciam.
> Lacunas detectadas durante a anotação estão em §3 com proposta de
> código v1.1 (a aplicar na TASK 1.5, sem reescrever os JSONs v1).

## 1. Língua Portuguesa (120 questões: 60/60)

### INTERPRETACAO_TEXTUAL (60)
| Subassunto | O que cobre |
|---|---|
| INTENCAO_COMUNICATIVA | propósito/ideia dominante do texto |
| INFORMACAO_EXPLICITA | localizar o que está dito |
| INFERENCIA | concluir o não-dito a partir de pistas |
| FUNCAO_ELEMENTO_PARAGRAFO | papel de parágrafo/trecho na arquitetura do texto |
| TIPO_TEXTUAL | narração/descrição/dissertacao etc. |
| GENERO_TEXTUAL | artigo, charge, relatório etc. |

### GRAMATICA_NORMA (60)
| Subassunto | O que cobre |
|---|---|
| SINTAXE_FUNCAO | funções sintáticas dos termos |
| SINTAXE_PERIODO | estrutura do período, orações |
| PONTUACAO | vírgula, dois-pontos, aspas, parênteses |
| COESAO_REFERENCIA | pronomes, retomadas, referentes |
| MORFOLOGIA | classes de palavras |
| SEMANTICA_VOCABULARIO | sentido de vocábulos, substituição sem prejuízo |
| NORMA_PADRAO | concordância, regência, reescrita padrão |

## 2. Matemática (120 questões)

| Assunto (n) | Subassuntos |
|---|---|
| RAZAO_PROPORCAO (25) | RAZAO, REGRA_DE_TRES, ESCALA, PROPORCIONALIDADE |
| ARITMETICA (21) | OPERACOES, FRACOES, NOTACAO_CIENTIFICA, DIVISIBILIDADE_MMC_MDC |
| ALGEBRA (18) | FUNCAO_AFIM, EQUACOES, SISTEMAS, SEQUENCIAS |
| GEOMETRIA (18) | AREA_PLANA, PERIMETRO_COMPRIMENTO, POLIGONOS, PITAGORAS_DISTANCIA, VOLUME, UNIDADES_MEDIDA |
| ESTATISTICA_DADOS (17) | MEDIA, MEDIANA_MODA, LEITURA_GRAFICO_TABELA, CONTAGEM_COMBINATORIA, PROBABILIDADE |
| PORCENTAGEM (12) | CALCULO_DIRETO, VARIACAO, REPRESENTACAO_FRACAO |
| MATEMATICA_FINANCEIRA (5) | JUROS_SIMPLES, JUROS_COMPOSTOS, DESCONTO |
| GRANDEZAS_MEDIDAS (3) | TEMPO, VOLUME_CAPACIDADE, MASSA_COMPRIMENTO |
| OUTRO (1) | Q21/2026, ver §3 |

Habilidades: LOCALIZAR_INFORMACAO_EXPLICITA, INFERIR_INFORMACAO,
IDENTIFICAR_INTENCAO_COMUNICATIVA, IDENTIFICAR_TIPO_GENERO,
ANALISAR_FUNCAO_SINTATICA, ANALISAR_ESTRUTURA_PERIODO, APLICAR_PONTUACAO,
IDENTIFICAR_REFERENTE_COESIVO, ANALISAR_CLASSE_MORFOLOGICA,
INFERIR_SENTIDO_VOCABULARIO, APLICAR_NORMA_PADRAO, CALCULAR_DIRETO,
MODELAR_SITUACAO_PROBLEMA, INTERPRETAR_GRAFICO_TABELA, CONVERTER_UNIDADES,
ESTIMAR_APROXIMAR, RACIOCINIO_ESPACIAL.

Tipos de raciocínio: INTERPRETATIVO_LOCALIZACAO,
INTERPRETATIVO_INFERENCIAL, ANALITICO_GRAMATICAL, CALCULO_DIRETO,
MODELAGEM_MONOETAPA, MODELAGEM_MULTIETAPAS, VISUAL_ESPACIAL, PROPORCIONAL,
ESTIMATIVA.

## 3. Lacunas da v1 (códigos propostos v1.1, aprovar na TASK 1.5)

| # | Lacuna | Evidência | Proposta |
|---|---|---|---|
| G1 | Acentuação gráfica sem código próprio (forçado em NORMA_PADRAO, conf. MEDIA) | 2022 Q12, 2023 Q12, 2024 Q17 | `GRAMATICA_NORMA / ACENTUACAO_GRAFICA` |
| G2 | Numeração romana / sistemas de numeração (único OUTRO em 240) | 2026 Q21 (conf. BAIXA, status OK) | `ARITMETICA / SISTEMAS_NUMERACAO` |
| G3 | Variação/registro linguístico sem código (mapeado p/ NORMA_PADRAO) | 2022 Q16 | avaliar `GRAMATICA_NORMA / VARIACAO_LINGUISTICA` |
| G4 | Comparação intertextual sem habilidade própria | 2026 Q20 | avaliar habilidade `COMPARAR_TEXTOS` |

## 4. Regras de confiança (aplicadas)

* Disciplina: ALTA se `discipline_source=HEADER` (extração), MEDIA se
  `INFERRED_RANGE`.
* Assunto: ALTA = questão típica e inequívoca; MEDIA = sobreposição entre
  códigos ou mapeamento esticado (documentado); BAIXA = figura essencial
  ilegível ou conteúdo fora da lista.
* Dificuldade: sempre BAIXA (`ESTIMATIVA_ESPECIALISTA_SEM_DADOS`);
  FACIL/MEDIA/DIFICIL por heurística de nº de passos + potencial de erro.
