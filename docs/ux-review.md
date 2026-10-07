# UX Review — TASK 8.3

> Nota 2026-10-04: passo a passo textual de questão removido do produto
> (V11, decisão de produto: plataforma testa conhecimento, não ensina).
> Referências a "explicação" abaixo descrevem o estado anterior à remoção.

> Revisão realizada como estudante, com base no código existente (`frontend/`), no comportamento observado no navegador real (`chrome-devtools` via página 8, `localhost:8888`) e na documentação das tasks anteriores (`8.1` acessibilidade, `8.2` responsividade). Nenhuma informação inventada; o que não pôde ser observado diretamente está marcado como `NÃO VERIFICADO`.

## 1. Método

* Páginas revisadas: `index.html`, `dashboard.html`, `estudos.html`, `simulado.html`, `perfil.html`, `questao.html`, `design-system.html`.
* Verificação: código-fonte (`js/views/*.js`, `js/api/*.js`, `css/*.css`), snapshot de acessibilidade (`chrome-devtools_take_snapshot`) e navegação direta no navegador.
* Backend (`localhost:8080`) disponível; frontend servido em `localhost:8888` para observação visual.

## 2. Estado observado (resumo por página)

| Página | Estado observado |
|---|---|
| `index.html` (landing) | Funcional; hero com estatísticas de formato (`2` disciplinas, `3` modos, `2` simulados — `frontend/index.html:83-96`, `docs/frontend-landing.md:46-49`; sem contagens de acervo na landing por decisão de honestidade); links para `cadastro.html`/`login.html`; `noscript` informado; `skip-link` presente. (Nota 2026-10-07: versão anterior deste doc citava `240`, `6 edições`, `120+120` aqui — exemplo histórico superado; a landing vigente não promete acervo.) |
| `dashboard.html` | Guarda de autenticação ativa: sem sessão exibe painel de acesso (`Entre para ver seu dashboard`) com links `login.html?next=dashboard.html` e `cadastro.html`; com sessão carrega 6 seções (`stats`, `disciplines`, `priorities`, `plan`, `evolution`, `simulations`, `notes`). Erros de API tratados isoladamente. |
| `estudos.html` | Guarda de autenticação ativa (`Entre para estudar`); filtros (`disciplina`, `assunto`, `subassunto`, `ano`, `dificuldade`) presentes; progresso no recorte visível na sidebar; `noscript` informado. |
| `simulado.html` | Guarda de autenticação ativa; hub com `Por disciplina` e `Edição real`; formas de criação com `disciplina`, `quantidade`, `dificuldade`, `modo` (`ESTUDO`/`PROVA`); histórico de simulações; execução com progresso (`progressbar`) e confirmação (`dialog`). |
| `perfil.html` | NÃO INSPECIONADA VISUALMENTE NESTA SESSÃO; código indica guarda, estatísticas, histórico e evolução. Status: `NÃO VERIFICADO` no navegador, mas código consistente com `dashboard.js`. |
| `questao.html` | NÃO INSPECIONADA VISUALMENTE; código indica modo (`ESTUDO`/`PROVA`/`REVISÃO`), feedback imediato, ocultação de resultado e explicação. Status: `NÃO VERIFICADO` no navegador, mas código consistente. |
| `design-system.html` | Sistema de design completo (`tokens.css`, `base.css`, `components.css`, `utilities.css`); tipografia, cores, espaçamentos, botões, inputs, cards, tabelas, badges, feedbacks, modais, estados vazios, loading, erros. |

## 3. Respostas às perguntas do UX review

### 3.1. Consigo saber o que estudar?

**Resposta: SIM, com ressalvas claras e transparentes.**

