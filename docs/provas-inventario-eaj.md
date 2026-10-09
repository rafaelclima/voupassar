# Inventário das Edições — Provas EAJ/UFRN (TASK A.2)

> Fonte de verdade para auditoria: os 3 PDFs em `data/provas/EAJ/`.
> Fonte de extração (caminho crítico): os 3 `questoes.md` em
> `data/provas/EAJ/<ano>/` (TASKS.md regra 1 — nenhum `pdftotext`/OCR no
> caminho crítico; PDF serve SOMENTE como referência/auditoria).
> Método deste inventário: `pdfinfo` + `pdftotext -layout` (capas pp. 1–2 e
> full-scan por palavra-chave) + `sha256sum` + leitura dos `.md`.
> Sem classificação pedagógica (assunto/dificuldade) neste documento.
> O que não pôde ser comprovado está marcado como `DESCONHECIDO` /
> `NÃO CONFIRMADO` / `NECESSITA REVISÃO` (AGENTS.md §4).
>
> AVISO DE PROVENIÊNCIA: as respostas nos `questoes.md` são gabarito
> TRANSCRITO pela curadoria (decisão do responsável 2026-10-09), NÃO
> documento oficial. Não há PDF de gabarito EAJ no repo (ver
> `docs/blockers.md`). Nunca apresentar a transcrição como "gabarito oficial
> confirmado por PDF" (TASKS.md regra 2).
>
> NAMESPACE: tudo que é chaveado por ano leva `institution` junto
> (`EAJ-2022` ≠ `IFRN-2022`, `EAJ-2025` ≠ `IFRN-2025`). A estrutura de cada
> edição vale SÓ para ela (AGENTS.md §10) — 2021 (50Q/4 áreas) não é molde
> para 2022/2025 (40Q/2 áreas) e vice-versa, nem para o IFRN.

## 0. Cobertura

| Edição | Pasta | Status |
|---|---|---|
| 2021 | `data/provas/EAJ/2021/` | PRESENTE (1 PDF caderno + 1 `.md` fonte + 13 PNGs) |
| 2022 | `data/provas/EAJ/2022/` | PRESENTE (1 PDF caderno + 1 `.md` fonte + 12 PNGs) |
| 2023 | — | **AUSENTE** — nenhuma edição EAJ-2023 no dataset (não interpolar; nunca zeros inventados) |
| 2024 | — | **AUSENTE** — nenhuma edição EAJ-2024 no dataset |
| 2025 | `data/provas/EAJ/2025/` | PRESENTE (1 PDF caderno + 1 `.md` fonte + 8 PNGs) |
| 2026 | — | **AUSENTE** — nenhuma edição EAJ-2026 no dataset |

Total EAJ observado: **130 questões** (50 + 40 + 40) via `.md` (fonte),
**~33 PNGs** de figuras. Nenhum PDF de gabarito oficial EAJ no repo.

## 1. Quadro comparativo (o que a capa de cada caderno declara NAQUELE documento)

| Ed. | Caderno (págs/tam./SHA) | Distribuição declarada na capa | Duração na capa | Discursiva | Gabarito oficial no repo | `.md` fonte (SHA) |
|---|---|---|---|---|---|---|
| 2021 | `eaj_2021.pdf` 19 p / 1250991 bytes / `a8695775…81fc` | 50 objetivas: 01–15 Português, 16–30 Matemática, 31–42 Ciências da Natureza, 43–50 Ciências Humanas | 3h ("Você dispõe de, no máximo, três horas…") | NÃO DETECTADA (ver §3) | AUSENTE — só transcrição no `.md` | `questoes.md` 437 linhas / `8e9272c6…47c3a17` — formato `**NN.** + Gabarito: X` + Textos Base |
| 2022 | `eaj_2022.pdf` 17 p / 1373149 bytes / `1674e89c…bff581` | 40 objetivas: 01–20 Português; 21–40 Matemática | 3h (mesma frase literal) | NÃO DETECTADA (ver §3) | AUSENTE — só transcrição no `.md` | `questoes.md` 433 linhas / `4398b1e6…7311c` — formato `Questão NN/Enunciado/Alternativas/Resposta` + Texto 01 |
| 2025 | `eaj_2025.pdf` 15 p / 502455 bytes / `8bfaef8a…86edb42` | 40 objetivas: 01–20 Língua Portuguesa; 21–40 Matemática | 3h (mesma frase literal) | NÃO DETECTADA (ver §3) | AUSENTE — só transcrição no `.md` (o `.md` traz tabela compilada, que é transcrição, não PDF oficial) | `questoes.md` 476 linhas / `ba0157ad…303b5` — formato `**NN.` + `Resposta Correta` + tabela compilada + Textos 1–3 |

