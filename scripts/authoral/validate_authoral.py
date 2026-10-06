#!/usr/bin/env python3
"""Validador dos specs autorais — TASK 15.1.

Aplica o contrato de `docs/authoral/manual-item-v1.md` a cada JSON em
`data/authoral/specs/`:

  - schema completo (discipline/topic/subtopic/skill/reasoning_type/
    difficulty_alvo/referencias_oficiais/texto_base/enunciado/options[4]/
    answerKey/distractor_rationale[3]);
  - códigos v1.1 válidos com par topic↔subtopic coerente (mesmo mapa do
    seed `V2__seed.sql`);
  - enunciado com comando; 4 opções distintas não-vazias;
  - answerKey A–D (nunca X);
  - referencias_oficiais existentes e do mesmo subassunto (v1.1);
  - texto_base próprio obrigatório em LP, proibido em MAT;
  - ausência de `IFRN` / `edital` / `prova 20XX` (case-insensitive);
  - checksum SHA-256 (mesma normalização do importador oficial:
    NFC + colapso de whitespace) distinto dos 240 oficiais e único no lote.

Somente leitura (nunca altera os specs). Falha alta: saída != 0 se
qualquer spec estiver vermelho ou se o diretório estiver vazio.

Uso:
    python3 scripts/authoral/validate_authoral.py [--check] [--specs DIR]
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
import unicodedata
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SPECS = REPO_ROOT / "data" / "authoral" / "specs"
EXTRACTED_DIR = REPO_ROOT / "data" / "extracted"

EDITIONS = ["2020", "2022", "2023", "2024", "2025", "2026"]

# Mapa v1.1 — cópia fiel dos pares discipline/topic/subtopic do seed
# (database/migrations/V2__seed.sql). Nada fora daqui é aceito.
DISCIPLINE_TOPICS: dict[str, set[str]] = {
    "LINGUA_PORTUGUESA": {"GRAMATICA_NORMA", "INTERPRETACAO_TEXTUAL"},
    "MATEMATICA": {
        "RAZAO_PROPORCAO", "ARITMETICA", "ALGEBRA", "GEOMETRIA",
        "ESTATISTICA_DADOS", "PORCENTAGEM", "MATEMATICA_FINANCEIRA",
        "GRANDEZAS_MEDIDAS",
    },
}
TOPIC_SUBTOPICS: dict[str, set[str]] = {
    "GRAMATICA_NORMA": {
        "MORFOLOGIA", "PONTUACAO", "SEMANTICA_VOCABULARIO",
        "COESAO_REFERENCIA", "SINTAXE_PERIODO", "SINTAXE_FUNCAO",
        "NORMA_PADRAO", "ACENTUACAO_GRAFICA",
    },
    "INTERPRETACAO_TEXTUAL": {
        "INTENCAO_COMUNICATIVA", "INFORMACAO_EXPLICITA", "INFERENCIA",
        "FUNCAO_ELEMENTO_PARAGRAFO", "TIPO_TEXTUAL", "GENERO_TEXTUAL",
    },
    "RAZAO_PROPORCAO": {"RAZAO", "REGRA_DE_TRES", "ESCALA", "PROPORCIONALIDADE"},
    "ARITMETICA": {
        "OPERACOES", "FRACOES", "NOTACAO_CIENTIFICA",
        "DIVISIBILIDADE_MMC_MDC", "SISTEMAS_NUMERACAO",
    },
    "ALGEBRA": {"FUNCAO_AFIM", "EQUACOES", "SISTEMAS", "SEQUENCIAS"},
    "GEOMETRIA": {
        "AREA_PLANA", "PERIMETRO_COMPRIMENTO", "POLIGONOS",
        "PITAGORAS_DISTANCIA", "VOLUME", "UNIDADES_MEDIDA",
    },
    "ESTATISTICA_DADOS": {"MEDIA", "LEITURA_GRAFICO_TABELA", "PROBABILIDADE"},
    "PORCENTAGEM": {"CALCULO_DIRETO", "VARIACAO", "REPRESENTACAO_FRACAO"},
    "MATEMATICA_FINANCEIRA": {"JUROS_SIMPLES"},
    "GRANDEZAS_MEDIDAS": {"TEMPO", "VOLUME_CAPACIDADE"},
}

SKILLS = {
    "LOCALIZAR_INFORMACAO_EXPLICITA", "INFERIR_INFORMACAO",
    "IDENTIFICAR_INTENCAO_COMUNICATIVA", "IDENTIFICAR_TIPO_GENERO",
    "ANALISAR_FUNCAO_SINTATICA", "ANALISAR_ESTRUTURA_PERIODO",
    "APLICAR_PONTUACAO", "IDENTIFICAR_REFERENTE_COESIVO",
    "ANALISAR_CLASSE_MORFOLOGICA", "INFERIR_SENTIDO_VOCABULARIO",
    "APLICAR_NORMA_PADRAO", "CALCULAR_DIRETO", "MODELAR_SITUACAO_PROBLEMA",
    "INTERPRETAR_GRAFICO_TABELA", "CONVERTER_UNIDADES", "ESTIMAR_APROXIMAR",
    "RACIOCINIO_ESPACIAL",
}
REASONINGS = {
    "INTERPRETATIVO_LOCALIZACAO", "INTERPRETATIVO_INFERENCIAL",
    "ANALITICO_GRAMATICAL", "CALCULO_DIRETO", "MODELAGEM_MONOETAPA",
    "MODELAGEM_MULTIETAPAS", "VISUAL_ESPACIAL", "PROPORCIONAL", "ESTIMATIVA",
}
DIFFICULTIES = {"FACIL", "MEDIA", "DIFICIL"}

# Mesmos overrides v1.1 do importador oficial (para resolver o subassunto
# das referencias_oficiais).
V11_OVERRIDES = {
    ("2022", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2023", 12): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2024", 17): ("GRAMATICA_NORMA", "ACENTUACAO_GRAFICA"),
    ("2026", 21): ("ARITMETICA", "SISTEMAS_NUMERACAO"),
}

REF_RE = re.compile(r"^(2020|2022|2023|2024|2025|2026)-Q([1-9]|[12][0-9]|3[0-9]|40)$")
COMMAND_RE = re.compile(
    r"\b(assinale|indique|identifique|calcule|determine|marque|escolha|selecione|aponte)\b",
    re.IGNORECASE,
)
FORBIDDEN = [
    (re.compile(r"ifrn", re.IGNORECASE), "IFRN"),
    (re.compile(r"edital", re.IGNORECASE), "edital"),
    (re.compile(r"\bprovas?\s+20\d{2}\b", re.IGNORECASE), "prova 20XX"),
]

REQUIRED_KEYS = {
    "id", "discipline", "topic", "subtopic", "skill", "reasoning_type",
    "difficulty_alvo", "referencias_oficiais", "texto_base", "enunciado",
    "options", "answerKey", "distractor_rationale",
}


def norm(text: str) -> str:
    return " ".join(unicodedata.normalize("NFC", text).split())


def checksum(statement: str, options: dict[str, str]) -> str:
    payload = norm(statement) + "\n" + "\n".join(
        f"{lbl}={norm(options[lbl])}" for lbl in ("A", "B", "C", "D")
    )
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def load_official() -> tuple[dict[str, str], dict[tuple[str, int], tuple[str, str, str]]]:
    """Retorna (checksums_hex, mapa (edicao, numero) -> (disciplina, assunto, sub))."""
    checksums: dict[str, str] = {}
    official: dict[tuple[str, int], tuple[str, str, str]] = {}
    classif_root = REPO_ROOT / "docs" / "content-analysis" / "per-edition"
    for ed in EDITIONS:
        ext = json.loads((EXTRACTED_DIR / f"{ed}.json").read_text(encoding="utf-8"))
        clf = json.loads((classif_root / f"{ed}.json").read_text(encoding="utf-8"))
        cf = {c["number"]: c for c in clf["classificacoes"]}
        for q in ext["questions"]:
            n = q["number"]
            cks = checksum(q["statement"], q["options"])
            checksums[cks] = f"{ed}-Q{n}"
            assunto, sub = cf[n]["assunto"], cf[n]["subassunto"]
            if (ed, n) in V11_OVERRIDES:
                assunto, sub = V11_OVERRIDES[(ed, n)]
            official[(ed, n)] = (q["discipline"], assunto, sub)
    return checksums, official


def validate_spec(
    path: Path,
    official_cks: dict[str, str],
    official_map: dict[tuple[str, int], tuple[str, str, str]],
) -> tuple[list[str], str | None]:
    errs: list[str] = []
    try:
        spec = json.loads(path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, UnicodeDecodeError) as e:
        return [f"JSON ilegível: {e}"], None
    if not isinstance(spec, dict):
        return ["raiz deve ser objeto"], None

    missing = REQUIRED_KEYS - set(spec)
    extra = set(spec) - REQUIRED_KEYS
    if missing:
        errs.append(f"chaves ausentes: {sorted(missing)}")
    if extra:
        errs.append(f"chaves estranhas: {sorted(extra)}")

    if spec.get("id") != path.stem:
        errs.append(f"id {spec.get('id')!r} diverge do arquivo {path.stem!r}")

    disc = spec.get("discipline")
    topic = spec.get("topic")
    sub = spec.get("subtopic")
    if disc not in DISCIPLINE_TOPICS:
        errs.append(f"discipline inválida: {disc!r}")
    elif topic not in DISCIPLINE_TOPICS.get(disc, set()):
        errs.append(f"topic {topic!r} fora da disciplina {disc}")
    if topic not in TOPIC_SUBTOPICS:
        errs.append(f"topic desconhecido: {topic!r}")
    elif sub not in TOPIC_SUBTOPICS.get(topic, set()):
        errs.append(f"subtopic {sub!r} incoerente com topic {topic}")

    if spec.get("skill") not in SKILLS:
        errs.append(f"skill inválida: {spec.get('skill')!r}")
    if spec.get("reasoning_type") not in REASONINGS:
        errs.append(f"reasoning_type inválido: {spec.get('reasoning_type')!r}")
    if spec.get("difficulty_alvo") not in DIFFICULTIES:
        errs.append(f"difficulty_alvo inválida: {spec.get('difficulty_alvo')!r}")

    refs = spec.get("referencias_oficiais")
    if not isinstance(refs, list) or not refs:
        errs.append("referencias_oficiais deve ser lista não-vazia")
        refs = []
    else:
        for r in refs:
            if not isinstance(r, str) or not REF_RE.match(r):
                errs.append(f"referencia com formato inválido: {r!r} (esperado AAAA-QN)")
                continue
            ed, num = r.split("-Q")
            key = (ed, int(num))
            if key not in official_map:
                errs.append(f"referencia inexistente no dataset oficial: {r}")
                continue
            odisc, oass, osub = official_map[key]
            if disc in DISCIPLINE_TOPICS and (odisc != disc or osub != sub):
                errs.append(
                    f"referencia {r} é {odisc}/{osub}, diverge de {disc}/{sub}"
                )

    base = spec.get("texto_base")
    if disc == "LINGUA_PORTUGUESA":
        if not isinstance(base, str) or len(base.strip()) < 50:
            errs.append("texto_base próprio obrigatório em LP (≥ 50 caracteres)")
    elif disc == "MATEMATICA":
        if base not in (None, ""):
            errs.append("texto_base proibido em MAT (deve ser null)")

    enun = spec.get("enunciado")
    if not isinstance(enun, str) or len(enun.strip()) < 30:
        errs.append("enunciado vazio ou curto (< 30 caracteres)")
    elif not (COMMAND_RE.search(enun) or enun.strip().endswith("?")):
        errs.append("enunciado sem comando (ver manual §4)")

    opts = spec.get("options")
    opt_norms: dict[str, str] = {}
    if not isinstance(opts, dict) or set(opts) != {"A", "B", "C", "D"}:
        errs.append("options deve ser objeto com exatamente A, B, C, D")
    else:
        for lbl in "ABCD":
            t = opts[lbl]
            if not isinstance(t, str) or not t.strip():
                errs.append(f"options.{lbl} vazia")
            else:
                opt_norms[lbl] = norm(t).casefold()
        if len(set(opt_norms.values())) != 4:
            errs.append("options com textos duplicados (normalizados)")

    ans = spec.get("answerKey")
    if ans not in ("A", "B", "C", "D"):
        errs.append(f"answerKey inválida: {ans!r} (nunca X)")
        ans = None

    dr = spec.get("distractor_rationale")
    if not isinstance(dr, list) or len(dr) != 3:
        errs.append("distractor_rationale deve ser array com 3 itens")
    else:
        seen = set()
        for i, item in enumerate(dr):
            if not isinstance(item, dict) or set(item) != {"option", "rationale"}:
                errs.append(f"distractor_rationale[{i}] deve ser {{option, rationale}}")
                continue
            o, r = item["option"], item["rationale"]
            seen.add(o)
            if ans is not None and o == ans:
                errs.append(f"distractor_rationale[{i}] aponta a correta ({ans})")
            if o not in ("A", "B", "C", "D"):
                errs.append(f"distractor_rationale[{i}].option inválida: {o!r}")
            if not isinstance(r, str) or len(r.strip()) < 20:
                errs.append(f"distractor_rationale[{i}] vazia ou curta (< 20 chars)")
        if ans is not None and seen != ({"A", "B", "C", "D"} - {ans}):
            errs.append(
                f"distractor_rationale deve cobrir as 3 erradas "
                f"({sorted({'A', 'B', 'C', 'D'} - {ans})}), achado {sorted(seen)}"
            )

    scanned = [
        ("texto_base", base if isinstance(base, str) else ""),
        ("enunciado", enun if isinstance(enun, str) else ""),
        *[("options." + lbl, opts[lbl] if isinstance(opts, dict) and isinstance(opts.get(lbl), str) else "")
          for lbl in "ABCD"],
    ]
    if isinstance(dr, list):
        for i, item in enumerate(dr):
            if isinstance(item, dict) and isinstance(item.get("rationale"), str):
                scanned.append((f"distractor_rationale[{i}]", item["rationale"]))
    for field, text in scanned:
        for rx, name in FORBIDDEN:
            if rx.search(text):
                errs.append(f"string proibida {name!r} em {field}")
                break

    cks = None
    if isinstance(enun, str) and isinstance(opts, dict) \
            and set(opts) == {"A", "B", "C", "D"} \
            and all(isinstance(opts[l], str) for l in "ABCD"):
        cks = checksum(enun, opts)
        if cks in official_cks:
            errs.append(f"checksum colide com oficial {official_cks[cks]}")

    return errs, cks


def main() -> int:
    ap = argparse.ArgumentParser(description="Validador dos specs autorais (TASK 15.1)")
    ap.add_argument("--check", action="store_true",
                    help="modo CI: saída concisa, falha alta")
    ap.add_argument("--specs", default=str(DEFAULT_SPECS),
                    help="diretório dos specs JSON")
    args = ap.parse_args()

    specs_dir = Path(args.specs)
    files = sorted(specs_dir.glob("*.json")) if specs_dir.is_dir() else []
    if not files:
        print(f"ERRO: nenhum spec em {specs_dir}", file=sys.stderr)
        return 1

    try:
        official_cks, official_map = load_official()
    except (FileNotFoundError, KeyError, json.JSONDecodeError) as e:
        print(f"ERRO: dataset oficial ilegível: {e}", file=sys.stderr)
        return 2
    if len(official_map) != 240:
        print(f"ERRO: esperado 240 oficiais, achado {len(official_map)}", file=sys.stderr)
        return 2

    results: dict[str, list[str]] = {}
    checksums: dict[str, str] = {}
    for path in files:
        errs, cks = validate_spec(path, official_cks, official_map)
        results[path.stem] = errs
        if cks:
            checksums[path.stem] = cks

    seen: dict[str, str] = {}
    for stem, cks in checksums.items():
        if cks in seen:
            results[stem].append(f"checksum duplicado no lote (igual a {seen[cks]})")
            results[seen[cks]].append(f"checksum duplicado no lote (igual a {stem})")
        else:
            seen[cks] = stem

    green = sum(1 for e in results.values() if not e)
    for stem in sorted(results):
        errs = results[stem]
        status = "VERDE" if not errs else "VERMELHO"
        print(f"{stem}: {status} (checksum {checksums.get(stem, '—')[:12]})")
        if not args.check or errs:
            for e in errs:
                print(f"  ERRO {stem}: {e}")
    print(f"specs={len(files)} verdes={green} oficiais=240 colisoes_oficiais=0"
          if green == len(files) else
          f"specs={len(files)} verdes={green} FALHA")
    return 0 if green == len(files) else 1


if __name__ == "__main__":
    sys.exit(main())
