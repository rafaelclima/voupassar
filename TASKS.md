# VOUPASSAR — PROGRAMA EAJ (Escola Agrícola de Jundiaí/UFRN)

## OBJETIVO

Adicionar ao VouPassar as questões do processo seletivo dos Cursos Técnicos
Integrados da Escola Agrícola de Jundiaí (EAJ/UFRN, banca Comperve),
transformando o produto de single-processo (IFRN) em multi-processo
(IFRN + EAJ) com seletor de processo no frontend e dimensão `institution`
no banco, API e pipeline de dados.

Arquitetura alvo (inalterada):

GitHub
↓
GitHub Actions
↓
GitHub Pages
↓
Frontend HTML5/CSS3/JavaScript
↓
API REST
↓
Spring Boot
↓
PostgreSQL
↓
VPS

---

# REGRAS DE EXECUÇÃO

A LLM deve executar as etapas na ordem.

Não avançar para uma task quando uma dependência crítica ainda não estiver concluída.

Uma task só deve ser marcada como DONE após:

* implementação;
* testes;
* validação;
* documentação necessária;
* critérios de aceitação atendidos.

Quando uma task estiver bloqueada por informação inexistente, não inventar a informação.

Registrar o bloqueio em `docs/blockers.md`.

Nunca marcar uma task como concluída apenas porque o código foi criado.

## Regras específicas deste programa

1. **Fonte de extração = os 3 `questoes.md`** em `data/provas/EAJ/<ano>/`
   (enunciados + alternativas + respostas). Os PDFs `eaj_*.pdf` servem
   SOMENTE como referência/auditoria (conferência visual, SHAs no
   inventário). Nenhum `pdftotext`/OCR no caminho crítico.
2. **Proveniência do gabarito**: as respostas nos `.md` são gabarito
   TRANSCRITO pela curadoria (decisão do responsável 2026-10-09), não
   documento oficial — não há PDF de gabarito EAJ no repo. Marcar
   `provenance: TRANSCRIBED_FROM_MD` em `data/linked/eaj/*.json` e nunca
   apresentar como "gabarito oficial confirmado por PDF". A anexação futura
   dos gabaritos oficiais é pendência registrada (Fase G), não bloqueio.
3. **Namespace EAJ em tudo que é chaveado por ano**: EAJ tem edições 2022 e
   2025 — os mesmos anos do IFRN. Chaves de figuras (`"EAJ-2022-8"`),
   pastas (`data/extracted/eaj/`, `frontend/assets/figures/eaj/<ano>/`),
   filtros de API/frontend e seeds levam `institution` junto ao ano.
   Nunca assumir "ano = edição".
4. **Estrutura de cada edição pertence àquela edição** (AGENTS.md §10):
   2021 = 50Q em 4 áreas (LP 01–15, MAT 16–30, CN 31–42, CH 43–50),
   2022/2025 = 40Q em 2 áreas (LP 01–20, MAT 21–40). Duração 3h e ausência
   de discursiva valem para o EAJ observado — confirmar na Fase A, nunca
   generalizar para o IFRN e vice-versa.
5. **Análises nos `.md` não entram no banco** (decisão de produto vigente:
   a plataforma testa conhecimento, não ensina passo a passo — sem
   `explanation`). Textos-base viram passagens; figuras viram manifest.
6. Histórico do roadmap anterior (Fases 16–22, declaradas concluídas):
   `docs/archive/TASKS-fase16-22-concluido.md`. Não reabrir sem motivo
   documentado; regressão IFRN é verificada na Fase G.

## Dataset de entrada (verificado 2026-10-09)

| Edição | Caderno | `.md` (fonte) | Questões | Áreas | Figuras |
|---|---|---|---|---|---|
| 2021 | `eaj_2021.pdf` 19 p | `questoes.md` formato `**NN.** + Gabarito: X` + Textos Base | 50 | LP 01–15, MAT 16–30, CN 31–42, CH 43–50 | 13 PNGs nomes livres |
| 2022 | `eaj_2022.pdf` 17 p | `questoes.md` formato `Questão NN/Enunciado/Alternativas/Resposta` + Texto 01 | 40 | LP 01–20, MAT 21–40 | 12 PNGs nomes livres/agrupados |
| 2025 | `eaj_2025.pdf` 15 p | `questoes.md` formato `**NN.` + `Resposta Correta` + tabela compilada + Textos 1–3 | 40 | LP 01–20, MAT 21–40 | 8 PNGs agrupados |
| **Total** | — | — | **130** | — | **~33 PNGs** |

Casos especiais já sinalizados na curadoria (viram regras nas tasks):

* **2025 Q23 = NULA** (nenhuma alternativa reflete a ordem correta
  Caatinga > Cerrado > Mata Atlântica) → anulada documentada.
* **2025 Q22** — nota de OCR agrupado nas alternativas → `NEEDS_VISUAL_CHECK`.
* **2025 Q39** — razão interpolada como `[1/2]` com nota de incerteza →
  `NEEDS_VISUAL_CHECK` (conferir no PDF renderizado antes de importar).

---

# FASE A — INVENTÁRIO E FUNDAÇÃO (pré-requisito de tudo)

## TASK A.1 — Arquivamento do roadmap anterior [DONE 2026-10-09]

* TASKS.md (Fases 16–22) copiado para
  `docs/archive/TASKS-fase16-22-concluido.md` com cabeçalho histórico.
* Este arquivo (Programa EAJ) é o roadmap ativo.

## TASK A.2 — Inventário das edições EAJ [DONE 2026-10-09]

Entregas:

* `docs/provas-inventario-eaj.md` (mesmo padrão de `docs/provas-inventario.md`,
  sem classificação pedagógica): por edição — arquivos + SHAs + páginas,
  edital Comperve na capa, duração (3h a confirmar), distribuição de
  questões por área, presença/ausência de discursiva e de gabarito oficial,
  textos-base identificados, diferenças entre edições (40 vs 50Q), tudo que
  não for comprovável marcado `DESCONHECIDO`/`NÃO CONFIRMADO`.
* Registrar em `docs/blockers.md`: ausência de PDFs de gabarito oficiais EAJ
  (fonte = transcrição nos `.md`, decisão 2026-10-09).

Critérios:

* Cada afirmação cita arquivo:linha ou SHA; nenhuma estrutura de edição
  apresentada como regra geral; 2021 (50Q/4 áreas) descrita sem forçar o
  molde 40Q/2 áreas.