Detalhe de produção dos PDFs (`pdfinfo`): 2021 `Author: comperve`,
`Creator: Microsoft® Word 2013`, `CreationDate: 2021-05-12`, 19 p A4;
2022 `Creator: Chromium`, `Producer: pdf-lib`, `CreationDate: 2022-01-17`
(sem `Author`), 17 p A4; 2025 `Author: comperve`,
`Creator: Microsoft® Word para Microsoft 365`, `CreationDate: 2024-12-11`,
15 p A4. Metadados são do arquivo, não fato sobre a prova.

## 2. Por edição (transcrições literais mínimas + auditoria)

### 2021 — Seleção 2021 (EAJ/UFRN, banca Comperve)

- Arquivos: `data/provas/EAJ/2021/eaj_2021.pdf`
  (SHA `a86957757c3d48e76c75e505fcd6553193dcc9312a33a48e398ac119c96481fc`,
  19 p, 1250991 bytes); `questoes.md`
  (SHA `8e9272c6c8fb5f6b696b92c4414ecd29fd31a9ae357c808cb9583942a47c3a17`,
  437 linhas); 13 PNGs (nomes livres, ver lista abaixo).
- Capa (`pdftotext -layout -f 1 -l 1`, transcrição literal parcial):
  "Este Caderno contém cinquenta questões de múltipla escolha, assim
  distribuídas: 01 a 15 — Português, 16 a 30 — Matemática, 31 a 42 —
  Ciências da Natureza, 43 a 50 — Ciências Humanas."
  "Cada questão de múltipla escolha apresenta quatro opções de resposta,
  das quais apenas uma é correta."
  "Você dispõe de, no máximo, três horas para responder às questões e
  preencher a Folha de Respostas."
  Rodapé: "UFRN — Escola Agrícola de Jundiaí — Seleção 2021".
  Menção à banca: "A Comperve recomenda o uso de caneta esferográfica…".
- Edital na capa: **NÃO CONFIRMADO** — nenhum número de edital localizado
  no texto extraído da capa (pp. 1–2) nem no full-scan (`Edital` = 0
  ocorrências). Banca Comperve confirmada só como organizadora citada na
  capa; número do edital = `DESCONHECIDO`.
- Discursiva: **ausente nesta edição conforme o observado** — full-scan do
  PDF sem `Produção Textual`/`Redação`/`Discursiva`/`dissert`; o caderno
  termina na Q50 (última página com Q49–Q50). Vale só para EAJ-2021.
- Textos-base: `.md` linhas 1–54 transcreve TEXTO 01 (ultraprocessados —
  Monteiro/Jaime, Guia Alimentar), TEXTO 02 (tirinha Calvin e Haroldo,
  4 quadrinhos descritos), TEXTO 03 (infográfico IBGE Censo Agro 2017 RN).
  PDF confirma `Texto 01/02/03` + `TEXTO` nas mesmas posições
  (full-scan §auditoria). Sem juízo pedagógico aqui.
- Figuras: 13 PNGs nomes livres:
  `Q11_Q12_Q13_tirinha.png`, `Q14_Q15_censo.png`, `Q18_tipobanana.png`,
  `Q21_nutricaoatleta.png`, `Q22_grafico.png`, `Q27_consumoalimento.png`,
  `Q28_triangulo.png`, `Q34_afirmativas.png`, `Q40_esquematransporte.png`,
  `Q43_figura.png` (Portinari Retirantes), `Q44_fragmentotextual.png`,
  `Q46_imagem.png` (Theodor de Bry), `Q47_Q48_mapa.png`.
  Convenção namespaced (`EAJ-2021-N`) é objeto da Fase B.3, não deste
  inventário.
- Gabarito: **nenhum PDF oficial no repo** (só `**Gabarito: X**` inline no
  `.md`, 50/50). Proveniência = `TRANSCRIBED_FROM_MD` (decisão 2026-10-09).

### 2022 — Seleção 2022 (EAJ/UFRN, banca Comperve citada)

- Arquivos: `data/provas/EAJ/2022/eaj_2022.pdf`
  (SHA `1674e89c210cbcd315ff19abe4eb69bad00463b9f47f9874f30013a4b9bff581`,
  17 p, 1373149 bytes); `questoes.md`
  (SHA `4398b1e68f8abe3a5f0569cdca86d82744556f34e9058650fd7df73a5607311c`,
  433 linhas); 12 PNGs (nomes livres/agrupados, ver lista abaixo).
