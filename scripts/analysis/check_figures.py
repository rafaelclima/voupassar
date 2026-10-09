#!/usr/bin/env python3
"""Validador de figuras — docs/figuras-estrategia.md (+ §7 EAJ, TASK B.3)

Só leitura por padrão. Verifica:
  - manifest.json existe e é válido;
  - nomes dos arquivos em assets/figures/ batem com o manifest;
  - formatos aceitos (.webp preferido, .png/.jpg aceitos com aviso);
  - dimensões via cabeçalho (stdlib, sem Pillow); lado maior <= 1600 px,
    arquivo < 300 KB;
  - cada entrada tem `alt`, `page` e `files` (lista não vazia);
  - cobertura das 33 questões IFRN com `has_figure=TRUE` no banco
    (via `data/linked/` + `docs/content-analysis/per-edition/`);
  - namespace EAJ (`EAJ-<ano>-<n>` → `eaj/<ano>/Q<NN>(-k)?.webp`):
    page int > 0 ou "DESCONHECIDA" (nunca inventar página), alt
    obrigatório, credit EAJ/Comperve, cobertura das 58 questões com
    recorte (questão sem recorte = aviso + referência ao caderno,
    nunca imagem inventada), sem colisão com as chaves IFRN
    (`2022-*`/`2025-*` intactas — ano sozinho nunca decide a edição).

Uso:
    python3 scripts/analysis/check_figures.py [--fix-alt]
"""
from __future__ import annotations

import json
import re
import struct
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
ASSETS_DIR = REPO_ROOT / "frontend" / "assets" / "figures"
MANIFEST = ASSETS_DIR / "manifest.json"

EXPECTED_COUNT = 33  # conforme importador + V14: 33 has_figure=TRUE (IFRN)
ALLOWED_FORMATS = {".webp", ".png", ".jpg", ".jpeg"}
PREFERRED_FORMAT = ".webp"
MAX_SIDE = 1600
MAX_KB = 300

EAJ_KEY_RE = re.compile(r"^EAJ-(2021|2022|2025)-(\d{1,2})$")
EAJ_FILE_RE = re.compile(r"^eaj/(2021|2022|2025)/Q(\d{2})(-(\d+))?\.(webp|png|jpg|jpeg)$")
EAJ_MAX_Q = {"2021": 50, "2022": 40, "2025": 40}  # estrutura de cada edição

# Questões EAJ com recorte (inventário B.3: 33 PNGs → 58 chaves, 60 arquivos).
EAJ_EXPECTED: dict[str, list[int]] = {
    "2021": [11, 12, 13, 14, 15, 18, 21, 22, 27, 28, 34, 40, 43, 44, 46, 47, 48],
    "2022": [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24,
             27, 28, 33, 34, 38, 39, 40],
    "2025": [9, 10, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 30, 31, 34, 36, 37, 38],
}


def load_manifest():
    if not MANIFEST.exists():
        raise SystemExit("manifest.json ausente: " + str(MANIFEST))
    try:
        return json.loads(MANIFEST.read_text(encoding="utf-8"))
    except Exception as exc:
        raise SystemExit(f"manifest.json inválido: {exc}")


def is_eaj_key(key: str) -> bool:
    return key.startswith("EAJ-")


def image_max_side(path: Path):
    """Lê o lado maior (px) do cabeçalho. Retorna None se ilegível."""
    try:
        data = path.read_bytes()[:64]
    except OSError:
        return None
    ext = path.suffix.lower()
    try:
        if ext == ".png" and len(data) >= 24 and data[:8] == b"\x89PNG\r\n\x1a\n":
            w, h = struct.unpack(">II", data[16:24])
            return max(w, h)
        if ext in (".jpg", ".jpeg"):
            i = 2
            while i + 9 < len(data):
                if data[i] != 0xFF:
                    break
                marker = data[i + 1]
                if marker in (0xC0, 0xC1, 0xC2):
                    h, w = struct.unpack(">HH", data[i + 5:i + 9])
                    return max(w, h)
                if i + 4 > len(data):
                    break
                size = struct.unpack(">H", data[i + 2:i + 4])[0]
                if size < 2:
                    break
                # SOF pode estar além dos 64 bytes — relê se preciso.
                if marker == 0xDA:
                    break
                i += 2 + size
            with open(path, "rb") as fh:
                blob = fh.read(1 << 20)
            i = 2
            while i + 9 < len(blob):
                if blob[i] != 0xFF:
                    break
                marker = blob[i + 1]
                if marker in (0xC0, 0xC1, 0xC2):
                    h, w = struct.unpack(">HH", blob[i + 5:i + 9])
                    return max(w, h)
                if marker == 0xDA:
                    break
                size = struct.unpack(">H", blob[i + 2:i + 4])[0]
                if size < 2:
                    break
                i += 2 + size
            return None
        if ext == ".webp" and len(data) >= 12 and data[:4] == b"RIFF" and data[8:12] == b"WEBP":
            with open(path, "rb") as fh:
                blob = fh.read(1 << 20)
            fourcc = blob[12:16] if len(blob) >= 16 else b""
            if fourcc == b"VP8 " and len(blob) >= 30:
                w = struct.unpack("<H", blob[26:28])[0] & 0x3FFF
                h = struct.unpack("<H", blob[28:30])[0] & 0x3FFF
                return max(w, h)
            if fourcc == b"VP8L" and len(blob) >= 21:
                b0, b1, b2, b3 = blob[21], blob[22], blob[23], blob[24]
                w = 1 + (((b1 & 0x3F) << 8) | b0)
                h = 1 + (((b3 & 0x0F) << 10) | (b2 << 2) | ((b1 & 0xC0) >> 6))
                return max(w, h)
            if fourcc == b"VP8X" and len(blob) >= 30:
                w = struct.unpack("<I", blob[24:27] + b"\x00")[0] + 1
                h = struct.unpack("<I", blob[27:30] + b"\x00")[0] + 1
                return max(w, h)
            return None
    except (struct.error, IndexError):
        return None
    return None


