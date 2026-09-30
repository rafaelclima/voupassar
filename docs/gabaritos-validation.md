# Validação da Vinculação com Gabaritos — TASK 1.3

> Fonte de verdade: os 6 PDFs de gabarito em `data/provas/<edicao>/` + as
> extrações `data/extracted/<edicao>.json` (TASK 1.2). Método:
> `scripts/analysis/link_answer_keys.py` v1.0.0 (`pdftotext -layout`,
> poppler-utils 26.08.0) → `data/linked/<edicao>.json` +
> `data/linked/manifest.json`. Reexecução idempotente sobrescreve os JSONs
> com o mesmo conteúdo (a menos do carimbo `generated_at_utc`).
> Nenhuma classificação pedagógica (assunto/dificuldade) neste documento —
> isso é TASK 1.4. Letras A–D/X abaixo são transcrição literal do gabarito,
> não inferência. O que não pôde ser comprovado está marcado como
> `DESCONHECIDO` / `NÃO CONFIRMADO` / `NECESSITA REVISÃO`.

## 0. Resultado global

| Edição | Documento do gabarito | Páginas | Tipo detectado | Vinculadas | Confirmadas (A–D) | Anuladas (X) | Sem resposta | Divergência prelim/def |
|---|---|---|---|---|---|---|---|---|
| 2020 | `data/provas/2020/Gabarito_Final.pdf` | 1 | grade LP/MAT final | 40/40 | 38 | 2 (Q7, Q26) | 0 | N/A (só final no repo) |
| 2022 | `data/provas/2022/GABARITO_Prova_Exame_Tecnico_Integrado_2022.pdf` | 4 | preliminar + definitivo (FUNCERN) | 40/40 | 40 | 0 | 0 | **0 diferenças (idênticos)** |
| 2023 | `data/provas/2023/Gabarito_Final_-_Exame_de_Selecao_2023.pdf` | 3 | definitivo (grade pág. 3; págs. 1–2 = ofertas) | 40/40 | 39 | 1 (Q21) | 0 | N/A (só definitivo no repo) |
| 2024 | `data/provas/2024/Gabarito_Oficial_Definitivo_IFRN_78.pdf` | 1 | grade LP/MAT definitiva | 40/40 | 39 | 1 (Q17) | 0 | N/A (só definitivo no repo) |
| 2025 | `data/provas/2025/Exame_de_Seleção_2024_-_Gabarito_Final.pdf` | 1 | grade LP/MAT definitiva | 40/40 | 40 | 0 | 0 | N/A (só definitivo no repo) |
| 2026 | `data/provas/2026/GABARITO_EXAME_DE_SELEÇÃO_2026_-_DEFINITIVO.pdf` | 1 | tabela definitiva 2 colunas | 40/40 | 39 | 1 (Q37) | 0 | N/A (só definitivo no repo) |
| **Total** | 6 documentos | — | — | **240/240** | **235** | **5** | **0** | **0** |

Hashes SHA-256 (gabaritos, para auditoria):

| Edição | SHA-256 |
|---|---|
| 2020 | `d12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8` |
| 2022 | `35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c` |
| 2023 | `57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac` |
| 2024 | `6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6` |
| 2025 | `878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756` |
| 2026 | `5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece` |

## 1. Gabaritos transcritos (letras oficiais por questão)

Legenda: `X` = questão anulada conforme o próprio gabarito. Divisão
LP 1–20 / MAT 21–40 conforme os rótulos do gabarito quando presentes;
para 2022 e 2026 o gabarito não rotula disciplinas — a divisão 1–20/21–40
é herdada do caderno (ver §3).

### 2020 — `Gabarito_Final.pdf` (1 pág., "GABARITO FINAL", `X (questão anulada)`)

LP: 1A 2C 3B 4A 5B 6C 7X 8C 9A 10C 11A 12A 13D 14D 15A 16C 17A 18C 19A 20D
MAT: 21B 22B 23B 24A 25A 26X 27A 28C 29A 30B 31C 32D 33A 34A 35C 36C 37A 38A 39D 40C

### 2022 — `GABARITO_..._2022.pdf` (4 págs., FUNCERN, preliminar + definitivo)

Preliminar e definitivo transcritos integralmente e comparados
programaticamente: **40/40 idênticos, zero divergências, zero anuladas.**

