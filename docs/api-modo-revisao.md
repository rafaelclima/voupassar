# API do Modo Revisão — TASK 5.4 (experiência dedicada)

> Sessão de revisão exclusiva: caderno congelado do top-N da fila (TASK 4.5),
> progresso com feedback imediato por posição e resultado ao encerrar — sobre
> `study_sessions` (modo `REVISAO`) + `review_session_questions` (caderno
> congelado, DDL V5) + última tentativa `REVISAO` por questão no fato imutável
> `question_attempts` (TASK 3.7).
>
> Responde via `POST /api/v1/review/sessions`, `GET
> /api/v1/review/sessions/{id}` e `GET /api/v1/review/sessions/{id}/result`.
> As respostas continuam via `POST /attempts` com `studySessionId` e
> `mode=REVISAO` (TASK 3.7, inalterado); o encerramento usa `POST
> /study-sessions/{id}/finish` (TASK 3.7, inalterado).

## Endpoints (autenticados, dono do token)

| Rota | Descrição |
|---|---|
| `POST /api/v1/review/sessions` | criar e iniciar: `{limit? 1–100 (padrão 20), discipline?, topicId?, onlyErrors?}` → `201` com caderno congelado (ordem da fila) |
| `GET /api/v1/review/sessions/{id}` | retomar: caderno + progresso (`answered`/pendente) com feedback imediato sempre revelado |
| `GET /api/v1/review/sessions/{id}/result` | visualizar resultado (só após encerrar a sessão) |

Disponibilidade e erros (envelope `{code, message, details, traceId,
timestamp, path}`, sem stack trace; `traceId` também no header `X-Trace-Id`):

| Situação | Resposta |
|---|---|
| Sem token | `401 {"code":"UNAUTHORIZED",…}` (secure-by-default, igual às TASKs 3.2–5.3) |
| Fila vazia com filtros válidos | `400 NO_REVIEW_ITEMS` com orientação (nunca sessão vazia inventada) |
| Disciplina/assunto inexistentes | `404 DISCIPLINE_NOT_FOUND` / `TOPIC_NOT_FOUND` (herdados da fila, TASK 4.5) |
| Sessão inexistente **ou de outro aluno** | `404 SESSION_NOT_FOUND` (sem distinguir, anti-enumeração) |
| Sessão sem caderno de revisão (ESTUDO/PROVA ou REVISAO genérica) | `404 REVIEW_SESSION_NOT_FOUND` com orientação para criar via este endpoint |
| `GET /result` com sessão `IN_PROGRESS` | `409 REVIEW_NOT_FINISHED` (feedback por questão já disponível no `GET`) |
| `limit` fora de 1–100 | `400` (`VALIDATION_ERROR` na borda) |

## Contrato de resposta

`ReviewSessionResponse` (criação e retomada):

- `sessionId`, `mode` (`REVISAO`), `status`, `startedAt`, `finishedAt`;
- `totalItems`, `answered`, `unanswered` (progresso **nesta sessão**);
- `items` — caderno congelado na ordem das posições, cada um com:
  - `position`, `questionId`, `disciplineCode/Name`, `sourceYear`,
    `sourceQuestionNumber`;
  - `topicCode/Name`, `reviewCategory`, `reason` — da fila vigente (NULL
    quando a questão sai do top-100 vigente; nunca motivo inventado);
  - `answered`, `selectedOption`, `isCorrect` — última tentativa **nesta
    sessão**, sempre revelados (REVISAO nunca oculta);
  - `wasAnnulled` — fora do aproveitamento (pontuação DESCONHECIDA,
    TASK 1.3 §4).
- `notes` — evidências, limitações e referências.

`ReviewSessionResultResponse` (após encerrar):

- `total/answered/unanswered/scored/correct/incorrect/annulled`, `accuracy`
  (NULL quando sem pontuáveis, nunca zero inventado);
- `items` — correção por posição (última resposta REVISAO por questão;
  não respondidas como pendentes, nunca erro inventado).

## Regras de evidência

* **Revisão não gera simulado (ERD §2.6):** aqui nasce `study_sessions`
  com `mode=REVISAO`, nunca `simulations`. A tabela nova
  `review_session_questions` (V5) referencia `study_sessions`
  (`ON DELETE CASCADE`) e congela só a ordem (posição + questão) — sem
  `frozen_answer_key` (diferença intencional para a TASK 5.1): a revisão
  responde via `POST /attempts`, cuja correção usa o gabarito vigente
  (TASK 3.7); congelar gabarito aqui divergiria do `is_correct`
  persistido no fato imutável.
* **Ordem congelada = top-N da fila no momento da criação:** mesmos
  filtros e mesma ordenação determinística da TASK 4.5 (erros persistentes
  e regressões primeiro; depois assuntos frágeis; por fim manutenção).
  Reuso por composição (`ReviewService.getQueue`), sem duplicar a lógica
  de baldes.
