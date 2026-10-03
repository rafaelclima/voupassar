# Mapeamento das Figuras — Curadoria Manual (data/provas/)

Arquivos colocados pelo usuário em `data/provas/<ano>/`. Cada arquivo precisa ser relacionado ao `manifest.json` (`frontend/assets/figures/manifest.json`) e, se aplicável, copiado/renomeado para `frontend/assets/figures/<ano>/` conforme a convenção `Q<NN>.webp` (ou `Q<NN>-k.webp`).

## 2020

| Arquivo (data/provas/) | Conteúdo estimado | Questão(ões) no manifest | Ação necessária |
|---|---|---|---|
| `2020_Q34.webp` | Figura de Q34 (não presente no manifest atual) | Nenhuma (`manifest` não tem `2020-34`) | Verificar se Q34 tem `has_figure` no PDF; se sim, adicionar ao manifest (`2020-34`) e copiar como `frontend/assets/figures/2020/Q34.webp` |
| `2020_Q35_Q36.webp` | Figura compartilhada para Q35 e Q36 | `2020-35`, `2020-36` | Copiar/duplicar: `frontend/assets/figures/2020/Q35.webp` e `Q36.webp` (ou manter um arquivo com referência dupla no manifest) |
| `2020_texto2.webp` | Texto-base compartilhado (Texto 2 — charge) | Não é figura de questão, mas texto-base | Usar com `passage.js` (texto-base), não com `figure.js` |

Observações:
- `manifest` atual: `2020-16`, `17`, `18`, `19`, `20`, `26`, `35`, `36`.
- Arquivos `Q35_Q36.webp` sugerem que Q35 e Q36 compartilham a mesma figura (possivelmente uma charge ou gráfico usado em ambas).
- `Q34.webp` indica uma figura para uma questão que não está registrada no manifest atual; precisa ser verificada no PDF (`Prova.pdf`, páginas relevantes).

## 2022

| Arquivo (data/provas/) | Conteúdo estimado | Questão(ões) no manifest | Ação necessária |
|---|---|---|---|
| `2022_Q32_figura1.webp` | Figura 1 da Q32 | Nenhuma (`manifest` não tem `2022-32`) | Verificar se Q32 tem figura; se sim, adicionar (`2022-32`) |
| `2022_Q34.png` | Figura da Q34 | Nenhuma (`manifest` não tem `2022-34`) | Verificar; converter para `.webp`; adicionar se confirmada |
| `2022_Q35.webp` | Figura da Q35 | Nenhuma (`manifest` não tem `2022-35`) | Adicionar (`2022-35`) se confirmada no PDF |
| `2022_Q37_figura3.webp` | Figura 3 (terceira figura) da Q37 | `2022-37` (manifest tem apenas `files: ["2022/Q37.webp"]`) | Atualizar `manifest`: adicionar `2022/Q37-2.webp` e `2022/Q37-3.webp` (ou renomear para `Q37-3.webp` e manter a primeira) |
| `2022_Q39_figura4.webp` | Figura 4 (quarta figura) da Q39 | `2022-39` (manifest tem apenas `files: ["2022/Q39.webp"]`) | Atualizar `manifest`: adicionar `Q39-2.webp`, `Q39-3.webp`, `Q39-4.webp` |
| `2022_texto2.webp` | Texto-base compartilhado (Texto 2 — charge) | Não é figura de questão | Usar com `passage.js` |
| `2022_texto3.webp` | Texto-base compartilhado (Texto 3 — gráfico) | Não é figura de questão | Usar com `passage.js` |

Observações:
- `manifest` atual para 2022: `2022-15`, `16`, `17`, `18`, `19`, `31`, `37`, `39`.
- Os arquivos `figura3` e `figura4` indicam que Q37 e Q39 têm múltiplas figuras, mas o manifest atual só registra uma (`files` com 1 item). A estratégia (`docs/figuras-estrategia.md` §3) permite `Q<NN>-2.webp`, `Q<NN>-3.webp`, etc.
- `Q32.webp` e `Q34.webp` são questões não registradas no manifest; precisam ser verificadas no `data/extracted/2022.json`.

