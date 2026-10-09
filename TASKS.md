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

## TASK A.2 — Inventário das edições EAJ

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

## TASK A.3 — Normalização dos 3 `questoes.md` para schema único

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

## TASK B.3 — Figuras: PNGs livres → convenção namespaced + WebP

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

## TASK C.1 — Dimensão `institution` em `exams` + contagens por área

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

## TASK C.2 — Questões até 50 + disciplinas CN/CH

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

## TASK C.3 — Seed edições e documentos EAJ

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

## TASK D.1 — Classificação pedagógica EAJ

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

## TASK D.2 — Mapa de conteúdo EAJ (separado do IFRN)

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

## TASK D.3 — Importação das 130 questões (idempotente)

Entregas:

* `import_questions.py` estendido (namespace EAJ, 50Q, CN/CH, fonte
  `TRANSCRIBED_FROM_MD`, `question_sources` PRIMARY=`questoes.md` +
  GABARITO_TRANSCRITO): importa 129 + Q23-2025 como anulada (`X`);
  **Q22/Q39-2025 BLOQUEADAS até conferência visual** (importador recusa com
  exit 2 listando-as, salvo flag explícita de curadoria após a conferência).
* `--check` + `--report` em `data/import/report-eaj.json`.

Critérios:

* Contagens: 130 `questions` (OFFICIAL, `institution=EAJ`), 518+ opções
  (129×4 + Q23 sem efeito de pontuação — alternativas presentes),
  classificações 130, checksums distintos 130; 2ª execução idempotente
  (exit 0, 0 a inserir); suíte backend verde.

## TASK D.4 — Sync figuras + passagens EAJ

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

## TASK E.1 — Edições e questões com `institution`

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

## TASK E.2 — Diagnóstico, roteiro e revisão na trilha EAJ

Entregas:

* Diagnóstico/conteúdos/recomendação com filtro `institution`: frequência
  histórica EAJ (D.2) alimenta evidência do roteiro da trilha EAJ sem
  contaminar o perfil IFRN; `evidenceJson` cita edições EAJ
  (`EAJ 2022 Q12`); wizard "Descobrir meu nível" parametrizável por processo
  (default IFRN, sem quebrar o fluxo existente).
* Docs `api-diagnostico.md`, `api-recomendacao.md`, `api-roteiro.md`,
  `api-revisao.md` atualizadas.

Critérios:

* Conta com histórico EAJ recebe plano com evidência EAJ válida (edições +
  questões existentes no banco); conta IFRN inalterada (regressão
  automatizada); `ALGORITHM_VERSION` bump documentado se a fórmula mudar.

---

# FASE F — FRONTEND MULTI-PROCESSO

> Depende de D + E. Seguir AGENTS.md §31 (navegador real quando disponível;
> senão `node --check` + `check_frontend.py` + serve 200 e pendência em
> `blockers.md`).

## TASK F.1 — Seletor IFRN/EAJ + filtros estendidos

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

## TASK F.2 — Figuras e passagens EAJ na UI

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

## TASK G.1 — Docs, direitos e pendências

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

## TASK G.2 — Checklist ponta a ponta (trilha EAJ + regressão IFRN)

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
