# PORTAL IFRN — ROADMAP DE DESENVOLVIMENTO

## OBJETIVO

Construir uma plataforma web completa de preparação para o processo seletivo dos Cursos Técnicos Integrados do IFRN. Nomeei essa plataforma como "VouPassar".

Arquitetura alvo:

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

---

# FASE 16 — PÓS-AUDITORIA 2026-10-07 (escopo desta entrega)

> Origem: auditoria completa do produto de 2026-10-07 (ver resposta da
> auditoria §§1–14). Esta fase transforma as recomendações em tasks
> executáveis, uma a uma, na ordem de dependência.
>
> Exclusões explícitas por decisão do responsável em 2026-10-07:
> * P4 (figuras órfãs / `List.of()` vazio) — **FORA**: já resolvido na VPS.
> * F5 (enunciado visual completo / pipeline de figuras) — **FORA**: figuras
>   já estão sendo mostradas aos usuários.
> * P5 (tempo agregado em desempenho/diagnóstico/roteiro) — **ESCOPO REDUZIDO**:
>   só vale como noção de ritmo **dentro da sessão de simulado** vs. tempo de
>   prova real (TASK 21.1). Não agregar `time_spent_seconds` em performance,
>   diagnóstico, recomendação ou revisão.
>
> Ordem de execução: 16 → 17 → 18 → 19 → 20 → 21 → 22. Não pular 16.1
> (segurança) nem 17 (revisão) antes de 19 (raio-X depende de 17).
>
> Tag `[VPS]` no título = executável direto na VPS (repo + backend + banco
> + docs). Sem a tag = fazer local (frontend exige navegador real com
> Chromium para validação E2E, que a VPS não tem — ver `docs/blockers.md`).
>
> Trilha VPS (sequencial, mesmos arquivos — 1 LLM por vez):
> `16.1 → 18.1 → 18.2 → 20.1 → 22.1 → 22.2 → 22.3`.
> Paralelo seguro com a trilha local: `16.2` a qualquer momento; o frontend
> (16.3, 17.x, 18.3, 19.x, 20.2, 21.1) roda local em paralelo, desde que
> ninguém toque `studyplan/*` (VPS) nem `simulado.js` (19.1 × 21.1) ao mesmo
> tempo. Sincronizar via git entre as trilhas antes de cada task.

---

## TASK 16.1 — Correção de autorização no roteiro (P1 — Crítico) [PRIORIDADE MÁXIMA] [VPS]

Problema (auditoria §6 P1):

* `GET /recommendations` e `GET /recommendations/plan` aceitam `?userId`
  opcional e usam no lugar do dono
  (`studyplan/controller/RecommendationController.java:38,47` —
  `long uid = (userId != null) ? userId : requireAuth(principal)`);
* `POST /recommendations/items/{itemId}/status` não recebe `principal` nem
  checa dono (`RecommendationController.java:57-62` vs.
  `RecommendationService.java:250` sem `userId`).

Entregas:

* remover `?userId` dos dois GETs (usar só `principal`);
* `updateItemStatus(itemId, status, principalUserId)`: buscar item → plano →
  comparar `plan.userId` com o autenticado → `403` se divergir;
* trocar retorno de entidade JPA `StudyPlan` por DTO (não vazar entidade);
* teste de integração: A não gera/vê/atualiza plano de B (401 sem token,
  403 com dono divergente, 200 no dono);
* atualizar `docs/api-recomendacao.md` + `docs/api-roteiro.md` +
  `docs/security-audit.md` (remover `?userId` do contrato).

Critérios:

* `rg userId backend/src/main/java/br/com/voupassar/studyplan` sem ocorrência;
* suíte backend verde; tentativa IDOR coberta por teste automatizado;
* `node --check`, `check_frontend.py` (frontend intocado, mas validar).

---

## TASK 16.2 — Sincronização de docs divergentes (P8 — quick win, sem código) [VPS]

Problema (auditoria §6 P8): docs à frente/atrás do código.

Entregas (uma a uma):

1. `docs/frontend-admin.md`: remover fila `review-queue` + 2× PATCH; documentar
   somente-leitura real (2 GETs) + referência à decisão 2026-10-06;
2. `docs/ux-review.md:19`: trocar `240/6 edições/120+120` pelos números reais
   da landing (`index.html:83-96`, `docs/frontend-landing.md:46-49`) ou marcar
   como exemplo histórico;