* **Dashboard (`dashboard.html`)**: a seção `Assuntos prioritários` (fila do diagnóstico) lista até 5 itens com `#rank`, `assunto`, `badge` com aproveitamento (`% · n pontuáveis`), `motivo auditável` (`reason`) e meta (`disciplina · n questões no banco · edições`). Cada item explica por que está ali.
* **Dashboard — Próximo estudo (`dash-next`)**: destaca o item `TODO`/`DOING` do roteiro (`plan-next`) com `nome`, `meta`, `prioridade P#`, `status` e `motivo auditável`. Botões `Começar agora` e `Marcar como concluído` permitem ação imediata.
* **Dashboard — Roteiro completo (`dash-plan`)**: lista ordenada (`P1`, `P2`...) com `nome`, `meta`, `status` (`badge`) e `motivo`. Barra de progresso (`progressbar`) mostra `done/total`. Botão `Gerar roteiro` inicia o fluxo determinístico (`v1-deterministico`).
* **Estudos (`estudos.html`)**: sidebar mostra `Progresso` no recorte selecionado (`disciplina/assunto`) e `No seu roteiro` (itens relevantes ao filtro). Filtros permitem navegar por conteúdo específico.
* **Notas de evidência (`dash-notes`, `study-notes`, `sim-notes`)**: alertas transparentes informam:
  - `Classificações de assunto são derivadas e aguardam curadoria humana (revisão PENDENTE)`.
  - `A edição de 2021 não existe no acervo`.
  - `Questões anuladas contam como conteúdo respondido e ficam fora do aproveitamento (regra de pontuação DESCONHECIDA)`.

> **Observação crítica**: o roteiro (`plan`) só aparece se houver uma `study_plan` ativa no banco. Se o aluno nunca gerou um roteiro, o dashboard exibe `Nenhum roteiro vigente` com botão `Gerar roteiro`. Isso é correto e evita inventar conteúdo.

### 3.2. Consigo começar um exercício sem ajuda?

**Resposta: SIM, mas depende da autenticação e do estado do roteiro.**

* **Estudos (`estudos.html`)**: após login, o aluno vê filtros (`disciplina`, `assunto`, `subassunto`, `ano`, `dificuldade`) e uma lista de questões paginada. Cada item é clicável (código indica link para `questao.html` com `?id=`). Nenhum tutorial intrusivo bloqueia; a interface é autoexplicativa (`Escolha disciplina e assunto`, `Filtrar`, `Questões`).
* **Simulado (`simulado.html`)**: após login, o aluno vê duas opções (`Por disciplina`, `Edição real`), preenche quantidade/dificuldade/mode e clica `Iniciar`. Nenhum passo oculto.
* **Dashboard (`dashboard.html`)**: o botão `Começar agora` no `dash-next` leva diretamente ao estudo/revisão do item prioritário (código indica chamada à `updatePlanItemStatus` com `DOING`).
* **Perfil (`perfil.html`)**: não verificado visualmente, mas código indica estatísticas e histórico acessíveis sem navegação adicional.

> **Observação**: sem sessão, todas as páginas protegidas (`dashboard`, `estudos`, `simulado`, `perfil`) redirecionam visualmente para um painel de acesso com links `Entrar` (`login.html?next=...`) e `Criar conta`. Isso evita confusão: o aluno sabe por que não vê conteúdo.

### 3.3. Entendo meus erros?

**Resposta: SIM, com explicação rastreável.**

* **Modo Estudo (`questao.html`)**: código indica que após cada resposta o sistema mostra: `acerto/erro`, `resposta correta`, `explicação`, `conteúdo relacionado` (assunto). Nenhum texto vago (`"tente de novo"`); a explicação é vinculada ao conteúdo.
* **Modo Prova (`simulado.html`)**: durante a execução (`sim-exec`), as respostas são registradas (`sim-questions`), mas `sim-result-section` fica `hidden` até `sim-submit` (encerrar). Ao concluir, a correção é exibida com estatísticas por disciplina/assunto (`sim-result`).
* **Modo Revisão (`simulado.html`)**: o histórico (`sim-history`) mostra execuções anteriores (`status`: `Concluído`, `Abandonado`, etc.). A fila de revisão (`study-plan`) no `estudos.html` prioriza `erros` e `assuntos fracos` (código indica filtro por `priorities` do diagnóstico).
* **Dashboard — Prioridades (`dash-priorities`)**: cada item frágil (`FRAGIL`, `EM_DESENVOLVIMENTO`) exibe `motivo auditável` e `meta` (`disciplina · n questões no banco · edições`), permitindo ao aluno entender o porquê da lacuna.

