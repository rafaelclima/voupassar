# Revisão guiada — execução no frontend (TASK 17.3)

> `simulado.html?review=<sessionId>` executa a sessão criada pela fila em
> Estudos (`js/views/revisao.js`, `Iniciar revisão (N)`). Dono da tela:
> `js/views/review-exec.js` (delegado por `js/views/simulado.js`, que só
> decide hub × execução × revisão pela URL); API fina em `js/api/review.js`;
> lógica pura e testada em `js/views/review-logic.js`
> (`node scripts/analysis/test_review_logic.mjs`).

## 1. Fluxo → fontes (backend pronto e testado, sem mudança de contrato)

| Passo na tela | Fonte |
|---|---|
| Caderno congelado + progresso | `GET /review/sessions/{id}` (posições, categoria/motivo da fila vigente, `answered/selectedOption/isCorrect` sempre revelados — REVISAO nunca oculta) |
| Enunciados | `GET /questions/{id}` por posição (mesmos componentes do simulado: passagens, figuras expansíveis, matemática) |
| Responder / Em branco | `POST /attempts {mode: REVISAO, studySessionId}` (`BLANK` conta como erro quando não anulada — regra do servidor) |
| Encerrar | `POST /study-sessions/{id}/finish` (antes disso, `GET …/result` responde `409 REVIEW_NOT_FINISHED`) |
| Placar + correção por posição | `GET /review/sessions/{id}/result` (vale a última por questão; anuladas fora; sem pontuável → aproveitamento NULL) |
| Próximo passo | `Voltar à fila` (`estudos.html?aba=revisao` — a fila é recalculada no servidor; item consolidado some sozinho) |

## 2. Comportamentos

* **Feedback imediato por posição:** acertou (com a letra, vinda do detalhe —
  só usada *após* responder, nunca como cola), errou (com a correta), anulada
  (fora do aproveitamento) — mais assunto da questão. Marcação verde/vermelha
  nas alternativas, `Tentar novamente` (vale a última). Retomada mostra o
  progresso persistido.
* **Encerrar com confirmação inline** (pendentes explícitos — nunca contam
  como erro). Após encerrar, o caderno vira somente-leitura + placar
  (`corretas/pontuáveis`, aproveitamento, respondidas/total, anuladas) +
  correção por posição. Sessão já encerrada abre direto no resultado.
* **Erros honestos:** `404` (de outra conta ou fora do fluxo) vira erro
  visível + volta à fila; `400 NO_REVIEW_ITEMS` e `SESSION_CLOSED` (encerrou
  em outra aba) recarregam para o estado vigente; `401` vira guarda de
  acesso. Tudo com `traceId` + `Tentar de novo` por seção, sem `notes` de
  auditoria na tela.
* **Atalho no dashboard:** `Revisar erros` junto às prioridades
  (`js/views/dashboard.js`) leva à fila (`estudos.html?aba=revisao`).

## 3. Honestidade (§4 — nada inventado)

* Letra correta só aparece depois de responder a posição; detalhe sem
  `answerKey` (ou `X`) = veredito sem letra, nunca letra chutada.
* Sem resposta = `Pendente`, nunca erro. `accuracy` NULL → `—`, nunca zero.
* Fora do top-100 vigente, o cartão omite categoria/motivo (nunca inventa).

## 4. Verificação

```bash
for f in frontend/js/api/review.js frontend/js/views/review-exec.js \
  frontend/js/views/review-logic.js frontend/js/views/simulado.js \
  frontend/js/views/dashboard.js; do node --check "$f"; done
node scripts/analysis/test_review_logic.mjs
python3 scripts/analysis/check_frontend.py
```

Critérios 17.3: errar 3 → fila com 3 (`ERRO_SEM_ACERTO`) → sessão com 3 →
acertar tudo (resultado 3/3) → fila recalculada: a visão só-erros esvazia
(0 itens) e a fila cheia rebaixa os 3 para `REFORÇO` — o motor 4.5/5.4 não
deleta histórico, só reprioriza (decisão Fase 17: expor, não reconstruir).
`BLANK` = erro e anulada fora (regras do servidor); fila vazia, limite e
só-erros cobertos por teste de lógica + E2E em navegador real.
