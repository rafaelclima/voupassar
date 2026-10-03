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

## Passagens 2023 — importado em 2026-10-03 (4 passagens, 24 vínculos)

Decisão: escopo = textos-base compartilhados da abertura do caderno
(2 Textos + 2 Trechos), mesmo padrão do piloto 2020 e de 2022.

Vínculos (todos com evidência no enunciado, `extract_passages.py --check` OK):
TEXTO-1 (artigo "Dez anos da Lei das Cotas", p. 2 → Q1, Q2, Q3, Q4, Q5,
Q16, Q33 + Q20 duplo); TEXTO-2 (charge da bola/"cota", p. 3, descrição
curada → Q17, Q18, Q19, Q23, Q39 + Q20 duplo); TRECHO-Q06-12 (quadro
emoldurado p. 4 → Q6–Q12); TRECHO-Q13-15 (quadro emoldurado p. 6 →
Q13–Q15). Q20 ("Os Textos 1 e 2") com vínculo duplo (padrão 2020 Q20).
Q23/Q39 citam o Texto 2 e a Figura 1: vínculo só com o texto; a figura
vai para a fase de figuras.

### Questões 2023 SEM vínculo com passagem (decisão consciente)

| Q | Motivo | Destino |
|---|--------|---------|
| 21, 22, 26, 27, 30–32, 34–38, 40 | Matemática auto-contida, sem citação de texto/trecho/figura. | Nada a fazer. |
| 24, 25 | Figura 1 (pentágono/hexágono lado 5 cm, p. 8) e Figura 2 (organograma de vagas, p. 9). Q25 não cita a figura no enunciado (o cabeçalho "Observe, na Figura 2..." vazou para dentro da alternativa D da Q24 em `data/extracted/2023.json` — contaminação de extração a corrigir fora desta fase, sem impacto nos vínculos). | Fase de figuras (`docs/figuras-estrategia.md`): `2023/Q23-24-fig1.webp` (a Q23 também usa a Figura 1, mas já vinculada ao Texto 2), `2023/Q25-fig2.webp`. |
| 28, 29 | Gráfico 1 (reserva de vagas por faixa etária, p. 10; cabeçalho "Considere o Gráfico 1..."). Validador só aceita rótulo `Texto N`/intervalo de trecho: gráfico é figura por questão. | Fase de figuras: `2023/Q28-29-graf1.webp`. |

### Validação em navegador real (AGENTS.md §31) — EXECUTADA via headless

Sem handlers MCP (`chrome-devtools`/`playwright`) nesta sessão; validação
feita com Chromium real (`/usr/bin/chromium` + `playwright-core`, scripts em
`/tmp/opencode/pwtest/fase2-2023.cjs`, login via UI com usuário descartável
já removido). Amostras 2023 em `estudos.html?ano=2023` e
`questao.html?id=81/97/86/100`: painel expande (Texto 1 com 3498 chars,
charge como "Mostrar imagem: Texto 2", trecho Q06-12, Q20 com 2 painéis);
mobile 360px com overflow 0 em estudos e questão; zero `pageerror`.
Console: apenas 404 de `GET /api/v1/recommendations/plan` em usuário novo
sem plano (endpoint existe; comportamento fora do escopo, igual a 2022).
Texto confere com o PDF (conferência visual na curadoria + diff
programático contra `pdftotext -layout`: únicas diferenças = artefatos de
normalização de hífen de fim de linha).

### Ajustes fora da curadoria exigidos por esta edição (documentados)

1. `scripts/db/extract_passages.py` (`trecho_questions`): o caderno 2023
   redige "questões 13 a 15" (intervalo sem "de"; o 2020 usa "de X a Y").
   Regex estendido para `(?:de\s+)?N\s+a\s+M` — comportamento idêntico
   para 2020/2022 (verificado: `--check` OK nos 3 anos + casos de prova).
2. `frontend/css/components.css` (`.passage__credit`): `overflow-wrap:
   anywhere` (padrão já usado em 8+ regras do DS). Causa: a URL de 100
   chars do `source_note` do Texto 1 2023 estourava o grid do card em
   360px (overflow 182 só com o painel aberto; 2022 = 0). Dado preservado
   literalmente; fix de 1 linha validado em 2022 + 2023.

## Fase 2 — a fazer (uma edição por vez)

- [x] 2022 — concluída (este registro).
- [x] 2023 — concluída (este registro).
- [ ] 2024 — repetir roteiro §8 (suggest → curadoria → check → import → amostra).
- [ ] 2025 — idem.
- [ ] 2026 — idem.
- [ ] 2024 — idem.
- [ ] 2025 — idem.
- [ ] 2026 — idem.
- Critério de pronto: `--check` dos 6 anos OK + questões que citam
  `Texto N`/`trecho`/`tabela`/`gráfico` com vínculo + amostra por edição
  validada no navegador.
