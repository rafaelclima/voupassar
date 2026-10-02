# Design system — VouPassar

> Fundação visual e de código do frontend. Showcase em
> `frontend/design-system.html`. Este documento é o contrato que toda tela
> deve seguir.

## 1. Arquivos

```
frontend/
├── css/
│   ├── tokens.css          # ÚNICA fonte de cor/tipo/espaço/raio/sombra/movimento
│   ├── base.css            # @font-face, reset, tipografia, primitivas de layout,
│   │                       # cabeçalho, rodapé, foco, reduced-motion
│   ├── components.css      # botões, campos, cards, chips, tabelas, badges,
│   │                       # alerts, modal, vazio, loading, erros, molde de questão
│   ├── utilities.css       # utilitários pontuais (visually-hidden, mt-*, docs-grid…)
│   ├── dark.css            # só ajustes de componente do dark (tokens vivem em tokens.css)
│   └── <página>.css        # layout exclusivo de uma tela
├── js/
│   ├── config.js           # ÚNICO lugar com API_BASE_URL (meta → global → ?api=)
│   ├── vocab.js            # tradução dos enums da API para português
│   ├── api/client.js       # Fetch + envelope {code,message,traceId} + ApiError
│   ├── api/*.js            # uma pasta por domínio de dados
│   ├── state/store.js      # pub/sub mínimo (user, lastError)
│   ├── components/ui.js    # el(), toast, modal, loading, empty, erros de formulário
│   ├── main.js             # bootstrap global (ano, nav, tema, modais)
│   └── views/*.js          # uma view por página
└── assets/
    ├── fonts/              # Plus Jakarta Sans (woff2, subconjuntos latin/latin-ext)
    ├── logo-mark.png       # símbolo do header (tema claro)
    ├── logo-mark-light.png # símbolo do header (tema escuro)
    └── favicon.svg
```

Regras (AGENTS.md §19): páginas carregam os CSS por `<link>` e os JS por
`type="module"`. Proibido `innerHTML` com dados (XSS — §15), regra de negócio
em view, `px` mágico em página, ou token novo fora de `tokens.css`.

## 2. Paleta

Derivada de `assets/logo.png`, que tem duas posições de azul:
**navy `#0D2B45`** e **azul `#0F5084`**. Toda a escala `--color-primary-*`
nasce dessas duas cores.

| Papel | Token | Claro | Escuro |
|---|---|---|---|
| Ação principal | `--color-primary-600` | `#0f5084` | `#3d8fd1` |
| Ação em bloco de marca | `--color-hero-from/via/to` | `#0d2b45` → `#0f5084` | **iguais** |
| Texto de link | `--color-primary-700` | `#0f3e63` | `#7fb4e4` |
| Acento (uso parcimonioso) | `--color-accent-500/600` | `#6d43cc` / `#5b36b0` | `#a485ea` / `#7a4fdb` |
| Texto | `--color-ink-900/700/500` | `#0e1720` / `#34465a` / `#5e7288` | `#eef3f8` / `#b9c6d4` / `#8b9cad` |
| Superfície | `--color-surface` / `--color-bg-50/100` | `#ffffff` / `#fafcfd` / `#f4f7fa` | `#12283c` / `#0a1a2a` / `#0e1e2e` |

Três regras que a escala impõe:

1. **`--color-primary-*` é a marca, e no tema escuro ela vira cor de TEXTO.**
   Por isso não pode ser usada como fundo ali: um bloco azul claro receberia
   texto branco e quebraria o contraste. Para fundo de destaque existem os
   tokens `--color-hero-*`, que **não** são redefinidos no escuro — o bloco de
   marca precisa ser reconhecível igual nos dois temas.
2. **Nenhum componente presume "branco" sobre cor sólida.** Existe
   `--color-on-primary` / `--color-on-accent` / `--color-on-navy`, que
   invertem no escuro. Sem eles, o botão primário escuro passaria de 3,47:1
   para 4,82:1 só porque o texto deixou de ser branco.
