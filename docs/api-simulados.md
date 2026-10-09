# API de Simulados — TASK 5.1 (por disciplina) + nota multi-processo E.1

> Primeira API real da Fase 5. Cria e executa simulados do tipo
> `BY_DISCIPLINE` sobre `simulations` (definição) + `simulation_attempts`
> (execução) + `simulation_questions` (caderno congelado), com respostas no
> fato imutável `question_attempts` (TASK 3.7) e placar calculado no servidor.
>
> Permite: escolher disciplina, quantidade e dificuldade; iniciar; retomar
> (pausa = manter `IN_PROGRESS`); concluir; abandonar; visualizar resultado.
> O simulado de edição real é a TASK 5.5 (`docs/api-simulado-edicao.md`,
> com `(institution,year)` na E.1).
>
> Nota E.1: `POST /by-discipline` segue sem filtro `institution` (candidatas
> de ambas — IFRN+EAJ — quando a disciplina existe nos dois; CN/CH só têm
> EAJ). O recorte por processo chega no frontend F.1 / trilha E.2; o
> caderno/respostas já carregam `institution` por posição para distinguir
> EAJ-2022 ≠ IFRN-2022.

## Endpoints (todos autenticados)

| Rota | Descrição |
|---|---|
| `POST /api/v1/simulations/by-discipline` | criar e iniciar: `{disciplineCode!, questionCount! 1–100, difficulty?, mode! ESTUDO/PROVA}` → `201` com caderno congelado (gabarito oculto) |
| `GET /api/v1/simulations/attempts?page=&size=` | listar execuções do dono (mais recentes primeiro, `page` 0-based, `size` 1–100) |
| `GET /api/v1/simulations/attempts/{id}` | consultar execução: em andamento (só progresso) ou encerrada (gabarito + resumo) |
| `POST /api/v1/simulations/attempts/{id}/submit` | concluir: `IN_PROGRESS → SUBMITTED` com placar do servidor |
| `POST /api/v1/simulations/attempts/{id}/abandon` | desistir: `IN_PROGRESS → ABANDONED` com placar parcial |
| `GET /api/v1/simulations/attempts/{id}/result` | visualizar resultado (só após encerrar) |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–4.5). Disciplina inexistente → `404
DISCIPLINE_NOT_FOUND`; execução inexistente **ou de outro aluno** → `404
SIMULATION_ATTEMPT_NOT_FOUND` (sem distinguir, anti-enumeração); quantidade
acima do disponível → `400 INSUFFICIENT_QUESTIONS` com o disponível
explícito (nunca redução silenciosa); modo/dificuldade/quantidade
malformados → `400` (`VALIDATION_ERROR` na borda, `INVALID_MODE` /
`INVALID_DIFFICULTY` / `INVALID_QUESTION_COUNT` no serviço); encerrar
execução já encerrada → `409 SIMULATION_CLOSED`; ver resultado em andamento
→ `409 SIMULATION_NOT_FINISHED`. Tudo no envelope `{code, message, details,
traceId, timestamp, path}`, sem stack trace; `traceId` também no header
`X-Trace-Id`.

## Regras de evidência

* **Seleção:** amostra aleatória simples sem reposição (`SecureRandom`) sobre
  as candidatas elegíveis (disciplina + filtro opcional de dificuldade
  + filtro de origem TASK 15.4, **não-anuladas**). Origem ausente =
  `OFFICIAL` (padrão); `ALL` = sem filtro (caderno misto); demais valores
  seguem `questions.source_type` (inválido → `400 INVALID_SOURCE_TYPE`).
  A origem efetiva fica registrada em `simulations.filter_json.sourceType`.
  Anuladas ficam fora da seleção: com `isCorrect` NULL quebrariam o aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4). O
  recorte por dificuldade herda a limitação da estimativa (palpite com
  confiança BAIXA global — questões sem estimativa saem só no recorte sem
  filtro). A ordem sorteada vira a ordem do caderno (posições 1..N).
* **Caderno congelado:** cada posição grava `question_id` + `frozen_answer_key`
  (gabarito vigente na criação). A correção confronta a resposta do aluno com
  o **congelado**, nunca com o gabarito "atual" — protege contra
  reclassificação posterior.
* **Correção no servidor:** o cliente nunca escreve nota. Vale a **última**
  tentativa por questão vinculada à execução (maior `answeredAt`, desempate
  por maior `id`); `score_json` é carimbado ao encerrar. Questões sem resposta
  contam como não respondidas (nunca como erro inventado); `accuracy` NULL =
  sem pontuáveis (nunca zero inventado).
* **Gabarito oculto em andamento:** `GET` com `IN_PROGRESS` devolve posições
  com `answered` (progresso) mas sem `frozenAnswerKey`/`selectedOption`/
  `isCorrect` — preserva a sensação de prova nos dois modos. No Modo Estudo
  o feedback imediato vem de `POST /attempts` (TASK 3.7); no Modo Prova a
  correção aparece só após concluir (base da TASK 5.3; o detalhamento por
  modo é as TASKs 5.2–5.4).
