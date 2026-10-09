# API de Questões — TASK 3.4

> Terceira API real do backend. Somente leitura sobre `questions` +
> `question_options` (conteúdo integral: enunciado, alternativas, gabarito)
> com proveniência (`source_type/year/number`, páginas, disciplina, edição) e
> classificação vigente (mais recente por questão, taxonomia v1.1).
> Filtros por disciplina, assunto, subassunto, edição, dificuldade e origem +
> paginação. Crédito + página sempre presentes; textos de terceiros seguem
> com fonte e takedown reativo (decisão de produto 2026-10-06).

## Endpoints (todos autenticados até a TASK 3.5 emitir tokens)

| Rota | Descrição |
|---|---|
| `GET /api/v1/questions?disciplineCode=&topicId=&subtopicId=&year=&institution=&difficulty=&sourceType=&page=&size=` | listar paginado (ordem fixa: ano-fonte, número, id): enunciado + alternativas + gabarito + proveniência (`institution` E.1) + assunto + notas de evidência. `year` sem `institution` = ambas (EAJ-2022+IFRN-2022); com `institution` = recorte `(institution,year)` |
| `GET /api/v1/questions/{id}` | detalhe integral da questão (com `institution` da edição-fonte; `AUTHORAL` = nulo) |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default).
Filtros para disciplina/assunto/subassunto/edição inexistentes →
`404 {"code":"DISCIPLINE/TOPIC/SUBTOPIC/EDITION_NOT_FOUND",…}` (nunca página
vazia silenciosa); `IFRN 2021` registra a ausência e aponta EAJ-2021;
`EAJ 2023` registra que EAJ possui só 2021/2022/2025. Questão inexistente →
`404 {"code":"QUESTION_NOT_FOUND",…}`. Filtro/paginação malformados → `400`
(`page` 0-based; `size` em `[1, 100]`; `difficulty` em
`FACIL/MEDIA/DIFICIL`; `sourceType` em
`OFFICIAL/AUTHORAL/ADAPTED/INTERNAL_REVIEW/EXPERIMENTAL`; `institution` em
`IFRN/EAJ`).

## Regras de evidência

* `OFFICIAL` = prova real (IFRN **ou** EAJ/UFRN — ver `institution` na E.1);
  qualquer outro `sourceType` nunca é oficial e sai identificado como tal
  (AGENTS.md §11). IFRN: 240 `OFFICIAL` (40 × 6 edições, TASK 2.3);
  EAJ: 128 `OFFICIAL` (D.3: 50/40/38 + Q23-2025 X; Q22/Q39-2025 excluídas) +
  60 `AUTHORAL` (lote piloto, TASK 15.2/15.3).
* Toda questão traz **nota fixa de origem** em `notes[]` (TASK 15.4 + E.1):
  oficial cita processo e edição (`Questão oficial do IFRN (edição 2026).` /
  `Questão oficial do EAJ/UFRN (edição 2022).`); autoral declara-se
  (`Questão autoral criada pelo VouPassar… — não é uma questão oficial do
  IFRN nem do EAJ/UFRN.`). É proveniência, não gabarito: segue visível no
  Modo Prova. Resposta `institution` = processo da edição-fonte (nulo fora de
  `OFFICIAL`).
* `answerKey = "X"` ⟺ anulada (5 no banco): sai normalmente, conta como
  conteúdo que apareceu na prova, nunca pontua aqui; cada item anulado traz
  a nota explícita (regra de pontuação DESCONHECIDA, TASK 1.3 §4).
* `difficultyEstimate` é palpite com confiança BAIXA global (sem calibração
  — Fase 4); confiança BAIXA = assunto NÃO CONFIRMADO — tudo
  sinalizado no array `notes` de cada questão, nunca omitido.
* Questão sem classificação vigente sai com `topic/subtopic` nulos +
  nota de assunto NÃO CONFIRMADO (nunca com assunto inventado).
* `hasFigure=true` indica figura no PDF-fonte (ver páginas; texto extraído
  pode estar ilegível — risco assumido em `docs/blockers.md`).
* Ordem fixa e determinística (ano-fonte, número, id): sem ordenação por
  relevância sem algoritmo auditável (Fase 4).
* Origem válida sem linhas (ex. `sourceType=AUTHORAL` hoje) = página vazia
  legítima (`totalElements: 0`), não erro.
* Modo Prova (TASK 5.3, AGENTS.md §9): questão em simulado `PROVA` ainda
  `IN_PROGRESS` deste aluno sai com `answerKey` nulo + nota de
  gabarito oculto (via `searchForUser`/`getByIdForUser`); após
  concluir/abandonar revela normalmente. Ver `docs/api-modo-prova.md`.
* Textos-base (TASK 6.9): `passages[]` em lista e detalhe (leitura em lote
  via `question_passages`, sem N+1; default `[]`). Transcrição literal do
  caderno (`content` nulo = puramente visual, ver `visualDescription`).
  Parte do enunciado: **visível também no Modo Prova**. Sem gate de
  publicação (decisão em `docs/passagens-estrategia.md` §1).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Questões`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **58/58** (41 anteriores + 17 novos:
  `QuestionServiceTest` 11, `QuestionControllerTest` 6).
* Integração real contra o Postgres local (seed V2 + 240 importadas):
  sem filtros = 240 (12 páginas de 20); `MATEMATICA`/`LINGUA_PORTUGUESA` =
  120/120; `year=2026` = 40; `DIFICIL` = 11; `ALGEBRA` (id resolvido por
  código) = 18; detalhe id 1 = enunciado + 4 opções + `answerKey: A` +
  `INTERPRETACAO_TEXTUAL/INTENCAO_COMUNICATIVA`; 2026 Q37 anulada (`X` +
  nota); `year=2021 → EDITION_NOT_FOUND`; `size=101 → 400`
  (teste scratch executado e removido — exigia banco com dados; CI usa
  mocks, sem Docker).
* `docker compose up -d --build app` → `healthy`; sem auth as 2 rotas
  respondem `401` em envelope com `X-Trace-Id`; `/v3/api-docs` lista os
  14 paths (2 novos + 7 de conteúdos + 4 de edições + health).

## Notas técnicas (para não redescobrir)

* `Question` (em `exams.entity`) foi expandida da projeção mínima da TASK 3.2
  para o modelo completo de leitura; `QuestionOption` vive em
  `questions.entity` e as opções são carregadas em lote (`IN`, sem N+1) —
  sem `@OneToMany` na entidade (coleções + paginação não se misturam).
* `SMALLINT` → `Short`, `CHAR(n)` → `@JdbcTypeCode(SqlTypes.CHAR)`
  (mesma regra da TASK 3.2); `checksum` não é mapeado (dedup interno do
  importador, sem valor para o cliente).
* Filtros por assunto/subassunto usam `EXISTS` sobre classificações
  vigentes (maior id por questão; mesmo critério das contagens da TASK 3.3).
* `findActiveByQuestionIds` usa `LEFT JOIN FETCH` de tópico/subassunto; a
  disciplina do tópico sai por lazy dentro da transação (cache de 1º nível —
  só 2 disciplinas distintas).
* Camadas: `questions/controller → service → repository` (+ `dto/`,
  `entity/`); entidades `Discipline`/`Topic`/`Subtopic`/`QuestionClassification`
  reutilizadas dos pacotes `exams`/`content`, sem duplicar mapeamento;
  nenhuma regra em controller (AGENTS.md §19).
