# Tela de questão dedicada — VouPassar (TASK 6.6)

> Experiência dedicada de leitura, seleção, confirmação e feedback em
> `frontend/questao.html` (+ `css/questao.css` +
> `js/api/questao.js` + `js/views/questao.js`). Cobre TASKS.md 6.6 sobre a
> base da 6.5 (prática livre em modo ESTUDO com correção imediata do
> servidor, AGENTS.md §9). Reutiliza todos os tokens/componentes da
> TASK 6.1 — nada reinventado. Decisão de produto 2026-10-04: sem passo a
> passo textual (plataforma testa conhecimento, não ensina); feedback =
> acerto/erro + resposta correta + assunto.

## 1. Cobertura (TASKS.md) → seções e fontes

| Exigido na TASK 6.6 | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Leitura | `article[aria-labelledby=questao-title]` → `#questao-title` + `#questao-meta` + `#questao-badges` + `#questao-statement` (+ `#questao-passages`, `#questao-figure`, `#questao-topic`) | `GET /questions/{id}` (TASK 3.4): enunciado + alternativas + `answerKey` + proveniência + assunto vigente + `passages[]` (TASK 6.9) |
| Seleção | `#questao-form > #questao-options` (`fieldset/legend` + radios `questao-option`, alvo ≥44px) + `#questao-hint` | Estado local; nada é enviado antes da confirmação |
| Confirmação | `#questao-submit` (Responder) + `#questao-blank` (em branco → `BLANK`) + `#questao-reset` (Responder novamente) | `POST /study-sessions {mode: ESTUDO}` (lazy, id em memória + `sessionStorage`) + `POST /attempts {questionId, selectedOption, mode: ESTUDO, timeSpentSeconds, studySessionId}` (TASK 3.7) |
| Feedback | `#questao-feedback` (`role=status`, `data-tone`) + marcação das alternativas (`--correct/--wrong`) | Correção do servidor (última tentativa vinculada); `409 SESSION_CLOSED` reabre a sessão e repete uma vez |

Proveniência e classificação (`#questao-source`): `sourceType`, edição/número,
disciplina, páginas do PDF-fonte, figura, assunto, status/confiança da
classificação, taxonomia e `publicationStatus`. Notas de evidência
(`#questao-notes`): notas da questão (máx. 6) + notas fixas de honestidade.

## 2. Comportamentos

* **Guarda de auth:** `restoreSession()` (TASK 6.3). Sem sessão →
  `#questao-guard` com vazio + `Entrar`
  (`login.html?next=questao.html%3Fid%3D…`) e `Criar conta`. `401` no meio
  da carga/resposta → mesmo painel. `Sair` (`[data-logout]`) revoga no
  servidor e volta ao login preservando o `?next=`.
* **URL:** `?id=<long>` obrigatório (positivo; ausente/inválido → erro
  visível + saídas para estudos/dashboard, nunca fetch). `?voltar=` aceita
  **só** caminho relativo `./…` (whitelist anti-open-redirect; default
  `./estudos.html`) e alimenta `#questao-back`.
* **Navegação estudos → questão:** cada cartão da área de estudos (6.5) tem
  `Abrir em tela dedicada` (`./questao.html?id=…&voltar=…` com o filtro
  atual preservado no voltar).
* **Confirmação:** `Responder` sem seleção → toast + dica inline + foco na
  primeira alternativa (nunca envio vazio). `Responder em branco` envia
  `BLANK`. Anulada (`answerKey X`) desabilita o form com nota explícita
  (fora do aproveitamento, regra DESCONHECIDA, TASK 1.3 §4).
* **Feedback:** sucesso/erro/anulada com `Você marcou X` + resposta correta
  (`answerKey` do detalhe carregado) + assunto; alternativas marcadas
  `--correct/--wrong`. `Responder novamente` reabilita, limpa o feedback e
  devolve o foco às opções (nova tentativa = fato novo, trigger de
  imutabilidade respeitado).
* **Textos-base (TASK 6.9):** `q.passages[]` vira painéis `<details
  class="passage">` (`js/components/passage.js`, só tokens do DS) entre o
  enunciado e a figura, via `#questao-passages`. Botão *"Mostrar
  texto/imagem: <rótulo>"*, colapsado por padrão; corpo com título, byline,
  parágrafos, nota de formato e crédito com página. Mesmo componente nos
  cards de `estudos` e `simulado` (inclusive Modo Prova). Sem vínculo: nada
  renderizado.
* **Erros:** `QUESTION_NOT_FOUND` (404) e demais falhas viram
  `renderErrorSummary` com `traceId` + botões de saída — nunca página vazia
  silenciosa. Origem válida sem alternativas = nota `NECESSITA REVISÃO`.

## 3. Honestidade (§4 — nada inventado)

* Não-oficial (`AUTHORAL/ADAPTED/…`) sai identificado como nunca-do-IFRN
  (§11); sem `sourceType` = origem desconhecida.
* Assunto ausente = `NÃO CONFIRMADO`; dificuldade = estimativa com confiança
  BAIXA explícita no badge; páginas ausentes = DESCONHECIDA.
* `hasFigure` vira aviso de figura no PDF-fonte (PDFs não redistribuídos,
  §12). `2021` não é edição válida aqui (`QUESTION_NOT_FOUND` genérico do
  backend, sem preencher lacuna).
* Tempo de resposta (`timeSpentSeconds`) é medido do carregamento à
  confirmação — métrica local de tentativa, nunca medida oficial de prova.

## 4. A11y, responsivo, Pages

* Semântico (`header/main/nav/article/section`, `h1` único com
  `tabindex=-1` e foco no carregamento), `fieldset/legend` com radios em
  `label` clicável, feedback em `role=status` com foco no botão de nova
  tentativa, erros em `role=alert`, foco visível e contraste herdados,
  `prefers-reduced-motion` herdado, `noscript` com aviso.
* Mobile-first: 1 coluna, enunciado em `measure` com tipo maior (`lg→xl` em
  `48rem`); proveniência vira grade `12rem 1fr` em `48rem`.
* Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
  `API_BASE_URL` via `<meta>` (build injeta em 10.1). Zero dependências.

## 5. Verificação

```bash
for f in frontend/js/api/questao.js frontend/js/views/questao.js frontend/js/views/estudos.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): sem sessão → painel de acesso com ?next=;
# login → questao.html?id=1 (2020 Q1, correção imediata A/BLANK, marcação
# correct/wrong, acerto/erro + resposta correta + assunto); ?id=999999 → 404 com saídas;
# sem ?id= → erro de uso; voltar preserva o filtro dos estudos.
```

Critérios 6.6: 4 blocos (leitura, seleção, confirmação, feedback) presentes e alimentados pela API real; guarda de
auth com `?next=` seguro; correção do servidor com `Tentar/Responder
novamente`; `?id=` + `?voltar=` whitelisted; link estudos → dedicada;
`node --check` OK; 200 no serve; `check_frontend.py` OK estendido com
cobertura questão.
