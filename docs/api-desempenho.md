# API de Desempenho — TASK 4.1

> Cálculo determinístico do desempenho a partir do fato imutável
> `question_attempts`: acerto geral, por disciplina, por assunto, por
> subassunto e evolução temporal — mais o rebuild idempotente do agregado
> `student_topic_performance`. Primeira API do motor educacional (Fase 4);
> diagnóstico/roteiro/recomendação ficam para as TASKs 4.2–4.4.

## Endpoints (todos autenticados)

| Rota | Descrição |
|---|---|
| `GET /api/v1/performance/overview` | retrato: totais, `accuracy`, por disciplina/assunto/subassunto, `unclassifiedAttempts`, `notes` |
| `GET /api/v1/performance/evolution?granularity=WEEK` | série temporal: baldes `DAY`/`WEEK` (segunda-feira)/`MONTH` (dia 1) em UTC, só baldes com tentativas |
| `POST /api/v1/performance/rebuild` | reconstrói `student_topic_performance` (delete + insert idempotente) → `{rebuiltRows, rebuiltAt}` |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–3.7). Granularidade inválida → `400 INVALID_GRANULARITY`;
conta inexistente → `404 USER_NOT_FOUND`. Tudo no envelope
`{code, message, details, traceId, timestamp, path}`, sem stack trace;
`traceId` também no header `X-Trace-Id`.

## Regras de evidência

* **Fonte única:** só `question_attempts` (fato imutável, trigger
  `trg_attempts_immutable`) + a classificação vigente não-rejeitada mais
  recente por questão. Nenhum número inventado: sem pontuáveis, `accuracy`
  sai NULL com nota explícita de DESCONHECIDO.
* **Anuladas:** contam como conteúdo respondido e ficam fora do
  aproveitamento — regra de pontuação DESCONHECIDA (TASK 1.3 §4), com nota
  explícita. No overview cada recorte traz `attempts` (total) vs `scored`
  (pontuáveis); no materializado só pontuáveis (`attempts=scored`,
  `hits=correct`) para que a coluna gerada `accuracy=hits/attempts` coincida
  com o ao vivo — limitação documentada (anuladas por conteúdo visíveis no
  overview, não no materializado).
* **Por disciplina (factual):** `questions.discipline_id` — LÍNGUA_PORTUGUESA
  / MATEMATICA observadas nas provas.
* **Por assunto/subassunto (derivado):** vigente `status <> 'REJECTED'`
  (hoje tudo `PENDING`, revisão humana PENDENTE — TASK 12.2; nunca verdade
  oficial do IFRN). Sem vigente, a tentativa conta no geral/disciplina e em
  `unclassifiedAttempts`, nunca em tópico inventado (nota `NECESSITA
  REVISÃO`). Linha de subassunto só quando a vigente tem subassunto; a de
  tópico agrega tudo do assunto. Ordenação: piores aproveitamentos primeiro
  (NULLs por último), desempate por código.
* **Evolução:** baldes de calendário UTC (`DAY` / `WEEK` segunda-feira ISO /
  `MONTH` dia 1), ordem cronológica, só baldes com tentativas, sem
  interpolação de lacunas. Limitação: o dia local do estudante pode divergir
  em horas (sem fuso por aluno no MVP).
* **Rebuild:** delete + insert na mesma transação a partir do fato (reexecução
  segura). Disparado automaticamente a cada `POST /attempts` (mesma
  transação — overview ao vivo e materializado nunca divergem) e sob demanda
  em `POST /performance/rebuild`. `GET /profile/stats` (TASK 3.6) continua
  lendo o materializado em `topicProgress` — a nota de "sem dados até a 4.1"
  cai quando há linhas.
* **Fora de escopo (não inventados):** diagnóstico, nível de domínio,
  roteiro e recomendações (TASKs 4.2–4.4); snapshots periódicos
  (`progress_snapshots` segue para otimização de gráficos, fora do MVP);
  calibração de dificuldade (segue palpite BAIXA); correção de discursiva;
  conquistas/metas (Fase 7).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Desempenho`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **137/137** (125 anteriores + 12 novos:
  `PerformanceServiceTest` 8, `PerformanceControllerTest` 4).
* `docker compose up -d --build app` → `healthy` (valida o mapeamento JPA
  contra o schema real via `ddl-auto: validate`); sem token as 3 rotas
  respondem `401` em envelope; `/v3/api-docs` lista os 3 paths novos
  (34 no total).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → overview zerado (`accuracy: null`) → sessão `201 IN_PROGRESS` →
  tentativa correta Q1=A (`isCorrect: true`) → errada Q2=A (`false`) →
  anulada Q7 (`wasAnnulled: true, isCorrect: null`) → overview `total 3,
  scored 2, correct 1, accuracy 0.5` com `byDiscipline/byTopic/bySubtopic`
  preenchidos e `unclassified 0` → `GET /profile/stats 200` com
  `topicProgress` populado (rebuild automático) → evolution `DAY` com 1
  balde (`attempts 3, scored 2, correct 1`) → `POST /rebuild 200`
  (idempotente, mesmas linhas) → evolução com granularidade `YEAR` `400
  INVALID_GRANULARITY`. Scratch removido após (trigger de imutabilidade
  desligado só para o `DELETE` de limpeza e religado em seguida —
  tentativas 0, usuários 0, 240 questões preservadas; CI usa mocks, sem
  Docker).

## Notas técnicas (para não redescobrir)

* Pacote novo `br.com.voupassar.performance` segue o padrão por domínio
  (`controller/` → `service/` → `dto/`; repositórios continuam em
  `profile.repository` + `content.repository`, sem duplicar mapeamento;
  nenhuma regra em controller — AGENTS.md §19).
* Leitura base `findAllByUserIdWithQuestion` (fetch questão + disciplina,
  ordem cronológica, sem N+1) + `findActiveByQuestionIds` (vigente =
  primeira por questão na ordem `id DESC`). Agregações em Java (O(N) por
  aluno) — suficiente para o MVP; otimização SQL futura sem mudar o
  contrato.
* `StudentTopicPerformance` ganhou setters só para a construção do INSERT
  (mesmo padrão de `QuestionAttempt` na TASK 3.7); `accuracy` continua
  `insertable=false, updatable=false` (coluna gerada).
* `deleteByUserId` precisa de `@Modifying` (delete derivado); o rebuild roda
  na mesma transação do `POST /attempts` (join automático) — se o rebuild
  falhar, a tentativa falha junto (visível, nunca divergência silenciosa).
* Testes com mocks: classificar exige `Topic.discipline` e
  `Subtopic.topic.discipline` preenchidos via `ReflectionTestUtils`, senão
  NPE ao montar `byTopic/bySubtopic` — o banco real traz via lazy dentro da
  transação.
* Rebuild usa bulk `DELETE` explícito (`@Query`), nunca `deleteBy` derivado:
  o derivado carrega as linhas e agenda `remove()`, e o flush ordena INSERTs
  antes de DELETEs — violação da UNIQUE `(user, topic, subtopic)` no segundo
  rebuild. Sem `clearAutomatically`: o rebuild roda na mesma transação do
  `POST /attempts` e limpar o contexto descartaria o INSERT da tentativa
  pendente (verificado ao vivo: segundo `POST /attempts` dava 500 antes do
  fix).
