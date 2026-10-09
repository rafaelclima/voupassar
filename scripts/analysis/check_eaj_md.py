#!/usr/bin/env python3
"""Validador dos 3 `questoes.md` EAJ — TASK A.3 (somente leitura).

Schema único esperado por edição (só estrutura; conteúdo preservado):
  `# EAJ/UFRN <ANO> ...` + nota de proveniência (transcrição, não oficial)
  `## Textos-base` (blocos transcritos)
  `## Língua Portuguesa` / `## Matemática` (+ `## Ciências da Natureza` /
  `## Ciências Humanas` só em 2021)
  por questão: `**NN.**` <enunciado...> + `A)` `B)` `C)` `D)` +
  (`**Análise:**`)? + `**Gabarito: X**` (X em A–D; NULA só em EAJ-2025 Q23)
  `## Gabarito compilado (transcrição — não é documento oficial)` +
  tabela `| **NN** | X |` com uma linha por questão.

Verifica (exit 0 = verde):
  - totais: 130 questões (2021: 50; 2022: 40; 2025: 40);
  - por área: 2021 LP 01–15 / MAT 16–30 / CN 31–42 / CH 43–50
    (15/15/12/8); 2022 e 2025 LP 01–20 / MAT 21–40 (20/20);
  - 4 alternativas A–D por questão (Q23-2025 NULA tem as 4 alternativas
    transcritas; a isenção "exceto NULA" refere-se à RESPOSTA, não às
    alternativas — todas as 130 têm A–D);
  - resposta inline presente (A–D; NULA só em 2025-Q23);
  - consistência inline × tabela compilada (divergência = exit 2, nunca
    resolução silenciosa — antecipa a regra da TASK B.2);
  - Q22-2025 e Q39-2025 com notas de incerteza transcritas viram
    `NEEDS_VISUAL_CHECK` (aviso, NÃO falha).

Uso:
    python3 scripts/analysis/check_eaj_md.py

Nunca escreve nos `.md`. Fonte de extração = `.md`; PDF só auditoria
(TASKS.md regra 1). Transcrição nunca é documento oficial (regra 2).
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
EAJ_DIR = REPO_ROOT / "data" / "provas" / "EAJ"

EXPECTED = {
    "2021": {
        "areas": [
            ("Língua Portuguesa", list(range(1, 16))),
            ("Matemática", list(range(16, 31))),
            ("Ciências da Natureza", list(range(31, 43))),
            ("Ciências Humanas", list(range(43, 51))),
        ]
    },
    "2022": {
        "areas": [
            ("Língua Portuguesa", list(range(1, 21))),
            ("Matemática", list(range(21, 41))),
        ]
    },
    "2025": {
        "areas": [
            ("Língua Portuguesa", list(range(1, 21))),
            ("Matemática", list(range(21, 41))),
        ]
    },
}

TABLE_HEAD = "## Gabarito compilado (transcrição — não é documento oficial)"

Q_PAT = re.compile(
    r"^\*\*(\d{1,2})\.\*\*[ \t]*([\s\S]*?)\n"
    r"(^A\)[^\n]*\n^B\)[^\n]*\n^C\)[^\n]*\n^D\)[^\n]*)"
    r"\n(?:\*\*Análise:\*\*[ \t]*([\s\S]*?)\n)?"
    r"^\*\*Gabarito:[ \t]*([A-D]|NULA)\*\*",
    flags=re.M,
)
TABLE_ROW = re.compile(r"^\|\s*\*\*(\d{1,2})\*\*\s*\|\s*([A-D]|Nula|NULA)\s*\|", flags=re.M)


def check_edition(ano: str) -> tuple[list[str], list[str], dict]:
    errors: list[str] = []
    warnings: list[str] = []
    path = EAJ_DIR / ano / "questoes.md"
    if not path.exists():
        return [f"EAJ-{ano}: arquivo ausente: {path}"], warnings, {}
    text = path.read_text(encoding="utf-8")

    # Cabeçalho de schema
    if not re.search(r"^# EAJ/UFRN 202\d", text, flags=re.M):
        errors.append(f"EAJ-{ano}: título `# EAJ/UFRN <ANO>` ausente (schema único)")
    if "TRANSCRIBED_FROM_MD" not in text:
        errors.append(f"EAJ-{ano}: nota de proveniência TRANSCRIBED_FROM_MD ausente")
    if not re.search(r"^## Textos-base\s*$", text, flags=re.M):
        errors.append(f"EAJ-{ano}: seção `## Textos-base` ausente")
    if "NÃO alimenta o banco" not in text:
        errors.append(f"EAJ-{ano}: nota de que `Análise:` não alimenta o banco ausente")
    if TABLE_HEAD not in text:
        errors.append(f"EAJ-{ano}: seção `{TABLE_HEAD}` ausente")
    if re.search(r"GABARITO OFICIAL", text):
        errors.append(
            f"EAJ-{ano}: cabeçalho com 'GABARITO OFICIAL' — transcrição nunca é documento oficial"
        )

    for area, _ in EXPECTED[ano]["areas"]:
        if not re.search(rf"^## {re.escape(area)}\s*$", text, flags=re.M):
            errors.append(f"EAJ-{ano}: seção `## {area}` ausente")

    # Questões inline (só antes da tabela compilada)
    body = text.split(TABLE_HEAD)[0] if TABLE_HEAD in text else text
    found = list(Q_PAT.finditer(body))
    inline: dict[int, str] = {}
    for m in found:
        n = int(m.group(1))
        if n in inline:
            errors.append(f"EAJ-{ano}: questão {n:02d} duplicada no inline")
        inline[n] = m.group(5).strip()

    # Contagens por área (faixa de número daquela edição)
    for area, nums in EXPECTED[ano]["areas"]:
        missing = [n for n in nums if n not in inline]
        extra = [n for n in inline if n in nums and n not in nums]  # nunca ocorre; clareza
        if missing:
            errors.append(f"EAJ-{ano} {area}: faltando {missing}")
        _ = extra

    total_esperado = sum(len(nums) for _, nums in EXPECTED[ano]["areas"])
    if len(inline) != total_esperado:
        errors.append(
            f"EAJ-{ano}: {len(inline)} questões inline, esperado {total_esperado} "
            f"({'+'.join(str(len(n)) for _, n in EXPECTED[ano]['areas'])})"
        )

    # Resposta: A–D, NULA só em 2025-Q23
    for n, g in sorted(inline.items()):
        if g == "NULA":
            if not (ano == "2025" and n == 23):
                errors.append(f"EAJ-{ano} Q{n:02d}: NULA fora de 2025-Q23 (não permitido)")
        elif g not in ("A", "B", "C", "D"):
            errors.append(f"EAJ-{ano} Q{n:02d}: gabarito inline inválido: {g!r}")

    # Alternativas A–D: o regex já exige as 4; confere letra inicial em ordem
    for m in found:
        alts = [line.strip() for line in m.group(3).split("\n")]
        if [a[0] for a in alts] != ["A", "B", "C", "D"]:
            errors.append(f"EAJ-{ano} Q{int(m.group(1)):02d}: alternativas fora de A–D")

    # Tabela compilada
    table_text = text.split(TABLE_HEAD)[1] if TABLE_HEAD in text else ""
    table: dict[int, str] = {}
    for m in TABLE_ROW.finditer(table_text):
        n = int(m.group(1))
        g = m.group(2).strip().upper()
        if g == "NULA":
            pass
        if n in table:
            errors.append(f"EAJ-{ano}: questão {n:02d} duplicada na tabela compilada")
        table[n] = g
    if len(table) != total_esperado:
        errors.append(
            f"EAJ-{ano}: tabela compilada com {len(table)} linhas, esperado {total_esperado}"
        )
    for n, g_inline in sorted(inline.items()):
        if n not in table:
            errors.append(f"EAJ-{ano} Q{n:02d}: ausente na tabela compilada")
        elif table[n] != g_inline:
            errors.append(
                f"EAJ-{ano} Q{n:02d}: divergência inline ({g_inline}) × "
                f"tabela ({table[n]}) — ver exit 2"
            )

    # Distribuição das respostas: degenerada (ex. 40×A) vira aviso, nunca falha
    # (conteúdo transcrito é preservado; a conferência é contra o gabarito
    # oficial quando anexado — ver docs/blockers.md).
    from collections import Counter

    dist = Counter(inline.values())
    if len(inline) > 0 and len(dist) == 1:
        only = next(iter(dist))
        warnings.append(
            f"EAJ-{ano}: distribuição degenerada — {len(inline)}/{len(inline)} "
            f"respostas = {only} (transcrição preservada; NÃO CONFIRMADO até o "
            f"gabarito oficial; ver docs/blockers.md)"
        )
    if ano == "2025":
        q22 = re.search(
            r"^\*\*22\.\*\*[\s\S]*?^\*\*Gabarito:[ \t]*([A-D]|NULA)\*\*",
            body,
            flags=re.M,
        )
        if q22 and "OCR" not in q22.group(0):
            warnings.append("EAJ-2025 Q22: nota de OCR agrupado não localizada (esperava NEEDS_VISUAL_CHECK)")
        elif q22:
            warnings.append("EAJ-2025 Q22: NEEDS_VISUAL_CHECK — nota de OCR agrupado transcrita (conferir no PDF antes de importar)")
        q39 = re.search(
            r"^\*\*39\.\*\*[\s\S]*?^\*\*Gabarito:[ \t]*([A-D]|NULA)\*\*",
            body,
            flags=re.M,
        )
        if q39 and ("mancha" not in q39.group(0) and "interpolada" not in q39.group(0)):
            warnings.append("EAJ-2025 Q39: nota de razão interpolada não localizada (esperava NEEDS_VISUAL_CHECK)")
        elif q39:
            warnings.append("EAJ-2025 Q39: NEEDS_VISUAL_CHECK — razão [1/2] interpolada com incerteza (conferir no PDF antes de importar)")

    stats = {"inline": len(inline), "table": len(table)}
    return errors, warnings, stats


def main() -> int:
    all_errors: list[str] = []
    all_warnings: list[str] = []
    totals = 0
    nula_ok = False
    for ano in ("2021", "2022", "2025"):
        errors, warnings, stats = check_edition(ano)
        all_errors.extend(errors)
        all_warnings.extend(warnings)
        totals += stats.get("inline", 0)
        t = (EAJ_DIR / ano / "questoes.md").read_text(encoding="utf-8") if (EAJ_DIR / ano / "questoes.md").exists() else ""
        m23 = re.search(r"^\*\*23\.\*\*[\s\S]*?^\*\*Gabarito:[ \t]*NULA\*\*", t.split(TABLE_HEAD)[0] if TABLE_HEAD in t else t, flags=re.M)
        if ano == "2025" and m23:
            nula_ok = True

    print(f"EAJ questoes.md — inline: {totals}/130 (2021: 15/15/12/8; 2022/2025: 20/20)")
    for w in all_warnings:
        print(f"WARN: {w}")
    if totals != 130:
        all_errors.append(f"total inline {totals} != 130")
    if not nula_ok:
        all_errors.append("2025-Q23 NULA declarada não encontrada (`**Gabarito: NULA**`)")

    diverg = [e for e in all_errors if "divergência inline" in e]
    other = [e for e in all_errors if "divergência inline" not in e]
    for e in other + diverg:
        print(f"ERROR: {e}")

    if diverg and not other:
        print("FALHA DE VINCULAÇÃO (exit 2): divergência inline × tabela — nunca resolver silenciosamente.")
        return 2
    if all_errors:
        print(f"FALHA: {len(all_errors)} erro(s).")
        return 1
    print("OK: 130 questões (129 A–D + 2025-Q23 NULA); inline × tabela consistentes; Q22/Q39-2025 como NEEDS_VISUAL_CHECK.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
