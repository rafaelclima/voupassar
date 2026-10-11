# Schema da prova-IA — TASK A.1 (DRAFT VPS)

> **Status: DRAFT — não é DONE.**
> Escopo VPS (sem testes): este documento + o prompt `ai-prova-v1`
> (`docs/ai/prompts/ai-prova-v1.md`). Validador Java + `mvn test` = DEV-only.
> DONE só na DEV após implementação + testes + validação (TASKS.md
> `Paralelo DEV × VPS` + `Regras de execução`).
>
> Branch: `draft/a1-vps-schema-prompt`. Nenhum Java/SQL/JS de produção
> neste draft.

## 0. Fontes (rastreabilidade, AGENTS.md §2/§4)

* Formatos e N por edição-modelo: `docs/provas-inventario.md §1`
  (IFRN 6 edições: 20 LP + 20 MAT + discursiva, 4h) e
  `docs/provas-inventario-eaj.md §1–§2` (EAJ-2021: 50Q 15/15/12/8, 3h;
  EAJ-2022/2025: 40Q 20/20, 3h; sem discursiva detectada).
* Vocabulário LP/MAT v1.1: `database/migrations/V2__seed.sql`
  (10 topics + 42 subtopics) + `docs/content-map.md §3` +
  `docs/content-analysis/taxonomy.md`.
* Vocabulário CN/CH (só EAJ-2021, só o observado):
  `database/migrations/V20__eaj_taxonomy_cn_ch.sql`
  (11 topics + 19 subtopics) + `docs/content-map-eaj.md §0–§3`.
* Proibições v1 (passagem, figura, explanation): TASKS.md Programa
  Prova-IA decisões 4–6 + decisão de produto 2026-10-04 (sem ensino passo
  a passo) + `docs/passagens-estrategia.md §2` (regra de vínculo por
  rótulo) + `docs/figuras-estrategia.md` + `docs/blockers.md`.
* Selo + isolamento: TASKS.md regras 1–3 do programa
  (`AI_GENERATED`, nunca oficial; nada-IA em `questions`/motor
  educacional). `source_type` vigente no banco:
  `docs/database-erd.md:238`
  (`OFFICIAL/AUTHORAL/ADAPTED/INTERNAL_REVIEW/EXPERIMENTAL`) — valor
  `AI_GENERATED` (ou equivalente) será criado na migração da TASK A.2
  (DEV); até lá, este schema usa o nome lógico `AI_GENERATED` sem presumir
  DDL.

O que não está nas fontes acima está marcado DESCONHECIDO/NÃO CONFIRMADO.
Nada aqui promete "gabarito confiável" (TASKS.md decisão 4).

## 1. Envelope (contrato entre provedor e backend)

```json
{
  "format": "IFRN",
  "editionModel": "IFRN-2026",
  "questions": [
    {
      "n": 1,
      "discipline": "LINGUA_PORTUGUESA",
      "topic": "INTERPRETACAO_TEXTUAL",
      "subtopic": "INFERENCIA",
      "stem": "…enunciado auto-contido…",
      "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
      "answer": "B"
    }
  ]
}
```

Regras do envelope:

* `format`: `IFRN` | `EAJ`. Obrigatório.
* `editionModel`: código da edição-modelo que dirige a estrutura
  (§2). Obrigatório. Formato *inspirado em*, nunca *extraído de* — nunca
  citar edição real como origem da questão (regra 1 do programa).
* `questions`: array ordenado por `n`, sem lacuna, sem repetição (§3).
* Por questão: `n` (1-based), `discipline` (§3), `topic` + `subtopic`
  opcional (§4), `stem` não-vazio auto-contido (§6), `options` com
  exatamente as chaves `A,B,C,D` não-vazias e distintas (§7/DEV),
  `answer` ∈ `{A,B,C,D}`.
* **Proibidos no schema v1:** `explanation`, `passage`/`texto_base`,
  `figure`/`image`/`figureUrl`, `markdown` no corpo, qualquer campo fora
  desta seção. Se o provedor enviar, o backend descarta sem usar e o
  validador reprova o lote se houver marcador no texto (§6) — sem correção
  silenciosa.

## 2. Edições-modelo vigentes (estrutura dirige N e disciplinas)

