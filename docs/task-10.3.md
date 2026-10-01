# Observabilidade — TASK 10.3

Status: `PARCIAL` — health implementado; logs, métricas e diagnóstico autenticado `PENDENTES` (código deixado para etapa posterior).

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

## 10.3.2 Logs estruturados (PENDENTE — código deixado para depois)

### Estado atual

- `backend/src/main/resources/logback-spring.xml`: console com padrão legível (`%d{...} %level [traceId=%X{traceId:-}] ...`), `CONSOLE` appender, nível `INFO`. Sem JSON. Sem rotação de arquivo (`rolling-file`). Sem limite de tamanho ou retenção.
- `docs/architecture.md` (§9): menciona "logs JSON com rotação" — não implementado.
- `docs/security-audit.md` (§3): ressalva sobre conteúdo real dos logs (`log/app/*.log` ou `log/sys/*.log`) não verificado — `PENDENTE`.

### O que falta (não implementado nesta etapa)

- Configuração de `rolling-file` (tamanho máximo, retenção, compressão).
- Pattern JSON (`%json` ou encoder JSON) para integração com agregadores (ex.: Loki, ELK, CloudWatch).
- Verificação de que nenhum `stacktrace`, token JWT, senha ou `PII` é logado (`docs/security-audit.md` §3 — `PENDENTE` de ambiente, não bloqueia TASK 10.3).

---

## 10.3.3 Métricas essenciais (PENDENTE — código deixado para depois)

### Estado atual

- Nenhum endpoint de métricas exposto (`management.endpoints.web.exposure.include` = `health` apenas).
- Nenhum `MeterRegistry`, `Counter`, `Timer` ou `Gauge` implementado no código.
- Nenhuma métrica essencial (latência por endpoint, taxa de 5xx, tentativas/dia por aluno, disponibilidade) está disponível.

### O que falta (não implementado nesta etapa)

- Endpoint de diagnóstico autenticado (`CURATOR+` ou `ADMIN`) — ver `docs/architecture.md` §5 (autorização RBAC).
- Métricas essenciais:
  - Latência média/percentil por endpoint (`/api/v1/*`).
  - Taxa de erros (`5xx`) e `4xx` (sem expor `PII`).
  - Número de tentativas (`attempts`) por dia.
  - Disponibilidade (`health` status histórico).
- Configuração do Actuator (`/actuator/metrics`) restrita (não exposta publicamente — conforme `docs/security-audit.md` §2.9).

### Referência de arquitetura

- `docs/architecture.md` §9: "métricas essenciais (latência, 5xx, tentativas/dia) sem PII."
- `docs/security-audit.md` §2.9: confirma `management.endpoints.web.exposure.include: health` (correto — apenas health exposto; métricas devem ser protegidas por role).

---

## 10.3.4 Endpoint de diagnóstico autenticado (PENDENTE — código deixado para depois)

### O que falta

- Endpoint (ex.: `GET /api/v1/admin/diagnosis` ou `GET /actuator/diagnosis`) protegido por `CURATOR` ou `ADMIN`.
- Payload mínimo (ex.: `service`, `version`, `dbStatus`, `lastMigration`, `uptime`, `metricsSnapshot`).
- Nenhuma alteração feita no `SecurityConfig.java` ou `HealthController.java` para adicionar esse endpoint.

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
