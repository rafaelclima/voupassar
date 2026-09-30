# Inventário das Edições — Provas IFRN (TASK 1.1)

> Fonte de verdade: os 12 PDFs em `data/provas/`. Método: `pdfinfo` + `pdftotext -layout` (2026 normalizado para remover U+200B antes da contagem). Nenhuma classificação pedagógica (assunto/dificuldade) neste documento — isso é TASK 1.4. O que não pôde ser comprovado está marcado como `DESCONHECIDO` / `NÃO CONFIRMADO` / `NECESSITA REVISÃO`.

## 0. Cobertura e ausência

| Edição | Pasta | Status |
|---|---|---|
| 2020 | `data/provas/2020/` | PRESENTE (2 PDFs) |
| 2021 | — | **AUSENTE** — nenhum diretório/arquivo. Jamais preencher com dados inventados |
| 2022 | `data/provas/2022/` | PRESENTE (2 PDFs) |
| 2023 | `data/provas/2023/` | PRESENTE (2 PDFs) |
| 2024 | `data/provas/2024/` | PRESENTE (2 PDFs) |
| 2025 | `data/provas/2025/` | PRESENTE (2 PDFs) |
| 2026 | `data/provas/2026/` | PRESENTE (2 PDFs) |

## 1. Quadro comparativo (o que a capa e as seções declaram)

Todas as capas declaram **naquele documento**: 20 questões de Língua Portuguesa + 20 de Matemática + 1 Produção Textual, duração máxima de 4h. Não transformar em regra universal — cada simulado real deve ser dirigido pela configuração da própria edição.

| Ed. | Edital na capa | Prova (págs/tam.) | Seções internas confirmadas | Objetivas detectadas (`NN.` + texto) | Discursiva | Gabarito no repo |
|---|---|---|---|---|---|---|
| 2020 | 29/2019 | `Prova.pdf` 15 p / 609 KiB | LP 01–20; Mat 21–40; `PROPOSTA DE PRODUÇÃO TEXTUAL` + `RASCUNHO` | 1–40 completas (40/40), ~160 marcadores `A)–D)` | Sim — artigo de opinião, cyberbullying/escola, pseudônimo Ariel da Net | `Gabarito_Final.pdf` 1 p; LP 1–20 / Mat 21–40; **anuladas Q7, Q26** |
| 2022 | 041/2021 | `Prova_Exame_Tecnico_Integrado_2022_XNn8mg4.pdf` 15 p / 765 KiB | LP 01–20; Mat 21–40; proposta + rascunho | 1–40 completas, ~161 `A)–D)` | Sim — artigo, terras indígenas, pseudônimo Potiguar da Silva | `GABARITO_..._2022.pdf` 4 p (FUNCERN); **Preliminar + Definitivo idênticos**, 40 A–D, **sem `X`**; sem rótulo disciplinar no gabarito |
| 2023 | 040/2022 | `Caderno_de_provas.pdf` 14 p / 704 KiB | LP 01–20; Mat 21–40; proposta + rascunho | 1–40 completas, ~161 `A)–D)` | Sim — artigo, Lei de Cotas, pseudônimo Negritude Iorubá | `Gabarito_Final_-_Exame_de_Selecao_2023.pdf` 3 p; págs.1–2 listam ~83 ofertas curso/campus/turno; pág.3 LP 1–20 / Mat 21–40; **anulada Q21**; sem preliminar |
| 2024 | 078/2023 | `Cadernos_de_provas_..._2024_.pdf` 15 p / 559 KiB | LP 01–20; Mat 21–40; proposta + rascunho | 1–40 completas, ~161 `A)–D)` | Sim — artigo, Amazônia, pseudônimo Amazonense da Silva | `Gabarito_Oficial_Definitivo_IFRN_78.pdf` 1 p; LP 1–20 / Mat 21–40; **anulada Q17**; sem preliminar |
| 2025 | 23/2024 | `PROVA_IFRN_-_EXAME_DE_SELEÇÃO_2025.pdf` 14 p / 634 KiB | LP 01–20; Mat 21–40; `PROPOSTA DE PRODUÇÃO TEXTUAL ESCRITA` + rascunho | 1–40 completas, ~164 `A)–D)` | Sim — artigo, celulares nas escolas, pseudônimo O REI DA NET | `Exame_de_Seleção_2024_-_Gabarito_Final.pdf` 1 p (conteúdo diz 2025); LP 1–20 / Mat 21–40; **sem `X`**; sem preliminar |
| 2026 | 48/2025 | `Caderno_de_Provas_-Técnico_Integrado_2026.pdf` 14 p / 1,3 MiB | LP 01–20; Mat 21–40; `PRODUÇÃO TEXTUAL` + `RASCUNHO DA PRODUÇÃO TEXTUAL ESCRITA (DISCURSIVA)` | 1–40 completas (após normalizar U+200B), 168 `A)–D)` | Sim — artigo, Brasil × mudanças climáticas, pseudônimo Amazonino Belém | `GABARITO_EXAME_DE_SELEÇÃO_2026_-_DEFINITIVO.pdf` 1 p, data 04/11/2025; tabela 1–40; **anulada Q37**; sem preliminar |

