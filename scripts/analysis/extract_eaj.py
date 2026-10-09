#!/usr/bin/env python3
"""Parser `.md` EAJ -> `data/extracted/eaj/*.json` — TASK B.1.

Le os 3 `questoes.md` normalizados em `data/provas/EAJ/<ano>/` (fonte de
extracao; TASKS.md regra 1) e gera evidencia maquina-legivel em
`data/extracted/eaj/{2021,2022,2025}.json` + `data/extracted/eaj/manifest.json`,
no mesmo contrato dos IFRN (`extract_questions.py`): enunciado, alternativas
A-D, disciplina, documento de origem + SHA, bloco bruto para auditoria.

Regras especificas deste programa (TASKS.md):

* Fonte = `.md`; os PDFs `eaj_*.pdf` servem SOMENTE como auditoria. Nenhum
  `pdftotext`/OCR no caminho critico.
* Respostas = gabarito TRANSCRITO pela curadoria (decisao 2026-10-09), nunca
  documento oficial: `answer_provenance: TRANSCRIBED_FROM_MD` em tudo.
* Namespace EAJ: `institution: EAJ` em cada questao (colisao 2022/2025 com o
  IFRN — ano sozinho nunca identifica a edicao).
* Estrutura de cada edicao vale SO para ela (AGENTS.md §10): 2021 = 50Q em
  4 areas (LP 01-15, MAT 16-30, CN 31-42, CH 43-50); 2022/2025 = 40Q em
  2 areas (LP 01-20, MAT 21-40). Disciplina pela faixa de numero DAQUELA
  edicao (`discipline_source: RANGE_PER_EDITION`), com checagem cruzada
  contra o cabecalho de secao onde a questao foi encontrada.
* `Análise:` dos `.md` NAO entra no banco (regra 5, decisao de produto
  2026-10-04): o extrator registra apenas `has_analysis` (auditoria da regra),
  nunca o texto da analise.
* Paginas: os `.md` nao trazem pagina por questao, logo `page_start/end`
  = NULL + `page_status: DESCONHECIDO` em tudo (nunca inventar pagina).
* Achados NAO CONFIRMADOS preservados sem "correcao" por inferencia:
  EAJ-2022 com 40/40 respostas A transcritas (aviso, nunca falha);
  2025 Q22/Q39 com `needs_visual_check: true` + motivo (trava da D.3);
  2025 Q23 com `answer: NULA` + motivo transcrito.

Somente stdlib. Nunca escreve nos `.md`. Idempotente: reexecucao
sobrescreve os JSONs com o mesmo conteudo (a menos do carimbo
`generated_at_utc`).

Uso:
    python3 scripts/analysis/extract_eaj.py [--check]

`--check` apenas valida os JSONs existentes (130 questoes, 50/40/40,
4 alternativas nao vazias em todas, resposta em A-D/NULA, disciplina pela
faixa da edicao, paginas NULL+DESCONHECIDO, proveniencia constante,
Q23-2025 NULA, Q22/Q39-2025 com needs_visual_check + motivo, SHA do `.md`
inalterado) e sai com codigo != 0 em caso de erro.
"""

from __future__ import annotations

import hashlib
import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

EXTRACTOR_VERSION = "1.0.0"
EXTRACTOR = "scripts/analysis/extract_eaj.py"
REPO_ROOT = Path(__file__).resolve().parents[2]
EAJ_MD_DIR = REPO_ROOT / "data" / "provas" / "EAJ"
OUT_DIR = REPO_ROOT / "data" / "extracted" / "eaj"

INSTITUTION = "EAJ"
ANSWER_PROVENANCE = "TRANSCRIBED_FROM_MD"

