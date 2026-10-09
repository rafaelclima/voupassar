#!/usr/bin/env python3
"""Gabarito transcrito EAJ -> `data/linked/eaj/*.json` — TASK B.2.

Consolida, por edicao, a resposta-inline (campo `answer` de
`data/extracted/eaj/<ano>.json`, TASK B.1, SHA-pinned ao `.md`) com a tabela
compilada transcrita ao final de cada `data/provas/EAJ/<ano>/questoes.md`
(schema unico da TASK A.3) e gera evidencia maquina-legivel em
`data/linked/eaj/{2021,2022,2025}.json` + `data/linked/eaj/manifest.json`,
espelhando o papel do `link_answer_keys.py` no pipeline IFRN, com namespace
`eaj`.

Regras especificas deste programa (TASKS.md):

* Fonte = `.md`; os PDFs `eaj_*.pdf` servem SOMENTE como auditoria. Nenhum
  `pdftotext`/OCR no caminho critico (regra 1).
* Respostas = gabarito TRANSCRITO pela curadoria (decisao 2026-10-09), nunca
  documento oficial: `provenance: TRANSCRIBED_FROM_MD` em tudo, e nunca
  apresentar como "gabarito oficial confirmado por PDF" (regra 2;
  `docs/blockers.md`). Nao ha PDF de gabarito EAJ no repo.
* Namespace EAJ: `institution: EAJ` em cada questao (colisao 2022/2025 com o
  IFRN — ano sozinho nunca identifica a edicao) (regra 3).
* Estrutura de cada edicao vale SO para ela: 2021 = 50Q; 2022/2025 = 40Q.
  Totais esperados como lista fechada (regra 4).
* Sem `explanation` no banco (regra 5, decisao de produto 2026-10-04): este
  script nunca escreve explicacao; `--check` falha se encontrar o campo.
* Status por questao (lista fechada; 130 = 127 + 2 + 1):
  `CONFIRMED_TRANSCRIBED` (127) / `NEEDS_VISUAL_CHECK` (Q22/Q39-2025,
  trava da D.3) / `ANNULLED_TRANSCRIBED` (Q23-2025, motivo transcrito).
  As 129 nao-anuladas sao as "129 transcritas" da TASK B.2.
* Divergencia inline x tabela = erro (exit 2), nunca resolucao silenciosa:
  em modo de geracao nenhum arquivo e escrito quando ha divergencia; em
  `--check`, divergencia tambem sai com codigo 2.
* Achados NAO CONFIRMADOS preservados sem "correcao" por inferencia:
  EAJ-2022 com 40/40 respostas A transcritas (aviso, nunca falha; ver
  `docs/blockers.md`).

Somente stdlib. Nunca escreve nos `.md` nem em `data/extracted/eaj/`.
Idempotente: reexecucao sobrescreve os JSONs com o mesmo conteudo (a menos
do carimbo `generated_at_utc`).

Uso:
    python3 scripts/analysis/link_eaj_keys.py [--check]

`--check` apenas valida os JSONs existentes em `data/linked/eaj/` contra
`data/extracted/eaj/` + tabela fresca do `.md` (130/130 vinculadas,
0 divergencias, Q23-2025 `annulled: true` com motivo, Q22/Q39-2025 como
`NEEDS_VISUAL_CHECK` com motivo, fonte `questoes.md` + SHA, proveniencia
constante) e sai com codigo != 0 em caso de erro (2 = divergencia
inline x tabela).
"""

from __future__ import annotations

import hashlib
import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

LINKER_VERSION = "1.0.0"
LINKER = "scripts/analysis/link_eaj_keys.py"
REPO_ROOT = Path(__file__).resolve().parents[2]
EAJ_MD_DIR = REPO_ROOT / "data" / "provas" / "EAJ"
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted" / "eaj"
OUT_DIR = REPO_ROOT / "data" / "linked" / "eaj"

