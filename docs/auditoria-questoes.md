# Auditoria do Banco de Questões — TASK 11.1

> Data: 2026-10-02. Fonte de verdade: PostgreSQL local (Docker, `database/migrations/V1__schema.sql`,
> seed V2, importador `scripts/db/import_questions.py` v1.0.0) + arquivos da Fase 1
> (`data/extracted/`, `data/linked/`, `docs/content-analysis/`) + `docs/gabaritos-validation.md` (TASK 1.3)
> + `docs/importacao-questoes.md` (TASK 2.3). Nada inventado: o que a fonte não comprova está
> marcado como `PENDENTE` / `NECESSITA REVISÃO` / `DESCONHECIDO`.

## 0. Método e reprodução

```bash
python3 scripts/analysis/extract_questions.py --check   # 6×40 OK, sem opts vazias
python3 scripts/analysis/link_answer_keys.py --check   # 6×40 OK, 5 anuladas
python3 scripts/analysis/validate_classification.py    # 6×40 OK (38 em revisão)
python3 scripts/db/import_questions.py --check        # 240 presentes, 0 a inserir, 0 divergências
```

Relatório do importador: `data/import/report.json` (`status: OK`,
`questions=240, options=960, sources=480, classifications=240, has_figure=36`).
Consultas SQL abaixo foram executadas contra o banco local em 2026-10-02
(`docker compose ps`: `db` e `app` healthy).

## 1. Contagens gerais (banco × esperado)

| Tabela | Banco | Esperado | Veredito |
|---|---|---|---|
| `questions` (OFFICIAL) | 240 (40 × 6 edições) | 240 | OK |
| `question_options` | 960 | 960 | OK |
| `question_sources` | 480 (240 PRIMARY + 240 GABARITO) | 480 | OK |
| `question_classifications` (v1.1/PENDING) | 240 | 240 | OK |
| `checksum` distintos | 240 | 240 | OK |
| `has_figure=TRUE` | 36 | 36 (importador §2) | OK |

Por edição (LP 20 + MAT 20 em todas — ver §6):

| Edição | Total | Anuladas (X) | Confirmadas (A–D) |
|---|---|---|---|
| 2020 | 40 | 2 (Q7, Q26) | 38 |
| 2022 | 40 | 0 | 40 |
| 2023 | 40 | 1 (Q21) | 39 |
| 2024 | 40 | 1 (Q17) | 39 |
| 2025 | 40 | 0 | 40 |
| 2026 | 40 | 1 (Q37) | 39 |
| **Total** | **240** | **5** | **235** |

Distribuição de `answer_key`: A=73, B=50, C=66, D=46, X=5. Registro factual,
sem inferência sobre intencionalidade da banca.

## 2. Duplicatas

| Checagem | SQL | Resultado |
|---|---|---|
| Chave natural `(source_type, source_year, source_question_number, exam_document_id)` | `GROUP BY … HAVING count(*)>1` | **0 grupos** |
| `checksum` (SHA-256 enunciado + A–D normalizados) | `GROUP BY checksum HAVING count(*)>1` | **0 grupos** |
| Alternativas duplicadas dentro da questão | `GROUP BY (questão, option_text) HAVING count(*)>1` | **0 grupos** |
| Enunciados idênticos cross-questão (trecho 80 chars normalizado) | agrupamento por prefixo | 4 prefixos genéricos de LP repetidos (ex. `De acordo com o Texto 1,` em 5 questões) — **falso positivo esperado**: stems de interpretação se repetem entre edições; `checksum` completo distingue as 240 (nenhum checksum repetido) |

Reexecução do importador é idempotente (`ON CONFLICT DO NOTHING` + checagem de
checksum; 2ª execução documentada na TASK 2.3: `ja_presentes=240, divergencias=0`;
`--check` de 2026-10-02 confirma o mesmo estado). Detecção automática gera
suspeita, nunca apaga conteúdo (AGENTS.md §23) — nenhum DELETE foi executado.

## 3. Respostas