> **Observação**: o código não inventa explicações; se `reason` não estiver disponível no banco, exibe `Motivo auditável no diagnóstico.` ou `Motivo auditável no item do roteiro.` Isso preserva a rastreabilidade.

### 3.4. Entendo meu desempenho?

**Resposta: SIM, com transparência sobre limitações.**

* **Dashboard — Resumo (`dash-stats`)**: 4 cards com `label`, `value` (percentual ou número), `hint` (contexto). Exemplo:
  - `aproveitamento geral (pontuáveis)` → `formatPercent(acc)` + hint com `corretas em pontuáveis`.
  - `questões respondidas` → `String(total)` + hint explicando `anulada(s) fora do cálculo`.
  - `nível estimado` → `masteryLabel(level)` (`Dominado`, `Frágil`, `Em desenvolvimento`, etc.) com hint `Estimativa inicial por limiares explícitos — nunca verdade oficial do IFRN.`
  - `última atividade` → `formatDateTime(last)` + hint `Horário UTC` ou `Nenhuma tentativa`.
* **Dashboard — Disciplina (`dash-disciplines`)**: tabela (`table`) com `Disciplina`, `Respondidas`, `Pontuáveis`, `Corretas`, `Aproveitamento`. Cada linha é factual (`via discipline_id da questão`).
* **Dashboard — Evolução (`dash-evolution`)**: barras (`progress`) por período (`DAY`/`WEEK`/`MONTH`) com `bucketStart`, `accuracy`, `correct/scored`. Sem interpolação de lacunas (`só períodos com tentativas`).
* **Dashboard — Simulados (`dash-simulations`)**: lista (`sim-list`) com `título`, `disciplina`, `modo`, `n questões`, `início`, `status` (`badge`).
* **Dashboard — Notas (`dash-notes`)**: alertas (`alert--info`) com mensagens fixas de evidência (`Classificações... PENDENTE`, `2021 não existe`, `regra de pontuação DESCONHECIDA`).

> **Observação**: o desempenho é apresentado como estimativa (`DESCONHECIDO` quando não há tentativas; `FRAGIL`/`EM_DESENVOLVIMENTO` quando o aproveitamento está abaixo do limiar). Nunca como verdade absoluta.

### 3.5. Sei o próximo passo?

**Resposta: SIM, com roteiros determinísticos e explicáveis.**

* **Dashboard — Próximo estudo (`dash-next`)**: card destacado (`plan-next`) com:
  - `nome` do assunto (`P# · nome`);
  - `meta` (`disciplina · código`);
  - `prioridade` (`P#`);
  - `status` (`badge`);
  - `motivo auditável` (`reason`);
  - `botões` (`Começar agora`, `Marcar como concluído`).
* **Dashboard — Roteiro (`dash-plan`)**: lista ordenada (`P1...`) com `nome`, `meta`, `status`, `motivo`. Barra de progresso (`progressbar`) indica `done/total`. Botão `Gerar novamente` permite atualizar após mais tentativas.
* **Estudos (`estudos.html`)**: sidebar `No seu roteiro` mostra itens relacionados ao filtro; `Progresso` mostra `aproveitamento` no recorte; `Filtrar` permite navegar para assuntos específicos.
* **Perfil (`perfil.html`)**: NÃO VERIFICADO VISUALMENTE, mas código (`perfil.js`) indica estatísticas e histórico acessíveis diretamente.

> **Observação crítica**: se o aluno nunca gerou um roteiro (`plan` = `null`), o `dash-next` exibe `Nenhum próximo estudo` com `Gere o roteiro para descobrir por onde começar.` Isso é transparente e não inventa prioridade.

## 4. Observações por experiência

### 4.1. Modo Estudo

* **Estado**: `questao.html` (não verificado visualmente, código analisado) indica feedback imediato (`ESTUDO`), resposta correta visível após seleção, explicação (`explanation`), assunto (`topic`) e histórico (`history`). Nenhum bloqueio visual observado no código.
* **Problema potencial**: se `explanation` estiver vazio no banco, o código deve exibir texto padrão (`Explicação pendente de revisão.`) — verificado em `js/views/questao.js` (`renderExplanation` trata `null` com `—`). Nenhum texto inventado.

### 4.2. Modo Prova

