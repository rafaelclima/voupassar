# Prompt `ai-prova-v1` — TASK A.1 (DRAFT VPS)

> **Status: DRAFT — não é DONE.**
> Escopo VPS: texto do prompt + 1 few-shot + proibições. Validador Java +
> `mvn test` = DEV-only. DONE só na DEV.
>
> Contrato: `docs/ai-prova-schema.md`. Caminho final do prompt (candidatos
> no TASKS.md A.1: `backend/src/main/resources/ai/prompts/ai-prova-v1.txt`
> ou `docs/ai/prompts/ai-prova-v1.md`) a fixar na implementação DEV sem
> quebrar o schema. Este arquivo é o draft versionado `ai-prova-v1`.
>
> Auditoria: toda geração registra `prompt_version: ai-prova-v1`
> (TASK C.1, sem PII no corpo — só formato/disciplina/tópicos).

---

## Texto do prompt (a enviar ao provedor; variáveis entre {{}})

> Você gera PROVAS INÉDITAS de múltipla escolha no formato de um processo
> seletivo brasileiro de nível médio. As questões são INVENTADAS por você
> neste ato — NUNCA as apresente como questões oficiais de nenhuma edição;
> NUNCA cite edição, ano, banca ou documento real como origem de qualquer
> questão. O formato é apenas INSPIRADO na estrutura abaixo.
>
> Parâmetros desta geração:
> `INSTITUTION={{INSTITUTION}}` (IFRN|EAJ),
> `EDITION_MODEL={{EDITION_MODEL}}` (ex.: IFRN-2026, EAJ-2022, EAJ-2021),
> `N={{N}}` (40, ou 50 se EAJ-2021),
> `MAPA_DE_POSICOES={{DISCIPLINE_MAP}}`
> (ex.: IFRN 1–20 LINGUA_PORTUGUESA + 21–40 MATEMATICA;
> EAJ-2021 1–15 LP + 16–30 MAT + 31–42 CIENCIAS_NATUREZA + 43–50
> CIENCIAS_HUMANAS).
>
> Gere EXATAMENTE {{N}} questões, numeradas de 1 a {{N}} sem lacuna nem
> repetição, seguindo o MAPA_DE_POSICOES. Cada questão tem: enunciado
> completo (`stem`), 4 alternativas (`A,B,C,D`) não-vazias e distintas
> entre si, resposta (`answer` ∈ {A,B,C,D}), `discipline`, `topic` e
> `subtopic` opcional — só do vocabulário permitido abaixo, ou o literal
> `DESCONHECIDO`. NUNCA invente código de assunto.
>
> REGRAS OBRIGATÓRIAS:
> 1. Questões AUTO-CONTIDAS: todo o necessário para resolver está no
>    enunciado + alternativas. PROIBIDO depender de texto-base externo,
>    trecho compartilhado, figura, imagem, gráfico, tabela, charge,
>    mapa, foto ou qualquer anexo. PROIBIDO escrever "Texto N", "trecho",
>    "observe", "conforme", "abaixo/acima", "a seguir" referindo-se a
>    material externo.
> 2. PROIBIDO gerar `explanation`, `resolução`, `passo a passo`,
>    `comentário` ou qualquer ensino após o enunciado. Apenas os campos
>    do schema.
> 3. PROIBIDO gerar `passage`, `figure`, `image` ou campo fora do schema.
> 4. PROIBIDO conteúdo ofensivo, discriminatório, identificador ou com
>    dados pessoais. Linguagem neutra, nível de Ensino Fundamental.
> 5. Variedade: distribua os assuntos do vocabulário; não repita o mesmo
>    `topic/subtopic` em todas as questões; não repita a mesma letra de
>    `answer` em todas as posições.
>
> FORMATO DE SAÍDA (estrito, sem markdown, sem cerca de código, sem texto
> fora do JSON):
> `{"format": "{{INSTITUTION}}", "editionModel": "{{EDITION_MODEL}}",
> "questions": [{"n": 1, "discipline": "…", "topic": "…", "subtopic": "…",
> "stem": "…", "options": {"A": "…", "B": "…", "C": "…", "D": "…"},
> "answer": "…"}, …]}`
>
> Vocabulário permitido (usar SÓ estes códigos ou DESCONHECIDO):
> {{TOPIC_VOCAB}}
> (LP/MAT v1.1 + CN/CH só se EAJ-2021 — ver `docs/ai-prova-schema.md §4`.)
>
> EXEMPLO DE FORMA (1 questão sintética — NÃO é questão oficial, NÃO vem
> de nenhuma edição; só mostra o formato):

```json
{
  "format": "IFRN",
  "editionModel": "IFRN-2026",
  "questions": [
    {
      "n": 1,
      "discipline": "MATEMATICA",
      "topic": "PORCENTAGEM",
      "subtopic": "CALCULO_DIRETO",
      "stem": "EXEMPLO SINTÉTICO (não oficial): Uma loja anuncia 20% de desconto sobre um produto de R$ 150,00. Qual é o preço final com o desconto?",
      "options": {"A": "R$ 100,00", "B": "R$ 110,00", "C": "R$ 120,00", "D": "R$ 130,00"},
      "answer": "C"
    }
  ]
}
```

> Agora gere a prova completa com {{N}} questões no JSON estrito acima.
> Sem markdown. Sem texto antes ou depois. Sem campo extra.

---

## Notas de versionamento (v1)

* `ai-prova-v1`: primeira versão draftada na TASK A.1-VPS (2026-10-11).
  Mudanças futuras exigem nova versão (`ai-prova-v2`…) + registro de
  auditoria; o backend envia e registra `prompt_version` (C.1).
* `{{TOPIC_VOCAB}}` é preenchido pelo backend a partir do vocabulário
  vigente (`docs/ai-prova-schema.md §4`); o prompt nunca lista assunto
  fora dele.
* Sem PII: o corpo leva só formato/disciplina/tópicos/idioma
  (TASKS.md regra 4). Nenhum dado do aluno sai para o provedor.
* O "sem garantia" da decisão 4 vale para a correção pedagógica, nunca
  para o formato — a validação estrutural (§7 do schema) é obrigatória.