- Capa (literal parcial): "Este Caderno contém quarenta questões de
  múltipla escolha, assim distribuídas: 01 a 20 — Português; e 21 a 40 —
  Matemática." Mesmas frases de 4 opções/uma correta e de três horas.
  Rodapé: "UFRN — Escola Agrícola de Jundiaí — Seleção 2022".
  Menção "A Comperve recomenda…" presente.
- Edital na capa: **NÃO CONFIRMADO / DESCONHECIDO** (mesmo método: 0
  ocorrências de `Edital` no full-scan). **Nunca reutilizar número de
  edital do IFRN-2022 (041/2021, FUNCERN) para o EAJ-2022** — processos
  distintos, bancas distintas.
- Discursiva: **ausente nesta edição conforme o observado** (mesmo método;
  caderno termina na Q40). Vale só para EAJ-2022.
- Textos-base: `.md` linhas 1–16 transcreve o Texto 01 (pesquisa musical
  Hello Research, Vítor Paiva). Textos 02 e 03 são **citados** nas questões
  (Q10–Q14 -> Texto 02; Q15–Q20 -> Texto 03) mas **não transcritos em bloco**
  no `.md` — conteúdo visual coberto pelos PNGs agrupados; transcrição =
  `NÃO CONFIRMADA` até a Fase B.4 (registrar SEM vínculo por falta de bloco
  transcrito, nunca por inferência).
- Figuras: 12 PNGs:
  `Q08_Q09.png`, `Q10_Q11_Q12_Q13_Q14_Q38_Q39.png`,
  `Q15_Q16_Q17_Q18_Q19_Q20_Q40.png`, `Q21.png`, `Q23.png`, `Q24.png`,
  `Q27_02.png`, `Q27.png`, `Q28.png`, `Q33.png`, `Q34.png`, `Q38.png`.
  Nomes agrupados indicam multi-questão; desmembramento namespaced é Fase B.3.
- Gabarito: **nenhum PDF oficial no repo** (só `**Resposta:** X` inline no
  `.md`, 40/40). Proveniência = `TRANSCRIBED_FROM_MD`.
  **Achado NÃO CONFIRMADO (2026-10-09):** as 40 respostas transcritas são
  todas `A` (distribuição degenerada 40/40 — implausível para prova real;
  possível erro/placeholder de transcrição). Preservado sem alteração;
  aviso no validador; conferência pendente do gabarito oficial (Fase G).
  Ver `docs/blockers.md`.

### 2025 — Seleção 2025 (EAJ/UFRN, banca Comperve)

- Arquivos: `data/provas/EAJ/2025/eaj_2025.pdf`
  (SHA `8bfaef8a188add78613b3dbe1a3d183b86d2a11d87172090c01a9e89986edb42`,
  15 p, 502455 bytes); `questoes.md`
  (SHA `ba0157ad62a6c3e0f743f52549acd0221bb74c5f1cca41220045286a07c303b5`,
  476 linhas); 8 PNGs agrupados (ver lista abaixo).
- Capa (literal parcial): "Este Caderno contém quarenta questões de
  múltipla escolha, assim distribuídas: 01 a 20 — Língua Portuguesa; e
  21 a 40 — Matemática." Mesmas frases de 4 opções/uma correta e de três
  horas. Rodapé: "UFRN — EAJ — Seleção 2025".
  Menção "A Comperve recomenda…" presente.
- Edital na capa: **NÃO CONFIRMADO / DESCONHECIDO** (0 ocorrências de
  `Edital`). **Nunca reutilizar o edital do IFRN-2025 (23/2024) para o
  EAJ-2025** — processos distintos.
- Discursiva: **ausente nesta edição conforme o observado** (mesmo método;
  caderno termina na Q40). Vale só para EAJ-2025.
- Textos-base: `.md` linhas 5–23 transcreve o Texto 1 (IA na sociedade,
  Margareth Artur/revista USP, com nota de rodapé Sichman); linhas 144–148
  transcrevem o Texto 2 (charge IA: "VOCÊ SE PREOCUPA…" / "RETROCESSO DA
  INTELIGÊNCIA NATURAL"); linhas 209–213 trazem o Texto 3 (queimadas,
  MapBiomas 1985–2020) + linhas 317–318 o fragmento para Q30–Q31
  (desmatamento +22,3% / 2,05 Mha). PDF confirma `Texto 1/2/3`.
- Figuras: 8 PNGs agrupados:
  `Q09_Q10.png`, `Q14_Q15_Q16_Q17_Q18_Q19_Q20.png`, `Q21_Q22_Q23.png`,
  `Q30_Q31.png`, `Q34.png`, `Q36.png`, `Q37.png`, `Q38.png`.