INSTITUTION = "EAJ"
PROVENANCE = "TRANSCRIBED_FROM_MD"
GABARITO_KIND = "TRANSCRIBED_TABLE_IN_MD"

EDITIONS = ("2021", "2022", "2025")
EXPECTED_COUNTS = {"2021": 50, "2022": 40, "2025": 40}

TABLE_HEAD = "## Gabarito compilado (transcrição — não é documento oficial)"
TABLE_ROW = re.compile(r"^\|\s*\*\*(\d{1,2})\*\*\s*\|\s*([A-D]|Nula|NULA)\s*\|", flags=re.M)

ALLOWED_STATUSES = (
    "CONFIRMED_TRANSCRIBED",
    "NEEDS_VISUAL_CHECK",
    "ANNULLED_TRANSCRIBED",
)

# Listas fechadas confirmadas da B.1 (espelho de `extract_eaj.py`; a B.2 as
# reconfere contra os flags do JSON extraido — nunca inferir outras).
NEEDS_VISUAL_CHECK: dict[tuple[str, int], str] = {
    ("2025", 22): (
        "nota de OCR agrupado transcrita na alternativa B "
        "(`*(nota: OCR agrupou alternativas)*`); conferir no PDF renderizado "
        "antes de importar (trava D.3)."
    ),
    ("2025", 39): (
        "razão `[1/2]` interpolada com incerteza + nota de mancha gráfica "
        "transcrita no enunciado; conferir no PDF renderizado antes de "
        "importar (trava D.3)."
    ),
}
ANNULLED_TRANSCRIBED: dict[tuple[str, int], str] = {
    ("2025", 23): (
        "NULA declarada no `.md`: nenhuma alternativa reflete a ordem "
        "correta Caatinga > Cerrado > Mata Atlântica (análise transcrita; "
        "não é laudo oficial)."
    ),
}


class RuntimeFailure(RuntimeError):
    pass


class LinkDivergence(RuntimeFailure):
    pass


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def parse_compiled_table(ano: str) -> dict[int, str]:
    """Le a tabela compilada transcrita ao final do `.md` (parse fresco)."""
    path = EAJ_MD_DIR / ano / "questoes.md"
    if not path.exists():
        raise RuntimeFailure(f"EAJ-{ano}: arquivo ausente: {path}")
    text = path.read_text(encoding="utf-8")
    if TABLE_HEAD not in text:
        raise RuntimeFailure(f"EAJ-{ano}: tabela compilada ausente (schema A.3)")
    table_text = text.split(TABLE_HEAD, 1)[1]
    table: dict[int, str] = {}
    for m in TABLE_ROW.finditer(table_text):
        n = int(m.group(1))
        g = m.group(2).strip().upper()
        if n in table:
            raise RuntimeFailure(f"EAJ-{ano}: questão {n:02d} duplicada na tabela compilada")
        table[n] = g
    return table


def load_extracted(ano: str) -> dict:
    """Le o JSON da B.1 (resposta-inline, SHA-pinned ao `.md`)."""
    jf = EXTRACTED_DIR / f"{ano}.json"
    if not jf.exists():
        raise RuntimeFailure(f"EAJ-{ano}: {jf} ausente (executar TASK B.1 primeiro)")
    return json.loads(jf.read_text(encoding="utf-8"))


