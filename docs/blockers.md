# Bloqueios e pendências de curadoria

Registro exigido pelo roteiro de `docs/passagens-estrategia.md` §8 (passo 3)
e por `docs/figuras-estrategia.md`: tudo que ficou **de fora** de um vínculo
ou de uma importação, com o motivo — nunca adivinhar (AGENTS.md §4).

## Passagens 2022 — importado em 2026-10-03 (6 passagens, 29 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(3 Textos + 3 Trechos), mesmo padrão do piloto 2020.

### Questões 2022 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 7 | Enunciado cita frase do Texto 1 **sem o rótulo** ("...mesmo motivo de “Segundo o levantamento...”"). Frase auto-contida no enunciado; questão resolvível sem o texto. Regra §2: sem citação do rótulo = sem vínculo. | Nada a fazer; reavaliar somente se a regra de vínculo for ampliada. |
| 18 | "A expressão na face do Juiz indica" — só resolvível com a charge (Texto 3), mas o enunciado **não cita** "Texto 3". | Fase de figuras (`2022/Q18.webp`) cobre a necessidade visual; vínculo textual pendente de decisão sobre citação implícita. |
| 20 | "É comum aos três textos desta avaliação" — citação genérica, sem número. Validador exige `Texto N`. | Idem Q18: reavaliar se a regra passar a aceitar referência coletiva (vincularia aos 3 textos). |
| 32, 35, 37, 39 | Citam "Figura N" (convite de doação, troncos, árvore/triângulo, lago). Figura é parte do enunciado da própria questão, não texto-base compartilhado. | Fase de figuras (`docs/figuras-estrategia.md`): `2022/Q32.webp`, `Q35.webp`, `Q37.webp`, `Q39.webp`. |
| 34 | Cita "Tabela 1" **e** "Texto 1" → vinculada ao TEXTO-1 (evidência do rótulo). A tabela em si (dados por sexo/problema) é figura da questão. | Tabela transcrita na fase de figuras se necessário. |
| 26–30 | Matemática auto-contida, sem citação de texto/figura/tabela. | Nada a fazer. |

### Validação em navegador real (AGENTS.md §31) — EXECUTADA via headless

Sem handlers MCP (`chrome-devtools`/`playwright`) nesta sessão; validação
feita com Chromium real (`/usr/bin/chromium` + `playwright-core`, scripts em
`/tmp/opencode/pwtest/`, login via UI com usuário descartável já removido).
Amostras 2022 em `estudos.html?ano=2022` e `questao.html?id=48`:
10/10 cards com `details.passage` ("Mostrar texto: Texto 1",
"Mostrar texto: Trecho — questões 4 a 6"); painel expande (corpo com
3052 chars do Texto 1); mobile 360px com overflow 0; zero `pageerror`.
Console: apenas 404 de `GET /api/v1/recommendations/plan` em usuário novo
sem plano (endpoint existe; comportamento fora do escopo desta fase).
Texto confere com o PDF (conferência visual feita na curadoria).

Observação 2026-10-03 (RESOLVIDO): relato de "sem botão em nenhuma questão"
num navegador do responsável, com console limpo, NÃO reproduzido em
navegador limpo. Causa confirmada: JS antigo em cache naquele perfil
(hard refresh resolveu). Código + API + dados estavam corretos.

## Fase 2 — a fazer (uma edição por vez)

- [x] 2022 — concluída (este registro).
- [ ] 2023 — repetir roteiro §8 (suggest → curadoria → check → import → amostra).
- [ ] 2024 — idem.
- [ ] 2025 — idem.
- [ ] 2026 — idem.
- Critério de pronto: `--check` dos 6 anos OK + questões que citam
  `Texto N`/`trecho`/`tabela`/`gráfico` com vínculo + amostra por edição
  validada no navegador.