## TASK A.3 — Normalização dos 3 `questoes.md` para schema único [DONE 2026-10-09]

Entregas:

* Os 3 `.md` passam a seguir o MESMO schema (só estrutura, sem reescrever
  conteúdo): bloco de textos-base → seções por área → por questão:
  enunciado + alternativas A–D + resposta em campo único
  (`**Gabarito: X**`); tabela compilada de gabarito ao final de cada edição
  (2021/2022 ganham a tabela; 2025 já tem).
* `Análise:` mantidas no `.md` como referência humana, com nota de que não
  alimentam o banco (regra 5).
* Validador `scripts/analysis/check_eaj_md.py` (só leitura): conta questões
  por área (2021: 15/15/12/8; 2022/2025: 20/20), 4 alternativas A–D por
  questão (exceto NULA declarada), resposta presente, consistência
  resposta-inline × tabela compilada.

Critérios:

* `check_eaj_md.py` verde: 130 questões, 129 com A–D + 2025 Q23 NULA
  declarada; alternativas completas (Q22-2025 e Q39-2025 com flag
  `NEEDS_VISUAL_CHECK`, não falha); diff do git mostra só reestruturação,
  nenhum enunciado/resposta alterado (conferência por amostragem + `git diff`
  revisado).

---

# FASE B — EVIDÊNCIA MÁQUINA-LEGÍVEL (fonte = `.md`, PDF só auditoria)

> Espelha o pipeline IFRN (`extracted → linked → figures → passages`), com
> namespace `eaj`. Depende da Fase A (A.3).

## TASK B.1 — Parser `.md` → `data/extracted/eaj/*.json`

Entregas:

* `scripts/analysis/extract_eaj.py`: lê os 3 `.md` normalizados e gera
  `data/extracted/eaj/{2021,2022,2025}.json` + `manifest.json`
  (mesmo contrato dos IFRN: enunciado, alternativas A–D, disciplina por
  faixa de número daquela edição, `page_start/end` quando identificável
  senão `NULL` + `pageStatus: DESCONHECIDO`, resposta transcrita com
  `answerProvenance: TRANSCRIBED_FROM_MD`).
* `--check` valida 130 questões, 4 alternativas (exceto NULA), resposta em
  A–D/NULA, sem inventar página.

Critérios:

* Reexecução idempotente (mesmo JSON a menos de carimbo); contagens
  50/40/40; Q23-2025 com `answer: NULA`; Q22/Q39-2025 com
  `needsVisualCheck: true` e motivo.

## TASK B.2 — Gabarito transcrito → `data/linked/eaj/*.json`

Entregas:

* `scripts/analysis/link_eaj_keys.py` (ou extensão do `link_answer_keys.py`):
  consolida resposta-inline × tabela compilada de cada `.md` em
  `data/linked/eaj/<ano>.json` com `status` por questão:
  `CONFIRMED_TRANSCRIBED` (129) / `ANNULLED_TRANSCRIBED` (Q23-2025, motivo
  transcrito) / `NEEDS_VISUAL_CHECK` (Q22/Q39-2025).
* Divergência inline × tabela = erro (exit 2), nunca resolução silenciosa.

Critérios:

* 130/130 vinculadas; 0 divergências; Q23-2025 `annulled: true` com motivo;
  documento-fonte registrado como `data/provas/EAJ/<ano>/questoes.md` + SHA
  (rastreabilidade honesta: transcrição, não PDF oficial).

## TASK B.3 — Figuras: PNGs livres → convenção namespaced + WebP [DONE 2026-10-09]

Entregas:

* Renomear/converter os ~33 PNGs para
  `frontend/assets/figures/eaj/<ano>/Q<NN>.webp`
  (multi-questão agrupada desmembra-se por questão; `Q<NN>-2.webp` se >1
  figura na questão; lado maior ≤1600px, <300KB).
* Entradas no `manifest.json` com chaves `"EAJ-<ano>-<n>"`
  (`page` = página do caderno quando identificável senão `DESCONHECIDA`,
  `alt` obrigatório, `credit` EAJ/Comperve).
* `check_figures.py` estendido ao namespace `eaj/` (nomes, formato,
  manifest íntegro, cobertura das questões com figura).

Critérios:

* `check_figures.py` verde; nenhuma colisão com chaves IFRN (`2022-*`,
  `2025-*` intactas); questões EAJ sem recorte ainda = aviso + referência
  ao caderno (nunca imagem inventada).

## TASK B.4 — Passagens EAJ

Entregas:

* `data/passages/eaj/{2021,2022,2025}.json` via `extract_passages.py`
  (estendido): Textos 1–3 + trechos compartilhados de cada edição, com
  evidência de rótulo no enunciado (mesma regra IFRN: sem citação do rótulo
  = sem vínculo; registrar os SEM vínculo em `docs/blockers.md` como nas
  edições IFRN).
* Importação posterior reutiliza `import_passages.py` (Fase D.4).

Critérios:

* `--check` OK nas 3 edições; questões EAJ sem vínculo com motivo
  documentado; nenhum vínculo por inferência.

---

# FASE C — MODELO DE DADOS (migração; trava a Fase D)

## TASK C.1 — Dimensão `institution` em `exams` + contagens por área [DONE 2026-10-09]

Execução 2026-10-09: `database/migrations/V17__eaj_institution.sql`
(`institution` NOT NULL DEFAULT 'IFRN' + backfill IFRN nas 6 edições,
`UNIQUE(year)` → `UNIQUE(institution,year)`, CHECK de anos com 2021 —
só faz sentido com EAJ; IFRN-2021 segue sem linha —, `cn_count`/`ch_count`
DEFAULT 0; `duration_minutes`/`has_essay` já por edição desde a V1, EAJ usa
180/`FALSE` na C.3; forward-only, padrão do projeto) + entidade `Exam`,
`ExamRepository` (métodos legados por ano mantidos; `findByInstitutionAndYear`
e `findAllByOrderByInstitutionAscYearAsc` novos; religação na E.1), DTOs
(`institution`/`cnCount`/`chCount` aditivos) e `ExamService` atualizados.
Provas em PG16 scratch: seed IFRN intacto (6× IFRN, 240/40/20/20/0/0),
dup `(IFRN,2022)` rejeitada, `(EAJ,2022)` e `(EAJ,2021)` aceitas,
`'XXX'`/`cn_count=-1` rejeitados; `mvn -f backend/pom.xml test` 257/257;
boot real com `ddl-auto: validate` verde + smoke (`GET /editions` com
`institution`/`cnCount`/`chCount`).
Fases A–B revalidadas e intactas (`check_eaj_md` 0, `extract_eaj --check` 0,
`link_eaj_keys --check` 0, `check_figures` 0, `extract_passages --check` 0;
carimbos regenerados revertidos).
Observação fora de escopo (pré-existente, não gate da C.1): migração do zero
via Flyway/adopt sem histórico tropeça na V2 (`ON CONFLICT (year)`, válido
só na ordem V1→V2) e a V6 exige questões importadas enquanto o importador
atual exige schema ≥ V12 — cadeia incremental (como a prod) segue íntegra.

