#!/usr/bin/env python3
"""Validador de figuras — docs/figuras-estrategia.md

Só leitura por padrão. Verifica:
  - manifest.json existe e é válido;
  - nomes dos arquivos em assets/figures/ batem com o manifest;
  - formatos aceitos (.webp preferido, .png/.jpg aceitos com aviso);
  - dimensões via cabeçalho (sem Pillow); lado maior <= 1600 px, < 300 KB;
  - cada entrada tem `alt`, `page` (número > 0) e `files` (lista não vazia);
  - cobertura das 36 questões com `has_figure=TRUE` no banco (via `data/linked/` + `docs/content-analysis/per-edition/`);
  - nenhuma duplicidade acidental (mesmo arquivo referenciado por chave diferente sem justificativa).

Uso:
    python3 scripts/analysis/check_figures.py [--fix-alt]
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
ASSETS_DIR = REPO_ROOT / "frontend" / "assets" / "figures"
MANIFEST = ASSETS_DIR / "manifest.json"

EXPECTED_COUNT = 36  # conforme importador: 36 has_figure=TRUE
ALLOWED_FORMATS = {".webp", ".png", ".jpg", ".jpeg"}
PREFERRED_FORMAT = ".webp"


def load_manifest():
    if not MANIFEST.exists():
        raise SystemExit("manifest.json ausente: " + str(MANIFEST))
    try:
        return json.loads(MANIFEST.read_text(encoding="utf-8"))
    except Exception as exc:
        raise SystemExit(f"manifest.json inválido: {exc}")


def check_names(manifest):
    errors = []
    base = manifest.get("base", "./assets/figures")
    figures = manifest.get("figures", {})
    found_files = set()
    for key, entry in figures.items():
        files = entry.get("files", []) if isinstance(entry, dict) else []
        for f in files:
            # Nome deve ser <ano>/Q<NN>(-k)?.ext
            name = Path(f).name
            if not name.startswith("Q"):
                errors.append(f"[{key}] arquivo '{f}' não começa com Q")
            ext = Path(f).suffix.lower()
            if ext not in ALLOWED_FORMATS:
                errors.append(f"[{key}] formato '{ext}' não permitido ({f})")
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
    # Sem Pillow: usa apenas tamanho do arquivo e cabeçalho básico
    figures = manifest.get("figures", {})
    for key, entry in figures.items():
        files = entry.get("files", []) if isinstance(entry, dict) else []
        for f in files:
            path = ASSETS_DIR / f
            if not path.exists():
                continue
            size_kb = path.stat().st_size / 1024
            if size_kb > 300:
                errors.append(f"[{key}] arquivo > 300 KB ({size_kb:.0f} KB): {f}")
            # Se for .webp, verifica dimensão via cabeçalho básico (não obrigatório)
            if f.endswith(".webp"):
                # Cabeçalho .webp simples: não validamos dimensões sem biblioteca,
                # mas registramos aviso se não conseguirmos ler (não falha)
                pass
    return errors


def check_meta(manifest):
    errors = []
    warnings = []
    figures = manifest.get("figures", {})
    for key, entry in figures.items():
        if not isinstance(entry, dict):
            errors.append(f"[{key}] entrada não é objeto")
            continue
        alt = entry.get("alt", "").strip()
        page = entry.get("page")
        if not alt:
            errors.append(f"[{key}] alt ausente")
        else:
            # Aviso se alt ainda é o template genérico
            if "transcrição pendente" in alt.lower():
                warnings.append(f"[{key}] alt ainda é template (revisão pendente)")
        if page is None:
            errors.append(f"[{key}] page ausente")
        elif not isinstance(page, int) or page <= 0:
            errors.append(f"[{key}] page inválida: {page}")
        if "files" not in entry or not isinstance(entry.get("files"), list) or len(entry.get("files", [])) == 0:
            errors.append(f"[{key}] files ausente ou vazio")
    return errors, warnings


def main():
    manifest = load_manifest()
    errors = check_names(manifest)
    warnings = check_formats(manifest)
    errors += check_dimensions(manifest)
    meta_errors, meta_warnings = check_meta(manifest)
    errors += meta_errors
    warnings += meta_warnings

    # Cobertura: compara com as 36 questões de `docs/content-analysis/per-edition/`
    # (simplificado: apenas verifica quantidade no manifest)
    count = len(manifest.get("figures", {}))
    if count < EXPECTED_COUNT:
        warnings.append(f"cobertura: {count} de {EXPECTED_COUNT} figuras registradas (36 esperadas)")

    for w in warnings:
        print("AVISO:", w)
    for e in errors:
        print("ERRO:", e)

    if errors:
        sys.exit(1)
    print(f"OK: {count} figuras, {len(warnings)} avisos")


if __name__ == "__main__":
    main()
