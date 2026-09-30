#!/usr/bin/env python3
"""Validador da classificacao pedagogica — TASK 1.4.

Verifica `docs/content-analysis/per-edition/<edicao>.json`:
  - 40 classificacoes, numeros 1-40 unicos;
  - todos os campos obrigatorios presentes;
  - assunto compativel com a disciplina (LP x MAT);
  - flags `anulada` e `disciplina` consistentes com `data/linked/` e
    `data/extracted/` (fontes da verdade);
  - invariantes de honestidade: dificuldade_base fixa, origem fixa,
    evidencia <= 200 caracteres.

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
CONF = {"ALTA", "MEDIA", "BAIXA"}
DIF = {"FACIL", "MEDIA", "DIFICIL"}


def main() -> int:
    ok = True
    editions = sorted(p.stem for p in CLASSIF_DIR.glob("*.json"))
    if not editions:
        print("ERRO: nenhum JSON em docs/content-analysis/per-edition/")
        return 1
    for ed in editions:
        data = json.loads((CLASSIF_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        cs = data.get("classificacoes", [])
        linked = {q["number"]: q for q in
                  json.loads((LINKED_DIR / f"{ed}.json").read_text(encoding="utf-8"))["questions"]}
        ext = {q["number"]: q for q in
               json.loads((EXTRACTED_DIR / f"{ed}.json").read_text(encoding="utf-8"))["questions"]}
        errs: list[str] = []
        if sorted(c.get("number") for c in cs) != list(range(1, 41)):
            errs.append("numeracao != 1-40 unica")
        for c in cs:
            n = c.get("number")
            for f in REQUIRED:
                if f not in c:
                    errs.append(f"Q{n}: campo ausente {f}")
            if c.get("anulada") != linked.get(n, {}).get("annulled"):
                errs.append(f"Q{n}: anulada diverge de data/linked")
            if c.get("disciplina") != ext.get(n, {}).get("discipline"):
                errs.append(f"Q{n}: disciplina diverge de data/extracted")
            vocab = LP_ASSUNTOS if c.get("disciplina") == "LINGUA_PORTUGUESA" else MAT_ASSUNTOS
            if c.get("assunto") not in vocab:
                errs.append(f"Q{n}: assunto fora do vocabulario da disciplina")
            if c.get("assunto_confianca") not in CONF:
                errs.append(f"Q{n}: confianca invalida")
            if c.get("dificuldade_estimada") not in DIF:
                errs.append(f"Q{n}: dificuldade invalida")
            if c.get("dificuldade_base") != "ESTIMATIVA_ESPECIALISTA_SEM_DADOS":
                errs.append(f"Q{n}: dificuldade_base adulterada")
            if c.get("origem") != "CLASSIFICACAO_DERIVADA_FONTE":
                errs.append(f"Q{n}: origem adulterada")
            if len(str(c.get("evidencia", ""))) > 200:
                errs.append(f"Q{n}: evidencia > 200 caracteres")
            if c.get("status") == "NECESSITA_REVISAO" and not str(c.get("observacao", "")).strip():
                errs.append(f"Q{n}: NECESSITA_REVISAO sem observacao")
        n_rev = sum(1 for c in cs if c.get("status") == "NECESSITA_REVISAO")
        print(f"{ed}: {'OK' if not errs else 'FALHA'} "
              f"(n={len(cs)}, em_revisao={n_rev})")
        for e in errs:
            print(f"  ERRO {ed}: {e}")
            ok = False
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