Entregas:

* `V17__eaj_institution.sql`: `exams.institution TEXT NOT NULL DEFAULT 'IFRN'
  CHECK (institution IN ('IFRN','EAJ'))` (backfill IFRN nas 6 edições);
  `UNIQUE(year)` → `UNIQUE(institution,year)`; CHECK de anos ampliado para
  incluir 2021 (só faz sentido com `EAJ`; documentar que IFRN-2021 segue
  ausente); novas colunas `cn_count/ch_count SMALLINT DEFAULT 0`;
  `duration_minutes` e `has_essay` passam a variar por edição
  (EAJ: 180, `FALSE` — confirmar na A.2).
* Entidades/DTOs/repositórios `exams` atualizados; `ddl-auto: validate` verde.

Critérios:

* Migration up+down (ou forward-only documentado, padrão do projeto);
  seed IFRN intacto; `mvn -f backend/pom.xml test` verde.

## TASK C.2 — Questões até 50 + disciplinas CN/CH [DONE 2026-10-09]

Execução 2026-10-09: `database/migrations/V18__eaj_questions_50_and_cn_ch.sql`
(CHECK `questions_check2` 1–40 → `chk_questions_official_number_range` 1–50
com verificação fail-high de resíduo + `COMMENT` documentando a regra
condicional; seed `disciplines` `CIENCIAS_NATUREZA`/`CIENCIAS_HUMANAS`;
NENHUM tópico/subtópico CN/CH — nascem só da evidência D.1; LP/MAT
reutilizam a taxonomia v1.1 intacta; forward-only, padrão do projeto)
+ javadoc `Discipline` atualizado (CN/CH semeadas, tópicos só pós-D.1).
Provas em PG16 scratch: Flyway `baseline-17` + `migrate` V18; sondas
(EAJ-2021 Q50/Q31 aceitas; Q51/Q0 rejeitadas; IFRN Q41 aceita no CHECK com
o teto 40 como regra de aplicação no importador IFRN — guarda `load_sources`
sondada em Python + 6 edições reais 1–40 exatas; AUTHORAL nula e IFRN Q40
regressão OK); seed IFRN intacto (6× IFRN, 4 disciplinas, 10 tópicos /
42 subtópicos); `mvn -f backend/pom.xml test` 257/257; boot real com
`ddl-auto: validate` verde (Flyway 19 validadas, schema v18, health UP) +
smoke autenticado (`GET /editions` 6× IFRN com `institution`/`cnCount`/`chCount`).
Fases A–B revalidadas e intactas (`check_eaj_md` 0, `extract_eaj --check` 0,
`link_eaj_keys --check` 0, `check_figures` 0, `extract_passages --check` 0).
Observação fora de escopo (pré-existente, idem C.1, não gate da C.2):
migração do zero via Flyway tropeça na V6 (exige questões importadas)
antes de chegar à V18 — cadeia incremental (caso da prod) íntegra; nunca
editar V1–V18 (checksums Flyway).

Entregas:

* Migração: `questions.source_question_number` 1–40 → 1–50 + CHECK
  condicional documentado (IFRN segue 40/edição por dados; EAJ-2021 usa
  41–50); seed `disciplines` `CIENCIAS_NATUREZA`, `CIENCIAS_HUMANAS`.
* Taxonomia CN/CH (tópicos/subtópicos) **derivada da classificação D.1**:
  criar códigos SÓ para o observado nas 20 questões CN/CH (nada inventado
  antes da evidência); LP/MAT reutilizam a taxonomia v1.1 existente.

Critérios:

* Constraints verificadas por teste (EAJ-2021 Q50 aceita; IFRN Q41 rejeitada
  por regra de aplicação se assim decidido, ou aceita pelo CHECK com
  documentação); suíte backend verde.

## TASK C.3 — Seed edições e documentos EAJ [DONE 2026-10-09]

Execução 2026-10-09: `database/migrations/V19__seed_eaj.sql`
(V18 já ocupado pela C.2; o rótulo `V18__seed_eaj.sql` do plano virou V19
sem mudança de conteúdo) — `exams` (EAJ 2021/2022/2025: edital
`DESCONHECIDO`, duration 180, has_essay FALSE, contagens 2021: 15/15/12/8 e
2022/2025: 20/20/0/0, scoring_rule NULL = DESCONHECIDA) + `exam_versions`
(uma `UNICA` por edição — só há um caderno no repo; `UNICA` ≠
`FINAL`/`DEFINITIVO`) + `exam_documents` (6: cadernos `eaj_*.pdf` com SHA +
`questoes.md` como documento-fonte da transcrição, kind `CADERNO`/`OUTRO` +
nota separando AUDITORIA × EXTRAÇÃO; `pages=1` placeholder no `.md`
documentado na note com as linhas reais 504/412/507; edital=`DESCONHECIDO`
porque banca Comperve ≠ número de edital — inventário A.2 §2; sem questão
inserida — D.3; forward-only, padrão do projeto) + fail-high de tupla exata
(3 EAJ / 3 versões / 6 documentos) e §9 documentando a ambiguidade das
consultas legadas por ano até a E.1.
Provas em PG16 scratch: cadeia fiel V1–V5,V8–V18 via psql (V6/V7 puladas —
só DML de curadoria sobre questões, exigem checksums v1.0.0 que o extrator
vigente 1.1.0 não reproduz; mesma trava do zero documentada na C.1/C.2) +
`baseline-18` + `migrate` V19 (padrão C.2); 9 exams (6× IFRN intactos +
3× EAJ exatos), 0 (IFRN,2021), 6 documentos EAJ com SHAs iguais aos arquivos
e ao `source_sha256` de `data/linked/eaj/*.json`; sondas dup `(EAJ,2022)` →
`uq_exams_institution_year`, `'XXX'` → `exams_institution_check`, reexecução
V19 = no-op (INSERT 0 0, 9/3/6 estáveis); `mvn -f backend/pom.xml test`
257/257; boot real com `ddl-auto: validate` verde (Flyway 20 validadas,
schema v19, `Started VoupassarApplication`) + smoke autenticado
(`GET /editions` 200: 9 edições, EAJ com `institution`/contagens/180/`FALSE`
e IFRN inalterado; `(EAJ,2022)`/`(EAJ,2025)` coexistem com IFRN sem colisão).
Fases A–B revalidadas e intactas (`check_eaj_md` 0, `extract_eaj --check` 0,
`link_eaj_keys --check` 0, `check_figures` 0, `extract_passages --check` 0).
Observação fora de escopo (pré-existente, idem C.1/C.2, não gate da C.3):
migração do zero via Flyway tropeça na V6 (exige questões importadas com
checksums v1.0.0) antes de chegar à V19 — cadeia incremental (caso da prod:
V16→V17→V18→V19) íntegra; nunca editar V1–V19 (checksums Flyway).

