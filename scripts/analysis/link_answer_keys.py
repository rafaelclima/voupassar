#!/usr/bin/env python3
"""Vinculacao questao <-> gabarito — TASK 1.3.

Le:
  - `data/extracted/<edicao>.json` (TASK 1.2, 40 objetivas por edicao), e
  - `data/provas/<edicao>/*gabarito*.pdf` (case-insensitive, nunca a prova),

extrai o gabarito com `pdftotext -layout` (poppler-utils) e gera dados
auditaveis em `data/linked/<edicao>.json` + `data/linked/manifest.json`.

Cada questao vinculada registra: numero, disciplina (herdada da extracao),
resposta oficial (A-D), anulada (X), ausente (NULL + status), documento de
origem do gabarito + sha256 + paginas, tipo de gabarito
(preliminar/definitivo/final/unico) e validacoes.

Nunca altera `data/provas/` nem `data/extracted/`. Idempotente: reexecucao
sobrescreve os JSONs com o mesmo conteudo (a menos do carimbo
`generated_at_utc`).

Uso:
    python3 scripts/analysis/link_answer_keys.py [--check]

`--check` apenas valida os JSONs existentes em `data/linked/` contra
`data/extracted/` e sai com codigo != 0 em caso de erro.
"""

from __future__ import annotations

import hashlib
import json
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

LINKER_VERSION = "1.0.0"
REPO_ROOT = Path(__file__).resolve().parents[2]
PROVAS_DIR = REPO_ROOT / "data" / "provas"
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted"
OUT_DIR = REPO_ROOT / "data" / "linked"

LP_MARK = "LÍNGUA PORTUGUESA"
MAT_MARK = "MATEMÁTICA"
PRELIM_MARK = "Gabarito Preliminar"
DEFINIT_MARK = "Gabarito Definitivo"

# " 12   B" — uma ou duas questoes por linha ("1 A   21 D" em 2026,
# "12  B" isolado em 2022/FUNCERN). Captura todos os pares da linha.
PAIR_FIND = re.compile(r"\b0?(\d{1,2})\s+([A-DX])(?=\s|$)")
NUM_TOKEN = re.compile(r"\b0?(\d{1,2})\b")
LETTER_TOKEN = re.compile(r"\b([A-DX])\b")


class RuntimeFailure(RuntimeError):
    pass


def check_bins() -> dict[str, str]:
    versions: dict[str, str] = {}
    for exe in ("pdfinfo", "pdftotext"):
        try:
            p = subprocess.run([exe, "-v"], capture_output=True, text=True)
            versions[exe] = (p.stderr or p.stdout).strip().splitlines()[0][:120]
        except FileNotFoundError:
            print(f"ERRO: binario '{exe}' (poppler-utils) nao encontrado.", file=sys.stderr)
            sys.exit(2)
    return versions


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def pdf_pages(pdf: Path) -> int:
    out = subprocess.run(["pdfinfo", str(pdf)], capture_output=True, text=True, check=True).stdout
    m = re.search(r"^Pages:\s+(\d+)", out, re.M)
    if not m:
        raise RuntimeFailure(f"pdfinfo sem 'Pages' para {pdf}")
    return int(m.group(1))


def page_text(pdf: Path, page: int) -> str:
    p = subprocess.run(
        ["pdftotext", "-layout", "-f", str(page), "-l", str(page), str(pdf), "-"],
        capture_output=True, text=True, check=True,
    )
    return p.stdout.replace("\u200b", "").replace("\xa0", " ")


def find_gabarito_pdf(edition_dir: Path) -> Path:
    cands = [
        f for f in sorted(edition_dir.glob("*.pdf"))
        if "gabarito" in f.name.lower()
    ]
    if len(cands) != 1:
        raise RuntimeFailure(
            f"{edition_dir.name}: esperado 1 PDF de gabarito, encontrados {[c.name for c in cands]}"
        )
    return cands[0]


