#!/usr/bin/env python3
"""Sincronizador do manifest.json para o banco (docs/figuras-estrategia.md).

Le `frontend/assets/figures/manifest.json` (arquivo + alt + página + crédito)
e sincroniza com a tabela `question_figures` (V8, sem coluna de status
desde V12 — servir figura é crédito + página, sem gate).

Namespace EAJ (TASK D.4): chaves `EAJ-<ano>-<n>` → `eaj/<ano>/Q<NN>.webp`
(B.3, 58 chaves); `page` DESCONHECIDA → NULL (nunca inventar, V20 §3).
Refs por (institution,year,number): EAJ-2022 ≠ IFRN-2022. Questão ausente
no banco (ex. EAJ-2025 Q22 excluída na D.3) = pular com aviso, nunca erro.

Idempotente: ON CONFLICT DO NOTHING + comparacao de checksum (file_path,
alt_text, page). Divergencia = sai com codigo 2 sem alterar nada.

Uso:
    python3 scripts/db/sync_figures.py [--check] [--institution IFRN|EAJ|ALL]
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


def parse_key(key: str) -> tuple[str, str, str] | None:
    """`2022-8` → (IFRN,2022,8); `EAJ-2022-8` → (EAJ,2022,8)."""
    parts = key.split("-")
    if len(parts) == 2 and parts[0].isdigit() and parts[1].isdigit():
        return ("IFRN", parts[0], str(int(parts[1])))
    if len(parts) == 3 and parts[0] == "EAJ" and parts[1].isdigit() \
            and parts[2].isdigit():
        return ("EAJ", parts[1], str(int(parts[2])))
    return None


def sql_page(page) -> str:
    # EAJ: "DESCONHECIDA" (string do manifest B.3) → NULL (V20 §3);
    # IFRN: int → int; resto → NULL (nunca inventar).
    if isinstance(page, int) and page > 0:
        return str(page)
    if isinstance(page, str) and page.isdigit() and int(page) > 0:
        return str(int(page))
    return "NULL"


def load_refs(env_vars: dict[str, str]) -> dict[tuple[str, str, str], int]:
    # Mapeia (institution, ano, numero) -> question_id (colisão 2022/2025).
    sql = ("SELECT e.institution, q.source_year, q.source_question_number, q.id "
           "FROM questions q JOIN exams e ON e.id = q.exam_id "
           "WHERE q.source_type = 'OFFICIAL'")
    out = psql(env_vars, sql)
    refs: dict[tuple[str, str, str], int] = {}
    for line in out.splitlines():
        if not line.strip():
            continue
        inst, yr, num, qid = line.split("|")
        refs[(inst, yr, str(int(num)))] = int(qid)
    return refs


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true",
                    help="Só valida manifest + estado do banco, sem escrever.")
    ap.add_argument("--institution", default="ALL", choices=["ALL", "IFRN", "EAJ"],
                    help="filtra chaves por processo (default ALL)")
    args = ap.parse_args()

    if not MANIFEST.exists():
        print(f"manifest ausente: {MANIFEST}", file=sys.stderr)
        return 1
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))

    figures = manifest.get("figures", {})
    if args.institution == "IFRN":
        wanted = {k: v for k, v in figures.items() if not k.startswith("EAJ-")}
    elif args.institution == "EAJ":
        wanted = {k: v for k, v in figures.items() if k.startswith("EAJ-")}
    else:
        wanted = figures

    # Só valida se --check
    if args.check:
        bad = [k for k in wanted if parse_key(k) is None]
        n_ifrn = sum(1 for k in wanted if not k.startswith("EAJ-"))
        n_eaj = sum(1 for k in wanted if k.startswith("EAJ-"))
        print(f"manifest OK: {len(wanted)} entradas "
              f"(IFRN {n_ifrn}, EAJ {n_eaj})")
        if bad:
            print(f"chaves fora do padrão: {bad}", file=sys.stderr)
            return 1
        return 0

    # Sincroniza com banco
    root = REPO_ROOT
    env_vars = load_dotenv(root)
    refs = load_refs(env_vars)
    inserted, skipped = 0, 0
    for key, entry in wanted.items():
        parsed = parse_key(key)
        if parsed is None:
            print(f"chave ignorada (formato): {key}", file=sys.stderr)
            continue
        qid = refs.get(parsed)
        if qid is None:
            # Questão ausente (ex. EAJ-2025 Q22 excluída na D.3): pular
            # com aviso — nunca erro, nunca imagem inventada (B.3).
            print(f"chave pulada (sem questao no banco): {key}")
            skipped += 1
            continue
        files = entry.get("files", [])
        file_path = files[0] if files else None
        alt_text = entry.get("alt", "")
        page = sql_page(entry.get("page"))
        if file_path is None or not alt_text.strip():
            print(f"chave ignorada (sem arquivo/alt): {key}")
            continue
        # Inserta idempotente (sem status desde V12)
        sql = (f"INSERT INTO question_figures "
               f"(question_id, position, file_path, alt_text, page) "
               f"VALUES ({qid}, 1, '{file_path.replace(chr(39), chr(39)+chr(39))}', "
               f"'{alt_text.replace(chr(39), chr(39)+chr(39))}', {page}) "
               f"ON CONFLICT (question_id, position) DO NOTHING;")
        psql(env_vars, sql)
        inserted += 1
    print(f"sincronizado: {inserted} figuras (de {len(wanted)} no manifest; "
          f"puladas sem questão: {skipped})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