LP: 1B 2A 3C 4B 5D 6C 7B 8A 9A 10B 11C 12A 13D 14C 15C 16A 17B 18A 19D 20B
MAT: 21C 22B 23D 24A 25D 26A 27B 28D 29C 30D 31D 32C 33B 34A 35B 36C 37C 38D 39C 40B

### 2023 — `Gabarito_Final_-_Exame_de_Selecao_2023.pdf` (3 págs., "Gabarito Oficial Definitivo")

Grade na pág. 3. Págs. 1–2 listam ofertas nº 1–83 (curso/campus/turno) —
não são respostas por questão; o parser só lê a seção após os marcadores
`LÍNGUA PORTUGUESA` / `MATEMÁTICA`, portanto as ofertas não contaminam a
vinculação.

LP: 1C 2A 3D 4B 5D 6A 7C 8C 9D 10B 11A 12C 13A 14B 15C 16D 17A 18C 19B 20C
MAT: 21X 22A 23B 24A 25C 26C 27A 28D 29B 30A 31D 32C 33C 34A 35C 36C 37D 38A 39B 40A

### 2024 — `Gabarito_Oficial_Definitivo_IFRN_78.pdf` (1 pág., "Gabarito Oficial Definitivo")

LP: 1D 2A 3C 4C 5A 6B 7C 8D 9A 10B 11B 12D 13A 14C 15A 16A 17X 18B 19C 20C
MAT: 21A 22C 23C 24A 25C 26B 27A 28D 29D 30C 31D 32A 33B 34B 35D 36C 37C 38A 39A 40A

### 2025 — `Exame_de_Seleção_2024_-_Gabarito_Final.pdf` (1 pág., "Gabarito Oficial Definitivo")

LP: 1C 2B 3A 4D 5B 6D 7A 8B 9B 10C 11D 12C 13D 14A 15A 16D 17C 18A 19C 20B
MAT: 21D 22C 23B 24C 25D 26A 27C 28B 29A 30C 31C 32A 33D 34A 35C 36A 37B 38A 39B 40D

### 2026 — `GABARITO_EXAME_DE_SELEÇÃO_2026_-_DEFINITIVO.pdf` (1 pág., data interna 04/11/2025, `X = QUESTÃO ANULADA`)

Tabela de duas colunas (`1 A … 21 D` por linha), sem rótulos disciplinares:

LP: 1A 2D 3A 4C 5D 6A 7B 8A 9B 10D 11B 12A 13B 14A 15D 16B 17D 18C 19B 20A
MAT: 21D 22B 23A 24A 25B 26D 27C 28C 29D 30B 31C 32C 33A 34C 35D 36A 37X 38A 39C 40B

## 2. Detecções exigidas pela TASK

### 2.1 Respostas ausentes

**Nenhuma.** Todas as 240 questões (40 por edição × 6 edições) encontraram
resposta no respectivo gabarito (`n_missing_answer = 0` em todas as edições).
Nenhum valor fora de `A/B/C/D/X` foi encontrado.

### 2.2 Questões anuladas

5 no total, características de cada edição (nunca regra geral):

| Edição | Anuladas | Evidência literal no gabarito |
|---|---|---|
| 2020 | Q7 (LP), Q26 (MAT) | `Observação: X (questão anulada)` |
| 2022 | — (zero) | preliminar e definitivo sem `X` |
| 2023 | Q21 (MAT) | `X` na grade, pág. 3 |
| 2024 | Q17 (LP) | `X` na grade |
| 2025 | — (zero) | grade 40× `A–D`, sem `X` |
| 2026 | Q37 (MAT) | `X = QUESTÃO ANULADA` |

### 2.3 Divergência preliminar × definitivo

Só a edição 2022 possui os dois gabaritos no repo (págs. 1–2 preliminar,
págs. 2–4 definitivo, via FUNCERN). Comparação automatizada questão a
questão: **idênticos 40/40, `preliminary_vs_definitive_diff = []`.**
Demais edições: só o (definitivo/final) está no repo — divergência
**NÃO CONFIRMADA** (não afirmar que não houve retificação fora do repo;
para 2022, ver pendência §4 sobre retificações posteriores).

### 2.4 Inconsistências e observações (não bloqueiam a vinculação)

1. **2025 — nome × conteúdo:** arquivo
   `Exame_de_Seleção_2024_-_Gabarito_Final.pdf` cujo conteúdo declara
   `...Forma Integrada - 2025` / `Gabarito Oficial Definitivo` com 40
   respostas. A vinculação usa o conteúdo, não o nome; o mapeamento
   canônico (renomear vs. preservar nome original) fica como pendência de
   curadoria — o script registra `gabarito_document` literal para
   rastreabilidade.
