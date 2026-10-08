# Perfil — VouPassar (TASK 6.8)

> Página protegida em `frontend/perfil.html` (+ `css/perfil.css` +
> `js/api/perfil.js` + `js/views/perfil.js`). Mostra os 8 itens da TASKS.md
> 6.8 sobre as APIs prontas e testadas (3.6 perfil, 4.1 desempenho, 4.2
> diagnóstico, 4.3/4.4 roteiro só leitura do plano vigente).
> Reutiliza todos os tokens/componentes da TASK 6.1 — nada reinventado.

## 1. Cobertura (TASKS.md) → seções e fontes

| Exigido na TASK 6.8 | Seção | Fonte (backend pronto e testado) |
|---|---|---|
| Dados | `#sec-dados-t → #perfil-dados` + `#perfil-form` (`pf-nome/pf-ano/pf-alvo/pf-objetivo`, `PUT` completo) | `GET /api/v1/profile` + `PUT /api/v1/profile` (TASK 3.6; nome 2–80, opcionais nuláveis, e-mail/senha fora daqui) |
| Estatísticas | `#sec-stats-t → #perfil-stats` (4 cards) + `#perfil-modes` + `#perfil-disciplines` | `GET /api/v1/profile/stats` (TASK 3.6; anuladas fora do aproveitamento, `accuracy` NULL honesto) com fallback de totais via `GET /performance/overview` (TASK 4.1) |
| Evolução | `#sec-evo-t` (seletor `DAY`/`WEEK`/`MONTH` + barras CSS) | `GET /performance/evolution?granularity=` (TASK 4.1; UTC, sem interpolar lacunas) |
| Histórico | `#sec-hist-t → #perfil-history` (+ `#perfil-more`, paginado 20) | `GET /api/v1/profile/history?page=&size=` (TASK 3.6; `answeredAt DESC`, proveniência mínima, link para `questao.html?id=`) |
| Conquistas | `#sec-conq-t → #perfil-achievements` | **Fase 7 PENDENTE** — vazio honesto (nenhum ponto/selo inventado; link para o dashboard) |
| Metas | `#sec-metas-t → #perfil-metas` | Declaradas do perfil (`studyGoal/targetYear/schoolYear`) + progresso do plano vigente (`GET /recommendations/plan`, `404 NO_ACTIVE_PLAN` vira vazio) + próximo item com motivo e CTAs (TASK 18.3); metas quantitativas marcadas PENDENTES (Fase 7) |
| Conteúdos dominados | `#sec-dom-t → #perfil-strengths` | `GET /api/v1/diagnosis` → `strengths` (TASK 4.2; `DOMINADO` ≥ 70% com sinal ≥ 3) |
| Pontos de atenção | `#sec-ate-t → #perfil-weaknesses` | `GET /api/v1/diagnosis` → `weaknesses` (`FRAGIL`) + top 5 `priorities` com `reason` auditável |

## 2. Comportamentos

* **Guarda de auth:** `requireSessionOrGuard(showGuard)` + `renderAuthGuard()`
  (`js/views/auth-shared.js`, TASK 16.3; envolve `restoreSession()` da 6.3).
  Sem sessão → `#perfil-guard` com vazio + `Entrar`
  (`login.html?next=perfil.html`) e `Criar conta`. `401` no meio da
  carga/evolução/histórico → mesmo painel com nota de expiração (o
  `request()` de `js/api/client.js` já tentou `refresh` 1× antes). `Sair`
  (`[data-logout]`) revoga no servidor e volta ao login preservando o `?next=`.
* **Carga paralela:** `Promise.allSettled` — cada seção falha isolada; o
  resto continua. Falhas viram resumo no topo (`#perfil-error`, com `traceId`)
  + erro na seção com botão **"Tentar de novo"** (`renderErrorWithRetry()`,
  TASK 16.3): dados/hero (`retryProfileSection`), stats/modos/disciplinas/
  forças/atenção (`retryStatsSections`), evolução (`retryEvolutionSection`) e
  histórico (`retryHistorySection`) recarregam só a seção, sem reload.
  `404 NO_ACTIVE_PLAN` não é erro: vira vazio nas metas.
* **Edição de dados:** validação cliente (nome 2–80, ano ≤ 40, alvo
  2000–2100, objetivo ≤ 500 com contador) + `PUT` completo; `400
  VALIDATION_ERROR` mapeia `details` por campo (`setFieldError`), demais
  erros viram resumo com `traceId`. Sucesso atualiza dados + metas com toast.
