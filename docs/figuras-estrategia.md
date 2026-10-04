# Estratégia de Figuras — Questões com Imagem (PORTUGUÊS + MATEMÁTICA)

> Problema: ~36 questões oficiais dependem de figura/gráfico/charge ausente do
> texto extraído (`has_figure=TRUE`, tag `FIGURA`). Sem a imagem, o aluno não
> consegue responder. Antes desta estratégia, a UI só exibia
> "consulte o PDF-fonte" — sem imagem e sem link acionável.

## 1. Decisão de produto (2026-10-02)

* **Hospedagem: assets no frontend** — recortes otimizados em
  `frontend/assets/figures/`, servidos pelo GitHub Pages (estático).
  A API retorna apenas metadados (caminho relativo + `alt` + página +
  `publicationStatus`); nunca o binário.
* **Curadoria manual:** o responsável recorta as figuras do caderno oficial e
  coloca os arquivos na pasta padronizada abaixo. O frontend casa
  arquivo ↔ questão por convenção + `manifest.json` (sem adivinhar conteúdo).
* **Direitos (AGENTS.md §12):** cada figura carrega proveniência
  (edição, páginas, documento-fonte). A exibição plena é liberada quando
  `publicationStatus = PUBLICAVEL`; enquanto `PENDENTE_REVISAO`, a UI mostra
  o aviso de curadoria + referência de página (nunca imagem inventada pela IA).

## 2. Convenção de nomeação (NÃO quebrar sem migrar o validador)

```
frontend/assets/figures/
  manifest.json
  2020/Q16.webp
  2020/Q17.webp
  ...
  2026/Q18.webp
```

* **Pasta = edição-fonte (ano):** `2020 | 2022 | 2023 | 2024 | 2025 | 2026`.
  Não usar sigla de matéria na pasta/arquivo: a chave natural da questão
  oficial é `(ano, número)` — a disciplina vem da classificação e pode ser
  revisada sem renomear arquivo (ex.: 2024 Q17).
* **Arquivo = `Q<NN>.webp`:** `NN` com 2 dígitos, `01`–`40`, igual ao número
  impresso no caderno (não o `id` do banco). Ex.: questão 7 → `Q07.webp`.
* **Múltiplas figuras na mesma questão:** `Q<NN>-2.webp`, `Q<NN>-3.webp`, …
  (`Q<NN>.webp` é sempre a figura 1). Ordem = ordem de leitura no caderno.
* **Texto-base compartilhado** (ex.: 2020 Texto 2 vale para Q16–Q20):
  duplicar o mesmo recorte em cada `Q<NN>.webp` correspondente **ou**
  declarar uma vez no `manifest.json` apontando vários `Q<NN>` para o mesmo
  arquivo. Duplicar é preferível (resolução direta, sem lógica extra).
* **Formato:** `.webp` (preferido, lado maior ≤ 1600 px, < 300 KB).
  `.png` / `.jpg` são aceitos como fallback e o validador avisa
  (não falha) para converter quando possível.
* **Proibido:** `espaços`, `acentos`, `maiúsculas` fora do `Q`, qualquer
  nome fora de `Q<NN>(-<k>)?.(webp|png|jpg|jpeg)`.

## 3. `manifest.json` (contrato frontend ↔ curadoria ↔ banco)

```json
{
  "version": 1,
  "base": "./assets/figures",
  "figures": {
    "2026-18": {
      "files": ["2026/Q18.webp"],
      "page": 7,
      "alt": "Charge em preto e branco com dois personagens dialogando (ver imagem).",
      "credit": "Fonte: IFRN — Caderno 2026, p. 7 (recorte para estudo).",
      "status": "PENDENTE_REVISAO"
    }
  }
}
```

* Chave = `"<ano>-<numero sem zero>"` (ex.: `"2020-7"`, `"2026-18"`).
* `files`: 1+ caminhos relativos à pasta `figures/`, na ordem de leitura.
* `page`: primeira página do caderno onde a figura aparece (confere com
  `data/extracted/<ano>.json` → `page_start`).
* `alt`: **obrigatório** quando o arquivo existe. Descreve o *tipo* do visual
  sem transcrever valores não conferidos. Template neutro aceito até a
  revisão: `"Figura da questão <N> da prova <ano> (página <p> do caderno) — transcrição pendente de revisão."`
