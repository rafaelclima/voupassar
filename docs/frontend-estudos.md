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
| Navegação | `#study-filters` (6 selects + Filtrar/Limpar), botões `Estudar` do navegador, `← Anterior/Próxima →`, URL sincronizada (`?disciplina=&topico=&subtopico=&ano=&dificuldade=&fonte=&pagina=`) | Estado local + `history.replaceState`; sem rota futura linkada (só `dashboard.html` e `index.html`) |

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
* **Filtros dependentes + lista automática (U1/U2 2026-10-05):** trocar
  disciplina/assunto/subassunto/ano/dificuldade lista sozinho (1 passo,
  com `syncUrl()` + `loadQuestions()` + progresso/plano/hero). O botão
  `Filtrar` segue como ação explícita + rolagem até o caderno (teclado/AT).
  Trocar disciplina recarrega assuntos e limpa assunto/subassunto; trocar
  assunto recarrega subassuntos. Ano sem 2021 (ausente do dataset — opção
  nem existe). Dificuldade só `FACIL/MEDIA/DIFICIL`.
* **Abertura com conteúdo (U2):** sem recorte na URL, a carga aplica um
  recorte padrão (item DOING/TODO do roteiro, senão 1ª disciplina) via
  `applyDefaultRecorte()` — o caderno abre sozinho (`?disciplina=…` na URL).
  `Limpar` volta ao mesmo padrão, nunca ao estado-guia vazio.
* **Navegador paginado (U2):** 12 assuntos por disciplina por padrão, com
  `Mostrar mais N assuntos` (+12) e `Mostrar menos` (volta a 12) — só UI,
  sem novo fetch. Contador `N disciplinas · M assuntos` preservado.
* **Recorte visível + paginação espelhada + assunto ativo (U3 2026-10-05):**
  o topo do caderno mostra `Recorte atual: …` (disciplina · assunto ·
  subassunto · edição · dificuldade, nomes do catálogo + `difficultyLabel`,
  nunca código cru) com `Ajustar filtros` (`#sec-filtros-t`, com
  `scroll-margin` do header). A paginação existe no topo
  (`#study-pagination-top`, compacta, sem `role=status` duplo) e na base
  (anúncio mantido); ambas chamam o mesmo `fillPager()`. No navegador, o
  assunto do filtro atual vira linha `--active` com botão `Atual`
  desabilitado (`aria-current="true"`). Só UI — sem novo fetch, sem
  inventar conteúdo.
* **Correção junto (U3):** trocar assunto/subassunto pelo select não
  filtrava — o `change` lia a seleção depois de `refreshDependentSelects()`
  reconstruir as opções (a escolha nova virava `""`; só `Filtrar` e
  `Estudar` funcionavam). Agora o handler captura `topicId`/`subtopicId`
  antes do refresh; validado no navegador (assunto `2` → URL `topico=2`,
  60 questões, linha ativa).
* **Ajustar filtros com foco gerenciado (U4 2026-10-05):** o link
  `#study-ajustar` era âncora pura — rolava até `#sec-filtros-t` sem
  mover o foco (o `tabindex="-1"` de U3 ficava morto; teclado/AT
  permanecia no caderno). Agora `bindAjustarFiltros()` rola + foca o
  título dos filtros (mesmo padrão da paginação → `#sec-questoes-t`),
  com `preventScroll` + respeito a modificadores (nova aba intacta).
  Só UI — sem novo fetch, sem mudar filtros, sem inventar conteúdo.
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
  TASK 1.3 §4). Feedback U2: “O IFRN não informa como pontuar questões
  anuladas.” (sem `o` minúsculo).
* Vazio sem resultado (U2): “Tente outra disciplina, assunto ou edição…”
  + botão `Limpar filtros` (antes: jargão “Origem válida sem linhas…”).
  Erro de lista (U2): “Se precisar de ajuda, anote este código: …” (antes:
  “Código de rastreio: …”).
* `No seu roteiro` sem match (U2): explica que dá para praticar mesmo assim
  + `Ver roteiro completo` + `Abrir assunto do roteiro` (quando há DOING/TODO).
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

## 7. Revisão guiada (TASK 17.2 — fila priorizada)

Seção `#sec-revisao-t` na mesma página, com dono próprio
(`js/views/revisao.js`; `estudos.js` só chama `initReviewSection()` após
auth + conteúdo — sem ciclo). Fonte: `GET /review/queue` + `POST
/review/sessions` via `js/api/review.js` (TASK 17.1). Ordem e motivos vivem
no backend; o frontend nunca reordena.

* **Filtros** (`#revisao-filters`): `#r-disciplina`, `#r-topico`
  (dependente, via `GET /topics?disciplineCode=`), `#r-so-erros` (só baldes
  0–1, `ERRO_SEM_ACERTO` + `ERRO_RECENTE`), `Buscar fila`/`Limpar`. Trocar
  lista sozinho (mesmo padrão U2 dos filtros de questões); `Buscar` é a ação
  explícita + rolagem. Filtros locais (sem URL), exceto a leitura inicial.
