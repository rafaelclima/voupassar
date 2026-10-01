# Fase 7 — Gamificação ADIADA (decisão do responsável)

> Decisão registrada em 2026-10-01 pelo responsável do projeto: a **Fase 7
> inteira NÃO será implementada agora**. Ela fica para depois, somente quando
> o responsável pedir explicitamente.
>
> Nenhuma task da Fase 7 deve ser iniciada sem pedido explícito. Isso não é
> um bloqueio por falta de informação (não vai para `docs/blockers.md`) —
> é uma decisão de escopo.

## Escopo adiado (TASKS.md)

| Task | Título | Estado |
|---|---|---|
| 7.1 | Pontuação | ADIADA — não iniciar |
| 7.2 | Conquistas | ADIADA — não iniciar |
| 7.3 | Sequência de estudos (streak) | ADIADA — não iniciar |

## Estado atual (nada implementado, nada inventado)

* **Backend:** sem endpoints, sem tabelas ativas e sem regras de pontuação,
  conquistas ou streak. Nenhum número de gamificação existe no banco.
* **Frontend:** as telas que tocam no tema declaram o adiamento em vez de
  inventar dados:
  * `frontend/perfil.html` → `#perfil-achievements` ("Conquistas em
    construção", Fase 7 PENDENTE) e `#perfil-metas` (metas quantitativas
    PENDENTES) — ver `docs/frontend-perfil.md`.
* **Regra vigente:** nenhuma tela pode exibir pontos, selos, níveis de
  gamificação ou streaks enquanto a Fase 7 estiver adiada (AGENTS.md §4 —
  nunca inventar).

## O que fazer quando (e se) for pedida

1. Pedido explícito do responsável é o único gatilho — retomar sem ele
   viola esta decisão.
2. Ao retomar, seguir a ordem do TASKS.md (7.1 → 7.2 → 7.3) e a prioridade
   global (qualidade do dataset e motor educacional antes de gamificação).
3. Respeitar o AGENTS.md §6: gamificação complementa o estudo, jamais domina
   a experiência; sistema de pontuação transparente desde o dia 1.
4. Atualizar este documento (marcar como retomada com data) e as telas que
   hoje exibem PENDENTE.

## Referências

* `TASKS.md` — FASE 7 (fonte das tasks adiadas).
* `AGENTS.md` §6 (princípios de design) e §4 (regra de ouro do conteúdo).
* `docs/frontend-perfil.md` §1 e §3 (tratamento honesto atual).
