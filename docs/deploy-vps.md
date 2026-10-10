# VouPassar — Runbook de produção (VPS + Dokploy)

> Deploy realizado em 2026-10-03. Segredos **nunca** neste arquivo:
> estão em `/root/.voupassar-prod.env` (chmod 600, fora do Git).
> Dump de origem: `data/dump/voupassar-20261003_203213.sql`.

## 1. Topologia (padrão smartficha: swarm + Traefik do Dokploy)

| Peça | Nome | Detalhe |
|---|---|---|
| Banco | service `voupassar-db` | `postgres:16-alpine`, volume `voupassar-pgdata`, rede `dokploy-network`, limite 256M |
| Backend | service `voupassar-api` | imagem `voupassar-backend:prod-20261003` (build de `backend/Dockerfile`, contexto raiz), rede `dokploy-network`, limite 512M, `JAVA_OPTS=-Xms256m -Xmx384m`, profile `prod` |
| Rota TLS | `/etc/dokploy/traefik/dynamic/voupassar-api.yml` | `https://api-voupassar.hojeafestaenossa.site` → `http://voupassar-api:8080`, `certResolver: letsencrypt` (mesmo molde de `smartficha-api.yml`) |
| DNS | Cloudflare, `api-voupassar` em **DNS only (nuvem cinza)** | Decisão 2026-10-03: com proxy laranja o edge devolvia 403 sem CORS para navegadores reais (app exonerado por testes). Traefik emite TLS sozinho; único custo é expor o IP de origem |
| Frontend | GitHub Pages | `https://rafaelclima.github.io/voupassar/` — exige `vars.API_BASE_URL=https://api-voupassar.hojeafestaenossa.site` + re-run do workflow `frontend.yml` |
| Backup | `/root/backups/voupassar-20261003-init.dump` | `pg_dump -Fc`, 302 entradas; validação via `pg_restore --list` |

Projeto `voupassar` já existia no Dokploy (só metadados); banco/app foram
criados como swarm services isolados — **nenhum outro projeto foi tocado**
(`borali`, `botsena`, `smartficha`, `poupae`, `hojeafestaenossa` intactos).

## 2. Variáveis do backend (nomes; valores em `/root/.voupassar-prod.env`)

`SPRING_PROFILES_ACTIVE=prod`, `SERVER_PORT=8080`,
`SPRING_DATASOURCE_URL=jdbc:postgresql://voupassar-db:5432/voupassar`,
`SPRING_DATASOURCE_USERNAME/ PASSWORD`, `CORS_ALLOWED_ORIGINS=https://rafaelclima.github.io`
(origins nunca têm path — o valor com `/voupassar` quebrava o preflight com
403; corrigido em 2026-10-03 via `docker service update --env-add`),
`JWT_SECRET` (48 bytes, base64), `JWT_ISSUER=voupassar`,
`ACCESS_TTL_MINUTES=15`, `REFRESH_TTL_DAYS=7`, `RESET_TTL_MINUTES=60`,
`BCRYPT_STRENGTH=10`, `RATE_LIMIT_ENABLED=true`, `RATE_LIMIT_MAX=60`,
`RATE_LIMIT_WINDOW_SECONDS=60`, `RATE_LIMIT_WRITE_MAX=120` (default no código;
escrita em `/attempts`, `/simulations`, `/recommendations` — TASK 22.1),
`BEHIND_PROXY=true` (desde 2026-10-07: traefik injeta `X-Forwarded-For` real;
sem isso o rate limit usa o IP do proxy, compartilhado — TASK 22.1/22.3),
`JAVA_OPTS=-Xms256m -Xmx512m→384m` (VPS 2 GB).

## 3. Operação

```bash
# status
docker service ps voupassar-db voupassar-api
docker service logs voupassar-api --tail 50

# backup (senha via arquivo, nunca na linha de comando do histórico)
PGPW=$(sed -n 's/^POSTGRES_PASSWORD=//p' /root/.voupassar-prod.env)
docker run --rm --network dokploy-network -v /root/backups:/backups \
  -e PGPASSWORD="$PGPW" postgres:16-alpine \
  pg_dump -Fc --no-acl --no-owner -h voupassar-db -U voupassar voupassar \
  -f /backups/voupassar-AAAAMMDD.dump

# restore (banco vazio/novo; o dump contém schema + dados + flyway V1–V9)
docker run --rm --network dokploy-network -v /root/backups:/backups:ro \
  postgres:16-alpine pg_restore -h voupassar-db -U voupassar -d voupassar \
  /backups/voupassar-AAAAMMDD.dump

# rebuild + redeploy do backend (nunca compilar fora de container)
docker build -f backend/Dockerfile -t voupassar-backend:<TAG> .
docker service update --image voupassar-backend:<TAG> voupassar-api
# com env junto (um restart só):
# docker service update --image voupassar-backend:<TAG> --env-add BEHIND_PROXY=true voupassar-api
# rollback: docker service update --image voupassar-backend:<TAG-ANTERIOR> voupassar-api
```

## 3b. Deploy 2026-10-07 (trilha VPS 16.1–22.3)

- Imagem `voupassar-backend:prod-20261007` (era `prod-20261006`, V13):
  IDOR do roteiro, score v2, evidência rica, PROVISORIO, rate-limit em
  escrita + `Retry-After`, XFF atrás de proxy, logs JSON, contadores +
  `GET /admin/diagnostics`, README.