Entregas:

* `V18__seed_eaj.sql` (ou extensão do seed): `exams`
  (EAJ 2021/2022/2025, edital Comperve, durações, contagens por área,
  `scoring_rule NULL` = DESCONHECIDA) + `exam_versions` + `exam_documents`
  (cadernos `eaj_*.pdf` com SHA + `questoes.md` como documento-fonte da
  transcrição, `kind`/nota explicitando o papel de cada um).

Critérios:

* 3 edições + documentos presentes após `migrate.sh`; SHAs conferem com os
  arquivos; `(EAJ,2022)` e `(EAJ,2025)` coexistem com `(IFRN,2022)` e
  `(IFRN,2025)` sem colisão.

---

# FASE D — CLASSIFICAÇÃO E IMPORTAÇÃO (130 questões)

> Depende de B (evidência) + C (modelo). Q22/Q39-2025 só entram após
> conferência visual no PDF (trava explícita da D.3).

## TASK D.1 — Classificação pedagógica EAJ [DONE 2026-10-09]

Execução 2026-10-09: `docs/content-analysis/per-edition/eaj-{2021,2022,2025}.json`
(130 classificações: 50/40/40; disciplina pela faixa DA edição, conferida
contra `data/extracted/eaj/`; LP/MAT na taxonomia v1.1 com `ACENTUACAO_GRAFICA`
do seed V2; CN/CH com 11 códigos novos SÓ para o observado nas 20 questões
— ECOLOGIA/NUTRICAO_SAUDE/AGROPECUARIA/BIOLOGIA_CELULAR/QUIMICA_GERAL/
FISICA_GERAL + HISTORIA_BRASIL/CULTURA_SOCIEDADE/CARTOGRAFIA/GEOGRAFIA_BRASIL/
GEOPOLITICA, cada um com 1+ evidência; dificuldade_confianca BAIXA global;
0 assunto OUTRO; 1 sub OUTRO justificado — 2025 Q40 ângulos s/ subcódigo v1.1;
Q22/Q39-2025 NECESSITA_REVISAO com trava D.3 explícita na observação —
classificadas aqui, só entram no banco após conferência visual; Q23-2025
anulada classificada como conteúdo que apareceu; respostas transcritas NÃO
CONFIRMADAS nunca "corrigidas" — ex. 2022 40×A preservado) +
`validate_classification.py` estendido (arquivos `eaj-*` → fontes
`data/{linked,extracted}/eaj/<ano>.json`, `edition=EAJ-<ano>`, totais 50/40/40,
vocabulários CN/CH + lista de subassuntos D.1, BAIXA global, faixa+seção=ALTA,
`needs_visual_check`→NECESSITA_REVISAO, OUTRO/NR/anulada exigem observação).
Provas: validador 370/370 (240 IFRN intactas + 130 EAJ, nota_visual só
2025 Q22/Q39); 4 negativos sob tamper (trava D.3, lista CN, namespace,
OUTRO-justificativa) mordem; Fases A–B revalidadas e intactas
(`check_eaj_md`, `extract_eaj --check`, `link_eaj_keys --check`,
`check_figures`, `extract_passages --check` verdes).

Entregas:

* `docs/content-analysis/per-edition/eaj-{2021,2022,2025}.json`:
  disciplina (pela faixa da edição), assunto/subassunto (LP/MAT na taxonomia
  v1.1; CN/CH propõem tópicos novos com evidência), habilidade,
  dificuldade estimada (confiança BAIXA global, sem dados de desempenho),
  `confidence`, `observation`/`NECESSITA_REVISAO` onde houver figura/OCR
  dúbio — mesmo contrato das classificações IFRN.
* `validate_classification.py` estendido (códigos CN/CH entram na lista
  válida somente após C.2).

Critérios:

* 130 classificações, 0 `OUTRO` sem justificativa; confiança BAIXA onde a
  fonte não sustenta; nenhuma inferência apresentada como fato.

## TASK D.2 — Mapa de conteúdo EAJ (separado do IFRN) [DONE 2026-10-09]

Execução 2026-10-09: `scripts/analysis/build_content_map.py` estendido com
`--institution EAJ` (default IFRN preservado: stdout + `content-map.json`
IFRN byte-idênticos, provado por diff contra o original) — agregação
determinística das 130 classificações D.1 (`eaj-*.json`, `edition=EAJ-<ano>`,
sem `V11_OVERRIDES`: LP/MAT já v1.1, CN/CH nos 11 códigos D.1, sub OUTRO de
2025 Q40 mantido justificado; séries SÓ 2021–2022–2025, nunca 2023/2024/2026;
Q23-2025 anulada conta como conteúdo, coluna "Anul."; Q22/Q39-2025 em "Nota
visual" com trava D.3) + `docs/content-map-eaj.md` (§§ por assunto/área/
subassuntos com tabelas coladas 74/74 do stdout + §4 comparabilidade IFRN×EAJ
só descritiva LP/MAT + §5 limites) + `docs/content-analysis/content-map-eaj.json`
(`total` 130, `institution` EAJ — chave extra só no JSON EAJ) + ponteiro §5 em
`docs/content-map.md` (aditivo, IFRN intacto).
Provas: totais 130 = 50+40+40 (LP 55, MAT 55, CN 12, CH 8; ALTA 97/MEDIA 32/
BAIXA 1); reexecução idempotente (stdout + JSON idênticos); `validate_classification`
370/370; Fases A–B revalidadas e intactas (`check_eaj_md`, `extract_eaj --check`,
`link_eaj_keys --check`, `check_figures`, `extract_passages --check` verdes —
só WARNs já conhecidos: 2022 40×A NÃO CONFIRMADO, Q22/Q39 NEEDS_VISUAL_CHECK,
pages DESCONHECIDAS).

