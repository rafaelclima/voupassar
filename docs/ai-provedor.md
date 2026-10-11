# Provedor OpenRouter (server-to-server) — TASK C.1 (DRAFT VPS)

> **Status: DRAFT — não é DONE.**
> Escopo VPS (sem testes): este documento. Cliente HTTP + testes com
> servidor falso + varredura de segredo = DEV-only. DONE só na DEV após
> implementação + testes + validação (TASKS.md `Paralelo DEV × VPS`).
>
> Branch: `draft/c1-vps-provedor`. Nenhum Java/SQL/JS de produção neste
> draft. Depende de A + B (TASKS.md Fase C): a troca mock → real não muda
> contrato (`docs/ai-prova-schema.md`), isolamento nem validação — só a
> fonte dos bytes.

## 0. Fontes

* Contrato e prompt: `docs/ai-prova-schema.md`,
  `backend/src/main/resources/ai/prompts/ai-prova-v1.txt`
  (`prompt_version: ai-prova-v1`).
* Regras do programa: TASKS.md decisões 3 (chave no servidor + fila,
  3 provas-IA/dia, 1 geração por vez) e 4–6, regras 4–6 (PII, erros
  honestos, segredos em ambiente).
* Envelope de erro vigente: `{code, message, details, traceId, timestamp,
  path}` + header `X-Trace-Id`, sem stack trace
  (`backend/.../exception/GlobalExceptionHandler.java`,
  `docs/api-simulados.md`).
* O que não foi confirmado na documentação vigente do provedor a partir
  desta VPS está marcado DESCONHECIDO (AGENTS.md §4) — a DEV fixa contra
  a doc vigente na implementação.

## 1. Modelo

* `model` default: **DESCONHECIDO** (não confirmado nesta VPS; a DEV
  fixa o default + allowlist de modelos free contra a documentação
  vigente do OpenRouter na implementação).
* Allowlist de modelos free fixada no backend contra a doc vigente;
  **override de modelo pelo cliente é proibido** (só o servidor escolhe).
* `prompt_version` (`ai-prova-v1`) + `model` efetivo são enviados ao
  provedor e registrados na auditoria da geração (TASK C.2/A.2), sem PII.

## 2. Timeouts e tentativas

* Timeout configurável via env (chaves em `TASK C.2`/`.env.example`:
  `AI_TIMEOUT_*`); default na faixa **60–90s** (TASKS.md C.1).
  Valor exato a fixar na DEV.
* **1 tentativa síncrona por geração na v1** — sem retry com custo
  (decisão de custo; retries só sem custo, se houver, a critério DEV
  documentado).

## 3. Segredos e PII (AGENTS.md §15/§16/§29)

* `OPENROUTER_API_KEY` **só via ambiente**, nunca no Git, nunca no
  frontend. Placeholder em `.env.example` (draft na TASK C.2).
* Corpo da chamada ao provedor: só formato/disciplina/tópicos/idioma
  (TASKS.md regra 4). **Nenhum dado do aluno sai para o provedor.**
* Logs: sem prompt completo, sem chave; só `prompt_version`, `model`,
  latência, status (TASKS.md C.1).

## 4. Rota free — limites

* Limites da rota free (cota, janela, throughput): **DESCONHECIDOS**
  (não confirmados nesta VPS; a DEV apura na doc vigente e registra
  aqui, ou mantém DESCONHECIDO).
* O que é pago / desde quando: **DESCONHECIDO** (mesma regra).
* Regra de conduta até confirmação: tratar todo `429` como cota do
  provedor (repassar `Retry-After` quando houver) e nunca presumir
  gratuidade ilimitada.

## 5. Erros honestos (contrato para a DEV implementar)

| Situação do provedor | Resposta da API |
|---|---|
| timeout / 5xx / 429 | `502 AI_PROVIDER_UNAVAILABLE` em envelope com `traceId`; repassar `Retry-After` quando o provedor enviar |
| resposta não-JSON ou fora do schema após validação A.1 | `422 AI_GENERATION_INVALID` com motivo por posição (validador vigente) |
| qualquer outro estado | envelope honesto, sem stack trace; **sem fallback silencioso para mock em produção** (TASKS.md C.1) |

Cota de negócio (3/dia) e concorrência (1 em andamento → `409`) são
TASK C.2, não deste documento.

## 6. Pacote e separação (orientação DEV, sem código neste draft)

* Cliente HTTP em pacote próprio do backend (ex.: `aiprova/provider`),
  sem regra de negócio em controller (AGENTS.md §19).
* Teste de varredura: chave nunca aparece em log ou resposta
  (critério C.1).

## 7. Pendências DEV (não bloqueiam este draft)

* [ ] Fixar `model` default + allowlist contra a doc vigente (ou manter
  DESCONHECIDO com motivo).
* [ ] Implementar cliente + testes com servidor HTTP falso (timeout,
  429 + Retry-After, 500, corpo inválido → envelopes do §5).
* [ ] Teste de varredura de segredo + `mvn test` verde.
