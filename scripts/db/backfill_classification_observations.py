#!/usr/bin/env python3
"""Backfill idempotente de `question_classifications.observation`.

Fonte de verdade: `docs/content-analysis/per-edition/<edicao>.json`
(campo `classificacoes[].observacao` da TASK 1.4). O importador
(`import_questions.py`) agora grava essa coluna na inserção; este script
reaplica o mesmo valor às linhas já existentes sem reescrever conteúdo.

Regras:
  * Nunca inventa: atualiza apenas quando o JSON tem observação não-vazia.
  * Só sobrescreve NULL/vazio no banco (primeira aplicação); para forcar
    reconciliação total use `--force`.
  * Auditável: relatório JSON com contagens e ids afetados.
"""

from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CLASSIF_DIR = REPO_ROOT / "docs" / "content-analysis" / "per-edition"
EDITIONS = ["2020", "2022", "2023", "2024", "2025", "2026"]


def load_dotenv(root: Path) -> None:
    env = root / ".env"
    if not env.exists():
        return
    for line in env.read_text(encoding="utf-8").splitlines():
        s = line.strip()
        if not s or s.startswith("#") or "=" not in s:
            continue
        k, v = s.split("=", 1)
        os.environ.setdefault(k.strip(), v.strip())


def pg_env() -> dict[str, str]:
    load_dotenv(REPO_ROOT)
    for var in ("POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD"):
        if not os.environ.get(var):
            raise RuntimeError(f"{var} ausente")
    env = dict(os.environ)
    env["PGHOST"] = os.environ.get("POSTGRES_HOST", "localhost")
    env["PGPORT"] = os.environ.get("POSTGRES_PORT", "5432")
    env["PGDATABASE"] = os.environ["POSTGRES_DB"]
    env["PGUSER"] = os.environ["POSTGRES_USER"]
    env["PGPASSWORD"] = os.environ["POSTGRES_PASSWORD"]
    return env


def psql(env: dict[str, str], sql: str) -> str:
    p = subprocess.run(
        ["psql", "-X", "-q", "-A", "-t", "--set", "ON_ERROR_STOP=1", "-c", sql],
        capture_output=True, text=True, env=env)
    if p.returncode != 0:
        raise RuntimeError(p.stderr.strip()[:2000])
    return p.stdout


def esc(text: str) -> str:
    return "'" + text.replace("'", "''") + "'"


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    ap.add_argument("--force", action="store_true")
    ap.add_argument("--report", default=str(REPO_ROOT / "data" / "import" / "backfill-observations.json"))
    args = ap.parse_args()

    env = pg_env()
    rows = psql(env, "SELECT id, question_id, taxonomy_version, observation FROM question_classifications WHERE origin='CLASSIFICACAO_DERIVADA_FONTE'").splitlines()
    by_q = {}
    for r in rows:
        parts = r.split("|", 3)
        by_q[int(parts[1])] = {"id": int(parts[0]), "tax": parts[2], "obs": parts[3] if len(parts) > 3 else ""}

    updates = []
    for ed in EDITIONS:
        clf = json.loads((CLASSIF_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        for c in clf["classificacoes"]:
            obs = (c.get("observacao") or "").strip()
            if not obs:
                continue
            # question_id via statement -> use sessao via source_year+number
            updates.append((ed, int(c["number"]), obs))

    affected = 0
    report_updates = []
    for ed, num, obs in updates:
        sql_fetch = (
            f"SELECT qc.id, qc.observation FROM question_classifications qc "
            f"JOIN questions q ON q.id = qc.question_id "
            f"WHERE q.source_year = {ed} AND q.source_question_number = {num} "
            f"AND qc.origin='CLASSIFICACAO_DERIVADA_FONTE' LIMIT 1"
        )
        out = psql(env, sql_fetch).strip()
        if not out:
            continue
        qid, current = out.split("|", 1) if "|" in out else (out, "")
        if current and current.strip() and not args.force:
            report_updates.append({"year": ed, "number": num, "action": "skipped_has_value"})
            continue
        if args.check:
            report_updates.append({"year": ed, "number": num, "action": "would_update"})
            affected += 1
            continue
        sql = (
            f"UPDATE question_classifications SET observation = {esc(obs)} "
            f"WHERE id = {qid}"
        )
        psql(env, sql)
        report_updates.append({"year": ed, "number": num, "action": "updated"})
        affected += 1

    report = {
        "script": "scripts/db/backfill_classification_observations.py",
        "check_only": args.check,
        "force": args.force,
        "candidates": len(updates),
        "affected": affected,
        "updates": report_updates,
    }
    Path(args.report).write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: v for k, v in report.items() if k != "updates"}, ensure_ascii=False))
    print(f"relatorio: {args.report}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except RuntimeError as e:
        print(f"ERRO: {e}", file=sys.stderr)
        sys.exit(2)