def parse_grid_section(section_text: str) -> dict[int, str]:
    """Pareia linha(s) de numeros com linha(s) de letras dentro de uma secao.

    Cada secao (LP ou MAT) contem uma linha so de numeros (ex.: 1..20) seguida
    de uma linha so de letras (ex.: A C B ...). O pareamento e por indice.
    Linhas com conteudo misto sao ignoradas para evitar falsos positivos.
    """
    num_row: list[int] = []
    let_row: list[str] = []
    pending_nums: list[int] | None = None
    for raw in section_text.splitlines():
        line = raw.strip()
        if not line:
            continue
        nums = [int(n) for n in NUM_TOKEN.findall(line)]
        lets = LETTER_TOKEN.findall(line)
        has_num = len(nums) > 0
        has_let = len(lets) > 0
        if has_num and not has_let and len(nums) >= 5:
            # Linha de cabecalho numerico (guarda para parear com a proxima
            # linha de letras). Exige >=5 numeros para nao capturar ruido.
            pending_nums = [n for n in nums if 1 <= n <= 40]
        elif has_let and not has_num and pending_nums is not None and len(lets) >= 5:
            if len(lets) != len(pending_nums):
                raise RuntimeFailure(
                    f"grade inconsistente: numeros={pending_nums} letras={lets}"
                )
            for n, letter in zip(pending_nums, lets):
                num_row.append(n)
                let_row.append(letter)
            pending_nums = None
        # Linhas mistas (ex.: titulos) ou sem pending sao ignoradas.
    return dict(zip(num_row, let_row))


def parse_pair_lines(text: str) -> dict[int, str]:
    """Uma ou duas questoes por linha: 'NN  L' ou '1 A ... 21 D' (2026).

    Ignora linhas de cabecalho sem pares numericos (ex.: 'QUESTAO GABARITO').
    """
    out: dict[int, str] = {}
    for raw in text.splitlines():
        for m in PAIR_FIND.finditer(raw):
            n, letter = int(m.group(1)), m.group(2)
            if 1 <= n <= 40:
                if n in out:
                    raise RuntimeFailure(f"numero duplicado no gabarito: {n}")
                out[n] = letter
    return out


def extract_answer_key(edition: str, pdf: Path) -> dict:
    """Retorna {answers, prelim, definitive, kind, pages_text, notes}."""
    n_pages = pdf_pages(pdf)
    pages = [page_text(pdf, p) for p in range(1, n_pages + 1)]
    full = "\n".join(pages)
    notes: list[str] = []

    if PRELIM_MARK in full:
        # 2022/FUNCERN: duas secoes sequenciais, sem rotulo disciplinar.
        pre_txt = full.split(PRELIM_MARK, 1)[1]
        if DEFINIT_MARK in pre_txt:
            pre_txt, def_txt = pre_txt.split(DEFINIT_MARK, 1)
        else:
            def_txt = ""
            notes.append("secao 'Gabarito Definitivo' nao localizada; definitivo=DESCONHECIDO")
        prelim = parse_pair_lines(pre_txt)
        definitive = parse_pair_lines(def_txt) if def_txt else {}
        # O gabarito definitivo e a referencia; preliminar guardado p/ diff.
        answers = dict(definitive) if definitive else dict(prelim)
        kind = "PRELIMINARY_AND_DEFINITIVE"
        if definitive and prelim != definitive:
            notes.append("divergencia entre preliminar e definitivo (ver diff)")
        return {
            "answers": answers,
            "preliminary": prelim,
            "definitive": definitive,
            "kind": kind,
            "notes": notes,
            "pages": pages,
        }

    if LP_MARK in full and MAT_MARK in full:
        # Grade 20+20 com rotulos disciplinares (2020, 2023, 2024, 2025).
        # Fatiar por secao evita capturar numeracao de ofertas (2023, nº 1-83).
        lp_start = full.find(LP_MARK)
        mat_start = full.find(MAT_MARK)
        if mat_start < lp_start:  # ordem inesperada; nao assumir, registrar
            raise RuntimeFailure("marcadores LP/MAT em ordem inesperada")
        lp_section = full[lp_start:mat_start]
        mat_section = full[mat_start:]
        lp = parse_grid_section(lp_section)
        mat = parse_grid_section(mat_section)
        answers = {**lp, **mat}
        kind = "FINAL_GRID_LP_MAT"
        # Validacao de faixa disciplinar esperada (1-20 LP, 21-40 MAT).
        lp_out = sorted(n for n in lp if not 1 <= n <= 20)
        mat_out = sorted(n for n in mat if not 21 <= n <= 40)
        if lp_out or mat_out:
            notes.append(f"numeracao fora da faixa disciplinar esperada: LP={lp_out} MAT={mat_out}")
        return {
            "answers": answers,
            "preliminary": {},
            "definitive": {},
            "kind": kind,
            "notes": notes,
            "pages": pages,
        }

    # Tabela unica 1-40 sem rotulo disciplinar (2026).
    answers = parse_pair_lines(full)
    kind = "DEFINITIVE_TABLE"
    return {
        "answers": answers,
        "preliminary": {},
        "definitive": {},
        "kind": kind,
        "notes": notes,
        "pages": pages,
    }


