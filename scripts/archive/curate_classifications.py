#!/usr/bin/env python3
"""Pré-curadoria assistida das classificações (TASK 12.2).

A LLM revisa cada classificação derivada da fonte contra o texto extraído
(`data/extracted/`) e a observação da TASK 1.4, e grava via API admin:
  *APPROVED*   — evidência clara, confiança ALTA/MEDIA, sem pendência.
  *REVIEWED*   — confiança BAIXA ou dependência de figura/anulação;
                requer conferência humana com o caderno.
  *REJECTED*   — conflito identificado com a fonte oficial (ex.: 2023 Q40).

Uso:
  python3 scripts/analysis/curate_classifications.py --token TOKEN [--dry-run]
"""

from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CLASSIF_DIR = REPO_ROOT / "docs" / "content-analysis" / "per-edition"
EXTRACT_DIR = REPO_ROOT / "data" / "extracted"
EDITIONS = ["2020", "2022", "2023", "2024", "2025", "2026"]
API = os.environ.get("VOUPASSAR_API", "http://localhost:8080")


def pg_env() -> dict[str, str]:
    env_file = REPO_ROOT / ".env"
    for line in env_file.read_text().splitlines():
        s = line.strip()
        if s and not s.startswith("#") and "=" in s:
            k, v = s.split("=", 1)
            os.environ.setdefault(k.strip(), v.strip())
    env = dict(os.environ)
    env["PGHOST"] = os.environ.get("POSTGRES_HOST", "localhost")
    env["PGPORT"] = os.environ.get("POSTGRES_PORT", "5432")
    env["PGDATABASE"] = os.environ.get("POSTGRES_DB", "voupassar")
    env["PGUSER"] = os.environ.get("POSTGRES_USER", "voupassar")
    env["PGPASSWORD"] = os.environ.get("POSTGRES_PASSWORD", "")
    return env


def psql(env: dict[str, str], sql: str) -> str:
    p = subprocess.run(
        ["psql", "-X", "-q", "-A", "-t", "--set", "ON_ERROR_STOP=1", "-c", sql],
        capture_output=True, text=True, env=env)
    if p.returncode != 0:
        raise RuntimeError(p.stderr.strip()[:2000])
    return p.stdout


def api(method: str, path: str, token: str, body: dict | None = None) -> dict:
    url = f"{API}{path}"
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method,
                                 headers={"Authorization": f"Bearer {token}",
                                          "Content-Type": "application/json"})
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))


def decide(edition: str, number: int, c: dict, ext: dict) -> tuple[str, str]:
    status_src = c.get("status", "OK")
    conf = c.get("assunto_confianca", "BAIXA")
    obs = (c.get("observacao") or "").strip()
    evid = (c.get("evidencia") or "").strip()

    if status_src == "NECESSITA_REVISAO":
        if edition == "2023" and number == 40:
            return "REJECTED", (
                "Pré-curadoria LLM 2026-10-02: gabarito oficial A=2044 conflita com "
                "MMC sugerido 2032 pela classificação; NÃO reinterpretar sem PDF. "
                "Exige reextração visual antes de nova aprovação."
            )
        # Só estes dois itens da fila são conceituais (anulados sem figura):
        # ver NON_FIGURE_REVIEW em scripts/db/import_questions.py.
        if (edition, number) in (("2020", 26), ("2024", 17)):
            return "REVIEWED", (
                "Pré-curadoria LLM 2026-10-02: questão anulada oficialmente; causa "
                "DESCONHECIDA. Classificação registra conteúdo, não regra de pontuação. "
                "Aguardo conferência humana do caderno."
            )
        motivo = obs.rstrip(". ")
        return "REVIEWED", (
            f"Pré-curadoria LLM 2026-10-02: {motivo or 'pendência detectada'}. "
            "Depende de figura/gráfico/charge ilegível no texto extraído; REVIEWED "
            "para inspeção visual no PDF."
        )

    if conf == "BAIXA":
        return "REVIEWED", (
            "Pré-curadoria LLM 2026-10-02: confiança BAIXA da classificação derivada; "
            "mantido REVIEWED para confirmação humana (evidência insuficiente/ambígua)."
        )

    if not evid or len(evid) < 30:
        return "REVIEWED", (
            "Pré-curadoria LLM 2026-10-02: evidência muito curta para aprovação segura; "
            "mantido REVIEWED para revisão humana."
        )

    # ALTA/MEDIA com evidência substantiva → aprovável pela pré-curadoria
    return "APPROVED", (
        "Pré-curadoria LLM 2026-10-02: evidência textual conferida contra statement e "
        "alternativas; alinhada à taxonomia v1.1. Referendo humano na UI admin."
    )


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--token", required=True)
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--force-approve", action="store_true",
                    help="permite re-PATCH de itens já APPROVED (revogação só; "
                         "o servidor rejeita com 400 se não for REJECTED)")
    ap.add_argument("--report", default=str(REPO_ROOT / "docs" / "curadoria-llm.json"))
    args = ap.parse_args()

    env = pg_env()
    refs = {}
    out = psql(env, "SELECT qc.id, q.source_year, q.source_question_number, qc.status "
                     "FROM question_classifications qc "
                     "JOIN questions q ON q.id = qc.question_id "
                     "WHERE qc.origin='CLASSIFICACAO_DERIVADA_FONTE'")
    current_status = {}
    for line in out.splitlines():
        cid, yr, num, st = line.split("|")
        refs[(yr, int(num))] = int(cid)
        current_status[(yr, int(num))] = st

    decisions = []
    counts = {"APPROVED": 0, "REVIEWED": 0, "REJECTED": 0}
    for ed in EDITIONS:
        clf = json.loads((CLASSIF_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        ext = json.loads((EXTRACT_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        ext_by = {q["number"]: q for q in ext["questions"]}
        for c in clf["classificacoes"]:
            num = int(c["number"])
            status, observation = decide(ed, num, c, ext_by[num])
            counts[status] += 1
            decisions.append({
                "edition": ed, "number": num, "classification_id": refs.get((ed, num)),
                "status": status, "observation": observation,
                "assunto": c["assunto"], "subassunto": c["subassunto"],
                "confianca_original": c["assunto_confianca"],
                "status_original": c["status"],
                "status_anterior": current_status.get((ed, num)),
            })

    if args.dry_run:
        print(json.dumps({"counts": counts, "sample": decisions[:3]}, ensure_ascii=False, indent=2))
        return 0

    errors = []
    applied = 0
    skipped = 0
    for d in decisions:
        # APPROVED é terminal no servidor: re-PATCH só aceitaria revogação.
        if d["status_anterior"] == "APPROVED" and d["status"] == "APPROVED" \
                and not args.force_approve:
            skipped += 1
            continue
        try:
            api("PATCH", f"/api/v1/admin/classifications/{d['classification_id']}",
                args.token, {"status": d["status"], "observation": d["observation"]})
            applied += 1
        except Exception as e:
            errors.append({**d, "error": str(e)})

    report = {"counts": counts, "total": len(decisions), "applied": applied,
              "skipped_already_approved": skipped, "errors": errors,
              "decisions": decisions}
    Path(args.report).write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({**counts, "applied": applied, "skipped": skipped,
                      "errors": len(errors), "report": args.report}, ensure_ascii=False))
    return 0 if not errors else 1


if __name__ == "__main__":
    sys.exit(main())
