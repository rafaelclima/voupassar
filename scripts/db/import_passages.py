#!/usr/bin/env python3
"""Importador idempotente de textos-base — TASK 6.9 / docs/passagens-estrategia.md.

Lê os JSONs curados `data/passages/<ano>.json` (transcrição literal conferida
contra o caderno; validados por `extract_passages.py --check`) e carrega as
tabelas V9 (`passages` + `question_passages`).

Idempotente: chave natural (exam_id, passage_key) + (question_id, passage_id)
com ON CONFLICT DO NOTHING. Divergência de checksum = sai com código 2 SEM
alterar nada (nunca UPDATE silencioso — mesma regra do importador TASK 2.3).

Uso:
    python3 scripts/db/import_passages.py --check   # valida sem escrever
    python3 scripts/db/import_passages.py           # importa (idempotente)
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import subprocess
import sys
import unicodedata
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
PASSAGES_DIR = REPO_ROOT / "data" / "passages"


def load_dotenv(root: Path) -> dict[str, str]:
    env_vars: dict[str, str] = {}
    env = root / ".env"
    if env.exists():
        for line in env.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            env_vars[k.strip()] = v.strip()
    return env_vars


def psql(env_vars: dict[str, str], sql: str, tuples_only: bool = True) -> str:
    cmd = ["psql", "-X", "-q", "--set", "ON_ERROR_STOP=1"]
    if tuples_only:
        cmd += ["-A", "-t"]
    cmd += ["-c", sql]
    full_env = dict(os.environ)
    full_env.update({
        "PGHOST": env_vars.get("POSTGRES_HOST", "localhost"),
        "PGPORT": env_vars.get("POSTGRES_PORT", "5432"),
        "PGDATABASE": env_vars.get("POSTGRES_DB", ""),
        "PGUSER": env_vars.get("POSTGRES_USER", ""),
        "PGPASSWORD": env_vars.get("POSTGRES_PASSWORD", ""),
    })
    p = subprocess.run(cmd, capture_output=True, text=True, env=full_env)
    if p.returncode != 0:
        raise SystemExit(f"psql falhou: {p.stderr.strip()[:2000]}")
    return p.stdout


def esc(text: str | None) -> str:
    if text is None:
        return "NULL"
    return "'" + text.replace("'", "''") + "'"


def norm(text: str | None) -> str:
    if not text:
        return ""
    t = unicodedata.normalize("NFC", text)
    return " ".join(t.split())


def checksum(p: dict) -> str:
    base = "|".join([
        p["key"], p["kind"], p["label"] or "",
        p.get("title") or "", p.get("byline") or "", p.get("subtitle") or "",
        p.get("intro") or "", p.get("content") or "",
        p.get("visual_description") or "", p.get("format_note") or "",
        p.get("source_note") or "",
        str(p["page_start"]), str(p["page_end"]),
    ])
    return hashlib.sha256(norm(base).encode("utf-8")).hexdigest()


def load_files() -> list[dict]:
    items = []
    for f in sorted(PASSAGES_DIR.glob("*.json")):
        d = json.loads(f.read_text(encoding="utf-8"))
        for p in d.get("passages", []):
            items.append({"edition": d["edition"], **p})
    return items


def main() -> int:
    ap = argparse.ArgumentParser(description="Importador idempotente de textos-base")
    ap.add_argument("--check", action="store_true", help="valida sem escrever")
    args = ap.parse_args()

    items = load_files()
    if not items:
        print("nada a importar: data/passages/*.json ausente ou vazio")
        return 1

    env_vars = load_dotenv(REPO_ROOT)
    exams = {}
    for row in psql(env_vars, "SELECT id, year FROM exams;").strip().splitlines():
        if row.strip():
            eid, yr = row.split("|")
            exams[int(yr)] = int(eid)
    qids: dict[tuple[int, int], int] = {}
    for row in psql(
        env_vars,
        "SELECT source_year, source_question_number, id FROM questions "
        "WHERE source_type = 'OFFICIAL';",
    ).strip().splitlines():
        if row.strip():
            yr, num, qid = row.split("|")
            qids[(int(yr), int(num))] = int(qid)

    existing: dict[tuple[int, str], str] = {}
    for row in psql(
        env_vars,
        "SELECT e.year, p.passage_key, p.checksum FROM passages p "
        "JOIN exams e ON e.id = p.exam_id;",
    ).strip().splitlines():
        if row.strip():
            yr, key, chk = row.split("|")
            existing[(int(yr), key)] = chk
    existing_links: set[tuple[int, int]] = set()
    for row in psql(
        env_vars, "SELECT question_id, passage_id FROM question_passages;"
    ).strip().splitlines():
        if row.strip():
            q, pg = row.split("|")
            existing_links.add((int(q), int(pg)))

    to_insert, to_link, divergences = [], [], []
    passage_ids: dict[tuple[int, str], int] = {}
    for it in items:
        yr = it["edition"]
        if yr not in exams:
            print(f"ERRO: edição {yr} inexistente na tabela exams", file=sys.stderr)
            return 1
        key = (yr, it["key"])
        chk = checksum(it)
        if key in existing:
            if existing[key] != chk:
                divergences.append(f"{yr}:{it['key']}")
            continue
        to_insert.append(it)
        for qn in it.get("questions", []):
            qid = qids.get((yr, qn))
            if qid is None:
                print(f"ERRO: questão {yr} Q{qn} inexistente", file=sys.stderr)
                return 1

    if divergences:
        print("DIVERGÊNCIAS (nada alterado):")
        for dv in divergences:
            print(" -", dv)
        return 2

    if args.check:
        print(f"OK (--check): {len(to_insert)} passagens a inserir, "
              f"{sum(len(i.get('questions', [])) for i in to_insert)} vínculos; "
              f"{len(existing)} já presentes, 0 divergências")
        return 0

    for it in to_insert:
        eid = exams[it["edition"]]
        cols = ("exam_id", "passage_key", "label", "kind", "title", "byline",
                "subtitle", "intro", "content", "visual_description",
                "format_note", "source_note", "page_start", "page_end", "checksum")
        vals = (str(eid), esc(it["key"]), esc(it["label"]), esc(it["kind"]),
                esc(it.get("title")), esc(it.get("byline")), esc(it.get("subtitle")),
                esc(it.get("intro")), esc(it.get("content")),
                esc(it.get("visual_description")), esc(it.get("format_note")),
                esc(it.get("source_note")),
                str(it["page_start"]), str(it["page_end"]), esc(checksum(it)))
        out = psql(
            env_vars,
            f"INSERT INTO passages ({', '.join(cols)}) VALUES ({', '.join(vals)}) "
            f"ON CONFLICT (exam_id, passage_key) DO NOTHING RETURNING id;",
        ).strip()
        if out:
            passage_ids[(it["edition"], it["key"])] = int(out.splitlines()[0])

    # Resolve ids (inclui recém-inseridos) e insere vínculos.
    idmap: dict[tuple[int, str], int] = {}
    for row in psql(
        env_vars,
        "SELECT e.year, p.passage_key, p.id FROM passages p "
        "JOIN exams e ON e.id = p.exam_id;",
    ).strip().splitlines():
        if row.strip():
            yr, key, pid = row.split("|")
            idmap[(int(yr), key)] = int(pid)
    links = 0
    for it in items:
        pid = idmap.get((it["edition"], it["key"]))
        if pid is None:
            continue
        pos = 0
        for qn in it.get("questions", []):
            pos += 1
            qid = qids[(it["edition"], qn)]
            if (qid, pid) in existing_links:
                continue
            psql(
                env_vars,
                "INSERT INTO question_passages (question_id, passage_id, position) "
                f"VALUES ({qid}, {pid}, {pos}) "
                "ON CONFLICT (question_id, passage_id) DO NOTHING;",
            )
            links += 1

    print(f"OK: {len(to_insert)} passagens inseridas, {links} vínculos novos; "
          f"{len(existing)} já presentes, 0 divergências")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