def link_edition(ano: str) -> dict:
    """Consolida inline x tabela de uma edicao. Nunca resolve divergencia."""
    md_path = EAJ_MD_DIR / ano / "questoes.md"
    extracted = load_extracted(ano)
    ext_questions = {int(q["number"]): q for q in extracted["questions"]}

    # O inline da B.1 so vale se ainda corresponde ao `.md` atual.
    md_sha = sha256(md_path)
    if extracted.get("metadata", {}).get("source_sha256") != md_sha:
        raise RuntimeFailure(
            f"EAJ-{ano}: SHA do `.md` divergiu do registrado em "
            f"data/extracted/eaj/{ano}.json (reexecutar a TASK B.1; "
            f"nunca editar o JSON à mão)"
        )
    if extracted.get("metadata", {}).get("institution") != INSTITUTION:
        raise RuntimeFailure(f"EAJ-{ano}: extracted sem namespace EAJ")

    table = parse_compiled_table(ano)
    total = EXPECTED_COUNTS[ano]
    if sorted(table) != list(range(1, total + 1)):
        missing = [n for n in range(1, total + 1) if n not in table]
        raise RuntimeFailure(f"EAJ-{ano}: tabela compilada incompleta (faltantes={missing})")

    warnings: list[str] = []
    divergences: list[dict] = []
    linked: list[dict] = []
    for n in range(1, total + 1):
        q = ext_questions.get(n)
        tag = f"EAJ-{ano} Q{n:02d}"
        if q is None:
            raise RuntimeFailure(f"{tag}: ausente na extração B.1 (sem inline vinculado)")
        inline = str(q.get("answer", "")).strip().upper()
        tabulated = table[n]
        if inline != tabulated:
            divergences.append({"number": n, "inline": inline, "table": tabulated})
            continue  # nunca consolidar valor divergente; erro ao final

        key = (ano, n)
        want_nvc = key in NEEDS_VISUAL_CHECK
        want_ann = key in ANNULLED_TRANSCRIBED
        # A B.2 reconfere as listas fechadas contra os flags da B.1.
        if bool(q.get("needs_visual_check")) != want_nvc:
            raise RuntimeFailure(
                f"{tag}: needs_visual_check da B.1={q.get('needs_visual_check')!r} "
                f"fora da lista fechada {sorted(NEEDS_VISUAL_CHECK)}"
            )
        if bool(q.get("annulled")) != want_ann:
            raise RuntimeFailure(
                f"{tag}: annulled da B.1={q.get('annulled')!r} "
                f"fora da lista fechada {sorted(ANNULLED_TRANSCRIBED)}"
            )
        if want_ann:
            if inline != "NULA":
                raise RuntimeFailure(f"{tag}: anulação transcrita esperava NULA, achou {inline}")
            status = "ANNULLED_TRANSCRIBED"
            validation = (
                f"ANNULLED_TRANSCRIBED: {ANNULLED_TRANSCRIBED[key]} "
                f"(no banco a D.3 registra X por convenção; "
                f"alternativas presentes sem efeito de pontuação)"
            )
        elif want_nvc:
            if inline not in ("A", "B", "C", "D"):
                raise RuntimeFailure(f"{tag}: resposta inválida: {inline!r}")
            status = "NEEDS_VISUAL_CHECK"
            validation = (
                f"NEEDS_VISUAL_CHECK: {NEEDS_VISUAL_CHECK[key]} "
                f"Inline == tabela, mas conferir no PDF antes de importar (trava D.3)."
            )
        else:
            if inline not in ("A", "B", "C", "D"):
                raise RuntimeFailure(f"{tag}: resposta inválida: {inline!r}")
            status = "CONFIRMED_TRANSCRIBED"
            validation = "ok — inline == tabela (transcrição, NÃO oficial)"

        linked.append(
            {
                "institution": INSTITUTION,
                "edition": ano,
                "number": n,
                "discipline": q.get("discipline"),
                "discipline_source": q.get("discipline_source"),
                "source_document": f"data/provas/EAJ/{ano}/questoes.md",
                "extracted_source": f"data/extracted/eaj/{ano}.json",
                "answer_inline": inline,
                "answer_table": tabulated,
                "answer_key": inline,
                "answer_provenance": PROVENANCE,
                "provenance": PROVENANCE,
                "annulled": want_ann,
                "annulled_reason_transcribed": ANNULLED_TRANSCRIBED.get(key),
                "needs_visual_check": want_nvc,
                "needs_visual_check_reason": NEEDS_VISUAL_CHECK.get(key),
                "status": status,
                "validation": validation,
            }
        )

    # Distribuicao degenerada (ex.: 40xA em 2022) = aviso, nunca falha: a
    # transcricao e preservada e a conferencia e contra o gabarito oficial
    # (Fase G; ver docs/blockers.md).
    from collections import Counter

    dist = Counter(q["answer_key"] for q in linked) if linked else Counter()
    if linked and len(dist) == 1:
        only = next(iter(dist))
        warnings.append(
            f"EAJ-{ano}: distribuição degenerada — {len(linked)}/{total} "
            f"respostas = {only} (transcrição preservada; NÃO CONFIRMADO até o "
            f"gabarito oficial; ver docs/blockers.md)"
        )

    payload = {
        "metadata": {
            "institution": INSTITUTION,
            "edition": ano,
            "linker": LINKER,
            "linker_version": LINKER_VERSION,
            "generated_at_utc": datetime.now(timezone.utc).isoformat(),
            "extracted_source": f"data/extracted/eaj/{ano}.json",
            "extracted_sha256": sha256(EXTRACTED_DIR / f"{ano}.json"),
            "source_document": f"data/provas/EAJ/{ano}/questoes.md",
            "source_sha256": md_sha,
            "provenance": PROVENANCE,
            "gabarito_kind": GABARITO_KIND,
            "discipline_ranges": extracted.get("metadata", {}).get("discipline_ranges", []),
            "note": (
                "Vinculação TASK B.2. Resposta-inline (data/extracted/eaj, "
                "TASK B.1) consolidada com a tabela compilada transcrita no "
                "`.md` (schema A.3). Ambas as fontes são TRANSCRIÇÃO da "
                "curadoria (TRANSCRIBED_FROM_MD), nunca documento oficial — "
                "não há PDF de gabarito EAJ no repo (ver docs/blockers.md). "
                "Sem classificação pedagógica; sem explanation."
            ),
        },
        "questions": linked,
        "divergences_inline_vs_table": divergences,
        "warnings": warnings,
        "issues": [],
    }
    return payload


