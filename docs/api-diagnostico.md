# API de Diagnóstico — TASK 4.2

> Diagnóstico inicial do aluno: determinístico, transparente, explicável e
> auditável sobre o fato imutável `question_attempts`.
>
> Entrada: respostas registradas pelo aluno. Saída: pontos fortes, pontos
> fracos, lacunas, fila de prioridades, nível de domínio estimado e nota
> de evidência. Nenhum número inventado; todo item traz motivo textual
> derivado das mesmas variáveis que o classificam.

## Endpoint

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/diagnosis` | Bearer JWT | Diagnóstico do dono do token |

## Contrato de resposta

`DiagnosisResponse` (record):

- `totalAttempts` — total de respostas registradas.
- `scoredAttempts` — pontuáveis (não-anuladas).
- `correct`, `incorrect`, `annulled`.
- `accuracy` — `correct / scored` (NULL quando sem pontuáveis, nunca zero inventado).
- `overallLevel` — `DESCONHECIDO` / `INICIAL` / `EM_DESENVOLVIMENTO` / `CONSOLIDADO`.
- `lastAttemptAt` — última tentativa (UTC).
- `unclassifiedAttempts` — tentativas sem classificação vigente.
- `strengths` — `DOMINADO` (aproveitamento >= 70%, sinal >= 3 pontuáveis).
- `weaknesses` — `FRAGIL` (aproveitamento < 50%, sinal >= 3 pontuáveis).
- `gaps` — `NAO_AVALIADO` (0 tentativas pontuáveis).
- `lowSignal` — `EM_OBSERVACAO` (1-2 pontuáveis).
- `priorities` — fila de atenção: não-dominados em ordem determinística.
- `byTopic` — todos os assuntos da taxonomia com diagnóstico.
- `byDiscipline` — disciplina factual (`questions.discipline_id`).
- `notes` — evidências, limitações e referências (TASK 1.3, 12.2, etc.).

## Regras determinísticas

### Limiares (documentados e auditáveis)

| Condição | Classificação | Observação |
|---|---|---|
| `scored == 0` | `NAO_AVALIADO` | Lacuna (sem tentativas no assunto) |
| `1 <= scored < 3` | `EM_OBSERVACAO` | Sinal insuficiente (mínimo 3) |
| `scored >= 3` e `accuracy >= 0.7` | `DOMINADO` | Ponto forte |
| `scored >= 3` e `0.5 <= accuracy < 0.7` | `EM_DESENVOLVIMENTO` | Intermediário |
| `scored >= 3` e `accuracy < 0.5` | `FRAGIL` | Ponto fraco |

Nível geral: `desconhecido` (sem pontuáveis) → `inicial` (< 0.5) → `em_desenvolvimento` (0.5–0.7) → `consolidado` (>= 0.7).

### Prioridade (fila de atenção)

Só assuntos não-dominados (`FRAGIL`, `EM_DESENVOLVIMENTO`, `NAO_AVALIADO`, `EM_OBSERVACAO`). Ordem determinística:

1. `FRAGIL` — pior aproveitamento primeiro.
2. `EM_DESENVOLVIMENTO` abaixo da média geral.
3. `NAO_AVALIADO` — lacunas (recorrência histórica desc).
4. `EM_OBSERVACAO` — sinal insuficiente (recorrência desc).
5. `EM_DESENVOLVIMENTO` na média ou acima.

Desempates: código alfabético (nunca aleatório).

### Fontes de evidência

- **Factual (desempenho):** `question_attempts` — imutável (trigger `trg_attempts_immutable`).
- **Derivada (assunto):** `question_classifications` vigente (`status <> 'REJECTED'`) — revisão humana PENDENTE (TASK 12.2), nunca verdade oficial do IFRN.
- **Derivada (histórico):** `classifications.countByTopic()` + `questions.countByDisciplineCode()` — mesma revisão pendente.
- **Anuladas:** contam como conteúdo, nunca entram no aproveitamento (regra DESCONHECIDA, TASK 1.3 §4).
- **Dificuldade estimada:** NÃO usada (palpite global BAIXA, sem calibração por desempenho).
- **Edições:** 6 edições analisadas (2020, 2022–2026); 2021 ausente no dataset, nunca interpolado.

### Explicabilidade

Cada `TopicDiagnosisItem` traz `reason`: frase determinística com números (`corretas / pontuáveis`, aproveitamento, quantidade histórica, edições, percentual do banco) e referência à evidência (`task 1.5`, `task 1.4`, `task 4.1`). A fila de prioridade (`PriorityItem`) repete o `reason` com `rank` determinístico (1-based).

## Segurança e limites

- Apenas o dono do token (`@AuthenticationPrincipal`).
- Sem token → `401 UNAUTHORIZED` no envelope `{code, message, details, traceId, timestamp, path}`.
- Nenhum dado pessoal no corpo além dos próprios; PII só no contexto autenticado.
- Nenhum stack trace exposto em produção.

## OpenAPI

Contratos disponíveis em `GET /v3/api-docs` (público, sem PII) e `GET /swagger-ui.html`. Tag `Diagnóstico`; escopo `/api/v1/**`.

## Verificação (2026-09-30)

- `mvn test` → 142/142 (incl. `DiagnosisServiceTest` 3, `DiagnosisControllerTest` 2).
- `GET /api/v1/diagnosis` (autenticado) retorna `200` com envelope completo; sem token retorna `401`.
- `GET /v3/api-docs` lista o path `/api/v1/diagnosis`.
- Limiares auditáveis via `DiagnosisService.MasteryLevel()` (testado com 7 casos: NAO_AVALIADO, EM_OBSERVACAO, FRAGIL, DOMINADO, EM_DESENVOLVIMENTO).
