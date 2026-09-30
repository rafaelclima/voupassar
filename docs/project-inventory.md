# Inventário do Projeto — VouPassar

> TASK 0.1 — Inspeção inicial (TASKS.md). Fonte de verdade: arquivos presentes no repositório no momento da inspeção. Nenhuma informação foi inventada. Campos não comprováveis estão marcados como `DESCONHECIDO` / `NÃO CONFIRMADO` / `NECESSITA REVISÃO`.

- **Data da inspeção:** 2026-09-30 (UTC)
- **Commit inspecionado:** `f7ec098` — "chore: initial commit with project docs and IFRN exam data (2020, 2022-2026)"
- **Branch:** `main` (sincronizada com `origin/main`)
- **Método:** leitura de `AGENTS.md`, `TASKS.md`, `PROMPT.md`; listagem recursiva do disco; `pdfinfo` + `pdftotext -layout` sobre os 12 PDFs de `data/provas/`; inspeção de `.git` (log, status, remotes). Nenhum código foi executado além de leitura/extração para auditoria. Fontes originais em `data/provas/` não foram modificadas.

## 1. Estrutura atual do repositório

```
/
├── AGENTS.md          (regras do produto — existe)
├── PROMPT.md          (prompt operacional — existe)
├── TASKS.md           (roadmap Fase 0–14 — existe)
├── data/
│   └── provas/
│       ├── 2020/ (2 PDFs)
│       ├── 2022/ (2 PDFs)
│       ├── 2023/ (2 PDFs)
│       ├── 2024/ (2 PDFs)
│       ├── 2025/ (2 PDFs)
│       └── 2026/ (2 PDFs)
├── docs/              (criado nesta task; antes inexistente)
└── .git/              (repo git, remote origin git@github.com:rafaelclima/voupassar.git)
```

**Diretórios previstos em `PROMPT.md` que NÃO existem (lacuna):** `frontend/`, `backend/`, `database/`, `scripts/`, `docs/content-analysis/`, `docs/decisions/`, `data/extracted/`, `data/curated/`, `.github/workflows/`, `README.md`, `.env.example`, `Dockerfile`, `docker-compose.yml`, `pom.xml`.

## 2. Tecnologias e configurações detectadas

| Item | Estado |
|---|---|
| Código frontend (HTML/CSS/JS) | NÃO EXISTE |
| Código backend (Java/Spring) | NÃO EXISTE |
| Migrations / schema SQL | NÃO EXISTE |
| Docker / Compose | NÃO EXISTE (arquivos); ferramentas disponíveis no ambiente local: Docker 29.7.2, Compose 5.5.1 — ver §6 |
| CI/CD (GitHub Actions) | NÃO EXISTE (nenhum `.github/workflows/`) |
| Dependências / lockfiles | NÃO EXISTE |
| Lint / testes | NÃO EXISTE |
| README.md | NÃO EXISTE (exigido na Fase 14) |
| `docs/` anterior | NÃO EXISTIA antes desta task |

O repositório hoje é **dataset + especificação**. Não há arquitetura implementada, apenas arquitetura prescrita em `AGENTS.md`/`PROMPT.md`.

## 3. Dataset — provas e gabaritos (evidência direta)

Total: **6 edições × 2 PDFs = 12 arquivos**. Todos legíveis por `pdfinfo`/`pdftotext`, nenhum criptografado. Edição **2021 ausente** (confirmado: nenhum diretório `data/provas/2021/`). Não preencher com dados inventados.

### 3.1 Arquivos, páginas e metadados (via `pdfinfo`)

| Edição (pasta) | Arquivo de prova | Páginas / tamanho | Arquivo de gabarito | Páginas / tamanho |
|---|---|---|---|---|
| 2020 | `Prova.pdf` | 15 p / 609 KiB | `Gabarito_Final.pdf` | 1 p / 417 KiB |
| 2022 | `Prova_Exame_Tecnico_Integrado_2022_XNn8mg4.pdf` | 15 p / 765 KiB | `GABARITO_Prova_Exame_Tecnico_Integrado_2022.pdf` | 4 p / 122 KiB |
| 2023 | `Caderno_de_provas.pdf` | 14 p / 704 KiB | `Gabarito_Final_-_Exame_de_Selecao_2023.pdf` | 3 p / 209 KiB |
| 2024 | `Cadernos_de_provas_-_Cursos_Tecnicos_Integrados_2024_.pdf` | 15 p / 559 KiB | `Gabarito_Oficial_Definitivo_IFRN_78.pdf` | 1 p / 90 KiB |
| 2025 | `PROVA_IFRN_-_EXAME_DE_SELEÇÃO_2025.pdf` | 14 p / 634 KiB | `Exame_de_Seleção_2024_-_Gabarito_Final.pdf` | 1 p / 88 KiB |
| 2026 | `Caderno_de_Provas_-Técnico_Integrado_2026.pdf` | 14 p / 1,3 MiB | `GABARITO_EXAME_DE_SELEÇÃO_2026_-_DEFINITIVO.pdf` | 1 p / 459 KiB |