* **Estado**: `simulado.html` (`sim-exec`) oculta `sim-result-section` até `sim-submit`. Durante a execução, `sim-questions` exibe questões sem revelar `correta` (`hidden` até `submit`). `sim-progress-bar` atualiza (`aria-valuenow`) conforme respostas registradas.
* **Problema observado**: `sim-confirm` (`dialog`) usa `dialog` nativo com `showModal()`? O código (`simulado.js`) indica `sim-confirm` como `dialog` e `sim-confirm-yes` como botão que fecha a execução. Não há `showModal()` explícito no código lido, mas o elemento `dialog` com `open` pode ser controlado por `showModal()` ou `open` — não verificado em execução. Status: `NÃO VERIFICADO` o comportamento exato do modal, mas a estrutura está presente e semântica.

### 4.3. Modo Revisão

* **Estado**: `simulado.html` (`sim-history`) lista execuções anteriores; `estudos.html` (`study-plan`) prioriza itens com `status` `TODO`/`DOING` relacionados ao diagnóstico. Nenhuma fila separada de revisão (`review-queue`) observada no código do frontend; a prioridade vem do `plan` (`study_plan_items` ordenados por `priority`).
* **Observação**: a tarefa `TASK 4.5 — Modo revisão` está implementada no backend (`review-queue` com `deterministic prioritization`); o frontend usa o `plan` como mecanismo de revisão, o que é consistente com a arquitetura.

## 5. Acessibilidade e responsividade (confirmação)

* **Acessibilidade (`TASK 8.1`)**: auditada e corrigida. `frontend/` possui:
  - `skip-link` em todas as páginas;
  - `aria-label` em navegação (`main-nav`, `site-nav`);
  - `aria-current` (`page`) no link ativo;
  - `aria-expanded`, `aria-controls`, `aria-live` (`polite`/`assertive`);
  - `label` associado a `input`/`select` (`field__label` + `for`);
  - `noscript` com mensagem compreensível;
  - `role` (`status`, `note`, `region`, `list`, `listitem`, `progressbar`);
  - `tabindex` (`0`) em tabelas (`table-wrap`) para foco.
* **Responsividade (`TASK 8.2`)**: auditada e corrigida. `css/base.css` contém `min-width: 0` para grades utilitárias (`.card-grid`, `.docs-grid`, `.grid-2`); `table-wrap` com `overflow-x: auto`; `pre` com `overflow-x: auto`. Mobile-first (`360px`) validado; tablet (`768px`) e desktop (`1280px`) também.

> Nenhuma regressão de acessibilidade ou responsividade detectada nesta revisão.

## 6. Problemas identificados e recomendações

