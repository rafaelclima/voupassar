# API de Recomendação — TASK 4.3

> Motor determinístico e explicável para priorização de estudos (AGENTS.md §8).
>
> Sem ML na versão inicial: score por assunto = função auditável de
> `frequência histórica × (1 − acerto) × peso de dificuldade × recência × carência`.
> Cada item traz motivo textual derivado dos mesmos fatores que o classificam,
> e `evidence_json` rastreável para edições/questões que sustentam a recomendação.

## Endpoints

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/recommendations` | Bearer JWT | Gera/regenera roteiro (cria `study_plan` + `study_plan_items`) |
| `GET` | `/api/v1/recommendations/plan` | Bearer JWT | Recupera roteiro vigente com itens ordenados |
| `POST` | `/api/v1/recommendations/items/{itemId}/status` | Bearer JWT | Atualiza status (`TODO` → `DOING` → `DONE`/`SKIPPED`) |

## Contrato de resposta

### POST `/api/v1/recommendations`

Retorna o `StudyPlan` criado (com `items` populados após geração).

Campos principais:
- `id`, `userId`, `isActive`, `algorithmVersion` (`v1-deterministico`)
- `generatedAt`, `createdAt`, `updatedAt`
- `items` (lista de `StudyPlanItem`):
  - `topicId`, `subtopicId` (NULL na versão inicial)
  - `priority` (1–5, determinístico: maior prioridade = menor número)
  - `reason` (texto explicável, derivado de desempenho + histórico)
  - `evidenceJson` (JSONB com referência a edições/questões — inicial simplificado)
  - `status` (`TODO` padrão)

### GET `/api/v1/recommendations/plan`

Mesma estrutura, mas recupera o plano ativo atual (não cria novo).
Se não houver plano ativo → `404 NO_ACTIVE_PLAN` (envelope).

## Regras determinísticas

### Algoritmo (versão inicial sem ML)

1. **Entrada:** `topics` (taxonomia v1.1) + `student_topic_performance` (agregado da TASK 4.1).
2. **Fatores mínimos (auditáveis):**
   - `frequencyFactor` = `min(1.0, attempts / 5.0)` (carência de tentativas);
   - `performanceFactor` = `1.0 - accuracy` (quanto menor o acerto, maior prioridade);
   - `difficultyFactor` = `1.0` (palpite BAIXA — DESCONHECIDO até calibração);
   - `recencyFactor` = `1.0` (simplificado — calibrado na Fase 4 completa).
3. **Score:** `frequencyFactor * performanceFactor * difficultyFactor * recencyFactor`.
4. **Ajuste para lacunas:** se `attempts == 0` → `score = max(score, 0.9)`.
5. **Ordenação:** maior score primeiro; desempate por `topic.code` (determinístico).
6. **Prioridade:** `min(5, max(1, 5 - i / max(1, size/5)))` (distribuição aproximada em 5 níveis).

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
- `POST /api/v1/recommendations` (autenticado) → `200` com `StudyPlan` criado e `items` populados; `algorithmVersion = v1-deterministico`.
- `POST /api/v1/recommendations/items/{id}/status` (autenticado) → atualiza `status` no banco.

## Limitações conhecidas (não inventadas)

- `evidenceJson` é versão simplificada (não vincula ainda edições/questões concretas — requer `question_classifications` aprovadas e curadoria, TASK 1.5, 11.2, 12.2).
- `difficultyFactor` e `recencyFactor` são palpitues (BAIXA / 1.0) — calibrados na Fase 4 completa após análise de desempenho por edição.
- Subtópicos (`subtopic_id`) permanecem `NULL` na versão inicial — refinamento requer classificação por subassunto confirmada.
- A tabela `student_topic_performance` pode estar vazia até a TASK 4.1 ser concluída; o algoritmo trata `attempts == 0` como lacuna (score elevado) sem inventar progresso.