Metadados de autoria (apenas informativo, não é evidência pedagógica): 2020/2022/2023/2025 gerados via Microsoft Word (autor `comperve`/`anna`/`Setor Consultoria`); 2024-prova via PDFium (Creator PDFium, sem autor); 2026-prova via PDF24; gabaritos 2022–2025 via Chrome/Skia/PDF (origem FUNCERN/inscricoes.funcern.org em 2022). Datas de criação nos metadados são datas de geração do PDF, **não** datas oficiais da prova.

### 3.2 Estrutura declarada na capa de cada caderno (transcrição literal da capa)

Todas as seis capas declaram a mesma composição **naquele documento** (não generalizar como regra universal — cada edição deve ser tratada isoladamente na Fase 1):

| Edição | Edital na capa (como impresso) | Composição declarada na capa | Duração | Pontuação na capa |
|---|---|---|---|---|
| 2020 | 29/2019 | 20 LP + 20 Mat + 1 Produção Textual | 4h | LP 100 + Mat 100 (+ Produção — ver §3.4 NECESSITA REVISÃO) |
| 2022 | 041/2021 | 20 LP + 20 Mat + 1 Produção Textual | 4h | LP 100 + Mat 100 + Prod 100 |
| 2023 | 040/2022 | 20 LP + 20 Mat + 1 Produção Textual | 4h | tabela presente, valores NÃO CONFIRMADOS por extração (layout) |
| 2024 | 078/2023 | 20 LP + 20 Mat + 1 Produção Textual | 4h | LP 100 + Mat 100 + Prod 100 |
| 2025 | 23/2024 | 20 LP + 20 Mat + 1 Produção Textual Escrita | 4h | LP 100 + Mat 100 + Prod Escrita 100 |
| 2026 | 48/2025 | 20 LP + 20 Mat + 1 Produção Textual Escrita | 4h | DESCONHECIDO (capa não traz tabela de pontos na extração; NECESSITA REVISÃO visual) |

Seções internas confirmadas por extração de texto em **todas** as provas: cabeçalho `QUESTÕES DE MÚLTIPLA ESCOLHA – LÍNGUA PORTUGUESA` (questões 01–20), cabeçalho `QUESTÕES DE MÚLTIPLA ESCOLHA – MATEMÁTICA` (questões 21–40) e seção `PROPOSTA DE PRODUÇÃO TEXTUAL` (escrita/discursiva). 2026 traz ainda folha `RASCUNHO DA PRODUÇÃO TEXTUAL ESCRITA (DISCURSIVA)` numerada.

### 3.3 Gabaritos — o que foi efetivamente extraído (sem inferência)

| Edição | Tipo de documento encontrado | Divisão por disciplina no gabarito | Anuladas (`X`) | Observações / pendências |
|---|---|---|---|---|
| 2020 | `GABARITO FINAL`, Edital 29/2019 | LP 1–20, Mat 21–40 | **Q7 (LP) e Q26 (Mat)** | — |
| 2022 | HTML-do-FUNCERN impresso em PDF (4 p), contém **Preliminar + Definitivo** | Sem rótulo de disciplina no gabarito (NECESSITA REVISÃO: assumir 1–20 LP / 21–40 Mat só após conferência com o caderno na Fase 1) | **Nenhuma**; preliminar e definitivo extraídos são **idênticos** (40/40 A–D, sem `X`) | Verificar fora do repo se houve retificação posterior — NÃO CONFIRMADO |
| 2023 | `Gabarito Oficial Definitivo`, Edital 40/2022 (3 p; págs. 1–2 listam ~83 ofertas curso/campus/turno) | LP 1–20, Mat 21–40 (pág. 3) | **Q21 (Mat)** marcada `X` | Sem gabarito preliminar no repo |
| 2024 | `Gabarito Oficial Definitivo`, Edital 78/2023 | LP 1–20, Mat 21–40 | **Q17 (LP)** | Sem preliminar no repo |
| 2025 | `Gabarito Oficial Definitivo`, cabeçalho "…Forma Integrada - 2025" | LP 1–20, Mat 21–40 | **Nenhuma** (40/40 A–D) | **Inconsistência de nome de arquivo:** o PDF do gabarito chama-se `Exame_de_Seleção_2024_-_Gabarito_Final.pdf` mas o conteúdo diz 2025. Tratar conteúdo como 2025 e renomear/mapear apenas na Fase 1 com registro. Sem preliminar no repo |
| 2026 | `Gabarito Definitivo`, Edital 48/2025, data 04/11/2025, "X = QUESTÃO ANULADA" | Tabela única 1–40 (1–20 LP / 21–40 Mat por posição; rótulo de disciplina ausente no doc — confirmar na Fase 1) | **Q37 (Mat)** | Sem preliminar no repo. PROMPT.md já alertava para esta anulada — confirmado na fonte |

