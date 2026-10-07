# API de Roteiro de Estudos — TASK 4.4

> Gera roteiro baseado no diagnóstico (TASK 4.2) e no motor de recomendação (TASK 4.3).
>
> Estrutura esperada: `study_plans` (plano vigente por usuário) + `study_plan_items`
> (cada recomendação com prioridade, motivo, evidência e status de acompanhamento).

## Endpoints

> Os endpoints são compartilhados com a TASK 4.3 (`/api/v1/recommendations`),
> mas esta documentação foca na estrutura do roteiro e no acompanhamento de progresso.
>
> TASK 16.1 (P1 — IDOR, 2026-10-07): sem `?userId` em nenhum endpoint do
> roteiro. `POST /recommendations` e `GET /recommendations/plan` usam só o
> dono do token (`principal.userId()`). `POST .../items/{id}/status` exige o
> dono (`plan.userId == principal.userId`, senão `403 FORBIDDEN`). Respostas
> são DTOs (`StudyPlanResponse` / `StudyPlanItemResponse` em
> `studyplan/dto/`), nunca a entidade JPA `StudyPlan`
> (`RecommendationController.java`, `RecommendationService.java`).

## Modelo de dados

### `study_plans`

| Coluna | Tipo | Regra |
|---|---|---|
| `id` | BIGINT PK | Identidade |
| `user_id` | BIGINT FK (`users`) | `ON DELETE CASCADE` |
| `is_active` | BOOLEAN DEFAULT TRUE | Único ativo por usuário (`UNIQUE` parcial) |
| `algorithm_version` | TEXT NOT NULL | `v2-deterministico` desde a 18.1 (`v1-…` nos planos antigos; sem ML no MVP) |
| `generated_at` | TIMESTAMPTZ | Quando o roteiro foi criado |

### `study_plan_items`

| Coluna | Tipo | Regra |
|---|---|---|
| `id` | BIGINT PK | Identidade |
| `study_plan_id` | BIGINT FK (`study_plans`) | `ON DELETE CASCADE` |
| `topic_id` | BIGINT FK (`topics`) | `NOT NULL` — recomendação sempre por conteúdo controlado |
| `subtopic_id` | BIGINT FK (`subtopics`) | `NULL` na versão inicial |
| `priority` | SMALLINT (1–5) | Ordem determinística (menor = maior prioridade) |
| `reason` | TEXT NOT NULL | Motivo textual explicável |
| `evidence_json` | JSONB NOT NULL | `CHECK (evidence_json <> '{}')` — rastreabilidade |
| `status` | TEXT DEFAULT 'TODO' | `TODO` / `DOING` / `DONE` / `SKIPPED` |

## Fluxo do roteiro

1. **Diagnóstico (TASK 4.2)** → `GET /api/v1/diagnosis` retorna pontos fortes, fracos, lacunas e fila de prioridades.
2. **Geração (TASK 4.3)** → `POST /api/v1/recommendations` cria `study_plan` + `study_plan_items` com prioridades determinísticas.
3. **Acompanhamento (TASK 4.4)** → aluno atualiza `status` (`POST .../items/{id}/status`) para registrar progresso.
4. **Revisão** → quando o desempenho muda (mais tentativas registradas), `POST /api/v1/recommendations` regenera o roteiro (desativa anterior, preserva histórico).

## Critérios de aceitação

- [x] `POST /api/v1/recommendations` retorna `StudyPlanResponse` (DTO) com `algorithm_version` documentado.
- [x] `GET /api/v1/recommendations/plan` retorna roteiro vigente do dono do token, ordenado por `priority`.
- [x] `POST .../items/{id}/status` atualiza `status` sem inventar progresso; item alheio → `403`.
- [x] `evidence_json` não vazio (`CHECK`) — rastreabilidade obrigatória (AGENTS.md §8, §11).
- [x] Nenhuma recomendação apresentada como verdade absoluta — `reason` inclui nota de evidência derivada (`PENDING` — revisão humana necessária).

## Pendências (não decididas sem evidência)

- Vinculação completa de `evidence_json` a edições/questões concretas (requer `question_classifications` `APPROVED` + curadoria, TASK 11.2, 12.2).
- Refinamento por subassunto (`subtopic_id`) — requer classificação confirmada por subtópico.
- Calibração de `difficultyFactor` e `recencyFactor` — palpitues BAIXA / 1.0 no MVP.
- Regeneração automática após cada `POST /attempts` — avaliado para Fase 4 completa, mas não obrigatório no MVP.

## Referências

- `docs/architecture.md` §7 (Motor educacional)
- `docs/database-erd.md` §2.7 (Roteiro de estudos)
- `AGENTS.md` §8 (Roteiro de estudos — explicabilidade)
- `TASKS.md` §4.3, §4.4
