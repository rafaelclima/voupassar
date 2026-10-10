# VOUPASSAR — PROGRAMA PROVA-IA (Gerador de provas com IA)

## OBJETIVO

Adicionar ao VouPassar uma área onde o aluno gera provas inéditas com
questões criadas por IA, a partir do formato de um processo existente
(IFRN ou EAJ). O backend monta um prompt versionado, chama a LLM via
OpenRouter (chave só no servidor) e recebe um formato JSON conhecido; o
frontend executa a prova no motor de simulado existente (modos
ESTUDO/PROVA) com selo experimental visível.

Decisões de produto travadas no brainstorm 2026-10-10 (não reabrir sem
decisão documentada do responsável):

1. **Formatos v1: só IFRN + EAJ.** Sem ENEM (seria 3º processo, fora do
   banco atual), sem formato livre. Estrutura dirigida pelos dados da
   edição-modelo (IFRN: 40Q 20LP+20MAT; EAJ-2022/2025: 40Q 20/20;
   EAJ-2021: 50Q 15/15/12/8) — nunca quantidades universais.
2. **Questões efêmeras, não salvas no acervo.** Nada-IA entra em
   `questions`, `question_options`, `question_sources` ou mapa de conteúdo.
3. **Chave no servidor + fila.** `OPENROUTER_API_KEY` só em env (nunca no
   Git, nunca no frontend). Teto inicial: **3 provas-IA/dia por aluno**,
   1 geração por vez por usuário.
4. **Gabarito sem garantia de correção.** Exibido como experimental, com
   banner e selo por questão. A validação **estrutural** (JSON parseável,
   N questões, A–D completas, resposta ∈ alternativas) é obrigatória —
   o "sem garantia" vale para a correção pedagógica, nunca para o formato.
5. **Reusa o motor de simulado.** Criar, retomar, responder, concluir,
   abandonar, resultado e feedback seguem o ciclo de `simulations` /
   `simulation_attempts` (TASKs 5.1–5.3), sobre caderno-IA congelado.
6. **PROIBIDO na v1: passagem, figura e explanation.**
   * Passagem (texto-base compartilhado — TEXTO/TRECHO, ver
     `docs/passagens-estrategia.md`): questões-IA devem ser auto-contidas.
   * Figura (recorte visual por questão): a rota de texto do OpenRouter não
     gera imagens; nenhuma imagem ilustrativa/inventada será servida.
     Questão que cite figura/“observe a imagem” é rejeitada.
   * Explanation (ensino passo a passo): proibida pela decisão de produto
     2026-10-04 (a plataforma testa conhecimento). O prompt proíbe, o
     backend descarta se vier, o front nunca renderiza.

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
Spring Boot (+ OpenRouter server-to-server)
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

1. **Nunca apresentar questão-IA como oficial.** Selo obrigatório na API e
   na UI: "Questão gerada por IA — não é questão oficial IFRN/EAJ".
   `source_type` novo (ex.: `AI_GENERATED`), distinto de
   `OFFICIAL/AUTORAL/ADAPTED`. Nunca citar edição real como origem da
   questão (o formato é *inspirado em*, nunca *extraído de*).
2. **Isolamento da cadeia de evidência (AGENTS.md §2, §32).** Nada gerado
   por IA alimenta diagnóstico, roteiro/recomendações, revisão, performance,
   estatísticas de conteúdo ou mapa de conteúdo. Placar-IA é informativo e
   experimental, sem validade de prova real — e o resultado deve dizer isso.
3. **Sem FK em `questions` para caderno-IA.** O caderno efêmero vive em
   estrutura própria (nova tabela ou payload versionado — decisão da
   Fase A, com migração Flyway; V22 é a última vigente). Respostas a
   questão-IA nunca entram em `question_attempts` sem marca que as exclua
   de todo o motor educacional; preferir tabela de respostas própria.
4. **Sem PII no prompt.** O prompt leva só formato/disciplina/tópicos
   (AGENTS.md §16). Nenhum dado do aluno sai para o provedor.
