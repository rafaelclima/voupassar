# Importação das Questões para o PostgreSQL — TASK 2.3

> Processo repetível, idempotente e auditável que converte o dataset da
> Fase 1 (`data/extracted/`, `data/linked/`,
> `docs/content-analysis/per-edition/`) nas tabelas de conteúdo do banco
> (DDL + seed: TASK 2.2, `database/migrations/V1__schema.sql`,
> `V2__seed.sql`). Implementação: `scripts/db/import_questions.py` v1.0.0
> (stdlib + `psql`, sem dependências novas).
> Nenhuma informação inventada: o que a fonte não comprova entra como
> `PENDING` / `PENDENTE_REVISAO` / `NULL`.

## 0. Como executar

```bash
cp .env.example .env            # uma vez (nunca commitar o .env)
docker compose up -d db
./scripts/db/migrate.sh         # V1 + V2 (base; questões só via importador)
python3 scripts/db/import_questions.py --check   # valida sem escrever
python3 scripts/db/import_questions.py           # importa (idempotente)
```

`--report PATH` (padrão `data/import/report.json`): relatório de auditoria
da execução. Saída com código `2` = divergência fonte×banco (nada alterado)
ou falha de validação; `1` = contagens finais fora do esperado; `0` = OK.

## 1. Contrato de idempotência (docs/database-erd.md §8)

* Chave natural: `(source_type, source_year, source_question_number,
  exam_document_id)` + `ON CONFLICT DO NOTHING`.
* `checksum` (SHA-256 de enunciado + alternativas A–D normalizados com NFC
  + colapso de whitespace): igual = reexecução segura; diferente =
  divergência registrada no relatório, **sem UPDATE/DELETE silencioso**.
* Filhos (`options`, `sources`, `classifications`, `tag_map`) são
  conferidos e completados a cada execução (recupera interrupção parcial
  sem duplicar: `ON CONFLICT DO NOTHING` + checagem de existência, pois
  `question_sources` não tem UNIQUE).
* Documentos resolvidos por `sha256` (nomes literais conferidos; divergência
  de nome = erro, não correção automática).

## 2. Regras de mapeamento (decisões auditáveis)

| Campo | Regra |
|---|---|
| `source_type / kind` | `OFFICIAL` / `OBJECTIVE` em tudo (240; fonte só tem objetivas) |
| `answer_key / annulled` | transcrição literal do gabarito (`X` ⟺ anulada; 5 anuladas) |
| `validation_status / publication_status` | `PENDING` / `PENDENTE_REVISAO` em tudo (curadoria: TASK 12.2) |
| `difficulty_estimate` | palpite da classificação (confiança BAIXA global, sem dados) |
| `question_classifications` | `taxonomy_version='v1.1'`, `status='PENDING'`, `confidence` = `assunto_confianca`, `observation` = motivo original (NULL se vazio) |
| normalização v1.1 | mesma `V11_OVERRIDES` de `build_content_map.py` (2022 Q12, 2023 Q12, 2024 Q17 → `ACENTUACAO_GRAFICA`; 2026 Q21 → `ARITMETICA/SISTEMAS_NUMERACAO`) |
| `has_figure` | `TRUE` nos 36 `NECESSITA_REVISAO` com evidência visual; `FALSE` nos 2 conceituais sem figura: (2023,Q40) e (2024,Q17) — ver observações em `per-edition/` |
| tags | `FIGURA` nos 36 `has_figure` (granularidade CHARGE/GRAFICO = curadoria) |
| `question_sources` | 2 por questão: `PRIMARY` (caderno + páginas) e `GABARITO` (sha, sem páginas) |

## 3. Resultado verificado (2026-09-30, Postgres 16 local)

1ª execução: `a_inserir=240 ja_presentes=0 divergencias=0` → exit 0.
2ª execução: `a_inserir=0 ja_presentes=240 divergencias=0` → exit 0
(idempotência comprovada; contagens idênticas).

| Tabela | Linhas | Esperado |
|---|---|---|
| `questions` (OFFICIAL) | 240 (40 × 6 edições) | 240 |
| `question_options` | 960 | 960 |
| `question_sources` | 480 (240 PRIMARY + 240 GABARITO) | 480 |
| `question_classifications` (v1.1/PENDING) | 240 | 240 |
| `question_tag_map` (FIGURA) | 36 | 36 |
| `checksum` distintos | 240 | 240 |
| anuladas (`X`: 2020 Q7/Q26, 2023 Q21, 2024 Q17, 2026 Q37) | 5 | 5 |

Relatório máquina-legível: `data/import/report.json` (regenerado a cada
execução; trilha de auditoria, não fonte de verdade — a fonte seguem sendo
os PDFs + JSONs da Fase 1).

## 4. Pendências (não bloqueiam a TASK)

* 38 classificações aguardam revisão humana (`NECESSITA_REVISAO` na origem;
  fila detalhada em `docs/content-analysis/summary.md §3`).
* Regra de pontuação de anuladas segue DESCONHECIDA (TASK 1.3 §4).
* Nota 2026-10-04: coluna `questions.explanation` removida (V11) por decisão
  de produto — a plataforma testa conhecimento, não ensina passo a passo.
