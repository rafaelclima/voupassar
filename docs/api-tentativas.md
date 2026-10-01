# API de Tentativas — TASK 3.7

> Escrita do fato de resposta: questão, resposta, resultado calculado no
> servidor, tempo, data, modo e vínculo (sessão ou simulado). Quinta API real
> do backend, sobre `question_attempts` (escrita) + `study_sessions`
> (abrir/consultar/encerrar) e validação do vínculo opcional com
> `simulation_attempts` (só leitura — a execução de simulados é a Fase 5).
> Leitura agregada continua em `/profile/stats` e `/profile/history`
> (TASK 3.6); cálculo de desempenho/roteiro é a Fase 4.

## Endpoints (todos autenticados)

| Rota | Descrição |
|---|---|
| `POST /api/v1/study-sessions` | abrir sessão: `{mode!}` → `201` `{id, mode, status: IN_PROGRESS, startedAt, finishedAt: null}` |
| `GET /api/v1/study-sessions/{id}` | consultar sessão do dono do token |
| `POST /api/v1/study-sessions/{id}/finish` | encerrar: `IN_PROGRESS → FINISHED` (carimba `finishedAt`) |
| `POST /api/v1/attempts` | registrar tentativa: `{questionId!, selectedOption!, mode!, timeSpentSeconds?, studySessionId?, simulationAttemptId?}` → `201` com correção do servidor |
| `GET /api/v1/attempts/{id}` | consultar tentativa do dono do token |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–3.6). Validação → `400 VALIDATION_ERROR` (detalhes por
campo) ou códigos de negócio (`ATTEMPT_LINK_REQUIRED`, `INVALID_OPTION`,
`INVALID_MODE`, `MODE_MISMATCH`); questão/sessão/simulado/tentativa
inexistentes **ou de outro aluno** → `404 QUESTION/SIMULATION_ATTEMPT/
SESSION/ATTEMPT_NOT_FOUND` (sem distinguir, anti-enumeração); sessão/
simulado já encerrados → `409 SESSION/SIMULATION_CLOSED`. Tudo no envelope
`{code, message, details, traceId, timestamp, path}`, sem stack trace;
`traceId` também no header `X-Trace-Id`.

## Regras de evidência

* **Correção no servidor:** o cliente nunca informa `isCorrect`,
  `wasAnnulled` nem `answeredAt`. `wasAnnulled = (answer_key = 'X')`,
  `isCorrect = NULL` quando anulada e `selected = answer_key` caso
  contrário; `answeredAt = now()` do servidor. Anuladas contam como
  conteúdo respondido e ficam **fora** do aproveitamento — regra de
  pontuação DESCONHECIDA (TASK 1.3 §4), com nota explícita em `notes`.
* **BLANK:** resposta em branco (`A/B/C/D/BLANK`, case-insensitive
  normalizado para maiúsculas). Em questão pontuável conta como **erro**,
  nunca como acerto — com nota explícita. Exigia a migração **V4**
  (`selected_option CHAR(1) → TEXT`, mesmo `CHECK`): o `CHAR(1)` da V1 não
  comportava os 5 chars de `BLANK` (limitação documentada na TASK 3.6).
* **Vínculo obrigatório:** `studySessionId` ou `simulationAttemptId`
  (ERD §5 item 6 — resposta órfã proibida; ambos aceitos, ao menos um).
  Cada vínculo é validado por posse (dono do token), status `IN_PROGRESS`
  e coerência de modo com a tentativa (`MODE_MISMATCH` se divergir).
  Simulado: só valida existência/posse/status/modo — criar, submeter e
  congelar caderno é a Fase 5.
* **Resultado devolvido, salvo em prova em andamento** (TASK 3.7 + TASK 5.3):
  em `PROVA` com execução/sessão ainda `IN_PROGRESS`, `POST /attempts`
  persiste a correção mas devolve `isCorrect: null` + nota de gabarito
  oculto (AGENTS.md §9); `GET /attempts/{id}` aplica a mesma regra (sem
  atalho via GET); após encerrar revela normalmente. Ver
  `docs/api-modo-prova.md`.
* **Imutabilidade:** `question_attempts` tem trigger anti-UPDATE/DELETE —
  sem PUT nem DELETE; correção só via nova tentativa.
* **Fora de escopo (não inventados):** pausar/retomar sessão, correção de
  discursiva, agregados por conteúdo (Fase 4 / TASK 4.1), diagnóstico,
  roteiro e recomendações (Fase 4); simulados completos (Fase 5);
  conquistas/metas (Fase 7). `publication_status`/`validation_status` da
  questão **não** bloqueiam resposta (todas as 240 importadas estão
  `PENDENTE_REVISAO` — bloquear zeraria o MVP; a curadoria é a TASK 12.2).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tags `Tentativas` e
`Sessões de estudo`; escopo `/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **125/125** (104 anteriores + 21 novos:
  `AttemptServiceTest` 13, `AttemptControllerTest` 4,
  `StudySessionControllerTest` 4).
* `docker compose up -d --build app` → `healthy` (Flyway V4 aplicada:
  `selected_option` agora `TEXT` com o mesmo `CHECK`; valida o mapeamento
  JPA contra o schema real via `ddl-auto: validate`); sem token as 5 rotas
  respondem `401` em envelope; `/v3/api-docs` lista os 5 paths novos
  (31 no total).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → sessão `201 IN_PROGRESS` → tentativa correta Q1=A
  (`isCorrect: true`) → errada Q2=A (`false`) → em branco Q2=BLANK
  (`false` + nota) → anulada Q7 (`wasAnnulled: true, isCorrect: null` +
  nota) → órfã `400 ATTEMPT_LINK_REQUIRED` → questão inexistente `404` →
  modo divergente `400 MODE_MISMATCH` → `GET /profile/stats 200`
  (`total 4, scored 3, correct 1, accuracy 1/3`, anulada fora) →
  encerrar sessão `200 FINISHED` → tentativa em sessão encerrada `409
  SESSION_CLOSED`. Scratch removido após (trigger de imutabilidade
  desligado só para o `DELETE` de limpeza e religado em seguida —
  tentativas 0, usuários 0, 240 questões preservadas; CI usa mocks, sem
  Docker).

## Notas técnicas (para não redescobrir)

* Pacote novo `br.com.voupassar.attempts` segue o padrão por domínio
  (`exams`, `content`, `questions`, `auth`, `profile`): `controller/` →
  `service/` → `repository/` (+ `dto/`, `entity/`); nenhuma regra em
  controller (AGENTS.md §19).
* `QuestionAttempt` (em `profile.entity`) ganhou setters só para a
  construção do INSERT + mapeamento `selected_option` sem `CHAR`
  (pós-V4); `StudySession` é entidade nova, `SimulationAttemptRef` é
  `@Immutable` (só valida vínculo da Fase 5).
* Testes com mocks precisam simular o ID gerado no `save` (`thenAnswer` +
  `ReflectionTestUtils.setField(…, "id", …)`), senão o record de resposta
  (`long id`) estoura NPE no unboxing — o banco real gera via IDENTITY.
* Normalização: `mode`/`selectedOption` aceitam minúsculas
  (`trim().toUpperCase()`) antes da validação de conjunto — evita `400`
  em `"c"` vs `"C"`, mantendo o domínio estrito e documentado.
