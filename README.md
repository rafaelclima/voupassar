# VouPassar — preparação para o IFRN Integrado + EAJ/UFRN (multi-processo)

Plataforma web de preparação para os processos seletivos dos Cursos Técnicos
Integrados do IFRN e da Escola Agrícola de Jundiaí (EAJ/UFRN, banca Comperve),
orientada por evidências: provas reais → extração → classificação → banco →
diagnóstico → roteiro → simulados → desempenho.

> Cada afirmação abaixo cita arquivo:linha do código vigente (critério G.1).
> Nenhum número, endpoint, coluna ou fluxo é prometido sem existir no repo.

## Acervo multi-processo (G.1)

Seletor IFRN/EAJ no frontend (`frontend/js/state/process.js:10-16`,
`frontend/js/components/process-selector.js` — regra em
`scripts/analysis/check_frontend.py:706-738`); trilha via `?institution=` no
backend (`docs/api-provas.md:17`, `docs/api-questoes.md:15-16`,
`docs/api-simulado-edicao.md:23`, `docs/api-diagnostico.md:16-17`,
`docs/api-revisao.md:27`, `docs/api-roteiro.md:23-33`,
`docs/api-recomendacao.md:16-17`). Ano sozinho nunca decide a edição
(EAJ-2022 ≠ IFRN-2022).

| Processo | Edições no banco | Questões `OFFICIAL` | Estrutura por edição |
|---|---|---|---|
| IFRN | 6 (2020, 2022–2026; IFRN-2021 ausente) | **240** (40 × 6; LP 120 + MAT 120) | 40Q 20 LP + 20 MAT + 1 textual, 240 min (`docs/provas-inventario.md §1`) |
| EAJ/UFRN | 3 (2021, 2022, 2025; sem 2020/2023/2024/2026) | **128** importadas (50/40/38: LP 55, MAT 53, CN 12, CH 8; Q23-2025 anulada `X`; Q22/Q39-2025 excluídas — `data/import/report-eaj.json:33-54`) de 130 candidatas (`data/import/report-eaj.json:13-14`) | 2021: 50Q (15/15/12/8); 2022/2025: 40Q (20/20); 180 min, sem discursiva (`database/migrations/V19__seed_eaj.sql:62-67`) |

Dimensão `institution` (`IFRN`/`EAJ`) em `exams`
(`database/migrations/V17__eaj_institution.sql:32-42`,
`backend/src/main/java/br/com/voupassar/exams/entity/Exam.java:38-39`);
oficiais até 50 + CN/CH
(`database/migrations/V18__eaj_questions_50_and_cn_ch.sql:64-76`,
`database/migrations/V20__eaj_taxonomy_cn_ch.sql:38-81`);
roteiro com uma trilha por processo
(`database/migrations/V21__study_plans_institution.sql:18-27`).
Modelo completo em `docs/database-erd.md §11`.

Todo conteúdo EAJ servido carrega crédito + fonte (AGENTS.md §12):
nota de origem em cada questão
(`backend/src/main/java/br/com/voupassar/questions/service/QuestionService.java:547-549`,
campo em
`backend/src/main/java/br/com/voupassar/questions/dto/QuestionResponse.java:31-32`);
figuras `EAJ/UFRN (Comperve)` + página DESCONHECIDA
(`frontend/js/components/figure.js:105-114`);
passagens com fallback Comperve + DESCONHECIDA
(`frontend/js/components/passage.js:41-45`);
selo/título/anulada por processo
(`frontend/js/vocab.js:61-66`, `frontend/js/vocab.js:86-111`).
PDFs nunca redistribuídos como binário (só metadados — `docs/api-provas.md:7-8`);
proteção por takedown reativo, sem gate de publicação (ver `docs/blockers.md`).

## Arquitetura

`GitHub → Actions → Pages (frontend estático) → API REST (Spring Boot) → PostgreSQL (VPS via containers)`

| Parte | Tech | Docs |
|---|---|---|
| Frontend | HTML5/CSS3/JS (ES modules, sem framework) | `docs/frontend-*.md` |
| Backend | Java 21 + Spring Boot (`backend/`) | `backend/README.md`, `docs/api-*.md` |
| Banco | PostgreSQL + Flyway (`database/migrations/`) | `docs/database-erd.md` |
| Análise | Scripts Python (`scripts/analysis/`) | `docs/content-map.md` |

## Rodar local

```bash
cp .env.example .env   # preencher POSTGRES_PASSWORD e JWT_SECRET
docker compose up -d --build
curl http://localhost:8080/api/v1/health
```

## Roteiro de estudos (score v2)

Determinístico e explicável (`ALGORITHM_VERSION=v2-deterministico`,
`docs/api-recomendacao.md`): `frequência histórica real × (1−acerto) ×
dificuldade × recência`, lacuna com piso + ordem por frequência, carga por
meta/ano-alvo só no banding. Cada item cita edições e questões oficiais
(`evidenceJson`); conta nova recebe plano `PROVISORIO` (só frequência) que
vira `PESSOAL` após 3+ pontuáveis (`docs/api-roteiro.md`).

## Revisão guiada

Motor pronto no backend (`GET /review/queue`, `POST /review/sessions`,
`docs/api-revisao.md`); aba de Revisão em Estudos e wizard "Descobrir meu
nível" (12Q) são trilha local com navegador real (TASKS.md Fases 17/20.2);
raio-X pós-atividade e ritmo de simulado idem (19.x/21.1).

## Limitações conhecidas

- Dificuldade é palpite global BAIXA com peso limitado (+50%) — calibração
  real só com desempenho por edição (Fase 4).
- Classificações derivadas, sem carimbo humano (decisão 2026-10-06).
- Edição IFRN-2021 ausente no dataset — nunca interpolada (EAJ-2021 existe,
  50Q em 4 áreas, só na trilha EAJ).
- EAJ, gabarito = transcrição da curadoria (`TRANSCRIBED_FROM_MD`), nunca
  oficial: sem PDF de gabarito EAJ no repo (`data/import/report-eaj.json:6-7`,
  `docs/provas-inventario-eaj.md:13-17`); Q22/Q39-2025 excluídas até
  retranscrição + revalidação B.1/B.2/D.1 (`data/import/report-eaj.json:15-31`);
  Q23-2025 anulada `X` fora do aproveitamento (ordem das frações NÃO
  CONFIRMADA); EAJ-2022 40/40 `A` NÃO CONFIRMADO; páginas EAJ DESCONHECIDAS
  (`backend/src/main/java/br/com/voupassar/exams/entity/Question.java:66-71`,
  `backend/src/main/java/br/com/voupassar/exams/entity/Passage.java:71-77`).
  Detalhes e destino em `docs/blockers.md`.
- Banco local com só IFRN-240; EAJ-128 validado em scratch (D.3) e com
  sintéticos + manifest real (F.2) — importação EAJ no banco local e E2E
  autenticado por trilha pendentes para a G.2 (ver `docs/blockers.md`).
- CTR de revisão/roteiro/raio-X, conclusão de wizard e D7: encanamento
  servidor pronto (`GET /admin/diagnostics`), metades de clique na trilha local.
- E2E em navegador real pendente nesta VPS (sem Chromium) — ver `docs/blockers.md`.
- Produção exige `BEHIND_PROXY=true` (proxy reverso injeta `X-Forwarded-For`
  real) e segredos via ambiente — ver `docs/deploy-vps.md`.