3. **Todo par texto/fundo foi medido**, não estimado. `--color-ink-500` é
   `#5e7288` e não `#64798f` porque o segundo dava 4,49:1 no branco — 0,01
   abaixo do AA. Para revalidar ao mexer na paleta, o procedimento está em
   `scripts/analysis/check_frontend.py` (a lista de pares medidos está no
   histórico deste doc).

## 3. Tipografia

**Plus Jakarta Sans variável (pesos 400–800)**, servida do próprio repositório:
`assets/fonts/plus-jakarta-sans-latin.woff2` (27 KB) e `-latin-ext.woff2`
(22 KB), com `font-display: swap`, `preload` do subconjunto latino e
`unicode-range` em cada `@font-face`. O navegador baixa só o subconjunto que
precisa e **não há requisição a terceiro** — o que mantém AGENTS.md §16
(privacidade) e §18 (leve) intactos. Se um dia for preciso CDN, usar
`display=swap` + `preconnect` e nunca bloquear a renderização.

Escala com `clamp()` para o título fluir entre mobile e desktop:

| Token | Tamanho | Uso |
|---|---|---|
| `--text-2xs` | 11px | micro-rótulo (só com `.eyebrow`, em caixa alta) |
| `--text-xs` … `--text-xl` | 12 → 20px | interface e corpo |
| `--text-2xl` | 24px | título de bloco |
| `--text-3xl` | 28 → 36px | título de página, número de métrica |
| `--text-4xl` | 32 → 48px | `.display` |
| `--text-display` | 40 → 72px | hero da landing |

`--leading-heading: 1.15` e `--tracking-tighter: -0.03em` nos títulos: é o
espaçamento negativo que separa tipografia de verdade de padrão de sistema.

## 4. Forma, elevação e movimento

* Raios: `10 / 14 / 18 / 24 / 32 / pill`. Botões e chips usam `pill` — é o que
  dá o aspecto atual sem depender de cor forte.
* Sombras tingidas com o navy da marca (`rgb(13 43 69 / …)`), nunca preto puro:
  `sm` para cartão, `md` para cartão interativo, `lg` para modal,
  `--shadow-brand` para o CTA do bloco de destaque.
* Movimento: `--ease-out` compartilhado, 140/220/420 ms. Desligado sob
  `prefers-reduced-motion`.
* Alvo de toque mínimo 44px (`--touch-min`); botão padrão tem 48px.

## 5. Componentes

* **Botões:** `.btn` + `--primary/secondary/ghost/danger/on-navy`, `--sm/lg/block`.
  Loading via `setButtonLoading()` (preserva o rótulo para leitores de tela).
* **Campos:** `.field > .field__label + .input/.select/.textarea + .field__hint/.field__error`;
  borda 1.5px, erro com `aria-invalid` + `aria-describedby`.
* **Cartões:** `.card > __header/__body/__footer`, `--interactive`, `--stat`,
  `--accent`. `.stat-value` para número; `.stat-value--text` quando o valor é
  rótulo (data, "Ainda sem dados") — usar o corpo gigante nesses casos quebra
  em duas linhas e desalinha a fileira de cartões.
* **Chips:** `.chip` para assunto/disciplina/ano (peso leve); `.badge` para
  estado (sucesso, atenção, anulada).
* **Tabelas:** `.table-wrap[tabindex=0][role=region]` + `.table` com `caption` e `scope`.
* **Feedback:** `.alert--success/danger/warning/info`; `.toast-region` via `toast()`.
* **Modal:** `<dialog class="modal" data-modal>`; `Esc` nativo; foco volta ao
  elemento que abriu.
* **Vazio:** `.empty` ou `renderEmpty()` — sempre título + descrição + ação.
* **Loading:** `.spinner(--sm)`, `.skeleton`, `.loading-block[role=status]`.
* **Erros:** `.error-summary[role=alert]` + `traceId`; API traduzida por `friendlyMessage()`.
* **Alternativa de questão:** `.question__option` (+`--correct/--wrong`),
  marcada com `:has(input:checked)`. O `<legend>` do `<fieldset>` precisa
  continuar existindo: `questao.js` e `simulado.js` leem
  `fieldset.querySelector("legend")`.