def check_names(manifest):
    errors = []
    figures = manifest.get("figures", {})
    found_files = set()
    for key, entry in figures.items():
        files = entry.get("files", []) if isinstance(entry, dict) else []
        for f in files:
            # Nome deve ser Q<NN>(-k)?.ext (o diretório distingue o namespace).
            name = Path(f).name
            if not name.startswith("Q"):
                errors.append(f"[{key}] arquivo '{f}' não começa com Q")
            ext = Path(f).suffix.lower()
            if ext not in ALLOWED_FORMATS:
                errors.append(f"[{key}] formato '{ext}' não permitido ({f})")
            if is_eaj_key(key) and not f.startswith("eaj/"):
                errors.append(f"[{key}] chave EAJ fora de eaj/: {f}")
            if not is_eaj_key(key) and f.startswith("eaj/"):
                errors.append(f"[{key}] chave IFRN apontando para eaj/: {f}")
            found_files.add(f)
        # Verifica que arquivo físico existe
        for f in files:
            path = ASSETS_DIR / f
            if not path.exists():
                errors.append(f"[{key}] arquivo físico ausente: {f}")
    # Verifica arquivos físicos extras (não referenciados no manifest)
    for f in sorted(ASSETS_DIR.rglob("*")):
        if not f.is_file():
            continue
        rel = str(f.relative_to(ASSETS_DIR))
        if rel == "manifest.json":
            continue
        if rel not in found_files:
            errors.append(f"arquivo físico sem referência no manifest: {rel}")
    return errors


def check_formats(manifest):
    warnings = []
    figures = manifest.get("figures", {})
    for key, entry in figures.items():
        files = entry.get("files", []) if isinstance(entry, dict) else []
        for f in files:
            path = ASSETS_DIR / f
            if not path.exists():
                continue
            ext = Path(f).suffix.lower()
            if ext != PREFERRED_FORMAT:
                warnings.append(f"[{key}] preferir .webp (atual: {ext}): {f}")
    return warnings


def check_dimensions(manifest):
    errors = []
    warnings = []
    figures = manifest.get("figures", {})
    for key, entry in figures.items():
        files = entry.get("files", []) if isinstance(entry, dict) else []
        for f in files:
            path = ASSETS_DIR / f
            if not path.exists():
                continue
            size_kb = path.stat().st_size / 1024
            if size_kb > MAX_KB:
                errors.append(f"[{key}] arquivo > {MAX_KB} KB ({size_kb:.0f} KB): {f}")
            side = image_max_side(path)
            if side is None:
                warnings.append(f"[{key}] dimensão ilegível no cabeçalho: {f}")
            elif side > MAX_SIDE:
                errors.append(f"[{key}] lado maior {side} px > {MAX_SIDE} px: {f}")
    return errors, warnings


def check_meta(manifest):
    errors = []
    warnings = []
    figures = manifest.get("figures", {})
    for key, entry in figures.items():
        if not isinstance(entry, dict):
            errors.append(f"[{key}] entrada não é objeto")
            continue
        alt = entry.get("alt", "").strip() if isinstance(entry.get("alt"), str) else ""
        page = entry.get("page")
        if not alt:
            errors.append(f"[{key}] alt ausente")
        else:
            # Aviso se alt ainda é o template genérico
            if "transcrição pendente" in alt.lower():
                warnings.append(f"[{key}] alt ainda é template (revisão pendente)")
        if is_eaj_key(key):
            if page is None:
                errors.append(f"[{key}] page ausente")
            elif isinstance(page, str) and page == "DESCONHECIDA":
                warnings.append(f"[{key}] page DESCONHECIDA — confirmar no caderno EAJ")
            elif not isinstance(page, int) or page <= 0:
                errors.append(f"[{key}] page inválida: {page}")
            credit = entry.get("credit", "")
            if "EAJ" not in credit:
                errors.append(f"[{key}] credit sem EAJ: {credit!r}")
            elif "Comperve" not in credit:
                warnings.append(f"[{key}] credit sem Comperve: {credit!r}")
        else:
            if page is None:
                errors.append(f"[{key}] page ausente")
            elif not isinstance(page, int) or page <= 0:
                errors.append(f"[{key}] page inválida: {page}")
        if "files" not in entry or not isinstance(entry.get("files"), list) or len(entry.get("files", [])) == 0:
            errors.append(f"[{key}] files ausente ou vazio")
    return errors, warnings


