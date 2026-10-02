# API Administrativa — TASK 12.1

> Base: `/api/v1/admin`. Autorização: `CURATOR` ou `ADMIN` em **todas** as rotas
> (`@PreAuthorize("hasAnyRole('CURATOR','ADMIN')")`; sem papel → 403 em envelope,
> sem Bearer → 401). Implementação: `backend/src/main/java/br/com/voupassar/admin/`.
> Testes: `AdminServiceTest` (regras) + `AdminSecurityTest` (matriz 401/403/200).

## 0. Papéis e concessão

Papéis-semente (V2): `STUDENT` (todo cadastro), `CURATOR` (revisar
classificações e publicação), `ADMIN` (métricas e gestão). Os papéis viajam no
access JWT (`roles`) e viram `ROLE_*` no filtro — nenhum segredo novo.

O cadastro cria só `STUDENT`. Conceder curadoria é SQL direto (sem UI, sem
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
| GET | `/admin/review-queue?validationStatus=PENDING&page=0&size=20` | CURATOR+ | Questões por status de validação, ordem ano-fonte/número/id (detalhe integral no `GET /questions/{id}`) |
| GET | `/admin/inconsistencies` | CURATOR+ | 4 checagens vivas (contagem + até 20 ids de amostra) |
| GET | `/admin/metrics` | CURATOR+ | Totais por validação/publicação/classificação, anuladas, com-figura (sem PII, sem conteúdo) |
| PATCH | `/admin/questions/{id}/status` | CURATOR+ | Atualiza `validationStatus` e/ou `publicationStatus` |
| PATCH | `/admin/classifications/{id}` | CURATOR+ | `REVIEWED/APPROVED/REJECTED` + `observation`; carimba `reviewed_by/at` |

Exemplos:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "$BASE/api/v1/admin/metrics"

curl -X PATCH -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"APPROVED","observation":"Assunto confirmado contra o caderno, pág. 12."}' \
  "$BASE/api/v1/admin/classifications/10"

curl -X PATCH -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"validationStatus":"APPROVED","publicationStatus":"SOMENTE_REFERENCIA"}' \
  "$BASE/api/v1/admin/questions/1/status"
```

## 2. Regras auditáveis (erros 400 em envelope)

* Ao menos um campo no PATCH de questão; enums estritos nos dois PATCHes.
* `PUBLICAVEL` exige questão `APPROVED` — nunca publicar sem revisão.
* Questão `APPROVED` só sai desse estado para `REJECTED` (sem regressão a
  `PENDING/REVIEWED`); classificação `APPROVED` idem (só `REJECTED`/revogação).
* 404 para questão/classificação inexistente; observações vazias viram NULL.
* Escritas via UPDATE dirigido (`@Modifying` nativo); entidades de leitura
  seguem `@Immutable`. Nenhuma deleção por aqui (detecção gera fila, AGENTS.md §23).

## 3. Inconsistências cobertas

`OPTIONS_COUNT` (alternativas ≠ 4) · `ANSWER_NOT_IN_OPTIONS` (resposta fora das
opções, não-anuladas) · `EMPTY_STATEMENT` · `CLASSIFICATION_WITHOUT_TOPIC`.
Estado esperado hoje (TASK 11.1): tudo zero. Checagem de fontes
(`PRIMARY+GABARITO`) segue file-level na auditoria 11.1 (sem entidade JPA para
`question_sources` — pendência honesta, não gap silencioso).

## 4. Pendências (não bloqueiam a 12.1)

* Tela web de admin (fila/métricas/curadoria em UI) — API pronta, UI futura.
* Curadoria efetiva das 38 classificações `NECESSITA_REVISAO` (TASK 12.2).
* Entidade JPA de `question_sources` para trazer a checagem de fontes à API.
