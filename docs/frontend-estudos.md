# Área de estudos — VouPassar (TASK 6.5)

> Página protegida em `frontend/estudos.html` (+ `css/estudos.css` +
> `js/api/estudos.js` + `js/views/estudos.js`). Cobre TASKS.md 6.5:
> conteúdo, questões, progresso e navegação — prática livre em modo ESTUDO
> com correção imediata do servidor (AGENTS.md §9). Reutiliza todos os
> tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Cobertura (TASKS.md) → seções e fontes

| Exigido na TASK 6.5 | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Conteúdo | `#sec-conteudo-t` → `#study-browser` (disciplinas + top 12 assuntos, com contagem histórica) | `GET /disciplines`, `GET /topics?disciplineCode=` (TASK 3.3); `GET /subtopics?topicId=` alimenta o filtro |
| Questões | `#sec-questoes-t` → `#study-list` (cartões com enunciado, alternativas, gabarito e assunto) + `#study-pagination` | `GET /questions?disciplineCode=&topicId=&subtopicId=&year=&difficulty=&page=&size=10` + detalhe já incluso na página (TASK 3.4, ordem fixa ano/número/id) |
| Progresso | `aside` → `#study-progress` (geral + recorte atual) e `#study-plan` (item do roteiro no assunto) | `GET /performance/overview` (TASK 4.1); `GET /recommendations/plan` + `GET /diagnosis` p/ contexto (TASKs 4.2–4.4) |
| Navegação | `#study-filters` (5 selects + Filtrar/Limpar), botões `Estudar` do navegador, `← Anterior/Próxima →`, URL sincronizada (`?disciplina=&topico=&subtopico=&ano=&dificuldade=&pagina=`) | Estado local + `history.replaceState`; sem rota futura linkada (só `dashboard.html` e `index.html`) |

Prática livre (Modo Estudo, AGENTS.md §9): cada cartão tem `fieldset` de
alternativas + `Responder` / `Responder em branco`. A resposta abre (lazy)
`POST /study-sessions {mode: ESTUDO}` uma vez (id em memória +
`sessionStorage`) e registra `POST /attempts {questionId, selectedOption,
mode: ESTUDO, timeSpentSeconds, studySessionId}` (TASK 3.7). O servidor
corrige; o feedback mostra acerto/erro, resposta correta (`answerKey` da
questão carregada) e assunto — e
permite `Tentar novamente` (nova tentativa, fato imutável). `409
SESSION_CLOSED` reabre a sessão e repete uma vez.

## 2. Comportamentos

* **Guarda de auth:** `restoreSession()` (TASK 6.3). Sem sessão → `#study-guard`
  com vazio + `Entrar` (`login.html?next=estudos.html`) e `Criar conta`.
  `401` no meio da carga → mesmo painel. `Sair` (`[data-logout]`) revoga no
  servidor e volta ao login.
* **Carga isolada:** `Promise.allSettled` — disciplinas são obrigatórias;
  tópicos indisponíveis degradam para o catálogo vazio sem derrubar a página;
  overview/diagnosis/plano ausentes viram vazios honestos. Filtros 400/404 do
  backend viram alerta com `traceId` + botão `Limpar filtros` (nunca página
  vazia silenciosa).
* **Filtros dependentes:** trocar disciplina recarrega assuntos e limpa
  assunto/subassunto; trocar assunto recarrega subassuntos. Ano sem 2021
  (ausente do dataset — opção nem existe). Dificuldade só `FACIL/MEDIA/DIFICIL`.
* **Paginação:** `size=10` fixo (leitura confortável), `page` 0-based na URL
  como `pagina`. Anterior desabilitado na primeira; Próxima desabilitada na
  última (`first/last` do `PageResponse`).
* **Navegador → lista:** `Estudar` fixa disciplina + assunto, reseta página,
  sincroniza URL, recarrega lista/progresso/plano e rola até as questões.
* **Progresso reativo:** cada resposta agenda refetch do overview (+400 ms
  debounce) e atualiza navegador + progresso sem recarregar.

## 3. Honestidade (§4 — nada inventado)

* Anuladas: cartão desabilita resposta, conta como conteúdo, fora do
  aproveitamento, com nota explícita (regra de pontuação DESCONHECIDA,
  TASK 1.3 §4).
* Assuntos: classificação derivada, revisão humana PENDENTE (TASK 12.2) —
  linha `NÃO CONFIRMADO` quando sem vigente; notas da questão (máx. 4).
* `accuracy` NULL → `—` (nunca zero). Origem válida sem linhas = vazio
  legítimo com texto explicativo.
* `2021 ausente` citado nos filtros e nas notas. Sem preço/garantia/milhares
  (mesmo vocabulário proibido da landing). `hasFigure` vira aviso de figura
  no PDF-fonte (PDFs não redistribuídos, §12).

## 4. A11y, responsivo, Pages

* Semântico (`header/main/nav/section/aside`, `h1` único, `skip-link`),
  `fieldset/legend` por questão, radios com `label` clicável (alvo ≥44px),
  feedback em `role=status`, contagem e paginação com `role=status`,
  `alert` nos erros, foco visível e contraste herdados,
  `prefers-reduced-motion` herdado, `noscript` com aviso.
* Mobile-first: 1 coluna; `48rem` → filtros em 2 colunas; `64rem` → filtros
  em 3 colunas e grade `1fr 20rem` (conteúdo + lateral fixa).
* Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
  `API_BASE_URL` via `<meta>` (build injeta em 10.1). Zero dependências.

## 5. Verificação

```bash
for f in frontend/js/api/estudos.js frontend/js/views/estudos.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): sem sessão → painel de acesso; login →
# navegador com 2 disciplinas, filtros dependentes, lista paginada 10/pág,
# responder A (correção imediata), em branco (BLANK), anulada desabilitada,
# progresso atualiza, URL sincronizada, Limpar restaura.
```

Critérios 6.5: 4 blocos presentes e alimentados pela API real; guarda de
auth com `?next=` seguro; prática ESTUDO com correção do servidor e
`Tentar novamente`; paginação + URL; `node --check` OK; 200 no serve;
`check_frontend.py` OK estendido com cobertura estudos.

## 6. Correção pós-validação em navegador (2026-10-02, Playwright)

Bug real encontrado com usuário novo (sem `study_plan`): `loadAll()`
quebrava em `ReferenceError: i is not defined` (`estudos.js`, filtro de
`warnings` — `.filter(({ r }) => ... i ...)` sem desestruturar `i`) e a
página exibia erro fatal em vez do conteúdo. Todo usuário sem roteiro era
afetado (o `404` do plano é o caminho normal, não exceção). Correção em
uma linha (`.filter(({ r, i }) => ...)`); `console.error` de diagnóstico
mantido no `catch` de `loadAll`. Revalidado no navegador: filtros,
catálogo (2 disciplinas, 10 assuntos), lista paginada (240, 24 págs),
`2020 Q1` e plano ausente tratado como estado vazio — sem `pageerror`.
Conta de teste `pwtest_*@example.com` (id 20) + 1 tentativa mantidas no
banco dev (tentativas são imutáveis por trigger — remoção bloqueada por
desenho); concessão temporária de `CURATOR` revogada.