def check_payload(ano: str, data: dict) -> tuple[list[str], list[str], list[str]]:
    """Valida um JSON existente. Retorna (erros, divergencias, avisos)."""
    errors: list[str] = []
    divergences: list[str] = []
    warnings: list[str] = []
    md_path = EAJ_MD_DIR / ano / "questoes.md"
    total = EXPECTED_COUNTS[ano]

    md = data.get("metadata", {})
    if md.get("institution") != INSTITUTION:
        errors.append(f"EAJ-{ano}: institution={md.get('institution')!r} (esperado 'EAJ')")
    if md.get("edition") != ano:
        errors.append(f"EAJ-{ano}: edition={md.get('edition')!r}")
    if md.get("source_document") != f"data/provas/EAJ/{ano}/questoes.md":
        errors.append(f"EAJ-{ano}: source_document inesperado: {md.get('source_document')!r}")
    if md_path.exists():
        if md.get("source_sha256") != sha256(md_path):
            errors.append(
                f"EAJ-{ano}: SHA do `.md` divergiu do registrado "
                f"(reexecutar o linker; nunca editar o JSON à mão)"
            )
    else:
        errors.append(f"EAJ-{ano}: `.md` fonte ausente: {md_path}")
    if md.get("provenance") != PROVENANCE:
        errors.append(f"EAJ-{ano}: provenance={md.get('provenance')!r}")
    if md.get("gabarito_kind") != GABARITO_KIND:
        errors.append(f"EAJ-{ano}: gabarito_kind={md.get('gabarito_kind')!r}")
    if md.get("extracted_source") != f"data/extracted/eaj/{ano}.json":
        errors.append(f"EAJ-{ano}: extracted_source inesperado: {md.get('extracted_source')!r}")
    ext_path = EXTRACTED_DIR / f"{ano}.json"
    if ext_path.exists():
        if md.get("extracted_sha256") != sha256(ext_path):
            errors.append(
                f"EAJ-{ano}: SHA do extracted divergiu do registrado "
                f"(reexecutar o linker após a B.1)"
            )
    else:
        errors.append(f"EAJ-{ano}: extracted ausente: {ext_path}")

    qs = data.get("questions", [])
    if len(qs) != total:
        errors.append(f"EAJ-{ano}: {len(qs)} vinculadas, esperado {total}")
    if sorted(q.get("number") for q in qs) != list(range(1, total + 1)):
        errors.append(f"EAJ-{ano}: numeração fora de 1..{total} únicos")

    # Tabela fresca do `.md`: a verdade contra a qual o vinculado e conferido.
    try:
        table = parse_compiled_table(ano)
    except RuntimeFailure as e:
        errors.append(str(e))
        table = {}

    for q in qs:
        n = q.get("number")
        tag = f"EAJ-{ano} Q{n:02d}" if isinstance(n, int) else f"EAJ-{ano} Q{n!r}"
        if q.get("institution") != INSTITUTION:
            errors.append(f"{tag}: sem namespace EAJ")
        if q.get("source_document") != f"data/provas/EAJ/{ano}/questoes.md":
            errors.append(f"{tag}: source_document inesperado")
        if q.get("provenance") != PROVENANCE or q.get("answer_provenance") != PROVENANCE:
            errors.append(f"{tag}: proveniência diferente de {PROVENANCE}")
        if "explanation" in q:
            errors.append(f"{tag}: campo `explanation` proibido no banco")
        for bad in ("analise", "análise", "analysis_text"):
            if bad in q:
                errors.append(f"{tag}: texto de análise no JSON (`{bad}`) — regra 5")
        status = q.get("status")
        if status not in ALLOWED_STATUSES:
            errors.append(f"{tag}: status inválido: {status!r}")
            continue
        key = (ano, n)
        if key == ("2025", 23):
            if status != "ANNULLED_TRANSCRIBED":
                errors.append(f"{tag}: esperado ANNULLED_TRANSCRIBED, achou {status}")
            if not q.get("annulled"):
                errors.append(f"{tag}: esperado annulled=true (transcrito)")
            if not q.get("annulled_reason_transcribed"):
                errors.append(f"{tag}: sem motivo transcrito da anulação")
            if q.get("answer_key") != "NULA":
                errors.append(f"{tag}: esperado answer_key=NULA, achou {q.get('answer_key')!r}")
        elif key in NEEDS_VISUAL_CHECK:
            if status != "NEEDS_VISUAL_CHECK":
                errors.append(f"{tag}: esperado NEEDS_VISUAL_CHECK, achou {status}")
            if not q.get("needs_visual_check"):
                errors.append(f"{tag}: esperado needs_visual_check=true")
            if not q.get("needs_visual_check_reason"):
                errors.append(f"{tag}: sem motivo do needs_visual_check")
            if q.get("answer_key") not in ("A", "B", "C", "D"):
                errors.append(f"{tag}: answer_key inválido: {q.get('answer_key')!r}")
            if q.get("annulled"):
                errors.append(f"{tag}: annulled=true fora de 2025-Q23")
        else:
            if status != "CONFIRMED_TRANSCRIBED":
                errors.append(f"{tag}: esperado CONFIRMED_TRANSCRIBED, achou {status}")
            if q.get("answer_key") not in ("A", "B", "C", "D"):
                errors.append(f"{tag}: answer_key inválido: {q.get('answer_key')!r}")
            if q.get("annulled"):
                errors.append(f"{tag}: annulled=true fora de 2025-Q23")
            if q.get("needs_visual_check"):
                errors.append(f"{tag}: needs_visual_check=true fora de Q22/Q39-2025")
        # Consistencia inline x tabela (fresca do `.md`).
        if isinstance(n, int) and n in table:
            exp = table[n]
            for field in ("answer_inline", "answer_table", "answer_key"):
                if str(q.get(field, "")).strip().upper() != exp:
                    divergences.append(
                        f"{tag}: divergência inline×tabela — "
                        f"{field}={q.get(field)!r} vs tabela={exp!r}"
                    )

    if data.get("divergences_inline_vs_table"):
        divergences.append(
            f"EAJ-{ano}: divergences_inline_vs_table não-vazio: "
            f"{data['divergences_inline_vs_table']}"
        )

    from collections import Counter

    dist = Counter(q.get("answer_key") for q in qs)
    if len(qs) == total and len(dist) == 1 and qs:
        warnings.append(
            f"EAJ-{ano}: distribuição degenerada — {total}/{total} = "
            f"{next(iter(dist))} (preservada; ver docs/blockers.md)"
        )
    return errors, divergences, warnings


