# API de Revisão — TASK 4.5

> Fila de revisão do aluno: determinística, transparente, explicável e
> auditável sobre o fato imutável `question_attempts`.
>
> Prioriza, nesta ordem: erros persistentes (questão tentada e nunca
> acertada), regressões (última incorreta com acerto anterior), acertos em
> assuntos frágeis, reforço (em desenvolvimento / em observação / sem
> classificação), manutenção (dominado com exposição única — risco de baixa
> retenção) e consolidados. Cada item traz motivo textual derivado das mesmas
> variáveis que o ordenam. Nenhum número inventado.

## Endpoint

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/review/queue` | Bearer JWT | Fila de revisão do dono do token |

### Parâmetros

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `limit` | int 1–100 | 20 | Máximo de itens devolvidos |
| `discipline` | string | — | Filtro por disciplina (ex. `MATEMATICA`); inexistente → `404 DISCIPLINE_NOT_FOUND` |
| `topicId` | long | — | Filtro por assunto; inexistente → `404 TOPIC_NOT_FOUND`, `<= 0` → `400` |
| `onlyErrors` | boolean | false | Quando `true`, só `ERRO_SEM_ACERTO` e `ERRO_RECENTE` |

## Contrato de resposta

`ReviewQueueResponse` (record):

- `totalAttempts`, `scoredAttempts`, `correct`, `incorrect`, `annulled`.
- `accuracy` — `correct / scored` (NULL quando sem pontuáveis, nunca zero inventado).
- `unclassifiedAttempts` — tentativas sem classificação vigente (entram na fila sem assunto, nunca omitidas).
- `distinctQuestions` — questões pontuáveis distintas (tamanho da fila antes do limite).
- `returned`, `limit`, `onlyErrors`, `disciplineFilter`, `topicFilter`.
- `items` — `ReviewItem` em ordem determinística (rank 1-based):
  - `questionId`, `disciplineCode/Name`, `sourceYear`, `sourceQuestionNumber`;
  - `topicId/Code/Name`, `subtopicId/Code/Name` (NULL quando sem classificação vigente);
  - `classificationStatus/Confidence/TaxonomyVersion` (NULL quando sem classificação);
  - `attempts`, `correct`, `incorrect`, `lastCorrect`, `lastAttemptAt`, `daysSinceLastAttempt` (informativo);
  - `topicMastery` (`DOMINADO` / `EM_DESENVOLVIMENTO` / `FRAGIL` / `EM_OBSERVACAO` / `NAO_AVALIADO` / `NAO_CLASSIFICADO`);
  - `topicScored`, `topicAccuracy`, `reviewCategory`, `reason`.
- `notes` — evidências, limitações e referências.

### Categorias (baldes 0–5, menor = mais urgente)

| Categoria | Peso | Condição |
|---|---|---|
| `ERRO_SEM_ACERTO` | 0 | Questão pontuável tentada e nunca acertada (`correct == 0`) |
| `ERRO_RECENTE` | 1 | Última pontuável incorreta, com acerto anterior (regressão) |
| `TOPICO_FRAGIL` | 2 | Última correta, assunto `FRAGIL` (≥ 3 pontuáveis, < 50%) |
| `REFORCO` | 3 | Última correta, assunto `EM_DESENVOLVIMENTO` / `EM_OBSERVACAO` / `NAO_AVALIADO` / `NAO_CLASSIFICADO` |
| `MANUTENCAO` | 4 | Última correta, assunto `DOMINADO`, exposição única (`attempts <= 1`) |
| `CONSOLIDADO` | 5 | Última correta, assunto `DOMINADO`, mais de uma exposição |

Limiares de domínio idênticos aos da TASK 4.2 (sinal mínimo 3 pontuáveis;
`DOMINADO ≥ 70%`, `FRÁGIL < 50%`), duplicados no serviço para manter
diagnóstico e revisão desacoplados.

### Desempates determinísticos (sem aleatoriedade)

- Erros (baldes 0–1): mais tentativas primeiro (persistência), depois a mais
  recente, depois menor aproveitamento na questão, depois menor `questionId`.
- Acertos (baldes 2–5): assunto mais fraco primeiro (`topicAccuracy` asc,
  NULLs por último), depois a mais antiga (risco de esquecimento), depois
  menor `questionId`.

## Regras de evidência

- **Factual (desempenho):** `question_attempts` — imutável (trigger
  `trg_attempts_immutable`).
- **Derivada (assunto):** `question_classifications` vigente (`status <>
  'REJECTED'`, mais recente por questão) — revisão humana PENDENTE (TASK
  12.2), nunca verdade oficial do IFRN.
- **Anuladas:** ficam fora da fila; contam no resumo, sem pontuar (regra de
  pontuação DESCONHECIDA, TASK 1.3 §4).
- **Nunca tentadas:** ficam fora da fila — pertencem ao diagnóstico
  (lacunas, TASK 4.2) e ao roteiro (TASK 4.4).
- **Dificuldade estimada:** NÃO usada (palpite global BAIXA, sem calibração
  por desempenho).
- **`daysSinceLastAttempt`:** informativo (dias entre `lastAttemptAt` e
  agora); define apenas desempate relativo, nunca o balde — sem limiar de
  dias inventado.

### Explicabilidade

Cada `ReviewItem` traz `reason`: frase determinística com números
(`corretas / tentativas` na questão, última correta/incorreta há N dias,
assunto + domínio + pontuáveis do assunto + média geral) e dica da categoria
(prioridade máxima, regressão, assunto frágil, reforço, baixa retenção,
manutenção). A questão se resolve via `GET /api/v1/questions/{id}` (TASK
3.4) — a fila referencia, nunca duplica enunciado/gabarito.

## Segurança e limites

- Apenas o dono do token (`@AuthenticationPrincipal`).
- Sem token → `401 UNAUTHORIZED` no envelope `{code, message, details, traceId, timestamp, path}`.
- `limit` fora de 1–100 → `400`; disciplina/assunto inexistente → `404`
  com código explícito (nunca fila vazia silenciosa).
- Somente leitura: nenhum estado persistido (a fila deriva do fato a cada
  chamada; o progresso se registra via `POST /attempts`, TASK 3.7, e via
  status do roteiro, TASK 4.4).
- Nenhum dado pessoal no corpo além dos próprios; PII só no contexto autenticado.
- Nenhum stack trace exposto em produção.

## OpenAPI

Contratos disponíveis em `GET /v3/api-docs` (público, sem PII) e
`GET /swagger-ui.html`. Tag `Modo Revisão`; escopo `/api/v1/**`.

## Verificação (2026-10-01)

- `mvn test` → 153/153 (incl. `ReviewServiceTest` 6 casos, `ReviewControllerTest` 2).
- `GET /api/v1/review/queue` (sem token) → `401`; autenticado sem tentativas → `200` com `items: []` e nota orientando estudo/simulado.
- Ordenação auditada em teste: `ERRO_SEM_ACERTO` (0/2) → `ERRO_RECENTE` (1/2, última errada) → `TOPICO_FRAGIL` (última correta, assunto 2/5 FRÁGIL).
- `onlyErrors=true` devolve só baldes 0–1; `limit=1` devolve o rank 1; anuladas contam no resumo e ficam fora da fila.

## Limitações conhecidas (não inventadas)

- Sem limiar absoluto de retenção em dias (ex.: "30 dias") — `MANUTENCAO`
  usa exposição única como proxy determinístico; calibração de esquecimento
  requer dados longitudinais (Fase 4 completa).
- Sem filtro por modo/simulado na versão inicial (todos os modos compõem o
  fato; recorte por `ESTUDO`/`PROVA`/simulado avaliado para Fase 5).
- Sem paginação por offset (só `limit` top-N determinístico); paginação
  completa avaliada com os simulados (Fase 5).
- Classificações com revisão PENDENTE — a fila herda a mesma pendência do
  diagnóstico (TASK 12.2); itens sem classificação entram como `REFORCO`
  com nota `NECESSITA REVISÃO`, nunca omitidos.
