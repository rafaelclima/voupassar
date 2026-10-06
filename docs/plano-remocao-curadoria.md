# Plano — Remoção total do contrato de curadoria humana

> Origem: plano proposto pela LLM na máquina dev, registrado e adaptado para
> execução aqui na VPS (acesso direto a backend + DB).
> Data: 2026-10-06. Estado base: `origin/main = c1a7e69` sincronizado nesta
> VPS; `docs/content-analysis/taxonomy.md` modificado local preservado;
> `docs/curadoria-rodada2..7.md` untracked preservados.
> Estado do banco vivo (2026-10-06): `questions=240`
> (42 `APPROVED/PUBLICAVEL`, 197 `PENDING/PENDENTE_REVISAO`,
> 1 `REJECTED/NAO_PUBLICAVEL`);
> `question_classifications`: 169 `APPROVED`, 70 `PENDING`, 1 `REJECTED`;
> Flyway aplicado até `V9__passages` (repo já contém V10+V11 pendentes aqui).

Decisão de produto assumida: **confiança = veredito do pipeline**
(checksum SHA-256 estável + 40/40 vinculadas ao gabarito + 4 alternativas +
resposta entre alternativas + páginas válidas + `question_sources`
caderno+gabarito). Carimbo humano (`validation_status`, `publication_status`,
`classification.status/reviewed_by/reviewed_at`, fila admin, badges de
curadoria) é removido por completo — não apenas destravado.

## Riscos assumidos (registrar em `docs/blockers.md` ao concluir)

1. `2023 Q40` (MMC=2032 x gabarito A=2044) — hoje `REJECTED`; sem humano vira
   "gabarito oficial manda", sem sinal de conflito.
2. `38 NECESSITA_REVISAO` (figuras ilegíveis no `pdftotext`, ex. `2020 Q38`
   frações achatadas) — sem quarentena, o aluno vê texto degradado como íntegro.
3. `SOMENTE_REFERENCIA` (proteção legal p/ trechos de terceiros) — sem a
   coluna, tudo é servido; mitigação passa a ser crédito + página + takedown
   reativo documentado.

## Critério global de pronto (apurado em 2026-10-06)

- [x] Grep de enums de curadoria zerado em `backend/src/main` e `frontend/js`; em `database/migrations` restam só V1/V8 (migrations aplicadas, imutáveis por checksum Flyway). Restam ainda comentários históricos intencionais + papel RBAC `CURATOR`.
- [x] `mvn test` verde (225/225).
- [x] `import_questions.py --check` verde + `report.json` sem divergência (scratch V12 e vivo V12).
- [x] `node --check` + `check_frontend.py` + serve 200 verdes.
- [ ] Navegador real (loading/vazio/erro, mobile+desktop) — PENDENTE, sem browser nesta VPS.
- [x] `docs/blockers.md` com os 3 riscos datados como decisão de produto.

---

## Task 0 — Reconciliação VPS + baseline (obrigatória antes de qualquer DROP)

Objetivo: nunca destruir trabalho não pushado e partir de baseline auditável.

Pré-requisito: repo sincronizado (esta VPS já está em `c1a7e69`, ver log abaixo).

Comandos (somente leitura/segurança):

```bash
git status --short
git log --oneline -5
git diff --stat
# backup do banco ANTES de qualquer migração
docker exec voupassar-db pg_dump -U voupassar -d voupassar -Fc > /tmp/opencode/voupassar-preV12.dump
ls -lh /tmp/opencode/voupassar-preV12.dump
# baseline de dados
docker exec voupassar-db psql -U voupassar -d voupassar -c "SELECT validation_status, publication_status, count(*) FROM questions GROUP BY 1,2 ORDER BY 1,2;"
docker exec voupassar-db psql -U voupassar -d voupassar -c "SELECT status, count(*) FROM question_classifications GROUP BY 1 ORDER BY 1;"
docker exec voupassar-db psql -U voupassar -d voupassar -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 15;"
python3 scripts/db/import_questions.py --check
```