* **Feedback imediato:** `POST /attempts` com `mode=REVISAO` devolve
  `isCorrect` revelado (a ocultação da TASK 5.3 vale só para PROVA) e o
  `GET` desta sessão mostra `selectedOption`/`isCorrect` da última
  tentativa vinculada — vale a última por questão (maior `answeredAt`,
  desempate por maior `id`).
* **Fila vigente no GET/resultado:** categoria e motivo vêm do top-100
  vigente sem filtros; itens fora dele mantêm posição e progresso com
  nota de motivo indisponível. A leitura do caderno nunca quebra por
  causa do enriquecimento (fallback com nota).
* **Anuladas:** fora do aproveitamento (pontuação DESCONHECIDA, TASK 1.3
  §4); contam no resumo, nunca como acerto nem como erro.
* **Enunciados** não são duplicados no caderno: cada posição referencia
  `questionId` (resolve-se via `GET /api/v1/questions/{id}`, TASK 3.4).
* **Fora de escopo (não inventados):** simulado de edição real (TASK
  5.5), embaralhamento de alternativas, tempo limite por sessão (coluna
  inexistente no DDL — `NECESSITA REVISÃO` se o produto exigir),
  correção de discursiva, paginação por offset do caderno (ordem fixa
  1..N, sem N+1 relevante).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Modo Revisão`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-10-01)

* `mvn -f backend/pom.xml test` → **199/199** (187 anteriores + 12 novos:
  `ReviewSessionServiceTest` 7, `ReviewSessionControllerTest` 5).
* Casos cobertos: caderno congela top-N na ordem da fila; fila vazia `400
  NO_REVIEW_ITEMS`; progresso com `isCorrect` revelado em andamento
  (REVISAO nunca oculta, ao contrário da PROVA); sessão sem caderno `404
  REVIEW_SESSION_NOT_FOUND`; sessão alheia `404 SESSION_NOT_FOUND`;
  resultado em andamento `409 REVIEW_NOT_FINISHED`; resultado após
  `FINISHED` com última-tentativa-por-questão e `accuracy`; controller
  `401` sem token, `201` na criação (inclusive corpo `{}` com padrões) e
  `400` com `limit` fora de 1–100.
* `docker compose up -d --build app` → `healthy` (Flyway V5 aplicada:
  `review_session_questions` + trigger `updated_at`; valida o mapeamento
  JPA contra o schema real via `ddl-auto: validate`); sem token as 3
  rotas respondem `401` em envelope; `/v3/api-docs` lista os 3 paths
  novos (49 no total).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → sessão ESTUDO → 2 erros + 1 acerto → fila com 3 itens
  (`ERRO_SEM_ACERTO × 2` primeiro) → `POST /review/sessions {limit: 2}`
  `201` (caderno q2, q1 — ordem da fila) → resposta REVISAO correta
  (`isCorrect true` imediato) → `GET` (1/1 respondida/pendente) → `GET
  /result` `409 REVIEW_NOT_FINISHED` → `POST
  /study-sessions/{id}/finish` `200 FINISHED` → `GET /result` `200`
  (1/1, `accuracy 1.0`) → `onlyErrors` com 1 item restante; corpo `{}`
  cria com padrões. Scratch removido após (trigger de imutabilidade
  desligado só para o `DELETE` de limpeza e religado em seguida —
  tentativas 0, usuários 0, 240 questões preservadas; CI usa mocks, sem
  Docker).

## Notas técnicas (para não redescobrir)

* Pacote `br.com.voupassar.review` segue o padrão por domínio:
  `controller/` → `service/` → `repository/` (+ `dto/`, `entity/`);
  nenhuma regra em controller (AGENTS.md §19). `ReviewSessionService`
  compõe `ReviewService` (mão única, sem ciclo).
* `ReviewSessionQuestion` usa `@IdClass` (composta `study_session_id +
  position`), mesmo padrão de `SimulationQuestion` (TASK 5.1).
* `QuestionAttemptRepository.findByStudySessionIdAndUserId` (derivado) é
  a base do progresso e do resultado — mesmo padrão do finder por
  `simulationAttemptId` (TASK 5.1).
* `CreateReviewSessionRequest.onlyErrors` é `Boolean` (boxed): primitivo
  ausente no JSON quebra a desserialização do record com
  `MALFORMED_REQUEST` — o serviço normaliza `null → false`.
* `POST` aceita corpo ausente (`required = false` + padrão): `{}` cria
  com `limit` padrão 20, sem filtros.
* `liveMeta` faz `catch (RuntimeException)` → caderno sem categoria
  (nunca 500 por causa do enriquecimento); o `log.warn` registra a causa
  no servidor.
