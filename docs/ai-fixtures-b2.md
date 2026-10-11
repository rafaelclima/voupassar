# Bateria de fixtures adversárias — TASK B.2 (DRAFT VPS)

> **Status: DRAFT — não é DONE.**
> Escopo VPS (sem testes): draft dos payloads inválidos por classe de
> defeito (lista do corpo da B.2). Execução contra o validador + `422`
> por posição = DEV-only. DONE só na DEV.
>
> Branch: `draft/b2-vps-fixtures`. Nenhum Java/SQL/JS de produção neste
> draft. Validador vigente: `backend/.../aiprova/AiProvaValidator.java`
> (A.1-DEV, `59eaa18`); testes vigentes:
> `backend/.../aiprova/AiProvaValidatorTest.java`.
>
> **Reconciliação DEV 2026-10-11 (dona do validador): DONE.**
> Bateria executada em
> `backend/src/test/java/br/com/voupassar/aiprova/AiProvaAdversarialTest.java`
> (16/16 verdes): B2-01–B2-13 rejeitados com `AI_GENERATION_INVALID`
> (422, mapeamento provado em `GlobalExceptionHandlerTest`) e motivo por
> posição; lote válido passa (A.1); nenhum caso gera caderno parcial.
> Correções contra o draft (conflito resolvido pela DEV, regra do
> paralelo): **B2-09b** ("conforme o trecho") cai na mensagem de
> **figura**, não de passagem — `conforme` é marcador de figura
> (`docs/ai-prova-schema.md §6`) e a checagem de figura precede a de
> passagem (`AiProvaValidator.java:230-238`); rejeição com posição
> igualmente honesta, executada em `b209b_conformeTrechoFallsInFigureMessage`.
> **B2-02** (N a maior) não tinha teste dedicado — coberto em
> `b202_overCount` (N=40 + `q41: n fora de 1..40`).
>
> Convenção deste draft: cada fixture = **base válida + 1 mutação**.
> Base: `AiProvaValidatorTest.validBatch("IFRN-2026")` (N=40, LP 1–20
> `INTERPRETACAO_TEXTUAL`, MAT 21–40 `ALGEBRA`, `answer: "A"`, sem campo
> extra). Os trechos abaixo mostram SÓ a mutação (excertos ilustrativos,
> não lotes executáveis) — a DEV aplica cada mutação ao lote completo de
> 40 para execução. Motivos esperados copiados do validador vigente.

## B2-01 — N a menor (39/40)

* Mutação: remover a questão `n=40` do lote base.
* Motivo esperado: `envelope: esperado N=40 para IFRN-2026, recebido 39`
  (+ `envelope: MATEMATICA esperado 20, recebido 19`).
* Ref DEV: `wrongCountRejected`.
* Excerto da mutação: lote com `questions[0..38]` (q40 ausente).

## B2-02 — N a maior (41/40)

* Mutação: duplicar a q1 como `n=41` ao final do lote base.
* Motivo esperado: `envelope: esperado N=40 para IFRN-2026, recebido 41`
  + `q41: n fora de 1..40`.
* Ref DEV: (classe sem teste dedicado — DEV cobre no `wrongCountRejected`
  invertido ou adiciona).

## B2-03 — Lacuna de numeração

* Mutação: `{"n": 7, …}` → `{"n": 8, …}` (q7 some, q8 duplica).
* Motivo esperado: `q8: n duplicado` (validador suprime a varredura de
  lacuna quando há duplicata — ver `AiProvaValidator.java:207`).
* Ref DEV: `numberingGapRejected`.

## B2-04 — Alternativa ausente (chave fora de A–D)

* Mutação na q1: `"D": "Opção D da 1"` → `"D2": "Opção D da 1"`.
* Motivo esperado: `q1: options deve conter exatamente A,B,C,D`.
* Ref DEV: `missingAlternativeRejected`.

## B2-05 — Alternativa vazia

* Mutação na q3: `"C": "Opção C da 3"` → `"C": "  "`.
* Motivo esperado: `q3: alternativa C vazia`.
* Ref DEV: `emptyAlternativeRejected`.

## B2-06 — Alternativas duplicadas

* Mutação na q2: `"B": "Opção B da 2"` → `"B": "Opção A da 2"`.
* Motivo esperado: `q2: alternativas duplicadas`.
* Ref DEV: `duplicateAlternativesRejected`.

## B2-07 — Resposta fora de A–D

* Mutação na q1: `"answer": "A"` → `"answer": "E"`.
* Motivo esperado: `q1: answer deve ser A, B, C ou D`.
* Ref DEV: `answerOutsideAlternativesRejected`.

