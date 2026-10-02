#!/usr/bin/env python3
"""Pipeline de extracao de questoes objetivas — TASK 1.2.

Le as provas em `data/provas/<edicao>/*.pdf` (somente o caderno, nunca o
gabarito) com `pdftotext -layout` pagina a pagina (poppler-utils) e gera
dados intermediarios auditaveis em `data/extracted/<edicao>.json` mais um
`data/extracted/manifest.json` global.

Cada questao registra: numero, enunciado (bruto), alternativas A-D
(brutas), pagina inicial/final, disciplina (derivada do cabecalho de secao
na ordem de leitura, com fallback para faixa numerica sinalizado),
documento de origem + sha256. A proposta discursiva e registrada a parte
(pagina + trecho inicial), sem classificacao pedagogica.

Somente stdlib + binarios poppler (`pdfinfo`, `pdftotext`). Nunca altera
`data/provas/`. Idempotente: reexecucao sobrescreve os JSONs com o mesmo
conteudo (a menos do carimbo `generated_at_utc`).

Uso:
    python3 scripts/analysis/extract_questions.py [--check]

`--check` apenas valida os JSONs existentes (40 objetivas 1-40 unicas,
4 alternativas nao vazias) e sai com codigo != 0 em caso de erro.
"""

from __future__ import annotations

import hashlib
import json
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

EXTRACTOR_VERSION = "1.1.0"
REPO_ROOT = Path(__file__).resolve().parents[2]
PROVAS_DIR = REPO_ROOT / "data" / "provas"
OUT_DIR = REPO_ROOT / "data" / "extracted"

LP_HEADER = "LÍNGUA PORTUGUESA"
MAT_HEADER = "MATEMÁTICA"
SECTION_MARK = "QUESTÕES DE MÚLTIPLA ESCOLHA"
# Maiusculo exato do cabecalho da secao discursiva. Cobre tanto
# "PROPOSTA DE PRODUÇÃO TEXTUAL" (2020-2025) quanto "PRODUÇÃO TEXTUAL"
# isolado (2026). Ocorrencias na capa usam caixa mista ("Produção
# Textual") e por isso nao disparam. Primeira ocorrencia = proposta.
DISCURSIVE_MARK = "PRODUÇÃO TEXTUAL"

# "01. texto" / "21. texto" — exige espaco apos o ponto (rejeita "10.000",
# "4.1"), tolera U+200B (2026), NBSP e zeros a esquerda.
Q_START = re.compile(r"^\s*0?([1-9]|[12][0-9]|3[0-9]|40)\.\s+(?=\S)")
# "A) texto" ou "A)" isolado (fracao vertical com texto na linha seguinte).
# Tolera U+200B, NBSP e "A." ocasional.
ALT_START = re.compile(r"^\s*([A-D])[\).]\s*(?:(?=\S)|$)")

# Rodape de pagina do caderno (v1.1.0): boilerplate do IFRN que o
# pdftotext -layout entrega no meio do bloco da questao e colava na
# alternativa D (ultima da pagina). Variantes observadas 2020-2026:
#   "Processo Seletivo – ... Forma Integrada 2022 ... 13" (nº da pagina
#   colado no fim) e "PROCESSO SELETIVO ... EDITAL Nº ... – PROEN/IFRN"
#   (2024 usa "– PROEN/RN"). Auditoria em 2026-10-02: TODA linha de bloco
#   contendo "processo seletivo" casa este padrao — zero falso positivo —
#   por isso a regra dispensa recorte por posicao na pagina. Numero de
#   pagina isolado ("4", "11") so sai quando vizinho de rodape (fração
#   "5/2400" e numero de figura jamais sao tocados).
FOOTER_LINE = re.compile(
    r"processo\s+seletivo\b.*(edital|proen|forma\s+integrada)"
    r"|^\s*edital\s+n[ºo]?\b.*(proen|ifrn)"
    r"|^\s*ifrn\s+[–—-]\s*exame\s+de\s+sele[cç][aã]o\s+[–—-]\s+20\d\d\b",
    re.I,
)
LONE_PAGE_NUMBER = re.compile(r"^\s*\d{1,3}\s*$")


