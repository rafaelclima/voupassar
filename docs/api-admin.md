# API Administrativa — observabilidade do banco

> Base: `/api/v1/admin`. Autorização: `CURATOR` ou `ADMIN` em **todas** as rotas
> (`@PreAuthorize("hasAnyRole('CURATOR','ADMIN')")`; sem papel → 403 em envelope,
> sem Bearer → 401). Implementação: `backend/src/main/java/br/com/voupassar/admin/`.
> Testes: `AdminServiceTest` (métricas/inconsistências) + `AdminSecurityTest` (matriz 401/403/200).
>
> Desde a decisão de produto 2026-10-06 não há fila de revisão nem PATCH de
> curadoria: esta área é **somente leitura** (observabilidade). Histórico do
> contrato anterior em `docs/curadoria.md` (superseded).

## 0. Papéis e concessão

Papéis-semente (V2): `STUDENT` (todo cadastro), `CURATOR` (gestão do banco),
`ADMIN` (métricas e gestão). Os papéis viajam no
access JWT (`roles`) e viram `ROLE_*` no filtro — nenhum segredo novo.

O cadastro cria só `STUDENT`. Conceder papel é SQL direto (sem UI, sem
endpoint de autopromoção — autopromoção seria falha de segurança):

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT :user_id, r.id FROM roles r WHERE r.code = 'CURATOR'
ON CONFLICT DO NOTHING;
```

O token em uso precisa ser reemitido (login/refresh) para carregar o novo papel.

## 1. Endpoints

| Método | Rota | Papel | O que faz |
|---|---|---|---|
| GET | `/admin/inconsistencies` | CURATOR+ | 4 checagens vivas (contagem + até 20 ids de amostra) |
| GET | `/admin/metrics` | CURATOR+ | Totais de questões/classificações, anuladas, com-figura (sem PII, sem conteúdo) |
| GET | `/admin/diagnostics` | CURATOR+ | Diagnóstico técnico (TASK 22.2): serviço, versão, `dbStatus`, última migration, uptime + contadores `voupassar.*` (sem PII) |

`AdminMetricsResponse`: `questionsTotal`, `questionsAnnulled`,
`questionsWithFigure`, `classificationsTotal` (sem mapas por status).

Exemplo:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/v1/admin/metrics"

curl -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/v1/admin/inconsistencies"

curl -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/v1/admin/diagnostics"
```

## 2. Inconsistências cobertas

`OPTIONS_COUNT` (alternativas ≠ 4) · `ANSWER_NOT_IN_OPTIONS` (resposta fora das
opções, não-anuladas) · `EMPTY_STATEMENT` · `CLASSIFICATION_WITHOUT_TOPIC`.
Estado esperado hoje (TASK 11.1): tudo zero. Checagem de fontes
(`PRIMARY+GABARITO`) segue file-level na auditoria 11.1 (sem entidade JPA para
`question_sources` — pendência honesta, não gap silencioso).

## 3. Pendências

* Entidade JPA de `question_sources` para trazer a checagem de fontes à API.