Pontuação na capa: 2020/2022/2024/2025/2026 registram 100 + 100 + 100 (objetivas + discursiva). 2023: tabela presente mas valores das objetivas **NÃO CONFIRMADOS** na extração (só `Produção Textual (Discursiva) 01 questões 100` legível) — NECESSITA REVISÃO visual. Detalhe literal: a capa de 2020 grafa `Produção Textual Inscrita (Discursiva)` (sic, como extraído); 2025/2026 usam o termo `Produção Textual Escrita`.

## 2. Por edição (transcrições literais mínimas)

### 2020 — Edital 29/2019
- Arquivos: `Prova.pdf` (15 p, author `comperve`, 2019-09-16), `Gabarito_Final.pdf` (1 p, author Jose Everaldo Pereira, 2019-10-18).
- Capa: 2 Folhas de Respostas (objetiva + textual); caderno pode ser levado após o prazo; "20 (vinte) questões de Conhecimentos da Língua Portuguesa, 20 (vinte) questões de Matemática e 01 (uma) Produção Textual".
- Proposta (início literal): "Considerando os textos desta prova e seu conhecimento prévio sobre a temática em foco, escreva um artigo de opinião em que se posicione sobre a seguinte questão: a responsabilidade de resolver o problema do cyberbullying é da escola? Assine seu artigo com o pseudônimo de Ariel da Net."
- Gabarito: `X` em Q7 (LP) e Q26 (Mat). Sem preliminar no repo.
- Pendência: pontuação da discursiva na capa truncada na extração — NECESSITA REVISÃO.

### 2022 — Edital 041/2021
- Arquivos: prova 15 p (author `anna`, 2021-10-29), gabarito 4 p (Chrome/Skia, `inscricoes.funcern.org`, 2022-09-30).
- Capa: mesma estrutura 20+20+1; tabela 100+100+100.
- Proposta: "…polêmica levantada pelos textos sobre a posse das terras indígenas, escreva um artigo de opinião… as terras indígenas devem ser protegidas pelo governo brasileiro? … pseudônimo de Potiguar da Silva." + `ORIENTAÇÕES E CRITÉRIOS DE CORREÇÃO`.
- Gabarito: preliminar e definitivo **idênticos** (conferência regex das 40 linhas `Nº + letra`); nenhuma anulada; gabarito não rotula disciplinas — divisão 1–20/21–40 assumida apenas pelo caderno (confirmar na vinculação TASK 1.3).
- Pendência: verificar fora do repo se houve retificação posterior — NÃO CONFIRMADO.

### 2023 — Edital 40/2022
- Arquivos: caderno 14 p (`anna`, 2022-11-17), gabarito 3 p (Chrome/Skia, 2022-12-14).
- Capa: 20+20+1; tabela de pontos ilegível na extração exceto discursiva.
- Proposta: "…A Lei das Cotas privilegia pretos e pardos em detrimento dos direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades? Assine … Negritude Iorubá."
- Gabarito pág. 3: LP 1–20, Mat 21–40, `X` em Q21. Págs. 1–2: lista de ~83 ofertas (nº 1–83, curso/resolução/campus/turno) — não é gabarito por questão, é lotação de provas por oferta. Sem preliminar.
- Pendências: confirmar pontos das objetivas no PDF renderizado; confirmar se as ~83 ofertas implicam múltiplos cadernos ou caderno único — NÃO CONFIRMADO.