Check de entrega Task 0 (CONCLUÍDA em 2026-10-06 nesta VPS):

- [x] `git status` mostra apenas `M docs/content-analysis/taxonomy.md` + `?? docs/curadoria-rodada*.md` (ou decisão registrada sobre eles).
- [x] Dump `/tmp/opencode/voupassar-preV12.dump` existe e tem tamanho > 0 (240K, EXIT=0).
- [x] Contagens anotadas: questions 240 (42 APPROVED/PUBLICAVEL, 197 PENDING/PENDENTE_REVISAO, 1 REJECTED/NAO_PUBLICAVEL); classifications 169/70/1; options 960; sources 480; has_figure=36.
- [x] `import --check` verde antes de mudar qualquer código (240 candidatas, 0 a inserir, 0 divergências).
- [x] V10+V11 reconciliadas antes da V12 — NOTA DE EXECUÇÃO: `migrate.sh` mira o stack compose local (sem `.env` aqui, e com pgdata separado), então foi aplicado Flyway direto no DB vivo via container `flyway/flyway:11-alpine` na rede `dokploy-network` (`migrate` V9→V11, `success=t`); pós-V11 o `--check` segue verde e a API swarm segue `healthy`. V11 sem perda (coluna `explanation` 0/240 não-NULL; zero referências em `backend/src/main`).

---

## Task 1 — Banco: migração V12 (DROP dos gates + carimbo do pipeline)

Objetivo: remover o contrato humano e introduzir o carimbo máquina-legível.

Arquivos:

- CRIADO: `database/migrations/V12__drop_review_gates.sql` (versão final difere do rascunho abaixo — ver notas).
- NÃO TOCADO: `V1__schema.sql` (editar migração já aplicada quebraria o checksum do Flyway no DB vivo; instalação nova chega ao estado final pela cadeia V1..V12).
- Verificado: `V2__seed.sql` sem referências a status (grep zerado).
- Trigger `enforce_four_options()` reescrito na V12 sem a exceção `validation_status <> 'PENDING'` (regra volta a ser universal; baseline 240x4=960 confere).

Notas de execução 2026-10-06 (divergências honestas do rascunho):

1. V12 também remove `question_figures.publication_status` (criada em `V8__question_figures.sql:13-14`) — gate de figura não estava explícito no plano original, mas é o mesmo contrato.
2. `pipeline_version DEFAULT 'importer-1.0.0'` (não `2.0.0`): é o produtor real das 240 linhas atuais; a Task 3 sobe para `2.0.0` na reescrita do importador.
3. DROPs sem `IF EXISTS` (falha alto, AGENTS.md §4).

Conteúdo da V12 (rascunho original — o arquivo criado prevalece):

```sql
-- V12: remove gates humanos; confiança = pipeline
ALTER TABLE questions DROP COLUMN validation_status;
ALTER TABLE questions DROP COLUMN publication_status;
DROP INDEX IF EXISTS idx_questions_public;
ALTER TABLE question_classifications DROP COLUMN status;
ALTER TABLE question_classifications DROP COLUMN reviewed_by;
ALTER TABLE question_classifications DROP COLUMN reviewed_at;
DROP INDEX IF EXISTS uq_qclass_approved;
DROP INDEX IF EXISTS idx_qclass_q;
CREATE INDEX idx_qclass_q ON question_classifications(question_id, taxonomy_version);
ALTER TABLE questions ADD COLUMN pipeline_version TEXT NOT NULL DEFAULT 'importer-2.0.0';
ALTER TABLE questions ADD COLUMN pipeline_verified_at TIMESTAMPTZ NOT NULL DEFAULT now();
```

Verificação:

```bash
./scripts/db/migrate.sh
docker exec voupassar-db psql -U voupassar -d voupassar -c "\d questions" | grep -i "validation\|publication\|pipeline"
docker exec voupassar-db psql -U voupassar -d voupassar -c "\d question_classifications" | grep -i "status\|reviewed\|taxonomy"
python3 scripts/db/import_questions.py --check
```

