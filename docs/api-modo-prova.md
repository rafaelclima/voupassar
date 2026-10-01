# API do Modo Prova — TASK 5.3 (ocultação durante a execução + correção ao final)

> Sensação de prova de verdade: durante a execução o resultado e o gabarito
> ficam ocultos; ao final, correção completa, estatísticas e análise.
>
> Cobre `POST /api/v1/attempts` e `GET /api/v1/attempts/{id}` (resultado
> oculto em `PROVA` com execução/sessão ainda `IN_PROGRESS`) e `GET
> /api/v1/questions` + `GET /api/v1/questions/{id}` (gabarito/explicação
> ocultos quando a questão está em simulado `PROVA` ainda `IN_PROGRESS`
> deste aluno). A correção ao final reutiliza o placar do servidor da TASK
> 5.1 (`POST /submit`, `POST /abandon`, `GET /result`) e o feedback por
> posição da TASK 5.2 (liberado para `PROVA` só após encerrar).

## Comportamento (autenticado, dono do token)

| Rota | Durante `PROVA IN_PROGRESS` | Após encerrar |
|---|---|---|
| `POST /api/v1/attempts` | `201` com `isCorrect: null` + nota de gabarito oculto (valor correto persistido no fato imutável) | — (nova resposta após encerrar: `409 SIMULATION_CLOSED` / `SESSION_CLOSED`, regra TASK 3.7) |
| `GET /api/v1/attempts/{id}` | `200` com `isCorrect: null` + nota de oculto (sem atalho via GET) | `200` com `isCorrect` revelado + nota de execução encerrada |
| `GET /api/v1/questions/{id}` | `200` com `answerKey: null`, `explanation: null` + nota de gabarito oculto (enunciado/alternativas/proveniência preservados) | `200` integral |
| `GET /api/v1/questions?...` | mesmos itens mascarados só nas questões do caderno em prova (1 query extra, sem N+1) | `200` integral |
| `GET /api/v1/simulations/attempts/{id}` | caderno só com `answered` (regra TASK 5.1, inalterada) | caderno com gabarito congelado + resumo (inalterado) |
| `GET /api/v1/simulations/attempts/{id}/result` | `409 SIMULATION_NOT_FINISHED` (inalterado) | `200` correção por posição + totais + `accuracy` (inalterado) |
| `GET /api/v1/simulations/attempts/{id}/feedback/{position}` | `409 STUDY_FEEDBACK_UNAVAILABLE` (regra TASK 5.2, inalterada) | `200` acerto/erro + resposta correta + explicação + assunto (inalterado) |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–5.2). Vínculo inexistente **ou de outro aluno** → `404`
sem distinguir (anti-enumeração). Tudo no envelope `{code, message, details,
traceId, timestamp, path}`, sem stack trace; `traceId` também no header
`X-Trace-Id`.

## Contrato de ocultação

`AttemptResponse` em prova em andamento:

- `isCorrect: null` (oculto — nunca `false` inventado como "errou" nem `true`
  como "acertou");
- `wasAnnulled` segue verídico (anulada não tem resposta correta a vazar;
  além disso anuladas ficam fora da seleção da TASK 5.1);
- `notes` com "resultado oculto durante a execução … conclua ou abandone …".

`QuestionResponse` em prova em andamento:

- `answerKey: null`, `explanation: null` (omitidos no JSON via `NON_NULL`);
- `statement`, `options`, `discipline`, proveniência, classificação e
  `annulled` preservados (nada inventado, nada removido além do gabarito);
- `notes` com "Gabarito oculto durante a execução no Modo Prova …".

## Regras de evidência

* **Persistência verídica:** a ocultação é só na resposta JSON. O fato
  `question_attempts` guarda `is_correct`/`was_annulled` calculados no
  servidor (TASK 3.7) — o placar do simulado (TASK 5.1) e o `GET` após
  encerrar revelam o valor persistido, nunca um valor recalculado na hora.
* **Sem atalho:** `GET /attempts/{id}` aplica a mesma regra do `POST`
  (oculto quando ao menos um vínculo `PROVA` segue `IN_PROGRESS`; revela
  quando todos encerraram). Questão fora de prova em andamento nunca é
  mascarada (nunca inventar prova sem evidência de `simulation_questions` ×
  `simulation_attempts IN_PROGRESS PROVA` do dono).
* **Correção ao final (reuso, sem endpoint novo):** totais +
  `accuracy` (NULL quando sem pontuáveis, nunca zero inventado) em
  `submit/abandon/result`; correção por posição contra o gabarito
  **congelado**; explicação só quando redigida (NULL = `NECESSITA REVISÃO`,
  nunca texto gerado); assunto da classificação vigente (revisão PENDENTE,
  TASK 12.2 — nunca verdade oficial do IFRN); não respondidas como
  pendentes (nunca erro inventado); anuladas fora do aproveitamento
  (pontuação DESCONHECIDA, TASK 1.3 §4).
* **Fora de escopo (não inventados):** simulado de edição real (TASK 5.5),
  experiência dedicada de revisão (TASK 5.4, já existe a fila em
  `/review/queue`), correção de discursiva, tempo limite por simulado
  (coluna inexistente no DDL), embaralhamento de alternativas, bloqueio de
  `profile/stats` durante a prova (o placar da execução segue oculto no
  `GET` do simulado; agregados globais continuam ao vivo por decisão da
  TASK 3.7/4.1 — documentado como limitação, não como correção silenciosa).

## OpenAPI

Sem path novo (só mudança de conteúdo em respostas existentes). Contrato em
`GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas públicas — só
schemas, sem PII). Tags `Tentativas`, `Questões` e `Simulados`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-10-01)

* `mvn -f backend/pom.xml test` → **187/187** (180 anteriores + 7 novos:
  `AttemptServiceTest` 2, `QuestionServiceTest` 3, `QuestionControllerTest` 2).
* Casos cobertos: POST PROVA persiste `true` no fato mas devolve `null` +
  nota de oculto; GET durante a prova segue oculto e após `SUBMITTED`
  revela; ESTUDO continua imediato; questão em prova sai com `answerKey`/
  `explanation` nulos + nota e fora de prova revela; lista mascara só as
  posições em prova (1 query, sem N+1); controller `401` sem token.
* Sem migração nova: só leitura extra sobre `simulation_questions` ×
  `simulation_attempts` + mascaramento em memória; valida o mapeamento JPA
  contra o schema real via `ddl-auto: validate`.

## Notas técnicas (para não redescobrir)

* `AttemptService` ganhou `toAttempt(a, hidden)` + `isHiddenDuringProva`
  (oculto quando ao menos um vínculo `PROVA` segue `IN_PROGRESS`; `POST`
  sempre oculta em `PROVA` porque só aceita vínculo `IN_PROGRESS`).
* `SimulationQuestionRepository` ganhou `existsInProvaInProgress` e
  `findInProvaInProgress` (JPQL `SimulationQuestion × SimulationAttempt`,
  sem entidade nova — `simulationAttemptId` é coluna solta, o join é por
  valor).
* `QuestionService` ganhou `searchForUser`/`getByIdForUser` + `maskForProva`
  (catchall em `try/catch` → sem ocultação quando a sonda falha: nunca
  quebrar leitura por causa da máscara). Construtor de 8 args leva
  `@Autowired` (dois construtores exigem a anotação explícita — mesmo
  padrão do `SimulationService`, TASK 5.1).
* `QuestionController` agora extrai `UserPrincipal` (com
  `AuthenticationPrincipalArgumentResolver` nos testes slice) e chama as
  variantes `ForUser`; sem principal → `401` (secure-by-default).