* **Histórico:** página 0-based, 20 itens, `Carregar mais` anexa a próxima;
  `last: true` (ou vazio) esgota e oculta o botão. Cada item linka a questão
  (`questao.html?id=`) para revisão; resultado `Anulada/Acertou/Errou/
  Registrada` (nunca zero inventado).
* **Evolução:** troca de granularidade recarrega só a seção, com loading
  inline e `role=status`.
* **Vazio honesto:** conta nova (`0` tentativas) mostra `—` + notas
  (`DESCONHECIDO, nunca zero inventado`); nenhuma rota futura é linkada
  (conquistas apontam só para dashboard/estudos existentes).
* **Próximo do roteiro (TASK 18.3):** abaixo do progresso, as metas mostram
  o próximo item aberto (`nextStudyOf` compartilhado) com o mesmo motivo
  do painel (`evidenceLine` de `js/components/plan-evidence.js` — reuso,
  sem duplicar lógica) + `Praticar agora` (`?disciplina=&topico=
  &subtopico=&origem=perfil`), `Revisar erros` (fila filtrada por assunto)
  e `Ver exemplo oficial` (quando houver `sampleQuestionIds[]`). O nome do
  assunto vem do catálogo (`GET /api/v1/topics`, falha tolerada — sem ele,
  `Assunto do seu roteiro`, nunca nome inventado). Salvar os dados não
  apaga o bloco (o plano vigente fica em cache na view).

## 3. Honestidade (§4 — nada inventado)

* Anuladas: contam como conteúdo, fora do aproveitamento, com nota explícita
  (regra de pontuação DESCONHECIDA, TASK 1.3 §4).
* Assuntos: classificação derivada, revisão humana PENDENTE (TASK 12.2) —
  nota fixa em `#perfil-notes` + notas vindas da API (`stats.notes`,
  `overview.notes`, `diagnosis.notes`), sem duplicar.
* `accuracy` NULL → `—` (nunca zero). Nível (`INICIAL/EM_DESENVOLVIMENTO/
  CONSOLIDADO/DESCONHECIDO`) descrito como estimativa por limiares.
* Conquistas/pontos/streak: Fase 7 sem regra no backend — seção declara
  `PENDENTE` em vez de exibir selos inventados.
* `2021 ausente` citado nas notas. Sem preço/garantia/milhares (mesmo
  vocabulário proibido da landing).

## 4. A11y, responsivo, Pages

* Semântico (`header/main/nav/section`, `h1` único, `skip-link`), `dl` para
  dados, tabelas com `caption` em região rolável, `progressbar` com
  `aria-valuenow/min/max`, `alert/status/note` nos lugares certos, erros de
  campo com `aria-invalid/describedby`, foco visível e contraste herdados,
  alvos ≥44px, `prefers-reduced-motion` herdado, `noscript` com aviso.
* Mobile-first: 1 coluna; `48rem` → 2 colunas (stats e grades modo/disciplina
  e dominados/atenção, form em 2 colunas); `64rem` → 4 stats. Só tabelas
  rolam internamente.
* Estático puro: caminhos `./` relativos, dinâmica só via `fetch()`,
  `API_BASE_URL` via `<meta>` (build injeta em 10.1). Zero dependências,
  barras de evolução em CSS puro (sem lib de gráficos).

## 5. Verificação

```bash
for f in frontend/js/api/perfil.js frontend/js/views/perfil.js; do node --check "$f"; done
python3 scripts/analysis/check_frontend.py
timeout 20 python3 -m http.server 8899 --directory frontend
# Fluxo ao vivo (backend local): sem sessão → painel de acesso; login →
# perfil com dados, 4 stats, modos, disciplinas, metas, dominados, atenção,
# evolução WEEK, histórico paginado e conquistas PENDENTE; PUT válido atualiza.
```

Critérios 6.8: 8 blocos presentes e alimentados pela API real (conquistas
como vazio honesto da Fase 7); edição de dados com `PUT` funcional;
histórico pagina sem duplicar; guarda de auth com `?next=` seguro; `node
--check` OK; 200 no serve; `check_frontend.py` OK estendido com cobertura
perfil; navegação (Dashboard, Estudos, Questão, Simulado) linka o Perfil.