- Flyway aplicou V14+V15+V16 limpas no start (validadas antes em scratch
  via restore do prod).
- Smoke pós-deploy: `/api/v1/health` UP, 401 em envelope sem `stackTrace`,
  preflight CORS 200, logs JSON, `BEHIND_PROXY=true` no spec.
- Rollback (se necessário):
  `docker service update --image voupassar-backend:prod-20261006 voupassar-api`
  (nota: V14–V16 já aplicadas no banco permanecem — são compatíveis com
  leitura pelo código antigo: só adicionam coluna `study_plans.status` com
  default e um marker; downgrade de código não desfaz DDL).

## 3c. Deploy 2026-10-10 (EAJ go-live: V17–V22 + 130 questões)

- Imagens `voupassar-backend:prod-20261010` (código `fe9e53f`: E.1/E.2/F.1/F.2,
  G.1/G.2) e `prod-20261010-v22` (vigente; adiciona só a V22). Backups `-Fc`:
  `voupassar-20261010-preEAJ.dump` e `voupassar-20261010-preV22.dump`.
- Flyway aplicou V17–V22 limpas no start (`eaj institution`, `50Q + CN/CH`,
  `seed EAJ`, `taxonomy CN/CH`, `study_plans.institution`, `retranscrição
  2025`). A V22 foi validada antes em dry-run transacional na VPS
  (`UPDATE 1` + `ROLLBACK`) e dupla aplicação em scratch.
- Import EAJ: `import_questions.py --institution EAJ
  --allow-needs-visual-check` → 130/520/260/130 (LP 55, MAT 55, CN 12, CH 8;
  relatório `data/import/report-eaj.json`, `status OK`). Banco: 430 questões
  (240 IFRN intactas + 130 EAJ 50/40/40).
- `sync_figures.py --institution EAJ` → 58 figuras (0 puladas; `has_figure`
  99 = 41 IFRN + 58 EAJ). `import_passages.py --institution EAJ` → 11
  passagens + 34 vínculos (0 pulados; Q22←TEXTO-3 ligado).
- Smoke público 7/7 (conta scratch removida após, com `trg_attempts_immutable`
  religado): EAJ 130Q, EAJ-2021 50Q, EAJ-2025 40Q (sem `409`), IFRN 240Q +
  IFRN-2026 40Q. Nota: o edge devolve 403 para UA `python-urllib` (curl/UA de
  navegador passam) — comportamento da borda, não da API.
- Rollback (se necessário):
  `docker service update --image voupassar-backend:prod-20261007 voupassar-api`
  (nota: V17–V22 no banco permanecem; código antigo não conhece `institution`
  — downgrade só como ponte até forward-fix).

## 4. Smoke executado em 2026-10-03 (todos OK)

- `GET /actuator/health` → `{"status":"UP"}` (interno e via domínio).
- `GET /api/v1/editions` sem token → `401 UNAUTHORIZED` (secure-by-default).
- Preflight CORS `Origin: https://rafaelclima.github.io/voupassar` → `200` + `access-control-allow-origin` correto.
- `POST /api/v1/auth/register` → JWT `STUDENT`; `GET /api/v1/questions?page=0&size=1` → Q1/2020 com gabarito e classificação. Usuário de smoke (`id 13`) removido após o teste.
- Banco: 240 questões, 960 alternativas, 6 edições, 31 passagens, `flyway_schema_history` V1–V9 `success=t`.
- Contas de teste vindas do dump (`teste@voupassar.local`, `aluno@teste.local`) mantidas; remover antes do lançamento se necessário.

## 5. Pendências (não bloqueiam o backend)

1. **Frontend**: definir `vars.API_BASE_URL=https://api-voupassar.hojeafestaenossa.site`
   (repo → Settings → Variables → Actions) e re-executar o workflow `frontend.yml`.
   Sem isso o Pages continua apontando para `localhost:8080`.
2. Opcional: cadastrar Database/Application no painel Dokploy para gestão visual
   (hoje são swarm services manuais no mesmo padrão dos demais).
3. Rotina de backup diário (cron) + retenção — ver TASK 13.2.

## 6. CI/CD do backend (decisão 2026-10-04)

* Push/PR roda só `validate-and-test` (build Maven + testes). Sem SSH,
  sem restart na VPS.
* `build-image` + `deploy` rodam **só via `workflow_dispatch`** (botão
  "Run workflow" no Actions, branch `main`). Motivo: cada push na main
  — docs, JSON de questões, frontend — rebuilding a imagem e reiniciando
  a API em produção sem necessidade.
* O build usa `push: false` + `load: true` e a imagem viaja por artifact
  (`docker save`/`load`); **não há login em registry** (removido após
  falhar por `DOCKER_USERNAME` ausente — o login era desnecessário).
* O job `deploy` confere `VPS_HOST`/`VPS_USER`/`VPS_SSH_KEY` no início e
  falha com mensagem clara quando ausentes.
* Secrets (Settings → Secrets and variables → Actions):
  `VPS_HOST` + `VPS_USER` (variables), `VPS_SSH_KEY` (secret, chave
  privada). Chave dedicada `github-actions-voupassar` (ed25519, sem
  passphrase), com a pública em `~/.ssh/authorized_keys` da VPS.
  Revogação: remover a linha dela do `authorized_keys`.
* Flyway aplica as migrations no start do backend — o deploy manual é o
  momento em que correções de dados (ex.: V10, frações) chegam à API.
