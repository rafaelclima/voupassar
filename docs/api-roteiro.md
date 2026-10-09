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

### `study_plans` (TASK E.2 — trilha por processo)

| Coluna | Tipo | Regra |
|---|---|---|
| `id` | BIGINT PK | Identidade |
| `user_id` | BIGINT FK (`users`) | `ON DELETE CASCADE` |
| `institution` | TEXT NOT NULL DEFAULT 'IFRN' | Trilha do processo (`IFRN`/`EAJ`) — V21 |
| `is_active` | BOOLEAN DEFAULT TRUE | Único ativo por `(user_id, institution)` (`UNIQUE` parcial V21) — um roteiro vigente por trilha, sem desativar a outra |
| `algorithm_version` | TEXT NOT NULL | `v2-deterministico` (global legado, `null`/ausente); `v2.1-institution` (trilha informada — EAJ) |
| `status` | TEXT NOT NULL | `PROVISORIO` (<3 pontuáveis na trilha) / `PESSOAL` (≥3) — TASK 20.1, `V16__study_plan_status.sql` |
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

### Schema do `evidence_json` (TASK 18.2 — trilha E.2)

```json
{
  "topic_id": 5,
  "institution": "EAJ",
  "historicalQuestions": 55,
  "editionsCount": 3,
  "editions": [2021, 2022, 2025],
  "sampleQuestionIds": [101, 102, 103],
  "sampleLabels": ["EAJ 2022 Q12", "EAJ 2025 Q05", "EAJ 2021 Q08"],
  "accuracy": 0.2,
  "attempts": 5,
  "lastAttemptAt": "2026-09-27T10:00:00Z",
  "algorithmVersion": "v2.1-institution"
}
```

Regras (TASK E.2 — evidência da trilha, sem contaminar o IFRN):

- `institution`: `IFRN` ou `EAJ` quando `?institution=` informado; ausente quando global (legado).
- `historicalQuestions`/`editions[]`: só do processo (`countByTopicForInstitution` / `editionsByTopicForInstitution`). Na trilha EAJ = 3 edições (2021/2022/2025, D.2); na IFRN = 6 edições (2020, 2022–2026; 2021 ausente).
- `sampleQuestionIds[]`: só `source_type = 'OFFICIAL'` do processo (`officialQuestionIdsByTopicForInstitution`). Autorais/adaptadas nunca entram (AGENTS.md §11).
- `sampleLabels[]`: rótulo de citação (`"EAJ 2022 Q12"`) — nunca id inventado; cada rótulo aponta para questão e edição existentes no banco (auditável por `GET /questions/{id}`). Vazio quando sem oficiais na trilha (`[]`, nunca `null`).
- `algorithmVersion`: `v2.1-institution` (trilha informada); `v2-deterministico` (global, comportamento pré-E.2).
- Nenhuma recomendação como verdade absoluta: `reason` inclui `institution` quando aplicável (ex. `"[trilha EAJ] ..."`), `PROVISORIO` quando `<3` pontuáveis, nota `DERIVADO_EVIDENCIA` com `PENDING`.

## Fluxo do roteiro

1. **Diagnóstico (TASK 4.2)** → `GET /api/v1/diagnosis` retorna pontos fortes, fracos, lacunas e fila de prioridades.
2. **Geração (TASK 4.3)** → `POST /api/v1/recommendations` cria `study_plan` + `study_plan_items` com prioridades determinísticas.
3. **Acompanhamento (TASK 4.4)** → aluno atualiza `status` (`POST .../items/{id}/status`) para registrar progresso.
4. **Revisão** → quando o desempenho muda (mais tentativas registradas), `POST /api/v1/recommendations` regenera o roteiro (desativa anterior, preserva histórico).

### Plano provisório (TASK 20.1)

Conta nova sem tentativas pontuáveis recebe plano em 1 clique (`POST
/api/v1/recommendations`, só dono do token): `study_plans.status =
PROVISORIO`, itens ordenados só por frequência histórica
(`countByTopic/editionsByTopic` — top `60/60/25/…` do `content-map.md`),
100% oficiais, com nota no `reason` ("comece pelo que mais cai — vira
pessoal após o diagnóstico"). Com ≥3 pontuáveis (mesmo limiar
`MIN_SCORED_FOR_SIGNAL` do diagnóstico), a (re)geração marca `PESSOAL`.
Quem pula o wizard da 20.2 fica com o provisório até ter sinal.

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
