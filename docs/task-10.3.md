# Observabilidade — TASK 10.3 (código na TASK 22.2)

Status: `DONE` — health + logs JSON + contadores + diagnóstico autenticado
implementados (2026-10-07); backup/restore seguem na TASK 13.2.

Fonte de verdade: `docs/architecture.md` §9, `docs/security-audit.md`, `backend/src/main/resources/`, `backend/Dockerfile`, `docker-compose.yml`.

---

## 10.3.1 Health check (IMPLEMENTADO)

### Backend (`backend/src/`)

- `HealthController.java` (`GET /api/v1/health`) — público, mínimo (`status`, `service`, `version`, `timestamp`). Sem dependência do banco.
- `application.yml` (§53-66): `management.endpoints.web.exposure.include: health`; `/actuator/health` exposto; `show-details: never`; `db.enabled: true`.
- `backend/Dockerfile`: `HEALTHCHECK` (intervalo 20s, timeout 5s, start-period 40s, retries 3) — verifica `/actuator/health` via `wget`.
- `docker-compose.yml`: `healthcheck` para `app` (teste `wget -qO- ... | grep -q '"status":"UP"'`) e `db` (`pg_isready`).

### Validação estática

- `python3 scripts/analysis/check_frontend.py` não cobre health (não é frontend).
- `docs/security-audit.md` (§2.9): confirma `management.endpoints.web.exposure.include: health` e `show-details: never` — correto.

---

## 10.3.2 Logs estruturados (IMPLEMENTADO na 22.2)

### Estado atual

- `backend/src/main/resources/logback-spring.xml`: console em JSON
  (`LogstashEncoder`, com `traceId` do MDC) — agregadores (Loki/ELK).
- Rotação no runtime, não no app: `docker-compose.yml` (`app.logging`:
  `json-file`, `max-size: 10m`, `max-file: 3`); na VPS (swarm/dokploy),
  mesma política no daemon — sem appender de arquivo no container.
- Auditoria de conteúdo (2026-10-07): 19 `log.*` no backend, todos com
  `user_id` técnico — nenhum e-mail, senha, token ou SQL com dados;
  `GlobalExceptionHandler.java:115` loga stack só no servidor (com traceId).
- `docs/security-audit.md` §3: ressalva de ambiente mantida (o conteúdo real
  dos logs de produção confirma-se no deploy, TASK 22.3).

---

## 10.3.3 Métricas essenciais (IMPLEMENTADO na 22.2)

### Estado atual

- `TechMetrics.java` (`admin/service`): 5 contadores de eventos de negócio
  já visíveis ao servidor (sem clique/dwell/fingerprint — AGENTS.md §16):
  `voupassar.plans.generated`, `voupassar.diagnoses.generated`,
  `voupassar.attempts.submitted`, `voupassar.simulations.submitted`,
  `voupassar.review.sessions.created` (em memória, por instância).
- `/actuator/metrics` segue NÃO exposto (`exposure.include: health`);
  o snapshot vai no diagnóstico autenticado (abaixo).
- Mapa das 5 métricas da auditoria §13 (TASKS.md Fase 22):
  | Métrica §13 | Fonte servidor (22.2) | Falta (trilha local) |
  |---|---|---|
  | CTR revisão | `review.sessions.created` (criações) | cliques na UI (17.x) → CTR real |
  | CTR roteiro | `plans.generated` (gerações) | cliques "Praticar/Revisar" (18.3) |
  | CTR raio-X | — | bloco `result-next` ainda não existe (19.1) |
  | Conclusão de wizard | — | wizard ainda não existe (20.2) |
  | D7 pós-simulado | `simulations.submitted` (conclusões) | coorte por usuário = query analítica, não contador |

### Referência de arquitetura

- `docs/architecture.md` §9: "métricas essenciais (latência, 5xx, tentativas/dia) sem PII."
- `docs/security-audit.md` §2.9: confirma `management.endpoints.web.exposure.include: health` (correto — apenas health exposto; métricas devem ser protegidas por role).

---

## 10.3.4 Endpoint de diagnóstico autenticado (IMPLEMENTADO na 22.2)

- `GET /api/v1/admin/diagnostics` (`AdminController.java`, `@PreAuthorize`
  `CURATOR`/`ADMIN`; 401 sem token, 403 sem papel): `{service, version,
  dbStatus, lastMigration, uptimeMillis, metrics}` — sem PII, sem conteúdo.
- `GET /health` (e `/actuator/health`) seguem públicos com
  `show-details: never`.

---

## 10.3.5 Backup/restore (referenciado, não implementado na TASK 10.3)

- `docs/architecture.md` (§13): estratégia de backup PostgreSQL documentada; `TASK 12.2` (curadoria) e `TASK 13.2` (backup) são responsáveis pela implementação física.
- `docker-compose.yml`: volume `pgdata` persistente; sem script de backup automático no Compose.

---

## Critério de conclusão desta etapa

- [x] Documentação (`docs/task-10.3.md`) criada.
- [x] Status de cada sub-item registrado (`IMPLEMENTADO` / `PENDENTE`).
- [x] Nenhuma alteração de código feita (decisão documentada: código deixado para depois).
- [x] Nenhum arquivo destruído.
- [x] Nenhuma informação inventada (todos os `PENDENTES` são verificados no código/disco; nenhum log JSON ou métrica inventado).

---

## Próximos passos (quando a etapa de código for retomada)

1. Atualizar `logback-spring.xml` para JSON + rotação (`rolling-file`).
2. Adicionar endpoint de diagnóstico autenticado (`CURATOR+`).
3. Configurar métricas essenciais (`MeterRegistry` + endpoint protegido).
4. Testar no Compose (`docker compose up -d --build app` + verificar `/actuator/health` e logs).
5. Atualizar `docs/security-audit.md` após implementação.