> Detalhamento questão-a-questão (enunciado, alternativas, página, vínculo gabarito) é objeto da **TASK 1.2/1.3**, não desta inspeção. Nenhuma classificação pedagógica (assunto/subassunto/dificuldade) foi realizada nesta task — qualquer lista de conteúdos neste momento seria alucinação.

### 3.4 Pontos que exigem conferência visual humana (NECESSITA REVISÃO)

1. 2020: tabela de pontos da capa truncada na extração (`Língua Portuguesa 20 questões 100 pontos`; restante ilegível no texto). Pontuação da Produção Textual 2020 = NÃO CONFIRMADA.
2. 2023: valores da tabela de pontos da capa não recuperados via `pdftotext -layout`; confirmar no PDF renderizado.
3. 2026: capa sem tabela de pontos na extração; confirmar se o edital 48/2025 define pontuação em outro documento (não presente no repo).
4. 2022: confirmar divisão disciplinar 1–20/21–40 confrontando caderno × gabarito (o gabarito não rotula disciplinas).
5. 2025: resolver a divergência nome-do-arquivo × conteúdo antes da importação (risco de idempotência).

## 4. Riscos identificados

1. **Gabarito preliminar ausente em 5/6 edições** — só 2022 traz preliminar + definitivo. Divergências preliminares (fonte de anuladas) são DESCONHECIDAS nas demais.
2. **Extração por `pdftotext` é parcial** — layout em duas colunas, fórmulas, frações, figuras (charges, tirinhas, gráficos, bola de futebol em 2023-Q23/39, TV em 2024, mapas) e textos com imagem não sobrevivem como texto. Pipeline da Fase 1 precisará de extração com número de página + revisão humana; OCR sem revisão geraria alucinação.
3. **Figuras são carga pedagógica real** — ex.: 2020-T2 (imagem da mão), 2023-T2 (bola), 2024-T2 (TV). O modelo de dados deve prever anexos/figuras por questão (ver `architecture.md`).
4. **Proveniência e direitos** — documentos são públicos no repo, mas isso não equivale a autorização de redistribuição irrestrita. O banco deve suportar `PUBLICÁVEL / NÃO PUBLICÁVEL / PENDENTE DE REVISÃO / SOMENTE REFERÊNCIA` desde o dia 1 (AGENTS.md §12).
5. **VPS de 2 GB RAM / 2 vCPU** — Spring Boot + PostgreSQL + reverse proxy cabem, mas sem margem para JVM folgada, builds in-place ou múltiplas réplicas. Exige limites de memória, Swap/ majoração documentada e deploy por imagem (ver `architecture.md`).
6. **Ausência total de CI/CD, testes e observabilidade** — nada impede hoje um deploy quebrado. Fase 10 deve vir antes de qualquer "lançamento".
7. **2021 inexistente** — qualquer série histórica 2020–2026 terá um buraco. Visualizações e estatísticas devem rotular explicitamente a ausência em vez de interpolar.

## 5. Lacunas (o que falta para o MVP, por ordem de TASKS.md)

- Fase 0: `docs/architecture.md` (entregue junto nesta leva), `README.md` (Fase 14).
- Fase 1: `docs/provas-inventario.md`, `data/extracted/`, `docs/gabaritos-validation.md`, `docs/content-analysis/`, `docs/content-map.md` — nada iniciado.
- Fases 2–13: banco, backend, motor educacional, frontend, simulados, gamificação, a11y, segurança, CI/CD, admin, produção — nada iniciado.
- Transversais: `docs/blockers.md` (criar quando o primeiro bloqueio real aparecer), `docs/decisions/` (ADRs), `.env.example`, licenciamento do conteúdo (DESCONHECIDO — decidir antes de publicar questões).

## 6. Ambiente local de referência (apenas informativo)

`java 27 (OpenJDK 27+35)`, `mvn` AUSENTE, `Docker 29.7.2 + Compose 5.5.1`, `psql 18.6`, `node v26.7.0`, `python3 14.7`, `pdfinfo/pdftotext` Poppler. Máquina local com 31 GiB RAM — **não** representa a VPS alvo (2 GB). Versões de produção serão fixadas na implementação com documentação oficial (ver `architecture.md`).

## 7. Entregas desta task e próximos passos

- [x] Repositório listado, tecnologias (in)existentes registradas, dataset identificado por edição com páginas/editais/estrutura/gabaritos, riscos e lacunas registrados.
- [x] `docs/architecture.md` criado em conjunto (TASK 0.2).
- [ ] Próxima task elegível: **TASK 1.1 — Inventário das edições** (`docs/provas-inventario.md`), que aprofunda por edição o que aqui foi resumido, sem inventar classificações.
- [ ] `docs/blockers.md`: nenhum bloqueio formal até o momento; será criado se a Fase 1 encontrar questão ilegível, gabarito divergente ou figura essencial sem texto alternativo.