5. **Erros honestos, nunca caderno parcial silencioso.** Falha do provedor
   (429/5xx/timeout), validação estrutural reprovada ou questão citando
   figura/passagem → erro em envelope com `traceId`, sem stack trace.
   Redução silenciosa de N questões é proibida.
6. **Segredos só em ambiente** (AGENTS.md §15, §29). Placeholder
   `OPENROUTER_API_KEY` em `.env.example`; chave real fora do Git.
7. Histórico dos roadmaps anteriores:
   `docs/archive/TASKS-fase16-22-concluido.md` (Fases 16–22) e
   `docs/archive/TASKS-programa-eaj-concluido.md` (Programa EAJ, go-live
   VPS 2026-10-10). Não reabrir sem motivo documentado; regressão
   IFRN/EAJ é verificada na Fase E.

---

# FASE A — CONTRATO + MOCK (pré-requisito de tudo)

> Nenhuma chamada real à LLM nesta fase. O produto (endpoint, caderno
> congelado, execução, resultado) nasce testável contra um provedor
> mockado determinístico.

## TASK A.1 — Schema da prova-IA + prompt `ai-prova-v1` versionado

Entregas:

* Schema JSON documentado (ex.: `docs/ai-prova-schema.md`): envelope
  `{format: IFRN|EAJ, editionModel: "IFRN-2026"|"EAJ-2021"|…,
  questions: [{n, discipline, topic, stem, options{A,B,C,D}, answer}]}`,
  com regras: N exato do formato-modelo; disciplinas só as da edição-modelo
  (EAJ-2021 admite CN/CH; demais, só LP/MAT); `topic` restrito ao
  vocabulário vigente (LP/MAT v1.1, CN/CH nos 11 códigos EAJ) ou
  `DESCONHECIDO` — nunca código inventado; sem `explanation`, sem
  `passage`, sem `figure`; enunciados auto-contidos (regex de recusa para
  menções a figura/imagem/gráfico/tabela/Texto N/trecho).
* Prompt `ai-prova-v1` versionado em arquivo (ex.:
  `backend/src/main/resources/ai/prompts/ai-prova-v1.txt` ou
  `docs/ai/prompts/ai-prova-v1.md` — fixar na implementação): com
  estrutura do formato-modelo, 1 exemplo few-shot, proibições explícitas
  (passagem, figura, explanation, conteúdo ofensivo/identificador),
  instrução de JSON estrito sem markdown.
* Validador estrutural no backend (testável sem rede): JSON parseável,
  N exato, posições 1..N sem lacuna, 4 alternativas não-vazias e distintas,
  `answer` ∈ {A,B,C,D}, sem marcadores de figura/passagem, sem
  `explanation`. Reprovação → `422 AI_GENERATION_INVALID` com motivo por
  posição (nunca correção silenciosa pela aplicação).

Critérios:

* Schema + prompt revisados contra este TASKS.md (nenhum campo fora do
  contrato; nenhuma promessa de "gabarito confiável");
* validador com testes: lote válido passa; cada classe de defeito
  (N errado, alternativa ausente/duplicada, resposta fora, menção a
  figura, `explanation` presente) é rejeitada com código honesto;
* `mvn -f backend/pom.xml test` verde.

## TASK A.2 — Modelo de dados do caderno-IA efêmero (migração)

Entregas:

* Migração Flyway (próxima livre após V22): caderno-IA sem FK em
  `questions` — ex.: `ai_generations` (id, user_id, institution,
  edition_model, mode, prompt_version, model, status, caderno JSON ou
  linhas filhas, `frozen_at`) + `ai_generation_questions`
  (generation_id, position, discipline, topic, stem, options JSON,
  `frozen_answer_key`) + respostas próprias
  (`ai_generation_answers`, nunca `question_attempts` sem isolamento;
  se reutilizar `question_attempts`, coluna/marca + testes de exclusão).
* `type` de simulação para IA (ex.: `AI_GENERATED_EDITION`) ou vínculo
  `simulation ↔ ai_generation`; `filter_json` carrega
  `{type, institution, editionModel, mode, promptVersion, model,
  experimental: true}`; gabarito congelado (`frozen_answer_key`) imutável.