Check de entrega Task 1 (ARQUIVO + SCRATCH OK em 2026-10-06; APPLY NO VIVO ADIADO — ver nota):

- [x] V12 escrita e validada em banco scratch: restore do dump pré-V12 (V9, 240) → Flyway V10+V11+V12 aplicadas com sucesso; colunas gates removidas (`questions`, `question_classifications`, `question_figures`); `pipeline_version/pipeline_verified_at` presentes; índices (`idx_questions_public`, `uq_qclass_approved` removidos; `idx_qclass_q` recriado); trigger sem `validation_status`; contagens preservadas 240/960/480/240. Scratch destruído após verificação.
- [ ] Apply no DB vivo + `import --check` pós-V12 — ADIADO PARA A TASK 6 (deploy junto com o backend da Task 2).
- NOTA DE SEQUENCIAMENTO (segurança de produção): ao contrário de V10/V11 (compatíveis com a API no ar), a V12 remove colunas que a API swarm atual (`prod-20261003`) mapeia via JPA — aplicá-la agora derrubaria os endpoints de questões (500 `column does not exist`) até o deploy do backend novo. Por isso o DB vivo segue em V11 até o deploy conjunto código+migração da Task 6. O critério "V12 aplicada na VPS" original foi substituído por este sequenciamento.

---

## Task 2 — Backend (entidades → repos → services → controllers → testes)

Objetivo: eliminar o contrato dos tipos, queries, regras e DTOs.

### 2.1 Entidades

- [ ] `backend/src/main/java/br/com/voupassar/exams/entity/Question.java:84,87` — remover `validationStatus`, `publicationStatus` + getters.
- [ ] `backend/src/main/java/br/com/voupassar/content/entity/QuestionClassification.java` — remover `status/reviewed_by/reviewed_at`.
- Check: `grep -rn "validationStatus\|publicationStatus\|reviewedBy\|reviewedAt" backend/src/main` zerado.

### 2.2 Repositórios

- [ ] `exams/repository/QuestionRepository.java:143,150,160,162,175-181` — remover `findByValidationStatus`, `countByValidationStatus`, `countByPublicationStatus`, `updateStatuses`.
- [ ] `QuestionClassificationRepository` — remover `countByStatus`, `review()`, filtros `status <> 'REJECTED'` em `search/findActive*` (vigente passa a ser maior `id`).
- Check: compilação `mvn -q -DskipTests compile` verde.

### 2.3 Services

- [ ] `admin/service/AdminService.java:42,66-69,104-125,194` — remover `reviewQueue` por status, `updateQuestionStatus`, `reviewClassification`; manter `inconsistencies` + `metrics` enxutas (observabilidade, sem mapas por status).
- [ ] `questions/service/QuestionService.java:300,498-499` — remover `notes[]` de `PENDING/PENDENTE_REVISAO` e `classificationStatus` do response; adicionar `pipelineVersion/pipelineVerifiedAt` se útil.
- [ ] `ContentService.existsByStatus` + trechos de simulado/diagnóstico que leem `classificationStatus`.
- Check: `grep -rn "PENDENTE_REVISAO\|PUBLICAVEL\|APPROVED\|REJECTED" backend/src/main` zerado (exceto comentários históricos a remover).

### 2.4 Controllers / DTOs

- [ ] `admin/controller/AdminController.java:71,82,110` — remover 3 endpoints de fila/curadoria, manter 2 (inconsistências + métricas).
- [ ] Remover/arquivar: `UpdateQuestionStatusRequest`, `UpdateClassificationRequest`, `ReviewQueueItemResponse`, `ClassificationReviewResponse`.
- [ ] `questions/dto/QuestionResponse.java:19-20,50-51` — remover `validationStatus/publicationStatus/classificationStatus`; remover `FigureResponse.publicationStatus`.
- Check: `mvn test` verde.

### 2.5 Testes backend

- [ ] Atualizar: `AdminServiceTest`, `AdminSecurityTest:89`, `QuestionServiceTest:111-112,301-302`, `QuestionControllerTest:92-93,194`, `SimulationServiceTest`, `ContentServiceTest`.
- Check: `mvn test` 100% verde, sem `@Disabled` silencioso.

