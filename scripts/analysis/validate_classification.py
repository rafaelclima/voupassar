#!/usr/bin/env python3
"""Validador da classificacao pedagogica — TASK 1.4 + TASK D.1 (EAJ).

Verifica `docs/content-analysis/per-edition/<edicao>.json`:
  - 40 classificacoes (IFRN) ou 50/40 (EAJ-2021/EAJ-2022/EAJ-2025),
    numeros 1-N unicos;
  - todos os campos obrigatorios presentes;
  - assunto compativel com a disciplina (LP x MAT x CN x CH);
  - subassunto na lista valida (LP/MAT taxonomia v1.1; CN/CH codigos
    novos D.1, SÓ para o observado nas 20 questoes CN/CH de 2021);
  - flags `anulada` e `disciplina` consistentes com `data/linked/` e
    `data/extracted/` (fontes da verdade; namespace EAJ em
    `data/{linked,extracted}/eaj/<ano>.json`);
  - invariantes de honestidade: dificuldade_base fixa, origem fixa,
    evidencia <= 200 caracteres, OUTRO/NECESSITA_REVISAO/anulada com
    observacao (0 OUTRO sem justificativa);
  - EAJ: `edition` namespaced (`EAJ-<ano>`, nunca ano sozinho);
    disciplina pela faixa DA edicao; dificuldade_confianca BAIXA global
    (sem dados de desempenho); `needs_visual_check` do extraido exige
    `NECESSITA_REVISAO` (Q22/Q39-2025: trava D.3 — classificadas aqui,
    só entram no banco após conferencia visual).

Somente leitura (nunca altera os JSONs). Sai com codigo != 0 em caso de erro.

Uso:
    python3 scripts/analysis/validate_classification.py
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
LINKED_DIR = REPO_ROOT / "data" / "linked"
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted"
CLASSIF_DIR = REPO_ROOT / "docs" / "content-analysis" / "per-edition"

REQUIRED = [
    "edition", "number", "anulada", "disciplina", "disciplina_confianca",
    "assunto", "subassunto", "assunto_confianca", "habilidade",
    "tipo_raciocinio", "dificuldade_estimada", "dificuldade_base",
    "dificuldade_confianca", "evidencia", "origem", "status", "observacao",
]
LP_ASSUNTOS = {"INTERPRETACAO_TEXTUAL", "GRAMATICA_NORMA", "OUTRO"}
MAT_ASSUNTOS = {
    "PORCENTAGEM", "RAZAO_PROPORCAO", "ARITMETICA", "ALGEBRA", "GEOMETRIA",
    "ESTATISTICA_DADOS", "MATEMATICA_FINANCEIRA", "GRANDEZAS_MEDIDAS", "OUTRO",
}
# TASK D.1 — codigos CN novos, SÓ para o observado nas 12 questoes de 2021
# (evidencia entre parenteses; nada alem disso entra na lista valida):
CN_ASSUNTOS = {
    "ECOLOGIA",          # Q31 (niveis troficos), Q32 (autotrofos/heterotrofos)
    "NUTRICAO_SAUDE",    # Q33, Q34 (ultraprocessados, afirmativas I-IV)
    "AGROPECUARIA",      # Q35 (Censo Agropecuario 2017, proteina animal)
    "BIOLOGIA_CELULAR",  # Q36 (respiracao celular, glicose)
    "QUIMICA_GERAL",     # Q37 (fritura), Q38 (polaridade), Q39 (radioatividade)
    "FISICA_GERAL",      # Q40 (energia), Q41 (escalas), Q42 (peso aparente)
}
# TASK D.1 — codigos CH novos, SÓ para o observado nas 8 questoes de 2021:
CH_ASSUNTOS = {
    "HISTORIA_BRASIL",   # Q43 (migracoes), Q44 (pecuaria colonial), Q46 (Staden)
    "CULTURA_SOCIEDADE", # Q45 (acaraje/candomble)
    "CARTOGRAFIA",       # Q47 (legenda do Mapa 1)
    "GEOGRAFIA_BRASIL",  # Q48 (rebanho Centro-Oeste, Cerrado)
    "GEOPOLITICA",       # Q49 (UE), Q50 (Velha Ordem Bipolar)
}
LP_SUBS = {
    "INTENCAO_COMUNICATIVA", "INFORMACAO_EXPLICITA", "INFERENCIA",
    "FUNCAO_ELEMENTO_PARAGRAFO", "TIPO_TEXTUAL", "GENERO_TEXTUAL",
    "SINTAXE_FUNCAO", "SINTAXE_PERIODO", "PONTUACAO", "COESAO_REFERENCIA",
    "MORFOLOGIA", "SEMANTICA_VOCABULARIO", "NORMA_PADRAO",
    "ACENTUACAO_GRAFICA",  # v1.1 (seed V2)
    "OUTRO",
}
MAT_SUBS = {
    "RAZAO", "REGRA_DE_TRES", "ESCALA", "PROPORCIONALIDADE",
    "OPERACOES", "FRACOES", "NOTACAO_CIENTIFICA", "DIVISIBILIDADE_MMC_MDC",
    "SISTEMAS_NUMERACAO",  # v1.1
    "FUNCAO_AFIM", "EQUACOES", "SISTEMAS", "SEQUENCIAS",
    "AREA_PLANA", "PERIMETRO_COMPRIMENTO", "POLIGONOS",
    "PITAGORAS_DISTANCIA", "VOLUME", "UNIDADES_MEDIDA",
    "MEDIA", "MEDIANA_MODA", "LEITURA_GRAFICO_TABELA",
    "CONTAGEM_COMBINATORIA", "PROBABILIDADE",
    "CALCULO_DIRETO", "VARIACAO", "REPRESENTACAO_FRACAO",
    "JUROS_SIMPLES", "JUROS_COMPOSTOS", "DESCONTO",
    "TEMPO", "VOLUME_CAPACIDADE", "MASSA_COMPRIMENTO",
    "OUTRO",
}
# TASK D.1 — subcodigos CN novos (1+ evidencia cada; lista fechada):
CN_SUBS = {
    "NIVEL_TROFICO", "NUTRICAO_SERES_VIVOS",
    "ALIMENTOS_PROCESSADOS",
    "PRODUCAO_ALIMENTOS",
    "RESPIRACAO_CELULAR",
    "FENOMENOS_QUIMICOS", "POLARIDADE_MISTURAS", "RADIOATIVIDADE",
    "ENERGIA_CONSERVACAO", "LEIS_NEWTON", "ESCALAS_TERMOMETRICAS",
}
# TASK D.1 — subcodigos CH novos (1+ evidencia cada; lista fechada):
CH_SUBS = {
    "MIGRACOES_REPUBLICA", "ECONOMIA_COLONIAL", "COLONIZACAO_CONTATO",
    "CULTURA_AFRO_BRASILEIRA",
    "ELEMENTOS_MAPA",
    "BIOMAS_IMPACTOS",
    "BLOCOS_ECONOMICOS", "ORDEM_MUNDIAL",
}
EAJ_SUBS = {"LINGUA_PORTUGUESA": LP_SUBS, "MATEMATICA": MAT_SUBS,
            "CIENCIAS_NATUREZA": CN_SUBS, "CIENCIAS_HUMANAS": CH_SUBS}
CONF = {"ALTA", "MEDIA", "BAIXA"}
DIF = {"FACIL", "MEDIA", "DIFICIL"}

# EAJ-2021 = 50Q em 4 areas; 2022/2025 = 40Q em 2 areas (estrutura de cada
# edicao vale só para ela; TASKS.md regra 4).
EAJ_TOTAIS = {"2021": 50, "2022": 40, "2025": 40}


def vocab(disciplina: str) -> set[str]:
    if disciplina == "LINGUA_PORTUGUESA":
        return LP_ASSUNTOS
    if disciplina == "CIENCIAS_NATUREZA":
        return CN_ASSUNTOS
    if disciplina == "CIENCIAS_HUMANAS":
        return CH_ASSUNTOS
    return MAT_ASSUNTOS


def check_file(ed: str, errs: list[str]) -> tuple[int, int]:
    """Valida um arquivo; retorna (n_classificacoes, n_necessita_revisao)."""
    data = json.loads((CLASSIF_DIR / f"{ed}.json").read_text(encoding="utf-8"))
    cs = data.get("classificacoes", [])
    if ed.startswith("eaj-"):
        ano = ed[4:]
        if ano not in EAJ_TOTAIS:
            errs.append(f"ano EAJ desconhecido: {ano}")
            return len(cs), 0
        total = EAJ_TOTAIS[ano]
        expected_edition = f"EAJ-{ano}"
        linked = {q["number"]: q for q in
                  json.loads((LINKED_DIR / "eaj" / f"{ano}.json").read_text(encoding="utf-8"))["questions"]}
        ext = {q["number"]: q for q in
               json.loads((EXTRACTED_DIR / "eaj" / f"{ano}.json").read_text(encoding="utf-8"))["questions"]}
    else:
        total = 40
        expected_edition = ed
        linked = {q["number"]: q for q in
                  json.loads((LINKED_DIR / f"{ed}.json").read_text(encoding="utf-8"))["questions"]}
        ext = {q["number"]: q for q in
               json.loads((EXTRACTED_DIR / f"{ed}.json").read_text(encoding="utf-8"))["questions"]}
    if sorted(c.get("number") for c in cs) != list(range(1, total + 1)):
        errs.append(f"numeracao != 1-{total} unica")
    for c in cs:
        n = c.get("number")
        tag = f"Q{n}"
        for f in REQUIRED:
            if f not in c:
                errs.append(f"{tag}: campo ausente {f}")
        if ed.startswith("eaj-") and c.get("edition") != expected_edition:
            errs.append(f"{tag}: edition={c.get('edition')!r} (esperado {expected_edition!r}; ano sozinho nunca identifica a edicao)")
        if c.get("anulada") != linked.get(n, {}).get("annulled"):
            errs.append(f"{tag}: anulada diverge de data/linked")
        if c.get("disciplina") != ext.get(n, {}).get("discipline"):
            errs.append(f"{tag}: disciplina diverge de data/extracted")
        if c.get("assunto") not in vocab(c.get("disciplina", "")):
            errs.append(f"{tag}: assunto fora do vocabulario da disciplina")
        if ed.startswith("eaj-"):
            subs = EAJ_SUBS.get(c.get("disciplina", ""), set())
            if c.get("subassunto") not in subs:
                errs.append(f"{tag}: subassunto fora da lista valida D.1")
            if c.get("dificuldade_confianca") != "BAIXA":
                errs.append(f"{tag}: dificuldade_confianca != BAIXA (EAJ: sem dados de desempenho)")
            if c.get("disciplina_confianca") != "ALTA":
                errs.append(f"{tag}: disciplina_confianca != ALTA (faixa+secao concordam)")
            if ext.get(n, {}).get("needs_visual_check") and c.get("status") != "NECESSITA_REVISAO":
                errs.append(f"{tag}: needs_visual_check no extraido exige NECESSITA_REVISAO (trava D.3)")
            if c.get("anulada") and not str(c.get("observacao", "")).strip():
                errs.append(f"{tag}: anulada sem observacao")
        if c.get("assunto_confianca") not in CONF:
            errs.append(f"{tag}: confianca invalida")
        if c.get("dificuldade_estimada") not in DIF:
            errs.append(f"{tag}: dificuldade invalida")
        if c.get("dificuldade_base") != "ESTIMATIVA_ESPECIALISTA_SEM_DADOS":
            errs.append(f"{tag}: dificuldade_base adulterada")
        if c.get("origem") != "CLASSIFICACAO_DERIVADA_FONTE":
            errs.append(f"{tag}: origem adulterada")
        if len(str(c.get("evidencia", ""))) > 200:
            errs.append(f"{tag}: evidencia > 200 caracteres")
        if c.get("status") == "NECESSITA_REVISAO" and not str(c.get("observacao", "")).strip():
            errs.append(f"{tag}: NECESSITA_REVISAO sem observacao")
        if c.get("assunto") == "OUTRO" and not str(c.get("observacao", "")).strip():
            errs.append(f"{tag}: OUTRO sem justificativa")
        if c.get("subassunto") == "OUTRO" and not str(c.get("observacao", "")).strip():
            errs.append(f"{tag}: subassunto OUTRO sem justificativa")
    n_nota_visual = sum(1 for c in cs if c.get("status") == "NECESSITA_REVISAO")
    return len(cs), n_nota_visual


def main() -> int:
    ok = True
    editions = sorted(p.stem for p in CLASSIF_DIR.glob("*.json"))
    if not editions:
        print("ERRO: nenhum JSON em docs/content-analysis/per-edition/")
        return 1
    total = 0
    for ed in editions:
        errs: list[str] = []
        n, n_nota_visual = check_file(ed, errs)
        total += n
        print(f"{ed}: {'OK' if not errs else 'FALHA'} "
              f"(n={n}, nota_visual={n_nota_visual})")
        for e in errs:
            print(f"  ERRO {ed}: {e}")
            ok = False
    print(f"Total: {total} classificacoes")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