## B2-08 — Menção a figura (3 variantes)

* B2-08a (stem): `Enunciado auto-contido número 5 …` →
  `Observe a imagem abaixo e responda a 5 …`
* B2-08b (stem): `… número 6 …` → `… conforme o gráfico abaixo, …`
* B2-08c (alternativa): `"A": "Opção A da 7"` → `"A": "os dados da tabela acima"`
* Motivo esperado: `qN: menção a figura/imagem/gráfico/tabela em
  stem|alternativa A (questão deve ser auto-contida)`.
* Ref DEV: `figureMentionRejected`, `quadroAndTirinhaMentionRejected`.

## B2-09 — Menção a passagem (2 variantes)

* B2-09a (stem): `… número 6 …` → `Segundo o Texto 1, responda a 6 …`
  → `q6: menção a passagem/texto-base em stem`.
* B2-09b (stem): `… número 7 …` → `… conforme o trecho apresentado …`
  → `q7: menção a figura/imagem/gráfico/tabela em stem` (**correção DEV**:
  `conforme` é marcador de figura e a checagem de figura é anterior;
  rejeição com posição igualmente honesta).
* Ref DEV: `passageMentionRejected` (09a), `b209b_conformeTrechoFallsInFigureMessage` (09b).

## B2-10 — `explanation` presente

* Mutação na q1: `"answer": "A"` →
  `"answer": "A", "explanation": "Porque sim"`.
* Motivo esperado: `q1: campo proibido presente (explanation)`.
* Ref DEV: `explanationPresentRejected`.
* Nota: backend descarta se vier; front nunca renderiza (decisão
  2026-10-04).

## B2-11 — JSON com markdown (fences)

* Mutação: envolver o lote válido em ```` ```json … ``` ````.
* Motivo esperado: `envelope: json estrito esperado, sem fences de
  markdown`.
* Ref DEV: `markdownFencesRejected`.

## B2-12 — JSON malformado

* Mutação: truncar o corpo (`{"format": "IFRN", `).
* Motivo esperado: `envelope: json invalido`.
* Ref DEV: `malformedJsonRejected`.

## B2-13 — `subtopic` presente (reconciliação DEV, fora do contrato v1)

* Mutação na q1: `"answer": "A"` →
  `"answer": "A", "subtopic": "INFERENCIA"`.
* Motivo esperado: `q1: campo proibido presente (subtopic)`.
* Ref DEV: `subtopicOutsideV1ContractRejected`.
* Nota de conflito assumida: o draft VPS da A.1 previa `subtopic`
  opcional; a DEV decidiu que a v1 não tem `subtopic` (TASKS.md A.1/A.2
  não o listam) e o validador rejeita. Conflito de contrato resolve-se
  pela DEV (dona do validador) — este draft segue o validador vigente.

## Cobertura já existente (não duplicar em JSON)

* `topic` inventado → `qN: topic inválido…` (`inventedTopicRejected`).
* Disciplina fora do modelo → `qN: discipline inválida ou fora do modelo…`
  (`disciplineOutsideModelRejected`).
* Modelo inexistente (`IFRN-2021`, `IFRN-2030`) → `envelope: editionModel
  desconhecido…` (`absentEditionNeverInvented`,
  `unknownEditionModelRejected`).
* Lote nulo/vazio → corpo vazio (`nullBatchRejected`).

## Resultado esperado em todos os casos (DEV-only)

* Cada fixture → exceção `AI_GENERATION_INVALID` (mapeada para
  `422 AI_GENERATION_INVALID`) com motivo por posição; **nenhum caso gera
  caderno parcial** (TASKS.md regra 5).
* Lote válido passa (3 formas: IFRN-40, EAJ-40, EAJ-2021-50 —
  `validBatchesPassForAllThreeShapes`).

## Proposta de texto para `docs/blockers.md` (draft — DEV confirma)

> O validador estrutural da Prova-IA (A.1/B.2) cobre SÓ formato: JSON
> parseável e estrito, N exato, 1..N sem lacuna, A–D completas e
> distintas, `answer` ∈ alternativas, disciplinas/contagens do modelo,
> `topic` no vocabulário ou `DESCONHECIDO`, sem figura/passagem/
> explanation. A **correção pedagógica do gabarito gerado por IA NÃO é
> verificada** (limitação assumida, TASKS.md decisão 4): o placar-IA é
> experimental, sem validade de prova real, e nada-IA alimenta o motor
> educacional (isolamento B.1).