# Faixa de numero -> disciplina, POR EDICAO (TASKS.md regra 4; a estrutura de
# cada edicao vale so para ela). Fonte: capas dos cadernos transcritas em
# `docs/provas-inventario-eaj.md` §1.
# (cabecalho de secao no .md, codigo da disciplina, primeiro N, ultimo N)
DISCIPLINE_RANGES: dict[str, list[tuple[str, str, int, int]]] = {
    "2021": [
        ("Língua Portuguesa", "LINGUA_PORTUGUESA", 1, 15),
        ("Matemática", "MATEMATICA", 16, 30),
        ("Ciências da Natureza", "CIENCIAS_NATUREZA", 31, 42),
        ("Ciências Humanas", "CIENCIAS_HUMANAS", 43, 50),
    ],
    "2022": [
        ("Língua Portuguesa", "LINGUA_PORTUGUESA", 1, 20),
        ("Matemática", "MATEMATICA", 21, 40),
    ],
    "2025": [
        ("Língua Portuguesa", "LINGUA_PORTUGUESA", 1, 20),
        ("Matemática", "MATEMATICA", 21, 40),
    ],
}

TABLE_HEAD = "## Gabarito compilado (transcrição — não é documento oficial)"
SECTION_RE = re.compile(r"^## (.+?)\s*$", flags=re.M)
Q_START_RE = re.compile(r"^\*\*(\d{1,2})\.\*\*[ \t]*(.*)$")
ALT_RE = re.compile(r"^([A-D])\)[ \t]*(.*)$")
ANALYSIS_RE = re.compile(r"^\*\*Análise:\*\*")
ANSWER_RE = re.compile(r"^\*\*Gabarito:[ \t]*([A-D]|NULA)\*\*")

# (edicao, numero) -> motivo do flag (lista fechada da TASK B.1; nunca
# inferir outros). A trava de importacao e da D.3.
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
# Palavras-chave da nota transcrita cujo sumico indica deriva do `.md`.
VISUAL_NOTE_KEYWORDS: dict[tuple[str, int], list[str]] = {
    ("2025", 22): ["OCR"],
    ("2025", 39): ["mancha", "interpolada"],
}

# (edicao, numero) -> motivo transcrito da anulacao (lista fechada; B.2
# confirma como ANNULLED_TRANSCRIBED).
ANNULLED_TRANSCRIBED: dict[tuple[str, int], str] = {
    ("2025", 23): (
        "NULA declarada no `.md`: nenhuma alternativa reflete a ordem "
        "correta Caatinga > Cerrado > Mata Atlântica (análise transcrita; "
        "não é laudo oficial)."
    ),
}


class RuntimeFailure(RuntimeError):
    pass


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def range_for(ano: str, n: int) -> tuple[str, str]:
    """Retorna (secao, codigo) da faixa da EDICAO para o numero n."""
    for section, code, first, last in DISCIPLINE_RANGES[ano]:
        if first <= n <= last:
            return section, code
    raise RuntimeFailure(f"EAJ-{ano} Q{n:02d}: fora das faixas da edicao")


def norm_statement(lines: list[str]) -> str:
    """Junta linhas do enunciado como o contrato IFRN (espaco simples)."""
    return re.sub(r"\s+", " ", " ".join(lines)).strip()