2. **2023 — ofertas nº 1–83:** págs. 1–2 listam ofertas curso/campus/turno
   (inclui salto `59 → 61`, i.e. sem nº 60 na extração — transcrito como
   observado, sem inferir motivo). Não confundir com questões; parser
   restrito às seções LP/MAT da pág. 3.
3. **2022/2026 — sem rótulo disciplinar no gabarito:** a atribuição
   LP 1–20 / MAT 21–40 vem do caderno (TASK 1.2, `discipline_source`).
   Em `data/linked/` a disciplina é herdada da extração, com a fonte
   preservada por questão.
4. **2026 — layout de 2 colunas + mojibake no cabeçalho:** extração
   `pdftotext` mostra `TÉCNICOS`/`NÍVEL MÉDIO`/`QUESTÃO` com escapes
   (`TM-CM-^ICNICOS` etc.) — artefato de codificação do cabeçalho, sem
   impacto nas 40 linhas `NN L` (parseadas por regex posicional, 40/40
   recuperadas).
5. **2020 Q38 — frações/figuras:** a extração TASK 1.2 emitiu avisos de
   "alternativa sem texto na mesma linha", mas o texto das 4 alternativas
   foi recuperado das linhas seguintes (`A: 12 5`, `B: 12 2`, … — ordem de
   leitura do `pdftotext`, provavelmente frações verticais achatadas).
   Resposta oficial Q38 = A, vinculada como CONFIRMED; o **conteúdo**
   (fração/figura) **NECESSITA REVISÃO visual** antes de qualquer
   publicação — registrado aqui, não silenciado.
6. **Pontuação da capa (2020/2023):** pendência herdada da TASK 1.1
   (discursiva 2020 truncada; tabela de pontos 2023 ilegível na extração)
   — fora do escopo da vinculação objetiva, sem impacto nas letras.

## 3. Proveniência por questão (como auditar)

Cada objeto em `data/linked/<edicao>.json → questions[]` contém:

* `edition`, `number`;
* `discipline` + `discipline_source` (herdados de `data/extracted/`);
* `source_document` da questão + `question_page_start/end`;
* `answer_key` (`A`–`D`, `X`, ou `null` se ausente);
* `annulled` (bool), `status`
  (`CONFIRMED` / `ANNULLED` / `MISSING_ANSWER` / `MISSING_QUESTION` /
  `NEEDS_REVIEW`), `validation` (nota textual);
* metadados do gabarito: `gabarito_document` + `gabarito_sha256` +
  `gabarito_pages` + `gabarito_kind`;
* para 2022: `preliminary{}`, `definitive{}` e
  `preliminary_vs_definitive_diff[]` na raiz do JSON.

Reprodução:

```bash
python3 scripts/analysis/link_answer_keys.py        # (re)gera data/linked/
python3 scripts/analysis/link_answer_keys.py --check  # valida 6×40 vínculos
python3 scripts/analysis/extract_questions.py --check # valida TASK 1.2
```

## 4. Pendências para curadoria (não bloquear TASK 1.3)

* [ ] Conferência visual amostral das grades (2020/2023/2024/2025) no PDF
  renderizado — a transcrição regex foi validada por contagem (20+20 por
  seção, 40/40 letras), mas uma leitura humana da imagem elimina risco
  residual de desalinhamento de coluna.
* [ ] 2020 Q38 e demais figuras/fórmulas/gráficos: registrar página +
  alternativa textual/imagem antes da TASK 2.3 (importação ao banco).
* [ ] Decisão de curadoria: renomear o gabarito 2025 para nome canônico
  (preservando o original em metadados) ou manter nome + documentar.
* [ ] 2022: verificar fora do repo se houve retificação posterior ao
  definitivo FUNCERN de 30/09/2022 — NÃO CONFIRMADO.
* [ ] Regra de negócio para anuladas (pontuação/bônus) — DESCONHECIDA nos
  documentos analisados; não assumir Sporting; definir na Fase 4/5 com
  fonte explícita.
* [ ] Nenhum enunciado/alternativa foi copiado para este documento além
  das letras-resposta por questão; os textos integrais permanecem em
  `data/extracted/` (auditoria) e nos PDFs-fonte.