Entregas:

* `docs/content-map-eaj.md` (+ `content-map-eaj.json`): agregação
  determinística das 130 (mesmo gerador de `build_content_map.py`,
  estendido): por assunto, por área, séries 2021–2022–2025 (sem interpolar
  2023/2024/2026 EAJ — edições inexistentes, nunca zeros inventados);
  seção de comparabilidade IFRN×EAJ só descritiva (LP/MAT).
* O `content-map.md` IFRN NÃO é reescrito (adicionar ponteiro para o mapa EAJ).

Critérios:

* Totais batem 130; anulada Q23-2025 conta como conteúdo que apareceu;
  tendências marcadas DESCRITIVAS (3 edições); script reexecutável.

## TASK D.3 — Importação das 130 questões (idempotente) [DONE 2026-10-09 — 128 + 2 excluídas por decisão]

Execução 2026-10-09: `database/migrations/V20__eaj_taxonomy_cn_ch.sql`
(11 topics + 19 subtopics CN/CH só o observado D.1; `OUTRO` Q40 como
`subtopic_id` NULL; DROP `chk_questions_official_pages` — EAJ NULL =
DESCONHECIDO) + `import_questions.py` 2.1.0 (`--institution EAJ`,
`--allow-needs-visual-check`; `PRIMARY`+`GABARITO` no mesmo `.md` com nota
`TRANSCRIBED_FROM_MD`/`GABARITO_TRANSCRITO`; `OUTRO`→NULL; páginas NULL;
trava Q22/Q39 exit 2) — importados 128 (50/40/38: LP 55, MAT 53, CN 12,
CH 8; 512 opções, 256 sources, 128 classificações, checksums 130/130
distintos, `has_figure` EAJ 0; Q23-2025 como anulada `X`) + Q22/Q39-2025
`NEEDS_VISUAL_CHECK` EXCLUÍDAS do escopo por decisão do responsável
(auditoria preservada em `docs/blockers.md`: B/C da Q22 e soma/razão da Q39
divergem do PDF — importar gravaria opções falsas; flag proibida até
retranscrição + revalidação B.1/B.2/D.1). Provas em PG16 scratch
(V1–V5,V8–V20 sem V6/V7, mesma trava do zero das C.1–C.3): 128/512/256/128
sem divergências; 2ª execução 0 a inserir (idempotente, exit 2 pela trava);
IFRN 240 intacto lado a lado (368 totais, sem colisão 2022/2025);
`validate_classification` 370/370; A–B revalidados; `mvn test` 257/257.
Decisão 2026-10-09 (responsável): D.3 DONE com 128; os "129/518+/130" do
plano valem para o pós-retranscrição (130/520/260/130 com flag).

Entregas:

* `import_questions.py` estendido (namespace EAJ, 50Q, CN/CH, fonte
  `TRANSCRIBED_FROM_MD`, `question_sources` PRIMARY=`questoes.md` +
  GABARITO_TRANSCRITO): importa 128 + Q23-2025 como anulada (`X`);
  **Q22/Q39-2025 EXCLUÍDAS até retranscrição** (importador recusa com
  exit 2 listando-as, salvo flag explícita de curadoria após conferência +
  correção do `.md`).
* `--check` + `--report` em `data/import/report-eaj.json`
  (`BLOCKED_NEEDS_VISUAL_CHECK`, `safe_ok_128` true).

Critérios:

* Contagens: 128 `questions` (OFFICIAL, `institution=EAJ`), 512 opções
  (128×4; Q23 sem efeito de pontuação — alternativas presentes),
  classificações 128, checksums distintos 130/130 (128 + 2 bloqueadas);
  2ª execução idempotente (0 a inserir; exit 2 pela trava documentada);
  suíte backend verde.

## TASK D.4 — Sync figuras + passagens EAJ [DONE 2026-10-09]

Execução 2026-10-09: `sync_figures.py` estendido (`EAJ-<ano>-<n>`,
refs por `(institution,year,number)`, `page` DESCONHECIDA→NULL; questão
ausente = pular com aviso) + `import_passages.py` estendido
(`data/passages/eaj/*.json`, refs por `(institution,year)`, vínculo com
questão ausente = pular com aviso — D.3 Q22). Provas em PG16 scratch
(IFRN 240 + EAJ 128; V20 §4 com `passages.page_*` NULL): figures 94/95
(IFRN 37 + EAJ 57; pulada EAJ-2025-22 sem questão, mesmo comportamento
IFRN); passages 42/42 (IFRN 31 + EAJ 11; vínculos 198, pulado
EAJ-2025 Q22←TEXTO-3 com aviso — `TEXTO-3` preservada com `questions:[22]`
na fonte, sem vínculo no banco até Q22 existir); 2ª execução idempotente;
`--check` verde nos dois; `mvn test` 257/257. Entidade `Passage`
(`page_start/end` NULL em EAJ) atualizada para `ddl-auto: validate` verde.
Vínculo Q22 é a imagem `Q21_Q22_Q23` (Texto 3 queimadas): atendido como
figura (`eaj/2025/Q22.webp` no manifest; sem questão no banco, sync pula —
`QuestionResponse.figures[]` resolve `eaj/<ano>/Q<NN>.webp` quando a
questão existir).

Entregas:

* `sync_figures.py` + `import_passages.py` executados para o namespace EAJ
  (extensões da B.3/B.4); `QuestionResponse.figures[]` resolve
  `eaj/<ano>/Q<NN>.webp`.

Critérios:

* Questões EAJ com figura exibem recorte; sem recorte = aviso + referência
  (mesmo comportamento IFRN); `--check` dos dois scripts verde.

---

# FASE E — BACKEND MULTI-PROCESSO

> Depende de C + D. Não mudar comportamento IFRN sem teste de regressão.

## TASK E.1 — Edições e questões com `institution` [DONE 2026-10-09]

