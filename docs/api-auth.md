# API de Autenticação — TASK 3.5

> Registro, login, logout (único + global), refresh rotativo com detecção de
> reuso, recuperação e troca de senha. Access JWT HS256 curto + refresh/reset
> opacos (hash SHA-256 no banco, nunca o valor). Desbloqueia as APIs das
> TASKs 3.2–3.4, até aqui 401 por falta de emissor de token.

## Endpoints

| Rota | Auth | Descrição |
|---|---|---|
| `POST /api/v1/auth/register` | pública | cadastro: `{email, password, displayName, schoolYear?, targetYear?, studyGoal?}` → `201` par de tokens + `STUDENT` + perfil mínimo |
| `POST /api/v1/auth/login` | pública | `{email, password}` → `200` par; falha sempre genérica `401 INVALID_CREDENTIALS` (anti-enumeração, inclusive conta desativada) |
| `POST /api/v1/auth/refresh` | pública (c/ refresh) | rotação: pai revogado + filho encadeado → `200` par novo; reuso de pai → `401 REFRESH_REUSED` e cadeia inteira revogada |
| `POST /api/v1/auth/logout` | pública (c/ refresh) | revoga um refresh → `200` (idempotente: token desconhecido também dá 200) |
| `POST /api/v1/auth/logout-all` | Bearer | revoga todos os refreshes do usuário → `200` |
| `POST /api/v1/auth/password/forgot` | pública | emite reset de uso único p/ conta ativa; resposta **sempre genérica** `200` (exista ou não a conta) |
| `POST /api/v1/auth/password/reset` | pública (c/ token) | `{token, newPassword}` → `200`; inválido/usado/expirado → `400 INVALID_OR_EXPIRED_TOKEN`; derruba sessões (`credential_version++` + revoga refreshes) |
| `POST /api/v1/auth/password/change` | Bearer | `{currentPassword, newPassword}` → `200` **par novo** (segue logado); atual errada → `401`; igual à atual → `400 SAME_PASSWORD` |
| `GET /api/v1/auth/me` | Bearer | conta + perfil mínimo + papéis (`200`); valida o token no filtro |

Erros de validação → `400 VALIDATION_ERROR` (detalhes por campo); e-mail
repetido → `409 EMAIL_IN_USE` (checado antes + `UNIQUE` como rede de
segurança p/ corrida); excesso no `/auth` → `429 RATE_LIMITED`. Tudo no
envelope `{code, message, details, traceId, timestamp, path}`, sem stack
trace; `traceId` também no header `X-Trace-Id`.

## Regras de evidência/segurança

* Senha: política `8–72` (teto do bcrypt), hash **bcrypt custo 10**
  (calibrado p/ VPS 2 GB — `BCRYPT_STRENGTH`, teto 14); nunca em log/resposta.
* E-mail normalizado (`trim + lowercase`); unicidade case-insensitive via
  `CITEXT` (V1) — `ALUNO@x` × `aluno@x` colidem em `409`.
* Access JWT (15 min padrão): claims `sub=userId, email, roles,
  cv=credentialVersion, iss, iat, exp` (JJWT 0.13.0, HS256, segredo 32+
  bytes com fail-fast no boot, skew 60s). O filtro descarta token de conta
  desativada ou `cv` divergente como anônimo (sem vazar motivo).
* Refresh opaco (7 dias padrão, 32 bytes `SecureRandom`): só o hash fica em
  `refresh_tokens`, com `revoked_at` + `replaced_by_id` (cadeia de rotação).
  **Detalhe que mordeu no teste real:** a revogação anti-roubo acontece na
  mesma transação que termina em 401 — sem `noRollbackFor` o rollback
  desfazia a proteção (`AuthService.refresh`).
* Reset opaco de uso único (60 min padrão, V3 `password_reset_tokens`).
* Rate limiting de borda no `/auth` (60 req/min por IP+caminho, em memória,
  desligável em teste); proxy reverso aplica o limite real em produção.
* RBAC: registro concede `STUDENT`; checagens por papel (`CURATOR/ADMIN`)
  entram na área administrativa (TASK 12.1) — fora deste escopo.

## Pendências explícitas (não inventadas)

* **Entrega do token de recuperação por e-mail: PENDENTE.** Provedor
  transacional NÃO CONFIRMADO (architecture.md §11). `forgot` emite e guarda
  só o hash; sem canal de entrega, o token não chega ao usuário fora do
  banco. Resposta genérica já é a correta contra enumeração.
* **Refresh em cookie `HttpOnly`:** architecture.md §5 prevê cookie; neste
  MVP o par sai no JSON (frontend Pages × API em origens distintas — decisão
  de cookie fica p/ ADR quando o domínio de produção existir).

## Verificação (2026-09-30)

* `mvn -f backend/pom.xml test` → **91/91** (58 anteriores + 33 novos:
  `JwtServiceTest` 6, `AuthServiceTest` 15, `AuthControllerTest` 8,
  `AuthSecurityTest` 3, `GlobalExceptionHandlerTest` +1).
* Migração V3 aplicada via `./scripts/db/migrate.sh` (`2 → 3`).
* `docker compose up -d --build app` → `healthy`; fluxo ao vivo: registro
  `201` → duplicado case-insensitive `409` → `/me` `200` → `editions` e
  `questions` desbloqueadas com Bearer (3.2–3.4) → login errado `401`
  genérico → refresh rotaciona → reuso do pai `REFRESH_REUSED` + filho cai
  `401` → change invalida access/refresh antigos e devolve par novo →
  forgot genérico p/ existente e inexistente → reset válido `200`, reuso
  `400`, login só com a nova → logout/logout-all `200` (+ idempotente).
  (Teste scratch com usuários `@exemplo.com`, removidos após; 240 questões
  preservadas. CI usa mocks, sem Docker.)

## Notas técnicas (para não redescobrir)

* `users.email` é `CITEXT`: `String` puro falha no `ddl-auto: validate`
  (`found [citext] expecting [varchar]`) e `@JdbcTypeCode(OTHER)` binda como
  `bytea` (`citext = bytea`). Solução: `common/jpa/CitextJdbcType`
  (código `OTHER` p/ validar + bind/extract de `varchar`).
* Hibernate 7 não atribui `CURRENT_TIMESTAMP` (`Timestamp`) a
  `OffsetDateTime` em `@Modifying`: passar o instante como parâmetro
  (`revokeAllActiveByUserId(userId, now)`).
* Camadas: `auth/controller → service → repository` (+ `dto/`, `entity/`,
  `config/`); filtros em `security/` (`JwtAuthenticationFilter`,
  `AuthRateLimitFilter`); nada de regra em controller (AGENTS.md §19).
* H2 do profile `test` não tem as tabelas (Flyway off): testes de contexto
  não tocam o banco (só validação/chain); regra coberta com mocks.
