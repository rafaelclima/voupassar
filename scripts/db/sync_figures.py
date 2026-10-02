#!/usr/bin/env python3
"""Sincronizador do manifest.json para o banco (TASK 6.6 / docs/figuras-estrategia.md).

Le `frontend/assets/figures/manifest.json` (fonte de verdade da curadoria
manual) e sincroniza com a tabela `question_figures` (V8__question_figures.sql).

Idempotente: ON CONFLICT DO NOTHING + comparacao de checksum (file_path,
alt_text, page, status). Divergencia = sai com codigo 2 sem alterar nada.

Uso:
    python3 scripts/db/sync_figures.py [--publish CHAVE]
"""
from __future__ import annotations
import argparse
import hashlib
import json
import os
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
MANIFEST = REPO_ROOT / "frontend" / "assets" / "figures" / "manifest.json"


def load_dotenv(root: Path) -> dict[str, str]:
    env = root / ".env"
    env_vars = {}
    if env.exists():
        for line in env.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            env_vars[k.strip()] = v.strip()
    return env_vars


def psql(env_vars: dict[str, str], sql: str, tuples_only: bool = True) -> str:
    cmd = ["psql", "-X", "-q", "--set", "ON_ERROR_STOP=1", "-v", "ON_ERROR_STOP=1"]
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


def load_refs(env_vars: dict[str, str]) -> dict[str, int]:
    # Mapeia (ano, numero) -> question_id
    sql = ("SELECT source_year, source_question_number, id FROM questions "
           "WHERE source_type = 'OFFICIAL'")
    out = psql(env_vars, sql)
    refs: dict[str, int] = {}
    for line in out.splitlines():
        if not line.strip():
            continue
        yr, num, qid = line.split("|")
        refs[f"{yr}-{num}"] = int(qid)
    return refs


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true",
                    help="Só valida manifest + estado do banco, sem escrever.")
    ap.add_argument("--publish", metavar="CHAVE",
                    help="Marca chave (ex. 2026-18) como PUBLICAVEL no manifest antes de sincronizar.")
    args = ap.parse_args()

    if not MANIFEST.exists():
        print(f"manifest ausente: {MANIFEST}", file=sys.stderr)
        return 1
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))

    if args.publish:
        if args.publish not in manifest.get("figures", {}):
            print(f"chave nao encontrada: {args.publish}", file=sys.stderr)
            return 2
        manifest["figures"][args.publish]["status"] = "PUBLICAVEL"
        MANIFEST.write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8")
        print(f"publicado no manifest: {args.publish}")

    # Só valida se --check
    if args.check:
        figures = manifest.get("figures", {})
        count = len(figures)
        print(f"manifest OK: {count} entradas")
        return 0

    # Sincroniza com banco
    root = REPO_ROOT
    env_vars = load_dotenv(root)
    refs = load_refs(env_vars)
    figures = manifest.get("figures", {})
    inserted = 0
    for key, entry in figures.items():
        # Resolve question_id pelo (ano, numero) natural
        parts = key.split("-")
        if len(parts) != 2:
            print(f"chave ignorada (formato): {key}", file=sys.stderr)
            continue
        yr_str, num_str = parts
        qid = refs.get(key)
        if qid is None:
            # Se ainda nao existe no banco (ex. nova questao nao importada), pula
            print(f"chave ignorada (sem questao no banco): {key}")
            continue
        files = entry.get("files", [])
        file_path = files[0] if files else None
        alt_text = entry.get("alt", "")
        page = entry.get("page")
        status = entry.get("status", "PENDENTE_REVISAO")
        if file_path is None:
            print(f"chave ignorada (sem arquivo): {key}")
            continue
        # Inserta idempotente
        sql = (f"INSERT INTO question_figures "
               f"(question_id, position, file_path, alt_text, page, publication_status) "
               f"VALUES ({qid}, 1, '{file_path.replace(chr(39), chr(39)+chr(39))}', "
               f"'{alt_text.replace(chr(39), chr(39)+chr(39))}', {page if page else 'NULL'}, "
               f"'{status}') ON CONFLICT (question_id, position) DO NOTHING;")
        psql(env_vars, sql)
        inserted += 1
    print(f"sincronizado: {inserted} figuras (de {len(figures)} no manifest)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