3. `docs/security-audit.md:30`: corrigir "nenhum endpoint `/admin`" (hoje existe
   `AdminController.java:30` com RBAC);
4. `docs/database-erd.md:225-304`: remover `validation_status`,
   `publication_status`, `status/reviewed_by/reviewed_at` (removidos em
   `V12__drop_review_gates.sql:13-29`); documentar "vigente = maior `id`" +
   `pipeline_version/pipeline_verified_at`;
5. `docs/frontend-auth.md:11`: corrigir "Painel logado" (código faz redirect —
   `views/login.js:29-34`).

Critérios:

* cada doc cita o arquivo:linha do código vigente;
* nenhum doc promete endpoint, coluna ou fluxo que não existe;
* sem alteração de código-fonte (só `docs/`).

---

## TASK 16.3 — Higiene de frontend: retry por seção + auth global + next/voltar (P9/P10)

Problema (auditoria §6 P9/P10):

* dashboard/perfil/simulado-hub mostram `friendlyMessage+traceId` sem botão
  "Tentar de novo" por seção (só estudos-lista e caderno têm);
* cada `views/*.js` repete `restoreSession+showGuard`; `api/client.js` não
  renova refresh sozinho;
* `safeNextParam` aceita `/...` (`auth-shared.js:117-128`) mas `sanitizeBack`
  só `./` (`questao.js:161-169`).

Entregas:

1. botão "Tentar de novo" por seção em `dashboard.js` e `perfil.js`
   (mesmo padrão de `renderMissingCard` do simulado);
2. unificar `safeNextParam/sanitizeBack` em `auth-shared.js` (uma função,
   usada por login, questao-voltar e simulado-guard);
3. interceptor 401 central em `api/client.js` (tentar `refresh` 1× via
   `api/auth.js:138-164`, senão `showGuard`); remover duplicação nas views
   sem mudar comportamento visível;
4. atualizar `docs/frontend-dashboard.md`, `docs/frontend-perfil.md`,
   `docs/frontend-auth.md` + `scripts/analysis/check_frontend.py` se houver
   nova convenção.

Critérios:

* `node --check` + `check_frontend.py` + serve 200;
* navegador real: derrubar 1 seção simulada → retry funciona sem reload;
  token expirado → 1 refresh silencioso, depois guard;
* mobile 360px + console limpo.

---

# FASE 17 — REVISÃO GUIADA (F1 — backend pronto, UI zero; P2 Crítico)

> Decisão: **evolução, não feature nova**. Não criar motor novo; expor
> `GET /review/queue` + `POST /review/sessions` que já existem
> (`ReviewService.java:109`, `ReviewSessionService.java:43-58`).

## TASK 17.1 — API client + contrato de revisão no frontend

Entregas:

* `frontend/js/api/review.js`: `getQueue({limit,discipline,topicId,onlyErrors})`,
  `createSession({questionIds?,limit,...})`, `getSession(id)`,
  `getSessionResult(id)` — espelho fino de `api/simulado.js`;
* `frontend/js/vocab.js`: rótulos das categorias 0–5
  (`ERRO_SEM_ACERTO → CONSOLIDADO`) + motivos, sem enum cru;
* doc `docs/api-revisao.md` + `docs/api-modo-revisao.md` sem mudança de
  contrato (só exemplos de uso pelo frontend).

Critérios:

* `node --check`; nenhum fetch fora de `api/`; erros com `traceId`.

## TASK 17.2 — Aba Revisão em Estudos (fila priorizada)

Entregas:

* nova aba/seção em `estudos.html` + `views/estudos.js` (ou `views/revisao.js`
  dedicado): filtros disciplina/tópico/só-erros + lista da fila com
  categoria + motivo + `daysSince` informativo + "Iniciar revisão (N)";
