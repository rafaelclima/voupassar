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
`RATE_LIMIT_WINDOW_SECONDS=60`, `JAVA_OPTS=-Xms256m -Xmx512m→384m` (VPS 2 GB).

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
```

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