| `editionModel` | Base em | Objetivas | Disciplinas por posição |
|---|---|---|---|
| `IFRN-2020` | `docs/provas-inventario.md §2` | 40 | 1–20 `LINGUA_PORTUGUESA`, 21–40 `MATEMATICA` |
| `IFRN-2022` | idem | 40 | idem |
| `IFRN-2023` | idem | 40 | idem |
| `IFRN-2024` | idem | 40 | idem |
| `IFRN-2025` | idem | 40 | idem |
| `IFRN-2026` | idem | 40 | idem |
| `EAJ-2021` | `docs/provas-inventario-eaj.md §2` (50Q 15/15/12/8, 3h) | 50 | 1–15 `LINGUA_PORTUGUESA`, 16–30 `MATEMATICA`, 31–42 `CIENCIAS_NATUREZA`, 43–50 `CIENCIAS_HUMANAS` |
| `EAJ-2022` | idem (40Q 20/20, 3h) | 40 | 1–20 `LINGUA_PORTUGUESA`, 21–40 `MATEMATICA` |
| `EAJ-2025` | idem (40Q 20/20, 3h) | 40 | idem |

Ausências (nunca inventar): `IFRN-2021` AUSENTE
(`docs/provas-inventario.md §0`); `EAJ-2023/2024/2026` AUSENTES
(`docs/provas-inventario-eaj.md §0`). Pedido com `editionModel`
inexistente → `404 EDITION_NOT_FOUND` (contrato da TASK A.3, DEV).
Discursiva/produção textual NÃO faz parte do caderno-IA v1 (só objetivas).
Duração oficial (IFRN 4h, EAJ 3h) é referência de ritmo da UI, não campo
do schema.

## 3. N exato e disciplinas (fail-high)

* N deve ser EXATO ao modelo: 40 (`IFRN-*`, `EAJ-2022`, `EAJ-2025`) ou 50
  (`EAJ-2021`). N a menor/maior → lote reprovado, sem caderno parcial
  (TASKS.md regra 5).
* `n` cobre `1..N` sem lacuna e sem repetição.
* `discipline` restrita às do modelo: `EAJ-2021` admite as 4 áreas;
  demais modelos, SÓ `LINGUA_PORTUGUESA`/`MATEMATICA`. Disciplina fora do
  modelo → lote reprovado.
* Códigos de disciplina vigentes: `LINGUA_PORTUGUESA`, `MATEMATICA`
  (V2) + `CIENCIAS_NATUREZA`, `CIENCIAS_HUMANAS` (V18/V20, só EAJ-2021).
  Nenhum outro código é válido.

## 4. Vocabulário de `topic`/`subtopic` (restrito ou DESCONHECIDO)

`topic` é obrigatório; `subtopic` é opcional (EAJ-2025 Q40 mostra que
`GEOMETRIA/OUTRO` existe no observado sem subcódigo — V20 §3: `NULL`
justificado, sem linha nova). Valores fora do vocabulário abaixo são
inválidos, EXCETO o literal `DESCONHECIDO` (quando a LLM não consegue
mapear — nunca código inventado).

LP/MAT v1.1 (V2 + `docs/content-map.md §3`):

* `GRAMATICA_NORMA`: `MORFOLOGIA`, `PONTUACAO`, `SEMANTICA_VOCABULARIO`,
  `COESAO_REFERENCIA`, `SINTAXE_PERIODO`, `SINTAXE_FUNCAO`,
  `NORMA_PADRAO`, `ACENTUACAO_GRAFICA`.
* `INTERPRETACAO_TEXTUAL`: `INTENCAO_COMUNICATIVA`,
  `INFORMACAO_EXPLICITA`, `INFERENCIA`,
  `FUNCAO_ELEMENTO_PARAGRAFO`, `TIPO_TEXTUAL`, `GENERO_TEXTUAL`.
* `RAZAO_PROPORCAO`: `RAZAO`, `REGRA_DE_TRES`, `ESCALA`,
  `PROPORCIONALIDADE`.
* `ARITMETICA`: `OPERACOES`, `FRACOES`, `NOTACAO_CIENTIFICA`,
  `DIVISIBILIDADE_MMC_MDC`, `SISTEMAS_NUMERACAO`.