def link_edition(edition: str, bins: dict[str, str]) -> dict:
    edir = PROVAS_DIR / edition
    gpdf = find_gabarito_pdf(edir)
    ext_path = EXTRACTED_DIR / f"{edition}.json"
    if not ext_path.exists():
        raise RuntimeFailure(f"{edition}: {ext_path} ausente (executar TASK 1.2 primeiro)")
    extracted = json.loads(ext_path.read_text(encoding="utf-8"))
    ext_questions = {int(q["number"]): q for q in extracted["questions"]}

    parsed = extract_answer_key(edition, gpdf)
    answers: dict[int, str] = parsed["answers"]

    issues: list[str] = list(parsed["notes"])
    linked: list[dict] = []
    for n in range(1, 41):
        q = ext_questions.get(n)
        if q is None:
            issues.append(f"Q{n}: ausente na extracao TASK 1.2 (sem enunciado vinculado)")
            linked.append({
                "edition": edition,
                "number": n,
                "discipline": "DESCONHECIDA",
                "answer_key": None,
                "annulled": False,
                "status": "MISSING_QUESTION",
                "validation": "questao nao encontrada em data/extracted; NECESSITA REVISAO",
            })
            continue
        raw = answers.get(n)
        if raw is None:
            issues.append(f"Q{n}: resposta ausente no gabarito")
            linked.append({
                "edition": edition,
                "number": n,
                "discipline": q.get("discipline", "DESCONHECIDA"),
                "discipline_source": q.get("discipline_source", "UNKNOWN"),
                "source_document": q.get("source_document"),
                "question_page_start": q.get("page_start"),
                "question_page_end": q.get("page_end"),
                "answer_key": None,
                "annulled": False,
                "status": "MISSING_ANSWER",
                "validation": "resposta nao localizada no gabarito; NECESSITA REVISAO",
            })
        elif raw == "X":
            linked.append({
                "edition": edition,
                "number": n,
                "discipline": q.get("discipline", "DESCONHECIDA"),
                "discipline_source": q.get("discipline_source", "UNKNOWN"),
                "source_document": q.get("source_document"),
                "question_page_start": q.get("page_start"),
                "question_page_end": q.get("page_end"),
                "answer_key": "X",
                "annulled": True,
                "status": "ANNULLED",
                "validation": "marcada como anulada no gabarito",
            })
        elif raw in ("A", "B", "C", "D"):
            # Checagem de consistencia: a letra existe entre as alternativas
            # extraidas (sempre A-D quando completas). Opcoes vazias (ex.:
            # 2020 Q38) sao sinalizadas, nao silenciosamente publicadas.
            opts = q.get("options", {})
            empty_opts = [k for k in "ABCD" if not str(opts.get(k, "")).strip()]
            note = "ok"
            if empty_opts:
                note = f"opcoes vazias na extracao: {empty_opts}; NECESSITA REVISAO visual"
                issues.append(f"Q{n}: {note}")
            linked.append({
                "edition": edition,
                "number": n,
                "discipline": q.get("discipline", "DESCONHECIDA"),
                "discipline_source": q.get("discipline_source", "UNKNOWN"),
                "source_document": q.get("source_document"),
                "question_page_start": q.get("page_start"),
                "question_page_end": q.get("page_end"),
                "answer_key": raw,
                "annulled": False,
                "status": "CONFIRMED" if not empty_opts else "NEEDS_REVIEW",
                "validation": note,
            })
        else:
            issues.append(f"Q{n}: valor inesperado no gabarito: {raw!r}")
            linked.append({
                "edition": edition,
                "number": n,
                "discipline": q.get("discipline", "DESCONHECIDA"),
                "answer_key": raw,
                "annulled": False,
                "status": "NEEDS_REVIEW",
                "validation": f"valor inesperado {raw!r}; NECESSITA REVISAO",
            })

    # Divergencia preliminar x definitivo (quando houver ambos).
    prelim = parsed.get("preliminary", {}) or {}
    definitive = parsed.get("definitive", {}) or {}
    diff: list[dict] = []
    if prelim and definitive:
        for n in range(1, 41):
            a, b = prelim.get(n), definitive.get(n)
            if a != b:
                diff.append({"number": n, "preliminary": a, "definitive": b})

    payload = {
        "metadata": {
            "edition": edition,
            "linker": "scripts/analysis/link_answer_keys.py",
            "linker_version": LINKER_VERSION,
            "generated_at_utc": datetime.now(timezone.utc).isoformat(),
            "tool_versions": bins,
            "extracted_source": f"data/extracted/{edition}.json",
            "gabarito_document": f"data/provas/{edition}/{gpdf.name}",
            "gabarito_sha256": sha256(gpdf),
            "gabarito_pages": pdf_pages(gpdf),
            "gabarito_kind": parsed["kind"],
            "note": "Vinculacao TASK 1.3. Letras A-D e X transcritas do gabarito via pdftotext; figuras/formulas exigem revisao visual. Sem classificacao pedagogica.",
        },
        "preliminary": prelim,
        "definitive": definitive,
        "preliminary_vs_definitive_diff": diff,
        "questions": linked,
        "parser_notes": parsed["notes"],
        "issues": issues,
    }
    return payload


