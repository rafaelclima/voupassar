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

Regras de honestidade (AGENTS.md §4, decisão de produto 2026-10-06):
  - difficulty_estimate vem da classificacao (palpite, confianca BAIXA);
  - sem carimbo humano: questions não tem validation/publication_status e
    question_classifications não tem status (V12); confiança = pipeline;
  - cada linha carimba pipeline_version (versão deste importador);
  - has_figure=TRUE nos NECESSITA_REVISAO com evidencia visual (33 itens);
    excecoes conceituais sem figura: (2023,Q40) e (2024,Q17) — ver
    observacao original em per-edition/*.json;
  - normalizacao v1.1 (4 itens) = mesma V11_OVERRIDES de
    scripts/analysis/build_content_map.py; JSONs v1 congelados como trilha.

Uso:
    python3 scripts/db/import_questions.py [--institution IFRN|EAJ]
        [--check] [--report PATH] [--allow-needs-visual-check]
    DB via env POSTGRES_HOST/PORT/DB/USER/PASSWORD (ou .env na raiz).

Modo EAJ (TASK D.3, Programa EAJ):
    fonte = `.md` (data/extracted/eaj/*.json + data/linked/eaj/*.json +
    docs/content-analysis/per-edition/eaj-*.json); PDFs só auditoria.
    Proveniência TRANSCRIBED_FROM_MD (nunca oficial; ver docs/blockers.md).
    Namespace EAJ em tudo chaveado por ano (EAJ-2022 ≠ IFRN-2022).
    2021 = 50Q (LP 01-15, MAT 16-30, CN 31-42, CH 43-50);
    2022/2025 = 40Q (LP 01-20, MAT 21-40). Q23-2025 anulada (X);
    Q22/Q39-2025 NEEDS_VISUAL_CHECK BLOQUEADAS até conferência visual
    (exit 2 sem --allow-needs-visual-check; com a flag, entram após
    curadoria — nunca por inferência).
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

IMPORTER_VERSION = "2.1.0"
TAXONOMY_VERSION = "v1.1"

REPO_ROOT = Path(__file__).resolve().parents[2]
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted"
LINKED_DIR = REPO_ROOT / "data" / "linked"
CLASSIF_DIR = REPO_ROOT / "docs" / "content-analysis" / "per-edition"
DEFAULT_REPORT = REPO_ROOT / "data" / "import" / "report.json"
DEFAULT_REPORT_EAJ = REPO_ROOT / "data" / "import" / "report-eaj.json"

EDITIONS = ["2020", "2022", "2023", "2024", "2025", "2026"]

# ---- EAJ (TASK D.3) ----
EAJ_EDITIONS = {"2021": 50, "2022": 40, "2025": 40}
EAJ_ALLOWED_DISCIPLINES = {"LINGUA_PORTUGUESA", "MATEMATICA",
                           "CIENCIAS_NATUREZA", "CIENCIAS_HUMANAS"}
EAJ_LINKED_STATUS = {"CONFIRMED_TRANSCRIBED", "ANNULLED_TRANSCRIBED",
                     "NEEDS_VISUAL_CHECK"}
# (institution, edition, number) com NEEDS_VISUAL_CHECK textual (sem figura):
# Q22 (OCR agrupado) e Q39 (razão interpolada + mancha) — has_figure FALSE.
# A trava D.3 é de conteúdo (conferência visual no PDF), não de figura.
EAJ_NON_FIGURE_REVIEW = {("EAJ", "2025", 22), ("EAJ", "2025", 39)}

# (edition, number) -> (assunto, subassunto) v1.1.
# Copia fiel de scripts/analysis/build_content_map.py::V11_OVERRIDES.
V11_OVERRIDES = {
    ("2022", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2023", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2024", 17): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2026", 21): ("ARITMETICA", "SISTEMAS_NUMERACAO"),
}

# NECESSITA_REVISAO cuja observacao nao indica figura ausente, mas questao
# conceitual ou só-texto (sem has_figure). Todo outro NECESSITA_REVISAO
# tem figura. Curadoria manual 2026-10-07 (espelha V14):
# 2020 Q26 só texto; 2025 Q33 sem figura; 2025 Q36 com o percentual
# (83%) no próprio bloco textual (2025 Q39 já é OK/sem figura).
NON_FIGURE_REVIEW = {("2023", 40), ("2024", 17),
                     ("2020", 26), ("2025", 33), ("2025", 36)}

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
                "institution": "IFRN",
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


def load_sources_eaj() -> tuple[list[dict], list[dict]]:
    """Fontes EAJ (TASK D.3): `.md` como extração; PDF só auditoria.

    Retorna (items, blocked): items = importáveis (CONFIRMED_TRANSCRIBED +
    ANNULLED_TRANSCRIBED como X); blocked = NEEDS_VISUAL_CHECK (Q22/Q39-2025)
    com motivo — nunca importados sem --allow-needs-visual-check.
    """
    items: list[dict] = []
    blocked: list[dict] = []
    for ed, total in EAJ_EDITIONS.items():
        ext = json.loads(
            (EXTRACTED_DIR / "eaj" / f"{ed}.json").read_text(encoding="utf-8"))
        lnk = json.loads(
            (LINKED_DIR / "eaj" / f"{ed}.json").read_text(encoding="utf-8"))
        clf = json.loads(
            (CLASSIF_DIR / f"eaj-{ed}.json").read_text(encoding="utf-8"))
        ex = {q["number"]: q for q in ext["questions"]}
        lk = {q["number"]: q for q in lnk["questions"]}
        cf = {c["number"]: c for c in clf["classificacoes"]}
        if set(ex) != set(range(1, total + 1)) \
                or set(lk) != set(range(1, total + 1)) \
                or set(cf) != set(range(1, total + 1)):
            raise ImportFailure(f"EAJ-{ed}: esperado numeros 1-{total} nas 3 fontes")
        ext_meta, lnk_meta = ext["metadata"], lnk["metadata"]
        # proveniência honesta: transcrição, nunca PDF oficial (B.2).
        if lnk_meta.get("provenance") != "TRANSCRIBED_FROM_MD":
            raise ImportFailure(
                f"EAJ-{ed}: linked.provenance={lnk_meta.get('provenance')!r} "
                f"(esperado TRANSCRIBED_FROM_MD; ver docs/blockers.md)")
        if lnk_meta.get("source_document") != \
                f"data/provas/EAJ/{ed}/questoes.md":
            raise ImportFailure(f"EAJ-{ed}: source_document fora do .md fonte")
        if ext_meta.get("source_document") != \
                f"data/provas/EAJ/{ed}/questoes.md":
            raise ImportFailure(f"EAJ-{ed}: extracted fora do .md fonte")
        ext_sha = ext_meta.get("source_sha256") or ""
        lnk_sha = lnk_meta.get("source_sha256") or ""
        if not ext_sha or not lnk_sha:
            raise ImportFailure(f"EAJ-{ed}: sha do .md ausente nos metadados")
        if ext_sha != lnk_sha:
            raise ImportFailure(
                f"EAJ-{ed}: sha extracted ({ext_sha[:12]}...) != "
                f"linked ({lnk_sha[:12]}...) — mesmo .md esperado")
        ext_doc = ext_meta["source_document"]
        lnk_doc = lnk_meta["source_document"]
        for n in range(1, total + 1):
            q, l, c = ex[n], lk[n], cf[n]
            tag = f"EAJ-{ed} Q{n}"
            if q.get("institution") != "EAJ" or l.get("institution") != "EAJ":
                raise ImportFailure(f"{tag}: institution != EAJ")
            if q.get("kind") != "OBJECTIVE":
                raise ImportFailure(f"{tag}: kind={q.get('kind')!r}")
            if q["discipline"] not in EAJ_ALLOWED_DISCIPLINES:
                raise ImportFailure(f"{tag}: disciplina={q['discipline']!r}")
            if set(q["options"]) != {"A", "B", "C", "D"} \
                    or not all(q["options"][k].strip() for k in "ABCD"):
                raise ImportFailure(f"{tag}: alternativas incompletas")
            if not q["statement"].strip():
                raise ImportFailure(f"{tag}: enunciado vazio")
            # páginas DESCONHECIDAS: o `.md` não traz página (B.1) — NULL,
            # nunca número inventado (V20 relaxa o CHECK da V13).
            if q.get("page_start") is not None or q.get("page_end") is not None:
                raise ImportFailure(f"{tag}: page_start/end deve ser NULL "
                                    f"(DESCONHECIDO; nunca inventar)")
            if q.get("page_status") != "DESCONHECIDO":
                raise ImportFailure(f"{tag}: page_status fora de DESCONHECIDO")
            if q.get("answer_provenance") != "TRANSCRIBED_FROM_MD":
                raise ImportFailure(f"{tag}: answer_provenance fora de "
                                    f"TRANSCRIBED_FROM_MD")
            # gabarito transcrito: inline (extracted.answer) == tabela (linked)
            ak_raw, an = l["answer_key"], l["annulled"]
            if q.get("answer") != ak_raw and not (
                    q.get("annulled") and ak_raw == "NULA"):
                # Q23: extracted.answer NULA, linked NULA — confere;
                # demais: letra deve casar.
                if not (q.get("answer") == ak_raw):
                    raise ImportFailure(
                        f"{tag}: resposta extracted={q.get('answer')!r} "
                        f"diverge de linked={ak_raw!r}")
            if l.get("status") not in EAJ_LINKED_STATUS:
                raise ImportFailure(f"{tag}: status={l.get('status')!r}")
            if l["status"] == "CONFIRMED_TRANSCRIBED":
                if not (ak_raw in "ABCD" and not an):
                    raise ImportFailure(f"{tag}: gabarito incoerente {ak_raw}/{an}")
                ak, ann = ak_raw, False
            elif l["status"] == "ANNULLED_TRANSCRIBED":
                # Q23-2025: NULA transcrita → X no banco (convenção IFRN),
                # alternativas presentes sem efeito de pontuação.
                if not (ak_raw == "NULA" and an):
                    raise ImportFailure(f"{tag}: anulada incoerente {ak_raw}/{an}")
                if not l.get("annulled_reason_transcribed", "").strip():
                    raise ImportFailure(f"{tag}: anulada sem motivo transcrito")
                ak, ann = "X", True
            # classificação (vale também para as bloqueadas: D.1 classifica,
            # D.3 só libera após conferência visual — nunca sem flag)
            if c["disciplina"] != q["discipline"]:
                raise ImportFailure(f"{tag}: disciplina diverge extracao")
            if c.get("edition") != f"EAJ-{ed}":
                raise ImportFailure(f"{tag}: edition={c.get('edition')!r} "
                                    f"(ano sozinho nunca identifica a edição)")
            if c["assunto_confianca"] not in ALLOWED_CONF:
                raise ImportFailure(f"{tag}: confianca invalida")
            if c["dificuldade_estimada"] not in ALLOWED_DIFF:
                raise ImportFailure(f"{tag}: dificuldade invalida")
            if c["status"] not in ALLOWED_STATUS:
                raise ImportFailure(f"{tag}: status classif invalido")
            if len(c["evidencia"]) > 200 or not c["evidencia"].strip():
                raise ImportFailure(f"{tag}: evidencia fora do limite")
            if c["status"] == "NECESSITA_REVISAO" and not c["observacao"].strip():
                raise ImportFailure(f"{tag}: NECESSITA_REVISAO sem motivo")
            if c.get("anulada") != (l["status"] == "ANNULLED_TRANSCRIBED"):
                raise ImportFailure(f"{tag}: anulada diverge de data/linked")
            full_item = _eaj_item(ed, n, q, c, ak if l["status"] != "NEEDS_VISUAL_CHECK" else ak_raw,
                                  ann if l["status"] != "NEEDS_VISUAL_CHECK" else False,
                                  ext_doc, ext_sha, lnk_doc, lnk_sha)
            if l["status"] == "NEEDS_VISUAL_CHECK":  # trava D.3
                if not (ak_raw in "ABCD" and not an):
                    raise ImportFailure(f"{tag}: NEEDS_VISUAL_CHECK incoerente")
                if not l.get("needs_visual_check"):
                    raise ImportFailure(f"{tag}: NEEDS sem flag needs_visual_check")
                if c.get("status") != "NECESSITA_REVISAO":
                    raise ImportFailure(
                        f"{tag}: needs_visual_check exige NECESSITA_REVISAO")
                blocked.append({
                    "institution": "EAJ", "edition": ed, "number": n,
                    "status": l["status"], "answer_key": ak_raw,
                    "reason": l.get("needs_visual_check_reason")
                    or l.get("validation") or "conferir no PDF",
                    "_item": full_item,
                })
                continue
            items.append(full_item)
    if len(items) + len(blocked) != 130:
        raise ImportFailure(
            f"EAJ: esperado 130 itens totais, obtidos "
            f"{len(items)}+{len(blocked)} bloqueadas")
    return items, blocked


def _eaj_item(ed: str, n: int, q: dict, c: dict,
              ak: str, ann: bool,
              ext_doc: str, ext_sha: str, lnk_doc: str, lnk_sha: str) -> dict:
    """Monta o item EAJ (comum aos caminhos com/sem flag visual).

    PRIMARY = .md (extração); GABARITO = mesmo .md (tabela compilada
    transcrita — GABARITO_TRANSCRITO, nunca oficial). Ambos apontam para o
    mesmo exam_document_id (.md OUTRO da V19); papéis distinguem a evidência.
    """
    assunto = c["assunto"]
    sub = c["subassunto"]
    # OUTRO justificado (EAJ-2025 Q40, GEOMETRIA sem subcódigo v1.1):
    # topic GEOMETRIA, subtopic NULL (ERD §2.4; V20 §3). Nada inventado.
    sub_or_null = None if sub == "OUTRO" else sub
    if sub == "OUTRO" and not str(c.get("observacao", "")).strip():
        raise ImportFailure(f"EAJ-{ed} Q{n}: sub OUTRO sem justificativa")
    return {
        "institution": "EAJ",
        "edition": ed, "number": n,
        "statement": q["statement"], "options": dict(q["options"]),
        "page_start": None, "page_end": None,
        "discipline": q["discipline"],
        "caderno_doc": ext_doc,
        "caderno_sha": ext_sha,
        "gabarito_doc": lnk_doc,
        "gabarito_sha": lnk_sha,
        "answer_key": ak, "annulled": ann,
        "assunto": assunto, "subassunto": sub_or_null,
        "subassunto_raw": sub,
        "skill": c["habilidade"], "reasoning": c["tipo_raciocinio"],
        "confidence": c["assunto_confianca"],
        "evidence": c["evidencia"],
        "difficulty": ALLOWED_DIFF[c["dificuldade_estimada"]],
        "observation": c["observacao"].strip() or None,
        "needs_review": c["status"] == "NECESSITA_REVISAO",
        "checksum": checksum(q["statement"], q["options"]),
    }


# ---------------- mapas de referencia do banco ----------------

def fetch_rows(env: dict[str, str], sql: str) -> list[list[str]]:
    out = psql(env, sql)
    return [l.split("|") for l in out.splitlines() if l.strip()]


def check_schema(env: dict[str, str]) -> None:
    """Falha alto se o banco ainda tem o contrato de curadoria (pré-V12)."""
    cols = psql(env,
        "SELECT column_name FROM information_schema.columns "
        "WHERE table_name = 'questions'").split()
    if "pipeline_version" not in cols:
        raise ImportFailure(
            "banco anterior à V12__drop_review_gates (sem pipeline_version); "
            "aplicar database/migrations até V12 antes de importar")
    for dead in ("validation_status", "publication_status"):
        if dead in cols:
            raise ImportFailure(
                f"banco com coluna {dead} (pré-V12); aplicar V12 antes de importar")


def load_refs(env: dict[str, str], items: list[dict],
              institution: str = "IFRN") -> dict:
    # exams por (institution, year): ano sozinho nunca identifica a edição
    # (EAJ-2022 ≠ IFRN-2022; V17 uq_exams_institution_year; V19 §9).
    try:
        exam_rows = fetch_rows(env, "SELECT institution, year, id FROM exams")
    except ImportFailure:
        raise ImportFailure(
            "tabela exams sem institution (pré-V17); aplicar V17 antes de importar")
    exams = {(r[0], r[1]): int(r[2]) for r in exam_rows}
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
        inst = it.get("institution", "IFRN")
        ed_label = f"{inst}-{it['edition']}" if inst == "EAJ" else it['edition']
        for doc, sha, fname in (("caderno", it["caderno_sha"], it["caderno_doc"]),
                                ("gabarito", it["gabarito_sha"],
                                 it["gabarito_doc"])):
            if sha not in docs:
                raise ImportFailure(
                    f"{ed_label} Q{it['number']}: sha do {doc} "
                    f"{sha[:12]}... ausente em exam_documents (rodar V2/V19 seed)")
            if docs[sha][1] != fname:
                raise ImportFailure(
                    f"{ed_label} Q{it['number']}: nome do {doc} diverge "
                    f"(fonte={fname!r} banco={docs[sha][1]!r})")
    refs = {"exams": exams, "docs": docs, "discs": discs,
            "topics": topics, "subtopics": subtopics}
    # taxonomia cobre tudo (pos-normalizacao); nada pode ser inventado.
    # OUTRO justificado (EAJ-2025 Q40): subtopic NULL, só topic exigido.
    for it in items:
        inst = it.get("institution", "IFRN")
        ed_label = f"{inst}-{it['edition']}" if inst == "EAJ" else it['edition']
        d = it["discipline"]
        if d not in discs:
            raise ImportFailure(f"disciplina {d} ausente no banco")
        if (d, it["assunto"]) not in topics:
            raise ImportFailure(
                f"{ed_label} Q{it['number']}: topico "
                f"{d}/{it['assunto']} ausente no banco (V20 para CN/CH)")
        sub = it["subassunto"]
        if sub is not None:
            if (d, it["assunto"], sub) not in subtopics:
                raise ImportFailure(
                    f"{ed_label} Q{it['number']}: subtopico "
                    f"{d}/{it['assunto']}/{sub} ausente no banco")
        else:
            # NULL só aceito para OUTRO justificado (ERD §2.4; V20 §3)
            if it.get("subassunto_raw") != "OUTRO" or not it.get("observation"):
                raise ImportFailure(
                    f"{ed_label} Q{it['number']}: subtopic NULL sem OUTRO justificado")
        if (inst, it["edition"]) not in exams:
            raise ImportFailure(f"edicao ({inst},{it['edition']}) ausente em exams")
    return refs


# ---------------- estado atual (idempotencia) ----------------

def fetch_existing(env: dict[str, str], institution: str = "IFRN") -> dict:
    # Filtra por institution via exams (colisão 2022/2025 IFRN×EAJ: ano
    # sozinho nunca decide a edição — V19 §9; religação total na E.1).
    inst = institution.replace("'", "''")
    out = psql(env,
        "SELECT q.source_year, q.source_question_number, q.id, q.checksum, "
        "q.exam_document_id FROM questions q JOIN exams e ON e.id = q.exam_id "
        f"WHERE q.source_type = 'OFFICIAL' AND e.institution = '{inst}'")
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

def sql_page(v) -> str:
    return "NULL" if v is None else str(int(v))


def has_figure_for(it: dict) -> bool:
    inst = it.get("institution", "IFRN")
    if inst == "EAJ":
        return bool(it["needs_review"]) and \
            (inst, it["edition"], it["number"]) not in EAJ_NON_FIGURE_REVIEW
    return bool(it["needs_review"]) and (it["edition"], it["number"]) \
        not in NON_FIGURE_REVIEW


def sql_insert_questions(new: list[dict], refs: dict) -> str:
    stmts = []
    for it in new:
        inst = it.get("institution", "IFRN")
        exam_id = refs["exams"][(inst, it["edition"])]
        doc_id = refs["docs"][it["caderno_sha"]][0]
        disc_id = refs["discs"][it["discipline"]]
        has_fig = has_figure_for(it)
        stmts.append(
            "INSERT INTO questions (source_type, exam_id, exam_document_id, "
            "source_year, source_question_number, statement, kind, "
            "discipline_id, page_start, page_end, answer_key, annulled, "
            "checksum, has_figure, difficulty_estimate, "
            "pipeline_version) VALUES "
            f"('OFFICIAL', {exam_id}, {doc_id}, {it['edition']}, "
            f"{it['number']}, {esc(it['statement'])}, 'OBJECTIVE', "
            f"{disc_id}, {sql_page(it['page_start'])}, {sql_page(it['page_end'])}, "
            f"{esc(it['answer_key'])}, {'TRUE' if it['annulled'] else 'FALSE'}, "
            f"{esc(it['checksum'])}, {'TRUE' if has_fig else 'FALSE'}, "
            f"{esc(it['difficulty'])}, 'importer-{IMPORTER_VERSION}') "
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
             "do texto extraido; ver paginas do caderno-fonte') "
             "ON CONFLICT (code) DO NOTHING;"]
    for it in items:
        qid = existing[(it["edition"], it["number"])]["id"]
        caderno_id = refs["docs"][it["caderno_sha"]][0]
        gab_id = refs["docs"][it["gabarito_sha"]][0]
        inst = it.get("institution", "IFRN")
        if opt_counts.get(qid, 0) < 4:
            for lbl in "ABCD":
                stmts.append(
                    "INSERT INTO question_options (question_id, label, "
                    f"option_text) VALUES ({qid}, '{lbl}', "
                    f"{esc(it['options'][lbl])}) ON CONFLICT DO NOTHING;")
        if inst == "EAJ":
            # EAJ: páginas DESCONHECIDAS (NULL) + nota de proveniência honesta
            # (transcrição, nunca oficial). PRIMARY = .md (extração);
            # GABARITO = mesmo .md (tabela compilada transcrita).
            roles = (
                ("PRIMARY", caderno_id, "NULL, NULL", it["caderno_sha"],
                 "TRANSCRIBED_FROM_MD: enunciados+alternativas transcritos "
                 "do .md (nunca oficial; ver docs/blockers.md)"),
                ("GABARITO", gab_id, "NULL, NULL", it["gabarito_sha"],
                 "GABARITO_TRANSCRITO: tabela compilada transcrita no mesmo "
                 ".md (nunca oficial; ver docs/blockers.md)"),
            )
        else:
            roles = (
                ("PRIMARY", caderno_id,
                 f"{it['page_start']}, {it['page_end']}", it["caderno_sha"], None),
                ("GABARITO", gab_id, "NULL, NULL", it["gabarito_sha"], None),
            )
        for role, doc_id, pages, sha, note in roles:
            if (str(qid), role, str(doc_id)) not in src_rows:
                if note is None:
                    stmts.append(
                        "INSERT INTO question_sources (question_id, "
                        "exam_document_id, role, page_start, page_end, doc_sha256)"
                        f" VALUES ({qid}, {doc_id}, '{role}', {pages}, "
                        f"{esc(sha)}) ;")
                else:
                    stmts.append(
                        "INSERT INTO question_sources (question_id, "
                        "exam_document_id, role, page_start, page_end, "
                        "doc_sha256, note)"
                        f" VALUES ({qid}, {doc_id}, '{role}', {pages}, "
                        f"{esc(sha)}, {esc(note)}) ;")
        if (str(qid), TAXONOMY_VERSION) not in clf_rows:
            topic_id = refs["topics"][(it["discipline"], it["assunto"])]
            sub = it["subassunto"]
            sub_sql = "NULL" if sub is None else str(
                refs["subtopics"][(it["discipline"], it["assunto"], sub)])
            obs = "NULL" if it["observation"] is None else esc(it["observation"])
            stmts.append(
                "INSERT INTO question_classifications (question_id, "
                "taxonomy_version, topic_id, subtopic_id, skill, "
                "reasoning_type, confidence, evidence, origin, observation) "
                f"VALUES ({qid}, '{TAXONOMY_VERSION}', {topic_id}, {sub_sql}, "
                f"{esc(it['skill'])}, {esc(it['reasoning'])}, "
                f"{esc(it['confidence'])}, {esc(it['evidence'])}, "
                "'CLASSIFICACAO_DERIVADA_FONTE', " + obs + ");")
        has_fig = has_figure_for(it)
        if has_fig and tag_counts.get(qid, 0) == 0:
            stmts.append(
                "INSERT INTO question_tag_map (question_id, tag_id) "
                "SELECT {qid}, id FROM question_tags WHERE code = 'FIGURA' "
                "ON CONFLICT DO NOTHING;".format(qid=qid))
    return "\n".join(stmts)


# ---------------- verificacao final ----------------

def verify(env: dict[str, str], items: list[dict],
           institution: str = "IFRN") -> dict:
    inst = institution.replace("'", "''")
    filt = f"q.source_type = 'OFFICIAL' AND e.institution = '{inst}'"
    join_q = "JOIN exams e ON e.id = q.exam_id"
    q = psql(env, "SELECT count(*) FROM questions q "
                  f"{join_q} WHERE {filt}").strip()
    o = psql(env, "SELECT count(*) FROM question_options o JOIN questions q "
                  f"ON q.id = o.question_id {join_q} WHERE {filt}").strip()
    s = psql(env, "SELECT count(*) FROM question_sources s JOIN questions q "
                  f"ON q.id = s.question_id {join_q} WHERE {filt}").strip()
    c = psql(env, "SELECT count(*) FROM question_classifications cl "
                  f"JOIN questions q ON q.id = cl.question_id {join_q} "
                  f"WHERE {filt}").strip()
    f = psql(env, "SELECT count(*) FROM questions q "
                  f"{join_q} WHERE {filt} AND q.has_figure").strip()
    # checksums no banco == recalculados das fontes (namespace por ano:
    # (institution,year,number) — EAJ-2022 ≠ IFRN-2022).
    out = psql(env,
        "SELECT q.source_year, q.source_question_number, q.checksum "
        f"FROM questions q {join_q} WHERE {filt}")
    db_cks = {}
    for line in out.splitlines():
        if line.strip():
            yr, num, cks = line.split("|")
            db_cks[(yr, int(num))] = cks
    mism = [(it["edition"], it["number"]) for it in items
            if db_cks.get((it["edition"], it["number"])) != it["checksum"]]
    # checksums distintos (trava duplicidade acidental)
    cks_list = [it["checksum"] for it in items]
    dup = len(cks_list) != len(set(cks_list))
    # por disciplina (auditoria EAJ: LP 55 / MAT 55 / CN 12 / CH 8)
    by_disc: dict[str, int] = {}
    for it in items:
        by_disc[it["discipline"]] = by_disc.get(it["discipline"], 0) + 1
    return {"questions": int(q), "options": int(o), "sources": int(s),
            "classifications": int(c), "has_figure": int(f),
            "checksum_mismatches": mism,
            "checksums_distinct": not dup,
            "by_discipline": by_disc}


# ---------------- main ----------------

def main() -> int:
    ap = argparse.ArgumentParser(
        description="Importador idempotente (TASK 2.3 + TASK D.3 EAJ)")
    ap.add_argument("--institution", default="IFRN", choices=["IFRN", "EAJ"],
                    help="processo seletivo (namespace; default IFRN legado)")
    ap.add_argument("--check", action="store_true",
                    help="somente valida fontes + estado do banco, sem escrever")
    ap.add_argument("--report", default=None,
                    help="caminho do relatorio JSON de auditoria "
                         "(default: report.json IFRN, report-eaj.json EAJ)")
    ap.add_argument("--allow-needs-visual-check", action="store_true",
                    help="EAJ: libera Q22/Q39-2025 NEEDS_VISUAL_CHECK após "
                         "conferência visual no PDF (curadoria; nunca inferência)")
    args = ap.parse_args()
    institution = args.institution
    report_path = args.report or str(
        DEFAULT_REPORT_EAJ if institution == "EAJ" else DEFAULT_REPORT)

    started = datetime.now(timezone.utc).isoformat()
    if institution == "EAJ":
        return main_eaj(args, report_path, started)
    items = load_sources()
    env = pg_env()
    check_schema(env)
    refs = load_refs(env, items, "IFRN")
    existing = fetch_existing(env, "IFRN")

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
        "institution": "IFRN",
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
        write_report(report_path, report)
        print(f"DIVERGENCIA: {len(divergences)} questao(oes) no banco com "
              f"checksum/documento diferente da fonte. Nada foi alterado. "
              f"Ver {report_path}", file=sys.stderr)
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
            existing = fetch_existing(env, "IFRN")
        kids = fetch_children(env)
        # top-up: completa filhos faltantes (recuperacao + idempotencia)
        todo = [it for it in items
                if (it["edition"], it["number"]) in existing]
        cfile = tmpdir / "children.sql"
        cfile.write_text("BEGIN;\n" + sql_insert_children(todo, refs, existing,
                                                           kids)
                         + "\nCOMMIT;\n", encoding="utf-8")
        psql_file(env, cfile)

    state = verify(env, items, "IFRN")
    report["db_state"] = state
    ok = (state["questions"] == 240 and state["options"] == 960
          and state["sources"] == 480 and state["classifications"] == 240
          and state["has_figure"] == 33 and not state["checksum_mismatches"]
          and state["checksums_distinct"])
    report["status"] = "OK" if ok else "MISMATCH"
    report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
    write_report(report_path, report)

    print(f"[IFRN] candidatas=240 a_inserir={len(inserted)} "
          f"ja_presentes={len(skipped)} divergencias=0")
    print(f"banco(IFRN): questions={state['questions']} options={state['options']} "
          f"sources={state['sources']} classifications={state['classifications']} "
          f"has_figure={state['has_figure']}")
    print(f"relatorio: {report_path}")
    if args.check:
        print("(modo --check: nenhuma escrita executada)")
    return 0 if ok else 1


def main_eaj(args, report_path: str, started: str) -> int:
    allow_visual = args.allow_needs_visual_check
    items_safe, blocked = load_sources_eaj()
    if allow_visual:
        items = items_safe + [b["_item"] for b in blocked]
        blocked_report = [
            {k: b[k] for k in ("institution", "edition", "number",
                               "status", "answer_key", "reason")}
            for b in blocked
        ]
        visual_note = ("flag --allow-needs-visual-check ativa: Q22/Q39-2025 "
                       "liberadas APÓS conferência visual no PDF (curadoria)")
    else:
        items = items_safe
        blocked_report = [
            {k: b[k] for k in ("institution", "edition", "number",
                               "status", "answer_key", "reason")}
            for b in blocked
        ]
        visual_note = ("trava D.3: Q22/Q39-2025 NEEDS_VISUAL_CHECK BLOQUEADAS "
                       "até conferência visual no PDF; sem a flag, não entram")
    # 130 = 50+40+40 (D.1/D.2); 128 importáveis sem flag (127 CONFIRMED +
    # Q23 anulada X); 2 bloqueadas com flag pós-conferência.
    total = len(items_safe) + len(blocked)
    env = pg_env()
    check_schema(env)
    # EAJ exige V20 (taxonomia CN/CH + páginas NULL DESCONHECIDO)
    try:
        psql(env, "SELECT 1 FROM topics t JOIN disciplines d "
                  "ON d.id = t.discipline_id "
                  "WHERE d.code = 'CIENCIAS_NATUREZA' AND t.code = 'ECOLOGIA' "
                  "LIMIT 1")
    except ImportFailure:
        pass
    refs = load_refs(env, items, "EAJ")
    existing = fetch_existing(env, "EAJ")

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
                "institution": "EAJ",
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
        "institution": "EAJ",
        "provenance": "TRANSCRIBED_FROM_MD",
        "provenance_note": ("respostas = gabarito TRANSCRITO nos .md "
                            "(decisão 2026-10-09); nunca oficial; sem PDF de "
                            "gabarito no repo — ver docs/blockers.md"),
        "started_at_utc": started,
        "check_only": args.check,
        "allow_needs_visual_check": allow_visual,
        "visual_note": visual_note,
        "source_files": 9,
        "candidates_total": total,
        "candidates_importable": len(items),
        "blocked_needs_visual_check": blocked_report,
        "candidates": len(items),
        "to_insert": len(inserted),
        "already_present": len(skipped),
        "divergences": divergences,
    }

    if divergences:
        report["status"] = "DIVERGENCE"
        report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
        write_report(report_path, report)
        print(f"[EAJ] DIVERGENCIA: {len(divergences)} questao(oes) no banco com "
              f"checksum/documento diferente da fonte. Nada foi alterado. "
              f"Ver {report_path}", file=sys.stderr)
        return 2

    if not args.check:
        tmpdir = Path(os.environ.get("TMPDIR", "/tmp")) / "voupassar-import-eaj"
        tmpdir.mkdir(parents=True, exist_ok=True)
        if inserted:
            qfile = tmpdir / "questions.sql"
            qfile.write_text("BEGIN;\n"
                             + sql_insert_questions(inserted, refs)
                             + "\nCOMMIT;\n", encoding="utf-8")
            psql_file(env, qfile)
            existing = fetch_existing(env, "EAJ")
        kids = fetch_children(env)
        todo = [it for it in items
                if (it["edition"], it["number"]) in existing]
        cfile = tmpdir / "children.sql"
        cfile.write_text("BEGIN;\n" + sql_insert_children(todo, refs, existing,
                                                           kids)
                         + "\nCOMMIT;\n", encoding="utf-8")
        psql_file(env, cfile)

    state = verify(env, items, "EAJ")
    report["db_state"] = state
    if allow_visual:
        ok = (state["questions"] == 130 and state["options"] == 520
              and state["sources"] == 260 and state["classifications"] == 130
              and state["has_figure"] == 0
              and not state["checksum_mismatches"]
              and state["checksums_distinct"]
              and state["by_discipline"].get("LINGUA_PORTUGUESA") == 55
              and state["by_discipline"].get("MATEMATICA") == 55
              and state["by_discipline"].get("CIENCIAS_NATUREZA") == 12
              and state["by_discipline"].get("CIENCIAS_HUMANAS") == 8)
        report["status"] = "OK" if ok else "MISMATCH"
    else:
        # Sem flag: 128 importáveis (127 CONFIRMED + Q23 X); 2 bloqueadas.
        # Status final é sempre BLOCKED (trava D.3) — mesmo com 128 OK.
        safe_ok = (state["questions"] == 128 and state["options"] == 512
                   and state["sources"] == 256
                   and state["classifications"] == 128
                   and state["has_figure"] == 0
                   and not state["checksum_mismatches"]
                   and state["checksums_distinct"])
        report["safe_ok_128"] = safe_ok
        report["status"] = "BLOCKED_NEEDS_VISUAL_CHECK"
    report["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
    write_report(report_path, report)

    print(f"[EAJ] total=130 importaveis={len(items)} "
          f"bloqueadas={len(blocked_report)} a_inserir={len(inserted)} "
          f"ja_presentes={len(skipped)} divergencias=0")
    for b in blocked_report:
        print(f"[EAJ] BLOQUEADA EAJ-{b['edition']} Q{b['number']}: "
              f"{b['status']} ({b['reason'][:100]})")
    print(f"banco(EAJ): questions={state['questions']} options={state['options']} "
          f"sources={state['sources']} classifications={state['classifications']} "
          f"has_figure={state['has_figure']} by_disc={state['by_discipline']}")
    print(f"relatorio: {report_path}")
    if args.check:
        print("(modo --check: nenhuma escrita executada)")
    if not allow_visual:
        print(f"[EAJ] {visual_note} (exit 2)", file=sys.stderr)
        return 2
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
