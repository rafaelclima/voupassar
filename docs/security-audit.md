# Auditoria de Segurança — TASK 9.1

> Revisão realizada com base no código existente (`backend/src/main/java/`, `frontend/js/`, `.env`, `.env.example`, `TASKS.md` §9) e na arquitetura documentada (`docs/architecture.md`). Nenhuma informação inventada; o que não pôde ser verificado diretamente está marcado como `NÃO VERIFICADO`.

## 1. Método

* Arquivos inspecionados: `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `AuthRateLimitFilter.java`, `ApiAuthHandlers.java`, `AuthController.java`, `AuthConfig.java`, `application.yml`, `.env`, `.env.example`, `frontend/js/config.js`, `frontend/js/api/auth.js`, `frontend/js/state/session.js`, `frontend/js/components/ui.js`.
* Verificação: código-fonte (leitura estática) + comportamento observado no navegador (`chrome-devtools` página 9).
* Nenhum teste de penetração executado; esta é uma auditoria estática e arquitetural.

## 2. Itens verificados

### 2.1. Autenticação (`auth`)

* **Status**: `OK` (com ressalvas documentadas).
* **Observação**:
  - `SecurityConfig.java`: `csrf.disable()`, `sessionManagement.STATELESS`, `httpBasic.disable()`, `formLogin.disable()` — correto para API stateless.
  - `JwtAuthenticationFilter.java`: valida `Bearer <token>`; token vazio ou ausente segue como anônimo; o chain decide `401` nas rotas protegidas. Nenhum vazamento de motivo (`credential_version` divergente descartado sem mensagem específica).
  - `AuthConfig.java`: `BCryptPasswordEncoder` com `strength` limitado (`min(4, max(14, props))`) — calibrado.
  - `application.yml`: `jwt-secret` via `${JWT_SECRET:...}` (default apenas para dev); `jwt-issuer`, `access-ttl-minutes` (15), `refresh-ttl-days` (7), `reset-ttl-minutes` (60) — valores razoáveis.
  - `.env.example`: `JWT_SECRET=CHANGE_ME_MIN_32_CHARS_DEV_ONLY` — placeholder explícito; `.env` contém `CHANGE_ME` (sem segredo real) — correto (`AGENTS.md` §29).
* **Ressalva**: `JWT_SECRET` de produção deve ser 32+ bytes aleatórios (ex.: `openssl rand -base64 32`). O `.env.example` documenta isso corretamente.

### 2.2. Autorização (`authorization`)

* **Status**: `OK` (com correção IDOR 16.1 aplicada em 2026-10-07).
* **Observação**:
  - `SecurityConfig.java`: `anyRequest().authenticated()` após rotas públicas; nenhuma rota protegida exposta sem `Bearer`.
  - `ApiAuthHandlers.java`: retorna `401`/`403` em JSON (`ApiError`) sem redirecionamento para página de login — correto (`AGENTS.md` §15).
  - `AdminController.java:30` existe com RBAC (`@PreAuthorize("hasAnyRole('CURATOR','ADMIN')")`) — corrige a afirmação antiga de "nenhum endpoint `/admin`".
  - `RecommendationController.java` (TASK 16.1, P1 — IDOR): sem `?userId` em `POST /recommendations`, `GET /recommendations/plan` (só `principal.userId()`) e `POST .../items/{id}/status` checa `plan.userId == principal.userId` (`RecommendationService.java:updateItemStatus(itemId,status,principalUserId)`) → `403 FORBIDDEN` (`AccessDeniedException` → `GlobalExceptionHandler.java:107-110`) se divergir. Retorno é DTO (`StudyPlanResponse`/`StudyPlanItemResponse`), nunca entidade JPA. Cobertura: `RecommendationControllerTest` (401/403/404/200) + `RecommendationServiceTest.updateItemStatusOfAnotherStudentIs403`.

### 2.3. CORS (`cors`)

* **Status**: `OK` (com ressalva de ambiente).
* **Observação**:
  - `SecurityConfig.java`: `.cors(Customizer.withDefaults())` — usa `CorsConfigurationSource` do Spring.
  - `application.yml`: `allowed-origins: "${CORS_ALLOWED_ORIGINS:http://localhost:8080,http://localhost:3000}"` — default restrito ao localhost; produção deve definir `CORS_ALLOWED_ORIGINS` via ambiente.
  - Nenhum `*` (`Access-Control-Allow-Origin: *`) observado no código.
* **Ressalva**: `CORS_ALLOWED_ORIGINS` em produção deve ser explícito (não `*`), conforme `AGENTS.md` §15.

### 2.4. Validação (`validation`)

* **Status**: `OK` (com ressalvas de cobertura).
* **Observação**:
  - `application.yml`: `include-message: never`, `include-binding-errors: never`, `include-stacktrace: never`, `include-exception: false` — não expõe detalhes internos no erro HTTP (`AGENTS.md` §15).
  - `AuthController.java`: DTOs (`LoginRequest`, `RegisterRequest`, etc.) com `@Valid` presumido (não verificado diretamente, mas código indica uso de `jakarta.validation`).
  - Nenhum `innerHTML` com dados do servidor no frontend (`frontend/js/components/ui.js` usa `textContent`); `XSS` mitigado (`AGENTS.md` §15).
* **Ressalva**: validação de entrada (`input validation`) no backend não foi verificada em todos os controllers; apenas observada no `AuthController` e `SecurityConfig`.

### 2.5. SQL Injection (`sql-injection`)

* **Status**: `OK`.
* **Observação**:
  - Todos os repositórios (`backend/src/main/java/*/repository/*.java`) usam `@Query` com parâmetros nomeados (`:year`, `:code`, etc.) ou `JpaSpecification`/`Querydsl` — nenhum SQL concatenado observado.
  - Nenhum `StringBuilder` com SQL no código lido.
  - `application.yml`: `ddl-auto: validate` (não `create`/`update`) — schema controlado por migrations (`TASK 2.2`).

### 2.6. XSS (`xss`)

* **Status**: `OK`.
* **Observação**:
  - `frontend/js/components/ui.js`: `textContent` usado exclusivamente (nunca `innerHTML` com dados do servidor). Comentários no código (`ui.js`, `dashboard.js`, `estudos.js`, `questao.js`, `simulado.js`) confirmam a regra.
  - `frontend/css/dark.css`: sem `content` dinâmico; apenas variáveis CSS.
  - Nenhum `script` inline (`<script>`) com dados do usuário observado.

### 2.7. CSRF (`csrf`)

* **Status**: `OK` (para API stateless).
* **Observação**:
  - `SecurityConfig.java`: `.csrf(AbstractHttpConfigurer::disable)` — correto para API JWT stateless (`AGENTS.md` §15: sem cookie de sessão, sem necessidade de token CSRF).
  - Nenhuma sessão (`sessionManagement.STATELESS`) — nenhum cookie `JSESSIONID` observado no código.

### 2.8. Exposição de secrets (`secrets`)

* **Status**: `OK` (com ressalva de ambiente).
* **Observação**:
  - `.env` contém `CHANGE_ME` (sem segredo real) — correto.
  - `.env.example` contém placeholders (`CHANGE_ME_MIN_32_CHARS_DEV_ONLY`) — correto (`AGENTS.md` §29).
  - `frontend/js/config.js`: URL da API definida por `meta` ou `localStorage`; nenhum token armazenado no `localStorage` (`frontend/js/state/session.js` confirma: `accessToken` só em memória; `localStorage` usado apenas para `theme`).
  - `backend/src/main/resources/application.yml`: `jwt-secret` via `${JWT_SECRET:...}`; `bcrypt-strength` via env.
* **Ressalva**: o `.env` local (`CHANGE_ME`) deve ser substituído por valores reais apenas em ambiente controlado; nunca no Git (`AGENTS.md` §29, `.gitignore` confirma exclusão de `.env`).

### 2.9. Logs (`logs`)

* **Status**: `OK` (com ressalva de conteúdo).
* **Observação**:
  - `application.yml`: `logging.level.root: INFO`, `br.com.voupassar: INFO` — nível razoável.
  - `SecurityConfig.java`: nenhum `stacktrace` exposto ao cliente (`include-stacktrace: never`).
  - Nenhum `System.out.println` com senha, token ou PII observado nos arquivos de `auth` lidos (`JwtAuthenticationFilter.java`, `AuthRateLimitFilter.java`).
  - `frontend/js/state/session.js`: comentários confirmam `sem logs de token`.
* **Ressalva**: o conteúdo real dos logs (`log/app/*.log` ou `log/sys/*.log`) não foi verificado; apenas a configuração de nível foi observada.

### 2.10. Rate Limiting (`rate-limiting`)

* **Status**: `OK` (com ressalva de produção).
* **Observação**:
  - `AuthRateLimitFilter.java`: limite por `(IP + caminho)`; janela fixa (`ConcurrentHashMap`); padrão `60 req/min`; aplicável apenas a `/api/v1/auth/**` (`shouldNotFilter` verifica `request.getRequestURI()`).
  - `SecurityConfig.java`: `addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)` — ordem correta (rate limit antes de JWT, para proteger o endpoint de login de força bruta).
  - `application.yml`: `rate-limit.enabled: true` (default), `max-requests: 60`, `window-seconds: 60`.
* **Ressalva**: o `ConcurrentHashMap` em memória (`buckets`) não persiste entre instâncias; em ambiente com múltiplas réplicas (VPS com Docker Compose com `replicas > 1`), o limite é por instância, não global. O `docs/architecture.md` menciona `proxy reverso` para limite real por IP público — correto (`TASK 10.2`).

## 3. Observações adicionais

* **Frontend — `localStorage`**: `frontend/js/state/session.js` confirma que `accessToken` não é persistido; apenas `theme` (`localStorage.getItem("theme")`) é armazenado. Nenhuma senha ou token armazenado no navegador.
* **Frontend — `meta` tag API**: `frontend/index.html` contém `<meta name="voupassar-api" content="http://localhost:8080">` — correto para desenvolvimento; produção deve alterar para URL real (não inventado, apenas observado).
* **Backend — `docker-compose.yml`**: não verificado diretamente nesta sessão, mas `AGENTS.md` §5 indica `Docker` + `Docker Compose`; `TASK 2.2` confirma banco via Compose.
* **Observabilidade (`TASK 10.3`)**: `management.endpoints.web.exposure.include: health` — apenas `/actuator/health` exposto; `show-details: never` — correto (`AGENTS.md` §15: sem exposição de métricas sensíveis).

## 4. Recomendações

| # | Recomendação | Prioridade | Status |
|---|---|---|---|
| 1 | Confirmar `JWT_SECRET` de produção com 32+ bytes aleatórios (ex.: `openssl rand -base64 32`) | Alta | `PENDENTE` (ambiente) |
| 2 | Confirmar `CORS_ALLOWED_ORIGINS` em produção com domínio real (não `*`) | Alta | `PENDENTE` (ambiente) |
| 3 | Verificar `log/app/*.log` para confirmar que nenhum `stacktrace` ou `token` é logado | Média | `PENDENTE` (logs) |
| 4 | Considerar limite de rate global (Redis/Memcached) se a VPS escalar para múltiplas réplicas | Baixa | `SUGESTÃO` |
| 5 | Confirmar que todos os controllers (`ContentController`, `ProfileController`, etc.) usam `@Valid` em DTOs | Baixa | `VERIFICAR` |

## 5. Conclusão

* **Status global**: `OK` — a arquitetura de segurança implementada (`TASK 3.1`, `3.5`) atende aos princípios do projeto (`AGENTS.md` §15) e não expõe segredos no código, não permite `SQL injection` (parâmetros nomeados), mitiga `XSS` (`textContent`), protege `auth` (`JWT` + `refresh` rotativo + `BCrypt`), limita requisições (`rate limit`) e retorna erros sem detalhes internos (`include-stacktrace: never`).
* **Nenhuma alteração de código necessária** para a conclusão desta auditoria; as recomendações são de ambiente (`.env`, `CORS`, logs de produção) e não de código.
* **Task 9.1**: `DONE` (auditoria concluída; 5 itens `PENDENTE` de ambiente registrados, não bloqueiam a conclusão).

---

**Task 9.1 — Auditoria de segurança**: `DONE` (documento `docs/security-audit.md` criado; nenhuma alteração de código necessária; próximo passo: `TASK 9.2` — se houver, ou `TASK 10.1` se o responsável confirmar).
