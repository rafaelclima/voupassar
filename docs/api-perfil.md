# API de Perfil — TASK 3.6

> Perfil do aluno: dados, preferências, estatísticas, progresso e histórico —
> tudo escopado ao dono do token (Bearer). Quarta API real do backend, sobre
> `users` + `student_profiles` (escrita) e `question_attempts` +
> `student_topic_performance` (só leitura). A escrita de tentativas é a TASK
> 3.7; o cálculo de desempenho/roteiro é a Fase 4 — aqui só fatos registrados.

## Endpoints (todos autenticados)

| Rota | Descrição |
|---|---|
| `GET /api/v1/profile` | perfil completo: `{id, email, displayName, schoolYear, targetYear, studyGoal, roles}` |
| `PUT /api/v1/profile` | atualização completa: `{displayName!, schoolYear?, targetYear?, studyGoal?}` → `200` perfil |
| `GET /api/v1/profile/stats` | estatísticas + progresso: totais, `accuracy`, por modo, por disciplina, `topicProgress`, `notes` |
| `GET /api/v1/profile/history?page=&size=` | histórico paginado de tentativas (mais recentes primeiro), com proveniência mínima |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–3.4). Validação → `400 VALIDATION_ERROR` (detalhes por
campo); paginação fora de `[1, 100]` → `400`; conta/perfil inexistentes →
`404 USER_NOT_FOUND / PROFILE_NOT_FOUND`. Tudo no envelope
`{code, message, details, traceId, timestamp, path}`, sem stack trace;
`traceId` também no header `X-Trace-Id`.

## Regras de evidência

* **Perfil/preferências:** mesmos limites do registro (TASK 3.5) — nome
  `2–80` obrigatório; `schoolYear` até 40, `targetYear` `2000–2100`,
  `studyGoal` até 500, todos nuláveis. Em-branco vira NULL nos opcionais.
  E-mail e senha **não** mudam aqui (senha em `/auth/password/change`).
  `PUT` é completo: opcionais ausentes são gravados como NULL.
* **Estatísticas:** derivam só de `question_attempts` (fato imutável,
  trigger `trg_attempts_immutable`). Anuladas (`wasAnnulled=true`,
  `isCorrect` NULL) contam como conteúdo respondido e ficam **fora** do
  aproveitamento — regra de pontuação DESCONHECIDA (TASK 1.3 §4).
  Sem tentativas pontuáveis, `accuracy` sai NULL (nunca zero inventado) +
  nota explícita de DESCONHECIDO.
* **Progresso (`topicProgress`):** leitura direta de
  `student_topic_performance` (agregado escrito pelo job da TASK 4.1).
  Enquanto a 4.1 não existe a lista tende a vir vazia — o serviço sinaliza
  isso em `notes` em vez de inventar progresso.
* **Histórico:** página 0-based, ordem fixa (`answeredAt DESC, id DESC`),
  sem enunciado/alternativas/gabarito (isso é a TASK 3.4). Cada item traz
  `questionId`, ano-fonte, número, disciplina, `selectedOption`,
  `isCorrect` (NULL quando anulada), `mode`, tempo e vínculo de sessão/
  simulado quando houver.
* **Fora de escopo (não inventados):** diagnóstico, roteiro e recomendações
  (Fase 4); conquistas/metas (Fase 7 / TASK 6.8); correção de discursiva;
  preferências em JSONB (o ERD sugere JSONB validado, mas a V1 tem só
  `study_goal TEXT` — sem migração nesta task).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Perfil`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **104/104** (91 anteriores + 13 novos:
  `ProfileServiceTest` 7, `ProfileControllerTest` 6).
* `docker compose up -d --build app` → `healthy` (valida o mapeamento JPA
  contra o schema real via `ddl-auto: validate`); sem token as 4 rotas
  respondem `401` em envelope; `/v3/api-docs` lista os paths novos
  (`/profile`, `/profile/stats`, `/profile/history`).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → `GET /profile 200` (campos do registro) → `PUT 200` (nome +
  preferências) → `PUT {"displayName":""} → 400 VALIDATION_ERROR` →
  `GET /stats 200` zerado (`accuracy: null` + notas) →
  `GET /history 200` vazio → `size=101 → 400`.
* Agregados ao vivo com 3 tentativas inseridas via SQL (1 acerto + 1 erro +
  1 anulada 2020 Q7): `total 3, scored 2, correct 1, accuracy 0.5`,
  anulada fora do cálculo com nota, histórico ordenado com `isCorrect: null`
  na anulada; linha em `student_topic_performance` (PORCENTAGEM 10/6)
  aparece em `topicProgress` com `accuracy 0.6` e derruba a nota de
  "sem dados". Scratch removido após (trigger de imutabilidade desligado
  só para o `DELETE` de limpeza e religado em seguida — tentativas 0,
  usuários 0, 240 questões preservadas; CI usa mocks, sem Docker).

## Notas técnicas (para não redescobrir)

* `selected_option` é `CHAR(1)` no banco (`bpchar`): o JPA precisa de
  `@JdbcTypeCode(SqlTypes.CHAR)` para passar no `validate` (mesma regra da
  TASK 3.4 para `answer_key`). **Limitação conhecida da DDL:** o `CHECK`
  admite `'BLANK'` (5 chars), que não cabe em `CHAR(1)` — a escrita de
  `BLANK` exige migração corretiva antes da TASK 3.7.
* `accuracy` de `student_topic_performance` é coluna gerada: mapear com
  `insertable=false, updatable=false` (leitura como `BigDecimal`).
* `question_attempts` é imutável por trigger: nem `DELETE` de limpeza passa
  com o trigger ligado (inclui o `SET NULL` do `ON DELETE SET NULL` de
  `study_sessions`). Limpeza scratch exige `DISABLE TRIGGER` temporário +
  `ENABLE` imediato — nunca em código da API.
* Camadas: `profile/controller → service → repository` (+ `dto/`,
  `entity/`); entidades `User`, `StudentProfile`, `Question`, `Discipline`,
  `Topic`, `Subtopic` reutilizadas dos pacotes existentes, sem duplicar
  mapeamento; `PageResponse` reutilizado de `questions.dto`; nenhuma regra
  em controller (AGENTS.md §19).
* Pacote novo `br.com.voupassar.profile` segue o padrão por domínio
  (`exams`, `content`, `questions`, `auth`). Sem migração nova: a V1 já tem
  todas as tabelas lidas/escritas aqui.