## 2023

| Arquivo (data/provas/) | Conteúdo estimado | Questão(ões) no manifest | Ação necessária |
|---|---|---|---|
| `2023_figura2.webp` | Figura 2 de 2023 (possivelmente charge do Texto 2) | Nenhuma chave explícita; pode ser `2023-` sem número ou texto-base | Verificar se corresponde a `2023-` (Texto 2) ou a uma questão específica |
| `2023_Q23_Q24_figura1.webp` | Figura 1 compartilhada por Q23 e Q24 | `2023-23`, `2023-24` | Copiar/duplicar para cada questão (`Q23.webp`, `Q24.webp`) ou declarar no manifest com `files` apontando para o mesmo arquivo |
| `2023_Q28_Q29_grafico1.webp` | Gráfico 1 para Q28 e Q29 | `2023-28`, `2023-29` | Mesma situação: duplicar ou referenciar no manifest |
| `2023_texto2.webp` | Texto-base compartilhado (Texto 2 — charge) | Não é figura de questão | Usar com `passage.js` |

Observações:
- `manifest` atual para 2023: `2023-23`, `24`, `25`, `28`, `29`.
- `figura2.webp` pode ser o recorte da charge (Texto 2) que aparece em Q17, Q18, Q19, Q23, Q39 (como citado em `docs/blockers.md`). Se for o caso, precisa ser vinculada às questões correspondentes.

## 2024

| Arquivo (data/provas/) | Conteúdo estimado | Questão(ões) no manifest | Ação necessária |
|---|---|---|---|
| `2024_texto2.webp` | Texto-base compartilhado (Texto 2 — charge) | Não é figura de questão | Usar com `passage.js` |
| `2024_texto3.webp` | Texto-base compartilhado (Texto 3 — gráfico) | Não é figura de questão | Usar com `passage.js`; se for gráfico usado em Q18, Q31, Q32, Q33, pode ser referenciado como figura |

Observações:
- `manifest` atual para 2024: `2024-18`, `31`, `32`, `33`.
- Os arquivos `texto2.webp` e `texto3.webp` são textos-base (não figuras de questão). Mas se `texto3.webp` é o gráfico usado nas questões 31–33, pode ser considerado como figura para essas questões.
- Nenhum arquivo `.webp` específico para Q18, Q31, Q32, Q33 foi colocado pelo usuário; precisa ser verificado se há recortes adicionais nos PDFs.

## 2025

| Arquivo (data/provas/) | Conteúdo estimado | Questão(ões) no manifest | Ação necessária |
|---|---|---|---|
| `2025_Q26.webp` | Figura da Q26 (gráfico de barras) | `2025-26` | Copiar para `frontend/assets/figures/2025/Q26.webp` |
| `2025_Q27.webp` | Figura da Q27 (planta da sala) | `2025-27` | Copiar para `frontend/assets/figures/2025/Q27.webp` |
| `2025_Q40.webp` | Figura da Q40 (não no manifest atual) | Nenhuma (`manifest` não tem `2025-40`) | Verificar no PDF; se Q40 tem figura, adicionar (`2025-40`) |
| `2025_texto2.webp` | Texto-base compartilhado (Texto 2 — charge) | Não é figura de questão | Usar com `passage.js` |

Observações:
- `manifest` atual para 2025: `2025-15`, `16`, `26`, `27`, `33`, `36`.
- `Q26.webp` e `Q27.webp` estão no manifest (`2025-26`, `2025-27`), então a integração é direta.
- `Q40.webp` indica uma figura para uma questão não registrada no manifest; precisa ser verificada.

## 2026

