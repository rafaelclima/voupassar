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
| `subtopic_id` | BIGINT FK (`subtopics`) | `NULL` sem sinal; subassunto mais fraco do aluno no assunto quando há linhas de subassunto com tentativas (TASK 18.2, coerência checada no `SubtopicRepository`) |
| `priority` | SMALLINT (1–5) | Ordem determinística (menor = maior prioridade) |
| `reason` | TEXT NOT NULL | Motivo textual explicável |
| `evidence_json` | JSONB NOT NULL | Schema rico TASK 18.2 (abaixo) — rastreabilidade |
| `status` | TEXT DEFAULT 'TODO' | `TODO` / `DOING` / `DONE` / `SKIPPED` |

### Schema do `evidence_json` (TASK 18.2)

```json
{
  "topic_id": 5,
  "historicalQuestions": 70,
  "editionsCount": 6,
  "editions": [2020, 2022, 2023, 2024, 2025, 2026],
  "sampleQuestionIds": [101, 102, 103],
  "accuracy": 0.2,
  "attempts": 5,
  "lastAttemptAt": "2026-09-27T10:00:00Z",
  "algorithmVersion": "v2-deterministico"
}
```

Regras:

- `editions[]` vem de `classifications.editionsByTopic` (anos crescentes);
  `sampleQuestionIds[]` (máx 5) só com `source_type = 'OFFICIAL'`
  (`classifications.officialQuestionIdsByTopic`, ordem ano/número/id) —
  autorais/adaptadas nunca produzem evidência (regra Fase 15, AGENTS.md §11).
- `accuracy`/`attempts`/`lastAttemptAt` espelham o agregado do aluno no
  momento da geração (`lastAttemptAt = null` sem tentativas).
- Todo item de plano gerado com histórico cita ≥1 edição e ≥1 questão oficial
  existentes (coberto por `RecommendationServiceTest`); assunto sem questão
  oficial carrega `sampleQuestionIds: []` honesto, nunca id inventado.

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

- Calibração fina de `difficultyFactor` (usa palpite BAIXA com peso limitado a
  +50%) e pesos da frequência — só com desempenho por edição (Fase 4).
- Regeneração automática após cada `POST /attempts` — avaliado para Fase 4 completa, mas não obrigatório no MVP.

## Referências

- `docs/architecture.md` §7 (Motor educacional)
- `docs/database-erd.md` §2.7 (Roteiro de estudos)
- `AGENTS.md` §8 (Roteiro de estudos — explicabilidade)
- `TASKS.md` §4.3, §4.4