def strip_page_furniture(block_lines: list[str]) -> tuple[list[str], int]:
    """Remove rodape + nº de pagina vizinho do bloco. Retorna (linhas, n)."""
    is_footer = [bool(FOOTER_LINE.search(l)) for l in block_lines]
    if not any(is_footer):
        return block_lines, 0
    drop = [False] * len(block_lines)
    for i, f in enumerate(is_footer):
        if f:
            drop[i] = True
    for i, line in enumerate(block_lines):
        if drop[i] or not LONE_PAGE_NUMBER.match(line):
            continue
        # 2026 poe o rodape no TOPO da pagina e o nº embaixo ("4" p4 +
        # PROCESSO/EDITAL p5); as demais edicoes colam o nº no rodape.
        # Logo o nº de pagina pode estar ANTES ou DEPOIS do rodape.
        # A varredura ignora SÓ brancos: linhas de rodape marcadas contam
        # como vizinhas (se fossem puladas, o nº perderia a referencia).
        for step in (-1, 1):
            j = i + step
            while 0 <= j < len(block_lines) and not block_lines[j].strip():
                j += step
            if 0 <= j < len(block_lines) and is_footer[j]:
                drop[i] = True
                break
    kept = [l for l, d in zip(block_lines, drop) if not d]
    return kept, sum(drop)


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
        raise Runtimefailure(f"pdfinfo sem 'Pages' para {pdf}")
    return int(m.group(1))


class Runtimefailure(RuntimeError):
    pass


def page_text(pdf: Path, page: int) -> str:
    p = subprocess.run(
        ["pdftotext", "-layout", "-f", str(page), "-l", str(page), str(pdf), "-"],
        capture_output=True, text=True, check=True,
    )
    return p.stdout.replace("\u200b", "").replace("\xa0", " ")


def find_prova_pdf(edition_dir: Path) -> Path:
    cands = [
        f for f in sorted(edition_dir.glob("*.pdf"))
        if "gabarito" not in f.name.lower()
    ]
    if len(cands) != 1:
        raise Runtimefailure(
            f"{edition_dir.name}: esperado 1 PDF de prova, encontrados {[c.name for c in cands]}"
        )
    return cands[0]


def parse_options(block_lines: list[str]) -> tuple[dict[str, str], list[str]]:
    """Extrai A-D do bloco. Retorna (opcoes, avisos)."""
    opts: dict[str, list[str]] = {}
    current: str | None = None
    pre_lines: list[str] = []
    warnings: list[str] = []
    for line in block_lines[1:]:  # pula a linha "NN. ..."
        m = ALT_START.match(line)
        if m:
            current = m.group(1)
            tail = line[m.end():].strip()
            opts.setdefault(current, []).append(tail)
            if not tail:
                warnings.append(
                    f"alternativa {current} sem texto na mesma linha "
                    "(possivel fracao/figura vertical; conferir raw_block)"
                )
        elif current is not None:
            if line.strip():
                opts[current].append(line.strip())
        else:
            pre_lines.append(line)
    options = {k: " ".join(v).strip() for k, v in opts.items()}
    for letter in "ABCD":
        if letter not in options or not options[letter]:
            warnings.append(f"alternativa {letter} ausente ou vazia")
    statement = " ".join(
        [block_lines[0][Q_START.match(block_lines[0]).end():].strip()]  # type: ignore[union-attr]
        + [l.strip() for l in pre_lines if l.strip()]
    )
    statement = re.sub(r"\s+", " ", statement).strip()
    if not statement:
        warnings.append("enunciado vazio")
    return {"statement": statement, **{k: options.get(k, "") for k in "ABCD"}}, warnings