* `ALGEBRA`: `FUNCAO_AFIM`, `EQUACOES`, `SISTEMAS`, `SEQUENCIAS`.
* `GEOMETRIA`: `AREA_PLANA`, `PERIMETRO_COMPRIMENTO`, `POLIGONOS`,
  `PITAGORAS_DISTANCIA`, `VOLUME`, `UNIDADES_MEDIDA`.
* `ESTATISTICA_DADOS`: `MEDIA`, `LEITURA_GRAFICO_TABELA`,
  `PROBABILIDADE`. (Demais subcódigos da taxonomia v1 sem ocorrência —
  `MEDIANA_MODA`, `CONTAGEM_COMBINATORIA` — NÃO são válidos até migração
  futura; ver V2 + `taxonomy.md §3`.)
* `PORCENTAGEM`: `CALCULO_DIRETO`, `VARIACAO`, `REPRESENTACAO_FRACAO`.
* `MATEMATICA_FINANCEIRA`: `JUROS_SIMPLES`. (`JUROS_COMPOSTOS`,
  `DESCONTO` sem ocorrência — inválidos por ora.)
* `GRANDEZAS_MEDIDAS`: `TEMPO`, `VOLUME_CAPACIDADE`.
  (`MASSA_COMPRIMENTO` sem ocorrência — inválido por ora.)

CN/CH — SÓ `EAJ-2021`, SÓ o observado (V20 + `docs/content-map-eaj.md`):

* `ECOLOGIA`: `NIVEL_TROFICO`, `NUTRICAO_SERES_VIVOS`.
* `NUTRICAO_SAUDE`: `ALIMENTOS_PROCESSADOS`.
* `AGROPECUARIA`: `PRODUCAO_ALIMENTOS`.
* `BIOLOGIA_CELULAR`: `RESPIRACAO_CELULAR`.
* `QUIMICA_GERAL`: `FENOMENOS_QUIMICOS`, `POLARIDADE_MISTURAS`,
  `RADIOATIVIDADE`.
* `FISICA_GERAL`: `ENERGIA_CONSERVACAO`, `ESCALAS_TERMOMETRICAS`,
  `LEIS_NEWTON`.
* `HISTORIA_BRASIL`: `MIGRACOES_REPUBLICA`, `ECONOMIA_COLONIAL`,
  `COLONIZACAO_CONTATO`.
* `CULTURA_SOCIEDADE`: `CULTURA_AFRO_BRASILEIRA`.
* `CARTOGRAFIA`: `ELEMENTOS_MAPA`.
* `GEOGRAFIA_BRASIL`: `BIOMAS_IMPACTOS`.
* `GEOPOLITICA`: `BLOCOS_ECONOMICOS`, `ORDEM_MUNDIAL`.

`topic` CN/CH fora de `EAJ-2021` → lote reprovado.

## 5. Proibições v1 (prompt proíbe, validador recusa)

1. **Passagem/texto-base compartilhado** (`passagens-estrategia.md`):
   questões-IA são auto-contidas. Sem `Texto N`, `trecho`, `textos`,
   cabeçalho de bloco ou dependência de leitura externa.
2. **Figura** (rota de texto não gera imagens; nenhuma ilustrativa/
   inventada será servida): sem `figura`, `imagem`, `gráfico`, `tabela`,
   `observe`, `conforme`, `abaixo`, `acima`, `charge`, `tirinha`, `mapa`,
   `foto`, `ilustração`. Questão que cite figura é rejeitada.
3. **Explanation** (decisão 2026-10-04: a plataforma testa, não ensina
   passo a passo): sem `explanation`, `explicação`, `resolução`,
   `passo a passo`, `comentário`. Se vier, o backend descarta e o front
   nunca renderiza.
4. Conteúdo ofensivo/identificador/PII: proibido no prompt; sem dado do
   aluno no corpo (só formato/disciplina/tópicos — TASKS.md regra 4).

## 6. Enunciados auto-contidos + regex de recusa (draft para o validador DEV)

Todo `stem` + `options` deve ser resolvível sem texto/figura externa.
Draft de marcadores (case-insensitive) que reprovam a posição — lista
inicial derivada de `blockers.md` + `passagens-estrategia.md §2`; o DEV
fixa a regex final com testes:

```
texto\s*\d|textos|trecho|passagem|figura|imagem|gr[áa]fico|tabela|
quadro|charge|tirinha|mapa|foto|ilustra|observe|conforme|acima|abaixo|
a\s*seguir|explanation|explica[çc][aã]o|resolu[çc][aã]o|passo\s*a\s*passo|coment[áa]rio
```

Casos da bateria B.2 que este draft já prevê (fixtures DEV-only):
"observe a imagem", "gráfico abaixo", "tabela", "Texto 1", "trecho".

## 7. Validador estrutural (DEV-only — resumo do contrato para a DEV)

Implementação na TASK A.1-DEV (testável sem rede). Reprovação →
`422 AI_GENERATION_INVALID` com motivo por posição (nunca correção
silenciosa):

* JSON não-parseável ou com markdown;
* N ≠ N do modelo; `n` com lacuna/repetição/fora de `1..N`;
* `options` sem exatamente `A–D`, com vazia ou duplicada;
* `answer` ∉ `{A,B,C,D}`;
* marcador de figura/passagem (§6) em `stem`/`options`;
* campo `explanation` presente;
* `discipline`/`topic`/`subtopic` fora do vocabulário do modelo
  (exceto `DESCONHECIDO`).

## 8. Selo, origem e isolamento (não-negociáveis)

* Origem lógica: `AI_GENERATED` (distinto de
  `OFFICIAL/AUTORAL/ADAPTED` — TASKS.md regra 1; DDL na A.2). Nunca citar
  edição real como origem da questão.
* API e UI carregam selo obrigatório: "Questão gerada por IA — não é
  questão oficial IFRN/EAJ" por questão + "placar sobre gabarito gerado
  por IA — sem validade de prova real" no resultado.
* Isolamento (TASKS.md regras 2–3): nada-IA entra em `questions`,
  `question_options`, `question_sources`, mapa de conteúdo, diagnóstico,
  roteiro, revisão, performance ou `GET /questions`. Caderno efêmero em
  estrutura própria (A.2); gabarito congelado imutável.
* Auditoria da geração (prompt_version `ai-prova-v1`, modelo, timestamp,
  tentativas) sem PII — campos na A.2/C.2.

## 9. Exemplo de envelope (excerto — N real é 40/50, §3)

> Excerto ilustrativo de FORMA (2 de 40). Conteúdo sintético, NÃO é
> questão oficial.

```json
{
  "format": "IFRN",
  "editionModel": "IFRN-2026",
  "questions": [
    {
      "n": 1,
      "discipline": "LINGUA_PORTUGUESA",
      "topic": "INTERPRETACAO_TEXTUAL",
      "subtopic": "INFERENCIA",
      "stem": "EXEMPLO SINTÉTICO: …enunciado completo e auto-contido…",
      "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
      "answer": "B"
    },
    {
      "n": 2,
      "discipline": "MATEMATICA",
      "topic": "PORCENTAGEM",
      "subtopic": "CALCULO_DIRETO",
      "stem": "EXEMPLO SINTÉTICO: …enunciado completo e auto-contido…",
      "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
      "answer": "C"
    }
  ]
}
```

Exemplo completo de 1 questão few-shot no prompt:
`docs/ai/prompts/ai-prova-v1.md`.

## 10. Fora deste schema (não prometer aqui)

* Modelo default, timeout, limites da rota free, segredos: TASK C.1
  (`docs/ai-provedor.md`, DEV). Este schema não fixa modelo nem timeout.
* Cota (3/dia), concorrência, `429/409`: TASK C.2 (DEV).
* Endpoint, mock, selo em resposta, `GET` execução/submit/result:
  TASK A.3 (DEV). `institution` inválida → `400`; modelo inexistente →
  `404` (sem inventar estrutura).
* Tabela do caderno efêmero, `frozen_answer_key`, auditoria: TASK A.2
  (migração após V22 vigente).
* Correção pedagógica do gabarito: limitação assumida (decisão 4) —
  documentar em `docs/blockers.md` na B.2; o validador cobre SÓ formato.

## 11. Pendências DEV (não bloqueiam este draft)

* [ ] Validador Java + `mvn -f backend/pom.xml test` verde (critérios A.1).
* [ ] Fixar caminho do prompt (candidatos no TASKS.md A.1) sem quebrar
  este doc.
* [ ] `source_type` `AI_GENERATED` criado na migração A.2.
* [ ] Bateria B.2 executada contra o validador.