* `credit`: fixo no formato acima (o frontend gera sozinho se ausente).
* `status`: `PENDENTE_REVISAO` (default) → `PUBLICAVEL` após revisão visual
  (confere figura × enunciado × alternativas) — espelha
  `question_figures.publication_status` no banco (migração `V8`).

## 4. Como adicionar uma figura (passo a passo)

1. Abra o caderno em `data/provas/<ano>/` na página indicada pelo
   `manifest.json` (`page`) ou por `data/extracted/<ano>.json`.
2. Recorte **só a figura** (sem enunciado/alternativas de outras questões).
3. Exporte em `.webp` (lado maior ≤ 1600 px) como
   `frontend/assets/figures/<ano>/Q<NN>.webp` (ou `Q<NN>-2.webp`, …).
4. Preencha `alt` da entrada correspondente no `manifest.json`.
5. Rode `python3 scripts/analysis/check_figures.py` — ele valida nome,
   formato, dimensão, `alt`, página e lista as 36 pendências.
6. Publique: quando a revisão visual estiver OK, marque `status` como
   `PUBLICAVEL` no manifest e sincronize o banco com
   `python3 scripts/db/sync_figures.py --publish` (cria `question_figures`
   + espelha o status; idempotente).

## 5. Comportamento da UI (todas as telas de questão)

Módulo único `frontend/js/components/figure.js`:

* Prioridade: `API q.figures[] (PUBLICAVEL)` → `manifest.json` →
  convenção direta `./assets/figures/<ano>/Q<NN>.webp` → aviso.
* Com imagem: `<figure class="qfigure"><img loading="lazy" …><figcaption>…</figcaption></figure>`
  (`alt` sempre presente; `onerror` volta para o aviso — nunca img quebrada).
* Sem imagem (ainda em curadoria): aviso textual atual preservado
  ("possui figura no caderno original") **+** caminho esperado do arquivo
  (ex.: `2026/Q18.webp`) para orientar quem recorta — sem expor stack/URL interna.
* Acessibilidade: `alt` real, `figcaption` com fonte + página, foco visível,
  responsivo (`max-width: 100%`, dark-mode por tokens, `prefers-reduced-motion`
  respeitado — sem zoom animado).
* Telas cobertas: `questao.html` (leitura), `estudos.html` (cards),
  `simulado.html` (modo Estudo/Prova/Revisão — a figura **aparece** nos três;
  só o gabarito é oculto no Modo Prova, AGENTS.md §9).

## 6. Backend (contrato, sem redistribuir binário)

* Migração `V8__question_figures.sql` → tabela `question_figures`
  (`question_id`, `position`, `file_path`, `alt_text`, `page`,
  `publication_status`, …; UNIQUE `(question_id, position)`).
* `QuestionResponse.figures[]` → `{path, alt, page, position, publicationStatus}`.
  `path` é relativo a `frontend/assets/figures/` (ex.: `"2026/Q18.webp"`).
* `sync_figures.py` (idempotente, `ON CONFLICT DO NOTHING` + checagem de
  divergência como o importador TASK 2.3): lê o manifest, resolve
  `(ano, numero) → question_id` e insere/atualiza `question_figures`.
* `hasFigure` continua existindo (sinal rápido): `TRUE` = depende de visual
  no PDF-fonte, tenha ou não recorte publicado.

## 7. Validação

* `python3 scripts/analysis/check_figures.py` (novo, só leitura por padrão):
  nomes, formatos, dimensões via cabeçalho (sem Pillow), manifest íntegro,
  páginas contra `data/extracted/`, cobertura das 36 com figura.
* `python3 scripts/analysis/check_frontend.py`: passa a exigir
  `js/components/figure.js`, `assets/figures/manifest.json` e o container
  `#questao-figure` (ver §8 das notas de implementação).
* `node --check` nos JS tocados + teste de fumaça servindo `frontend/`
  (HTTP 200 + manifest válido).
* Backend: `mvn -f backend/pom.xml test` (adição é compatível: campo novo
  `figures` com default `[]`, sem quebrar os 58 testes existentes).

## 8. Situação inicial

As 36 entradas já estão esqueletadas no `manifest.json` com `status`
`PENDENTE_REVISAO` e `alt` template — ou seja, a UI hoje mostra o aviso +
caminho esperado. À medida que os recortes forem colocados na pasta e o
`alt` for redigido, a imagem aparece automaticamente (sem mudar código).
