# Auditoria e limites de custo da Prova-IA — TASK C.2 (DRAFT VPS)

> **Status: DRAFT — não é DONE.**
> Escopo VPS (sem testes): placeholders em `.env.example` (feito neste
> branch) + este texto de regra de cota. Lógica de cota/concorrência +
> testes `429/409` = DEV-only. DONE só na DEV.
>
> Branch: `draft/c2-vps-cota`. Nenhum Java/SQL/JS de produção neste
> draft. Base vigente: `ai_generations` (V23) com índice parcial
> `uq_ai_generations_busy` (1 `GENERATING` por aluno) + colunas de
> auditoria (`prompt_version`, `model`, `repair_attempts`,
> `failure_reason`, timestamps).

## 1. Regra de cota (proposta VPS — DEV fixa)

* Teto: **3 gerações/dia por aluno** (TASKS.md decisão 3).
  Contam as gerações **iniciadas** (inserção em `ai_generations`),
  independentemente de sucesso/falha do provedor — cota é controle de
  custo, não de aproveitamento.
* Janela proposta: **dia UTC** (00:00–23:59 UTC). Motivo: determinística,
  auditável por `created_at`, reset honesto e simples de informar
  (`Retry-After` até a próxima 00:00 UTC). Alternativa considerada e
  **não proposta**: janela deslizante de 24h (mais justa no limite, mas
  reset móvel e auditoria mais complexa). **DEV fixa e documenta.**
* Estouro → `429 AI_GENERATION_QUOTA` em envelope com `traceId` + reset
  informado (quando da janela fixada). Sem stack trace.

## 2. Concorrência (1 geração por vez por usuário)

* Suporte DDL já vigente: `uq_ai_generations_busy`
  (`database/migrations/V23__ai_generations.sql`) — 2ª geração
  concorrente viola o índice; o serviço mapeia para
  `409 AI_GENERATION_BUSY` (DEV-only, com teste de 2 concorrentes).
* Geração presa em `GENERATING` (crash sem transição para
  `READY`/`FAILED`): destino a definir na DEV (ex.: varredura com TTL ou
  transição administrativa auditada) — nunca desbloqueio silencioso.

## 3. Teto de tamanho

* N = N do formato-modelo (40, ou 50 se EAJ-2021), **sem override**
  (TASKS.md C.2). O validador A.1 já impõe o N exato; a cota não altera N.

## 4. Auditoria por geração (sem PII)

Por linha de `ai_generations` (colunas vigentes, sem conteúdo do aluno):

* `prompt_version`, `model`, latência (a derivar de timestamps na DEV),
  status (`GENERATING`/`READY`/`FAILED` + `failure_reason`), N questões
  (contagem em `ai_generation_questions`).
* Contadores agregados expostos no diagnóstico existente
  (admin/curadoria) — endpoint e forma a definir na DEV.
* Logs seguem a regra C.1: sem prompt completo, sem chave, sem PII.

## 5. Relação com o rate-limit da borda (E.1, DEV)

* A cota C.2 é regra de **negócio** (por aluno, por dia); o rate-limit
  E.1 é proteção de **borda** (rajada curta → `429 RATE_LIMITED` +
  `Retry-After`). Códigos distintos, sem confusão (`AI_GENERATION_QUOTA`
  ≠ `RATE_LIMITED`).

## 6. Critérios de aceite (DEV-only, para referência)

* 3ª geração no dia passa, 4ª → `429 AI_GENERATION_QUOTA` com reset.
* 2 concorrentes → 1× `409 AI_GENERATION_BUSY`.
* Contadores de auditoria conferem; `.env.example` com os placeholders
  (feito); `mvn test` verde.
