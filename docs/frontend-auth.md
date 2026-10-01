# Autenticação no frontend — VouPassar (TASK 6.3)

> Telas de login, cadastro, recuperação e redefinição de senha, consumindo a
> API da TASK 3.5 (`docs/api-auth.md`). Zero dependências, ES Modules, Fetch,
> reutilizando todos os tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Páginas e fluxos

| Página | Form | Endpoint | Sucesso |
|---|---|---|---|
| `login.html` (+ `js/views/login.js`) | e-mail, senha, “manter conectado” | `POST /api/v1/auth/login` | Painel logado (nome + e-mail) ou segue `?next=` interno; dashboard real chega na TASK 6.4 |
| `cadastro.html` (+ `js/views/cadastro.js`) | nome, e-mail, senha + opcionais (ano escolar, ano da prova, objetivo) | `POST /api/v1/auth/register` | “Conta criada”, já conectado (papel STUDENT, perfil mínimo) |
| `recuperar-senha.html` (+ `js/views/recuperar-senha.js`) | e-mail | `POST /api/v1/auth/password/forgot` | Mensagem **sempre genérica** (exista ou não a conta) + link p/ redefinir |
| `redefinir-senha.html` (+ `js/views/redefinir-senha.js`) | token, nova senha + confirmação local | `POST /api/v1/auth/password/reset` | “Senha redefinida, sessões encerradas” + link p/ login; `?token=` preenche sozinho |

Módulos compartilhados:

* `js/api/auth.js` — `register/login/refreshSession/logout/logoutAll/`
  `forgotPassword/resetPassword/changePassword/fetchMe/restoreSession` +
  `normalizeEmail` (trim + lowercase, como o backend). Senha nunca em log.
* `js/state/session.js` — access **só em memória**; refresh em
  `sessionStorage` (padrão) ou `localStorage` (com “manter conectado”);
  `loadPersistedSession/setSession/clearSession/restore` + espelho do
  usuário em `state/store.js` (token nunca no store).
* `js/views/auth-shared.js` — validação cliente (espelha Bean Validation),
  `authErrorMessage()` por código do envelope, `renderAuthError()` (resumo +
  `traceId` + distribui `campo: msg` nos inputs), toggle Mostrar/Ocultar
  senha, `safeNextParam()` (só relativo interno, anti open-redirect),
  painel “já conectado” com Sair.
* `css/auth.css` — só layout do cartão estreito (`.auth-wrap/.auth-card`),
  campo de senha com toggle e links; todo o resto vem do design system.

## 2. Validação (cliente falha rápido; servidor é fonte da verdade)

E-mail obrigatório/`@`/≤254 · senha 8–72 · nome 2–80 · ano escolar ≤40 ·
ano da prova inteiro 2000–2100 · objetivo ≤500 · token obrigatório ·
confirmação deve coincidir (só local, não enviada). Erros usam
`setFieldError` (`aria-invalid` + `aria-describedby`) + resumo com foco
(WCAG 3.3.1); loading preserva rótulo p/ leitor de tela.

## 3. Erros traduzidos (sem vazar existência de conta)

`INVALID_CREDENTIALS` → “E-mail ou senha inválidos.” (genérico, sem marcar
campo) · `EMAIL_IN_USE` (409) → orienta entrar/recuperar ·
`INVALID_OR_EXPIRED_TOKEN` → “peça um novo link” · `REFRESH_REUSED` →
re-login · `SAME_PASSWORD` · `RATE_LIMITED`/429 · `VALIDATION_ERROR` com
detalhes por campo · 5xx/rede via `friendlyMessage`. `traceId` exibido
quando o envelope traz.

## 4. Sessão e segurança (§15)

Access em memória (`js/api/client.js` injeta `Bearer`). `fetch` com
`credentials: "same-origin"` (padrão do `request()` desde a 6.3): o MVP não
usa cookies e o backend não responde `Access-Control-Allow-Credentials`, de
modo que `"include"` reprovava o preflight cross-origin (Pages × API) —
verificado em navegador (cadastro e login reais). Quando o refresh passar a
cookie HttpOnly + CORS com `allowCredentials`, passar `credentials:
"include"` na chamada específica. Refresh rotativo: `restoreSession()` renova uma vez após 401 e
repete `/me`; reuso derruba a cadeia no servidor (`REFRESH_REUSED`).
Logout revoga no servidor e limpa os dois armazenamentos; “Sair desta
conta” está no painel “já conectado”. Sem `innerHTML` com dados (só
`textContent` via `el()`), sem senha em storage/log, sem URL de API fora
de `config.js`, sem segredo no frontend. `?next=` restrito a relativo.

## 5. Honestidade (§4 — nada inventado)

* Recuperação: alerta visível de que a **entrega por e-mail está pendente**
  de provedor transacional (mesma pendência de `docs/api-auth.md`); em
  desenvolvimento o token precisa ser obtido no banco.
* Pós-login/cadastro: painel informa que **dashboard, diagnóstico e roteiro
  chegam nas TASKs 6.4+** — nenhuma tela futura é linkada como existente.
* Troca de senha autenticada (`/password/change`) existe no `auth.js` para
  a futura tela de perfil (TASK 6.8); não há tela própria nesta task.

## 6. A11y, responsivo, Pages

Semântico (`header/main/nav`, `h1` único, `skip-link`), labels reais,
`autocomplete` (email/current/new/one-time-code), foco visível herdado,
contraste AA herdado, alvos ≥44px, `prefers-reduced-motion` herdado,
teclado nativo. Mobile-first: cartão `29rem` centralizado, 1 breakpoint em
`48rem`. Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
`API_BASE_URL` via `<meta>` (build injeta em 10.1).

## 7. Verificação

```bash
for f in frontend/js/state/session.js frontend/js/api/auth.js \
  frontend/js/views/auth-shared.js frontend/js/views/login.js \
  frontend/js/views/cadastro.js frontend/js/views/recuperar-senha.js \
  frontend/js/views/redefinir-senha.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): cadastrar → 201, duplicado → 409,
# login errado → 401 genérico, forgot → 200 genérico p/ existente e
# inexistente, reset válido → 200 + reuso → 400, refresh rotaciona.
```

Critérios 6.3: 4 páginas com forms funcionais contra a API real; validações
espelhadas; erros traduzidos com rastreio; sessão em memória + refresh
persistido com opção; `?token=` e `?next=` tratados; `node --check` OK;
200 no serve; `check_frontend.py` OK estendido com cobertura auth.

> **Nota de ambiente (descoberta em teste real):** o `docker-compose.yml`
> fixa `CORS_ALLOWED_ORIGINS` só em `http://localhost:8080` (o default do
> código inclui também `:3000`). Para testar as páginas em navegador com
> `python3 -m http.server 3000 --directory frontend`, subir a API com
> `CORS_ALLOWED_ORIGINS="http://localhost:8080,http://localhost:3000"
> docker compose up -d app`. Em produção, fixar a origem exata do GitHub
> Pages nessa variável.