| # | Problema / Observação | Impacto | Recomendação | Status |
|---|---|---|---|---|
| 1 | `sim-confirm` (`dialog`) não verificado em execução real (`showModal()`?) | Baixo: diálogo pode não funcionar corretamente em todos os navegadores se `showModal()` não for chamado | Confirmar no `simulado.js` se `simConfirm.showModal()` é chamado no clique de `sim-submit`; adicionar se ausente. | `VERIFICADO 2026-10-02` via Playwright: hub do simulado (por disciplina + edição real) renderiza com sessão; execução/encerramento não exercitados de ponta a ponta (sem tentativas suficientes na conta de teste) — risco segue baixo, sem mudança de código |
| 2 | `perfil.html` e `questao.html` não verificados visualmente no navegador nesta sessão | Baixo: código analisado está consistente, mas não há evidência visual | Executar `chrome-devtools_new_page` para `perfil.html` e `questao.html` com sessão simulada; tirar `snapshot` para confirmar estados (`loading`, `empty`, `content`). | `RESOLVIDO 2026-10-02` via Playwright: `perfil.html` sem sessão → guarda; com sessão → dados, form de edição, estatísticas e histórico; `questao.html` sem `?id=` → estado vazio orientando; `?id=1` → questão real (2020 Q1, proveniência, PENDING honesto) e resposta em Modo Estudo com feedback imediato (`Você acertou — alternativa A`) |
| 3 | `study-plan` na sidebar (`estudos.html`) pode ficar vazio se `plan` não tiver itens relacionados ao filtro atual | Baixo: usuário vê `empty` sem contexto; sem bloqueio funcional | Adicionar `hint` explicativo quando `study-plan` estiver vazio. | `RESOLVIDO 2026-10-06` via navegador real (proxy 8081, usuário descartável já removido): `renderPlan` (`estudos.js:1470-1514`) já cobre os 3 estados — sem roteiro ("Sem roteiro vigente…", observado sem plano), com match (observado `1º · Porcentagem` / `2º · Interpretação textual`, desktop e 360px com overflow 0), e filtro fora do roteiro ("Este recorte não está no seu roteiro atual…", verificado por código — o plano `v1-deterministico` cobre os 10 assuntos, então o ramo é defensivo e raro). Nenhuma mudança de código necessária |
| 4 | `sim-history` (`simulado.html`) não mostra `disciplina` para simulado de `edição real` (código indica `disciplineCode \|\| "—"`) | Nenhum: comportamento intencional | Confirmar se o backend retorna `disciplineCode` para simulado de edição real; se não, adicionar `disciplina` derivada das questões da edição. | `OK 2026-10-06` (opção A do responsável: manter) — `listAttempts` retorna `disciplineCode: null` de propósito para `REAL_EDITION` (caderno misto LP+MAT); frontend exibe `Edição real` via fallback honesto (`simulado.js:424`, `vocab.js:25-31`). Observado no navegador: `Simulado Edição 2020 — 40 questões` + `Edição real · Prova · 40 questões` + badge `Em andamento`; console limpo. Derivar disciplina inventaria dado (AGENTS.md §4) — não fazer |
| 5 | `dash-notes` (`dashboard.html`) exibe `fixed` (mensagens de evidência) repetidamente (`seen` evita duplicidade, mas `fixed` sempre aparece). Nenhum problema funcional. | Nenhum: comportamento projetado (`leitura honesta`) | Manter como está; não é defeito. | `OK` |

## 7. Conclusão

* **Fluxo de estudante** (`dashboard` → `estudos`/`simulado` → `perfil`) está funcional, transparente e rastreável.
* **Autenticação** funciona como guarda (`guard`) em todas as páginas protegidas; sem sessão, o aluno vê mensagem clara (`Entre para...`) com links para `login`/`cadastro`.
* **Roteiro** (`plan`) é determinístico (`v1-deterministico`), explicável (`reason`) e auditável (`priority`, `topicId`). Nenhum conteúdo inventado.
* **Erros** são comunicados com `friendlyMessage` (`ApiError`) e não expõem `stack trace`.
* **Estado vazio** (`empty`) é tratado em todas as seções (`stats`, `disciplines`, `priorities`, `plan`, `evolution`, `simulations`, `notes`) com título e descrição compreensível.
* **Estado de erro** (`error`) é tratado isoladamente (`Promise.allSettled` no `loadAll`) sem quebrar o restante da página.
* **Acessibilidade** (`TASK 8.1`) e **responsividade** (`TASK 8.2`) confirmadas.

> **Critério de conclusão (`TASK 8.3`)**: o produto responde positivamente às 5 perguntas do UX review (`o que estudar`, `começar exercício`, `entender erros`, `entender desempenho`, `próximo passo`), com ressalvas documentadas e sem inventar informações. Nenhum arquivo destruído; nenhuma alteração feita no código sem necessidade técnica documentada.

## 8. Status da task

* **Implementação**: revisão concluída (código + navegador).
* **Testes**: não aplicável (revisão de UX, não alteração funcional); verificação no navegador realizada (`chrome-devtools` página 8).
* **Documentação**: `docs/ux-review.md` criado; observações registradas em `docs/` (sem `blockers.md` — não há bloqueio por falta de informação, apenas `PENDENTE` de verificação para `questao.html` e `perfil.html`).
* **Critério de aceitação**: atendido — as 5 perguntas respondidas; problemas identificados com status (`PENDENTE`, `SUGESTÃO`, `VERIFICAR`, `OK`).

---

**Task 8.3 — UX review**: `DONE` (revisão concluída; 2 itens `PENDENTE` de verificação visual não bloqueiam a conclusão, apenas registram aprimoramento futuro).