* Auditoria da geração (prompt_version, modelo, timestamp, nº de
  tentativas de repair) sem PII.

Critérios:

* `ddl-auto: validate` verde; suíte backend verde;
* prova por teste de que nenhuma linha-IA cai em `questions` /
  `question_options` / `question_sources`;
* rollback documentado (forward-only, padrão do projeto) ou down testado.

## TASK A.3 — `POST /simulations/ai-generated` com provedor mockado

Entregas:

* `POST /api/v1/simulations/ai-generated`
  `{institution! IFRN|EAJ, editionModel? (default: edição mais recente da
  trilha no banco), mode! ESTUDO/PROVA}` → `201` com caderno-IA congelado
  (gabarito oculto em andamento, mesmo contrato de ocultação das TASKs
  5.1/5.3) + `GET` execução / `POST submit|abandon` / `GET result` /
  `GET feedback/{position}` (ESTUDO imediato; PROVA só após encerrar)
  reutilizando o ciclo existente sobre o caderno-IA.
* Provedor mockado determinístico injetável (perfil `dev`/flag): devolve
  prova válida no schema para os 3 modelos (IFRN-40, EAJ-40, EAJ-2021-50).
* Selo experimental em toda resposta: por questão
  (`aiGenerated: true`, nota "gerada por IA — não oficial") e no
  resultado ("placar sobre gabarito gerado por IA — sem validade de
  prova real").
* Erros: `institution` inválida → `400 INVALID_INSTITUTION`;
  `editionModel` inexistente → `404 EDITION_NOT_FOUND` (sem inventar
  estrutura); modo inválido → `400 INVALID_MODE`; tudo em envelope com
  `traceId`, sem stack trace; `401` sem token.

Critérios:

* Fluxo completo contra o mock: criar (3 modelos) → responder → concluir →
  resultado com placar contra o congelado + selo experimental; PROVA oculta
  gabarito em andamento; `mvn test` verde (controller + service do novo
  endpoint);
* OpenAPI (`/v3/api-docs`) lista o novo path com selo experimental no
  schema.

---

# FASE B — ISOLAMENTO E HONESTIDADE (trava as Fases C e D)

## TASK B.1 — Nada-IA contamina o motor educacional

Entregas:

* Exclusões implementadas e testadas: diagnóstico, recomendações/roteiro,
  fila de revisão, performance/overview, estatísticas de conteúdo e
  `content-map` ignoram 100% das respostas/placares-IA (queries com filtro
  ou tabelas separadas — verificado por teste, não por convenção).
* `GET /questions`, catálogo de estudos, simulados por disciplina e por
  edição real nunca listam questão-IA.

Critérios:

* Testes de regressão: conta com N tentativas-IA e 0 oficiais → diagnóstico
  `DESCONHECIDO`, roteiro `PROVISORIO` sem evidência-IA, fila de revisão
  vazia de itens-IA, `GET /questions` sem linhas-IA;
* `rg` (ou teste equivalente) prova que os serviços de
  diagnosis/studyplan/review/performance não leem as tabelas-IA;
* suíte backend verde.

## TASK B.2 — Validação estrutural fail-high + recusa de figura/passagem

Entregas:

* Bateria de fixtures adversárias contra o validador da A.1: N a
  menor/maior, lacuna de numeração, alternativa vazia/duplicada, resposta
  fora de A–D, menção a figura ("observe a imagem", "gráfico abaixo",
  "tabela"), menção a passagem ("Texto 1", "trecho"), `explanation`
  presente, JSON com markdown.
* Cada caso → `422 AI_GENERATION_INVALID` com motivo por posição; nenhum
  caso gera caderno parcial.

Critérios:

* Todos os fixtures rejeitados com código honesto; lote válido passa;
* documentado em `docs/blockers.md` o que o validador **não** cobre
  (correção pedagógica do gabarito — limitação assumida, decisão 4).

---

# FASE C — PROVEDOR REAL (OpenRouter)

> Depende de A + B. A troca mock → real não muda contrato, isolamento nem
> validação (só a fonte dos bytes).

## TASK C.1 — Cliente OpenRouter server-to-server

Entregas:

* Cliente HTTP no backend (pacote próprio, sem regra em controller):
  `OPENROUTER_API_KEY` via env (placeholder em `.env.example`, nunca no
  Git); allowlist de modelos free (fixada contra a documentação vigente na
  implementação; `model` default + override proibido ao cliente);
  timeout configurável (default 60–90s); 1 tentativa síncrona (sem retry
  com custo na v1); `prompt_version` (`ai-prova-v1`) + `model` enviados e
  registrados na auditoria da geração.
* Sem PII no corpo (só formato/disciplina/tópicos/idioma); logs sem prompt
  completo nem chave (só `prompt_version`, `model`, latência, status).
* Erros honestos: timeout/5xx/429 do provedor → `502 AI_PROVIDER_UNAVAILABLE`
  (com `Retry-After` repassado quando houver); resposta não-JSON ou fora do
  schema após validação → `422 AI_GENERATION_INVALID`; sem fallback
  silencioso para mock em produção.

Critérios:

* Testes com servidor HTTP falso: timeout, 429 + Retry-After, 500 e corpo
  inválido mapeados para os envelopes acima; chave nunca aparece em log ou
  resposta (teste de varredura);
* `mvn test` verde; doc `docs/ai-provedor.md` (modelo default, timeout,
  limites da rota free, o que é/desde quando é pago — DESCONHECIDO se não
  confirmado na doc vigente).

## TASK C.2 — Auditoria e limites de custo

Entregas:

* Por geração: `prompt_version`, `model`, latência, status
  (sucesso/inválido/provedor), N questões — sem conteúdo PII; endpoint de
  diagnóstico existente (admin/curadoria) expõe contadores agregados.
* Teto aplicado no serviço: 3 gerações/dia por usuário (janela deslizante
  ou dia UTC — fixar e documentar) → `429 AI_GENERATION_QUOTA` com reset
  informado; 1 geração em andamento por usuário → `409 AI_GENERATION_BUSY`;
  teto de tamanho = N do formato-modelo (40/50), sem override.

Critérios:

* Testes: 3ª geração passa, 4ª no dia → `429`; 2 concorrentes → 1×`409`;
  contadores de auditoria conferem;
* `.env.example` com `OPENROUTER_API_KEY=`, `AI_TIMEOUT_*`,
  `AI_DAILY_QUOTA=3` (placeholders, sem segredo).

---

# FASE D — FRONTEND (área de prova-IA)

> Depende de A (contrato) + B (selos/isolamento). Seguir AGENTS.md §31
> (navegador real quando disponível; senão `node --check` +
> `check_frontend.py` + serve 200 e pendência em `blockers.md`).

## TASK D.1 — Entrada "Gerar prova com IA (experimental)" no hub

Entregas:

* Nova seção no hub do simulado (`simulado.html`): seletor de processo
  existente (IFRN/EAJ, default IFRN), seletor de edição-modelo (só edições
  presentes no banco, rotuladas `ano · processo`), seletor de modo
  (ESTUDO/PROVA), botão de gerar com aviso de tempo ("pode levar até ~2
  min") e de cota ("3 por dia").
* Criação via `POST /simulations/ai-generated`; redireciono para a
  execução (`?id=` + marcador IA, ex.: `?id=<attemptId>&ai=1`).

Critérios:

* Sem sessão → guarda de acesso existente (nunca formulário fantasma);
  `401/400/404/409/422/429/502` viram mensagens honestas com `traceId`;
  `node --check` + `check_frontend.py` + serve 200.

## TASK D.2 — Execução e resultado com selo experimental

Entregas:

* Execução reaproveita os componentes do simulado (cartões por posição,
  responder/alterar, concluir/abandonar com `<dialog>`, ritmo informativo):
  selo "gerada por IA — não oficial" por questão + banner no topo;
  feedback imediato só em ESTUDO; PROVA oculta até encerrar (mesmas regras
  5.2/5.3); sem painel de passagem, sem figura, sem explanation.
* Resultado: placar contra o gabarito-IA congelado + banner "placar sobre
  gabarito gerado por IA — sem validade de prova real" + correção por
  posição; raio-X "o que fazer agora" **desligado** para prova-IA (ele
  sugeriria roteiro a partir de evidência-IA — proibido pela regra 2) ou
  restrito a links neutros sem assunto-IA (decidir na implementação e
  documentar).
* Histórico: execuções-IA listadas com selo experimental (ou seção
  separada — decidir na implementação, sem misturar com oficiais).

Critérios:

* Fluxo E2E contra backend local (mock ou provedor): gerar → responder →
  concluir → resultado com banners; PROVA sem vazamento de gabarito;
  mobile 360px sem overflow; console sem erros inesperados;
  acessibilidade prática (foco, teclado, `role=status/alert` nos mesmos
  padrões do simulado).

---

# FASE E — GUARDS, REGRESSÃO E LANÇAMENTO

## TASK E.1 — Rate-limit, concorrência e segurança

Entregas:

* Rate-limit na borda para o novo endpoint somado à cota de negócio da
  C.2 (rajada curta → `429 RATE_LIMITED` + `Retry-After`, sem stack trace);
  CORS inalterado (restritivo); validação de entrada em todas as bordas;
  `institution`/`editionModel`/`mode` fora do vocabulário → `400`.
* Teste de abuso: rajada acima do teto → `429`; sem token → `401` em
  envelope; IDOR (execução-IA de outro aluno) → `404` sem distinguir.

Critérios:

* Testes de segurança verdes; `mvn test` completo verde;
* nenhum segredo no Git (`git status` + varredura de `OPENROUTER` nos
  arquivos versionados, exceto placeholder do `.env.example`).

## TASK E.2 — Checklist ponta a ponta + regressão IFRN/EAJ

Entregas:

* Fluxo IA: cadastro → gerar (IFRN-40, EAJ-40, EAJ-2021-50) → executar
  ESTUDO e PROVA → resultado com selo → histórico com selo → diagnóstico e
  roteiro **inalterados** (prova do isolamento B.1 em conta real).
* Regressão: fluxo IFRN (240Q) e EAJ (130Q) inalterados; simulados oficiais
  sem selo-IA; `GET /questions` sem linhas-IA.
* Docs: `docs/api-simulados.md` (novo endpoint + envelopes + cota),
  `docs/frontend-simulado.md` (nova seção + selos), `README.md` (recurso
  experimental + limites), `docs/blockers.md` (limitação do gabarito-IA,
  instabilidade conhecida da rota free, MCP se indisponível),
  `.env.example` (novas vars), ERD/doc de auditoria se nova tabela.

Critérios:

* Checklist todo verde com evidências (logs/testes); sem DONE sem evidência;
  pendências restantes em `blockers.md`, nunca silenciadas.

---

# CRITÉRIO GLOBAL DE CONCLUSÃO

O programa Prova-IA só está concluído quando, ALÉM dos fluxos IFRN/EAJ vigentes:

ESTUDANTE
↓
HUB DO SIMULADO → "GERAR PROVA COM IA (EXPERIMENTAL)"
↓
FORMATO IFRN/EAJ + MODO ESTUDO/PROVA
↓
GERAÇÃO (OpenRouter, prompt ai-prova-v1, validada, congelada)
↓
EXECUÇÃO (gabarito oculto em PROVA, selo por questão)
↓
RESULTADO (placar experimental + correção contra o congelado)
↓
HISTÓRICO COM SELO
↓
DIAGNÓSTICO/ROTEIRO/REVISÃO INALTERADOS (isolamento provado)

---

# ORDEM DE PRIORIDADE

1. contrato e mock (A: sem formato conhecido, nada anda);
2. isolamento e honestidade (B: trava C e D);
3. provedor real (C);
4. frontend (D);
5. guards e lançamento (E).

Nunca sacrificar a honestidade do conteúdo (selo experimental + isolamento)
para entregar interface mais rapidamente. Nunca apresentar questão gerada
por IA como questão oficial do IFRN/EAJ; nunca servir figura, passagem ou
explanation gerada por IA nesta v1.