def parse_section(
    ano: str, section: str, text: str, warnings: list[str]
) -> list[dict]:
    """Extrai as questoes de uma secao `## <area>` (maquina de estados)."""
    lines = text.splitlines()
    # Indices das linhas `**NN.**` dentro da secao.
    starts = [i for i, l in enumerate(lines) if Q_START_RE.match(l)]
    questions: list[dict] = []
    seen: set[int] = set()
    for idx, si in enumerate(starts):
        end = starts[idx + 1] if idx + 1 < len(starts) else len(lines)
        block = lines[si:end]
        m0 = Q_START_RE.match(block[0])
        assert m0 is not None
        n = int(m0.group(1))
        if n in seen:
            raise RuntimeFailure(f"EAJ-{ano}: questão {n:02d} duplicada no inline")
        seen.add(n)

        stmt_lines = [m0.group(2)] if m0.group(2).strip() else []
        options: dict[str, list[str]] = {}
        opt_order: list[str] = []
        current: str | None = None
        in_statement = True
        has_analysis = False
        in_analysis = False
        answer: str | None = None

        for line in block[1:]:
            if line.strip() == "---":
                # Separador estrutural de secao (nunca conteudo de questao;
                # preservado verbatim no raw_block para auditoria).
                continue
            if in_analysis:
                if ANSWER_RE.match(line.strip()):
                    answer = ANSWER_RE.match(line.strip()).group(1)  # type: ignore[union-attr]
                    in_analysis = False
                continue
            if ANALYSIS_RE.match(line.strip()):
                has_analysis = True
                in_analysis = True
                current = None
                in_statement = False
                continue
            am = ANSWER_RE.match(line.strip())
            if am:
                answer = am.group(1)
                current = None
                in_statement = False
                continue
            om = ALT_RE.match(line)
            if om and not in_analysis:
                current = om.group(1)
                if current not in opt_order:
                    opt_order.append(current)
                options.setdefault(current, []).append(om.group(2))
                in_statement = False
                continue
            if in_statement:
                if line.strip():
                    stmt_lines.append(line.strip())
            elif current is not None:
                if line.strip():
                    options[current].append(line.strip())
            elif line.strip():
                raise RuntimeFailure(
                    f"EAJ-{ano} Q{n:02d}: linha orfa apos o gabarito: {line.strip()[:80]!r}"
                )

        if in_analysis:
            raise RuntimeFailure(f"EAJ-{ano} Q{n:02d}: `**Análise:**` sem `**Gabarito:**`")
        if answer is None:
            raise RuntimeFailure(f"EAJ-{ano} Q{n:02d}: resposta ausente (`**Gabarito: X**`)")
        if opt_order != ["A", "B", "C", "D"]:
            raise RuntimeFailure(
                f"EAJ-{ano} Q{n:02d}: alternativas fora de A-D em ordem ({opt_order})"
            )
        opts = {k: v[0].strip() if len(v) == 1 else " ".join(v).strip()
                for k, v in options.items()}
        empty = [k for k in "ABCD" if not opts.get(k, "").strip()]
        if empty:
            raise RuntimeFailure(f"EAJ-{ano} Q{n:02d}: alternativas vazias: {empty}")
        statement = norm_statement(stmt_lines)
        if not statement:
            raise RuntimeFailure(f"EAJ-{ano} Q{n:02d}: enunciado vazio")

        # Disciplina pela FAIXA da edicao + checagem contra a secao.
        exp_section, code = range_for(ano, n)
        if exp_section != section:
            raise RuntimeFailure(
                f"EAJ-{ano} Q{n:02d}: sob `## {section}` mas na faixa de "
                f"`## {exp_section}` — nunca rotular por inferencia"
            )

        key = (ano, n)
        nvc = key in NEEDS_VISUAL_CHECK
        if nvc:
            kws = VISUAL_NOTE_KEYWORDS.get(key, [])
            raw = "\n".join(block)
            if kws and not any(k in raw for k in kws):
                warnings.append(
                    f"EAJ-{ano} Q{n:02d}: nota transcrita de NEEDS_VISUAL_CHECK "
                    f"nao localizada no bloco (deriva do `.md`?)"
                )
        annulled = key in ANNULLED_TRANSCRIBED
        if answer == "NULA" and not annulled:
            raise RuntimeFailure(
                f"EAJ-{ano} Q{n:02d}: NULA fora da lista fechada "
                f"({sorted(ANNULLED_TRANSCRIBED)}) — nao permitido"
            )
        if annulled and answer != "NULA":
            raise RuntimeFailure(
                f"EAJ-{ano} Q{n:02d}: anulacao transcrita esperava NULA, achou {answer}"
            )

        questions.append(
            {
                "institution": INSTITUTION,
                "edition": ano,
                "number": n,
                "kind": "OBJECTIVE",
                "discipline": code,
                "discipline_source": "RANGE_PER_EDITION",
                "section": section,
                "source_document": f"data/provas/EAJ/{ano}/questoes.md",
                "page_start": None,
                "page_end": None,
                "page_status": "DESCONHECIDO",
                "statement": statement,
                "options": {k: opts[k] for k in "ABCD"},
                "answer": answer,
                "answer_provenance": ANSWER_PROVENANCE,
                "annulled": annulled,
                "annulled_reason_transcribed": ANNULLED_TRANSCRIBED.get(key),
                "needs_visual_check": nvc,
                "needs_visual_check_reason": NEEDS_VISUAL_CHECK.get(key),
                "has_analysis": has_analysis,
                "raw_block": "\n".join(block),
            }
        )
    return questions


