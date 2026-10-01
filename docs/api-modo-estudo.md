# API do Modo Estudo — TASK 5.2 (feedback imediato)

> Feedback imediato por posição do caderno: acerto/erro, resposta correta,
> explicação e conteúdo relacionado — sobre `simulation_questions` (caderno
> congelado, TASK 5.1) + última tentativa vinculada em `question_attempts`
> (fato imutável, TASK 3.7) + classificação vigente em
> `question_classifications` (TASK 3.3).
>
> Responde via `GET /api/v1/simulations/attempts/{id}/feedback/{position}`.
> O `POST /attempts` (TASK 3.7) continua existindo e inalterado: ele registra
> a resposta; este endpoint revela o porquê (correção + explicação + assunto).

## Endpoint (autenticado)

| Rota | Descrição |
|---|---|
| `GET /api/v1/simulations/attempts/{id}/feedback/{position}` | feedback da posição (1-based) na execução do dono do token |

Disponibilidade por modo (confronto direto com a TASK 5.3):

| Modo | Status | Resposta |
|---|---|---|
| `ESTUDO` | qualquer (`IN_PROGRESS`, `SUBMITTED`, `ABANDONED`) | `200` com o feedback |
| `PROVA` | `IN_PROGRESS` | `409 STUDY_FEEDBACK_UNAVAILABLE` (gabarito oculto até encerrar) |
| `PROVA` | `SUBMITTED` / `ABANDONED` | `200` com o feedback (correção após concluir) |

Posição sem resposta vinculada → `409 FEEDBACK_NOT_AVAILABLE` (nunca feedback
inventado). Posição inexistente → `404 SIMULATION_QUESTION_NOT_FOUND` com o
total de posições explícito. Posição `< 1` → `400 INVALID_POSITION`.
Execução inexistente **ou de outro aluno** → `404
SIMULATION_ATTEMPT_NOT_FOUND` (sem distinguir, anti-enumeração).
Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–5.1). Tudo no envelope `{code, message, details, traceId,
timestamp, path}`, sem stack trace; `traceId` também no header `X-Trace-Id`.

## Contrato de resposta

`StudyFeedbackResponse` (record):

- `attemptId`, `position`, `questionId`, `disciplineCode/Name`,
  `sourceYear`, `sourceQuestionNumber`;
- `selectedOption` — última resposta vinculada a esta execução (maior
  `answeredAt`, desempate por maior `id`);
- `isCorrect` — contra o gabarito **congelado** (NULL quando anulada);
- `wasAnnulled` — anulada no congelado, no gabarito atual ou na tentativa;
- `correctAnswer` — resposta correta (`frozen_answer_key`);
- `explanation` — explicação redigida (NULL = `NECESSITA REVISÃO`, nunca texto
  gerado automaticamente);
- `topicId/Code/Name`, `subtopicId/Code/Name` (NULL quando sem classificação
  vigente — assunto `NÃO CONFIRMADO`, nunca omitido);
- `classificationStatus/Confidence/TaxonomyVersion` (NULL quando sem
  classificação);
- `notes` — evidências, limitações e referências.

## Regras de evidência

* **Última tentativa por questão:** o fato é imutável (trigger
  `trg_attempts_immutable`); correção só via nova tentativa — o feedback
  reflete sempre a resposta vigente, igual ao placar da TASK 5.1.
* **Gabarito congelado, não o atual:** o confronto usa o
  `frozen_answer_key` gravado na criação. Se a questão for reclassificada
  depois, o feedback continua consistente com o placar do simulado (difere
  nisso do `POST /attempts`, que corrige pelo gabarito vigente — decisão da
  TASK 3.7, aqui inalterada).
* **Anuladas:** `isCorrect` NULL, fora do aproveitamento (pontuação
  DESCONHECIDA, TASK 1.3 §4); contam como conteúdo respondido.
* **Explicação:** vem de `questions.explanation` (NULL na maioria do banco —
  sinalizado em `notes`, nunca inventada).
* **Conteúdo relacionado:** classificação vigente (`status <> 'REJECTED'`,
  mais recente por questão) — revisão humana PENDENTE (TASK 12.2), nunca
  verdade oficial do IFRN.
* **Enunciado e alternativas** não são duplicados: resolve-se via
  `GET /api/v1/questions/{id}` (TASK 3.4).
* **Fora de escopo (não inventados):** regras de ocultação do `POST
  /attempts` no Modo Prova e da consulta de questão durante a prova (TASK
  5.3); experiência dedicada de revisão (TASK 5.4, já existe a fila em
  `/review/queue`); simulado de edição real (TASK 5.5).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Simulados`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-10-01)

* `mvn -f backend/pom.xml test` → **180/180** (169 anteriores + 11 novos:
  `SimulationServiceTest` 10, `SimulationControllerTest` 1).
* Casos cobertos: feedback ESTUDO com gabarito/explicação/assunto; uso do
  congelado após mudança do gabarito atual; última tentativa por questão;
  PROVA em andamento `409 STUDY_FEEDBACK_UNAVAILABLE`; PROVA encerrada `200`;
  posição sem resposta `409 FEEDBACK_NOT_AVAILABLE`; posição inexistente
  `404`; posição `< 1` `400`; execução de outro aluno `404`; anulada com
  `isCorrect` NULL; sem classificação/explicação com notas `NÃO
  CONFIRMADO`/`NECESSITA REVISÃO`; controller `401` sem token e `200` com a
  forma do contrato.
* `docker compose up -d --build app` → `healthy` (sem migração nova: só
  leitura sobre `simulation_questions` + `question_attempts` +
  `question_classifications`; valida o mapeamento JPA contra o schema real via
  `ddl-auto: validate`); sem token a rota nova responde `401` em envelope;
  `/v3/api-docs` lista o path novo (46 no total).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → simulado `MATEMATICA × 2 ESTUDO` `201 IN_PROGRESS` → `GET
  /feedback/1` `409 FEEDBACK_NOT_AVAILABLE` → resposta `A` via `POST
  /attempts` (`simulationAttemptId`) `201 isCorrect true` → `GET /feedback/1`
  `200` (selected `A`, correct `A`, assunto real `ESTATISTICA_DADOS`,
  explicação ausente sinalizada `NECESSITA REVISÃO`) → simulado `PROVA` +
  `GET /feedback/1` `409 STUDY_FEEDBACK_UNAVAILABLE`. Scratch removido após
  (trigger de imutabilidade desligado só para o `DELETE` de limpeza e religado
  em seguida — tentativas 0, usuários 0, 240 questões preservadas; CI usa
  mocks, sem Docker).

## Notas técnicas (para não redescobrir)

* Pacote `br.com.voupassar.simulations` segue o padrão por domínio:
  `controller/` → `service/` → `repository/` (+ `dto/`, `entity/`); nenhuma
  regra em controller (AGENTS.md §19).
* `SimulationService` ganhou a dependência `QuestionClassificationRepository`
  (construtor `@Autowired` + package-private com `Random` injetável para
  testes); `SimulationServiceTest.setup` atualizado em consequência.
* A posição é resolvida em memória sobre
  `findBySimulationAttemptIdOrderByPositionAsc` (caderno ≤ 100 posições —
  sem query nova, sem N+1 relevante) em vez de finder derivado por posição.
* A comparação de recência reutiliza `compareRecency` (maior `answeredAt`,
  desempate por maior `id`) — mesma regra do placar.