* estados: loading (`Buscando fila…`), vazio honesto ("responda 3+ questões
  para ativar" — mesmo `MIN_SCORED` do diagnóstico), erro com retry;
* `REVISAO` nos selects de modo onde houver (`simulado.html:142-145,170-173`
  hoje só `ESTUDO/PROVA`);
* atualizar `docs/frontend-estudos.md` + `check_frontend.py`.

Critérios:

* conta nova → vazio honesto, sem 500; conta com erros → fila ordenada
  igual ao backend (pesos 0–5, desempates determinísticos);
* guarda de auth + 401 → guard (padrão das demais telas);
* navegador real: fila → iniciar → caderno → resultado, mobile + console limpo.

## TASK 17.3 — Execução e resultado da sessão de revisão

Entregas:

* reutilizar caderno do simulado (modo `REVISAO`, nunca oculta — por doc);
* `POST /review/sessions` congela top-N em `review_session_questions`;
  responder via `POST /attempts {mode:REVISAO}`; `GET /result`;
* ao final: "voltar à fila" (fila recomputada — item consolidado some);
* doc `docs/frontend-revisao.md` (novo, curto) + link no dashboard
  ("Revisar erros" junto às prioridades).

Critérios:

* E2E: errar 3 → fila contém 3 → sessão com 3 → acertar tudo → fila esvazia;
* `BLANK`=erro, anulada fora (mesmas regras do backend);
* testes frontend de lógica crítica (fila vazia, limite, só-erros).

---

# FASE 18 — ROTEIRO EXPLICÁVEL V2 (F2 — P3 Crítico + P6)

> Decisão: **evoluir o roteiro, não criar feature nova**. Manter determinístico,
> transparente e auditável (`ALGORITHM_VERSION=v2-deterministico`).

## TASK 18.1 — Score v2 com frequência histórica real + recência + dificuldade [VPS]

Entregas (backend, `RecommendationService.java:118-186`):

* `frequencyFactor` = contagem histórica normalizada (`countByTopic` +
  `editionsByTopic`, mesma fonte do diagnóstico `DiagnosisService.java:147-154`,
  via `ContentService`), **não** `attempts/5`;
* `recencyFactor` de `StudentTopicPerformance.lastAttemptAt` (já persistido;
  hoje `1.0` em `:146`);
* `difficultyFactor` da dificuldade classificada (`per-edition/*.json`,
  hoje `1.0` em `:147`); documentar pesos e normalização em
  `docs/api-recomendacao.md:42-50`;
* `target_year` (urgência) e porte via `study_goal` entram como multiplicador
  de carga, não como filtro (usa P6 sem nova migração);
* `ALGORITHM_VERSION` bump + migração Flyway só para versionar
  (sem novas tabelas de domínio).

Critérios:

* teste unitário: tópico frequente-e-fraco > raro-e-fraco, tudo o mais igual;
  `attempts==0` segue lacuna mas ordenado por frequência;
* `docs/api-recomendacao.md` com fórmula + exemplo numérico;
* suíte backend verde.

## TASK 18.2 — Evidência rica + subtópico (quando houver sinal) [VPS]

Entregas:

* `evidenceJson`: `{historicalQuestions, editionsCount, editions[],
  sampleQuestionIds[], accuracy, attempts, lastAttemptAt, algorithmVersion}`
  (hoje só `topic_id+attempts+nota` em `:219-226`); `content-map` e evidência
  seguem 100% oficiais (autorais nunca produzem evidência — regra Fase 15);
* `subtopicId` preenchido quando `bySubtopic` tiver sinal
  (hoje `NULL` fixo em `:177`; `SubtopicRepository` injetado mas ocioso);
* teste: cada item do plano cita ≥1 edição e ≥1 questão oficial existentes.

Critérios:

* `GET /plan` de conta com histórico contém `editions[]` e
  `sampleQuestionIds[]` válidos (checados contra banco);
* docs `api-roteiro.md:15-36` atualizadas com novo schema do JSON.

## TASK 18.3 — UI da evidência + "Praticar / Revisar agora"

Entregas (frontend):

* `dashboard.js:818-925 renderPlan`: cada item mostra
  "prioridade · você X% (N tentativas) · caiu em E edições (ex. 2024 Q12) ·
  último erro há N dias" + CTAs "Praticar agora" (recorte em estudos) e
  "Revisar erros" (fila filtrada por tópico — depende da 17.2);
* `estudos.js:722-839 renderHero` e `perfil.js:803-866 renderMetas` exibem o
  mesmo motivo (reuso, sem duplicar lógica);
* atualizar `docs/frontend-dashboard.md` + `check_frontend.py`.

Critérios:

* usuário entende "por que estudar isso" sem abrir doc;
* CTA pratica abre recorte correto; CTA revisa abre fila filtrada;
* navegador real desktop + mobile, console limpo.

---

# FASE 19 — RAIO-X PÓS-ATIVIDADE (F3 — segunda ordem, depende da Fase 17)

## TASK 19.1 — Componente `result-next` no simulado

Entregas:

* `frontend/js/components/result-next.js` reutilizável: recebe `result`
  (`scoreBoard` + `resultItems`) e renderiza (a) piores tópicos da atividade,
  (b) erros sem acerto prévio, (c) 3 CTAs máximos: "Revisar estes N agora"
  (`POST /review/sessions`), "Praticar tópico X" (estudos com recorte),
  "Atualizar meu plano" (`POST /recommendations`);
* ligado em `views/simulado.js:946-1003` (após `renderFinishedState`);
* doc curto em `docs/frontend-simulado.md`.

Critérios:

* E2E simulado com 6 erros → bloco mostra 6 + top tópico + 3 CTAs funcionais;
* sem CTA quebrado quando fila/plano vazios (fallbacks honestos);
* máximo 3 CTAs (risco de sobrecarga mitigado).

## TASK 19.2 — Raio-X em Estudos + criação de sessão por ids (opcional)

Entregas:

* fim de página/lista em `estudos.js`: "nesta sessão você errou N
  (M sem acerto prévio)" + mesmos CTAs do 19.1;
* **se** necessário: estender `POST /review/sessions` para aceitar
  `{questionIds[]}` além do top-N (retrocompatível; hoje só top-N);
  caso contrário, reutilizar filtro por tópico da 17.2 e não mexer no backend;
* testes de integração do fluxo
  simulado → revisar-N → fila menor → roteiro atualizado.

Critérios:

* fluxo completo em 1 sessão de navegador real sem reload manual;
* métricas de clique instrumentáveis (ver Fase 22).

---

# FASE 20 — DIAGNÓSTICO DE ENTRADA + PLANO INICIAL (F4 — P7 cold start)

## TASK 20.1 — Plano provisório só-frequência (sem tentativas) [VPS]

Entregas:

* `POST /recommendations` com zero tentativas gera plano `PROVISORIO`
  ordenado só por `countByTopic/editionsByTopic` (`content-map.md:21-32`
  top `60/60/25/...`), marcado como provisório na UI ("comece pelo que
  mais cai — vira pessoal após o diagnóstico");
* flag mínima (`study_plans.status` ou `score_json.provisorio`; 1 migração
  Flyway pequena) + doc `api-roteiro.md`.

Critérios:

* conta nova sem tentativas recebe plano em 1 clique, 100% oficial;
* após ≥3 pontuáveis o plano vira pessoal (regeneração cobre a flag).

## TASK 20.2 — Wizard "Descobrir meu nível" (12Q) + geração automática

Entregas:

* pós-cadastro/primeiro login: oferta "Descobrir meu nível (12 questões:
  6 LP + 6 MAT, as mais frequentes)" → simulado disciplina misto curto
  (reuso `POST /simulations/by-discipline`, sem endpoint novo se possível;
  **só se** indispensável: `POST /diagnosis/bootstrap` fino sobre
  simulado+recomendação);
* ao concluir: diagnóstico + plano v1 gerados automaticamente, dashboard
  mostra "Seu ponto de partida: X, Y, Z" com `EM_OBSERVACAO` sinalizado
  (sem fingir precisão);
* quem pular fica com o provisório da 20.1;
* docs `frontend-dashboard.md` + `api-diagnostico.md` atualizados.

Critérios:

* E2E conta nova: wizard → 12 respostas → diagnóstico + plano sem clique extra;
* pular wizard → provisório visível;
* navegador real + mobile; taxa de conclusão instrumentável.

---

# FASE 21 — RITMO EM SIMULADO (P5 — escopo reduzido por decisão 2026-10-07)

> Decisão: **não** agregar `time_spent_seconds` em performance, diagnóstico,
> recomendação ou revisão. Só exibir ritmo **dentro da sessão de simulado**,
> onde o aluno quer saber "estou no ritmo da prova real".

## TASK 21.1 — Indicador de ritmo no simulado (só exibição)

Entregas:

* durante `views/simulado.js` execução: "questão i/N · tempo decorrido total ·
  média por questão vs. referência da edição" (referência = duração oficial
  daquela edição quando conhecida, senão `DESCONHECIDA` — nunca inventar);