def extract_edition(edition: str) -> dict:
    edir = PROVAS_DIR / edition
    pdf = find_prova_pdf(edir)
    n_pages = pdf_pages(pdf)
    warnings: list[str] = []

    # Passada 1: texto por pagina (ordem de leitura preservada).
    pages: list[str] = []
    for n in range(1, n_pages + 1):
        pages.append(page_text(pdf, n))

    # Passada 2: blocos de questao (podem atravessar paginas). A disciplina
    # e rastreada linha a linha na ordem de leitura, pois o cabecalho da
    # secao pode estar no meio da pagina (ex.: 2026, MAT apos Q18).
    questions: list[dict] = []
    buf: list[str] = []
    buf_num: int | None = None
    buf_page_start = 1
    buf_disc = "DESCONHECIDA"
    current_disc = "DESCONHECIDA"
    disc_source = "UNKNOWN"

    def flush(page_end: int) -> None:
        nonlocal buf, buf_num
        if buf_num is None:
            buf = []
            return
        # v1.1.0: rodape/nº de pagina fora do parse (raw_block preserva a
        # evidencia original para auditoria).
        clean_buf, furniture = strip_page_furniture(buf)
        parsed, warns = parse_options(clean_buf)
        if furniture:
            warns.append(
                f"rodape de pagina removido do bloco ({furniture} linhas; "
                "ver raw_block)"
            )
        disc = buf_disc
        dsource = disc_source if disc_source != "UNKNOWN" else "UNKNOWN"
        if dsource == "UNKNOWN":  # fallback auditavel por faixa numerica
            disc = "LINGUA_PORTUGUESA" if buf_num <= 20 else "MATEMATICA"
            dsource = "INFERRED_RANGE"
        questions.append(
            {
                "edition": edition,
                "number": buf_num,
                "kind": "OBJECTIVE",
                "discipline": disc,
                "discipline_source": dsource,
                "source_document": f"data/provas/{edition}/{pdf.name}",
                "page_start": buf_page_start,
                "page_end": page_end,
                "statement": parsed["statement"],
                "options": {k: parsed[k] for k in "ABCD"},
                "raw_block": "\n".join(buf)[:6000],
            }
        )
        for w in warns:
            warnings.append(f"Q{buf_num}: {w}")
        buf, buf_num = [], None

    for idx, txt in enumerate(pages):
        pg = idx + 1
        for line in txt.splitlines():
            if DISCURSIVE_MARK in line:
                # Proposta discursiva sempre apos Q40: encerra a leitura de
                # objetivas para nao capturar listas de instrucoes ("1. ...").
                flush(pg)
                in_discursive = True
                break
            if SECTION_MARK in line:
                if LP_HEADER in line:
                    current_disc, disc_source = "LINGUA_PORTUGUESA", "HEADER"
                elif MAT_HEADER in line:
                    current_disc, disc_source = "MATEMATICA", "HEADER"
                continue
            m = Q_START.match(line)
            if m:
                num = int(m.group(1))
                last = questions[-1]["number"] if questions else 0
                if buf_num is not None:
                    last = max(last, buf_num)
                if num <= last:
                    # Falso positivo (ex.: "10.000 m2", item de orientacao):
                    # funde ao bloco corrente e registra.
                    warnings.append(
                        f"p{pg}: marcador '{num}.' apos Q{last} fundido ao bloco "
                        "(provavel falso positivo de numeracao)"
                    )
                    if buf_num is not None:
                        buf.append(line.rstrip())
                    continue
                flush(pg)
                buf_num = num
                buf_page_start = pg
                buf_disc = current_disc
                buf = [line.rstrip()]
            elif buf_num is not None:
                buf.append(line.rstrip())
        else:
            continue
        break
    else:
        in_discursive = False
    if not in_discursive:
        flush(n_pages)

    # Ordena e valida unicidade/faixa sem reordenar paginas (preserva evidencia).
    seen = [q["number"] for q in questions]
    if sorted(seen) != list(range(1, 41)):
        missing = [n for n in range(1, 41) if n not in seen]
        dupes = sorted({n for n in seen if seen.count(n) > 1})
        warnings.append(f"numeracao inesperada: faltantes={missing} duplicadas={dupes}")
    questions.sort(key=lambda q: q["number"])

    # Discursiva: primeira ocorrencia do cabecalho + trecho.
    discursive: dict = {"present": False}
    full = "\n".join(pages)
    di = full.find(DISCURSIVE_MARK)
    if di >= 0:
        # pagina da proposta: primeira pagina cujo texto contem a marca
        dpage = next((i + 1 for i, t in enumerate(pages) if DISCURSIVE_MARK in t), None)
        snippet = re.sub(r"\s+", " ", full[di:di + 900]).strip()
        discursive = {"present": True, "page": dpage, "proposal_excerpt": snippet[:900]}

    return {
        "edition": edition,
        "pdf": pdf,
        "n_pages": n_pages,
        "questions": questions,
        "discursive": discursive,
        "warnings": warnings,
    }


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
            nums = [q["number"] for q in data["questions"]]
            bad_opts = [
                q["number"] for q in data["questions"]
                if any(not q["options"][k].strip() for k in "ABCD")
            ]
            status = "OK" if (sorted(nums) == list(range(1, 41)) and not bad_opts) else "FALHA"
            if status == "FALHA":
                ok = False
            print(f"{ed}: {status} (n={len(nums)}, opts_vazias={bad_opts or '-'})")
        return 0 if ok else 1

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    manifest_entries: list[dict] = []
    global_warnings: list[str] = []
    for ed in editions:
        res = extract_edition(ed)
        payload = {
            "metadata": {
                "edition": ed,
                "source_document": f"data/provas/{ed}/{res['pdf'].name}",
                "source_sha256": sha256(res["pdf"]),
                "source_pages": res["n_pages"],
                "extractor": "scripts/analysis/extract_questions.py",
                "extractor_version": EXTRACTOR_VERSION,
                "generated_at_utc": datetime.now(timezone.utc).isoformat(),
                "tool_versions": bins,
                "note": "Dados intermediarios para auditoria (TASK 1.2). Enunciados/alternativas em texto bruto de pdftotext; figuras/formulas exigem revisao visual. Sem classificacao pedagogica.",
            },
            "questions": res["questions"],
            "discursive": res["discursive"],
            "warnings": res["warnings"],
        }
        out = OUT_DIR / f"{ed}.json"
        out.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        n_ok = sum(1 for q in res["questions"] if all(q["options"][k].strip() for k in "ABCD"))
        print(f"{ed}: {len(res['questions'])} objetivas ({n_ok} com A-D completas), "
              f"discursiva={res['discursive'].get('present')}, avisos={len(res['warnings'])}")
        for w in res["warnings"]:
            print(f"  AVISO {ed}: {w}")
            global_warnings.append(f"{ed}: {w}")
        manifest_entries.append(
            {
                "edition": ed,
                "source_document": payload["metadata"]["source_document"],
                "source_sha256": payload["metadata"]["source_sha256"],
                "source_pages": res["n_pages"],
                "n_objective": len(res["questions"]),
                "n_complete_options": n_ok,
                "discursive_present": bool(res["discursive"].get("present")),
                "discursive_page": res["discursive"].get("page"),
                "n_warnings": len(res["warnings"]),
            }
        )
    manifest = {
        "extractor": "scripts/analysis/extract_questions.py",
        "extractor_version": EXTRACTOR_VERSION,
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "tool_versions": bins,
        "editions": manifest_entries,
        "totals": {
            "editions": len(manifest_entries),
            "objective_questions": sum(e["n_objective"] for e in manifest_entries),
        },
        "warnings": global_warnings,
    }
    (OUT_DIR / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Manifest: {OUT_DIR / 'manifest.json'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
