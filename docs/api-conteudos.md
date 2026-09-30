# API de Disciplinas e Conteúdos — TASK 3.3

> Segunda API real do backend. Somente leitura sobre `disciplines`,
> `topics`, `subtopics` (DDL + seed V2, taxonomia v1.1 em
> `docs/content-map.md`) + agregados de `question_classifications`
> (importador TASK 2.3) e `questions`.
> Enunciados, alternativas e gabaritos **não** saem aqui (TASK 3.4).

## Endpoints (todos autenticados até a TASK 3.5 emitir tokens)

| Rota | Descrição |
|---|---|
| `GET /api/v1/disciplines` | listar disciplinas (código crescente): código, nome, nº de assuntos, nº de questões importadas |
| `GET /api/v1/disciplines/{code}` | detalhe + assuntos da disciplina |
| `GET /api/v1/topics?disciplineCode=` | listar assuntos (todos ou por disciplina) com contagem histórica e nº de subassuntos |
| `GET /api/v1/topics/{id}` | detalhe: disciplina, série por edição, confiança ALTA/MEDIA/BAIXA, subassuntos |
| `GET /api/v1/subtopics?topicId=` | listar subassuntos (todos ou por assunto) com contagem histórica |
| `GET /api/v1/subtopics/{id}` | detalhe: assunto/disciplina, série por edição, confiança |
| `GET /api/v1/content/stats` | panorama: total importado × classificado, versões de taxonomia, por disciplina e por assunto (% + edições + anuladas) |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default).
Disciplina inexistente → `404 {"code":"DISCIPLINE_NOT_FOUND",…}`;
assunto → `TOPIC_NOT_FOUND`; subassunto → `SUBTOPIC_NOT_FOUND`.
Código em branco ou fora de `[A-Z_]` → `400`; ID `<= 0` → `400`.

## Regras de evidência

* Disciplinas/assuntos/subassuntos vêm do seed V2 (só o observado nas
  provas) — nada inventado; `is_active=false` preserva histórico, nunca apaga.
* Contagens de disciplinas usam `questions.discipline_id` (factual);
  contagens de assuntos/subassuntos usam `question_classifications`
  não-rejeitadas (`status <> 'REJECTED'`, tópico presente) — derivado.
* Anuladas (`X` no gabarito) contam como conteúdo que apareceu na prova;
  a resposta nunca pontua aqui — só conta.
* Classificações têm revisão humana PENDENTE (TASK 12.2): todo payload
  agregado sai com nota explícita; frequências são derivadas, nunca verdade
  oficial do IFRN. Dificuldade estimada não entra (sempre confiança BAIXA,
  sem dados — calibração na Fase 4).
* Tendência NÃO é calculada (6 edições, sem teste estatístico; oscilações de
  1–2 questões são ruído — ver `docs/content-map.md`).
* Banco recém-migrado sem importação responde zeros + notas, sem inventar.
* Séries por edição pulam 2021 (ausente do dataset, AGENTS.md §3).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII e sem conteúdo de questão).
Tag `Conteúdos`; escopo `/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **41/41** (20 anteriores + 21 novos:
  `ContentServiceTest` 12, `ContentControllerTest` 9).
* Integração real contra o Postgres local (seed V2 + 240 importadas):
  2 disciplinas (120 + 120), 10 assuntos, 42 subassuntos;
  ÁLGEBRA = 18 (série 4-1-4-3-2-4), EQUACOES = 8, stats 240/240 `["v1.1"]`
  com `classificationReviewPending: true`
  (teste scratch executado e removido — exigia banco com dados; CI usa
  mocks, sem Docker).
* `docker compose up -d --build app` → `healthy`; sem auth as 7 rotas
  respondem `401` em envelope com `X-Trace-Id`; `/v3/api-docs` lista os
  12 paths (7 novos + 4 de edições + health).

## Notas técnicas (para não redescobrir)

* `Discipline`/`Question` são reutilizadas de `exams.entity` (mesma tabela,
  sem duplicar mapeamento); `Topic`/`Subtopic`/`QuestionClassification`
  vivem em `content.entity` (`@Immutable` na classificação, como em `Question`).
* `DisciplineRepository.findByCode` e
  `QuestionRepository.countByDisciplineCode` foram adicionados para esta TASK.
* JPQL nunca compara `CHAR`/`SMALLINT` como string: ano sai como
  `Short` (`q.exam.year`) e `SUM(CASE…)` nulo vira `0` no serviço.
* Camadas: `content/controller → service → repository` (+ `dto/`, `entity/`);
  nenhuma regra em controller (AGENTS.md §19).