* usa `time_spent_seconds` já coletado + `answered_at`; **nenhum** uso em
  score, ranking ou roteiro;
* referência de duração por edição vem dos documentos oficiais quando
  existir (`docs/provas-inventario.md`); se ausente, exibir "ritmo
  informativo, sem tempo oficial confirmado";
* doc `docs/frontend-simulado.md` + `docs/api-simulados.md` com a regra.

Critérios:

* Modo Prova e Estudo mostram o mesmo ritmo (é cronômetro, não gabarito);
* nenhum teste de desempenho muda por causa do tempo;
* `rg timeSpent backend/.../performance backend/.../diagnosis
  backend/.../studyplan backend/.../review` continua vazio.

---

# FASE 22 — OBSERVABILIDADE, SEGURANÇA RESTANTE E PRÉ-LANÇAMENTO

## TASK 22.1 — Rate-limit global leve + `Retry-After` + proxy confiável (P11) [VPS]

Entregas:

* estender `AuthRateLimitFilter.java:30-76` (hoje só `/auth/**`, em memória,
  sem `Retry-After`) para rotas de escrita (`/attempts`, `/simulations`,
  `/recommendations`) com limites documentados;
* `X-Forwarded-For` (`AuthController.java:165-173`) só confiável atrás de
  proxy configurado (`application.yml` + `docs/security-audit.md`);