def parse_edition(ano: str) -> tuple[list[dict], list[str]]:
    """Le o `.md` normalizado de uma edicao. Retorna (questoes, avisos)."""
    path = EAJ_MD_DIR / ano / "questoes.md"
    if not path.exists():
        raise RuntimeFailure(f"EAJ-{ano}: arquivo ausente: {path}")
    text = path.read_text(encoding="utf-8")
    warnings: list[str] = []

    if TABLE_HEAD not in text:
        raise RuntimeFailure(f"EAJ-{ano}: tabela compilada ausente (schema A.3)")
    body = text.split(TABLE_HEAD)[0]

    # Fatiar o corpo por secoes `## ...`; questoes so dentro das secoes de area.
    sec_matches = list(SECTION_RE.finditer(body))
    if not sec_matches:
        raise RuntimeFailure(f"EAJ-{ano}: nenhuma secao `## ...` no corpo")
    sections: dict[str, str] = {}
    for i, sm in enumerate(sec_matches):
        s_end = sec_matches[i + 1].start() if i + 1 < len(sec_matches) else len(body)
        sections[sm.group(1).strip()] = body[sm.end():s_end]

    expected_sections = [s for s, _, _, _ in DISCIPLINE_RANGES[ano]]
    for s in expected_sections:
        if s not in sections:
            raise RuntimeFailure(f"EAJ-{ano}: secao `## {s}` ausente")
    if "Textos-base" not in sections:
        raise RuntimeFailure(f"EAJ-{ano}: secao `## Textos-base` ausente")

    questions: list[dict] = []
    for section in expected_sections:
        questions.extend(parse_section(ano, section, sections[section], warnings))

    total = sum(last - first + 1 for _, _, first, last in DISCIPLINE_RANGES[ano])
    nums = sorted(q["number"] for q in questions)
    if nums != list(range(1, total + 1)):
        missing = [n for n in range(1, total + 1) if n not in nums]
        raise RuntimeFailure(f"EAJ-{ano}: numeracao inesperada (faltantes={missing})")

    questions.sort(key=lambda q: q["number"])
    return questions, warnings


def build_payload(ano: str) -> dict:
    questions, warnings = parse_edition(ano)
    md_path = EAJ_MD_DIR / ano / "questoes.md"

    # Distribuicao degenerada (ex.: 40xA) = aviso, nunca falha: a transcricao
    # e preservada e a conferencia e contra o gabarito oficial (Fase G).
    from collections import Counter

    dist = Counter(q["answer"] for q in questions)
    if len(dist) == 1:
        only = next(iter(dist))
        warnings.append(
            f"EAJ-{ano}: distribuição degenerada — {len(questions)}/{len(questions)} "
            f"respostas = {only} (transcrição preservada; NÃO CONFIRMADO até o "
            f"gabarito oficial; ver docs/blockers.md)"
        )

    return {
        "metadata": {
            "institution": INSTITUTION,
            "edition": ano,
            "source_document": f"data/provas/EAJ/{ano}/questoes.md",
            "source_sha256": sha256(md_path),
            "source_pages": None,
            "source_page_status": "DESCONHECIDO",
            "extractor": EXTRACTOR,
            "extractor_version": EXTRACTOR_VERSION,
            "generated_at_utc": datetime.now(timezone.utc).isoformat(),
            "discipline_ranges": [
                {"section": s, "discipline": c, "first": f, "last": l}
                for s, c, f, l in DISCIPLINE_RANGES[ano]
            ],
            "discipline_source_note": (
                "Disciplina pela faixa de número DESTA edição "
                "(RANGE_PER_EDITION), com checagem cruzada contra o cabeçalho "
                "de seção; ver docs/provas-inventario-eaj.md §1."
            ),
            "note": (
                "Evidência intermediária (TASK B.1). Enunciados/alternativas "
                "transcritos do `.md` (fonte); respostas = gabarito TRANSCRITO "
                "pela curadoria (TRANSCRIBED_FROM_MD), nunca documento oficial. "
                "`Análise:` dos `.md` NÃO alimenta o banco (só `has_analysis`). "
                "Páginas DESCONHECIDAS (o `.md` não traz página por questão). "
                "PDFs servem SOMENTE como auditoria."
            ),
        },
        "questions": questions,
        "discursive": {
            "present": False,
            "note": (
                "Ausente nesta edição conforme o observado no caderno "
                "(ver docs/provas-inventario-eaj.md §2/§3); vale só para "
                "esta edição."
            ),
        },
        "warnings": warnings,
    }