def run_check() -> int:
    all_errors: list[str] = []
    all_diverg: list[str] = []
    all_warnings: list[str] = []
    totals = 0
    n_conf = n_nvc = n_ann = 0
    for ano in EDITIONS:
        jf = OUT_DIR / f"{ano}.json"
        if not jf.exists():
            all_errors.append(f"EAJ-{ano}: AUSENTE {jf}")
            continue
        data = json.loads(jf.read_text(encoding="utf-8"))
        errors, diverg, warnings = check_payload(ano, data)
        all_errors.extend(errors)
        all_diverg.extend(diverg)
        all_warnings.extend(warnings)
        qs = data.get("questions", [])
        totals += len(qs)
        c = sum(1 for q in qs if q.get("status") == "CONFIRMED_TRANSCRIBED")
        v = sum(1 for q in qs if q.get("status") == "NEEDS_VISUAL_CHECK")
        a = sum(1 for q in qs if q.get("status") == "ANNULLED_TRANSCRIBED")
        n_conf += c
        n_nvc += v
        n_ann += a
        nvc = sorted(q["number"] for q in qs if q.get("status") == "NEEDS_VISUAL_CHECK")
        ann = sorted(q["number"] for q in qs if q.get("status") == "ANNULLED_TRANSCRIBED")
        flag = "OK" if not errors and not diverg else "FALHA"
        print(f"EAJ-{ano}: {flag} (n={len(qs)}/{EXPECTED_COUNTS[ano]}, "
              f"CONFIRMED={c}, NEEDS_VISUAL_CHECK={nvc or '-'}, "
              f"ANNULLED={ann or '-'}, diverg={len(diverg)})")
    print(f"Total: {totals}/130 vinculadas "
          f"({n_conf} CONFIRMED_TRANSCRIBED + {n_nvc} NEEDS_VISUAL_CHECK "
          f"+ {n_ann} ANNULLED_TRANSCRIBED), {len(all_diverg)} divergências")
    for w in all_warnings:
        print(f"WARN: {w}")
    for e in all_errors:
        print(f"ERROR: {e}")
    for d in all_diverg:
        print(f"DIVERGENCIA: {d}")
    if totals != 130:
        all_errors.append(f"total {totals} != 130")
    if all_diverg:
        print("FALHA DE VINCULAÇÃO (exit 2): divergência inline × tabela — "
              "nunca resolver silenciosamente.")
        return 2
    if all_errors:
        print(f"FALHA: {len(all_errors)} erro(s).")
        return 1
    print("OK: 130/130 vinculadas (129 transcritas não-anuladas: 127 "
          "CONFIRMED_TRANSCRIBED + 2 NEEDS_VISUAL_CHECK; 1 ANNULLED_TRANSCRIBED "
          "Q23-2025 com motivo); 0 divergências inline×tabela; proveniência "
          "TRANSCRIBED_FROM_MD; fonte questoes.md + SHA.")
    return 0


