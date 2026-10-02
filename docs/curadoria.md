# Curadoria Assistida — TASK 12.2

> Data: 2026-10-02. Ferramenta: Claude/LLM via OpenCode, usuário
> `curadoria@voupassar.local` (CURATOR+ADMIN) com novo token após promoção.
> Decisões gravadas via `PATCH /api/v1/admin/classifications/{id}` — nunca
> direto no banco para carimbar `reviewed_by/at` corretamente.
> Nenhuma questão foi marcada `PUBLICAVEL`: publicação depende de revisão
> visual de figuras + explicação redigida (ambos pendentes).

## 1. O que foi feito

1. Correção do importador: `scripts/db/import_questions.py` agora grava a
   coluna `observation` em `question_classifications` (antes a variável `obs`
   era calculada e descartada). Backfill executado via
   `scripts/db/backfill_classification_observations.py` (105 observações
   preenchidas, relatório em `data/import/backfill-observations.json`).
2. Pré-curadoria das 240 classificações via
   `scripts/analysis/curate_classifications.py`, cruzando
   `docs/content-analysis/per-edition/` com `data/extracted/`:
   - **APPROVED: 200** — evidência ≥30 chars, confiança ALTA/MEDIA, sem
     pendência `NECESSITA_REVISAO`.
   - **REVIEWED: 39** — confiança BAIXA (24) + figuras/gráficos/charges
     ilegíveis (14) + anuladas conceituais (2: 2020 Q26, 2024 Q17);
     exigem conferência visual no PDF.
   - **REJECTED: 1** — 2023 Q40: classificação sugere MMC=2032, mas o
     gabarito oficial indica A=2044; NÃO reinterpretar sem o PDF.
3. UI admin (TASK 12.1) ampliada: fila agora mostra
   `classificationObservation` e `classificationEvidence` para o curador
   humano, via `ReviewQueueItemResponse` (novos campos).

## 2. Estado do banco (métrica API admin, 2026-10-02 14:20 UTC)

`classificationsByStatus`: APPROVED 200 · REVIEWED 39 · REJECTED 1 ·
PENDING 0. `questionsByValidation` permanece 240 PENDING /
`questionsByPublication` 240 PENDENTE_REVISAO — aprovação de questão só após
conferência de figura e explicação redigida. `question_attempts` e roteiro
continuam alimentados pelo estado honesto da classificação.

Por edição (40 itens cada):

| Ed. | APPROVED | REVIEWED | REJECTED |
|---|---|---|---|
| 2020 | 32 | 8 | 0 |
| 2022 | 32 | 8 | 0 |
| 2023 | 33 | 6 | 1 (Q40) |
| 2024 | 35 | 5 | 0 |
| 2025 | 34 | 6 | 0 |
| 2026 | 34 | 6 | 0 |

240/240 classificações com `reviewed_by` carimbado. As 5 anuladas (2020 Q7 e
Q26, 2023 Q21, 2024 Q17, 2026 Q37) seguem registradas como conteúdo; a
anulação é fato da questão (badge "Anulada (X)" na fila), não da classificação,
e anuladas já ficam fora do sorteio de simulado por regra do repositório.

## 3. Honestidade

- Isto é **pré-curadoria LLM**, não aprovação final humana. O campo
  `reviewed_by` aponta para o usuário de curadoria criado para esta operação;
  um humano referenda na UI admin antes de promover questão a APPROVED
  (regra já no `AdminService`).
- Comentários "Referendo humano…" foram gravados em todas as observações de
  APPROVED para não confundir aprovação provisória com confirmação visual.
- A decisão de REVIEWED nunca "melhora" o dado: 39 itens continuam pendentes
  de conferência visual, alguns com conteúdo ilegível no texto extraído.
  Uma classificação aprovada aqui significa "a evidência textual sustenta o
  assunto", não "a questão está pronta para publicar".
- A credencial usada na operação (`curadoria@voupassar.local`, senha gerada
  nesta sessão) é local de desenvolvimento. Promoção de papel é SQL por ADMIN
  (`docs/api-admin.md §0`); em ambiente real use cofre de segredos e conta
  nominal do responsável.
- Questões anuladas (2020 Q7/Q26, 2023 Q21, 2024 Q17, 2026 Q37) seguem
  registrando conteúdo; regra de pontuação DESCONHECIDA.

## 4. Como operar a curadoria humana final

1. Login com papel CURATOR/ADMIN (SQL de concessão em `docs/api-admin.md §0`).
2. Em `admin.html`, filtrar "Status da classificação" = `REVIEWED` (39 itens)
   — cada item traz `Observação IA` e `Evidência` para decidir sem abrir a
   questão; `Ver questão` abre o detalhe integral.
3. Conferir contra o PDF em `data/provas/<ano>/` e o caderno impresso.
4. Via UI: alterar status da questão, classificação e publicação; ou via API:
   `PATCH /api/v1/admin/questions/{id}/status` e
   `PATCH /admin/classifications/{id}`.
5. Só após revisão visual + explicação redigida usar
   `validationStatus=APPROVED` e `publicationStatus=PUBLICAVEL`.

## 5. Reprodução

```bash
# 1. observações da IA no banco (idempotente)
python3 scripts/db/backfill_classification_observations.py --check

# 2. pré-curadoria (dry-run primeiro; token precisa dos papéis já no JWT)
python3 scripts/analysis/curate_classifications.py --token "$TOKEN" --dry-run
python3 scripts/analysis/curate_classifications.py --token "$TOKEN"

# 3. conferência com a API
curl -H "Authorization: Bearer $TOKEN" "$BASE/api/v1/admin/metrics"
```

`APPROVED` é terminal no servidor: o script pula itens já aprovados
(`skipped_already_approved`) em vez de tomar 400 do backend. Decisão completa
item a item em `docs/curadoria-llm.json`.
