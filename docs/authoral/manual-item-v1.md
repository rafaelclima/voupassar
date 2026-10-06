# Manual do Item Autoral v1 — VouPassar (TASK 15.1)

> Fase 15 (decisão de produto 2026-10-06): expandir o banco com questões
> não oficiais derivadas do perfil observado nas 6 edições, sem fila de
> curadoria humana — o gate é o veredito do pipeline (validadores
> determinísticos + checksum). Autoral **consome** evidência oficial, nunca
> a produz: `content-map` e `evidence_json` do roteiro seguem 100% oficiais;
> simulado de edição real segue 100% oficial; textos-base de LP autorais são
> inéditos (redação própria), sem copiar IFRN ou terceiros. Produção textual
> fora do escopo.

Este manual define o contrato do **spec autoral** (arquivo JSON em
`data/authoral/specs/`), o molde por subassunto do lote piloto e o checklist
de distratores. O validador que aplica este contrato é
`scripts/authoral/validate_authoral.py` (`--check`, falha alta).

## 1. Schema do spec

Arquivo: `data/authoral/specs/<id>.json`, com `id` igual ao nome do arquivo
(sem extensão). JSON objeto com exatamente estas chaves:

| Chave | Tipo | Regra |
|---|---|---|
| `id` | string | igual ao nome do arquivo; prefixo `AUT-` |
| `discipline` | string | `LINGUA_PORTUGUESA` ou `MATEMATICA` |
| `topic` | string | código v1.1 válido para a disciplina (§2) |
| `subtopic` | string | código v1.1 válido dentro do topic (§2) |
| `skill` | string | vocabulário fechado da taxonomia (§3) |
| `reasoning_type` | string | vocabulário fechado da taxonomia (§3) |
| `difficulty_alvo` | string | `FACIL`, `MEDIA` ou `DIFICIL` (palpite editorial; será calibrada com dados de desempenho na Fase 4, como nas oficiais) |
| `referencias_oficiais` | array de string (≥1) | IDs no formato `AAAA-QN` (ex. `2024-Q28`); cada ID deve existir no dataset oficial (`data/extracted/AAAA.json`, número 1–40) **e pertencer ao mesmo subassunto** (pós-normalização v1.1) — é o vínculo auditável "molde de forma" |
| `texto_base` | string \| null | **LP: obrigatório** (texto inédito, redação própria, §5). **MAT: proibido** (deve ser `null` ou ausente; o contexto do problema vive no enunciado) |
| `enunciado` | string | ≥ 30 caracteres, com **comando explícito** (§4) |
| `options` | objeto `{A,B,C,D}` | 4 textos não-vazios, distintos entre si (normalizados) |
| `answerKey` | string | `A`, `B`, `C` ou `D` — **nunca `X`** (`X` = anulada oficial; autoral nasce válida) |
| `distractor_rationale` | array de 3 objetos | um por opção errada: `{"option": "<rótulo>", "rationale": "<texto ≥ 20 chars>"}`; os 3 rótulos devem ser exatamente as opções não-corretas |

## 2. Códigos v1.1 válidos (fonte: `V2__seed.sql` + `docs/content-map.md`)

Pares `topic ↔ subtopic` aceitos (qualquer outro par é rejeitado):

* `LINGUA_PORTUGUESA / GRAMATICA_NORMA`: `MORFOLOGIA`, `PONTUACAO`,
  `SEMANTICA_VOCABULARIO`, `COESAO_REFERENCIA`, `SINTAXE_PERIODO`,
  `SINTAXE_FUNCAO`, `NORMA_PADRAO`, `ACENTUACAO_GRAFICA`.
* `LINGUA_PORTUGUESA / INTERPRETACAO_TEXTUAL`: `INTENCAO_COMUNICATIVA`,
  `INFORMACAO_EXPLICITA`, `INFERENCIA`, `FUNCAO_ELEMENTO_PARAGRAFO`,
  `TIPO_TEXTUAL`, `GENERO_TEXTUAL`.