* `answer_key` fora de `A/B/C/D/X`: **0** (constraint `CHECK` + `--check` da TASK 1.3).
* Questões sem resposta (`MISSING_ANSWER`): **0** — 240/240 vinculadas (TASK 1.3 §0).
* Coerência `annulled ⟺ answer_key='X'`: **0 violações** (constraint + checagem).
* Toda questão não-anulada tem sua `answer_key` presente entre as 4 alternativas: **0 violações**
  (`NOT EXISTS` alternativo = 0).
* Todas as objetivas têm exatamente 4 alternativas: **240/240 com `n_opts=4`**; nenhuma
  questão com `<>4` (trigger `enforce_four_options` + checagem).
* Nenhum `statement` vazio e nenhuma `option_text` vazia: **0 linhas**.
* Regra de pontuação de anuladas: **DESCONHECIDA** (TASK 1.3 §4) — o banco registra o fato
  (`annulled=TRUE`, `answer_key='X'`), não a regra de negócio.

## 4. Fontes e proveniência

* Toda questão tem exatamente 2 fontes: **240/240 com `n_src=2`** (`PRIMARY` + `GABARITO`);
  zero questões sem `PRIMARY` ou sem `GABARITO`.
* `question_sources`: 240 `PRIMARY` (caderno + páginas) + 240 `GABARITO` (sha, sem páginas).
* `exam_document_id` resolvido por `sha256` (nomes literais conferidos; divergência de
  nome = erro, não correção — TASK 2.3 §1).
* `source_type`: 240 `OFFICIAL`; zero `exam_id` nulo em `OFFICIAL`; zero números fora de 1–40;
  zero páginas inválidas (`page_start>0`, `page_end>=page_start`).
* Disciplina por edição: 20 `LINGUA_PORTUGUESA` + 20 `MATEMATICA` nas 6 edições (12/12 grupos = 20).
  Para 2022/2026 a divisão LP 1–20 / MAT 21–40 é herdada do caderno (`discipline_source`),
  pois o gabarito não rotula disciplinas (TASK 1.3 §2.4 item 3).
* Status de publicação: **240/240 `validation_status=PENDING` / `publication_status=PENDENTE_REVISAO`**
  — nenhuma questão marcada `PUBLICAVEL` sem curadoria (TASK 12.2). Mecanismo de proveniência
  exigido pelo AGENTS.md §12 existe no schema (`question_sources`, `validation_status`,
  `publication_status`); o conteúdo completo publicado segue pendente de revisão humana.

## 5. Classificações

* 240/240 questões com 1 classificação `taxonomy_version='v1.1'`, `status='PENDING'`,
  `origin='CLASSIFICACAO_DERIVADA_FONTE'`; zero `APPROVED` (curadoria TASK 12.2 pendente).
* Confiança do assunto: ALTA 170 · MEDIA 46 · BAIXA 24. Dificuldade estimada é palpite
  (confiança BAIXA global, sem dados de desempenho) — ver `docs/content-analysis/summary.md`.
* `validate_classification.py`: OK nas 6 edições; **38 itens em `NECESSITA_REVISAO`**
  (fila detalhada em `summary.md §3`: 35 figuras/gráficos/charges + 3 conceituais —
  2020 Q26, 2024 Q17, 2023 Q40).
* `has_figure=TRUE` em 36 questões; tag `FIGURA` em 36 (`question_tag_map`).
  Divergência aparente 36 vs 38: 2 itens da fila são conceituais sem figura
  ((2023,Q40) e (2024,Q17)) — coerente com o importador (TASK 2.3 §2).
* `explanation`: **NULL em 240/240** — nenhuma explicação inventada (correto; redação
  de correções é trabalho futuro de curadoria, não desta task).

## 6. Questões anuladas

5 no total, características por edição (nunca regra geral):

