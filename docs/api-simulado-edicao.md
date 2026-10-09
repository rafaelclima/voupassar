# API do Simulado Real por Edição — TASK 5.5 + multi-processo E.1

> Selecionar uma edição real e reproduzir a estrutura **daquela edição**:
> caderno integral em ordem original sobre `simulations`
> (`type=REAL_EDITION` + `exam_id`) + `simulation_attempts` (execução) +
> `simulation_questions` (caderno congelado), com respostas no fato imutável
> `question_attempts` (TASK 3.7) e placar calculado no servidor.
>
> Responde via `POST /api/v1/simulations/by-edition`. Retomar, concluir,
> abandonar, resultado e feedback por posição reutilizam o ciclo da TASK 5.1 /
> 5.2 sem endpoint novo; a ocultação do Modo Prova é a da TASK 5.3.
> A discursiva sai só como referência em notas (correção automática
> DESCONHECIDA / fora do MVP).
>
> TASK E.1: edição composta `(institution,year)` — EAJ-2021 (50Q 15/15/12/8),
> EAJ-2022/2025 (40Q 20/20), IFRN (40Q 20/20). Ano sozinho nunca decide
> (EAJ-2022 ≠ IFRN-2022).

## Endpoint (autenticado)

| Rota | Descrição |
|---|---|
| `POST /api/v1/simulations/by-edition` | criar e iniciar: `{editionYear! 2000–2100, mode! ESTUDO/PROVA, institution? IFRN\|EAJ}` → `201` com caderno integral em ordem original (gabarito oculto). `institution` ausente = IFRN (compatibilidade); título e `filter_json` carregam o processo (`Simulado Edição EAJ 2022 — 40 questões [PROVA]`) |

Sem token → `401 {"code":"UNAUTHORIZED",…}` em envelope (secure-by-default,
igual às TASKs 3.2–5.4). Edição inexistente → `404 EDITION_NOT_FOUND`
(`IFRN 2021` aponta EAJ-2021; `EAJ 2023` registra só 2021/2022/2025 — nunca
dados inventados); ano fora de 2000–2100 → `400` (`VALIDATION_ERROR` na borda,
`INVALID_EDITION_YEAR` no serviço); modo malformado → `400`
(`VALIDATION_ERROR` na borda, `INVALID_MODE` no serviço);
`institution` inválida → `400` (`VALIDATION_ERROR` na borda,
`INVALID_INSTITUTION` no serviço); banco divergente da
capa → `409 INCOMPLETE_EDITION` com esperado × importado explícitos (nunca
caderno parcial silencioso). Tudo no envelope `{code, message, details,
traceId, timestamp, path}`, sem stack trace; `traceId` também no header
`X-Trace-Id`.

## Regras de evidência

* **Configuração por edição, nunca universal:** total, divisão LP/MAT/CN/CH,
  duração e discursiva lidos de `exams` (+ `exam_essay_prompts`) daquela
  edição. IFRN: 6 capas com 20 LP + 20 MAT + 1 textual
  (`docs/provas-inventario.md §1`); EAJ: 2021 = 50Q (15/15/12/8, 180 min, sem
  discursiva), 2022/2025 = 40Q (20/20, 180 min, sem discursiva)
  (`docs/provas-inventario-eaj.md`, V19) — o código não hardcoda esses números,
  a validação confronta o banco contra a linha da edição.
* **Fidelidade do caderno:** sem sorteio e sem filtro de dificuldade;
  posições 1..N seguem `source_question_number`; a criação falha com `409`
  quando o total diverge do `objective_count` da capa, quando há lacuna na
  numeração 1..N ou quando a divisão disciplinar importada diverge da capa
  (LP/MAT/CN/CH quando a soma fecha; LP/MAT no legado IFRN).
* **Anuladas nas posições originais:** ao contrário da seleção por disciplina
  (TASK 5.1, que exclui anuladas), aqui elas participam do caderno
  (`frozen_answer_key='X'`) e ficam fora do aproveitamento (`isCorrect` NULL,
  pontuação DESCONHECIDA, TASK 1.3 §4). Q23-2025 (X) participa; Q22/Q39-2025
  excluídas na D.3 falham com `409 INCOMPLETE_EDITION` até retranscrição.
* **Caderno misto:** o cabeçalho da execução/resultado não carrega disciplina
  única (`disciplineCode/Name` NULL); cada posição mantém a sua **+**
  `institution` (EAJ-2022 ≠ IFRN-2022). Título e `filter_json`
  determinísticos: `Simulado Edição {IFRN|EAJ} {ano} — {N} questões [{modo}]` /
  `{"type":"REAL_EDITION","institution":…,"editionYear":…,"mode":…}`.
  Ritmo de referência = duração daquela edição (EAJ 180 min ≈ 3h; IFRN 240 min
  ≈ 4h) — só exibição no frontend, nunca regra de correção.
