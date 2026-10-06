# Relatório do lote piloto 60 — questões autorais (TASK 15.2)

> Data: 2026-10-06. Contrato: `docs/authoral/manual-item-v1.md` (v1 + §11
> suplemento 15.2). Validador: `scripts/authoral/validate_authoral.py --check`.
> Resultado: **60/60 verdes, 0 colisão de checksum contra os 240 oficiais,
> 0 colisão dentro do lote.**

## 1. Composição do lote

O lote de 60 é a soma dos **10 itens-teste da 15.1** (mesmo contrato, já
verdes, aprovados pelo responsável) com **50 itens novos** gerados para
completar exatamente a distribuição exigida:

| Disciplina | Subassunto | 15.1 | novos | total | Alvo 15.2 |
|---|---|---|---|---|---|
| MAT | REGRA_DE_TRES | 1 | 5 (`AUT-RT-02…06`) | 6 | 6 ✓ |
| MAT | FUNCAO_AFIM | 1 | 4 (`AUT-FA-01,03,04,05`) | 5 | 5 ✓ |
| MAT | EQUACOES | 1 | 4 (`AUT-EQ-01,02,04,05`) | 5 | 5 ✓ |
| MAT | CALCULO_DIRETO | 1 | 4 (`AUT-PC-01,02,03,05`) | 5 | 5 ✓ |
| MAT | MEDIA | 1 | 3 (`AUT-MD-01,02,03`) | 4 | 4 ✓ |
| MAT | PROBABILIDADE | 0 | 3 (`AUT-PB-01…03`) | 3 | 3 ✓ |
| MAT | AREA_PLANA | 0 | 2 (`AUT-AP-01,02`) | 2 | 2 ✓ |
| LP | INFERENCIA | 1 | 7 (`AUT-IN-01…05,07,08`) | 8 | 8 ✓ |
| LP | INFORMACAO_EXPLICITA | 1 | 5 (`AUT-IE-01…05`) | 6 | 6 ✓ |
| LP | PONTUACAO | 1 | 4 (`AUT-PT-01…04`) | 5 | 5 ✓ |
| LP | COESAO_REFERENCIA | 1 | 3 (`AUT-CR-01…03`) | 4 | 4 ✓ |
| LP | MORFOLOGIA | 1 | 3 (`AUT-MF-01…03`) | 4 | 4 ✓ |
| LP | INTENCAO_COMUNICATIVA | 0 | 3 (`AUT-IC-01…03`) | 3 | 3 ✓ |
| **Total** | | **10** | **50** | **60** | **60 ✓** |

Por assunto: `RAZAO_PROPORCAO` 6 · `ALGEBRA` 10 · `PORCENTAGEM` 5 ·
`ESTATISTICA_DADOS` 7 · `GEOMETRIA` 2 · `GRAMATICA_NORMA` 13 ·
`INTERPRETACAO_TEXTUAL` 17.

## 2. Por dificuldade (alvo editorial, a calibrar na Fase 4)

| difficulty_alvo | n | % |
|---|---|---|
| FACIL | 41 | 68,3% |
| MEDIA | 19 | 31,7% |
| DIFICIL | 0 | 0% |

Decisão registrada: nenhum DIFICIL no piloto — os moldes de 2 etapas
(REGRA_DE_TRES composta, MEDIA com total implícito, CALCULO_DIRETO reverso)
foram classificados MEDIA; itens DIFICIL ficam para lote futuro com dados
de desempenho para calibragem, como nas oficiais.

## 3. Distribuição dos gabaritos

`A: 16 · B: 16 · C: 16 · D: 12` — sem padrão explorável (a correta não
concentra numa letra; D levemente abaixo por arredondamento da partilha,
sem viés por subassunto).

## 4. Taxa de reprovação nos validadores

| Camada | Resultado |
|---|---|
| `validate_authoral.py --check` (60 specs) | **60/60 verdes na 1ª passagem — 0 reprovações** |
| Colisão de checksum vs. 240 oficiais | 0 |
| Colisão de checksum dentro do lote | 0 |
| Conferência independente de gabarito (recomputação dos 27 MAT numéricos + inspeção das 3 transcrições de função) | 27/27 com o valor correto no rótulo certo; LP conferidos por trecho do `texto_base` (checklist do manual §8.5) — 0 correções necessárias |
| Itens reescritos após reprovação | **0 (nenhum item precisou retornar)** |

Leitura honesta: taxa 0% reflete geração em passedada única sob o manual
v1 (molde + checklist aplicados na redação, antes do validador), não
ausência de gate — o gate rodou integralmente e falha alto (provado na
15.1 com casos adversariais: `answerKey: X`, string proibida e checksum
duplicado, todos rejeitados com exit 1).

## 5. Exclusões do piloto (respeitadas)

Nenhum spec usa: `GRANDEZAS_MEDIDAS`, `SISTEMAS_NUMERACAO`,
`VOLUME`/`PERIMETRO_COMPRIMENTO`/`UNIDADES_MEDIDA`, códigos sem ocorrência
(`JUROS_COMPOSTOS`, `DESCONTO`, `MEDIANA_MODA`), itens com figura, nem
qualquer string de `IFRN`/`edital`/`prova 20XX` (verificado pelo validador
em enunciado, opções, texto-base e rationales).

## 6. Rastreabilidade

Cada spec carrega 2 `referencias_oficiais` do **mesmo subassunto** (v1.1),
todas resolvidas contra `data/extracted/*.json` pelo validador. Textos-base
dos 30 itens de LP são inéditos (redação própria), sem transcrição ou
paráfrase de cadernos oficiais ou de terceiros.

## 7. Importação — TASK 15.3 [CONCLUÍDA 2026-10-06]

Migração `V13__authoral_pipeline.sql` (páginas NULL sem documento-fonte;
`chk_questions_authoral_nulls` + `chk_questions_official_pages`) e
`scripts/db/import_authoral.py` (gate do validador, idempotente por
checksum + `spec=<id>`, `--report data/authoral/report.json`).

Validado em scratch (restore do dev + Flyway V8..V13 + 240 oficiais +
60 autorais) e aplicado no dev: `official=240` intacto,
`authoral=60/options=240/classifications=60`, reexecução sem duplicar
(0 a inserir), spec adulterado → DIVERGENCIA exit 2 sem escrita.
API: `GET /questions?sourceType=AUTHORAL` = 60,
`?sourceType=OFFICIAL` = 240; 4 checks do `/admin/inconsistencies` zerados.

> Nota de sequenciamento para a 15.4: com as 60 no banco, listagens sem
> filtro de origem passam a misturar (`GET /questions` padrão, `conteúdos`
> por confiança/ano, seleção de simulado por disciplina e revisão) — o
> filtro Origem + toggle (default `OFFICIAL`) + selo da 15.4 resolvem a
> vitrine; `content-map`/`evidence_json` do roteiro e simulado de edição
> real seguem 100% oficiais por construção.