* header `Retry-After` em `429`.

Critérios:

* teste: rajada acima do limite → `429 + Retry-After`, sem travar uso normal;
* doc de limites em `security-audit.md`.

## TASK 22.2 — Observabilidade mínima (completa a 10.3) [VPS]

Entregas (pendências de `docs/task-10.3.md:25-80`):

* logs estruturados + rotação (sem PII/segredo; já há base em
  `AuthService.java:130,148,172,245`);
* métricas essenciais + endpoint de diagnóstico autenticado
  (health segue público com `show-details: never`);
* instrumentação das métricas da auditoria §13 (CTR revisão/roteiro/raio-X,
  conclusão de wizard, D7 pós-simulado) — sem tracking invasivo
  (respeitar AGENTS §16).

Critérios:

* `GET /health` público + diagnóstico autenticado restrito;
* dashboard técnico (ou log consultável) responde as 5 métricas do §13.

## TASK 22.3 — Pré-lançamento (checklist executável) [VPS]

Entregas:

* executar o fluxo completo contra o novo escopo:
  auth (incl. IDOR regressão 16.1) → diagnóstico → roteiro v2 com evidência →
  questões → simulado → ritmo (21.1) → resultado → raio-X → revisão →
  perfil atualizado → novas recomendações;
* validar responsividade (desktop/tablet/mobile), acessibilidade prática
  (teclado, foco, contraste, labels), segurança (CORS prod, secrets via env,
  sem stacktrace), backup/restauração validados;
* registrar pendência de navegador real se MCP indisponível (padrão
  `docs/blockers.md`), nunca marcar DONE sem evidência.

Critérios:

* checklist todo verde com evidências (prints/logs/testes);
* `README.md` atualizado com roteiro v2 + revisão + wizard +
  limitações conhecidas.

---

# CRITÉRIO GLOBAL DE CONCLUSÃO (atualizado 2026-10-07)

O projeto só pode ser considerado MVP quando o seguinte fluxo funcionar de ponta a ponta:

USUÁRIO
↓
CADASTRO
↓
LOGIN
↓
DIAGNÓSTICO
↓
ANÁLISE DO DESEMPENHO
↓
RECOMENDAÇÃO
↓
ROTEIRO DE ESTUDOS
↓
QUESTÕES
↓
SIMULADO
↓
RESULTADO
↓
ANÁLISE DE ERROS
↓
ATUALIZAÇÃO DO PERFIL
↓
NOVAS RECOMENDAÇÕES

O produto deve estar funcional tanto no frontend quanto no backend e banco.

---

# ORDEM DE PRIORIDADE

Priorizar nesta ordem:

1. segurança (autorização do roteiro);
2. revisão guiada (expor motor existente);
3. roteiro explicável v2 (frequência real + evidência);
4. raio-X pós-atividade (fechar o loop);
5. diagnóstico de entrada (cold start);
6. ritmo em simulado (só exibição);
7. observabilidade e pré-lançamento.

Nunca sacrificar a confiabilidade do conteúdo para entregar interface mais rapidamente.
