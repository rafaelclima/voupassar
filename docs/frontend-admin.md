# Administração — VouPassar (TASK 12.1, somente leitura desde 2026-10-06)

> Interface web da área administrativa em `frontend/admin.html`
> (+ `css/admin.css` + `js/api/admin.js` + `js/views/admin.js`).
> Fecha a pendência honesta de `docs/api-admin.md` §4
> ("Tela web de admin — API pronta, UI futura").
> A API real é **somente leitura**: 2 GETs (`/admin/inconsistencies`,
> `/admin/metrics`, `CURATOR`/`ADMIN`, `AdminController.java:30,45-60`;
> testes `AdminServiceTest` + `AdminSecurityTest`) — **não existe**
> `GET /admin/review-queue` nem `PATCH` de questão/classificação desde a
> decisão de produto 2026-10-06 (`docs/api-admin.md:8-10`; histórico do
> contrato anterior em `docs/curadoria.md`, superseded). Este doc descreve
> só o que existe; qualquer menção anterior a fila/PATCH está superada.
> Reutiliza todos os tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Cobertura (TASKS.md) → seções e fontes

| Capacidade | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Visualizar inconsistências | `#sec-inc-t` (4 checagens com contagem + amostra de ids) | `GET /admin/inconsistencies` (`AdminController.java:45-50`) |
| Consultar métricas | `#sec-metrics-t` (stat cards + tabelas, sem mapas por status) | `GET /admin/metrics` (`AdminController.java:52-60`) |
| Leitura honesta dos dados | `#sec-notes-t` (notas: 2021 ausente, sem preço/garantia) | texto estático + `GET /admin/metrics` |
O conteúdo integral (enunciado, alternativas, proveniência) continua no
`GET /questions/{id}` existente — cada inconsistência linka
`questao.html?id=` para conferência contra o caderno.
Não há fila de revisão, forms de curadoria, nem escrita de qualquer tipo
(`frontend/js/api/admin.js:11-17` expõe só `fetchInconsistencies` +
`fetchAdminMetrics`; `frontend/js/views/admin.js:1-12` declara "sem nenhuma
escrita").

## 2. Comportamentos

* **Guarda em duas camadas:** `restoreSession()` (TASK 6.3). Sem sessão →
  `#admin-guard` com vazio + `Entrar` (`login.html?next=admin.html`) e
  `Criar conta`. Com sessão mas sem `CURATOR`/`ADMIN` em `user.roles`
  (`GET /auth/me`) → `#admin-forbidden` (403 honesto: concessão é SQL por
  ADMIN, sem autopromoção — `docs/api-admin.md` §0). `401`/`403` no meio da
  carga caem nos mesmos painéis (sessão expirada / papel revogado).
* **Carga paralela:** `Promise.allSettled` (métricas + inconsistências)
  — cada seção falha isolada; o resto continua (`frontend/js/views/admin.js`,
  `loadAll`). Falhas viram resumo no topo (`#admin-error`, com `traceId`)
  + vazio na seção.
* **Sem escrita:** não há filtro de status de curadoria, paginação de fila,
  forms inline, `toast` de salvamento nem `400` de regra de publicação —
  tudo isso pertencia ao contrato anterior (ver `docs/curadoria.md`,
  superseded) e foi removido com os gates humanos na
  `V12__drop_review_gates.sql`. A página recarrega métricas +
  inconsistências; contar e listar é tudo o que ela faz.

## 3. Honestidade (§4 — nada inventado)

* Observabilidade, nunca curadoria: a página conta e lista, nunca cria
  enunciado, gabarito, assunto, status ou revisão.
* Positivo nas inconsistências é fila de trabalho, nunca deleção
  automática (AGENTS.md §23).
* `2021 ausente` citado nas notas. Sem preço/garantia/milhares (mesmo
  vocabulário proibido da landing).
* A página não é linkada nos navs de estudante (dashboard/estudos/questão/
  simulado/perfil) de propósito: é acesso direto por conta autorizada,
  sem expor curadoria a quem não tem papel.

## 4. A11y, responsivo, Pages

* Semântico (`header/main/nav/section`, `h1` único, `skip-link`), tabelas
  de métricas com `caption` em região rolável, `alert/status/note` nos
  lugares certos, `label` em todo `select`/`textarea` (ids únicos por
  item), foco visível e contraste herdados, alvos ≥44px,
  `prefers-reduced-motion` herdado, `noscript` com aviso.
* Mobile-first: 1 coluna; `48rem` → stats em 2 colunas e curadoria lado a
  lado; `64rem` → 4 stats. Só tabelas rolam internamente.
* Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
  `API_BASE_URL` via `<meta>` (build injeta em 10.1). Zero dependências.
  Tema escuro via `main.js` (automático, sem CSS extra).

## 5. Verificação

```bash
for f in frontend/js/api/admin.js frontend/js/views/admin.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): sem sessão → painel de acesso; STUDENT →
# painel 403 honesto; CURATOR/ADMIN → métricas + 4 checagens zeradas;
# conta sem papel nunca vê conteúdo admin. Sem fila, sem PATCH.
```

Critérios 12.1 (UI, escopo pós-2026-10-06): 2 capacidades operáveis pela
API real (métricas + inconsistências); guarda de auth + guarda de papel
com `?next=` seguro; `node --check` OK; 200 no serve;
`check_frontend.py` OK. Backend inalterado (`AdminServiceTest` +
`AdminSecurityTest` seguem verdes).

> Validação em navegador real — estado anterior (2026-10-02, Playwright,
> contrato com fila/PATCH, hoje superseded por `docs/curadoria.md` e pela
> V12): sem sessão → painel de acesso com `login.html?next=admin.html`;
> STUDENT → painel 403 honesto (sem autopromoção); CURATOR → métricas
> (240/5/36), 4 checagens, fila PENDING paginada. **Pendente revalidar o
> somente-leitura atual** (métricas + inconsistências, sem fila/PATCH) em
> navegador real; mobile 360px + console limpo. Notas de ambiente: (1) o
> backend em execução precedia a API admin (jar de 01/10) — rebuild da
> imagem `voupassar-backend` + restart resolveu; (2) CORS local exige
> `CORS_ALLOWED_ORIGINS` com a origem do serve de teste (env, sem mudança
> de código).
