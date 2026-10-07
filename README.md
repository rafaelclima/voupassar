# VouPassar — preparação para o IFRN Integrado

Plataforma web de preparação para o processo seletivo dos Cursos Técnicos
Integrados do IFRN, orientada por evidências: provas reais → extração →
classificação → banco → diagnóstico → roteiro → simulados → desempenho.

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
- Edição 2021 ausente no dataset — nunca interpolada.
- CTR de revisão/roteiro/raio-X, conclusão de wizard e D7: encanamento
  servidor pronto (`GET /admin/diagnostics`), metades de clique na trilha local.
- E2E em navegador real pendente nesta VPS (sem Chromium) — ver `docs/blockers.md`.
- Produção exige `BEHIND_PROXY=true` (proxy reverso injeta `X-Forwarded-For`
  real) e segredos via ambiente — ver `docs/deploy-vps.md`.