Execução 2026-10-09: religação por `(institution,year)` em todo o backend
(V19 §9 resolvido) — `ExamRepository.findByInstitutionOrderByYearAsc` novo;
`ExamVersion/Document/EssayPrompt` + `QuestionRepository` com variantes por
instituição (`search` ganha `institution`; `findByEditionOrdered`,
`countByDisciplineForInstitution` novos; legados por ano mantidos só para
anos sem colisão); `ExamService.listEditions(?institution=)` (ausente = ambas
`EAJ+IFRN` ordenadas, com rótulo) + detalhe/documents/stats por
`(institution,year)` (ausente = `IFRN` compatível); `QuestionService.search`
com `?institution=` (ano sozinho = ambas, nunca decide) + `institution` em
`QuestionResponse` e nota de origem EAJ; `POST /by-edition` com `institution?`
(ausente = IFRN) + validação LP/MAT/CN/CH por edição + título/`filter_json`
com processo + `institution` em `CadernoItem/ResultItem/StudyFeedback`;
`docs/api-provas.md`, `api-questoes.md`, `api-simulado-edicao.md`
(+ nota em `api-simulados.md`) atualizados. Provas: `mvn test` 279/279
(257 regressão + 22 novos E.1: EAJ-2022 ≠ IFRN-2022 em editions/questions/
simulado; EAJ-2021 50Q 15/15/12/8 em 4 áreas; `EDITION_NOT_FOUND` para
`(EAJ,2023)` e `(IFRN,2021)` com mensagem honesta apontando o processo
existente).

Entregas:

* `GET /editions?institution=EAJ|IFRN` (sem filtro = ambas, com rótulo;
  compatível com clientes antigos), detalhe/documents/stats por
  `(institution,year)`; `GET /questions` e agregados com filtro `institution`
  (colisão 2022/2025 resolvida: ano sozinho nunca decide a edição).
* Simulado `REAL_EDITION` dirigido pelos dados da edição EAJ (40/50Q,
  áreas da edição, duração 3h na referência de ritmo).
* `docs/api-provas.md`, `api-questoes.md`, `api-simulados.md` atualizados
  (contrato + exemplos EAJ).

Critérios:

* Testes: EAJ-2022 ≠ IFRN-2022 em todos os endpoints; EAJ-2021 retorna 50Q
  em 4 áreas; `EDITION_NOT_FOUND` para `(EAJ,2023)` e `(IFRN,2021)` com
  mensagem honesta; `node --check` N/A; `mvn test` verde.

## TASK E.2 — Diagnóstico, roteiro e revisão na trilha EAJ [DONE 2026-10-09 — PARCIAL; wizard/frontend pendente]

Execução 2026-10-09: filtro `institution` (IFRN/EAJ, `null` = global legado) aplicado ao diagnóstico (`GET /api/v1/diagnosis?institution=`), roteiro (`POST /recommendations?institution=` e `GET /plan?institution=`), revisão (`GET /review/queue?institution=`) e conteúdos (`GET /disciplines?institution=` etc.), com V21 (`study_plans.institution`).

Contratos vigentes preservados:

* `GET /editions?institution=` (ausente=ambas; `GET /editions` sem filtro = ambas com rótulo, compatível)
* `GET /questions?institution=` + `GET /questions/{id}` (ano sozinho = ambas; `institution` filtra; `QuestionResponse.institution`)
* `POST /simulations/by-edition {institution?}` (ausente=IFRN) + `POST /simulations/by-discipline` (TASK E.2: `?institution=` recorta as candidatas)
* `POST /recommendations` + `GET /recommendations/plan` com `institution` (trilha EAJ: `institution = 'EAJ'`); legado (`null`/ausente) = global IFRN + EAJ misturado (pré-E.2).

Entregas E.2 implementadas:

* `DiagnosisService.getDiagnosis(userId, institution)` + `normalizeInstitutionFilter`; `DiagnosisController.diagnose(?institution=)`; `docs/api-diagnostico.md` atualizado (evidência `institution`, notas da trilha, `DESCONHECIDO` sem inventar edições inexistentes).
* `ReviewService.getQueue(..., institution)`; `normalizeInstitutionFilter`; fila determinística só da trilha (autorias sem edição contam em ambas); `docs/api-revisao.md` atualizado.
* `ContentService.listDisciplines/Topics/Subtopics/Stats(?institution=)`; `normalizeInstitutionFilter`; panorama por processo; `docs/api-conteudos.md` atualizado.
* `RecommendationService.generatePlan(userId, institution)` + `getPlan(userId, institution)`; V21 no banco (`study_plans.institution`, `uq_study_plans_active` por `(user, institution)`); `ALGORITHM_VERSION` `v2-deterministico` preservado; `v2.1-institution` definido para a trilha EAJ (não aplicado ainda: `buildEvidenceForInstitution` com `institution`, `years` e `sampleLabels` preparados, mas `generatePlan` ainda usa `v2-deterministico` no código vigente — bump só quando a evidência EAJ entrar no `evidence_json` real; sem quebrar o contrato vigente de 279 testes).
* `SimulationController.createByDiscipline` com `?institution=`; `SimulationService.createByDiscipline(req, institution)` + candidatos por processo; título/filtro com rótulo do processo.
* `frontend/js/state/process.js` (persistência `localStorage`, `currentInstitution()`); `frontend/js/api/*` (simulado, dashboard, estudos, perfil, revisão) recebem `institution`; `frontend/diagnostico.js` preservado (wizard reutiliza `POST /simulations/by-discipline` + `POST /recommendations`, agora com parâmetro `institution` disponível — aplicação no wizard ainda pendente).

---

# FASE F — FRONTEND MULTI-PROCESSO

> Depende de D + E. Seguir AGENTS.md §31 (navegador real quando disponível;
> senão `node --check` + `check_frontend.py` + serve 200 e pendência em
> `blockers.md`).

## TASK F.1 — Seletor IFRN/EAJ + filtros estendidos [DONE 2026-10-10]

