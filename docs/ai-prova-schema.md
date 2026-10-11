# Prova-IA — schema do caderno gerado (`ai-prova-v1`)

> TASK A.1 (Programa Prova-IA, `TASKS.md`) — DONE na DEV.
> Reconciliação 2026-10-11: merge do draft VPS
> (`draft/a1-vps-schema-prompt`) com a implementação DEV. Conflitos de
> contrato resolvidos pela DEV, dona do validador (regra do paralelo
> DEV × VPS). Decisões DEV vs draft: **sem `subtopic` na v1** (o contrato
> do TASKS.md A.1 e a tabela da A.2 não preveem o campo — lote com
> `subtopic` é reprovado como campo fora do contrato); **9 modelos**
> (todas as edições presentes — incorporado do draft); **regex final com
> `quadro/tirinha/foto`, sem `mapa`/`textos` sem número** (justificativa
> no §6); **prompt canônico único** em
> `backend/src/main/resources/ai/prompts/ai-prova-v1.txt` (o draft em
> `docs/ai/prompts/` foi incorporado e removido para não haver duas
> fontes da verdade).
>
> O "sem garantia" da decisão 4 vale para a **correção pedagógica do
> gabarito**, nunca para o formato — a validação estrutural abaixo é
> obrigatória e fail-high (`422 AI_GENERATION_INVALID`).

## 0. Fontes (rastreabilidade, AGENTS.md §2/§4)

* Formatos e N por edição-modelo: `docs/provas-inventario.md §1`
  (IFRN 6 edições: 20 LP + 20 MAT + discursiva, 4h) e
  `docs/provas-inventario-eaj.md §1–§2` (EAJ-2021: 50Q 15/15/12/8, 3h;
  EAJ-2022/2025: 40Q 20/20, 3h; sem discursiva detectada).
* Vocabulário LP/MAT v1.1: `database/migrations/V2__seed.sql:22-34`
  (10 topics) + `docs/content-map.md §3`.
* Vocabulário CN/CH (só EAJ-2021, só o observado):
  `database/migrations/V20__eaj_taxonomy_cn_ch.sql:38-81`
  (11 topics) + `docs/content-map-eaj.md`.
* Proibições v1 (passagem, figura, explanation): TASKS.md decisões 4–6 +
  decisão de produto 2026-10-04 + `docs/passagens-estrategia.md §2` +
  `docs/figuras-estrategia.md`.
* Selo + isolamento: TASKS.md regras 1–3 do programa. `source_type`
  vigente: `docs/database-erd.md:238`
  (`OFFICIAL/AUTHORAL/ADAPTED/INTERNAL_REVIEW/EXPERIMENTAL`) — valor
  `AI_GENERATED` (ou equivalente) criado na migração da TASK A.2 (DEV).