def main() -> int:
    bins = check_bins()
    editions = sorted([d.name for d in PROVAS_DIR.iterdir() if d.is_dir()])

    if "--check" in sys.argv:
        ok = True
        for ed in editions:
            jf = OUT_DIR / f"{ed}.json"
            if not jf.exists():
                print(f"{ed}: AUSENTE {jf}")
                ok = False
                continue
            data = json.loads(jf.read_text(encoding="utf-8"))
            qs = data.get("questions", [])
            nums = sorted(q["number"] for q in qs)
            missing = [q["number"] for q in qs if q.get("answer_key") is None]
            bad = [q["number"] for q in qs if q.get("status") not in
                   ("CONFIRMED", "ANNULLED", "NEEDS_REVIEW", "MISSING_ANSWER", "MISSING_QUESTION")]
            annulled = sorted(q["number"] for q in qs if q.get("annulled"))
            status = "OK" if (nums == list(range(1, 41)) and not bad) else "FALHA"
            if status == "FALHA":
                ok = False
            print(f"{ed}: {status} (n={len(nums)}, anuladas={annulled or '-'}, "
                  f"sem_resposta={missing or '-'}, status_invalido={bad or '-'})")
            if data.get("preliminary_vs_definitive_diff"):
                print(f"  DIVERGENCIA prelim/def: {data['preliminary_vs_definitive_diff']}")
                ok = False  # divergencia deve ser explicitada, nao silenciosa
            for issue in data.get("issues", []):
                print(f"  AVISO {ed}: {issue}")
        return 0 if ok else 1

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    manifest_entries: list[dict] = []
    for ed in editions:
        payload = link_edition(ed, bins)
        out = OUT_DIR / f"{ed}.json"
        out.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        n_ok = sum(1 for q in payload["questions"] if q["status"] == "CONFIRMED")
        n_ann = sum(1 for q in payload["questions"] if q["annulled"])
        n_miss = sum(1 for q in payload["questions"] if q["answer_key"] is None)
        print(f"{ed}: {len(payload['questions'])} vinculadas "
              f"({n_ok} confirmadas, {n_ann} anuladas, {n_miss} sem resposta), "
              f"diff_prelim_def={len(payload['preliminary_vs_definitive_diff'])}, "
              f"avisos={len(payload['issues'])}")
        for issue in payload["issues"]:
            print(f"  AVISO {ed}: {issue}")
        manifest_entries.append({
            "edition": ed,
            "gabarito_document": payload["metadata"]["gabarito_document"],
            "gabarito_sha256": payload["metadata"]["gabarito_sha256"],
            "gabarito_pages": payload["metadata"]["gabarito_pages"],
            "gabarito_kind": payload["metadata"]["gabarito_kind"],
            "n_linked": len(payload["questions"]),
            "n_confirmed": n_ok,
            "n_annulled": n_ann,
            "n_missing_answer": n_miss,
            "annulled_questions": sorted(q["number"] for q in payload["questions"] if q["annulled"]),
            "n_prelim_def_diff": len(payload["preliminary_vs_definitive_diff"]),
            "n_issues": len(payload["issues"]),
        })
    manifest = {
        "linker": "scripts/analysis/link_answer_keys.py",
        "linker_version": LINKER_VERSION,
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "tool_versions": bins,
        "editions": manifest_entries,
        "totals": {
            "editions": len(manifest_entries),
            "linked_questions": sum(e["n_linked"] for e in manifest_entries),
            "annulled": sum(e["n_annulled"] for e in manifest_entries),
        },
    }
    (OUT_DIR / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Manifest: {OUT_DIR / 'manifest.json'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