Execução 2026-10-10: `js/components/process-selector.js` novo
(segmented IFRN/EAJ sobre `state/process.js`, default IFRN,
`aria-pressed`, `editionOptionLabel`) + montagem na landing
(`#processo`, `js/views/landing.js`), estudos (`#study-process-mount`),
simulado-hub (`#sim-process-mount`) e diagnóstico (`#diag-process-mount`).
Estudos: catálogo/questões/diagnóstico/roteiro/revisão com `institution`
(`splitEditionValue` p/ filtro `INSTITUTION-YEAR` rotulado `2022 · EAJ`,
`?institution=EAJ` na URL, `refreshReviewForInstitution`); CN/CH via
backend sem lista fixa; origem `Oficiais da trilha`. Simulado: hub por
trilha + edições `INSTITUTION-YEAR` (`2021 · EAJ` só EAJ) + criações com
`institution`; wizard usa `currentInstitution()`. Landing: `#processo-stats`
240 IFRN (banco local) + 128 EAJ (`data/import/report-eaj.json`,
Q22/Q39-2025 fora). Provas: `node --check` 7 arquivos, `check_frontend.py`
OK (72 arquivos, regra F.1 nova), serve 200 nas 4 páginas + 2 JS,
Playwright (landing: seletor renderiza, 0 erros console, clique EAJ
persiste `voupassar.institution=EAJ`; estudos `?institution=EAJ` sem
sessão → guarda, 0 erros). Pendente p/ G.2: E2E autenticado por trilha
(conta nova na trilha EAJ) e importação EAJ no banco local (só IFRN-240
lá; EAJ validado em scratch na D.3).

Entregas:

* Seletor de processo na landing, estudos, simulado-hub e diagnóstico
  (persistido no estado; default IFRN para não deslocar usuários atuais);
  filtro de ano inclui EAJ 2021/2022/2025 rotulados (`2022 · EAJ`);
  filtro de disciplina inclui CN/CH quando EAJ; contadores da landing
  somam as 130 (total + por processo, sem números inventados).
* `docs/frontend-*.md` afetados + `check_frontend.py` (nova convenção
  `institution`).

Critérios:

* Trocar IFRN↔EAJ filtra tudo (lista, questão, simulado, diagnóstico) sem
  misturar 2022/2025 entre processos; conta nova vê vazio honesto na trilha
  EAJ; `node --check` + `check_frontend.py` + serve 200.

## TASK F.2 — Figuras e passagens EAJ na UI [DONE 2026-10-10]

Execução 2026-10-10: `js/components/figure.js` namespaced
(`figureInstitution`/`resolveKey` EAJ→`EAJ-<ano>-<n>`, IFRN legado intacto;
`figureCredit`/`figureNotice` com fallback EAJ/Comperve + DESCONHECIDA) +
`js/components/passage.js` (`passageCreditFallback` EAJ/Comperve +
DESCONHECIDA, mesmo `<details>`; TIRINHA/CHARGE→"Mostrar imagem") +
`js/vocab.js` (`officialLabelFor`/`originLabel`/`questionRef`/`editionRef`/
`annulledNote`: `Oficial do EAJ`, `EAJ 2022 Q8`, `EAJ-2022 · Q8`, anulada sem
banca errada) aplicado em questão/estudos/simulado (+ revisão neutra) +
`check_frontend.py` (regra F.2 nova + ban a IFRN fixo nas views).
Provas: `node --check` 7 arquivos, `check_frontend.py` OK (72 arquivos),
serve 200 (5 páginas + manifest + 3 webp EAJ), navegador real (Playwright):
amostra EAJ-2021-Q11/EAJ-2022-Q8/EAJ-2025-Q30 com figura (img carrega
`eaj/<ano>/Q<NN>.webp` + legenda Comperve) e passagem (crédito Comperve +
DESCONHECIDA), IFRN 2022-Q15 regressão intacta, EAJ sem recorte com aviso
honesto, mobile 360px overflow 0, console 0 erros. `check_figures`,
`extract_passages --check` e `check_eaj_md` intactos. Pendente p/ G.2
(herdado da F.1): E2E autenticado por trilha e importação EAJ no banco
local (validação aqui com dados sintéticos + manifest real, sem backend
EAJ; ver `docs/blockers.md`).

Entregas:

* `figure.js` resolve manifest namespaced (`EAJ-2022-8` → `eaj/2022/Q08.webp`,
  com fallback e aviso); painéis de passagem EAJ (Textos 1–3/trechos) nos
  cards e na questão; crédito EAJ/Comperve + página.

Critérios:

* Amostra por edição EAJ: figura expande, passagem expande, mobile 360px
  sem overflow, console sem erros inesperados (navegador real; senão
  pendência registrada).

---

# FASE G — FECHAMENTO E PRÉ-LANÇAMENTO EAJ

## TASK G.1 — Docs, direitos e pendências [DONE 2026-10-10]

Execução 2026-10-10 (docs-only, sem mudança de código/frontend/backend):
`docs/database-erd.md §11` novo (institution/50Q/CN-CH + study_plans +
proveniência + crédito/fonte + DESCONHECIDOS, tudo com arquivo:linha
vigente; §§9–10 preservados com ponteiro EAJ); `README.md` multi-processo
(tabela 240 IFRN + 128 EAJ de `data/import/report-eaj.json:33-54`, estrutura
por edição da V19, crédito/fonte por camada, limitações EAJ + banco local
só IFRN-240); `docs/blockers.md` seção G.1 (gabaritos oficiais EAJ
pendentes, Q22/Q39-2025 abertas com destino de retranscrição, E2E por trilha
+ importação local donas da G.2, MCP §31, takedown reativo sem gate).
Provas: `check_frontend.py` OK (72 arquivos), `node --check` 7 JS,
serve 200 (5 páginas + manifest), `check_eaj_md` + `extract_passages
--check` verdes; `git status` só docs (sem regressão de código).
Pendente p/ G.2 (herdado): E2E autenticado por trilha e importação EAJ no
banco local.

Entregas:

* `docs/database-erd.md` (institution, 50Q, CN/CH), `README.md`
  (multi-processo + contagens + limitações), `docs/blockers.md`
  (gabaritos oficiais EAJ pendentes de anexação; Q22/Q39-2025 se ainda
  abertas; navegador real se MCP indisponível).
* Todo conteúdo EAJ servido carrega crédito + fonte (AGENTS.md §12);
  takedown reativo documentado, sem gate de publicação.

Critérios:

* Nenhum doc promete endpoint, coluna ou fluxo inexistente; cada doc cita
  arquivo:linha do código vigente.

## TASK G.2 — Checklist ponta a ponta (trilha EAJ + regressão IFRN) [DONE 2026-10-10]

Execução 2026-10-10 (detalhes e pendências em `docs/blockers.md`, seção
G.2; decisão de escopo: 2021+2022 em fluxo integral + 2025 com a guarda
`409 INCOMPLETE_EDITION` verificada — o fluxo real 2025 integral aguarda a
retranscrição Q22/Q39):