def main() -> int:
    if "--check" in sys.argv:
        return run_check()

    # Construir tudo em memoria primeiro: com divergencia em QUALQUER edicao,
    # nada e escrito (exit 2, nunca resolucao silenciosa).
    try:
        payloads = {ano: link_edition(ano) for ano in EDITIONS}
    except LinkDivergence as e:
        print(f"DIVERGENCIA: {e}", file=sys.stderr)
        return 2
    pending_diverg: list[str] = []
    for ano, payload in payloads.items():
        for d in payload["divergences_inline_vs_table"]:
            pending_diverg.append(
                f"EAJ-{ano} Q{d['number']:02d}: inline={d['inline']!r} × "
                f"tabela={d['table']!r}"
            )
    if pending_diverg:
        for d in pending_diverg:
            print(f"DIVERGENCIA: {d}", file=sys.stderr)
        print("FALHA DE VINCULAÇÃO (exit 2): divergência inline × tabela — "
              "nada foi escrito; nunca resolver silenciosamente.", file=sys.stderr)
        return 2

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    manifest_entries: list[dict] = []
    global_warnings: list[str] = []
    for ano in EDITIONS:
        payload = payloads[ano]
        out = OUT_DIR / f"{ano}.json"
        out.write_text(
            json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
        qs = payload["questions"]
        c = sum(1 for q in qs if q["status"] == "CONFIRMED_TRANSCRIBED")
        v = sorted(q["number"] for q in qs if q["status"] == "NEEDS_VISUAL_CHECK")
        a = sorted(q["number"] for q in qs if q["status"] == "ANNULLED_TRANSCRIBED")
        print(f"EAJ-{ano}: {len(qs)} vinculadas "
              f"({c} CONFIRMED_TRANSCRIBED, NEEDS_VISUAL_CHECK={v or '-'}, "
              f"ANNULLED={a or '-'}, divergências=0, avisos={len(payload['warnings'])})")
        for w in payload["warnings"]:
            print(f"  AVISO EAJ-{ano}: {w}")
            global_warnings.append(f"EAJ-{ano}: {w}")
        manifest_entries.append(
            {
                "institution": INSTITUTION,
                "edition": ano,
                "source_document": payload["metadata"]["source_document"],
                "source_sha256": payload["metadata"]["source_sha256"],
                "extracted_source": payload["metadata"]["extracted_source"],
                "extracted_sha256": payload["metadata"]["extracted_sha256"],
                "provenance": PROVENANCE,
                "gabarito_kind": GABARITO_KIND,
                "n_linked": len(qs),
                "n_confirmed_transcribed": c,
                "n_needs_visual_check": len(v),
                "needs_visual_check_questions": v,
                "n_annulled_transcribed": len(a),
                "annulled_questions": a,
                "n_divergences": 0,
                "n_warnings": len(payload["warnings"]),
            }
        )
    manifest = {
        "institution": INSTITUTION,
        "linker": LINKER,
        "linker_version": LINKER_VERSION,
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "provenance": PROVENANCE,
        "editions": manifest_entries,
        "totals": {
            "editions": len(manifest_entries),
            "linked_questions": sum(e["n_linked"] for e in manifest_entries),
            "confirmed_transcribed": sum(e["n_confirmed_transcribed"] for e in manifest_entries),
            "needs_visual_check": sum(e["n_needs_visual_check"] for e in manifest_entries),
            "annulled_transcribed": sum(e["n_annulled_transcribed"] for e in manifest_entries),
            "divergences": 0,
        },
        "warnings": global_warnings,
        "note": (
            "Namespace EAJ (EAJ-2022 ≠ IFRN-2022, EAJ-2025 ≠ IFRN-2025). "
            "Respostas TRANSCRITAS do `.md` (inline × tabela compilada), "
            "nunca documento oficial."
        ),
    }
    (OUT_DIR / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Manifest: {OUT_DIR / 'manifest.json'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