### 2024 — Edital 078/2023
- Arquivos: cadernos 15 p (PDFium, 2023-11-26), gabarito 1 p (Chrome/Skia, 2023-12-05).
- Capa: 20+20+1; 100+100+100; cabeçalho interno grafa `PROEN/RN` (sic).
- Proposta: "…escreva um ARTIGO DE OPINIÃO em que se posicione sobre a seguinte questão: a responsabilidade pela preservação da Amazônia é de todos os países? … pseudônimo Amazonense da Silva."
- Gabarito: LP 1–20 / Mat 21–40, `X` em Q17. Sem preliminar.

### 2025 — Edital 23/2024
- Arquivos: prova 14 p (`Setor Consultoria`, 2024-09-25), gabarito 1 p (Chrome/Skia, 2024-10-31).
- Capa: 20+20+1 `Escrita`; 100+100+100; novidade: "Em nenhum momento será permitido ao candidato levar o Caderno de Provas" + entrega obrigatória do caderno ao fiscal (nas edições 2020–2024 o caderno podia ser levado após 4h).
- Proposta: "…O USO DE CELULARES DEVE SER PROIBIDO NAS ESCOLAS BRASILEIRAS? … pseudônimo O REI DA NET." Cita "item 61, alínea e do Edital nº…" (número do edital na regra de nota zero truncado na extração — NECESSITA REVISÃO).
- Gabarito: LP 1–20 / Mat 21–40, 40 A–D, sem `X`. Sem preliminar.
- **Inconsistência registrada:** arquivo `Exame_de_Seleção_2024_-_Gabarito_Final.pdf` com conteúdo de 2025. Resolver o mapeamento canônico na TASK 1.3 antes de importar (risco de duplicidade).

### 2026 — Edital 48/2025
- Arquivos: caderno 14 p (PDF24, 2025-10-08, 1,3 MiB — maior do dataset), gabarito 1 p (`anna`, 2025-11-03, data interna 04/11/2025).
- Capa: 20+20+1 `Escrita`; 100+100+100; entrega obrigatória do caderno (como 2025); cita "item 61 do Edital nº 48/2025".
- Textos-base: Texto 1 sobre a COP30 em Belém (40 mil visitantes, FGV, 38 obras — conforme extração); Texto 2 NECESSITA REVISÃO de conteúdo na Fase 1.
- Proposta: "…o Brasil deve assumir um papel central no combate às mudanças climáticas? … pseudônimo Amazonino Belém."
- Gabarito: tabela única 1–40 com `X = QUESTÃO ANULADA` em Q37. Sem preliminar.
- Nota técnica: numeração das questões e alternativas usa U+200B após `NN.` — o pipeline de extração (TASK 1.2) deve normalizar esse caractere, senão a prova "parece" ter 0 questões.

## 3. Diferenças relevantes entre edições (só o observado)

1. Terminologia da discursiva: `Produção Textual` (2020–2024) → `Produção Textual Escrita` (2025–2026); 2020 grafa `Inscrita` (sic).
2. Guarda do caderno: levável após 4h/prazo (2020–2024) → entrega obrigatória, vedado levar (2025–2026).
3. Gabarito 2022 é o único com preliminar (via FUNCERN) e o único sem rótulos disciplinares; gabarito 2023 é o único que embute a lista de ofertas.
4. Anuladas por edição: 2020 {7, 26}; 2022 {}; 2023 {21}; 2024 {17}; 2025 {}; 2026 {37} — características de cada edição, nunca regra geral.
5. Tamanho: 2026-prova (1,3 MiB) é ~2× as demais, compatível com textos-base longos e formatação com U+200B.

## 4. Pendências para TASK 1.2/1.3 (não bloquear 1.1)

- [ ] Conferência visual: tabela de pontos 2023; pontuação discursiva 2020; número do edital na regra de nota zero 2025.
- [ ] Mapeamento canônico do gabarito 2025 (nome × conteúdo).
- [ ] Normalização U+200B no extrator (2026).
- [ ] Figuras/gráficos/fórmulas (ex.: bola 2023, TV 2024) exigem registro de página + alternativa textual — `pdftotext` não basta.
- [ ] Nenhum enunciado/alternativa/gabarito foi copiado para este documento além dos trechos mínimos de identificação acima; a extração integral é objeto da TASK 1.2 em `data/extracted/` com auditoria.
