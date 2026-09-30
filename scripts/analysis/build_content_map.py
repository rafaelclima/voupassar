#!/usr/bin/env python3
"""Mapa do conteudo cobrado — TASK 1.5.

Agrega deterministicamente `docs/content-analysis/per-edition/*.json`
(240 classificacoes da TASK 1.4) em serie historica por assunto/subassunto:
contagem, edicoes, disciplinas, percentual do total, tendencia descritiva e
confianca da classificacao.

Normalizacao v1.1 (mapeamento aplicado SOMENTE nesta agregacao; os JSONs
per-edition v1 permanecem congelados como trilha de auditoria):
  - G1: (2022,Q12) (2023,Q12) (2024,Q17) GRAMATICA_NORMA/* -> subassunto
    ACENTUACAO_GRAFICA (acentuacao grafica sem codigo proprio na v1).
  - G2: (2026,Q21) OUTRO/OUTRO -> ARITMETICA/SISTEMAS_NUMERACAO
    (numeracao romana; unico OUTRO em 240).
  - G3/G4 (2022 Q16 variacao; 2026 Q20 intertextual): mantidos como
    classificados, citados como limitacao.

Tendencia e DESCRITIVA (6 pontos, sem teste estatistico). Questoes anuladas
(5) contam como conteudo cobrado que apareceu na prova, sinalizadas a parte.

Uso:
    python3 scripts/analysis/build_content_map.py [--json-out PATH]
Imprime tabelas markdown no stdout; com --json-out grava os agregados.
"""

from __future__ import annotations

import argparse
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
PER_EDITION = REPO_ROOT / "docs" / "content-analysis" / "per-edition"

EDITIONS = ["2020", "2022", "2023", "2024", "2025", "2026"]

# (edition, number) -> (assunto, subassunto) normalizados v1.1
V11_OVERRIDES = {
    ("2022", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2023", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2024", 17): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2026", 21): ("ARITMETICA", "SISTEMAS_NUMERACAO"),
}


def trend_label(series: list[int]) -> str:
    """Rotulo descritivo puro (sem inferencia estatistica).

    Com 6 edicoes e contagens pequenas, oscilacoes de 1-2 questoes sao
    ruido: so recebem CRESCENTE/DECRESCENTE variacoes >= 2 no extremo.
    """
    total = sum(series)
    absent = sum(1 for s in series if s == 0)
    if total <= 3 or absent >= 3:
        return "ESPORADICO"
    if absent >= 1 or total < 12:
        return "BAIXA_RECORRENCIA"
    if max(series) - min(series) <= 1:
        return "ESTAVEL"
    if series[-1] - series[0] >= 2 and series[-1] == max(series):
        return "CRESCENTE"
    if series[0] - series[-1] >= 2 and series[-1] == min(series):
        return "DECRESCENTE"
    if absent == 0:
        return "RECORRENTE_OSCILANTE"
    return "INTERMITENTE"


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--json-out", default=None)
    args = ap.parse_args()

    items: list[dict] = []
    for ed in EDITIONS:
        data = json.loads((PER_EDITION / f"{ed}.json").read_text(encoding="utf-8"))
        for c in data["classificacoes"]:
            assunto, sub = c["assunto"], c["subassunto"]
            norm = ""
            if (ed, c["number"]) in V11_OVERRIDES:
                assunto, sub = V11_OVERRIDES[(ed, c["number"])]
                norm = "V1.1"
            items.append({
                "edition": ed, "number": c["number"], "anulada": c["anulada"],
                "disciplina": c["disciplina"], "assunto": assunto, "subassunto": sub,
                "normalizado": norm, "assunto_confianca": c["assunto_confianca"],
                "dificuldade_estimada": c["dificuldade_estimada"],
                "status": c["status"],
            })

    total = len(items)
    by_assunto: dict[str, list[dict]] = defaultdict(list)
    for it in items:
        by_assunto[it["assunto"]].append(it)

    agg = {"total": total, "editions": EDITIONS, "v11_overrides": [
        {"edition": e, "number": n, "assunto": a, "subassunto": s}
        for (e, n), (a, s) in sorted(V11_OVERRIDES.items())], "assuntos": {}}
    for assunto, lst in sorted(by_assunto.items()):
        per_ed = [sum(1 for it in lst if it["edition"] == e) for e in EDITIONS]
        discs = sorted({it["disciplina"] for it in lst})
        conf = Counter(it["assunto_confianca"] for it in lst)
        n_ann = sum(1 for it in lst if it["anulada"])
        n_rev = sum(1 for it in lst if it["status"] == "NECESSITA_REVISAO")
        subs = Counter(it["subassunto"] for it in lst)
        agg["assuntos"][assunto] = {
            "n": len(lst), "pct_total": round(100 * len(lst) / total, 1),
            "editions_present": [e for e, n in zip(EDITIONS, per_ed) if n],
            "series": dict(zip(EDITIONS, per_ed)),
            "trend": trend_label(per_ed),
            "disciplinas": discs,
            "confianca": dict(conf),
            "n_anuladas": n_ann, "n_em_revisao": n_rev,
            "subassuntos": dict(subs),
        }

    if args.json_out:
        Path(args.json_out).write_text(
            json.dumps(agg, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    # Saida markdown (tabelas para docs/content-map.md)
    print("## Por assunto")
    print("| Assunto | n | % total | Edições | Tendência | Conf (A/M/B) | Anul. | Em revisão |")
    print("|---|---|---|---|---|---|---|---|")
    for assunto, a in sorted(agg["assuntos"].items(), key=lambda kv: -kv[1]["n"]):
        c = a["confianca"]
        print(f"| {assunto} | {a['n']} | {a['pct_total']}% | "
              f"{','.join(a['editions_present'])} | {a['trend']} | "
              f"{c.get('ALTA',0)}/{c.get('MEDIA',0)}/{c.get('BAIXA',0)} | "
              f"{a['n_anuladas']} | {a['n_em_revisao']} |")
    print("\n## Por subassunto")
    print("| Assunto | Subassunto | n | Série 20-22-23-24-25-26 |")
    print("|---|---|---|---|")
    for assunto, a in sorted(agg["assuntos"].items(), key=lambda kv: -kv[1]["n"]):
        for sub, n in sorted(a["subassuntos"].items(), key=lambda kv: -kv[1]):
            serie = "-".join(str(sum(1 for it in items if it["assunto"] == assunto
                                     and it["subassunto"] == sub and it["edition"] == e))
                             for e in EDITIONS)
            print(f"| {assunto} | {sub} | {n} | {serie} |")
    return 0


if __name__ == "__main__":
    sys.exit(main())