Check de entrega Task 2:

- [ ] `mvn -q -DskipTests compile` verde.
- [ ] `mvn test` verde.
- [ ] Grep de enums de curadoria zerado em `backend/src/main`.
- [ ] Health + fluxo aluno (questões, simulado, roteiro) validados contra a API local.

---

## Task 3 — Scripts / pipeline

Objetivo: importador e validadores sem defaults de curadoria.

- [ ] `scripts/db/import_questions.py:32-33,334,340,398` — remover defaults `PENDING/PENDENTE_REVISAO` e `INSERTs` de status; gravar `pipeline_version`.
- [ ] `scripts/db/sync_figures.py`, `check_figures.py`, `extract_figures.py` — status do manifest vira fixo ou some (não mais `PUBLICAVEL` condicional).
- [ ] `scripts/analysis/validate_classification.py`, `build_content_map.py` — `n_rev` some; `NECESSITA_REVISAO` vira `has_figure`/nota, não gate.
- [ ] Arquivar (mover para `scripts/archive/`, não deletar silenciosamente): `curate_classifications.py`, `backfill_classification_observations.py` e demais one-shots já executados.

Verificação:

```bash
python3 scripts/db/import_questions.py --check
python3 scripts/db/extract_passages.py --check
python3 scripts/analysis/validate_classification.py
python3 scripts/analysis/build_content_map.py --check
cat data/import/report.json | head -60
```

Check de entrega Task 3:

- [ ] `--check` verde nos 6 anos (2020, 2022, 2023, 2024, 2025, 2026).
- [ ] `report.json` sem divergência de checksums/gabaritos.
- [ ] Nenhum script ativo referencia `PENDENTE_REVISAO/PUBLICAVEL/PENDING/APPROVED`.

---

## Task 4 — Frontend (fila vira painel de saúde)

Objetivo: remover UI de curadoria; aluno sem jargão interno.

- [ ] `frontend/js/api/admin.js` — remover chamadas de fila + PATCHs de curadoria; manter inconsistências/métricas.
- [ ] `frontend/js/views/admin.js:46,70,141-145,310,412-414,444,510-513,543-560,585-608,668` — fila, filtros por status, badges, forms `questionStatusForm/classificationForm` viram painel de saúde (sem ações de aprovação).
- [ ] `frontend/js/vocab.js:4-5,96-98` — remover `CLASSIFICATIONS/PUBLICATIONS`.
- [ ] `frontend/js/views/questao.js:461,520` — remover linha Publicação + nota de curadoria.
- [ ] `frontend/js/components/figure.js:4-5,82-83,93,107,128` — remover gate `PENDENTE_REVISAO` (mostrar figura + crédito/página, sem “em curadoria”).
- [ ] `frontend/js/components/passage.js:7,55` — ajustar copy “conferida pela curadoria” para crédito ao caderno/página.
- [ ] Notes em `estudos.js:878`, `simulado.js:800,985`, `questao.js` que citam curadoria.

Verificação:

```bash
node --check frontend/js/views/admin.js
node --check frontend/js/components/figure.js
node --check frontend/js/vocab.js
python3 scripts/analysis/check_frontend.py
python3 -m http.server 8081 --directory frontend &
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/estudos.html
# + navegador real: guarda de autenticação, fluxo principal, loading/vazio/erro, mobile+desktop, console sem erros inesperados
```

Check de entrega Task 4 (CONCLUÍDA em 2026-10-06):

- [x] `node --check` verde nos 8 arquivos tocados.
- [x] `check_frontend.py` verde (3 regras do próprio validador atualizadas).
- [x] Serve 200 em `index/estudos/questao/simulado/admin.html`.
- [ ] Navegador real — PENDENTE (sem Chromium/Playwright nesta VPS; deferido para pós-deploy).

---

## Task 5 — Docs + constituição (separar commit técnico de decisão)

Objetivo: docs refletirem o novo contrato; mudança constitucional explícita.