* `MATEMATICA / RAZAO_PROPORCAO`: `RAZAO`, `REGRA_DE_TRES`, `ESCALA`,
  `PROPORCIONALIDADE`.
* `MATEMATICA / ARITMETICA`: `OPERACOES`, `FRACOES`, `NOTACAO_CIENTIFICA`,
  `DIVISIBILIDADE_MMC_MDC`, `SISTEMAS_NUMERACAO`.
* `MATEMATICA / ALGEBRA`: `FUNCAO_AFIM`, `EQUACOES`, `SISTEMAS`, `SEQUENCIAS`.
* `MATEMATICA / GEOMETRIA`: `AREA_PLANA`, `PERIMETRO_COMPRIMENTO`,
  `POLIGONOS`, `PITAGORAS_DISTANCIA`, `VOLUME`, `UNIDADES_MEDIDA`.
* `MATEMATICA / ESTATISTICA_DADOS`: `MEDIA`, `LEITURA_GRAFICO_TABELA`,
  `PROBABILIDADE`.
* `MATEMATICA / PORCENTAGEM`: `CALCULO_DIRETO`, `VARIACAO`,
  `REPRESENTACAO_FRACAO`.
* `MATEMATICA / MATEMATICA_FINANCEIRA`: `JUROS_SIMPLES`.
* `MATEMATICA / GRANDEZAS_MEDIDAS`: `TEMPO`, `VOLUME_CAPACIDADE`.

Códigos da taxonomia v1 sem ocorrência observada (ex. `MEDIANA_MODA`,
`JUROS_COMPOSTOS`, `DESCONTO`) **não** são aceitos até migração futura —
nada inventado. Subassuntos fora do piloto da TASK 15.2 seguem a mesma
regra (§7 da TASK 15.2 em `TASKS.md`).

## 3. Vocabulários fechados (fonte: `docs/content-analysis/taxonomy.md`)

Habilidades (`skill`): `LOCALIZAR_INFORMACAO_EXPLICITA`,
`INFERIR_INFORMACAO`, `IDENTIFICAR_INTENCAO_COMUNICATIVA`,
`IDENTIFICAR_TIPO_GENERO`, `ANALISAR_FUNCAO_SINTATICA`,
`ANALISAR_ESTRUTURA_PERIODO`, `APLICAR_PONTUACAO`,
`IDENTIFICAR_REFERENTE_COESIVO`, `ANALISAR_CLASSE_MORFOLOGICA`,
`INFERIR_SENTIDO_VOCABULARIO`, `APLICAR_NORMA_PADRAO`, `CALCULAR_DIRETO`,
`MODELAR_SITUACAO_PROBLEMA`, `INTERPRETAR_GRAFICO_TABELA`,
`CONVERTER_UNIDADES`, `ESTIMAR_APROXIMAR`, `RACIOCINIO_ESPACIAL`.

Tipos de raciocínio (`reasoning_type`): `INTERPRETATIVO_LOCALIZACAO`,
`INTERPRETATIVO_INFERENCIAL`, `ANALITICO_GRAMATICAL`, `CALCULO_DIRETO`,
`MODELAGEM_MONOETAPA`, `MODELAGEM_MULTIETAPAS`, `VISUAL_ESPACIAL`,
`PROPORCIONAL`, `ESTIMATIVA`.

## 4. Enunciado com comando

Todo enunciado deve conter um comando explícito ao estudante. O validador
aceita se houver (case-insensitive) um dos verbos
`assinale | indique | identifique | calcule | determine | marque | escolha |
selecione | aponte`, **ou** se o enunciado terminar com `?`.

Recomendação de forma (espelha as oficiais sem copiá-las): fechar o
enunciado com "Assinale a opção correta." / "Assinale a opção que …".
Ex.: "…nas mesmas condições. Assinale a opção que indica …".