## 6. Vocabulário (`js/vocab.js`)

A API responde com códigos técnicos. **Mostrá-los ao aluno é jargão**, e a
regra é centralizada: nenhuma view interpola enum cru em texto.

```js
import { disciplineLabel, modeLabel, statusLabel } from "../vocab.js";
text: disciplineLabel(q.disciplineCode, q.disciplineName)
```

Cobre disciplina, modo, origem da questão, dificuldade, classificação,
confiança, publicação, status e nível. O mesmo assunto chega com códigos
diferentes em endpoints diferentes (o diagnóstico traz `MATEMATICA`, o
catálogo traz `LINGUA_PORTUGUESA`), então o mapa cobre as duas formas. Fallback
sempre é um texto neutro em português — nunca o enum cru.

O dicionário de assuntos (`topicLabel`) foi conferido na tabela `topics` do
banco; não é adivinhação.

## 7. Copy: o que o aluno lê

Regras aplicadas nas telas de aluno:

* **Sem bloco de auditoria.** O "Leitura honesta dos números" e as `notes` que a
  API devolve por questão/rodada/simulado são trilha interna do motor
  ("curadoria TASK 12.2", "regra DESCONHECIDA"). Não vão para a tela do aluno.
  A honestidade do AGENTS.md §4 continua: onde não sabemos, o texto diz em
  português que ainda não sabemos, e o detalhe fica na área administrativa e
  em `docs/`.
* **Sem nome de tabela, coluna, endpoint ou versão de algoritmo** no texto do
  aluno (`question_attempts`, `discipline_id`, `v1-deterministico`, taxonomia).
* **Frases explicadas com os números que a API já devolve.** As justificativas
  do diagnóstico narravam o algoritmo ("score determinístico 0"). As telas
  reescrevem a frase a partir de acertos, tentativas e peso do assunto na
  prova — sem estimar nada.
* **Nada de jargão de infraestrutura na autenticação** (`sessionStorage`,
  limite de bcrypt, anti-enumeração).

`scripts/analysis/check_frontend.py` verifica as três primeiras regras, então a
regressão falha no CI.

## 8. Acessibilidade (WCAG prática, §17)

HTML semântico, `lang="pt-BR"`, `skip-link`, rótulos reais, `aria-live` em
toast/loading/API, foco visível, contraste AA medido, `prefers-reduced-motion`,
toque ≥ 44px, `caption` em tabela.

Tema: um script inline no `<head>` de cada página define `data-theme` **antes
da primeira pintura**, evitando o flash de tema. `dark.css` é injetado por
`main.js` e só carrega ajustes de componente — redeclarar token lá sobrescreve
a escala da marca, porque ele entra depois de `tokens.css`.

Logotipo: a marca tem um componente navy que some em fundo escuro, então há
duas versões do símbolo e o tema escolhe. O seletor precisa de `img` no
caminho (`.brand img.brand__mark--inverse`), senão `.brand img` ganha por
especificidade e as duas marcas aparecem juntas.

## 9. Verificação

```bash
python3 -m http.server 8899 --directory frontend   # http://localhost:8899/
python3 scripts/analysis/check_frontend.py         # estático: tokens, a11y, sem segredos, sem jargão
for f in frontend/js/*.js frontend/js/*/*.js; do node --check "$f"; done
```

Além disso, validar no navegador real (MCP `chrome-devtools` ou Playwright):
guarda de autenticação, fluxo principal contra o backend local, estados de
loading/vazio/erro, responsividade, e console sem erros.

## 10. O que ainda usa o layout antigo

Redesenhados: fundação (tokens/base/components), auth, dashboard.
**Ainda no layout anterior**, já consumindo a fundação nova: `index.html`
(landing), `estudos`, `questao`, `simulado`, `perfil`, `admin` e a galeria
`design-system.html`. As telas restantes precisam seguir os mesmos tokens e
componentes — não criar estilo novo por página.