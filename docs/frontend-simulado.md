# Simulado — VouPassar (TASK 6.7)

> Experiência de prova real em `frontend/simulado.html` (+ `css/simulado.css` +
> `js/api/simulado.js` + `js/views/simulado.js`). Cobre TASKS.md 6.7 sobre as
> APIs prontas e testadas da Fase 5 (5.1 por disciplina, 5.5 edição real, 5.2
> feedback imediato, 5.3 ocultação no Modo Prova, respostas na 3.7).
> Reutiliza todos os tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Cobertura (TASKS.md) → seções e fontes

| Exigido na TASK 6.7 | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Escolher e iniciar | `#sim-hub` → `#sim-create-discipline` (disciplina + qtd 1–100 + dificuldade opcional + modo) e `#sim-create-edition` (edição + modo) | `POST /simulations/by-discipline` (TASK 5.1, sorteio sem reposição, `INSUFFICIENT_QUESTIONS` explícito) e `POST /simulations/by-edition` (TASK 5.5, caderno integral em ordem original, `INCOMPLETE_EDITION` explícito) |
| Experiência de prova | `#sim-exec` (`?id=<attemptId>`) → `#sim-exec-title/meta/badges`, `#sim-progress-*`, `#sim-questions` (caderno posição a posição), `#sim-submit/#sim-abandon` + `#sim-confirm` | `GET /simulations/attempts/{id}` (gabarito oculto em `IN_PROGRESS`); respostas via `POST /attempts {simulationAttemptId}` (TASK 3.7); enunciados via `GET /questions/{id}` (TASK 3.4, mascarados em PROVA — TASK 5.3) |
| Correção ao final | `#sim-result-section → #sim-result` (placar + correção por posição) | `POST …/submit` / `POST …/abandon` + `GET …/result` (placar do servidor: última por questão, pendentes, anuladas fora — TASK 5.1) |
| Feedback imediato (Estudo) | `sim-feedback` por cartão no Modo ESTUDO | `GET …/feedback/{position}` (TASK 5.2: acerto/erro, congelado, explicação, assunto) |
| Histórico | `#sec-hist-t → #sim-history` (+ `#sim-more`, paginado 20) | `GET /simulations/attempts?page=&size=` (mais recentes primeiro) |

## 2. Comportamentos

* **Guarda de auth:** `restoreSession()` (TASK 6.3). Sem sessão →
  `#sim-guard` com vazio + `Entrar` (`login.html?next=simulado.html…`) e
  `Criar conta`. `401` no meio da carga/resposta → mesmo painel. `Sair`
  (`[data-logout]`) revoga no servidor e volta ao login preservando o `?next=`.
* **Hub:** disciplinas (`GET /disciplines`) e edições (`GET /editions`,
  ordem crescente) alimentam os selects; criação redireciona para
  `./simulado.html?id=<attemptId>`. Histórico com `Retomar` (`IN_PROGRESS`)
  ou `Ver resultado` (encerradas).
* **Execução ESTUDO:** cada `Responder`/`Em branco` registra via
  `POST /attempts` e busca `GET /feedback/{position}` — acerto/erro,
  resposta correta (congelada), explicação e assunto, com marcação das
  alternativas. Vale a última resposta (`Alterar resposta` reabilita).
* **Execução PROVA:** a resposta registra (`isCorrect: null` do servidor) e
  mostra só `Resposta registrada — resultado oculto até concluir` (nunca
  `feedback/{position}`, que daria `409 STUDY_FEEDBACK_UNAVAILABLE`);
  `Alterar resposta` permite trocar (vale a última). Correção só após
  `Concluir`/`Abandonar` (placar + correção por posição contra o congelado).
* **Encerrar:** `Concluir simulado`/`Abandonar` pedem confirmação em
  `<dialog>` (pendentes explícitos na pergunta); após encerrar, o caderno
  desabilita, o cabeçalho atualiza o status e `#sim-result` mostra totais
  (`correct/scored`, aproveitamento, respondidas/total, anuladas) +
  correção por posição (acertou/errou/pendente/anulada). `409
  SIMULATION_CLOSED` (encerrou em outra aba) atualiza para o estado final.
* **Retomada:** `GET` com `IN_PROGRESS` mostra só progresso; posições já
  respondidas saem marcadas (`answered`) e aceitam alteração. Enunciado que
  falha carrega cartão de erro com `traceId` + `Tentar de novo` (nunca
  caderno parcial silencioso).
* **Erros:** `404` na execução (inexistente ou de outro aluno) vira erro
  visível + saídas (nunca conteúdo alheio). `INSUFFICIENT_QUESTIONS`,
  `INCOMPLETE_EDITION`, `EDITION_NOT_FOUND` viram toast + resumo com
  `traceId` — nunca redução/caderno parcial silencioso.

## 3. Honestidade (§4 — nada inventado)

* Edições vêm do backend — 2021 nem aparece como opção (ausente do dataset,
  §3); nota fixa repete a ausência.
* Por disciplina: sorteio sobre não-anuladas (anuladas fora da seleção,
  TASK 5.1). Edição real: anuladas participam nas posições originais, fora
  do aproveitamento (regra DESCONHECIDA, TASK 1.3 §4).
* `accuracy` NULL → `—` (nunca zero). Não respondidas = pendentes (nunca
  erro). Explicação NULL = `NECESSITA REVISÃO`, nunca texto gerado.
* `hasFigure` vira aviso de figura no PDF-fonte (PDFs não redistribuídos,
  §12). Discursiva sai só como `+ texto` no rótulo da edição (correção
  automática DESCONHECIDA / fora do MVP, TASK 5.5).

## 4. A11y, responsivo, Pages

* Semântico (`header/main/nav/section/article/dialog`, `h1` único,
  `h2#sim-exec-title` com foco no carregamento), `fieldset/legend` por
  posição, radios em `label` clicável (alvo ≥44px), feedback/progresso em
  `role=status`, `progressbar` com `aria-valuenow`, erros em `role=alert`,
  foco visível e contraste herdados, `prefers-reduced-motion` herdado,
  `noscript` com aviso. `<dialog>` nativo (Esc fecha, foco no confirmar).
* Mobile-first: 1 coluna, caderno em `measure`; `48rem` → hub em 2 colunas
  e placar em 4 colunas.
* Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
  `API_BASE_URL` via `<meta>` (build injeta em 10.1). Zero dependências.

## 5. Verificação

```bash
for f in frontend/js/api/simulado.js frontend/js/views/simulado.js frontend/js/views/dashboard.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
```

Critérios 6.7: hub cria por disciplina e por edição real via API (com
`401/400/404/409` honestos); execução responde posição a posição com
feedback imediato em ESTUDO e ocultação em PROVA; concluir/abandonar com
confirmação e placar do servidor; resultado com correção por posição;
histórico retoma/revê; guarda de auth com `?next=` seguro; `node --check`
OK; `check_frontend.py` OK estendido com cobertura simulado; navegação
(Dashboard, Estudos, Questão) linka o Simulado.
