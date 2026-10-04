# Administração — VouPassar (TASK 12.1)

> Interface web da área administrativa em `frontend/admin.html`
> (+ `css/admin.css` + `js/api/admin.js` + `js/views/admin.js`).
> Fecha a pendência honesta de `docs/api-admin.md` §4
> ("Tela web de admin — API pronta, UI futura").
> A API (`GET review-queue/inconsistencies/metrics`, dois `PATCHes`,
> `CURATOR`/`ADMIN`, testes `AdminServiceTest` + `AdminSecurityTest`)
> já existia e **não foi alterada** — aqui só a UI que a opera.
> Reutiliza todos os tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Cobertura (TASKS.md) → seções e fontes

| Exigido na TASK 12.1 | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Revisar questões | `#sec-queue-t` (fila + form "Status da questão" por item + `Ver questão`) | `GET /admin/review-queue` + `PATCH /admin/questions/{id}/status` |
| Revisar classificações | form "Classificação" por item (REVIEWED/APPROVED/REJECTED + observação) | `PATCH /admin/classifications/{id}` |
| Alterar publicação | select de publicação no mesmo form (`PENDENTE_REVISAO/SOMENTE_REFERENCIA/PUBLICAVEL/NAO_PUBLICAVEL`) | mesmo `PATCH` de questão (regra `PUBLICAVEL exige APPROVED` no servidor) |
| Visualizar inconsistências | `#sec-inc-t` (4 checagens com contagem + amostra de ids) | `GET /admin/inconsistencies` |
| Consultar métricas | `#sec-metrics-t` (4 stat cards + 3 tabelas por status) | `GET /admin/metrics` |
O conteúdo integral (enunciado, alternativas, proveniência) continua no
`GET /questions/{id}` existente — cada item da fila linka
`questao.html?id=` para conferência contra o caderno antes de aprovar.
A fila mostra só o necessário para priorizar (origem, disciplina,
gabarito, assunto vigente, confiança, os dois status).

## 2. Comportamentos

* **Guarda em duas camadas:** `restoreSession()` (TASK 6.3). Sem sessão →
  `#admin-guard` com vazio + `Entrar` (`login.html?next=admin.html`) e
  `Criar conta`. Com sessão mas sem `CURATOR`/`ADMIN` em `user.roles`
  (`GET /auth/me`) → `#admin-forbidden` (403 honesto: concessão é SQL por
  ADMIN, sem autopromoção — `docs/api-admin.md` §0). `401`/`403` no meio da
  carga caem nos mesmos painéis (sessão expirada / papel revogado).
* **Carga paralela:** `Promise.allSettled` (métricas + inconsistências +
  fila) — cada seção falha isolada; o resto continua. Falhas viram resumo
  no topo (`#admin-error`, com `traceId`) + vazio na seção.
* **Fila:** filtro `validationStatus` da questão (`PENDING/REVIEWED/APPROVED/
  REJECTED`, padrão `PENDING`) **e** filtro `classificationStatus` da
  classificação ativa (`Todas/PENDING/REVIEWED/APPROVED/REJECTED`) + paginação
  (`page` 0-based, `size` 20, anterior/próxima com `disabled` nos limites).
  O filtro de classificação é o que torna a curadoria operável de fato: sem
  ele, as 39 classificações em REVIEWED ficam espalhadas por 240 itens.
  Recarrega só a fila ao filtrar ou paginar, com `role=status` na contagem.
* **Curadoria:** cada item tem dois forms inline. Cada item mostra também
  `Observação IA` e `Evidência` (campos novos do `ReviewQueueItemResponse`
  na TASK 12.2) — o revisor decide sem abrir a questão. Estado vigente fora
  da lista de transições aparece como option desativado `X (vigente)`, nunca
  select vazio. Salvar mostra loading no botão, `toast` de confirmação e
  recarrega fila + métricas (contagens continuam honestas após a escrita).
  Erro `400` do servidor (ex. `PUBLICAVEL sem APPROVED`, regressão de
  `APPROVED`) aparece via `friendlyMessage` — a regra vive no backend, o hint
  textual no form só resume.
* **Item rejeitado continua legível:** a fila do admin mostra a classificação
  mais recente de cada questão, inclusive `REJECTED` (a API pública de
  conteúdo e o motor de recomendação seguem usando só as não-rejeitadas). Sem
  isso o item apareceria sem classificação e a UI afirmaria falsamente
  "sem classificação ativa".
* **Sem classificação ativa** (`classificationId` nulo) → nota de
  inconsistência no lugar do form (nunca form quebrado nem assunto
  inventado).

## 3. Honestidade (§4 — nada inventado)

* Curadoria revisa, nunca inventa: nenhum enunciado, gabarito ou assunto
  é criado aqui — só status e revisão com observação.
* Nada publicado sem revisão: `PUBLICAVEL` exige `APPROVED` (servidor
  responde `400` caso contrário); 240 questões seguem `PENDENTE_REVISAO`
  até curadoria (TASK 12.2).
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
# painel 403 honesto; CURATOR/ADMIN → métricas (240/5/36), 4 checagens
# zeradas, fila PENDING paginada; PATCH questão/classificação com toast e
# recarga de fila + métricas; conta sem papel nunca vê conteúdo admin.
```

Critérios 12.1 (UI): 5 capacidades operáveis pela API real; guarda de
auth + guarda de papel com `?next=` seguro; `node --check` OK; 200 no
serve; `check_frontend.py` OK estendido com cobertura admin. Backend
inalterado (`AdminServiceTest` + `AdminSecurityTest` seguem verdes).

> Validação em navegador real — RESOLVIDA (2026-10-02, Playwright):
> sem sessão → painel de acesso com `login.html?next=admin.html`;
> STUDENT → painel 403 honesto (sem autopromoção); CURATOR → métricas
> (240/5/36), 4 checagens, fila PENDING paginada (240, 12 págs) e forms de
> curadoria; `PATCH .../status` com `PUBLICAVEL` sem `APPROVED` → `400`
> com mensagem amigável (sem mutação). Mobile 360px e desktop 1280px OK,
> console sem erros inesperados. Notas de ambiente: (1) o backend em
> execução precedia a API admin (jar de 01/10) — rebuild da imagem
> `voupassar-backend` + restart resolveu; (2) CORS local exige
> `CORS_ALLOWED_ORIGINS` com `http://localhost:8888` (env de teste, sem
> mudança de código). Pendência anterior (MCP indisponível) substituída
> por esta validação.
>
> Revalidação da TASK 12.2 (2026-10-02, Playwright): filtro
> `classificationStatus` — `REVIEWED` → "39 questão(ões) … página 1 de 2",
> `APPROVED` → "200 … página 1 de 10", `REJECTED` → "1 … página 1 de 1",
> todos batendo com `GET /admin/metrics`. Item `REJECTED` (id 120 = 2023 Q40)
> exibindo assunto `ARITMETICA/DIVISIBILIDADE_MMC_MDC`, confiança e a
> observação do conflito com o gabarito — sem o falso aviso de "sem
> classificação". Select de revisão com `REJECTED` selecionado. 360px sem
> overflow horizontal; console limpo.