O que não está nas fontes acima está marcado DESCONHECIDO/NÃO CONFIRMADO.
Nada aqui promete "gabarito confiável" (decisão 4).

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
      "stem": "…enunciado auto-contido…",
      "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
      "answer": "B"
    }
  ]
}
```

| Campo | Tipo | Regra |
|---|---|---|
| `format` | string | Exatamente `IFRN` ou `EAJ`. Deve ser o prefixo de `editionModel`. |
| `editionModel` | string | Um dos 9 modelos do §2 (só edições presentes — nunca inventar). |
| `questions` | array | Tamanho **exato** do modelo + ordem `n` crescente (sem redução silenciosa). |
| `n` | int | Posições `1..N` sem lacuna nem duplicata. |
| `discipline` | string | Só as da edição-modelo, na contagem exata do §2. |
| `topic` | string | Vocabulário do §4 **ou** `DESCONHECIDO` — nunca código inventado. |
| `stem` | string | Enunciado auto-contido, não-vazio (§6). |
| `options` | objeto | Exatamente `A,B,C,D`, não-vazias e **distintas** (trim + collapse de espaços). |
| `answer` | string | Exatamente `A`, `B`, `C` ou `D`. |

**Proibidos na v1** (presença reprova o lote — nunca correção silenciosa):
`explanation`, `subtopic`, `passage`, `texto_base`/`textBase`,
`figure`/`figureUrl`/`image`, `markdown`, qualquer outro campo fora desta
tabela. Decisão DEV: `subtopic` não existe no contrato do TASKS.md A.1
nem na tabela da A.2 (`ai_generation_questions` tem `topic`, sem
subtópico) — o prompt proíbe explicitamente o campo e o validador o
rejeita como fora do contrato.

## 2. Edições-modelo vigentes (estrutura dirige N e disciplinas)

| `editionModel` | Objetivas | Mapa canônico de posições |
|---|---|---|
| `IFRN-2020` | 40 | 1–20 `LINGUA_PORTUGUESA`, 21–40 `MATEMATICA` |
| `IFRN-2022` | 40 | idem |
| `IFRN-2023` | 40 | idem |
| `IFRN-2024` | 40 | idem |
| `IFRN-2025` | 40 | idem |
| `IFRN-2026` | 40 | idem |
| `EAJ-2021` | 50 | 1–15 `LINGUA_PORTUGUESA`, 16–30 `MATEMATICA`, 31–42 `CIENCIAS_NATUREZA`, 43–50 `CIENCIAS_HUMANAS` |
| `EAJ-2022` | 40 | 1–20 `LINGUA_PORTUGUESA`, 21–40 `MATEMATICA` |
| `EAJ-2025` | 40 | idem |

Ausências (nunca inventar): `IFRN-2021` AUSENTE; `EAJ-2020/2023/2024/2026`
AUSENTES. Pedido com `editionModel` inexistente → `404 EDITION_NOT_FOUND`
(contrato da TASK A.3, DEV). Discursiva/produção textual NÃO faz parte do
caderno-IA v1 (só objetivas). Duração oficial (IFRN 4h, EAJ 3h) é
referência de ritmo da UI, não campo do schema.

O validador exige as **contagens** por disciplina, não a ordem (o mapa
acima é a ordem canônica recomendada ao provedor via prompt).

## 3. N exato e disciplinas (fail-high)

* N EXATO do modelo: 40 (`IFRN-*`, `EAJ-2022`, `EAJ-2025`) ou 50
  (`EAJ-2021`). N a menor/maior → lote reprovado, sem caderno parcial
  (TASKS.md regra 5).
* `n` cobre `1..N` sem lacuna e sem repetição.
* `discipline` restrita às do modelo: `EAJ-2021` admite as 4 áreas;
  demais modelos, SÓ `LINGUA_PORTUGUESA`/`MATEMATICA`.
* Códigos vigentes: `LINGUA_PORTUGUESA`, `MATEMATICA` (V2) +
  `CIENCIAS_NATUREZA`, `CIENCIAS_HUMANAS` (V18/V20, só EAJ-2021).

## 4. Vocabulário de `topic` (restrito ou DESCONHECIDO)

| Disciplina | Códigos aceitos |
|---|---|
| `LINGUA_PORTUGUESA` | `GRAMATICA_NORMA`, `INTERPRETACAO_TEXTUAL`, `DESCONHECIDO` |
| `MATEMATICA` | `RAZAO_PROPORCAO`, `ARITMETICA`, `ALGEBRA`, `GEOMETRIA`, `ESTATISTICA_DADOS`, `PORCENTAGEM`, `MATEMATICA_FINANCEIRA`, `GRANDEZAS_MEDIDAS`, `DESCONHECIDO` |
| `CIENCIAS_NATUREZA` (só EAJ-2021) | `ECOLOGIA`, `NUTRICAO_SAUDE`, `AGROPECUARIA`, `BIOLOGIA_CELULAR`, `QUIMICA_GERAL`, `FISICA_GERAL`, `DESCONHECIDO` |
| `CIENCIAS_HUMANAS` (só EAJ-2021) | `HISTORIA_BRASIL`, `CULTURA_SOCIEDADE`, `CARTOGRAFIA`, `GEOGRAFIA_BRASIL`, `GEOPOLITICA`, `DESCONHECIDO` |

Subcódigos (subtopics) **não fazem parte do contrato v1** (decisão DEV
acima); o vocabulário fino por tópico vive nas fontes do §0 (V2, V20,
content-maps) para uso futuro, sem duplicação aqui. `topic` CN/CH fora de
`EAJ-2021` → lote reprovado.

## 5. Proibições v1 (prompt proíbe, validador recusa)

1. **Passagem/texto-base compartilhado**: questões-IA auto-contidas. Sem
   `Texto N`, `trecho`, cabeçalho de bloco ou dependência externa.
2. **Figura**: sem `figura`, `imagem`, `gráfico`, `tabela`, `quadro`,
   `tirinha`, `foto`, `observe`, `conforme`, `charge`, `ilustração`.
3. **Explanation** (decisão 2026-10-04): sem `explanation` — o prompt
   proíbe, o backend reprova se vier, o front nunca renderiza.
4. Conteúdo ofensivo/identificador/PII: proibido no prompt; corpo sem
   dado do aluno (só formato/disciplina/tópicos — TASKS.md regra 4).

## 6. Enunciados auto-contidos + regex de recusa (fixada na DEV, com testes)

Aplicada em `stem` + valores de `options` (case-insensitive, com acentos):

* **Figura**: `\bfigura\b`, `\bimagem\b`, `\bgr[áa]fico\b`, `\btabela\b`,
  `\bquadro\b`, `\btirinha\b`, `\bfoto\b`, `\bilustra[çc][ãa]o\b`,
  `\bobserve\b`, `\bconforme\b`, `\bcharge\b`.
* **Passagem**: `\btexto\s*[0-9ivx]+\b`, `\btrecho\b`, `texto-base`,
  `quest[õo]es?\s+\d+\s*(a|e|-)`.

Decisões DEV contra o draft VPS (registradas para auditoria): `mapa`
NÃO foi incluído — questões auto-contidas de ESCALA usam "mapa"
legitimamente (`RAZAO_PROPORCAO/ESCALA`); referências externas a mapa
caem em `observe`/`conforme`/`Texto N`. `textos` sem número NÃO foi
incluído — gênero/tipo textual citam "textos" legitimamente. Palavras
como `resolução`/`passo a passo`/`comentário` NÃO viraram marcador de
texto: a proibição de explanation é pelo **campo** (reprovado no §1),
não por vocabulário do enunciado.

Implementação vigente:
`backend/src/main/java/br/com/voupassar/aiprova/AiProvaValidator.java`.

## 7. Validador estrutural (DEV — testável sem rede)

Reprovação → `422 AI_GENERATION_INVALID` com motivo por posição (nunca
correção silenciosa, nunca caderno parcial):

* JSON não-parseável ou com fences de markdown;
* N ≠ N do modelo; `n` com lacuna/repetição/fora de `1..N`;
* `options` sem exatamente `A–D`, com vazia ou duplicada;
* `answer` ∉ `{A,B,C,D}`;
* marcador de figura/passagem (§6) em `stem`/`options`;
* campo proibido presente (§1);
* `discipline`/`topic` fora do vocabulário do modelo (exceto
  `DESCONHECIDO`); `format`/`editionModel` inconsistentes ou
  desconhecidos.

Cobertura: `backend/src/test/java/br/com/voupassar/aiprova/AiProvaValidatorTest.java`
+ envelope 422 em `GlobalExceptionHandlerTest`.

## 8. Selo, origem e isolamento (não-negociáveis, TASKS.md regras 1–3)

* Origem lógica: `AI_GENERATED` (distinto de `OFFICIAL/AUTORAL/ADAPTED`;
  DDL na V23 — ver abaixo). Nunca citar edição real como origem da questão
  — o formato é *inspirado em*, nunca *extraído de*.
* Selo obrigatório na API e na UI (fases A.3/D): por questão ("gerada por
  IA — não oficial") + no resultado ("placar sobre gabarito gerado por IA
  — sem validade de prova real").
* Isolamento: nada-IA entra em `questions`, `question_options`,
  `question_sources`, mapa de conteúdo, diagnóstico, roteiro, revisão,
  performance ou `GET /questions` (trava da B.1, DEV). Caderno efêmero em
  estrutura própria (A.2); gabarito congelado imutável.
* Auditoria da geração (`prompt_version=ai-prova-v1`, modelo, timestamp,
  tentativas) sem PII — campos na A.2/C.2.

## 9. Prompt `ai-prova-v1` (canônico único)

`backend/src/main/resources/ai/prompts/ai-prova-v1.txt` — estrutura por
modelo via parâmetros (`INSTITUTION/EDITION_MODEL/N/MAPA_DE_POSICOES`),
1 exemplo few-shot, proibições explícitas (§5 + sem `subtopic`), JSON
estrito sem markdown, regra de variedade (soft, só prompt). Mudança de
instrução exige `ai-prova-v2`, nunca edição silenciosa. Carregável e
testado via `AiProvaPrompts` (`PROMPT_VERSION=ai-prova-v1`).

## 10. Fora deste schema (não prometer aqui)

* Modelo default, timeout, limites da rota free, segredos: TASK C.1
  (`docs/ai-provedor.md`, DEV).
* Cota (3/dia), concorrência, `429/409`: TASK C.2 (DEV).
* Endpoint, mock, selo em resposta, execução/submit/result: TASK A.3 —
  DONE DEV (mock determinístico `AiMockProvider` válido nos 9 modelos;
  `POST /simulations/ai-generated` + execução/resposta/conclusão/abandono/
  resultado/feedback sobre o caderno-IA com ocultação em PROVA; selo por
  questão e no placar; `POST /attempts` recusa execução-IA com `400
  AI_ATTEMPT_EXCLUSIVE`; suíte 362/362; contrato em
  `docs/api-simulados.md`). `institution` inválida → `400`; modelo
  inexistente → `404`.
* Tabela do caderno efêmero, `frozen_answer_key`, auditoria: TASK A.2 —
  DONE DEV (`database/migrations/V23__ai_generations.sql`: `ai_generations`
  + `ai_generation_questions` + `ai_generation_answers`, type
  `AI_GENERATED_EDITION` em `simulations`; ERD §12). Decisão registrada:
  **nenhum `source_type` novo em `questions`** — questão-IA nunca entra no
  acervo, logo não precisa de código de origem lá; `AI_GENERATED` vive
  como origem lógica no selo da API/UI (A.3/D) e no `filter_json` da
  execução-IA.
* Correção pedagógica do gabarito: limitação assumida (decisão 4) —
  documentar em `docs/blockers.md` na B.2; o validador cobre SÓ formato.

## 11. Exemplo (excerto — N real é 40/50, §2)

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
      "stem": "EXEMPLO SINTÉTICO: …enunciado completo e auto-contido…",
      "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
      "answer": "B"
    },
    {
      "n": 2,
      "discipline": "MATEMATICA",
      "topic": "PORCENTAGEM",
      "stem": "EXEMPLO SINTÉTICO: …enunciado completo e auto-contido…",
      "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
      "answer": "C"
    }
  ]
}
```

Few-shot completo no prompt canônico (§9).