def check_payload(ano: str, data: dict) -> tuple[list[str], list[str]]:
    """Valida um JSON existente. Retorna (erros, avisos)."""
    errors: list[str] = []
    warnings: list[str] = []
    md_path = EAJ_MD_DIR / ano / "questoes.md"

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
                "(reexecutar a extração; nunca editar o JSON à mão)"
            )
    else:
        errors.append(f"EAJ-{ano}: `.md` fonte ausente: {md_path}")

    qs = data.get("questions", [])
    total = sum(last - first + 1 for _, _, first, last in DISCIPLINE_RANGES[ano])
    if len(qs) != total:
        errors.append(f"EAJ-{ano}: {len(qs)} questões, esperado {total}")
    if sorted(q.get("number") for q in qs) != list(range(1, total + 1)):
        errors.append(f"EAJ-{ano}: numeração fora de 1..{total} únicos")

    for q in qs:
        n = q.get("number")
        tag = f"EAJ-{ano} Q{n:02d}" if isinstance(n, int) else f"EAJ-{ano} Q{n!r}"
        exp_section, exp_code = (range_for(ano, n) if isinstance(n, int)
                                 and 1 <= n <= total else (None, None))
        if q.get("institution") != INSTITUTION:
            errors.append(f"{tag}: sem namespace EAJ")
        if q.get("discipline") != exp_code:
            errors.append(f"{tag}: discipline={q.get('discipline')!r} (faixa: {exp_code})")
        if q.get("discipline_source") != "RANGE_PER_EDITION":
            errors.append(f"{tag}: discipline_source={q.get('discipline_source')!r}")
        if q.get("section") != exp_section:
            errors.append(f"{tag}: section={q.get('section')!r} (faixa: {exp_section})")
        if not str(q.get("statement", "")).strip():
            errors.append(f"{tag}: enunciado vazio")
        opts = q.get("options", {})
        if [k for k in opts] != ["A", "B", "C", "D"] or any(
            not str(opts[k]).strip() for k in "ABCD"
        ):
            errors.append(f"{tag}: alternativas A-D incompletas ou vazias")
        ans = q.get("answer")
        if (ano, n) == ("2025", 23):
            if ans != "NULA":
                errors.append(f"{tag}: esperado answer=NULA, achou {ans!r}")
            if not q.get("annulled"):
                errors.append(f"{tag}: esperado annulled=true (transcrito)")
            if not q.get("annulled_reason_transcribed"):
                errors.append(f"{tag}: sem motivo transcrito da anulação")
        elif ans not in ("A", "B", "C", "D"):
            errors.append(f"{tag}: answer inválido: {ans!r}")
        elif q.get("annulled"):
            errors.append(f"{tag}: annulled=true fora de 2025-Q23")
        if q.get("answer_provenance") != ANSWER_PROVENANCE:
            errors.append(f"{tag}: answer_provenance={q.get('answer_provenance')!r}")
        if q.get("page_start") is not None or q.get("page_end") is not None:
            errors.append(f"{tag}: página inventada (page_start/end devem ser NULL)")
        if q.get("page_status") != "DESCONHECIDO":
            errors.append(f"{tag}: page_status={q.get('page_status')!r}")
        if "explanation" in q:
            errors.append(f"{tag}: campo `explanation` proibido no banco")
        for bad in ("analise", "análise", "analysis_text"):
            if bad in q:
                errors.append(f"{tag}: texto de análise no JSON (`{bad}`) — regra 5")
        want_nvc = (ano, n) in NEEDS_VISUAL_CHECK
        if bool(q.get("needs_visual_check")) != want_nvc:
            errors.append(f"{tag}: needs_visual_check={q.get('needs_visual_check')!r}")
        if want_nvc and not q.get("needs_visual_check_reason"):
            errors.append(f"{tag}: sem motivo do needs_visual_check")
        if not str(q.get("raw_block", "")).strip():
            errors.append(f"{tag}: raw_block vazio (auditoria)")
        if not isinstance(q.get("has_analysis"), bool):
            errors.append(f"{tag}: has_analysis não-booleano")

    from collections import Counter

    dist = Counter(q.get("answer") for q in qs)
    if len(dist) == 1 and qs:
        warnings.append(
            f"EAJ-{ano}: distribuição degenerada — {len(qs)}/{len(qs)} = "
            f"{next(iter(dist))} (preservada; ver docs/blockers.md)"
        )
    return errors, warnings


