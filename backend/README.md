# Backend — VouPassar (TASK 3.1 bootstrap)

API REST em Java 21 + Spring Boot 4.1.0 (latest stable 2026-06-10, Maven Central).
Escopo do bootstrap: configuração por profile, logging com traceId,
tratamento global de erros e health endpoints. Sem regra de negócio.

## Rodar local (paridade com a VPS)

```bash
cp .env.example .env
docker compose up -d db
./scripts/db/migrate.sh
export PATH="$HOME/.local/share/mise/installs/maven/3.9.16/apache-maven-3.9.16/bin:$PATH"
mvn -f backend/pom.xml spring-boot:run
# ou com o profile dev explícito:
# SPRING_PROFILES_ACTIVE=dev mvn -f backend/pom.xml spring-boot:run
```

Ou tudo no Compose (app + db):

```bash
docker compose up -d --build
docker compose ps
curl -s http://localhost:8080/api/v1/health
```

## Endpoints do bootstrap

| Rota | Auth | Descrição |
|---|---|---|
| `GET /api/v1/health` | pública | status mínimo `{status, service, version, timestamp}` |
| `GET /actuator/health` | pública | health do Actuator (inclui DB quando configurado) |

Toda resposta de erro usa o envelope `{code, message, details, traceId, timestamp, path}`
e nunca inclui stack trace. O `traceId` vai no header `X-Trace-Id`.

## Configuração

| Variável | Padrão dev | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` / `prod` / `test` |
| `SERVER_PORT` | `8080` | porta HTTP |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/${POSTGRES_DB}` | JDBC (no Compose: `jdbc:postgresql://db:5432/...`) |
| `SPRING_DATASOURCE_USERNAME/PASSWORD` | `POSTGRES_USER/PASSWORD` | credenciais (nunca no Git) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:8080,http://localhost:3000` | origens exatas, vírgula |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | heap conservador p/ VPS 2 GB |

Flyway reaproveita `database/migrations/*.sql` (copiadas para
`classpath:db/migration` no build — sem duplicar SQL). Tabela
`flyway_schema_history`, a mesma do `migrate` via Compose.

## Segurança (base; JWT na TASK 3.5)

Secure-by-default: só o health é público; o resto exige autenticação
(hoje responde 401/403 — correto até a TASK 3.5 emitir tokens).
CORS restritivo, CSRF off (API stateless), sessão stateless.

## Testes

```bash
mvn -f backend/pom.xml test
```

* `VoupassarApplicationTests` — contexto com profile `test` (H2, sem Postgres).
* `HealthControllerTest` — health público, envelope 404, rota protegida não pública.
* `GlobalExceptionHandlerTest` — envelope 400/404/500 sem vazamento.

## Estrutura

```
backend/
├── pom.xml
├── Dockerfile              # build context = raiz do repo
├── README.md
└── src/
    ├── main/java/br/com/voupassar/
    │   ├── VoupassarApplication.java
    │   ├── health/         # GET /api/v1/health
    │   ├── exception/      # GlobalExceptionHandler + 400/404 de negócio
    │   ├── common/dto/     # ApiError (envelope)
    │   ├── config/         # CORS + CorrelationIdFilter (traceId)
    │   └── security/       # base permissiva só p/ health (JWT na 3.5)
    └── main/resources/
        ├── application.yml (+ dev/prod/test) + logback-spring.xml
        └── db/migration/   # gerado no build a partir de database/migrations
```

Próximas TASKs criam `exams/`, `questions/`, `auth/` etc. como pacotes
`controller → service → repository` — nunca regra em controller.
OpenAPI (springdoc) entra na TASK 3.2 com a primeira API real.

## Notas Boot 4 (para não redescobrir)

* `AutoConfigureMockMvc` vive em `spring-boot-starter-webmvc-test`
  (pacote `org.springframework.boot.webmvc.test.autoconfigure`).
* `@WithMockUser` no MockMvc exige `spring-boot-security-test`.
* Flyway só executa com `spring-boot-starter-flyway` (o `flyway-core`
  sozinho não registra o auto-configuration).
* Jackson 3 (`tools.jackson`): `ObjectMapper` é `tools.jackson.databind`;
  a propriedade `spring.jackson.serialization.*` do Jackson 2 não dá bind.