## 5. Textos-base de LP: inéditos, redação própria

* Todo spec de `LINGUA_PORTUGUESA` traz `texto_base` (≥ 50 caracteres) com
  texto corrido inventado para o item — microconto, cena escolar, aviso —
  **nunca** transcrição ou paráfrase de caderno oficial ou de terceiros.
* Vale para gramática e interpretação: em gramática o `texto_base` é o
  parágrafo/frase-suporte que o enunciado referencia.
* MAT usa `texto_base: null` (o problema é autocontido no enunciado;
  itens com figura estão fora do piloto — ver §7).

## 6. Strings proibidas (anti-falsa-oficialidade)

Em `texto_base`, `enunciado`, `options` e `distractor_rationale`, o
validador rejeita (case-insensitive):

* `IFRN`;
* `edital` (cobre `Edital nº …`);
* `prova 20XX` / `provas 20XX` (regex `\bprovas?\s+20\d{2}\b`).

Motivo: nenhuma autoral pode soar como enunciado oficial ("conforme o
Edital…", "na prova 2024…"). A palavra "prova" isolada não é proibida,
mas evite-a perto de anos.

## 7. Checksum e anti-duplicata

* Checksum = SHA-256 hex de `enunciado + alternativas A–D` normalizados com
  **a mesma normalização do importador oficial**
  (`scripts/db/import_questions.py`): Unicode NFC + colapso de whitespace.
* O validador exige checksum **distinto dos 240 oficiais** (recalculados de
  `data/extracted/*.json`) e **único dentro do lote** — colisão = falha alta.
* Regra de honestidade herdada: nunca editar um spec para "parecer outro"
  após colisão; reescrever o item.

## 8. Checklist de distratores (aplicar a todo item)

1. Cada distrator corresponde a um **erro cognitivo plausível** documentado
   em `distractor_rationale` (ex.: inverte direto/inverso; troca taxa fixa
   por variável; soma em vez de subtrair ao isolar; subtrai reais em vez de
   percentual; confunde referente próximo com referente real).
2. Nenhum distrator pode ser **absurdo eliminável sem ler** (ordem de
   grandeza errada, categoria gramatical impossível no contexto).
3. Nenhum distrator pode ser **defensável como correto** (ambiguidade =
   reescrever o item, nunca o rationale).
4. Alternativas com **paralelismo de forma**: mesma categoria de resposta
   (todas valores, todas frases completas, todas classes) e extensão
   semelhante — a correta não pode ser "a mais completa".
5. Gabarito conferido por **cálculo/leitura independente** antes de fechar o
   spec (para MAT, refazer a conta; para LP, apontar o trecho do
   `texto_base` que sustenta a resposta).

## 9. Moldes por subassunto (lote piloto)

Perfil observado = contagem em `docs/content-map.md §3`; referências = IDs
oficiais do mesmo subassunto usados como molde de forma.

### MAT — REGRA_DE_TRES (`RAZAO_PROPORCAO`, 10 ocorrências, série 0-1-1-3-3-2)

* Skill típica: `MODELAR_SITUACAO_PROBLEMA`; raciocínio `PROPORCIONAL`.
* Molde: situação de produção/consumo com 3 grandezas conhecidas +
  comando de 4ª quantidade, mantido o ritmo. Preferir composta (2 etapas)
  para alvo MEDIA; direta/inversa simples para FACIL.
* Referências do item-teste: `2024-Q28`, `2023-Q30`.

### MAT — FUNCAO_AFIM (`ALGEBRA`, 6 ocorrências, 1 por edição)

* Skill típica: `MODELAR_SITUACAO_PROBLEMA`; raciocínio `MODELAGEM_MONOETAPA`.
* Molde: tarifa com parte fixa + parte variável; comando = escrever `f(x)`.
  Distratores canônicos: omitir o fixo; trocar fixo↔variável; fundir os dois
  num coeficiente só.
* Referências do item-teste: `2023-Q27`, `2024-Q37`.

### MAT — EQUACOES (`ALGEBRA`, 8 ocorrências)

* Skill típica: `MODELAR_SITUACAO_PROBLEMA`; raciocínio `MODELAGEM_MONOETAPA`
  (ou `MODELAGEM_MULTIETAPAS` se exigir 2 isolamentos).
* Molde: relação linear descrita em linguagem corrente ("o triplo de um
  número diminuído de 9 resulta em 24") + comando pelo número.
  Distratores canônicos: opera só um lado; soma em vez de subtrair;
  ignora o coeficiente.
* Referências do item-teste: `2023-Q34`, `2025-Q30`.

### MAT — CALCULO_DIRETO (`PORCENTAGEM`, 6 ocorrências)

* Skill típica: `CALCULAR_DIRETO`; raciocínio `CALCULO_DIRETO` (ou
  `MODELAGEM_MULTIETAPAS` com 2 descontos encadeados).
* Molde: preço + desconto/percentual aplicado uma vez + comando pelo valor
  final. Distrator canônico obrigatório: subtrair o número do percentual
  como reais (20% → −R$ 20).
* Referências do item-teste: `2025-Q32`, `2026-Q28`.

### MAT — MEDIA (`ESTATISTICA_DADOS`, 6 ocorrências, 1 por edição)

* Skill típica: `CALCULAR_DIRETO` (sem gráfico) ou
  `INTERPRETAR_GRAFICO_TABELA` (com gráfico — fora do piloto);
  raciocínio `CALCULO_DIRETO`.
* Molde: lista curta de valores inteiros com soma múltipla da quantidade
  (média exata, sem dízima) + comando pela média. Distrator canônico:
  valor máximo/mínimo como resposta (confundir média com extremo).
* Referências do item-teste: `2025-Q26`, `2026-Q32`.

### LP — INFERENCIA (`INTERPRETACAO_TEXTUAL`, 20 ocorrências, maior subassunto)

* Skill: `INFERIR_INFORMACAO`; raciocínio `INTERPRETATIVO_INFERENCIAL`.
* Molde: microtexto narrativo com mudança de estado implícita (nada
  declarado em tese) + comando pela conclusão autorizada. Correta =
  paráfrase do não-dito com apoio em ≥ 2 pistas; erradas = fato isolado
  sem generalizar, extrapolação além do texto, contradição.
* Referências do item-teste: `2022-Q1`, `2020-Q10`.

### LP — INFORMACAO_EXPLICITA (`INTERPRETACAO_TEXTUAL`, 15 ocorrências)

* Skill: `LOCALIZAR_INFORMACAO_EXPLICITA`; raciocínio
  `INTERPRETATIVO_LOCALIZACAO`.
* Molde: microtexto informativo com dados enumeráveis (quantidades, ordem
  de eventos) + comando pelo dado literal ("Segundo o texto, …").
  Distratores: troca de um quantificador, inversão temporal, fato de outro
  trecho.
* Referências do item-teste: `2020-Q2`, `2020-Q3`.

### LP — PONTUACAO (`GRAMATICA_NORMA`, 10 ocorrências)

* Skill: `APLICAR_PONTUACAO`; raciocínio `ANALITICO_GRAMATICAL`.
* Molde: frase-suporte com uso modelar (aposto, adjunto antecipado,
  enumeração) + comando "mesmo motivo" sobre 4 frases candidatas, cada
  uma com função distinta da vírgula. Só uma compartilha a função.
* Referências do item-teste: `2020-Q7`, `2023-Q10`.

### LP — COESAO_REFERENCIA (`GRAMATICA_NORMA`, 8 ocorrências)

* Skill: `IDENTIFICAR_REFERENTE_COESIVO`; raciocínio `ANALITICO_GRAMATICAL`.
* Molde: parágrafo com pronome/retomada + 2 referentes plausíveis (o real
  e um "quase" — agente próximo, termo coletivo) + comando pelo referente.
* Referências do item-teste: `2020-Q12`, `2024-Q11`.

### LP — MORFOLOGIA (`GRAMATICA_NORMA`, 11 ocorrências)

* Skill: `ANALISAR_CLASSE_MORFOLOGICA`; raciocínio `ANALITICO_GRAMATICAL`.
* Molde: frase cotidiana com vocábulo de classe testável no contexto
  (advérbio vs. adjetivo é o contraste clássico) + comando pela classe.
  Distratores: classe do vizinho, classe do mesmo vocábulo em outro
  contexto, categoria inexistente no trecho.
* Referências do item-teste: `2020-Q4`, `2023-Q8`.

## 10. Itens-teste da 15.1 (10 specs verdes = gate da 15.2)

`data/authoral/specs/`: `AUT-RT-01` (REGRA_DE_TRES), `AUT-FA-02`
(FUNCAO_AFIM), `AUT-EQ-03` (EQUACOES), `AUT-PC-04` (CALCULO_DIRETO),
`AUT-MD-05` (MEDIA), `AUT-IN-06` (INFERENCIA), `AUT-IE-07`
(INFORMACAO_EXPLICITA), `AUT-PT-08` (PONTUACAO), `AUT-CR-09`
(COESAO_REFERENCIA), `AUT-MF-10` (MORFOLOGIA).

Critérios da TASK 15.1 (`TASKS.md`): 10/10 verdes no `--check`, 0 colisão
contra os 240 oficiais, e **parar para aprovação do responsável antes da
15.2**.

## 11. Suplemento 15.2 — moldes dos 3 subassuntos restantes do lote piloto

Os 10 itens-teste compõem o lote de 60 (mesmo contrato, já verdes); os 50
restantes completam a distribuição da TASK 15.2. Três subassuntos do lote
não tinham molde no §9; seguem os moldes, no mesmo formato.

### MAT — PROBABILIDADE (`ESTATISTICA_DADOS`, 7 ocorrências)

* Skill típica: `CALCULAR_DIRETO`; raciocínio `CALCULO_DIRETO`.
* Molde: universo finito com contagem explícita (sorteio, urna, grupo) +
  comando pela probabilidade do evento (fração simplificada ou percentual).
  Sem figura nem gráfico (fora do piloto). Distratores canônicos: usar o
  complementar; não simplificar; trocar parte pelo todo.
* Referências utilizáveis: `2023-Q22`, `2026-Q23`, `2025-Q23`.

### MAT — AREA_PLANA (`GEOMETRIA`, 8 ocorrências)

* Skill típica: `CALCULAR_DIRETO` ou `MODELAR_SITUACAO_PROBLEMA`; raciocínio
  `CALCULO_DIRETO` ou `MODELAGEM_MONOETAPA`. Sem figura: só polígonos
  descritos por medidas (retângulo, subtração de áreas).
* Molde: terreno/sala retangular com medidas inteiras + comando pela área
  (ou área restante após recorte retangular). Distratores canônicos:
  confundir área com perímetro; errar uma dimensão; somar em vez de subtrair
  o recorte.
* Referências utilizáveis: `2026-Q31`, `2025-Q27`, `2020-Q28`.

### LP — INTENCAO_COMUNICATIVA (`INTERPRETACAO_TEXTUAL`, 9 ocorrências)

* Skill: `IDENTIFICAR_INTENCAO_COMUNICATIVA` (9/9 nas oficiais); raciocínio
  `INTERPRETATIVO_INFERENCIAL`.
* Molde: microtexto de gênero marcado (aviso, campanha, convite) + comando
  "a intenção comunicativa predominante é". Correta = verbo de propósito
  sustentado pelo todo; erradas = efeito colateral, tema do texto
  confundido com propósito, intenção inexistente.
* Referências utilizáveis: `2026-Q3`, `2025-Q3`, `2020-Q1`.