* **Reuso do ciclo (sem endpoint novo):** respostas via `POST /attempts` com
  `simulationAttemptId` (TASK 3.7, ocultas em PROVA pela TASK 5.3); `GET`
  (retomar, gabarito oculto), `POST /submit` / `POST /abandon` (placar do
  servidor), `GET /result` e `GET /feedback/{position}` (TASK 5.2; em PROVA
  só após encerrar) funcionam sem mudança (posições agora com `institution`).
* **Curadoria:** questões `PENDING/PENDENTE_REVISAO` participam com nota de
  revisão pendente (TASK 12.2) — nunca verdade oficial do IFRN/EAJ.
  Gabarito EAJ = transcrição (`TRANSCRIBED_FROM_MD`, sem PDF oficial —
  `docs/blockers.md`).
* **Fora de escopo (não inventados):** correção da discursiva, tempo limite
  por simulado (coluna inexistente no DDL), embaralhamento de alternativas,
  múltiplos cadernos por edição (ofertas 2023 NÃO CONFIRMADAS — se
  confirmado, vira nova `exam_versions` + documentos, sem mudar a API).

## OpenAPI

Contrato em `GET /v3/api-docs` e UI em `GET /swagger-ui.html` (rotas de
documentação públicas — só schemas, sem PII). Tag `Simulados`; escopo
`/api/v1/**` já configurado (TASK 3.2).

## Verificação (2026-10-01)

* `mvn -f backend/pom.xml test` → **211/211** (199 anteriores + 12 novos:
  `SimulationServiceTest` 10, `SimulationControllerTest` 2).
* Casos cobertos: caderno integral em ordem original com cabeçalho sem
  disciplina única; anulada na posição original com `frozen X`; prompt da
  discursiva em notas; `404 EDITION_NOT_FOUND` (incl. 2021 com motivo de
  ausência); `409 INCOMPLETE_EDITION` (total, lacuna de numeração, divisão
  LP/MAT); `400` (modo/ano); controller `401` sem token, `201` e `400`.
* `docker compose up -d --build app` → `healthy` (sem migração nova: `type`
  `REAL_EDITION` + `exam_id` já existem na V1; valida o mapeamento JPA contra
  o schema real via `ddl-auto: validate`); sem token a rota responde `401` em
  envelope; `/v3/api-docs` lista o path novo (50 no total).
* Fluxo ao vivo (usuário scratch `@exemplo.com`, removido após): registro
  `201` → simulado `Edição 2026 PROVA` `201 REAL_EDITION` (40 posições
  1..40, gabarito oculto) → 3 respostas via `POST /attempts`
  (`simulationAttemptId`) com `isCorrect null` (prova em andamento) → `GET
  /feedback/1` `409 STUDY_FEEDBACK_UNAVAILABLE` → `GET /result` `409
  SIMULATION_NOT_FINISHED` → `POST /submit` `200 SUBMITTED` (total 40,
  answered 3, scored 2, annulled 1, `accuracy 0.5`; pos. 37 com `frozen X`,
  `wasAnnulled true`) → `GET /feedback/37` `200` (anulada revelada após
  encerrar) → `by-edition 2021` `404 EDITION_NOT_FOUND`. Scratch removido
  após (trigger de imutabilidade desligado só para o `DELETE` de limpeza e
  religado em seguida — tentativas 0, 240 questões preservadas; CI usa
  mocks, sem Docker).

## Notas técnicas (para não redescobrir)

* Pacote `br.com.voupassar.simulations` segue o padrão por domínio:
  `controller/` → `service/` → `repository/` (+ `dto/`, `entity/`); nenhuma
  regra em controller (AGENTS.md §19).
* `QuestionRepository.findByEditionYearOrdered` (JPQL `JOIN q.exam`, ordem por
  `sourceQuestionNumber`) é a base do caderno; a validação
  (`validateEditionBoard`: total × capa, 1..N sem lacunas, LP/MAT × capa) usa
  só entidades já carregadas, sem query extra.
* `SimulationService` ganhou `ExamRepository` + `ExamEssayPromptRepository`
  (construtor `@Autowired` + package-private com `Random` injetável;
  `SimulationServiceTest.setup` atualizado em consequência).
* `toAttemptResponse` / `toResultResponse` / `listAttempts` anulam a
  disciplina do cabeçalho quando `type=REAL_EDITION` (caderno misto); as
  notas específicas da edição (edital, estrutura, discursiva, scoring
  DESCONHECIDA) são prefixadas em `createByEdition` sobre as notas genéricas.
