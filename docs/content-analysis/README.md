# Análise de Conteúdo — TASK 1.4 (classificação pedagógica)

> Julgamento pedagógico DERIVADO das provas reais. Nenhum assunto,
> dificuldade ou habilidade aqui é fato oficial do IFRN: cada item carrega
> `origem = CLASSIFICACAO_DERIVADA_FONTE`, confiança explícita
> (`ALTA/MEDIA/BAIXA`) e evidência textual. Dificuldade é sempre
> `ESTIMATIVA_ESPECIALISTA_SEM_DADOS` (sem dados de desempenho de alunos
> não há calibração possível). Revisão humana: PENDENTE em todos os itens.

## Conteúdo deste diretório

| Arquivo | O quê |
|---|---|
| `taxonomy.md` | Taxonomia v1 (códigos observados nas provas) + lacunas detectadas |
| `summary.md` | Agregados, distribuição de confiança e fila de revisão |
| `per-edition/<edicao>.json` | 40 classificações por edição (240 no total), uma por questão |

## Método

1. Fonte: `data/extracted/<edicao>.json` (enunciado + alternativas + bloco
   bruto + página + disciplina da TASK 1.2).
2. Anotação assistida por LLM, uma edição por vez, com lista controlada de
   códigos (§ taxonomia). Códigos fora da lista proibidos, exceto `OUTRO`
   com justificativa em `observacao` (ocorreu 1× em 240).
3. Cruzamento automático: `anulada` ≡ `data/linked/`; `disciplina` ≡
   `data/extracted/` (divergência = erro).
4. Validação: `python3 scripts/analysis/validate_classification.py`
   (schema + invariantes de honestidade + `NECESSITA_REVISAO` com motivo).
5. Spot-check humano por amostragem nos casos sensíveis (OUTROs, anuladas,
   Q40/2023) antes da publicação deste diretório.

## Campos por questão

`edition, number, anulada, disciplina, disciplina_confianca, assunto,
subassunto, assunto_confianca, habilidade, tipo_raciocinio,
dificuldade_estimada, dificuldade_base, dificuldade_confianca, evidencia
(<=200 chars), origem, status (OK/NECESSITA_REVISAO), observacao`.

## Limitações conhecidas (não silenciadas)

* ~16% dos itens (38/240) dependem de figura/gráfico/charge ausente do
  `pdftotext` → `status = NECESSITA_REVISAO`, motivo em `observacao`.
  Ver fila em `summary.md`.
* Acentuação gráfica (3 edições) e numeração romana (1 questão) não tinham
  código próprio na taxonomia v1 → documentados como lacunas em
  `taxonomy.md`, não forçados em códigos vizinhos sem registro.
* Q40/2023: conta direta (MMC) diverge do gabarito oficial — registrado
  para revisão humana, nunca "corrigido" por inferência.
* Dificuldade: distribuição (FACIL 108 / MEDIA 121 / DIFICIL 11) é
  palpite calibrado por heurística de passos, não medida. Será
  recalibrada com dados reais de tentativas (Fase 4) e então versionada.