def main() -> int:
    if "--check" in sys.argv:
        all_errors: list[str] = []
        all_warnings: list[str] = []
        totals = 0
        for ano in ("2021", "2022", "2025"):
            jf = OUT_DIR / f"{ano}.json"
            if not jf.exists():
                all_errors.append(f"EAJ-{ano}: AUSENTE {jf}")
                continue
            data = json.loads(jf.read_text(encoding="utf-8"))
            errors, warnings = check_payload(ano, data)
            all_errors.extend(errors)
            all_warnings.extend(warnings)
            totals += len(data.get("questions", []))
            nvc = sorted(
                q["number"] for q in data.get("questions", []) if q.get("needs_visual_check")
            )
            nula = sorted(
                q["number"] for q in data.get("questions", []) if q.get("answer") == "NULA"
            )
            print(f"EAJ-{ano}: {len(data.get('questions', []))}Q "
                  f"(NULA={nula or '-'}, needsVisualCheck={nvc or '-'}) "
                  f"erros={len(errors)}")
        print(f"Total: {totals}/130")
        for w in all_warnings:
            print(f"WARN: {w}")
        for e in all_errors:
            print(f"ERROR: {e}")
        if totals != 130:
            all_errors.append(f"total {totals} != 130")
        if all_errors:
            print(f"FALHA: {len(all_errors)} erro(s).")
            return 1
        print("OK: 130Q (50/40/40); NULA só 2025-Q23; Q22/Q39-2025 com "
              "needsVisualCheck + motivo; páginas DESCONHECIDAS; "
              "proveniência TRANSCRIBED_FROM_MD; sem explanation.")
        return 0

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    manifest_entries: list[dict] = []
    global_warnings: list[str] = []
    for ano in ("2021", "2022", "2025"):
        payload = build_payload(ano)
        out = OUT_DIR / f"{ano}.json"
        out.write_text(
            json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
        qs = payload["questions"]
        n_nula = sum(1 for q in qs if q["answer"] == "NULA")
        n_nvc = sorted(q["number"] for q in qs if q["needs_visual_check"])
        from collections import Counter

        dist = dict(sorted(Counter(q["answer"] for q in qs).items()))
        print(f"EAJ-{ano}: {len(qs)} objetivas, NULA={n_nula}, "
              f"needsVisualCheck={n_nvc or '-'}, distribuição={dist}, "
              f"avisos={len(payload['warnings'])}")
        for w in payload["warnings"]:
            print(f"  AVISO EAJ-{ano}: {w}")
            global_warnings.append(f"EAJ-{ano}: {w}")
        manifest_entries.append(
            {
                "institution": INSTITUTION,
                "edition": ano,
                "source_document": payload["metadata"]["source_document"],
                "source_sha256": payload["metadata"]["source_sha256"],
                "n_objective": len(qs),
                "n_complete_options": sum(
                    1 for q in qs
                    if all(str(q["options"][k]).strip() for k in "ABCD")
                ),
                "n_nula_transcribed": n_nula,
                "needs_visual_check": n_nvc,
                "answer_distribution": dist,
                "n_warnings": len(payload["warnings"]),
            }
        )
    manifest = {
        "institution": INSTITUTION,
        "extractor": EXTRACTOR,
        "extractor_version": EXTRACTOR_VERSION,
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "editions": manifest_entries,
        "totals": {
            "editions": len(manifest_entries),
            "objective_questions": sum(e["n_objective"] for e in manifest_entries),
        },
        "warnings": global_warnings,
        "note": (
            "Namespace EAJ (EAJ-2022 ≠ IFRN-2022, EAJ-2025 ≠ IFRN-2025). "
            "Respostas TRANSCRITAS do `.md`, nunca documento oficial."
        ),
    }
    (OUT_DIR / "manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Manifest: {OUT_DIR / 'manifest.json'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
