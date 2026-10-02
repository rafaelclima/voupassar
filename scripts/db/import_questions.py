#!/usr/bin/env python3
"""Importador das questoes oficiais para o PostgreSQL — TASK 2.3.

Le (somente leitura, nunca altera a fonte):
  - `data/extracted/<edicao>.json`  (TASK 1.2: enunciado, alternativas, pagina)
  - `data/linked/<edicao>.json`     (TASK 1.3: resposta oficial A-D/X, anulada)
  - `docs/content-analysis/per-edition/<edicao>.json` (TASK 1.4: classificacao)

Escreve (via `psql`, transacoes com ON_ERROR_STOP):
  - `questions` (240), `question_options` (960),
  - `question_sources` (480: PRIMARY caderno + GABARITO),
  - `question_classifications` (240, taxonomia v1.1),
  - `question_tags` + `question_tag_map` (tag FIGURA nos itens com figura).

Propriedades exigidas pela TASK:
  * repetivel  — mesmos inputs geram os mesmos efeitos;
  * idempotente — reexecucao nao duplica nada (chave natural + checksum);
  * auditavel  — relatorio JSON (`--report`) + validacoes que falham alto.

Contrato de idempotencia (docs/database-erd.md §8):
  chave natural (source_type, source_year, source_question_number,
  exam_document_id) com ON CONFLICT DO NOTHING + comparacao de `checksum`:
  checksum igual = reexecucao segura (filhos conferidos e completados);
  checksum diferente = DIVERGENCIA registrada, sem UPDATE/DELETE silencioso
  (saida com codigo 2).

Checksum: SHA-256 hex de enunciado + alternativas A-D normalizados
(NFC Unicode + colapso de whitespace). Definicao unica deste script.

Regras de honestidade (AGENTS.md §4):
  - explanation sempre NULL (nunca inventar correcao);
  - difficulty_estimate vem da classificacao (palpite, confianca BAIXA);
  - validation_status=PENDING, publication_status=PENDENTE_REVISAO em tudo;
  - classificacao DB status=PENDING em tudo (revisao humana PENDENTE);
  - has_figure=TRUE nos NECESSITA_REVISAO com evidencia visual (36 itens);
    excecoes conceituais sem figura: (2023,Q40) e (2024,Q17) — ver
    observacao original em per-edition/*.json;
  - normalizacao v1.1 (4 itens) = mesma V11_OVERRIDES de
    scripts/analysis/build_content_map.py; JSONs v1 congelados como trilha.

Uso:
    python3 scripts/db/import_questions.py [--check] [--report PATH]
    DB via env POSTGRES_HOST/PORT/DB/USER/PASSWORD (ou .env na raiz).
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import subprocess
import sys
import unicodedata
from datetime import datetime, timezone
from pathlib import Path

IMPORTER_VERSION = "1.0.0"
TAXONOMY_VERSION = "v1.1"

REPO_ROOT = Path(__file__).resolve().parents[2]
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted"
LINKED_DIR = REPO_ROOT / "data" / "linked"
CLASSIF_DIR = REPO_ROOT / "docs" / "content-analysis" / "per-edition"
DEFAULT_REPORT = REPO_ROOT / "data" / "import" / "report.json"

EDITIONS = ["2020", "2022", "2023", "2024", "2025", "2026"]

# (edition, number) -> (assunto, subassunto) v1.1.
# Copia fiel de scripts/analysis/build_content_map.py::V11_OVERRIDES.
V11_OVERRIDES = {
    ("2022", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2023", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2024", 17): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2026", 21): ("ARITMETICA", "SISTEMAS_NUMERACAO"),
}

# NECESSITA_REVISAO cuja observacao nao indica figura ausente, mas questao
# conceitual (sem has_figure). Todo outro NECESSITA_REVISAO tem figura.
NON_FIGURE_REVIEW = {("2023", 40), ("2024", 17)}

ALLOWED_CONF = {"ALTA", "MEDIA", "BAIXA"}
ALLOWED_DIFF = {"FACIL": "FACIL", "MEDIA": "MEDIA", "DIFICIL": "DIFICIL"}
ALLOWED_STATUS = {"OK", "NECESSITA_REVISAO"}


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


# ---------------- helpers SQL ----------------

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


# ---------------- carga e validacao das fontes ----------------

def load_sources() -> list[dict]:
    items: list[dict] = []
    for ed in EDITIONS:
        ext = json.loads((EXTRACTED_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        lnk = json.loads((LINKED_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        clf = json.loads((CLASSIF_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        ex = {q["number"]: q for q in ext["questions"]}
        lk = {q["number"]: q for q in lnk["questions"]}
        cf = {c["number"]: c for c in clf["classificacoes"]}
        if set(ex) != set(range(1, 41)) or set(lk) != set(range(1, 41)) \
                or set(cf) != set(range(1, 41)):
            raise ImportFailure(f"{ed}: esperado numeros 1-40 nas 3 fontes")
        for n in range(1, 41):
            q, l, c = ex[n], lk[n], cf[n]
            # extracao
            if q.get("kind") != "OBJECTIVE":
                raise ImportFailure(f"{ed} Q{n}: kind={q.get('kind')!r}")
            if q["discipline"] not in ("LINGUA_PORTUGUESA", "MATEMATICA"):
                raise ImportFailure(f"{ed} Q{n}: disciplina={q['discipline']!r}")
            if set(q["options"]) != {"A", "B", "C", "D"} \
                    or not all(q["options"][k].strip() for k in "ABCD"):
                raise ImportFailure(f"{ed} Q{n}: alternativas incompletas")
            if not q["statement"].strip():
                raise ImportFailure(f"{ed} Q{n}: enunciado vazio")
            # gabarito
            ak, an = l["answer_key"], l["annulled"]
            if not ((ak in "ABCD" and not an) or (ak == "X" and an)):
                raise ImportFailure(f"{ed} Q{n}: gabarito incoerente {ak}/{an}")
            if l["status"] != ("ANNULLED" if an else "CONFIRMED"):
                raise ImportFailure(f"{ed} Q{n}: status={l['status']!r}")
            # classificacao
            if c["disciplina"] != q["discipline"]:
                raise ImportFailure(f"{ed} Q{n}: disciplina diverge extracao")
            if c["assunto_confianca"] not in ALLOWED_CONF:
                raise ImportFailure(f"{ed} Q{n}: confianca invalida")
            if c["dificuldade_estimada"] not in ALLOWED_DIFF:
                raise ImportFailure(f"{ed} Q{n}: dificuldade invalida")
            if c["status"] not in ALLOWED_STATUS:
                raise ImportFailure(f"{ed} Q{n}: status classif invalido")
            if len(c["evidencia"]) > 200 or not c["evidencia"].strip():
                raise ImportFailure(f"{ed} Q{n}: evidencia fora do limite")
            if c["status"] == "NECESSITA_REVISAO" and not c["observacao"].strip():
                raise ImportFailure(f"{ed} Q{n}: NECESSITA_REVISAO sem motivo")
            assunto, sub = c["assunto"], c["subassunto"]
            if (ed, n) in V11_OVERRIDES:
                assunto, sub = V11_OVERRIDES[(ed, n)]
            items.append({
                "edition": ed, "number": n,
                "statement": q["statement"], "options": dict(q["options"]),
                "page_start": q["page_start"], "page_end": q["page_end"],
                "discipline": q["discipline"],
                "caderno_doc": ext["metadata"]["source_document"],
                "caderno_sha": ext["metadata"]["source_sha256"],
                "gabarito_doc": lnk["metadata"]["gabarito_document"],
                "gabarito_sha": lnk["metadata"]["gabarito_sha256"],
                "answer_key": ak, "annulled": an,
                "assunto": assunto, "subassunto": sub,
                "skill": c["habilidade"], "reasoning": c["tipo_raciocinio"],
                "confidence": c["assunto_confianca"],
                "evidence": c["evidencia"],
                "difficulty": ALLOWED_DIFF[c["dificuldade_estimada"]],
                "observation": c["observacao"].strip() or None,
                "needs_review": c["status"] == "NECESSITA_REVISAO",
                "checksum": checksum(q["statement"], q["options"]),
            })
    if len(items) != 240:
        raise ImportFailure(f"esperado 240 itens, obtidos {len(items)}")
    return items


# ---------------- mapas de referencia do banco ----------------

def fetch_rows(env: dict[str, str], sql: str) -> list[list[str]]:
    out = psql(env, sql)
    return [l.split("|") for l in out.splitlines() if l.strip()]


def load_refs(env: dict[str, str], items: list[dict]) -> dict:
    exams = {r[0]: int(r[1])
             for r in fetch_rows(env, "SELECT year, id FROM exams")}
    docs = {r[0]: (int(r[1]), r[2])
            for r in fetch_rows(
                env, "SELECT sha256, id, file_name FROM exam_documents")}
    discs = {r[0]: int(r[1])
             for r in fetch_rows(env, "SELECT code, id FROM disciplines")}
    topics = {(r[0], r[1]): int(r[2]) for r in fetch_rows(
        env, "SELECT d.code, t.code, t.id FROM topics t "
             "JOIN disciplines d ON d.id = t.discipline_id")}
    subtopics = {(r[0], r[1], r[2]): int(r[3]) for r in fetch_rows(
        env, "SELECT d.code, t.code, s.code, s.id FROM subtopics s "
             "JOIN topics t ON t.id = s.topic_id "
             "JOIN disciplines d ON d.id = t.discipline_id")}
    # documentos exigidos existem (por sha) e o nome literal confere;
    # divergencia de nome = deriva da fonte, falha alto.
    for it in items:
        for doc, sha, fname in (("caderno", it["caderno_sha"], it["caderno_doc"]),
                                ("gabarito", it["gabarito_sha"],
                                 it["gabarito_doc"])):
            if sha not in docs:
                raise ImportFailure(
                    f"{it['edition']} Q{it['number']}: sha do {doc} "
                    f"{sha[:12]}... ausente em exam_documents (rodar V2 seed)")
            if docs[sha][1] != fname:
                raise ImportFailure(
                    f"{it['edition']} Q{it['number']}: nome do {doc} diverge "
                    f"(fonte={fname!r} banco={docs[sha][1]!r})")
    refs = {"exams": exams, "docs": docs, "discs": discs,
            "topics": topics, "subtopics": subtopics}
    # taxonomia cobre tudo (pos-normalizacao); nada pode ser inventado
    for it in items:
        d = it["discipline"]
        if d not in discs:
            raise ImportFailure(f"disciplina {d} ausente no banco")
        if (d, it["assunto"]) not in topics:
            raise ImportFailure(
                f"{it['edition']} Q{it['number']}: topico "
                f"{d}/{it['assunto']} ausente no banco")
        if (d, it["assunto"], it["subassunto"]) not in subtopics:
            raise ImportFailure(
                f"{it['edition']} Q{it['number']}: subtopico "
                f"{d}/{it['assunto']}/{it['subassunto']} ausente no banco")
        if it["edition"] not in exams:
            raise ImportFailure(f"edicao {it['edition']} ausente em exams")
    return refs


# ---------------- estado atual (idempotencia) ----------------

def fetch_existing(env: dict[str, str]) -> dict:
    out = psql(env,
        "SELECT source_year, source_question_number, id, checksum, "
        "exam_document_id FROM questions WHERE source_type = 'OFFICIAL'")
    existing: dict = {}
    for line in out.splitlines():
        if not line.strip():
            continue
        yr, num, qid, cks, docid = line.split("|")
        existing[(yr, int(num))] = {"id": int(qid), "checksum": cks,
                                    "doc_id": int(docid)}
    return existing


def fetch_children(env: dict[str, str]) -> dict:
    kids: dict = {}
    for tbl, col in (("question_options", None), ("question_sources", None),
                     ("question_classifications", None), ("question_tag_map", None)):
        if tbl == "question_options":
            out = psql(env, "SELECT question_id, count(*) FROM question_options "
                            "GROUP BY 1")
        elif tbl == "question_sources":
            out = psql(env, "SELECT question_id, role, exam_document_id "
                            "FROM question_sources")
        elif tbl == "question_classifications":
            out = psql(env, "SELECT question_id, taxonomy_version "
                            "FROM question_classifications")
        else:
            out = psql(env, "SELECT question_id, count(*) FROM question_tag_map "
                            "GROUP BY 1")
        rows = [l for l in out.splitlines() if l.strip()]
        kids[tbl] = rows
    return kids


# ---------------- geracao SQL ----------------

def sql_insert_questions(new: list[dict], refs: dict) -> str:
    stmts = []
    for it in new:
        exam_id = refs["exams"][it["edition"]]
        doc_id = refs["docs"][it["caderno_sha"]][0]
        disc_id = refs["discs"][it["discipline"]]
        has_fig = it["needs_review"] and (it["edition"], it["number"]) \
            not in NON_FIGURE_REVIEW
        stmts.append(
            "INSERT INTO questions (source_type, exam_id, exam_document_id, "
            "source_year, source_question_number, statement, kind, "
            "discipline_id, page_start, page_end, answer_key, annulled, "
            "checksum, has_figure, difficulty_estimate, explanation, "
            "validation_status, publication_status) VALUES "
            f"('OFFICIAL', {exam_id}, {doc_id}, {it['edition']}, "
            f"{it['number']}, {esc(it['statement'])}, 'OBJECTIVE', "
            f"{disc_id}, {it['page_start']}, {it['page_end']}, "
            f"{esc(it['answer_key'])}, {'TRUE' if it['annulled'] else 'FALSE'}, "
            f"{esc(it['checksum'])}, {'TRUE' if has_fig else 'FALSE'}, "
            f"{esc(it['difficulty'])}, NULL, 'PENDING', 'PENDENTE_REVISAO') "
            "ON CONFLICT DO NOTHING;")
    return "\n".join(stmts)


def sql_insert_children(items: list[dict], refs: dict,
                        existing: dict, kids: dict) -> str:
    opt_counts: dict[int, int] = {}
    for line in kids["question_options"]:
        qid, n = line.split("|")
        opt_counts[int(qid)] = int(n)
    src_rows = {tuple(l.split("|")) for l in kids["question_sources"]}
    clf_rows = {tuple(l.split("|")) for l in kids["question_classifications"]}
    tag_counts: dict[int, int] = {}
    for line in kids["question_tag_map"]:
        qid, n = line.split("|")
        tag_counts[int(qid)] = int(n)

    stmts = ["INSERT INTO question_tags (code, description) VALUES "
             "('FIGURA', 'Questao depende de figura/grafico/charge ausente "
             "do texto extraido; requer revisao visual antes de publicar') "
             "ON CONFLICT (code) DO NOTHING;"]
    for it in items:
        qid = existing[(it["edition"], it["number"])]["id"]
        caderno_id = refs["docs"][it["caderno_sha"]][0]
        gab_id = refs["docs"][it["gabarito_sha"]][0]
        if opt_counts.get(qid, 0) < 4:
            for lbl in "ABCD":
                stmts.append(
                    "INSERT INTO question_options (question_id, label, "
                    f"option_text) VALUES ({qid}, '{lbl}', "
                    f"{esc(it['options'][lbl])}) ON CONFLICT DO NOTHING;")
        for role, doc_id, pages in (
                ("PRIMARY", caderno_id,
                 f"{it['page_start']}, {it['page_end']}"),
                ("GABARITO", gab_id, "NULL, NULL")):
            if (str(qid), role, str(doc_id)) not in src_rows:
                if role == "PRIMARY":
                    sha = it["caderno_sha"]
                else:
                    sha = it["gabarito_sha"]
                stmts.append(
                    "INSERT INTO question_sources (question_id, "
                    "exam_document_id, role, page_start, page_end, doc_sha256)"
                    f" VALUES ({qid}, {doc_id}, '{role}', {pages}, "
                    f"{esc(sha)}) ;")
        if (str(qid), TAXONOMY_VERSION) not in clf_rows:
            topic_id = refs["topics"][(it["discipline"], it["assunto"])]
            sub_id = refs["subtopics"][(it["discipline"], it["assunto"],
                                        it["subassunto"])]
            obs = "NULL" if it["observation"] is None else esc(it["observation"])
            stmts.append(
                "INSERT INTO question_classifications (question_id, "
                "taxonomy_version, topic_id, subtopic_id, skill, "
                "reasoning_type, confidence, evidence, origin, status, observation) "
                f"VALUES ({qid}, '{TAXONOMY_VERSION}', {topic_id}, {sub_id}, "
                f"{esc(it['skill'])}, {esc(it['reasoning'])}, "
                f"{esc(it['confidence'])}, {esc(it['evidence'])}, "
                "'CLASSIFICACAO_DERIVADA_FONTE', 'PENDING', " + obs + ");")
        has_fig = it["needs_review"] and (it["edition"], it["number"]) \
            not in NON_FIGURE_REVIEW
        if has_fig and tag_counts.get(qid, 0) == 0:
            stmts.append(
                "INSERT INTO question_tag_map (question_id, tag_id) "
                "SELECT {qid}, id FROM question_tags WHERE code = 'FIGURA' "
                "ON CONFLICT DO NOTHING;".format(qid=qid))
    return "\n".join(stmts)


# ---------------- verificacao final ----------------

def verify(env: dict[str, str], items: list[dict]) -> dict:
    q = psql(env, "SELECT count(*) FROM questions "
                  "WHERE source_type = 'OFFICIAL'").strip()
    o = psql(env, "SELECT count(*) FROM question_options o JOIN questions q "
                  "ON q.id = o.question_id "
                  "WHERE q.source_type = 'OFFICIAL'").strip()
    s = psql(env, "SELECT count(*) FROM question_sources s JOIN questions q "
                  "ON q.id = s.question_id "
                  "WHERE q.source_type = 'OFFICIAL'").strip()
    c = psql(env, "SELECT count(*) FROM question_classifications cl "
                  "JOIN questions q ON q.id = cl.question_id "
                  "WHERE q.source_type = 'OFFICIAL'").strip()
    f = psql(env, "SELECT count(*) FROM questions "
                  "WHERE source_type = 'OFFICIAL' AND has_figure").strip()
    # checksums no banco == recalculados das fontes
    out = psql(env, "SELECT source_year, source_question_number, checksum "
                    "FROM questions WHERE source_type = 'OFFICIAL'")
    db_cks = {}
    for line in out.splitlines():
        if line.strip():
            yr, num, cks = line.split("|")
            db_cks[(yr, int(num))] = cks
    mism = [(it["edition"], it["number"]) for it in items
            if db_cks.get((it["edition"], it["number"])) != it["checksum"]]
    return {"questions": int(q), "options": int(o), "sources": int(s),
            "classifications": int(c), "has_figure": int(f),
            "checksum_mismatches": mism}


# ---------------- main ----------------

def main() -> int:
    ap = argparse.ArgumentParser(description="Importador idempotente (TASK 2.3)")
    ap.add_argument("--check", action="store_true",
                    help="somente valida fontes + estado do banco, sem escrever")
    ap.add_argument("--report", default=str(DEFAULT_REPORT),
                    help="caminho do relatorio JSON de auditoria")
    args = ap.parse_args()

    started = datetime.now(timezone.utc).isoformat()
    items = load_sources()
    env = pg_env()
    refs = load_refs(env, items)
    existing = fetch_existing(env)

    inserted, skipped, divergences = [], [], []
    for it in items:
        key = (it["edition"], it["number"])
        if key not in existing:
            inserted.append(it)
            continue
        cur = existing[key]
        caderno_id = refs["docs"][it["caderno_sha"]][0]
        if cur["checksum"] != it["checksum"] or cur["doc_id"] != caderno_id:
            divergences.append({
                "edition": it["edition"], "number": it["number"],
                "question_id": cur["id"],
                "reason": "checksum_ou_documento_divergente",
                "db_checksum": cur["checksum"], "file_checksum": it["checksum"],
            })
        else:
            skipped.append(it)

    report: dict = {
        "importer": "scripts/db/import_questions.py",
        "importer_version": IMPORTER_VERSION,
        "taxonomy_version": TAXONOMY_VERSION,
        "started_at_utc": started,
        "check_only": args.check,
        "source_files": 18,
        "candidates": len(items),
        "to_insert": len(inserted),
        "already_present": len(skipped),
        "divergences": divergences,
    }

    if divergences:
        report["status"] = "DIVERGENCE"
        report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
        write_report(args.report, report)
        print(f"DIVERGENCIA: {len(divergences)} questao(oes) no banco com "
              f"checksum/documento diferente da fonte. Nada foi alterado. "
              f"Ver {args.report}", file=sys.stderr)
        return 2

    if not args.check:
        tmpdir = Path(os.environ.get("TMPDIR", "/tmp")) / "voupassar-import"
        tmpdir.mkdir(parents=True, exist_ok=True)
        if inserted:
            qfile = tmpdir / "questions.sql"
            qfile.write_text("BEGIN;\n"
                             + sql_insert_questions(inserted, refs)
                             + "\nCOMMIT;\n", encoding="utf-8")
            psql_file(env, qfile)
            existing = fetch_existing(env)
        kids = fetch_children(env)
        # top-up: completa filhos faltantes (recuperacao + idempotencia)
        todo = [it for it in items
                if (it["edition"], it["number"]) in existing]
        cfile = tmpdir / "children.sql"
        cfile.write_text("BEGIN;\n" + sql_insert_children(todo, refs, existing,
                                                           kids)
                         + "\nCOMMIT;\n", encoding="utf-8")
        psql_file(env, cfile)

    state = verify(env, items)
    report["db_state"] = state
    ok = (state["questions"] == 240 and state["options"] == 960
          and state["sources"] == 480 and state["classifications"] == 240
          and state["has_figure"] == 36 and not state["checksum_mismatches"])
    report["status"] = "OK" if ok else "MISMATCH"
    report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
    write_report(args.report, report)

    print(f"candidatas=240 a_inserir={len(inserted)} "
          f"ja_presentes={len(skipped)} divergencias=0")
    print(f"banco: questions={state['questions']} options={state['options']} "
          f"sources={state['sources']} classifications={state['classifications']} "
          f"has_figure={state['has_figure']}")
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