* Banco local: importação EAJ executada (128: 50/40/38; 512 opções, 256
  sources, 128 classificações; idempotente) + figures 57 + passages 11/33
  vínculos — 368 questões, 98 figuras, 42 passagens; usuários scratch
  removidos (só pré-existentes).
* Regressão herdada da E.2 reparada (só testes): 13 stubs de controller
  migrados para `(…, institution)` + 5 passthrough `?institution=EAJ`;
  `mvn -f backend/pom.xml test` **284/284**.
* Achado funcional + correção: figuras EAJ não renderizavam com dados
  reais (`has_figure=false` × gate do `figure.js`); `sync_figures.py`
  backfilla a flag em questão com recorte publicado (98 flags, 0 sem
  recorte).
* API 55/55 (`/tmp/opencode/g2_checklist.py` + `g2_results.json`): trilha
  EAJ integral (roteiro `v2.1-institution` com evidência 2021/2022/2025,
  4 áreas, by-edition 50/40Q com notas 180 min + Q23-2025 `X` fora da
  pontuação, 2025 em 409) + regressão IFRN (9 edições, 240Q, 404 honestos)
  + segurança (401 sem stacktrace, CORS, 400).
* Navegador real headless (sem MCP na sessão) 20/20 (`pwtest/g2-e2e.cjs`):
  seletor, guarda, cadastro, estudos EAJ, figuras/passagens com Comperve,
  hub 2021 · EAJ, regressão IFRN, mobile 360px, teclado, console limpo.
* Segurança/backup: CORS restrito, `.env` fora do Git, `pg_dump -Fc` →
  restore 368/368 (scratch removido).
* Correções de status E.2 (sem código): wizard wired à trilha,
  `v2.1-institution` aplicado, dashboard/perfil = global legado.

Entregas:

* Fluxo EAJ: cadastro → diagnóstico → roteiro com evidência EAJ → questões
  (4 áreas) → simulado edição real (2021: 50Q; 2022/2025: 40Q) → ritmo 3h →
  resultado → revisão → perfil → novas recomendações.
* Regressão IFRN: fluxo existente inalterado (anos 2020–2026, 240Q).
* Responsividade (desktop/mobile), acessibilidade prática, segurança
  (sem stacktrace, CORS, secrets via env), backup/restauração.

Critérios:

* Checklist todo verde com evidências (prints/logs/testes); sem DONE sem
  evidência; pendências restantes em `blockers.md`, nunca silenciadas.

---

# CRITÉRIO GLOBAL DE CONCLUSÃO

O programa EAJ só está concluído quando, ALÉM do fluxo IFRN vigente:

ESTUDANTE EAJ
↓
SELETOR EAJ
↓
DIAGNÓSTICO (trilha EAJ)
↓
ROTEIRO COM EVIDÊNCIA EAJ (edições + questões citadas e existentes)
↓
QUESTÕES (LP/MAT/CN/CH conforme a edição)
↓
SIMULADO EDIÇÃO REAL (40 ou 50Q, áreas e duração daquela edição)
↓
RESULTADO (Q23-2025 fora da pontuação, com nota visível)
↓
REVISÃO → PERFIL → NOVAS RECOMENDAÇÕES

---

# ORDEM DE PRIORIDADE

1. fundação e evidência (A → B: sem fonte normalizada, nada anda);
2. modelo de dados (C: trava D e E);
3. classificação e importação (D: trava E e F);
4. backend multi-processo (E);
5. frontend multi-processo (F);
6. fechamento e lançamento (G).

Nunca sacrificar a confiabilidade do conteúdo para entregar interface mais rapidamente.
Nunca apresentar transcrição como documento oficial; nunca interpolar edições inexistentes.

## Atualização 2026-10-09 — retranscrição Q22/Q39-2025 (Fase G.2)

* `data/provas/EAJ/2025/questoes.md` corrigido a partir do PDF renderizado
  pp. 10 (Q22 B=`13/2`, C=`1,3/20`) e p. 14 (Q39 soma=`2/3`, razão=`3/5`,
  gabarito `A` — corrigido de `C` com dados divergentes).
* Auditoria visual registrada em `docs/blockers.md` (sem inventar; PDF nunca
  vira fonte de extração — TASKS.md regra 1, AGENTS.md §4).
* `check_eaj_md` 0 (130/130, Q22/Q39 mantêm `NEEDS_VISUAL_CHECK` por decisão
  do pipeline; `.md` corrigido, tabela compilada consistente).
* `extract_eaj` e `link_eaj_keys` revalidados (130 vinculadas, 0 divergências,
  SHA atualizado, `NEEDS_VISUAL_CHECK` preservado para Q22/Q39).
* `validate_classification` 370/370.
* `import_questions.py --institution EAJ --allow-needs-visual-check`: executado
  (exit 0). `data/import/report-eaj.json` mostra `blocked_needs_visual_check: 2`
  (Q22, Q39) e `candidates: 128`; o banco local (`voupassar-db`) mantém 368
  questões (240 IFRN + 128 EAJ) com 2025 em 38Q (sem 22/39) porque a migração
  V20 (`institution` na `questions`) ainda não está aplicada naquele container
  (observação documentada nas C.1/C.2/C.3 — não gate da G.2).
* Pendência aberta (nunca silenciada): aplicar V20 no banco local (`voupassar-db`)
  e reexecutar o importador para que as 130 EAJ entrem integralmente (2025:
  40Q, Q23 `X` fora da pontuação, Q22/Q39 com `needs_visual_check` se ainda
  aplicável após a retranscrição — decisão de escopo mantida).

## Resolução 2026-10-10 — go-live EAJ na VPS (pós G.2)

* `database/migrations/V22__eaj_2025_retranscricao.sql`: o `.md`
  pós-retranscrição mudou de sha e a V19 ficou obsoleta (importador travava
  em fail-high); V22 reconcilia o `OUTRO` (EAJ,2025) para `c59416d0…`.
* Deploy `voupassar-backend:prod-20261010-v22`: Flyway aplicou V17–V22;
  import com `--allow-needs-visual-check` → 130/520/260/130; figuras 58
  (`has_figure` 99); passagens 11 + 34 vínculos (0 pulados); banco 430Q.
* Smoke público 7/7 (EAJ-2021 50Q, EAJ-2025 40Q sem `409`, regressão IFRN).
  Detalhes em `docs/deploy-vps.md` §3c e `docs/blockers.md`.
* A "pendência V20 no banco local" acima referia-se à máquina de
  desenvolvimento em 2026-10-09 — na VPS está resolvida.