Commit técnico (pode ir com o código):

- [ ] `docs/database-erd.md §§2.4/5/7/8` — novo modelo sem gates.
- [ ] `docs/api-admin.md` — só inconsistências + métricas.
- [ ] `docs/api-questoes.md` — response sem status de curadoria, com `pipeline_version` se exposto.
- [ ] `docs/auditoria-questoes.md`, `docs/relatorio-cobertura.md` — sem “% publicável”.
- [ ] `docs/curadoria.md` vira histórico (adicionar cabeçalho “superseded em 2026-10-06 por este plano”).
- [ ] `TASKS.md 11.1/11.2/12.1/12.2` — marcar superseded ou redefinir.
- [ ] `docs/blockers.md` — registrar os 3 riscos assumidos com data + responsável.

Decisão explícita separada (NÃO no mesmo commit técnico — `AGENTS.md §28`):

- [ ] `AGENTS.md §§4,11,12,22,23` — reescrever regra de ouro, proveniência/direitos, validação e duplicidade para “pipeline como fonte de confiança”. Exige aprovação explícita do responsável.

Check de entrega Task 5 (CONCLUÍDA em 2026-10-06, 2 commits):

- [x] Docs técnicos atualizados e sem referência ativa a fila/curadoria.
- [x] `curadoria.md` preservado como histórico, não deletado.
- [x] Mudança do `AGENTS.md` aprovada explicitamente (sim, 2026-10-06; §§4/12/22 reescritos, §11/§23 mantidos) em commit separado.

---

## Task 6 — Deploy VPS a partir do repo (nada direto na VPS sem push)

Objetivo: VPS como réplica do validado, nunca origem de mudança.

```bash
git status --short
git diff --stat
mvn test
python3 scripts/db/import_questions.py --check
python3 scripts/analysis/check_frontend.py
git add -A && git commit -m "chore: remove contrato de curadoria humana (pipeline como fonte de confiança)"
git push origin main
# na VPS, somente a partir do repo:
git pull --ff-only
./scripts/db/migrate.sh
docker compose up -d --build app
curl -s http://localhost:8080/actuator/health || docker service ls
```

Check de entrega Task 6 (CONCLUÍDA em 2026-10-06 nesta VPS):

- [x] Testes + checks verdes antes do push (mvn 225/225; import --check em scratch V12; check_frontend.py).
- [x] Deploy via repo, sem edição direta: backup fresco `/root/backups/voupassar-20261006-preV12.dump` → build `voupassar-backend:prod-20261006` (V12 no jar) → `docker service update --image` (Flyway V12 no boot, `success=t`). NOTA: `migrate.sh`/`compose up` do rascunho miram o stack local, não o swarm de produção — o caminho correto aqui é build + service update (runbook `docs/deploy-vps.md` §3).
- [x] Pós-deploy: questions=240 preservadas; `/actuator/health` UP via domínio; `/editions` sem token 401 em envelope; `GET /questions` sem campos de status; `/admin/metrics` (CURATOR) com novo formato 240/5/36/240; `/admin/review-queue` 404 (endpoint extinto); inconsistências 4× zero; importador 2.0.0 `--check` verde contra o vivo (240/0/0, exit 0). Usuário de smoke criado e removido.
- [x] Falha em testes teria impedido o deploy (suíte rodada antes do build).
- [ ] Navegador real + Pages pós-CI — PENDENTE (sem browser nesta VPS).

---

## Anexos — evidências de partida (2026-10-06, VPS)

- `git log`: `c1a7e69 docs(ux): baixa itens 3 e 4 do UX review validados no navegador` (sincronizado).
- `git status`: `M docs/content-analysis/taxonomy.md` (G1/G2 RESOLVIDO v1.1, G3 APROVADO PENDENTE migração) + `?? docs/curadoria-rodada2..7.md`.
- Banco: 42 `APPROVED/PUBLICAVEL`, 197 `PENDING/PENDENTE_REVISAO`, 1 `REJECTED/NAO_PUBLICAVEL`; classificações 169/70/1; Flyway até V9.
