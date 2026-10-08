# Dashboard — VouPassar (TASK 6.4)

> Página protegida em `frontend/dashboard.html` (+ `css/dashboard.css` +
> `js/api/dashboard.js` + `js/views/dashboard.js`). Responde às 4 perguntas
> do AGENTS.md §7: onde estou, o que estudar, como evoluo, o que fazer agora.
> Reutiliza todos os tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Cobertura (TASKS.md) → seções e fontes

| Exigido na TASK 6.4 | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Desempenho | `#sec-resumo-t` (4 stat cards) + `#sec-disc-t` (tabela por disciplina) | `GET /performance/overview` (TASK 4.1); `GET /diagnosis` p/ nível |
| Progresso | cards + barra do roteiro (`DONE/total`, `progressbar` com `aria-*`) | `GET /recommendations/plan` + `POST .../items/{id}/status` (TASKs 4.3/4.4) |
| Assuntos prioritários | `#sec-prio-t` (top 5 da fila, com motivo) | `GET /diagnosis` → `priorities` (TASK 4.2) |
| Próximo estudo | `#sec-next-t` (P1 aberto + Começar/Concluir) | `GET /recommendations/plan` (menor `priority` com `TODO`/`DOING`) |
| Últimos simulados | `#sec-sim-t` (5 recentes, com status) | `GET /simulations/attempts?page=0&size=5` (TASK 5.1) |
| Evolução | `#sec-evo-t` (seletor `DAY`/`WEEK`/`MONTH` + barras CSS) | `GET /performance/evolution?granularity=` (TASK 4.1) |

Nomes dos itens do plano: `StudyPlanItem` traz só `topicId` — o
`dashboard.js` resolve via `GET /topics` (TASK 3.3). Sem o catálogo, o item
aparece como `Assunto #id` com nota `DESCONHECIDO — NECESSITA REVISÃO`
(nunca nome inventado).

## 2. Comportamentos

* **Guarda de auth:** `requireSessionOrGuard(showGuard)` + `renderAuthGuard()`
  (`js/views/auth-shared.js`, TASK 16.3; envolve `restoreSession()` da 6.3).
  Sem sessão → `#dash-guard` com vazio + `Entrar`
  (`login.html?next=dashboard.html`) e `Criar conta`. `401` no meio da
  carga → mesmo painel com nota de expiração (o `request()` de
  `js/api/client.js` já tentou `refresh` 1× antes). `Sair` (`[data-logout]`)
  revoga no servidor e volta ao login.
* **Carga paralela:** `Promise.allSettled` — cada seção falha isolada; o
  resto continua. Falhas viram resumo no topo (`#dash-error`, com `traceId`)
  + erro na seção com botão **"Tentar de novo"** (`renderErrorWithRetry()` em
  `js/components/ui.js`, mesmo padrão do `renderMissingCard` do simulado —
  TASK 16.3): resumo (`stats/disciplinas/prioridades`), roteiro, evolução e
  simulados recarregam só a seção, sem reload da página.
  `404 NO_ACTIVE_PLAN` não é erro: vira vazio com `Gerar roteiro`
  (`POST /recommendations`).
* **Roteiro:** `Gerar roteiro` / `Gerar novamente` (`POST`, preserva histórico
  no servidor); `Começar agora` (`TODO→DOING`) e `Concluir` (`→DONE`,
  `POST ...?status=`) atualizam o próximo estudo sem recarregar.
* **Evidência por item (TASK 18.3):** cada linha do roteiro mostra o motivo
  em linguagem de estudante — `Nº no roteiro · você X% (N tentativas) ·
  caiu em H questões de E edições (anos) · última tentativa há N dias` —
  montado do `evidenceJson` rico (TASK 18.2) pelo formatador compartilhado
  `js/components/plan-evidence.js` (`parseEvidence` + `evidenceLine`, com
  teste `node scripts/analysis/test_plan_evidence.mjs`). Sem evidência
  aproveitável, cai para o `reason` traduzido (nunca código cru, nunca zero
  inventado). O hero "Estude agora" exibe o mesmo motivo do item (não um
  texto paralelo). CTAs por item: `Praticar agora` (recorte em estudos com
  `?disciplina=&topico=&subtopico=&origem=painel`), `Revisar erros` (fila
  filtrada por assunto — `estudos.html?aba=revisao&disciplina=&topico=`,
  TASK 17.2 + `?topico=` da 18.3) e `Ver exemplo oficial` (primeiro
  `sampleQuestionIds[]`, só quando existir — assunto sem questão oficial não
  ganha link inventado). `Concluir` mantido.
* **Prioridades → revisão (TASK 17.3):** `Revisar erros`
  (`estudos.html?aba=revisao`) junto à lista de prioridades (e no vazio
  `Nada pendente`) leva à fila de revisão — o aluno sai do "o que treinar"
  direto para os erros a consolidar.
* **Evolução:** troca de granularidade recarrega só a seção, com loading
  inline e `role=status`.
