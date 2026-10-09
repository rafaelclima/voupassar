# API de Provas — TASK 3.2 + multi-processo E.1

> Primeira API real do backend. Somente leitura sobre `exams`,
> `exam_versions`, `exam_documents`, `exam_essay_prompts` (DDL TASK 2.2,
> seed V2) + agregados de `questions`/`disciplines` (importador TASK 2.3).
> Enunciados, alternativas e gabaritos **não** saem aqui (TASK 3.4).
> PDFs-fonte **não** são redistribuídos: documentos saem como metadados
> (nome literal + SHA-256 + páginas), nunca como binário (AGENTS.md §12).
>
> TASK E.1 (Programa EAJ): dimensão `institution` (IFRN|EAJ) em todas as
> rotas. Ano sozinho nunca decide a edição (EAJ-2022 ≠ IFRN-2022).

## Endpoints (todos autenticados até a TASK 3.5 emitir tokens)

| Rota | Descrição |
|---|---|
| `GET /api/v1/editions?institution=` | listar edições (ordem: instituição, ano): ano, `institution`, edital, duração, contagens LP/MAT/CN/CH, `has_essay`, `scoring_rule`, nº de versões/documentos. Sem filtro = ambas (IFRN+EAJ, com rótulo — compatível com clientes antigos); com `?institution=IFRN\|EAJ` = só aquele processo |
| `GET /api/v1/editions/{year}?institution=` | detalhe: versões + documentos + prompt da discursiva da edição `(institution,year)`. Ausente = IFRN (compatibilidade) |
| `GET /api/v1/editions/{year}/documents?institution=` | documentos-fonte (metadados auditáveis) de `(institution,year)` |
| `GET /api/v1/editions/{year}/stats?institution=` | estatísticas: **esperado** (capa) × **importado** (banco), por disciplina, de `(institution,year)` |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default).
Ano fora de 2000–2100 → `400`. `?institution=` inválido → `400`.
Edição inexistente → `404 {"code":"EDITION_NOT_FOUND",…}`:
`IFRN 2021` registra a ausência do dataset IFRN e aponta EAJ-2021 (50Q);
`EAJ 2023` (e 2020/2024/2026) registra que EAJ possui só 2021/2022/2025 —
nunca dados inventados, nunca interpolação.

## Regras de evidência

* `scoring_rule` NULL = **DESCONHECIDA** (pontuação de anuladas + discursiva,
  TASK 1.3 §4). O campo sai como `null` e o `/stats` adiciona nota +
  `scoringRuleKnown: false` — nunca default inventado.
* `/stats` distingue `objectiveExpected` (capa daquela edição) de
  `questionsImported` (banco via TASK 2.3). Banco recém-migrado sem importação
  responde `questionsImported: 0` + nota, sem inventar.
* Anuladas (`X` no gabarito) contam como conteúdo que apareceu na prova;
  a resposta nunca pontua aqui — só conta.
* Estrutura de cada edição pertence àquela edição (divisão 20+20, discursiva
  artigo de opinião — conforme `docs/provas-inventario.md`), nunca regra
  universal: o simulado real (TASK 5.5) deve ser dirigido por estes dados.

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII e sem conteúdo de questão).
Config em `config/OpenApiConfig.java` + `application.yml` (`springdoc.*`,
escopo `/api/v1/**`).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **20/20** (8 do bootstrap + 12 novos:
  `ExamServiceTest` 7, `ExamControllerTest` 5).
* Integração real contra o Postgres local (seed V2 + 240 importadas):
  6 edições, `GET`-lógica de 2026 = 40 importadas / 1 anulada / 39
  confirmadas, detalhe 2022 com prompt, `2021 → EDITION_NOT_FOUND`
  (teste scratch executado e removido — exigia banco com dados; CI usa
  mocks, sem Docker).
* `docker compose up -d --build app` → `healthy`; sem auth as 4 rotas
  respondem `401` em envelope com `X-Trace-Id`; `/v3/api-docs` lista os
  5 paths (4 novos + health).

## Notas técnicas (para não redescobrir)

* DDL usa `SMALLINT` e `CHAR(n)`: entidades mapeiam com `Short` e
  `@JdbcTypeCode(SqlTypes.CHAR)` respectivamente — `String` puro ou
  `columnDefinition="char(64)"` falham no `ddl-auto: validate`
  (`found [int2] expecting [integer]`, `found [bpchar] expecting [varchar]`).
* `Question` é projeção mínima `@Immutable` só para agregados; o domínio
  completo de questões é a TASK 3.4.
* Camadas: `exams/controller → service → repository` (+ `dto/`, `entity/`);
  nenhuma regra em controller (AGENTS.md §19).