* **Pausa:** não existe estado de pausa no DDL (só
  `IN_PROGRESS/SUBMITTED/ABANDONED`) — "pausar" é manter `IN_PROGRESS` e
  retomar via `GET`; abandonar é a desistência explícita (placar parcial).
* **Respostas** continuam via `POST /attempts` com `simulationAttemptId`
  (TASK 3.7, sem mudança); após encerrar, novas respostas retornam `409
  SIMULATION_CLOSED` (validação já existente).
* **Fora de escopo (não inventados):** simulado de edição real (TASK 5.5),
  feedback imediato dedicado (TASK 5.2), regras do Modo Prova (TASK 5.3),
  revisão dedicada (TASK 5.4, já existe a fila em `/review/queue`),
  correção de discursiva, tempo limite por simulado (coluna inexistente no
  DDL — `NECESSITA REVISÃO` se o produto exigir), embaralhamento de
  alternativas (só a ordem das questões é sorteada).
* **Enunciados** não são duplicados no caderno: cada posição referencia
  `questionId` (resolve-se via `GET /api/v1/questions/{id}`, TASK 3.4).

## Ritmo em simulado — só exibição (TASK 21.1, decisão 2026-10-07)

> `time_spent_seconds` (por tentativa, `POST /attempts`) e `answeredAt`
> continuam sendo só fato de auditoria. Nenhum score, ranking, desempenho,
> diagnóstico, recomendação, roteiro ou revisão usa tempo — `rg timeSpent
> backend/.../performance backend/.../diagnosis backend/.../studyplan
> backend/.../review` permanece vazio. O ritmo é cronômetro informativo
> 100% no frontend (`frontend/js/components/pace.js` + `#sim-pace` em
> `simulado.html`), idêntico em ESTUDO e PROVA.

* Referência por edição = duração oficial da capa naquela edição
  (`docs/provas-inventario.md §1`: 2020, 2022, 2023, 2024, 2025 e 2026
  declaram 4h) ÷ total de objetivas do caderno. Ano fora desse mapa ou
  simulado por disciplina (recorte sem tempo oficial) = `DESCONHECIDO` com
  o aviso "ritmo informativo, sem tempo oficial confirmado" — nunca inventar.
* Exibição: "Questão i/N · HH:MM:SS decorridos · sua média X min/questão ·
  referência da edição YYYY: Y min/questão" (ou o aviso honesto). Encerrada
  congela no `submittedAt`.

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Simulados`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-10-01)

* `mvn -f backend/pom.xml test` → **169/169** (153 anteriores + 16 novos:
  `SimulationServiceTest` 12, `SimulationControllerTest` 4).
* `docker compose up -d --build app` → `healthy` (sem migração nova: as
  tabelas `simulations`/`simulation_attempts`/`simulation_questions` já
  existem na V1; valida o mapeamento JPA contra o schema real via
  `ddl-auto: validate`); sem token as 6 rotas respondem `401` em envelope;
  `/v3/api-docs` lista os 6 paths novos (45 no total).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → simulado `MATEMATICA × 5 PROVA` `201 IN_PROGRESS` (5 posições,
  gabarito oculto) → `GET` retomada (mesmo caderno, `answered: false`) →
  3 respostas via `POST /attempts` (`simulationAttemptId`) → `GET /result`
  `409 SIMULATION_NOT_FINISHED` → `POST /submit` `200 SUBMITTED`
  (placar 2/3, 2 não respondidas como pendentes, `accuracy` sobre as
  pontuáveis) → nova resposta `409 SIMULATION_CLOSED` → segundo simulado +
  `POST /abandon` `200 ABANDONED` (parcial) → `GET /attempts` `200`
  (2 execuções, mais recente primeiro). Scratch removido após (trigger de
  imutabilidade desligado só para o `DELETE` de limpeza e religado em
  seguida — tentativas 0, usuários 0, 240 questões preservadas; CI usa
  mocks, sem Docker).

## Notas técnicas (para não redescobrir)

* Pacote novo `br.com.voupassar.simulations` segue o padrão por domínio:
  `controller/` → `service/` → `repository/` (+ `dto/`, `entity/`); nenhuma
  regra em controller (AGENTS.md §19).
* `SimulationAttempt` (escrita) e `SimulationAttemptRef` (pacote `attempts`,
  `@Immutable`, TASK 3.7) mapeiam a mesma tabela sem conflito: esta escreve
  o ciclo da Fase 5, aquela só valida o vínculo da tentativa. O construtor
  de teste com `Random` injetável é package-private; o público leva
  `@Autowired` (sem ele o Spring falha com "No default constructor found" —
  dois construtores exigem a anotação explícita).
* `filter_json`/`score_json` usam `@JdbcTypeCode(SqlTypes.JSON)` sobre
  `String` (mesmo padrão de `evidence_json`, TASK 4.3); `frozen_answer_key`
  usa `SqlTypes.CHAR` (mesmo padrão de `answer_key`, TASK 3.4).
* `SimulationQuestion` usa `@IdClass` (composta `attempt_id + position`);
  `SimulationQuestionRepository` tem o finder `In` para a lista paginada
  (cadernos da página em 1 query, sem N+1).
* `QuestionAttemptRepository.findBySimulationAttemptIdAndUserId` (derivado)
  é a base do placar e das flags de progresso.