| Edição | Anuladas | Evidência |
|---|---|---|
| 2020 | Q7 (LP), Q26 (MAT) | `X (questão anulada)` no gabarito final |
| 2022 | — | preliminar ≡ definitivo 40/40, sem `X` |
| 2023 | Q21 (MAT) | `X` na grade pág. 3 |
| 2024 | Q17 (LP) | `X` na grade |
| 2025 | — | grade 40× A–D, sem `X` |
| 2026 | Q37 (MAT) | `X = QUESTÃO ANULADA` |

Banco: `annulled=TRUE` nas mesmas 5; `question_attempts` com `was_annulled`/`is_correct`
tratados pelo schema (tentativas em anuladas: `is_correct IS NULL`).

## 7. Dados incompletos / pendências (não silenciados)

1. **Revisão visual pendente (38 itens)** — `summary.md §3` + TASK 1.3 §4: figuras, frações
   (ex. 2020 Q38 — frações achatadas pelo `pdftotext`, resposta A vinculada mas conteúdo
   `NECESSITA REVISÃO`), charges e gráficos ilegíveis no texto extraído. Nenhum item
   publicado como `PUBLICAVEL`.
2. **Explicações ausentes (240/240 NULL)** — esperado; redigir com fonte, sem inventar.
3. **Regra de pontuação de anuladas DESCONHECIDA** — definir na Fase 4/5 com fonte explícita.
4. **Gabarito 2025 com nome de arquivo `…2024…` mas conteúdo `…2025`** — vinculação usa o
   conteúdo; decisão de renomear é de curadoria (TASK 1.3 §2.4 item 1).
5. **2022: retificação posterior ao definitivo FUNCERN NÃO CONFIRMADA** (fora do repo).
6. **Conferência visual amostral das grades no PDF renderizado** — contagem regex validada
   (40/40 por edição), leitura humana residual pendente (TASK 1.3 §4).
7. Observabilidade do banco (backup/restore) é TASK 13.2, fora deste escopo.
8. **Trechos compartilhados presos na alternativa D (7 itens, 2026-10-02)** — o bloco da
   questão engole o texto-base das questões seguintes ("Considere o trecho…", figura ou
   gráfico): 2020 Q3 (trecho p/ Q4–Q7); 2023 Q12 (trecho p/ Q13–Q15), Q24 (Figura 2),
   Q27 (Gráfico 1 p/ Q28–Q29); 2024 Q24 (trecho p/ Q25–Q27); 2026 Q7 (trecho p/ Q8–Q10),
   Q10 (trecho p/ Q11–Q14). O rodapé do meio foi removido (V6), mas o trecho real foi
   **mantido em D sem perda de dado** — realocar para as questões dependentes é decisão
   de modelagem + curadoria humana (TASK 12.2), não desta limpeza. 2023 Q24/Q27 já estão
   na fila de revisão (figuras); os demais seguem `PENDING`/`PENDENTE_REVISAO`.

## 8. Veredito TASK 11.1

* **Duplicatas:** nenhuma real (0 chaves repetidas, 0 checksums repetidos, 0 alternativas
  duplicadas; stems repetidos de LP são falso positivo documentado).
* **Respostas:** 240/240 vinculadas, 0 ausentes, 0 incoerências `annulled⟺X`,
  240/240 com 4 alternativas e resposta presente entre elas.
* **Fontes:** 240/240 com `PRIMARY+GABARITO`, proveniência completa, páginas válidas.
* **Classificações:** 240/240 presentes (v1.1/PENDING), 38 `NECESSITA_REVISAO` mapeados,
  0 `APPROVED` sem revisão humana — estado honesto.
* **Anuladas:** 5 corretamente marcadas, por edição, sem generalização.
* **Incompletos:** nenhum silenciado — 240 `PENDENTE_REVISAO`, 240 explicações NULL,
  38 fila de revisão, pendências listadas no §7.

**Conclusão:** o banco de questões está íntegro para os critérios da TASK 11.1.
Nenhum bloqueador para a TASK 11.2 (relatório de cobertura). Curadoria humana
(TASK 12.2) continua pendente por definição — não é falha da auditoria.