* **Vazio honesto:** conta nova (`0` tentativas) mostra `—` + notas
  (`DESCONHECIDO, nunca zero inventado`), prioridades como lacunas e
  simulados com aviso de que as telas de prova chegam nas TASKs 6.5–6.7
  (nenhuma rota futura é linkada).

## 3. Honestidade (§4 — nada inventado)

* Anuladas: contam como conteúdo, fora do aproveitamento, com nota explícita
  (regra de pontuação DESCONHECIDA, TASK 1.3 §4).
* Assuntos: classificação derivada, revisão humana PENDENTE (TASK 12.2) —
  nota fixa em `#dash-notes` + notas vindas da API (`overview.notes`,
  `diagnosis.notes`), sem duplicar.
* `accuracy` NULL → `—` (nunca zero). Nível (`INICIAL/EM_DESENVOLVIMENTO/
  CONSOLIDADO/DESCONHECIDO`) descrito como estimativa por limiares.
* Evolução em UTC, sem interpolar lacunas (só períodos com tentativas).
* `2021 ausente` citado nas notas. Sem preço/garantia/milhares (mesmo
  vocabulário proibido da landing).

## 4. A11y, responsivo, Pages

* Semântico (`header/main/nav/section`, `h1` único, `skip-link`), tabelas com
  `caption` em região rolável, `progressbar` com `aria-valuenow/min/max`,
  `alert/status/note` nos lugares certos, foco visível e contraste herdados,
  alvos ≥44px, `prefers-reduced-motion` herdado, `noscript` com aviso.
* Mobile-first: 1 coluna; `48rem` → 2 colunas (stats e grade prio/próximo);
  `64rem` → 4 stats. Só tabelas rolam internamente.
* Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
  `API_BASE_URL` via `<meta>` (build injeta em 10.1). Zero dependências,
  barras de evolução em CSS puro (sem lib de gráficos).

## 5. Verificação

```bash
for f in frontend/js/api/dashboard.js frontend/js/views/dashboard.js \
  frontend/js/components/plan-evidence.js \
  frontend/js/views/login.js frontend/js/views/cadastro.js \
  frontend/js/views/auth-shared.js; do node --check "$f"; done
node scripts/analysis/test_plan_evidence.mjs
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): sem sessão → painel de acesso; login →
# dashboard com 4 stats, disciplinas, top 5 prioridades, próximo estudo,
# evolução WEEK e simulados; conta nova → vazios honestos + gerar roteiro.
```

Critérios 6.4: 6 blocos presentes e alimentados pela API real; guarda de
auth com `?next=` seguro; `Gerar roteiro` e `status` funcionais; `node
--check` OK; 200 no serve; `check_frontend.py` OK estendido com cobertura
dashboard.

## 6. Diagnóstico inicial — wizard "Descubra seu nível" (TASK 20.2)

* **Página:** `frontend/diagnostico.html` (+ `js/views/diagnostico.js`,
  guarda de auth com `?next=diagnostico.html`). Oferta pós-cadastro
  (`js/views/cadastro.js`: primário "Descobrir meu nível (12 questões)",
  secundário "Ir para o dashboard").
* **Sem endpoint novo:** etapa 1 cria `POST /simulations/by-discipline`
  (6Q LP, Modo PROVA, `OFFICIAL` padrão) e redireciona para
  `simulado.html?id=&diag=LP`. Códigos de disciplina resolvidos via
  `GET /disciplines` (casa por "portugues"/"matemat", tolerante a `LP`/`MAT`
  vs nomes longos) — nunca fixos no código.
* **Encadeamento:** gancho do wizard em `renderNextSteps`
  (`js/views/simulado.js`, `?diag=LP|MAT`) abre o bloco antes dos CTAs
  genéricos: `LP` → "Continuar: Matemática (6 questões)";
  `MAT` → "Ver meu ponto de partida" (`POST /recommendations` automático +
  `dashboard.html?origem=diagnostico`). Fora do wizard nada é renderizado.
* **Ponto de partida:** `?origem=diagnostico` exibe alerta "Seu ponto de
  partida" no topo do próximo passo — estimativa inicial com poucas
  questões (`EM_OBSERVACAO` sinalizado, sem fingir precisão). Quem pula o
  wizard recebe o provisório da 20.1 (só-frequência, backend/VPS).
* **Verificação:** `node --check` nos 4 arquivos + `check_frontend.py`
  (bloco 20.2: página, reuso `by-discipline`, gancho `?diag=`, boas-vindas
  `?origem=`). E2E real (2026-10-08, conta nova): cadastro → wizard →
  6 LP (`id=15`) → "Continuar: Matemática" → 6 MAT (`id=16`) → "Ver meu
  ponto de partida" (plano v1 automático) → `dashboard?origem=diagnostico`
  com boas-vindas + roteiro; zero erros de console; desktop + 390px.
  Achado do teste: `renderResultNext` limpa o container (`textContent=""`),
  então o cartão do wizard usa mount próprio com prepend (antes do placar).
