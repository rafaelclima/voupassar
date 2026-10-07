# API de Recomendação — TASK 4.3 (score v2 na TASK 18.1)

> Motor determinístico e explicável para priorização de estudos (AGENTS.md §8).
>
> Sem ML: score v2 por assunto = `frequência histórica real × (1 − acerto) ×
> dificuldade do assunto × recência` (pesos e fontes auditáveis abaixo),
> lacuna com piso + ordenação por frequência, carga por meta/ano-alvo só no
> banding. `ALGORITHM_VERSION=v2-deterministico` (v1 em planos antigos).
> Cada item traz motivo textual derivado dos mesmos fatores que o classificam,
> e `evidence_json` rastreável para edições/questões que sustentam a recomendação.

## Endpoints

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/recommendations` | Bearer JWT (dono do token; sem `?userId` desde 16.1) | Gera/regenera roteiro (cria `study_plan` + `study_plan_items`) |
| `GET` | `/api/v1/recommendations/plan` | Bearer JWT (dono do token; sem `?userId` desde 16.1) | Recupera roteiro vigente com itens ordenados |
| `POST` | `/api/v1/recommendations/items/{itemId}/status?status=` | Bearer JWT (só o dono; item alheio → `403`) | Atualiza status (`TODO` → `DOING` → `DONE`/`SKIPPED`) |

> TASK 16.1 (P1 — IDOR, 2026-10-07): removido `?userId` dos dois GETs/POST de
> geração. O backend usa só `principal.userId()`. `POST .../status` checa
> `plan.userId == principal.userId` e responde `403 FORBIDDEN` se divergir
> (via `AccessDeniedException` → envelope `FORBIDDEN`), `404 ITEM_NOT_FOUND`
> se o item não existir, `400 INVALID_STATUS` para status inválido, `401`
> sem token. Sem `?userId` não há como pedir o plano de outro aluno.

## Contrato de resposta

### POST `/api/v1/recommendations`

Retorna o `StudyPlanResponse` (DTO — nunca a entidade JPA `StudyPlan`).

Campos principais (`StudyPlanResponse` + `StudyPlanItemResponse`, TASK 16.1):
- `id`, `userId`, `isActive`, `algorithmVersion` (`v2-deterministico` desde 18.1; `v1-…` em planos antigos)
- `generatedAt`, `createdAt`, `updatedAt`
- `items` (lista de DTOs, ordenados por `priority` no `GET /plan`):
  - `id`, `topicId`, `subtopicId` (NULL na versão inicial)
  - `priority` (1–5, determinístico: maior prioridade = menor número)
  - `reason` (texto explicável, derivado de desempenho + histórico)
  - `evidenceJson` (JSONB com referência a edições/questões — inicial simplificado)
  - `status` (`TODO` padrão)

### GET `/api/v1/recommendations/plan`

Mesma estrutura, mas recupera o plano ativo atual (não cria novo).
Se não houver plano ativo → `404 NO_ACTIVE_PLAN` (envelope).

## Regras determinísticas

### Algoritmo v2 (`v2-deterministico`, TASK 18.1)

Fórmula (pesos fixos no código — `RecommendationService.java`):

```
frequencyFactor   = 0.7 × (hist / maxHist) + 0.3 × (editions / maxEditions)   # [0,1]
performanceFactor = 1.0 − accuracy                                            # [0,1]
recencyFactor     = 1.0 + min(0.5, diasDesdeÚltimaTentativa / 90)              # [1.0,1.5]; sem sinal → 1.0
difficultyFactor  = 1.0 + 0.5 × (MEDIA+DIFICIL) / totalMarcadas               # [1.0,1.5]; sem sinal → 1.0
score             = frequency × performance × recency × difficulty             # até 2.25
lacuna            = 0.9 + 0.1 × frequency   (quando attempts == 0, ordena por frequência)
carga             = urgência(target_year) × meta(study_goal)                   # só banding, nunca filtro
```

Fontes (nada inventado):

1. **Frequência real:** `question_classifications.countByTopic` +
   `editionsByTopic` — a mesma fonte do diagnóstico
   (`DiagnosisService.java:147-154`). Banco atual: 300 classificações com
   tópico em 10 assuntos (máx 77). Normalização por máximo (não por total),
   para o assunto mais cobrado valer 1.0.
2. **Recência:** `student_topic_performance.last_attemptAt` (já persistido;
   era `1.0` fixo na v1). Semântica: quanto mais parado, maior a prioridade
   (revisão espaçada), com teto em 90 dias.
3. **Dificuldade:** mix de `questions.difficulty_estimate` por assunto via
   `classifications.difficultyByTopic` (banco atual: FACIL 149, MEDIA 140,
   DIFICIL 11 — palpite global BAIXA, `Question.java:28`; o peso 0.5 limita
   o quanto o palpite move o score). Assunto sem questão marcada → 1.0
   neutro (DESCONHECIDO).
4. **Carga:** `student_profiles.target_year` (prova neste ano/atrasada → 1.2;
   próximo ano → 1.1; demais/ausente → 1.0) × `study_goal` presente → 1.05.
   Só concentra/alarga as faixas de prioridade (`bandSize =
   round(size / (5 × carga))`, mínimo 1); nunca filtra assunto. Ausentes →
   1.0 neutro (DESCONHECIDO).
5. **Prioridade:** rank 0 → P1 (menor número = maior prioridade, como o
   frontend ordena). Corrige a v1, que dava P5 ao melhor rank (fórmula
   `5 − i/…` invertida).

### Exemplo numérico

Assunto X: 70 questões em 6 edições (máx do banco: 77 em 6) →
`frequency = 0.7×(70/77) + 0.3×(6/6) = 0.936`. Aluno com 5 tentativas,
20% de acerto, última há 10 dias, mix só FACIL, sem ano-alvo:
`performance = 0.8`, `recency = 1 + 10/90 = 1.111`, `difficulty = 1.0`,
`score = 0.936 × 0.8 × 1.111 × 1.0 = 0.832`.
Assunto Y raro (5 questões/1 edição), mesmo desempenho:
`frequency = 0.7×(5/77) + 0.3×(1/6) = 0.095` → `score = 0.084`.
X vem antes de Y, tudo o mais igual (coberto por teste).

### Explicabilidade

Cada `StudyPlanItem` traz:
- `reason`: frase determinística com números (`corretas / tentativas`, aproveitamento, score, referência a evidência).
- `evidenceJson`: `{topic_id, historical_evidence: "taxonomia_v1.1", source_type: "DERIVADO_EVIDENCIA", attempts: N, note: "..."}`.

Nunca apresenta recomendação como verdade absoluta — notas explícitas indicam:
- classificação derivada (`PENDING` — revisão humana PENDENTE, TASK 12.2);
- evidência simplificada (requer curadoria e vinculação a edições para completude).

## Segurança e limites

- Apenas o dono do token (`@AuthenticationPrincipal`).
- `study_plans` e `study_plan_items` estão protegidos por `ON DELETE CASCADE` no banco (não apaga histórico ao criar novo plano — apenas desativa `is_active`).
- Nenhum dado pessoal no corpo além do `userId`; PII só no contexto autenticado.
- Nenhum `innerHTML` com dados não confiáveis (relevante para futuro frontend, Fase 6).

## OpenAPI

Contratos disponíveis em `GET /v3/api-docs` (público, sem PII) e `GET /swagger-ui.html`. Tag `Motor Educacional`; escopo `/api/v1/**`.

## Verificação (2026-09-30)

- `mvn test` → 137/137 (incl. `RecommendationServiceTest` 3 casos).
- `GET /api/v1/recommendations/plan` (sem token) → `401`; com token e plano ativo → `200` com `items` ordenados; sem plano → `404 NO_ACTIVE_PLAN`.
- `POST /api/v1/recommendations` (autenticado) → `200` com `StudyPlan` criado e `items` populados; `algorithmVersion = v2-deterministico` (v1 nos planos gerados antes da 18.1).
- `POST /api/v1/recommendations/items/{id}/status` (autenticado) → atualiza `status` no banco.

## Correção 2026-10-01 (encontrada pela TASK 6.4, dashboard)

O teste ao vivo do dashboard com conta nova revelou 3 defeitos que
contradiziam a verificação acima (todos com `500 INTERNAL_ERROR`):

1. **Recursão infinita no JSON:** `GET /plan` e `POST /recommendations`
   serializavam a entidade `StudyPlan` com a volta
   `items[].studyPlan.items...` → `StackOverflow`/`LazyInitialization`.
   Fix: `@JsonIgnore` em `StudyPlanItem.studyPlan` (direção `plan → items`
   preservada). Regressão coberta em `StudyPlanSerializationTest`.
2. **`NO_ACTIVE_PLAN`/`ITEM_NOT_FOUND`/`USER_NOT_FOUND` como 500:**
   `IllegalArgumentException` caía no handler genérico. Fix:
   `ResourceNotFoundException` (404), igual aos demais serviços; mensagem de
   `NO_ACTIVE_PLAN` orienta a gerar o roteiro.
3. **Regenerar quebrava (500 por `uq_study_plans_active`):** o
   `UPDATE` de desativação descarregava depois do `INSERT`. Fix:
   `saveAndFlush` após desativar.
4. **Status sem validação:** valor fora de `TODO/DOING/DONE/SKIPPED`
   violava o `CHECK` (500). Fix: `BadRequestException INVALID_STATUS`
   (400) no serviço.

Pós-fix ao vivo: `GET /plan` sem plano → `404 NO_ACTIVE_PLAN`; `POST` →
`200` (10 itens, sem volta `studyPlan`); `GET /plan` → `200`; segundo
`POST` (regenerar) → `200`; `status=DONE` → `200`; `status=CONCLUIDO` →
`400 INVALID_STATUS`; item inexistente → `404 ITEM_NOT_FOUND`.
`mvn test` → `216/216` (incl. 5 testes novos de regressão).

## Limitações conhecidas (não inventadas)

- `evidenceJson` é versão simplificada (não vincula ainda edições/questões concretas — requer `question_classifications` aprovadas e curadoria, TASK 1.5, 11.2, 12.2; enriquecido na TASK 18.2).
- `difficultyFactor` usa `difficulty_estimate` (palpite global BAIXA) com peso
  limitado a +50% — calibrado de verdade só com desempenho por edição (Fase 4).
- `subtopic_id` preenchido na TASK 18.2 quando houver sinal (era `NULL` fixo).
- A tabela `student_topic_performance` pode estar vazia; `attempts == 0`
  segue lacuna (piso 0.9) ordenada por frequência histórica real.
