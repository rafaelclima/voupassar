#!/usr/bin/env python3
"""Importador das questoes autorais para o PostgreSQL — TASK 15.3.

Le (somente leitura, nunca altera a fonte):
  - `data/authoral/specs/*.json` (contrato em
    `docs/authoral/manual-item-v1.md`, gate em
    `scripts/authoral/validate_authoral.py --check`, executado aqui como
    pré-requisito: spec vermelho = aborto, sem escrita).

Escreve (via `psql`, transacoes com ON_ERROR_STOP):
  - `questions` (source_type AUTHORAL, sem vinculo de edicao, paginas NULL),
  - `question_options` (4 por questao),
  - `question_classifications` (1 por questao, taxonomy_version v1.1,
    origin AUTORAL_DERIVADA_PERFIL, confidence MEDIA).

Sem linhas em `question_sources` (exige exam_document_id NOT NULL: so faz
sentido para fontes oficiais). A proveniencia da autoral vive em
`classifications.observation` (spec + refs oficiais) + `questions.checksum`
+ `questions.pipeline_version`. Evidencia = citacao genuina (LP: trecho do
texto_base; MAT: enunciado), truncada em 200 caracteres como nas oficiais.

Propriedades exigidas pela TASK:
  * repetivel  — mesmos inputs geram os mesmos efeitos;
  * idempotente — reexecucao nao duplica nada: chave = `checksum` (UNIQUE
    no banco) + `spec=<id>` em observation (spec conhecido com checksum
    diferente = DIVERGENCIA registrada, sem UPDATE/DELETE silencioso,
    saida com codigo 2);
  * auditavel  — relatorio JSON (`--report`) + validacoes que falham alto.

Checksum: SHA-256 hex de enunciado + alternativas A-D normalizados
(NFC Unicode + colapso de whitespace). Definicao unica, identica a de
scripts/db/import_questions.py e scripts/authoral/validate_authoral.py.

Confidence MEDIA (nem ALTA nem BAIXA): codigos v1.1 validos e molde
documentado com refs oficiais (nao e palpite), mas sem verificacao
independente e sem dados de desempenho (nao e carimbo). BAIXA esta fora
de questao: marcaria "assunto NAO CONFIRMADO" nas respostas da API.

Uso:
    python3 scripts/db/import_authoral.py [--check] [--report PATH] [--specs DIR]
    DB via env POSTGRES_HOST/PORT/DB/USER/PASSWORD (ou .env na raiz).
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
import unicodedata
from datetime import datetime, timezone
from pathlib import Path

IMPORTER_VERSION = "1.0.0"
PIPELINE_VERSION = "authoral-1.0.0"
TAXONOMY_VERSION = "v1.1"
ORIGIN = "AUTORAL_DERIVADA_PERFIL"
CONFIDENCE = "MEDIA"

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SPECS = REPO_ROOT / "data" / "authoral" / "specs"
VALIDATOR = REPO_ROOT / "scripts" / "authoral" / "validate_authoral.py"
DEFAULT_REPORT = REPO_ROOT / "data" / "authoral" / "report.json"

SPEC_RE = re.compile(r"spec=([A-Za-z0-9-]+)")


class ImportFailure(RuntimeError):
    pass


# ---------------- conexao ----------------

def load_dotenv(root: Path) -> None:
    env = root / ".env"
    if not env.exists():
        return
    for line in env.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        os.environ.setdefault(k.strip(), v.strip())


def pg_env() -> dict[str, str]:
    load_dotenv(REPO_ROOT)
    for var in ("POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD"):
        if not os.environ.get(var):
            raise ImportFailure(f"variavel {var} ausente (.env ou ambiente)")
    env = dict(os.environ)
    env["PGHOST"] = os.environ.get("POSTGRES_HOST", "localhost")
    env["PGPORT"] = os.environ.get("POSTGRES_PORT", "5432")
    env["PGDATABASE"] = os.environ["POSTGRES_DB"]
    env["PGUSER"] = os.environ["POSTGRES_USER"]
    env["PGPASSWORD"] = os.environ["POSTGRES_PASSWORD"]
    return env


def psql_file(env: dict[str, str], path: Path) -> None:
    cmd = ["psql", "-X", "-q", "-v", "ON_ERROR_STOP=1", "-f", str(path)]
    p = subprocess.run(cmd, capture_output=True, text=True, env=env)
    if p.returncode != 0:
        raise ImportFailure(f"psql -f falhou: {p.stderr.strip()[:2000]}")


def psql(env: dict[str, str], sql: str, tuples_only: bool = True) -> str:
    cmd = ["psql", "-X", "-q", "--set", "ON_ERROR_STOP=1", "-v", "ON_ERROR_STOP=1"]
    if tuples_only:
        cmd += ["-A", "-t"]
    cmd += ["-c", sql]
    p = subprocess.run(cmd, capture_output=True, text=True, env=env)
    if p.returncode != 0:
        raise ImportFailure(f"psql falhou: {p.stderr.strip()[:2000]}")
    return p.stdout


# ---------------- helpers ----------------

def esc(text: str) -> str:
    """Literal SQL seguro (standard_conforming_strings=on: so ' dobra)."""
    return "'" + text.replace("'", "''") + "'"


def norm(text: str) -> str:
    return " ".join(unicodedata.normalize("NFC", text).split())


def checksum(statement: str, options: dict[str, str]) -> str:
    payload = norm(statement) + "\n" + "\n".join(
        f"{lbl}={norm(options[lbl])}" for lbl in ("A", "B", "C", "D")
    )
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def gate_specs(specs_dir: Path) -> None:
    """Roda o validador autoral como gate: vermelho = aborto sem escrita."""
    p = subprocess.run(
        [sys.executable, str(VALIDATOR), "--check", "--specs", str(specs_dir)],
        capture_output=True, text=True)
    if p.returncode != 0:
        raise ImportFailure(
            f"gate validate_authoral.py reprovou (exit {p.returncode}):\n"
            f"{(p.stdout + p.stderr).strip()[:2000]}")


def load_specs(specs_dir: Path) -> list[dict]:
    files = sorted(specs_dir.glob("*.json"))
    if not files:
        raise ImportFailure(f"nenhum spec em {specs_dir}")
    items = []
    for path in files:
        spec = json.loads(path.read_text(encoding="utf-8"))
        opts = spec["options"]
        ev_source = spec["texto_base"] if spec["discipline"] == "LINGUA_PORTUGUESA" \
            else spec["enunciado"]
        items.append({
            "id": spec["id"],
            "discipline": spec["discipline"],
            "topic": spec["topic"], "subtopic": spec["subtopic"],
            "skill": spec["skill"], "reasoning": spec["reasoning_type"],
            "difficulty": spec["difficulty_alvo"],
            "refs": list(spec["referencias_oficiais"]),
            "statement": spec["enunciado"], "options": dict(opts),
            "answer_key": spec["answerKey"],
            "evidence": norm(ev_source)[:200],
            "observation": (
                f"AUTORAL-DERIVADA-PERFIL spec={spec['id']} "
                f"refs={','.join(spec['referencias_oficiais'])} "
                f"manual=item-v1"),
            "checksum": checksum(spec["enunciado"], opts),
        })
    return items


# ---------------- estado do banco ----------------

def check_schema(env: dict[str, str]) -> None:
    """Falha alto se o banco nao tem o contrato autoral (pre-V13)."""
    out = psql(env,
        "SELECT conname FROM pg_constraint WHERE conrelid = 'questions'::regclass")
    names = set(out.split())
    for need in ("chk_questions_official_pages", "chk_questions_authoral_nulls"):
        if need not in names:
            raise ImportFailure(
                f"banco sem constraint {need} (pre-V13__authoral_pipeline); "
                f"aplicar database/migrations ate V13 antes de importar")
    cols = psql(env,
        "SELECT column_name FROM information_schema.columns "
        "WHERE table_name = 'questions'").split()
    if "pipeline_version" not in cols:
        raise ImportFailure("banco anterior a V12 (sem pipeline_version)")


def fetch_rows(env: dict[str, str], sql: str) -> list[list[str]]:
    out = psql(env, sql)
    return [l.split("|") for l in out.splitlines() if l.strip()]


def load_refs(env: dict[str, str], items: list[dict]) -> dict:
    discs = {r[0]: int(r[1])
             for r in fetch_rows(env, "SELECT code, id FROM disciplines")}
    topics = {(r[0], r[1]): int(r[2]) for r in fetch_rows(
        env, "SELECT d.code, t.code, t.id FROM topics t "
             "JOIN disciplines d ON d.id = t.discipline_id")}
    subtopics = {(r[0], r[1], r[2]): int(r[3]) for r in fetch_rows(
        env, "SELECT d.code, t.code, s.code, s.id FROM subtopics s "
             "JOIN topics t ON t.id = s.topic_id "
             "JOIN disciplines d ON d.id = t.discipline_id")}
    for it in items:
        d = it["discipline"]
        if d not in discs:
            raise ImportFailure(f"disciplina {d} ausente no banco")
        if (d, it["topic"]) not in topics:
            raise ImportFailure(
                f"{it['id']}: topico {d}/{it['topic']} ausente no banco")
        if (d, it["topic"], it["subtopic"]) not in subtopics:
            raise ImportFailure(
                f"{it['id']}: subtopico {d}/{it['topic']}/{it['subtopic']} "
                f"ausente no banco")
    return {"discs": discs, "topics": topics, "subtopics": subtopics}


def fetch_existing(env: dict[str, str]) -> tuple[dict, dict]:
    """Retorna (por_checksum, spec_id -> {id, checksum}) das autorais + checksums oficiais."""
    by_cks: dict[str, dict] = {}
    out = psql(env,
        "SELECT id, source_type, checksum FROM questions")
    for line in out.splitlines():
        if not line.strip():
            continue
        qid, stype, cks = line.split("|")
        by_cks[cks] = {"id": int(qid), "source_type": stype}
    spec_ids: dict[str, dict] = {}
    out = psql(env,
        "SELECT q.id, q.checksum, cl.observation FROM questions q "
        "JOIN question_classifications cl ON cl.question_id = q.id "
        "WHERE q.source_type = 'AUTHORAL'")
    for line in out.splitlines():
        if not line.strip():
            continue
        qid, cks, obs = line.split("|", 2)
        m = SPEC_RE.search(obs or "")
        if m:
            spec_ids[m.group(1)] = {"id": int(qid), "checksum": cks}
    return by_cks, spec_ids


# ---------------- geracao SQL ----------------

def sql_insert_questions(new: list[dict], refs: dict) -> str:
    stmts = []
    for it in new:
        disc_id = refs["discs"][it["discipline"]]
        stmts.append(
            "INSERT INTO questions (source_type, exam_id, exam_document_id, "
            "source_year, source_question_number, statement, kind, "
            "discipline_id, page_start, page_end, answer_key, annulled, "
            "checksum, has_figure, difficulty_estimate, "
            "pipeline_version) VALUES "
            f"('AUTHORAL', NULL, NULL, NULL, NULL, {esc(it['statement'])}, "
            f"'OBJECTIVE', {disc_id}, NULL, NULL, {esc(it['answer_key'])}, "
            f"FALSE, {esc(it['checksum'])}, FALSE, {esc(it['difficulty'])}, "
            f"{esc(PIPELINE_VERSION)}) "
            "ON CONFLICT DO NOTHING;")
    return "\n".join(stmts)


def sql_insert_children(items: list[dict], refs: dict, existing: dict) -> str:
    opt_counts: dict[int, int] = {}
    out = psql_cached_counts.get("options", "")
    for line in out.splitlines():
        if line.strip():
            qid, n = line.split("|")
            opt_counts[int(qid)] = int(n)
    clf_present = set()
    for line in psql_cached_counts.get("classif", "").splitlines():
        if line.strip():
            clf_present.add(int(line.strip()))

    stmts = []
    for it in items:
        qid = existing[it["checksum"]]["id"]
        if opt_counts.get(qid, 0) < 4:
            for lbl in "ABCD":
                stmts.append(
                    "INSERT INTO question_options (question_id, label, "
                    f"option_text) VALUES ({qid}, '{lbl}', "
                    f"{esc(it['options'][lbl])}) ON CONFLICT DO NOTHING;")
        if qid not in clf_present:
            topic_id = refs["topics"][(it["discipline"], it["topic"])]
            sub_id = refs["subtopics"][(it["discipline"], it["topic"],
                                        it["subtopic"])]
            stmts.append(
                "INSERT INTO question_classifications (question_id, "
                "taxonomy_version, topic_id, subtopic_id, skill, "
                "reasoning_type, confidence, evidence, origin, observation) "
                f"VALUES ({qid}, '{TAXONOMY_VERSION}', {topic_id}, {sub_id}, "
                f"{esc(it['skill'])}, {esc(it['reasoning'])}, "
                f"'{CONFIDENCE}', {esc(it['evidence'])}, "
                f"'{ORIGIN}', {esc(it['observation'])});")
    return "\n".join(stmts)


psql_cached_counts: dict[str, str] = {}


def refresh_child_counts(env: dict[str, str]) -> None:
    psql_cached_counts["options"] = psql(
        env, "SELECT question_id, count(*) FROM question_options "
             "WHERE question_id IN (SELECT id FROM questions "
             "WHERE source_type = 'AUTHORAL') GROUP BY 1")
    psql_cached_counts["classif"] = psql(
        env, "SELECT question_id FROM question_classifications "
             "WHERE question_id IN (SELECT id FROM questions "
             "WHERE source_type = 'AUTHORAL')")


# ---------------- verificacao final ----------------

def verify(env: dict[str, str], items: list[dict]) -> dict:
    by_cks, _ = fetch_existing(env)
    missing = [it["id"] for it in items if it["checksum"] not in by_cks]
    wrong_type = [it["id"] for it in items
                  if it["checksum"] in by_cks
                  and by_cks[it["checksum"]]["source_type"] != "AUTHORAL"]
    official = psql(env, "SELECT count(*) FROM questions "
                         "WHERE source_type = 'OFFICIAL'").strip()
    authoral = psql(env, "SELECT count(*) FROM questions "
                         "WHERE source_type = 'AUTHORAL'").strip()
    opt = psql(env, "SELECT count(*) FROM question_options o JOIN questions q "
                    "ON q.id = o.question_id "
                    "WHERE q.source_type = 'AUTHORAL'").strip()
    clf = psql(env, "SELECT count(*) FROM question_classifications cl "
                    "JOIN questions q ON q.id = cl.question_id "
                    "WHERE q.source_type = 'AUTHORAL'").strip()
    return {"specs": len(items), "specs_present": len(items) - len(missing),
            "missing": missing, "wrong_source_type": wrong_type,
            "official": int(official), "authoral": int(authoral),
            "authoral_options": int(opt), "authoral_classifications": int(clf)}


# ---------------- main ----------------

def main() -> int:
    ap = argparse.ArgumentParser(description="Importador de autorais (TASK 15.3)")
    ap.add_argument("--check", action="store_true",
                    help="somente valida specs + estado do banco, sem escrever")
    ap.add_argument("--report", default=str(DEFAULT_REPORT),
                    help="caminho do relatorio JSON de auditoria")
    ap.add_argument("--specs", default=str(DEFAULT_SPECS),
                    help="diretorio dos specs JSON")
    args = ap.parse_args()

    started = datetime.now(timezone.utc).isoformat()
    specs_dir = Path(args.specs)
    gate_specs(specs_dir)
    items = load_specs(specs_dir)
    env = pg_env()
    check_schema(env)
    refs = load_refs(env, items)
    by_cks, spec_ids = fetch_existing(env)

    inserted, skipped, divergences = [], [], []
    for it in items:
        if it["checksum"] in by_cks:
            if by_cks[it["checksum"]]["source_type"] != "AUTHORAL":
                divergences.append({
                    "spec": it["id"], "checksum": it["checksum"],
                    "reason": "checksum_igual_a_questao_nao_autoral",
                    "db_source_type": by_cks[it["checksum"]]["source_type"],
                    "question_id": by_cks[it["checksum"]]["id"],
                })
            else:
                skipped.append(it)
            continue
        if it["id"] in spec_ids:
            divergences.append({
                "spec": it["id"], "checksum": it["checksum"],
                "reason": "spec_ja_importado_com_checksum_diferente",
                "question_id": spec_ids[it["id"]]["id"],
                "db_checksum": spec_ids[it["id"]]["checksum"],
            })
            continue
        inserted.append(it)

    report: dict = {
        "importer": "scripts/db/import_authoral.py",
        "importer_version": IMPORTER_VERSION,
        "pipeline_version": PIPELINE_VERSION,
        "taxonomy_version": TAXONOMY_VERSION,
        "origin": ORIGIN,
        "started_at_utc": started,
        "check_only": args.check,
        "candidates": len(items),
        "to_insert": len(inserted),
        "already_present": len(skipped),
        "divergences": divergences,
    }

    if divergences:
        report["status"] = "DIVERGENCE"
        report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
        write_report(args.report, report)
        print(f"DIVERGENCIA: {len(divergences)} item(ns). Nada foi alterado. "
              f"Ver {args.report}", file=sys.stderr)
        return 2

    if not args.check:
        tmpdir = Path(os.environ.get("TMPDIR", "/tmp")) / "voupassar-import-authoral"
        tmpdir.mkdir(parents=True, exist_ok=True)
        if inserted:
            qfile = tmpdir / "authoral-questions.sql"
            qfile.write_text("BEGIN;\n"
                             + sql_insert_questions(inserted, refs)
                             + "\nCOMMIT;\n", encoding="utf-8")
            psql_file(env, qfile)
            by_cks, _ = fetch_existing(env)
        refresh_child_counts(env)
        existing = {cks: info for cks, info in by_cks.items()
                    if info["source_type"] == "AUTHORAL"}
        todo = [it for it in items if it["checksum"] in existing]
        cfile = tmpdir / "authoral-children.sql"
        cfile.write_text("BEGIN;\n" + sql_insert_children(todo, refs, existing)
                         + "\nCOMMIT;\n", encoding="utf-8")
        psql_file(env, cfile)

    state = verify(env, items)
    report["db_state"] = state
    ok = (not state["missing"] and not state["wrong_source_type"]
          and state["official"] == 240
          and state["authoral_options"] == 4 * state["authoral"]
          and state["authoral_classifications"] == state["authoral"])
    report["status"] = "OK" if ok else "MISMATCH"
    report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
    write_report(args.report, report)

    print(f"candidatas={len(items)} a_inserir={len(inserted)} "
          f"ja_presentes={len(skipped)} divergencias=0")
    print(f"banco: official={state['official']} authoral={state['authoral']} "
          f"authoral_options={state['authoral_options']} "
          f"authoral_classifications={state['authoral_classifications']} "
          f"specs_presentes={state['specs_present']}/{state['specs']}")
    print(f"relatorio: {args.report}")
    if args.check:
        print("(modo --check: nenhuma escrita executada)")
    return 0 if ok else 1


def write_report(path: str, report: dict) -> None:
    p = Path(path)
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n",
                 encoding="utf-8")


if __name__ == "__main__":
    try:
        sys.exit(main())
    except ImportFailure as e:
        print(f"ERRO: {e}", file=sys.stderr)
        sys.exit(2)