- Gabarito: **nenhum PDF oficial no repo**. O `.md` traz `Resposta Correta`
  inline (Q01–Q22, Q24–Q40) + tabela compilada linhas 453–476 — AMBOS são
  transcrição, não documento oficial. Casos especiais transcritos (viram
  regras nas Fases B–D, sem juízo aqui):
  Q23 = **NULA** declarada no `.md` (linha 243; tabela linha 459);
  Q22 traz nota "OCR agrupou alternativas" (linha 226);
  Q39 traz nota de razão interpolada `[1/2]` com incerteza (linhas 422–423).

## 3. Método da verificação "sem discursiva" (auditoria, não extração)

Full-scan `pdftotext -layout` dos 3 PDFs (2026-10-09): 0 ocorrências para
`Produção`/`PRODUÇÃO`, `Redação`, `Discursiva`, `dissert`/`Dissert` em
2021 (39936 chars), 2022 (29449 chars) e 2025 (28914 chars); `Comperve` = 1
ocorrência em cada (frase da caneta); `três horas` = literal nas 3 capas;
caudas dos PDFs terminam em Q50 (2021) / Q40 (2022/2025). Ausência de
seção discursiva = **observado nos cadernos**, não regra geral (o IFRN tem
discursiva em todas as edições — `docs/provas-inventario.md` §1).

## 4. Diferenças relevantes entre as edições EAJ (só o observado)

1. Tamanho: 2021 = 50Q em 4 áreas (LP 01–15, MAT 16–30, CN 31–42, CH 43–50);
   2022/2025 = 40Q em 2 áreas (LP 01–20, MAT 21–40). **Descrever 2021 sem
   forçar o molde 40Q/2 áreas.**
2. Nomenclatura da área: 2021/2022 grafam `Português`; 2025 grafa
   `Língua Portuguesa` na capa (variação literal, sem efeito de conteúdo).
3. Rodapé: 2021/2022 "UFRN — Escola Agrícola de Jundiaí — Seleção YYYY";
   2025 abrevia "UFRN — EAJ — Seleção 2025".
4. Lema do rodapé: 2021 "E saciarei a fome do necessitado…"
   (José Bezerra Gomes); 2022 "Clara a manhã de ouro…" (Palmyra Wanderley);
   2025 "Eu sou de uma terra que o povo padece…" (Patativa do Assaré).
5. Formato dos `.md` difere por edição (normalização = TASK A.3, só
   estrutura): 2021 `**NN.** + Gabarito: X`; 2022
   `Questão NN/Enunciado/Alternativas/Resposta + Análise`; 2025
   `**NN. + Resposta Correta + Análise + tabela compilada`.
6. Anulada transcrita: só 2025 Q23 (NULA declarada no `.md`); 2021/2022 sem
   NULA transcrita — **característica de cada edição, nunca regra geral**.
7. Colisão de anos com o IFRN: EAJ-2022 ≠ IFRN-2022 (041/2021, FUNCERN,
   20+20+discursiva) e EAJ-2025 ≠ IFRN-2025 (23/2024, 20+20+discursiva).
   Ano sozinho nunca identifica a edição.

## 5. Pendências para as Fases B–D (não bloqueiam A.2)

- [ ] Anexar PDFs de gabarito oficiais EAJ quando existirem (ver
  `docs/blockers.md` — pendência registrada, não bloqueio; Fase G).
  Prioridade: EAJ-2022 (40/40 `A` transcritos — NÃO CONFIRMADO).
- [ ] Conferência visual no PDF renderizado antes de importar: 2025 Q22
  (alternativas agrupadas no OCR) e Q39 (razão `[1/2]` interpolada) —
  trava explícita da D.3 (`NEEDS_VISUAL_CHECK`, nunca falha do validador).
- [ ] Q23-2025 NULA: importar como anulada com motivo transcrito
  (fora da pontuação, com nota visível — critério global).
- [ ] Textos 02/03 de 2022 sem bloco transcrito no `.md`: decidir na B.4
  (passagens) o que é figura vs. passagem, com `NEEDS_VISUAL_CHECK` onde
  couber; registrar SEM vínculo em `blockers.md` como nas edições IFRN.
- [ ] Páginas por questão (`page_start/end`): `DESCONHECIDAS` até a Fase B.1
  (nunca inventar página; `pageStatus: DESCONHECIDO` quando não identificável).
- [ ] Pontuação/regra de pontuação EAJ (`scoring_rule`): `DESCONHECIDA`
  (capa não declara pontos por área; não importar a tabela 100+100+100 do IFRN).
- [ ] Nenhum enunciado/alternativa/gabarito foi copiado para este documento
  além dos trechos mínimos de identificação acima; a evidência
  máquina-legível é objeto da Fase B em `data/extracted/eaj/` com auditoria.