| Arquivo (data/provas/) | Conteúdo estimado | Questão(ões) no manifest | Ação necessária |
|---|---|---|---|
| `2026_Q30_figura1.webp` | Figura 1 da Q30 (triângulo retângulo / figura geométrica) | `2026-30` | Copiar para `frontend/assets/figures/2026/Q30.webp` |
| `2026_texto2.webp` | Texto-base compartilhado (Texto 2 — charge) | Não é figura de questão | Usar com `passage.js` |

Observações:
- `manifest` atual para 2026: `2026-18`, `19`, `20`, `30`, `39`.
- Nenhum arquivo específico para Q18, Q19, Q20, Q39 foi colocado pelo usuário; precisa ser verificado se há recortes adicionais.
- `Q30_figura1.webp` corresponde ao manifest (`2026-30`).

---

## Plano de Entrega (passo a passo)

1. **Verificação de cada arquivo** (manual/automática):
   - Confirmar se cada arquivo corresponde a uma questão com `has_figure=TRUE` no banco (`data/linked/` ou `data/extracted/`).
   - Se o arquivo é uma figura de questão (`Q<NN>`), atualizar o `manifest.json` se ainda não estiver lá.
   - Se o arquivo é um texto-base (`texto2`, `texto3`), vincular ao sistema de passagens (`passage.js`), não ao componente de figuras (`figure.js`).

2. **Renomeação e cópia para `frontend/assets/figures/`**:
   - Para cada arquivo que corresponde a uma chave existente no manifest, copiar para `frontend/assets/figures/<ano>/Q<NN>.webp` (ou `Q<NN>-k.webp` se for múltipla figura).
   - Se o arquivo é compartilhado (ex.: `Q35_Q36.webp`), duplicar o arquivo físico (`Q35.webp` e `Q36.webp`) ou atualizar o `manifest` para apontar para o mesmo arquivo (`files: ["2020/Q35.webp"]` para `2020-35` e `files: ["2020/Q35.webp"]` para `2020-36`, se compartilhado).

3. **Compressor/Redimensionador**:
   - Alguns arquivos estão acima de 300 KB (`2022_texto3.webp`: 752 KB; `2024_texto2.webp`: 410 KB; `2023_texto2.webp`: 179 KB; `2025_Q27.webp`: 373 KB; `2026_texto2.webp`: 306 KB). A estratégia exige `< 300 KB`.
   - Executar `convert -resize 1600x1600> -quality 80` (ou `optipng` / `cwebp`) para reduzir o tamanho.

4. **Atualização do `manifest.json`**:
   - Adicionar chaves faltantes (`2020-34`, `2022-32`, `2022-34`, `2022-35`, `2025-40`, `2026-...` se confirmadas).
   - Atualizar `files` para múltiplas figuras (`Q37-2.webp`, `Q39-2.webp`, etc.) quando aplicável.
   - Atualizar `alt` para cada entrada (sair do template `PENDENTE_REVISAO`).

5. **Sincronização com o banco**:
   - Executar `python3 scripts/db/sync_figures.py --publish <chave>` para cada chave que for marcada como `PUBLICAVEL`.
   - Se ainda em revisão (`PENDENTE_REVISAO`), não executar `--publish`, mas executar `sync_figures.py` (sem `--publish`) para criar `question_figures` com o status atual.

6. **Validação final**:
   - `python3 scripts/analysis/check_figures.py`
   - `python3 scripts/analysis/check_frontend.py`
   - Verificação visual no navegador (`estudos.html` / `questao.html`) para confirmar que as imagens aparecem e o aviso de curadoria está correto.

---

## Próximo Passo Imediato

Confirmar com o usuário:
- Se os arquivos `texto2.webp`, `texto3.webp` são textos-base (vincular a `passage.js`) ou figuras de questões específicas.
- Se `Q34.webp` (2020), `Q32.webp`, `Q34.png`, `Q35.webp` (2022), `Q40.webp` (2025) são figuras de questões que precisam ser adicionadas ao manifest.
- Se devemos prosseguir com a cópia/renomeação dos arquivos físicos e a atualização do `manifest.json`.
