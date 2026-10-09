#!/usr/bin/env python3
"""Apoio à curadoria de textos-base — TASK 6.9 / docs/passagens-estrategia.md
(+ Programa EAJ, TASK B.4: namespace `eaj`).

NÃO extrai nada sozinho para o banco: a fonte de verdade é o JSON curado
`data/passages/<ano>.json` (IFRN; transcrição literal conferida
visualmente contra o caderno oficial) e `data/passages/eaj/<ano>.json`
(EAJ; blocos transcritos literalmente da seção `## Textos-base` dos
`questoes.md` — fonte da Fase B — mais descrições visuais curadas onde o
`.md` não traz bloco, ver curadoria de cada arquivo). Este script só:

  --suggest ANO   lista candidatos a passagem no caderno IFRN (cabeçalhos
                  "Texto N" e blocos "Considere o trecho...") com a página
                  impressa e o início do bloco, para orientar a curadoria;
  --check         valida os JSONs curados (IFRN + EAJ): esquema, páginas,
                  questões vinculadas existem no `extracted` do namespace
                  e o enunciado cita o rótulo da passagem (regra de vínculo).

Uso:
    python3 scripts/db/extract_passages.py --suggest 2020
    python3 scripts/db/extract_passages.py --check
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
PASSAGES_DIR = REPO_ROOT / "data" / "passages"
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted"
# Namespace EAJ (TASK B.4): EAJ-2022 ≠ IFRN-2022, EAJ-2025 ≠ IFRN-2025 —
# ano sozinho nunca identifica a edição (TASKS.md regra 3).
PASSAGES_EAJ_DIR = REPO_ROOT / "data" / "passages" / "eaj"
EXTRACTED_EAJ_DIR = REPO_ROOT / "data" / "extracted" / "eaj"
EAJ_EDITIONS = (2021, 2022, 2025)  # estrutura de cada edição vale só para ela

KINDS = {"TEXTO", "TRECHO", "TABELA", "GRAFICO", "IMAGEM", "CHARGE", "TIRINHA"}

HEADER_RE = re.compile(r"^(Texto\s+\d+)\s*$", re.IGNORECASE | re.MULTILINE)
TRECHO_RE = re.compile(
    r"^((?:Considere|Leia|Utilize) o trech[oa][^\n]*?quest(?:[aã]o|[õo]es)\s+[^\n]*)",
    re.IGNORECASE | re.MULTILINE,
)


def trecho_questions(header: str) -> list[int]:
    """Questões cobertas pelo cabeçalho de um bloco-trecho.

    O caderno varia a redação: "questões de X a Y" (2020), "questões
    04, 05 e 06" / "questões 09, 10, 11 e 12" (enumeração, 2022),
    "questão 8" (singular, 2022), "questões 13 a 15" (intervalo sem
    "de", 2023) ou "questões de 06 e 09" (par de extremos com "e",
    2024: as Q07–Q08 estão fisicamente sob o mesmo cabeçalho e usam
    os mesmos marcadores (1)/(2) do quadro). Intervalo "X a Y" (com
    ou sem "de") vira range; par "X e Y" sem vírgula (só 2 números)
    também vira range X..Y; nas demais formas valem os números
    explícitos no cabeçalho (referências a "Texto N" no próprio
    cabeçalho são ignoradas).
    """
    clean = re.sub(r"textos?\s*\d+", "", header, flags=re.IGNORECASE)
    m = re.search(r"(?:de\s+)?(\d+)\s+a\s+(\d+)", clean, re.IGNORECASE)
    if m:
        return list(range(int(m.group(1)), int(m.group(2)) + 1))
    nums = [int(x) for x in re.findall(r"\d+", clean)]
    if (
        len(nums) == 2
        and "," not in clean
        and nums[1] >= nums[0]
        and re.search(r"\d+\s+e\s+\d+", clean, re.IGNORECASE)
    ):
        return list(range(nums[0], nums[1] + 1))
    return nums


def cited_text_numbers(statement: str) -> set[int]:
    """Números de "Texto N" citados no enunciado (case-insensitive).

    Aceita as variantes impressas no caderno: "Texto 1", "texto 1",
    "Texto 01" (zero à esquerda, 2022 Q22) e "Texto1" (sem espaço,
    2022 Q38); comparação por inteiro. O plural explícito
    "Textos 1 e 2" (2020 Q20) vincula ambos; a enumeração plural
    "textos 1, 2 e 3" (2024 Q19–Q20) vincula os três (todos os
    inteiros na sequência após "textos"). Citação genérica sem
    número ("três textos", 2022 Q20) continua sem vínculo.
    """
    stl = statement.lower()
    nums = {int(x) for x in re.findall(r"textos?\s*0*(\d+)", stl)}
    for m in re.finditer(r"textos\s+((?:0*\d+\s*(?:[,e]\s*)?)+)", stl):
        nums.update(int(x) for x in re.findall(r"\d+", m.group(1)))
    return nums


def caderno_pdf(year: int) -> Path | None:
    d = REPO_ROOT / "data" / "provas" / str(year)
    if not d.is_dir():
        return None
    for p in sorted(d.glob("*.pdf")):
        if "gabarito" not in p.name.lower():
            return p
    return None


def pdftotext_pages(pdf: Path) -> list[str]:
    p = subprocess.run(
        ["pdftotext", "-layout", str(pdf), "-"],
        capture_output=True, text=True,
    )
    if p.returncode != 0:
        raise SystemExit(f"pdftotext falhou: {p.stderr.strip()[:500]}")
    return p.stdout.split("\x0c")


def printed_number(page_text: str) -> int | None:
    # Rodapé do caderno: "N ... Processo Seletivo ..." ou
    # "... Integrada 2020 ... N" no fim da página.
    tail = page_text[-600:]
    m = re.search(r"(?m)^(\d{1,2})\s+Processo Seletivo", tail)
    if m:
        return int(m.group(1))
    m = re.search(r"Processo Seletivo[^\n]*?(\d{1,2})\s*$", tail)
    return int(m.group(1)) if m else None


def cmd_suggest(year: int) -> int:
    pdf = caderno_pdf(year)
    if pdf is None:
        print(f"sem caderno em data/provas/{year}/", file=sys.stderr)
        return 2
    pages = pdftotext_pages(pdf)
    print(f"# {year}: {pdf.name} ({len(pages)} págs. PDF)")
    for i, text in enumerate(pages):
        num = printed_number(text)
        tag = f"impressa {num}" if num else "sem número impresso (capa?)"
        for m in HEADER_RE.finditer(text):
            nxt = " ".join(text[m.end():].split())[:120]
            print(f"- PDF p{i + 1} ({tag}): {m.group(1)} :: {nxt}...")
        for m in TRECHO_RE.finditer(text):
            header = " ".join(m.group(1).split())
            print(f"- PDF p{i + 1} ({tag}): TRECHO Q{trecho_questions(header)} :: {header[:100]}")
    return 0


def load_extracted_numbers(year: int) -> dict[int, str]:
    f = EXTRACTED_DIR / f"{year}.json"
    if not f.exists():
        return {}
    d = json.loads(f.read_text(encoding="utf-8"))
    return {q["number"]: (q.get("statement") or "") for q in d.get("questions", [])}


def load_extracted_numbers_eaj(year: int) -> dict[int, str]:
    """Enunciados EAJ (namespace `eaj`: fonte = `.md`, TASK B.1)."""
    f = EXTRACTED_EAJ_DIR / f"{year}.json"
    if not f.exists():
        return {}
    d = json.loads(f.read_text(encoding="utf-8"))
    return {q["number"]: (q.get("statement") or "") for q in d.get("questions", [])}


def check_file(f: Path, statements: dict[int, str], namespace: str) -> list[str]:
    """Valida um JSON de passagens. Retorna a lista de erros.

    `namespace` = "IFRN" (arquivos em `data/passages/*.json`, páginas
    impressas do caderno) ou "EAJ" (arquivos em `data/passages/eaj/`,
    páginas DESCONHECIDAS salvo auditoria futura — nunca inventar).
    A regra de vínculo é idêntica nos dois (TASKS.md B.4): TEXTO — o
    enunciado cita o rótulo com número; TRECHO — a questão está no
    intervalo declarado no `intro`. Sem evidência = sem vínculo.
    """
    errors: list[str] = []
    try:
        d = json.loads(f.read_text(encoding="utf-8"))
    except Exception as exc:
        return [f"{f.name}: JSON inválido ({exc})"]
    year = d.get("edition")
    if namespace == "EAJ":
        if d.get("institution") != "EAJ":
            errors.append(f"{f.name}: sem namespace EAJ "
                          f"(institution={d.get('institution')!r})")
        if year not in EAJ_EDITIONS:
            errors.append(f"{f.name}: edition={year!r} fora de {list(EAJ_EDITIONS)}")
    for p in d.get("passages", []):
        key = p.get("key", "?")
        tag = f"{f.name}:{key}"
        if p.get("kind") not in KINDS:
            errors.append(f"{tag}: kind inválido ({p.get('kind')})")
        if not p.get("label"):
            errors.append(f"{tag}: label ausente")
        ps, pe = p.get("page_start"), p.get("page_end")
        if namespace == "EAJ" and ps is None and pe is None:
            # `.md` não traz página (TASK B.1): exigir o carimbo honesto.
            if p.get("page_status") != "DESCONHECIDO":
                errors.append(f"{tag}: páginas NULL sem "
                              f"page_status=DESCONHECIDO")
        elif not isinstance(ps, int) or not isinstance(pe, int) or ps < 1 or pe < ps:
            errors.append(f"{tag}: páginas inválidas ({ps}–{pe})")
        has_text = bool((p.get("content") or "").strip())
        if not has_text and not (p.get("visual_description") or "").strip():
            errors.append(f"{tag}: sem content nem visual_description")
        for qn in p.get("questions", []):
            st = statements.get(qn)
            if st is None:
                errors.append(f"{tag}: questão {qn} inexistente em "
                              f"extracted/{namespace.lower() + '/' if namespace == 'EAJ' else ''}{year}.json")
                continue
            # Regra de vínculo (auditável, duas formas de evidência):
            # TEXTO/TABELA/...: o enunciado cita o rótulo ("Texto 1",
            #   inclusive no plural "Textos 1 e 2");
            # TRECHO: o número da questão está no intervalo declarado no
            #   cabeçalho do bloco ("...questões de X a Y").
            label = (p.get("label") or "")
            if (p.get("kind") or "") == "TRECHO":
                covered = trecho_questions(p.get("intro") or "")
                ok = qn in covered
                hint = "fora do intervalo do intro" if not ok else ""
            else:
                m = re.search(r"(\d+)", label)
                ok = bool(m) and int(m.group(1)) in cited_text_numbers(st)
                hint = f"enunciado não cita o rótulo ({(st or '')[:60]}…)"
            if not ok:
                errors.append(f"{tag}: Q{qn} sem evidência de vínculo ({hint})")
    return errors


def cmd_check() -> int:
    errors: list[str] = []
    files = sorted(p for p in PASSAGES_DIR.glob("*.json") if p.is_file())
    if not files:
        errors.append("nenhum data/passages/*.json")
    for f in files:
        d = json.loads(f.read_text(encoding="utf-8")) if f.exists() else {}
        statements = load_extracted_numbers(d.get("edition")) if d.get("edition") else {}
        errors.extend(check_file(f, statements, "IFRN"))
    eaj_files = sorted(PASSAGES_EAJ_DIR.glob("*.json")) if PASSAGES_EAJ_DIR.is_dir() else []
    for ano in EAJ_EDITIONS:
        if PASSAGES_EAJ_DIR / f"{ano}.json" not in eaj_files:
            errors.append(f"eaj/{ano}.json ausente (TASK B.4)")
    for f in eaj_files:
        try:
            d = json.loads(f.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"eaj/{f.name}: JSON inválido ({exc})")
            continue
        statements = load_extracted_numbers_eaj(d.get("edition"))
        if not statements and d.get("edition") in EAJ_EDITIONS:
            errors.append(f"eaj/{f.name}: data/extracted/eaj/{d.get('edition')}.json "
                          f"ausente ou sem questões (dependência B.1)")
        errors.extend(check_file(f, statements, "EAJ"))
    for e in errors:
        print("ERRO:", e)
    if errors:
        return 1
    print(f"OK: {len(files)} arquivo(s) IFRN + {len(eaj_files)} arquivo(s) EAJ válidos")
    return 0


def main() -> int:
    ap = argparse.ArgumentParser(description="Apoio à curadoria de textos-base")
    ap.add_argument("--suggest", type=int, metavar="ANO")
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()
    if args.suggest:
        return cmd_suggest(args.suggest)
    if args.check:
        return cmd_check()
    ap.print_help()
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
