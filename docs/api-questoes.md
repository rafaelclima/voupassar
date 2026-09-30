# API de Questões — TASK 3.4

> Terceira API real do backend. Somente leitura sobre `questions` +
> `question_options` (conteúdo integral: enunciado, alternativas, gabarito)
> com proveniência (`source_type/year/number`, páginas, disciplina, edição) e
> classificação vigente (`question_classifications` não-rejeitada mais recente).
> Filtros por disciplina, assunto, subassunto, edição, dificuldade e origem +
> paginação. PDFs-fonte **não** são redistribuídos (AGENTS.md §12).

## Endpoints (todos autenticados até a TASK 3.5 emitir tokens)

| Rota | Descrição |
|---|---|
| `GET /api/v1/questions?disciplineCode=&topicId=&subtopicId=&year=&difficulty=&sourceType=&page=&size=` | listar paginado (ordem fixa: ano-fonte, número, id): enunciado + alternativas + gabarito + proveniência + assunto + notas de evidência |
| `GET /api/v1/questions/{id}` | detalhe integral da questão |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default).
Filtros para disciplina/assunto/subassunto/edição inexistentes →
`404 {"code":"DISCIPLINE/TOPIC/SUBTOPIC/EDITION_NOT_FOUND",…}` (nunca página
vazia silenciosa); para **2021** a mensagem registra explicitamente a
ausência do dataset (AGENTS.md §3). Questão inexistente →
`404 {"code":"QUESTION_NOT_FOUND",…}`. Filtro/paginação malformados → `400`
(`page` 0-based; `size` em `[1, 100]`; `difficulty` em
`FACIL/MEDIA/DIFICIL`; `sourceType` em
`OFFICIAL/AUTHORAL/ADAPTED/INTERNAL_REVIEW/EXPERIMENTAL`).

## Regras de evidência

* `OFFICIAL` = prova real do IFRN; qualquer outro `sourceType` nunca é do
  IFRN e sai identificado como tal (AGENTS.md §11). Hoje o banco tem 240
  `OFFICIAL` (40 × 6 edições, importador TASK 2.3).
* `answerKey = "X"` ⟺ anulada (5 no banco): sai normalmente, conta como
  conteúdo que apareceu na prova, nunca pontua aqui; cada item anulado traz
  a nota explícita (regra de pontuação DESCONHECIDA, TASK 1.3 §4).
* `difficultyEstimate` é palpite com confiança BAIXA global (sem calibração
  — Fase 4); `explanation` NULL = correção ainda não redigida (nunca
  inventada); classificação com revisão humana PENDENTE (TASK 12.2) — tudo
  sinalizado no array `notes` de cada questão, nunca omitido.
* Questão sem classificação vigente sai com `topic/subtopic` nulos +
  nota de assunto NÃO CONFIRMADO (nunca com assunto inventado).
* `publicationStatus` em todas as importadas é `PENDENTE_REVISAO`
  (curadoria TASK 12.2); `hasFigure=true` indica figura no PDF-fonte.
* Ordem fixa e determinística (ano-fonte, número, id): sem ordenação por
  relevância sem algoritmo auditável (Fase 4).
* Origem válida sem linhas (ex. `sourceType=AUTHORAL` hoje) = página vazia
  legítima (`totalElements: 0`), não erro.
* O Modo Prova (ocultar gabarito durante a execução) é responsabilidade da
  camada de simulados (Fase 5), não deste banco de leitura.

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
  não-rejeitadas (mesmo critério das contagens da TASK 3.3); a vigente é a
  de maior id (importador gera 1 por questão).
* `findActiveByQuestionIds` usa `LEFT JOIN FETCH` de tópico/subassunto; a
  disciplina do tópico sai por lazy dentro da transação (cache de 1º nível —
  só 2 disciplinas distintas).
* Camadas: `questions/controller → service → repository` (+ `dto/`,
  `entity/`); entidades `Discipline`/`Topic`/`Subtopic`/`QuestionClassification`
  reutilizadas dos pacotes `exams`/`content`, sem duplicar mapeamento;
  nenhuma regra em controller (AGENTS.md §19).