* **Linha do item** (nunca o `reason` bruto nem `notes` — ver § Honestidade):
  `#rank` + selo da categoria (`reviewCategoryLabel`) + origem (`2026 Q17 ·
  Matemática`, nomes da API) + dica (`reviewCategoryHint`) + fatos
  (`X/Y nesta questão (Z%) · última incorreta há N dias`, `daysSince`
  informativo, omitido quando NULL) + assunto (`topicName ·
  masteryLabel(topicMastery) · você X% em N`, ou "ainda sem classificação")
  + `Abrir questão` (`questao.html?id=…&voltar=…?aba=revisao`).
* **Estados:** loading (`Buscando fila…`), vazio honesto em conta nova
  (`Nada para revisar ainda` — responder no Estudo/simulados alimenta a fila;
  o "3+" citado é o sinal mínimo do diagnóstico, nunca promessa de item
  oculto), vazio de filtro (`Nada neste filtro` + limpar), erro com
  `Tentar de novo` por seção (`renderErrorWithRetry`, traceId; 401 vira
  convite a entrar de novo — o interceptor do `client.js` já tentou 1
  refresh).
* **Iniciar revisão (N):** congela exatamente o top-N exibido
  (`createSession({limit: N, …filtros})`, mesma ordem da fila) e navega para
  `simulado.html?review=<sessionId>` (execução completa na 17.3: caderno com
  feedback imediato + resultado + volta à fila — ver
  `docs/frontend-revisao.md`). Fila esvaziada na corrida (`400
  NO_REVIEW_ITEMS`) recarrega em vez de travar.
* **Atalhos:** `estudos.html?aba=revisao` rola + foca a seção (hub do
  simulado em modo Revisão; `&disciplina=` pré-seleciona o filtro). O hub
  ganha `REVISAO` nos dois selects de modo **como roteamento, não como
  simulação**: o backend rejeita REVISAO em simulados (`400 INVALID_MODE`,
  `SimulationService` MODES = ESTUDO/PROVA), então escolher Revisão abre a
  fila — quantidade/dificuldade/origem não seguem (a revisão tem filtros
  próprios).

## 8. Motivo do roteiro + fila filtrada por assunto (TASK 18.3)

* **Mesmo motivo do painel:** o hero (`renderHero`) e o bloco `No seu
  roteiro` (`renderPlan`) usam `evidenceLine(parseEvidence(item))` de
  `js/components/plan-evidence.js` — a mesma frase do dashboard para o
  mesmo item (reuso, sem duplicar formatação). Sem evidência aproveitável,
  o hero mantém o retrato ao vivo que já exibia (overview por
  assunto/disciplina); o `reason` cru da API nunca vai para a tela.
* **CTAs:** o bloco do roteiro ganha `Revisar erros deste assunto`
  (`revisaoHref`: `estudos.html?aba=revisao&disciplina=&topico=`) e `Ver
  exemplo oficial` (primeiro `sampleQuestionIds[]`, quando existir). Fora
  do roteiro, o assunto atual também oferece a revisão (a fila filtra por
  assunto, não por plano).
* **`?topico=` na fila:** `js/views/revisao.js` (`readUrlIntoReview` +
  `resolveDisciplineForTopic`) pré-seleciona disciplina + assunto vindos
  dos CTAs do roteiro; sem disciplina na URL, ela é derivada do catálogo
  (`GET /topics`); assunto inválido/inexistente limpa o filtro em vez de
  quebrar a fila. `?aba=revisao` continua rolando até a seção.

## 9. Raio-X da sessão (TASK 19.2 — sem backend novo)

* **Bloco `#study-raiox`** no fim do caderno: `Nesta sessão você errou N (M
  sem acerto prévio nesta sessão)` + `renderResultNext` (mesmos 3 CTAs do
  19.1: `Revisar estes N agora`, `Praticar <assunto>`, `Atualizar meu plano`).
  Assunto via `q.topic`/`q.discipline` da lista; sem classificação, agrupa por
  disciplina — nunca inventa. Anuladas fora; trocar de filtro/página limpa o
  bloco (só vale para a sessão visível).
* **`questionIds[]` dispensado (decisão 19.2):** a revisão congela o topo da
  fila no assunto que mais pesou (`createSession({limit: N, topicId?,
  discipline?, onlyErrors: true})`, igual ao simulado) — recorte por tópico
  já cobre os erros sem estender `POST /review/sessions`. `api/review.js`
  segue rejeitando `questionIds` no cliente com mensagem explícita.
* **Handlers:** `startReviewFromRaiox` → `simulado.html?review=<id>` (401 →
  guard); `refreshPlanFromRaiox` → `POST /recommendations` + dashboard.

## 10. Verificação 17.2

```bash
for f in frontend/js/api/review.js frontend/js/views/revisao.js frontend/js/views/estudos.js frontend/js/views/simulado.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): sem sessão → guarda; conta nova → vazio
# honesto; 3+ respostas (2 erros + 1 acerto) → fila ordenada (erros primeiro);
# filtros disciplina/só-erros; Iniciar → 201 → ?review= com resumo; hub em
# modo Revisão → ?aba=revisao; mobile 360px + console limpo.
```