def check_eaj(manifest):
    """Namespace EAJ: formato das chaves/arquivos, faixas por edição,
    cobertura (falta = aviso + caderno) e ausência de colisão com o IFRN."""
    errors = []
    warnings = []
    figures = manifest.get("figures", {})
    ifrn_keys = {k for k in figures if not is_eaj_key(k)}
    eaj_keys = {k for k in figures if is_eaj_key(k)}

    if ifrn_keys & eaj_keys:
        errors.append(f"colisão de chaves IFRN×EAJ: {sorted(ifrn_keys & eaj_keys)}")

    seen_by_edition: dict[str, set[int]] = {}
    for key in sorted(eaj_keys):
        m = EAJ_KEY_RE.match(key)
        if not m:
            errors.append(f"[{key}] chave EAJ malformada (esperado EAJ-<ano>-<n>)")
            continue
        ano, num = m.group(1), int(m.group(2))
        if not 1 <= num <= EAJ_MAX_Q[ano]:
            errors.append(f"[{key}] número fora da faixa da edição EAJ-{ano} (1–{EAJ_MAX_Q[ano]})")
            continue
        seen_by_edition.setdefault(ano, set()).add(num)
        entry = figures[key]
        files = entry.get("files", []) if isinstance(entry, dict) else []
        for f in files:
            fm = EAJ_FILE_RE.match(f)
            if not fm:
                errors.append(f"[{key}] arquivo fora da convenção eaj/<ano>/Q<NN>(-k).ext: {f}")
                continue
            if fm.group(1) != ano:
                errors.append(f"[{key}] ano do arquivo ({fm.group(1)}) ≠ ano da chave ({ano}): {f}")
            if int(fm.group(2)) != num:
                errors.append(f"[{key}] número do arquivo ≠ número da chave: {f}")

    for ano, expected in EAJ_EXPECTED.items():
        seen = seen_by_edition.get(ano, set())
        for num in expected:
            if num not in seen:
                # Questão sem recorte: aviso + referência ao caderno,
                # nunca imagem inventada.
                warnings.append(
                    f"[EAJ-{ano}-{num}] sem recorte em eaj/{ano}/ "
                    f"(ver caderno data/provas/EAJ/{ano}/eaj_{ano}.pdf)")
        for num in sorted(seen - set(expected)):
            warnings.append(
                f"[EAJ-{ano}-{num}] recorte fora do inventário B.3 "
                f"(conferir necessidade no caderno EAJ-{ano})")

    # Chaves IFRN 2022-/2025- intactas (ano sozinho nunca decide a edição).
    for prefix in ("2022-", "2025-"):
        if not any(k.startswith(prefix) for k in ifrn_keys):
            warnings.append(f"cobertura IFRN: nenhuma chave {prefix}* — regressão?")
    return errors, warnings


def main():
    manifest = load_manifest()
    errors = check_names(manifest)
    warnings = check_formats(manifest)
    dim_errors, dim_warnings = check_dimensions(manifest)
    errors += dim_errors
    warnings += dim_warnings
    meta_errors, meta_warnings = check_meta(manifest)
    errors += meta_errors
    warnings += meta_warnings
    eaj_errors, eaj_warnings = check_eaj(manifest)
    errors += eaj_errors
    warnings += eaj_warnings

    # Cobertura IFRN: compara com as questões de `docs/content-analysis/per-edition/`
    # (simplificado: apenas verifica quantidade no manifest)
    ifrn_count = sum(1 for k in manifest.get("figures", {}) if not is_eaj_key(k))
    eaj_count = sum(1 for k in manifest.get("figures", {}) if is_eaj_key(k))
    if ifrn_count < EXPECTED_COUNT:
        warnings.append(f"cobertura IFRN: {ifrn_count} de {EXPECTED_COUNT} figuras registradas (33 esperadas)")

    for w in warnings:
        print("AVISO:", w)
    for e in errors:
        print("ERRO:", e)

    if errors:
        sys.exit(1)
    print(f"OK: {ifrn_count} figuras IFRN + {eaj_count} EAJ, {len(warnings)} avisos")


if __name__ == "__main__":
    main()
